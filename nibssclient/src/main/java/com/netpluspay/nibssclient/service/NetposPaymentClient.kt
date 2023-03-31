package com.netpluspay.nibssclient.service

import android.content.Context
import android.text.format.DateUtils
import android.widget.Toast
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.danbamitale.epmslib.entities.*
import com.danbamitale.epmslib.entities.CardData
import com.danbamitale.epmslib.entities.KeyHolder
import com.danbamitale.epmslib.entities.OriginalDataElements
import com.danbamitale.epmslib.entities.TransactionResponse
import com.danbamitale.epmslib.entities.TransactionType
import com.danbamitale.epmslib.processors.TerminalConfigurator
import com.danbamitale.epmslib.processors.TransactionProcessor
import com.danbamitale.epmslib.utils.IsoAccountType
import com.danbamitale.epmslib.utils.MessageReasonCode
import com.google.gson.Gson
import com.isw.gateway.TransactionProcessorWrapper
import com.isw.iswclient.iswapiclient.getTokenClient
import com.isw.iswclient.request.IswParameters
import com.isw.iswclient.request.TokenPassportRequest
import com.netpluspay.nibssclient.dao.TransactionResponseDao
import com.netpluspay.nibssclient.dao.TransactionTrackingTableDao
import com.netpluspay.nibssclient.database.AppDatabase
import com.netpluspay.nibssclient.models.*
import com.netpluspay.nibssclient.network.RrnApiService
import com.netpluspay.nibssclient.network.StormApiClient
import com.netpluspay.nibssclient.network.StormApiService
import com.netpluspay.nibssclient.util.ConnectionErrorConstants.isConnectionError
import com.netpluspay.nibssclient.util.Constants.ISW_TOKEN
import com.netpluspay.nibssclient.util.Constants.LAST_POS_CONFIGURATION_TIME
import com.netpluspay.nibssclient.util.Constants.PREF_CONFIG_DATA
import com.netpluspay.nibssclient.util.Constants.PREF_KEYHOLDER
import com.netpluspay.nibssclient.util.Constants.TOKEN_RESPONSE_TAG
import com.netpluspay.nibssclient.util.Mapper.mapToAccountBalanceResponse
import com.netpluspay.nibssclient.util.RandomNumUtil.mapDanbamitaleResponseToResponseWithRrn
import com.netpluspay.nibssclient.util.ResponseCodeWarrantingForReversalConstants.doesResponseCodeWarrantsReversal
import com.netpluspay.nibssclient.util.ResponseCodeWarrantingForReversalConstants.wasTransactionCompletedPartially
import com.netpluspay.nibssclient.util.SharedPrefManager
import com.netpluspay.nibssclient.util.SharedPrefManager.getUserData
import com.netpluspay.nibssclient.util.Singletons
import com.netpluspay.nibssclient.util.Singletons.getKeyHolder
import com.netpluspay.nibssclient.util.Singletons.getSavedConfigurationData
import com.netpluspay.nibssclient.util.Singletons.setConfigData
import com.netpluspay.nibssclient.util.Utility
import com.netpluspay.nibssclient.util.Utility.getTransactionResponseToLog
import com.netpluspay.nibssclient.util.alerts.Alerter.showToast
import com.netpluspay.nibssclient.work.ModelObjects
import com.netpluspay.nibssclient.work.ModelObjects.disposeWith
import com.netpluspay.nibssclient.work.ModelObjects.mapToTransactionResponseX
import com.netpluspay.nibssclient.work.RepushFailedTransactionToBackendWorker
import com.pixplicity.easyprefs.library.Prefs
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import timber.log.Timber
import java.util.concurrent.TimeUnit

object NetposPaymentClient {
    private var transactionResponseDao: TransactionResponseDao? = null
    private lateinit var workManager: WorkManager
    private val gson = Gson()
    private var amountDbl = 0.0
    private var amountLong = 0L
    private var transResp: TransactionResponse? = null
    private val stormApiService: StormApiService = StormApiClient.getStormApiLoginInstance()
    private val rrnApiService: RrnApiService = StormApiClient.getRrnServiceInstance()
    private var configurationData: ConfigurationData = getSavedConfigurationData()
    private var user: User? = null
    private var connectionData: ConnectionData = ConnectionData(
        ipAddress = configurationData.ip,
        ipPort = configurationData.port.toInt(),
        isSSL = true,
    )
    private var terminalId: String? = null
    private var currentlyLoggedInUser: UserData? = null
    private var terminalConfigurator: TerminalConfigurator =
        TerminalConfigurator(connectionData)

    private fun getTerminalId() = terminalId ?: ""
    private fun setCurrentlyLoggedInUser() {
        currentlyLoggedInUser = getUserData()
    }

    private var iswPaymentProcessorObject: TransactionProcessorWrapper? = null
    private val compositeDisposable: CompositeDisposable by lazy { CompositeDisposable() }

    /**
     * Registers the parameters of the user and the terminal
     * @param context: Context
     * @param serializedUserData: String
     * Example user data is as follows: UserData(
     businessName = "Netplus",
     partnerName = "Netplus",
     partnerId = "5de231d9-1be0-4c31-8658-6e15892f2b83",
     terminalId = "2033ALZP",
     terminalSerialNumber = "1234556789", // This is just a placeholder, Kindly replace with yours
     businessAddress = "Marwa Lagos",
     customerName = "Test Account"
     )
     *
     * @return Unit
     * */
    fun logUser(context: Context, serializedUserData: String) {
        val userData = Gson().fromJson(serializedUserData, UserData::class.java)
        if (userData !is UserData) {
            showToast("Invalid UserData", context)
            return
        }
        SharedPrefManager.setUserData(userData)
    }

    private fun setTerminalId() {
        terminalId = getUserData().terminalId
    }

    private fun validateField(amount: Long) {
        amountDbl = amount.toDouble()
        this.amountLong = amountDbl.toLong()
    }

    private var keyHolder: KeyHolder? = null
    private var configData: ConfigData? = null

    /**
     * Configures the terminal and sets up other neccessary data
     * @param context: Context
     * @param configureSilently: Boolean
     * @param serializedUserData: String
     * @return Single<Pair<KeyHolder?, ConfigData?>> Single of pair of Keyholder and configData objects
     * */
    fun init(
        context: Context,
        serializedUserData: String,
    ): Single<Pair<KeyHolder?, ConfigData?>> {
        // Repush failed transactions back to server
        workManager = WorkManager.getInstance(context.applicationContext)
        repushTransactionsToBackend()
        user = Singletons.getCurrentlyLoggedInUser()
        logUser(context, serializedUserData)
        setCurrentlyLoggedInUser()
        setTerminalId()

        if (getUserData().mid.isNotEmpty() && getUserData().bankAccountNumber.isNotEmpty() && getUserData().terminalSerialNumber.isNotEmpty()) {
            setUpIswToken()
        }

        KeyHolder.setHostKeyComponents(
            configurationData.key1,
            configurationData.key2,
        ) // default to test  //Set your base keys here
        keyHolder = getKeyHolder()
        configData = Singletons.getConfigData()
        val req = when {
            DateUtils.isToday(Prefs.getLong(LAST_POS_CONFIGURATION_TIME, 0)).not() -> {
                configureTerminal(context)
            }
            keyHolder != null && configData != null -> {
                callHome(context).onErrorResumeNext {
                    configureTerminal(context)
                }
            }
            else -> configureTerminal(context)
        }
        return req
    }

    private fun callHome(context: Context): Single<Pair<KeyHolder?, ConfigData?>> {
        Timber.e(keyHolder.toString())
        return terminalConfigurator.nibssCallHome(
            context,
            getTerminalId(),
            keyHolder?.clearSessionKey ?: "",
            currentlyLoggedInUser!!.terminalSerialNumber,
        ).flatMap {
            Timber.e("call home result $it")
            if (it == "00") {
                return@flatMap Single.just(Pair(null, null))
            } else {
                Single.error(Exception("call home failed"))
            }
        }
    }

    /**
     * Calls Home to refresh the session key
     * Can be called after at an interval of 5 - 10 minutes or if the app has been idle for a while
     *
     * @param context
     * @param terminalId
     * @param keyHolderClearSessionKey
     * @param terminalSerialNumber
     * @return String, which when successful is "00" else, call the configure terminal to get a new session key
     * */
    fun callHomeToRefreshSessionKeys(
        context: Context,
        terminalId: String,
        keyHolderClearSessionKey: String,
        terminalSerialNumber: String,
    ): Single<String> = terminalConfigurator.nibssCallHome(
        context,
        terminalId,
        keyHolderClearSessionKey,
        terminalSerialNumber,
    )

    private fun configureTerminal(context: Context): Single<Pair<KeyHolder?, ConfigData?>> {
        return terminalConfigurator.downloadNibssKeys(context, getTerminalId())
            .flatMap { nibssKeyHolder ->
                var resp: Single<Pair<KeyHolder?, ConfigData?>> = Single.just(Pair(null, null))
                if (nibssKeyHolder.isValid) {
                    keyHolder = nibssKeyHolder
                    Prefs.putLong(LAST_POS_CONFIGURATION_TIME, System.currentTimeMillis())
                    Prefs.putString(PREF_KEYHOLDER, gson.toJson(nibssKeyHolder))
                    return@flatMap terminalConfigurator.downloadTerminalParameters(
                        context,
                        getTerminalId(),
                        nibssKeyHolder.clearSessionKey,
                        currentlyLoggedInUser!!.terminalSerialNumber,
                    ).map { nibssConfigData ->
                        setConfigData(nibssConfigData)
                        configData = nibssConfigData
                        Prefs.putString(PREF_CONFIG_DATA, gson.toJson(nibssConfigData))
                        Timber.e("Config data set")
                        return@map Pair(nibssKeyHolder, nibssConfigData)
                    }
                } else {
                    Single.just(Pair(null, null))
                }
            }
    }

    private fun repushTransactionsToBackend() {
        val workRequest = PeriodicWorkRequestBuilder<RepushFailedTransactionToBackendWorker>(
            15,
            TimeUnit.MINUTES,
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(
                    NetworkType.CONNECTED,
                ).build(),
        ).build()
        workManager.enqueue(workRequest)
    }

    fun makePayment(
        context: Context,
        terminalId: String? = "",
        makePaymentParams: String,
        cardScheme: String,
        cardHolder: String,
        remark: String,
        rrn: String = "",
        stan: String = "",
    ): Single<TransactionWithRemark?> {
        return if (getUserData().bankAccountNumber.isEmpty() || getUserData().terminalSerialNumber.isEmpty() || getUserData().mid.isEmpty()) {
            makePaymentNibss(context, terminalId, makePaymentParams, cardScheme, cardHolder, remark)
        } else {
            makePaymentIsw(context, terminalId, makePaymentParams, cardScheme, cardHolder, remark)
        }
    }

    private fun makePaymentNibss(
        context: Context,
        terminalId: String? = "",
        makePaymentParams: String,
        cardScheme: String,
        cardHolder: String,
        remark: String,
        rrn: String = "",
        stan: String = "",
    ): Single<TransactionWithRemark?> {
        transactionResponseDao = AppDatabase.getDatabaseInstance(context).transactionResponseDao()
        val transactionTrackingTableDao =
            AppDatabase.getDatabaseInstance(context).transactionTrackingTableDao()
        val params = gson.fromJson(makePaymentParams, MakePaymentParams::class.java)
        validateField(params.amount)

        return if (rrn.isNotEmpty() && rrn.length >= 12) {
            makePaymentViaNibss(
                context,
                terminalId,
                makePaymentParams,
                cardScheme,
                cardHolder,
                remark,
                transactionTrackingTableDao,
                rrn,
                if (stan.isNotEmpty()) stan else null,
            )
        } else {
            getRRn()
                .onErrorResumeNext(Single.just(Response.success(Utility.getCustomRrn())))
                .flatMap {
                    makePaymentViaNibss(
                        context,
                        terminalId,
                        makePaymentParams,
                        cardScheme,
                        cardHolder,
                        remark,
                        transactionTrackingTableDao,
                        it.body(),
                        if (stan.isNotEmpty()) stan else null,
                    )
                }
        }
    }

    private fun makePaymentIsw(
        context: Context,
        terminalId: String? = "",
        makePaymentParams: String,
        cardScheme: String,
        cardHolder: String,
        remark: String,
        rrn: String = "",
        stan: String = "",
    ): Single<TransactionWithRemark?> {
        transactionResponseDao = AppDatabase.getDatabaseInstance(context).transactionResponseDao()
        val transactionTrackingTableDao =
            AppDatabase.getDatabaseInstance(context).transactionTrackingTableDao()
        val params = gson.fromJson(makePaymentParams, MakePaymentParams::class.java)
        validateField(params.amount)

        val bankAccNumber = getUserData().bankAccountNumber
        val mId = getUserData().mid
        val institutionalCode = getUserData().institutionalCode

        val interSwitchObject =
            GetPartnerInterSwitchThresholdResponse(0, bankAccNumber, institutionalCode)
        val interSwitchObjectInString = gson.toJson(interSwitchObject)

        val iswToken = Prefs.getString(getUserData().partnerId + ISW_TOKEN, "")

        return if (rrn.isNotEmpty()) {
            abstractedCheckForIswThresholdToProcessIswPayment(
                context,
                terminalId,
                makePaymentParams,
                cardScheme,
                cardHolder,
                remark,
                if (stan.isNotEmpty()) stan else null,
                interSwitchObjectInString,
                iswToken,
                transactionTrackingTableDao,
                rrn,
                mId,
            )
        } else {
            return getRRn()
                .onErrorResumeNext(Single.just(Response.success(Utility.getCustomRrn())))
                .flatMap flatMapB@{
                    val rrnFromBackend = it.body()
                    return@flatMapB abstractedCheckForIswThresholdToProcessIswPayment(
                        context,
                        terminalId,
                        makePaymentParams,
                        cardScheme,
                        cardHolder,
                        remark,
                        if (stan.isNotEmpty()) stan else null,
                        interSwitchObjectInString,
                        iswToken,
                        transactionTrackingTableDao,
                        rrnFromBackend ?: "",
                        mId,
                    )
                }
        }
    }

    private fun abstractedCheckForIswThresholdToProcessIswPayment(
        context: Context,
        terminalId: String? = "",
        makePaymentParams: String,
        cardScheme: String,
        cardHolder: String,
        remark: String,
        stan: String?,
        interSwitchObject: String,
        iswToken: String,
        transactionTrackingTableDao: TransactionTrackingTableDao,
        rrn: String,
        mId: String,
    ): Single<TransactionWithRemark?> =
        if (iswToken.isNotEmpty()) {
            processTransactionViaInterSwitchMakePayment(
                context,
                terminalId,
                makePaymentParams,
                cardScheme,
                cardHolder,
                remark,
                transactionTrackingTableDao,
                rrn,
                interSwitchObject,
                iswToken,
                stan,
                mId,
            )
        } else {
            getIswTokenAtRuntime()
                .flatMap {
                    return@flatMap processTransactionViaInterSwitchMakePayment(
                        context,
                        terminalId,
                        makePaymentParams,
                        cardScheme,
                        cardHolder,
                        remark,
                        transactionTrackingTableDao,
                        rrn,
                        interSwitchObject,
                        it,
                        stan,
                        mId,
                    )
                }
        }

    private fun processTransactionViaInterSwitchMakePayment(
        context: Context,
        terminalId: String? = "",
        makePaymentParams: String,
        cardScheme: String,
        cardHolder: String,
        remark: String,
        transactionTrackingTableDao: TransactionTrackingTableDao,
        rrn: String?,
        interSwitchObject: String,
        iswToken: String,
        stan: String?,
        mId: String,
    ): Single<TransactionWithRemark?> {
        val params = gson.fromJson(makePaymentParams, MakePaymentParams::class.java)
        // IsoAccountType.
        this.amountLong = amountDbl.toLong()

        val requestData =
            TransactionRequestData(
                TransactionType.PURCHASE,
                amountLong,
                0L,
                accountType = IsoAccountType.valueOf(params.accountType.name),
                RRN = rrn,
                STAN = stan,
            )

        val destinationAcc = gson.fromJson(
            interSwitchObject,
            GetPartnerInterSwitchThresholdResponse::class.java,
        ).bankAccountNumber

        if (destinationAcc.isEmpty()) {
            Toast.makeText(context, "No destination account found", Toast.LENGTH_LONG).show()
        }

        val iswParam = IswParameters(
            mId,
            user?.business_address ?: "",
            token = iswToken,
            "",
            terminalId = getUserData().terminalId,
            terminalSerial = getUserData().terminalSerialNumber,
            destinationAcc,
        )

        requestData.iswParameters = iswParam

        iswPaymentProcessorObject =
            TransactionProcessorWrapper(
                mId,
                getUserData().terminalId,
                requestData.amount,
                transactionRequestData = requestData,
                keyHolder = null,
                configData = null,
            )

        val transactionToLog = params.getTransactionResponseToLog(cardScheme, requestData, rrn)

        return abstractedPaymentWithIswImplementation(
            context,
            transactionToLog,
            params,
            cardScheme,
            cardHolder,
            remark,
            transactionTrackingTableDao,
            rrn,
            requestData,
        ).flatMap {
            Single.just(it)
        }
    }

    private fun abstractedPaymentWithIswImplementation(
        context: Context,
        transactionToLog: TransactionToLogBeforeConnectingToNibbs,
        params: MakePaymentParams,
        cardScheme: String,
        cardHolder: String,
        remark: String,
        transactionTrackingTableDao: TransactionTrackingTableDao,
        rrn: String?,
        requestData: TransactionRequestData,
    ): Single<TransactionWithRemark> {
        return logTransactionToBackEndBeforeMakingPayment(transactionToLog)
            .doOnError {
                Toast.makeText(
                    context,
                    "An error occured, please try again later",
                    Toast.LENGTH_LONG,
                ).show()
                Timber.d("ERROR_FROM_LOGGING_TO_SERVER=====>%s", it.localizedMessage)
                return@doOnError
            }.flatMap {
                params.cardData.let { cardData ->
                    iswPaymentProcessorObject!!.processIswTransaction(cardData)
                        .flatMap labelCheckForReversal@{ transRes ->
                            return@labelCheckForReversal when {
                                isConnectionError(transRes.responseMessage) ->
                                    iswPaymentProcessorObject!!.rollback(
                                        context,
                                        MessageReasonCode.Timeout,
                                    )
                                wasTransactionCompletedPartially(transRes.responseCode) -> iswPaymentProcessorObject!!.rollback(
                                    context,
                                    MessageReasonCode.CompletedPartially,
                                )
                                doesResponseCodeWarrantsReversal(transRes.responseCode) -> iswPaymentProcessorObject!!.rollback(
                                    context,
                                    MessageReasonCode.UnSpecified,
                                )
                                else ->
                                    Single.just(transRes)
                            }
                        }
                        .flatMap {
                            transResp = it
                            if (it.responseCode == "A3") {
                                Prefs.remove(PREF_CONFIG_DATA)
                                Prefs.remove(PREF_KEYHOLDER)
                                configureTerminal(context)
                            }

                            it.cardHolder = cardHolder
                            it.cardLabel = cardScheme
                            it.amount = requestData.amount
                            val message =
                                (if (it.responseCode == "00") "Transaction Approved" else "Transaction Not approved")
                            Timber.d("RESPONSE=>$it")
                            showToast(message, context)
                            transactionResponseDao?.insertNewTransaction(it)?.flatMap { _ ->
                                handleTransactionUpdateAndReturnSingle(
                                    it,
                                    transactionToLog,
                                    rrn ?: "",
                                    remark,
                                    transactionTrackingTableDao,
                                )
                            } ?: run {
                                handleTransactionUpdateAndReturnSingle(
                                    it,
                                    transactionToLog,
                                    rrn ?: "",
                                    remark,
                                    transactionTrackingTableDao,
                                )
                            }
                        }
                }
            }
    }

    private fun handleTransactionUpdateAndReturnSingle(
        it: TransactionResponse,
        transactionToLog: TransactionToLogBeforeConnectingToNibbs,
        rrn: String,
        remark: String,
        transactionTrackingTableDao: TransactionTrackingTableDao,
    ): Single<TransactionWithRemark> {
        if (it.responseCode == "00") {
            updateTransactionInBackendAfterMakingPayment(
                transactionToLog.transactionResponse.rrn,
                mapDanbamitaleResponseToResponseWithRrn(it, remark, rrn),
                "APPROVED",
                transactionTrackingTableDao,
            )
        } else {
            updateTransactionInBackendAfterMakingPayment(
                rrn = transactionToLog.transactionResponse.rrn,
                transactionResponse = mapDanbamitaleResponseToResponseWithRrn(
                    it,
                    remark = remark,
                    rrn,
                ),
                status = it.responseMessage,
                transactionTrackingTableDao,
            )
        }

        return Single.just(mapDanbamitaleResponseToResponseWithRrn(it, remark, rrn))
    }

    // Make Payment Via Nibss Correct Implementation
    private fun makePaymentViaNibss(
        context: Context,
        terminalId: String? = "",
        makePaymentParams: String,
        cardScheme: String,
        cardHolder: String,
        remark: String,
        transactionTrackingTableDao: TransactionTrackingTableDao,
        rrn: String?,
        stan: String?,
    ): Single<TransactionWithRemark?> {
        val params = gson.fromJson(makePaymentParams, MakePaymentParams::class.java)
        validateField(params.amount)
        val configData: ConfigData = Singletons.getConfigData() ?: run {
            showToast(
                "Terminal has not been configured, restart the application to configure",
                context,
            )
            return Single.just(null)
        }
        val keyHolder: KeyHolder =
            getKeyHolder() ?: run {
                showToast(
                    "Terminal has not been configured, restart the application to configure",
                    context,
                )
                return Single.just(null)
            }

        val hostConfig = HostConfig(
            if (terminalId.isNullOrEmpty()) getUserData().terminalId else terminalId,
            connectionData,
            keyHolder,
            configData,
        )

        // IsoAccountType.
        this.amountLong = amountDbl.toLong()
        val originalDataElements =
            OriginalDataElements(
                originalTransactionType = TransactionType.PURCHASE,
                originalRRN = rrn?.takeLast(12) ?: "",
                originalSTAN = stan?.takeLast(6) ?: "",
            )
        val requestData =
            TransactionRequestData(
                TransactionType.PURCHASE,
                amountLong,
                0L,
                accountType = IsoAccountType.parseStringAccountType(params.accountType.name),
                RRN = rrn?.takeLast(12),
                originalDataElements = originalDataElements,
                STAN = stan?.takeLast(6),
            )

        val transactionToLog = params.getTransactionResponseToLog(cardScheme, requestData, rrn)

        val processor = TransactionProcessor(hostConfig)

        // Send to backend first
        return logTransactionToBackEndBeforeMakingPayment(transactionToLog)
            .doOnError {
                Toast.makeText(
                    context,
                    "An error occured, please try again later",
                    Toast.LENGTH_LONG,
                ).show()
                Timber.d("ERROR_FROM_FIRST_LOGGING_TO_SERVER=====>%s", it.localizedMessage)
                return@doOnError
            }
            .flatMap {
                Timber.d("SUCCESSFULLY_LOGGED_TO_BACKEND=====>%s", gson.toJson(it))
                processor.processTransaction(context, requestData, params.cardData)
                    .onErrorResumeNext {
                        processor.rollback(
                            context,
                            MessageReasonCode.Timeout,
                        )
                    }
                    .flatMap labelCheckForReversal@{ transRes ->
                        return@labelCheckForReversal when {
                            isConnectionError(transRes.responseMessage) ->
                                processor.rollback(context, MessageReasonCode.Timeout)
                            wasTransactionCompletedPartially(transRes.responseCode) -> processor.rollback(
                                context,
                                MessageReasonCode.CompletedPartially,
                            )
                            doesResponseCodeWarrantsReversal(transRes.responseCode) -> processor.rollback(
                                context,
                                MessageReasonCode.UnSpecified,
                            )
                            else ->
                                Single.just(transRes)
                        }
                    }
                    .flatMap { transResponse ->
                        updateTransactionInBackendAfterMakingPayment(
                            transResponse.RRN,
                            mapDanbamitaleResponseToResponseWithRrn(
                                transResponse,
                                remark,
                                transResponse.RRN,
                            ),
                            if (transResponse.responseCode == "00") "APPROVED" else transResponse.responseMessage,
                            transactionTrackingTableDao,
                        )
                        Timber.d(
                            "PAYMENT_DONE_SUCCESSFULLY=====>%s",
                            gson.toJson(transResponse),
                        )
                        transResp = transResponse
                        if (transResponse.responseCode == "A3") {
                            Prefs.remove(PREF_CONFIG_DATA)
                            Prefs.remove(PREF_KEYHOLDER)
                            configureTerminal(context)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe { data, _ ->
                                    data?.let { configResult ->
                                        Prefs.putString(
                                            PREF_KEYHOLDER,
                                            gson.toJson(configResult.first),
                                        )
                                        Prefs.putString(
                                            PREF_CONFIG_DATA,
                                            gson.toJson(configResult.second),
                                        )
                                    }
                                }.disposeWith(compositeDisposable)
                        }

                        transResponse.cardHolder = cardHolder
                        transResponse.cardLabel = cardScheme
                        transResponse.amount = requestData.amount
                        val message =
                            (if (transResponse.responseCode == "00") "Transaction Approved" else "Transaction Not approved")
                        Timber.d("RESPONSE=>$transResponse")
                        showToast(message, context)
                        transactionResponseDao?.insertNewTransaction(transResponse)?.flatMap {
                            Single.just(
                                mapDanbamitaleResponseToResponseWithRrn(
                                    transResponse,
                                    remark,
                                    rrn,
                                ),
                            )
                        } ?: run {
                            Single.just(
                                mapDanbamitaleResponseToResponseWithRrn(
                                    transResponse,
                                    remark,
                                    rrn,
                                ),
                            )
                        }
                    }
            }
    }

    private fun logTransactionToBackEndBeforeMakingPayment(dataToLog: TransactionToLogBeforeConnectingToNibbs): Single<ResponseBodyAfterLoginToBackend> {
        return stormApiService.logTransactionBeforeMakingPayment(dataToLog)
    }

    private fun updateTransactionInBackendAfterMakingPayment(
        rrn: String,
        transactionResponse: TransactionWithRemark,
        status: String,
        transactionTrackingTableDao: TransactionTrackingTableDao,
    ) {
        val dataToLog = DataToLogAfterConnectingToNibss(status, transactionResponse, rrn)
        stormApiService.updateLogAfterConnectingToNibss2(rrn, dataToLog)
            .subscribeOn(Schedulers.io())
            .doOnError {
                saveTransactionForTracking(
                    ModelObjects.TransactionResponseXForTracking(
                        dataToLog.rrn,
                        mapToTransactionResponseX(mapToTransactionResponse(dataToLog.transactionResponse)),
                        dataToLog.status,
                    ),
                    transactionTrackingTableDao,
                )
                Timber.d("ERROR_SAVING_TRANS_FOR_TRACKING==>${it.localizedMessage}")
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { data, error ->
                data?.let {
                    if (it.message()
                            .contains("There is an error") || it.code() == 404 || it.code() == 500 || it.code() in 400..500
                    ) {
                        saveTransactionForTracking(
                            ModelObjects.TransactionResponseXForTracking(
                                dataToLog.rrn,
                                mapToTransactionResponseX(mapToTransactionResponse(dataToLog.transactionResponse)),
                                dataToLog.status,
                            ),
                            transactionTrackingTableDao,
                        )
                    }
                }
                error?.let {
                }
            }.disposeWith(compositeDisposable)
    }

    private fun saveTransactionForTracking(
        transactionResponse: ModelObjects.TransactionResponseXForTracking,
        transactionTrackingTableDao: TransactionTrackingTableDao,
    ) {
        val disposable = CompositeDisposable()
        transactionTrackingTableDao.insertTransactionForTracking(transactionResponse)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { t1, t2 ->
                t1?.let {
                    Timber.d("SUCCESS_SAVING_FOR_TRACKING=====>%s", it.toString())
                }
                t2?.let {
                    Timber.d("ERROR_SAVING_FOR_TRACKING=====>%s", it.localizedMessage)
                }
            }.disposeWith(disposable)
    }

    private fun setUpIswToken() {
        val req = TokenPassportRequest(getUserData().mid, getUserData().terminalId)
        getTokenClient.getToken(req)
            .doOnError {
                Timber.d("$TOKEN_RESPONSE_TAG${it.localizedMessage}")
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { t1, t2 ->
                t1?.let {
                    Timber.d("$TOKEN_RESPONSE_TAG${it.token}")
                    Prefs.putString(getUserData().partnerId + TOKEN_RESPONSE_TAG, it.token)
                }
                t2?.let {
                }
            }.disposeWith(compositeDisposable)
    }

    private fun getIswTokenAtRuntime(): Single<String> {
        val req = TokenPassportRequest(getUserData().mid, getUserData().terminalId)
        return getTokenClient.getToken(req)
            .doOnError {
                Timber.d("$TOKEN_RESPONSE_TAG${it.localizedMessage}")
                return@doOnError
            }
            .flatMap {
                Timber.d("$TOKEN_RESPONSE_TAG${it.token}")
                Prefs.putString(getUserData().partnerId + TOKEN_RESPONSE_TAG, it.token)
                Single.just(it.token)
            }
    }

    private fun getRRn() =
        rrnApiService.getRrn()

    /**
     * Checks account balance
     * @param context
     * @param cardData An object of card data
     * @param accountType
     * @return CheckAccountBalanceResponse
     * */
    fun balanceEnquiry(
        context: Context,
        cardData: CardData,
        accountType: String,
    ): Single<CheckAccountBalanceResponse> =
        if (getUserData().mid.isNotEmpty() && getUserData().bankAccountNumber.isNotEmpty() && getUserData().terminalSerialNumber.isNotEmpty()) {
            val iswToken = Prefs.getString(getUserData().partnerId + TOKEN_RESPONSE_TAG, "")
            if (iswToken.isNotEmpty()) {
                balanceEnquiryIsw(
                    cardData,
                    accountType,
                    getUserData().mid,
                    iswToken,
                    getUserData().bankAccountNumber,
                )
            } else {
                getIswTokenAtRuntime().flatMap {
                    balanceEnquiryIsw(
                        cardData,
                        accountType,
                        getUserData().mid,
                        it,
                        getUserData().bankAccountNumber,
                    )
                }
            }
        } else {
            balanceEnquiryNibss(context, cardData, accountType)
        }

    private fun balanceEnquiryNibss(
        context: Context,
        cardData: CardData,
        accountType: String,
    ): Single<CheckAccountBalanceResponse> {
        val transactionType = TransactionType.BALANCE
        val configData: ConfigData = Singletons.getConfigData() ?: kotlin.run {
            showToast(
                "Terminal has not been configured, restart the application to configure",
                context,
            )
            return Single.just(null)
        }
        val keyHolder: KeyHolder =
            getKeyHolder() ?: kotlin.run {
                showToast(
                    "Terminal has not been configured, restart the application to configure",
                    context,
                )
                return Single.just(null)
            }

        val hostConfig = HostConfig(
            getUserData().terminalId,
            connectionData,
            keyHolder,
            configData,
        )

        val requestData =
            TransactionRequestData(
                transactionType,
                amount = 0L,
                accountType = IsoAccountType.parseStringAccountType(accountType),
            )

        val processor = TransactionProcessor(hostConfig)

        return processor.processTransaction(context, requestData, cardData)
            .flatMap {
                val accountBalance =
                    it.accountBalances.map { accountBalance -> accountBalance.mapToAccountBalanceResponse() }
                val response =
                    CheckAccountBalanceResponse(it.responseCode, it.responseMessage, accountBalance)

                Single.just(response)
            }
    }

    private fun balanceEnquiryIsw(
        cardData: CardData,
        accountType: String,
        mId: String,
        iswToken: String,
        destinationAcc: String,
    ): Single<CheckAccountBalanceResponse> {
        val requestData =
            TransactionRequestData(
                TransactionType.BALANCE,
                amount = 15L,
                accountType = IsoAccountType.parseStringAccountType(accountType),
            )

        val iswParam = IswParameters(
            mId,
            getUserData().businessAddress,
            token = iswToken,
            "",
            terminalId = getUserData().terminalId,
            terminalSerial = getUserData().terminalSerialNumber,
            destinationAcc,
        )

        requestData.iswParameters = iswParam

        iswPaymentProcessorObject =
            TransactionProcessorWrapper(
                mId,
                getUserData().terminalId,
                requestData.amount,
                transactionRequestData = requestData,
                keyHolder = null,
                configData = null,
            )

        return iswPaymentProcessorObject!!.processIswTransaction(cardData)
            .flatMap {
                val accountBalance =
                    it.accountBalances.map { accountBalance -> accountBalance.mapToAccountBalanceResponse() }
                val response =
                    CheckAccountBalanceResponse(it.responseCode, it.responseMessage, accountBalance)

                Single.just(response)
            }
    }

    /**
     This method should be called on the ondestroy method of the lifecycle owner (Fragment or activity) where this class, NetposPaymentClient, is used
     * */
    fun finish() {
        compositeDisposable.clear()
        transactionResponseDao = null
    }
}

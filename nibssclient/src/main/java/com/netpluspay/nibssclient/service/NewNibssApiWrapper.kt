package com.netpluspay.nibssclient.service

import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.format.DateUtils
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.danbamitale.epmslib.entities.*
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
import com.netpluspay.nibssclient.R
import com.netpluspay.nibssclient.dao.TransactionResponseDao
import com.netpluspay.nibssclient.dao.TransactionTrackingTableDao
import com.netpluspay.nibssclient.database.AppDatabase
import com.netpluspay.nibssclient.models.*
import com.netpluspay.nibssclient.network.RrnApiService
import com.netpluspay.nibssclient.network.StormApiClient
import com.netpluspay.nibssclient.network.StormApiService
import com.netpluspay.nibssclient.util.Constants.ISW_TOKEN
import com.netpluspay.nibssclient.util.Constants.LAST_POS_CONFIGURATION_TIME
import com.netpluspay.nibssclient.util.Constants.PREF_CONFIG_DATA
import com.netpluspay.nibssclient.util.Constants.PREF_KEYHOLDER
import com.netpluspay.nibssclient.util.RandomNumUtil.generateRandomRrn
import com.netpluspay.nibssclient.util.RandomNumUtil.mapDanbamitaleResponseToResponseWithRrn
import com.netpluspay.nibssclient.util.SharedPrefManager
import com.netpluspay.nibssclient.util.SharedPrefManager.getUserData
import com.netpluspay.nibssclient.util.Singletons
import com.netpluspay.nibssclient.util.Singletons.getKeyHolder
import com.netpluspay.nibssclient.util.Singletons.getSavedConfigurationData
import com.netpluspay.nibssclient.util.Singletons.setConfigData
import com.netpluspay.nibssclient.util.Utility.getCustomRrn
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

private const val CONFIGURATION_STATUS = "terminal_configuration_status"
private const val CONFIGURATION_ACTION = "com.woleapp.netpos.TERMINAL_CONFIGURATION"
private const val DEFAULT_TERMINAL_ID = "2057H63U"

object NewNibssApiWrapper {
    private lateinit var transactionResponseDao: TransactionResponseDao
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
        isSSL = true
    )
    private val lastTransactionResponse = MutableLiveData<TransactionResponse>()
    private var _partnerThreshold: MutableLiveData<GetPartnerInterSwitchThresholdResponse> =
        MutableLiveData()
    private var terminalId: String? = null
    private var currentlyLoggedInUser: UserData? = null
    private var isConfigurationInProcess = false
    private var configurationStatus = -1
    private val sendIntent = Intent(CONFIGURATION_ACTION)
    private var terminalConfigurator: TerminalConfigurator =
        TerminalConfigurator(connectionData)

    private fun getTerminalId() = terminalId ?: ""
    private fun setCurrentlyLoggedInUser() {
        currentlyLoggedInUser = getUserData()
    }

    private var iswPaymentProcessorObject: TransactionProcessorWrapper? = null
    private val compositeDisposable: CompositeDisposable by lazy { CompositeDisposable() }

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

    fun init(
        context: Context,
        configureSilently: Boolean = false,
        serializedUserData: String
    ): Single<Pair<KeyHolder?, ConfigData?>> {
        // Repush failed transactions back to server
        workManager = WorkManager.getInstance(context.applicationContext)
        repushTransactionsToBackend()
        user = Singletons.getCurrentlyLoggedInUser()
        logUser(context, serializedUserData)
        setCurrentlyLoggedInUser()
        setTerminalId()

        KeyHolder.setHostKeyComponents(
            configurationData.key1,
            configurationData.key2
        ) // default to test  //Set your base keys here
        Timber.e("Terminal ID: $terminalId")
        keyHolder = getKeyHolder()
        Timber.d("KEY_HOLDER$keyHolder")
        configData = Singletons.getConfigData()
        val localBroadcastManager = LocalBroadcastManager.getInstance(context)
//        if (isConfigurationInProcess) {
//            return Single.just(Pair(keyHolder, configData))
//        }
        configurationStatus = 0
        sendIntent.putExtra(CONFIGURATION_STATUS, configurationStatus)
        localBroadcastManager.sendBroadcast(sendIntent)
        val req = when {
            DateUtils.isToday(Prefs.getLong(LAST_POS_CONFIGURATION_TIME, 0)).not() -> {
                Timber.e("last configuration time was not today, configure terminal now")
                configureTerminal(context)
            }
            keyHolder != null && configData != null -> {
                Timber.e("calling home")
                configurationStatus = 1
                callHome(context).onErrorResumeNext {
                    Timber.e(it)
                    Timber.e("call home failed, configure terminal")
                    configureTerminal(context)
                }
            }
            else -> configureTerminal(context)
        }
        return req
    }

    fun callHome(context: Context): Single<Pair<KeyHolder?, ConfigData?>> {
        Timber.e(keyHolder.toString())
        return terminalConfigurator.nibssCallHome(
            context,
            getTerminalId(),
            keyHolder?.clearSessionKey ?: "",
            currentlyLoggedInUser!!.terminalSerialNumber
        ).flatMap {
            Timber.e("call home result $it")
            if (it == "00") {
                return@flatMap Single.just(Pair(null, null))
            } else Single.error(Exception("call home failed"))
        }
    }

    private fun configureTerminal(context: Context): Single<Pair<KeyHolder?, ConfigData?>> {
        val localBroadcastManager = LocalBroadcastManager.getInstance(context)

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
                        currentlyLoggedInUser!!.terminalSerialNumber
                    ).map { nibssConfigData ->
                        setConfigData(nibssConfigData)
                        configData = nibssConfigData
                        Prefs.putString(PREF_CONFIG_DATA, gson.toJson(nibssConfigData))
                        configurationStatus = 1
                        sendIntent.putExtra(CONFIGURATION_STATUS, configurationStatus)
                        localBroadcastManager.sendBroadcast(sendIntent)
                        Timber.e("Config data set")
                        return@map Pair(nibssKeyHolder, nibssConfigData)
                    }
                } else {
                    configurationStatus = -1
                    sendIntent.putExtra(CONFIGURATION_STATUS, configurationStatus)
                    localBroadcastManager.sendBroadcast(sendIntent)
                    Single.just(Pair(null, null))
                }
            }
    }

    private fun disposeDisposables() {
        compositeDisposable.clear()
    }

    private fun repushTransactionsToBackend() {
        val workRequest = OneTimeWorkRequestBuilder<RepushFailedTransactionToBackendWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(
                        NetworkType.CONNECTED
                    ).build()
            ).build()
        workManager.enqueue(workRequest)
    }

    fun makePayment(
        context: Context,
        terminalId: String? = "",
        makePaymentParams: String,
        cardScheme: String,
        cardHolder: String,
        remark: String
    ): Single<TransactionWithRemark?> {
        // some comments here
        transactionResponseDao = AppDatabase.getDatabaseInstance(context).transactionResponseDao()
        val transactionTrackingTableDao =
            AppDatabase.getDatabaseInstance(context).transactionTrackingTableDao()
        val params = gson.fromJson(makePaymentParams, MakePaymentParams::class.java)
        validateField(params.amount)

        val interSwitchObject =
            Prefs.getString(getUserData().partnerId + "iswThreshold", "")
        val thresHold =
            Gson().fromJson(
                interSwitchObject,
                GetPartnerInterSwitchThresholdResponse::class.java
            )?.interSwitchThreshold ?: 0

        return getRRn()
            .onErrorResumeNext(Single.just(Response.success(getCustomRrn())))
            .flatMap {
                makePaymentViaNibss(
                    context,
                    null,
                    terminalId,
                    makePaymentParams,
                    cardScheme,
                    cardHolder,
                    remark,
                    transactionTrackingTableDao,
                    it.body()
                )
            }
    }

    private fun makePaymentViaNibssA(
        context: Context,
        inputTransactionType: String? = null,
        terminalId: String? = "",
        makePaymentParams: String,
        cardScheme: String,
        cardHolder: String,
        remark: String,
        transactionTrackingTableDao: TransactionTrackingTableDao,
        rrn: String?
    ): Single<TransactionWithRemark?> {
        val transactionType =
            inputTransactionType?.let { TransactionType.valueOf(it) } ?: TransactionType.PURCHASE
        val params = gson.fromJson(makePaymentParams, MakePaymentParams::class.java)
        validateField(params.amount)
        val configData: ConfigData = Singletons.getConfigData() ?: kotlin.run {
            showToast(
                "Terminal has not been configured, restart the application to configure",
                context
            )
            return Single.just(null)
        }
        val keyHolder: KeyHolder =
            getKeyHolder() ?: kotlin.run {
                showToast(
                    "Terminal has not been configured, restart the application to configure",
                    context
                )
                return Single.just(null)
            }

        val hostConfig = HostConfig(
            if (terminalId.isNullOrEmpty()) getUserData().terminalId else terminalId,
            connectionData,
            keyHolder,
            configData
        )

        // IsoAccountType.
        this.amountLong = amountDbl.toLong()
        val requestData =
            TransactionRequestData(
                transactionType,
                amountLong,
                0L,
                accountType = IsoAccountType.parseStringAccountType(params.accountType.name),
                RRN = rrn
            )

        val transactionToLog = params.getTransactionResponseToLog(cardScheme, requestData, rrn)

        val processor = TransactionProcessor(hostConfig)

        // Send to backend first
        return logTransactionToBackEndBeforeMakingPayment(transactionToLog)
            .subscribeOn(Schedulers.io())
            .flatMap Foo@{
                processor.processTransaction(context, requestData, params.cardData)
                    .onErrorResumeNext { processor.rollback(context, MessageReasonCode.Timeout) }
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
                        lastTransactionResponse.postValue(
                            rrn?.let { it1 -> it.copy(RRN = it1) }
                                ?: it
                        )
                        val message =
                            (if (it.responseCode == "00") "Transaction Approved" else "Transaction Not approved")
                        Timber.d("RESPONSE=>$it")
                        showToast(message, context)
                        transactionResponseDao
                            .insertNewTransaction(it)
                        Single.just(it)
                    }.flatMap {
                        val resp = lastTransactionResponse.value!!
                        if (resp.responseCode == "00") {
                            updateTransactionInBackendAfterMakingPaymentA(
                                transactionToLog.transactionResponse.rrn,
                                mapDanbamitaleResponseToResponseWithRrn(resp, remark, rrn),
                                "APPROVED",
                                transactionTrackingTableDao
                            ).subscribeOn(Schedulers.io())
                                .zipWith(
                                    Single.just(
                                        mapDanbamitaleResponseToResponseWithRrn(
                                            lastTransactionResponse.value!!,
                                            remark,
                                            rrn
                                        )
                                    )
                                ) { _: LogToBackendResponse,
                                    secondResponse: TransactionWithRemark ->
                                    secondResponse
                                }
                        } else {
                            updateTransactionInBackendAfterMakingPaymentA(
                                rrn = transactionToLog.transactionResponse.rrn,
                                transactionResponse = mapDanbamitaleResponseToResponseWithRrn(
                                    resp,
                                    remark = remark,
                                    rrn
                                ),
                                status = resp.responseMessage,
                                transactionTrackingTableDao
                            ).subscribeOn(Schedulers.io())
                                .zipWith(
                                    Single.just(
                                        mapDanbamitaleResponseToResponseWithRrn(
                                            lastTransactionResponse.value!!,
                                            remark,
                                            rrn
                                        )
                                    )
                                ) { _: LogToBackendResponse,
                                    secondResponse: TransactionWithRemark ->
                                    secondResponse
                                }
                        }
                    }
            }
    }

    // Make Payment Via Nibss Correct Implementation
    private fun makePaymentViaNibss(
        context: Context,
        inputTransactionType: String? = null,
        terminalId: String? = "",
        makePaymentParams: String,
        cardScheme: String,
        cardHolder: String,
        remark: String,
        transactionTrackingTableDao: TransactionTrackingTableDao,
        rrn: String?
    ): Single<TransactionWithRemark?> {
        val transactionType =
            inputTransactionType?.let { TransactionType.valueOf(it) } ?: TransactionType.PURCHASE
        val params = gson.fromJson(makePaymentParams, MakePaymentParams::class.java)
        validateField(params.amount)
        val configData: ConfigData = Singletons.getConfigData() ?: kotlin.run {
            showToast(
                "Terminal has not been configured, restart the application to configure",
                context
            )
            return Single.just(null)
        }
        val keyHolder: KeyHolder =
            getKeyHolder() ?: kotlin.run {
                showToast(
                    "Terminal has not been configured, restart the application to configure",
                    context
                )
                return Single.just(null)
            }

        val hostConfig = HostConfig(
            if (terminalId.isNullOrEmpty()) getUserData().terminalId else terminalId,
            connectionData,
            keyHolder,
            configData
        )

        // IsoAccountType.
        this.amountLong = amountDbl.toLong()
        val originalDataElements =
            OriginalDataElements(
                originalTransactionType = transactionType,
                originalRRN = rrn?.takeLast(12) ?: ""
            )
        val requestData =
            TransactionRequestData(
                transactionType,
                amountLong,
                0L,
                accountType = IsoAccountType.parseStringAccountType(params.accountType.name),
                RRN = rrn?.takeLast(12),
                originalDataElements = originalDataElements
            )

        val transactionToLog = params.getTransactionResponseToLog(cardScheme, requestData, rrn)

        val processor = TransactionProcessor(hostConfig)

        // Send to backend first
        return logTransactionToBackEndBeforeMakingPayment(transactionToLog)
            .doOnError {
                Timber.d("ERROR_FROM_FIRST_LOGGING_TO_SERVER=====>%s", it.localizedMessage)
                return@doOnError
            }
            .flatMap {
                Timber.d("SUCCESSFULLY_LOGGED_TO_BACKEND=====>%s", gson.toJson(it))
                processor.processTransaction(context, requestData, params.cardData)
                    .doFinally {
                        if (lastTransactionResponse.value!!.responseCode == "00") {
                            updateTransactionInBackendAfterMakingPaymentA(
                                transactionToLog.transactionResponse.rrn,
                                mapDanbamitaleResponseToResponseWithRrn(
                                    lastTransactionResponse.value!!,
                                    remark,
                                    rrn
                                ),
                                "APPROVED",
                                transactionTrackingTableDao
                            ).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe { t1, t2 ->
                                    t1?.let { resp ->
                                        Timber.d("$resp")
                                    }
                                    t2?.let { error ->
                                        Timber.d(error.localizedMessage)
                                    }
                                }.disposeWith(compositeDisposable)
                        } else {
                            updateTransactionInBackendAfterMakingPaymentA(
                                rrn = transactionToLog.transactionResponse.rrn,
                                transactionResponse = mapDanbamitaleResponseToResponseWithRrn(
                                    lastTransactionResponse.value!!,
                                    remark = remark,
                                    rrn
                                ),
                                status = lastTransactionResponse.value!!.responseMessage,
                                transactionTrackingTableDao
                            ).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe { t1, t2 ->
                                    t1?.let { resp ->
                                        Timber.d("$resp")
                                    }
                                    t2?.let { error ->
                                        Timber.d("${error.localizedMessage}")
                                    }
                                }.disposeWith(compositeDisposable)
                        }
                    }
                    .onErrorResumeNext {
                        processor.rollback(
                            context,
                            MessageReasonCode.Timeout
                        )
                    }
                    .flatMap { transResponse ->
                        Timber.d(
                            "PAYMENT_DONE_SUCCESSFULLY=====>%s",
                            gson.toJson(transResponse)
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
//                                        IsoTimeManager.
                                        Prefs.putString(
                                            PREF_KEYHOLDER,
                                            gson.toJson(configResult.first)
                                        )
                                        Prefs.putString(
                                            PREF_CONFIG_DATA,
                                            gson.toJson(configResult.second)
                                        )
                                    }
                                }.disposeWith(compositeDisposable)
                        }

                        transResponse.cardHolder = cardHolder
                        transResponse.cardLabel = cardScheme
                        transResponse.amount = requestData.amount
                        lastTransactionResponse.postValue(
                            rrn?.let { transResponse1 -> transResponse.copy(RRN = transResponse1) }
                                ?: transResponse
                        )
                        val message =
                            (if (transResponse.responseCode == "00") "Transaction Approved" else "Transaction Not approved")
                        Timber.d("RESPONSE=>$transResponse")
                        showToast(message, context)
                        transactionResponseDao
                            .insertNewTransaction(transResponse)
                        Single.just(
                            mapDanbamitaleResponseToResponseWithRrn(
                                transResponse,
                                remark,
                                rrn
                            )
                        )
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
        transactionTrackingTableDao: TransactionTrackingTableDao
    ) {
        val dataToLog = DataToLogAfterConnectingToNibss(status, transactionResponse, rrn)
        compositeDisposable.add(
            stormApiService.updateLogAfterConnectingToNibss(rrn, dataToLog)
                .subscribeOn(Schedulers.io())
                .doOnError {
                    saveTransactionForTracking(
                        ModelObjects.TransactionResponseXForTracking(
                            dataToLog.rrn,
                            mapToTransactionResponseX(mapToTransactionResponse(dataToLog.transactionResponse)),
                            dataToLog.status
                        ),
                        transactionTrackingTableDao
                    )
                    Timber.d("ERROR_UPDATING_TRANS==>${it.localizedMessage}")
                }
                .onErrorResumeNext(Single.just(LogToBackendResponse(listOf(1), "", "failed")))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { data, error ->
                    data?.let {
                    }
                    error?.let {
                    }
                }
        )
    }

    private fun updateTransactionInBackendAfterMakingPaymentA(
        rrn: String,
        transactionResponse: TransactionWithRemark,
        status: String,
        transactionTrackingTableDao: TransactionTrackingTableDao
    ): Single<LogToBackendResponse> {
        val dataToLog = DataToLogAfterConnectingToNibss(status, transactionResponse, rrn)
        return stormApiService.updateLogAfterConnectingToNibss(rrn, dataToLog)
            .subscribeOn(Schedulers.io())
            .doOnError {
                saveTransactionForTracking(
                    ModelObjects.TransactionResponseXForTracking(
                        dataToLog.rrn,
                        mapToTransactionResponseX(mapToTransactionResponse(dataToLog.transactionResponse)),
                        dataToLog.status
                    ),
                    transactionTrackingTableDao
                )
                Timber.d("ERROR_UPDATING_TRANS==>${it.localizedMessage}")
            }
            .onErrorResumeNext(Single.just(LogToBackendResponse(listOf(1), "", "failed")))
    }

    private fun saveTransactionForTracking(
        transactionResponse: ModelObjects.TransactionResponseXForTracking,
        transactionTrackingTableDao: TransactionTrackingTableDao
    ) {
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
            }.disposeWith(compositeDisposable)
    }

    private fun getIswToken(context: Context): String {
        Timber.d("CALLED")
        val req = TokenPassportRequest(context.getString(R.string.userMD), getUserData().terminalId)
        return try {
            var iswToken = ""
            val disposable = CompositeDisposable()
            disposable.add(
                getTokenClient.getToken(req)
                    .doOnError {
                        Timber.d("TOKEN_ERROR==>${it.localizedMessage}")
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { t1, t2 ->
                        t1?.let {
                            Timber.d("TOKEN_RESPONSE==>${it.token}")
                            Prefs.putString(ISW_TOKEN, it.token)
                            iswToken = it.token
                        }
                        t2?.let {
                        }
                    }
            )
            disposable.clear()
            iswToken
        } catch (e: Exception) {
            "RUBISH"
        }
    }

    private fun processTransactionViaInterSwitchMakePayment(
        context: Context,
        inputTransactionType: String? = null,
        terminalId: String? = "",
        makePaymentParams: String,
        cardScheme: String,
        cardHolder: String,
        remark: String,
        transactionTrackingTableDao: TransactionTrackingTableDao,
        rrn: String?
    ): Single<TransactionWithRemark?> {
        val customRrn = generateRandomRrn(12)
        val params = gson.fromJson(makePaymentParams, MakePaymentParams::class.java)
        val transactionType =
            inputTransactionType?.let { TransactionType.valueOf(it) } ?: TransactionType.PURCHASE

        val configData: ConfigData = Singletons.getConfigData() ?: kotlin.run {
            showToast(
                "Terminal has not been configured, restart the application to configure",
                context
            )
            return Single.just(null)
        }

        val keyHolder = getKeyHolder()

        getIswToken(context)
        getThreshold()

        // IsoAccountType.
        this.amountLong = amountDbl.toLong()
        val requestData =
            TransactionRequestData(
                transactionType,
                amountLong,
                0L,
                accountType = IsoAccountType.valueOf(params.accountType.name),
                RRN = rrn
            )

        if (Prefs.getString(getUserData().partnerId + "iswThreshold", "")
            .isEmpty()
        ) {
            Toast.makeText(context, "Unable to identify partner", Toast.LENGTH_LONG).show()
        }
        val interSwitchObject =
            Prefs.getString(getUserData().partnerId + "iswThreshold", "")
        val destinationAcc = if (interSwitchObject.isNotEmpty()) gson.fromJson(
            interSwitchObject,
            GetPartnerInterSwitchThresholdResponse::class.java
        ).bankAccountNumber else {
            getIswToken(context)
            getThreshold()
            _partnerThreshold.value?.bankAccountNumber ?: ""
        }

        Timber.d("DATA_DESTINATION==>$destinationAcc")
        Timber.d("DATA_INTERSWITCH==>$interSwitchObject")
        if (destinationAcc.isNullOrEmpty()) {
            Toast.makeText(context, "No destination account found", Toast.LENGTH_LONG).show()
        }

        val iswParam = IswParameters(
            context.getString(R.string.userMD),
            user?.business_address ?: "",
            token = Prefs.getString(ISW_TOKEN, "error2"),
            "",
            terminalId = getUserData().terminalId,
            terminalSerial = getUserData().terminalSerialNumber,
            destinationAcc
        )

        requestData.iswParameters = iswParam

        iswPaymentProcessorObject =
            TransactionProcessorWrapper(
                context.getString(R.string.userMD),
                getUserData().terminalId,
                requestData.amount,
                transactionRequestData = requestData,
                keyHolder = keyHolder,
                configData = configData
            )

        val transactionToLog = params.getTransactionResponseToLog(cardScheme, requestData, rrn)

        // Send to backend first
        logTransactionToBackEndBeforeMakingPayment(transactionToLog)
        return params.cardData.let { cardData ->
            iswPaymentProcessorObject!!.processIswTransaction(cardData)
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
                    lastTransactionResponse.postValue(rrn?.let { it1 -> it.copy(RRN = it1) } ?: it)
                    val message =
                        (if (it.responseCode == "00") "Transaction Approved" else "Transaction Not approved")
                    Timber.d("RESPONSE=>$it")
                    showToast(message, context)
                    transactionResponseDao
                        .insertNewTransaction(it)
                }.flatMap {
                    val resp: TransactionResponse = lastTransactionResponse.value!!
                    if (resp.responseCode == "00") {
                        updateTransactionInBackendAfterMakingPayment(
                            transactionToLog.transactionResponse.rrn,
                            mapDanbamitaleResponseToResponseWithRrn(resp, remark, rrn),
                            "APPROVED",
                            transactionTrackingTableDao
                        )
                    } else {
                        updateTransactionInBackendAfterMakingPayment(
                            rrn = transactionToLog.transactionResponse.rrn,
                            transactionResponse = mapDanbamitaleResponseToResponseWithRrn(
                                resp,
                                remark = remark,
                                rrn
                            ),
                            status = resp.responseMessage,
                            transactionTrackingTableDao
                        )
                    }

                    Single.just(mapDanbamitaleResponseToResponseWithRrn(resp, remark, rrn))
                }
        }
    }

    private fun getRRn() =
        rrnApiService.getRrn()

    // GET THRESHOLD AMOUNT TO ROUTE USER'S TRANSACTION TO ISW
    private fun getThreshold() {
        val disposable = CompositeDisposable()
        disposable.add(
            stormApiService.getPartnerInterSwitchThreshold(
                getUserData().partnerId
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { data ->
                        // Save the threshold to sharedPrefs
                        Timber.d("ISW_THRESHOLD==>${Gson().toJson(data)}")
                        val thresholdObjectInString = gson.toJson(data)
                        Prefs.putString(
                            getUserData().partnerId + "iswThreshold",
                            thresholdObjectInString
                        )
                        _partnerThreshold.postValue(data)
                    },
                    { throwable ->
                        Timber.e(throwable)
                    }
                )
        )
        disposable.clear()
    }

    fun getDateObject(dateInString: String): LocalDateTime? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss a", Locale.ENGLISH)
            LocalDateTime.parse(dateInString, pattern)
        } else {
            Timber.d("VERSION_OF_ANDROID=====>${Build.VERSION.SDK_INT}")
            null
        }

    /**
     * @param dateInSting: String, (dateString from the payload received back from transaction processing),
     * @param format: String, format in which you want to get the date string back, e.g "yyyy-MM-dd'T'HH:MM:SS.T00Z"
     * @return date as a string in the specified format
     * */
    fun formatDate(dateInString: String, format: String): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss a", Locale.ENGLISH)
            LocalDateTime.parse(dateInString, pattern).format(pattern)
        } else {
            Timber.d("VERSION_OF_ANDROID=====>${Build.VERSION.SDK_INT}")
            null
        }
    }
}

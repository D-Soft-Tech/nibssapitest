package com.netpluspay.nibssclient.service

import android.content.Context
import android.content.Intent
import android.text.format.DateUtils
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.danbamitale.epmslib.entities.* // ktlint-disable no-wildcard-imports
import com.danbamitale.epmslib.entities.KeyHolder
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
import com.netpluspay.nibssclient.database.AppDatabase
import com.netpluspay.nibssclient.models.* // ktlint-disable no-wildcard-imports
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
import com.netpluspay.nibssclient.util.Utility.getTransactionResponseToLog
import com.netpluspay.nibssclient.util.alerts.Alerter.showToast
import com.pixplicity.easyprefs.library.Prefs
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

private const val CONFIGURATION_STATUS = "terminal_configuration_status"
private const val CONFIGURATION_ACTION = "com.woleapp.netpos.TERMINAL_CONFIGURATION"
private const val DEFAULT_TERMINAL_ID = "2057H63U"

object NewNibssApiWrapper {
    private lateinit var transactionResponseDao: TransactionResponseDao
    private val gson = Gson()
    private var amountDbl = 0.0
    private var amountLong = 0L
    private var transResp: TransactionResponse? = null

    private val stormApiService: StormApiService = StormApiClient.getStormApiLoginInstance()
    private var configurationData: ConfigurationData = getSavedConfigurationData()
    private var user: User? = null
    private val disposables = CompositeDisposable()
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
        getIswToken(context)
        getThreshold()
    }

    private fun setTerminalId() {
        terminalId = getUserData().terminalId
    }

    private fun validateField(amount: Long) {
        amountDbl = amount.toDouble() * 100
        this.amountLong = amountDbl.toLong()
    }

    private var keyHolder: KeyHolder? = null
    private var configData: ConfigData? = null

    fun init(
        context: Context,
        configureSilently: Boolean = false,
        serializedUserData: String
    ) {
        user = Singletons.getCurrentlyLoggedInUser()
        transactionResponseDao = AppDatabase.getDatabaseInstance(context).transactionResponseDao()
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
        if (isConfigurationInProcess)
            return
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

        val disposable = req.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                isConfigurationInProcess = true
            }
            .doFinally { isConfigurationInProcess = false }
            .subscribe { pair, error ->
                error?.let {
                    // TerminalManager.getInstance().beep(context, TerminalManager.BEEP_MODE_FAILURE)
                    configurationStatus = -1
                    sendIntent.putExtra(CONFIGURATION_STATUS, configurationStatus)
                    localBroadcastManager.sendBroadcast(sendIntent)
                    Timber.e(it)
                }
                pair?.let {
                    pair.first?.let {
                        Prefs.putLong(LAST_POS_CONFIGURATION_TIME, System.currentTimeMillis())
                        Prefs.putString(PREF_CONFIG_DATA, gson.toJson(pair.second))
                        Prefs.putString(PREF_KEYHOLDER, gson.toJson(pair.first))
                        this.configData = pair.second
                    }
                    configurationStatus = 1
                    sendIntent.putExtra(CONFIGURATION_STATUS, configurationStatus)
                    localBroadcastManager.sendBroadcast(sendIntent)
                    Timber.e("Config data set")
                    disposeDisposables()
                }
            }
        disposables.add(disposable)
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
            if (it == "00")
                return@flatMap Single.just(Pair(null, null))
            else Single.error(Exception("call home failed"))
        }
    }

    fun configureTerminal(context: Context): Single<Pair<KeyHolder?, ConfigData?>> =
        terminalConfigurator.downloadNibssKeys(context, getTerminalId())
            .flatMap { nibssKeyHolder ->
                keyHolder = nibssKeyHolder
                terminalConfigurator.downloadTerminalParameters(
                    context,
                    getTerminalId(),
                    nibssKeyHolder.clearSessionKey,
                    currentlyLoggedInUser!!.terminalSerialNumber
                ).map { nibssConfigData ->
                    setConfigData(nibssConfigData)
                    configData = nibssConfigData
                    return@map Pair(nibssKeyHolder, nibssConfigData)
                }
            }

    private fun disposeDisposables() {
        disposables.clear()
    }

    fun makePayment(
        context: Context,
        terminalId: String? = "",
        makePaymentParams: String,
        cardScheme: String,
        cardHolder: String,
        remark: String
    ): Single<TransactionWithRemark?> {
        val params = gson.fromJson(makePaymentParams, MakePaymentParams::class.java)
        validateField(params.amount)

        val interSwitchObject =
            Prefs.getString(getUserData().partnerId + "iswThreshold", "")
        val thresHold =
            Gson().fromJson(
                interSwitchObject,
                GetPartnerInterSwitchThresholdResponse::class.java
            )?.interSwitchThreshold ?: 0

        return if (thresHold == 0) {
            makePaymentViaNibss(
                context,
                null,
                terminalId,
                makePaymentParams,
                cardScheme,
                cardHolder,
                remark
            )
        } else {
            if (params.amount.toDouble() < 15 /*thresHold.toDouble()*/) {
                makePaymentViaNibss(
                    context,
                    null,
                    terminalId,
                    makePaymentParams,
                    cardScheme,
                    cardHolder,
                    remark
                )
            } else {
                processTransactionViaInterSwitchMakePayment(
                    context,
                    null,
                    terminalId,
                    makePaymentParams,
                    cardScheme,
                    cardHolder,
                    remark
                )
            }
        }
    }

    private fun makePaymentViaNibss(
        context: Context,
        inputTransactionType: String? = null,
        terminalId: String? = "",
        makePaymentParams: String,
        cardScheme: String,
        cardHolder: String,
        remark: String
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
                accountType = IsoAccountType.parseStringAccountType(params.accountType.name)
            )

        val transactionToLog = params.getTransactionResponseToLog(cardScheme, requestData)

        // Send to backend first
        logTransactionToBackEndBeforeMakingPayment(transactionToLog)
        val processor = TransactionProcessor(hostConfig)
        return processor.processTransaction(context, requestData, params.cardData)
            .doOnError {
                Log.d("ERROR1", it.localizedMessage.toString())
            }
            .onErrorResumeNext {
                processor.rollback(context, MessageReasonCode.Timeout)
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
                lastTransactionResponse.postValue(it)
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
                    updateTransactionInBackendAfterMakingPayment(
                        transactionToLog.transactionResponse.rrn,
                        mapDanbamitaleResponseToResponseWithRrn(resp, remark),
                        "APPROVED"
                    )
                } else {
                    updateTransactionInBackendAfterMakingPayment(
                        rrn = transactionToLog.transactionResponse.rrn,
                        transactionResponse = mapDanbamitaleResponseToResponseWithRrn(
                            resp,
                            remark = remark
                        ),
                        status = resp.responseMessage
                    )
                }
                Single.just(mapDanbamitaleResponseToResponseWithRrn(resp, remark))
            }
    }

    private fun logTransactionToBackEndBeforeMakingPayment(dataToLog: TransactionToLogBeforeConnectingToNibbs) {
        compositeDisposable.add(
            stormApiService.logTransactionBeforeMakingPayment(dataToLog)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { t1, t2 ->
                    t1?.let {
                        Timber.d("SUCCESS SAVING 1")
                    }
                    t2?.let {
                        Timber.d("ERROR SAVING 1")
                    }
                }
        )
    }

    private fun updateTransactionInBackendAfterMakingPayment(
        rrn: String,
        transactionResponse: TransactionWithRemark,
        status: String
    ) {
        val dataToLog = DataToLogAfterConnectingToNibss(status, transactionResponse, rrn)
        compositeDisposable.add(
            stormApiService.updateLogAfterConnectingToNibss(rrn, dataToLog)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    Timber.d("ERROR_UPDATING_TRANS==>${it.localizedMessage}")
                }
                .subscribe { t1, t2 ->
                    t1?.let { Timber.d("UPDATED_TRANS==>${it.status}, ${it.message}, ${it.data[0]}") }
                    t2?.let { Timber.d("ERROR_UPDATING_TRANS2==>${it.message}") }
                }
        )
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

    private fun processTransactionViaInterSwitchMakePayment(
        context: Context,
        inputTransactionType: String? = null,
        terminalId: String? = "",
        makePaymentParams: String,
        cardScheme: String,
        cardHolder: String,
        remark: String
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

        // IsoAccountType.
        this.amountLong = amountDbl.toLong()
        val requestData =
            TransactionRequestData(
                transactionType,
                amountLong,
                0L,
                accountType = IsoAccountType.valueOf(params.accountType.name)
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

        val transactionToLog = params.getTransactionResponseToLog(cardScheme, requestData)

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
                    lastTransactionResponse.postValue(it)
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
                            mapDanbamitaleResponseToResponseWithRrn(resp, remark),
                            "APPROVED"
                        )
                    } else {
                        updateTransactionInBackendAfterMakingPayment(
                            rrn = transactionToLog.transactionResponse.rrn,
                            transactionResponse = mapDanbamitaleResponseToResponseWithRrn(
                                resp,
                                remark = remark
                            ),
                            status = resp.responseMessage
                        )
                    }

                    Single.just(mapDanbamitaleResponseToResponseWithRrn(resp, remark))
                }
        }
    }
}

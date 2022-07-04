package com.netpluspay.nibssclient.service

import android.content.Context
import android.content.Intent
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.danbamitale.epmslib.entities.*
import com.danbamitale.epmslib.entities.KeyHolder
import com.danbamitale.epmslib.entities.TransactionResponse
import com.danbamitale.epmslib.entities.TransactionType
import com.danbamitale.epmslib.processors.TerminalConfigurator
import com.danbamitale.epmslib.processors.TransactionProcessor
import com.danbamitale.epmslib.utils.IsoAccountType
import com.danbamitale.epmslib.utils.MessageReasonCode
import com.google.gson.Gson
import com.isw.gateway.TransactionProcessorWrapper
import com.netpluspay.nibssclient.dao.TransactionResponseDao
import com.netpluspay.nibssclient.database.AppDatabase
import com.netpluspay.nibssclient.models.*
import com.netpluspay.nibssclient.network.StormApiClient
import com.netpluspay.nibssclient.network.StormApiService
import com.netpluspay.nibssclient.util.Constants.LAST_POS_CONFIGURATION_TIME
import com.netpluspay.nibssclient.util.Constants.PREF_CONFIG_DATA
import com.netpluspay.nibssclient.util.Constants.PREF_KEYHOLDER
import com.netpluspay.nibssclient.util.RandomNumUtil.mapDanbamitaleResponseToResponseWithRrn
import com.netpluspay.nibssclient.util.SharedPrefManager
import com.netpluspay.nibssclient.util.Singletons
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
    private val disposables = CompositeDisposable()
    private var connectionData: ConnectionData = ConnectionData(
        ipAddress = configurationData.ip,
        ipPort = configurationData.port.toInt(),
        isSSL = true
    )
    private var tokenPassportRequest: String = ""
    private val lastTransactionResponse = MutableLiveData<TransactionResponse>()
    private var terminalId: String? = null
    private var currentlyLoggedInUser: UserData? = null
    private var isConfigurationInProcess = false
    private var configurationStatus = -1
    private val sendIntent = Intent(CONFIGURATION_ACTION)
    private var terminalConfigurator: TerminalConfigurator =
        TerminalConfigurator(connectionData)

    private fun getTerminalId() = terminalId ?: ""
    private fun setCurrentlyLoggedInUser() {
        currentlyLoggedInUser = SharedPrefManager.getUserData()
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
        terminalId = SharedPrefManager.getUserData().terminalId
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
        transactionResponseDao = AppDatabase.getDatabaseInstance(context).transactionResponseDao()
        logUser(context, serializedUserData)
        setCurrentlyLoggedInUser()
        setTerminalId()
        KeyHolder.setHostKeyComponents(
            configurationData.key1,
            configurationData.key2
        ) // default to test  //Set your base keys here
        Timber.e("Terminal ID: $terminalId")
        keyHolder = Singletons.getKeyHolder()
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
        return makePaymentViaNibss(
            context,
            null,
            terminalId,
            makePaymentParams,
            cardScheme,
            cardHolder,
            remark
        )
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

        Timber.d("CALLING_ONLY_NIBSS")
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
            Singletons.getKeyHolder() ?: kotlin.run {
                showToast(
                    "Terminal has not been configured, restart the application to configure",
                    context
                )
                return Single.just(null)
            }

        val hostConfig = HostConfig(
            if (terminalId.isNullOrEmpty()) SharedPrefManager.getUserData().terminalId else terminalId,
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
//        transactionState.value = Constants.STATE_PAYMENT_STARTED
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
                    Timber.d(Singletons.gson.toJson(it))
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
                .subscribe { t1, t2 ->
                    t1?.let { Timber.d(it.message) }
                    t2?.let { Timber.d(it.localizedMessage) }
                }
        )
    }
}

package com.netpluspay.nibssclient.viewModels

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.danbamitale.epmslib.entities.*
import com.danbamitale.epmslib.processors.TransactionProcessor
import com.danbamitale.epmslib.utils.Event
import com.danbamitale.epmslib.utils.IsoAccountType
import com.danbamitale.epmslib.utils.MessageReasonCode
import com.google.gson.Gson
import com.isw.gateway.TransactionProcessorWrapper
import com.isw.iswclient.iswapiclient.getTokenClient
import com.isw.iswclient.request.IswParameters
import com.isw.iswclient.request.TokenPassportRequest
import com.netpluspay.nibssclient.dao.TransactionResponseDao
import com.netpluspay.nibssclient.models.DataToLogAfterConnectingToNibss
import com.netpluspay.nibssclient.models.LogToBackendResponse
import com.netpluspay.nibssclient.models.TransactionResponseX
import com.netpluspay.nibssclient.models.TransactionToLogBeforeConnectingToNibbs
import com.netpluspay.nibssclient.network.StormApiService
import com.netpluspay.nibssclient.util.Constants.PREF_CONFIG_DATA
import com.netpluspay.nibssclient.util.Constants.PREF_KEYHOLDER
import com.netpluspay.nibssclient.util.Constants.STATE_PAYMENT_STAND_BY
import com.netpluspay.nibssclient.util.Constants.STATE_PAYMENT_STARTED
import com.netpluspay.nibssclient.util.RandomNumUtil.formattedTime
import com.netpluspay.nibssclient.util.RandomNumUtil.generateRandomRrn
import com.netpluspay.nibssclient.util.RandomNumUtil.getCurrentDateTime
import com.netpluspay.nibssclient.util.RandomNumUtil.getDate
import com.netpluspay.nibssclient.util.RandomNumUtil.mapDanbamitaleResponseToResponseX
import com.netpluspay.nibssclient.util.SharedPrefManager
import com.netpluspay.nibssclient.util.Singletons
import com.pixplicity.easyprefs.library.Prefs
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class SalesViewModel @Inject constructor(private val transactionResponseDao: TransactionResponseDao) {
    @Inject
    lateinit var stormApiService: StormApiService
    var transResp: TransactionResponse? = null
    var cardData: CardData? = null
    private val compositeDisposable: CompositeDisposable by lazy { CompositeDisposable() }
    val transactionState = MutableLiveData(STATE_PAYMENT_STAND_BY)
    private val lastTransactionResponse = MutableLiveData<TransactionResponse>()
    private var partnerThreshold: Int = Singletons.getPartnerThreshold()
    val amount: MutableLiveData<String> = MutableLiveData<String>("")
    var amountLong = 0L
    private var tokenPassportRequest: String = ""
    private var cardScheme: String? = null
    val customerName = MutableLiveData("")
    private var isoAccountType: IsoAccountType? = null
    private var amountDbl: Double = 0.0
    private val _shouldRefreshNibssKeys = MutableLiveData<Event<Boolean>>()
    private val _finish = MutableLiveData<Event<Boolean>>()
    private var user: com.netpluspay.nibssclient.models.UserData?
    private var iswPaymentProcessorObject: TransactionProcessorWrapper? = null
    private val _message: MutableLiveData<Event<String>> by lazy {
        MutableLiveData<Event<String>>()
    }
    private val _getCardData = MutableLiveData<Event<Boolean>>()
    private var connectionData: ConnectionData = ConnectionData(
        ipAddress = Singletons.getSavedConfigurationData().ip,
        ipPort = Singletons.getSavedConfigurationData().port.toInt(),
        isSSL = true
    )

    init {
        user = SharedPrefManager.getUserData()
    }

    private fun setCustomerName(name: String) {
        customerName.value = name
    }

    fun validateField() {
        amountDbl = (
            amount.value!!.toDoubleOrNull() ?: kotlin.run {
                _message.value = Event("Enter a valid amount")
                return
            }
            ) * 100
        this.amountLong = amountDbl.toLong()
        _getCardData.value = Event(true)
    }

    private fun logTransactionBeforeConnectingToNibss(dataToLog: TransactionToLogBeforeConnectingToNibbs) {
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

    private fun logTransactionAfterConnectingToNibss(
        rrn: String,
        transactionResponse: TransactionResponseX,
        status: String
    ): Single<LogToBackendResponse> {
        val dataToLog = DataToLogAfterConnectingToNibss(status, transactionResponse, rrn)
        return stormApiService!!.updateLogAfterConnectingToNibss(rrn, dataToLog)
    }

    private fun getPartnerInterSwitchThreshold() {
        compositeDisposable.add(
            stormApiService!!.getPartnerInterSwitchThreshold(user!!.partnerId)
                .subscribeOn(Schedulers.io())
                .subscribe { t1, t2 ->
                    t1?.let {
                        Singletons.setPartnerThreshold(it.interSwitchThreshold)
                    }
                    t2?.let {
                        Timber.d(it.localizedMessage)
                    }
                }
        )
    }

    private fun getToken() {
        val req = TokenPassportRequest(user?.partnerId!!, user?.terminalId!!)

        try {
            compositeDisposable.add(
                getTokenClient.getToken(req)
                    .subscribeOn(Schedulers.io())
                    .subscribe { t1, t2 ->
                        t1?.let {
                            tokenPassportRequest = it.token
                        }
                        t2?.let {
                            Timber.d(it.localizedMessage)
                        }
                    }
            )
        } catch (e: Exception) {
            Timber.d("An Error occured")
        }
    }

    fun makePayment(inputAmount: Int, context: Context, transactionType: String) {
        setCustomerName(user!!.customerName)
        amount.value = inputAmount.toString()
        if (inputAmount < getPartnersThreshold()) {
            makePaymentViaNibss(
                context,
                TransactionType.valueOf(transactionType),
                user!!.terminalId
            )
        } else {
            processTransactionViaInterSwitchMakePayment(
                context,
                TransactionType.valueOf(transactionType),
                user!!.terminalId,
                cardData!!
            )
        }
    }

    private fun getPartnersThreshold(): Int {
        if (partnerThreshold < 0) {
            getPartnerInterSwitchThreshold()
            partnerThreshold = Singletons.getPartnerThreshold()
        }
        return partnerThreshold
    }

    private fun processTransactionViaInterSwitchMakePayment(
        context: Context,
        transactionType: TransactionType = TransactionType.PURCHASE,
        terminalSerial: String,
        cardData: CardData
    ) {
        // Get token to pass to isw make payment endpoint
        getToken()
        val customRrn = generateRandomRrn(12)

        val configData: ConfigData = Singletons.getConfigData() ?: kotlin.run {
            _message.value =
                Event("Terminal has not been configured, restart the application to configure")
            return
        }
//        val keyHolder = NetPosTerminalConfig.getKeyHolder()!!
        val keyHolder = Singletons.getKeyHolder()

        // IsoAccountType.
        this.amountLong = amountDbl.toLong()
        val requestData =
            TransactionRequestData(
                transactionType,
                amountLong,
                0L,
                accountType = isoAccountType!!
            )

        if (tokenPassportRequest.isEmpty()) {
            Toast.makeText(context, "Invalid token id", Toast.LENGTH_LONG).show()
        }
        val requestDataForIsw = requestData
        val iswParam = IswParameters(
            user?.partnerId!!,
            user?.businessAddress ?: "",
            tokenPassportRequest,
            "",
            terminalId = user?.terminalId!!,
            terminalSerial = terminalSerial,
            ""
        )

        requestDataForIsw.iswParameters = iswParam

        iswPaymentProcessorObject =
            TransactionProcessorWrapper(
                user?.partnerId!!,
                user?.terminalId!!,
                requestData.amount,
                transactionRequestData = requestDataForIsw,
                keyHolder = KeyHolder(),
                configData = configData
            )
        Timber.d(Gson().toJson(cardData).toString())
        cardData.let { cardDataLambdaVariable ->
            Timber.d(Gson().toJson(requestDataForIsw).toString())
            iswPaymentProcessorObject!!.processIswTransaction(cardDataLambdaVariable)
                .flatMap {
                    transResp = it
                    if (it.responseCode == "A3") {
                        Prefs.remove(PREF_CONFIG_DATA)
                        Prefs.remove(PREF_KEYHOLDER)
                        _shouldRefreshNibssKeys.postValue(Event(true))
                    }

                    it.cardHolder = customerName.value!!
                    it.cardLabel = cardScheme!!
                    it.amount = requestData.amount
                    lastTransactionResponse.postValue(it)
                    Timber.e(it.toString())
                    Timber.e(it.responseCode)
                    Timber.e(it.responseMessage)
                    _message.postValue(Event(if (it.responseCode == "00") "Transaction Approved" else "Transaction Not approved"))
                    // Insert to database
                    transactionResponseDao.insertNewTransaction(
                        it
                    )
                }.flatMap {
                    val resp: TransactionResponse = lastTransactionResponse.value!!
                    if (resp.responseCode == "00") {
                        logTransactionAfterConnectingToNibss(
                            rrn = resp.RRN,
                            transactionResponse = mapDanbamitaleResponseToResponseX(resp),
                            status = "APPROVED"
                        )
                    } else {
                        logTransactionAfterConnectingToNibss(
                            rrn = resp.RRN,
                            transactionResponse = mapDanbamitaleResponseToResponseX(resp),
                            status = "DECLINED"
                        )
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { t1, throwable ->
                    t1?.let {
                        // _finish.value = Event(true)
                    }
                    throwable?.let {
                        _message.value = Event("Error: ${it.localizedMessage}")
                        Timber.e(it)
                    }
                }
        }
    }

    private fun makePaymentViaNibss(
        context: Context,
        transactionType: TransactionType = TransactionType.PURCHASE,
        terminalId: String
    ) {
        Timber.e(cardData.toString())
        val configData: ConfigData = Singletons.getConfigData() ?: kotlin.run {
            _message.value =
                Event("Terminal has not been configured, restart the application to configure")
            return
        }
//        val keyHolder: KeyHolder = NetPosTerminalConfig.getKeyHolder()!!
        val keyHolder: KeyHolder =
            Singletons.getKeyHolder() ?: return

        val hostConfig = HostConfig(
            terminalId,
            connectionData,
            keyHolder,
            configData
        )

        val customStan = generateRandomRrn(6)
        val customRrn = generateRandomRrn(12)
        val transTime = formattedTime.replace(":", "")
        val transDateTime = getCurrentDateTime()
        println("=========TTT+Time $transDateTime")

        // IsoAccountType.
        this.amountLong = amountDbl.toLong()
        val requestData =
            TransactionRequestData(
                transactionType,
                amountLong,
                0L,
                accountType = isoAccountType!!
            )

        val transactionToLog = cardData?.expiryDate?.let {
            customerName.value?.let { it1 ->
                user?.partnerId?.let { it2 ->
                    TransactionToLogBeforeConnectingToNibbs(
                        status = "PENDING",
                        TransactionResponseX(
                            AID = "",
                            rrn = customRrn,
                            STAN = customStan,
                            TSI = "",
                            TVR = "",
                            accountType = isoAccountType!!.name,
                            acquiringInstCode = "",
                            additionalAmount_54 = "",
                            amount = amount.value!!.toInt(),
                            appCryptogram = "",
                            authCode = "",
                            cardExpiry = it,
                            cardHolder = it1,
                            cardLabel = cardScheme.toString(),
                            id = 0,
                            localDate_13 = getDate(),
                            localTime_12 = transTime,
                            maskedPan = cardData!!.pan,
                            merchantId = it2,
                            originalForwardingInstCode = "",
                            otherAmount = requestData.otherAmount.toInt(),
                            otherId = "",
                            responseCode = "99",
                            responseDE55 = "",
                            terminalId = user!!.terminalId!!,
                            transactionTimeInMillis = 0,
                            transactionType = requestData.transactionType.name,
                            transmissionDateTime = transDateTime
                        )
                    )
                }
            }
        }

        // Send to backend first
        logTransactionBeforeConnectingToNibss(transactionToLog!!)
        val processor = TransactionProcessor(hostConfig)
        transactionState.value = STATE_PAYMENT_STARTED
        compositeDisposable.add(
            processor.processTransaction(context, requestData, cardData!!)
                .onErrorResumeNext {
                    processor.rollback(context, MessageReasonCode.Timeout)
                }
                .flatMap {
                    transResp = it
                    if (it.responseCode == "A3") {
                        Prefs.remove(PREF_CONFIG_DATA)
                        Prefs.remove(PREF_KEYHOLDER)
                        _shouldRefreshNibssKeys.postValue(Event(true))
                    }

                    it.cardHolder = customerName.value!!
                    it.cardLabel = cardScheme!!
                    it.amount = requestData.amount
                    lastTransactionResponse.postValue(it)
                    Timber.e(it.toString())
                    Timber.e(it.responseCode)
                    Timber.e(it.responseMessage)
                    _message.postValue(Event(if (it.responseCode == "00") "Transaction Approved" else "Transaction Not approved"))
                    transactionResponseDao
                        .insertNewTransaction(it)
                }.flatMap {
                    val resp = lastTransactionResponse.value!!
                    if (resp.responseCode == "00") {
                        Timber.d(Singletons.gson.toJson(it))
                        logTransactionAfterConnectingToNibss(
                            customRrn,
                            mapDanbamitaleResponseToResponseX(resp),
                            "APPROVED"
                        )
                    } else {
                        logTransactionAfterConnectingToNibss(
                            rrn = customRrn,
                            transactionResponse = mapDanbamitaleResponseToResponseX(resp),
                            status = resp.responseMessage
                        )
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { t1, throwable ->
                    t1?.let {
                        // _finish.value = Event(true)
                    }
                    throwable?.let {
                        _message.value = Event("Error: ${it.localizedMessage}")
                        Timber.e(it)
                    }
                }
        )
//        return lastTransactionResponse.value
    }

//    override fun onCleared() {
//        super.onCleared()
//        compositeDisposable.clear()
//    }

    fun setAccountType(accountType: IsoAccountType) {
        this.isoAccountType = accountType
    }

    fun setCardScheme(cardScheme: String?) {
        this.cardScheme = if (cardScheme.equals("no match", true)) "VERVE" else cardScheme
    }

    fun finish() {
        _finish.value = Event(true)
    }
}

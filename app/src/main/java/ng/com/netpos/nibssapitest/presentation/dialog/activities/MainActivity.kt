package ng.com.netpos.nibssapitest.presentation.dialog.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.danbamitale.epmslib.entities.CardData
import com.danbamitale.epmslib.entities.clearPinKey
import com.danbamitale.epmslib.extensions.formatCurrencyAmount
import com.google.gson.Gson
import com.netpluspay.netpossdk.NetPosSdk
import com.netpluspay.netpossdk.emv.CardReadResult
import com.netpluspay.netpossdk.emv.CardReaderEvent
import com.netpluspay.netpossdk.emv.CardReaderService
import com.netpluspay.netpossdk.utils.DeviceConfig
import com.netpluspay.nibssclient.models.IsoAccountType
import com.netpluspay.nibssclient.models.MakePaymentParams
import com.netpluspay.nibssclient.models.UserData
import com.netpluspay.nibssclient.service.NetposPaymentClient
import com.pixplicity.easyprefs.library.Prefs
import com.pos.sdk.emvcore.POIEmvCoreManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ng.com.netpos.nibssapitest.AppConstant
import ng.com.netpos.nibssapitest.AppConstant.PAYMENT_SUCCESS_DATA_TAG
import ng.com.netpos.nibssapitest.AppConstant.TAG_CHECK_BALANCE
import ng.com.netpos.nibssapitest.AppConstant.getSampleUserData
import ng.com.netpos.nibssapitest.R
import ng.com.netpos.nibssapitest.data.models.Status
import ng.com.netpos.nibssapitest.databinding.ActivityMainBinding
import ng.com.netpos.nibssapitest.presentation.dialog.LoadingDialog
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    var netposPaymentClient: NetposPaymentClient = NetposPaymentClient
    private lateinit var editAmount: EditText
    private lateinit var balanceEnquiry: Button
    private lateinit var resultViewerTextView: TextView
    private val userData: UserData = getSampleUserData()
    private val gson: Gson = Gson()
    private var cardData: CardData? = null
    private var previousAmount: Long? = null
    private lateinit var cardResult: CardReadResult
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        editAmount = binding.editTextNumber
        balanceEnquiry = binding.checkAccountBalance
        resultViewerTextView = binding.resultTextView

        balanceEnquiry.setOnClickListener {
        }

        netposPaymentClient.logUser(this, Gson().toJson(userData))
        compositeDisposable.add(
            netposPaymentClient.init(this, Gson().toJson(userData))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { data, error ->
                    data?.let { response ->
                        val keyHolder = response.first
                        val pinKey = keyHolder?.clearPinKey
                        if (pinKey != null) {
                            NetPosSdk.writeTpkKey(DeviceConfig.TPKIndex, pinKey)
                        }
                        Timber.d("DATA_CLEAR_PIN_KEY%s", "$pinKey")
                        Timber.d("GOTTEN_KEYHOLDER%s", Gson().toJson(response.first))
                        Timber.d("GOTTEN_CONFIG_DATA%s", Gson().toJson(response.second))

                        val savedKeyHolder =
                            Prefs.getString("pref_keyholder", "KeyHolderWasn'tSaved")
                        val savedConfigData =
                            Prefs.getString("pref_config_data", "KeyHolderWasn'tSaved")
                    }
                    error?.let {
                        Timber.d("GOTTEN_ERROR%s", it.localizedMessage)
                    }
                }
        )
    }

    fun makePayment(v: View) {
        if (editAmount.text.isNullOrEmpty() || editAmount.text.toString().toLong() < 15) {
            Toast.makeText(
                this,
                getString(R.string.amount_can_not_be_less_than_15),
                Toast.LENGTH_LONG
            ).show()
            return
        }
        readCard("makePayment")
    }

    fun balanceEnquiry(v: View) {
        readCard("balanceEnquiry")
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    override fun onResume() {
        super.onResume()
        val savedKeyHolder = Prefs.getString("pref_keyholder", "KeyHolderWasn'tSaved")
        val savedConfigData = Prefs.getString("pref_config_data", "ConfigDataWasn'tSaved")

        Timber.d("SAVED_KEYHOLDER_ON_RESUME%s", savedKeyHolder)
        Timber.d("SAVED_CONFIG_DATA_ON_RESUME%s", savedConfigData)
    }

    private fun readCard(action: String) {
        val preferredModes: MutableList<Int> = ArrayList()
        preferredModes.add(POIEmvCoreManager.DEV_ICC)
        preferredModes.add(POIEmvCoreManager.DEV_PICC)
        val cardReader = CardReaderService(this, preferredModes, 45)
        val amount = if (editAmount.text == null || editAmount.text.toString()
            .toLong() < 15
        ) 15L else editAmount.text.toString().toLong()
        compositeDisposable.add(
            cardReader.initiateICCCardPayment(amount, 0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it is CardReaderEvent.CardDetected) {
                        val mode: Int = it.mode
                        var cardReadVia = ""
                        when (mode) {
                            POIEmvCoreManager.DEV_ICC -> cardReadVia = "Emv Chip"
                            POIEmvCoreManager.DEV_PICC -> cardReadVia = "Contactless"
                            POIEmvCoreManager.DEV_MAG -> cardReadVia = "Magnetic Stripe"
                        }
                        Toast.makeText(this, "$cardReadVia Detected Card", Toast.LENGTH_SHORT)
                            .show()
                    } else if (it is CardReaderEvent.CardRead) {
                        Timber.d("CARD_DATA%s", Gson().toJson(it.data))
                        cardResult = it.data
                        Timber.e(cardResult.toString())
                        val cardData = it.data.let { data ->
                            CardData(
                                data.track2Data!!,
                                data.nibssIccSubset,
                                data.applicationPANSequenceNumber!!
                            )
                        }
                        if (action == "makePayment") {
                            callMakePayment(
                                cardData,
                                it.data.encryptedPinBlock!!,
                                it.data.cardScheme!!,
                                editAmount.text.toString().toLong().times(100)
                            )
                        } else {
//                            checkBalance(it.data)
                        }
                    }
                }
        )
    }

    private fun callMakePayment(
        cardData: CardData,
        pinBlock: String,
        cardScheme: String,
        amountToPay: Long
    ) {
        val loaderDialog: LoadingDialog = LoadingDialog()
        loaderDialog.loadingMessage = getString(R.string.processing_payment)
        loaderDialog.show(supportFragmentManager, AppConstant.TAG_MAKE_PAYMENT)

        val makePaymentParams =
            cardData.let { cdData ->
                previousAmount = amountToPay
                MakePaymentParams(
                    amount = amountToPay,
                    terminalId = userData.terminalId,
                    cardData = cdData,
                    accountType = IsoAccountType.SAVINGS
                )
            }
        cardData.pinBlock = pinBlock
        compositeDisposable.add(
            netposPaymentClient.makePayment(
                this,
                userData.terminalId,
                gson.toJson(makePaymentParams),
                cardScheme,
                AppConstant.CARD_HOLDER_NAME,
                "TESTING_TESTING"
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { transactionWithRemark ->
                        loaderDialog.dismiss()
                        resultViewerTextView.text = gson.toJson(transactionWithRemark)
                        Timber.d(
                            "$PAYMENT_SUCCESS_DATA_TAG%s",
                            gson.toJson(transactionWithRemark)
                        )
                    },
                    { throwable ->
                        loaderDialog.dismiss()
                        resultViewerTextView.text = throwable.localizedMessage
                        Timber.d(
                            "${AppConstant.PAYMENT_ERROR_DATA_TAG}%s",
                            throwable.localizedMessage
                        )
                    }
                )
        )
    }

//    private fun checkBalance(cardReadResult: CardReadResult): Disposable {
//        val loaderDialog: LoadingDialog = LoadingDialog()
//        loaderDialog.loadingMessage = getString(R.string.checking_balance)
//        loaderDialog.show(supportFragmentManager, TAG_CHECK_BALANCE)
//        val cardData = cardReadResult.let {
//            CardData(
//                it.track2Data!!,
//                it.nibssIccSubset,
//                it.applicationPANSequenceNumber!!,
//                AppConstant.POS_ENTRY_MODE
//            ).also { cardD ->
//                cardD.pinBlock = it.encryptedPinBlock
//            }
//        }
//        return netposPaymentClient.balanceEnquiry(this, cardData, IsoAccountType.SAVINGS.name)
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe { data, error ->
//                data?.let {
//                    loaderDialog.dismiss()
//                    val responseString = if (it.responseCode == Status.APPROVED.statusCode) {
//                        "Response: APPROVED\nResponse Code: ${it.responseCode}\n\nAccount Balance:\n" + it.accountBalances.joinToString(
//                            "\n"
//                        ) { accountBalance ->
//                            "${accountBalance.accountType}: ${
//                            accountBalance.amount.div(100).formatCurrencyAmount()
//                            }"
//                        }
//                    } else {
//                        "Response: ${it.responseMessage}\nResponse Code: ${it.responseCode}"
//                    }
//                    resultViewerTextView.text = responseString
//                }
//                error?.let {
//                    loaderDialog.dismiss()
//                    resultViewerTextView.text = it.localizedMessage
//                }
//            }
//    }
}

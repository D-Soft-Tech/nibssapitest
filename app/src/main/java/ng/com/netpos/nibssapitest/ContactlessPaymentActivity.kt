package ng.com.netpos.nibssapitest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.danbamitale.epmslib.entities.CardData
import com.danbamitale.epmslib.entities.clearPinKey
import com.danbamitale.epmslib.extensions.formatCurrencyAmount
import com.google.gson.Gson
import com.netpluspay.contactless.sdk.start.ContactlessSdk
import com.netpluspay.contactless.sdk.utils.ContactlessReaderResult
import com.netpluspay.nibssclient.models.IsoAccountType
import com.netpluspay.nibssclient.models.MakePaymentParams
import com.netpluspay.nibssclient.models.UserData
import com.netpluspay.nibssclient.service.NetposPaymentClient
import com.pixplicity.easyprefs.library.Prefs
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ng.com.netpos.nibssapitest.AppConstant.CARD_HOLDER_NAME
import ng.com.netpos.nibssapitest.AppConstant.CONFIG_DATA
import ng.com.netpos.nibssapitest.AppConstant.ERROR_TAG
import ng.com.netpos.nibssapitest.AppConstant.KEY_HOLDER
import ng.com.netpos.nibssapitest.AppConstant.PAYMENT_ERROR_DATA_TAG
import ng.com.netpos.nibssapitest.AppConstant.PAYMENT_SUCCESS_DATA_TAG
import ng.com.netpos.nibssapitest.AppConstant.POS_ENTRY_MODE
import ng.com.netpos.nibssapitest.AppConstant.TAG_CHECK_BALANCE
import ng.com.netpos.nibssapitest.AppConstant.TAG_MAKE_PAYMENT
import ng.com.netpos.nibssapitest.AppConstant.TAG_TERMINAL_CONFIGURATION
import ng.com.netpos.nibssapitest.AppConstant.getSampleUserData
import ng.com.netpos.nibssapitest.AppConstant.getSavedKeyHolder
import ng.com.netpos.nibssapitest.data.models.CardResult
import ng.com.netpos.nibssapitest.data.models.Status
import ng.com.netpos.nibssapitest.presentation.dialog.LoadingDialog
import timber.log.Timber

class ContactlessPaymentActivity : AppCompatActivity() {
    private val gson: Gson = Gson()
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var previousAmount: Long? = null
    private var userData: UserData = getSampleUserData()
    private lateinit var makePaymentButton: Button
    private lateinit var balanceEnquiryButton: Button
    private lateinit var resultViewerTextView: TextView
    private lateinit var amountET: EditText
    var netposPaymentClient: NetposPaymentClient = NetposPaymentClient
    private val makePaymentResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            if (result.resultCode == ContactlessReaderResult.RESULT_OK) {
                data?.let { i ->
                    val amountToPay = amountET.text.toString().toLong()
                    amountET.text.clear()
                    val cardReadData = i.getStringExtra("data")!!
                    val cardResult = gson.fromJson(cardReadData, CardResult::class.java)
                    makePayment(cardResult, amountToPay)
                }
            }
            if (result.resultCode == ContactlessReaderResult.RESULT_ERROR) {
                data?.let { i ->
                    val error = i.getStringExtra("data")
                    error?.let {
                        Timber.d("ERROR_TAG===>%s", it)
                        resultViewerTextView.text = it
                    }
                }
            }
        }

    private val checkBalanceResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            if (result.resultCode == ContactlessReaderResult.RESULT_OK) {
                data?.let { i ->
                    val cardReadData = i.getStringExtra("data")!!
                    val cardResult = gson.fromJson(cardReadData, CardResult::class.java)
                    checkBalance(cardResult)
                }
            }
            if (result.resultCode == ContactlessReaderResult.RESULT_ERROR) {
                data?.let { i ->
                    val error = i.getStringExtra("data")
                    error?.let {
                        Timber.d("ERROR_TAG===>%s", it)
                        resultViewerTextView.text = it
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contactless_payment)
        initializeViews()
        configureTerminal()
        netposPaymentClient.logUser(this, gson.toJson(userData))
        makePaymentButton.setOnClickListener {
            resultViewerTextView.text = ""
            if (amountET.text.isNullOrEmpty() || amountET.text.toString().toLong() < 200L) {
                Toast.makeText(this, getString(R.string.enter_valid_amount), Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }
            val amountToPay = amountET.text.toString().toLong().toDouble()

            launchContactless(makePaymentResultLauncher, amountToPay)
        }

        balanceEnquiryButton.setOnClickListener {
            launchContactless(checkBalanceResultLauncher, 200.0)
        }
    }

    private fun makePayment(cardResult: CardResult, amountToPay: Long) {
        val loaderDialog: LoadingDialog = LoadingDialog()
        loaderDialog.loadingMessage = getString(R.string.processing_payment)
        loaderDialog.show(supportFragmentManager, TAG_MAKE_PAYMENT)
        val cardData = cardResult.cardReadResult.let {
            CardData(it.track2Data, it.iccString, it.pan, POS_ENTRY_MODE)
        }

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
        cardData.pinBlock = cardResult.cardReadResult.pinBlock
        compositeDisposable.add(
            netposPaymentClient.makePayment(
                this,
                userData.terminalId,
                gson.toJson(makePaymentParams),
                cardResult.cardScheme,
                CARD_HOLDER_NAME,
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
                            "$PAYMENT_ERROR_DATA_TAG%s",
                            throwable.localizedMessage
                        )
                    }
                )
        )
    }

    private fun launchContactless(
        launcher: ActivityResultLauncher<Intent>,
        amountToPay: Double,
        cashBackAmount: Double = 0.0
    ) {
        val savedKeyHolder = getSavedKeyHolder()

        savedKeyHolder?.run {
            ContactlessSdk.readContactlessCard(
                this@ContactlessPaymentActivity,
                launcher,
                this.clearPinKey, // "86CBCDE3B0A22354853E04521686863D" // pinKey
                amountToPay, // amount
                cashBackAmount // cashbackAmount(optional)
            )
        } ?: run {
            Toast.makeText(
                this,
                getString(R.string.terminal_not_configured),
                Toast.LENGTH_LONG
            ).show()
            configureTerminal()
        }
    }

    private fun initializeViews() {
        makePaymentButton = findViewById(R.id.read_card_btn)
        balanceEnquiryButton = findViewById(R.id.check_balance_btn)
        resultViewerTextView = findViewById(R.id.result_tv)
        amountET = findViewById(R.id.amountToPay)
    }

    private fun configureTerminal() {
        val loaderDialog: LoadingDialog = LoadingDialog()
        loaderDialog.loadingMessage = getString(R.string.configuring_terminal)
        loaderDialog.show(supportFragmentManager, TAG_TERMINAL_CONFIGURATION)
        compositeDisposable.add(
            netposPaymentClient.init(this, Gson().toJson(userData))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { data, error ->
                    data?.let { response ->
                        Timber.d("%s%s", TAG_TERMINAL_CONFIGURATION, gson.toJson(response))
                        Toast.makeText(
                            this,
                            getString(R.string.terminal_configured),
                            Toast.LENGTH_LONG
                        ).show()
                        loaderDialog.dismiss()
                        val keyHolder = response.first
                        val configData = response.second
                        val pinKey = keyHolder?.clearPinKey
                        if (pinKey != null) {
                            Prefs.putString(KEY_HOLDER, gson.toJson(keyHolder))
                            Prefs.putString(CONFIG_DATA, gson.toJson(configData))
                        }
                    }
                    error?.let {
                        Toast.makeText(
                            this,
                            getString(R.string.terminal_config_failed),
                            Toast.LENGTH_LONG
                        ).show()
                        loaderDialog.dismiss()
                        Timber.d("%s%s", ERROR_TAG, it.localizedMessage)
                    }
                }
        )
    }

    private fun checkBalance(cardResult: CardResult) {
        val loaderDialog: LoadingDialog = LoadingDialog()
        loaderDialog.loadingMessage = getString(R.string.checking_balance)
        loaderDialog.show(supportFragmentManager, TAG_CHECK_BALANCE)
        val cardData = cardResult.cardReadResult.let {
            CardData(it.track2Data, it.iccString, it.pan, POS_ENTRY_MODE).also { cardD ->
                cardD.pinBlock = it.pinBlock
            }
        }
        compositeDisposable.add(
            netposPaymentClient.balanceEnquiry(this, cardData, IsoAccountType.SAVINGS.name)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { data, error ->
                    data?.let {
                        loaderDialog.dismiss()
                        val responseString = if (it.responseCode == Status.APPROVED.statusCode) {
                            "Response: APPROVED\nResponse Code: ${it.responseCode}\n\nAccount Balance:\n" + it.accountBalances.joinToString(
                                "\n"
                            ) { accountBalance ->
                                "${accountBalance.accountType}: ${
                                accountBalance.amount.div(100).formatCurrencyAmount()
                                }"
                            }
                        } else {
                            "Response: ${it.responseMessage}\nResponse Code: ${it.responseCode}"
                        }
                        resultViewerTextView.text = responseString
                    }
                    error?.let {
                        loaderDialog.dismiss()
                        resultViewerTextView.text = it.localizedMessage
                    }
                }
        )
    }
}

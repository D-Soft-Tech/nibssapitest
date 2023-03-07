package ng.com.netpos.nibssapitest

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.danbamitale.epmslib.entities.* // ktlint-disable no-wildcard-imports
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
import com.netpluspay.nibssclient.work.ModelObjects.disposeWith
import com.pixplicity.easyprefs.library.Prefs
import com.pos.sdk.emvcore.POIEmvCoreManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    var netposPaymentClient: NetposPaymentClient = NetposPaymentClient
    private lateinit var editAmount: EditText
    private lateinit var reversalRrn: EditText
    private lateinit var reversalBtn: Button
    private lateinit var callHomeButton: Button
    private lateinit var userData: UserData
    private var cardData: CardData? = null
    private var previousAmount: Long? = null
    private lateinit var cardResult: CardReadResult
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        editAmount = findViewById(R.id.editTextNumber)
        reversalBtn = findViewById(R.id.reversal_button)
        callHomeButton = findViewById(R.id.call_home_btn)
        userData = UserData(
            businessName = "Netplus",
            partnerName = "Netplus",
            partnerId = "5de231d9-1be0-4c31-8658-6e15892f2b83",
            terminalId = "2033ALZP",
            terminalSerialNumber = NetPosSdk.getDeviceSerial(),
            businessAddress = "Marwa Lagos",
            customerName = "Test Account"
        )

        callHomeButton.setOnClickListener {
            val savedKeyHolder = Prefs.getString("pref_keyholder", "")
            val keyHolder = Gson().fromJson<KeyHolder>(savedKeyHolder, KeyHolder::class.java)
            NetposPaymentClient.callHomeToRefreshSessionKeys(
                this,
                "2033ALZP",
                keyHolder.clearSessionKey,
                NetPosSdk.getDeviceSerial()
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { t1, t2 ->
                    t1?.let {
                        Timber.d("CALL_HOME_RESPONSE=====>%s", it)
                        Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                    }
                    t2?.let {
                        Timber.d("CALL_HOME_ERROR====>%s", it.localizedMessage)
                    }
                }.disposeWith(compositeDisposable)
        }

        reversalRrn = findViewById(R.id.reversl_editText)

        reversalBtn.setOnClickListener {
            val makePaymentParams = MakePaymentParams(
                amount = previousAmount!!,
                terminalId = userData.terminalId,
                cardData = cardData!!,
                accountType = IsoAccountType.SAVINGS
            )
//            NewNibssApiWrapper.triggerReversalForLastTransaction(
//                this,
//                reversalRrn.text.toString(),
//                userData.terminalId,
//                Gson().toJson(makePaymentParams)
//            ).subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe { t1, t2 ->
//                    t1?.let {
//                        Toast.makeText(this, Gson().toJson(it), Toast.LENGTH_LONG).show()
//                        Timber.d("REVERSAL_RESPONSE====>%s", Gson().toJson(it))
//                    }
//                    t2?.let {
//                        Timber.d("REVERSAL_ERROR====>%s", it.localizedMessage)
//                    }
//                }.disposeWith(compositeDisposable)
        }

        netposPaymentClient.logUser(this, Gson().toJson(userData))
        compositeDisposable.add(
            netposPaymentClient.init(this, false, Gson().toJson(userData))
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
        readCard()
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

    private fun readCard() {
        val preferredModes: MutableList<Int> = ArrayList()
        preferredModes.add(POIEmvCoreManager.DEV_ICC)
        preferredModes.add(POIEmvCoreManager.DEV_PICC)
        val cardReader = CardReaderService(this, preferredModes, 45)
        compositeDisposable.add(
            cardReader.initiateICCCardPayment(200, 0)
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

                        val card = cardResult.track2Data?.let {
                            cardResult.applicationPANSequenceNumber?.let { it1 ->
                                CardData(
                                    it,
                                    cardResult.nibssIccSubset,
                                    it1,
                                    "051"
                                )
                            }
                        }

                        val makePaymentParams =
                            card?.let { cdData ->
                                cardData = cdData
                                previousAmount = editAmount.text.toString().toInt().times(100L)
                                MakePaymentParams(
                                    amount = editAmount.text.toString().toInt().times(100L),
                                    terminalId = userData.terminalId,
                                    cardData = cdData,
                                    accountType = IsoAccountType.SAVINGS
                                )
                            }
                        cardResult.cardScheme?.let { it1 ->
                            cardResult.cardHolderName?.let { it2 ->
                                netposPaymentClient.makePayment(
                                    this,
                                    userData.terminalId,
                                    Gson().toJson(makePaymentParams),
                                    it1,
                                    it2,
                                    "TestingAgain"
                                ).subscribeOn(Schedulers.io())
                                    .doOnError { error ->
                                        Timber.d("ERROR_THAT_HAPPENED===>%s", Gson().toJson(error))
                                    }
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                        { transactionWithRemark ->
                                            Toast.makeText(
                                                this,
                                                transactionWithRemark.toString(),
                                                Toast.LENGTH_LONG
                                            ).show()
                                            Timber.d("DATA==>${Gson().toJson(transactionWithRemark)}")
                                        },
                                        { throwable ->
                                            Timber.d("ERROR==>${throwable.localizedMessage}")
                                        }
                                    )
                            }?.let { it3 ->
                                compositeDisposable.add(
                                    it3
                                )
                            }
                        }
                    }
                }
        )
    }
}

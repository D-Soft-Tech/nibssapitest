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
                                            if (transactionWithRemark != null) {
                                                Timber.d(
                                                    "TIME===>%s",
                                                    Gson().toJson(
                                                        NetposPaymentClient.getDateObject(
                                                            transactionWithRemark.transmissionDateTime
                                                        )
                                                    )
                                                )
                                            }
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

    fun makePayment2(v: View) {
        val kudivisaeerr =
            "820238009F360200939F2701809F34034103029F1E086D6F726566756E319F100706011203A4B0109F3303E0F8C89F3501229F37045FD9345A9F01063132333435369F03060000000000008104000000C89F02060000000002005F24032311305F25032010205A0848484211638175015F3401019F150230319F160F3030303030303030303030303030309F1A0205669F1C08313233313233313257104848421163817501D2311226180133195F2A0205669F21031541179C01008E1800000000000000004105440502054103440342035E031F029F0D0598409C98009F0E0500100000009F4005FF80F000019F2608A924F203659E06759F0702FF809A032102225F280205669F090200009F4104000000009F0F0598409C98005F201A434F534D494320494E54454C4C4947454E542F504F5320544541950508800000009B02E8009F0607A0000000031010500C5669736120507265706169648407A0000000031010"
        // val iccData = "820238009F360201249F2701809F34034103029F1E086D6F726566756E319F100706010A03A4A0009F3303E0F8C89F3501229F370487336E659F01063132333435369F03060000000000008104000000649F02060000000001005F24032309305F25032009015A0841874518029592725F3401029F150230319F160F3030303030303030303030303030309F1A0205669F1C08313233313233313257104187451802959272D2309226101571235F2A0205669F21031651309C01008E1200000000000000004103440342035E031F039F0D05B860AC88009F0E0500100000009F4005FF80F000019F26082D2CA6E024FDA78C9F0702FF809A032101285F280205669F090200009F4104000000009F0F05B868BC98005F20084C41425320342F49950508800000009B02E8009F0607A0000000031010500A564953412044454249548407A0000000031010"
        val iccDatak =
            "9F2608E80508DAE3C5620D9F2701809F10120110A50003040000000000000000000000FF9F3704AB8051D79F3602021E950508C00080009A032101299C01009F02060000000000015F2A020566820238009F1A0205669F03060000000000009F330360D0C89F34034103029F3501228407A00000000410109F4104000000079F090200209F1E086D665F3630622020"
        val iccDataKudiVerve =
            "820258009F360200F79F2701809F34034103029F1E086D6F726566756E319F10200FA501A239F8040000000000000000000F0100000000000000000000000000009F3303E0F8C89F3501229F37041696E6159F01063132333435369F03060000000000008104000000649F02060000000001005F24032306305F25032006015A0A5061049133864459770F5F3401019F150230319F160F3030303030303030303030303030309F1A0205669F1C08313233313233313257125061049133864459770D23066010135308315F2A0205669F21030829149C01008E0E0000C35000000000410342031F069F0D05F04064A0009F0E0500108800009F4005FF80F000019F260846C0194C3449A6909F0702FF009A032101295F280205669F090200009F4104000000099F0F05F06064F8005F20083030303034303135950542800000009B02E8009F0607A000000371000150095665727665204350418407A0000003710001"
        val iccDataKudiVerve2 =
            "820258009F360200CA9F2701809F34034103029F1E086D6F726566756E319F10200FA501A039F8040000000000000000000F0F08010000000000000000000000009F3303E0F8C89F3501229F37047AD489019F01063132333435369F03060000000000008104000000649F02060000000001005F24032311305F25032011015A0A5061020454130998117F5F3401009F150230319F160F3030303030303030303030303030309F1A0205669F1C08313233313233313257125061020454130998117D23116010017576375F2A0205669F21031213169C01008E0E0000C35000000000410342031F069F0D05F04064A0009F0E0500108800009F4005FF80F000019F26081D8F768390862CF89F0702FF009A032101295F280205669F090200009F4104000000009F0F05F06064F8005F2012494E54454C4C4947454E542F434F534D4943950542800000009B02E8009F0607A000000371000150095665727665204350418407A0000003710001"
        val iccDataMeVerve =
            "57125061840800158084537D2111601016422918820258009F360200559F1E0842313739314531589F10200FA501A238F8040000000000000000000F0100000000000000000000000000009F3303E0F8C89F350122950542800080009F0106A000000000019F02060000000010005F24032111305A0A5061840800158084537F5F3401019F150239009F160F3030303030303030303030303030309F1A0205669F1C0831323334353637388104000003E85F2A0205669A032101299F21031054339C01005F20124F4C5557415441594F2F41444547424F59455F280205665F3401019F2608DCE80AD9804A63A49F2701809F34034103029F3704B499A82D9F03060000000000005F25031911019F4104000002439F0702FF008E0E0000C35000000000410342031F069F0D05F04064A0009F0E0500108800009F0F05F06064F8005F280205668407A00000037100019F0902008C9B02E800"
        val iccDataKudiVisa =
            "820238009F360201249F2701809F34034103029F1E086D6F726566756E319F100706010A03A4A0009F3303E0F8C89F3501229F370487336E659F01063132333435369F03060000000000008104000000649F02060000000001005F24032309305F25032009015A0841874518029592725F3401029F150230319F160F3030303030303030303030303030309F1A0205669F1C08313233313233313257104187451802959272D2309226101571235F2A0205669F21031651309C01008E1200000000000000004103440342035E031F039F0D05B860AC88009F0E0500100000009F4005FF80F000019F26082D2CA6E024FDA78C9F0702FF809A032101285F280205669F090200009F4104000000009F0F05B868BC98005F20084C41425320342F49950508800000009B02E8009F0607A0000000031010500A564953412044454249548407A0000000031010"
        var card = CardData.initCardDataFromTrack(iccDataMeVerve)
        card = CardData(
            card.track2Data,
            card.nibssIccSubset,
            card.panSequenceNumber,
            "051"
        )
        Timber.e(card.toString())
        val makePaymentParams =
            MakePaymentParams(
                terminalId = "2033ALZP",
                amount = 200,
                otherAmount = 0,
                transactionType = TransactionType.PURCHASE,
                accountType = IsoAccountType.CURRENT,
                cardData = card,
                remark = "Testing_from_bayo"
            )

        compositeDisposable.add(
            netposPaymentClient.makePayment(
                this,
                userData.terminalId,
                Gson().toJson(makePaymentParams),
                "Visa card",
                "OLOYEDE ADEBAYO",
                makePaymentParams.remark ?: "AAAAA"
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { t1, t2 ->
                    t1?.let {
                        Timber.d("RESPONSE====>%s", it.toString())
                    }
                }
        )
    }
}

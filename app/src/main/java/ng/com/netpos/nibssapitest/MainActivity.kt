package ng.com.netpos.nibssapitest

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.netpluspay.netpossdk.NetPosSdk
import com.netpluspay.netpossdk.utils.DeviceConfig
import com.netpluspay.nibssclient.models.*
import com.netpluspay.nibssclient.service.NibssApiWrapper
import com.netpluspay.nibssclient.service.SSLClientManager
import com.netpluspay.nibssclient.service.SocketClient
import com.netpluspay.nibssclient.util.app.NibssClient
import com.pos.sdk.accessory.POIGeneralAPI.*
import com.pos.sdk.printer.POIPrinterManage
import com.pos.sdk.printer.models.BitmapPrintLine
import com.pos.sdk.printer.models.PrintLine
import com.pos.sdk.printer.models.TextPrintLine
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            EasyPermissions.requestPermissions(
                this,
                "rat",
                100,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
        NibssClient.init("test-kudi.cert.pem", "test-kudi.key.pem", "2035S059", Build.ID)
        //NibssClient.setTestParams("68.183.135.207", 6868)
        NibssClient.useTestEnvironment(true)
        NibssClient.useSSL(true)
        val subscribe = Single.fromCallable {
            Timber.e("call socket")
            val s = SocketClient . write (
                    this,
            "{\"action\":\"getConfigData\",\"terminalId\":\"20390007\",\"terminalSerial\":\"1234567890\"}"
            )
            s
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { t1, t2 ->
                t1?.let {
                    Timber.e(it)
                }
                t2?.let {
                    Timber.e(it)
                }
            }
//        val subscribe1 = NibssApiWrapper.configureTerminal(this, ConfigurationParams())
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe { t1, t2 ->
//                t1?.let {
//                    Timber.e(it.toString())
//                }
//                t2?.let {
//                    Timber.e(it)
//                }
//            }
    }


    fun printSample() {
//        val poiGeneralApi = POIGeneralAPI.getDefault()
//        poiGeneralApi.setLed(LED_RED, true)
//        poiGeneralApi.setLed(LED_BLUE, true)
//        poiGeneralApi.setLed(LED_GREEN, true)
//        poiGeneralApi.setLed(LED_YELLOW, true)
//        poiGeneralApi.setLedFlash(LED_YELLOW, 10, 100)
//        poiGeneralApi.setBeep(true, 10, 100)
//        poiGeneralApi.getVersion(VERSION_TYPE_EXTERNAL_BASE)

        val printerManager = NetPosSdk.getPrinterManager()
        printerManager.apply {
            setLineSpace(2)
            setPrintGray(4000)
            cleanCache()
        }
        val bitmap = Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(resources, R.drawable.ic_netpos_logo),
            180,
            120,
            false
        )
        val bitmapPrintLine = BitmapPrintLine()
            .apply {
                type = PrintLine.BITMAP
                this.bitmap = bitmap
                position = PrintLine.CENTER
            }
        val textPrintLine = TextPrintLine().apply {
            type = PrintLine.TEXT
            content = "Cyberpunk"
            position = PrintLine.CENTER
            size = TextPrintLine.FONT_NORMAL
            isBold = false
            isItalic = false
            isInvert = false

        }
        //printerManager.addPrintLine(bitmapPrintLine)
        //printerManager.addPrintLine(textPrintLine)
        printerManager.apply {
            addPrintLine(bitmapPrintLine)
            addPrintLine(textPrintLine)
            addPrintLine(textPrintLine)
            addPrintLine(textPrintLine)
        }

        printerManager.beginPrint(object : POIPrinterManage.IPrinterListener {
            override fun onError(p0: Int, p1: String?) {
                //updateUi(p1)
                Timber.e("Printer Error with code: $p0 and message $p1")
                Toast.makeText(this@MainActivity, "Printer Error", Toast.LENGTH_SHORT).show()
            }

            override fun onFinish() {
                //updateUi("Printing finished")
                Toast.makeText(this@MainActivity, "Printing finished", Toast.LENGTH_SHORT).show()
            }

            override fun onStart() {
                //updateUi("")
                Toast.makeText(this@MainActivity, "Printing started", Toast.LENGTH_SHORT).show()
            }

        })
    }

    fun makePayment(view: View) {
        makePaymentWithIcc()
        MakePaymentParams(100, 0, null).apply {
            remark = ""
        }
    }

    fun makePaymentWithIcc() {
        val kudivisaeerr =
            "820238009F360200939F2701809F34034103029F1E086D6F726566756E319F100706011203A4B0109F3303E0F8C89F3501229F37045FD9345A9F01063132333435369F03060000000000008104000000C89F02060000000002005F24032311305F25032010205A0848484211638175015F3401019F150230319F160F3030303030303030303030303030309F1A0205669F1C08313233313233313257104848421163817501D2311226180133195F2A0205669F21031541179C01008E1800000000000000004105440502054103440342035E031F029F0D0598409C98009F0E0500100000009F4005FF80F000019F2608A924F203659E06759F0702FF809A032102225F280205669F090200009F4104000000009F0F0598409C98005F201A434F534D494320494E54454C4C4947454E542F504F5320544541950508800000009B02E8009F0607A0000000031010500C5669736120507265706169648407A0000000031010"
        //val iccData = "820238009F360201249F2701809F34034103029F1E086D6F726566756E319F100706010A03A4A0009F3303E0F8C89F3501229F370487336E659F01063132333435369F03060000000000008104000000649F02060000000001005F24032309305F25032009015A0841874518029592725F3401029F150230319F160F3030303030303030303030303030309F1A0205669F1C08313233313233313257104187451802959272D2309226101571235F2A0205669F21031651309C01008E1200000000000000004103440342035E031F039F0D05B860AC88009F0E0500100000009F4005FF80F000019F26082D2CA6E024FDA78C9F0702FF809A032101285F280205669F090200009F4104000000009F0F05B868BC98005F20084C41425320342F49950508800000009B02E8009F0607A0000000031010500A564953412044454249548407A0000000031010"
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
        val makePaymentParams = MakePaymentParams(200, 0, card)
        val subscribe = NibssApiWrapper.makePayment(context = this, params = makePaymentParams)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { t1, t2 ->
                t1?.let {
                    Timber.e(it.toString())
                    Timber.e(it.responseMessage)
                }
                t2?.let {
                    Timber.e(it)
                }
            }
    }


    fun configure(view: View) {
        //callHome()
        //printSample()
        val subscribe = NibssApiWrapper.configureTerminal(this, ConfigurationParams())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { t1, t2 ->
                t1?.let {
                    Timber.e("keyholder $it")
                    val keyHolder: KeyHolder = it
                    // NetPosSdk.writeTpkKey(DeviceConfig.TPKIndex, keyHolder.clearPinKey!!)
                }
                t2?.let {
                    Timber.e("keyholder error: $it")
                }
            }
    }

    fun callHome() {
        val subscribe = NibssApiWrapper.callHome(this, ConfigurationParams())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { t1, t2 ->
                t1?.let {
                    Timber.e(it)
                }
                t2?.let {
                    Timber.e(it)
                }
            }
    }

    fun makePayments(view: View) {
        val iccData =
            "9F02060000001000009F26089CE2B606A05985409F2701809F10120110A00003240000000000000000000000FF9F3704361F65389F360201D5950508400480009A032101219C01005F2A020156820238009F1A0201569F03060000000000009F33036040C89F34034203009F3501229F1E086D665F36306220208407A00000000410109F090200029F410400000000"
        //Timber.e(CardData.getNibssTags(iccData))
        val card = CardData.initCardDataFromTrack(iccData)
        Timber.e(card.toString())
        Timber.e("track2Data ${card.track2Data}")
        // Timber.e(CardData.getPan(card.track2Data))
        //Timber.e(CardData.getServiceCode(card.track2Data))
        //Timber.e(CardData.getAcquiringInstitutionIdCode(card.track2Data))
        //Timber.e(CardData.getExpiryDate(track2Data = card.track2Data))
    }

    fun makePaymentWithReader() {
        showCardDialog(this, 200, 0).observe(this) {
            Timber.e(it.toString())
            //Timber.e(it.cardData.toString())
            //Timber.e(CardData.getNibssTags("9F02060000001000009F26089CE2B606A05985409F2701809F10120110A00003240000000000000000000000FF9F3704361F65389F360201D5950508400480009A032101219C01005F2A020156820238009F1A0201569F03060000000000009F33036040C89F34034203009F3501229F1E086D665F36306220208407A00000000410109F090200029F410400000000"))
            //Timber.e(CardData.getNibssTags(it.cardReadResult.iccDataString))
            Timber.e("Pinblock: ${it.cardReadResult?.encryptedPinBlock}")
            val makePaymentParams = MakePaymentParams(200, 0, it.cardData)
            makePaymentParams.transactionType = TransactionType.PRE_AUTHORIZATION
            makePaymentParams.accountType = IsoAccountType.SAVINGS
            NibssApiWrapper.makePayment(this, params = makePaymentParams)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { t1, t2 ->
                    t1?.let {
                        Timber.e(it.toString())
                        Timber.e(it.responseMessage)
                    }
                    t2?.let {
                        Timber.e(it)
                    }
                }
        }
    }

}
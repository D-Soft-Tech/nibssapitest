package ng.com.netpos.nibssapitest

import com.danbamitale.epmslib.entities.KeyHolder
import com.dsofttech.dprefs.utils.DPrefs
import com.google.gson.Gson
import com.netpluspay.nibssclient.models.UserData

object AppConstant {
    const val KEY_HOLDER = "KEY_HOLDER"
    const val CONFIG_DATA = "CONFIG_DATA"
    const val ERROR_TAG = "ERROR_TAG===>"
    const val TAG_MAKE_PAYMENT = "TAG_MAKE_PAYMENT"
    const val TAG_CHECK_BALANCE = "TAG_CHECK_BALANCE"
    const val PAYMENT_SUCCESS_DATA_TAG = "PAYMENT_SUCCESS_DATA_TAG"
    const val PAYMENT_ERROR_DATA_TAG = "PAYMENT_ERROR_DATA_TAG"
    const val TAG_TERMINAL_CONFIGURATION = "TAG_TERMINAL_CONFIGURATION"
    const val CARD_HOLDER_NAME = "CUSTOMER"
    const val POS_ENTRY_MODE = "051"

    fun getSampleUserData() = UserData(
        businessName = "Netplus",
        partnerName = "Netplus",
        partnerId = "9E89FFBD-9968-4F69-96DB-4E1250F14D55",
        terminalId = "2035BDZ9",
        terminalSerialNumber = "1142016190002868", // getDeviceSerialNumber(),
        businessAddress = "Marwa Lagos",
        customerName = "Test Account",
        mid = "2033LAGPOOO7885",
        bankAccountNumber = "0169422762",
        institutionalCode = "627787",
    )

    fun getSavedKeyHolder(): KeyHolder? {
        val savedKeyHolderInStringFormat = DPrefs.getString(KEY_HOLDER, "")
        return Gson().fromJson(savedKeyHolderInStringFormat, KeyHolder::class.java)
    }

    val nibssApiTest = mapOf(
        "RrnUrl" to "https://getrrn.netpluspay.com",
        "TestKeyOne" to "5D25072F04832A2329D93E4F91BA23A2",
        "TestKeyTwo" to "86CBCDE3B0A22354853E04521686863D",
        "EpmsLiveKeyOne" to "E6891F73948F16C4D6E979D68534D0F4",
        "EpmsLiveKeyTwo" to "3D10EF707F98E3543E32B570E9E9AE86",
        "PosvasLiveKeyOne" to "9BF76D3E13ADD67A51549B7C3EB0E3AD",
        "PosvasLiveKeyTwo" to "A4BAEC5E31BFD913919262C7A7A76D52",
        "NibssIp" to "196.6.103.18",
        "NibssPort" to "5016",
        "NibssIp2" to "196.6.103.18",
        "NibssPort2" to "4016",
        "NibssTestUrl" to "epms.test.netpluspay.com",
        "NibssTestPort" to "6868",
        "NetPlusPayUrl" to "https://device.netpluspay.com/",
        "DefaultTid" to "2057H63U",
        "NibssTestIp" to "196.6.103.10",
        "NibssConnectionTestPort" to "55533",
        "NibssConnectionTestKeyOne" to "5D25072F04832A2329D93E4F91BA23A2",
        "NibssConnectionTestKeyTwo" to "86CBCDE3B0A22354853E04521686863D",
        "NibssConnectionTestTid" to "20398A4C",
        "SessionKey" to "3F2216D8297BCE9C",
        "IpekTest" to "9F8011E7E71E483B",
        "KsnTest" to "0000000006DDDDE01500",
        "KsnLive" to "0000000002DDDDE00001",
        "NibssTest" to "196.6.103.73",
    )

    val secretKeyModuleParams = mapOf(
        "Ip" to "196.6.103.10",
        "Port" to "55533",
        "DefaultIp" to "196.6.103.73",
        "DefaultPort" to "5043",
        "NibssIp" to "196.6.103.18",
        "NibssPort" to "4016",
        "NibssTestUrl" to "epms.test.netpluspay.com",
        "NibssTestPort" to "6868",
        "NetPlusPayUrl" to "https://device.netpluspay.com/",
        "DefaultTid" to "2057H63U",
        "NibssTestIp" to "196.6.103.10",
        "NibssConnectionTestPort" to "55533",
        "NibssConnectionTestKeyOne" to "5D25072F04832A2329D93E4F91BA23A2",
        "NibssConnectionTestKeyTwo" to "86CBCDE3B0A22354853E04521686863D",
        "NibssConnectionTestTid" to "20398A4C",
        "SessionKey" to "3F2216D8297BCE9C",
        "IpekTest" to "9F8011E7E71E483B",
        "KsnTest" to "0000000006DDDDE01500",
        "KsnLive" to "0000000002DDDDE00001",
    )
}

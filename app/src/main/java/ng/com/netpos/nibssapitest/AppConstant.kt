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
}

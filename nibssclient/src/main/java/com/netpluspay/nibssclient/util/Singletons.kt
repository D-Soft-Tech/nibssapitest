package com.netpluspay.nibssclient.util

import com.danbamitale.epmslib.entities.ConfigData
import com.danbamitale.epmslib.entities.KeyHolder
import com.danbamitale.epmslib.entities.TransactionResponse
import com.danbamitale.epmslib.entities.responseMessage
import com.google.gson.Gson
import com.netpluspay.nibssclient.models.ConfigurationData
import com.netpluspay.nibssclient.models.NibssResponse
import com.netpluspay.nibssclient.models.User
import com.netpluspay.nibssclient.nibss.Keys
import com.netpluspay.nibssclient.util.Constants.DEFAULT_TERMINAL_ID
import com.netpluspay.nibssclient.util.Constants.PREF_CONFIG_DATA
import com.netpluspay.nibssclient.util.Constants.PREF_KEYHOLDER
import com.netpluspay.nibssclient.util.Constants.PREF_PARTNER_THRESHOLD
import com.netpluspay.nibssclient.util.Constants.PREF_USER
import com.netpluspay.nibssclient.util.Constants.PREF_USE_STORM_TERMINAL_ID
import com.pixplicity.easyprefs.library.Prefs

fun useStormTerminalId() = Prefs.getBoolean(PREF_USE_STORM_TERMINAL_ID, true)
fun TransactionResponse.toNibssResponse(remark: String? = null): NibssResponse =
    Singletons.gson.fromJson(
        Singletons.gson.toJson(this),
        NibssResponse::class.java
    ).also {
        it.responseMessage = try {
            this.responseMessage
        } catch (e: Exception) {
            ""
        }
        it.additionalAmount = this.additionalAmount_54.toLongOrNull() ?: 0
        it.localDate = this.localDate_13
        it.localTime = this.localTime_12
        it.amount = this.amount.div(100)
        remark?.let { r ->
            it.remark = r
        }
    }

object Singletons {
    @JvmStatic
    fun getGsonInstance() = Gson()

    fun setPartnerThreshold(partnerThresh: Int) {
        Prefs.putInt(PREF_PARTNER_THRESHOLD, partnerThresh)
    }

    fun getPartnerThreshold() =
        Prefs.getInt(PREF_PARTNER_THRESHOLD, -10)

    fun setUseStormTid(useStormTid: Boolean) =
        Prefs.putBoolean(PREF_USE_STORM_TERMINAL_ID, useStormTid)

    val gson = Gson()
    fun getCurrentlyLoggedInUser(): User? =
        gson.fromJson(Prefs.getString(PREF_USER, ""), User::class.java)

    fun getSavedConfigurationData(): ConfigurationData {
        // LIVE ENDPOINT
        return ConfigurationData(
            UtilityParams.nibss_ip,
            UtilityParams.nibss_port.toString(),
            DEFAULT_TERMINAL_ID,
            Keys.posvasLiveKey1,
            Keys.posvasLiveKey2
        )

        // TEST ENDPOINT
//        return ConfigurationData(
//            UtilityParams.nibssConnectionTestIpAddress,
//            UtilityParams.nibssConnectionTestPortAddress.toString(),
//            UtilityParams.nibssTestTid,
//            UtilityParams.nibssTestKey1,
//            UtilityParams.nibssTestKey2
//        )
    }

    fun getKeyHolder(): KeyHolder? =
        gson.fromJson(Prefs.getString(PREF_KEYHOLDER, null), KeyHolder::class.java)

    fun settKeyHolder(keyHolder: KeyHolder) {
        Prefs.putString(PREF_KEYHOLDER, gson.toJson(keyHolder, KeyHolder::class.java))
    }

    fun getConfigData(): ConfigData? =
        gson.fromJson(Prefs.getString(PREF_CONFIG_DATA, null), ConfigData::class.java)

    fun setConfigData(configData: ConfigData) {
        Prefs.putString(PREF_CONFIG_DATA, gson.toJson(configData, ConfigData::class.java))
    }
}

var TransactionResponse.additionalAmount: Long?
    get() = 0
    set(value) {
    }

sealed class LoadingState

object LoadingMore : LoadingState()
object LoadingInitial : LoadingState()
object LoadingDone : LoadingState()
data class LoadingError(val errorMessage: String, val exception: Throwable) : LoadingState()

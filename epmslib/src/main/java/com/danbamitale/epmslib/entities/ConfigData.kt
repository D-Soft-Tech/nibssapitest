package com.danbamitale.epmslib.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class ConfigData(
    var epmsDateTime: String = "",
    var cardAcceptorIdCode: String = "",
    var hostTimeOut: String = "",
    var currencyCode: String = "",
    var countryCode: String = "",
    var callHomeTime: String = "",
    var merchantNameLocation: String = "",
    var merchantCategoryCode: String = ""
) : Parcelable {

    @PrimaryKey
    var id = 1

    companion object {
        @Ignore
        val TAG_LEN_EPMS_DATE_TIME = "02014"
        @Ignore
        val TAG_LEN_CARD_ACCEPTOR_ID_CODE = "03015"
        @Ignore
        val TAG_LEN_TIMEOUT = "04002"
        @Ignore
        val TAG_LEN_CURRENCY_CODE = "05003"
        @Ignore
        val TAG_LEN_COUNTRY_CODE = "06003"
        @Ignore
        val TAG_LEN_CALL_HOME_TIME = "07002"
        @Ignore
        val TAG_LEN_MERCHANT_NAME_LOCATION = "52040"
        @Ignore
        val TAG_LEN_MERCHANT_CATEGORY_CODE = "08004"
    }
}

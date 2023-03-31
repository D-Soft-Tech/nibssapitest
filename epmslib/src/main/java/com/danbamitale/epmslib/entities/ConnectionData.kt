package com.danbamitale.epmslib.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.danbamitale.epmslib.R
import com.danbamitale.epmslib.utils.Utility.POS_VAS_NIBSS_DEFAULT_IP
import com.danbamitale.epmslib.utils.Utility.POS_VAS_NIBSS_DEFAULT_PORT
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class ConnectionData
/**
 *
 * @param ipAddress
 * @param ipPort
 * @param isSSL
 * @param certFileResId
 */(
    var ipAddress: String = POS_VAS_NIBSS_DEFAULT_IP,
    var ipPort: Int = POS_VAS_NIBSS_DEFAULT_PORT,
    var isSSL: Boolean = true,
    var certFileResId: Int = R.raw.nibss_cert_live
) : Parcelable {
    @PrimaryKey
    var id = 1
}

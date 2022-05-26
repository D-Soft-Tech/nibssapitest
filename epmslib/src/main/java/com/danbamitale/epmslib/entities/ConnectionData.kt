package com.danbamitale.epmslib.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.danbamitale.epmslib.R
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
    var ipAddress: String = DEFAULT_NIBSS_IP,
    var ipPort: Int = DEFAULT_NIBSS_PORT,
    var isSSL: Boolean = true,
    var certFileResId: Int = R.raw.nibss_cert_live
) : Parcelable {
    @PrimaryKey
    var id = 1
}

const val DEFAULT_NIBSS_IP = "196.6.103.73"
const val DEFAULT_NIBSS_PORT = 5043

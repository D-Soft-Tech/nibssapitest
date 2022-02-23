package com.netpluspay.nibssclient.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class KeyHolder(
    val clearPinKey: String? = null,
    val pinKey: String? = null,
    @SerializedName("clearMasterkey")
    val masterKey: String? = null
) : Parcelable {
    @IgnoredOnParcel
    @PrimaryKey
    var id = 1
    val isValid: Boolean
        get() = clearPinKey != null && pinKey != null && masterKey != null
}

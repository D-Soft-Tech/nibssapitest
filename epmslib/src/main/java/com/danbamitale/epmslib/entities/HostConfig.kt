package com.danbamitale.epmslib.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class HostConfig(
    val terminalId: String,
    val connectionData: ConnectionData,
    var keyHolder: @RawValue KeyHolder,
    val configData: ConfigData
) : Parcelable

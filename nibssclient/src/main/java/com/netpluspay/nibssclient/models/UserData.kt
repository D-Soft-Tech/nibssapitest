package com.netpluspay.nibssclient.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserData(
    var businessName: String,
    var partnerName: String,
    var partnerId: String,
    var terminalId: String,
    var terminalSerialNumber: String,
    var businessAddress: String,
    var customerName: String,
    val mid: String,
    val institutionalCode: String,
    val bankAccountNumber: String
) : Parcelable

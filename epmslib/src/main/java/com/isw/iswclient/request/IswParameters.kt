package com.isw.iswclient.request

data class IswParameters @JvmOverloads constructor(
    val merchantId: String,
    val merchantNameLocation: String,
    val token: String? = "",
    val pinKey: String? = "",
    var terminalId: String = "",
    var terminalSerial: String = "",
    var destinationAccountNumber: String = "",
    var receivingInstitutionId: String = "",
    var interSwitchThreshold: Long = 0L,
    @Transient var remark: String = "",
)

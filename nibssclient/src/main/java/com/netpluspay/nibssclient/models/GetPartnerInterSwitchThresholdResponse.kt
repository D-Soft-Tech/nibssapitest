package com.netpluspay.nibssclient.models

data class GetPartnerInterSwitchThresholdResponse(
    val interSwitchThreshold: Int,
    val bankAccountNumber: String,
    val institutionalCode: String
)

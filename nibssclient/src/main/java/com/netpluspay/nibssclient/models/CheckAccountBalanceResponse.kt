package com.netpluspay.nibssclient.models

data class CheckAccountBalanceResponse(
    val responseCode: String,
    val responseMessage: String,
    val accountBalances: List<AccountBalanceResponse>
)

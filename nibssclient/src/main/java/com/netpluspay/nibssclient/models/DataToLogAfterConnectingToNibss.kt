package com.netpluspay.nibssclient.models

import com.google.gson.JsonObject
import java.util.* // ktlint-disable no-wildcard-imports

data class DataToLogAfterConnectingToNibss(
    val status: String,
    val transactionResponse: TransactionWithRemark,
    val rrn: String
)

data class LogToBackendResponse(
    val `data`: List<Int>,
    val message: String,
    val status: String
)

data class ResponseBodyAfterLoginToBackend(
    val message: String
)

data class TransactionWithRemark(
    val AID: String,
    val rrn: String,
    val STAN: String,
    val TSI: String,
    val TVR: String,
    val accountType: String,
    val acquiringInstCode: String,
    val additionalAmount_54: String,
    val amount: Int,
    val appCryptogram: String,
    val authCode: String,
    val cardExpiry: String,
    val cardHolder: String,
    val cardLabel: String,
    val id: Int,
    val localDate_13: String,
    val localTime_12: String,
    val maskedPan: String,
    val merchantId: String,
    val originalForwardingInstCode: String,
    val otherAmount: Int,
    val otherId: String,
    val responseCode: String,
    val responseDE55: String,
    var responseMessage: String,
    val terminalId: String,
    val transactionTimeInMillis: Long,
    val transactionType: String,
    val transmissionDateTime: String,
    val remark: String = ""
)

data class TransactionResponseX(
    val AID: String,
    val rrn: String,
    val STAN: String,
    val TSI: String,
    val TVR: String,
    val accountType: String,
    val acquiringInstCode: String,
    val additionalAmount_54: String,
    val amount: Int,
    val appCryptogram: String,
    val authCode: String,
    val cardExpiry: String,
    val cardHolder: String,
    val cardLabel: String,
    val id: Int,
    val localDate_13: String,
    val localTime_12: String,
    val maskedPan: String,
    val merchantId: String,
    val originalForwardingInstCode: String,
    val otherAmount: Int,
    val otherId: String,
    val responseCode: String,
    val responseDE55: String,
    var responseMessage: String,
    val terminalId: String,
    val transactionTimeInMillis: Long,
    val transactionType: String,
    val transmissionDateTime: String
) {
    fun mapToStormStructure(
        key: String,
        routingChan: String,
        stormId: String,
        transStatus: String,
        userType: String
    ): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty("AID", AID ?: "")
        jsonObject.addProperty("RRN", rrn ?: "")
        jsonObject.addProperty("STAN", STAN ?: "")
        jsonObject.addProperty("TSI", TSI ?: "")
        jsonObject.addProperty("TVR", TVR ?: "")
        jsonObject.addProperty("accountType", accountType ?: "")
        jsonObject.addProperty("acquiringInstCode", acquiringInstCode ?: "")
        jsonObject.addProperty("additionalAmount_54", additionalAmount_54 ?: "")
        jsonObject.addProperty("amount", (amount / 100).toDouble() ?: 0)
        jsonObject.addProperty("appCryptogram", appCryptogram ?: "")
        jsonObject.addProperty("authCode", authCode ?: "")
        jsonObject.addProperty("cardExpiry", cardExpiry ?: "")
        jsonObject.addProperty("cardHolder", cardHolder ?: "")
        jsonObject.addProperty("cardLabel", cardLabel ?: "")
        jsonObject.addProperty("id", id ?: 0)
        jsonObject.addProperty("key", key ?: "")
        jsonObject.addProperty("localDate_13", localDate_13 ?: "")
        jsonObject.addProperty("localTime_12", localTime_12 ?: "")
        jsonObject.addProperty("maskedPan", maskedPan ?: "")
        jsonObject.addProperty("merchantId", merchantId ?: "")
        jsonObject.addProperty("originalForwardingInstCode", originalForwardingInstCode ?: "")
        jsonObject.addProperty("otherAmount", otherAmount ?: 0)
        jsonObject.addProperty("otherId", otherId ?: "")
        jsonObject.addProperty("responseCode", responseCode ?: "")
        jsonObject.addProperty("responseDE55", responseDE55 ?: "")
        jsonObject.addProperty("routingChannel", routingChan.toUpperCase(Locale.ROOT) ?: "")
        jsonObject.addProperty("stormId", stormId ?: "")
        jsonObject.addProperty("terminalId", terminalId ?: "")
        jsonObject.addProperty("transactionStatus", transStatus.toLowerCase(Locale.ROOT) ?: "")
        jsonObject.addProperty("transactionTimeInMillis", transactionTimeInMillis ?: 0)
        jsonObject.addProperty("transactionType", transactionType ?: "")
        jsonObject.addProperty("transmissionDateTime", transmissionDateTime ?: "")
        jsonObject.addProperty("userType", userType ?: "")

        return jsonObject
    }
}

data class TransactionToLogBeforeConnectingToNibbs(
    val status: String,
    val transactionResponse: TransactionWithRemark
)

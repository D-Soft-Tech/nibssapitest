package com.netpluspay.nibssclient.models

import com.danbamitale.epmslib.entities.PosMode
import com.danbamitale.epmslib.entities.TransactionResponse
import com.danbamitale.epmslib.entities.TransactionType
import com.danbamitale.epmslib.entities.responseMessage
import com.danbamitale.epmslib.utils.IsoAccountType
import com.google.gson.JsonObject
import java.util.*

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

fun mapToTransactionResponse(transWithRemark: TransactionWithRemark) =
    TransactionResponse().apply {
        transactionType = TransactionType.valueOf(transWithRemark.transactionType)
        maskedPan = transWithRemark.maskedPan
        amount = transWithRemark.amount.toLong()
        transmissionDateTime = transWithRemark.transmissionDateTime
        STAN = transWithRemark.STAN
        RRN = transWithRemark.rrn
        localTime_12 = transWithRemark.localTime_12
        localDate_13 = transWithRemark.localDate_13
        otherAmount = transWithRemark.otherAmount.toLong()
        acquiringInstCode = transWithRemark.acquiringInstCode
        originalForwardingInstCode = transWithRemark.originalForwardingInstCode
        authCode = transWithRemark.authCode
        responseCode = transWithRemark.responseCode
        additionalAmount_54 = transWithRemark.additionalAmount_54
        echoData = ""
        cardLabel = transWithRemark.cardLabel
        cardExpiry = transWithRemark.cardExpiry
        cardHolder = transWithRemark.cardHolder
        TVR = transWithRemark.TVR
        TSI = transWithRemark.TSI
        AID = transWithRemark.AID
        appCryptogram = transWithRemark.appCryptogram
        transactionTimeInMillis = 0L
        accountType = IsoAccountType.valueOf(transWithRemark.accountType)
        terminalId = transWithRemark.terminalId
        merchantId = transWithRemark.merchantId
        otherId = transWithRemark.otherId

        id = transWithRemark.id.toLong()
        responseDE55 = transWithRemark.responseDE55
        source = PosMode.EPMS
        interSwitchThreshold = 0L
    }

fun mapTransactionResponseToTransactionWithRemark(transWithRemark: TransactionResponse): TransactionWithRemark {
    return TransactionWithRemark(
        transactionType = transWithRemark.transactionType.name,
        maskedPan = transWithRemark.maskedPan,
        amount = transWithRemark.amount.toInt(),
        transmissionDateTime = transWithRemark.transmissionDateTime,
        STAN = transWithRemark.STAN,
        rrn = transWithRemark.RRN,
        localTime_12 = transWithRemark.localTime_12,
        localDate_13 = transWithRemark.localDate_13,
        otherAmount = transWithRemark.otherAmount.toInt(),
        acquiringInstCode = transWithRemark.acquiringInstCode,
        originalForwardingInstCode = transWithRemark.originalForwardingInstCode,
        authCode = transWithRemark.authCode,
        responseCode = transWithRemark.responseCode,
        additionalAmount_54 = transWithRemark.additionalAmount_54,
        cardLabel = transWithRemark.cardLabel,
        cardExpiry = transWithRemark.cardExpiry,
        cardHolder = transWithRemark.cardHolder,
        TVR = transWithRemark.TVR,
        TSI = transWithRemark.TSI,
        AID = transWithRemark.AID,
        appCryptogram = transWithRemark.appCryptogram,
        transactionTimeInMillis = transWithRemark.transactionTimeInMillis,
        accountType = transWithRemark.accountType.name,
        terminalId = transWithRemark.terminalId,
        merchantId = transWithRemark.merchantId,
        otherId = transWithRemark.otherId,
        id = transWithRemark.id.toInt(),
        responseDE55 = transWithRemark.responseDE55 ?: "",
        responseMessage = transWithRemark.responseMessage
    )
}

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

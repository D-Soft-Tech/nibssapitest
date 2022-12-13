package com.netpluspay.nibssclient.util

import com.danbamitale.epmslib.entities.TransactionRequestData
import com.danbamitale.epmslib.entities.TransactionType
import com.danbamitale.epmslib.utils.IsoTimeManager
import com.netpluspay.nibssclient.models.* // ktlint-disable no-wildcard-imports
import com.netpluspay.nibssclient.util.RandomNumUtil.formattedTime
import com.netpluspay.nibssclient.util.RandomNumUtil.generateRandomRrn
import com.netpluspay.nibssclient.util.RandomNumUtil.getCurrentDateTime
import java.io.ByteArrayOutputStream
import java.text.NumberFormat
import java.util.* // ktlint-disable no-wildcard-imports

object Utility {

    fun hex(data: ByteArray): String {
        val sb = StringBuilder()
        for (b in data) {
            sb.append(Character.forDigit(b.toInt() and 240 shr 4, 16))
            sb.append(Character.forDigit(b.toInt() and 15, 16))
        }
        return sb.toString()
    }

    fun toHexString(b: ByteArray): String {
        var result = ""
        for (i in b.indices) {
            result += Integer.toString((b[i].toInt() and 0xFF) + 0x100, 16).substring(1)
        }
        return result
    }

    fun hexToByteArray(s: String?): ByteArray {
        var s = s
        if (s == null) {
            s = ""
        }
        val bout = ByteArrayOutputStream()
        var i = 0
        while (i < s.length - 1) {
            val data = s.substring(i, i + 2)
            bout.write(Integer.parseInt(data, 16))
            i += 2
        }
        return bout.toByteArray()
    }

    fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] =
                ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    fun parseLongIntoNairaKoboString(tempAmount: Long, currencySymbol: String = "\u20A6"): String {
        val amountNairaPart = tempAmount / 100.0

        val numFormatter = NumberFormat.getInstance(Locale.US)
        numFormatter.minimumFractionDigits = 2

        var amountInN = numFormatter.format(amountNairaPart)

        amountInN = currencySymbol + amountInN

        return amountInN
    }

    fun gatewayErrorTransactionResponse(
        amount: Long = 0,
        transactionType: TransactionType = TransactionType.PURCHASE,
        accountType: IsoAccountType = IsoAccountType.DEFAULT_UNSPECIFIED,
        errorMessage: String? = null
    ) = TransactionResponse().apply {
        this.transactionType = transactionType
        this.responseCode = "A0"
        this.RRN = "000000000000"
        this.STAN = "000000"
        this.AID = ""
        this.TSI = ""
        this.TVR = ""
        this.accountType = accountType
        this.transactionTimeInMillis = System.currentTimeMillis()
        this.acquiringInstCode = ""
        this.amount = amount
        this.errorMessage = errorMessage
    }

    fun getCustomRrn() = IsoTimeManager().fullDate.substring(2, 14)

    fun MakePaymentParams.getTransactionResponseToLog(
        cardScheme: String,
        requestData: TransactionRequestData,
        rrn: String?
    ) =
        this.cardData.expiryDate.let {
            val mid = Singletons.getConfigData()?.cardAcceptorIdCode ?: ""
            SharedPrefManager.getUserData().customerName.let { it1 ->
                TransactionToLogBeforeConnectingToNibbs(
                    status = "PENDING",
                    TransactionWithRemark(
                        AID = "",
                        rrn = rrn ?: getCustomRrn(),
                        STAN = generateRandomRrn(6),
                        TSI = "",
                        TVR = "",
                        accountType = accountType.name,
                        acquiringInstCode = "",
                        additionalAmount_54 = "",
                        amount = amount.toInt(),
                        appCryptogram = "",
                        authCode = "",
                        cardExpiry = it,
                        cardHolder = it1,
                        cardLabel = cardScheme.toString(),
                        id = 0,
                        localDate_13 = RandomNumUtil.getDate(),
                        localTime_12 = formattedTime.replace(":", ""),
                        maskedPan = cardData.pan.maskPan(),
                        merchantId = mid,
                        originalForwardingInstCode = "",
                        otherAmount = requestData.otherAmount.toInt(),
                        otherId = "",
                        responseCode = "99",
                        responseDE55 = "",
                        terminalId = SharedPrefManager.getUserData().terminalId,
                        transactionTimeInMillis = 0,
                        transactionType = requestData.transactionType.name,
                        transmissionDateTime = getCurrentDateTime(),
                        remark = remark ?: "",
                        responseMessage = ""
                    )
                )
            }
        }
}

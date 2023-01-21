package com.netpluspay.nibssclient.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.danbamitale.epmslib.entities.TransactionType
import com.netpluspay.nibssclient.util.Constants
import com.netpluspay.nibssclient.util.tlv.TLVList
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Entity(indices = [Index(value = ["RRN"], unique = true)])
@Parcelize
data class TransactionResponse(
    var transactionType: TransactionType,
    var maskedPan: String,
    var amount: Long,

    var transmissionDateTime: String,
    var STAN: String,
    var RRN: String,
    var localTime_12: String,
    var localDate_13: String,
    var otherAmount: Long = 0,
    var acquiringInstCode: String = "",
    var originalForwardingInstCode: String = "",
    var authCode: String = "",
    var responseCode: String = "",
    var additionalAmount_54: String = "",
    var echoData: String? = null,

    var cardLabel: String = "",
    var cardExpiry: String = "",
    var cardHolder: String = "",
    var TVR: String = "",
    var TSI: String = "",
    var AID: String = "",
    var appCryptogram: String = "",
    var transactionTimeInMillis: Long = 0L,
    var accountType: IsoAccountType = IsoAccountType.DEFAULT_UNSPECIFIED,

    var terminalId: String = "",
    var merchantId: String = "",
    var otherId: String = "",
    var transactionTime: String = "",
    var remark: String = "",

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @Ignore
    var responseDE55: String? = null
) : Parcelable {
    fun validateResponse() = responseCode.isEmpty().not()

    @IgnoredOnParcel
    var errorMessage: String? = null

    var source: String? = null

    constructor() : this(TransactionType.PURCHASE, "", 0L, "", "", "", "", "")
}

val TransactionResponse.isApproved: Boolean
    get() = responseCode == "00"

val TransactionResponse.responseMessage: String
    get() = errorMessage ?: Constants.getResponseMessageFromCode(responseCode)

val TransactionResponse.issuerAuthData91: String?
    get() {
        return responseDE55?.let {
            val tlvList = TLVList.fromBinary(it)
            tlvList.getTLVVL("91")
        }
    }

val TransactionResponse.issuerScript71: String?
    get() {
        return responseDE55?.let {
            val tlvList = TLVList.fromBinary(it)
            tlvList.getTLVVL("71")
        }
    }

val TransactionResponse.issuerScript72: String?
    get() {
        return responseDE55?.let {
            val tlvList = TLVList.fromBinary(it)
            tlvList.getTLVVL("72")
        }
    }

val TransactionResponse.accountBalances: List<AccountBalance>
    get() = parseField54AdditionalAmount(this.additionalAmount_54)

data class AccountBalance(
    val accountType: IsoAccountType,
    val amountType: String,
    val currencyCode: String,
    val amountSign: Char,
    val amount: Long
)

data class AccountBalanceResponse(
    val accountType: String,
    val amountType: String,
    val currencyCode: String,
    val amountSign: Char,
    val amount: Long
)

private fun parseAdditionalAmountString(inputString: String): AccountBalance {
    if (inputString.length < 20) error("Invalid string")

    val accountType = IsoAccountType.parseIntAccountType(inputString.substring(0, 2).toInt())
    val amountType = inputString.substring(2, 4)
    val currencyCode = inputString.substring(4, 7)
    val amountSign = inputString[7]
    val amount = inputString.substring(8, 20).toLong()

    return AccountBalance(accountType, amountType, currencyCode, amountSign, amount)
}

fun parseField54AdditionalAmount(inputString: String): List<AccountBalance> {
    if (inputString.length < 20) return listOf()

    val list = ArrayList<AccountBalance>()
    var count = 0
    do {
        list.add(parseAdditionalAmountString(inputString.substring(count, count + 20)))
        count += 20
    } while (count + 20 <= inputString.length)

    return list
}

fun currencySymbolFromCode(code: Int) = when (code) {
    566 -> "\u20A6 "
    840 -> "\u0024 "
    978 -> "\u20AC "
    826 -> "\u00A3 "
    156 -> "\u00A5 "
    36 -> "AUD "
    952 -> "XOF "
    950 -> "XAF "
    else -> "    "
}

data class OriginalDataElements(
    val originalTransactionType: TransactionType,
    val originalAmount: Long = 0L,
    val originalAuthorizationCode: String? = null,
    val originalTransmissionTime: String = "",
    val originalSTAN: String = "",
    val originalRRN: String = "",
    val originalAcquiringInstCode: String = "",
    var reversalReasonCode: MessageReasonCode = MessageReasonCode.CustomerCancellation,
    var originalForwardingInstCode: String? = null
)

fun TransactionResponse.toOriginalDataElements() = OriginalDataElements(
    originalTransactionType = transactionType,
    originalAmount = amount,
    originalAuthorizationCode = authCode,
    originalTransmissionTime = transmissionDateTime,
    originalSTAN = STAN,
    originalRRN = RRN,
    originalAcquiringInstCode = acquiringInstCode,
    originalForwardingInstCode = originalForwardingInstCode
)

enum class MessageReasonCode(val code: String) {

    CustomerCancellation("4000"), UnSpecified("4001"), CompletedPartially("4004"), Timeout("4021");
}

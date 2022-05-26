package com.danbamitale.epmslib.entities

import android.os.Parcelable
import com.danbamitale.epmslib.utils.IsoAccountType
import com.danbamitale.epmslib.utils.MessageReasonCode
import com.isw.iswclient.request.IswParameters
import com.isw.iswclient.request.ProcessingProperties
import kotlinx.android.parcel.Parcelize

/**
 * Payment request data.
 * otherAmount property is required for PURCHASE WITH CASHBACK transactions
 * For SALES COMPLETION, REFUND and REVERSAL transactions, originalDataElements property is required
 *
 * @property transactionType
 * @property amount
 * @property otherAmount
 * @property accountType
 *  @property additionalTransParams optional
 * @property echoData optional
 * @property originalDataElements  conditional
 */
@Parcelize
data class TransactionRequestData(
    val transactionType: TransactionType,
    var amount: Long,
    val otherAmount: Long = 0,
    val accountType: IsoAccountType = IsoAccountType.DEFAULT_UNSPECIFIED,
    var RRN: String? = null,
    var STAN: String? = null,
    val additionalTransParams: AdditionalTransParams? = null,
    val echoData: String? = null,
    val originalDataElements: OriginalDataElements? = null
) : Parcelable {
    var iswParameters: IswParameters? = null
    var processingProperties: ProcessingProperties? = null

    @JvmOverloads
    constructor(
        transactionType: TransactionType,
        amount: Long = 0,
        otherAmount: Long = 0,
        accountType: IsoAccountType = IsoAccountType.DEFAULT_UNSPECIFIED,
        RRN: String? = null,
        STAN: String? = null,
    ) : this(transactionType, amount, otherAmount, accountType, RRN, STAN, null, null, null)

    constructor(
        transactionType: TransactionType,
        amount: Long,
        otherAmount: Long = 0,
        accountType: IsoAccountType = IsoAccountType.DEFAULT_UNSPECIFIED,
        originalDataElements: OriginalDataElements? = null
    ) : this(
        transactionType,
        amount,
        otherAmount,
        accountType,
        null,
        null,
        null,
        null,
        originalDataElements
    )
}

@Parcelize
data class AdditionalTransParams(
    val transmissionDateF7: String,
    val stanF11: String,
    val localTimeF12: String,
    val localDateF13: String,
    val posConditionCodeF25: String,
    val pinCaptureModeF26: String,
    val amountTransactionFeeF28: String,
    val rrnF37: String,
    val posDataCodeF123: String
) : Parcelable

/**
 *
 *
 * @property originalTransactionType
 * @property originalAmount
 * @property originalAuthorizationCode
 * @property originalTransmissionTime
 * @property originalSTAN
 * @property originalRRN
 * @property originalAcquiringInstCode
 * @property originalForwardingInstCode
 */
@Parcelize
data class OriginalDataElements(
    val originalTransactionType: TransactionType,
    val originalAmount: Long = 0L,
    val originalAuthorizationCode: String? = null,
    val originalTransmissionTime: String = "",
    val originalSTAN: String = "",
    val originalRRN: String = "",
    val originalAcquiringInstCode: String = "",
    var reversalReasonCode: MessageReasonCode = MessageReasonCode.CustomerCancellation,
    var originalForwardingInstCode: String? = null,
    var originalInterSwitchThreshold: Long = 0L
) : Parcelable

fun TransactionResponse.toOriginalDataElements() = OriginalDataElements(
    originalTransactionType = transactionType,
    originalAmount = amount,
    originalAuthorizationCode = authCode,
    originalTransmissionTime = transmissionDateTime,
    originalSTAN = STAN,
    originalRRN = RRN,
    originalAcquiringInstCode = acquiringInstCode,
    originalForwardingInstCode = originalForwardingInstCode,
    originalInterSwitchThreshold = interSwitchThreshold
)

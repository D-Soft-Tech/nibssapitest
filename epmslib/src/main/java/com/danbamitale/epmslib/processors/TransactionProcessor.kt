package com.danbamitale.epmslib.processors

import android.content.Context
import com.danbamitale.epmslib.comms.SocketRequest
import com.danbamitale.epmslib.entities.* // ktlint-disable no-wildcard-imports
import com.danbamitale.epmslib.extensions.generateHash256Value
import com.danbamitale.epmslib.extensions.getTransactionType
import com.danbamitale.epmslib.extensions.padLeft
import com.danbamitale.epmslib.extensions.toTransactionResponse
import com.danbamitale.epmslib.utils.* // ktlint-disable no-wildcard-imports
import com.solab.iso8583.IsoMessage
import com.solab.iso8583.IsoType
import com.solab.iso8583.IsoValue
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

/**
 *
 * @property hostConfig
 */
class TransactionProcessor(private val hostConfig: HostConfig) {

    // Constants from Nibss specification
    private val posConditionCode = "00"
    private val pinCaptureMode = "12"
    private val amountTransactionFee = "D00000000"
    private val posDataCode =
        "510101511344101" // NIBSS
//     "510111511344101" //MasterCard

    private val requestHandler = SocketRequest(hostConfig.connectionData)

    private lateinit var requestIsoMessage: IsoMessage

    private var transactionTimeInMillis = 0L

    private fun setBaseFields(
        requestData: TransactionRequestData,
        cardData: CardData,
        configData: ConfigData
    ) =
        IsoMessage().apply {
            val addTransParams = requestData.additionalTransParams
            val timeMgr = IsoTimeManager()
            transactionTimeInMillis = System.currentTimeMillis()

            val transmissionDateAndTime = timeMgr.longDate
            val sequenceNumber = timeMgr.time
            val timeLocalTransaction = timeMgr.time
            val dateLocalTransaction = timeMgr.shortDate
            val RRN = requestData.RRN ?: timeMgr.fullDate.substring(2, 14)

            val processingCode =
                "${requestData.transactionType.code}${requestData.accountType.code}${IsoAccountType.DEFAULT_UNSPECIFIED.code}"

            type = requestData.transactionType.MTI
            setField(2, IsoValue(IsoType.LLVAR, cardData.pan))
            setField(3, IsoValue(IsoType.ALPHA, processingCode, 6))
            setField(
                4,
                IsoValue<String>(
                    IsoType.ALPHA,
                    (requestData.amount + requestData.otherAmount).toString().padLeft(12, '0'),
                    12
                )
            )
            setField(
                7,
                IsoValue(
                    IsoType.ALPHA,
                    addTransParams?.transmissionDateF7 ?: transmissionDateAndTime,
                    10
                )
            )
            setField(
                11,
                IsoValue<String>(IsoType.NUMERIC, addTransParams?.stanF11 ?: sequenceNumber, 6)
            )
            setField(
                12,
                IsoValue(IsoType.ALPHA, addTransParams?.localTimeF12 ?: timeLocalTransaction, 6)
            )
            setField(
                13,
                IsoValue(IsoType.ALPHA, addTransParams?.localDateF13 ?: dateLocalTransaction, 4)
            )
            setField(14, IsoValue(IsoType.ALPHA, cardData.expiryDate, 4))
            setField(18, IsoValue(IsoType.ALPHA, configData.merchantCategoryCode, 4))
            setField(22, IsoValue(IsoType.ALPHA, cardData.posEntryMode.padLeft(3, '0'), 3))

            if (cardData.panSequenceNumber.length == 3) {
                setField(23, IsoValue(IsoType.ALPHA, cardData.panSequenceNumber, 3))
            }

            setField(
                25,
                IsoValue(IsoType.ALPHA, addTransParams?.posConditionCodeF25 ?: posConditionCode, 2)
            )
            setField(
                26,
                IsoValue(IsoType.ALPHA, addTransParams?.pinCaptureModeF26 ?: pinCaptureMode, 2)
            )
            setField(
                28,
                IsoValue(
                    IsoType.ALPHA,
                    addTransParams?.amountTransactionFeeF28 ?: amountTransactionFee,
                    9
                )
            )
            setField(32, IsoValue(IsoType.LLVAR, cardData.acquiringInstitutionIdCode))
            setField(35, IsoValue(IsoType.LLVAR, cardData.track2Data))
            setField(37, IsoValue(IsoType.ALPHA, addTransParams?.rrnF37 ?: RRN, 12))
            setField(40, IsoValue(IsoType.ALPHA, cardData.serviceCode, 3))
            setField(41, IsoValue(IsoType.ALPHA, hostConfig.terminalId, 8))
            setField(42, IsoValue(IsoType.ALPHA, configData.cardAcceptorIdCode, 15))
            setField(43, IsoValue(IsoType.ALPHA, configData.merchantNameLocation, 40))
            setField(49, IsoValue(IsoType.ALPHA, configData.currencyCode, 3))

            cardData.pinBlock?.let { setField(52, IsoValue(IsoType.ALPHA, it.toUpperCase(), 16)) }

            if (cardData.nibssIccSubset.isNotBlank()) {
                setField(55, IsoValue(IsoType.LLLVAR, cardData.nibssIccSubset))
            }

            requestData.echoData?.let {
                setField(
                    Constants.TRANSPORT_ECHO_DATA_59,
                    IsoValue(IsoType.LLLVAR, it)
                )
            }

            setField(123, IsoValue(IsoType.LLLVAR, addTransParams?.posDataCodeF123 ?: posDataCode))
            setField(128, IsoValue(IsoType.ALPHA, "", 64))
        }

    private fun setOriginalTransactionData(
        isoMessage: IsoMessage,
        requestData: TransactionRequestData
    ) {
        requestData.originalDataElements?.let {
            val originalDataElements = getOriginalDataElementField90(
                it.originalTransactionType.MTI.toString(16),
                it.originalAcquiringInstCode,
                it.originalForwardingInstCode,
                it.originalSTAN,
                it.originalTransmissionTime
            )

            val replacementAmounts =
                getReplacementAmountField95(it.originalAmount, requestData.amount)

            isoMessage.setField(90, IsoValue(IsoType.ALPHA, originalDataElements, 42))
            isoMessage.setField(95, IsoValue(IsoType.ALPHA, replacementAmounts, 42))
        }
    }

    private fun getOriginalDataElementField90(
        originalMTI: String,
        acquiringInstCode: String,
        forwardingInstCode: String?,
        originalSTAN: String,
        originalTransmissionDateTime: String
    ): String {
        val acquiringInstitutionCode = acquiringInstCode.padLeft(11, '0')
        val originalForwardingInstitution =
            forwardingInstCode?.padLeft(11, '0') ?: "0".padLeft(11, '0')

        return originalMTI.padLeft(
            4,
            '0'
        ) + originalSTAN + originalTransmissionDateTime + acquiringInstitutionCode + originalForwardingInstitution
    }

    private fun getReplacementAmountField95(originalAmount: Long, newAmount: Long): String {
        val replacementAmount = originalAmount - newAmount

        return String.format("%012d%012dD00000000D00000000", replacementAmount, replacementAmount)
    }

    /**
     * Send a payment request to processor
     *
     * @param context
     * @param requestData
     * @param cardData
     */
    fun processTransaction(
        context: Context,
        requestData: TransactionRequestData,
        cardData: CardData
    ): Single<TransactionResponse> = Single.fromCallable {
        requestIsoMessage = setBaseFields(requestData, cardData, hostConfig.configData)
        when (requestData.transactionType) {
            TransactionType.PURCHASE_WITH_CASH_BACK -> {
                val additionalAmounts = String.format(
                    "%s05%sD%012d",
                    requestData.accountType.code,
                    hostConfig.configData.currencyCode,
                    requestData.otherAmount
                )
                requestIsoMessage.setField(
                    Constants.ADDITIONAL_AMOUNTS_54,
                    IsoValue(IsoType.LLLVAR, additionalAmounts)
                )
            }

            TransactionType.REVERSAL -> {
                requestIsoMessage.setField(
                    Constants.SYSTEMS_TRACE_AUDIT_NUMBER_11,
                    IsoValue<String>(
                        IsoType.ALPHA,
                        requestData.originalDataElements!!.originalSTAN,
                        6
                    )
                )

                requestIsoMessage.setField(
                    Constants.TIME_LOCAL_TRANSACTION_12,
                    IsoValue<String>(
                        IsoType.ALPHA,
                        requestData.originalDataElements.originalTransmissionTime.substring(4),
                        6
                    )
                )

                requestIsoMessage.setField(
                    Constants.DATE_LOCAL_TRANSACTION_13,
                    IsoValue<String>(
                        IsoType.ALPHA,
                        requestData.originalDataElements.originalTransmissionTime.substring(0, 4),
                        4
                    )
                )

                requestData.originalDataElements.originalRRN.let {
                    requestIsoMessage.setField(37, IsoValue(IsoType.ALPHA, it, 12))
                }

                requestData.originalDataElements.originalAuthorizationCode?.let {
                    requestIsoMessage.setField(
                        Constants.AUTHORIZATION_CODE_38,
                        IsoValue<String>(IsoType.ALPHA, it, 6)
                    )
                }

                requestIsoMessage.removeFields(Constants.INTEGRATED_CIRCUIT_CARD_SYSTEM_RELATED_DATA_55)
                requestIsoMessage.setField(
                    Constants.MESSAGE_REASON_CODE_56,
                    IsoValue<String>(
                        IsoType.LLLVAR,
                        requestData.originalDataElements.reversalReasonCode.code
                    )
                )

                setOriginalTransactionData(requestIsoMessage, requestData)
            }

            TransactionType.REFUND -> {
                requestData.originalDataElements?.originalAuthorizationCode?.let {
                    requestIsoMessage.setField(
                        Constants.AUTHORIZATION_CODE_38,
                        IsoValue<String>(IsoType.ALPHA, it, 6)
                    )
                }
                setOriginalTransactionData(requestIsoMessage, requestData)
            }

            TransactionType.PRE_AUTHORIZATION_COMPLETION -> {
                requestData.originalDataElements?.originalAuthorizationCode?.let {
                    requestIsoMessage.setField(
                        Constants.AUTHORIZATION_CODE_38,
                        IsoValue<String>(IsoType.ALPHA, it, 6)
                    )
                }

                requestData.originalDataElements?.let {
                    val originalDataElements = getOriginalDataElementField90(
                        it.originalTransactionType.MTI.toString(16),
                        it.originalAcquiringInstCode,
                        it.originalForwardingInstCode,
                        it.originalSTAN,
                        it.originalTransmissionTime
                    )

                    requestIsoMessage.setField(
                        90,
                        IsoValue(IsoType.ALPHA, originalDataElements, 42)
                    )

                    val replacementAmounts = String.format(
                        "%012d%012dD00000000D00000000",
                        requestData.amount,
                        it.originalAmount
                    )
                    requestIsoMessage.setField(95, IsoValue(IsoType.ALPHA, replacementAmounts, 42))
                }
            }
            else -> {}
        }
        IsoAdapter.logIsoMessage(requestIsoMessage)

        var messageString = String(requestIsoMessage.writeData()).trim { it <= ' ' }
        val hash = messageString.generateHash256Value(hostConfig.keyHolder.clearSessionKey)
        messageString += hash.toUpperCase()
        println("Request: $messageString")

        val isoMsgByteArray =
            IsoAdapter.prepareByteStream(messageString.toByteArray(charset("UTF-8")))

        val transactionResponse: TransactionResponse = try {
            val response = requestHandler.send(context, isoMsgByteArray)
            println("Response: $response")
            IsoAdapter.processISOBitStreamWithJ8583(context, response)
                .toTransactionResponse()
        } catch (e: Exception) {
            e.printStackTrace()
//            rollback(context).blockingGet()
            requestIsoMessage.toTransactionResponse().apply {
                errorMessage = e.localizedMessage
                responseCode = "A3"
            }
        }

        transactionResponse.apply {
            otherAmount = requestData.otherAmount
            amount -= otherAmount
            transactionTimeInMillis = this@TransactionProcessor.transactionTimeInMillis
        }
    }.subscribeOn(Schedulers.computation())

    /**
     * Roll back a transaction. If initialIsoMessage is specified, that rollback request
     * is built using that transaction, else the last sent transaction is rolled-back.
     *
     * @param context
     * @param initialIsoMessage Optional
     * @param sessionKey Optional
     */
    fun rollback(
        context: Context,
        reversalReasonCode: MessageReasonCode = MessageReasonCode.Timeout,
        initialIsoMessage: IsoMessage = requestIsoMessage,
        sessionKey: String = hostConfig.keyHolder.clearSessionKey
    ): Single<TransactionResponse> = Single.fromCallable {
        val timeMgr = IsoTimeManager()

        val originalTranType = initialIsoMessage.getTransactionType()

        val originalMTI = initialIsoMessage.type.toString(16)
        val originalSTAN =
            initialIsoMessage.getField<String>(Constants.SYSTEMS_TRACE_AUDIT_NUMBER_11).value
        val originalTransmissionDateTime =
            initialIsoMessage.getField<String>(Constants.TRANSMISSION_DATE_TIME_7).value
        val acquiringInstCode =
            initialIsoMessage.getField<String>(Constants.ACQUIRING_INSTITUTION_ID_CODE_32).value
        val forwardingInstCode: String? =
            if (initialIsoMessage.hasField(Constants.FORWARDING_INSTITUTION_IDENTIFICATION_33)) {
                initialIsoMessage.getField<String>(Constants.FORWARDING_INSTITUTION_IDENTIFICATION_33).value
            } else {
                null
            }

        val originalDataElements = getOriginalDataElementField90(
            originalMTI,
            acquiringInstCode,
            "",
            originalSTAN,
            originalTransmissionDateTime
        )

        val processingCode =
            "${TransactionType.REVERSAL.code}${IsoAccountType.DEFAULT_UNSPECIFIED.code}${IsoAccountType.DEFAULT_UNSPECIFIED.code}"

        initialIsoMessage.removeFields(Constants.ADDITIONAL_AMOUNTS_54)
        initialIsoMessage.removeFields(Constants.INTEGRATED_CIRCUIT_CARD_SYSTEM_RELATED_DATA_55)

        initialIsoMessage.type = Constants.MTI.REVERSAL_ADVICE_MTI.toInt(16)
        initialIsoMessage.setField(3, IsoValue(IsoType.ALPHA, processingCode, 6))
        initialIsoMessage.setField(7, IsoValue(IsoType.ALPHA, timeMgr.longDate, 10))
        initialIsoMessage.setField(
            Constants.MESSAGE_REASON_CODE_56,
            IsoValue(IsoType.LLLVAR, reversalReasonCode.code)
        )
        initialIsoMessage.setField(90, IsoValue(IsoType.ALPHA, originalDataElements, 42))
        initialIsoMessage.setField(
            95,
            IsoValue(IsoType.ALPHA, "000000000000000000000000D00000000D00000000", 42)
        )

        IsoAdapter.logIsoMessage(initialIsoMessage)

        var messageString = String(initialIsoMessage.writeData()).trim { it <= ' ' }
        val hash = messageString.generateHash256Value(sessionKey)
        messageString += hash.toUpperCase()

//        println(messageString)

        val isoMsgByteArray =
            IsoAdapter.prepareByteStream(messageString.toByteArray(charset("UTF-8")))
        try {
            val response = requestHandler.send(context, isoMsgByteArray)
            val parsedResponse = IsoAdapter.processISOBitStreamWithJ8583(context, response)

            val responseCode = parsedResponse.getField<String>(Constants.RESPONSE_CODE_39).value
            initialIsoMessage.setField(
                Constants.RESPONSE_CODE_39,
                IsoValue(IsoType.NUMERIC, if (responseCode == "00") "06" else responseCode, 2)
            )
        } catch (e: Exception) {
            initialIsoMessage.setField(
                Constants.RESPONSE_CODE_39,
                IsoValue(IsoType.NUMERIC, "20", 2)
            )
        }

        initialIsoMessage.toTransactionResponse().apply {
            transactionType = originalTranType
        }
    }.subscribeOn(Schedulers.io())
}

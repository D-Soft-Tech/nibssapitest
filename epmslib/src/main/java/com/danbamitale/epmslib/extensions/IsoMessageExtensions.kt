package com.danbamitale.epmslib.extensions

import com.danbamitale.epmslib.entities.TransactionResponse
import com.danbamitale.epmslib.entities.TransactionType
import com.danbamitale.epmslib.utils.Constants
import com.danbamitale.epmslib.utils.IsoAccountType
import com.solab.iso8583.IsoMessage

fun <T> IsoMessage.getValue(index: Int): T = this.getField<T>(index).value

fun IsoMessage.toTransactionResponse() = TransactionResponse(
    transactionType = getTransactionType(),
    accountType = getAccountType(),
    maskedPan = getValue<String>(Constants.PRIMARY_ACCOUNT_NUMBER_2).maskPan(),
    amount = getValue<String>(Constants.AMOUNT_TRANSACTION_4).toLong(),
    otherAmount = 0,
    transmissionDateTime = getValue(Constants.TRANSMISSION_DATE_TIME_7),
    STAN = getValue(Constants.SYSTEMS_TRACE_AUDIT_NUMBER_11),
    cardExpiry = if (hasField(Constants.DATE_EXPIRATION_14)) getValue(Constants.DATE_EXPIRATION_14) else "",
    RRN = getValue(Constants.RETRIEVAL_REFERENCE_NUMBER_37),
    localTime_12 = getValue(Constants.TIME_LOCAL_TRANSACTION_12),
    localDate_13 = getValue(Constants.DATE_LOCAL_TRANSACTION_13),
    acquiringInstCode = getValue(Constants.ACQUIRING_INSTITUTION_ID_CODE_32),
    terminalId = getValue(Constants.CARD_ACCEPTOR_TERMINAL_ID_41),
    merchantId = getValue(Constants.CARD_ACCEPTOR_ID_CODE_42),
    originalForwardingInstCode = if (hasField(Constants.FORWARDING_INSTITUTION_IDENTIFICATION_33)) getValue(Constants.FORWARDING_INSTITUTION_IDENTIFICATION_33) else "",
    authCode = if (hasField(Constants.AUTHORIZATION_CODE_38)) getValue(Constants.AUTHORIZATION_CODE_38) else "",
    responseCode = if (hasField(Constants.RESPONSE_CODE_39)) getValue(Constants.RESPONSE_CODE_39) else "20",
    additionalAmount_54 = if (hasField(Constants.ADDITIONAL_AMOUNTS_54)) getValue(Constants.ADDITIONAL_AMOUNTS_54) else "",
    responseDE55 = if (hasField(Constants.INTEGRATED_CIRCUIT_CARD_SYSTEM_RELATED_DATA_55)) getValue<String>(Constants.INTEGRATED_CIRCUIT_CARD_SYSTEM_RELATED_DATA_55) else null,
    echoData = if (hasField(Constants.TRANSPORT_ECHO_DATA_59)) getValue(Constants.TRANSPORT_ECHO_DATA_59) else null
)

fun IsoMessage.getTransactionType(): TransactionType {
    println("MTI: ${type.toString(16)}")
    if (type.toString(16).startsWith("4")) {
        return TransactionType.REVERSAL
    }
    return when (this.getValue<String>(Constants.PROCESSING_CODE_3).substring(0, 2)) {
        Constants.IsoTransactionTypeCode.PURCHASE -> TransactionType.PURCHASE
        Constants.IsoTransactionTypeCode.PURCHASE_WITH_CASH_BACK -> TransactionType.PURCHASE_WITH_CASH_BACK
        Constants.IsoTransactionTypeCode.PURCHASE_WITH_ADDITIONAL_DATA -> TransactionType.PURCHASE_WITH_ADDITIONAL_DATA
        Constants.IsoTransactionTypeCode.CASH_ADVANCE -> TransactionType.CASH_ADVANCE
        Constants.IsoTransactionTypeCode.DEPOSIT -> TransactionType.DEPOSIT
        Constants.IsoTransactionTypeCode.FUND_TRANSFER -> TransactionType.TRANSFER
        Constants.IsoTransactionTypeCode.BILL_PAYMENTS -> TransactionType.BILL_PAYMENT
        Constants.IsoTransactionTypeCode.PREPAID -> TransactionType.PREPAID
        Constants.IsoTransactionTypeCode.REFUND -> TransactionType.REFUND
        Constants.IsoTransactionTypeCode.PRE_AUTHORIZATION -> TransactionType.PRE_AUTHORIZATION
        Constants.IsoTransactionTypeCode.PRE_AUTHORIZATION_COMPLETION -> TransactionType.PRE_AUTHORIZATION_COMPLETION
        Constants.IsoTransactionTypeCode.BALANCE_INQUIRY -> TransactionType.BALANCE
        Constants.IsoTransactionTypeCode.PIN_CHANGE -> TransactionType.PIN_CHANGE
        Constants.IsoTransactionTypeCode.MINI_STATEMENT -> TransactionType.MINI_STATEMENT
        Constants.IsoTransactionTypeCode.LINK_ACCOUNT_INQUIRY -> TransactionType.LINK_ACCOUNT_INQUIRY
        Constants.IsoTransactionTypeCode.BILLER_LIST_DOWNLOAD -> TransactionType.BILLER_LIST_DOWNLOAD
        Constants.IsoTransactionTypeCode.PRODUCT_LIST_DOWNLOAD -> TransactionType.PRODUCT_LIST_DOWNLOAD
        Constants.IsoTransactionTypeCode.BILLER_SUBSCRIPTION_INFORMATION_DOWNLOAD -> TransactionType.BILLER_SUBSCRIPTION_INFO_DOWNLOAD
        Constants.IsoTransactionTypeCode.PAYMENT_VALIDATION -> TransactionType.PAYMENT_VALIDATION
        Constants.IsoTransactionTypeCode.TERMINAL_MASTER_KEY -> TransactionType.TERMINAL_MASTER_KEY
        Constants.IsoTransactionTypeCode.TERMINAL_SESSION_KEY -> TransactionType.TERMINAL_SESSION_KEY
        Constants.IsoTransactionTypeCode.TERMINAL_PIN_KEY -> TransactionType.TERMINAL_PIN_KEY
        Constants.IsoTransactionTypeCode.TERMINAL_PARAMETER_DOWNLOAD -> TransactionType.TERMINAL_PARAMETER_DOWNLOAD
        Constants.IsoTransactionTypeCode.CALL_HOME -> TransactionType.CALL_HOME
        Constants.IsoTransactionTypeCode.DAILY_TRANSACTION_REPORT_DOWNLOAD -> TransactionType.DAILY_TRANSACTION_REPORT_DOWNLOAD
        Constants.IsoTransactionTypeCode.DYNAMIC_CURRENCY_CONVERSION -> TransactionType.DYNAMIC_CURRENCY_CONVERSION
        Constants.IsoTransactionTypeCode.INITIAL_PIN_ENCRYPTION_KEY_DOWNLOAD_EMV -> TransactionType.INITIAL_PIN_ENCRYPTION_KEY_DOWNLOAD_EMV
        Constants.IsoTransactionTypeCode.INITIAL_PIN_ENCRYPTION_KEY_DOWNLOAD_TRACK2_DATA -> TransactionType.INITIAL_PIN_ENCRYPTION_KEY_DOWNLOAD_TRACK2_DATA
        Constants.IsoTransactionTypeCode.CA_PUBLIC_KEY_DOWNLOAD -> TransactionType.CA_PUBLIC_KEY_DOWNLOAD
        Constants.IsoTransactionTypeCode.EMV_APPLICATION_AID_DOWNLOAD -> TransactionType.EMV_APPLICATION_AID_DOWNLOAD
        Constants.IsoTransactionTypeCode.NEW_WORKING_KEY_INQUIRY_FROM_HOST -> TransactionType.TRANZAXIS_WORKING_KEY_INQUIRY
        Constants.IsoTransactionTypeCode.NEW_WORKING_KEY_FOR_TRAFFIC_ENCRYPTION_INQUIRY -> TransactionType.TRANZAXIS_TRAFFIC_ENCRYPTION_WORKING_KEY
        else -> TransactionType.TRANZAXIS_ECHO_TEST
    }
}

fun IsoMessage.getAccountType(): IsoAccountType =
    IsoAccountType.parseIntAccountType(
        this.getValue<String>(Constants.PROCESSING_CODE_3).substring(
            2,
            4
        ).toInt()
    )

// fun IsoMessage.getOtherAmount(): Long

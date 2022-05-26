package com.netpluspay.nibssclient.models

import com.netpluspay.nibssclient.util.Constants
import com.netpluspay.nibssclient.util.toInt16

enum class TransactionType {
    // EFT TRAN TYPES
    PURCHASE,
    PURCHASE_WITH_CASH_BACK, PURCHASE_WITH_ADDITIONAL_DATA, CASH_ADVANCE,
    REVERSAL, REFUND, PRE_AUTHORIZATION, PRE_AUTHORIZATION_COMPLETION, BALANCE, MINI_STATEMENT, LINK_ACCOUNT_INQUIRY,
    PIN_CHANGE, DEPOSIT, TRANSFER, BILL_PAYMENT, PREPAID, VOID, BILLER_LIST_DOWNLOAD, PRODUCT_LIST_DOWNLOAD,
    BILLER_SUBSCRIPTION_INFO_DOWNLOAD, PAYMENT_VALIDATION, CASH,

    PAYXPRESS,

    // NETWORK MGT TYPE
    TERMINAL_MASTER_KEY,
    TERMINAL_SESSION_KEY, TERMINAL_PIN_KEY,
    TERMINAL_PARAMETER_DOWNLOAD, CALL_HOME, DAILY_TRANSACTION_REPORT_DOWNLOAD, CA_PUBLIC_KEY_DOWNLOAD, EMV_APPLICATION_AID_DOWNLOAD,
    DYNAMIC_CURRENCY_CONVERSION, INITIAL_PIN_ENCRYPTION_KEY_DOWNLOAD_EMV, INITIAL_PIN_ENCRYPTION_KEY_DOWNLOAD_TRACK2_DATA,
    TRANZAXIS_WORKING_KEY_INQUIRY, TRANZAXIS_TRAFFIC_ENCRYPTION_WORKING_KEY, TRANZAXIS_ECHO_TEST;

    val code
        get() = when (this) {
            PURCHASE -> Constants.IsoTransactionTypeCode.PURCHASE
            PURCHASE_WITH_CASH_BACK -> Constants.IsoTransactionTypeCode.PURCHASE_WITH_CASH_BACK
            PURCHASE_WITH_ADDITIONAL_DATA -> Constants.IsoTransactionTypeCode.PURCHASE_WITH_ADDITIONAL_DATA
            CASH_ADVANCE -> Constants.IsoTransactionTypeCode.CASH_ADVANCE
            REVERSAL -> Constants.IsoTransactionTypeCode.REVERSAL
            REFUND -> Constants.IsoTransactionTypeCode.REFUND
            PRE_AUTHORIZATION -> Constants.IsoTransactionTypeCode.PRE_AUTHORIZATION
            PRE_AUTHORIZATION_COMPLETION -> Constants.IsoTransactionTypeCode.PRE_AUTHORIZATION_COMPLETION
            BALANCE -> Constants.IsoTransactionTypeCode.BALANCE_INQUIRY
            MINI_STATEMENT -> Constants.IsoTransactionTypeCode.MINI_STATEMENT
            LINK_ACCOUNT_INQUIRY -> Constants.IsoTransactionTypeCode.LINK_ACCOUNT_INQUIRY
            PIN_CHANGE -> Constants.IsoTransactionTypeCode.PIN_CHANGE
            DEPOSIT -> Constants.IsoTransactionTypeCode.DEPOSIT
            TRANSFER -> Constants.IsoTransactionTypeCode.FUND_TRANSFER
            BILL_PAYMENT -> Constants.IsoTransactionTypeCode.BILL_PAYMENTS
            PREPAID -> Constants.IsoTransactionTypeCode.PREPAID
            BILLER_LIST_DOWNLOAD -> Constants.IsoTransactionTypeCode.BILLER_LIST_DOWNLOAD
            PRODUCT_LIST_DOWNLOAD -> Constants.IsoTransactionTypeCode.PRODUCT_LIST_DOWNLOAD
            BILLER_SUBSCRIPTION_INFO_DOWNLOAD -> Constants.IsoTransactionTypeCode.BILLER_SUBSCRIPTION_INFORMATION_DOWNLOAD
            PAYMENT_VALIDATION -> Constants.IsoTransactionTypeCode.PAYMENT_VALIDATION
            TERMINAL_MASTER_KEY -> Constants.IsoTransactionTypeCode.TERMINAL_MASTER_KEY
            TERMINAL_SESSION_KEY -> Constants.IsoTransactionTypeCode.TERMINAL_SESSION_KEY
            TERMINAL_PIN_KEY -> Constants.IsoTransactionTypeCode.TERMINAL_PIN_KEY
            TERMINAL_PARAMETER_DOWNLOAD -> Constants.IsoTransactionTypeCode.TERMINAL_PARAMETER_DOWNLOAD
            CALL_HOME -> Constants.IsoTransactionTypeCode.CALL_HOME
            DAILY_TRANSACTION_REPORT_DOWNLOAD -> Constants.IsoTransactionTypeCode.DAILY_TRANSACTION_REPORT_DOWNLOAD
            DYNAMIC_CURRENCY_CONVERSION -> Constants.IsoTransactionTypeCode.DYNAMIC_CURRENCY_CONVERSION
            CA_PUBLIC_KEY_DOWNLOAD -> Constants.IsoTransactionTypeCode.CA_PUBLIC_KEY_DOWNLOAD
            EMV_APPLICATION_AID_DOWNLOAD -> Constants.IsoTransactionTypeCode.EMV_APPLICATION_AID_DOWNLOAD
            INITIAL_PIN_ENCRYPTION_KEY_DOWNLOAD_EMV -> Constants.IsoTransactionTypeCode.INITIAL_PIN_ENCRYPTION_KEY_DOWNLOAD_EMV
            INITIAL_PIN_ENCRYPTION_KEY_DOWNLOAD_TRACK2_DATA -> Constants.IsoTransactionTypeCode.INITIAL_PIN_ENCRYPTION_KEY_DOWNLOAD_TRACK2_DATA
            TRANZAXIS_WORKING_KEY_INQUIRY -> Constants.IsoTransactionTypeCode.NEW_WORKING_KEY_INQUIRY_FROM_HOST
            TRANZAXIS_TRAFFIC_ENCRYPTION_WORKING_KEY -> Constants.IsoTransactionTypeCode.NEW_WORKING_KEY_FOR_TRAFFIC_ENCRYPTION_INQUIRY
            TRANZAXIS_ECHO_TEST -> Constants.IsoTransactionTypeCode.ECHO_TEST
            else -> ""
        }

    val MTI
        get() = when (this) {
            PURCHASE,
            PURCHASE_WITH_CASH_BACK,
            PURCHASE_WITH_ADDITIONAL_DATA,
            CASH_ADVANCE,
            DEPOSIT,
            TRANSFER,
            BILL_PAYMENT,
            PREPAID,
            REFUND -> Constants.MTI.TRANSACTION_REQUEST_MTI
            PRE_AUTHORIZATION_COMPLETION -> Constants.MTI.TRANSACTION_ADVICE_MTI
            PRE_AUTHORIZATION,
            BALANCE,
            PIN_CHANGE,
            MINI_STATEMENT,
            LINK_ACCOUNT_INQUIRY -> Constants.MTI.AUTHORIZATION_REQUEST_MTI
            REVERSAL -> Constants.MTI.REVERSAL_ADVICE_MTI
            else -> Constants.MTI.NETWORK_MGT_REQUEST_MTI
        }.toInt16()

    override fun toString() = this.name.replace('_', ' ').toLowerCase().capitalize()

    val hasOriginalAmount: Boolean
        get() = when (this) {
            REFUND, REVERSAL, PRE_AUTHORIZATION_COMPLETION -> true
            else -> false
        }

    val hasAdditionalAmount: Boolean
        get() = when (this) {
            PURCHASE_WITH_CASH_BACK -> true
            else -> false
        }

    val isReversible: Boolean
        get() = when (this) {
            PURCHASE,
            PURCHASE_WITH_ADDITIONAL_DATA,
            PURCHASE_WITH_CASH_BACK,
            CASH_ADVANCE,
            PRE_AUTHORIZATION_COMPLETION,
            DEPOSIT,
            TRANSFER,
            PREPAID,
            BILL_PAYMENT,
            PRE_AUTHORIZATION -> true
            else -> false
        }
}

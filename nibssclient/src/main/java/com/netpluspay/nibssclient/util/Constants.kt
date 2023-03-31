package com.netpluspay.nibssclient.util

object Constants {
    val DEFAULT_TERMINAL_ID = UtilityParams.nibssDefaultTid
    const val ISW_TOKEN = "TokenForIsw"
    const val MARKETPLACE_STORE = "marketplace_store"
    const val STORE_CREATED = "store_created"
    const val MTI_INDEX = 0
    const val BITMAP = 1
    var USER_TYPE_NONE = -1
    const val PRIMARY_ACCOUNT_NUMBER_2 = 2
    const val PROCESSING_CODE_3 = 3
    const val AMOUNT_TRANSACTION_4 = 4
    const val AMOUNT_SETTLEMENT_5 = 5
    const val TRANSMISSION_DATE_TIME_7 = 7
    const val SYSTEMS_TRACE_AUDIT_NUMBER_11 = 11
    const val TIME_LOCAL_TRANSACTION_12 = 12
    const val DATE_LOCAL_TRANSACTION_13 = 13
    const val DATE_EXPIRATION_14 = 14
    const val DATE_SETTLEMENT_15 = 15
    const val MERCHANT_TYPE_18 = 18
    const val POS_ENTRY_MODE_MANDATORY_22 = 22
    const val CARD_SEQUENCE_NUMBER_23 = 23
    const val POS_CONDITION_CODE_25 = 25
    const val POS_PIN_CAPTURE_CODE_26 = 26
    const val AMOUNT_TRANSACTION_FEE_28 = 28
    const val AMOUNT_TRANSACTION_PROCESSING_FEE_30 = 30
    const val ACQUIRING_INSTITUTION_ID_CODE_32 = 32
    const val FORWARDING_INSTITUTION_IDENTIFICATION_33 = 33
    const val TRACK_2_DATA_35 = 35
    const val RETRIEVAL_REFERENCE_NUMBER_37 = 37
    const val AUTHORIZATION_CODE_38 = 38
    const val RESPONSE_CODE_39 = 39
    const val SERVICE_RESTRICTION_CODE_40 = 40
    const val CARD_ACCEPTOR_TERMINAL_ID_41 = 41
    const val CARD_ACCEPTOR_ID_CODE_42 = 42
    const val CARD_ACCEPTOR_NAME_LOCATION_43 = 43
    const val ADDITIONAL_DATA_48 = 48
    const val CURRENCY_CODE_49 = 49
    const val PIN_DATA_52 = 52
    const val SECURITY_RELATED_CONTROL_INFO_53 = 53
    const val ADDITIONAL_AMOUNTS_54 = 54
    const val INTEGRATED_CIRCUIT_CARD_SYSTEM_RELATED_DATA_55 = 55
    const val MESSAGE_REASON_CODE_56 = 56
    const val TRANSPORT_ECHO_DATA_59 = 59
    const val PAYMENT_INFORMATION_60 = 60
    const val PRIVATE_FIELD_MGT_DATA1_62 = 62
    const val PRIVATE_FIELD_MGT_DATA2_63 = 63
    const val PRIMARY_MESSAGE_HASH_VALUE_64 = 64
    const val ORIGINAL_DATA_ELEMENTS_90 = 90
    const val REPLACEMENT_AMOUNTS_95 = 95
    const val ACCOUNT_IDENTIFICATION1_102 = 102
    const val ACCOUNT_IDENTIFICATION2_103 = 103
    const val POS_DATA_CODE_123 = 123
    const val SECONDARY_MESSAGE_HASH_VALUE_128 = 128
    val BASE_URL_FOR_LOGGING_TO_BACKEND = UtilityParams.netPlusPayBaseUrl

    // ======= Copied from netpos ====== //
    const val STATE_PAYMENT_STAND_BY = 0
    const val STATE_PAYMENT_STARTED = 1
    const val STATE_PAYMENT_APPROVED = 2
    const val HISTORY_ACTION = "history_action"
    const val HISTORY_ACTION_DEFAULT = "Default"
    const val HISTORY_ACTION_REPRINT = "Reprint"
    const val HISTORY_ACTION_REFUND = "Refund"
    const val HISTORY_ACTION_PREAUTH = "PreAuth"
    const val HISTORY_ACTION_EOD = "End Of Day"
    const val HISTORY_ACTION_CASH = "Cash"
    const val TRANSACTION_TYPE = "transaction_type"
    const val PREF_APP_TOKEN = "app_token"
    const val PREF_USER_TOKEN = "user_token"
    const val PREF_USER = "user"
    const val PREF_AUTHENTICATED = "authenticated"
    const val PREF_CONFIGURATION_DATA = "configuration_data"
    const val PREF_KEYHOLDER = "pref_keyholder"
    const val PREF_CONFIG_DATA = "pref_config_data"
    const val PREF_LAST_LOCATION = "pref_last_location"
    const val PREF_USE_STORM_TERMINAL_ID = "pref_use_storm_tid"
    const val PREF_PARTNER_THRESHOLD = "pref_partner_threshold"
    const val PREF_BILLS_TOKEN = "pref_bills_token"
    const val PREF_PRINTER_SETTINGS = "pref_printer_settings"
    const val PREF_VALUE_PRINT_CUSTOMER_COPY_ONLY = "print_customer_copy_only"
    const val PREF_VALUE_PRINT_CUSTOMER_AND_MERCHANT_COPY = "print_merchant_and_customer_copy"
    const val PREF_VALUE_PRINT_ASK_BEFORE_PRINTING = "ask_before_printing"
    const val PREF_VALUE_PRINT_SMS = "send_sms"
    const val LAST_POS_CONFIGURATION_TIME = "last_pos_configuration_time"
    const val TRANSACTION_LAST_LOADED_PAGE = "transaction_last_loaded_page"
    const val PREF_REPRINT_PASSWORD = "reprint_password"
    const val ISW_THRESHOLD_TAG = "ISW_THRESHOLD_TAG==>"
    const val TOKEN_RESPONSE_TAG = "TOKEN_RESPONSE_TAG==>"

    // ======= Copied from netpos ====== //
    fun getDownloadParameterManagementData(mgtCode: String, mainString: String): String {
        val lengthOfTag = 2

        val indexOfMgtCodeInMainString = mainString.indexOf(mgtCode)

        if (indexOfMgtCodeInMainString < 0) {
            // throw new IllegalArgumentException("Could not locate data");
            return ""
        }

        val dataLenString = mgtCode.substring(lengthOfTag)

        val dataLength = Integer.parseInt(dataLenString)

        val indexOfMgtData = indexOfMgtCodeInMainString + mgtCode.length

        return mainString.substring(indexOfMgtData, indexOfMgtData + dataLength)
    }

    fun getResponseMessageFromCode(code: String): String {
        when (code) {
            "00" -> return "Approved"
            "01" -> return "Refer to card issuer"
            "02" -> return "Refer to card issuer, special condition"
            "03" -> return "Invalid merchant"
            "04" -> return "Pick-up card"
            "05" -> return "Do not honor"
            "06" -> return "Error"
            "07" -> return "Pick-up card, special condition"
            "08" -> return "Honor with identification"
            "09" -> return "Request in progress"
            "10" -> return "Approved,partial"
            "11" -> return "Approved,VIP"
            "12" -> return "Invalid transaction"
            "13" -> return "Invalid amount"
            "14" -> return "Invalid card number"
            "15" -> return "No such issuer"
            "16" -> return "Approved,update track 3"
            "17" -> return "Customer cancellation"
            "18" -> return "Customer dispute"
            "19" -> return "Re-enter transaction"
            "20" -> return "Invalid response"
            "21" -> return "No action taken"
            "22" -> return "Suspected malfunction"
            "23" -> return "Unacceptable transaction fee"
            "24" -> return "File update not supported"
            "25" -> return "Unable to locate record"
            "26" -> return "Duplicate record"
            "27" -> return "File update edit error"
            "28" -> return "File update file locked"
            "29" -> return "File update failed"
            "30" -> return "Format error"
            "31" -> return "Bank not supported"
            "32" -> return "Completed, partially"
            "33" -> return "Expired card, pick-up"
            "34" -> return "Suspected fraud, pick-up"
            "35" -> return "Contact acquirer, pick-up"
            "36" -> return "Restricted card, pick-up"
            "37" -> return "Call acquirer security, pick-up"
            "38" -> return "PIN tries exceeded, pick-up"
            "39" -> return "No credit account"
            "40" -> return "Function not supported"
            "41" -> return "Lost card"
            "42" -> return "No universal account"
            "43" -> return "Stolen card"
            "44" -> return "No investment account"
            "51" -> return "Not sufficent funds"
            "52" -> return "No check account"
            "53" -> return "No savings account"
            "54" -> return "Expired card"
            "55" -> return "Incorrect PIN"
            "56" -> return "No card record"
            "57" -> return "Transaction not permitted to cardholder"
            "58" -> return "Transaction not permitted on terminal"
            "59" -> return "Suspected fraud"
            "60" -> return "Contact acquirer"
            "61" -> return "Exceeds withdrawal limit"
            "62" -> return "Restricted card"
            "63" -> return "Security violation"
            "64" -> return "Original amount incorrect"
            "65" -> return "Exceeds withdrawal frequency"
            "66" -> return "Call acquirer security"
            "67" -> return "Hard capture"
            "68" -> return "Response received too late"
            "75" -> return "PIN tries exceeded"
            "77" -> return "Intervene, bank approval required"
            "78" -> return "Intervene, bank approval required for partial amount"
            "90" -> return "Cut-off in progress"
            "91" -> return "Issuer or switch inoperative"
            "92" -> return "Routing error"
            "93" -> return "Violation of law"
            "94" -> return "Duplicate transaction"
            "95" -> return "Reconcile error"
            "96" -> return "System malfunction"
            "98" -> return " Exceeds cash limit"
            "A3" -> return "Request Error"
            "A0" -> return "Gateway Error"

            else -> throw IllegalArgumentException("Code not recognized")
        }
    }

    object MTI {
        const val AUTHORIZATION_REQUEST_MTI = "0100"
        const val AUTHORIZATION_RESPONSE_MTI = "0110"

        const val TRANSACTION_REQUEST_MTI = "0200"
        const val TRANSACTION_RESPONSE_MTI = "0210"
        const val TRANSACTION_ADVICE_MTI = "0220"

        const val REVERSAL_ADVICE_MTI = "0420"
        const val REVERSAL_ADVICE_REPEAT_MTI = "0421"
        const val REVERSAL_ADVICE_RESPONSE_MTI = "0430"

        const val NETWORK_MGT_REQUEST_MTI = "0800"
        const val NETWORK_MGT_REQUEST_RESPONSE_MTI = "0810"
    }

    object IsoTransactionTypeCode {
        const val PURCHASE = "00"
        const val CASH_ADVANCE = "01"
        const val REVERSAL = "00"
        const val REFUND = "20"
        const val DEPOSIT = "21"
        const val PURCHASE_WITH_CASH_BACK = "09"
        const val PURCHASE_WITH_ADDITIONAL_DATA = "4F"
        const val BALANCE_INQUIRY = "31"
        const val LINK_ACCOUNT_INQUIRY = "30"
        const val MINI_STATEMENT = "38"
        const val FUND_TRANSFER = "40"
        const val BILL_PAYMENTS = "48"
        const val PREPAID = "4A"
        const val BILLER_LIST_DOWNLOAD = "4B"
        const val PRODUCT_LIST_DOWNLOAD = "4C"
        const val BILLER_SUBSCRIPTION_INFORMATION_DOWNLOAD = "4D"
        const val PAYMENT_VALIDATION = "4E"
        const val PRE_AUTHORIZATION = "60"
        const val PRE_AUTHORIZATION_COMPLETION = "61"
        const val PIN_CHANGE = "90"
        const val TERMINAL_MASTER_KEY = "9A"
        const val TERMINAL_SESSION_KEY = "9B"
        const val TERMINAL_PIN_KEY = "9G"
        const val TERMINAL_PARAMETER_DOWNLOAD = "9C"
        const val CALL_HOME = "9D"
        const val CA_PUBLIC_KEY_DOWNLOAD = "9E"
        const val EMV_APPLICATION_AID_DOWNLOAD = "9F"
        const val DAILY_TRANSACTION_REPORT_DOWNLOAD = "9H"
        const val INITIAL_PIN_ENCRYPTION_KEY_DOWNLOAD_TRACK2_DATA = "9I"
        const val INITIAL_PIN_ENCRYPTION_KEY_DOWNLOAD_EMV = "9J"
        const val DYNAMIC_CURRENCY_CONVERSION = "9K"
        const val NEW_WORKING_KEY_INQUIRY_FROM_HOST = "92"
        const val NEW_WORKING_KEY_FOR_TRAFFIC_ENCRYPTION_INQUIRY = "95"
        const val ECHO_TEST = "99"
    }
}

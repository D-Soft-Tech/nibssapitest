package com.netpluspay.nibssclient.models

import com.netpluspay.nibssclient.util.app.TerminalParams

data class RefundTransactionParams @JvmOverloads constructor(
    val cardData: CardData,
    var transactionResponse: TransactionResponse?,
    var accountType: IsoAccountType = IsoAccountType.DEFAULT_UNSPECIFIED,
    var messageReasonCode: MessageReasonCode = MessageReasonCode.Timeout,
    val terminalId: String? = TerminalParams.TERMINAL_ID
){
    private val action: String = "refundTransaction"
    var fundWallet: Boolean = true
    var originalDataElements: OriginalDataElements? = null
}
package com.netpluspay.nibssclient.models

import com.netpluspay.nibssclient.util.app.TerminalParams

data class CheckBalanceParams @JvmOverloads constructor(
    val cardData: CardData,
    var accountType: IsoAccountType = IsoAccountType.DEFAULT_UNSPECIFIED,
    val terminalId: String? = TerminalParams.defaultTerminalId()
) {
    private val action = "checkBalance"
}
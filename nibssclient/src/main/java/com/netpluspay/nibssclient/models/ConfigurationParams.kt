package com.netpluspay.nibssclient.models

import com.netpluspay.nibssclient.util.app.TerminalParams

data class ConfigurationParams(
    var terminalId: String? = TerminalParams.defaultTerminalId(),
    private var terminalSerial: String? = TerminalParams.defaultTerminalSerial(),
) {
    var action = "getConfigData"
    fun validate() = terminalId != null && terminalSerial != null
}
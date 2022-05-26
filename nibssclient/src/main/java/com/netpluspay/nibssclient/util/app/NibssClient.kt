package com.netpluspay.nibssclient.util.app

object NibssClient {

    @JvmStatic
    @JvmOverloads
    fun init(
        clientCertAssetFile: String,
        clientKeyAssetFile: String,
        terminalId: String,
        terminalSerial: String,
        fileContent: Boolean = false
    ) {
        TerminalParams.CLIENT_CERT_ASSET = clientCertAssetFile
        TerminalParams.CLIENT_KEY_ASSET = clientKeyAssetFile
        TerminalParams.TERMINAL_ID = terminalId
        TerminalParams.TERMINAL_SERIAL = terminalSerial
        TerminalParams.FILE_CONTENT = fileContent
    }

    @JvmStatic
    fun init2(
        terminalId: String,
        terminalSerial: String
    ) {
        TerminalParams.TERMINAL_ID = terminalId
        TerminalParams.TERMINAL_SERIAL = terminalSerial
    }

    @JvmStatic
    @JvmOverloads
    fun init(
        clientCertAssetFile: String,
        clientKeyAssetFile: String,
        fileContent: Boolean = false
    ) {
        TerminalParams.CLIENT_CERT_ASSET = clientCertAssetFile
        TerminalParams.CLIENT_KEY_ASSET = clientKeyAssetFile
        TerminalParams.FILE_CONTENT = fileContent
    }

    @JvmStatic
    fun useTestEnvironment(useTestEnvironment: Boolean) {
        TerminalParams.ON_TEST_ENV = useTestEnvironment
        if (useTestEnvironment) {
            TerminalParams.CONNECTION_HOST = TerminalParams.TEST_HOST
            TerminalParams.CONNECTION_PORT = TerminalParams.TEST_PORT
        } else {
            TerminalParams.CONNECTION_HOST = TerminalParams.PROD_HOST
            TerminalParams.CONNECTION_PORT = TerminalParams.PROD_PORT
        }
    }

    @JvmStatic
    fun setTestParams(host: String, port: Int) {
        TerminalParams.TEST_HOST = host
        TerminalParams.TEST_PORT = port
    }

    @JvmStatic
    fun useSSL(useSSL: Boolean) {
        TerminalParams.USE_SSL = useSSL
    }
}

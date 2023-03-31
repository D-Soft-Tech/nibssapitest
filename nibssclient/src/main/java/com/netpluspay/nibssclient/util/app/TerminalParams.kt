package com.netpluspay.nibssclient.util.app

import com.netpluspay.nibssclient.util.UtilityParams

object TerminalParams {
    val PROD_HOST = UtilityParams.nibss_ip
    val PROD_PORT = UtilityParams.nibss_port
    var TEST_HOST = UtilityParams.nibbs_test_url
    var TEST_PORT = UtilityParams.nibss_test_port
    var ON_TEST_ENV = false
    var CONNECTION_HOST = PROD_HOST
    var CONNECTION_PORT = PROD_PORT
    var USE_SSL = true
    var FILE_CONTENT = false

    lateinit var CLIENT_CERT_ASSET: String

    lateinit var CLIENT_KEY_ASSET: String

    lateinit var TERMINAL_ID: String

    lateinit var TERMINAL_SERIAL: String

    fun isClientCertInitialized() = ::CLIENT_CERT_ASSET.isInitialized
    fun isClientKeyInitialized() = ::CLIENT_KEY_ASSET.isInitialized
    fun defaultTerminalId() = if (::TERMINAL_ID.isInitialized) TERMINAL_ID else null
    fun defaultTerminalSerial() = if (::TERMINAL_SERIAL.isInitialized) TERMINAL_SERIAL else null
}

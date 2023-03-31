package com.netpluspay.nibssclient.util

object UtilityParams {
    init {
        System.loadLibrary("module-credentials")
    }

    private external fun getNibssIp(): String
    private external fun getNibssPort(): String
    private external fun getNibssTestUrl(): String
    private external fun getNibssTestPort(): String
    private external fun getNetPlusPayUrl(): String
    private external fun getDefaultTid(): String
    private external fun getNibssTestIp(): String
    private external fun getNibssConnectionTestPort(): String
    private external fun getNibssConnectionTestKeyOne(): String
    private external fun getNibssConnectionTestKeyTwo(): String
    private external fun getNibssConnectionTestTid(): String
    val nibss_ip = getNibssIp()
    val nibss_port = getNibssPort().toInt()
    val nibbs_test_url = getNibssTestUrl()
    val nibss_test_port = getNibssTestPort().toInt()
    val netPlusPayBaseUrl = getNetPlusPayUrl()
    val nibssDefaultTid = getDefaultTid()
    val nibssConnectionTestIpAddress = getNibssTestIp()
    val nibssConnectionTestPortAddress = getNibssConnectionTestPort().toInt()
    val nibssTestKey1 = getNibssConnectionTestKeyOne()
    val nibssTestKey2 = getNibssConnectionTestKeyTwo()
    val nibssTestTid = getNibssConnectionTestTid()
}

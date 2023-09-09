package com.danbamitale.epmslib.utils

import com.danbamitale.epmslib.di.DataEcImpl

object UtilityParams {
    init {
        System.loadLibrary("module-params")
    }

    private external fun getSeK(): String
    private external fun getSeiv(): String

    private val STRING_REQ_CRED_SEC_K = getSeK()
    private val STRING_REQ_CRED_IV = getSeiv()

    private val ec = DataEcImpl(STRING_REQ_CRED_SEC_K, STRING_REQ_CRED_IV)

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
    private external fun getSessionKey(): String
    private external fun getIpekTest(): String
    private external fun getKsnTest(): String
    private external fun getKsnLive(): String

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
    val STRING_SESSION_KEY = getSessionKey()
    val STRING_IPEK_TEST = getIpekTest()
    val STRING_KSN_TEST = getKsnTest()
    val STRING_KSN_LIVE = getKsnLive()
}

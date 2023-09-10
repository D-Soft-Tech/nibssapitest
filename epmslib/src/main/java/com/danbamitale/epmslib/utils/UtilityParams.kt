package com.danbamitale.epmslib.utils

import com.danbamitale.epmslib.di.AppModule.providesDataEc

object UtilityParams {
    init {
        System.loadLibrary("module-params")
    }

    private val ec = providesDataEc()

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

    val nibss_ip = ec.decryptData(getNibssIp())
    val nibss_port = ec.decryptData(getNibssPort()).toInt()
    val nibbs_test_url = ec.decryptData(getNibssTestUrl())
    val nibss_test_port = ec.decryptData(getNibssTestPort()).toInt()
    val netPlusPayBaseUrl = ec.decryptData(getNetPlusPayUrl())
    val nibssDefaultTid = ec.decryptData(getDefaultTid())
    val nibssConnectionTestIpAddress = ec.decryptData(getNibssTestIp())
    val nibssConnectionTestPortAddress = ec.decryptData(getNibssConnectionTestPort()).toInt()
    val nibssTestKey1 = ec.decryptData(getNibssConnectionTestKeyOne())
    val nibssTestKey2 = ec.decryptData(getNibssConnectionTestKeyTwo())
    val nibssTestTid = ec.decryptData(getNibssConnectionTestTid())
    val STRING_SESSION_KEY = ec.decryptData(getSessionKey())
    val STRING_IPEK_TEST = ec.decryptData(getIpekTest())
    val STRING_KSN_TEST = ec.decryptData(getKsnTest())
    val STRING_KSN_LIVE = ec.decryptData(getKsnLive())
}

package com.netpluspay.nibssclient.nibss

import com.danbamitale.epmslib.di.AppModule.providesDataEc

object Keys {

    init {
        System.loadLibrary("module-credentials")
    }

    private val ec = providesDataEc()

    private external fun getTestKeyOne(): String
    private external fun getTestKeyTwo(): String
    private external fun getEpmsLiveKeyOne(): String
    private external fun getEpmsLiveKeyTwo(): String
    private external fun getPosvasLiveKeyOne(): String
    private external fun getPosvasLiveKeyTwo(): String

    val testKey1 = ec.decryptData(getTestKeyOne())
    val testKey2 = ec.decryptData(getTestKeyTwo())

    // epms
    val epmsLiveKey1 = ec.decryptData(getEpmsLiveKeyOne())
    val epmsLiveKey2 = ec.decryptData(getEpmsLiveKeyTwo())

    // posvas
    val posvasLiveKey1 = ec.decryptData(getPosvasLiveKeyOne())
    val posvasLiveKey2 = ec.decryptData(getPosvasLiveKeyTwo())
}

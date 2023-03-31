package com.netpluspay.nibssclient.nibss

object Keys {

    init {
        System.loadLibrary("api-keys")
    }

    private external fun getTestKeyOne(): String
    private external fun getTestKeyTwo(): String
    private external fun getEpmsLiveKeyOne(): String
    private external fun getEpmsLiveKeyTwo(): String
    private external fun getPosvasLiveKeyOne(): String
    private external fun getPosvasLiveKeyTwo(): String

    val testKey1 = getTestKeyOne()
    val testKey2 = getTestKeyTwo()

    // epms
    val epmsLiveKey1 = getEpmsLiveKeyOne()
    val epmsLiveKey2 = getEpmsLiveKeyTwo()

    // posvas
    val posvasLiveKey1 = getPosvasLiveKeyOne()
    val posvasLiveKey2 = getPosvasLiveKeyTwo()
}

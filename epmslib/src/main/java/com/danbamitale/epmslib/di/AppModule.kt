package com.danbamitale.epmslib.di

import com.danbamitale.epmslib.comms.DataEcDc
import com.danbamitale.epmslib.domain.DataEc

object AppModule {
    init {
        System.loadLibrary("module-params")
    }

    private external fun getSeK(): String
    private external fun getSeiv(): String

    private external fun getSeK2(): String
    private external fun getSeiv2(): String

    private val STRING_REQ_CRED_SEC_K = getSeK()
    private val STRING_REQ_CRED_IV = getSeiv()

    private val STRING_REQ_CRED_SEC_K_2 = getSeK2()
    private val STRING_REQ_CRED_IV_2 = getSeiv2()

    private fun providesDataEcImpl(): DataEcImpl =
        DataEcImpl(STRING_REQ_CRED_SEC_K, STRING_REQ_CRED_IV)

    private fun providesDataEcImpl2(): DataEcDc =
        DataEcDc(STRING_REQ_CRED_SEC_K_2, STRING_REQ_CRED_IV_2)

    fun providesDataEc(): DataEc =
        providesDataEcImpl()

    fun providesDataEc2(): DataEc =
        providesDataEcImpl2()
}

package com.danbamitale.epmslib.di

import com.danbamitale.epmslib.domain.DataEc

object AppModule {
    init {
        System.loadLibrary("module-params")
    }

    private external fun getSeK(): String
    private external fun getSeiv(): String

    private val STRING_REQ_CRED_SEC_K = getSeK()
    private val STRING_REQ_CRED_IV = getSeiv()

    private fun providesDataEcImpl(): DataEcImpl =
        DataEcImpl(STRING_REQ_CRED_SEC_K, STRING_REQ_CRED_IV)

    fun providesDataEc(): DataEc =
        providesDataEcImpl()
}

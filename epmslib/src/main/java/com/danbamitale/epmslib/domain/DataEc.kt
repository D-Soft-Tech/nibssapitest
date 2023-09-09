package com.danbamitale.epmslib.domain

interface DataEc {
    fun encryptData(data: String): String
    fun decryptData(encryptedData: String): String
}

package com.danbamitale.epmslib.di

import com.danbamitale.epmslib.domain.DataEc
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class DataEcImpl(
    private val secretKey: String,
    private val iv: String,
) : DataEc {

    private fun getEncryptionKey(): ByteArray {
        val sha512 = MessageDigest.getInstance("SHA-512")
        return sha512.digest(secretKey.toByteArray()).copyOf(32)
    }

    private fun getEncryptionIV(): ByteArray {
        val sha512 = MessageDigest.getInstance("SHA-512")
        return sha512.digest(iv.toByteArray()).copyOf(16)
    }

    override fun encryptData(data: String): String {
        try {
            val key = SecretKeySpec(getEncryptionKey(), "AES")
            val encryptionIV = IvParameterSpec(getEncryptionIV())
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, key, encryptionIV)
            val encryptedBytes = cipher.doFinal(data.toByteArray())
            return encryptedBytes.joinToString("") { "%02x".format(it) }
        } catch (err: Exception) {
            println(err)
        }
        return ""
    }

    override fun decryptData(encryptedData: String): String {
        try {
            val key = SecretKeySpec(getEncryptionKey(), "AES")
            val encryptionIV = IvParameterSpec(getEncryptionIV())
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, key, encryptionIV)
            val encryptedBytes =
                encryptedData.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes)
        } catch (err: Exception) {
            println(err)
        }
        return ""
    }
}

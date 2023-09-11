package com.danbamitale.epmslib.comms

import com.danbamitale.epmslib.domain.DataEc
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.* // ktlint-disable no-wildcard-imports
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and

class DataEcDc(
    private val secretKey: String,
    private val iv: String,
) : DataEc {

    override fun encryptData(data: String): String {
        val bData: ByteArray = data.toByteArray()
        val bKey: ByteArray = secretKey.toByteArray()
        val bOutput: ByteArray = encrypt(bData, bKey)!!

        return hexToString(bOutput)!!
    }

    override fun decryptData(encryptedData: String): String {
        val bData: ByteArray = stringToHex(encryptedData)
        val bKey: ByteArray = stringToHex(secretKey)
        val bOutput: ByteArray = decrypt(bData, bKey)!!

        return hexToString(bOutput)!!
    }

    private fun getKey(key: ByteArray): ByteArray {
        val bKey = ByteArray(24)
        var i: Int
        when (key.size) {
            8 -> {
                i = 0
                while (i < 8) {
                    bKey[i] = key[i]
                    bKey[i + 8] = key[i]
                    bKey[i + 16] = key[i]
                    i++
                }
            }
            16 -> {
                i = 0
                while (i < 8) {
                    bKey[i] = key[i]
                    bKey[i + 8] = key[i + 8]
                    bKey[i + 16] = key[i]
                    i++
                }
            }
            24 -> {
                i = 0
                while (i < 24) {
                    bKey[i] = key[i]
                    i++
                }
            }
        }
        return bKey
    }

    private fun stringToHex(data: String): ByteArray {
        val result: ByteArray = ByteArray(data.length / 2)
        var i = 0
        while (i < data.length) {
            result[i / 2] = data.substring(i, i + 2).toInt(16).toByte()
            i += 2
        }
        return result
    }

    private fun hexToString(data: ByteArray?): String? {
        if (data == null) {
            return ""
        }
        var result: String? = ""
        for (i in data.indices) {
            var tmp: Int = data[i].toInt() shr 4
            result += Integer.toString(tmp and 0x0F, 16)
            tmp = (data[i] and 0x0F).toInt()
            result += Integer.toString(tmp and 0x0F, 16)
        }
        return result
    }

    private fun encrypt(data: ByteArray?, key: ByteArray?): ByteArray? {
        val sk: SecretKey = SecretKeySpec(getKey(key!!), "AES")
        try {
            val cipher =
                Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.ENCRYPT_MODE, sk)
            return cipher.doFinal(data)
        } catch (_: NoSuchPaddingException) {
        } catch (_: NoSuchAlgorithmException) {
        } catch (_: InvalidKeyException) {
        } catch (_: BadPaddingException) {
        } catch (_: IllegalBlockSizeException) {
        }
        return null
    }

    private fun decrypt(data: ByteArray, key: ByteArray): ByteArray? {
        val sk: SecretKey = SecretKeySpec(getKey(key), "AES")
        try {
            val cipher =
                Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.DECRYPT_MODE, sk)
            return cipher.doFinal(data)
        } catch (_: NoSuchPaddingException) {
        } catch (_: NoSuchAlgorithmException) {
        } catch (_: InvalidKeyException) {
        } catch (_: BadPaddingException) {
        } catch (_: IllegalBlockSizeException) {
        }
        return null
    }
}

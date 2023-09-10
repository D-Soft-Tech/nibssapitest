package com.danbamitale.epmslib.utils

import com.danbamitale.epmslib.di.AppModule.providesDataEc
import java.io.ByteArrayOutputStream
import java.text.NumberFormat
import java.util.*

object Utility {

    init {
        System.loadLibrary("module-params")
    }

    private val ec = providesDataEc()

    private external fun getDefaultIp(): String
    private external fun getDefaultPort(): String

    val POS_VAS_NIBSS_DEFAULT_IP = ec.decryptData(getDefaultIp())
    val POS_VAS_NIBSS_DEFAULT_PORT = ec.decryptData(getDefaultPort()).toInt()

    fun hex(data: ByteArray): String {
        val sb = StringBuilder()
        for (b in data) {
            sb.append(Character.forDigit(b.toInt() and 240 shr 4, 16))
            sb.append(Character.forDigit(b.toInt() and 15, 16))
        }
        return sb.toString()
    }

    fun toHexString(b: ByteArray): String {
        var result = ""
        for (i in b.indices) {
            result += Integer.toString((b[i].toInt() and 0xFF) + 0x100, 16).substring(1)
        }
        return result
    }

    fun hexToByteArray(s: String?): ByteArray {
        var s = s
        if (s == null) {
            s = ""
        }
        val bout = ByteArrayOutputStream()
        var i = 0
        while (i < s.length - 1) {
            val data = s.substring(i, i + 2)
            bout.write(Integer.parseInt(data, 16))
            i += 2
        }
        return bout.toByteArray()
    }

    fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] =
                ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    fun parseLongIntoNairaKoboString(tempAmount: Long, currencySymbol: String = "\u20A6"): String {
        val amountNairaPart = tempAmount / 100.0

        val numFormatter = NumberFormat.getInstance(Locale.US)
        numFormatter.minimumFractionDigits = 2

        var amountInN = numFormatter.format(amountNairaPart)

        amountInN = currencySymbol + amountInN

        return amountInN
    }
}

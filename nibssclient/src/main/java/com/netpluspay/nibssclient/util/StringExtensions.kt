package com.netpluspay.nibssclient.util

import java.io.UnsupportedEncodingException
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Pad a string to the left
 *
 * @param length
 * Length of resulting string
 * @param padChar
 * pad character
 * @return Padded string
 */
fun String.padLeft(length: Int, padChar: Char): String {
    val remaining = length - this.length

    var newData = this
    for (i in 0 until remaining)
        newData = padChar + newData
    return newData
}

/**
 * Pad a string to the left
 *
 * @param length
 * Length of resulting string
 * @param padChar
 * pad character
 * @return Padded string
 */
fun String.padRight(length: Int, padChar: Char): String {
    val remaining = length - this.length

    var newData = this
    for (i in 0 until remaining)
        newData += padChar
    return newData
}

fun String.maskPan(): String {
    val acqCode = this.substring(0, 6)
    val endCode = this.substring(this.length - 4, this.length)

    val elen = this.length - acqCode.length - endCode.length

    val etext = "x".padLeft(elen, 'x')

    return acqCode + etext + endCode
}

fun String.xor(key2: String): String {
    val keyB1 = Utility.hexStringToByteArray(this); // + key1.substring(0, 16))
    val keyB2 = Utility.hexStringToByteArray(key2); // + key2.substring(0, 16))

    for (i in keyB2.indices) {
        keyB1[i] = (keyB1[i].toInt() xor keyB2[i].toInt()).toByte()
    }

    return Utility.hex(keyB1).toUpperCase()
}

@Throws(UnsupportedEncodingException::class)
fun String.generateHash256Value(key: String): String {
    val m: MessageDigest
    var hashText: String? = null
    val actualKeyBytes = Utility.hexStringToByteArray(key)

    try {
        m = MessageDigest.getInstance("SHA-256")
        m.update(actualKeyBytes, 0, actualKeyBytes.size)
        try {
            m.update(this.toByteArray(charset("UTF-8")), 0, this.length)
        } catch (ex: UnsupportedEncodingException) {
            ex.printStackTrace()
        }

        hashText = BigInteger(1, m.digest()).toString(16)
    } catch (ex: NoSuchAlgorithmException) {
        ex.printStackTrace()
    }

    if (hashText!!.length < 64) {
        val numberOfZeroes = 64 - hashText.length
        var zeroes = ""

        for (i in 0 until numberOfZeroes)
            zeroes += "0"

        hashText = zeroes + hashText
    }

    return hashText
}

fun String.hexByteArray() = Utility.hexStringToByteArray(this)

fun String.toInt16() = this.toInt(16)

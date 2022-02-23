package com.netpluspay.nibssclient.models

import java.lang.IllegalArgumentException

data class NibssCA(
    val keyIndex: String,
    val internalReferenceNumber: Int,
    val keyName: String,
    val RID: String,
    val hashAlgorithm: String,
    val modulus: String,
    val exponent: String,
    val hash: String,
    val keyAlgorithm: String
) {


    companion object {
        const val CA_KEY_INDEX_32 = 32
        const val CA_KEY_INTERNAL_REFERENCE_33 = 33
        const val CA_KEY_NAME_34 = 34
        const val RID_35 = 35
        const val HASH_ALGORITHM_36 = 36
        const val CAPK_MODULUS_37 = 37
        const val CAPK_EXPONENT_38 = 38
        const val CAPK_HASH_39 = 39
        const val KEY_ALGORITHM_40 = 40

        val tags = arrayOf(
            CA_KEY_INDEX_32,
            CA_KEY_INTERNAL_REFERENCE_33,
            CA_KEY_NAME_34,
            RID_35,
            HASH_ALGORITHM_36,
            CAPK_MODULUS_37,
            CAPK_EXPONENT_38,
            CAPK_HASH_39,
            KEY_ALGORITHM_40
        )

        fun parse(data: String): NibssCA {
            val sb = StringBuilder(data)


            return tags.map {
                if (sb.length > 5 && sb.startsWith(it.toString())) {
                    sb.delete(0, 2)
                    val len = sb.substring(0, 3).toInt()
                    sb.delete(0, 3)
                    if (len > sb.length) {
                        throw IllegalArgumentException("Invalid input")
                    }

                    val value = sb.substring(0, len)
                    sb.delete(0, len)
                    Pair(it, value)
                } else {
                    Pair(it, "")
                }
            }.toMap().run {
                NibssCA(
                    keyIndex = get(CA_KEY_INDEX_32)!!,
                    internalReferenceNumber = get(CA_KEY_INTERNAL_REFERENCE_33)!!.toInt(),
                    keyName = get(CA_KEY_NAME_34)!!,
                    RID = get(RID_35)!!,
                    hashAlgorithm = get(HASH_ALGORITHM_36)!!,
                    modulus = get(CAPK_MODULUS_37)!!,
                    exponent = get(CAPK_EXPONENT_38)!!,
                    hash = get(CAPK_HASH_39)!!,
                    keyAlgorithm = get(KEY_ALGORITHM_40)!!
                )
            }
        }


        fun parseNibssResponse(response: String) = response.splitToSequence('~').map {
            parse(it)
        }.toList()

    }
}
package com.netpluspay.nibssclient.models

import com.netpluspay.nibssclient.util.tlv.TLVList

data class CardData(
    val track2Data: String,
    val nibssIccSubset: String,
    val panSequenceNumber: String,
    val posEntryMode: String = "051"
) {

    var pinBlock: String? = null

    var pan = getPan(track2Data)
    var serviceCode = getServiceCode(track2Data)
    var expiryDate = getExpiryDate(track2Data)
    var acquiringInstitutionIdCode = getAcquiringInstitutionIdCode(track2Data)

    companion object {
        val NIBSS_TAGS = arrayOf(
            "9F26",
            "9F27",
            "9F10",
            "9F37",
            "9F36",
            "95",
            "9A",
            "9C",
            "9F02",
            "5F2A",
            "82",
            "9F1A",
            "9F34",
            "9F33",
            "9F35",
            "9F1E",
            "84",
            "9F09",
            "9F03",
            "5F34",
            "8E"
        )

        fun getNibssTags(iccData: String): String {
            val tlvList = TLVList.fromBinary(iccData)
            val nibssIcc = StringBuilder()
            for (tag in NIBSS_TAGS) {
                if (tlvList.contains(tag)) {
                    nibssIcc.append(tlvList.getTLV(tag).toString())
                }
            }

            return nibssIcc.toString()
        }

        fun initCardDataFromTrack(fullIcc: String): CardData {

            val tlvList = TLVList.fromBinary(fullIcc)
            val nibssIcc = StringBuilder()
            for (tag in NIBSS_TAGS) {
                if (tlvList.contains(tag)) {
                    nibssIcc.append(tlvList.getTLV(tag).toString())
                }
            }
            val posEntryMode =
                if (tlvList.getTLVVL("9F39").isNullOrEmpty()) "051" else tlvList.getTLVVL("9F39")
            return CardData(
                nibssIccSubset = nibssIcc.toString(),
                track2Data = tlvList.getTLVVL("57"),
                panSequenceNumber = tlvList.getTLVVL("5F34").padLeft(3, '0'),
                posEntryMode = posEntryMode
            )
        }

        fun getPan(track2Data: String, separatingChar: Char = 'D'): String {
            val indexOfToken = track2Data.indexOf(separatingChar, 0, true)

            return track2Data.substring(0, indexOfToken)
        }

        fun getExpiryDate(track2Data: String, separatingChar: Char = 'D'): String {
            val indexOfToken = track2Data.indexOf(separatingChar, 0, true)
            val indexOfExpiryDate = indexOfToken + 1
            val lengthOfExpiryDate = 4
            return track2Data.substring(indexOfExpiryDate, indexOfExpiryDate + lengthOfExpiryDate)
        }

        fun getServiceCode(track2Data: String, separatingChar: Char = 'D'): String {
            val indexOfToken = track2Data.indexOf(separatingChar.toString(), 0, true)
            val indexOfServiceCode = indexOfToken + 5
            val lengthOfServiceCode = 3
            return track2Data.substring(
                indexOfServiceCode,
                indexOfServiceCode + lengthOfServiceCode
            )
        }

        fun getAcquiringInstitutionIdCode(track2Data: String): String {
            return track2Data.substring(0, 6)
        }
    }
}

fun getCardHolderFromTrack1(track1: String): String {
    val tokens = track1.split("^")

    return tokens[1]
}

fun String.padLeft(length: Int, padChar: Char): String {
    val remaining = length - this.length

    var newData = this
    for (i in 0 until remaining)
        newData = padChar + newData
    return newData
}

package com.netpluspay.nibssclient.models

import java.lang.IllegalArgumentException

data class NibssAID(
    val index: String,
    val internalReferenceNumber: Int,
    val AID: String,
    val match: Int,
    val applicationName: String,
    val applicationVersion: String,
    val appSelectionPriority: String,
    val DDOL: String,
    val TDOL: String,
    val tflDomestic: String = "",
    val tflInternational: String = "",
    val offlineThresholdDomestic: String = "",
    val maxTargetDomestic: String = "",
    val maxTargetInternational: String = "",
    val targetPercentageDomestic: String = "",
    val targetPercentageInternational: String = "",
    val tacDefault: String,
    val tacDenial: String,
    val tacOnline: String
) {

    companion object {
        const val AID_INDEX_13 = 13
        const val APP_INTERNAL_REF_NUM_14 = 14
        const val AID_15 = 15
        const val MATCH_16 = 16
        const val APP_NAME_17 = 17
        const val APP_VERSION_18 = 18
        const val APP_SEL_PRIORITY_19 = 19
        const val DDOL_20 = 20
        const val TDOL_21 = 21
        const val TFL_DOMESTIC_22 = 22
        const val TFL_INTERNATIONAL_23 = 23
        const val OFFLINE_THRESHOLD_DOMESTIC_24 = 24
        const val MAX_TARGET_DOMESTIC_25 = 25
        const val MAX_TARGET_INTERNATIONAL_26 = 26
        const val TARGET_PERCANTAGE_DOMESTIC_27 = 27
        const val TARGET_PERCANTAGE_INTERNATIONAL_28 = 28
        const val DEFAULT_TAC_VALUE_29 = 29
        const val TAC_DENIAL_30 = 30
        const val TAC_ONLINE_31 = 31

        val tags = arrayOf(
            AID_INDEX_13,
            APP_INTERNAL_REF_NUM_14,
            AID_15,
            MATCH_16,
            APP_NAME_17,
            APP_VERSION_18,
            APP_SEL_PRIORITY_19,
            DDOL_20,
            TDOL_21,
            TFL_DOMESTIC_22,
            TFL_INTERNATIONAL_23,
            OFFLINE_THRESHOLD_DOMESTIC_24,
            MAX_TARGET_DOMESTIC_25,
            MAX_TARGET_INTERNATIONAL_26,
            TARGET_PERCANTAGE_DOMESTIC_27,
            TARGET_PERCANTAGE_INTERNATIONAL_28,
            DEFAULT_TAC_VALUE_29,
            TAC_DENIAL_30,
            TAC_ONLINE_31
        )

        fun parse(data: String): NibssAID {
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
                NibssAID(
                    index = get(AID_INDEX_13)!!,
                    internalReferenceNumber = get(APP_INTERNAL_REF_NUM_14)!!.toInt(),
                    AID = get(AID_15)!!,
                    match = get(MATCH_16)?.toInt() ?: 0,
                    applicationName = get(APP_NAME_17)!!,
                    applicationVersion = get(APP_VERSION_18)!!,
                    appSelectionPriority = get(APP_SEL_PRIORITY_19)!!,
                    DDOL = get(DDOL_20)!!,
                    TDOL = get(TDOL_21)!!,
                    tflDomestic = get(TFL_DOMESTIC_22) ?: "",
                    tflInternational = get(TFL_INTERNATIONAL_23) ?: "",
                    offlineThresholdDomestic = get(OFFLINE_THRESHOLD_DOMESTIC_24) ?: "",
                    maxTargetDomestic = get(MAX_TARGET_DOMESTIC_25) ?: "",
                    maxTargetInternational = get(MAX_TARGET_INTERNATIONAL_26) ?: "",
                    targetPercentageDomestic = get(TARGET_PERCANTAGE_DOMESTIC_27) ?: "",
                    targetPercentageInternational = get(TARGET_PERCANTAGE_INTERNATIONAL_28) ?: "",
                    tacDefault = get(DEFAULT_TAC_VALUE_29)!!,
                    tacDenial = get(TAC_DENIAL_30)!!,
                    tacOnline = get(TAC_ONLINE_31)!!
                )
            }
        }

        fun parseNibssResponse(response: String) = response.splitToSequence('~').map {
            parse(it)
        }.toList()

    }
}
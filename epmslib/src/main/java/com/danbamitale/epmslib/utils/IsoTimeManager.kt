package com.danbamitale.epmslib.utils

import java.text.SimpleDateFormat
import java.util.*

class IsoTimeManager {

    private val now = Date(System.currentTimeMillis())


    val longDate: String
        get() = SimpleDateFormat(LONG_DATE, Locale.getDefault()).format(now)

    val shortDate: String
        get() = SimpleDateFormat(SHORT_DATE, Locale.getDefault()).format(now)

    val time: String
        get() = SimpleDateFormat(LONG_TIME, Locale.getDefault()).format(now)


    val fullDate: String
        get() =  SimpleDateFormat(FULL_DATE_TIME, Locale.getDefault()).format(now)



    companion object {
        private val LONG_DATE = "MMddHHmmss"
        private val SHORT_DATE = "MMdd"
        private val LONG_TIME = "HHmmss"
        private val FULL_DATE_TIME = "yyyyMMddHHmmss"
    }
}

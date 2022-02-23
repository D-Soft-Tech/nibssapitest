package com.netpluspay.nibssclient.util

import java.text.DecimalFormat

fun Number.formatAmount(): String {
    return DecimalFormat("#.##").format(this)
}

fun Number.formatCurrencyAmount(currencySymbol: String = "\u20A6"): String {
    val format = DecimalFormat("#,###.00")
    return "$currencySymbol ${format.format(this)}"
}
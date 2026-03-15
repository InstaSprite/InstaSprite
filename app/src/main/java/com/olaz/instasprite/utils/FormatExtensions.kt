package com.olaz.instasprite.utils

import java.text.DateFormat
import java.text.DecimalFormat
import java.util.Locale
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

fun Long.toDateString(dateFormat: Int = DateFormat.MEDIUM): String {
    val df = DateFormat.getDateInstance(dateFormat, Locale.getDefault())
    return df.format(this)
}

fun Long.toSuffixString(): String {
    if (this < 1000) {
        return this.toString()
    }

    val suffixes = charArrayOf('k', 'M', 'B', 'T', 'P', 'E')
    val value = floor(log10(this.toDouble())).toInt()
    val base = value / 3

    if (base >= suffixes.size) {
        return this.toString()
    }

    val decimalFormat = DecimalFormat("#0.0")
    val scaledNum = this / 10.0.pow((base * 3).toDouble())
    return decimalFormat.format(scaledNum) + suffixes[base - 1]
}
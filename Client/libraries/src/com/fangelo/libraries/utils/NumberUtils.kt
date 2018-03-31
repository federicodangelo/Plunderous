package com.fangelo.libraries.utils

fun Float.format(digits: Int): String = java.lang.String.format("%.${digits}f", this)

fun Double.format(digits: Int): String = java.lang.String.format("%.${digits}f", this)

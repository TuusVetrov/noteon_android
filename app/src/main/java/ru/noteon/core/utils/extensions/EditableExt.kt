package ru.noteon.core.utils.extensions

import android.text.Editable

fun Editable?.toStringOrEmpty(): String = this?.toString() ?: ""
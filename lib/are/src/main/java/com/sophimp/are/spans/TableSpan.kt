package com.sophimp.are.spans

import android.graphics.drawable.Drawable
import android.text.style.ImageSpan


class TableSpan(
    var htmlStr: String,
    drawable: Drawable
) : ImageSpan(drawable), IClickableSpan, ISpan {
    override val html: String
        get() = htmlStr
}
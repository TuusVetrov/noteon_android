package com.sophimp.are.spans

import android.graphics.Color
import android.text.style.BackgroundColorSpan

class FontBackgroundColorSpan(var colorStr: String) : BackgroundColorSpan(Color.TRANSPARENT), IDynamicSpan {
    private var mColor = Color.parseColor(colorStr)
    override val dynamicFeature: String
        get() = colorStr

}
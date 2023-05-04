package com.sophimp.are.spans

import android.text.style.AbsoluteSizeSpan

class FontSizeSpan(size: Int) : AbsoluteSizeSpan(size, true), IDynamicSpan {
    private var mFontSize = size
    override val dynamicFeature: String
        get() = "$mFontSize"

}
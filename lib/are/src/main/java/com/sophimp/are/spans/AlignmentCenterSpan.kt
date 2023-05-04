package com.sophimp.are.spans

import android.text.Layout
import android.text.style.AlignmentSpan

class AlignmentCenterSpan : AlignmentSpan, ISpan {
    override fun getAlignment(): Layout.Alignment {
        return Layout.Alignment.ALIGN_CENTER
    }
}
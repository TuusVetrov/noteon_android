package com.sophimp.are.spans

import android.text.Layout
import android.text.style.AlignmentSpan

class AlignmentRightSpan : AlignmentSpan, ISpan {
    override fun getAlignment(): Layout.Alignment {
        return Layout.Alignment.ALIGN_OPPOSITE
    }
}
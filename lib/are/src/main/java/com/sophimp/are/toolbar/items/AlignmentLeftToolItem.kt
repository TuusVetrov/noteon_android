package com.sophimp.are.toolbar.items

import com.sophimp.are.R
import com.sophimp.are.style.AlignmentLeftStyle
import com.sophimp.are.toolbar.IToolbarItemClickAction

class AlignmentLeftToolItem(style: AlignmentLeftStyle, itemClickAction: IToolbarItemClickAction? = null) :
    AbstractItem(style, itemClickAction) {
    override val srcResId: Int
        get() = R.drawable.ic_format_align_left_unsel

    override fun iconClickHandle() {
        super.iconClickHandle()
    }
}
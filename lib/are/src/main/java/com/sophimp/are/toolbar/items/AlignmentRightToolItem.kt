package com.sophimp.are.toolbar.items

import com.sophimp.are.R
import com.sophimp.are.style.AlignmentRightStyle
import com.sophimp.are.toolbar.IToolbarItemClickAction

class AlignmentRightToolItem(style: AlignmentRightStyle, itemClickAction: IToolbarItemClickAction? = null) :
    AbstractItem(style, itemClickAction) {
    override val srcResId: Int
        get() = R.drawable.ic_format_align_right_unsel

    override fun iconClickHandle() {
        super.iconClickHandle()
    }
}
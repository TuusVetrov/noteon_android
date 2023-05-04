package com.sophimp.are.toolbar.items

import com.sophimp.are.R
import com.sophimp.are.style.AlignmentCenterStyle
import com.sophimp.are.toolbar.IToolbarItemClickAction

class AlignmentCenterToolItem(style: AlignmentCenterStyle, itemClickAction: IToolbarItemClickAction? = null) :
    AbstractItem(style, itemClickAction) {
    override val srcResId: Int
        get() = R.drawable.ic_format_align_center_unsel

    override fun iconClickHandle() {
        super.iconClickHandle()
    }
}
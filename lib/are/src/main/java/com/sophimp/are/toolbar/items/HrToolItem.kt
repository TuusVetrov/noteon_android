package com.sophimp.are.toolbar.items

import com.sophimp.are.R
import com.sophimp.are.style.HrStyle
import com.sophimp.are.toolbar.IToolbarItemClickAction

class HrToolItem(style: HrStyle, itemClickAction: IToolbarItemClickAction? = null) :
    AbstractItem(style, itemClickAction) {
    override val srcResId: Int
        get() = R.drawable.ic_horizontal_split_unsel

}
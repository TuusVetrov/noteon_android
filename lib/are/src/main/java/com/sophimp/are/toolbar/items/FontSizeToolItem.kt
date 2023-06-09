package com.sophimp.are.toolbar.items

import com.sophimp.are.R
import com.sophimp.are.style.FontSizeStyle
import com.sophimp.are.toolbar.IToolbarItemClickAction

class FontSizeToolItem(style: FontSizeStyle, itemClickAction: IToolbarItemClickAction? = null) :
    AbstractItem(style, itemClickAction) {
    override val srcResId: Int
        get() = R.drawable.ic_format_size_unsel

}
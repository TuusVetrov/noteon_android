package com.sophimp.are.toolbar.items

import com.sophimp.are.R
import com.sophimp.are.style.FontColorStyle
import com.sophimp.are.toolbar.IToolbarItemClickAction

class FontColorToolItem(style: FontColorStyle, itemClickAction: IToolbarItemClickAction? = null) :
    AbstractItem(style, itemClickAction) {

    override val srcResId: Int
        get() = R.drawable.ic_format_color_text_unsel

}
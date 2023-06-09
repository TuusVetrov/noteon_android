package com.sophimp.are.toolbar.items

import com.sophimp.are.R
import com.sophimp.are.style.IndentLeftStyle
import com.sophimp.are.toolbar.IToolbarItemClickAction

class IndentLeftToolItem(style: IndentLeftStyle, itemClickAction: IToolbarItemClickAction? = null) : AbstractItem(style, itemClickAction) {
    override val srcResId: Int
        get() = R.drawable.ic_format_indent_decrease_unsel

}
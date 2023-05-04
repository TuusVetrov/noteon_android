package com.sophimp.are.toolbar.items

import com.sophimp.are.R
import com.sophimp.are.style.ListNumberStyle
import com.sophimp.are.toolbar.IToolbarItemClickAction

class ListNumberToolItem(style: ListNumberStyle, itemClickAction: IToolbarItemClickAction? = null) : AbstractItem(style, itemClickAction) {
    override val srcResId: Int
        get() = R.drawable.ic_format_list_numbered_unsel

}
package com.sophimp.are.toolbar.items

import com.sophimp.are.R
import com.sophimp.are.style.EmojiStyle
import com.sophimp.are.toolbar.IToolbarItemClickAction

class EmojiToolItem(style: EmojiStyle, itemClickAction: IToolbarItemClickAction? = null) : AbstractItem(style, itemClickAction) {
    override val srcResId: Int
        get() = R.drawable.ic_insert_emoticon_unsel

    override fun iconClickHandle() {
        super.iconClickHandle()
    }
}
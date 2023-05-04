package com.sophimp.are.toolbar.items

import android.graphics.Color
import com.sophimp.are.R
import com.sophimp.are.listener.OnSelectionChangeListener
import com.sophimp.are.style.SubscriptStyle
import com.sophimp.are.toolbar.IToolbarItemClickAction

class SubscriptToolItem(style: SubscriptStyle, itemClickAction: IToolbarItemClickAction? = null) : AbstractItem(style, itemClickAction) {

    override fun iconClickHandle() {
        super.iconClickHandle()
        iconView.setIconResId(if (style.isChecked) R.drawable.ic_subscript_sel else R.drawable.ic_subscript_unsel)
    }

    override val srcResId: Int
        get() = R.drawable.ic_subscript_unsel

}
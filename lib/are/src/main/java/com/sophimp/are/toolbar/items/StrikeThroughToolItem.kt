package com.sophimp.are.toolbar.items

import com.sophimp.are.R
import com.sophimp.are.listener.OnSelectionChangeListener
import com.sophimp.are.style.StrikethroughStyle
import com.sophimp.are.toolbar.IToolbarItemClickAction

class StrikeThroughToolItem(style: StrikethroughStyle, itemClickAction: IToolbarItemClickAction? = null) :
    AbstractItem(style, itemClickAction) {
    override val srcResId: Int
        get() = R.drawable.ic_strikethrough_unsel

    init {
        style.mEditText.registerOnSelectionChangedListener(object : OnSelectionChangeListener {
            override fun onSelectionChanged(selectionStart: Int, selectionEnd: Int) {
                style.onSelectionChanged(selectionEnd)
                iconView.setIconResId(if (style.isChecked) R.drawable.ic_strikethrough_sel else R.drawable.ic_strikethrough_unsel)
            }
        })
    }

    override fun iconClickHandle() {
        super.iconClickHandle()
        iconView.setIconResId(if (style.isChecked) R.drawable.ic_strikethrough_sel else R.drawable.ic_strikethrough_unsel)
    }
}
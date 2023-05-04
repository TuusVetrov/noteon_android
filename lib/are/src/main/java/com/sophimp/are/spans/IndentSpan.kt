package com.sophimp.are.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.style.LeadingMarginSpan
import com.sophimp.are.Constants
import com.sophimp.are.utils.Util.log

class IndentSpan constructor(level: Int = 0) : LeadingMarginSpan, ISpan {
    private var mLeadingMargin: Int

    /**
     * Set leading level
     *
     * @param level
     */
    var mLevel: Int = level

    companion object {
        /**
         * from 0 to 5, total five level
         */
        const val MAX_LEVEL = 5
        const val LEADING_MARGIN = 40
    }

    override fun getLeadingMargin(first: Boolean): Int {
        if (mLeadingMargin == 0) {
            mLeadingMargin = LEADING_MARGIN * mLevel
        }
        return mLeadingMargin
    }

    override fun drawLeadingMargin(
        c: Canvas, p: Paint, x: Int, dir: Int, top: Int,
        baseline: Int, bottom: Int, text: CharSequence, start: Int, end: Int,
        first: Boolean, layout: Layout
    ) {
        c.drawText(
            Constants.ZERO_WIDTH_SPACE_STR,
            x + dir + mLeadingMargin.toFloat(),
            baseline.toFloat(),
            p
        )
    }

    fun setLeadingMargin(leadingMargin: Int) {
        mLevel = leadingMargin / LEADING_MARGIN
        mLeadingMargin = LEADING_MARGIN * mLevel
    }

    /**
     * Increase leading level.
     *
     * @return
     */
    fun increaseLevel(): Int {
        if (mLevel >= MAX_LEVEL) {
            log("Максимальный отступ на строку " + MAX_LEVEL)
            return mLevel
        }
        ++mLevel
        mLeadingMargin = LEADING_MARGIN * mLevel
        return mLevel
    }

    /**
     * Decrease leading level.
     *
     * @return
     */
    fun decreaseLevel(): Int {
        --mLevel
        if (mLevel < 0) {
            mLevel = 0
        }
        mLeadingMargin = LEADING_MARGIN * mLevel
        return mLevel
    }

    init {
        mLevel = Math.min(level, MAX_LEVEL)
        mLeadingMargin = LEADING_MARGIN * mLevel
    }

}
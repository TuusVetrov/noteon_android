package com.sophimp.are.style

import android.text.Editable
import android.text.Spanned
import com.sophimp.are.RichEditText
import com.sophimp.are.spans.ISpan
import java.util.*
import kotlin.math.max
import kotlin.math.min

abstract class BaseCharacterStyle<E : ISpan>(editText: RichEditText) :
    BaseStyle<E>(editText) {

    var mFeature = ""

    override fun itemClickOnNonEmptyParagraph(curPStart: Int, curPEnd: Int): Int {
        handleAbsButtonClick(mEditText.selectionStart, mEditText.selectionEnd)
        return 0
    }

    override fun handleDeleteEvent(editable: Editable, epStart: Int, epEnd: Int) {
        handleDeleteAbsStyle()
    }

    override fun handleSingleParagraphInput(
        editable: Editable,
        changedText: String?,
        beforeSelectionStart: Int,
        afterSelectionEnd: Int,
        epStart: Int,
        epEnd: Int
    ) {
        handleAbsInput(beforeSelectionStart)
    }

    override fun handleMultiParagraphInput(
        editable: Editable,
        changedText: String?,
        beforeSelectionStart: Int,
        afterSelectionEnd: Int,
        epStart: Int,
        epEnd: Int
    ) {
        handleAbsInput(beforeSelectionStart)
    }

    protected open fun handleAbsInput(beforeSelectionStart: Int) {
        val sEnd = mEditText.selectionEnd
        if (beforeSelectionStart < sEnd) {
            val editable = mEditText.editableText
            val targetSpans =
                editable.getSpans(max(beforeSelectionStart - 1, 0), sEnd, targetClass())
            val newSpan = newSpan()
            if (targetSpans.isNotEmpty()) {
                var lastSpan = targetSpans[0]
                var preSpanStart = editable.getSpanStart(lastSpan)
                var preSpanEnd = editable.getSpanEnd(lastSpan)
                targetSpans.forEach {
                    preSpanStart = min(editable.getSpanStart(it), preSpanStart)
                    preSpanEnd = max(editable.getSpanEnd(it), preSpanEnd)
                }
                if (newSpan != null) {
                    removeSpans(editable, targetSpans)
                    if (!checkFeatureEqual(newSpan, targetSpans[targetSpans.size - 1])) {
                        if (preSpanStart < beforeSelectionStart) {
                            setSpan(lastSpan, preSpanStart, beforeSelectionStart)
                        }
                        if (beforeSelectionStart < sEnd) {
                            setSpan(newSpan, beforeSelectionStart, sEnd)
                        }
                        if (sEnd < preSpanEnd) {
                            val splitSpan = newSpan(lastSpan)
                            splitSpan?.let {
                                setSpan(splitSpan, sEnd, preSpanEnd)
                            }
                        }
                    } else {
                        var mergeEnd = max(sEnd, preSpanEnd)
                        if (preSpanStart < mergeEnd) {
                            setSpan(lastSpan, preSpanStart, mergeEnd)
                        }
                    }
                } else {
                    if (preSpanStart < beforeSelectionStart) {
                        setSpan(lastSpan, preSpanStart, beforeSelectionStart)
                    }
                    if (sEnd < preSpanEnd) {
                        val splitSpan = newSpan(lastSpan)
                        splitSpan?.let {
                            setSpan(splitSpan, sEnd, preSpanEnd)
                        }
                    }
                }
            } else {
                if (isChecked && newSpan != null) {
                    setSpan(newSpan, beforeSelectionStart, sEnd)
                }
            }
        }
    }

    protected open fun handleAbsButtonClick(start: Int, end: Int) {
        val editable = mEditText.editableText
        if (start < end) {
            val targetSpans = editable.getSpans(start, end, targetClass())
            val newSpan = newSpan()
            if (targetSpans.isNotEmpty()) {
                var hasSet = false
                for (tar in targetSpans) {
                    val curStart = editable.getSpanStart(tar)
                    val curEnd = editable.getSpanEnd(tar)
                    editable.removeSpan(tar)
                    splitSpan(tar, curStart, curEnd, start, end)
                    if (newSpan != null && !hasSet) {
                        if (checkFeatureEqual(tar, newSpan)) {
                            setSpan(tar, min(curStart, start), max(curEnd, end))
                        } else {
                            setSpan(newSpan, start, end)
                        }
                        hasSet = true
                    }
                }
            } else {
                if (isChecked && newSpan != null) {
                    setSpan(newSpan, start, end)
                }
            }
            mergeSameStyle(start, end)
        }
    }

    override fun handleInputNewLine(
        editable: Editable,
        beforeSelectionStart: Int,
        epStart: Int,
        epEnd: Int
    ) {
        /*
        */
//        val lastPStart: Int = Util.getParagraphStart(mEditText, beforeSelectionStart)
//        var lastPEnd: Int = Util.getParagraphEnd(editable, beforeSelectionStart)
//        if (lastPEnd <= lastPStart) return
        val newLineSpans = editable.getSpans(epStart, epEnd, targetClass())
        if (newLineSpans.isEmpty()) return
//        Util.log("pre line: " + lastPStart + " - " + lastPEnd + " cur line: " + mEditText.selectionStart + " - " + mEditText.selectionEnd)
        val newlineSpanStart = editable.getSpanStart(newLineSpans[newLineSpans.size - 1])
        val newlineSpanEnd = editable.getSpanEnd(newLineSpans[newLineSpans.size - 1])
        if (newlineSpanStart < epStart) {
            removeSpans(editable, newLineSpans)
            if (newlineSpanStart <= beforeSelectionStart) {
                setSpan(newLineSpans[newLineSpans.size - 1], newlineSpanStart, beforeSelectionStart)
            }
            val newSpan = newSpan(newLineSpans[newLineSpans.size - 1])
            newSpan?.let {
                if (epStart <= newlineSpanEnd) {
                    setSpan(newSpan, epStart, newlineSpanEnd)
                }
            }
        }
    }

    protected open fun mergeSameStyle(start: Int, end: Int) {
        val editable = mEditText.editableText
        val targetSpans =
            editable.getSpans(max(0, start - 1), min(end + 1, mEditText.length()), targetClass())
        if (targetSpans.size < 2) return
        Arrays.sort(targetSpans) { o1: E, o2: E ->
            editable.getSpanStart(o1) - editable.getSpanStart(o2)
        }
        var i = 0
        var tStart = editable.getSpanStart(targetSpans[0])
        var tEnd = editable.getSpanEnd(targetSpans[0])
        var tarSpan: ISpan? = null
        while (i < (targetSpans.size - 1)) {
            val curEnd = editable.getSpanEnd(targetSpans[i])
            val nextStart = editable.getSpanStart(targetSpans[i + 1])
            val nextEnd = editable.getSpanEnd(targetSpans[i + 1])
            if (checkFeatureEqual(targetSpans[i], targetSpans[i + 1])) {
                if (curEnd >= nextStart) {
                    tStart = min(tStart, nextStart)
                    tEnd = max(tEnd, nextEnd)
                    editable.removeSpan(targetSpans[i])
                    if (i == targetSpans.size - 2) {
                        editable.removeSpan(targetSpans[i + 1])
                    }
                    tarSpan = targetSpans[i + 1]
                } else if (curEnd < nextStart) {
                    break
                }
            } else {
                tStart = nextStart
                tEnd = nextEnd
            }
            i++
        }
        if (tarSpan != null && tStart < tEnd) {
            setSpan(tarSpan, tStart, tEnd)
        }
    }

    private fun splitSpan(tar: E, curStart: Int, curEnd: Int, start: Int, end: Int) {
        if (curStart < start) {
            setSpan(tar, curStart, start)
        }
        if (end < curEnd) {
            setSpan(newSpan(tar)!!, end, curEnd)
        }
    }

    open fun checkFeatureEqual(span1: ISpan?, span2: ISpan?): Boolean {
        return isChecked
    }

    private fun handleDeleteAbsStyle() {
        val targetSpans = mEditText.editableText.getSpans(
            mEditText.selectionStart,
            mEditText.selectionEnd,
            targetClass()
        )
        var hasDelete = false
        if (targetSpans.isNotEmpty()) {
            targetSpans.forEach {
                if (mEditText.editableText.getSpanStart(it) == mEditText.editableText.getSpanEnd(it)) {
                    hasDelete = true
                    mEditText.editableText.removeSpan(it)
                }
            }
        }
    }

    override fun newSpan(inheritSpan: ISpan?): ISpan? {
        return if (isChecked || inheritSpan != null) targetClass().newInstance() else null
    }

    override fun setSpan(span: ISpan, start: Int, end: Int) {
        mEditText.editableText.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        mEditText.isChange = true
        mEditText.refreshRange(start, end)
    }
}
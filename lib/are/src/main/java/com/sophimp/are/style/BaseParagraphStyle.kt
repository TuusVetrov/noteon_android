package com.sophimp.are.style

import android.text.Editable
import android.text.Spanned
import android.text.TextUtils
import com.sophimp.are.Constants
import com.sophimp.are.RichEditText
import com.sophimp.are.spans.ISpan
import com.sophimp.are.spans.IndentSpan
import com.sophimp.are.spans.LineSpaceSpan
import com.sophimp.are.utils.Util
import java.util.*
import kotlin.math.max
import kotlin.math.min

abstract class BaseParagraphStyle<T : ISpan>(editText: RichEditText) : BaseStyle<T>(editText) {

    override fun itemClickOnNonEmptyParagraph(curPStart: Int, curPEnd: Int): Int {

        removeMutexSpans(curPStart, curPEnd)

        val targets = mEditText.editableText.getSpans(curPStart, curPEnd, targetClass())
        Arrays.sort(targets) { o1: T, o2: T ->
            mEditText.editableText.getSpanStart(o1) - mEditText.editableText.getSpanStart(o2)
        }
        updateSpan(targets, curPStart, curPEnd)
        if (targetClass() == LineSpaceSpan::class.java || targetClass() == IndentSpan::class.java) {
            mEditText.refreshByInsert(curPStart)
        }
//        else {
//            mEditText.refresh(curPStart)
//        }
        return 0
    }
    open fun <T : ISpan> updateSpan(spans: Array<T>, start: Int, end: Int) {
        if (spans.isNotEmpty()) {
            removeSpans(mEditText.editableText, spans)
            setSpan(spans[0], start, end)
        } else {
            val ns = newSpan(null)
            if (ns != null) {
                setSpan(ns, start, end)
            }
        }
    }
    open fun removeMutexSpans(curPStart: Int, curPEnd: Int) {}

    override fun handleMultiParagraphInput(
        editable: Editable,
        changedText: String?,
        beforeSelectionStart: Int,
        afterSelectionEnd: Int,
        epStart: Int,
        epEnd: Int
    ) {
        val effectFirstPEnd: Int = Util.getParagraphEnd(editable, beforeSelectionStart)
        val firstTargetParagraphSpans: Array<T> = editable.getSpans(epStart, effectFirstPEnd, targetClass())
//        val firstPLeadingSpans: Array<IndentSpan> = editable.getSpans(effectPStart, effectFirstPEnd, IndentSpan::class.java)
        if (firstTargetParagraphSpans.isEmpty()) return
        val allTargetParagraphSpans: Array<T> = editable.getSpans(epStart, epEnd, targetClass())
//        val allPLeadSpans: Array<IndentSpan> = editable.getSpans(effectPStart, effectPEnd, IndentSpan::class.java)
        removeSpans(editable, allTargetParagraphSpans)
//        removeSpans(editable, allPLeadSpans)
        logAllSpans(editable, "Предварительная обработка многострочного ввода", 0, editable.length)
        handleCommonInput(editable, epStart, epEnd, firstTargetParagraphSpans)
    }

    private fun handleCommonInput(
        editable: Editable,
        effectPStart: Int,
        effectPEnd: Int,
        firstPListSpans: Array<T>
    ) {
        var index = effectPStart
        var off = 0
        while (index < effectPEnd) {
            off = 0
            var pEnd: Int = Util.getParagraphEnd(editable, index)
            if (pEnd < 0) {
                pEnd = effectPEnd
            }
            if (index < pEnd) {
                val nSpan = newSpan()
                if (firstPListSpans.isNotEmpty() && nSpan != null) {
                    setSpan(nSpan, index, pEnd)
                }
            }
            index = pEnd + 1 + off
        }
    }

    override fun handleSingleParagraphInput(
        editable: Editable,
        changedText: String?,
        beforeSelectionStart: Int,
        afterSelectionEnd: Int,
        epStart: Int,
        epEnd: Int
    ) {
        if (epStart < epEnd) {
            val base = editable.getSpans(epStart, epEnd, targetClass())
            if (base.isNotEmpty()) {
                removeSpans(editable, base)
                setSpan(base[0], epStart, epEnd)
            }
        }
    }

    override fun handleInputNewLine(
        editable: Editable,
        beforeSelectionStart: Int,
        epStart: Int,
        epEnd: Int
    ) {
        val lastPStart: Int = Util.getParagraphStart(mEditText, beforeSelectionStart)
        var lastPEnd: Int = Util.getParagraphEnd(editable, beforeSelectionStart)
        if (lastPEnd < lastPStart) lastPEnd = lastPStart
        val preParagraphSpans = editable.getSpans(lastPStart, lastPEnd, targetClass())
        if (preParagraphSpans.isEmpty()) return
        Util.log("pre line: " + lastPStart + " - " + lastPEnd + " cur line: " + mEditText.selectionStart + " - " + mEditText.selectionEnd)
        removeSpans(editable, preParagraphSpans)
        removeSpans(editable, editable.getSpans(epStart, epEnd, targetClass()))

        val lastContent = editable.subSequence(lastPStart, lastPEnd).toString()
        if (TextUtils.isEmpty(lastContent) || lastContent.length == 1 && lastContent[0].toInt() == Constants.ZERO_WIDTH_SPACE_INT) {
            editable.delete(max(0, mEditText.selectionStart - 1), mEditText.selectionStart)
            handleNewLineWithAboveLineSpan(null, epStart, epStart + 1)
        } else {
            setSpan(preParagraphSpans[0], lastPStart, lastPEnd)
            val nSpan: ISpan? = newSpan(preParagraphSpans[0])
            if (nSpan != null) {
                val curStart = epStart
                if (curStart >= editable.length || editable[curStart].toInt() != Constants.ZERO_WIDTH_SPACE_INT) {
                    editable.insert(curStart, Constants.ZERO_WIDTH_SPACE_STR)
                }
                setSpan(nSpan, curStart, min(curStart + 1, editable.length))
            }
            handleNewLineWithAboveLineSpan(preParagraphSpans[0], epStart, epStart + 1)
        }
    }

    protected open fun handleNewLineWithAboveLineSpan(preSpan: T?, start: Int, end: Int) {}

    override fun handleDeleteEvent(editable: Editable, epStart: Int, epEnd: Int) {

//        val curPStart: Int = Util.getParagraphStart(mEditText, mEditText.selectionStart)
//        val curPEnd: Int = Util.getParagraphEnd(editable, mEditText.selectionStart)
        val curTargetSpans: Array<T> = editable.getSpans(epStart, epEnd, targetClass())
        Arrays.sort(curTargetSpans) { o1: T, o2: T ->
            editable.getSpanStart(o1) - editable.getSpanStart(o2)
        }
        if (curTargetSpans.isEmpty()) return
        removeMutexSpans(epStart, epEnd)
        removeSpans(editable, curTargetSpans)
        if (epStart < epEnd) {
            setSpan(curTargetSpans[0], epStart, epEnd)
        }
    }

    override fun setSpan(span: ISpan, start: Int, end: Int) {
        if (start >= 0 && end <= mEditText.length()) {
            try {
                mEditText.editableText.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                mEditText.refreshRange(start, end)
                mEditText.isChange = true
            } catch (e: Exception) {

            }

        }
    }

}
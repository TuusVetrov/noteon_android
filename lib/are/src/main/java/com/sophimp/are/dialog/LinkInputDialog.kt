package com.sophimp.are.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import androidx.annotation.RequiresApi
import com.sophimp.are.R
import com.sophimp.are.databinding.DialogLinkInputBinding

class LinkInputDialog(context: Context) : Dialog(context, androidx.appcompat.R.style.ThemeOverlay_AppCompat_Dialog_Alert) {

    private lateinit var binding: DialogLinkInputBinding
    private var linkLister: OnInertLinkListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogLinkInputBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCanceledOnTouchOutside(false)
        setUpView()
        setOnKeyListener(object : DialogInterface.OnKeyListener {
            override fun onKey(dialog: DialogInterface?, keyCode: Int, event: KeyEvent?): Boolean {
                if (keyCode == KeyEvent.KEYCODE_BACK && event?.action == KeyEvent.ACTION_UP) {
                    dismiss()
                    return true
                }
                return false
            }
        })
        addListener()
    }

    private fun setUpView() {
        updateConfirmBtnState()
    }

    private fun addListener() {
        binding.etLinkAddr.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                updateConfirmBtnState()
            }

            override fun afterTextChanged(s: Editable) {}
        })
        binding.iclTitle.tvBtnClose.setOnClickListener { dismiss() }
        binding.iclTitle.tvBtnConfirm.setOnClickListener {
            if (linkLister != null) {
                linkLister!!.onLinkInert(
                    binding.etLinkAddr.text.toString(),
                    binding.etLinkAddrName.text.toString()
                )
            }
            dismiss()
        }
    }

    fun setLinkLister(linkLister: OnInertLinkListener?): LinkInputDialog {
        this.linkLister = linkLister
        return this
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun updateConfirmBtnState() {
        if (TextUtils.isEmpty(binding.etLinkAddr.text)) {
            binding.iclTitle.tvBtnConfirm.setTextColor(context.getColor(R.color.outline))
            binding.iclTitle.tvBtnConfirm.isEnabled = false
        } else {
            binding.iclTitle.tvBtnConfirm.setTextColor(context.getColor(R.color.primary))
            binding.iclTitle.tvBtnConfirm.isEnabled = true
        }
    }

    interface OnInertLinkListener {
        fun onLinkInert(linkAddr: String, linkName: String)
    }
}
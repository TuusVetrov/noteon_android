package ru.noteon.presentation.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ru.noteon.R
import ru.noteon.core.utils.extensions.hiltMainNavGraphViewModels
import ru.noteon.databinding.ItemChooseCardBinding
import ru.noteon.domain.model.FolderModel

class ChooseFolderDialogFragment(
    listener: FolderDialogListener,
    folders: List<FolderModel>
): BottomSheetDialogFragment() {
    lateinit var binding: ItemChooseCardBinding
    private val _folders = folders
    private var bottomSheetListener: FolderDialogListener ?= null

    init {
        this.bottomSheetListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setDimAmount(0.4f)

            setOnShowListener {
                val bottomSheet = findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
                bottomSheet.setBackgroundResource(android.R.color.transparent)
                addCardsToView(bottomSheet)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = ItemChooseCardBinding.bind(inflater.inflate(R.layout.item_choose_card, container))
        return binding.root

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        /** attach listener from parent fragment */
        try {
            bottomSheetListener = context as FolderDialogListener?
        }
        catch (e: ClassCastException){
        }
    }

    private fun addCardsToView(bottomSheet: FrameLayout) {
        val mInflater = requireContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rootView = bottomSheet.getChildAt(0) as ConstraintLayout
        val injectedView = rootView.getChildAt(1) as LinearLayout

        for (i in 1.._folders.size) {

            val rootView = mInflater.inflate(R.layout.folder_item_choosen, null) as ConstraintLayout

            rootView.setOnClickListener {
                bottomSheetListener?.chooseFolderClick(_folders[i-1])
            }


            /** Set view */
            val cardTextView = rootView.getChildAt(1) as TextView

            cardTextView.text = _folders[i-1].folderName
            /** Add to View container */
            injectedView.addView(rootView, injectedView.childCount)
        }
    }

    interface FolderDialogListener {
        fun chooseFolderClick(folder: FolderModel)
    }
}

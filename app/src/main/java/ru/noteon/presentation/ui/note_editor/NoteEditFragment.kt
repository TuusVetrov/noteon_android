package ru.noteon.presentation.ui.note_editor

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.internal.ViewUtils.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.noteon.R
import ru.noteon.core.utils.extensions.toStringOrEmpty
import ru.noteon.databinding.FragmentNoteEditBinding
import ru.noteon.presentation.ui.sign_up.SignUpState
import javax.inject.Inject

@AndroidEntryPoint
class NoteEditFragment : Fragment()
{
    private val args: NoteEditFragmentArgs by navArgs()

    private lateinit var binding: FragmentNoteEditBinding

    @Inject
    lateinit var viewModelAssistedFactory: NoteEditViewModel.Factory

    private var isNoteLoaded = false

    private val noteEditViewModel: NoteEditViewModel by viewModels {
        Log.d("NoteEditViewModel", args.noteId ?: "pizods")
        args.noteId?.let { noteId ->
            NoteEditViewModel.provideFactory(viewModelAssistedFactory, noteId)
        } ?: throw IllegalStateException("'noteId' shouldn't be null")
    }

    private var pinMenuItem: MenuItem? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNoteEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTopAppbar()
        initElements()
        observeState()
    }

    private fun initElements() {
        with(binding) {
            noteContentLayout.run {
                fieldTitle.addTextChangedListener { noteEditViewModel.setTitle(it.toStringOrEmpty()) }
                fieldNote.addTextChangedListener { noteEditViewModel.setBody(it.toStringOrEmpty()) }
            }
        }
    }

    private fun noteEditStateHandler(state: NoteEditState) {
        val title = state.title
        val body = state.body

        if (title != null && body != null && !isNoteLoaded) {
            isNoteLoaded = true
            binding.fieldTitle.setText(title)
            binding.fieldNote.setText(body)
        }

        if (state.finished) {
            findNavController().navigateUp()
        }

        val errorMessage = state.error
        if (errorMessage != null) {
           // toast("Error: $errorMessage")
        }
        updatePinnedIcon(state.isPinned)
    }

    private fun observeState() {
        noteEditViewModel.uiState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state -> noteEditStateHandler(state) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setupTopAppbar() {
        binding.topAppBar.setNavigationOnClickListener {

        }

        val doneButton =  binding.topAppBar.menu.findItem(R.id.action_done)

        binding.fieldNote.onFocusChangeListener = View.OnFocusChangeListener { view, b ->
            doneButton.isVisible = b
        }

        binding.fieldTitle.onFocusChangeListener = View.OnFocusChangeListener { view, b ->
            doneButton.isVisible = b
        }

        pinMenuItem = binding.topAppBar.menu.findItem(R.id.action_pin)
        binding.topAppBar.setOnMenuItemClickListener {menuItem ->
            when (menuItem.itemId) {
                R.id.action_delete -> {
                    confirmNoteDeletion()
                    true
                }
                R.id.action_pin -> {
                    noteEditViewModel.togglePin()
                    true
                }
                R.id.action_done -> {
                    noteEditViewModel.save()
                    clearFocus()
                    true
                }
                else -> false
            }
        }
    }

    private fun clearFocus() {
        hideKeyboard()
        binding.fieldTitle.clearFocus()
        binding.fieldNote.clearFocus()
    }

    private fun updatePinnedIcon(isPinned: Boolean) {
        pinMenuItem?.run {
            val icon = if (isPinned) R.drawable.ic_pined_24 else R.drawable.ic_unpined_24
            setIcon(icon)
        }
    }

    private fun confirmNoteDeletion() {
        val dialog: AlertDialog =
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.alert_delete_title)
                .setMessage(R.string.alert_delete_message)
                .setPositiveButton(R.string.alert_positive) { dialog, id ->
                    noteEditViewModel.delete()
                }
                .setNegativeButton(R.string.alert_negative) { dialog, id ->
                    dialog.dismiss()
                }
                .setCancelable(true)
                .create()
        dialog.show()
    }


    private fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
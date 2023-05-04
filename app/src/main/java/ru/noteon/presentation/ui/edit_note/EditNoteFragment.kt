package ru.noteon.presentation.ui.edit_note

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.noteon.R
import ru.noteon.core.utils.extensions.snackBar
import ru.noteon.core.utils.extensions.toStringOrEmpty
import ru.noteon.databinding.FragmentEditNoteBinding
import javax.inject.Inject

@AndroidEntryPoint
class EditNoteFragment : Fragment() {
    private lateinit var binding: FragmentEditNoteBinding

    private val args: EditNoteFragmentArgs by navArgs()

    @Inject
    lateinit var viewModelAssistedFactory: EditNoteViewModel.Factory

    private var isNoteLoaded = false

    private var pinMenuItem: MenuItem? = null

    private val viewModel: EditNoteViewModel by viewModels {
        args.noteId?.let { noteId ->
            EditNoteViewModel.provideFactory(viewModelAssistedFactory, noteId)
        } ?: throw IllegalStateException("'noteId' shouldn't be null")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditNoteBinding.inflate(inflater, container, false)
        binding.bottomToolbar.initDefaultToolItem(binding.noteBodyEditor)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initElements()
        observeState()
        setupTopAppbar()
    }

    private fun initElements() {
        binding.run {
            noteTitleEditor.addTextChangedListener { viewModel.setTitle(it.toStringOrEmpty()) }
        }
    }

    private fun editNoteStateHandler(state: EditNoteState) {
        val title = state.title
        val note = state.body

        if (title != null && note != null && !isNoteLoaded) {
            isNoteLoaded = true
            binding.noteTitleEditor.setText(title)
            binding.noteBodyEditor.fromHtml(note)
        }


        val errorMessage = state.error
        if (errorMessage != null) {
            val message = getString(R.string.message_note_error) + "\n" + errorMessage
            snackBar(message)
        }

        updatePinnedIcon(state.isPinned)
    }

    private fun setupTopAppbar() {
        with(binding) {
            topAppBar.setNavigationOnClickListener {
                viewModel.setBody(binding.noteBodyEditor.toHtml())
                viewModel.save()
                findNavController().navigateUp()
            }

            pinMenuItem = topAppBar.menu.findItem(R.id.action_pin)

            topAppBar.setOnMenuItemClickListener {menuItem ->
                when (menuItem.itemId) {
                    R.id.action_delete -> {
                        viewModel.delete()
                        findNavController().navigateUp()
                        true
                    }
                    R.id.action_pin -> {
                        viewModel.togglePin()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun observeState() {
        viewModel.uiState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state -> editNoteStateHandler(state) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun updatePinnedIcon(isPinned: Boolean) {
        pinMenuItem?.run {
            val icon = if (isPinned) R.drawable.ic_pined_24 else R.drawable.ic_unpined_24
            setIcon(icon)
        }
    }
}
package ru.noteon.presentation.ui.create_note

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
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
import ru.noteon.core.utils.extensions.hiltMainNavGraphViewModels
import ru.noteon.core.utils.extensions.snackBar
import ru.noteon.core.utils.extensions.toStringOrEmpty
import ru.noteon.databinding.FragmentCreateNoteBinding
import ru.noteon.presentation.ui.list_notes.ListNotesFragmentArgs
import ru.noteon.presentation.ui.list_notes.NotesViewModel
import javax.inject.Inject

@AndroidEntryPoint
class CreateNoteFragment : Fragment() {
    private lateinit var binding: FragmentCreateNoteBinding

    private val args: CreateNoteFragmentArgs by navArgs()

    private val viewModel: CreateNoteViewModel by hiltMainNavGraphViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        args.folderId?.let { folderId ->
            viewModel.setFolder(folderId)
        } ?: throw IllegalStateException("'folderId' shouldn't be null")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateNoteBinding.inflate(inflater, container, false)
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

    private fun createNoteStateHandler(state: CreateNoteState) {
        val errorMessage = state.error
        if (errorMessage != null) {
            val message = getString(R.string.message_note_error) + "\n" + errorMessage
            snackBar(message)
        }
    }

    private fun setupTopAppbar() {
        binding.topAppBar.setNavigationOnClickListener {
            viewModel.setBody(binding.noteBodyEditor.toHtml())
            viewModel.add()
            findNavController().navigateUp()
        }
    }

    private fun observeState() {
        viewModel.uiState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state -> createNoteStateHandler(state) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroyView() {
        viewModel.resetState()
        super.onDestroyView()
    }
}
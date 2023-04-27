package ru.noteon.presentation.ui.note_editor

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ru.noteon.databinding.FragmentNoteEditBinding
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
        args.noteId?.let { noteId ->
            NoteEditViewModel.provideFactory(viewModelAssistedFactory, noteId)
        } ?: throw IllegalStateException("'noteId' shouldn't be null")
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNoteEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}
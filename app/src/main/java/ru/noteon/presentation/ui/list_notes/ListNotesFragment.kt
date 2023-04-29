package ru.noteon.presentation.ui.list_notes

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.noteon.R
import ru.noteon.core.utils.extensions.hiltMainNavGraphViewModels
import ru.noteon.databinding.FragmentListNotesBinding
import ru.noteon.domain.model.NoteModel
import ru.noteon.presentation.ui.list_notes.adapter.NotesListAdapter
import ru.noteon.presentation.ui.list_notes.adapter.SwipeToDeleteCallback


const val CREATE_NOTE_TAG = "new_note"
@AndroidEntryPoint
class ListNotesFragment : Fragment() {
    private lateinit var binding: FragmentListNotesBinding
    private val notesViewModel: NotesViewModel by hiltMainNavGraphViewModels()

    private val notesAdapter by lazy { NotesListAdapter(::onPinClicked, ::onNoteClicked) }
    private val searchAdapter by lazy { NotesListAdapter(::onPinClicked, ::onNoteClicked) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initElements()
        observeState()
    }

    private fun initElements() {
        with(binding) {
            rvNotes.apply {
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                adapter = notesAdapter
            }

            searchView
                .editText
                .setOnEditorActionListener { v, actionId, event ->
                    notesViewModel.searchNotes(searchView.text.toString())
                    false
                }

            searchView.setOnClickListener { notesViewModel.restoreSearchNotes() }

            rvSearchNotes.apply {
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                adapter = searchAdapter
            }

            val rightSwipeHandler = object : SwipeToDeleteCallback(
                requireContext(),
                ResourcesCompat.getColor(resources, R.color.noteon_error, context?.theme),
                ResourcesCompat.getColor(resources, R.color.noteon_on_error, context?.theme)
            ) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val adapter = rvNotes.adapter as NotesListAdapter

                    val noteId = adapter.getNoteID(viewHolder.adapterPosition)
                    notesViewModel.delete(noteId)
                }
            }

            val rightItemTouchHelper = ItemTouchHelper(rightSwipeHandler)
            rightItemTouchHelper.attachToRecyclerView(rvNotes)

            fabNewNote.setOnClickListener {
                findNavController().navigate(
                    ListNotesFragmentDirections.actionListNotesFragmentToNoteEditFragment(CREATE_NOTE_TAG)
                )
            }

           swipeToRefreshNotes.setOnRefreshListener {
               notesViewModel.syncNotes()
               swipeToRefreshNotes.isRefreshing = false
            }
        }
    }

    private fun observeState() {
        notesViewModel.uiState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state -> notesStateHandler(state) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun notesStateHandler(state: NotesState) {
        binding.swipeToRefreshNotes.isEnabled = state.isConnectivityAvailable == true

        if (state.isUserLoggedIn == false) {
            logout()
        }

        notesAdapter.submitList(state.notes)
        searchAdapter.submitList(state.searchNotes)

        val errorMessage = state.error
        if (errorMessage != null) {
            // TODO: eroor handling
        }

        val isConnectivityAvailable = state.isConnectivityAvailable
        if (isConnectivityAvailable != null) {
            if (isConnectivityAvailable) {
                onConnectivityAvailable()
            } else {
                onConnectivityUnavailable()
            }
        }
    }

    @SuppressLint("ResourceType")
    private fun onConnectivityUnavailable() {
        val backgroundColor =
            ResourcesCompat.getColor(resources, R.color.noteon_error, context?.theme)

        with(binding) {
           textNetworkStatus.apply {
               text = getString(R.string.text_no_connectivity)
               setBackgroundColor(backgroundColor)
           }.also { it.visibility = View.VISIBLE }
        }
    }

    @SuppressLint("ResourceType")
    private fun onConnectivityAvailable() {
        val backgroundColor =
            ResourcesCompat.getColor(resources, R.color.noteon_primary, context?.theme)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                if (shouldSyncNotes()) {
                    notesViewModel.syncNotes()
                }

                with(binding) {
                    textNetworkStatus.apply {
                        text = getString(R.string.text_connectivity)
                        setBackgroundColor(backgroundColor)
                    }.also {
                        it.animate()
                            .alpha(1f)
                            .setStartDelay(ANIMATION_DURATION)
                            .setDuration(ANIMATION_DURATION)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    it.visibility = View.GONE
                                }
                            })
                    }
                }
            }
        }
    }

    private fun logout() {

    }

    private fun onNoteClicked(note: NoteModel) {
        findNavController().navigate(
            ListNotesFragmentDirections.actionListNotesFragmentToNoteEditFragment(note.id)
        )
    }

    private fun onPinClicked(note: NoteModel) {
        // TODO: this shit does not work, check view-model function
        Log.d("onPinClicked", note.id)
        val pinState = note.isPinned.not()
        notesViewModel.togglePin(note.id, pinState)
    }

    private fun shouldSyncNotes() = notesViewModel.uiState.value.error != null

    companion object {
        const val ANIMATION_DURATION = 2000L
    }
}
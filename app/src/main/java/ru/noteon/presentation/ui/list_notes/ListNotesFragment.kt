package ru.noteon.presentation.ui.list_notes

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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
import ru.noteon.presentation.ui.list_notes.adapters.NotesListAdapter
import ru.noteon.presentation.ui.list_notes.adapters.SwipeToDeleteCallback
import javax.inject.Inject

@AndroidEntryPoint
class ListNotesFragment : Fragment() {
    private lateinit var binding: FragmentListNotesBinding

    private val args: ListNotesFragmentArgs by navArgs()

    @Inject
    lateinit var viewModelAssistedFactory: NotesViewModel.Factory

    private val viewModel: NotesViewModel by viewModels {
        args.folderId?.let { folderId ->
            NotesViewModel.provideFactory(viewModelAssistedFactory, folderId)
        } ?: throw IllegalStateException("'folderId' shouldn't be null")
    }

    private val notesAdapter by lazy { NotesListAdapter(::onPinClicked, ::onNoteClicked) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListNotesBinding.inflate(inflater, container, false)
        binding.mainToolbar.title = args.folderName ?: ""
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initElements()
        observeState()
        setupToolbar()
    }

    private fun initElements() {
        with(binding) {
            rvNotes.apply {
                layoutManager = LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                    false
                )
                adapter = notesAdapter
            }

            val rightSwipeHandler = object : SwipeToDeleteCallback(
                requireContext(),
                ResourcesCompat.getColor(resources, R.color.noteon_error, context?.theme),
                ResourcesCompat.getColor(resources, R.color.noteon_on_error, context?.theme)
            ) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val adapter = rvNotes.adapter as NotesListAdapter

                    val noteId = adapter.getNoteID(viewHolder.adapterPosition)
                    viewModel.delete(noteId)
                }
            }

            val rightItemTouchHelper = ItemTouchHelper(rightSwipeHandler)
            rightItemTouchHelper.attachToRecyclerView(rvNotes)

            fabNewNote.setOnClickListener {
                args.folderId?.let { folderId ->
                    Log.d("findNavController", folderId)
                    findNavController().navigate(
                        ListNotesFragmentDirections.actionListNotesFragmentToCreateNoteFragment(folderId)
                    )
                } ?: throw IllegalStateException("'folderId' shouldn't be null")
            }

           swipeToRefreshNotes.setOnRefreshListener {
               viewModel.syncNotes()
               swipeToRefreshNotes.isRefreshing = false
            }
        }
    }

    private fun setupToolbar() {
        with(binding) {
            mainToolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            val item = mainToolbar.menu.findItem(R.id.search)
            val searchView = item.actionView as SearchView

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let {
                        searchNotes(it)
                    }
                    return true
                }
            })
        }
    }

    private fun searchNotes(query:String) {
        val searchQuery = "%$query%"

        viewModel.searchNote(searchQuery).observe(this) { list ->
            list.let {
                notesAdapter.submitList(it)
            }
        }
    }

    private fun observeState() {
        viewModel.uiState
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
                    viewModel.syncNotes()
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
            ListNotesFragmentDirections.actionListNotesFragmentToEditNoteFragment(note.id)
        )
    }

    private fun onPinClicked(note: NoteModel) {
        // TODO: this shit does not work, check view-model function
        Log.d("onPinClicked", note.id)
        val pinState = note.isPinned.not()
        viewModel.togglePin(note.id, pinState)
    }

    private fun shouldSyncNotes() = viewModel.uiState.value.error != null

    companion object {
        const val ANIMATION_DURATION = 2000L
    }
}
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.noteon.R
import ru.noteon.core.utils.extensions.hiltMainNavGraphViewModels
import ru.noteon.databinding.FragmentListNotesBinding
import ru.noteon.domain.model.NoteModel
import ru.noteon.presentation.ui.list_notes.adapter.NotesListAdapter
import ru.noteon.presentation.ui.login.LoginViewModel

@AndroidEntryPoint
class ListNotesFragment : Fragment() {
    private lateinit var binding: FragmentListNotesBinding
    private val notesViewModel: NotesViewModel by hiltMainNavGraphViewModels()

    private val notesAdapter by lazy { NotesListAdapter(::onNoteClicked) }

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

        val errorMessage = state.error
        if (errorMessage != null) {

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
        with(binding) {
           textNetworkStatus.apply {
               text = getString(R.string.text_no_connectivity)
           }

            networkStatusLayout.apply {
                setBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.light_error,
                        requireActivity().theme
                    )
                )
            }.also { it.visibility = View.VISIBLE }
        }
    }

    @SuppressLint("ResourceType")
    private fun onConnectivityAvailable() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            if (shouldSyncNotes()) {
                notesViewModel.syncNotes()
            }

            with(binding) {
                textNetworkStatus.text = getString(R.string.text_connectivity)
                networkStatusLayout.apply {
                    setBackgroundColor(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.light_primary,
                            requireActivity().theme
                        )
                    )
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

    private fun logout() {

    }

    private fun onNoteClicked(note: NoteModel) {

    }

    private fun shouldSyncNotes() = notesViewModel.uiState.value.error != null

    companion object {
        const val ANIMATION_DURATION = 2000L
    }
}
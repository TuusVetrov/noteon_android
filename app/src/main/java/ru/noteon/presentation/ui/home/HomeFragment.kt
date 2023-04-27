package ru.noteon.presentation.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.noteon.R
import ru.noteon.core.utils.extensions.hiltMainNavGraphViewModels
import ru.noteon.databinding.ActivityMainBinding
import ru.noteon.databinding.FragmentHomeBinding
import ru.noteon.presentation.ui.list_notes.NotesViewModel

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by hiltMainNavGraphViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeState()
    }

    private fun observeState() {
        homeViewModel.uiState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state -> render(state) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun render(state: HomeState) {
        val isLoggedIn = state.isLoggedIn ?: return

        val destination = if (isLoggedIn) {
            R.id.action_homeFragment_to_loginFragment
        } else {
            R.id.action_homeFragment_to_listNotesFragment
        }
        findNavController().navigate(R.id.action_homeFragment_to_noteEditFragment)
    }
}
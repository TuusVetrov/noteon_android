package ru.noteon.presentation.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.noteon.R
import ru.noteon.core.utils.extensions.hiltMainNavGraphViewModels
import ru.noteon.core.utils.extensions.snackBar
import ru.noteon.databinding.FragmentSettingsBinding
import ru.noteon.presentation.ui.sign_up.SignUpState
import ru.noteon.presentation.ui.sign_up.SignUpViewModel

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding

    private val viewModel: SettingsViewModel by hiltMainNavGraphViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeState()
        initElements()
    }

    override fun onResume() {
        super.onResume()
        viewModel.observeUser()
    }

    private fun initElements() {
        binding.topAppBar.setNavigationOnClickListener { navigateBack() }
        binding.btnChangePassword.setOnClickListener { navigateToChangePasswordScreen() }
        binding.btnChangeAccountInfo.setOnClickListener { navigateToChangeUserDataScreen() }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            navigateToChangeLoginScreen()
        }
    }

    private fun navigateToChangePasswordScreen() {
        findNavController().navigate(R.id.action_settingsFragment_to_changePasswordFragment)
    }

    private fun navigateToChangeLoginScreen() {
        findNavController().navigate(R.id.action_settingsFragment_to_loginFragment)
    }

    private fun navigateToChangeUserDataScreen() {
        findNavController().navigate(R.id.action_settingsFragment_to_changeUserDataFragment)
    }

    private fun settingsStateHandler(state: SettingsState) {
        if (viewModel.uiState.value.isLoading) {
            binding.btnChangeAccountInfo.isEnabled = false
            binding.btnChangePassword.isEnabled = false
        } else {
            binding.userName.resetLoader()
            binding.userEmail.resetLoader()
            binding.userName.text = viewModel.uiState.value.username
            binding.userEmail.text = viewModel.uiState.value.email
            binding.btnChangeAccountInfo.isEnabled = true
            binding.btnChangePassword.isEnabled = true
        }

        val errorMessage = state.errorMessage
        if (errorMessage != null) {
            val message = getString(R.string.error_signup_title) + "\n" + errorMessage
            snackBar(message)
        }
    }

    private fun navigateBack() {
        findNavController().navigateUp()
    }

    private fun observeState() {
        viewModel.uiState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state -> settingsStateHandler(state) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }
}
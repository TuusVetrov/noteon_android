package ru.noteon.presentation.ui.change_password

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.noteon.R
import ru.noteon.core.utils.extensions.hiltMainNavGraphViewModels
import ru.noteon.core.utils.extensions.setError
import ru.noteon.core.utils.extensions.snackBar
import ru.noteon.core.utils.extensions.toStringOrEmpty
import ru.noteon.databinding.FragmentChangePasswordBinding

class ChangePasswordFragment : Fragment() {
    private lateinit var binding: FragmentChangePasswordBinding

    private val viewModel: ChangePasswordViewModel by hiltMainNavGraphViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initElements()
        observeState()
    }

    private fun initElements() {
        with(binding) {
            btnChangePassword.setOnClickListener {
                viewModel.updatePassword()
            }
            topAppBar.setNavigationOnClickListener { navigateBack() }

            newPassword.editText?.addTextChangedListener {
                viewModel.setPassword(it.toStringOrEmpty())
            }

            confirmNewPassword.editText?.addTextChangedListener {
                viewModel.setConfirmPassword(it.toStringOrEmpty())
            }
        }
    }

    private fun changePasswordStateHandler(state: ChangePasswordState) {
        with(binding) {
            newPassword.setError(state.isValidPassword == false) {
                getString(R.string.error_message_password_invalid)
            }
            confirmNewPassword.setError(state.isPasswordsAreSame == false) {
                getString(R.string.message_password_mismatched)
            }
        }

        if (state.isSuccessful == true) {
            navigateBack()
        }

        val errorMessage = state.errorMessage
        if (errorMessage != null) {
            val message = getString(R.string.error_signup_title) + "\n" + errorMessage
            snackBar(message)
        }
        viewModel.clearError()
    }

    private fun observeState() {
        viewModel.uiState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state -> changePasswordStateHandler(state) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun navigateBack() {
        findNavController().navigateUp()
    }
}
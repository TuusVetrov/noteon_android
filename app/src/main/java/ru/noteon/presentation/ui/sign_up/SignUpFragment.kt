package ru.noteon.presentation.ui.sign_up

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.noteon.R
import ru.noteon.core.utils.extensions.hiltMainNavGraphViewModels
import ru.noteon.core.utils.extensions.setError
import ru.noteon.core.utils.extensions.toStringOrEmpty
import ru.noteon.databinding.FragmentSignUpBinding
import ru.noteon.presentation.ui.login.LoginState

class SignUpFragment : Fragment() {
    private lateinit var binding: FragmentSignUpBinding

    private val signUpViewModel: SignUpViewModel by hiltMainNavGraphViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initElements()
        observeState()
    }

    private fun initElements() {
        with(binding) {
            createAccountButton.setOnClickListener { signUpViewModel.signUp() }
            topAppBar.setNavigationOnClickListener { navigateBack() }
            emailTextInputLayout.editText?.addTextChangedListener {
                signUpViewModel.setEmail(it.toStringOrEmpty())
            }
            passwordTextInputLayout.editText?.addTextChangedListener {
                signUpViewModel.setPassword(it.toStringOrEmpty())
            }
            confirmPasswordTextInputLayout.editText?.addTextChangedListener {
                signUpViewModel.setPassword(it.toStringOrEmpty())
            }
        }
    }

    private fun signUpStateHandler(state: SignUpState) {
        with(binding) {
            emailTextInputLayout.setError(state.isValidEmail == false) {
                getString(R.string.error_message_email_invalid)
            }
            passwordTextInputLayout.setError(state.isValidPassword == false) {
                getString(R.string.error_message_password_invalid)
            }
            confirmPasswordTextInputLayout.setError(state.isValidConfirmPassword == false) {
                getString(R.string.message_password_mismatched)
            }
        }

        if (state.isLoggedIn) {
            navigateToOtpScreen()
        }

        val errorMessage = state.errorMessage
        if (errorMessage != null) {
            val message = getString(R.string.error_loading_title) + "\n" + errorMessage
            view?.let {
                Snackbar.make(it, message, Snackbar.LENGTH_SHORT).apply {
                    setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.light_error))
                    animationMode = Snackbar.ANIMATION_MODE_FADE
                    show()
                }
            }
        }
        signUpViewModel.clearError()
    }

    private fun observeState() {
        signUpViewModel.uiState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state -> signUpStateHandler(state) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun navigateToOtpScreen() {

    }

    private fun navigateBack() {
        findNavController().navigateUp()
    }
}
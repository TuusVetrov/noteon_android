package ru.noteon.presentation.ui.login

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.noteon.R
import ru.noteon.core.utils.extensions.hiltMainNavGraphViewModels
import ru.noteon.core.utils.extensions.snackBar
import ru.noteon.databinding.FragmentLoginBinding

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private val loginViewModel: LoginViewModel by hiltMainNavGraphViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initElements()
        observeState()
    }

    private fun initElements() {
        binding.emailTextInput.addTextChangedListener {
            loginViewModel.setEmail(it.toString())
        }

        binding.passwordTextInput.addTextChangedListener {
            loginViewModel.setPassword(it.toString())
        }

        binding.continueButton.setOnClickListener {
            loginViewModel.loginUser()
        }

        binding.passwordTextInput.setOnEditorActionListener {  _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                event.action == KeyEvent.ACTION_DOWN &&
                event.keyCode == KeyEvent.KEYCODE_ENTER
            ){
                loginViewModel.loginUser()
                true
            } else {
                false
            }
        }

        binding.createNewAccountButton.setOnClickListener { navigateToSignUpScreen() }
    }

    private fun loginStateHandler(state: LoginState) {
        binding.emailTextInputLayout.error = getString(R.string.error_message_email_invalid)
        binding.emailTextInputLayout.isErrorEnabled = state.isValidEmail == false

        binding.passwordTextInputLayout.error = getString(R.string.error_message_password_invalid)
        binding.passwordTextInputLayout.isErrorEnabled = state.isValidPassword == false

        if (state.isLoggedIn) {
            navigateToNotesScreen()
        }

        val errorMessage = state.errorMessage
        if (errorMessage != null) {
            val message = getString(R.string.error_loading_title) + "\n" + errorMessage
            snackBar(message)
        }
        loginViewModel.clearError()
    }

    private fun observeState() {
        loginViewModel.uiState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state -> loginStateHandler(state) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun navigateToNotesScreen() {
        findNavController().navigate(R.id.action_loginFragment_to_foldersListFragment)
    }

    private fun navigateToSignUpScreen() {
        findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
    }
}
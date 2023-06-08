package ru.noteon.presentation.ui.change_user_data

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
import ru.noteon.databinding.FragmentChangeUserDataBinding
import ru.noteon.presentation.ui.change_password.ChangePasswordState
import ru.noteon.presentation.ui.settings.SettingsState

class  ChangeUserDataFragment : Fragment() {
    private lateinit var binding: FragmentChangeUserDataBinding
    private val viewModel: ChangeUserDataViewModel by hiltMainNavGraphViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangeUserDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initElements()
        observeState()
    }

    private fun initElements() {
        with(binding) {
            binding.createAccountButton.setOnClickListener {
                viewModel.updateUserData()
                navigateBack()
            }
            topAppBar.setNavigationOnClickListener { navigateBack() }

            usernameTextInputLayout.editText?.addTextChangedListener {
                viewModel.setUsername(it.toStringOrEmpty())
            }

            emailTextInputLayout.editText?.addTextChangedListener {
                viewModel.setEmail(it.toStringOrEmpty())
            }
        }
    }

    private fun changePasswordStateHandler(state: ChangeDataState) {
        with(binding) {
            usernameTextInputLayout.setError(state.isValidUsername == false) {
                "Имя пользователя должно быть от 2 до 128 символов"
            }
            emailTextInputLayout.setError(state.isValidEmail == false) {
                "Проверьте правильность email"
            }
        }

        if(state.isSuccessful == true) {
            navigateBack()
        }

        val errorMessage = state.errorMessage
        if (errorMessage != null) {
            val message = "Не удалось обновить данные\n$errorMessage"
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
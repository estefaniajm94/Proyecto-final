package com.estef.antiphishingcoach.presentation.auth

import android.os.Bundle
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.databinding.FragmentLoginBinding
import com.estef.antiphishingcoach.presentation.common.AndroidStringResolver
import com.estef.antiphishingcoach.presentation.common.BaseFragment
import com.estef.antiphishingcoach.presentation.common.appContainer
import com.estef.antiphishingcoach.presentation.common.collectOnStarted
import com.estef.antiphishingcoach.presentation.common.navigateAfterAuth
import com.estef.antiphishingcoach.presentation.common.renderAvatar
import com.estef.antiphishingcoach.presentation.common.viewModelFactory
import androidx.navigation.fragment.findNavController
import com.estef.antiphishingcoach.presentation.navigation.SharedContentViewModel

class LoginFragment : BaseFragment<FragmentLoginBinding>(
    R.layout.fragment_login,
    FragmentLoginBinding::bind
) {

    private val sharedContentViewModel: SharedContentViewModel by activityViewModels()
    private val viewModel: LoginViewModel by viewModels {
        val c = appContainer()
        viewModelFactory {
            LoginViewModel(
                loginLocalUserUseCase = c.loginLocalUserUseCase,
                findUserByEmailUseCase = c.findUserByEmailUseCase,
                stringResolver = AndroidStringResolver(requireContext().applicationContext)
            )
        }
    }

    private var hasNavigated = false

    override fun onBoundView(savedInstanceState: Bundle?) {
        binding.etEmail.doAfterTextChanged { editable ->
            viewModel.onEmailChanged(editable?.toString().orEmpty())
        }
        viewModel.onEmailChanged(binding.etEmail.text?.toString().orEmpty())
        binding.btnLogin.setOnClickListener {
            viewModel.login(
                email = binding.etEmail.text?.toString().orEmpty(),
                password = binding.etPassword.text?.toString().orEmpty()
            )
        }
        binding.btnOpenRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
        collectOnStarted(viewModel.uiState) { state ->
            binding.progressLogin.visibility =
                if (state.isLoading) android.view.View.VISIBLE else android.view.View.GONE
            binding.tilEmail.error = state.emailError
            binding.tilPassword.error = state.passwordError
            binding.tvLoginStatus.text = state.statusMessage.orEmpty()
            binding.ivLoginAvatar.renderAvatar(state.previewAvatarId)
            binding.tvLoginAvatarStatus.text = state.previewMessage.orEmpty()

            if (state.authenticatedUser != null && !hasNavigated) {
                hasNavigated = true
                navigateAfterAuth(sharedContentViewModel.pendingSharedInput.value != null)
            }
        }
    }
}

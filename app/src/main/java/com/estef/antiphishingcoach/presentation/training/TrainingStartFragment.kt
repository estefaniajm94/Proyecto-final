package com.estef.antiphishingcoach.presentation.training

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.databinding.FragmentTrainingStartBinding
import com.estef.antiphishingcoach.presentation.common.BaseFragment
import com.estef.antiphishingcoach.presentation.common.appContainer
import kotlinx.coroutines.launch

class TrainingStartFragment : BaseFragment<FragmentTrainingStartBinding>(
    R.layout.fragment_training_start,
    FragmentTrainingStartBinding::bind
) {
    private val viewModel: TrainingViewModel by activityViewModels {
        TrainingViewModelFactory(appContainer().getTrainingQuestionsUseCase)
    }

    override fun onBoundView(savedInstanceState: Bundle?) {
        viewModel.ensureLoaded()
        setupActions()
        observeUi()
    }

    private fun setupActions() {
        binding.btnStartQuiz.setOnClickListener {
            viewModel.startQuiz()
            findNavController().navigate(R.id.action_trainingStart_to_quiz)
        }
    }

    private fun observeUi() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.tvTrainingLoading.isVisible = state.isLoading
                    binding.tvTotalQuestions.text = getString(
                        R.string.training_total_questions,
                        state.totalQuestions
                    )
                    binding.btnStartQuiz.isEnabled = !state.isLoading && state.totalQuestions > 0
                }
            }
        }
    }
}

package com.estef.antiphishingcoach.presentation.training

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.databinding.FragmentQuizResultBinding
import com.estef.antiphishingcoach.presentation.common.BaseFragment
import com.estef.antiphishingcoach.presentation.common.appContainer
import kotlinx.coroutines.launch

class QuizResultFragment : BaseFragment<FragmentQuizResultBinding>(
    R.layout.fragment_quiz_result,
    FragmentQuizResultBinding::bind
) {
    private val viewModel: TrainingViewModel by activityViewModels {
        TrainingViewModelFactory(appContainer().getTrainingQuestionsUseCase)
    }

    override fun onBoundView(savedInstanceState: Bundle?) {
        binding.btnRestartQuiz.setOnClickListener {
            viewModel.restart()
            findNavController().popBackStack(R.id.trainingStartFragment, false)
        }
        observeUi()
    }

    private fun observeUi() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val total = state.totalQuestions
                    val score = state.score
                    val ratio = if (total == 0) 0 else (score * 100) / total
                    binding.tvResultScore.text = getString(
                        R.string.training_result_score,
                        score,
                        total
                    )
                    binding.tvResultRatio.text = getString(
                        R.string.training_result_ratio,
                        ratio
                    )
                }
            }
        }
    }
}

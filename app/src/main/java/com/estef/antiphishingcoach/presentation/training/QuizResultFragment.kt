package com.estef.antiphishingcoach.presentation.training

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.databinding.FragmentQuizResultBinding
import com.estef.antiphishingcoach.presentation.common.BaseFragment
import com.estef.antiphishingcoach.presentation.common.appContainer
import com.estef.antiphishingcoach.presentation.common.collectOnStarted
import com.estef.antiphishingcoach.presentation.common.viewModelFactory

class QuizResultFragment : BaseFragment<FragmentQuizResultBinding>(
    R.layout.fragment_quiz_result,
    FragmentQuizResultBinding::bind
) {
    private val viewModel: TrainingViewModel by activityViewModels {
        val c = appContainer()
        viewModelFactory {
            TrainingViewModel(
                getTrainingQuestionsUseCase = c.getTrainingQuestionsUseCase,
                saveLatestTrainingProgressUseCase = c.saveLatestTrainingProgressUseCase
            )
        }
    }

    override fun onBoundView(savedInstanceState: Bundle?) {
        setupBackNavigation(binding.btnBack)
        binding.btnRestartQuiz.setOnClickListener {
            viewModel.restart()
            findNavController().popBackStack(R.id.trainingStartFragment, false)
        }
        collectOnStarted(viewModel.uiState) { state ->
            val total = state.totalQuestions
            val score = state.score
            val ratio = if (total == 0) 0 else (score * 100) / total
            binding.tvResultLevel.text =
                getString(R.string.training_result_level, getString(state.selectedLevel.labelResId()))
            binding.tvResultScore.text = getString(R.string.training_result_score, score, total)
            binding.tvResultRatio.text = getString(R.string.training_result_ratio, ratio)
            binding.tvResultMessage.text = getString(state.selectedLevel.resultMessageResId())
        }
    }
}

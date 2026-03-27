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
import com.estef.antiphishingcoach.domain.model.TrainingLevel
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
        setupBackNavigation(binding.btnBack)
        viewModel.ensureLoaded()
        setupActions()
        observeUi()
    }

    private fun setupActions() {
        binding.toggleTrainingLevels.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val level = when (checkedId) {
                R.id.btnLevelBeginner -> TrainingLevel.BEGINNER
                R.id.btnLevelIntermediate -> TrainingLevel.INTERMEDIATE
                R.id.btnLevelAdvanced -> TrainingLevel.ADVANCED
                else -> return@addOnButtonCheckedListener
            }
            viewModel.selectLevel(level)
        }
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
                        state.selectedLevelQuestionCount
                    )
                    binding.tvSelectedLevelTitle.text =
                        getString(state.selectedLevel.labelResId())
                    binding.tvSelectedLevelDescription.text =
                        getString(state.selectedLevel.descriptionResId())
                    binding.tvSelectedLevelCount.text = getString(
                        R.string.training_level_question_count,
                        state.selectedLevelQuestionCount
                    )
                    binding.btnStartQuiz.isEnabled =
                        !state.isLoading && state.selectedLevelQuestionCount > 0

                    val targetButtonId = when (state.selectedLevel) {
                        TrainingLevel.BEGINNER -> R.id.btnLevelBeginner
                        TrainingLevel.INTERMEDIATE -> R.id.btnLevelIntermediate
                        TrainingLevel.ADVANCED -> R.id.btnLevelAdvanced
                    }
                    if (binding.toggleTrainingLevels.checkedButtonId != targetButtonId) {
                        binding.toggleTrainingLevels.check(targetButtonId)
                    }
                }
            }
        }
    }
}

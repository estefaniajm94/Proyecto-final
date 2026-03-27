package com.estef.antiphishingcoach.presentation.training

import android.os.Bundle
import android.widget.TextView
import androidx.core.content.ContextCompat
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
import com.google.android.material.card.MaterialCardView
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
        binding.cardLevelBeginner.setOnClickListener { viewModel.selectLevel(TrainingLevel.BEGINNER) }
        binding.cardLevelIntermediate.setOnClickListener { viewModel.selectLevel(TrainingLevel.INTERMEDIATE) }
        binding.cardLevelAdvanced.setOnClickListener { viewModel.selectLevel(TrainingLevel.ADVANCED) }
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
                    binding.tvLevelBeginnerCount.text = getString(
                        R.string.training_level_question_count_short,
                        state.availableQuestionCounts[TrainingLevel.BEGINNER] ?: 0
                    )
                    binding.tvLevelIntermediateCount.text = getString(
                        R.string.training_level_question_count_short,
                        state.availableQuestionCounts[TrainingLevel.INTERMEDIATE] ?: 0
                    )
                    binding.tvLevelAdvancedCount.text = getString(
                        R.string.training_level_question_count_short,
                        state.availableQuestionCounts[TrainingLevel.ADVANCED] ?: 0
                    )
                    binding.btnStartQuiz.isEnabled =
                        !state.isLoading && state.selectedLevelQuestionCount > 0

                    updateBubbleSelection(state.selectedLevel)
                }
            }
        }
    }

    private fun updateBubbleSelection(selectedLevel: TrainingLevel) {
        updateBubble(
            card = binding.cardLevelBeginner,
            titleView = binding.tvLevelBeginner,
            isSelected = selectedLevel == TrainingLevel.BEGINNER
        )
        updateBubble(
            card = binding.cardLevelIntermediate,
            titleView = binding.tvLevelIntermediate,
            isSelected = selectedLevel == TrainingLevel.INTERMEDIATE
        )
        updateBubble(
            card = binding.cardLevelAdvanced,
            titleView = binding.tvLevelAdvanced,
            isSelected = selectedLevel == TrainingLevel.ADVANCED
        )
    }

    private fun updateBubble(
        card: MaterialCardView,
        titleView: TextView,
        isSelected: Boolean
    ) {
        val strokeColor = ContextCompat.getColor(
            requireContext(),
            if (isSelected) R.color.brand_primary else R.color.card_outline_soft
        )
        val backgroundColor = ContextCompat.getColor(
            requireContext(),
            if (isSelected) R.color.brand_secondary_soft else R.color.brand_surface
        )
        val titleColor = ContextCompat.getColor(
            requireContext(),
            if (isSelected) R.color.brand_primary_dark else R.color.brand_on_surface
        )

        card.strokeColor = strokeColor
        card.strokeWidth = if (isSelected) 3 else 1
        card.setCardBackgroundColor(backgroundColor)
        titleView.setTextColor(titleColor)
    }
}

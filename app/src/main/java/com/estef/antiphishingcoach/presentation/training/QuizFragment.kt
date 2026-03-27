package com.estef.antiphishingcoach.presentation.training

import android.os.Bundle
import android.widget.RadioButton
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.databinding.FragmentQuizBinding
import com.estef.antiphishingcoach.presentation.common.BaseFragment
import com.estef.antiphishingcoach.presentation.common.appContainer
import com.estef.antiphishingcoach.presentation.common.showShortMessage
import kotlinx.coroutines.launch

class QuizFragment : BaseFragment<FragmentQuizBinding>(
    R.layout.fragment_quiz,
    FragmentQuizBinding::bind
) {
    private val viewModel: TrainingViewModel by activityViewModels {
        TrainingViewModelFactory(appContainer().getTrainingQuestionsUseCase)
    }
    private var renderedQuestionId: String? = null
    private var navigatedToResult: Boolean = false

    override fun onBoundView(savedInstanceState: Bundle?) {
        setupBackNavigation(binding.btnBack)
        if (viewModel.uiState.value.currentQuestion == null && !viewModel.uiState.value.completed) {
            viewModel.startQuiz()
        }
        binding.btnQuizAction.setOnClickListener {
            onActionClicked()
        }
        observeUi()
    }

    private fun observeUi() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    render(state)
                }
            }
        }
    }

    private fun render(state: TrainingUiState) = with(binding) {
        if (state.completed && !navigatedToResult) {
            navigatedToResult = true
            findNavController().navigate(R.id.action_quiz_to_quizResult)
            return@with
        }

        val question = state.currentQuestion ?: return@with
        tvHeaderTitle.text = getString(
            R.string.training_question_counter,
            state.currentPosition,
            state.totalQuestions
        )
        tvQuestionMeta.text = getString(
            R.string.training_quiz_meta,
            getString(state.selectedLevel.labelResId()),
            question.category
        )
        tvQuestionPrompt.text = question.prompt

        if (renderedQuestionId != question.id) {
            renderedQuestionId = question.id
            rgOptions.removeAllViews()
            question.options.forEachIndexed { index, optionText ->
                val radioButton = RadioButton(requireContext()).apply {
                    id = index + 1000
                    text = optionText
                }
                rgOptions.addView(radioButton)
            }
            tvFeedback.text = ""
        }

        for (index in 0 until rgOptions.childCount) {
            rgOptions.getChildAt(index).isEnabled = !state.answerChecked
        }

        if (state.answerChecked) {
            val label = if (state.lastAnswerCorrect == true) {
                getString(R.string.training_feedback_correct)
            } else {
                getString(R.string.training_feedback_incorrect)
            }
            val explanation = getString(
                R.string.training_feedback_explanation,
                state.lastExplanation.orEmpty()
            )
            tvFeedback.text = "$label\n$explanation"
            btnQuizAction.text = if (state.currentPosition == state.totalQuestions) {
                getString(R.string.training_show_result)
            } else {
                getString(R.string.training_next_question)
            }
        } else {
            tvFeedback.text = ""
            btnQuizAction.text = getString(R.string.training_submit_answer)
        }
    }

    private fun onActionClicked() {
        val state = viewModel.uiState.value
        if (state.answerChecked) {
            viewModel.goToNextQuestion()
            return
        }
        val selectedRadio = binding.rgOptions.checkedRadioButtonId
        if (selectedRadio == -1) {
            showShortMessage(getString(R.string.training_select_option))
            return
        }
        val selectedOptionIndex = selectedRadio - 1000
        viewModel.submitAnswer(selectedOptionIndex)
    }
}

package com.estef.antiphishingcoach.domain.training

import com.estef.antiphishingcoach.domain.model.TrainingQuestion

/**
 * Motor puro de quiz para entrenamiento.
 */
class QuizEngine(
    questions: List<TrainingQuestion>
) {
    private val items: List<TrainingQuestion> = questions
    private var currentIndex: Int = 0
    private var score: Int = 0
    private var answeredCurrent: Boolean = false
    private var lastWasCorrect: Boolean? = null
    private var completed: Boolean = items.isEmpty()

    fun totalQuestions(): Int = items.size
    fun currentPosition(): Int = currentIndex + 1
    fun currentQuestion(): TrainingQuestion? = items.getOrNull(currentIndex)
    fun score(): Int = score
    fun isCompleted(): Boolean = completed
    fun hasAnsweredCurrent(): Boolean = answeredCurrent
    fun lastAnswerWasCorrect(): Boolean? = lastWasCorrect

    fun submitAnswer(selectedIndex: Int): AnswerResult {
        if (completed) return AnswerResult(false, null)
        if (answeredCurrent) return AnswerResult(false, lastWasCorrect)

        val question = requireNotNull(currentQuestion())
        val correct = selectedIndex == question.correctIndex
        if (correct) {
            score += 1
        }
        answeredCurrent = true
        lastWasCorrect = correct
        return AnswerResult(true, correct)
    }

    fun nextQuestion(): Boolean {
        if (completed || !answeredCurrent) return false
        if (currentIndex == items.lastIndex) {
            completed = true
            return true
        }
        currentIndex += 1
        answeredCurrent = false
        lastWasCorrect = null
        return true
    }
}

data class AnswerResult(
    val accepted: Boolean,
    val correct: Boolean?
)

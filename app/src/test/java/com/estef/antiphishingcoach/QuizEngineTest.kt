package com.estef.antiphishingcoach

import com.estef.antiphishingcoach.domain.model.TrainingQuestion
import com.estef.antiphishingcoach.domain.training.QuizEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizEngineTest {

    private fun sampleQuestions(): List<TrainingQuestion> {
        return listOf(
            TrainingQuestion(
                id = "q1",
                prompt = "P1",
                options = listOf("A", "B", "C"),
                correctIndex = 1,
                explanation = "E1"
            ),
            TrainingQuestion(
                id = "q2",
                prompt = "P2",
                options = listOf("A", "B"),
                correctIndex = 0,
                explanation = "E2"
            )
        )
    }

    @Test
    fun `arranca en primera pregunta`() {
        val engine = QuizEngine(sampleQuestions())

        assertEquals(2, engine.totalQuestions())
        assertEquals(1, engine.currentPosition())
        assertEquals("q1", engine.currentQuestion()?.id)
    }

    @Test
    fun `sumar puntuacion solo en respuesta correcta`() {
        val engine = QuizEngine(sampleQuestions())

        val first = engine.submitAnswer(1)
        assertTrue(first.accepted)
        assertTrue(first.correct == true)
        assertEquals(1, engine.score())

        engine.nextQuestion()
        val second = engine.submitAnswer(1)
        assertTrue(second.accepted)
        assertFalse(second.correct == true)
        assertEquals(1, engine.score())
    }

    @Test
    fun `no permite responder dos veces la misma pregunta`() {
        val engine = QuizEngine(sampleQuestions())

        val first = engine.submitAnswer(0)
        val second = engine.submitAnswer(1)

        assertTrue(first.accepted)
        assertFalse(second.accepted)
    }

    @Test
    fun `no avanza si no se ha respondido`() {
        val engine = QuizEngine(sampleQuestions())

        assertFalse(engine.nextQuestion())
        assertEquals(1, engine.currentPosition())
    }

    @Test
    fun `marca quiz completado al terminar ultima pregunta`() {
        val engine = QuizEngine(sampleQuestions())

        engine.submitAnswer(1)
        engine.nextQuestion()
        engine.submitAnswer(0)
        val moved = engine.nextQuestion()

        assertTrue(moved)
        assertTrue(engine.isCompleted())
        assertEquals(2, engine.score())
    }
}

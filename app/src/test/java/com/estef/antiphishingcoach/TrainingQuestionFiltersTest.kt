package com.estef.antiphishingcoach

import com.estef.antiphishingcoach.domain.model.TrainingLevel
import com.estef.antiphishingcoach.domain.model.TrainingQuestion
import com.estef.antiphishingcoach.domain.training.filterByLevel
import org.junit.Assert.assertEquals
import org.junit.Test

class TrainingQuestionFiltersTest {

    @Test
    fun `filtra preguntas por nivel seleccionado`() {
        val questions = listOf(
            TrainingQuestion(
                id = "b1",
                prompt = "P1",
                options = listOf("A", "B", "C", "D"),
                correctIndex = 0,
                explanation = "E1",
                level = TrainingLevel.BEGINNER,
                category = "Basico"
            ),
            TrainingQuestion(
                id = "i1",
                prompt = "P2",
                options = listOf("A", "B", "C", "D"),
                correctIndex = 1,
                explanation = "E2",
                level = TrainingLevel.INTERMEDIATE,
                category = "Intermedio"
            ),
            TrainingQuestion(
                id = "a1",
                prompt = "P3",
                options = listOf("A", "B", "C", "D"),
                correctIndex = 2,
                explanation = "E3",
                level = TrainingLevel.ADVANCED,
                category = "Avanzado"
            )
        )

        val result = questions.filterByLevel(TrainingLevel.INTERMEDIATE)

        assertEquals(1, result.size)
        assertEquals("i1", result.first().id)
        assertEquals(TrainingLevel.INTERMEDIATE, result.first().level)
    }
}

package com.estef.antiphishingcoach

import com.estef.antiphishingcoach.data.local.seed.SeedJsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeedJsonParserTest {

    private val parser = SeedJsonParser()

    @Test
    fun `parsea escenarios coach validos`() {
        val json = """
            [
              {
                "id":"s1",
                "title":"Smishing",
                "summary":"Resumen",
                "threatLabel":"SMS fraudulento",
                "typicalSigns":["A","B"],
                "whatToDoNow":["Paso 1","Paso 2"],
                "whatToAvoid":["No 1"],
                "whenToEscalate":["Escalar"],
                "recommendedAction":"Accion final",
                "closingNote":"Cierre"
              },
              {"id":"s2","title":"Vishing","checklist":["X"]}
            ]
        """.trimIndent()

        val result = parser.parseCoachScenarios(json)

        assertEquals(2, result.size)
        assertEquals("s1", result.first().id)
        assertEquals("SMS fraudulento", result.first().threatLabel)
        assertEquals("Vishing", result[1].title)
    }

    @Test
    fun `filtra escenarios coach invalidos`() {
        val json = """
            [
              {"id":"","title":"Sin id","checklist":["A"]},
              {"id":"sin_pasos","title":"Sin pasos","summary":"Resumen","whatToDoNow":[]},
              {"id":"ok","title":"Ok","summary":"Resumen","whatToDoNow":["A"]}
            ]
        """.trimIndent()

        val result = parser.parseCoachScenarios(json)

        assertEquals(1, result.size)
        assertEquals("ok", result.first().id)
    }

    @Test
    fun `mantiene compatibilidad con escenarios legacy basados en checklist`() {
        val json = """
            [
              {"id":"legacy","title":"Caso antiguo","checklist":["Paso 1","Paso 2"]}
            ]
        """.trimIndent()

        val result = parser.parseCoachScenarios(json)

        assertEquals(1, result.size)
        assertEquals("legacy", result.first().id)
        assertEquals(2, result.first().checklist?.size)
    }

    @Test
    fun `parsea preguntas training validas`() {
        val json = """
            [
              {"id":"q1","prompt":"P1","options":["A","B"],"correctIndex":1,"explanation":"E1","level":"beginner","category":"Smishing"},
              {"id":"q2","prompt":"P2","options":["A","B","C"],"correctIndex":0,"explanation":"E2"}
            ]
        """.trimIndent()

        val result = parser.parseTrainingQuestions(json)

        assertEquals(2, result.size)
        assertEquals("beginner", result.first().level)
        assertEquals("Smishing", result.first().category)
        assertEquals("q2", result[1].id)
        assertEquals(null, result[1].level)
    }

    @Test
    fun `mantiene compatibilidad con preguntas sin nivel ni categoria`() {
        val json = """
            [
              {"id":"legacy","prompt":"Pregunta anterior","options":["A","B"],"correctIndex":0,"explanation":"Exp"}
            ]
        """.trimIndent()

        val result = parser.parseTrainingQuestions(json)

        assertEquals(1, result.size)
        assertEquals("legacy", result.first().id)
        assertEquals(null, result.first().level)
        assertEquals(null, result.first().category)
    }

    @Test
    fun `filtra preguntas training con indice incorrecto`() {
        val json = """
            [
              {"id":"bad","prompt":"P","options":["A"],"correctIndex":2,"explanation":"E"},
              {"id":"ok","prompt":"P2","options":["A","B"],"correctIndex":1,"explanation":"E2"}
            ]
        """.trimIndent()

        val result = parser.parseTrainingQuestions(json)

        assertEquals(1, result.size)
        assertTrue(result.all { it.id == "ok" })
    }
}

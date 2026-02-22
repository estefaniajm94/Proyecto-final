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
              {"id":"s1","title":"Smishing","checklist":["A","B"]},
              {"id":"s2","title":"Vishing","checklist":["X"]}
            ]
        """.trimIndent()

        val result = parser.parseCoachScenarios(json)

        assertEquals(2, result.size)
        assertEquals("s1", result.first().id)
    }

    @Test
    fun `filtra escenarios coach invalidos`() {
        val json = """
            [
              {"id":"","title":"Sin id","checklist":["A"]},
              {"id":"ok","title":"Ok","checklist":["A"]}
            ]
        """.trimIndent()

        val result = parser.parseCoachScenarios(json)

        assertEquals(1, result.size)
        assertEquals("ok", result.first().id)
    }

    @Test
    fun `parsea preguntas training validas`() {
        val json = """
            [
              {"id":"q1","prompt":"P1","options":["A","B"],"correctIndex":1,"explanation":"E1"},
              {"id":"q2","prompt":"P2","options":["A","B","C"],"correctIndex":0,"explanation":"E2"}
            ]
        """.trimIndent()

        val result = parser.parseTrainingQuestions(json)

        assertEquals(2, result.size)
        assertEquals("q2", result[1].id)
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

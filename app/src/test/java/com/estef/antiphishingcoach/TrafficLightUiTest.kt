package com.estef.antiphishingcoach

import com.estef.antiphishingcoach.core.model.TrafficLight
import com.estef.antiphishingcoach.presentation.common.toDisplayLabelEs
import com.estef.antiphishingcoach.presentation.common.toTrafficLightOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrafficLightUiTest {

    @Test
    fun `convierte codigos ingles y espanol a TrafficLight`() {
        assertEquals(TrafficLight.GREEN, "GREEN".toTrafficLightOrNull())
        assertEquals(TrafficLight.YELLOW, "AMARILLO".toTrafficLightOrNull())
        assertEquals(TrafficLight.RED, "rojo".toTrafficLightOrNull())
    }

    @Test
    fun `retorna null para valor desconocido`() {
        assertNull("unknown".toTrafficLightOrNull())
    }

    @Test
    fun `genera etiqueta en espanol desde enum`() {
        assertEquals("VERDE", TrafficLight.GREEN.toDisplayLabelEs())
        assertEquals("AMARILLO", TrafficLight.YELLOW.toDisplayLabelEs())
        assertEquals("ROJO", TrafficLight.RED.toDisplayLabelEs())
    }
}

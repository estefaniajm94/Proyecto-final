package com.estef.antiphishingcoach

import com.estef.antiphishingcoach.core.model.TrafficLight
import com.estef.antiphishingcoach.domain.model.DetectedSignal
import com.estef.antiphishingcoach.domain.model.RecommendationItem
import com.estef.antiphishingcoach.presentation.analyze.AnalyzeActionPlanBuilder
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyzeActionPlanBuilderTest {

    @Test
    fun build_for_red_risk_prioritizes_blocking_and_verification() {
        val plan = AnalyzeActionPlanBuilder.build(
            trafficLight = TrafficLight.RED,
            signals = listOf(
                DetectedSignal(
                    signalCode = "SENSITIVE_DATA_REQUEST",
                    title = "Peticion de datos sensibles",
                    explanation = "Solicita credenciales",
                    weight = 24
                ),
                DetectedSignal(
                    signalCode = "URL_EXECUTABLE_EXTENSION",
                    title = "Archivo ejecutable",
                    explanation = "Descarga instalable",
                    weight = 30
                )
            ),
            recommendations = listOf(
                RecommendationItem("REC_VERIFY_OFFICIAL_CHANNEL", "Verifica por canal oficial", ""),
                RecommendationItem("REC_BLOCK_CONTACT", "Bloquea y reporta", "")
            )
        )

        assertTrue(plan.steps.any { it.contains("No abras el enlace") })
        assertTrue(plan.steps.any { it.contains("No compartas") })
        assertTrue(plan.steps.any { it.contains("No instales archivos") })
        assertTrue(plan.steps.any { it.contains("Verifica con la entidad") })
        assertTrue(plan.steps.any { it.contains("Bloquea el remitente") })
        assertTrue(plan.showOfficialResources)
    }

    @Test
    fun build_for_green_keeps_low_friction_guidance() {
        val plan = AnalyzeActionPlanBuilder.build(
            trafficLight = TrafficLight.GREEN,
            signals = emptyList(),
            recommendations = emptyList()
        )

        assertTrue(plan.steps.any { it.contains("No se ve riesgo alto") })
        assertFalse(plan.showOfficialResources)
    }
}

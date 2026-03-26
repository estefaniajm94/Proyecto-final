package com.estef.antiphishingcoach

import com.estef.antiphishingcoach.presentation.analyze.AnalyzeInputInsightBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyzeInputInsightBuilderTest {

    @Test
    fun inspect_extracts_url_breakdown_and_observations() {
        val insights = AnalyzeInputInsightBuilder.inspect(
            "Urgente: revisa tu cuenta en http://correos-secure-login.xyz/verify?next=https://correos.es&token=abc123"
        )

        assertEquals(1, insights.urlInsights.size)
        assertEquals("correos-secure-login.xyz", insights.urlInsights.first().domain)
        assertTrue(insights.urlInsights.first().observations.any { it.contains("HTTPS") })
        assertTrue(insights.urlInsights.first().observations.any { it.contains("otra URL") })
    }

    @Test
    fun inspect_detects_suspicious_phrases() {
        val insights = AnalyzeInputInsightBuilder.inspect(
            "Ultimo aviso: premio disponible. Introduce tu password para verificar la cuenta."
        )

        assertTrue(insights.suspiciousPhrases.any { it.phrase == "ultimo aviso" })
        assertTrue(insights.suspiciousPhrases.any { it.phrase == "premio" })
        assertTrue(insights.suspiciousPhrases.any { it.phrase == "password" })
    }
}

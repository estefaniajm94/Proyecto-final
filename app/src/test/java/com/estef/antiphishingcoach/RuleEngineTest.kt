package com.estef.antiphishingcoach

import com.estef.antiphishingcoach.core.model.SourceType
import com.estef.antiphishingcoach.core.model.TrafficLight
import com.estef.antiphishingcoach.domain.heuristics.RuleEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleEngineTest {

    private val engine = RuleEngine()

    @Test
    fun `detecta urgencia y genera senal`() {
        val result = engine.analyze("Ultimo aviso hoy, evita bloqueo")

        assertTrue(result.signals.any { it.signalCode == "URGENCY_THREAT" })
        assertTrue(result.score >= 16)
    }

    @Test
    fun `detecta premio sospechoso`() {
        val result = engine.analyze("Has ganado un regalo en sorteo exclusivo")

        assertTrue(result.signals.any { it.signalCode == "PRIZE_GIFT" })
    }

    @Test
    fun `detecta peticion de datos sensibles`() {
        val result = engine.analyze("Envia tu PIN y codigo de verificacion")

        assertTrue(result.signals.any { it.signalCode == "SENSITIVE_DATA_REQUEST" })
    }

    @Test
    fun `detecta enlace http inseguro`() {
        val result = engine.analyze("Actualiza datos en http://seguro-login.xyz")

        assertTrue(result.signals.any { it.signalCode == "URL_HTTP_INSECURE" })
    }

    @Test
    fun `detecta acortador de enlace`() {
        val result = engine.analyze("Confirma aqui https://bit.ly/3abc9")

        assertTrue(result.signals.any { it.signalCode == "URL_SHORTENER" })
    }

    @Test
    fun `detecta patron de dominio con guiones o numeros`() {
        val result = engine.analyze("Abre https://mi-banco-seguro-1234.com")

        assertTrue(result.signals.any { it.signalCode == "URL_DOMAIN_PATTERN" })
    }

    @Test
    fun `detecta tld sospechoso y arroba en url`() {
        val result = engine.analyze("Verifica en https://cliente@seguridad-alerta.top/acceso")

        assertTrue(result.signals.any { it.signalCode == "URL_SUSPICIOUS_TLD" })
        assertTrue(result.signals.any { it.signalCode == "URL_AT_SYMBOL" })
    }

    @Test
    fun `detecta subdominio enganoso con marca`() {
        val result = engine.analyze("Tu cuenta: https://santander-login.seguro-update.xyz")

        assertTrue(result.signals.any { it.signalCode == "URL_DECEPTIVE_SUBDOMAIN" })
    }

    @Test
    fun `infiere tipo fuente link cuando solo hay url`() {
        val result = engine.analyze("https://bit.ly/test")

        assertEquals(SourceType.LINK, result.sourceType)
        assertNotNull(result.sanitizedDomain)
    }

    @Test
    fun `semaforo rojo cuando score supera umbral`() {
        val result = engine.analyze(
            "Ultimo aviso urgente de banco. Has ganado premio. " +
                "Envia PIN y codigo en http://cliente@santander-login.seguro-update.top"
        )

        assertEquals(TrafficLight.RED, result.trafficLight)
        assertTrue(result.score >= 70)
    }
}

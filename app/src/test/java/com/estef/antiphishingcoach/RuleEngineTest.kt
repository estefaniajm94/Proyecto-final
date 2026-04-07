package com.estef.antiphishingcoach

import com.estef.antiphishingcoach.core.model.SourceType
import com.estef.antiphishingcoach.core.model.TrafficLight
import com.estef.antiphishingcoach.domain.heuristics.RuleEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleEngineTest {

    private val engine = RuleEngine()

    @Test
    fun `detecta urgencia y genera senal`() {
        val result = engine.analyze("Ultimo aviso hoy, evita bloqueo")

        assertTrue(result.signals.any { it.signalCode == "URGENCY_THREAT" })
        assertTrue(result.score >= 14)
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

    // ── Nuevas reglas semánticas ───────────────────────────────────────────────

    @Test
    fun `detecta presion temporal en 24 horas`() {
        val result = engine.analyze("Si no actúa en 24 horas, su cuenta será bloqueada.")

        assertTrue(result.signals.any { it.signalCode == "TEMPORAL_PRESSURE" })
    }

    @Test
    fun `detecta consecuencia negativa cuenta suspendida`() {
        val result = engine.analyze("Su cuenta ha sido suspendida. Verifique sus datos.")

        assertTrue(result.signals.any { it.signalCode == "NEGATIVE_CONSEQUENCE" })
    }

    @Test
    fun `detecta solicitud de verificacion de identidad`() {
        val result = engine.analyze("Verifique su identidad lo antes posible.")

        assertTrue(result.signals.any { it.signalCode == "ACCOUNT_VERIFICATION_REQUEST" })
    }

    @Test
    fun `detecta micro-pago gastos de envio`() {
        val result = engine.analyze("Abone 1,99€ en concepto de gastos de envío.")

        assertTrue(result.signals.any { it.signalCode == "MICRO_PAYMENT_REQUEST" })
    }

    @Test
    fun `detecta logistica paquete no entregado`() {
        val result = engine.analyze("No hemos podido entregar su paquete. Actualice la dirección de entrega.")

        assertTrue(result.signals.any { it.signalCode == "LOGISTICS_DELIVERY_CONTEXT" })
    }

    @Test
    fun `detecta contexto bancario banca online acceso inusual`() {
        val result = engine.analyze("Hemos detectado un acceso inusual a su banca online.")

        assertTrue(result.signals.any { it.signalCode == "BANKING_FINANCE_CONTEXT" })
    }

    @Test
    fun `detecta organismo publico agencia tributaria expediente`() {
        val result = engine.analyze("Agencia Tributaria: revise el expediente número 2024-001.")

        assertTrue(result.signals.any { it.signalCode == "PUBLIC_AUTHORITY_CONTEXT" })
    }

    @Test
    fun `detecta amenaza soporte tecnico dispositivo comprometido`() {
        val result = engine.analyze("Su dispositivo ha sido comprometido. Contacte soporte técnico.")

        assertTrue(result.signals.any { it.signalCode == "TECH_SUPPORT_THREAT" })
    }

    // ── Señales de co-ocurrencia ──────────────────────────────────────────────

    @Test
    fun `combinacion banco verificacion bloqueo dispara COMBINED_BANK_ACCOUNT_TAKEOVER`() {
        val result = engine.analyze(
            "Hemos detectado un acceso inusual a su cuenta. " +
                "Verifique su identidad o será bloqueada."
        )

        assertTrue(result.signals.any { it.signalCode == "COMBINED_BANK_ACCOUNT_TAKEOVER" })
    }

    @Test
    fun `combinacion entrega micro-pago dispara COMBINED_DELIVERY_FEE_PATTERN`() {
        val result = engine.analyze(
            "Su paquete no pudo entregarse. Abone 1,99€ de gastos de aduana."
        )

        assertTrue(result.signals.any { it.signalCode == "COMBINED_DELIVERY_FEE_PATTERN" })
    }

    @Test
    fun `combinacion organismo publico urgencia dispara COMBINED_PUBLIC_AUTHORITY_THREAT`() {
        val result = engine.analyze(
            "Agencia Tributaria: último aviso. Su expediente está pendiente de resolución."
        )

        assertTrue(result.signals.any { it.signalCode == "COMBINED_PUBLIC_AUTHORITY_THREAT" })
    }

    @Test
    fun `combinacion credenciales con enlace dispara COMBINED_CREDENTIALS_WITH_LINK`() {
        val result = engine.analyze(
            "Introduce tu contraseña en https://example-login.xyz/acceso"
        )

        assertTrue(result.signals.any { it.signalCode == "COMBINED_CREDENTIALS_WITH_LINK" })
    }

    // ── Catálogo de dominios confiables ──────────────────────────────────────

    @Test
    fun `dominio oficial en catalogo activa TRUSTED_DOMAIN_BONUS`() {
        val result = engine.analyze("Consulta tu pedido en https://amazon.es/orders/123")

        assertTrue(result.signals.any { it.signalCode == "TRUSTED_DOMAIN_BONUS" })
    }

    @Test
    fun `dominio oficial pero URL con arroba no activa TRUSTED_DOMAIN_BONUS`() {
        val result = engine.analyze("Accede en https://user@amazon.es/login")

        assertFalse(result.signals.any { it.signalCode == "TRUSTED_DOMAIN_BONUS" })
        assertTrue(result.signals.any { it.signalCode == "URL_AT_SYMBOL" })
    }

    @Test
    fun `dominio oficial con punycode no activa TRUSTED_DOMAIN_BONUS`() {
        val result = engine.analyze("https://xn--bbv-pma.com/login")

        assertFalse(result.signals.any { it.signalCode == "TRUSTED_DOMAIN_BONUS" })
    }

    @Test
    fun `dominio oficial con texto altamente coercitivo mantiene minimo amarillo`() {
        val result = engine.analyze(
            "Correos: su paquete fue retenido. Abone 1,99€ urgente en https://correos.es/pago"
        )

        // El moderador reduce algo el score, pero las señales semánticas mantienen el riesgo
        assertTrue(
            "Score debe ser >= 35 pese al dominio confiable",
            result.trafficLight == TrafficLight.YELLOW || result.trafficLight == TrafficLight.RED
        )
    }

    // ── Normalización de texto ────────────────────────────────────────────────

    @Test
    fun `texto con tildes y urgencia dispara urgency o temporal pressure`() {
        val result = engine.analyze("Último aviso: actúe de inmediato.")

        assertTrue(
            "Se esperaba URGENCY_THREAT o TEMPORAL_PRESSURE",
            result.signals.any {
                it.signalCode == "URGENCY_THREAT" || it.signalCode == "TEMPORAL_PRESSURE"
            }
        )
    }

    @Test
    fun `suspension con tilde dispara NEGATIVE_CONSEQUENCE`() {
        val result = engine.analyze("Su cuenta quedará en suspensión.")

        assertTrue(result.signals.any { it.signalCode == "NEGATIVE_CONSEQUENCE" })
    }
}

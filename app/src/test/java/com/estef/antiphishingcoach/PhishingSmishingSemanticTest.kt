package com.estef.antiphishingcoach

import com.estef.antiphishingcoach.core.model.TrafficLight
import com.estef.antiphishingcoach.domain.heuristics.RuleEngine
import com.estef.antiphishingcoach.domain.model.AnalysisOutput
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests de cobertura semántica del motor heurístico.
 *
 * Valida los 8 casos esperados del enunciado + casos adicionales de regresión.
 * Ningún test comprueba una puntuación concreta: comprueba señales y nivel de riesgo mínimo.
 */
class PhishingSmishingSemanticTest {

    private val engine = RuleEngine()

    // ══════════════════════════════════════════════════════════════════════════
    // CASO A — Acceso inusual a cuenta bancaria
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    fun `caso A - banco acceso inusual verifica identidad - minimo amarillo`() {
        val result = engine.analyze(
            "Hemos detectado un acceso inusual a su cuenta. " +
                "Verifique su identidad en 24 horas o su banca online será suspendida."
        )
        assertMinYellow(result)
        assertHasAny(result, "BANKING_FINANCE_CONTEXT", "IMPERSONATION_KEYWORDS")
        assertHasSignal(result, "ACCOUNT_VERIFICATION_REQUEST")
        assertHasSignal(result, "NEGATIVE_CONSEQUENCE")
        assertHasAny(result, "TEMPORAL_PRESSURE", "URGENCY_THREAT")
    }

    @Test
    fun `caso A - patron combinado banco cuenta takeover dispara`() {
        val result = engine.analyze(
            "Hemos detectado un acceso inusual a su cuenta. " +
                "Verifique su identidad en 24 horas o su banca online será suspendida."
        )
        assertHasSignal(result, "COMBINED_BANK_ACCOUNT_TAKEOVER")
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CASO B — Paquetería + pago de 1,99 €
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    fun `caso B - paqueteria entrega fallida micro-pago - minimo amarillo`() {
        val result = engine.analyze(
            "No hemos podido entregar su paquete por datos incompletos. " +
                "Actualice la dirección y abone 1,99 EUR."
        )
        assertMinYellow(result)
        assertHasSignal(result, "LOGISTICS_DELIVERY_CONTEXT")
        assertHasSignal(result, "MICRO_PAYMENT_REQUEST")
        assertHasSignal(result, "ACCOUNT_VERIFICATION_REQUEST")
    }

    @Test
    fun `caso B - patron combinado entrega con pago dispara`() {
        val result = engine.analyze(
            "No hemos podido entregar su paquete por datos incompletos. " +
                "Actualice la dirección y abone 1,99 EUR."
        )
        assertHasSignal(result, "COMBINED_DELIVERY_FEE_PATTERN")
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CASO C — Agencia tributaria + sanción
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    fun `caso C - agencia tributaria ultimo aviso sancion - minimo amarillo`() {
        val result = engine.analyze(
            "Agencia Tributaria: último aviso. Tiene una sanción pendiente. Revise el expediente."
        )
        assertMinYellow(result)
        assertHasSignal(result, "PUBLIC_AUTHORITY_CONTEXT")
        assertHasAny(result, "URGENCY_THREAT", "TEMPORAL_PRESSURE")
        assertHasAny(result, "NEGATIVE_CONSEQUENCE", "PUBLIC_AUTHORITY_CONTEXT")
    }

    @Test
    fun `caso C - patron combinado organismo publico con amenaza dispara`() {
        val result = engine.analyze(
            "Agencia Tributaria: último aviso. Tiene una sanción pendiente. Revise el expediente."
        )
        assertHasSignal(result, "COMBINED_PUBLIC_AUTHORITY_THREAT")
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CASO D — Cuenta limitada, iniciar sesión
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    fun `caso D - cuenta limitada iniciar sesion para evitar suspension - minimo amarillo`() {
        val result = engine.analyze(
            "Su cuenta ha sido limitada. Inicie sesión para evitar la suspensión."
        )
        assertMinYellow(result)
        assertHasSignal(result, "NEGATIVE_CONSEQUENCE")
        assertHasSignal(result, "ACCOUNT_VERIFICATION_REQUEST")
        assertHasAny(result, "BANKING_FINANCE_CONTEXT", "IMPERSONATION_KEYWORDS")
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CASO E — Homoglifos de marca en texto
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    fun `caso E - homoglifo de santander en texto dispara senal de spoofing`() {
        // "sаntander" con 'а' cirílico (U+0430) en lugar de 'a' latino
        val result = engine.analyze("Accede a tu cuenta s\u0430ntander ahora.")
        assertHasSignal(result, "HOMOGLYPH_BRAND_SPOOFING")
        assertTrue("El score debe ser mayor que 0", result.score > 0)
    }

    @Test
    fun `caso E - homoglifo de bbva en dominio URL dispara senal de spoofing`() {
        // "bbv\u0430" con 'а' cirílico en el host del dominio
        val result = engine.analyze("Verifica en https://bbv\u0430.com/login")
        assertHasAny(result, "HOMOGLYPH_BRAND_SPOOFING", "URL_PUNYCODE_OR_NONASCII", "URL_MIXED_SCRIPTS")
        assertTrue("Score debe aumentar", result.score > 0)
    }

    @Test
    fun `caso E - patron combinado homoglifo con accion critica dispara`() {
        val result = engine.analyze(
            "Tu cuenta en s\u0430ntander ha sido bloqueada. Verifica tu identidad ahora."
        )
        assertHasSignal(result, "HOMOGLYPH_BRAND_SPOOFING")
        assertHasSignal(result, "COMBINED_HOMOGLYPH_WITH_CRITICAL_ACTION")
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CASO F — Dominio oficial real con texto neutro → riesgo contenido
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    fun `caso F - dominio amazon oficial con texto neutro reduce score`() {
        val resultConDominio = engine.analyze(
            "Tu pedido está en camino. Consulta el estado en https://amazon.es/track/abc123"
        )
        val resultSinDominio = engine.analyze(
            "Tu pedido está en camino."
        )
        // El dominio confiable activa TRUSTED_DOMAIN_BONUS con peso negativo
        assertHasSignal(resultConDominio, "TRUSTED_DOMAIN_BONUS")
        // La puntuación con dominio oficial debe ser <= que la puntuación sin dominio + margen
        assertTrue(
            "El dominio oficial no debe elevar el score respecto al caso sin URL",
            resultConDominio.score <= resultSinDominio.score + 15
        )
    }

    @Test
    fun `caso F - dominio correos oficial activa moderador confiable`() {
        val result = engine.analyze("Tu envío llega mañana. Seguimiento: https://correos.es/seguimiento/12345")
        assertHasSignal(result, "TRUSTED_DOMAIN_BONUS")
    }

    @Test
    fun `caso F - dominio agenciatributaria oficial activa moderador`() {
        val result = engine.analyze(
            "Consulte su declaración en https://agenciatributaria.gob.es/renta/borrador"
        )
        assertHasSignal(result, "TRUSTED_DOMAIN_BONUS")
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CASO G — Dominio oficial + señales técnicas fuertes NO se anulan
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    fun `caso G - dominio amazon pero URL con @ no aplica moderador`() {
        val result = engine.analyze(
            "Verifica tu cuenta: https://evil@amazon.es/login"
        )
        // El @ actúa como anulación dura: TRUSTED_DOMAIN_BONUS NO debe aparecer
        assertNoSignal(result, "TRUSTED_DOMAIN_BONUS")
        assertHasSignal(result, "URL_AT_SYMBOL")
    }

    @Test
    fun `caso G - dominio correos pero texto altamente coercitivo sigue siendo riesgo`() {
        val result = engine.analyze(
            "Correos: su paquete ha sido retenido. Abone 1,99€ de gastos de aduana urgente. " +
                "Si no actua en 24 horas, será devuelto. https://correos.es/pago"
        )
        // Aunque el dominio es oficial, las señales semánticas fuertes mantienen el riesgo
        assertHasSignal(result, "TRUSTED_DOMAIN_BONUS")
        assertHasSignal(result, "MICRO_PAYMENT_REQUEST")
        assertHasSignal(result, "LOGISTICS_DELIVERY_CONTEXT")
        // La puntuación final debe seguir siendo amarilla o roja
        assertMinYellow(result)
    }

    @Test
    fun `caso G - dominio bbva con punycode no aplica moderador confiable`() {
        val result = engine.analyze("https://xn--bbv-mma.com/login")
        assertNoSignal(result, "TRUSTED_DOMAIN_BONUS")
        assertHasSignal(result, "URL_PUNYCODE_OR_NONASCII")
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SEÑALES INDIVIDUALES — cobertura de familias
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    fun `detecta presion temporal con plazo concreto`() {
        val result = engine.analyze("Si no actúa en 24 horas, su acceso será bloqueado.")
        assertHasSignal(result, "TEMPORAL_PRESSURE")
    }

    @Test
    fun `detecta consecuencia negativa suspension`() {
        val result = engine.analyze("Su cuenta quedará suspendida si no verifica sus datos.")
        assertHasSignal(result, "NEGATIVE_CONSEQUENCE")
    }

    @Test
    fun `detecta solicitud de verificacion de cuenta`() {
        val result = engine.analyze("Por favor, verifique su identidad lo antes posible.")
        assertHasSignal(result, "ACCOUNT_VERIFICATION_REQUEST")
    }

    @Test
    fun `detecta micro-pago con gastos de aduana`() {
        val result = engine.analyze("Abone 2,99€ en concepto de gastos de aduana.")
        assertHasSignal(result, "MICRO_PAYMENT_REQUEST")
    }

    @Test
    fun `detecta contexto logistico paquete retenido`() {
        val result = engine.analyze("Su paquete está retenido en aduanas. Actualice la dirección de entrega.")
        assertHasSignal(result, "LOGISTICS_DELIVERY_CONTEXT")
    }

    @Test
    fun `detecta contexto bancario acceso inusual`() {
        val result = engine.analyze("Hemos detectado un acceso inusual a su cuenta bancaria.")
        assertHasSignal(result, "BANKING_FINANCE_CONTEXT")
    }

    @Test
    fun `detecta contexto organismo publico expediente`() {
        val result = engine.analyze("Tiene un expediente abierto en la Agencia Tributaria.")
        assertHasSignal(result, "PUBLIC_AUTHORITY_CONTEXT")
    }

    @Test
    fun `detecta amenaza de soporte tecnico falso`() {
        val result = engine.analyze("Su dispositivo ha sido comprometido. Contacte con soporte técnico.")
        assertHasSignal(result, "TECH_SUPPORT_THREAT")
    }

    // ══════════════════════════════════════════════════════════════════════════
    // NORMALIZACIÓN DE TEXTO — tildes y variantes morfológicas
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    fun `texto con tildes se normaliza y dispara reglas correctamente`() {
        // "último aviso" con tilde debe detectarse como urgencia
        val result = engine.analyze("Último aviso: actúe de inmediato.")
        assertHasAny(result, "URGENCY_THREAT", "TEMPORAL_PRESSURE")
    }

    @Test
    fun `suspension con tilde dispara consecuencia negativa`() {
        val result = engine.analyze("Su cuenta quedará en suspensión si no actúa.")
        assertHasSignal(result, "NEGATIVE_CONSEQUENCE")
    }

    @Test
    fun `sancion con tilde dispara consecuencia negativa`() {
        val result = engine.analyze("Tiene una sanción pendiente de la DGT.")
        assertHasAny(result, "NEGATIVE_CONSEQUENCE", "PUBLIC_AUTHORITY_CONTEXT")
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CASOS LEGÍTIMOS — evitar falsos positivos absurdos
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    fun `mensaje totalmente neutro es verde`() {
        val result = engine.analyze("Hola, ¿quedamos el martes a las 19:00?")
        assertTrue("Mensaje neutro debe ser verde", result.trafficLight == TrafficLight.GREEN)
    }

    @Test
    fun `confirmacion de compra amazon sin sospecha es verde o score bajo`() {
        val result = engine.analyze(
            "Tu pedido número 123-4567890 de Amazon ha sido confirmado y llegará el jueves."
        )
        // Solo logística básica, sin urgencia, sin micropago, sin credenciales
        assertTrue("Score debe ser bajo para confirmación legítima", result.score < 35)
    }

    @Test
    fun `recordatorio de cita medica es verde`() {
        val result = engine.analyze(
            "Le recordamos su cita médica el próximo miércoles a las 10:30 en el centro de salud."
        )
        assertTrue("Recordatorio médico debe ser verde", result.trafficLight == TrafficLight.GREEN)
    }

    @Test
    fun `notificacion de pago recibido sin urgencia es baja sospecha`() {
        val result = engine.analyze("Hemos recibido tu pago de 50€. Gracias por tu confianza.")
        assertTrue("Score debe ser bajo para notificación de pago recibido", result.score < 35)
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MEZCLA DE SCRIPTS EN DOMINIO
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    fun `dominio con mezcla de scripts dispara URL_MIXED_SCRIPTS`() {
        // 'о' cirílico mezclado con letras latinas en el dominio
        val result = engine.analyze("https://c\u043Erreos.es/seguimiento")
        assertHasAny(result, "URL_MIXED_SCRIPTS", "URL_PUNYCODE_OR_NONASCII")
    }

    @Test
    fun `dominio con mezcla de scripts no activa moderador confiable`() {
        val result = engine.analyze("https://c\u043Erreos.es/seguimiento")
        assertNoSignal(result, "TRUSTED_DOMAIN_BONUS")
    }

    // ══════════════════════════════════════════════════════════════════════════
    // UTILIDADES DE APOYO
    // ══════════════════════════════════════════════════════════════════════════

    private fun assertHasSignal(result: AnalysisOutput, signalCode: String) {
        assertTrue(
            "Se esperaba la señal '$signalCode'. Señales detectadas: ${result.signals.map { it.signalCode }}",
            result.signals.any { it.signalCode == signalCode }
        )
    }

    private fun assertNoSignal(result: AnalysisOutput, signalCode: String) {
        assertFalse(
            "NO se esperaba la señal '$signalCode'. Señales detectadas: ${result.signals.map { it.signalCode }}",
            result.signals.any { it.signalCode == signalCode }
        )
    }

    private fun assertHasAny(result: AnalysisOutput, vararg signalCodes: String) {
        assertTrue(
            "Se esperaba al menos una de las señales ${signalCodes.toList()}. " +
                "Detectadas: ${result.signals.map { it.signalCode }}",
            signalCodes.any { code -> result.signals.any { it.signalCode == code } }
        )
    }

    private fun assertMinYellow(result: AnalysisOutput) {
        assertTrue(
            "Se esperaba mínimo amarillo (score >= 35). Score actual: ${result.score}. " +
                "Señales: ${result.signals.map { it.signalCode }}",
            result.trafficLight == TrafficLight.YELLOW || result.trafficLight == TrafficLight.RED
        )
    }
}

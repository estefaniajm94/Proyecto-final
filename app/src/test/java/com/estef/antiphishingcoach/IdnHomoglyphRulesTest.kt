package com.estef.antiphishingcoach

import com.estef.antiphishingcoach.core.model.TrafficLight
import com.estef.antiphishingcoach.domain.heuristics.RuleEngine
import com.estef.antiphishingcoach.domain.model.AnalysisOutput
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IdnHomoglyphRulesTest {

    private val engine = RuleEngine()
    private val baseScore = engine.analyze("https://example.com").score

    @Test
    fun `xn punycode dispara URL_PUNYCODE_OR_NONASCII`() {
        val result = engine.analyze("https://xn--exmple-cua.com/login")
        assertHasSignal(result, "URL_PUNYCODE_OR_NONASCII")
        assertScoreAboveBase(result.score)
    }

    @Test
    fun `host con caracteres cirilicos dispara regla idn y posible mixed scripts`() {
        // Dominio: rapural.com visualmente parecido, con letras cirilicas y una latina.
        val result = engine.analyze("https://\u0440\u0430\u0443\u0440\u0430l.com/login")
        val hasIdnSignal = result.signals.any { it.signalCode == "URL_PUNYCODE_OR_NONASCII" }
        val hasMixedSignal = result.signals.any { it.signalCode == "URL_MIXED_SCRIPTS" }
        assertTrue("Se esperaba senal de IDN o mixed scripts", hasIdnSignal || hasMixedSignal)
        assertScoreAboveBase(result.score)
    }

    @Test
    fun `dominio ascii limpio no dispara reglas idn`() {
        val result = engine.analyze("https://apple.com")
        assertNoIdnSignals(result)
    }

    @Test
    fun `dominio unicode legitimo dispara punycode o nonascii sin rojo automatico`() {
        val result = engine.analyze("https://m\u00FCnchen.de")
        assertHasSignal(result, "URL_PUNYCODE_OR_NONASCII")
        assertScoreAboveBase(result.score)
        assertTrue("No debe quedar en rojo por esta senal aislada", result.trafficLight != TrafficLight.RED)
    }

    @Test
    fun `homoglifo en google dispara nonascii o mixed scripts`() {
        // gooogle visual con segunda o en cirilico (U+043E)
        val result = engine.analyze("https://www.go\u043Egle.com")
        val hasIdnSignal = result.signals.any { it.signalCode == "URL_PUNYCODE_OR_NONASCII" }
        val hasMixedSignal = result.signals.any { it.signalCode == "URL_MIXED_SCRIPTS" }
        assertTrue("Se esperaba senal de IDN o mixed scripts", hasIdnSignal || hasMixedSignal)
        assertScoreAboveBase(result.score)
    }

    @Test
    fun `url normal con query no dispara reglas idn`() {
        val result = engine.analyze("https://example.com/path?x=1")
        assertNoIdnSignals(result)
    }

    @Test
    fun `host con guiones y numeros no dispara reglas idn`() {
        val result = engine.analyze("https://secure-login-123.com")
        assertNoIdnSignals(result)
    }

    @Test
    fun `entrada malformada no crashea`() {
        val result = engine.analyze("https://xn--")
        assertNotNull(result)
        assertTrue(result.score >= 0)
    }

    private fun assertHasSignal(result: AnalysisOutput, signalCode: String) {
        assertTrue(
            "No se detecto la senal esperada: $signalCode",
            result.signals.any { it.signalCode == signalCode }
        )
    }

    private fun assertNoIdnSignals(result: AnalysisOutput) {
        val found = result.signals.map { it.signalCode }
        assertFalse(
            "No se esperaba URL_PUNYCODE_OR_NONASCII. Encontrado: $found",
            found.contains("URL_PUNYCODE_OR_NONASCII")
        )
        assertFalse(
            "No se esperaba URL_MIXED_SCRIPTS. Encontrado: $found",
            found.contains("URL_MIXED_SCRIPTS")
        )
    }

    private fun assertScoreAboveBase(score: Int) {
        assertTrue("El score debe aumentar respecto al caso base ($baseScore).", score > baseScore)
    }
}

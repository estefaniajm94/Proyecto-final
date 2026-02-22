package com.estef.antiphishingcoach

import com.estef.antiphishingcoach.domain.heuristics.RuleEngine
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlHeuristicsExtendedTest {

    private val engine = RuleEngine()
    private val baseScore = engine.analyze("https://example.com/home").score

    @Test
    fun `intent scheme detecta senal de alto riesgo`() {
        assertSignalAndScoreIncrease(
            input = "intent://scan/#Intent;scheme=zxing;package=com.example;end",
            expectedCode = "URL_SCHEME_INTENT"
        )
    }

    @Test
    fun `javascript scheme detecta senal script data`() {
        assertSignalAndScoreIncrease(
            input = "javascript:alert(1)",
            expectedCode = "URL_SCHEME_SCRIPT_OR_DATA"
        )
    }

    @Test
    fun `data scheme detecta senal script data`() {
        assertSignalAndScoreIncrease(
            input = "data:text/html;base64,PHNjcmlwdD4=",
            expectedCode = "URL_SCHEME_SCRIPT_OR_DATA"
        )
    }

    @Test
    fun `shortener mantiene deteccion existente`() {
        assertSignalAndScoreIncrease(
            input = "https://bit.ly/xxxx",
            expectedCode = "URL_SHORTENER"
        )
    }

    @Test
    fun `query con redireccion anidada detecta nested url`() {
        assertSignalAndScoreIncrease(
            input = "https://example.com/redirect?url=https://evil.com",
            expectedCode = "URL_NESTED_URL_PARAMETER"
        )
    }

    @Test
    fun `host ip detecta senal dedicada`() {
        assertSignalAndScoreIncrease(
            input = "http://185.1.2.3/pay",
            expectedCode = "URL_HOST_IS_IP"
        )
    }

    @Test
    fun `punycode detecta senal visual`() {
        assertSignalAndScoreIncrease(
            input = "https://xn--exmple-cua.com/login",
            expectedCode = "URL_PUNYCODE_OR_NONASCII"
        )
    }

    @Test
    fun `extension ejecutable detecta riesgo`() {
        assertSignalAndScoreIncrease(
            input = "https://example.com/file.apk",
            expectedCode = "URL_EXECUTABLE_EXTENSION"
        )
    }

    @Test
    fun `tel scheme detecta esquema no http`() {
        assertSignalAndScoreIncrease(
            input = "tel:+34900111222",
            expectedCode = "URL_SCHEME_NON_HTTP"
        )
    }

    @Test
    fun `token de alta entropia en query detecta senal`() {
        assertSignalAndScoreIncrease(
            input = "https://example.com/path?token=QWxhZGRpbjpvcGVuIHNlc2FtZQ==",
            expectedCode = "URL_HIGH_ENTROPY_TOKEN"
        )
    }

    @Test
    fun `url muy larga detecta senal de longitud`() {
        val longPath = "a".repeat(180)
        assertSignalAndScoreIncrease(
            input = "https://example.com/$longPath",
            expectedCode = "URL_TOO_LONG"
        )
    }

    @Test
    fun `muchos parametros detectan senal dedicada`() {
        val params = (1..9).joinToString("&") { index -> "p$index=v$index" }
        assertSignalAndScoreIncrease(
            input = "https://example.com/path?$params",
            expectedCode = "URL_MANY_PARAMS"
        )
    }

    @Test
    fun `entrada rara no rompe analizador`() {
        val result = engine.analyze("intent://%%% data:text/html;base64,%%% http://[::1")
        assertNotNull(result)
        assertTrue(result.score >= 0)
    }

    private fun assertSignalAndScoreIncrease(input: String, expectedCode: String) {
        val result = engine.analyze(input)
        assertTrue(
            "No se detecto la senal esperada $expectedCode para: $input",
            result.signals.any { it.signalCode == expectedCode }
        )
        assertTrue(
            "El score no aumento frente al caso base. Base=$baseScore actual=${result.score}",
            result.score > baseScore
        )
    }
}

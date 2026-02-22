package com.estef.antiphishingcoach

import com.estef.antiphishingcoach.domain.heuristics.UrlNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlNormalizerTest {

    @Test
    fun `normaliza url completa y extrae campos estructurados`() {
        val parsed = UrlNormalizer.parse(
            "https://Example.com:8080/path/login?x=1&x=2&next=https://evil.com"
        )

        assertEquals("https", parsed.scheme)
        assertEquals("example.com", parsed.host)
        assertEquals(8080, parsed.port)
        assertTrue(parsed.hasPort)
        assertEquals("/path/login", parsed.path)
        assertEquals(listOf("1", "2"), parsed.queryParams["x"])
        assertEquals(3, parsed.paramCount)
        assertFalse(parsed.hostIsIp)
        assertFalse(parsed.hasUserInfo)
        assertFalse(parsed.hostHasNonAscii)
    }

    @Test
    fun `tel url mantiene esquema y sin host`() {
        val parsed = UrlNormalizer.parse("tel:+34900111222")

        assertEquals("tel", parsed.scheme)
        assertNull(parsed.host)
        assertFalse(parsed.hasPort)
        assertTrue(parsed.fullUrlNormalized.startsWith("tel:"))
    }

    @Test
    fun `detecta host ip y user info`() {
        val parsed = UrlNormalizer.parse("http://user:pass@185.1.2.3/pay")

        assertEquals("http", parsed.scheme)
        assertEquals("185.1.2.3", parsed.host)
        assertTrue(parsed.hostIsIp)
        assertTrue(parsed.hasUserInfo)
    }

    @Test
    fun `detecta punycode en host`() {
        val parsed = UrlNormalizer.parse("https://xn--exmple-cua.com/login")

        assertTrue(parsed.hostHasPunycode)
        assertFalse(parsed.hostHasNonAscii)
    }

    @Test
    fun `detecta mezcla de alfabetos por etiqueta`() {
        val parsed = UrlNormalizer.parse("https://www.go\u043Egle.com")

        assertTrue(parsed.hostHasNonAscii)
        assertTrue(parsed.hostHasMixedScripts)
    }
}

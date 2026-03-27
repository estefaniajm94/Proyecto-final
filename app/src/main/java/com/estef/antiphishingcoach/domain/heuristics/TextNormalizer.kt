package com.estef.antiphishingcoach.domain.heuristics

/**
 * Normalización robusta de texto para matching heurístico.
 * Sin dependencias externas: solo minúsculas, plegado de tildes y colapso de espacios.
 */
object TextNormalizer {

    /**
     * Tabla de caracteres acentuados → equivalente ASCII.
     * Solo afecta a variantes latinas comunes (no homoglifos Unicode, que gestiona HomoglyphDetector).
     */
    private val accentMap: Map<Char, Char> = mapOf(
        'á' to 'a', 'à' to 'a', 'â' to 'a', 'ä' to 'a', 'ã' to 'a',
        'é' to 'e', 'è' to 'e', 'ê' to 'e', 'ë' to 'e',
        'í' to 'i', 'ì' to 'i', 'î' to 'i', 'ï' to 'i',
        'ó' to 'o', 'ò' to 'o', 'ô' to 'o', 'ö' to 'o', 'õ' to 'o',
        'ú' to 'u', 'ù' to 'u', 'û' to 'u', 'ü' to 'u',
        'ñ' to 'n',
        'ç' to 'c'
    )

    /**
     * Normaliza un texto para matching heurístico:
     * 1. Minúsculas
     * 2. Plegado de tildes (á→a, é→e, ñ→n, etc.)
     * 3. Colapso de espacios múltiples
     */
    fun normalize(input: String): String {
        val sb = StringBuilder(input.length)
        var prevWasSpace = false
        for (rawCh in input) {
            val ch = rawCh.lowercaseChar()
            val mapped = accentMap[ch] ?: ch
            if (mapped == ' ' || mapped == '\t' || mapped == '\n' || mapped == '\r') {
                if (!prevWasSpace) sb.append(' ')
                prevWasSpace = true
            } else {
                sb.append(mapped)
                prevWasSpace = false
            }
        }
        return sb.toString().trim()
    }
}

package com.estef.antiphishingcoach.domain.heuristics

/**
 * Detección de suplantación visual mediante homoglifos (homograph spoofing).
 *
 * Estrategia:
 * 1. Se define una tabla de caracteres Unicode visualmente similares a letras ASCII.
 * 2. Se aplica esa sustitución al texto/dominio ("esqueleto").
 * 3. Si el esqueleto contiene el nombre de una marca pero el texto original NO (por usar
 *    caracteres no-ASCII), se considera suplantación visual.
 *
 * Diseño conservador: solo se alerta si hay caracteres no-ASCII presentes; el texto
 * completamente ASCII nunca dispara esta lógica.
 */
object HomoglyphDetector {

    /**
     * Tabla de confusables: carácter no-ASCII → equivalente ASCII más próximo.
     * Cubre los casos más frecuentes en ataques de homograph spoofing (cirílico, griego,
     * armenio y algunos latin extendidos).
     */
    private val confusableMap: Map<Char, Char> = buildMap {
        // ── Cirílico ──────────────────────────────────────────────────────────
        put('\u0430', 'a')  // а → a
        put('\u0410', 'a')  // А → a  (mayúscula)
        put('\u0435', 'e')  // е → e
        put('\u0415', 'e')  // Е → e
        put('\u0456', 'i')  // і → i  (ucraniano)
        put('\u0406', 'i')  // І → i
        put('\u043E', 'o')  // о → o
        put('\u041E', 'o')  // О → o
        put('\u0440', 'r')  // р → r
        put('\u0420', 'r')  // Р → r
        put('\u0441', 'c')  // с → c
        put('\u0421', 'c')  // С → c
        put('\u0445', 'x')  // х → x
        put('\u0425', 'x')  // Х → x
        put('\u0443', 'y')  // у → y
        put('\u0423', 'y')  // У → y
        put('\u0432', 'b')  // в → b  (aproximación visual)
        put('\u0455', 's')  // ѕ → s
        put('\u0501', 'd')  // ԁ → d
        put('\u04BB', 'h')  // һ → h
        put('\u0392', 'b')  // Β (griego mayúscula) → b

        // ── Griego ────────────────────────────────────────────────────────────
        put('\u03BF', 'o')  // ο → o
        put('\u039F', 'o')  // Ο → o
        put('\u03B1', 'a')  // α → a
        put('\u0391', 'a')  // Α → a
        put('\u03B5', 'e')  // ε → e
        put('\u0395', 'e')  // Ε → e
        put('\u03B9', 'i')  // ι → i
        put('\u0399', 'i')  // Ι → i
        put('\u03C1', 'p')  // ρ → p  (visualmente similar a p)
        put('\u03A1', 'p')  // Ρ → p
        put('\u03BD', 'v')  // ν → v
        put('\u03BA', 'k')  // κ → k
        put('\u03C4', 't')  // τ → t

        // ── Armenio ───────────────────────────────────────────────────────────
        put('\u0578', 'n')  // ո → n
        put('\u0566', 'z')  // զ → z
        put('\u0564', 'd')  // դ → d

        // ── Latin extendido / símbolos ─────────────────────────────────────────
        put('\u00F8', 'o')  // ø → o
        put('\u01A1', 'o')  // ơ → o
        put('\u0131', 'i')  // ı (i sin punto, turco) → i
        put('\u0261', 'g')  // ɡ → g
        put('\u0501', 'd')  // ԁ → d

        // ── Espacios invisibles / separadores ─────────────────────────────────
        put('\u2009', ' ')  // espacio fino
        put('\u200A', ' ')  // espacio cabello
        put('\u200B', ' ')  // espacio de ancho cero
        put('\u00A0', ' ')  // espacio no separable
        put('\u2060', ' ')  // word joiner
    }

    /**
     * Marcas y entidades sensibles usadas para detectar suplantación visual.
     * Se comprueba tanto en el texto visible del mensaje como en los dominios/URLs.
     */
    val sensitiveBrands: List<String> = listOf(
        // Banca
        "santander", "bbva", "caixabank", "bankinter", "unicaja",
        // Logística
        "correos", "seur", "mrw", "dhl", "ups", "fedex",
        // Administración
        "hacienda", "tributaria", "seguridad social",
        // Retail / tech
        "amazon", "paypal", "google", "apple", "microsoft", "netflix",
        // Otros frecuentes en smishing
        "whatsapp", "telegram"
    )

    /**
     * Convierte un string sustituyendo todos los homoglifos conocidos por su equivalente ASCII.
     * El resultado ("esqueleto") se usa para comparar contra nombres de marca.
     */
    fun normalizeHomoglyphs(input: String): String =
        input.map { ch -> confusableMap[ch] ?: ch }.joinToString("")

    /**
     * Detecta si [text] contiene una imitación visual de alguna marca de [brands].
     *
     * Condiciones para disparar la señal:
     * - El texto contiene al menos un carácter no-ASCII (si es todo ASCII, no hay homoglifo).
     * - El esqueleto (texto con homoglifos sustituidos, en minúsculas) contiene el nombre
     *   de la marca.
     * - El texto original en minúsculas NO contiene el nombre de la marca (de lo contrario
     *   sería una mención legítima o un false positive).
     *
     * @return Nombre de la marca suplantada, o null si no se detecta nada.
     */
    fun detectBrandSpoofing(text: String, brands: List<String> = sensitiveBrands): String? {
        if (text.isBlank()) return null
        if (text.all { it.code < 128 }) return null   // texto ASCII puro → sin riesgo

        val lowerText = text.lowercase()
        val skeleton = normalizeHomoglyphs(lowerText)

        return brands.firstOrNull { brand ->
            skeleton.contains(brand) && !lowerText.contains(brand)
        }
    }

    /**
     * Detecta spoofing de marca en un host de dominio, etiqueta a etiqueta.
     * (Complementa la detección de mezcla de scripts que ya hace [UrlNormalizer].)
     *
     * @param host Host ya normalizado (lowercase, sin www.).
     * @return Nombre de la marca suplantada, o null.
     */
    fun detectBrandSpoofingInHost(host: String?, brands: List<String> = sensitiveBrands): String? {
        if (host.isNullOrBlank()) return null
        // Comprobación rápida: si el host es todo ASCII ya no hay homoglifo que detectar
        if (host.all { it.code < 128 }) return null

        // Analizar etiqueta a etiqueta para mayor precisión
        for (label in host.split('.')) {
            val spoof = detectBrandSpoofing(label, brands)
            if (spoof != null) return spoof
        }
        return null
    }
}

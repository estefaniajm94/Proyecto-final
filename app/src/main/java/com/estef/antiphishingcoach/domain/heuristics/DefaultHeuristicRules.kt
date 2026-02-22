package com.estef.antiphishingcoach.domain.heuristics

import com.estef.antiphishingcoach.domain.model.DetectedSignal

/**
 * Reglas heuristicas transparentes para MVP A.
 */
object DefaultHeuristicRules {

    val defaultSuspiciousTlds = setOf("xyz", "top", "click", "gq", "tk", "work", "zip", "mov")
    val defaultShortenerDomains = setOf("bit.ly", "tinyurl.com", "t.co", "goo.gl", "cutt.ly", "ow.ly", "is.gd", "rb.gy")
    val defaultImpersonationKeywords = listOf(
        "banco",
        "cuenta",
        "hacienda",
        "agencia tributaria",
        "seguridad social",
        "correos",
        "paqueteria",
        "mensajeria"
    )
    val defaultBrandKeywords = listOf("santander", "bbva", "caixabank", "correos", "dhl", "amazon", "hacienda", "seg-social")
    val defaultOfficialDomains = listOf(
        "santander.es",
        "bbva.es",
        "caixabank.es",
        "correos.es",
        "dhl.com",
        "amazon.es",
        "agenciatributaria.gob.es",
        "seg-social.gob.es"
    )
    private val nonHttpSchemes = setOf("tel", "sms", "mailto", "market", "file", "content")
    private val scriptOrDataSchemes = setOf("javascript", "data")
    private val executableExtensions = setOf("apk", "exe", "msi", "dmg", "pkg", "jar", "js", "scr", "bat", "ps1")
    private const val longUrlThreshold = 140
    private const val manyParamsThreshold = 8
    private const val highEntropyMinLength = 24
    private val hexTokenRegex = Regex("^[A-Fa-f0-9]{24,}$")
    private val base64LikeTokenRegex = Regex("^[A-Za-z0-9+/=_-]{24,}$")

    fun build(
        suspiciousTlds: Set<String> = defaultSuspiciousTlds,
        shortenerDomains: Set<String> = defaultShortenerDomains,
        impersonationKeywords: List<String> = defaultImpersonationKeywords,
        brandKeywords: List<String> = defaultBrandKeywords,
        officialDomains: List<String> = defaultOfficialDomains
    ): List<HeuristicRule> {
        return listOf(
            urgencyThreatRule(),
            prizeGiftRule(),
            sensitiveDataRule(),
            insecureHttpRule(),
            shortenerRule(shortenerDomains),
            domainHyphenNumberRule(),
            suspiciousTldRule(suspiciousTlds),
            atSymbolInUrlRule(),
            deceptiveSubdomainRule(brandKeywords, officialDomains),
            nonHttpSchemeRule(),
            intentSchemeRule(),
            scriptOrDataSchemeRule(),
            executableExtensionRule(),
            hostIsIpRule(),
            punycodeOrNonAsciiRule(),
            mixedScriptsRule(),
            nestedUrlParameterRule(),
            urlTooLongRule(),
            manyParametersRule(),
            highEntropyTokenRule(),
            impersonationKeywordRule(impersonationKeywords)
        )
    }

    private fun urgencyThreatRule(): HeuristicRule = HeuristicRule { context ->
        val keywords = listOf("ultimo aviso", "hoy", "bloqueo", "multa", "urgente", "inmediato")
        if (containsAnyKeyword(context.normalizedInput, keywords)) {
            DetectedSignal(
                signalCode = "URGENCY_THREAT",
                title = "Urgencia o amenaza",
                explanation = "El mensaje presiona para actuar de inmediato.",
                weight = 16
            )
        } else {
            null
        }
    }

    private fun prizeGiftRule(): HeuristicRule = HeuristicRule { context ->
        val keywords = listOf("has ganado", "sorteo", "regalo", "premio")
        if (containsAnyKeyword(context.normalizedInput, keywords)) {
            DetectedSignal(
                signalCode = "PRIZE_GIFT",
                title = "Premio o regalo sospechoso",
                explanation = "Los premios inesperados son una tactica de engano frecuente.",
                weight = 14
            )
        } else {
            null
        }
    }

    private fun sensitiveDataRule(): HeuristicRule = HeuristicRule { context ->
        val keywords = listOf("contrasena", "password", "pin", "tarjeta", "codigo", "verificacion", "otp", "cvv")
        if (containsAnyKeyword(context.normalizedInput, keywords)) {
            DetectedSignal(
                signalCode = "SENSITIVE_DATA_REQUEST",
                title = "Peticion de datos sensibles",
                explanation = "Solicita credenciales o datos de pago que no deben compartirse.",
                weight = 24
            )
        } else {
            null
        }
    }

    private fun insecureHttpRule(): HeuristicRule = HeuristicRule { context ->
        if (context.urls.any { it.raw.startsWith("http://", ignoreCase = true) }) {
            DetectedSignal(
                signalCode = "URL_HTTP_INSECURE",
                title = "Enlace sin HTTPS",
                explanation = "El enlace usa HTTP en lugar de HTTPS.",
                weight = 12
            )
        } else {
            null
        }
    }

    private fun shortenerRule(shortenerDomains: Set<String>): HeuristicRule = HeuristicRule { context ->
        if (context.urls.any { parsed -> parsed.host != null && shortenerDomains.contains(parsed.host) }) {
            DetectedSignal(
                signalCode = "URL_SHORTENER",
                title = "Acortador de enlace",
                explanation = "Los acortadores ocultan el destino real del enlace.",
                weight = 18
            )
        } else {
            null
        }
    }

    private fun domainHyphenNumberRule(): HeuristicRule = HeuristicRule { context ->
        val hasSuspiciousPattern = context.urls.any { parsed ->
            val host = parsed.host ?: return@any false
            val domainBody = host.substringBeforeLast(".")
            val hyphenCount = domainBody.count { it == '-' }
            val numberCount = domainBody.count { it.isDigit() }
            hyphenCount >= 2 || numberCount >= 4
        }
        if (hasSuspiciousPattern) {
            DetectedSignal(
                signalCode = "URL_DOMAIN_PATTERN",
                title = "Dominio con patron sospechoso",
                explanation = "El dominio contiene muchos guiones o numeros.",
                weight = 12
            )
        } else {
            null
        }
    }

    private fun suspiciousTldRule(suspiciousTlds: Set<String>): HeuristicRule = HeuristicRule { context ->
        if (context.urls.any { parsed -> parsed.tld != null && suspiciousTlds.contains(parsed.tld) }) {
            DetectedSignal(
                signalCode = "URL_SUSPICIOUS_TLD",
                title = "TLD de riesgo",
                explanation = "El enlace usa una extension de dominio asociada con abuso.",
                weight = 14
            )
        } else {
            null
        }
    }

    private fun atSymbolInUrlRule(): HeuristicRule = HeuristicRule { context ->
        if (context.urls.any { parsed -> parsed.raw.contains("@") }) {
            DetectedSignal(
                signalCode = "URL_AT_SYMBOL",
                title = "Uso de @ en URL",
                explanation = "La presencia de @ en URL puede ocultar el dominio real.",
                weight = 20
            )
        } else {
            null
        }
    }

    private fun deceptiveSubdomainRule(
        brandKeywords: List<String>,
        officialDomains: List<String>
    ): HeuristicRule = HeuristicRule { context ->
        val hasDeceptiveDomain = context.urls.any { parsed ->
            val host = parsed.host ?: return@any false
            val hasBrand = brandKeywords.any { keyword -> host.contains(keyword) }
            val looksOfficial = officialDomains.any { official ->
                host == official || host.endsWith(".$official")
            }
            hasBrand && !looksOfficial && host.count { it == '.' } >= 2
        }
        if (hasDeceptiveDomain) {
            DetectedSignal(
                signalCode = "URL_DECEPTIVE_SUBDOMAIN",
                title = "Subdominio enganoso",
                explanation = "El dominio incluye una marca conocida en un dominio no oficial.",
                weight = 20
            )
        } else {
            null
        }
    }

    /**
     * Calibracion de pesos para no disparar rojo por una sola senal menor:
     * - Alto (25-40): acciones que pueden abrir apps/ejecutar contenido o descargar ejecutables.
     * - Medio (12-25): ocultacion de destino y patrones tipicos de suplantacion tecnica.
     * - Bajo/medio (6-15): complejidad sospechosa (longitud, parametros, tokens) acumulable.
     */
    private fun nonHttpSchemeRule(): HeuristicRule = HeuristicRule { context ->
        if (context.urls.any { parsed -> nonHttpSchemes.contains(parsed.scheme) }) {
            DetectedSignal(
                signalCode = "URL_SCHEME_NON_HTTP",
                title = "Esquema no web",
                explanation = "El enlace usa un esquema que puede abrir apps o acciones fuera del navegador.",
                weight = 20
            )
        } else {
            null
        }
    }

    private fun intentSchemeRule(): HeuristicRule = HeuristicRule { context ->
        if (context.urls.any { parsed -> parsed.scheme == "intent" }) {
            DetectedSignal(
                signalCode = "URL_SCHEME_INTENT",
                title = "Esquema intent detectado",
                explanation = "Los enlaces intent:// pueden lanzar componentes/apps en Android.",
                weight = 32
            )
        } else {
            null
        }
    }

    private fun scriptOrDataSchemeRule(): HeuristicRule = HeuristicRule { context ->
        if (context.urls.any { parsed -> scriptOrDataSchemes.contains(parsed.scheme) }) {
            DetectedSignal(
                signalCode = "URL_SCHEME_SCRIPT_OR_DATA",
                title = "Esquema script/data",
                explanation = "Los esquemas javascript:/data: pueden ejecutar contenido directamente en el navegador.",
                weight = 34
            )
        } else {
            null
        }
    }

    private fun executableExtensionRule(): HeuristicRule = HeuristicRule { context ->
        val hasExecutable = context.urls.any { parsed ->
            val path = parsed.path ?: return@any false
            val lastSegment = path.substringAfterLast("/")
            val extension = lastSegment.substringAfterLast(".", "")
                .substringBefore("?")
                .lowercase()
            executableExtensions.contains(extension)
        }
        if (hasExecutable) {
            DetectedSignal(
                signalCode = "URL_EXECUTABLE_EXTENSION",
                title = "Archivo ejecutable/instalable",
                explanation = "La URL apunta a un archivo ejecutable/instalable.",
                weight = 30
            )
        } else {
            null
        }
    }

    private fun hostIsIpRule(): HeuristicRule = HeuristicRule { context ->
        if (context.urls.any { parsed -> parsed.hostIsIp }) {
            DetectedSignal(
                signalCode = "URL_HOST_IS_IP",
                title = "Host en IP",
                explanation = "Enlaces a IPs en lugar de dominios son comunes en fraudes/malware.",
                weight = 18
            )
        } else {
            null
        }
    }

    private fun punycodeOrNonAsciiRule(): HeuristicRule = HeuristicRule { context ->
        if (context.urls.any { parsed -> parsed.hostHasPunycode || parsed.hostHasNonAscii }) {
            DetectedSignal(
                signalCode = "URL_PUNYCODE_OR_NONASCII",
                title = "Dominio visualmente enganoso",
                explanation = "El dominio usa caracteres especiales o codificacion internacional (IDN). A veces se usa para suplantacion visual.",
                weight = 16
            )
        } else {
            null
        }
    }

    private fun mixedScriptsRule(): HeuristicRule = HeuristicRule { context ->
        if (context.urls.any { parsed -> parsed.hostHasMixedScripts }) {
            DetectedSignal(
                signalCode = "URL_MIXED_SCRIPTS",
                title = "Mezcla de alfabetos en dominio",
                explanation = "El dominio mezcla alfabetos para parecerse a otro (tecnica de suplantacion).",
                weight = 22
            )
        } else {
            null
        }
    }

    private fun nestedUrlParameterRule(): HeuristicRule = HeuristicRule { context ->
        if (context.urls.any { parsed -> UrlNormalizer.hasNestedUrlParameter(parsed.queryParams) }) {
            DetectedSignal(
                signalCode = "URL_NESTED_URL_PARAMETER",
                title = "URL anidada en parametros",
                explanation = "El enlace incluye otra URL dentro: tipico de redirecciones y ocultacion del destino.",
                weight = 18
            )
        } else {
            null
        }
    }

    private fun urlTooLongRule(): HeuristicRule = HeuristicRule { context ->
        if (context.urls.any { parsed -> parsed.urlLength > longUrlThreshold }) {
            DetectedSignal(
                signalCode = "URL_TOO_LONG",
                title = "URL excesivamente larga",
                explanation = "URLs muy largas pueden ocultar el destino o incluir tokens sospechosos.",
                weight = 10
            )
        } else {
            null
        }
    }

    private fun manyParametersRule(): HeuristicRule = HeuristicRule { context ->
        if (context.urls.any { parsed -> parsed.paramCount > manyParamsThreshold }) {
            DetectedSignal(
                signalCode = "URL_MANY_PARAMS",
                title = "Muchos parametros",
                explanation = "Muchos parametros pueden usarse para rastreo u ocultacion.",
                weight = 10
            )
        } else {
            null
        }
    }

    private fun highEntropyTokenRule(): HeuristicRule = HeuristicRule { context ->
        val hasToken = context.urls.any { parsed ->
            parsed.queryParams.values.flatten().any { value ->
                val candidate = value.trim()
                // Umbral moderado para capturar tokens base64/hex habituales en enlaces de phishing.
                candidate.length >= highEntropyMinLength &&
                    (hexTokenRegex.matches(candidate) || base64LikeTokenRegex.matches(candidate))
            }
        }
        if (hasToken) {
            DetectedSignal(
                signalCode = "URL_HIGH_ENTROPY_TOKEN",
                title = "Token de alta entropia",
                explanation = "Parametros con tokens largos/aleatorios pueden ocultar identificadores o payloads.",
                weight = 12
            )
        } else {
            null
        }
    }

    private fun impersonationKeywordRule(keywords: List<String>): HeuristicRule = HeuristicRule { context ->
        if (containsAnyKeyword(context.normalizedInput, keywords)) {
            DetectedSignal(
                signalCode = "IMPERSONATION_KEYWORDS",
                title = "Posible suplantacion",
                explanation = "Aparecen terminos tipicos de banco, paqueteria o administracion.",
                weight = 16
            )
        } else {
            null
        }
    }

    private fun containsAnyKeyword(text: String, keywords: List<String>): Boolean {
        return keywords.any { keyword -> text.contains(keyword, ignoreCase = true) }
    }
}

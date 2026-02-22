package com.estef.antiphishingcoach.domain.heuristics

/**
 * Contexto ya preprocesado para ejecutar reglas explicables.
 */
data class HeuristicContext(
    val rawInput: String,
    val normalizedInput: String,
    val urls: List<ParsedUrl>
)

data class ParsedUrl(
    val raw: String,
    val scheme: String,
    val host: String?,
    val port: Int?,
    val path: String?,
    val queryParams: Map<String, List<String>>,
    val fullUrlNormalized: String,
    val hostIsIp: Boolean,
    val hasUserInfo: Boolean,
    val hostHasNonAscii: Boolean,
    val hostHasPunycode: Boolean,
    val hostHasMixedScripts: Boolean,
    val hasPort: Boolean,
    val urlLength: Int,
    val paramCount: Int,
    val tld: String?
)

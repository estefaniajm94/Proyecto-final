package com.estef.antiphishingcoach.domain.heuristics

import java.net.IDN
import java.net.URI
import java.net.URISyntaxException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.regex.Pattern

/**
 * Parser local de URL para extraer una estructura normalizada reutilizable en reglas.
 *
 * No realiza llamadas de red ni validaciones remotas: todo se calcula en memoria.
 */
object UrlNormalizer {

    private val embeddedUrlPattern = Regex("(?i)(?:[a-z][a-z0-9+.-]*://|www\\.)")
    private val ipv4Pattern = Pattern.compile(
        "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$"
    )
    private val ipv6LoosePattern = Pattern.compile("^[0-9a-fA-F:.]+$")

    fun parse(rawUrl: String): ParsedUrl {
        val cleanedRaw = rawUrl.trim()
        val withFallbackScheme = if (cleanedRaw.hasExplicitScheme()) cleanedRaw else "https://$cleanedRaw"
        val uri = parseUriSafely(withFallbackScheme)
        val authorityFallback = extractAuthorityFallback(cleanedRaw)

        val scheme = uri?.scheme?.lowercase(Locale.ROOT) ?: if (cleanedRaw.hasExplicitScheme()) {
            cleanedRaw.substringBefore(':').lowercase(Locale.ROOT)
        } else {
            "https"
        }

        val host = (uri?.host ?: authorityFallback?.host)
            ?.lowercase(Locale.ROOT)
            ?.removePrefix("www.")
            ?.trimEnd('.')
            ?.ifBlank { null }

        val port = uri?.port?.takeIf { it >= 0 } ?: authorityFallback?.port
        val hasPort = port != null
        val rawPath = uri?.rawPath?.takeIf { it.isNotBlank() } ?: extractPathFallback(cleanedRaw)
        val path = normalizePath(rawPath)
        val queryParams = parseQueryParams(uri?.rawQuery ?: extractRawQueryFallback(cleanedRaw))
        val paramCount = queryParams.values.sumOf { it.size }

        val hasUserInfo = !uri?.userInfo.isNullOrBlank() || authorityFallback?.hasUserInfo == true
        val hostHasPunycode = hasPunycodeLabel(host)
        val unicodeHost = toUnicodeHost(host)
        val hostHasNonAscii = host?.any { it.code > 127 } == true
        val hostHasMixedScripts = hasMixedScriptsByLabel(unicodeHost ?: host)
        val hostIsIp = isHostIp(host)

        val fullUrlNormalized = buildFullUrlNormalized(
            scheme = scheme,
            host = host,
            port = port,
            path = path,
            queryParams = queryParams,
            cleanedRaw = cleanedRaw
        )

        return ParsedUrl(
            raw = cleanedRaw,
            scheme = scheme,
            host = host,
            port = port,
            path = path,
            queryParams = queryParams,
            fullUrlNormalized = fullUrlNormalized,
            hostIsIp = hostIsIp,
            hasUserInfo = hasUserInfo,
            hostHasNonAscii = hostHasNonAscii,
            hostHasPunycode = hostHasPunycode,
            hostHasMixedScripts = hostHasMixedScripts,
            hasPort = hasPort,
            urlLength = fullUrlNormalized.length,
            paramCount = paramCount,
            tld = host?.substringAfterLast(".", "")
        )
    }

    /**
     * Detecta redirecciones ocultas: param clave conocida o cualquier valor que contenga otra URL.
     */
    fun hasNestedUrlParameter(queryParams: Map<String, List<String>>): Boolean {
        val redirectKeys = setOf("url", "u", "redirect", "next", "target")
        return queryParams.any { (key, values) ->
            val normalizedKey = key.lowercase(Locale.ROOT)
            val byKey = redirectKeys.contains(normalizedKey) && values.any { containsEmbeddedUrl(it) }
            val byValue = values.any { containsEmbeddedUrl(it) }
            byKey || byValue
        }
    }

    private fun containsEmbeddedUrl(value: String): Boolean {
        return embeddedUrlPattern.containsMatchIn(value)
    }

    private fun parseUriSafely(candidate: String): URI? {
        return try {
            URI(candidate)
        } catch (_: URISyntaxException) {
            null
        }
    }

    private fun normalizePath(rawPath: String?): String? {
        if (rawPath.isNullOrBlank()) return null
        return rawPath.trim().ifBlank { null }
    }

    private fun parseQueryParams(rawQuery: String?): Map<String, List<String>> {
        if (rawQuery.isNullOrBlank()) return emptyMap()
        val map = linkedMapOf<String, MutableList<String>>()
        rawQuery.split("&")
            .filter { it.isNotBlank() }
            .forEach { item ->
                val pair = item.split("=", limit = 2)
                val key = decodeComponent(pair[0]).trim()
                if (key.isEmpty()) return@forEach
                val value = if (pair.size == 2) decodeComponent(pair[1]).trim() else ""
                map.getOrPut(key) { mutableListOf() }.add(value)
            }
        return map.mapValues { (_, values) -> values.toList() }
    }

    private fun decodeComponent(value: String): String {
        return runCatching {
            URLDecoder.decode(value, StandardCharsets.UTF_8.name())
        }.getOrDefault(value)
    }

    private fun buildFullUrlNormalized(
        scheme: String,
        host: String?,
        port: Int?,
        path: String?,
        queryParams: Map<String, List<String>>,
        cleanedRaw: String
    ): String {
        val query = if (queryParams.isEmpty()) {
            ""
        } else {
            queryParams.entries.joinToString("&", prefix = "?") { (key, values) ->
                if (values.isEmpty()) key else values.joinToString("&") { value -> "$key=$value" }
            }
        }

        return if (host != null) {
            buildString {
                append(scheme)
                append("://")
                append(host)
                if (port != null) {
                    append(":")
                    append(port)
                }
                if (!path.isNullOrBlank()) {
                    if (!path.startsWith("/")) append("/")
                    append(path)
                }
                append(query)
            }
        } else {
            // Para esquemas sin host (tel, mailto, data, javascript, etc.), normalizamos solo el prefijo.
            if (cleanedRaw.hasExplicitScheme()) {
                val separatorIndex = cleanedRaw.indexOf(':')
                val remainder = if (separatorIndex >= 0) cleanedRaw.substring(separatorIndex + 1) else cleanedRaw
                "$scheme:$remainder"
            } else {
                cleanedRaw
            }
        }
    }

    private fun extractAuthorityFallback(rawUrl: String): AuthorityParts? {
        val authoritySegment = when {
            rawUrl.contains("://") -> rawUrl.substringAfter("://").substringBeforeAny("/", "?", "#")
            rawUrl.startsWith("www.", ignoreCase = true) -> rawUrl.substringBeforeAny("/", "?", "#")
            else -> return null
        }.trim()

        if (authoritySegment.isBlank()) return null
        val hasUserInfo = authoritySegment.contains("@")
        val hostAndPort = authoritySegment.substringAfterLast("@")

        if (hostAndPort.startsWith("[")) {
            val endBracket = hostAndPort.indexOf(']')
            if (endBracket < 0) return null
            val host = hostAndPort.substring(1, endBracket)
            val port = hostAndPort.substring(endBracket + 1).removePrefix(":").toIntOrNull()
            return AuthorityParts(host = host, port = port, hasUserInfo = hasUserInfo)
        }

        val host = hostAndPort.substringBefore(":").ifBlank { null } ?: return null
        val port = hostAndPort.substringAfter(":", "").toIntOrNull()
        return AuthorityParts(host = host, port = port, hasUserInfo = hasUserInfo)
    }

    private fun extractPathFallback(rawUrl: String): String? {
        val remainder = when {
            rawUrl.contains("://") -> rawUrl.substringAfter("://")
            rawUrl.startsWith("www.", ignoreCase = true) -> rawUrl
            else -> return null
        }
        val afterAuthority = remainder.substringAfter("/", "")
        if (afterAuthority.isBlank()) return null
        val rawPath = "/" + afterAuthority.substringBefore("?").substringBefore("#")
        return rawPath.ifBlank { null }
    }

    private fun extractRawQueryFallback(rawUrl: String): String? {
        if (!rawUrl.contains("?")) return null
        return rawUrl.substringAfter("?").substringBefore("#").ifBlank { null }
    }

    private fun hasPunycodeLabel(host: String?): Boolean {
        if (host.isNullOrBlank()) return false
        return host.split('.').any { label -> label.startsWith("xn--", ignoreCase = true) }
    }

    private fun toUnicodeHost(host: String?): String? {
        if (host.isNullOrBlank()) return null
        return runCatching {
            IDN.toUnicode(host)
        }.getOrNull()
    }

    /**
     * Marca mezcla sospechosa cuando una misma etiqueta combina alfabetos distintos
     * (p.ej. Latin + Cyrillic), ignorando digitos, guiones y scripts comunes.
     */
    private fun hasMixedScriptsByLabel(host: String?): Boolean {
        if (host.isNullOrBlank()) return false
        return host.split('.').any { label ->
            if (label.isBlank()) return@any false
            val scripts = linkedSetOf<Character.UnicodeScript>()
            label.forEach { ch ->
                if (ch.isDigit() || ch == '-') return@forEach
                val script = Character.UnicodeScript.of(ch.code)
                when (script) {
                    Character.UnicodeScript.COMMON,
                    Character.UnicodeScript.INHERITED,
                    Character.UnicodeScript.UNKNOWN -> Unit

                    else -> scripts.add(script)
                }
            }
            scripts.size > 1
        }
    }

    private fun isHostIp(host: String?): Boolean {
        if (host.isNullOrBlank()) return false
        if (ipv4Pattern.matcher(host).matches()) return true

        val normalized = host.removePrefix("[").removeSuffix("]")
        val hasIpv6Shape = normalized.contains(":") && ipv6LoosePattern.matcher(normalized).matches()
        return hasIpv6Shape
    }

    private fun String.hasExplicitScheme(): Boolean {
        return indexOf(':') in 1..20 && matches(Regex("^[a-zA-Z][a-zA-Z0-9+.-]*:.*$"))
    }

    private fun String.substringBeforeAny(vararg delimiters: String): String {
        var current = this
        delimiters.forEach { delimiter ->
            current = current.substringBefore(delimiter, current)
        }
        return current
    }

    private data class AuthorityParts(
        val host: String,
        val port: Int?,
        val hasUserInfo: Boolean
    )
}

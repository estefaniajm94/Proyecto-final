package com.estef.antiphishingcoach.presentation.analyze

import com.estef.antiphishingcoach.domain.heuristics.ParsedUrl
import com.estef.antiphishingcoach.domain.heuristics.UrlNormalizer

data class AnalyzeInputInsights(
    val quickExplanation: String,
    val urlInsights: List<AnalyzeUrlInsight>,
    val suspiciousPhrases: List<SuspiciousPhraseInsight>
)

data class AnalyzeUrlInsight(
    val rawUrl: String,
    val domain: String?,
    val scheme: String,
    val path: String?,
    val parameterCount: Int,
    val observations: List<String>
)

data class SuspiciousPhraseInsight(
    val phrase: String,
    val category: String
)

object AnalyzeInputInsightBuilder {
    private val urlRegex = Regex(
        """(?i)\b((?:https?://|www\.|intent://|javascript:|data:|mailto:|tel:|sms:|market://|file://|content://)[^\s<>"']+)"""
    )

    private val suspiciousPhraseGroups = listOf(
        PhraseGroup(
            category = "Urgencia",
            keywords = listOf("ultimo aviso", "hoy", "bloqueo", "multa", "urgente", "inmediato")
        ),
        PhraseGroup(
            category = "Premio o gancho",
            keywords = listOf("has ganado", "sorteo", "regalo", "premio")
        ),
        PhraseGroup(
            category = "Datos sensibles",
            keywords = listOf("contrasena", "password", "pin", "tarjeta", "codigo", "verificacion", "otp", "cvv")
        ),
        PhraseGroup(
            category = "Suplantacion",
            keywords = listOf("banco", "cuenta", "hacienda", "agencia tributaria", "seguridad social", "correos", "paqueteria", "mensajeria")
        )
    )

    fun inspect(input: String): AnalyzeInputInsights {
        val parsedUrls = extractUrls(input).map(UrlNormalizer::parse)
        val urlInsights = parsedUrls.map(::toUrlInsight)
        val suspiciousPhrases = findSuspiciousPhrases(input)
        val quickExplanation = buildQuickExplanation(urlInsights, suspiciousPhrases)

        return AnalyzeInputInsights(
            quickExplanation = quickExplanation,
            urlInsights = urlInsights,
            suspiciousPhrases = suspiciousPhrases
        )
    }

    private fun extractUrls(input: String): List<String> {
        return urlRegex.findAll(input)
            .map { match -> match.groupValues[1].trimEnd('.', ',', ';', ':', '!', '?') }
            .filter { it.isNotBlank() }
            .distinct()
            .toList()
    }

    private fun toUrlInsight(parsedUrl: ParsedUrl): AnalyzeUrlInsight {
        val observations = buildList {
            if (parsedUrl.host == null) add("No se pudo extraer un dominio claro.")
            if (parsedUrl.scheme != "https") add("No usa HTTPS como esquema principal.")
            if (parsedUrl.hasUserInfo) add("Incluye credenciales o prefijos antes del dominio.")
            if (parsedUrl.hostIsIp) add("Usa una IP en lugar de un dominio legible.")
            if (parsedUrl.hostHasPunycode || parsedUrl.hostHasNonAscii || parsedUrl.hostHasMixedScripts) {
                add("El dominio puede intentar parecerse visualmente a otro.")
            }
            if (UrlNormalizer.hasNestedUrlParameter(parsedUrl.queryParams)) {
                add("Incluye otra URL dentro de los parametros.")
            }
            if (parsedUrl.paramCount >= 5) add("Tiene muchos parametros para revisar.")
            if (parsedUrl.urlLength >= 100) add("La URL es larga y puede ocultar partes relevantes.")
        }

        return AnalyzeUrlInsight(
            rawUrl = parsedUrl.raw,
            domain = parsedUrl.host,
            scheme = parsedUrl.scheme,
            path = parsedUrl.path,
            parameterCount = parsedUrl.paramCount,
            observations = observations
        )
    }

    private fun findSuspiciousPhrases(input: String): List<SuspiciousPhraseInsight> {
        val normalizedInput = input.lowercase()
        return suspiciousPhraseGroups.flatMap { group ->
            group.keywords.mapNotNull { keyword ->
                if (normalizedInput.contains(keyword.lowercase())) {
                    SuspiciousPhraseInsight(
                        phrase = keyword,
                        category = group.category
                    )
                } else {
                    null
                }
            }
        }.distinctBy { insight -> insight.phrase }
    }

    private fun buildQuickExplanation(
        urlInsights: List<AnalyzeUrlInsight>,
        suspiciousPhrases: List<SuspiciousPhraseInsight>
    ): String {
        return when {
            urlInsights.any { it.observations.isNotEmpty() } -> {
                "La alerta principal esta en el enlace: revisa el dominio real y las observaciones tecnicas."
            }

            suspiciousPhrases.any { it.category == "Datos sensibles" || it.category == "Urgencia" } -> {
                "La alerta principal esta en el mensaje: intenta presionar o pedir informacion sensible."
            }

            urlInsights.isNotEmpty() -> {
                "Se ha detectado un enlace. Comprueba que el dominio real coincide con la entidad que aparenta ser."
            }

            suspiciousPhrases.isNotEmpty() -> {
                "El texto contiene frases tipicas de fraude. Conviene verificar el remitente y el contexto."
            }

            else -> {
                "No se ven patrones tecnicos fuertes, pero siempre conviene verificar remitente, contexto y canal oficial."
            }
        }
    }

    private data class PhraseGroup(
        val category: String,
        val keywords: List<String>
    )
}

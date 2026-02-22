package com.estef.antiphishingcoach.domain.heuristics

import com.estef.antiphishingcoach.core.model.SourceType
import com.estef.antiphishingcoach.core.model.TrafficLight
import com.estef.antiphishingcoach.domain.model.AnalysisOutput
import com.estef.antiphishingcoach.domain.model.DetectedSignal
import java.util.LinkedHashSet

/**
 * Motor de scoring transparente (sin IA): aplica reglas y devuelve resultado explicable.
 */
class RuleEngine(
    greenThreshold: Int = 35,
    yellowThreshold: Int = 70,
    rules: List<HeuristicRule> = DefaultHeuristicRules.build()
) {
    private val configuredGreenThreshold = greenThreshold.coerceAtLeast(0)
    private val configuredYellowThreshold = yellowThreshold.coerceAtLeast(configuredGreenThreshold + 1)
    private val configuredRules = rules

    fun analyze(input: String): AnalysisOutput {
        val context = buildContext(input)
        val signals = configuredRules.mapNotNull { rule -> rule.evaluate(context) }
        val score = signals.sumOf { signal -> signal.weight }.coerceIn(0, 100)
        val trafficLight = when {
            score < configuredGreenThreshold -> TrafficLight.GREEN
            score < configuredYellowThreshold -> TrafficLight.YELLOW
            else -> TrafficLight.RED
        }
        val recommendationCodes = buildRecommendationCodes(signals, score)
        return AnalysisOutput(
            score = score,
            trafficLight = trafficLight,
            sourceType = inferSourceType(context),
            sanitizedDomain = context.urls.firstNotNullOfOrNull { parsed -> parsed.host },
            recommendationCodes = recommendationCodes,
            signals = signals
        )
    }

    private fun buildContext(input: String): HeuristicContext {
        val urls = extractUrls(input).map { rawUrl -> UrlNormalizer.parse(rawUrl) }
        return HeuristicContext(
            rawInput = input,
            normalizedInput = input.lowercase().trim(),
            urls = urls
        )
    }

    private fun inferSourceType(context: HeuristicContext): SourceType {
        if (context.urls.isEmpty()) return SourceType.TEXT
        val textWithoutUrls = urlRegex.replace(context.rawInput, "").trim()
        return if (textWithoutUrls.isEmpty()) SourceType.LINK else SourceType.MIXED
    }

    private fun buildRecommendationCodes(signals: List<DetectedSignal>, score: Int): List<String> {
        val codes = LinkedHashSet<String>()
        signals.forEach { signal ->
            when (signal.signalCode) {
                "URGENCY_THREAT" -> codes.addAll(listOf("REC_DO_NOT_ACT_FAST", "REC_VERIFY_OFFICIAL_CHANNEL"))
                "PRIZE_GIFT" -> codes.add("REC_IGNORE_PRIZE_SCAM")
                "SENSITIVE_DATA_REQUEST" -> codes.add("REC_DO_NOT_SHARE_CREDENTIALS")
                "URL_HTTP_INSECURE" -> codes.add("REC_AVOID_NON_HTTPS")
                "URL_SHORTENER" -> codes.add("REC_EXPAND_SHORT_URL")
                "URL_DOMAIN_PATTERN", "URL_SUSPICIOUS_TLD", "URL_AT_SYMBOL", "URL_DECEPTIVE_SUBDOMAIN" -> {
                    codes.add("REC_VERIFY_DOMAIN")
                }
                "URL_SCHEME_NON_HTTP", "URL_SCHEME_INTENT", "URL_SCHEME_SCRIPT_OR_DATA" -> {
                    codes.add("REC_AVOID_DEEP_LINKS")
                }
                "URL_EXECUTABLE_EXTENSION" -> codes.add("REC_DO_NOT_INSTALL_FILES")
                "URL_HOST_IS_IP", "URL_PUNYCODE_OR_NONASCII", "URL_MIXED_SCRIPTS", "URL_NESTED_URL_PARAMETER" -> {
                    codes.add("REC_VERIFY_DOMAIN")
                }
                "URL_TOO_LONG", "URL_MANY_PARAMS", "URL_HIGH_ENTROPY_TOKEN" -> {
                    codes.add("REC_REVIEW_URL_CAREFULLY")
                }
                "IMPERSONATION_KEYWORDS" -> codes.add("REC_CALL_OFFICIAL_NUMBER")
            }
        }
        when {
            score >= 70 -> codes.addAll(listOf("REC_BLOCK_CONTACT", "REC_VERIFY_OFFICIAL_CHANNEL"))
            score in configuredGreenThreshold until configuredYellowThreshold -> codes.add("REC_VERIFY_OFFICIAL_CHANNEL")
            else -> codes.add("REC_STAY_ALERT")
        }
        return codes.toList()
    }

    private fun extractUrls(input: String): List<String> {
        return urlRegex.findAll(input)
            .map { match ->
                match.groupValues[1].trimEnd('.', ',', ';', ':', '!', '?')
            }
            .filter { it.isNotBlank() }
            .toList()
    }

    private companion object {
        private val urlRegex = Regex(
            """(?i)\b((?:https?://|www\.|intent://|javascript:|data:|mailto:|tel:|sms:|market://|file://|content://)[^\s<>"']+)"""
        )
    }
}

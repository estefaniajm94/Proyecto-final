package com.estef.antiphishingcoach.domain.heuristics

import com.estef.antiphishingcoach.core.model.SourceType
import com.estef.antiphishingcoach.core.model.TrafficLight
import com.estef.antiphishingcoach.domain.model.AnalysisOutput
import com.estef.antiphishingcoach.domain.model.DetectedSignal
import java.util.LinkedHashSet

/**
 * Motor de scoring transparente (sin IA): aplica reglas y devuelve resultado explicable.
 *
 * Flujo:
 * 1. Normaliza el texto (minúsculas + plegado de tildes + colapso de espacios).
 * 2. Extrae y parsea URLs.
 * 3. Aplica todas las reglas independientes → lista de señales individuales.
 * 4. Aplica detección de co-ocurrencia → señales de combinación bonus.
 * 5. Suma pesos (incluido el moderador negativo si procede) y clasifica.
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

        // Paso 3: reglas individuales
        val individualSignals = configuredRules.mapNotNull { rule -> rule.evaluate(context) }

        // Paso 4: señales de co-ocurrencia (bonus por combinación)
        val combinationSignals = buildCombinationSignals(individualSignals, context)

        val signals = individualSignals + combinationSignals
        val score = signals.sumOf { it.weight }.coerceIn(0, 100)
        val trafficLight = when {
            score < configuredGreenThreshold -> TrafficLight.GREEN
            score < configuredYellowThreshold -> TrafficLight.YELLOW
            else -> TrafficLight.RED
        }

        return AnalysisOutput(
            score = score,
            trafficLight = trafficLight,
            sourceType = inferSourceType(context),
            sanitizedDomain = context.urls.firstNotNullOfOrNull { it.host },
            recommendationCodes = buildRecommendationCodes(signals, score),
            signals = signals
        )
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CONSTRUCCIÓN DEL CONTEXTO
    // ══════════════════════════════════════════════════════════════════════════

    private fun buildContext(input: String): HeuristicContext {
        val urls = extractUrls(input).map { rawUrl -> UrlNormalizer.parse(rawUrl) }
        return HeuristicContext(
            rawInput = input,
            // Normalización completa: minúsculas + tildes + espacios (mejora recall en todas las reglas)
            normalizedInput = TextNormalizer.normalize(input),
            urls = urls
        )
    }

    private fun inferSourceType(context: HeuristicContext): SourceType {
        if (context.urls.isEmpty()) return SourceType.TEXT
        val textWithoutUrls = urlRegex.replace(context.rawInput, "").trim()
        return if (textWithoutUrls.isEmpty()) SourceType.LINK else SourceType.MIXED
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CO-OCURRENCIA: SEÑALES DE COMBINACIÓN
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Detecta patrones de alto riesgo cuando varias señales individuales aparecen juntas.
     * Cada combinación es explícita y auditable.
     */
    private fun buildCombinationSignals(
        signals: List<DetectedSignal>,
        context: HeuristicContext
    ): List<DetectedSignal> {
        val codes = signals.map { it.signalCode }.toSet()
        val hasUrl = context.urls.isNotEmpty()
        val result = mutableListOf<DetectedSignal>()

        // ── Toma de cuenta bancaria ────────────────────────────────────────────
        // Banking/impersonation + verification request + negative consequence
        if ((codes.contains("BANKING_FINANCE_CONTEXT") || codes.contains("IMPERSONATION_KEYWORDS")) &&
            (codes.contains("ACCOUNT_VERIFICATION_REQUEST") || codes.contains("SENSITIVE_DATA_REQUEST")) &&
            codes.contains("NEGATIVE_CONSEQUENCE")
        ) {
            result += DetectedSignal(
                signalCode = "COMBINED_BANK_ACCOUNT_TAKEOVER",
                title = "Patrón: toma de cuenta bancaria",
                explanation = "Combinación de contexto bancario + solicitud de verificación + amenaza de bloqueo: patrón clásico de phishing bancario.",
                weight = 12
            )
        }

        // ── Entrega con micro-pago ─────────────────────────────────────────────
        // Logistics + micro-payment
        if (codes.contains("LOGISTICS_DELIVERY_CONTEXT") && codes.contains("MICRO_PAYMENT_REQUEST")) {
            result += DetectedSignal(
                signalCode = "COMBINED_DELIVERY_FEE_PATTERN",
                title = "Patrón: entrega con pago pendiente",
                explanation = "Combinación de contexto de paquetería + solicitud de pago pequeño: patrón típico de smishing de mensajería.",
                weight = 10
            )
        }

        // ── Organismo público + amenaza ────────────────────────────────────────
        // Public authority + (urgency or temporal pressure or negative consequence)
        if (codes.contains("PUBLIC_AUTHORITY_CONTEXT") &&
            (codes.contains("URGENCY_THREAT") || codes.contains("TEMPORAL_PRESSURE") ||
                codes.contains("NEGATIVE_CONSEQUENCE"))
        ) {
            result += DetectedSignal(
                signalCode = "COMBINED_PUBLIC_AUTHORITY_THREAT",
                title = "Patrón: organismo público con amenaza",
                explanation = "Combinación de suplantación de organismo público + urgencia o consecuencia negativa.",
                weight = 10
            )
        }

        // ── Verificación urgente de cuenta ────────────────────────────────────
        // Account verification + (urgency or temporal pressure) + (banking or impersonation)
        if (codes.contains("ACCOUNT_VERIFICATION_REQUEST") &&
            (codes.contains("URGENCY_THREAT") || codes.contains("TEMPORAL_PRESSURE")) &&
            (codes.contains("BANKING_FINANCE_CONTEXT") || codes.contains("IMPERSONATION_KEYWORDS"))
        ) {
            result += DetectedSignal(
                signalCode = "COMBINED_URGENT_ACCOUNT_VERIFICATION",
                title = "Patrón: verificación de cuenta urgente",
                explanation = "Combinación de solicitud de verificación + presión temporal + contexto bancario o de entidad.",
                weight = 10
            )
        }

        // ── Spoofing de marca con enlace ──────────────────────────────────────
        // Brand spoofing (visual or deceptive subdomain) + any URL
        if ((codes.contains("HOMOGLYPH_BRAND_SPOOFING") || codes.contains("URL_DECEPTIVE_SUBDOMAIN")) && hasUrl) {
            result += DetectedSignal(
                signalCode = "COMBINED_BRAND_SPOOFING_WITH_LINK",
                title = "Patrón: suplantación de marca con enlace",
                explanation = "Se detectó imitación visual de una marca conocida junto a un enlace: riesgo elevado.",
                weight = 12
            )
        }

        // ── Credenciales + enlace ─────────────────────────────────────────────
        if (codes.contains("SENSITIVE_DATA_REQUEST") && hasUrl) {
            result += DetectedSignal(
                signalCode = "COMBINED_CREDENTIALS_WITH_LINK",
                title = "Patrón: credenciales con enlace",
                explanation = "Se solicitan datos sensibles (contraseña, PIN, CVV…) junto a un enlace: patrón de phishing directo.",
                weight = 10
            )
        }

        // ── Homoglifos + acción crítica ────────────────────────────────────────
        if (codes.contains("HOMOGLYPH_BRAND_SPOOFING") &&
            (codes.contains("ACCOUNT_VERIFICATION_REQUEST") || codes.contains("SENSITIVE_DATA_REQUEST") ||
                codes.contains("NEGATIVE_CONSEQUENCE"))
        ) {
            result += DetectedSignal(
                signalCode = "COMBINED_HOMOGLYPH_WITH_CRITICAL_ACTION",
                title = "Patrón: homoglifo de marca + acción crítica",
                explanation = "Suplantación visual de marca combinada con solicitud de datos o amenaza: riesgo muy elevado.",
                weight = 12
            )
        }

        return result
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  RECOMENDACIONES
    // ══════════════════════════════════════════════════════════════════════════

    private fun buildRecommendationCodes(signals: List<DetectedSignal>, score: Int): List<String> {
        val codes = LinkedHashSet<String>()
        signals.forEach { signal ->
            when (signal.signalCode) {
                // ── Reglas semánticas individuales ────────────────────────────
                "URGENCY_THREAT" ->
                    codes.addAll(listOf("REC_DO_NOT_ACT_FAST", "REC_VERIFY_OFFICIAL_CHANNEL"))
                "TEMPORAL_PRESSURE" ->
                    codes.add("REC_DO_NOT_ACT_FAST")
                "NEGATIVE_CONSEQUENCE" ->
                    codes.add("REC_VERIFY_OFFICIAL_CHANNEL")
                "ACCOUNT_VERIFICATION_REQUEST" ->
                    codes.addAll(listOf("REC_DO_NOT_SHARE_CREDENTIALS", "REC_VERIFY_OFFICIAL_CHANNEL"))
                "MICRO_PAYMENT_REQUEST" ->
                    codes.addAll(listOf("REC_DO_NOT_SHARE_CREDENTIALS", "REC_VERIFY_OFFICIAL_CHANNEL"))
                "LOGISTICS_DELIVERY_CONTEXT" ->
                    codes.add("REC_VERIFY_OFFICIAL_CHANNEL")
                "BANKING_FINANCE_CONTEXT" ->
                    codes.add("REC_CALL_OFFICIAL_NUMBER")
                "PUBLIC_AUTHORITY_CONTEXT" ->
                    codes.add("REC_CALL_OFFICIAL_NUMBER")
                "TECH_SUPPORT_THREAT" ->
                    codes.addAll(listOf("REC_DO_NOT_SHARE_CREDENTIALS", "REC_VERIFY_OFFICIAL_CHANNEL"))
                "PRIZE_GIFT" ->
                    codes.add("REC_IGNORE_PRIZE_SCAM")
                "SENSITIVE_DATA_REQUEST" ->
                    codes.add("REC_DO_NOT_SHARE_CREDENTIALS")
                "IMPERSONATION_KEYWORDS" ->
                    codes.add("REC_CALL_OFFICIAL_NUMBER")
                "HOMOGLYPH_BRAND_SPOOFING" ->
                    codes.addAll(listOf("REC_VERIFY_DOMAIN", "REC_CALL_OFFICIAL_NUMBER"))

                // ── Reglas de URL ─────────────────────────────────────────────
                "URL_HTTP_INSECURE" ->
                    codes.add("REC_AVOID_NON_HTTPS")
                "URL_SHORTENER" ->
                    codes.add("REC_EXPAND_SHORT_URL")
                "URL_DOMAIN_PATTERN", "URL_SUSPICIOUS_TLD",
                "URL_AT_SYMBOL", "URL_DECEPTIVE_SUBDOMAIN" ->
                    codes.add("REC_VERIFY_DOMAIN")
                "URL_SCHEME_NON_HTTP", "URL_SCHEME_INTENT",
                "URL_SCHEME_SCRIPT_OR_DATA" ->
                    codes.add("REC_AVOID_DEEP_LINKS")
                "URL_EXECUTABLE_EXTENSION" ->
                    codes.add("REC_DO_NOT_INSTALL_FILES")
                "URL_HOST_IS_IP", "URL_PUNYCODE_OR_NONASCII",
                "URL_MIXED_SCRIPTS", "URL_NESTED_URL_PARAMETER" ->
                    codes.add("REC_VERIFY_DOMAIN")
                "URL_TOO_LONG", "URL_MANY_PARAMS",
                "URL_HIGH_ENTROPY_TOKEN" ->
                    codes.add("REC_REVIEW_URL_CAREFULLY")

                // ── Señales de combinación ─────────────────────────────────────
                "COMBINED_BANK_ACCOUNT_TAKEOVER" ->
                    codes.addAll(listOf("REC_DO_NOT_ACT_FAST", "REC_CALL_OFFICIAL_NUMBER"))
                "COMBINED_DELIVERY_FEE_PATTERN" ->
                    codes.addAll(listOf("REC_DO_NOT_SHARE_CREDENTIALS", "REC_VERIFY_OFFICIAL_CHANNEL"))
                "COMBINED_PUBLIC_AUTHORITY_THREAT" ->
                    codes.addAll(listOf("REC_VERIFY_OFFICIAL_CHANNEL", "REC_DO_NOT_ACT_FAST"))
                "COMBINED_URGENT_ACCOUNT_VERIFICATION" ->
                    codes.addAll(listOf("REC_DO_NOT_ACT_FAST", "REC_VERIFY_OFFICIAL_CHANNEL"))
                "COMBINED_BRAND_SPOOFING_WITH_LINK" ->
                    codes.addAll(listOf("REC_VERIFY_DOMAIN", "REC_VERIFY_OFFICIAL_CHANNEL"))
                "COMBINED_CREDENTIALS_WITH_LINK" ->
                    codes.addAll(listOf("REC_DO_NOT_SHARE_CREDENTIALS", "REC_VERIFY_DOMAIN"))
                "COMBINED_HOMOGLYPH_WITH_CRITICAL_ACTION" ->
                    codes.addAll(listOf("REC_VERIFY_DOMAIN", "REC_CALL_OFFICIAL_NUMBER"))

                // TRUSTED_DOMAIN_BONUS: no genera recomendación propia
            }
        }

        // Recomendaciones globales según nivel de riesgo
        when {
            score >= configuredYellowThreshold ->
                codes.addAll(listOf("REC_BLOCK_CONTACT", "REC_VERIFY_OFFICIAL_CHANNEL"))
            score in configuredGreenThreshold until configuredYellowThreshold ->
                codes.add("REC_VERIFY_OFFICIAL_CHANNEL")
            else ->
                codes.add("REC_STAY_ALERT")
        }
        return codes.toList()
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  EXTRACCIÓN DE URLs
    // ══════════════════════════════════════════════════════════════════════════

    private fun extractUrls(input: String): List<String> {
        return urlRegex.findAll(input)
            .map { match -> match.groupValues[1].trimEnd('.', ',', ';', ':', '!', '?') }
            .filter { it.isNotBlank() }
            .toList()
    }

    private companion object {
        private val urlRegex = Regex(
            """(?i)\b((?:https?://|www\.|intent://|javascript:|data:|mailto:|tel:|sms:|market://|file://|content://)[^\s<>"']+)"""
        )
    }
}

package com.estef.antiphishingcoach.presentation.analyze

import com.estef.antiphishingcoach.core.model.TrafficLight
import com.estef.antiphishingcoach.domain.model.DetectedSignal
import com.estef.antiphishingcoach.domain.model.RecommendationItem

data class AnalyzeActionPlan(
    val steps: List<String>,
    val showOfficialResources: Boolean
)

/**
 * Construye un plan de acción inmediato a partir del nivel de riesgo, las señales
 * detectadas y las recomendaciones calculadas por el dominio.
 */
object AnalyzeActionPlanBuilder {

    fun build(
        trafficLight: TrafficLight,
        signals: List<DetectedSignal>,
        recommendations: List<RecommendationItem>
    ): AnalyzeActionPlan {
        val signalCodes = signals.map { it.signalCode }.toSet()
        val recommendationCodes = recommendations.map { it.code }.toSet()
        // linkedSetOf conserva el orden de lectura y evita repetir pasos equivalentes.
        val steps = linkedSetOf<String>()

        when (trafficLight) {
            TrafficLight.RED -> {
                steps += "No abras el enlace ni respondas al mensaje hasta verificarlo."
            }

            TrafficLight.YELLOW -> {
                steps += "Pausa antes de actuar y verifica el contexto por otra via."
            }

            TrafficLight.GREEN -> {
                steps += "No se ve riesgo alto, pero mantente alerta antes de compartir datos."
            }
        }

        if ("SENSITIVE_DATA_REQUEST" in signalCodes || "REC_DO_NOT_SHARE_CREDENTIALS" in recommendationCodes) {
            steps += "No compartas contrasenas, PIN, OTP ni datos bancarios."
        }

        if (
            "URL_SHORTENER" in signalCodes ||
            "URL_DECEPTIVE_SUBDOMAIN" in signalCodes ||
            "URL_AT_SYMBOL" in signalCodes ||
            "URL_DOMAIN_PATTERN" in signalCodes ||
            "URL_SUSPICIOUS_TLD" in signalCodes ||
            "REC_VERIFY_DOMAIN" in recommendationCodes ||
            "REC_REVIEW_URL_CAREFULLY" in recommendationCodes
        ) {
            steps += "Comprueba el dominio real completo antes de abrir el enlace."
        }

        if (
            "URL_SCHEME_NON_HTTP" in signalCodes ||
            "URL_SCHEME_INTENT" in signalCodes ||
            "URL_SCHEME_SCRIPT_OR_DATA" in signalCodes ||
            "URL_EXECUTABLE_EXTENSION" in signalCodes ||
            "REC_AVOID_DEEP_LINKS" in recommendationCodes ||
            "REC_DO_NOT_INSTALL_FILES" in recommendationCodes
        ) {
            steps += "No instales archivos ni abras enlaces que lancen otras aplicaciones."
        }

        if ("REC_VERIFY_OFFICIAL_CHANNEL" in recommendationCodes || "REC_CALL_OFFICIAL_NUMBER" in recommendationCodes) {
            steps += "Verifica con la entidad desde su web, app o telefono oficial."
        }

        if (trafficLight == TrafficLight.RED || "REC_BLOCK_CONTACT" in recommendationCodes) {
            steps += "Bloquea el remitente y guarda evidencia si vas a reportarlo."
        }

        if (steps.isEmpty()) {
            steps += "Revisa remitente, contexto y canal oficial antes de actuar."
        }

        return AnalyzeActionPlan(
            steps = steps.toList(),
            showOfficialResources = trafficLight != TrafficLight.GREEN ||
                "REC_VERIFY_OFFICIAL_CHANNEL" in recommendationCodes ||
                "REC_CALL_OFFICIAL_NUMBER" in recommendationCodes
        )
    }
}

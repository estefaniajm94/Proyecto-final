package com.estef.antiphishingcoach.domain.model

/**
 * Recomendaciones locales (por codigo) para mantener trazabilidad del analisis.
 */
object RecommendationCatalog {

    private val catalog = mapOf(
        "REC_VERIFY_OFFICIAL_CHANNEL" to RecommendationItem(
            code = "REC_VERIFY_OFFICIAL_CHANNEL",
            title = "Verifica por canal oficial",
            detail = "Contacta por telefono o app oficial antes de actuar."
        ),
        "REC_DO_NOT_ACT_FAST" to RecommendationItem(
            code = "REC_DO_NOT_ACT_FAST",
            title = "No actues con prisa",
            detail = "La urgencia forzada es una tactica comun de fraude."
        ),
        "REC_IGNORE_PRIZE_SCAM" to RecommendationItem(
            code = "REC_IGNORE_PRIZE_SCAM",
            title = "Desconfia de premios inesperados",
            detail = "No pagues tasas ni compartas datos para reclamar premios."
        ),
        "REC_DO_NOT_SHARE_CREDENTIALS" to RecommendationItem(
            code = "REC_DO_NOT_SHARE_CREDENTIALS",
            title = "No compartas credenciales",
            detail = "Ninguna entidad legitima pide PIN, OTP o contrasena por chat."
        ),
        "REC_AVOID_NON_HTTPS" to RecommendationItem(
            code = "REC_AVOID_NON_HTTPS",
            title = "Evita enlaces HTTP",
            detail = "Prioriza solo HTTPS y revisa el dominio completo."
        ),
        "REC_AVOID_DEEP_LINKS" to RecommendationItem(
            code = "REC_AVOID_DEEP_LINKS",
            title = "Evita deep links sospechosos",
            detail = "No abras enlaces que lancen apps o acciones externas sin validar origen."
        ),
        "REC_EXPAND_SHORT_URL" to RecommendationItem(
            code = "REC_EXPAND_SHORT_URL",
            title = "Cuidado con enlaces acortados",
            detail = "No abras acortadores sin verificar destino real."
        ),
        "REC_DO_NOT_INSTALL_FILES" to RecommendationItem(
            code = "REC_DO_NOT_INSTALL_FILES",
            title = "No instales archivos",
            detail = "Evita descargar o ejecutar APK/EXE/MSI/PKG desde enlaces no verificados."
        ),
        "REC_VERIFY_DOMAIN" to RecommendationItem(
            code = "REC_VERIFY_DOMAIN",
            title = "Comprueba el dominio",
            detail = "Revisa TLD, guiones, numeros y subdominios sospechosos."
        ),
        "REC_REVIEW_URL_CAREFULLY" to RecommendationItem(
            code = "REC_REVIEW_URL_CAREFULLY",
            title = "Revisa la URL completa",
            detail = "Comprueba longitud, parametros y tokens antes de abrir enlaces."
        ),
        "REC_CALL_OFFICIAL_NUMBER" to RecommendationItem(
            code = "REC_CALL_OFFICIAL_NUMBER",
            title = "Llama al numero oficial",
            detail = "Si suplantan banco/administracion, corta y llama al canal oficial."
        ),
        "REC_BLOCK_CONTACT" to RecommendationItem(
            code = "REC_BLOCK_CONTACT",
            title = "Bloquea y reporta",
            detail = "Bloquea el contacto y reporta el intento de fraude."
        ),
        "REC_STAY_ALERT" to RecommendationItem(
            code = "REC_STAY_ALERT",
            title = "Mantente alerta",
            detail = "Aunque el riesgo sea bajo, verifica siempre antes de compartir datos."
        )
    )

    fun fromCodes(codes: List<String>): List<RecommendationItem> {
        return codes.mapNotNull { code -> catalog[code] }
    }
}

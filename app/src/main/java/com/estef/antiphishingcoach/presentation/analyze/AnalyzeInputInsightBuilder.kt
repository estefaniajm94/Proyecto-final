package com.estef.antiphishingcoach.presentation.analyze

import com.estef.antiphishingcoach.domain.heuristics.ParsedUrl
import com.estef.antiphishingcoach.domain.heuristics.TextNormalizer
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

    /**
     * Grupos semánticos para detectar frases sospechosas en el texto visible.
     * Sincronizado con las familias de [DefaultHeuristicRules] para coherencia.
     * Las comparaciones se hacen contra el texto normalizado (sin tildes, minúsculas).
     */
    private val suspiciousPhraseGroups = listOf(
        PhraseGroup(
            category = "Urgencia",
            keywords = listOf(
                "ultimo aviso", "urgente", "inmediato", "inmediatamente",
                "actue ahora", "actua ahora", "de inmediato", "ahora mismo",
                "en 24 horas", "en 48 horas", "caduca hoy", "expira hoy",
                "accion requerida", "accion inmediata", "plazo improrrogable",
                "de lo contrario", "en caso contrario", "si no actua"
            )
        ),
        PhraseGroup(
            category = "Consecuencia negativa",
            keywords = listOf(
                "bloqueada", "bloqueado", "suspendida", "suspendido", "suspension",
                "cancelada", "cancelado", "restringida", "acceso limitado", "limitada",
                "sancion", "penalizacion", "cierre de cuenta",
                "perdera el acceso", "inhabilitada", "desactivada",
                "sera bloqueada", "quedara suspendida"
            )
        ),
        PhraseGroup(
            category = "Verificación de cuenta",
            keywords = listOf(
                "verifique su identidad", "verifica tu identidad",
                "valide su cuenta", "validar cuenta", "validacion de cuenta",
                "confirme sus datos", "confirmar datos",
                "actualice sus datos", "actualice la direccion",
                "reactive su cuenta", "confirmacion de identidad",
                "inicie sesion", "iniciar sesion", "complete la verificacion"
            )
        ),
        PhraseGroup(
            category = "Datos sensibles",
            keywords = listOf(
                "contrasena", "password", "pin", "tarjeta", "codigo", "verificacion",
                "otp", "cvv", "cvc", "numero de tarjeta", "datos bancarios",
                "clave de acceso", "iban", "token de seguridad", "codigo sms"
            )
        ),
        PhraseGroup(
            category = "Suplantación de entidad",
            keywords = listOf(
                "banco", "cuenta", "hacienda", "agencia tributaria", "seguridad social",
                "correos", "paqueteria", "mensajeria", "paquete", "pedido",
                "dgt", "bankinter", "unicaja"
            )
        ),
        PhraseGroup(
            category = "Paquetería / Logística",
            keywords = listOf(
                "no hemos podido entregar", "entrega fallida", "incidencia de entrega",
                "reprogramar entrega", "su paquete", "su pedido", "su envio",
                "numero de seguimiento", "paquete retenido", "datos de entrega"
            )
        ),
        PhraseGroup(
            category = "Banca / Finanzas",
            keywords = listOf(
                "acceso inusual", "actividad sospechosa", "movimiento inusual",
                "operacion retenida", "desbloquear cuenta", "banca online",
                "transferencia pendiente", "hemos detectado un acceso",
                "para evitar la suspension", "inicie sesion para evitar"
            )
        ),
        PhraseGroup(
            category = "Micro-pago / Tasa",
            keywords = listOf(
                "abone", "0,99", "1,99", "2,99", "gastos de envio", "gastos de aduana",
                "tasa de", "coste de gestion", "pago pendiente", "importe pendiente",
                "derechos de aduana", "pequeno importe"
            )
        ),
        PhraseGroup(
            category = "Organismo público",
            keywords = listOf(
                "agencia tributaria", "hacienda", "seguridad social", "dgt",
                "expediente", "notificacion oficial", "multa de trafico",
                "sancion de trafico", "declaracion de la renta", "revision fiscal"
            )
        ),
        PhraseGroup(
            category = "Soporte técnico falso",
            keywords = listOf(
                "dispositivo comprometido", "acceso no autorizado", "sesion comprometida",
                "virus detectado", "soporte tecnico", "cuenta comprometida",
                "alerta de seguridad", "restablezca el acceso"
            )
        ),
        PhraseGroup(
            category = "Premio o gancho",
            keywords = listOf(
                "has ganado", "ha ganado", "sorteo", "regalo", "premio",
                "ganador", "cupon", "bono", "recompensa", "reembolso extraordinario",
                "oferta exclusiva", "reclamar su premio"
            )
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
                add("Incluye otra URL dentro de los parámetros.")
            }
            if (parsedUrl.paramCount >= 5) add("Tiene muchos parámetros para revisar.")
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
        val normalizedInput = TextNormalizer.normalize(input)
        return suspiciousPhraseGroups.flatMap { group ->
            group.keywords.mapNotNull { keyword ->
                if (normalizedInput.contains(keyword, ignoreCase = false)) {
                    SuspiciousPhraseInsight(phrase = keyword, category = group.category)
                } else null
            }
        }.distinctBy { it.phrase }
    }

    private fun buildQuickExplanation(
        urlInsights: List<AnalyzeUrlInsight>,
        suspiciousPhrases: List<SuspiciousPhraseInsight>
    ): String = when {
        urlInsights.any { it.observations.isNotEmpty() } ->
            "La alerta principal está en el enlace: revisa el dominio real y las observaciones técnicas."
        suspiciousPhrases.any { it.category == "Datos sensibles" || it.category == "Urgencia" } ->
            "La alerta principal está en el mensaje: intenta presionar o pedir información sensible."
        urlInsights.isNotEmpty() ->
            "Se ha detectado un enlace. Comprueba que el dominio real coincide con la entidad que aparenta ser."
        suspiciousPhrases.isNotEmpty() ->
            "El texto contiene frases típicas de fraude. Conviene verificar el remitente y el contexto."
        else ->
            "No se ven patrones técnicos fuertes, pero siempre conviene verificar remitente, contexto y canal oficial."
    }

    private data class PhraseGroup(val category: String, val keywords: List<String>)
}

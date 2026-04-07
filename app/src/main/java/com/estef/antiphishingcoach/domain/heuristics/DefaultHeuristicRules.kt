package com.estef.antiphishingcoach.domain.heuristics

import com.estef.antiphishingcoach.domain.model.DetectedSignal

/**
 * Reglas heurísticas transparentes.
 *
 * Calibración de pesos:
 * - Alto (25-40):  acciones que abren apps, ejecutan código o descargan ejecutables.
 * - Medio (14-24): contexto semántico fuerte o suplantación técnica.
 * - Bajo (10-13):  señal acumulable (una sola no basta para subir el riesgo).
 * - Negativo (-10): factor moderador (dominio oficial reconocido).
 *
 * Todas las comparaciones de texto se hacen contra [HeuristicContext.normalizedInput],
 * que ya viene en minúsculas, sin tildes y con espacios colapsados (ver TextNormalizer).
 */
object DefaultHeuristicRules {

    // ── Catálogos de URLs ─────────────────────────────────────────────────────

    val defaultSuspiciousTlds = setOf(
        "xyz", "top", "click", "gq", "tk", "work", "zip", "mov"
    )

    val defaultShortenerDomains = setOf(
        "bit.ly", "tinyurl.com", "t.co", "goo.gl", "cutt.ly", "ow.ly", "is.gd", "rb.gy"
    )

    /**
     * Palabras clave de impersonación: mencionan entidades o contextos que suelen suplantarse.
     * Ampliado para incluir más marcas y conceptos frecuentes en smishing.
     */
    val defaultImpersonationKeywords = listOf(
        "banco", "cuenta", "hacienda", "agencia tributaria", "seguridad social",
        "correos", "paqueteria", "mensajeria", "paquete", "pedido",
        "dgt", "direccion general de trafico", "bankinter", "unicaja"
    )

    /**
     * Marcas conocidas para detectar subdominios engañosos en URLs.
     * Ampliado con más entidades financieras y de paquetería.
     */
    val defaultBrandKeywords = listOf(
        "santander", "bbva", "caixabank", "correos", "dhl", "amazon",
        "hacienda", "seg-social", "bankinter", "unicaja", "seur", "mrw",
        "ups", "fedex", "paypal", "tributaria", "agenciatributaria"
    )

    /**
     * Dominios oficiales para excluir de [deceptiveSubdomainRule].
     * Alineado con [TrustedDomainCatalog] para coherencia.
     */
    val defaultOfficialDomains = listOf(
        // Banca
        "bancosantander.es", "santander.es",
        "bbva.com", "bbva.es",
        "caixabank.es",
        "bankinter.com",
        "unicajabanco.es",
        // Paquetería
        "correos.es", "seur.com", "mrw.es", "dhl.com", "ups.com", "fedex.com", "gls-spain.es",
        // Administración
        "agenciatributaria.gob.es", "seg-social.gob.es", "dgt.es",
        "administracion.gob.es", "060.es",
        // Retail
        "amazon.es", "amazon.com", "elcorteingles.es", "pccomponentes.com"
    )

    // ── Constantes internas ───────────────────────────────────────────────────

    private val nonHttpSchemes = setOf("tel", "sms", "mailto", "market", "file", "content")
    private val scriptOrDataSchemes = setOf("javascript", "data")
    private val executableExtensions = setOf(
        "apk", "exe", "msi", "dmg", "pkg", "jar", "js", "scr", "bat", "ps1"
    )
    private const val longUrlThreshold = 140
    private const val manyParamsThreshold = 8
    private const val highEntropyMinLength = 24
    private val hexTokenRegex = Regex("^[A-Fa-f0-9]{24,}$")
    private val base64LikeTokenRegex = Regex("^[A-Za-z0-9+/=_-]{24,}$")

    // ── Señales fuertes de URL que impiden aplicar el moderador de dominio confiable ──

    /** Señales técnicas de URL que indican riesgo alto independientemente del dominio. */
    val hardOverrideSignals = setOf(
        "URL_SCHEME_INTENT",
        "URL_SCHEME_SCRIPT_OR_DATA",
        "URL_EXECUTABLE_EXTENSION",
        "URL_HOST_IS_IP",
        "URL_MIXED_SCRIPTS",
        "URL_PUNYCODE_OR_NONASCII",
        "URL_NESTED_URL_PARAMETER",
        "URL_AT_SYMBOL",
        "HOMOGLYPH_BRAND_SPOOFING"
    )

    // ── Constructor de reglas ─────────────────────────────────────────────────

    fun build(
        suspiciousTlds: Set<String> = defaultSuspiciousTlds,
        shortenerDomains: Set<String> = defaultShortenerDomains,
        impersonationKeywords: List<String> = defaultImpersonationKeywords,
        brandKeywords: List<String> = defaultBrandKeywords,
        officialDomains: List<String> = defaultOfficialDomains
    ): List<HeuristicRule> = listOf(

        // ── Reglas semánticas (texto) ─────────────────────────────────────────
        urgencyThreatRule(),
        temporalPressureRule(),
        negativeConsequenceRule(),
        accountVerificationRequestRule(),
        microPaymentRule(),
        logisticsDeliveryRule(),
        bankingFinanceContextRule(),
        publicAuthorityContextRule(),
        techSupportThreatRule(),
        prizeGiftRule(),
        sensitiveDataRule(),
        impersonationKeywordRule(impersonationKeywords),

        // ── Detección de homoglifos ───────────────────────────────────────────
        homoglyphBrandRule(),

        // ── Reglas de URL ─────────────────────────────────────────────────────
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

        // ── Moderador de dominio confiable (peso negativo) ────────────────────
        trustedDomainModeratorRule()
    )

    // ══════════════════════════════════════════════════════════════════════════
    //  REGLAS SEMÁNTICAS — TEXTO
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * A) Urgencia inmediata: palabras cortas/directas que presionan para actuar ya.
     * Peso moderado; en combinación con otras señales sube rápido el score.
     */
    private fun urgencyThreatRule(): HeuristicRule = HeuristicRule { ctx ->
        val keywords = listOf(
            "ultimo aviso", "bloqueo", "multa", "urgente", "inmediato", "inmediatamente",
            "actue ya", "actua ya", "actue ahora", "actua ahora",
            "no espere", "responda ahora", "ahora mismo", "de inmediato"
        )
        if (containsAny(ctx.normalizedInput, keywords)) DetectedSignal(
            signalCode = "URGENCY_THREAT",
            title = "Urgencia o amenaza",
            explanation = "El mensaje presiona para actuar de inmediato.",
            weight = 14
        ) else null
    }

    /**
     * A) Presión temporal con plazo concreto: complementa URGENCY_THREAT con frases
     * orientadas a un límite de tiempo específico.
     */
    private fun temporalPressureRule(): HeuristicRule = HeuristicRule { ctx ->
        val keywords = listOf(
            "en 24 horas", "en 48 horas", "en 72 horas",
            "caduca hoy", "expira hoy", "vence hoy", "caduca manana",
            "ultimo plazo", "plazo improrrogable", "plazo vencido", "tiempo agotado",
            "accion requerida", "accion inmediata", "respuesta inmediata",
            "antes de que", "sin demora", "a la mayor brevedad",
            "horas restantes", "si no actua", "si no responde", "de lo contrario",
            "en caso contrario", "a partir de hoy", "hoy es el ultimo"
        )
        if (containsAny(ctx.normalizedInput, keywords)) DetectedSignal(
            signalCode = "TEMPORAL_PRESSURE",
            title = "Presión temporal con plazo",
            explanation = "El mensaje impone un plazo concreto para presionar a actuar sin reflexionar.",
            weight = 12
        ) else null
    }

    /**
     * B) Amenaza o consecuencia negativa: bloqueos, suspensiones, sanciones.
     */
    private fun negativeConsequenceRule(): HeuristicRule = HeuristicRule { ctx ->
        val keywords = listOf(
            "bloqueada", "bloqueado", "ha sido bloqueada", "ha sido bloqueado",
            "suspendida", "suspendido", "suspension", "ha sido suspendida",
            "cancelada", "cancelado", "cancelacion",
            "restringida", "restringido", "restriccion",
            "acceso limitado", "limitada", "limitado", "cuenta limitada",
            "sancion", "penalizacion", "expediente sancionador",
            "cierre de cuenta", "clausura",
            "perdera el acceso", "perderas el acceso", "perder el acceso",
            "inhabilitada", "inhabilitado", "desactivada", "desactivado",
            "su cuenta sera", "tu cuenta sera", "sera eliminada", "sera bloqueada",
            "quedara suspendida", "quedara bloqueada"
        )
        if (containsAny(ctx.normalizedInput, keywords)) DetectedSignal(
            signalCode = "NEGATIVE_CONSEQUENCE",
            title = "Amenaza de consecuencia negativa",
            explanation = "El mensaje amenaza con bloquear, suspender o cancelar una cuenta si no se actúa.",
            weight = 14
        ) else null
    }

    /**
     * C) Verificación / validación de cuenta: solicitudes de confirmar identidad o datos.
     */
    private fun accountVerificationRequestRule(): HeuristicRule = HeuristicRule { ctx ->
        val keywords = listOf(
            "verifique su identidad", "verifica tu identidad",
            "verifique su cuenta", "verifica tu cuenta", "verificar identidad",
            "valide su cuenta", "valida tu cuenta", "validar cuenta", "validacion de cuenta",
            "confirme sus datos", "confirma tus datos", "confirmar datos",
            "actualice sus datos", "actualiza tus datos", "actualizar datos",
            "actualice la direccion", "actualiza la direccion",
            "reactive su cuenta", "reactiva tu cuenta", "reactivar cuenta",
            "confirmacion de identidad", "verificar acceso",
            "inicie sesion", "inicia sesion", "iniciar sesion",
            "acceda a su cuenta", "accede a tu cuenta",
            "complete la verificacion", "completar la validacion",
            "verifique su identidad en", "si no completa"
        )
        if (containsAny(ctx.normalizedInput, keywords)) DetectedSignal(
            signalCode = "ACCOUNT_VERIFICATION_REQUEST",
            title = "Solicitud de verificación de cuenta",
            explanation = "El mensaje solicita verificar identidad, validar cuenta o iniciar sesión.",
            weight = 16
        ) else null
    }

    /**
     * E) Micro-pago / tasa / coste pequeño: señal clásica de smishing de paquetería o deuda.
     */
    private fun microPaymentRule(): HeuristicRule = HeuristicRule { ctx ->
        val keywords = listOf(
            "0,99", "1,99", "2,99", "3,99", "4,99",
            "0.99", "1.99", "2.99", "3.99", "4.99",
            "gastos de envio", "gastos de aduana", "gastos de gestion", "gastos de tramitacion",
            "coste de gestion", "coste de envio", "coste de tramitacion",
            "tasa de", "tasa postal", "tarifa de",
            "derechos de aduana", "impuesto de aduana", "pago de aduana",
            "recargo de", "cargo pendiente", "importe pendiente",
            "abone", "abonar el importe", "pago de",
            "pago pendiente", "canon de", "pequeño importe"
        )
        if (containsAny(ctx.normalizedInput, keywords)) DetectedSignal(
            signalCode = "MICRO_PAYMENT_REQUEST",
            title = "Solicitud de micro-pago o tasa",
            explanation = "El mensaje pide abonar un pequeño importe (tasa, porte, aduanas), táctica típica de smishing.",
            weight = 12
        ) else null
    }

    /**
     * F) Logística / paquetería: contexto de envíos fallidos o pendientes.
     */
    private fun logisticsDeliveryRule(): HeuristicRule = HeuristicRule { ctx ->
        val keywords = listOf(
            "no hemos podido entregar", "no pudimos entregar", "entrega fallida",
            "incidencia de entrega", "problema con su entrega", "entrega pendiente",
            "reprogramar entrega", "nueva fecha de entrega",
            "su paquete", "su pedido", "su envio",
            "seguimiento de su paquete", "numero de seguimiento",
            "direccion de entrega", "datos de entrega", "datos incompletos",
            "paquete retenido", "paquete en aduana", "paquete en espera",
            "estado de su pedido", "estado de su envio"
        )
        if (containsAny(ctx.normalizedInput, keywords)) DetectedSignal(
            signalCode = "LOGISTICS_DELIVERY_CONTEXT",
            title = "Contexto de paquetería/logística",
            explanation = "El mensaje usa lenguaje de entrega de paquetes, habitual en smishing de mensajería.",
            weight = 10
        ) else null
    }

    /**
     * G) Banca / finanzas: contexto de operaciones bancarias sospechosas.
     */
    private fun bankingFinanceContextRule(): HeuristicRule = HeuristicRule { ctx ->
        val keywords = listOf(
            "acceso inusual", "actividad sospechosa", "acceso no reconocido",
            "movimiento inusual", "movimiento sospechoso",
            "operacion inusual", "operacion no reconocida", "operacion retenida", "operacion bloqueada",
            "desbloquear cuenta", "desbloqueo de cuenta",
            "banca online", "banco online", "banca electronica",
            "transferencia pendiente", "operacion pendiente",
            "entidad bancaria", "su banco le informa",
            "acceso a su cuenta", "acceder a su cuenta",
            "hemos detectado un acceso", "acceso sospechoso",
            "movimiento no reconocido", "cargo no reconocido",
            "para evitar la suspension", "para evitar el bloqueo",
            "inicie sesion para evitar"
        )
        if (containsAny(ctx.normalizedInput, keywords)) DetectedSignal(
            signalCode = "BANKING_FINANCE_CONTEXT",
            title = "Contexto bancario/financiero sospechoso",
            explanation = "El mensaje usa terminología de acceso inusual o alertas bancarias.",
            weight = 14
        ) else null
    }

    /**
     * H) Administración pública: suplantación de hacienda, DGT, TGSS, etc.
     */
    private fun publicAuthorityContextRule(): HeuristicRule = HeuristicRule { ctx ->
        val keywords = listOf(
            "agencia tributaria", "hacienda", "seguridad social", "tgss",
            "dgt", "trafico", "direccion general de trafico",
            "expediente", "numero de expediente", "referencia del expediente",
            "notificacion oficial", "notificacion administrativa",
            "declaracion de la renta", "devolucion fiscal", "reembolso fiscal",
            "multa de trafico", "sancion de trafico", "infraccion de trafico",
            "boe", "carpeta ciudadana", "clave pin", "cl@ve",
            "administracion publica", "ministerio", "delegacion de hacienda",
            "revision fiscal", "inspeccion fiscal", "regularizacion fiscal"
        )
        if (containsAny(ctx.normalizedInput, keywords)) DetectedSignal(
            signalCode = "PUBLIC_AUTHORITY_CONTEXT",
            title = "Suplantación de organismo público",
            explanation = "El mensaje menciona organismos de la administración pública (Hacienda, DGT, TGSS…).",
            weight = 14
        ) else null
    }

    /**
     * I) Soporte / seguridad técnica: mensajes de supuesto soporte que alertan de compromisos.
     */
    private fun techSupportThreatRule(): HeuristicRule = HeuristicRule { ctx ->
        val keywords = listOf(
            "dispositivo comprometido", "tu dispositivo ha sido", "su dispositivo ha sido",
            "acceso no autorizado",
            "sesion comprometida", "sesion iniciada desde",
            "virus detectado", "malware detectado", "amenaza detectada",
            "soporte tecnico", "servicio tecnico", "equipo de seguridad",
            "restablezca el acceso", "restablecer contrasena", "restablecer acceso",
            "cuenta comprometida", "cuenta hackeada", "cuenta vulnerada",
            "alerta de seguridad", "aviso de seguridad", "su cuenta ha sido accedida"
        )
        if (containsAny(ctx.normalizedInput, keywords)) DetectedSignal(
            signalCode = "TECH_SUPPORT_THREAT",
            title = "Amenaza de soporte técnico falso",
            explanation = "El mensaje alerta de un supuesto compromiso técnico para inducir una acción urgente.",
            weight = 14
        ) else null
    }

    /**
     * J) Premio / promoción sospechosa. Ampliado respecto al antiguo prizeGiftRule.
     */
    private fun prizeGiftRule(): HeuristicRule = HeuristicRule { ctx ->
        val keywords = listOf(
            "has ganado", "ha ganado", "sorteo", "regalo", "premio",
            "ganador", "ganadora", "has sido seleccionado", "ha sido seleccionado",
            "cupon", "bono", "recompensa", "cashback", "reembolso extraordinario",
            "oferta exclusiva", "beneficio exclusivo", "promocion exclusiva",
            "reclamar su premio", "reclamar su regalo", "reclamar su recompensa",
            "loteria", "rifas", "concurso"
        )
        if (containsAny(ctx.normalizedInput, keywords)) DetectedSignal(
            signalCode = "PRIZE_GIFT",
            title = "Premio o regalo sospechoso",
            explanation = "Los premios inesperados son una táctica de engaño frecuente.",
            weight = 14
        ) else null
    }

    /**
     * D) Credenciales y datos sensibles: solicitud directa de información crítica.
     * Ampliado con términos de banca online y autenticación.
     */
    private fun sensitiveDataRule(): HeuristicRule = HeuristicRule { ctx ->
        val keywords = listOf(
            "contrasena", "password", "pin", "tarjeta", "codigo", "verificacion",
            "otp", "cvv", "cvc",
            "numero de tarjeta", "datos bancarios", "datos de pago",
            "clave de acceso", "clave secreta", "numero de cuenta",
            "iban", "bic", "swift",
            "token de seguridad", "codigo sms", "codigo de un solo uso"
        )
        if (containsAny(ctx.normalizedInput, keywords)) DetectedSignal(
            signalCode = "SENSITIVE_DATA_REQUEST",
            title = "Petición de datos sensibles",
            explanation = "Solicita credenciales o datos de pago que no deben compartirse.",
            weight = 24
        ) else null
    }

    /**
     * Suplantación por palabras clave: menciones directas a entidades conocidas.
     */
    private fun impersonationKeywordRule(keywords: List<String>): HeuristicRule = HeuristicRule { ctx ->
        if (containsAny(ctx.normalizedInput, keywords)) DetectedSignal(
            signalCode = "IMPERSONATION_KEYWORDS",
            title = "Posible suplantación de entidad",
            explanation = "Aparecen términos típicos de banco, paquetería o administración.",
            weight = 16
        ) else null
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  DETECCIÓN DE HOMOGLIFOS EN TEXTO Y URL
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Detecta suplantación visual de marcas mediante caracteres Unicode confusables,
     * tanto en el texto visible del mensaje como en los hosts de las URLs.
     */
    private fun homoglyphBrandRule(): HeuristicRule = HeuristicRule { ctx ->
        // 1. Escanear texto visible
        val textSpoof = HomoglyphDetector.detectBrandSpoofing(ctx.rawInput)

        // 2. Escanear hosts de URLs
        val urlSpoof = if (textSpoof == null) {
            ctx.urls.firstNotNullOfOrNull { parsed ->
                HomoglyphDetector.detectBrandSpoofingInHost(parsed.host)
            }
        } else null

        val spoofedBrand = textSpoof ?: urlSpoof
        if (spoofedBrand != null) DetectedSignal(
            signalCode = "HOMOGLYPH_BRAND_SPOOFING",
            title = "Suplantación visual de marca",
            explanation = "Se detectó el nombre de '$spoofedBrand' escrito con caracteres Unicode " +
                "visualmente similares (homoglifos), técnica usada para engañar al usuario.",
            weight = 20
        ) else null
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  REGLAS DE URL
    // ══════════════════════════════════════════════════════════════════════════

    private fun insecureHttpRule(): HeuristicRule = HeuristicRule { ctx ->
        if (ctx.urls.any { it.raw.startsWith("http://", ignoreCase = true) }) DetectedSignal(
            signalCode = "URL_HTTP_INSECURE",
            title = "Enlace sin HTTPS",
            explanation = "El enlace usa HTTP en lugar de HTTPS.",
            weight = 12
        ) else null
    }

    private fun shortenerRule(shortenerDomains: Set<String>): HeuristicRule = HeuristicRule { ctx ->
        if (ctx.urls.any { p -> p.host != null && shortenerDomains.contains(p.host) }) DetectedSignal(
            signalCode = "URL_SHORTENER",
            title = "Acortador de enlace",
            explanation = "Los acortadores ocultan el destino real del enlace.",
            weight = 18
        ) else null
    }

    private fun domainHyphenNumberRule(): HeuristicRule = HeuristicRule { ctx ->
        val found = ctx.urls.any { p ->
            val host = p.host ?: return@any false
            val body = host.substringBeforeLast(".")
            body.count { it == '-' } >= 2 || body.count { it.isDigit() } >= 4
        }
        if (found) DetectedSignal(
            signalCode = "URL_DOMAIN_PATTERN",
            title = "Dominio con patrón sospechoso",
            explanation = "El dominio contiene muchos guiones o números.",
            weight = 12
        ) else null
    }

    private fun suspiciousTldRule(suspiciousTlds: Set<String>): HeuristicRule = HeuristicRule { ctx ->
        if (ctx.urls.any { p -> p.tld != null && suspiciousTlds.contains(p.tld) }) DetectedSignal(
            signalCode = "URL_SUSPICIOUS_TLD",
            title = "TLD de riesgo",
            explanation = "El enlace usa una extensión de dominio asociada con abuso.",
            weight = 14
        ) else null
    }

    private fun atSymbolInUrlRule(): HeuristicRule = HeuristicRule { ctx ->
        if (ctx.urls.any { p -> p.raw.contains("@") }) DetectedSignal(
            signalCode = "URL_AT_SYMBOL",
            title = "Uso de @ en URL",
            explanation = "La presencia de @ en una URL puede ocultar el dominio real.",
            weight = 20
        ) else null
    }

    private fun deceptiveSubdomainRule(
        brandKeywords: List<String>,
        officialDomains: List<String>
    ): HeuristicRule = HeuristicRule { ctx ->
        val found = ctx.urls.any { p ->
            val host = p.host ?: return@any false
            val hasBrand = brandKeywords.any { kw -> host.contains(kw) }
            val isOfficial = officialDomains.any { off -> host == off || host.endsWith(".$off") }
            hasBrand && !isOfficial && host.count { it == '.' } >= 2
        }
        if (found) DetectedSignal(
            signalCode = "URL_DECEPTIVE_SUBDOMAIN",
            title = "Subdominio engañoso",
            explanation = "El dominio incluye el nombre de una marca conocida en un dominio no oficial.",
            weight = 20
        ) else null
    }

    private fun nonHttpSchemeRule(): HeuristicRule = HeuristicRule { ctx ->
        if (ctx.urls.any { p -> nonHttpSchemes.contains(p.scheme) }) DetectedSignal(
            signalCode = "URL_SCHEME_NON_HTTP",
            title = "Esquema no web",
            explanation = "El enlace usa un esquema que puede abrir apps o acciones fuera del navegador.",
            weight = 20
        ) else null
    }

    private fun intentSchemeRule(): HeuristicRule = HeuristicRule { ctx ->
        if (ctx.urls.any { p -> p.scheme == "intent" }) DetectedSignal(
            signalCode = "URL_SCHEME_INTENT",
            title = "Esquema intent detectado",
            explanation = "Los enlaces intent:// pueden lanzar componentes/apps en Android.",
            weight = 32
        ) else null
    }

    private fun scriptOrDataSchemeRule(): HeuristicRule = HeuristicRule { ctx ->
        if (ctx.urls.any { p -> scriptOrDataSchemes.contains(p.scheme) }) DetectedSignal(
            signalCode = "URL_SCHEME_SCRIPT_OR_DATA",
            title = "Esquema script/data",
            explanation = "Los esquemas javascript:/data: pueden ejecutar contenido en el navegador.",
            weight = 34
        ) else null
    }

    private fun executableExtensionRule(): HeuristicRule = HeuristicRule { ctx ->
        val found = ctx.urls.any { p ->
            val path = p.path ?: return@any false
            val ext = path.substringAfterLast("/")
                .substringAfterLast(".", "")
                .substringBefore("?")
                .lowercase()
            executableExtensions.contains(ext)
        }
        if (found) DetectedSignal(
            signalCode = "URL_EXECUTABLE_EXTENSION",
            title = "Archivo ejecutable/instalable",
            explanation = "La URL apunta a un archivo ejecutable o instalable.",
            weight = 30
        ) else null
    }

    private fun hostIsIpRule(): HeuristicRule = HeuristicRule { ctx ->
        if (ctx.urls.any { p -> p.hostIsIp }) DetectedSignal(
            signalCode = "URL_HOST_IS_IP",
            title = "Host en IP",
            explanation = "Los enlaces a IPs en lugar de dominios son comunes en fraudes.",
            weight = 18
        ) else null
    }

    private fun punycodeOrNonAsciiRule(): HeuristicRule = HeuristicRule { ctx ->
        if (ctx.urls.any { p -> p.hostHasPunycode || p.hostHasNonAscii }) DetectedSignal(
            signalCode = "URL_PUNYCODE_OR_NONASCII",
            title = "Dominio visualmente engañoso",
            explanation = "El dominio usa caracteres especiales o codificación IDN, a veces usados para suplantación visual.",
            weight = 16
        ) else null
    }

    private fun mixedScriptsRule(): HeuristicRule = HeuristicRule { ctx ->
        if (ctx.urls.any { p -> p.hostHasMixedScripts }) DetectedSignal(
            signalCode = "URL_MIXED_SCRIPTS",
            title = "Mezcla de alfabetos en dominio",
            explanation = "El dominio mezcla alfabetos para parecerse a otro (técnica de suplantación).",
            weight = 22
        ) else null
    }

    private fun nestedUrlParameterRule(): HeuristicRule = HeuristicRule { ctx ->
        if (ctx.urls.any { p -> UrlNormalizer.hasNestedUrlParameter(p.queryParams) }) DetectedSignal(
            signalCode = "URL_NESTED_URL_PARAMETER",
            title = "URL anidada en parámetros",
            explanation = "El enlace incluye otra URL dentro: típico de redirecciones y ocultación del destino.",
            weight = 18
        ) else null
    }

    private fun urlTooLongRule(): HeuristicRule = HeuristicRule { ctx ->
        if (ctx.urls.any { p -> p.urlLength > longUrlThreshold }) DetectedSignal(
            signalCode = "URL_TOO_LONG",
            title = "URL excesivamente larga",
            explanation = "URLs muy largas pueden ocultar el destino o incluir tokens sospechosos.",
            weight = 10
        ) else null
    }

    private fun manyParametersRule(): HeuristicRule = HeuristicRule { ctx ->
        if (ctx.urls.any { p -> p.paramCount > manyParamsThreshold }) DetectedSignal(
            signalCode = "URL_MANY_PARAMS",
            title = "Muchos parámetros",
            explanation = "Muchos parámetros pueden usarse para rastreo u ocultación.",
            weight = 10
        ) else null
    }

    private fun highEntropyTokenRule(): HeuristicRule = HeuristicRule { ctx ->
        val found = ctx.urls.any { p ->
            p.queryParams.values.flatten().any { value ->
                val v = value.trim()
                v.length >= highEntropyMinLength &&
                    (hexTokenRegex.matches(v) || base64LikeTokenRegex.matches(v))
            }
        }
        if (found) DetectedSignal(
            signalCode = "URL_HIGH_ENTROPY_TOKEN",
            title = "Token de alta entropía",
            explanation = "Parámetros con tokens largos/aleatorios pueden ocultar identificadores o payloads.",
            weight = 12
        ) else null
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MODERADOR DE DOMINIO CONFIABLE (señal con peso negativo)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Reduce la sospecha básica cuando la URL pertenece a un dominio oficial del catálogo
     * y no se detectan señales técnicas de alto riesgo (anulación dura).
     *
     * El peso es -10: modera pero no anula señales semánticas o técnicas fuertes.
     * La condición de anulación dura se evalúa aquí directamente sobre la URL,
     * sin necesitar conocer las otras señales (principio de regla independiente).
     */
    private fun trustedDomainModeratorRule(): HeuristicRule = HeuristicRule { ctx ->
        if (ctx.urls.isEmpty()) return@HeuristicRule null

        // Comprobar si alguna URL tiene una condición de anulación dura en la propia URL
        val hasHardOverrideUrl = ctx.urls.any { p ->
            p.scheme == "intent" ||
                p.scheme == "javascript" ||
                p.scheme == "data" ||
                p.hostIsIp ||
                p.hostHasMixedScripts ||
                p.hostHasPunycode ||
                p.hostHasNonAscii ||
                p.raw.contains("@") ||
                HomoglyphDetector.detectBrandSpoofingInHost(p.host) != null
        }
        if (hasHardOverrideUrl) return@HeuristicRule null

        // Buscar coincidencia con dominio confiable
        val match = ctx.urls.firstNotNullOfOrNull { p -> TrustedDomainCatalog.findMatch(p.host) }
            ?: return@HeuristicRule null

        DetectedSignal(
            signalCode = "TRUSTED_DOMAIN_BONUS",
            title = "Dominio oficial reconocido",
            explanation = "El enlace pertenece al dominio oficial de '${match.brand}'. " +
                "Reduce la sospecha básica, pero no descarta otras alertas.",
            weight = -10
        )
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UTILIDADES
    // ══════════════════════════════════════════════════════════════════════════

    private fun containsAny(text: String, keywords: List<String>): Boolean =
        keywords.any { kw -> text.contains(kw, ignoreCase = false) }
}

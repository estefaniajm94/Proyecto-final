package com.estef.antiphishingcoach.domain.heuristics

/**
 * Entrada del catálogo de dominios oficiales conocidos.
 *
 * @param brand        Nombre de la entidad/marca.
 * @param rootDomains  Lista de dominios raíz oficiales (sin www. ni path).
 * @param allowSubdomains True si los subdominios del dominio raíz también son válidos.
 * @param category     Categoría de la entidad (banca, paqueteria, administracion, etc.).
 */
data class TrustedDomainEntry(
    val brand: String,
    val rootDomains: List<String>,
    val allowSubdomains: Boolean = true,
    val category: String
)

/**
 * Catálogo inicial de dominios oficiales conocidos.
 *
 * IMPORTANTE: Este catálogo actúa como señal MODERADORA del score, no como whitelist total.
 * Un dominio confiable reduce la sospecha básica, pero NO anula señales técnicas fuertes
 * (punycode, mezcla de scripts, intent://, ejecutables, IP directa, etc.).
 *
 * Diseñado para crecer fácilmente: añadir una nueva entrada a [entries] es suficiente.
 */
object TrustedDomainCatalog {

    val entries: List<TrustedDomainEntry> = listOf(

        // ── Banca ──────────────────────────────────────────────────────────────
        TrustedDomainEntry("Banco Santander", listOf("bancosantander.es", "santander.es"), true, "banca"),
        TrustedDomainEntry("BBVA", listOf("bbva.com", "bbva.es"), true, "banca"),
        TrustedDomainEntry("CaixaBank", listOf("caixabank.es"), true, "banca"),
        TrustedDomainEntry("Bankinter", listOf("bankinter.com"), true, "banca"),
        TrustedDomainEntry("Unicaja", listOf("unicajabanco.es"), true, "banca"),

        // ── Paquetería / logística ─────────────────────────────────────────────
        TrustedDomainEntry("Correos", listOf("correos.es"), true, "paqueteria"),
        TrustedDomainEntry("SEUR", listOf("seur.com"), true, "paqueteria"),
        TrustedDomainEntry("MRW", listOf("mrw.es"), true, "paqueteria"),
        TrustedDomainEntry("DHL", listOf("dhl.com"), true, "paqueteria"),
        TrustedDomainEntry("UPS", listOf("ups.com"), true, "paqueteria"),
        TrustedDomainEntry("FedEx", listOf("fedex.com"), true, "paqueteria"),
        TrustedDomainEntry("GLS", listOf("gls-spain.es"), true, "paqueteria"),

        // ── Administración pública ─────────────────────────────────────────────
        TrustedDomainEntry("Agencia Tributaria", listOf("agenciatributaria.gob.es"), true, "administracion"),
        TrustedDomainEntry("Seguridad Social", listOf("seg-social.gob.es"), true, "administracion"),
        TrustedDomainEntry("DGT", listOf("dgt.es"), true, "administracion"),
        TrustedDomainEntry(
            "Administración General del Estado",
            listOf("060.es", "administracion.gob.es", "carpetaciudadana.gob.es"),
            true,
            "administracion"
        ),

        // ── Salud ──────────────────────────────────────────────────────────────
        TrustedDomainEntry("Ministerio de Sanidad", listOf("sanidad.gob.es"), true, "salud"),
        TrustedDomainEntry("AEMPS", listOf("aemps.gob.es"), true, "salud"),

        // ── Retail / ecommerce ─────────────────────────────────────────────────
        TrustedDomainEntry("Amazon", listOf("amazon.es", "amazon.com"), true, "retail"),
        TrustedDomainEntry("El Corte Inglés", listOf("elcorteingles.es"), true, "retail"),
        TrustedDomainEntry("PcComponentes", listOf("pccomponentes.com"), true, "retail")
    )

    /**
     * Devuelve la [TrustedDomainEntry] si el host proporcionado coincide con algún dominio
     * del catálogo. Tiene en cuenta subdominios cuando [TrustedDomainEntry.allowSubdomains] = true.
     *
     * @param host Host ya normalizado (lowercase, sin www. inicial).
     * @return Entrada coincidente, o null si ninguna coincide.
     */
    fun findMatch(host: String?): TrustedDomainEntry? {
        if (host.isNullOrBlank()) return null
        val h = host.lowercase().trimStart('.')
        return entries.firstOrNull { entry ->
            entry.rootDomains.any { root ->
                h == root || (entry.allowSubdomains && h.endsWith(".$root"))
            }
        }
    }
}

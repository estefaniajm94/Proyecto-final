package com.estef.antiphishingcoach.presentation.resources

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.databinding.FragmentResourcesBinding
import com.estef.antiphishingcoach.presentation.common.BaseFragment
import com.estef.antiphishingcoach.presentation.common.showShortMessage

class ResourcesFragment : BaseFragment<FragmentResourcesBinding>(
    R.layout.fragment_resources,
    FragmentResourcesBinding::bind
) {
    override fun onBoundView(savedInstanceState: Bundle?) {
        val adapter = ResourcesAdapter(
            onOpenUrl = { url -> openUrl(url) },
            onDialPhone = { phone -> dialPhone(phone) }
        )
        binding.rvResources.layoutManager = LinearLayoutManager(requireContext())
        binding.rvResources.adapter = adapter
        adapter.submitList(buildOfficialResources())
    }

    private fun buildOfficialResources(): List<OfficialResourceItem> {
        return listOf(
            OfficialResourceItem(
                title = "INCIBE - Linea de ayuda 017",
                description = "Soporte en ciberseguridad para ciudadanos y empresas. Recurso de referencia ante phishing y fraude online.",
                phone = "017",
                url = "https://www.incibe.es/linea-de-ayuda-en-ciberseguridad"
            ),
            OfficialResourceItem(
                title = "Policia Nacional - Oficina Virtual de Denuncias",
                description = "Canal oficial para iniciar una denuncia telematica en casos compatibles.",
                phone = "091",
                url = "https://denuncias.policia.es/OVD/"
            ),
            OfficialResourceItem(
                title = "Guardia Civil - Delitos telematicos",
                description = "Informacion y contacto oficial de Guardia Civil para incidentes telematicos.",
                phone = "062",
                url = "https://www.guardiacivil.es/es/colaboracion/form_contacto/delitos_telematicos.html"
            ),
            OfficialResourceItem(
                title = "Emergencias",
                description = "Si existe riesgo inmediato para personas o patrimonio, usa el servicio de emergencias.",
                phone = "112"
            )
        )
    }

    private fun openUrl(url: String) {
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }.onFailure {
            showShortMessage("No se pudo abrir el enlace.")
        }
    }

    private fun dialPhone(phone: String) {
        runCatching {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
        }.onFailure {
            showShortMessage("No se pudo abrir el marcador.")
        }
    }
}

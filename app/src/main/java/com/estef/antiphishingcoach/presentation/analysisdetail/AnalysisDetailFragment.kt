package com.estef.antiphishingcoach.presentation.analysisdetail

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.databinding.FragmentAnalysisDetailBinding
import com.estef.antiphishingcoach.domain.model.IncidentRecord
import com.estef.antiphishingcoach.presentation.common.BaseFragment
import com.estef.antiphishingcoach.presentation.common.appContainer
import com.estef.antiphishingcoach.presentation.common.showShortMessage
import kotlinx.coroutines.launch

class AnalysisDetailFragment : BaseFragment<FragmentAnalysisDetailBinding>(
    R.layout.fragment_analysis_detail,
    FragmentAnalysisDetailBinding::bind
) {
    private val args: AnalysisDetailFragmentArgs by navArgs()
    private val viewModel: IncidentDetailViewModel by viewModels {
        IncidentDetailViewModelFactory(
            incidentId = args.incidentId,
            observeIncidentDetailUseCase = appContainer().observeIncidentDetailUseCase
        )
    }

    override fun onBoundView(savedInstanceState: Bundle?) {
        binding.btnShareReport.setOnClickListener {
            shareMarkdownReport()
        }
        observeUiState()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    render(state)
                }
            }
        }
    }

    private fun render(state: IncidentDetailUiState) = with(binding) {
        progressDetail.isVisible = state.isLoading
        tvNotFound.isVisible = state.notFound
        contentDetail.isVisible = state.incident != null

        val incident = state.incident ?: return@with
        tvIncidentId.text = getString(R.string.incident_id_label, incident.id)
        tvTitle.text = incident.title ?: getString(R.string.history_item_title_fallback)
        tvScore.text = getString(R.string.detail_score, incident.score)
        tvTrafficLight.text = getString(R.string.detail_light, incident.trafficLight)
        tvSourceApp.text = getString(R.string.detail_source, incident.sourceApp)
        tvSourceType.text = getString(R.string.detail_source_type, incident.sourceType)
        tvDomain.text = getString(R.string.detail_domain, incident.sanitizedDomain ?: "N/A")
        tvCreatedAt.text = getString(R.string.detail_created, state.createdAtText)
        tvSignals.text = renderSignals(incident)
        tvRecommendations.text = renderRecommendations(state)

        tvTrafficLight.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                incident.trafficLight.toColorRes()
            )
        )
    }

    private fun renderSignals(incident: IncidentRecord): String {
        if (incident.signals.isEmpty()) return "Sin senales registradas."
        return incident.signals.joinToString("\n") { signal ->
            "- ${signal.title}: ${signal.explanation} (peso ${signal.weight})"
        }
    }

    private fun renderRecommendations(state: IncidentDetailUiState): String {
        if (state.recommendations.isEmpty()) return "Sin recomendaciones registradas."
        return state.recommendations.joinToString("\n") { recommendation ->
            "- ${recommendation.title}: ${recommendation.detail}"
        }
    }

    private fun shareMarkdownReport() {
        val markdown = viewModel.buildMarkdownReport()
        if (markdown == null) {
            showShortMessage("No hay datos para exportar.")
            return
        }
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.detail_export_subject))
            putExtra(Intent.EXTRA_TEXT, markdown)
            type = "text/markdown"
        }
        startActivity(Intent.createChooser(sendIntent, null))
    }

    private fun String.toColorRes(): Int {
        return when (this) {
            "GREEN" -> R.color.traffic_green
            "YELLOW" -> R.color.traffic_yellow
            "RED" -> R.color.traffic_red
            else -> R.color.brand_on_surface
        }
    }
}

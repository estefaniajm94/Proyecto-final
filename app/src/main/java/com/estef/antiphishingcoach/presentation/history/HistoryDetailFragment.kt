package com.estef.antiphishingcoach.presentation.history

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.databinding.FragmentHistoryDetailBinding
import com.estef.antiphishingcoach.domain.model.IncidentRecord
import com.estef.antiphishingcoach.presentation.analysisdetail.IncidentDetailUiState
import com.estef.antiphishingcoach.presentation.analysisdetail.IncidentDetailViewModel
import com.estef.antiphishingcoach.presentation.common.BaseFragment
import com.estef.antiphishingcoach.presentation.common.appContainer
import com.estef.antiphishingcoach.presentation.common.collectOnStarted
import com.estef.antiphishingcoach.presentation.common.renderRiskGauge
import com.estef.antiphishingcoach.presentation.common.viewModelFactory

class HistoryDetailFragment : BaseFragment<FragmentHistoryDetailBinding>(
    R.layout.fragment_history_detail,
    FragmentHistoryDetailBinding::bind
) {
    private val args: HistoryDetailFragmentArgs by navArgs()
    private val viewModel: IncidentDetailViewModel by viewModels {
        val c = appContainer()
        viewModelFactory {
            IncidentDetailViewModel(
                incidentId = args.incidentId,
                observeIncidentDetailUseCase = c.observeIncidentDetailUseCase,
                exportReportToFileUseCase = c.exportReportToFileUseCase
            )
        }
    }

    override fun onBoundView(savedInstanceState: Bundle?) {
        setupBackNavigation(binding.btnBack)
        collectOnStarted(viewModel.uiState) { state -> render(state) }
    }

    private fun render(state: IncidentDetailUiState) = with(binding) {
        progressDetail.isVisible = state.isLoading
        tvNotFound.isVisible = state.notFound
        contentDetail.isVisible = state.incident != null

        val incident = state.incident ?: return@with
        tvIncidentId.text = getString(R.string.incident_id_label, incident.id)
        tvTitle.text = incident.title ?: getString(R.string.history_item_title_fallback)
        riskGaugeDetail.renderRiskGauge(incident.score)
        tvSourceApp.text = getString(R.string.detail_source, incident.sourceApp)
        tvSourceType.text = getString(R.string.detail_source_type, incident.sourceType)
        tvDomain.text = getString(
            R.string.detail_domain,
            incident.sanitizedDomain ?: getString(R.string.detail_value_not_available)
        )
        tvCreatedAt.text = getString(R.string.detail_created, state.createdAtText)
        tvSignals.text = renderSignals(incident)
        tvRecommendations.text = renderRecommendations(state)
    }

    private fun renderSignals(incident: IncidentRecord): String {
        if (incident.signals.isEmpty()) return getString(R.string.detail_no_signals)
        return incident.signals.joinToString("\n") { signal ->
            getString(R.string.detail_signal_line, signal.title, signal.explanation, signal.weight)
        }
    }

    private fun renderRecommendations(state: IncidentDetailUiState): String {
        if (state.recommendations.isEmpty()) return getString(R.string.detail_no_recommendations)
        return state.recommendations.joinToString("\n") { recommendation ->
            getString(R.string.detail_recommendation_line, recommendation.title, recommendation.detail)
        }
    }
}

package com.estef.antiphishingcoach.presentation.analysisdetail

import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.databinding.FragmentAnalysisDetailBinding
import com.estef.antiphishingcoach.domain.model.IncidentRecord
import com.estef.antiphishingcoach.presentation.common.BaseFragment
import com.estef.antiphishingcoach.presentation.common.appContainer
import com.estef.antiphishingcoach.presentation.common.collectOnStarted
import com.estef.antiphishingcoach.presentation.common.renderRiskGauge
import com.estef.antiphishingcoach.presentation.common.showShortMessage
import com.estef.antiphishingcoach.presentation.common.viewModelFactory

class AnalysisDetailFragment : BaseFragment<FragmentAnalysisDetailBinding>(
    R.layout.fragment_analysis_detail,
    FragmentAnalysisDetailBinding::bind
) {
    private val args: AnalysisDetailFragmentArgs by navArgs()
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
        binding.btnShareReport.setOnClickListener { shareMarkdownReport() }
        binding.btnOpenOfficialResources.setOnClickListener {
            findNavController().navigate(R.id.action_analysisDetail_to_resources)
        }
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
        tvActionPlan.text = state.actionPlan.steps.mapIndexed { index, step ->
            getString(R.string.analyze_action_plan_line, index + 1, step)
        }.joinToString("\n")
        btnOpenOfficialResources.isVisible = state.actionPlan.showOfficialResources
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

    private fun shareMarkdownReport() {
        val exportFile = viewModel.exportMarkdownReportFile()
        if (exportFile == null) {
            showShortMessage(getString(R.string.detail_export_no_data))
            return
        }
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.detail_export_subject))
            putExtra(Intent.EXTRA_STREAM, exportFile.uri)
            type = "text/markdown"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(sendIntent, null))
    }
}

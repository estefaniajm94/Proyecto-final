package com.estef.antiphishingcoach.presentation.analyze

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.graphics.Typeface
import android.widget.ArrayAdapter
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.core.model.SourceApp
import com.estef.antiphishingcoach.databinding.DialogOcrReviewBinding
import com.estef.antiphishingcoach.databinding.FragmentAnalyzeBinding
import com.estef.antiphishingcoach.presentation.common.AndroidStringResolver
import com.estef.antiphishingcoach.presentation.common.BaseFragment
import com.estef.antiphishingcoach.presentation.common.appContainer
import com.estef.antiphishingcoach.presentation.common.collectOnStarted
import com.estef.antiphishingcoach.presentation.common.renderRiskGauge
import com.estef.antiphishingcoach.presentation.common.viewModelFactory
import com.estef.antiphishingcoach.presentation.navigation.SharedContentViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AnalyzeFragment : BaseFragment<FragmentAnalyzeBinding>(
    R.layout.fragment_analyze,
    FragmentAnalyzeBinding::bind
) {
    private val sharedContentViewModel: SharedContentViewModel by activityViewModels()
    private val viewModel: AnalyzeViewModel by viewModels {
        val c = appContainer()
        viewModelFactory {
            AnalyzeViewModel(
                analyzeAndPersistUseCase = c.analyzeAndPersistUseCase,
                extractTextFromImageUseCase = c.extractTextFromImageUseCase,
                observeExtremePrivacyUseCase = c.observeExtremePrivacyUseCase,
                stringResolver = AndroidStringResolver(requireContext().applicationContext)
            )
        }
    }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            viewModel.onImageSelected(uri)
        }

    private var ocrReviewDialog: AlertDialog? = null
    private var lastRenderedOcrText: String? = null

    override fun onBoundView(savedInstanceState: Bundle?) {
        setupBackNavigation(binding.btnBack)
        setupSourceAppDropdown()
        setupActions()
        collectOnStarted(viewModel.uiState) { state -> render(state) }
        collectOnStarted(sharedContentViewModel.pendingSharedInput) { sharedInput ->
            if (sharedInput == null) return@collectOnStarted
            binding.etInput.setText(sharedInput.inputText)
            if (binding.etTitle.text.isNullOrBlank()) {
                binding.etTitle.setText(sharedInput.title.orEmpty())
            }
            binding.actvSourceApp.setText(sharedInput.sourceApp.toDisplayLabel(), false)
            viewModel.onSharedInputLoaded()
            sharedContentViewModel.consume()
        }
    }

    override fun onDestroyView() {
        ocrReviewDialog?.dismiss()
        ocrReviewDialog = null
        lastRenderedOcrText = null
        super.onDestroyView()
    }

    private fun setupSourceAppDropdown() {
        val options = SourceApp.values().map { it.toDisplayLabel() }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, options)
        binding.actvSourceApp.setAdapter(adapter)
        binding.actvSourceApp.setText(SourceApp.OTHER.toDisplayLabel(), false)
    }

    private fun setupActions() = with(binding) {
        etInput.doAfterTextChanged { tilInput.error = null }
        btnAnalyzeNow.setOnClickListener {
            viewModel.analyze(
                inputText = etInput.text?.toString().orEmpty(),
                title = etTitle.text?.toString(),
                sourceApp = displayLabelToSourceApp(actvSourceApp.text?.toString())
            )
        }
        btnAnalyzeFromCapture.setOnClickListener {
            viewModel.onPickImageRequested()
            pickImageLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
        btnOpenDetail.setOnClickListener {
            val incidentId = viewModel.uiState.value.result?.persistedIncidentId
                ?: return@setOnClickListener
            val action = AnalyzeFragmentDirections.actionAnalyzeToAnalysisDetail(incidentId)
            findNavController().navigate(action)
        }
        btnOpenResources.setOnClickListener {
            findNavController().navigate(R.id.action_analyze_to_resources)
        }
    }

    private fun render(state: AnalyzeUiState) = with(binding) {
        progressAnalyze.isVisible = state.isLoading
        tvPrivacyState.text = if (state.extremePrivacyEnabled) {
            getString(R.string.analyze_privacy_active)
        } else {
            getString(R.string.analyze_privacy_inactive)
        }
        tilInput.error = state.inputError
        tvStatusMessage.text = state.statusMessage.orEmpty()

        val result = state.result
        cardResult.isVisible = result != null
        if (result != null) {
            riskGaugeResult.renderRiskGauge(result.score)
            tvSourceTypeValue.text = result.sourceTypeLabel
            tvDomainValue.text = result.sanitizedDomain
                ?: getString(R.string.detail_value_not_available)
            tvAnalyzedInputValue.text = buildHighlightedAnalyzedInput(
                input = result.analyzedInput,
                suspiciousPhrases = result.suspiciousPhrases
            )
            tvQuickExplanationValue.text = result.quickExplanation
            tvUrlInsightsValue.text = if (result.urlInsights.isEmpty()) {
                getString(R.string.analyze_url_breakdown_empty)
            } else {
                result.urlInsights.joinToString("\n\n") { insight ->
                    val observations = if (insight.observations.isEmpty()) {
                        getString(R.string.analyze_url_observations_none)
                    } else {
                        insight.observations.joinToString(" | ")
                    }
                    listOf(
                        getString(R.string.analyze_url_domain_line, insight.domain ?: getString(R.string.detail_value_not_available)),
                        getString(R.string.analyze_url_scheme_line, insight.scheme),
                        getString(R.string.analyze_url_path_line, insight.path ?: "/"),
                        getString(R.string.analyze_url_params_line, insight.parameterCount),
                        getString(R.string.analyze_url_observations_line, observations)
                    ).joinToString("\n")
                }
            }
            tvSuspiciousPhrasesValue.text = if (result.suspiciousPhrases.isEmpty()) {
                getString(R.string.analyze_suspicious_phrases_empty)
            } else {
                result.suspiciousPhrases.joinToString("\n") { insight ->
                    getString(R.string.analyze_suspicious_phrase_line, insight.phrase, insight.category)
                }
            }
            tvActionPlanValue.text = result.actionPlan.steps.mapIndexed { index, step ->
                getString(R.string.analyze_action_plan_line, index + 1, step)
            }.joinToString("\n")
            btnOpenResources.isVisible = result.actionPlan.showOfficialResources
            tvSignalsValue.text = if (result.signals.isEmpty()) {
                getString(R.string.analyze_no_risk_signals)
            } else {
                result.signals.joinToString("\n") { signal ->
                    getString(R.string.analyze_signal_line, signal.title, signal.explanation, signal.weight)
                }
            }
            tvRecommendationsValue.text = if (result.recommendations.isEmpty()) {
                getString(R.string.analyze_no_extra_recommendations)
            } else {
                result.recommendations.joinToString("\n") { r ->
                    getString(R.string.analyze_recommendation_line, r.title, r.detail)
                }
            }
            tvPersistenceValue.text = if (result.persistedIncidentId == null) {
                getString(R.string.analyze_not_saved_by_privacy)
            } else {
                getString(R.string.analyze_saved_with_incident_id, result.persistedIncidentId)
            }
            btnOpenDetail.isVisible = result.persistedIncidentId != null
        }

        when (val flowState = state.flowState) {
            AnalyzeFlowState.Idle,
            AnalyzeFlowState.PickingImage,
            AnalyzeFlowState.OcrRunning,
            AnalyzeFlowState.Analyzing,
            AnalyzeFlowState.ResultReady -> lastRenderedOcrText = null

            is AnalyzeFlowState.OcrReady -> {
                if (lastRenderedOcrText != flowState.text || ocrReviewDialog?.isShowing != true) {
                    lastRenderedOcrText = flowState.text
                    showOcrReviewDialog(flowState.text)
                }
            }

            is AnalyzeFlowState.Error -> lastRenderedOcrText = null
        }
    }

    private fun showOcrReviewDialog(initialText: String) {
        if (ocrReviewDialog?.isShowing == true) return

        val dialogBinding = DialogOcrReviewBinding.inflate(layoutInflater)
        dialogBinding.etOcrText.setText(initialText)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.ocr_review_title))
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.ocr_review_cancel) { _, _ ->
                viewModel.onOcrReviewCancelled()
            }
            .setPositiveButton(R.string.ocr_review_confirm, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val sourceApp = displayLabelToSourceApp(binding.actvSourceApp.text?.toString())
                viewModel.onOcrTextConfirmed(
                    text = dialogBinding.etOcrText.text?.toString().orEmpty(),
                    title = binding.etTitle.text?.toString(),
                    sourceApp = sourceApp
                )
                dialog.dismiss()
            }
        }
        dialog.setOnCancelListener { viewModel.onOcrReviewCancelled() }
        dialog.setOnDismissListener { if (ocrReviewDialog === dialog) ocrReviewDialog = null }

        ocrReviewDialog = dialog
        dialog.show()
    }

    private fun displayLabelToSourceApp(label: String?): SourceApp {
        return SourceApp.values().firstOrNull { it.toDisplayLabel() == label } ?: SourceApp.OTHER
    }

    private fun SourceApp.toDisplayLabel(): String = when (this) {
        SourceApp.SMS -> getString(R.string.source_app_sms)
        SourceApp.WHATSAPP -> getString(R.string.source_app_whatsapp)
        SourceApp.EMAIL -> getString(R.string.source_app_email)
        SourceApp.OTHER -> getString(R.string.source_app_other)
    }

    private fun buildHighlightedAnalyzedInput(
        input: String,
        suspiciousPhrases: List<SuspiciousPhraseInsight>
    ): SpannableString {
        val spannable = SpannableString(input)
        val highlightColor = ContextCompat.getColor(requireContext(), R.color.traffic_red)

        suspiciousPhrases.forEach { insight ->
            Regex(Regex.escape(insight.phrase), RegexOption.IGNORE_CASE)
                .findAll(input)
                .forEach { match ->
                    spannable.setSpan(
                        ForegroundColorSpan(highlightColor),
                        match.range.first,
                        match.range.last + 1,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    spannable.setSpan(
                        StyleSpan(Typeface.BOLD),
                        match.range.first,
                        match.range.last + 1,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
        }
        return spannable
    }
}

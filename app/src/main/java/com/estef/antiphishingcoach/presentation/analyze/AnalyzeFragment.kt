package com.estef.antiphishingcoach.presentation.analyze

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.core.model.SourceApp
import com.estef.antiphishingcoach.databinding.DialogOcrReviewBinding
import com.estef.antiphishingcoach.databinding.FragmentAnalyzeBinding
import com.estef.antiphishingcoach.presentation.common.BaseFragment
import com.estef.antiphishingcoach.presentation.common.appContainer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class AnalyzeFragment : BaseFragment<FragmentAnalyzeBinding>(
    R.layout.fragment_analyze,
    FragmentAnalyzeBinding::bind
) {
    private val viewModel: AnalyzeViewModel by viewModels {
        val container = appContainer()
        AnalyzeViewModelFactory(
            analyzeAndPersistUseCase = container.analyzeAndPersistUseCase,
            extractTextFromImageUseCase = container.extractTextFromImageUseCase,
            observeExtremePrivacyUseCase = container.observeExtremePrivacyUseCase
        )
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        viewModel.onImageSelected(uri)
    }

    private var ocrReviewDialog: AlertDialog? = null
    private var lastRenderedOcrText: String? = null

    override fun onBoundView(savedInstanceState: Bundle?) {
        setupSourceAppDropdown()
        setupActions()
        observeUiState()
    }

    override fun onDestroyView() {
        ocrReviewDialog?.dismiss()
        ocrReviewDialog = null
        lastRenderedOcrText = null
        super.onDestroyView()
    }

    private fun setupSourceAppDropdown() {
        val options = SourceApp.values().map { sourceApp ->
            sourceApp.toDisplayLabel()
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, options)
        binding.actvSourceApp.setAdapter(adapter)
        binding.actvSourceApp.setText(SourceApp.OTHER.toDisplayLabel(), false)
    }

    private fun setupActions() = with(binding) {
        etInput.doAfterTextChanged { tilInput.error = null }
        btnAnalyzeNow.setOnClickListener {
            val sourceApp = displayLabelToSourceApp(actvSourceApp.text?.toString())
            viewModel.analyze(
                inputText = etInput.text?.toString().orEmpty(),
                title = etTitle.text?.toString(),
                sourceApp = sourceApp
            )
        }
        btnAnalyzeFromCapture.setOnClickListener {
            viewModel.onPickImageRequested()
            pickImageLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
        btnOpenDetail.setOnClickListener {
            val incidentId = viewModel.uiState.value.result?.persistedIncidentId ?: return@setOnClickListener
            val action = AnalyzeFragmentDirections.actionAnalyzeToAnalysisDetail(incidentId)
            findNavController().navigate(action)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: AnalyzeUiState) = with(binding) {
        progressAnalyze.isVisible = state.isLoading
        tvPrivacyState.text = if (state.extremePrivacyEnabled) {
            "Privacidad extrema: ACTIVA (no se guarda historial)."
        } else {
            "Privacidad extrema: INACTIVA (se guardan metadatos)."
        }
        tilInput.error = state.inputError
        tvStatusMessage.text = state.statusMessage.orEmpty()

        val result = state.result
        cardResult.isVisible = result != null
        if (result != null) {
            tvScoreValue.text = result.score.toString()
            tvTrafficLightValue.text = result.trafficLightLabel
            tvSourceTypeValue.text = result.sourceTypeLabel
            tvDomainValue.text = result.sanitizedDomain ?: "N/A"
            tvSignalsValue.text = if (result.signals.isEmpty()) {
                "No se detectaron senales de riesgo en esta entrada."
            } else {
                result.signals.joinToString("\n") { signal ->
                    "- ${signal.title}: ${signal.explanation} (peso ${signal.weight})"
                }
            }
            tvRecommendationsValue.text = if (result.recommendations.isEmpty()) {
                "Sin recomendaciones adicionales."
            } else {
                result.recommendations.joinToString("\n") { recommendation ->
                    "- ${recommendation.title}: ${recommendation.detail}"
                }
            }
            tvPersistenceValue.text = if (result.persistedIncidentId == null) {
                "No guardado por privacidad extrema."
            } else {
                "Guardado en historial con incidentId=${result.persistedIncidentId}"
            }
            btnOpenDetail.isVisible = result.persistedIncidentId != null
            tvTrafficLightValue.setTextColor(ContextCompat.getColor(requireContext(), result.toTrafficColorRes()))
        }

        when (val flowState = state.flowState) {
            AnalyzeFlowState.Idle,
            AnalyzeFlowState.PickingImage,
            AnalyzeFlowState.OcrRunning,
            AnalyzeFlowState.Analyzing,
            AnalyzeFlowState.ResultReady -> {
                lastRenderedOcrText = null
            }

            is AnalyzeFlowState.OcrReady -> {
                if (lastRenderedOcrText != flowState.text || ocrReviewDialog?.isShowing != true) {
                    lastRenderedOcrText = flowState.text
                    showOcrReviewDialog(flowState.text)
                }
            }

            is AnalyzeFlowState.Error -> {
                lastRenderedOcrText = null
            }
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
        dialog.setOnCancelListener {
            viewModel.onOcrReviewCancelled()
        }
        dialog.setOnDismissListener {
            if (ocrReviewDialog === dialog) {
                ocrReviewDialog = null
            }
        }

        ocrReviewDialog = dialog
        dialog.show()
    }

    private fun AnalysisPresentation.toTrafficColorRes(): Int {
        return when (trafficLightLabel) {
            "VERDE" -> R.color.traffic_green
            "AMARILLO" -> R.color.traffic_yellow
            else -> R.color.traffic_red
        }
    }

    private fun displayLabelToSourceApp(label: String?): SourceApp {
        return SourceApp.values().firstOrNull { sourceApp ->
            sourceApp.toDisplayLabel() == label
        } ?: SourceApp.OTHER
    }

    private fun SourceApp.toDisplayLabel(): String {
        return when (this) {
            SourceApp.SMS -> "SMS"
            SourceApp.WHATSAPP -> "WhatsApp"
            SourceApp.EMAIL -> "Email"
            SourceApp.OTHER -> "Otro"
        }
    }
}

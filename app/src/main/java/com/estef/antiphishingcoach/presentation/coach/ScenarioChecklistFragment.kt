package com.estef.antiphishingcoach.presentation.coach

import android.os.Bundle
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.databinding.FragmentScenarioChecklistBinding
import com.estef.antiphishingcoach.presentation.common.BaseFragment
import com.estef.antiphishingcoach.presentation.common.appContainer
import kotlinx.coroutines.launch

class ScenarioChecklistFragment : BaseFragment<FragmentScenarioChecklistBinding>(
    R.layout.fragment_scenario_checklist,
    FragmentScenarioChecklistBinding::bind
) {
    private val args: ScenarioChecklistFragmentArgs by navArgs()
    private val viewModel: ScenarioChecklistViewModel by viewModels {
        ScenarioChecklistViewModelFactory(
            scenarioId = args.scenarioId,
            getCoachScenariosUseCase = appContainer().getCoachScenariosUseCase
        )
    }

    private var renderedKey: String = ""

    override fun onBoundView(savedInstanceState: Bundle?) {
        setupBackNavigation(binding.btnBack)
        binding.btnOpenResources.setOnClickListener {
            findNavController().navigate(R.id.action_scenarioChecklist_to_resources)
        }
        observeUi()
    }

    private fun observeUi() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    render(state)
                }
            }
        }
    }

    private fun render(state: ScenarioChecklistUiState) = with(binding) {
        if (state.isLoading) return@with

        val scenario = state.scenario
        if (scenario == null) {
            tvScenarioThreat.isVisible = false
            tvScenarioTitle.text = getString(R.string.coach_scenario_not_found)
            tvScenarioSummary.text = ""
            cardSigns.isVisible = false
            cardActions.isVisible = false
            cardAvoid.isVisible = false
            cardEscalate.isVisible = false
            cardClosing.isVisible = false
            btnOpenResources.isVisible = false
            return@with
        }

        tvScenarioThreat.text = scenario.threatLabel
        tvScenarioThreat.isVisible = scenario.threatLabel.isNotBlank()
        tvScenarioTitle.text = scenario.title
        tvScenarioSummary.text = scenario.summary

        tvChecklistEmpty.isVisible = scenario.whatToDoNow.isEmpty()
        tvChecklistProgress.text = getString(
            R.string.coach_checklist_progress,
            state.markedCount,
            scenario.whatToDoNow.size
        )
        tvScenarioStatus.text = buildStatusMessage(state.markedCount, scenario.whatToDoNow.size)
        tvRecommendedAction.text = scenario.recommendedAction
        tvRecommendedAction.isVisible = scenario.recommendedAction.isNotBlank()
        tvClosingNote.text = scenario.closingNote
        tvClosingNote.isVisible = scenario.closingNote.isNotBlank()
        btnOpenResources.isVisible = true

        cardSigns.isVisible = scenario.typicalSigns.isNotEmpty()
        cardAvoid.isVisible = scenario.whatToAvoid.isNotEmpty()
        cardEscalate.isVisible = scenario.whenToEscalate.isNotEmpty()
        cardClosing.isVisible = true

        renderBulletSection(signsContainer, scenario.typicalSigns)
        renderBulletSection(avoidContainer, scenario.whatToAvoid)
        renderBulletSection(escalateContainer, scenario.whenToEscalate)

        val key = "${scenario.id}:${scenario.whatToDoNow.joinToString("|")}"
        if (renderedKey == key) return@with
        renderedKey = key

        checklistContainer.removeAllViews()
        scenario.whatToDoNow.forEachIndexed { index, item ->
            val checkBox = CheckBox(requireContext()).apply {
                text = item
                setOnCheckedChangeListener { _, _ ->
                    viewModel.onCheckedCountChanged(countMarked())
                }
            }
            checklistContainer.addView(checkBox, index)
        }
    }

    private fun countMarked(): Int {
        var count = 0
        val parent = binding.checklistContainer
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (child is CheckBox && child.isChecked) {
                count += 1
            }
        }
        return count
    }

    private fun renderBulletSection(container: android.widget.LinearLayout, items: List<String>) {
        container.removeAllViews()
        items.forEach { item ->
            val textView = TextView(requireContext()).apply {
                text = "\u2022 $item"
                setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Body2)
            }
            container.addView(textView)
        }
    }

    private fun buildStatusMessage(markedCount: Int, total: Int): String {
        return when {
            total == 0 || markedCount == 0 -> getString(R.string.coach_status_not_started)
            markedCount < total -> getString(R.string.coach_status_in_progress)
            else -> getString(R.string.coach_status_completed)
        }
    }
}

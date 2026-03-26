package com.estef.antiphishingcoach.presentation.coach

import android.os.Bundle
import android.widget.CheckBox
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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

        tvScenarioTitle.text = state.scenarioTitle.ifBlank { "Escenario no encontrado" }
        tvChecklistEmpty.isVisible = state.items.isEmpty()
        tvChecklistProgress.text = getString(
            R.string.coach_checklist_progress,
            state.markedCount,
            state.items.size
        )

        val key = "${state.scenarioTitle}:${state.items.joinToString("|")}"
        if (renderedKey == key) return@with
        renderedKey = key

        checklistContainer.removeAllViews()
        state.items.forEachIndexed { index, item ->
            val checkBox = CheckBox(requireContext()).apply {
                text = item
                setOnCheckedChangeListener { _, _ ->
                    val marked = countMarked()
                    viewModel.onCheckedCountChanged(marked)
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
}

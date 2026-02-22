package com.estef.antiphishingcoach.presentation.home

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.databinding.FragmentHomeBinding
import com.estef.antiphishingcoach.presentation.common.BaseFragment
import com.estef.antiphishingcoach.presentation.common.appContainer
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment<FragmentHomeBinding>(
    R.layout.fragment_home,
    FragmentHomeBinding::bind
) {

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(appContainer().observeLatestIncidentSummaryUseCase)
    }

    override fun onBoundView(savedInstanceState: Bundle?) {
        setupActions()
        observeUiState()
    }

    private fun setupActions() = with(binding) {
        cardAnalyze.setOnClickListener { findNavController().navigate(R.id.action_home_to_analyze) }
        cardCoach.setOnClickListener { findNavController().navigate(R.id.action_home_to_coach) }
        cardTraining.setOnClickListener { findNavController().navigate(R.id.action_home_to_trainingStart) }
        cardHistory.setOnClickListener { findNavController().navigate(R.id.action_home_to_history) }
        btnOpenSettings.setOnClickListener { findNavController().navigate(R.id.action_home_to_settings) }
        btnOpenResources.setOnClickListener { findNavController().navigate(R.id.action_home_to_resources) }
        btnLatestAnalysisDetail.setOnClickListener {
            val latestIncident = viewModel.uiState.value.latestIncident ?: return@setOnClickListener
            val action = HomeFragmentDirections.actionHomeToAnalysisDetail(latestIncident.incidentId)
            findNavController().navigate(action)
        }
        btnRepeatTraining.setOnClickListener { findNavController().navigate(R.id.action_home_to_trainingStart) }
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

    private fun render(state: HomeUiState) = with(binding) {
        val latest = state.latestIncident
        progressLatestIncident.isVisible = state.isLoading
        latestAnalysisContent.isVisible = !state.isLoading && latest != null
        tvLatestAnalysisEmpty.isVisible = !state.isLoading && latest == null

        if (latest != null) {
            tvLatestTitle.text = latest.title
            tvLatestScore.text = latest.scoreLine
            tvLatestDate.text = latest.createdAtLine
            chipLatestTraffic.text = latest.trafficLightLabel
            chipLatestTraffic.chipBackgroundColor = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), latest.trafficColorRes())
            )
        }

        tvLatestTrainingSummary.text = state.latestTrainingSummary
    }

    private fun LatestIncidentUi.trafficColorRes(): Int {
        return when (trafficLightLabel) {
            "GREEN" -> R.color.traffic_green
            "YELLOW" -> R.color.traffic_yellow
            else -> R.color.traffic_red
        }
    }
}

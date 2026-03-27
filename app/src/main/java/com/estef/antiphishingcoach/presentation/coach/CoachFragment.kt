package com.estef.antiphishingcoach.presentation.coach

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.databinding.FragmentCoachBinding
import com.estef.antiphishingcoach.presentation.common.BaseFragment
import com.estef.antiphishingcoach.presentation.common.appContainer
import com.estef.antiphishingcoach.presentation.common.collectOnStarted
import com.estef.antiphishingcoach.presentation.common.viewModelFactory

class CoachFragment : BaseFragment<FragmentCoachBinding>(
    R.layout.fragment_coach,
    FragmentCoachBinding::bind
) {
    private val viewModel: CoachViewModel by viewModels {
        viewModelFactory { CoachViewModel(appContainer().getCoachScenariosUseCase) }
    }

    private lateinit var adapter: CoachScenarioAdapter

    override fun onBoundView(savedInstanceState: Bundle?) {
        setupBackNavigation(binding.btnBack)
        adapter = CoachScenarioAdapter { scenario ->
            val action = CoachFragmentDirections.actionCoachToScenarioChecklist(scenario.id)
            findNavController().navigate(action)
        }
        binding.rvCoachScenarios.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@CoachFragment.adapter
        }
        collectOnStarted(viewModel.uiState) { state ->
            binding.tvCoachLoading.isVisible = state.isLoading
            binding.tvCoachEmpty.isVisible = !state.isLoading && state.scenarios.isEmpty()
            adapter.submitList(state.scenarios)
        }
    }
}

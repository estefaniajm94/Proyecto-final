package com.estef.antiphishingcoach.presentation.coach

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.databinding.FragmentCoachBinding
import com.estef.antiphishingcoach.presentation.common.BaseFragment
import com.estef.antiphishingcoach.presentation.common.appContainer
import kotlinx.coroutines.launch

class CoachFragment : BaseFragment<FragmentCoachBinding>(
    R.layout.fragment_coach,
    FragmentCoachBinding::bind
) {
    private val viewModel: CoachViewModel by viewModels {
        CoachViewModelFactory(appContainer().getCoachScenariosUseCase)
    }

    private lateinit var adapter: CoachScenarioAdapter

    override fun onBoundView(savedInstanceState: Bundle?) {
        setupBackNavigation(binding.btnBack)
        setupList()
        observeUi()
    }

    private fun setupList() {
        adapter = CoachScenarioAdapter { scenario ->
            val action = CoachFragmentDirections.actionCoachToScenarioChecklist(scenario.id)
            findNavController().navigate(action)
        }
        binding.rvCoachScenarios.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@CoachFragment.adapter
        }
    }

    private fun observeUi() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.tvCoachLoading.isVisible = state.isLoading
                    binding.tvCoachEmpty.isVisible = !state.isLoading && state.scenarios.isEmpty()
                    adapter.submitList(state.scenarios)
                }
            }
        }
    }
}

package com.estef.antiphishingcoach.presentation.home

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.databinding.FragmentHomeBinding
import com.estef.antiphishingcoach.presentation.common.AndroidStringResolver
import com.estef.antiphishingcoach.presentation.common.BaseFragment
import com.estef.antiphishingcoach.presentation.common.appContainer
import com.estef.antiphishingcoach.presentation.common.toTrafficColorRes
import com.estef.antiphishingcoach.presentation.common.toTrafficSeverityBarRes
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment<FragmentHomeBinding>(
    R.layout.fragment_home,
    FragmentHomeBinding::bind
) {

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(
            observeLatestIncidentSummaryUseCase = appContainer().observeLatestIncidentSummaryUseCase,
            stringResolver = AndroidStringResolver(requireContext().applicationContext)
        )
    }

    override fun onBoundView(savedInstanceState: Bundle?) {
        setupToolbar()
        setupPrimaryAction()
        setupQuickCards()
        setupSecondaryActions()
        observeUiState()
    }

    private fun setupToolbar() {
        binding.toolbarHome.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_home_settings -> {
                    findNavController().navigate(R.id.action_home_to_settings)
                    true
                }

                else -> false
            }
        }
    }

    private fun setupPrimaryAction() {
        binding.cardPrimaryAnalyze.setOnClickListener {
            navigateToAnalyze()
        }
        binding.btnPrimaryAnalyze.setOnClickListener {
            navigateToAnalyze()
        }
    }

    private fun setupQuickCards() {
        val quickCards = listOf(
            QuickActionCard(
                cardId = R.id.cardQuickCoach,
                iconRes = android.R.drawable.ic_menu_info_details,
                title = getString(R.string.home_quick_coach_title),
                description = getString(R.string.home_quick_coach_desc),
                onClick = { findNavController().navigate(R.id.action_home_to_coach) }
            ),
            QuickActionCard(
                cardId = R.id.cardQuickTraining,
                iconRes = android.R.drawable.ic_menu_agenda,
                title = getString(R.string.home_quick_training_title),
                description = getString(R.string.home_quick_training_desc),
                onClick = { findNavController().navigate(R.id.action_home_to_trainingStart) }
            ),
            QuickActionCard(
                cardId = R.id.cardQuickHistory,
                iconRes = android.R.drawable.ic_menu_recent_history,
                title = getString(R.string.home_quick_history_title),
                description = getString(R.string.home_quick_history_desc),
                onClick = { findNavController().navigate(R.id.action_home_to_history) }
            ),
            QuickActionCard(
                cardId = R.id.cardQuickResources,
                iconRes = android.R.drawable.ic_menu_help,
                title = getString(R.string.home_quick_resources_title),
                description = getString(R.string.home_quick_resources_desc),
                onClick = { findNavController().navigate(R.id.action_home_to_resources) }
            )
        )
        quickCards.forEach(::configureQuickCard)
    }

    private fun setupSecondaryActions() {
        binding.btnAnalyzeFromLatestEmpty.setOnClickListener {
            navigateToAnalyze()
        }
        binding.btnLatestAnalysisDetail.setOnClickListener {
            val latestIncident = viewModel.uiState.value.latestIncident ?: return@setOnClickListener
            val action = HomeFragmentDirections.actionHomeToAnalysisDetail(latestIncident.incidentId)
            findNavController().navigate(action)
        }
        binding.btnRepeatTraining.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_trainingStart)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: HomeUiState) = with(binding) {
        progressLatestIncident.isVisible = state.isLoading

        val latest = state.latestIncident
        layoutLatestIncidentData.isVisible = !state.isLoading && latest != null
        layoutLatestIncidentEmpty.isVisible = !state.isLoading && latest == null

        if (latest != null) {
            tvLatestTitle.text = latest.title
            chipLatestTraffic.text = latest.trafficLightCode
            chipLatestTraffic.chipBackgroundColor = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    latest.trafficLightCode.toTrafficColorRes(fallbackRes = R.color.traffic_red)
                )
            )
            tvLatestScore.text = "${latest.score}/100"
            tvLatestDate.text = latest.createdAtLine
            tvLatestDomain.isVisible = latest.sanitizedDomainLine != null
            tvLatestDomain.text = latest.sanitizedDomainLine.orEmpty()
            viewLatestSeverityBar.setBackgroundResource(
                latest.trafficLightCode.toTrafficSeverityBarRes(
                    fallbackRes = R.drawable.bg_severity_bar_red
                )
            )
        } else {
            viewLatestSeverityBar.setBackgroundResource(R.drawable.bg_severity_bar_green)
        }

        tvLatestTrainingSummary.text = state.latestTrainingSummary
    }

    private fun configureQuickCard(cardData: QuickActionCard) {
        val card = binding.root.findViewById<MaterialCardView>(cardData.cardId)
        val icon = card.findViewById<ImageView>(R.id.ivActionIcon)
        val titleView = card.findViewById<TextView>(R.id.tvActionTitle)
        val descriptionView = card.findViewById<TextView>(R.id.tvActionDescription)

        icon.setImageResource(cardData.iconRes)
        titleView.text = cardData.title
        descriptionView.text = cardData.description
        card.setOnClickListener { cardData.onClick() }
    }

    private fun navigateToAnalyze() {
        findNavController().navigate(R.id.action_home_to_analyze)
    }

    private data class QuickActionCard(
        val cardId: Int,
        val iconRes: Int,
        val title: String,
        val description: String,
        val onClick: () -> Unit
    )
}

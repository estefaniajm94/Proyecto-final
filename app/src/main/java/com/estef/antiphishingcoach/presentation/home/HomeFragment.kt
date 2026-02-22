package com.estef.antiphishingcoach.presentation.home

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.databinding.FragmentHomeBinding
import com.estef.antiphishingcoach.presentation.common.BaseFragment

class HomeFragment : BaseFragment<FragmentHomeBinding>(
    R.layout.fragment_home,
    FragmentHomeBinding::bind
) {
    override fun onBoundView(savedInstanceState: Bundle?) = with(binding) {
        btnAnalyze.setOnClickListener { findNavController().navigate(R.id.action_home_to_analyze) }
        btnCoach.setOnClickListener { findNavController().navigate(R.id.action_home_to_coach) }
        btnTraining.setOnClickListener { findNavController().navigate(R.id.action_home_to_trainingStart) }
        btnHistory.setOnClickListener { findNavController().navigate(R.id.action_home_to_history) }
        btnSettings.setOnClickListener { findNavController().navigate(R.id.action_home_to_settings) }
        btnResources.setOnClickListener { findNavController().navigate(R.id.action_home_to_resources) }
    }
}

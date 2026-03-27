package com.estef.antiphishingcoach.presentation.common

import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.estef.antiphishingcoach.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

fun Fragment.showShortMessage(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

/**
 * Collects [flow] in the STARTED lifecycle state, cancelling automatically on stop.
 */
fun <T> Fragment.collectOnStarted(flow: Flow<T>, collector: (T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect(collector)
        }
    }
}

/**
 * Generic ViewModelProvider.Factory that delegates creation to [creator].
 * Replaces the per-ViewModel factory boilerplate.
 */
fun viewModelFactory(creator: () -> ViewModel): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = creator() as T
    }
}

/**
 * Navigates out of the auth graph after a successful login or registration.
 * Goes to Analyze if there is a pending shared input, otherwise to Home.
 */
fun Fragment.navigateAfterAuth(hasPendingSharedInput: Boolean) {
    val dest = if (hasPendingSharedInput) R.id.analyzeFragment else R.id.homeFragment
    findNavController().navigate(
        dest,
        null,
        NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build()
    )
}

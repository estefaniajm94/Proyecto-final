package com.estef.antiphishingcoach.presentation.common

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * Base para Fragments con ViewBinding y limpieza automática en onDestroyView.
 */
abstract class BaseFragment<VB : ViewBinding>(
    @LayoutRes layoutRes: Int,
    private val binder: (View) -> VB
) : Fragment(layoutRes) {

    private var _binding: VB? = null
    protected val binding: VB
        get() = _binding ?: error("ViewBinding no disponible fuera del ciclo de la vista.")

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = binder(view)
        onBoundView(savedInstanceState)
    }

    protected open fun onBoundView(savedInstanceState: Bundle?) = Unit

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

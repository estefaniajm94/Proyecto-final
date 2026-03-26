package com.estef.antiphishingcoach.presentation.navigation

import androidx.lifecycle.ViewModel
import com.estef.antiphishingcoach.core.model.SourceApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SharedAnalyzeInput(
    val inputText: String,
    val title: String?,
    val sourceApp: SourceApp
)

class SharedContentViewModel : ViewModel() {
    private val _pendingSharedInput = MutableStateFlow<SharedAnalyzeInput?>(null)
    val pendingSharedInput: StateFlow<SharedAnalyzeInput?> = _pendingSharedInput.asStateFlow()

    fun publish(input: SharedAnalyzeInput) {
        _pendingSharedInput.value = input
    }

    fun consume() {
        _pendingSharedInput.value = null
    }
}

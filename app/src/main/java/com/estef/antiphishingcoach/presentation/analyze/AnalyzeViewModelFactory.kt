package com.estef.antiphishingcoach.presentation.analyze

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.estef.antiphishingcoach.domain.usecase.AnalyzeAndPersistUseCase
import com.estef.antiphishingcoach.domain.usecase.ExtractTextFromImageUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveExtremePrivacyUseCase

class AnalyzeViewModelFactory(
    private val analyzeAndPersistUseCase: AnalyzeAndPersistUseCase,
    private val extractTextFromImageUseCase: ExtractTextFromImageUseCase,
    private val observeExtremePrivacyUseCase: ObserveExtremePrivacyUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnalyzeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AnalyzeViewModel(
                analyzeAndPersistUseCase = analyzeAndPersistUseCase,
                extractTextFromImageUseCase = extractTextFromImageUseCase,
                observeExtremePrivacyUseCase = observeExtremePrivacyUseCase
            ) as T
        }
        throw IllegalArgumentException("ViewModel no soportado: ${modelClass.name}")
    }
}

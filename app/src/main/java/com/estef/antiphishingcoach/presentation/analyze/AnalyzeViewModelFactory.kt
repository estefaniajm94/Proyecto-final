package com.estef.antiphishingcoach.presentation.analyze

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.estef.antiphishingcoach.domain.usecase.AnalyzeAndPersistUseCase
import com.estef.antiphishingcoach.domain.usecase.ExtractTextFromImageUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveExtremePrivacyUseCase
import com.estef.antiphishingcoach.presentation.common.StringResolver

class AnalyzeViewModelFactory(
    private val analyzeAndPersistUseCase: AnalyzeAndPersistUseCase,
    private val extractTextFromImageUseCase: ExtractTextFromImageUseCase,
    private val observeExtremePrivacyUseCase: ObserveExtremePrivacyUseCase,
    private val stringResolver: StringResolver
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnalyzeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AnalyzeViewModel(
                analyzeAndPersistUseCase = analyzeAndPersistUseCase,
                extractTextFromImageUseCase = extractTextFromImageUseCase,
                observeExtremePrivacyUseCase = observeExtremePrivacyUseCase,
                stringResolver = stringResolver
            ) as T
        }
        throw IllegalArgumentException("ViewModel no soportado: ${modelClass.name}")
    }
}

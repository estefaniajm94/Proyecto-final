package com.estef.antiphishingcoach.domain.usecase

import android.net.Uri
import com.estef.antiphishingcoach.core.common.DispatcherProvider
import com.estef.antiphishingcoach.domain.repository.OcrRepository
import kotlinx.coroutines.withContext

/**
 * Ejecuta OCR local on-device sobre una imagen seleccionada.
 */
class ExtractTextFromImageUseCase(
    private val repository: OcrRepository,
    private val dispatchers: DispatcherProvider = DispatcherProvider()
) {
    suspend operator fun invoke(uri: Uri): String {
        return withContext(dispatchers.default) {
            repository.extractTextFromImageUri(uri)
        }
    }
}

package com.estef.antiphishingcoach.domain.repository

import android.net.Uri

/**
 * OCR local sobre imagen seleccionada por el usuario.
 * Implementaciones deben procesar todo en dispositivo y sin persistir imagen/texto.
 */
interface OcrRepository {
    suspend fun extractTextFromImageUri(uri: Uri): String
}

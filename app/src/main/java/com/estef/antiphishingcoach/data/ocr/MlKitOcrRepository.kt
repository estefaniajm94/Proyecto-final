package com.estef.antiphishingcoach.data.ocr

import android.content.Context
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import com.estef.antiphishingcoach.domain.repository.OcrRepository
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Implementacion OCR on-device con ML Kit Text Recognition.
 */
class MlKitOcrRepository(
    private val appContext: Context
) : OcrRepository {

    override suspend fun extractTextFromImageUri(uri: Uri): String {
        val startedAt = SystemClock.elapsedRealtime()
        return try {
            val image = InputImage.fromFilePath(appContext, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val text = recognizer.process(image).awaitResult().text
            val normalized = normalizeOcrText(text)
            val elapsedMs = SystemClock.elapsedRealtime() - startedAt
            Log.d(TAG, "OCR local completado en ${elapsedMs}ms, chars=${normalized.length}")
            normalized
        } catch (error: Exception) {
            val elapsedMs = SystemClock.elapsedRealtime() - startedAt
            Log.e(TAG, "OCR local fallo en ${elapsedMs}ms", error)
            throw error
        }
    }

    private fun normalizeOcrText(raw: String): String {
        return raw
            .replace("\r\n", "\n")
            .replace('\r', '\n')
            .replace('\u00A0', ' ')
            .split('\n')
            .joinToString("\n") { line -> line.trimEnd() }
            .trim()
    }

    private suspend fun <T> Task<T>.awaitResult(): T {
        return suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result ->
                if (continuation.isActive) {
                    continuation.resume(result)
                }
            }
            addOnFailureListener { error ->
                if (continuation.isActive) {
                    continuation.resumeWithException(error)
                }
            }
            addOnCanceledListener {
                if (continuation.isActive) {
                    continuation.cancel()
                }
            }
        }
    }

    private companion object {
        private const val TAG = "MlKitOcrRepository"
    }
}

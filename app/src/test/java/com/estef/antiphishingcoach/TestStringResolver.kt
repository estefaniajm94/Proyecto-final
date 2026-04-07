package com.estef.antiphishingcoach

import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.presentation.common.StringResolver
import java.util.Locale

class TestStringResolver : StringResolver {
    override fun get(resId: Int, vararg formatArgs: Any): String {
        val template = when (resId) {
            R.string.history_empty -> "Aun no hay analisis guardados."
            R.string.history_empty_extreme_privacy -> "Privacidad extrema activa: no se estan guardando nuevos analisis."
            R.string.history_empty_filtered -> "No hay resultados para los filtros actuales."
            R.string.history_item_title_fallback -> "Analisis sin titulo"
            R.string.history_item_meta -> "Origen: %1\$s"
            R.string.history_item_created -> "Fecha: %1\$s"
            R.string.analyze_error_input_required -> "Introduce texto o enlace para analizar."
            R.string.analyze_status_analyzing -> "Analizando entrada..."
            R.string.analyze_error_analysis_failed -> "No se pudo completar el analisis. Intentalo de nuevo."
            R.string.analyze_status_image_selection_cancelled -> "Seleccion de imagen cancelada."
            R.string.analyze_status_ocr_processing -> "Procesando OCR local en el dispositivo..."
            R.string.analyze_error_no_text_detected -> "No se detecto texto en la imagen seleccionada."
            R.string.analyze_status_ocr_ready -> "Texto detectado listo para revision."
            R.string.analyze_error_ocr_failed -> "No se pudo extraer texto de la imagen."
            R.string.analyze_status_ocr_review_cancelled -> "Revision OCR cancelada."
            R.string.analyze_status_result_privacy_on -> "Privacidad extrema activa: analisis mostrado sin guardar historial."
            R.string.analyze_status_result_saved -> "Analisis guardado en historial privado."
            R.string.settings_data_cleared -> "Datos locales eliminados."
            R.string.settings_biometric_not_available -> "No hay biometria o credencial del dispositivo disponible para activar el bloqueo local."
            R.string.settings_auth_error -> "No se pudo autenticar para abrir Ajustes protegidos."
            R.string.settings_local_lock_on -> "Bloqueo local activado para Historial y Ajustes."
            R.string.settings_local_lock_off -> "Bloqueo local desactivado."
            R.string.settings_logout_done -> "Sesion cerrada."
            R.string.training_level_beginner -> "Principiante"
            R.string.training_level_intermediate -> "Intermedio"
            R.string.training_level_advanced -> "Avanzado"
            R.string.training_result_score -> "Aciertos: %1\$d/%2\$d"
            R.string.training_feedback_full -> "%1\$s\n%2\$s"
            R.string.home_latest_date -> "Fecha: %1\$s"
            R.string.home_latest_training_summary -> "Nivel: %1\$s\n%2\$s\n%3\$s"
            R.string.home_training_no_progress -> "Sin progreso de entrenamiento registrado."
            R.string.coach_bullet_line -> "\u2022 %1\$s"
            R.string.resources_call_button -> "Llamar %1\$s"
            else -> "res-$resId"
        }
        return String.format(Locale.ROOT, template, *formatArgs)
    }
}

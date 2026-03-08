package com.estef.antiphishingcoach.presentation.common

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.core.model.TrafficLight
import java.util.Locale

fun TrafficLight.toDisplayLabelEs(): String {
    return when (this) {
        TrafficLight.GREEN -> "VERDE"
        TrafficLight.YELLOW -> "AMARILLO"
        TrafficLight.RED -> "ROJO"
    }
}

@ColorRes
fun TrafficLight.toColorRes(): Int {
    return when (this) {
        TrafficLight.GREEN -> R.color.traffic_green
        TrafficLight.YELLOW -> R.color.traffic_yellow
        TrafficLight.RED -> R.color.traffic_red
    }
}

@DrawableRes
fun TrafficLight.toSeverityBarRes(): Int {
    return when (this) {
        TrafficLight.GREEN -> R.drawable.bg_severity_bar_green
        TrafficLight.YELLOW -> R.drawable.bg_severity_bar_yellow
        TrafficLight.RED -> R.drawable.bg_severity_bar_red
    }
}

fun String.toTrafficLightOrNull(): TrafficLight? {
    return when (trim().uppercase(Locale.ROOT)) {
        "GREEN", "VERDE" -> TrafficLight.GREEN
        "YELLOW", "AMARILLO" -> TrafficLight.YELLOW
        "RED", "ROJO" -> TrafficLight.RED
        else -> null
    }
}

@ColorRes
fun String.toTrafficColorRes(@ColorRes fallbackRes: Int = R.color.brand_on_surface): Int {
    return toTrafficLightOrNull()?.toColorRes() ?: fallbackRes
}

@DrawableRes
fun String.toTrafficSeverityBarRes(@DrawableRes fallbackRes: Int = R.drawable.bg_severity_bar_green): Int {
    return toTrafficLightOrNull()?.toSeverityBarRes() ?: fallbackRes
}

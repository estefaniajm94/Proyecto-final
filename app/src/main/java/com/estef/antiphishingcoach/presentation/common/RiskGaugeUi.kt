package com.estef.antiphishingcoach.presentation.common

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.databinding.ViewRiskGaugeBinding

fun ViewRiskGaugeBinding.renderRiskGauge(score: Int) {
    val safeScore = score.coerceIn(0, 100)
    val gaugeColor = root.context.resolveRiskGaugeColor(safeScore)
    progressRisk.setIndicatorColor(gaugeColor)
    progressRisk.trackColor = ColorUtils.setAlphaComponent(gaugeColor, 56)
    progressRisk.setProgressCompat(safeScore, true)
    tvRiskScore.text = safeScore.toString()
    tvRiskLabel.text = root.context.resolveRiskLabel(safeScore)
    tvRiskLabel.setTextColor(gaugeColor)
}

fun Context.resolveRiskLabel(score: Int): String {
    return when {
        score < 35 -> getString(R.string.risk_level_low)
        score < 70 -> getString(R.string.risk_level_medium)
        else -> getString(R.string.risk_level_high)
    }
}

fun Context.resolveRiskGaugeColor(score: Int): Int {
    val safeScore = score.coerceIn(0, 100)
    val green = ContextCompat.getColor(this, R.color.traffic_green)
    val yellow = ContextCompat.getColor(this, R.color.traffic_yellow)
    val red = ContextCompat.getColor(this, R.color.traffic_red)

    return if (safeScore <= 50) {
        ColorUtils.blendARGB(green, yellow, safeScore / 50f)
    } else {
        ColorUtils.blendARGB(yellow, red, (safeScore - 50) / 50f)
    }
}

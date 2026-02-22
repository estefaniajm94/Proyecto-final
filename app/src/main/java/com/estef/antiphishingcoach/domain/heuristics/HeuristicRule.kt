package com.estef.antiphishingcoach.domain.heuristics

import com.estef.antiphishingcoach.domain.model.DetectedSignal

/**
 * Contrato de regla explicable del analizador.
 */
fun interface HeuristicRule {
    fun evaluate(context: HeuristicContext): DetectedSignal?
}

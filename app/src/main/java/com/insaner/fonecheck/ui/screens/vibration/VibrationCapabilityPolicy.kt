package com.insaner.fonecheck.ui.screens.vibration

object VibrationCapabilityPolicy {
    fun supportedEffects(
        results: IntArray,
        supportedValue: Int,
    ): List<VibrationEffectCode> =
        results
            .take(VibrationEffectCode.entries.size)
            .mapIndexedNotNull { index, result ->
                VibrationEffectCode.entries[index].takeIf { result == supportedValue }
            }

    fun supportedPrimitives(results: BooleanArray): List<VibrationPrimitiveCode> =
        results
            .take(VibrationPrimitiveCode.entries.size)
            .mapIndexedNotNull { index, supported ->
                VibrationPrimitiveCode.entries[index].takeIf { supported }
            }
}

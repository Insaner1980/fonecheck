package com.insaner.fonecheck.ui.screens.vibration

import android.os.VibrationEffect
import android.os.Vibrator
import org.junit.Assert.assertEquals
import org.junit.Test

class VibrationCapabilityPolicyTest {
    private val expectedEffects =
        listOf(
            VibrationEffectCode.CLICK to VibrationEffect.EFFECT_CLICK,
            VibrationEffectCode.DOUBLE_CLICK to VibrationEffect.EFFECT_DOUBLE_CLICK,
            VibrationEffectCode.HEAVY_CLICK to VibrationEffect.EFFECT_HEAVY_CLICK,
            VibrationEffectCode.TICK to VibrationEffect.EFFECT_TICK,
        )

    private val expectedPrimitives =
        listOf(
            VibrationPrimitiveCode.CLICK to VibrationEffect.Composition.PRIMITIVE_CLICK,
            VibrationPrimitiveCode.THUD to VibrationEffect.Composition.PRIMITIVE_THUD,
            VibrationPrimitiveCode.SPIN to VibrationEffect.Composition.PRIMITIVE_SPIN,
            VibrationPrimitiveCode.QUICK_RISE to VibrationEffect.Composition.PRIMITIVE_QUICK_RISE,
            VibrationPrimitiveCode.SLOW_RISE to VibrationEffect.Composition.PRIMITIVE_SLOW_RISE,
            VibrationPrimitiveCode.QUICK_FALL to VibrationEffect.Composition.PRIMITIVE_QUICK_FALL,
            VibrationPrimitiveCode.TICK to VibrationEffect.Composition.PRIMITIVE_TICK,
            VibrationPrimitiveCode.LOW_TICK to VibrationEffect.Composition.PRIMITIVE_LOW_TICK,
        )

    @Test
    fun effectMetadataPreservesEveryAndroidIdAndQueryOrder() {
        expectedEffects.forEach { (effect, androidId) ->
            assertEquals(effect.name, androidId, effect.androidEffectId)
        }
        assertEquals(expectedEffects.map { it.first }, VibrationEffectCode.entries)
        assertEquals(
            expectedEffects.map { it.second },
            VibrationEffectCode.entries.map { it.androidEffectId },
        )
    }

    @Test
    fun primitiveMetadataPreservesEveryAndroidIdAndQueryOrder() {
        expectedPrimitives.forEach { (primitive, androidId) ->
            assertEquals(primitive.name, androidId, primitive.androidPrimitiveId)
        }
        assertEquals(expectedPrimitives.map { it.first }, VibrationPrimitiveCode.entries)
        assertEquals(
            expectedPrimitives.map { it.second },
            VibrationPrimitiveCode.entries.map { it.androidPrimitiveId },
        )
    }

    @Test
    fun eachEffectResponseMapsBackToTheQueriedCapability() {
        expectedEffects.forEach { (expectedEffect, supportedId) ->
            val results =
                VibrationEffectCode.entries
                    .map {
                        if (it.androidEffectId == supportedId) {
                            Vibrator.VIBRATION_EFFECT_SUPPORT_YES
                        } else {
                            Vibrator.VIBRATION_EFFECT_SUPPORT_NO
                        }
                    }.toIntArray()
            assertEquals(
                listOf(expectedEffect),
                VibrationCapabilityPolicy.supportedEffects(results, Vibrator.VIBRATION_EFFECT_SUPPORT_YES),
            )
        }
    }

    @Test
    fun eachPrimitiveResponseMapsBackToTheQueriedCapability() {
        expectedPrimitives.forEach { (expectedPrimitive, supportedId) ->
            val results =
                VibrationPrimitiveCode.entries.map { it.androidPrimitiveId == supportedId }.toBooleanArray()
            assertEquals(
                listOf(expectedPrimitive),
                VibrationCapabilityPolicy.supportedPrimitives(results),
            )
        }
    }

    @Test
    fun onlyDefiniteEffectSupportIsReported() {
        assertEquals(
            listOf(VibrationEffectCode.CLICK, VibrationEffectCode.TICK),
            VibrationCapabilityPolicy.supportedEffects(
                results = intArrayOf(1, 0, -1, 1),
                supportedValue = 1,
            ),
        )
    }

    @Test
    fun primitiveSupportPreservesTheDocumentedQueryOrder() {
        assertEquals(
            listOf(VibrationPrimitiveCode.CLICK, VibrationPrimitiveCode.SPIN),
            VibrationCapabilityPolicy.supportedPrimitives(
                booleanArrayOf(true, false, true, false, false, false, false, false),
            ),
        )
    }
}

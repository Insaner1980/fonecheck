package com.insaner.fonecheck

import android.content.ComponentName
import android.content.pm.FeatureInfo
import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalOnlyManifestTest {
    @Test
    fun emojiCompatDoesNotRegisterAutomaticDownloadableFontInitialization() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val provider =
            context.packageManager.getProviderInfo(
                ComponentName(context, "androidx.startup.InitializationProvider"),
                PackageManager.GET_META_DATA,
            )

        assertFalse(
            provider.metaData?.containsKey("androidx.emoji2.text.EmojiCompatInitializer") == true,
        )
    }

    @Test
    fun cameraAutofocusAndMicrophoneRemainOptionalInstallFeatures() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val features =
            context.packageManager
                .getPackageInfo(context.packageName, PackageManager.GET_CONFIGURATIONS)
                .reqFeatures
                .orEmpty()
                .associateBy(FeatureInfo::name)

        listOf(
            PackageManager.FEATURE_CAMERA_AUTOFOCUS,
            PackageManager.FEATURE_MICROPHONE,
        ).forEach { featureName ->
            val feature = features[featureName]
            assertTrue("$featureName must be declared", feature != null)
            assertTrue(
                "$featureName must not filter unsupported devices",
                feature != null && feature.flags and FeatureInfo.FLAG_REQUIRED == 0,
            )
        }
    }
}

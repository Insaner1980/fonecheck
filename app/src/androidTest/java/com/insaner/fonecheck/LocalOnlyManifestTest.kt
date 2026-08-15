package com.insaner.fonecheck

import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
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
}

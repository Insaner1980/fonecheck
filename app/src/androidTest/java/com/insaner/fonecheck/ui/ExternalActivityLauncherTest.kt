package com.insaner.fonecheck.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExternalActivityLauncherTest {
    @Test
    fun returnsTrueWhenExternalActivityStarts() {
        val context = RecordingContext(shouldThrow = false)

        assertTrue(context.startExternalActivity(Intent(Intent.ACTION_VIEW)))
        assertTrue(context.started)
    }

    @Test
    fun returnsFalseWhenNoExternalActivityCanHandleIntent() {
        val context = RecordingContext(shouldThrow = true)

        assertFalse(context.startExternalActivity(Intent(Intent.ACTION_VIEW)))
    }

    private class RecordingContext(
        private val shouldThrow: Boolean,
    ) : ContextWrapper(InstrumentationRegistry.getInstrumentation().targetContext) {
        var started = false
            private set

        override fun startActivity(intent: Intent) {
            if (shouldThrow) throw ActivityNotFoundException()
            started = true
        }
    }
}

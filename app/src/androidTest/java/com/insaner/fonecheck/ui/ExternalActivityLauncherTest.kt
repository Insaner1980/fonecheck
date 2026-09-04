package com.insaner.fonecheck.ui

import android.app.Activity
import android.content.ActivityNotFoundException
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
    fun nonActivityContextStartsTheIntentInANewTask() {
        val context = RecordingContext()

        assertTrue(context.startExternalActivity(Intent(Intent.ACTION_VIEW)))
        assertTrue(context.started)
        assertTrue(context.startedIntent!!.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test
    fun activityContextPreservesNormalActivityLaunchBehavior() {
        val activity = RecordingActivity()

        assertTrue(activity.startExternalActivity(Intent(Intent.ACTION_VIEW)))
        assertTrue(activity.started)
        assertFalse(activity.startedIntent!!.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test
    fun returnsFalseWhenNoExternalActivityCanHandleIntent() {
        val context = RecordingContext(ActivityNotFoundException())

        assertFalse(context.startExternalActivity(Intent(Intent.ACTION_VIEW)))
    }

    @Test
    fun returnsFalseWhenExternalActivityRejectsTheUriGrant() {
        val context = RecordingContext(SecurityException("URI grant rejected"))

        assertFalse(context.startExternalActivity(Intent(Intent.ACTION_SEND)))
    }

    private class RecordingContext(
        private val failure: RuntimeException? = null,
    ) : ContextWrapper(InstrumentationRegistry.getInstrumentation().targetContext) {
        var started = false
            private set
        var startedIntent: Intent? = null
            private set

        override fun startActivity(intent: Intent) {
            failure?.let { throw it }
            started = true
            startedIntent = intent
        }
    }

    private class RecordingActivity : Activity() {
        var started = false
            private set
        var startedIntent: Intent? = null
            private set

        override fun startActivity(intent: Intent) {
            started = true
            startedIntent = intent
        }
    }
}

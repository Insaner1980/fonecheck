package com.insaner.fonecheck.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent

internal fun Context.startExternalActivity(intent: Intent): Boolean =
    try {
        val launchIntent =
            Intent(intent).apply {
                if (this@startExternalActivity !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
        startActivity(launchIntent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }

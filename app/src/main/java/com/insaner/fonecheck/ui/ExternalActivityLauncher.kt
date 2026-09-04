package com.insaner.fonecheck.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent

internal fun Context.startExternalActivity(intent: Intent): Boolean =
    try {
        startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }

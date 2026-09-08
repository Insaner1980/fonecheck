package com.insaner.fonecheck.journey

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import dagger.hilt.android.AndroidEntryPoint

/** Debug-only lifecycle host: the test supplies a NavHost, restored by the normal Activity owners. */
@AndroidEntryPoint
class ConfigurationJourneyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { requireNotNull(content).invoke() }
    }

    companion object {
        var content: (@Composable () -> Unit)? = null
    }
}

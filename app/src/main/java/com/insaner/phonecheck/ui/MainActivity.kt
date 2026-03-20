package com.insaner.phonecheck.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.rememberNavController
import com.insaner.phonecheck.R
import com.insaner.phonecheck.navigation.PhoneCheckNavHost
import com.insaner.phonecheck.ui.theme.PhoneCheckTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhoneCheckTheme {
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(R.string.app_name)) },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                        )
                    },
                ) { innerPadding ->
                    PhoneCheckNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

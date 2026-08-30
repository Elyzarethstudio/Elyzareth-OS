package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.components.DesktopEnvironment
import com.example.ui.theme.ElyBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ElyzarethOSViewModel

class MainActivity : ComponentActivity() {

    // Elyzareth OS Root Entry Point
    private val osViewModel: ElyzarethOSViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        osViewModel.initializePersistentState(applicationContext)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    color = ElyBackground
                ) {
                    DesktopEnvironment(viewModel = osViewModel)
                }
            }
        }
    }
}

package com.sih2026.palash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.sih2026.palash.ui.navigation.PalashNavHost
import com.sih2026.palash.ui.theme.PalashTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.sih2026.palash.ui.theme.ThemeManager.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            PalashTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PalashNavHost(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

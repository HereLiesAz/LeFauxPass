// FILE: app/src/main/java/com/hereliesaz/videorecon/MainActivity.kt
package com.hereliesaz.rta_animation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface { ReconstructionScreen() }
            }
        }
    }
}

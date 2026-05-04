package com.molihuan.testmupdf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.molihuan.testmupdf.ui.screen.app.AppScreen
import com.molihuan.testmupdf.ui.theme.TestmupdfTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestmupdfTheme {
                AppScreen()
            }
        }
    }
}
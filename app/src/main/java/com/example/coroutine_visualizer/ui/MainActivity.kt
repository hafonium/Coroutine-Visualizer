package com.example.coroutine_visualizer.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.coroutine_visualizer.ui.CoroutineVisualizer
import com.example.coroutine_visualizer.ui.theme.CoroutinevisualizerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CoroutinevisualizerTheme {
                CoroutineVisualizer()
            }
        }
    }
}
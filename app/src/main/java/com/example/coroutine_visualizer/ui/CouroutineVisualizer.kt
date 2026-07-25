package com.example.coroutine_visualizer.ui

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.coroutine_visualizer.data.remote.repositories.CatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

private const val LINE_ANIMATION_DURATION_MS = 300

@Composable
fun CoroutineVisualizer() {
    val scope = rememberCoroutineScope()
    val repository = remember { CatRepository() }

    // Toggle mode: true = Coroutines (Non-blocking), false = Traditional Threads (Blocking)
    var useCoroutineMode by remember { mutableStateOf(true) }

    // Single thread executor to demonstrate thread starvation in blocking mode
    val singleThreadDispatcher = remember {
        Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "SingleWorkerThread")
        }.asCoroutineDispatcher()
    }

    // Progress states for lines
    var progressLine1 by remember { mutableFloatStateOf(0f) }
    var progressLine2 by remember { mutableFloatStateOf(0f) }
    var progressLine3 by remember { mutableFloatStateOf(0f) }

    // Actual runtime Thread names
    var threadLine1 by remember { mutableStateOf("Not running") }
    var threadLine2 by remember { mutableStateOf("Not running") }
    var threadLine3 by remember { mutableStateOf("Not running") }

    // Thought messages describing state transitions
    var thought1 by remember { mutableStateOf("Waiting to start...") }
    var thought2 by remember { mutableStateOf("Ready...") }
    var thought3 by remember { mutableStateOf("Idle...") }

    var catBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isCallingApi by remember { mutableStateOf(false) }

    // Animatable manages packet progress safely across suspension points
    val packetAnimatable = remember { Animatable(0f) }

    // Active Jobs management to prevent race conditions
    var jobLine1 by remember { mutableStateOf<Job?>(null) }
    var jobLine2 by remember { mutableStateOf<Job?>(null) }
    var jobApi by remember { mutableStateOf<Job?>(null) }

    // Utility function switching execution behahavior based on selected mode
    suspend fun smartWait(ms: Long) {
        if (useCoroutineMode) {
            delay(ms) // Suspend: Yield thread to other tasks
        } else {
            Thread.sleep(ms) // Block: Freeze thread execution
        }
    }

    CoroutineVisualizerContent(
        useCoroutineMode = useCoroutineMode,
        onToggleMode = { newMode ->
            useCoroutineMode = newMode
            // Cancel active jobs when switching execution modes
            jobLine1?.cancel()
            jobLine2?.cancel()
            jobApi?.cancel()

            progressLine1 = 0f
            progressLine2 = 0f
            progressLine3 = 0f
            threadLine1 = "Not running"
            threadLine2 = "Not running"
            threadLine3 = "Not running"
            catBitmap = null
            thought1 = "Mode changed. Ready to start..."
            thought2 = "Ready..."
            thought3 = "Idle..."
            isCallingApi = false
        },
        progressLine1 = progressLine1,
        progressLine2 = progressLine2,
        progressLine3 = progressLine3,
        threadLine1 = threadLine1,
        threadLine2 = threadLine2,
        threadLine3 = threadLine3,
        thought1 = thought1,
        thought2 = thought2,
        thought3 = thought3,
        catBitmap = catBitmap,
        isCallingApi = isCallingApi,
        packetProgress = packetAnimatable.value,
        lineAnimationDurationMillis = LINE_ANIMATION_DURATION_MS,
        onStartTasks = {
            jobLine1?.cancel()
            jobLine2?.cancel()
            jobApi?.cancel()

            progressLine1 = 0f
            progressLine2 = 0f
            progressLine3 = 0f
            threadLine1 = "Not running"
            threadLine2 = "Not running"
            threadLine3 = "Not running"
            catBitmap = null
            thought1 = "..."
            thought2 = "..."
            thought3 = "Idle..."
            isCallingApi = false

            scope.launch {
                packetAnimatable.snapTo(0f)
            }

            // Dispatcher selection: Uses singleThreadDispatcher in Blocking Mode to demonstrate starvation
            val targetDispatcher = if (useCoroutineMode) Dispatchers.Default else singleThreadDispatcher

            jobLine1 = scope.launch(targetDispatcher) {
                threadLine1 = Thread.currentThread().name
                while (progressLine1 < 1.0f && !isCallingApi) {
                    smartWait(600)
                    if (isCallingApi) break
                    progressLine1 += 0.04f
                }
                if (!isCallingApi) {
                    thought1 = "Line 1 Completed!"
                }
            }

            jobLine2 = scope.launch(targetDispatcher) {
                threadLine2 = Thread.currentThread().name
                while (progressLine2 < 1.0f) {
                    smartWait(600)
                    progressLine2 += 0.04f
                }
                thought2 = "Line 2 Completed!"
            }
        },
        onCallApi = {
            isCallingApi = true
            jobApi?.cancel()

            jobApi = scope.launch {
                delay(LINE_ANIMATION_DURATION_MS.toLong())

                thought1 = if (useCoroutineMode) {
                    "⏸️ Suspended! API calling on I/O. Yielding Worker Thread..."
                } else {
                    "⛔ BLOCKED!"
                }

                packetAnimatable.animateTo(0.5f, animationSpec = tween(800))

                val targetDispatcher = if (useCoroutineMode) Dispatchers.Default else singleThreadDispatcher

                scope.launch(targetDispatcher) {
                    threadLine3 = Thread.currentThread().name
                    thought3 = if (useCoroutineMode) {
                        "⚡ Line 3 occupying a Worker Thread!"
                    } else {
                        "⏳ Line 3 Starving! Waiting for thread release..."
                    }

                    while (progressLine3 < 1.0f) {
                        smartWait(300)
                        progressLine3 += 0.1f
                    }
                    thought3 = "Line 3 Completed!"
                }

                // Network API Call
                val bitmap = repository.getRandomCatImage()

                packetAnimatable.animateTo(1.0f, animationSpec = tween(800))
                catBitmap = bitmap

                withContext(targetDispatcher) {
                    threadLine1 = Thread.currentThread().name
                    thought1 = "▶️ Resumed on (${threadLine1})!"

                    while (progressLine1 < 1.0f) {
                        smartWait(600)
                        progressLine1 += 0.04f
                    }
                }

                delay(LINE_ANIMATION_DURATION_MS.toLong())
                thought1 = "Line 1 Completed!"
                isCallingApi = false
            }
        }
    )
}
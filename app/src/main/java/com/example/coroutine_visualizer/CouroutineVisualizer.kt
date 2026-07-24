package com.example.coroutine_visualizer

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.coroutine_visualizer.data.remote.repositories.CatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val PACKET_ANIMATION_DURATION_MS = 1_500
private const val LINE_ANIMATION_DURATION_MS = 300

@Composable
fun CoroutineVisualizer() {
    val scope = rememberCoroutineScope()
    val repository = remember { CatRepository() }

    var progressLine1 by remember { mutableFloatStateOf(0f) }
    var progressLine2 by remember { mutableFloatStateOf(0f) }
    var progressLine3 by remember { mutableFloatStateOf(0f) }

    var thought1 by remember { mutableStateOf("Waiting to start...") }
    var thought2 by remember { mutableStateOf("Ready...") }
    var thought3 by remember { mutableStateOf("Idle...") }

    var catBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isCallingApi by remember { mutableStateOf(false) }
    var packetProgress by remember { mutableFloatStateOf(0f) }

    CoroutineVisualizerContent(
        progressLine1 = progressLine1,
        progressLine2 = progressLine2,
        progressLine3 = progressLine3,
        thought1 = thought1,
        thought2 = thought2,
        thought3 = thought3,
        catBitmap = catBitmap,
        isCallingApi = isCallingApi,
        packetProgress = packetProgress,
        lineAnimationDurationMillis = LINE_ANIMATION_DURATION_MS,
        packetAnimationDurationMillis = PACKET_ANIMATION_DURATION_MS,
        onStartTasks = {
            progressLine1 = 0f
            progressLine2 = 0f
            progressLine3 = 0f
            packetProgress = 0f
            catBitmap = null
            thought1 = "Running on Main Thread..."
            thought2 = "Processing in background..."
            thought3 = "Idle..."
            isCallingApi = false

            // Coroutine 1: Main thread task
            // Calculating progress for Line 1 in the main thread
            scope.launch {
                while (progressLine1 < 1.0f && !isCallingApi) {
                    // Delay to simulate work being done on the main thread (drawing UI, handling user input, etc.)
                    delay(600)
                    if (isCallingApi) {
                        break
                    }
                    progressLine1 += 0.04f
                }
                if (!isCallingApi) {
                    thought1 = "Line 1 Completed!"
                }
            }

            // Coroutine 2: Background thread task
            // Calculating progress for Line 2 in the background thread
            scope.launch(Dispatchers.Default) {
                while (progressLine2 < 1.0f) {
                    delay(600)
                    progressLine2 += 0.04f
                }
                thought2 = "Line 2 Completed!"
            }
        },
        onCallApi = {
            isCallingApi = true
            scope.launch {
                // Wait for Line 1 to draw its latest calculated width before starting other visuals.
                delay(LINE_ANIMATION_DURATION_MS.toLong())

                thought1 = "⏸️ Suspended! Calling API & yielding thread..."
                // Simulating packet progress for the API call
                packetProgress = 0.5f

                // Coroutine 3: Tapping into the released thread to run a new task while waiting for the API response
                val job3 = scope.launch {
                    thought3 = "⚡ Thread released! Line 3 starts running!"
                    while (progressLine3 < 1.0f) {
                        delay(250)
                        progressLine3 += 0.2f
                    }
                    thought3 = "Line 3 Completed!"
                }

                // Suspending the coroutine 1 (releasing the main thread) to fetch the cat image via API
                val bitmap = repository.getRandomCatImage()

                // Wait for Line 3 to complete before resuming Line 1
                job3.join()
                // After the API call is complete, we can update the packet progress to indicate that the response has been received
                packetProgress = 1.0f

                // Wait until the response packet reaches the end of the track before displaying its payload.
                delay(PACKET_ANIMATION_DURATION_MS.toLong())
                catBitmap = bitmap
                thought1 = "▶️ Response received! Line 1 Resumed..."
                // Resuming the coroutine 1 (main thread) to continue its progress after the API call
                while (progressLine1 < 1.0f) {
                    delay(600)
                    progressLine1 += 0.04f
                }

                // Keep the request process visible until Line 1's final width is drawn.
                delay(LINE_ANIMATION_DURATION_MS.toLong())
                thought1 = "Line 1 Completed!"
                isCallingApi = false
            }
        }
    )
}

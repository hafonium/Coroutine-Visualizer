package com.example.coroutine_visualizer.ui

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coroutine_visualizer.R
import kotlin.math.abs

@Composable
fun CoroutineVisualizerContent(
    useCoroutineMode: Boolean,
    onToggleMode: (Boolean) -> Unit,
    progressLine1: Float,
    progressLine2: Float,
    progressLine3: Float,
    threadLine1: String,
    threadLine2: String,
    threadLine3: String,
    thought1: String,
    thought2: String,
    thought3: String,
    catBitmap: Bitmap?,
    isCallingApi: Boolean,
    packetProgress: Float,
    lineAnimationDurationMillis: Int,
    onStartTasks: () -> Unit,
    onCallApi: () -> Unit
) {
    // Smooth progress animation for each task line
    val animLine1 by animateFloatAsState(
        progressLine1,
        tween(lineAnimationDurationMillis),
        label = "line 1"
    )
    val animLine2 by animateFloatAsState(
        progressLine2,
        tween(lineAnimationDurationMillis),
        label = "line 2"
    )
    val animLine3 by animateFloatAsState(
        progressLine3,
        tween(lineAnimationDurationMillis),
        label = "line 3"
    )

    var hasStarted by remember { mutableStateOf(false) }
    var isApiCallPending by remember { mutableStateOf(false) }

    // Reset hasStarted when all lines return to 0 (e.g. on mode toggle or reset)
    LaunchedEffect(progressLine1, progressLine2, progressLine3) {
        if (progressLine1 == 0f && progressLine2 == 0f && progressLine3 == 0f) {
            hasStarted = false
        }
    }

    // Line completion check
    val allLinesFinished = (progressLine1 >= 1f || progressLine1 == 0f) &&
            (progressLine2 >= 1f || progressLine2 == 0f) &&
            (progressLine3 >= 1f || progressLine3 == 0f)

    // Mode toggle is enabled only when all lines and API call processes have finished
    val isToggleEnabled = allLinesFinished && !isCallingApi && !isApiCallPending

    val completedTargetsReached = progressLine1 >= 1f && progressLine2 >= 1f
    val completedAnimationsDrawn = animLine1 >= 1f && animLine2 >= 1f
    val isStartEnabled = !hasStarted || (completedTargetsReached && completedAnimationsDrawn)

    // Ensures Line 1 finishes drawing its current state before triggering API call
    LaunchedEffect(isApiCallPending, animLine1, progressLine1) {
        val line1DrawingFinished = abs(animLine1 - progressLine1) < 0.0001f
        if (isApiCallPending && line1DrawingFinished) {
            isApiCallPending = false
            onCallApi()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Safe padding for device status bar
        Spacer(Modifier.height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding()))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Text(
                "Coroutine Execution Visualizer",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Display current rendering UI Thread
        Text(
            text = "🖥️ UI Rendering on Thread: ${Thread.currentThread().name}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )

        Spacer(Modifier.height(12.dp))

        // Mode Switcher Toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                if (useCoroutineMode) "⚡ Coroutine Mode (Non-blocking)" else "⛔ Thread Blocking Mode",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (useCoroutineMode) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
            Switch(
                checked = useCoroutineMode,
                enabled = isToggleEnabled, // Disabled until lines and requests finish
                onCheckedChange = {
                    hasStarted = false // Reset explicitly on toggle
                    onToggleMode(it)
                }
            )
        }

        Spacer(Modifier.height(12.dp))

        // Control Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                enabled = isStartEnabled,
                onClick = {
                    hasStarted = true
                    onStartTasks()
                }
            ) {
                Text("1. Start Tasks")
            }
            Button(
                enabled = progressLine1 > 0f &&
                        progressLine1 < 1f &&
                        !isCallingApi &&
                        !isApiCallPending,
                onClick = {
                    isApiCallPending = true
                }
            ) {
                Text("2. Call API (Suspend)")
            }
        }

        Spacer(Modifier.height(20.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LineItem("Line 1 (Worker Thread / Suspending)", threadLine1, animLine1, Color(0xFF1976D2), thought1)
            LineItem("Line 2 (Worker Thread)", threadLine2, animLine2, Color(0xFF388E3C), thought2)
            LineItem("Line 3 (Worker Thread)", threadLine3, animLine3, Color(0xFFD81B60), thought3)
        }

        Spacer(Modifier.height(20.dp))
        if (isCallingApi) {
            PacketAnimation(packetProgress)
        }

        Spacer(Modifier.height(12.dp))
        catBitmap?.let { CatResponse(it) }
    }
}

@Composable
private fun PacketAnimation(progress: Float) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(Modifier.padding(10.dp)) {
            Text(
                if (progress < 0.8f) "✉️ Sending Request to Server..."
                else "📦 Returning Response Payload...",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Canvas(Modifier.fillMaxWidth().height(10.dp)) {
                val yOffset = 5.dp.toPx()
                drawLine(
                    Color.LightGray,
                    Offset(0f, yOffset),
                    Offset(size.width, yOffset),
                    2.dp.toPx()
                )
                drawCircle(
                    if (progress < 0.8f) Color(0xFFFF9800) else Color(0xFF4CAF50),
                    6.dp.toPx(),
                    Offset(size.width * progress.coerceIn(0f, 1f), yOffset)
                )
            }
        }
    }
}

@Composable
private fun CatResponse(bitmap: Bitmap) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🐱 Response Payload Received:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Cat Image Response",
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            )
        }
    }
}

@Composable
private fun LineItem(
    label: String,
    threadName: String,
    progress: Float,
    color: Color,
    thought: String
) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("${(progress * 100).toInt()}%", fontSize = 12.sp, color = Color.Gray)
        }

        // Render actual system thread name running the task
        Text("🧵 Thread: $threadName", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF00838F))

        Text("💬 $thought", fontSize = 12.sp, color = Color(0xFF555555))
        Spacer(Modifier.height(4.dp))
        Canvas(Modifier.fillMaxWidth().height(14.dp)) {
            drawRoundRect(
                Color.LightGray.copy(alpha = 0.3f),
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(7.dp.toPx())
            )
            if (progress > 0f) {
                drawRoundRect(
                    color,
                    size = Size(size.width * progress.coerceIn(0f, 1f), size.height),
                    cornerRadius = CornerRadius(7.dp.toPx())
                )
            }
        }
    }
}

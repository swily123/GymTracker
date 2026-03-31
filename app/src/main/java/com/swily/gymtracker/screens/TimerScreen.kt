package com.swily.gymtracker.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swily.gymtracker.ui.theme.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TimerScreen(
    onBack: () -> Unit = {}
) {
    val presets = listOf(30, 60, 90, 120, 180, 300)
    val presetLabels = listOf("30с", "1:00", "1:30", "2:00", "3:00", "5:00")

    var selectedSeconds by remember { mutableIntStateOf(90) }
    var secondsLeft by remember { mutableIntStateOf(90) }
    var totalSeconds by remember { mutableIntStateOf(90) }
    var isRunning by remember { mutableStateOf(false) }
    var timerJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()

    val progress = if (totalSeconds > 0) secondsLeft.toFloat() / totalSeconds else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "timer_progress"
    )

    val minutes = secondsLeft / 60
    val secs = secondsLeft % 60
    val formattedTime = String.format("%02d:%02d", minutes, secs)

    // Функции управления
    fun startTimer() {
        isRunning = true
        timerJob?.cancel()
        timerJob = scope.launch {
            while (secondsLeft > 0) {
                delay(1000)
                secondsLeft -= 1
            }
            isRunning = false
        }
    }

    fun pauseTimer() {
        isRunning = false
        timerJob?.cancel()
    }

    fun stopTimer() {
        isRunning = false
        timerJob?.cancel()
        secondsLeft = selectedSeconds
        totalSeconds = selectedSeconds
    }

    fun addMinute() {
        secondsLeft += 60
        totalSeconds += 60
    }

    fun selectPreset(seconds: Int) {
        if (!isRunning) {
            selectedSeconds = seconds
            secondsLeft = seconds
            totalSeconds = seconds
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Шапка
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = TextWhite,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onBack() }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Таймер",
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.weight(0.3f))

        // Круговой таймер
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(220.dp),
                    strokeWidth = 8.dp,
                    color = Orange,
                    trackColor = DarkSurfaceLight,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formattedTime,
                        color = TextWhite,
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isRunning) "идёт отсчёт" else "выбери время",
                        color = TextGray,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Пресеты времени
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEachIndexed { index, seconds ->
                val isSelected = selectedSeconds == seconds && !isRunning
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Orange else DarkSurface)
                        .clickable { selectPreset(seconds) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = presetLabels[index],
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Кнопки управления
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Стоп
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .clickable { stopTimer() },
                contentAlignment = Alignment.Center
            ) {
                Text("⏹", fontSize = 20.sp)
            }

            // Старт/Пауза
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(CircleShape)
                    .background(Orange)
                    .clickable {
                        if (isRunning) pauseTimer() else startTimer()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isRunning) "⏸" else "▶",
                    fontSize = 24.sp
                )
            }

            // +1 мин
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .clickable { addMinute() },
                contentAlignment = Alignment.Center
            ) {
                Text("+1м", color = TextGray, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))
    }
}
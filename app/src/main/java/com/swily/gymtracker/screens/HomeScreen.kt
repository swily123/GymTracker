package com.swily.gymtracker.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swily.gymtracker.ui.theme.*
import com.swily.gymtracker.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.swily.gymtracker.WeightUtils

@Composable
fun HomeScreen(
    onStartWorkout: () -> Unit = {},
    onOpenTimer: () -> Unit = {},
    useKg: Boolean = true,
    homeViewModel: HomeViewModel = viewModel()
) {
    val lastSession by homeViewModel.lastSession.collectAsState(initial = null)

    val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale("ru"))
    val today = dateFormat.format(Date())
    val profile by homeViewModel.profile.collectAsState(initial = null)

    // Советы дня
    val tips = listOf(
        "Не забывай про разминку перед тяжёлыми подходами!",
        "Пей воду между подходами — обезвоживание снижает силу.",
        "Контролируй негативную фазу — опускай вес на 2-3 секунды.",
        "Следи за дыханием: выдох на усилии, вдох на расслаблении.",
        "Хороший сон = хороший прогресс. Спи минимум 7 часов.",
        "Не гонись за весом — техника важнее.",
        "Разнообразие — ключ к прогрессу. Меняй упражнения раз в месяц."
    )
    val tipOfDay = tips[(System.currentTimeMillis() / (1000 * 60 * 60 * 24) % tips.size).toInt()]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Приветствие
        Text(
            text = "Привет, ${profile?.name ?: "Атлет"}!",
            color = TextWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = today,
            color = TextGray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Баннер "Готов к тренировке?"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Green)
                .clickable { onStartWorkout() }
                .padding(24.dp)
        ) {
            Column {
                Text("🏋", fontSize = 32.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Готов к тренировке?",
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Выбери программу и начни прямо сейчас",
                    color = TextWhite.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TextWhite)
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Начать →",
                        color = Green,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Последняя тренировка
        Text(
            text = "Последняя тренировка",
            color = TextWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (lastSession != null) {
            val session = lastSession!!
            val sessionDate = SimpleDateFormat("d MMM, HH:mm", Locale("ru")).format(Date(session.startTime))
            val durationMin = session.durationSeconds / 60

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Тренировка",
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(text = ">", color = TextGray, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$sessionDate · ${durationMin} мин · ${session.exerciseCount} упражнений",
                        color = TextGray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Объём: ${WeightUtils.format(session.totalVolumeKg, useKg)}",
                        color = Orange,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Пока нет завершённых тренировок. Начни первую!",
                    color = TextGray,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Совет дня
        Text(
            text = "Совет дня",
            color = TextWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .padding(16.dp)
        ) {
            Row {
                Text("💡", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tipOfDay,
                    color = TextGray,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Spacer(modifier = Modifier.height(20.dp))

        // Таймер
        Text(
            text = "Таймер",
            color = TextWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .clickable { onOpenTimer() }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⏱", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "00:00", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Orange)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("▶", color = TextWhite, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
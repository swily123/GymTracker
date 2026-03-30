package com.swily.gymtracker.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swily.gymtracker.data.model.Exercise
import com.swily.gymtracker.ui.theme.*
import com.swily.gymtracker.viewmodel.ProgressViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = viewModel()
) {
    val exercises by viewModel.allExercises.collectAsState(initial = emptyList())
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Прогресс",
            color = TextWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TabButton(
                text = "Графики",
                isSelected = selectedTab == 0,
                onClick = { selectedTab = 0 }
            )
            TabButton(
                text = "Рекорды",
                isSelected = selectedTab == 1,
                onClick = { selectedTab = 1 }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> GraphsTab(exercises = exercises, viewModel = viewModel)
            1 -> RecordsTab(exercises = exercises, viewModel = viewModel)
        }
    }
}

@Composable
fun GraphsTab(exercises: List<Exercise>, viewModel: ProgressViewModel) {
    if (exercises.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Нет данных для отображения", color = TextGray, fontSize = 14.sp)
        }
        return
    }

    LazyColumn {
        items(exercises) { exercise ->
            ExerciseProgressCard(exercise = exercise, viewModel = viewModel)
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun ExerciseProgressCard(exercise: Exercise, viewModel: ProgressViewModel) {
    val logs by viewModel.getLogsForExercise(exercise.id).collectAsState(initial = emptyList())

    if (logs.isEmpty()) return

    val dateFormat = SimpleDateFormat("dd.MM", Locale("ru"))
    val grouped = logs
        .groupBy { dateFormat.format(Date(it.completedAt)) }
        .map { (_, dayLogs) -> dayLogs.maxByOrNull { it.weightKg }!! }
        .takeLast(10)

    if (grouped.size < 2) {
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exercise.name,
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${grouped.last().weightKg.toInt()} кг",
                    color = Orange,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        return
    }

    val weights = grouped.map { it.weightKg }
    val firstWeight = weights.first()
    val lastWeight = weights.last()
    val change = lastWeight - firstWeight
    val changePercent = if (firstWeight > 0) (change / firstWeight * 100) else 0f

    Spacer(modifier = Modifier.height(8.dp))
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
                    text = exercise.name,
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${if (change >= 0) "↑" else "↓"} ${String.format("%.0f", kotlin.math.abs(change))} кг (${String.format("%.0f", kotlin.math.abs(changePercent))}%)",
                    color = if (change >= 0) Green else Orange,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SimpleLineChart(
                values = weights,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${firstWeight.toInt()} кг",
                    color = TextGray,
                    fontSize = 12.sp
                )
                Text(
                    text = "${lastWeight.toInt()} кг",
                    color = Orange,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SimpleLineChart(
    values: List<Float>,
    modifier: Modifier = Modifier
) {
    val lineColor = Orange
    val dotColor = Orange

    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas

        val maxVal = values.max()
        val minVal = values.min()
        val range = if (maxVal - minVal > 0) maxVal - minVal else 1f

        val stepX = size.width / (values.size - 1)
        val padding = 8f

        val points = values.mapIndexed { index, value ->
            val x = index * stepX
            val y = padding + (1f - (value - minVal) / range) * (size.height - padding * 2)
            Offset(x, y)
        }

        val path = Path()
        points.forEachIndexed { index, point ->
            if (index == 0) path.moveTo(point.x, point.y)
            else path.lineTo(point.x, point.y)
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        points.forEach { point ->
            drawCircle(color = dotColor, radius = 4f, center = point)
        }

        points.lastOrNull()?.let { last ->
            drawCircle(color = dotColor, radius = 7f, center = last)
        }
    }
}

@Composable
fun RecordsTab(exercises: List<Exercise>, viewModel: ProgressViewModel) {
    LazyColumn {
        items(exercises) { exercise ->
            RecordCard(exercise = exercise, viewModel = viewModel)
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun RecordCard(exercise: Exercise, viewModel: ProgressViewModel) {
    val logs by viewModel.getLogsForExercise(exercise.id).collectAsState(initial = emptyList())

    if (logs.isEmpty()) return

    val maxWeight = logs.maxOf { it.weightKg }
    val maxWeightLog = logs.first { it.weightKg == maxWeight }
    val date = SimpleDateFormat("d MMM yyyy", Locale("ru")).format(Date(maxWeightLog.completedAt))

    Spacer(modifier = Modifier.height(8.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = exercise.name,
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = date,
                    color = TextGray,
                    fontSize = 12.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${maxWeight.toInt()} кг",
                    color = Orange,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${maxWeightLog.reps} повт.",
                    color = TextGray,
                    fontSize = 12.sp
                )
            }
        }
    }
}
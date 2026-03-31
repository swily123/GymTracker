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
import com.swily.gymtracker.data.model.Settings
import com.swily.gymtracker.data.model.UserProfile
import com.swily.gymtracker.ui.theme.*
import com.swily.gymtracker.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel()
) {
    val profile by viewModel.profile.collectAsState(initial = null)
    val settings by viewModel.settings.collectAsState(initial = null)
    val totalWorkouts by viewModel.totalWorkouts.collectAsState(initial = 0)
    val totalVolume by viewModel.totalVolume.collectAsState(initial = 0f)

    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditHeightDialog by remember { mutableStateOf(false) }
    var showEditWeightDialog by remember { mutableStateOf(false) }

    // Диалог редактирования имени
    if (showEditNameDialog && profile != null) {
        TextEditDialog(
            title = "Изменить имя",
            currentValue = profile!!.name,
            onConfirm = { newName ->
                viewModel.updateProfile(profile!!.copy(name = newName))
                showEditNameDialog = false
            },
            onDismiss = { showEditNameDialog = false }
        )
    }

    // Диалог редактирования роста
    if (showEditHeightDialog && profile != null) {
        NumberEditDialog(
            title = "Изменить рост (см)",
            currentValue = "${profile!!.heightCm}",
            onConfirm = { newValue ->
                newValue.toIntOrNull()?.let {
                    viewModel.updateProfile(profile!!.copy(heightCm = it))
                }
                showEditHeightDialog = false
            },
            onDismiss = { showEditHeightDialog = false }
        )
    }

    // Диалог редактирования веса
    if (showEditWeightDialog && profile != null) {
        NumberEditDialog(
            title = "Изменить вес (кг)",
            currentValue = "${profile!!.weightKg}",
            onConfirm = { newValue ->
                newValue.toFloatOrNull()?.let {
                    viewModel.updateProfile(profile!!.copy(weightKg = it))
                }
                showEditWeightDialog = false
            },
            onDismiss = { showEditWeightDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Шапка профиля
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🔥", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = profile?.name ?: "Пользователь",
                color = TextWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { showEditNameDialog = true }
            )
            Spacer(modifier = Modifier.height(4.dp))
            val startDate = profile?.trainingStartDate?.let {
                SimpleDateFormat("MMMM yyyy", Locale("ru")).format(Date(it))
            } ?: ""
            Text(
                text = "Тренируется с $startDate",
                color = TextGray,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Статистика
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProfileStatCard(
                value = "$totalWorkouts",
                label = "тренировок",
                modifier = Modifier.weight(1f)
            )
            ProfileStatCard(
                value = String.format("%.1fт", totalVolume / 1000),
                label = "тоннаж",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Параметры тела
        Text(
            text = "Параметры тела",
            color = TextWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Рост
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .clickable { showEditHeightDialog = true }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📏", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Рост", color = TextWhite, fontSize = 16.sp)
                }
                Text(
                    text = "${profile?.heightCm ?: 0} см",
                    color = Orange,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Вес
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .clickable { showEditWeightDialog = true }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚖", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Вес", color = TextWhite, fontSize = 16.sp)
                }
                Text(
                    text = "${profile?.weightKg ?: 0} кг",
                    color = Orange,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Настройки
        Text(
            text = "Настройки",
            color = TextWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Отдых между подходами
        SettingsRow(
            icon = "⏱",
            title = "Отдых между подходами",
            value = "${(settings?.restBetweenSetsSec ?: 90)}с",
            onDecrease = {
                settings?.let {
                    val newVal = (it.restBetweenSetsSec - 15).coerceAtLeast(15)
                    viewModel.updateSettings(it.copy(restBetweenSetsSec = newVal))
                }
            },
            onIncrease = {
                settings?.let {
                    val newVal = (it.restBetweenSetsSec + 15).coerceAtMost(600)
                    viewModel.updateSettings(it.copy(restBetweenSetsSec = newVal))
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Отдых между упражнениями
        SettingsRow(
            icon = "⏱",
            title = "Отдых между упражнениями",
            value = "${(settings?.restBetweenExercisesSec ?: 300)}с",
            onDecrease = {
                settings?.let {
                    val newVal = (it.restBetweenExercisesSec - 15).coerceAtLeast(15)
                    viewModel.updateSettings(it.copy(restBetweenExercisesSec = newVal))
                }
            },
            onIncrease = {
                settings?.let {
                    val newVal = (it.restBetweenExercisesSec + 15).coerceAtMost(600)
                    viewModel.updateSettings(it.copy(restBetweenExercisesSec = newVal))
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Вибрация
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📳", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Вибрация", color = TextWhite, fontSize = 16.sp)
                }
                Switch(
                    checked = settings?.vibrationEnabled ?: true,
                    onCheckedChange = { enabled ->
                        settings?.let {
                            viewModel.updateSettings(it.copy(vibrationEnabled = enabled))
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = TextWhite,
                        checkedTrackColor = Orange,
                        uncheckedThumbColor = TextGray,
                        uncheckedTrackColor = DarkSurfaceLight
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Единицы измерения
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔄", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Единицы", color = TextWhite, fontSize = 16.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val useKg = settings?.useKg ?: true
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (useKg) Orange else DarkSurfaceLight)
                            .clickable {
                                settings?.let {
                                    viewModel.updateSettings(it.copy(useKg = true))
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("kg", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!useKg) Orange else DarkSurfaceLight)
                            .clickable {
                                settings?.let {
                                    viewModel.updateSettings(it.copy(useKg = false))
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("lb", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun ProfileStatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                color = Orange,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, color = TextGray, fontSize = 12.sp)
        }
    }
}

@Composable
fun SettingsRow(
    icon: String,
    title: String,
    value: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(icon, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, color = TextWhite, fontSize = 14.sp)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceLight)
                        .clickable { onDecrease() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("-", color = TextWhite, fontSize = 18.sp)
                }
                Text(
                    text = value,
                    color = Orange,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceLight)
                        .clickable { onIncrease() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = TextWhite, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun TextEditDialog(
    title: String,
    currentValue: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var value by remember { mutableStateOf(currentValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = TextWhite) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Orange,
                    unfocusedBorderColor = DarkSurfaceLight,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    cursorColor = Orange
                ),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value) }) {
                Text("OK", color = Orange)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = TextGray)
            }
        },
        containerColor = DarkSurface
    )
}
package com.lovekey.ime.ui.host

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(context: Context) {
    val scope = rememberCoroutineScope()

    // States from DataStore
    val hapticFlow = remember(context) { context.dataStore.data.map { it[SettingsKeys.ENABLE_HAPTICS] ?: true } }
    val hapticEnabled by hapticFlow.collectAsState(initial = true)

    val typoFlow = remember(context) { context.dataStore.data.map { it[SettingsKeys.ENABLE_TYPO_CORRECTION] ?: true } }
    val typoEnabled by typoFlow.collectAsState(initial = true)

    val flow1 = remember(context) { context.dataStore.data.map { it[SettingsKeys.FUZZY_ZH_Z] ?: false } }
    val fuzzyZhZ by flow1.collectAsState(initial = false)
    val flow2 = remember(context) { context.dataStore.data.map { it[SettingsKeys.FUZZY_CH_C] ?: false } }
    val fuzzyChC by flow2.collectAsState(initial = false)
    val flow3 = remember(context) { context.dataStore.data.map { it[SettingsKeys.FUZZY_SH_S] ?: false } }
    val fuzzyShS by flow3.collectAsState(initial = false)
    val flow4 = remember(context) { context.dataStore.data.map { it[SettingsKeys.FUZZY_N_L] ?: false } }
    val fuzzyNL by flow4.collectAsState(initial = false)
    val flow5 = remember(context) { context.dataStore.data.map { it[SettingsKeys.FUZZY_EN_ENG] ?: false } }
    val fuzzyEnEng by flow5.collectAsState(initial = false)
    val flow6 = remember(context) { context.dataStore.data.map { it[SettingsKeys.FUZZY_IN_ING] ?: false } }
    val fuzzyInIng by flow6.collectAsState(initial = false)
    val flow7 = remember(context) { context.dataStore.data.map { it[SettingsKeys.FUZZY_AN_ANG] ?: false } }
    val fuzzyAnAng by flow7.collectAsState(initial = false)

    var testText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0F5))
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Immediate Test Area
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Try your keyboard here \uD83D\uDC96",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD81B60),
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = testText,
                        onValueChange = { testText = it },
                        placeholder = { Text("Tap to type...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE91E63),
                            unfocusedBorderColor = Color(0xFFF48FB1),
                            focusedContainerColor = Color(0xFFFFF0F5),
                            unfocusedContainerColor = Color(0xFFFFF0F5)
                        )
                    )
                }
            }
        }

        // Section 2: Feel & Feedback
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Feel & Feedback",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD81B60),
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Haptic Feedback (Vibration)", fontSize = 16.sp)
                            Text("Feel each tap", color = Color.Gray, fontSize = 12.sp)
                        }
                        Switch(
                            checked = hapticEnabled,
                            onCheckedChange = { checked ->
                                scope.launch { context.dataStore.edit { it[SettingsKeys.ENABLE_HAPTICS] = checked } }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFE91E63), checkedTrackColor = Color(0xFFFCE4EC))
                        )
                    }
                }
            }
        }

        // Section 3: Smart Input Brain
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Smart Input Brain",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD81B60),
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Typo Correction
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Typo Correction", fontSize = 16.sp)
                            Text("e.g. qign -> qing", color = Color.Gray, fontSize = 12.sp)
                        }
                        Switch(
                            checked = typoEnabled,
                            onCheckedChange = { checked ->
                                scope.launch { context.dataStore.edit { it[SettingsKeys.ENABLE_TYPO_CORRECTION] = checked } }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFE91E63), checkedTrackColor = Color(0xFFFCE4EC))
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Fuzzy Pinyin Toggles
                    Text("Fuzzy Pinyin (模糊音)", fontWeight = FontWeight.Bold, color = Color(0xFFD81B60), modifier = Modifier.padding(bottom = 8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FuzzyChip("z / zh", fuzzyZhZ) { checked -> scope.launch { context.dataStore.edit { it[SettingsKeys.FUZZY_ZH_Z] = checked } } }
                        FuzzyChip("c / ch", fuzzyChC) { checked -> scope.launch { context.dataStore.edit { it[SettingsKeys.FUZZY_CH_C] = checked } } }
                        FuzzyChip("s / sh", fuzzyShS) { checked -> scope.launch { context.dataStore.edit { it[SettingsKeys.FUZZY_SH_S] = checked } } }
                        FuzzyChip("n / l", fuzzyNL) { checked -> scope.launch { context.dataStore.edit { it[SettingsKeys.FUZZY_N_L] = checked } } }
                        FuzzyChip("en / eng", fuzzyEnEng) { checked -> scope.launch { context.dataStore.edit { it[SettingsKeys.FUZZY_EN_ENG] = checked } } }
                        FuzzyChip("in / ing", fuzzyInIng) { checked -> scope.launch { context.dataStore.edit { it[SettingsKeys.FUZZY_IN_ING] = checked } } }
                        FuzzyChip("an / ang", fuzzyAnAng) { checked -> scope.launch { context.dataStore.edit { it[SettingsKeys.FUZZY_AN_ANG] = checked } } }
                    }
                }
            }
        }
    }
}

@Composable
fun FuzzyChip(label: String, isSelected: Boolean, onToggle: (Boolean) -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0xFFE91E63) else Color(0xFFF5F5F5))
            .clickable { onToggle(!isSelected) }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else Color.DarkGray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

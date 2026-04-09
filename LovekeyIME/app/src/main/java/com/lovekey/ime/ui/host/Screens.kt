package com.lovekey.ime.ui.host

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object SettingsKeys {
                        val CUSTOM_KEY_ALPHA = floatPreferencesKey("custom_key_alpha")
    val SHORTCUT_PHRASES = stringPreferencesKey("shortcut_phrases")
    val CLIPBOARD_HISTORY = stringPreferencesKey("clipboard_history")
    val AFFECTION_SCORE = intPreferencesKey("affection_score")
    val PERSONA_ID = stringPreferencesKey("persona_id")
    val ENABLE_TYPO_CORRECTION = booleanPreferencesKey("enable_typo_correction")
    val FUZZY_ZH_Z = booleanPreferencesKey("fuzzy_zh_z")
    val FUZZY_CH_C = booleanPreferencesKey("fuzzy_ch_c")
    val FUZZY_SH_S = booleanPreferencesKey("fuzzy_sh_s")
    val FUZZY_N_L = booleanPreferencesKey("fuzzy_n_l")
    val FUZZY_EN_ENG = booleanPreferencesKey("fuzzy_en_eng")
    val FUZZY_IN_ING = booleanPreferencesKey("fuzzy_in_ing")
    val FUZZY_AN_ANG = booleanPreferencesKey("fuzzy_an_ang")
}


@Composable
fun OnboardingScreen(context: Context, onFinish: () -> Unit) {
    var step by remember { mutableStateOf(1) }
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    // Periodically check if the IME is enabled
    LaunchedEffect(step) {
        if (step == 1) {
            while (true) {
                val enabledImes = imm.enabledInputMethodList
                if (enabledImes.any { it.packageName == context.packageName }) {
                    step = 2
                    break
                }
                delay(1000)
            }
        } else if (step == 2) {
            while (true) {
                val currentImeId = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
                if (currentImeId != null && currentImeId.contains(context.packageName)) {
                    step = 3
                    break
                }
                delay(1000)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0F5)) // Cute pink background
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hi, I'm Lovekey!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD81B60)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Let's get you set up to type faster and happier.",
            fontSize = 18.sp,
            color = Color(0xFF880E4F),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))

        // Step 1: Enable
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = if (step == 1) Color.White else Color(0xFFFCE4EC)),
            elevation = CardDefaults.cardElevation(defaultElevation = if (step == 1) 4.dp else 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Step 1: Enable Lovekey", fontWeight = FontWeight.Bold, color = Color(0xFFD81B60))
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                    },
                    enabled = step == 1,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                ) {
                    Text(if (step > 1) "Enabled!" else "Enable in Settings")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Step 2: Set as Default
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = if (step == 2) Color.White else Color(0xFFFCE4EC)),
            elevation = CardDefaults.cardElevation(defaultElevation = if (step == 2) 4.dp else 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Step 2: Set as Default", fontWeight = FontWeight.Bold, color = Color(0xFFD81B60))
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        imm.showInputMethodPicker()
                    },
                    enabled = step == 2,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                ) {
                    Text(if (step > 2) "Default Set!" else "Choose Lovekey")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Step 3: Success & Test
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = if (step == 3) Color.White else Color(0xFFFCE4EC)),
            elevation = CardDefaults.cardElevation(defaultElevation = if (step == 3) 4.dp else 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Step 3: You're all set!", fontWeight = FontWeight.Bold, color = Color(0xFFD81B60))
                Spacer(modifier = Modifier.height(8.dp))
                var testText by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = testText,
                    onValueChange = { testText = it },
                    placeholder = { Text("Try typing here...") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = step == 3
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onFinish,
                    enabled = step == 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                ) {
                    Text("Go to Settings")
                }
            }
        }
    }
}

@Composable
fun SettingsHomeScreen(context: Context, onNavigateToSmartInput: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0F5))
            .padding(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFFE91E63), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("L", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Lovekey Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFFD81B60))
                Text("Make typing a joy again!", color = Color(0xFF880E4F))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Settings list
        Card(
            modifier = Modifier.fillMaxWidth().clickable { onNavigateToSmartInput() },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Smart Input", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFD81B60))
                    Text("Fuzzy pinyin, typo correction", color = Color.Gray, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(">", color = Color(0xFFE91E63), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Typing Feel", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFD81B60))
                    Text("Vibration, sound (Coming soon)", color = Color.Gray, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(">", color = Color(0xFFE91E63), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SmartInputScreen(context: Context, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val typoFlow = remember { context.dataStore.data.map { it[SettingsKeys.ENABLE_TYPO_CORRECTION] ?: true } }
    val typoEnabled by typoFlow.collectAsState(initial = true)

    val flow1 = remember { context.dataStore.data.map { it[SettingsKeys.FUZZY_ZH_Z] ?: false } }
    val fuzzyZhZ by flow1.collectAsState(initial = false)
    val flow2 = remember { context.dataStore.data.map { it[SettingsKeys.FUZZY_CH_C] ?: false } }
    val fuzzyChC by flow2.collectAsState(initial = false)
    val flow3 = remember { context.dataStore.data.map { it[SettingsKeys.FUZZY_SH_S] ?: false } }
    val fuzzyShS by flow3.collectAsState(initial = false)
    val flow4 = remember { context.dataStore.data.map { it[SettingsKeys.FUZZY_N_L] ?: false } }
    val fuzzyNL by flow4.collectAsState(initial = false)
    val flow5 = remember { context.dataStore.data.map { it[SettingsKeys.FUZZY_EN_ENG] ?: false } }
    val fuzzyEnEng by flow5.collectAsState(initial = false)
    val flow6 = remember { context.dataStore.data.map { it[SettingsKeys.FUZZY_IN_ING] ?: false } }
    val fuzzyInIng by flow6.collectAsState(initial = false)
    val flow7 = remember { context.dataStore.data.map { it[SettingsKeys.FUZZY_AN_ANG] ?: false } }
    val fuzzyAnAng by flow7.collectAsState(initial = false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0F5))
    ) {
        // Toolbar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)
        ) {
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(0xFFD81B60))) {
                Text("< Back")
            }
            Text("Smart Input", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFFD81B60), modifier = Modifier.padding(start = 16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Settings list
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Typo Correction", fontWeight = FontWeight.Bold, color = Color(0xFFD81B60), modifier = Modifier.padding(bottom = 8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Auto correct common typos")
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
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Fuzzy Pinyin (模糊音)", fontWeight = FontWeight.Bold, color = Color(0xFFD81B60), modifier = Modifier.padding(bottom = 8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FuzzySwitchItem("z / zh", fuzzyZhZ) { checked -> scope.launch { context.dataStore.edit { it[SettingsKeys.FUZZY_ZH_Z] = checked } } }
                    FuzzySwitchItem("c / ch", fuzzyChC) { checked -> scope.launch { context.dataStore.edit { it[SettingsKeys.FUZZY_CH_C] = checked } } }
                    FuzzySwitchItem("s / sh", fuzzyShS) { checked -> scope.launch { context.dataStore.edit { it[SettingsKeys.FUZZY_SH_S] = checked } } }
                    FuzzySwitchItem("n / l", fuzzyNL) { checked -> scope.launch { context.dataStore.edit { it[SettingsKeys.FUZZY_N_L] = checked } } }
                    FuzzySwitchItem("en / eng", fuzzyEnEng) { checked -> scope.launch { context.dataStore.edit { it[SettingsKeys.FUZZY_EN_ENG] = checked } } }
                    FuzzySwitchItem("in / ing", fuzzyInIng) { checked -> scope.launch { context.dataStore.edit { it[SettingsKeys.FUZZY_IN_ING] = checked } } }
                    FuzzySwitchItem("an / ang", fuzzyAnAng) { checked -> scope.launch { context.dataStore.edit { it[SettingsKeys.FUZZY_AN_ANG] = checked } } }
                }
            }
        }
    }
}

@Composable
fun FuzzySwitchItem(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 16.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFE91E63), checkedTrackColor = Color(0xFFFCE4EC))
        )
    }
}

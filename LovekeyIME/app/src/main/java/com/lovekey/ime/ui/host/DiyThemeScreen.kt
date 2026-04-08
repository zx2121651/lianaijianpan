package com.lovekey.ime.ui.host

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@Composable
fun DiyThemeScreen(context: Context, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var keyAlpha by remember { mutableStateOf(0.7f) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Save the image to internal storage so the IME service can access it reliably
            scope.launch {
                try {
                    val inputStream: InputStream? = context.contentResolver.openInputStream(it)
                    val outFile = File(context.filesDir, "custom_bg.jpg")
                    val outputStream = FileOutputStream(outFile)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0F5))
    ) {
        // Toolbar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(0xFFD81B60))
            ) {
                Text("< 返回")
            }
            Text("自定义键盘", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFFD81B60), modifier = Modifier.padding(start = 16.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.padding(16.dp)) {
            // Select Image Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { launcher.launch("image/*") },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFCE4EC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (selectedImageUri != null) "✓" else "+", color = Color(0xFFE91E63), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("选择背景图", fontWeight = FontWeight.Bold, color = Color(0xFFD81B60))
                        Text(if (selectedImageUri != null) "已选择图片" else "从相册挑选一张好看的图片", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Transparency Slider
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("按键透明度", fontWeight = FontWeight.Bold, color = Color(0xFFD81B60))
                    Spacer(modifier = Modifier.height(16.dp))
                    Slider(
                        value = keyAlpha,
                        onValueChange = { keyAlpha = it },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFE91E63),
                            activeTrackColor = Color(0xFFE91E63),
                            inactiveTrackColor = Color(0xFFFCE4EC)
                        )
                    )
                    Text("当前透明度: ${(keyAlpha * 100).toInt()}%", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.align(Alignment.End))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save and Apply Button
            Button(
                onClick = {
                    scope.launch {
                        context.dataStore.edit { prefs ->
                            prefs[SettingsKeys.CUSTOM_KEY_ALPHA] = keyAlpha
                            prefs[SettingsKeys.PERSONA_ID] = "theme_custom"
                        }
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("保存并使用", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

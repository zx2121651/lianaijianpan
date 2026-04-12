package com.lovekey.ime.ui.host

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.map

@Composable
fun ProfileScreen(context: Context, onNavigateToSmartInput: () -> Unit, onNavigateToAbout: () -> Unit) {
    val scoreFlow = remember { context.dataStore.data.map { it[SettingsKeys.AFFECTION_SCORE] ?: 150 } }
    val affectionScore by scoreFlow.collectAsState(initial = 150)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0F5))
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF48FB1)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Me", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Lovekey User", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color(0xFFD81B60))
                    Text("Typing with Love \uD83D\uDC96", color = Color.Gray)
                }
            }
        }

        // Stats Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Your Typing Journey", fontWeight = FontWeight.Bold, color = Color(0xFFD81B60), fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        StatItem("Affection Level", "$affectionScore ♥", Color(0xFFE91E63))
                        StatItem("Keys Typed", "1,204", Color(0xFF9C27B0))
                        StatItem("Typos Fixed", "89", Color(0xFF3F51B5))
                    }
                }
            }
        }

        // Action List
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    ListActionItem("Advanced Settings", "Manage deeper keyboard logic", onNavigateToSmartInput)
                    HorizontalDivider(color = Color(0xFFFCE4EC))
                    ListActionItem("About Lovekey", "Version 1.0.0", onNavigateToAbout)
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = color)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun ListActionItem(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color(0xFFD81B60))
            Text(subtitle, color = Color.Gray, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(">", color = Color(0xFFE91E63), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0F5))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)
        ) {
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(0xFFD81B60))) {
                Text("< Back")
            }
            Text("About Lovekey", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFFD81B60), modifier = Modifier.padding(start = 16.dp))
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE91E63)),
                contentAlignment = Alignment.Center
            ) {
                Text("L", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Lovekey IME", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD81B60))
            Text("Version 1.0.0", color = Color.Gray)
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Lovekey is an open-source Android Input Method Editor built completely in Jetpack Compose, featuring advanced T9 & Qwerty pinyin engines, emotional interaction logic, and deep UI customizations.",
                color = Color.DarkGray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

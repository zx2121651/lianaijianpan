package com.lovekey.ime.ui.host

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray

@Composable
fun PhrasesScreen(context: Context) {
    val scope = rememberCoroutineScope()

    // Retrieve phrases list from DataStore
    val phrasesJson by context.dataStore.data.map {
        it[SettingsKeys.SHORTCUT_PHRASES] ?: "[\"你好，我正在忙，稍后回复你哦~\", \"我的邮箱是：test@lovekey.com\", \"马上到！\"]"
    }.collectAsState(initial = "[]")

    val phrasesList = remember(phrasesJson) {
        try {
            val jsonArray = JSONArray(phrasesJson)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    var newPhraseText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0F5)) // Lovekey Pink
    ) {
        // Toolbar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text("快捷语料库", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFFD81B60))
        }

        // Add New Phrase Input
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("新增常用语", fontWeight = FontWeight.Bold, color = Color(0xFFD81B60), modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = newPhraseText,
                    onValueChange = { newPhraseText = it },
                    placeholder = { Text("输入你想一键发送的短语...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (newPhraseText.isNotBlank()) {
                            scope.launch {
                                context.dataStore.edit { prefs ->
                                    val currentList = phrasesList.toMutableList()
                                    currentList.add(0, newPhraseText.trim()) // Add to top
                                    prefs[SettingsKeys.SHORTCUT_PHRASES] = JSONArray(currentList).toString()
                                }
                            }
                            newPhraseText = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                ) {
                    Text("添加至键盘")
                }
            }
        }

        // Phrases List
        Text(
            text = "我的语料 (${phrasesList.size})",
            fontWeight = FontWeight.Bold,
            color = Color(0xFF880E4F),
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )

        if (phrasesList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无语料记录", color = Color.Gray, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(phrasesList) { text ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = text,
                                color = Color(0xFF333333),
                                fontSize = 16.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            // Delete button
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFFCE4EC))
                                    .clickable {
                                        scope.launch {
                                            context.dataStore.edit { prefs ->
                                                val currentList = phrasesList.toMutableList()
                                                currentList.remove(text)
                                                prefs[SettingsKeys.SHORTCUT_PHRASES] = JSONArray(currentList).toString()
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✕", color = Color(0xFFD81B60), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

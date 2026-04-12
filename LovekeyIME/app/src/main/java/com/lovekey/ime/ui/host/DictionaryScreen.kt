package com.lovekey.ime.ui.host

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

val CUSTOM_DICTIONARY = stringPreferencesKey("custom_dictionary")

@Composable
fun DictionaryScreen(context: Context, onNavigateToAddWord: () -> Unit) {
    val scope = rememberCoroutineScope()
    val dictFlow = remember(context) { context.dataStore.data.map { it[CUSTOM_DICTIONARY] ?: "[]" } }
    val dictJson by dictFlow.collectAsState(initial = "[]")

    val dictionaryList = remember(dictJson) {
        try {
            val jsonArray = JSONArray(dictJson)
            val list = mutableListOf<Pair<String, String>>() // Pair of pinyin to word
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(Pair(obj.getString("pinyin"), obj.getString("word")))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddWord,
                containerColor = Color(0xFFE91E63),
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Word")
            }
        },
        containerColor = Color(0xFFFFF0F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                "My Dictionary",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD81B60),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (dictionaryList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No custom words added yet.\nTap + to add one!", color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dictionaryList) { (pinyin, word) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(word, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFD81B60))
                                    Text(pinyin, color = Color.Gray, fontSize = 14.sp)
                                }

                                TextButton(onClick = {
                                    scope.launch {
                                        val newList = dictionaryList.filter { it.second != word }.toMutableList()
                                        val jsonArray = JSONArray()
                                        newList.forEach {
                                            val obj = JSONObject()
                                            obj.put("pinyin", it.first)
                                            obj.put("word", it.second)
                                            jsonArray.put(obj)
                                        }
                                        context.dataStore.edit { it[CUSTOM_DICTIONARY] = jsonArray.toString() }
                                    }
                                }) {
                                    Text("Delete", color = Color(0xFFE91E63))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWordScreen(context: Context, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var pinyin by remember { mutableStateOf("") }
    var word by remember { mutableStateOf("") }

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
            Text("Add Custom Word", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFFD81B60), modifier = Modifier.padding(start = 16.dp))
        }

        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = pinyin,
                onValueChange = { pinyin = it.lowercase() },
                label = { Text("Pinyin (e.g. juejuezi)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE91E63),
                    unfocusedBorderColor = Color(0xFFF48FB1)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = word,
                onValueChange = { word = it },
                label = { Text("Word (e.g. 绝绝子)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE91E63),
                    unfocusedBorderColor = Color(0xFFF48FB1)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (pinyin.isNotBlank() && word.isNotBlank()) {
                        scope.launch {
                            context.dataStore.edit { prefs ->
                                val currentStr = prefs[CUSTOM_DICTIONARY] ?: "[]"
                                val jsonArray = JSONArray(currentStr)
                                val obj = JSONObject()
                                obj.put("pinyin", pinyin.trim())
                                obj.put("word", word.trim())
                                jsonArray.put(obj)
                                prefs[CUSTOM_DICTIONARY] = jsonArray.toString()
                            }
                            onBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                enabled = pinyin.isNotBlank() && word.isNotBlank()
            ) {
                Text("Save to Dictionary", fontSize = 16.sp)
            }
        }
    }
}

package com.lovekey.ime.ui.host

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class PersonaCard(val id: String, val title: String, val author: String, val price: String, val color1: Color, val color2: Color)

@Composable
fun StoreScreen(context: Context, onNavigateToDiy: () -> Unit = {}) {
    var targetPercentage by remember { mutableStateOf(0f) }

val scope = rememberCoroutineScope()
    val currentPersonaId by context.dataStore.data.map { it[SettingsKeys.PERSONA_ID] ?: "theme_girl" }.collectAsState(initial = "theme_girl")
    val affectionScore by context.dataStore.data.map { it[SettingsKeys.AFFECTION_SCORE] ?: 150 }.collectAsState(initial = 150)


    val animatedPercentage by animateFloatAsState(
        targetValue = targetPercentage,
        animationSpec = tween(durationMillis = 2500)
    )

LaunchedEffect(affectionScore) {
        delay(300)
        targetPercentage = affectionScore / 1000f
    }

    val mockPersonas = listOf(
        PersonaCard("theme_girl", "初夏限定：元气学妹", "Lovekey 官方", "免费", Color(0xFFFFB6C1), Color(0xFFFF4081)),
        PersonaCard("theme_cyber", "赛博朋克：机械指令", "GeekStudio", "VIP", Color(0xFF00FF87), Color(0xFF60EFFF)),
        PersonaCard("theme_ink", "水墨丹青：长安时辰", "国风工作室", "免费", Color(0xFFCFD9DF), Color(0xFFE2EBF0)),
        PersonaCard("theme_dark", "暗夜极简：黑客帝国", "Lovekey 官方", "免费", Color(0xFF2C3E50), Color(0xFF000000)),
        PersonaCard("theme_magic", "星空梦境：魔法少女", "二次元部", "VIP", Color(0xFF8EC5FC), Color(0xFFE0C3FC)),
        PersonaCard("theme_rgb", "青轴手感：电竞RGB", "Razer Fan", "免费", Color(0xFFFF9A9E), Color(0xFFFECFEF))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
    ) {
        // Hero Header (Fluid Heart)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFD1DC), Color(0xFFF9F9F9))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Lovekey 灵魂契合度",
                    color = Color(0xFFD81B60),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                FluidHeartProgress(
                    percentage = animatedPercentage,
                    modifier = Modifier.size(160.dp),
                    frontWaveColor = Color(0xFFFF4081),
                    backWaveColor = Color(0x88FF4081),
                    backgroundColor = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("“键盘越来越懂你了”", color = Color(0xFF880E4F), fontSize = 12.sp)
            }
        }

        // 金刚区 (Quick Categories)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CategoryIcon("国风", Color(0xFFFFCDD2))
            CategoryIcon("二次元", Color(0xFFBBDEFB))
            CategoryIcon("极简", Color(0xFFC8E6C9))
            CategoryIcon("电竞", Color(0xFFFFF9C4))
            CategoryIcon("盲盒", Color(0xFFE1BEE7))
        }


        // DIY Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable { onNavigateToDiy() },
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).background(Color(0xFFE91E63), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("自定义键盘相册", fontWeight = FontWeight.Bold, color = Color(0xFFD81B60), fontSize = 16.sp)
                    Text("上传自己的照片，DIY 专属皮肤", color = Color(0xFFC2185B), fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text("制作 >", color = Color(0xFFD81B60), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        // Staggered Grid (瀑布流)
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp
        ) {
            items(mockPersonas) { persona ->
                PersonaCardView(persona, currentPersonaId, context)
            }
        }
    }
}

@Composable
fun CategoryIcon(label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(label.first().toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, color = Color.DarkGray)
    }
}

@Composable
fun PersonaCardView(persona: PersonaCard, currentPersonaId: String, context: Context) {
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Mock Cover Image with Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (persona.id.length % 2 == 0) 180.dp else 140.dp) // Staggered height
                    .background(Brush.linearGradient(listOf(persona.color1, persona.color2)))
            ) {
                // Price Tag
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(if (persona.price == "VIP") Color(0xFFFF5722) else Color(0x66000000), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(persona.price, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Info
            Column(modifier = Modifier.padding(12.dp)) {
                Text(persona.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF333333))
                Spacer(modifier = Modifier.height(4.dp))
                Text("by ${persona.author}", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                val isApplied = currentPersonaId == persona.id
                Button(
                    onClick = {
                        scope.launch {
                            context.dataStore.edit { prefs ->
                                prefs[SettingsKeys.PERSONA_ID] = persona.id
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isApplied) Color(0xFFE91E63) else Color(0xFFFCE4EC)
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        if (isApplied) "使用中" else "应用",
                        color = if (isApplied) Color.White else Color(0xFFD81B60),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

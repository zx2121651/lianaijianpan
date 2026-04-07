package com.lovekey.ime.theme

import androidx.compose.ui.graphics.Color

data class PersonaTheme(
    val id: String,
    val name: String,
    val boardColor: Color,
    val keyColor: Color,
    val functionKeyColor: Color,
    val accentColor: Color,
    val textColor: Color,
    val secondaryTextColor: Color,
    val unselectedTabColor: Color
)

object ThemePresets {
    val ThemeGirl = PersonaTheme(
        id = "theme_girl",
        name = "初夏学妹",
        boardColor = Color(0xFFF3EBEB),
        keyColor = Color.White,
        functionKeyColor = Color(0xFFE4DDDD),
        accentColor = Color(0xFFE2B4B8),
        textColor = Color(0xFF4A4443),
        secondaryTextColor = Color(0xFF988F8E),
        unselectedTabColor = Color(0xFFD6D1D1)
    )

    val ThemeCyber = PersonaTheme(
        id = "theme_cyber",
        name = "赛博黑客",
        boardColor = Color(0xFF121212),
        keyColor = Color(0xFF1E1E1E),
        functionKeyColor = Color(0xFF2C2C2C),
        accentColor = Color(0xFF00FF87),
        textColor = Color(0xFFFFFFFF),
        secondaryTextColor = Color(0xFFAAAAAA),
        unselectedTabColor = Color(0xFF555555)
    )

    val ThemeInk = PersonaTheme(
        id = "theme_ink",
        name = "水墨长安",
        boardColor = Color(0xFFF5F5DC),
        keyColor = Color(0xFFFFFFFF),
        functionKeyColor = Color(0xFFEAEAEA),
        accentColor = Color(0xFFC0392B),
        textColor = Color(0xFF222222),
        secondaryTextColor = Color(0xFF777777),
        unselectedTabColor = Color(0xFFBDBDBD)
    )

    fun getThemeById(id: String): PersonaTheme {
        return when (id) {
            "theme_girl" -> ThemeGirl
            "theme_cyber" -> ThemeCyber
            "theme_ink" -> ThemeInk
            else -> ThemeGirl
        }
    }
}

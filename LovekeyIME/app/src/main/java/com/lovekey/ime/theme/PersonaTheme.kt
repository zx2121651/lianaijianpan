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
    val unselectedTabColor: Color,
    val backgroundImagePath: String? = null,
    val keyAlpha: Float = 1.0f
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

    val ThemeCustom = PersonaTheme(
        id = "theme_custom",
        name = "自定义装扮",
        boardColor = Color(0x66000000), // Semi-transparent black fallback
        keyColor = Color(0xFFFFFFFF),
        functionKeyColor = Color(0xFFEAEAEA),
        accentColor = Color(0xFFE91E63),
        textColor = Color(0xFFFFFFFF),
        secondaryTextColor = Color(0xFFDDDDDD),
        unselectedTabColor = Color(0xFFBDBDBD),
        backgroundImagePath = null,
        keyAlpha = 0.7f
    )

    fun getThemeById(id: String): PersonaTheme {
        return when (id) {
            "theme_girl" -> ThemeGirl
            "theme_cyber" -> ThemeCyber
            "theme_ink" -> ThemeInk
            "theme_custom" -> ThemeCustom
            else -> ThemeGirl
        }
    }
}

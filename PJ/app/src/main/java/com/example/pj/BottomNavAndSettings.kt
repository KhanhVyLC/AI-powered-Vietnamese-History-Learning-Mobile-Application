//File: BottomNavAndSettings.kt - ✅ FIXED (NO TRANSLATIONS)
package com.example.pj

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BottomNavigationBar(
    currentScreen: String,
    language: String,
    isDarkMode: Boolean,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White,
        tonalElevation = 8.dp
    ) {
        // Home
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null
                )
            },
            label = {
                Text(
                    when (language) {
                        "en" -> "Home"
                        "zh" -> "首页"
                        else -> "Trang chủ"
                    },
                    fontSize = 12.sp
                )
            },
            selected = currentScreen == "home",
            onClick = { onNavigate("home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF7B1FA2),
                selectedTextColor = Color(0xFF7B1FA2),
                indicatorColor = Color(0xFFE1BEE7),
                unselectedIconColor = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray,
                unselectedTextColor = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray
            )
        )

        // Quiz History
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null
                )
            },
            label = {
                Text(
                    when (language) {
                        "en" -> "History"
                        "zh" -> "历史"
                        else -> "Lịch sử"
                    },
                    fontSize = 12.sp
                )
            },
            selected = currentScreen == "history",
            onClick = { onNavigate("history") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF7B1FA2),
                selectedTextColor = Color(0xFF7B1FA2),
                indicatorColor = Color(0xFFE1BEE7),
                unselectedIconColor = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray,
                unselectedTextColor = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray
            )
        )

        // Chat
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null
                )
            },
            label = {
                Text(
                    when (language) {
                        "en" -> "Chat"
                        "zh" -> "聊天"
                        else -> "Trò chuyện"
                    },
                    fontSize = 12.sp
                )
            },
            selected = currentScreen == "chat",
            onClick = { onNavigate("chat") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF7B1FA2),
                selectedTextColor = Color(0xFF7B1FA2),
                indicatorColor = Color(0xFFE1BEE7),
                unselectedIconColor = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray,
                unselectedTextColor = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray
            )
        )

        // Leaderboard
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null
                )
            },
            label = {
                Text(
                    when (language) {
                        "en" -> "Ranking"
                        "zh" -> "排行榜"
                        else -> "Xếp hạng"
                    },
                    fontSize = 12.sp
                )
            },
            selected = currentScreen == "leaderboard",
            onClick = { onNavigate("leaderboard") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF7B1FA2),
                selectedTextColor = Color(0xFF7B1FA2),
                indicatorColor = Color(0xFFE1BEE7),
                unselectedIconColor = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray,
                unselectedTextColor = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray
            )
        )
    }
}
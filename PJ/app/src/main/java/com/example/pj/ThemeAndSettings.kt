//File: ThemeAndSettings.kt - ✅ FIXED: Chào buổi sáng/chiều/tối ĐÚNG
package com.example.pj

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

/**
 * ViewModel quản lý Settings - CẬP NHẬT CHO FIREBASE
 */
class SettingsViewModel : ViewModel() {

    // Dark mode
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Language
    private val _language = MutableStateFlow("vi") // vi, en, zh
    val language: StateFlow<String> = _language.asStateFlow()

    // Weekly quiz count
    private val _weeklyQuizCount = MutableStateFlow(0)
    val weeklyQuizCount: StateFlow<Int> = _weeklyQuizCount.asStateFlow()

    // Login state
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // User info
    private val _userName = MutableStateFlow("Guest")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userScore = MutableStateFlow(0)
    val userScore: StateFlow<Int> = _userScore.asStateFlow()

    /**
     * TOGGLE DARK MODE
     */
    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    /**
     * SET LANGUAGE
     */
    fun setLanguage(lang: String) {
        _language.value = lang
    }

    /**
     * INCREMENT WEEKLY QUIZ COUNT
     */
    fun incrementWeeklyQuiz() {
        _weeklyQuizCount.value += 1
    }

    /**
     * ✅ CẬP NHẬT: LOGIN WITH FIREBASE DATA
     * Được gọi từ FirebaseViewModel sau khi đăng nhập thành công
     */
    fun login(username: String, score: Int = 0, weeklyQuizCount: Int = 0) {
        _isLoggedIn.value = true
        _userName.value = username
        _userScore.value = score
        _weeklyQuizCount.value = weeklyQuizCount
    }

    /**
     * ✅ CẬP NHẬT: UPDATE SCORE FROM FIREBASE
     * Được gọi khi FirebaseViewModel cập nhật điểm
     */
    fun updateScore(newScore: Int) {
        _userScore.value = newScore
    }

    /**
     * ✅ CẬP NHẬT: UPDATE WEEKLY QUIZ COUNT FROM FIREBASE
     */
    fun updateWeeklyQuizCount(count: Int) {
        _weeklyQuizCount.value = count
    }

    /**
     * ✅ LOGOUT
     */
    fun logout() {
        _isLoggedIn.value = false
        _userName.value = "Guest"
        _userScore.value = 0
        _weeklyQuizCount.value = 0
    }
}

/**
 * Theme với Dark Mode - GIỮ NGUYÊN
 */
@Composable
fun VietnamHistoryQuizTheme(
    isDarkMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkMode) {
        darkColorScheme(
            primary = Color(0xFFCE93D8),
            secondary = Color(0xFFBA68C8),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF7B1FA2),
            secondary = Color(0xFF9C27B0),
            background = Color(0xFFF5F5F5),
            surface = Color.White,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color.Black,
            onSurface = Color.Black
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

/**
 * ✅ THÊM MỚI: Xác định thời gian trong ngày
 */
fun getCurrentTimeOfDay(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> "morning"    // 0h - 11h59: Sáng
        in 12..17 -> "afternoon"  // 12h - 17h59: Chiều
        else -> "evening"         // 18h - 23h59: Tối
    }
}

/**
 * Translations - ✅ FIXED: Cập nhật greeting() để tự động lấy thời gian
 */
object Translations {

    /**
     * ✅ FIXED: Tự động xác định thời gian nếu không truyền vào
     */
    fun greeting(lang: String, timeOfDay: String? = null): String {
        val time = timeOfDay ?: getCurrentTimeOfDay()

        return when (lang) {
            "en" -> when (time) {
                "morning" -> "Good morning!"
                "evening" -> "Good evening!"
                else -> "Good afternoon!"
            }
            "zh" -> when (time) {
                "morning" -> "早上好！"
                "evening" -> "晚上好！"
                else -> "下午好！"
            }
            else -> when (time) {
                "morning" -> "Chào buổi sáng!"
                "evening" -> "Chào buổi tối!"
                else -> "Chào buổi chiều!"
            }
        }
    }

    fun weeklyQuizTitle(lang: String): String = when (lang) {
        "en" -> "Quizzes This Week"
        "zh" -> "本周测验"
        else -> "Tổng số quiz"
    }

    fun weeklyQuizCompleted(lang: String, count: Int): String = when (lang) {
        "en" -> "$count quizzes completed"
        "zh" -> "已完成 $count 个测验"
        else -> "$count bài quiz đã hoàn thành"
    }

    fun dailyQuizTitle(lang: String): String = when (lang) {
        "en" -> "Daily Quiz"
        "zh" -> "每日测验"
        else -> "Quiz Hàng Ngày"
    }

    fun dailyQuizDesc(lang: String): String = when (lang) {
        "en" -> "Play, learn, complete"
        "zh" -> "玩、学、完成"
        else -> "Chơi, học, hoàn thành"
    }

    fun joinQuiz(lang: String): String = when (lang) {
        "en" -> "Join quiz"
        "zh" -> "参加测验"
        else -> "Tham gia quiz"
    }

    fun exploreTopics(lang: String): String = when (lang) {
        "en" -> "Explore Topics"
        "zh" -> "探索主题"
        else -> "Khám phá chủ đề"
    }

    fun home(lang: String): String = when (lang) {
        "en" -> "Home"
        "zh" -> "首页"
        else -> "Trang chủ"
    }

    fun history(lang: String): String = when (lang) {
        "en" -> "History"
        "zh" -> "历史"
        else -> "Lịch sử"
    }

    fun leaderboard(lang: String): String = when (lang) {
        "en" -> "Leaderboard"
        "zh" -> "排行榜"
        else -> "BXH"
    }

    fun profile(lang: String): String = when (lang) {
        "en" -> "Profile"
        "zh" -> "个人资料"
        else -> "Hồ sơ"
    }

    fun qa(lang: String): String = when (lang) {
        "en" -> "Q&A"
        "zh" -> "问答"
        else -> "Hỏi đáp"
    }

    fun achievement(lang: String): String = when (lang) {
        "en" -> "Achievement"
        "zh" -> "成就"
        else -> "Thành tích"
    }

    fun settings(lang: String): String = when (lang) {
        "en" -> "Settings"
        "zh" -> "设置"
        else -> "Cài đặt"
    }

    fun darkMode(lang: String): String = when (lang) {
        "en" -> "Dark Mode"
        "zh" -> "深色模式"
        else -> "Chế độ tối"
    }

    fun language(lang: String): String = when (lang) {
        "en" -> "Language"
        "zh" -> "语言"
        else -> "Ngôn ngữ"
    }

    fun vietnamese(lang: String): String = when (lang) {
        "en" -> "Vietnamese"
        "zh" -> "越南语"
        else -> "Tiếng Việt"
    }

    fun english(lang: String): String = when (lang) {
        "en" -> "English"
        "zh" -> "英语"
        else -> "Tiếng Anh"
    }

    fun chinese(lang: String): String = when (lang) {
        "en" -> "Chinese"
        "zh" -> "中文"
        else -> "Tiếng Trung"
    }
}
//File: DailyQuizHistoryScreen.kt - ✅ FIXED: Refresh data on screen load
package com.example.pj

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyQuizHistoryScreen(
    settingsViewModel: SettingsViewModel,
    firebaseViewModel: FirebaseViewModel,
    onBack: () -> Unit,
    onResultClick: (QuizResult) -> Unit
) {
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val language by settingsViewModel.language.collectAsState()
    val dailyQuizHistory by firebaseViewModel.dailyQuizHistory.collectAsState()

    // ✅ TẢI LỊCH SỬ KHI VÀO SCREEN + PULL TO REFRESH
    LaunchedEffect(Unit) {
        firebaseViewModel.loadDailyQuizHistory()
    }

    val surfaceColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black

    // Nhóm theo ngày
    val groupedHistory = dailyQuizHistory.groupBy { result ->
        val date = result.completedAt ?: Date()
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (language) {
                            "en" -> "Daily Quiz History"
                            "zh" -> "每日测验历史"
                            else -> "Lịch sử Quiz Hàng Ngày"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isDarkMode) Color(0xFFCE93D8) else Color(0xFF7B1FA2)
                        )
                    }
                },
                actions = {
                    // ✅ REFRESH BUTTON
                    IconButton(onClick = { firebaseViewModel.loadDailyQuizHistory() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = if (isDarkMode) Color(0xFFCE93D8) else Color(0xFF7B1FA2)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = surfaceColor
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDarkMode) {
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF1E1E1E), Color(0xFF121212))
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFF3E5F5), Color.White)
                        )
                    }
                )
                .padding(paddingValues)
        ) {
            if (dailyQuizHistory.isEmpty()) {
                // Empty State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "📝",
                        fontSize = 80.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = when (language) {
                            "en" -> "No quiz history yet"
                            "zh" -> "还没有测验历史"
                            else -> "Chưa có lịch sử quiz"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (language) {
                            "en" -> "Complete a daily quiz to see it here"
                            "zh" -> "完成每日测验以查看"
                            else -> "Hoàn thành quiz hàng ngày để xem ở đây"
                        },
                        fontSize = 14.sp,
                        color = textColor.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Statistics Card
                    item {
                        DailyQuizStatsCard(
                            history = dailyQuizHistory,
                            isDarkMode = isDarkMode,
                            language = language
                        )
                    }

                    // Grouped History
                    groupedHistory.forEach { (date, results) ->
                        item {
                            Text(
                                text = date,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color(0xFFCE93D8) else Color(0xFF7B1FA2),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(results) { result ->
                            DailyHistoryCard(
                                result = result,
                                isDarkMode = isDarkMode,
                                language = language,
                                onClick = { onResultClick(result) }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun DailyQuizStatsCard(
    history: List<QuizResult>,
    isDarkMode: Boolean,
    language: String
) {
    val totalQuizzes = history.size
    val totalScore = history.sumOf { it.score }
    val totalCorrect = history.sumOf { it.correctAnswers }
    val totalQuestions = history.sumOf { it.totalQuestions }
    val avgAccuracy = if (totalQuestions > 0) {
        (totalCorrect.toDouble() / totalQuestions * 100).toInt()
    } else 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF2D2D2D) else Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF7B1FA2)
                )
                Text(
                    text = when (language) {
                        "en" -> "Overall Statistics"
                        "zh" -> "总体统计"
                        else -> "Thống kê tổng quan"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color.Black
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DailyStatItem(
                    icon = Icons.Default.List,
                    label = when (language) {
                        "en" -> "Quizzes"
                        "zh" -> "测验"
                        else -> "Quiz"
                    },
                    value = "$totalQuizzes",
                    color = Color(0xFF2196F3),
                    isDarkMode = isDarkMode
                )
                DailyStatItem(
                    icon = Icons.Default.Star,
                    label = when (language) {
                        "en" -> "Total Score"
                        "zh" -> "总分"
                        else -> "Tổng điểm"
                    },
                    value = "$totalScore",
                    color = Color(0xFFFFB300),
                    isDarkMode = isDarkMode
                )
                DailyStatItem(
                    icon = Icons.Default.CheckCircle,
                    label = when (language) {
                        "en" -> "Accuracy"
                        "zh" -> "准确率"
                        else -> "Độ chính xác"
                    },
                    value = "$avgAccuracy%",
                    color = Color(0xFF4CAF50),
                    isDarkMode = isDarkMode
                )
            }
        }
    }
}

@Composable
private fun DailyStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
    isDarkMode: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkMode) Color.White else Color.Black
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray
        )
    }
}

@Composable
private fun DailyHistoryCard(
    result: QuizResult,
    isDarkMode: Boolean,
    language: String,
    onClick: () -> Unit
) {
    val accuracy = if (result.totalQuestions > 0) {
        (result.correctAnswers.toDouble() / result.totalQuestions * 100).toInt()
    } else 0

    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val time = result.completedAt?.let { timeFormatter.format(it) } ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF2D2D2D) else Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Difficulty Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (result.difficulty) {
                        "Cực dễ", "Very Easy" -> Color(0xFF4CAF50)
                        "Dễ", "Easy" -> Color(0xFF8BC34A)
                        "Trung bình", "Medium" -> Color(0xFFFF9800)
                        "Khó", "Hard" -> Color(0xFFFF5722)
                        "Cực khó", "Very Hard" -> Color(0xFFF44336)
                        else -> Color(0xFF9E9E9E)
                    }.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = when (result.difficulty) {
                            "Cực dễ" -> when (language) {
                                "en" -> "VE"
                                "zh" -> "极易"
                                else -> "CD"
                            }
                            "Dễ" -> when (language) {
                                "en" -> "E"
                                "zh" -> "易"
                                else -> "D"
                            }
                            "Trung bình" -> when (language) {
                                "en" -> "M"
                                "zh" -> "中"
                                else -> "TB"
                            }
                            "Khó" -> when (language) {
                                "en" -> "H"
                                "zh" -> "难"
                                else -> "K"
                            }
                            "Cực khó" -> when (language) {
                                "en" -> "VH"
                                "zh" -> "极难"
                                else -> "CK"
                            }
                            else -> "?"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (result.difficulty) {
                            "Cực dễ", "Very Easy" -> Color(0xFF2E7D32)
                            "Dễ", "Easy" -> Color(0xFF558B2F)
                            "Trung bình", "Medium" -> Color(0xFFE65100)
                            "Khó", "Hard" -> Color(0xFFD84315)
                            "Cực khó", "Very Hard" -> Color(0xFFC62828)
                            else -> Color(0xFF616161)
                        }
                    )
                }

                Column {
                    Text(
                        text = "${result.totalQuestions} " + when (language) {
                            "en" -> "questions"
                            "zh" -> "个问题"
                            else -> "câu hỏi"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color.Black
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$accuracy%",
                            fontSize = 14.sp,
                            color = if (accuracy >= 70) Color(0xFF4CAF50) else Color(0xFFFF5722),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "•",
                            fontSize = 14.sp,
                            color = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray
                        )
                        Text(
                            text = time,
                            fontSize = 14.sp,
                            color = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${result.score}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color.Black
                    )
                }
                Text(
                    text = "${result.correctAnswers}/${result.totalQuestions}",
                    fontSize = 12.sp,
                    color = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray
                )
            }
        }
    }
}
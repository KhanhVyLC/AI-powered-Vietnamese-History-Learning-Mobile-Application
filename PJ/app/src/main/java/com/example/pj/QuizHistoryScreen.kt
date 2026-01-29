//File: QuizHistoryScreen.kt - ✅ IMPROVED UI: Thu gọn Daily Quiz
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
fun QuizHistoryScreen(
    settingsViewModel: SettingsViewModel,
    firebaseViewModel: FirebaseViewModel,
    onBack: () -> Unit,
    onResultClick: (QuizResult) -> Unit
) {
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val language by settingsViewModel.language.collectAsState()
    val quizHistory by firebaseViewModel.quizHistory.collectAsState()
    val authState by firebaseViewModel.authState.collectAsState()

    var selectedFilter by remember { mutableStateOf("all") }

    LaunchedEffect(authState.user?.uid) {
        authState.user?.uid?.let {
            firebaseViewModel.loadQuizHistory()
        }
    }

    val surfaceColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black

    val filteredHistory = when (selectedFilter) {
        "topic" -> quizHistory.filter { it.quizType == "topic" }
        "daily" -> quizHistory.filter { it.quizType == "daily" }
        else -> quizHistory
    }

    val groupedHistory = filteredHistory.groupBy { result ->
        val date = result.completedAt ?: Date()
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (language) {
                            "en" -> "Quiz History"
                            "zh" -> "测验历史"
                            else -> "Lịch sử Quiz"
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
                    IconButton(onClick = { firebaseViewModel.loadQuizHistory() }) {
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
            if (quizHistory.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "📝", fontSize = 80.sp)
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
                            "en" -> "Complete a quiz to see it here"
                            "zh" -> "完成测验以查看"
                            else -> "Hoàn thành quiz để xem ở đây"
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
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedFilter == "all",
                                onClick = { selectedFilter = "all" },
                                label = {
                                    Text(
                                        when (language) {
                                            "en" -> "All (${quizHistory.size})"
                                            "zh" -> "全部 (${quizHistory.size})"
                                            else -> "Tất cả (${quizHistory.size})"
                                        }
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.List,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                            FilterChip(
                                selected = selectedFilter == "topic",
                                onClick = { selectedFilter = "topic" },
                                label = {
                                    Text(
                                        when (language) {
                                            "en" -> "Topic (${quizHistory.count { it.quizType == "topic" }})"
                                            "zh" -> "主题 (${quizHistory.count { it.quizType == "topic" }})"
                                            else -> "Chủ đề (${quizHistory.count { it.quizType == "topic" }})"
                                        }
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Create,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                            FilterChip(
                                selected = selectedFilter == "daily",
                                onClick = { selectedFilter = "daily" },
                                label = {
                                    Text(
                                        when (language) {
                                            "en" -> "Daily (${quizHistory.count { it.quizType == "daily" }})"
                                            "zh" -> "每日 (${quizHistory.count { it.quizType == "daily" }})"
                                            else -> "Hàng ngày (${quizHistory.count { it.quizType == "daily" }})"
                                        }
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }

                    if (filteredHistory.isNotEmpty()) {
                        item {
                            QuizHistoryStatsCard(
                                history = filteredHistory,
                                isDarkMode = isDarkMode,
                                language = language
                            )
                        }
                    }

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
                            if (result.quizType == "topic") {
                                TopicQuizHistoryCard(
                                    result = result,
                                    isDarkMode = isDarkMode,
                                    language = language,
                                    onClick = { onResultClick(result) }
                                )
                            } else {
                                // ✅ DAILY QUIZ - THU GỌN
                                CompactDailyQuizCard(
                                    result = result,
                                    isDarkMode = isDarkMode,
                                    language = language,
                                    onClick = { onResultClick(result) }
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun QuizHistoryStatsCard(
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
                StatItem(
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
                StatItem(
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
                StatItem(
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
fun StatItem(
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
fun TopicQuizHistoryCard(
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
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF7B1FA2).copy(alpha = 0.2f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = null,
                        tint = Color(0xFF7B1FA2),
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = result.topicName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color.Black
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${result.correctAnswers}/${result.totalQuestions}",
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
                    text = "$accuracy%",
                    fontSize = 12.sp,
                    color = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray
                )
            }
        }
    }
}

/**
 * ✅ DAILY QUIZ - COMPACT CARD (THU GỌN)
 */
@Composable
fun CompactDailyQuizCard(
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
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFF9800).copy(alpha = 0.2f)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = when (language) {
                            "en" -> "Daily Quiz"
                            "zh" -> "每日测验"
                            else -> "Quiz Hàng Ngày"
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
                            text = "${result.totalQuestions} " + when (language) {
                                "en" -> "questions"
                                "zh" -> "个问题"
                                else -> "câu"
                            },
                            fontSize = 14.sp,
                            color = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray
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
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
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
                    color = if (accuracy >= 70) Color(0xFF4CAF50) else Color(0xFFFF5722),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
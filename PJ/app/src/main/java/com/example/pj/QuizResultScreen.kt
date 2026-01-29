//File: QuizResultScreen.kt - ✅ COMPACT VERSION
package com.example.pj

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultScreen(
    result: QuizResult,
    settingsViewModel: SettingsViewModel,
    onPlayAgain: () -> Unit,
    onBackHome: () -> Unit
) {
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val language by settingsViewModel.language.collectAsState()

    val surfaceColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black

    val accuracy = if (result.totalQuestions > 0) {
        (result.correctAnswers.toFloat() / result.totalQuestions * 100).toInt()
    } else 0

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (language) {
                            "en" -> "Quiz Results"
                            "zh" -> "测验结果"
                            else -> "Kết quả Quiz"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = surfaceColor
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
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
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ✅ ACTION BUTTONS
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onPlayAgain,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7B1FA2)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (language) {
                                "en" -> "Play Again"
                                "zh" -> "再玩一次"
                                else -> "Chơi lại"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onBackHome,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isDarkMode) Color(0xFFCE93D8) else Color(0xFF7B1FA2)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (language) {
                                "en" -> "Back to Home"
                                "zh" -> "返回首页"
                                else -> "Về trang chủ"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Stats Cards - Hàng 1
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Star,
                        label = when (language) {
                            "en" -> "Score"
                            "zh" -> "分数"
                            else -> "Điểm"
                        },
                        value = "${result.score}",
                        color = Color(0xFFFFB300),
                        isDarkMode = isDarkMode
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Check,
                        label = when (language) {
                            "en" -> "Accuracy"
                            "zh" -> "准确率"
                            else -> "Độ chính xác"
                        },
                        value = "$accuracy%",
                        color = Color(0xFF4CAF50),
                        isDarkMode = isDarkMode
                    )
                }
            }

            // Stats Cards - Hàng 2
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Done,
                        label = when (language) {
                            "en" -> "Correct"
                            "zh" -> "正确"
                            else -> "Đúng"
                        },
                        value = "${result.correctAnswers}/${result.totalQuestions}",
                        color = Color(0xFF2196F3),
                        isDarkMode = isDarkMode
                    )
                    TimeStatCard(
                        modifier = Modifier.weight(1f),
                        label = when (language) {
                            "en" -> "Time"
                            "zh" -> "时间"
                            else -> "Thời gian"
                        },
                        value = "${result.timeSpent / 60}:${String.format("%02d", result.timeSpent % 60)}",
                        isDarkMode = isDarkMode
                    )
                }
            }

            // Review Answers Header
            item {
                Text(
                    text = when (language) {
                        "en" -> "Review Answers"
                        "zh" -> "查看答案"
                        else -> "Xem lại câu trả lời"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Answer Cards
            itemsIndexed(result.answers) { index, answer ->
                AnswerReviewCard(
                    index = index + 1,
                    answer = answer,
                    isDarkMode = isDarkMode,
                    textColor = textColor
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
    isDarkMode: Boolean
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF2D2D2D) else Color.White
        ),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 20.sp,
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
}

@Composable
fun TimeStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    isDarkMode: Boolean
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF2D2D2D) else Color.White
        ),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF9C27B0).copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.p_time),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 20.sp,
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
}

@Composable
fun AnswerReviewCard(
    index: Int,
    answer: QuizAnswer,
    isDarkMode: Boolean,
    textColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (answer.isCorrect) {
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            } else {
                Color(0xFFFF5252).copy(alpha = 0.1f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Câu $index",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = textColor
                )
                Icon(
                    imageVector = if (answer.isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (answer.isCorrect) Color(0xFF4CAF50) else Color(0xFFFF5252),
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = answer.questionText,
                fontSize = 13.sp,
                color = textColor.copy(alpha = 0.9f)
            )

            if (!answer.isCorrect) {
                Divider(color = textColor.copy(alpha = 0.2f), thickness = 0.5.dp)

                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "❌ ",
                        fontSize = 13.sp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bạn chọn:",
                            fontSize = 11.sp,
                            color = textColor.copy(alpha = 0.7f)
                        )
                        Text(
                            text = answer.userAnswer,
                            fontSize = 13.sp,
                            color = Color(0xFFFF5252),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "✓ ",
                        fontSize = 13.sp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Đáp án đúng:",
                            fontSize = 11.sp,
                            color = textColor.copy(alpha = 0.7f)
                        )
                        Text(
                            text = answer.correctAnswer,
                            fontSize = 13.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                Text(
                    text = "✓ ${answer.userAnswer}",
                    fontSize = 13.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.p_time),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${answer.timeSpent}s",
                    fontSize = 11.sp,
                    color = textColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}
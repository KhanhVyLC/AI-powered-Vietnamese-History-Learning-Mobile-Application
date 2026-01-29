//File: DailyQuizSetupScreen.kt - ✅ UPDATED: Use drawable images for difficulty
package com.example.pj

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyQuizSetupScreen(
    settingsViewModel: SettingsViewModel,
    firebaseViewModel: FirebaseViewModel,
    onBack: () -> Unit,
    onStartQuiz: (Int, String) -> Unit
) {
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val language by settingsViewModel.language.collectAsState()

    var selectedQuestionCount by remember { mutableIntStateOf(5) }
    var selectedDifficulty by remember { mutableStateOf("Trung bình") }

    val surfaceColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black

    // ✅ CHỈ 3 CẤP ĐỘ với HÌNH ẢNH từ drawable
    val difficulties = listOf(
        DifficultyOption(
            nameVi = "Dễ",
            nameEn = "Easy",
            nameZh = "简单",
            emoji = "😊",
            color = Color(0xFF4CAF50),
            description = when (language) {
                "en" -> "Basic knowledge"
                "zh" -> "基础知识"
                else -> "Kiến thức cơ bản"
            },
            imageRes = R.drawable.p_easy //  NEW: Image from drawable
        ),
        DifficultyOption(
            nameVi = "Trung bình",
            nameEn = "Medium",
            nameZh = "中等",
            emoji = "🤔",
            color = Color(0xFFFF9800),
            description = when (language) {
                "en" -> "Solid understanding"
                "zh" -> "扎实理解"
                else -> "Hiểu biết vững chắc"
            },
            imageRes = R.drawable.p_normal //  NEW: Image from drawable
        ),
        DifficultyOption(
            nameVi = "Khó",
            nameEn = "Hard",
            nameZh = "困难",
            emoji = "🔥",
            color = Color(0xFFF44336),
            description = when (language) {
                "en" -> "In-depth knowledge"
                "zh" -> "深入知识"
                else -> "Kiến thức chuyên sâu"
            },
            imageRes = R.drawable.p_hard //  NEW: Image from drawable
        )
    )

    val questionCounts = listOf(5, 10, 15, 20)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (language) {
                            "en" -> "Daily Quiz Setup"
                            "zh" -> "每日测验设置"
                            else -> "Cài đặt Quiz Hàng Ngày"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
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
                            colors = listOf(Color(0xFFF3E5F5), Color.White))
                    }
                )
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Difficulty Header
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = when (language) {
                            "en" -> "Difficulty Level"
                            "zh" -> "难度级别"
                            else -> "Độ khó"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }

            // Difficulty Cards
            items(difficulties) { difficulty ->
                DifficultyCard(
                    difficulty = difficulty,
                    isSelected = selectedDifficulty == difficulty.nameVi,
                    language = language,
                    isDarkMode = isDarkMode,
                    onClick = { selectedDifficulty = difficulty.nameVi }
                )
            }

            // Question Count Header
            item {
                Text(
                    text = when (language) {
                        "en" -> "Number of Questions"
                        "zh" -> "问题数量"
                        else -> "Số lượng câu hỏi"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }

            // Question Count Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    questionCounts.forEach { count ->
                        QuestionCountCard(
                            count = count,
                            isSelected = selectedQuestionCount == count,
                            isDarkMode = isDarkMode,
                            language = language,
                            onClick = { selectedQuestionCount = count },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Start Button
            item {
                Button(
                    onClick = {
                        onStartQuiz(selectedQuestionCount, selectedDifficulty)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7B1FA2)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (language) {
                            "en" -> "Start Quiz"
                            "zh" -> "开始测验"
                            else -> "Bắt đầu Quiz"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

data class DifficultyOption(
    val nameVi: String,
    val nameEn: String,
    val nameZh: String,
    val emoji: String,
    val color: Color,
    val description: String,
    val imageRes: Int // ✅ NEW: Drawable resource ID
)

@Composable
fun DifficultyCard(
    difficulty: DifficultyOption,
    isSelected: Boolean,
    language: String,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                difficulty.color.copy(alpha = 0.2f)
            } else {
                if (isDarkMode) Color(0xFF2D2D2D) else Color.White
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, difficulty.color)
        } else null,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ✅ UPDATED: Use Image from drawable instead of emoji
                Image(
                    painter = painterResource(id = difficulty.imageRes),
                    contentDescription = difficulty.nameVi,
                    modifier = Modifier.size(60.dp),
                    contentScale = ContentScale.Fit
                )

                // Name & Description
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = when (language) {
                            "en" -> difficulty.nameEn
                            "zh" -> difficulty.nameZh
                            else -> difficulty.nameVi
                        },
                        fontSize = 18.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) difficulty.color else {
                            if (isDarkMode) Color.White else Color.Black
                        }
                    )

                    Text(
                        text = difficulty.description,
                        fontSize = 12.sp,
                        color = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = difficulty.color,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun QuestionCountCard(
    count: Int,
    isSelected: Boolean,
    isDarkMode: Boolean,
    language: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                Color(0xFF7B1FA2).copy(alpha = 0.2f)
            } else {
                if (isDarkMode) Color(0xFF2D2D2D) else Color.White
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF7B1FA2))
        } else null,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$count",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color(0xFF7B1FA2) else {
                        if (isDarkMode) Color.White else Color.Black
                    }
                )
                Text(
                    text = when (language) {
                        "en" -> "Qs"
                        "zh" -> "题"
                        else -> "câu"
                    },
                    fontSize = 12.sp,
                    color = if (isSelected) Color(0xFF7B1FA2) else {
                        if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray
                    }
                )
            }
        }
    }
}
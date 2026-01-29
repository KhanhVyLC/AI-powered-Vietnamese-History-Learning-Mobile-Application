//File: TopicSetsScreen.kt - ✅ COMPLETE WITH CLEAN DESIGN
package com.example.pj

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val TAG = "TopicSetsScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicSetsScreen(
    topic: Topic,
    settingsViewModel: SettingsViewModel,
    firebaseViewModel: FirebaseViewModel,
    onBack: () -> Unit,
    onSetClick: (Int) -> Unit
) {
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val language by settingsViewModel.language.collectAsState()
    val topicProgress by firebaseViewModel.topicProgress.collectAsState()

    // ✅ FORCE RELOAD khi vào screen
    LaunchedEffect(topic.id) {
        Log.d(TAG, "🔄 Loading progress for topic: ${topic.id}")
        firebaseViewModel.loadTopicProgress(topic.id)
    }

    val quizSets = TopicQuizData.getSetsForTopic(topic.id)
    val progress = topicProgress[topic.id]

    // ✅ DEBUG LOG
    LaunchedEffect(progress) {
        Log.d(TAG, "📦 Current progress: ${progress?.completedSets}")
        Log.d(TAG, "📊 Total sets: ${quizSets.size}")
        progress?.completedSets?.forEach { setNum ->
            Log.d(TAG, "  ✅ Completed set: $setNum")
        }
    }

    val surfaceColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black

    // ✅ SỬ DỤNG getCompletionPercentage từ TopicProgress
    val completionPercentage = progress?.getCompletionPercentage(quizSets.size) ?: 0

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = topic.getName(language),
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
                    // ✅ DEBUG: Manual refresh button
                    IconButton(onClick = {
                        Log.d(TAG, "🔄 Manual refresh triggered")
                        firebaseViewModel.loadTopicProgress(topic.id)
                    }) {
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
        ) {
            // Topic Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(200.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box {
                        Image(
                            painter = painterResource(id = topic.imageRes),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.7f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(20.dp)
                        ) {
                            Text(
                                text = topic.icon,
                                fontSize = 40.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = topic.getName(language),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${quizSets.size} " + when (language) {
                                    "en" -> "Quiz Sets"
                                    "zh" -> "个测验集"
                                    else -> "Bộ Quiz"
                                },
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // ✅ PROGRESS CARD
            if (progress != null && progress.completedSets.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (completionPercentage == 100) {
                                Color(0xFF4CAF50).copy(alpha = 0.2f)
                            } else {
                                Color(0xFF2196F3).copy(alpha = 0.2f)
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = when (language) {
                                            "en" -> "Your Progress"
                                            "zh" -> "你的进度"
                                            else -> "Tiến độ của bạn"
                                        },
                                        fontSize = 14.sp,
                                        color = if (completionPercentage == 100) Color(0xFF2E7D32) else Color(0xFF1565C0),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${progress.completedSets.size}/${quizSets.size} " + when (language) {
                                            "en" -> "completed"
                                            "zh" -> "已完成"
                                            else -> "đã hoàn thành"
                                        },
                                        fontSize = 18.sp,
                                        color = if (completionPercentage == 100) Color(0xFF1B5E20) else Color(0xFF0D47A1),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // ✅ COMPLETION BADGE với getCompletionPercentage
                                if (completionPercentage == 100) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF4CAF50),
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                } else {
                                    // Progress Circle
                                    Box(
                                        modifier = Modifier.size(48.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            progress = { completionPercentage / 100f },
                                            modifier = Modifier.size(48.dp),
                                            color = Color(0xFF2196F3),
                                            strokeWidth = 4.dp,
                                            trackColor = Color(0xFF2196F3).copy(alpha = 0.2f)
                                        )
                                        Text(
                                            text = "$completionPercentage%",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1565C0)
                                        )
                                    }
                                }
                            }

                            // Progress Bar với getCompletionPercentage
                            LinearProgressIndicator(
                                progress = { completionPercentage / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = if (completionPercentage == 100) Color(0xFF4CAF50) else Color(0xFF2196F3),
                                trackColor = if (isDarkMode) Color(0xFF3D3D3D) else Color(0xFFE0E0E0)
                            )
                        }
                    }
                }
            }

            // Section Title
            item {
                Text(
                    text = when (language) {
                        "en" -> "Choose a Quiz Set"
                        "zh" -> "选择测验集"
                        else -> "Chọn bộ Quiz"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // ✅ QUIZ SETS WITH CHECKMARK
            items(quizSets) { quizSet ->
                val isCompleted = progress?.isSetCompleted(quizSet.setNumber) ?: false

                // ✅ LOG mỗi set
                LaunchedEffect(isCompleted) {
                    Log.d(TAG, "Set ${quizSet.setNumber}: isCompleted = $isCompleted")
                }

                AnimatedQuizSetCard(
                    quizSet = quizSet,
                    isCompleted = isCompleted,
                    isDarkMode = isDarkMode,
                    language = language,
                    onClick = {
                        Log.d(TAG, "🎯 Clicked on Set ${quizSet.setNumber}")
                        onSetClick(quizSet.setNumber)
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

/**
 * ✅ QUIZ SET CARD - CLEAN DESIGN WITH GREEN CHECKMARK
 */
@Composable
fun AnimatedQuizSetCard(
    quizSet: TopicQuizSet,
    isCompleted: Boolean,
    isDarkMode: Boolean,
    language: String,
    onClick: () -> Unit
) {
    // Animation for checkmark appearance
    val scale by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "checkmark_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF2D2D2D) else Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // ✅ SET NUMBER BADGE - LUÔN MÀU TÍM
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF7B1FA2),
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${quizSet.setNumber}",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column {
                    Text(
                        text = quizSet.setName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color.Black
                    )
                    Text(
                        text = "${quizSet.questions.size} " + when (language) {
                            "en" -> "questions"
                            "zh" -> "个问题"
                            else -> "câu hỏi"
                        },
                        fontSize = 14.sp,
                        color = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray
                    )
                }
            }

            // ✅ STATUS ICON - CHỈ HIỆN TICK XANH KHI HOÀN THÀNH
            Icon(
                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = if (isCompleted) Color(0xFF4CAF50) else Color(0xFF7B1FA2).copy(alpha = 0.5f),
                modifier = Modifier
                    .size(32.dp)
                    .scale(if (isCompleted) scale else 1f)
            )
        }
    }
}
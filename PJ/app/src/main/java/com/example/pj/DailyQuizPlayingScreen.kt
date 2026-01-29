//File: DailyQuizPlayingScreen.kt - ✅ FIXED: Removed duplicate loop
package com.example.pj

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyQuizPlayingScreen(
    questionCount: Int,
    difficulty: String,
    settingsViewModel: SettingsViewModel,
    firebaseViewModel: FirebaseViewModel,
    onFinish: (QuizResult) -> Unit,
    onBack: () -> Unit
) {
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val language by settingsViewModel.language.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val quizKey = remember { "${questionCount}_${difficulty}_${System.currentTimeMillis()}" }
    val quizViewModel: QuizViewModel = viewModel(key = quizKey)
    val quizState by quizViewModel.quizState.collectAsState()

    val surfaceColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black

    var autoNext by remember { mutableStateOf(true) }

    // ✅ Track answered questions history
    val answeredQuestionsHistory = remember { mutableStateMapOf<Int, String>() }

    LaunchedEffect(quizKey) {
        quizViewModel.generateQuizWithDifficulty(questionCount, difficulty, language)
    }

    LaunchedEffect(quizState.isFinished) {
        if (quizState.isFinished) {
            val result = quizState.result.copy(
                quizType = "daily",
                difficulty = difficulty
            )
            firebaseViewModel.saveDailyQuizResult(result)
            onFinish(result)
        }
    }

    // LOADING STATE
    if (quizState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDarkMode) {
                        Brush.verticalGradient(colors = listOf(Color(0xFF1E1E1E), Color(0xFF121212)))
                    } else {
                        Brush.verticalGradient(colors = listOf(Color(0xFFF3E5F5), Color.White))
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF7B1FA2),
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = when (language) {
                        "en" -> "Generating questions..."
                        "zh" -> "生成问题中..."
                        else -> "Đang tạo câu hỏi..."
                    },
                    fontSize = 16.sp,
                    color = textColor
                )
            }
        }
        return
    }

    // ERROR STATE
    if (quizState.error != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDarkMode) {
                        Brush.verticalGradient(colors = listOf(Color(0xFF1E1E1E), Color(0xFF121212)))
                    } else {
                        Brush.verticalGradient(colors = listOf(Color(0xFFF3E5F5), Color.White))
                    }
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("❌", fontSize = 64.sp)
                Text(
                    text = quizState.error!!,
                    fontSize = 16.sp,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
                ) {
                    Text(
                        when (language) {
                            "en" -> "Go back"
                            "zh" -> "返回"
                            else -> "Quay lại"
                        }
                    )
                }
            }
        }
        return
    }

    val currentQuestion = quizState.currentQuestion ?: return

    // QUESTION STATE - ✅ Load from history if going back
    val historyAnswer = answeredQuestionsHistory[quizState.currentQuestionIndex]
    var selectedAnswer by remember(quizState.currentQuestionIndex) {
        mutableStateOf(historyAnswer)
    }
    var showKnowledgeDetail by remember(quizState.currentQuestionIndex) { mutableStateOf(false) }
    var isAnswered by remember(quizState.currentQuestionIndex) {
        mutableStateOf(historyAnswer != null)
    }
    var isProcessing by remember(quizState.currentQuestionIndex) { mutableStateOf(false) }
    var showScoreAnimation by remember(quizState.currentQuestionIndex) { mutableStateOf(false) }

    // ANIMATIONS
    val progress by animateFloatAsState(
        targetValue = (quizState.currentQuestionIndex + 1).toFloat() / quizState.questions.size,
        label = "progress"
    )

    val scoreScale by animateFloatAsState(
        targetValue = if (showScoreAnimation) 1.5f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "score_scale"
    )

    LaunchedEffect(showScoreAnimation) {
        if (showScoreAnimation) {
            delay(2000)
            showScoreAnimation = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (language) {
                                "en" -> "Daily Quiz"
                                "zh" -> "每日测验"
                                else -> "Quiz Hàng Ngày"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${quizState.currentQuestionIndex + 1}/${quizState.questions.size}",
                            fontSize = 12.sp,
                            color = textColor.copy(alpha = 0.7f)
                        )
                    }
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
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF7B1FA2)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${quizState.totalScore}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
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
                        Brush.verticalGradient(colors = listOf(Color(0xFF1E1E1E), Color(0xFF121212)))
                    } else {
                        Brush.verticalGradient(colors = listOf(Color(0xFFF3E5F5), Color.White))
                    }
                )
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ==================== PROGRESS BAR + TIMER + SETTINGS ====================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF7B1FA2),
                        trackColor = Color(0xFFE1BEE7)
                    )

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .border(3.dp, Color(0xFF7B1FA2), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${quizState.timeLeft}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (quizState.timeLeft <= 5) Color.Red else textColor
                        )
                    }

                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(50.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = if (isDarkMode) Color(0xFFCE93D8) else Color(0xFF7B1FA2),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (language) {
                                        "en" -> "Auto-next   "
                                        "zh" -> "自动下一题   "
                                        else -> "Tự động chuyển câu   "
                                    },
                                    fontSize = 14.sp
                                )
                                Switch(
                                    checked = autoNext,
                                    onCheckedChange = { autoNext = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF4CAF50),
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color.Gray
                                    )
                                )
                            }
                        }
                    }
                }

                // ==================== IMAGE ====================
                if (currentQuestion.imageUrl.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        AsyncImage(
                            model = currentQuestion.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // ==================== QUESTION ====================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) Color(0xFF2D2D2D) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Text(
                        text = currentQuestion.question,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Start
                    )
                }

                // ==================== ANSWERS ====================
                // ✅ FIXED: Removed duplicate forEach loop
                currentQuestion.answers.forEach { answer ->
                    CompactAnswerButton(
                        answer = answer,
                        isSelected = selectedAnswer == answer,
                        isCorrect = answer == currentQuestion.correctAnswer,
                        showResult = isAnswered,
                        enabled = !isAnswered && !isProcessing && historyAnswer == null,
                        isDarkMode = isDarkMode,
                        onClick = {
                            if (!isAnswered && !isProcessing && historyAnswer == null) {
                                scope.launch {
                                    isProcessing = true
                                    selectedAnswer = answer
                                    isAnswered = true

                                    // ✅ Save to history
                                    answeredQuestionsHistory[quizState.currentQuestionIndex] = answer

                                    val isCorrect = answer == currentQuestion.correctAnswer
                                    if (isCorrect) {
                                        showScoreAnimation = true
                                    }

                                    quizViewModel.submitAnswer(answer)

                                    if (autoNext) {
                                        delay(3000)
                                        if (autoNext) {
                                            scrollState.animateScrollTo(0)
                                            quizViewModel.moveToNextQuestion()
                                        }
                                    }

                                    isProcessing = false
                                }
                            }
                        }
                    )
                }

                // ==================== EXPLANATION ====================
                AnimatedVisibility(
                    visible = isAnswered,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkMode) Color(0xFF2D2D2D) else Color(0xFFF5F5F5)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showKnowledgeDetail = !showKnowledgeDetail },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (isDarkMode) Color(0xFF90CAF9) else Color(0xFF1976D2)
                                ),
                                contentPadding = PaddingValues(vertical = 10.dp)
                            ) {
                                Text(
                                    text = when (language) {
                                        "en" -> "Explanation"
                                        "zh" -> "解释"
                                        else -> "Giải thích đáp án"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = if (showKnowledgeDetail) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            AnimatedVisibility(
                                visible = showKnowledgeDetail,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFE3F2FD)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = when (language) {
                                                "en" -> "Correct answer:"
                                                "zh" -> "正确答案："
                                                else -> "Đáp án đúng:"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF1976D2)
                                        )
                                        Text(
                                            text = currentQuestion.correctAnswer,
                                            fontSize = 13.sp,
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.Medium,
                                            lineHeight = 18.sp
                                        )

                                        Divider(color = Color(0xFF90CAF9), thickness = 1.dp)

                                        Text(
                                            text = when (language) {
                                                "en" -> "Analysis:"
                                                "zh" -> "分析："
                                                else -> "Phân tích:"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF37474F)
                                        )
                                        Text(
                                            text = currentQuestion.explanation.ifEmpty {
                                                when (language) {
                                                    "en" -> "No explanation"
                                                    "zh" -> "没有解释"
                                                    else -> "Không có giải thích"
                                                }
                                            },
                                            fontSize = 13.sp,
                                            color = Color(0xFF37474F),
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ==================== NAVIGATION BUTTONS (when autoNext is OFF) ====================
                AnimatedVisibility(
                    visible = isAnswered && !autoNext,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // ✅ PREVIOUS BUTTON (but cannot change answer)
                        if (quizState.currentQuestionIndex > 0) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        scrollState.animateScrollTo(0)
                                        quizViewModel.moveToPreviousQuestion()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (isDarkMode) Color(0xFFCE93D8) else Color(0xFF7B1FA2)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (language) {
                                        "en" -> "Previous"
                                        "zh" -> "上一题"
                                        else -> "Câu trước"
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // NEXT/FINISH BUTTON
                        Button(
                            onClick = {
                                scope.launch {
                                    scrollState.animateScrollTo(0)
                                    quizViewModel.moveToNextQuestion()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7B1FA2)
                            )
                        ) {
                            Text(
                                text = if (quizState.currentQuestionIndex < quizState.questions.size - 1) {
                                    when (language) {
                                        "en" -> "Next"
                                        "zh" -> "下一题"
                                        else -> "Câu sau"
                                    }
                                } else {
                                    when (language) {
                                        "en" -> "Finish"
                                        "zh" -> "完成"
                                        else -> "Hoàn thành"
                                    }
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (quizState.currentQuestionIndex < quizState.questions.size - 1)
                                    Icons.Default.ArrowForward
                                else
                                    Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // ==================== SCORE ANIMATION ====================
            AnimatedVisibility(
                visible = showScoreAnimation,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF4CAF50),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .padding(top = 80.dp)
                        .scale(scoreScale)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "+${quizState.scoreGained}",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
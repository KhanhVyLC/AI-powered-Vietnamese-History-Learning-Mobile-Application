//File: TopicQuizPlayingScreen.kt - ✅ FIXED: Remove duplicate save
package com.example.pj

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicQuizPlayingScreen(
    topicId: Int,
    topicName: String,
    setNumber: Int,
    settingsViewModel: SettingsViewModel,
    firebaseViewModel: FirebaseViewModel,
    onFinish: (QuizResult) -> Unit,
    onBack: () -> Unit
) {
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val language by settingsViewModel.language.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val quizSet = remember { TopicQuizData.getQuizSet(topicId, setNumber) }

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var totalScore by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var lastScoreGained by remember { mutableIntStateOf(0) }
    var showScoreAnimation by remember { mutableStateOf(false) }
    var autoNext by remember { mutableStateOf(true) }
    val answeredQuestions = remember { mutableStateListOf<QuizAnswer>() }

    val currentQuestion = quizSet?.questions?.getOrNull(currentQuestionIndex)
    val currentAnsweredQuestion = answeredQuestions.find { it.questionId == currentQuestion?.id }

    var selectedAnswer by remember(currentQuestionIndex) {
        mutableStateOf<String?>(currentAnsweredQuestion?.userAnswer)
    }
    var showKnowledgeDetail by remember(currentQuestionIndex) { mutableStateOf(false) }
    var timeLeft by remember(currentQuestionIndex) { mutableIntStateOf(20) }
    var isAnswered by remember(currentQuestionIndex) {
        mutableStateOf(currentAnsweredQuestion != null)
    }
    var isProcessing by remember(currentQuestionIndex) { mutableStateOf(false) }
    var justAnswered by remember(currentQuestionIndex) { mutableStateOf(false) }

    val progress by animateFloatAsState(
        targetValue = (currentQuestionIndex + 1).toFloat() / (quizSet?.questions?.size ?: 1),
        label = "progress"
    )

    val surfaceColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black

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

    LaunchedEffect(currentQuestionIndex) {
        if (currentAnsweredQuestion == null) {
            timeLeft = 20
            isAnswered = false
            justAnswered = false

            while (timeLeft > 0 && !isAnswered) {
                delay(1000)
                timeLeft--
            }

            if (timeLeft == 0 && !isAnswered) {
                isAnswered = true
                justAnswered = true
                selectedAnswer = ""

                answeredQuestions.add(
                    QuizAnswer(
                        questionId = currentQuestion?.id ?: "",
                        questionText = currentQuestion?.question ?: "",
                        userAnswer = "",
                        correctAnswer = currentQuestion?.correctAnswer ?: "",
                        isCorrect = false,
                        timeSpent = 20
                    )
                )
            }
        }
    }

    // ✅ FIXED: Only create result and call onFinish, DO NOT save to Firebase here
    LaunchedEffect(justAnswered, autoNext, currentQuestionIndex) {
        if (justAnswered && autoNext) {
            delay(3000)

            if (autoNext) {
                if (currentQuestionIndex < quizSet!!.questions.size - 1) {
                    currentQuestionIndex++
                    scope.launch { scrollState.animateScrollTo(0) }
                } else {
                    // ✅ FIXED: Only create result and pass to onFinish
                    // MainActivity will handle saving via firebaseViewModel
                    val result = QuizResult(
                        userId = "",
                        quizId = "topic_${topicId}_set_${setNumber}_${System.currentTimeMillis()}",
                        topicId = topicId,
                        topicName = "$topicName - Set $setNumber",
                        score = totalScore,
                        totalQuestions = quizSet.questions.size,
                        correctAnswers = correctCount,
                        timeSpent = answeredQuestions.sumOf { it.timeSpent },
                        difficulty = "Topic",
                        answers = answeredQuestions.toList(),
                        completedAt = Date()
                    )
                    // ✅ Just pass result to callback, don't save here
                    onFinish(result)
                }
            }
        }
    }

    if (quizSet == null || currentQuestion == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Không tìm thấy bộ quiz", color = textColor)
        }
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$topicName - Set $setNumber",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${currentQuestionIndex + 1}/${quizSet.questions.size}",
                            fontSize = 12.sp,
                            color = textColor.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
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
                                text = "$totalScore",
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
                            text = "$timeLeft",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (timeLeft <= 5) Color.Red else textColor
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
                                    text = "Tự động chuyển câu   ",
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

                // ✅ Display image from drawable if available
                if (currentQuestion.imageRes != 0) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = currentQuestion.imageRes),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

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

                currentQuestion.answers.forEach { answer ->
                    CompactAnswerButton(
                        answer = answer,
                        isSelected = selectedAnswer == answer,
                        isCorrect = answer == currentQuestion.correctAnswer,
                        showResult = isAnswered,
                        enabled = !isAnswered && !isProcessing && currentAnsweredQuestion == null,
                        isDarkMode = isDarkMode,
                        onClick = {
                            if (!isAnswered && !isProcessing && currentAnsweredQuestion == null) {
                                scope.launch {
                                    isProcessing = true
                                    selectedAnswer = answer
                                    isAnswered = true
                                    justAnswered = true

                                    val isCorrect = answer == currentQuestion.correctAnswer
                                    val timeSpent = 20 - timeLeft

                                    if (isCorrect) {
                                        lastScoreGained = timeLeft
                                        totalScore += lastScoreGained
                                        correctCount++
                                        showScoreAnimation = true
                                    } else {
                                        lastScoreGained = 0
                                    }

                                    answeredQuestions.add(
                                        QuizAnswer(
                                            questionId = currentQuestion.id,
                                            questionText = currentQuestion.question,
                                            userAnswer = answer,
                                            correctAnswer = currentQuestion.correctAnswer,
                                            isCorrect = isCorrect,
                                            timeSpent = timeSpent.toLong()
                                        )
                                    )

                                    isProcessing = false
                                }
                            }
                        }
                    )
                }

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
                                    text = "Giải thích đáp án",
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
                                            text = "Đáp án đúng:",
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
                                            text = "Phân tích:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF37474F)
                                        )
                                        Text(
                                            text = currentQuestion.explanation,
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

                // ✅ NAVIGATION BUTTONS WITH PREVIOUS BUTTON
                AnimatedVisibility(
                    visible = isAnswered && !autoNext,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // ✅ PREVIOUS BUTTON (view only, cannot change answer)
                        if (currentQuestionIndex > 0) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        if (currentQuestionIndex > 0) {
                                            currentQuestionIndex--
                                            scrollState.animateScrollTo(0)
                                        }
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
                                    text = "Câu trước",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // ✅ FIXED: NEXT/FINISH BUTTON - Only call onFinish, don't save
                        Button(
                            onClick = {
                                scope.launch {
                                    if (currentQuestionIndex < quizSet.questions.size - 1) {
                                        currentQuestionIndex++
                                        scrollState.animateScrollTo(0)
                                    } else {
                                        // ✅ FIXED: Create result and pass to callback
                                        val result = QuizResult(
                                            userId = "",
                                            quizId = "topic_${topicId}_set_${setNumber}_${System.currentTimeMillis()}",
                                            topicId = topicId,
                                            topicName = "$topicName - Set $setNumber",
                                            score = totalScore,
                                            totalQuestions = quizSet.questions.size,
                                            correctAnswers = correctCount,
                                            timeSpent = answeredQuestions.sumOf { it.timeSpent },
                                            difficulty = "Topic",
                                            answers = answeredQuestions.toList(),
                                            completedAt = Date()
                                        )
                                        // ✅ Just call onFinish, MainActivity handles saving
                                        onFinish(result)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7B1FA2)
                            )
                        ) {
                            Text(
                                text = if (currentQuestionIndex < quizSet.questions.size - 1) "Câu sau" else "Hoàn thành",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (currentQuestionIndex < quizSet.questions.size - 1) Icons.Default.ArrowForward else Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Score Animation
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
                            text = "+$lastScoreGained",
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
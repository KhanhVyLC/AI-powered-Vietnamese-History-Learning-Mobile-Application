// File: PvpBattleScreen.kt - ✅ CRITICAL FIX: Remove accidental room creation
package com.example.pj

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import kotlinx.coroutines.delay

private const val TAG = "PvpBattleScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PvpBattleScreen(
    settingsViewModel: SettingsViewModel,
    firebaseViewModel: FirebaseViewModel,
    pvpViewModel: PvpViewModel,
    onNavigateToResult: () -> Unit,
    onBack: () -> Unit
) {
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val language by settingsViewModel.language.collectAsState()
    val authState by firebaseViewModel.authState.collectAsState()
    val pvpState by pvpViewModel.pvpState.collectAsState()

    val userId = authState.user?.uid ?: ""
    val currentRoom = pvpState.currentRoom
    val currentQuestionIndex = pvpState.currentQuestionIndex
    val currentQuestion = pvpViewModel.getCurrentQuestion()

    // ✅ CRITICAL: Validate room before rendering
    if (currentRoom == null) {
        Log.e(TAG, "❌ CRITICAL: Room is NULL in battle screen")
        LaunchedEffect(Unit) {
            delay(1000)
            onBack()
        }
        return
    }

    if (currentRoom.questions.isEmpty()) {
        Log.e(TAG, "❌ CRITICAL: No questions in room")
        LaunchedEffect(Unit) {
            delay(1000)
            onBack()
        }
        return
    }

    // ✅ Use key to prevent state reset when room updates
    val questionKey = remember(currentRoom.roomId, currentQuestionIndex) {
        "${currentRoom.roomId}_$currentQuestionIndex"
    }

    var selectedAnswer by remember(questionKey) { mutableStateOf<String?>(null) }
    var hasSubmitted by remember(questionKey) { mutableStateOf(false) }
    var questionStartTime by remember(questionKey) { mutableLongStateOf(System.currentTimeMillis()) }
    var timeLeft by remember(questionKey) { mutableIntStateOf(20) }

    val textColor = if (isDarkMode) Color.White else Color.Black
    val surfaceColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White

    // ✅ Check if user already answered in Firebase
    LaunchedEffect(questionKey) {
        Log.d(TAG, "📝 Question changed to index: $currentQuestionIndex")
        val userPlayer = currentRoom.getPlayer(userId)
        val alreadyAnswered = userPlayer?.hasAnswered(currentQuestionIndex) ?: false

        if (alreadyAnswered) {
            Log.d(TAG, "✅ User already answered this question")
            hasSubmitted = true
            val answer = userPlayer?.getAnswer(currentQuestionIndex)
            selectedAnswer = answer?.selectedAnswer
        } else {
            selectedAnswer = null
            hasSubmitted = false
            questionStartTime = System.currentTimeMillis()
            timeLeft = 20
        }
    }

    // ✅ Timer countdown with auto-submit
    LaunchedEffect(questionKey, hasSubmitted) {
        if (!hasSubmitted) {
            while (timeLeft > 0) {
                delay(1000)
                timeLeft -= 1
                if (timeLeft % 5 == 0) {
                    Log.d(TAG, "⏰ Time left: ${timeLeft}s")
                }
            }

            // ✅ AUTO-SUBMIT: Time's up
            if (!hasSubmitted) {
                Log.d(TAG, "⏰ Time's up! Auto-submitting...")

                // ✅ CRITICAL FIX: Only submit if room still exists and is IN_PROGRESS
                val currentRoomCheck = pvpViewModel.pvpState.value.currentRoom
                if (currentRoomCheck?.roomId == currentRoom.roomId &&
                    currentRoomCheck.status == RoomStatus.IN_PROGRESS) {

                    submitAnswer(
                        pvpViewModel = pvpViewModel,
                        roomId = currentRoom.roomId,
                        userId = userId,
                        questionIndex = currentQuestionIndex,
                        selectedAnswer = selectedAnswer ?: "",
                        timeSpent = 20000
                    )
                    hasSubmitted = true
                } else {
                    Log.w(TAG, "⚠️ Room changed or ended, skip auto-submit")
                }
            }
        }
    }

    // Navigate to result when match finished
    LaunchedEffect(currentRoom.status) {
        if (currentRoom.status == RoomStatus.FINISHED) {
            Log.d(TAG, "🏁 Match finished, navigating to result...")
            delay(2000)
            onNavigateToResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (language) {
                            "en" -> "Battle"
                            "zh" -> "对战中"
                            else -> "Đang đấu"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFF7B1FA2),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
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
            // Score Bar
            ScoreBar(
                userScore = pvpViewModel.getUserScore(userId),
                opponentScore = pvpViewModel.getOpponentScore(userId),
                userPlayer = currentRoom.getPlayer(userId),
                opponentPlayer = currentRoom.getOpponent(userId),
                isDarkMode = isDarkMode
            )

            // Progress Bar
            LinearProgressIndicator(
                progress = { (currentQuestionIndex + 1).toFloat() / currentRoom.questionCount },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFF7B1FA2),
                trackColor = Color.Gray.copy(alpha = 0.3f)
            )

            // Question Counter & Timer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (language) {
                        "en" -> "Question ${currentQuestionIndex + 1}/${currentRoom.questionCount}"
                        "zh" -> "题目 ${currentQuestionIndex + 1}/${currentRoom.questionCount}"
                        else -> "Câu ${currentQuestionIndex + 1}/${currentRoom.questionCount}"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textColor
                )

                TimerChip(
                    timeLeft = timeLeft,
                    totalTime = 20,
                    isDarkMode = isDarkMode
                )
            }

            // Question & Answers
            if (currentQuestion == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            color = Color(0xFF7B1FA2),
                            strokeWidth = 6.dp
                        )
                        Text(
                            text = when (language) {
                                "en" -> "Loading question..."
                                "zh" -> "加载题目..."
                                else -> "Đang tải câu hỏi..."
                            },
                            color = textColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Question Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = surfaceColor
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = currentQuestion.question,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 28.sp,
                                color = textColor
                            )
                        }
                    }

                    // Answer Options
                    currentQuestion.answers.forEach { answer ->
                        AnswerButton(
                            answer = answer,
                            isSelected = selectedAnswer == answer,
                            isCorrect = hasSubmitted && answer == currentQuestion.correctAnswer,
                            isWrong = hasSubmitted && selectedAnswer == answer && answer != currentQuestion.correctAnswer,
                            hasAnswered = hasSubmitted,
                            isDarkMode = isDarkMode,
                            onClick = {
                                if (!hasSubmitted) {
                                    Log.d(TAG, "📍 Answer selected: $answer")
                                    selectedAnswer = answer
                                }
                            }
                        )
                    }

                    // Submit Button
                    if (!hasSubmitted && selectedAnswer != null) {
                        Button(
                            onClick = {
                                Log.d(TAG, "📤 Submitting answer: $selectedAnswer")

                                // ✅ CRITICAL FIX: Validate room before submit
                                val currentRoomCheck = pvpViewModel.pvpState.value.currentRoom
                                if (currentRoomCheck?.roomId == currentRoom.roomId &&
                                    currentRoomCheck.status == RoomStatus.IN_PROGRESS) {

                                    submitAnswer(
                                        pvpViewModel = pvpViewModel,
                                        roomId = currentRoom.roomId,
                                        userId = userId,
                                        questionIndex = currentQuestionIndex,
                                        selectedAnswer = selectedAnswer ?: "",
                                        timeSpent = System.currentTimeMillis() - questionStartTime
                                    )
                                    hasSubmitted = true
                                } else {
                                    Log.e(TAG, "❌ Cannot submit: Room changed or ended")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7B1FA2)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = when (language) {
                                    "en" -> "Submit Answer"
                                    "zh" -> "提交答案"
                                    else -> "Gửi câu trả lời"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Waiting for opponent
                    if (hasSubmitted) {
                        val opponentAnswered = currentRoom.getOpponent(userId)?.hasAnswered(currentQuestionIndex) ?: false

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkMode) Color(0xFF2C2C2C) else Color(0xFFE3F2FD)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (opponentAnswered) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50)
                                    )
                                    Text(
                                        text = when (language) {
                                            "en" -> "Both players answered!"
                                            "zh" -> "双方已答题！"
                                            else -> "Cả hai đã trả lời!"
                                        },
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 3.dp
                                    )
                                    Text(
                                        text = when (language) {
                                            "en" -> "Waiting for opponent..."
                                            "zh" -> "等待对手..."
                                            else -> "Đang đợi đối thủ..."
                                        },
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        // Explanation
                        if (currentQuestion.explanation.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDarkMode) Color(0xFF2C2C2C) else Color(0xFFFFF3E0)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = Color(0xFFFF9800)
                                        )
                                        Text(
                                            text = when (language) {
                                                "en" -> "Explanation"
                                                "zh" -> "解释"
                                                else -> "Giải thích"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF9800)
                                        )
                                    }
                                    Text(
                                        text = currentQuestion.explanation,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp,
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreBar(
    userScore: Int,
    opponentScore: Int,
    userPlayer: PvpPlayer?,
    opponentPlayer: PvpPlayer?,
    isDarkMode: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF2C2C2C) else Color.White
        ),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = userPlayer?.displayName ?: "You",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = "$userScore pts",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7B1FA2)
                )
            }

            Text(
                text = "VS",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = opponentPlayer?.displayName ?: "Opponent",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = "$opponentScore pts",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B6B)
                )
            }
        }
    }
}

@Composable
fun TimerChip(timeLeft: Int, totalTime: Int, isDarkMode: Boolean) {
    val color = when {
        timeLeft <= 5 -> Color(0xFFF44336)
        timeLeft <= 10 -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "${timeLeft}s",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun AnswerButton(
    answer: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isWrong: Boolean,
    hasAnswered: Boolean,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isCorrect -> Color(0xFF4CAF50)
        isWrong -> Color(0xFFF44336)
        isSelected && !hasAnswered -> Color(0xFF7B1FA2)
        else -> if (isDarkMode) Color(0xFF2C2C2C) else Color.White
    }

    val borderColor = when {
        isCorrect -> Color(0xFF4CAF50)
        isWrong -> Color(0xFFF44336)
        isSelected && !hasAnswered -> Color(0xFF7B1FA2)
        else -> Color.Gray.copy(alpha = 0.3f)
    }

    val textColor = when {
        isCorrect || isWrong -> Color.White
        isSelected && !hasAnswered -> Color.White
        else -> if (isDarkMode) Color.White else Color.Black
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        onClick = onClick,
        enabled = !hasAnswered,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = answer,
                fontSize = 16.sp,
                color = textColor,
                modifier = Modifier.weight(1f)
            )

            if (hasAnswered) {
                Icon(
                    imageVector = if (isCorrect) Icons.Default.CheckCircle
                    else if (isWrong) Icons.Default.Close
                    else Icons.Default.Info,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

// ✅ CRITICAL FIX: Add validation before submit
private fun submitAnswer(
    pvpViewModel: PvpViewModel,
    roomId: String,
    userId: String,
    questionIndex: Int,
    selectedAnswer: String,
    timeSpent: Long
) {
    Log.d(TAG, "╔════════════════════════════════════════╗")
    Log.d(TAG, "║   SUBMITTING ANSWER                    ║")
    Log.d(TAG, "╚════════════════════════════════════════╝")
    Log.d(TAG, "Room ID: $roomId")
    Log.d(TAG, "Question Index: $questionIndex")
    Log.d(TAG, "Selected Answer: $selectedAnswer")
    Log.d(TAG, "Time Spent: ${timeSpent}ms")

    pvpViewModel.submitAnswer(
        roomId = roomId,
        userId = userId,
        questionIndex = questionIndex,
        selectedAnswer = selectedAnswer,
        timeSpent = timeSpent
    )
}
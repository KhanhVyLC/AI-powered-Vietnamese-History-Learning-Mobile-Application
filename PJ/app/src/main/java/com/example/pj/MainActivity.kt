// File: MainActivity.kt - ✅ UPDATED: Fixed PvP Result navigation
package com.example.pj

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val firebaseViewModel: FirebaseViewModel = viewModel()
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
            val authState by firebaseViewModel.authState.collectAsState()

            VietnamHistoryQuizTheme(isDarkMode = isDarkMode) {
                if (authState.isAuthenticated) {
                    VietnamHistoryQuizApp(
                        settingsViewModel = settingsViewModel,
                        firebaseViewModel = firebaseViewModel
                    )
                } else {
                    LoginScreen(
                        settingsViewModel = settingsViewModel,
                        firebaseViewModel = firebaseViewModel,
                        onLoginSuccess = { }
                    )
                }
            }
        }
    }
}

@Composable
fun VietnamHistoryQuizApp(
    settingsViewModel: SettingsViewModel,
    firebaseViewModel: FirebaseViewModel
) {
    val pvpViewModel: PvpViewModel = viewModel()
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()

    var currentScreen by remember { mutableStateOf("home") }
    var selectedTopic by remember { mutableStateOf<Topic?>(null) }
    var selectedQuizSet by remember { mutableIntStateOf(0) }
    var dailyQuizCount by remember { mutableIntStateOf(5) }
    var dailyQuizDifficulty by remember { mutableStateOf("Trung bình") }
    var quizResult by remember { mutableStateOf<QuizResult?>(null) }

    LaunchedEffect(currentScreen) {
        Log.d(TAG, "Navigation: $currentScreen")
    }

    when (currentScreen) {
        "home" -> HomeScreen(
            settingsViewModel = settingsViewModel,
            firebaseViewModel = firebaseViewModel,
            onNavigate = { screen ->
                Log.d(TAG, "Navigating to: $screen")
                currentScreen = screen
            },
            onTopicClick = { topic ->
                selectedTopic = topic
                currentScreen = "topic_sets"
            },
            onDailyQuizClick = {
                currentScreen = "daily_quiz_setup"
            }
        )

        "chat" -> ChatScreen(
            settingsViewModel = settingsViewModel,
            firebaseViewModel = firebaseViewModel,
            onBack = { currentScreen = "home" }
        )

        "leaderboard" -> LeaderboardScreen(
            settingsViewModel = settingsViewModel,
            firebaseViewModel = firebaseViewModel,
            onBack = { currentScreen = "home" }
        )

        "history" -> QuizHistoryScreen(
            settingsViewModel = settingsViewModel,
            firebaseViewModel = firebaseViewModel,
            onBack = { currentScreen = "home" },
            onResultClick = { result ->
                quizResult = result
                currentScreen = "quiz_result"
            }
        )

        "topic_sets" -> {
            selectedTopic?.let { topic ->
                TopicSetsScreen(
                    topic = topic,
                    settingsViewModel = settingsViewModel,
                    firebaseViewModel = firebaseViewModel,
                    onBack = { currentScreen = "home" },
                    onSetClick = { setNumber ->
                        selectedQuizSet = setNumber
                        currentScreen = "topic_quiz_playing"
                    }
                )
            }
        }

        "topic_quiz_playing" -> {
            selectedTopic?.let { topic ->
                TopicQuizPlayingScreen(
                    topicId = topic.id,
                    topicName = topic.nameVi,
                    setNumber = selectedQuizSet,
                    settingsViewModel = settingsViewModel,
                    firebaseViewModel = firebaseViewModel,
                    onFinish = { result ->
                        firebaseViewModel.saveTopicQuizResult(
                            result = result,
                            topicId = topic.id,
                            setNumber = selectedQuizSet
                        )
                        quizResult = result
                        currentScreen = "quiz_result"
                    },
                    onBack = {
                        currentScreen = "topic_sets"
                    }
                )
            }
        }

        "daily_quiz_setup" -> DailyQuizSetupScreen(
            settingsViewModel = settingsViewModel,
            firebaseViewModel = firebaseViewModel,
            onBack = { currentScreen = "home" },
            onStartQuiz = { count, difficulty ->
                dailyQuizCount = count
                dailyQuizDifficulty = difficulty
                currentScreen = "daily_quiz_playing"
            }
        )

        "daily_quiz_playing" -> DailyQuizPlayingScreen(
            questionCount = dailyQuizCount,
            difficulty = dailyQuizDifficulty,
            settingsViewModel = settingsViewModel,
            firebaseViewModel = firebaseViewModel,
            onFinish = { result ->
                quizResult = result
                currentScreen = "quiz_result"
            },
            onBack = { currentScreen = "daily_quiz_setup" }
        )

        "daily_quiz_history" -> DailyQuizHistoryScreen(
            settingsViewModel = settingsViewModel,
            firebaseViewModel = firebaseViewModel,
            onBack = { currentScreen = "home" },
            onResultClick = { result ->
                quizResult = result
                currentScreen = "quiz_result"
            }
        )

        "quiz_result" -> {
            quizResult?.let { result ->
                QuizResultScreen(
                    result = result,
                    settingsViewModel = settingsViewModel,
                    onPlayAgain = {
                        currentScreen = if (result.topicId == 0) {
                            "daily_quiz_setup"
                        } else {
                            "topic_sets"
                        }
                    },
                    onBackHome = {
                        currentScreen = "home"
                    }
                )
            }
        }

        "settings" -> SettingsScreen(
            settingsViewModel = settingsViewModel,
            firebaseViewModel = firebaseViewModel,
            onBack = { currentScreen = "home" }
        )

        // ==================== PVP NAVIGATION ====================
        "pvp_lobby" -> {
            Log.d(TAG, "Rendering PvpLobbyScreen")
            PvpLobbyScreen(
                settingsViewModel = settingsViewModel,
                firebaseViewModel = firebaseViewModel,
                pvpViewModel = pvpViewModel,
                onNavigateToWaitingRoom = {
                    Log.d(TAG, "✅ Navigating Lobby → Waiting Room (Friend Match)")
                    currentScreen = "pvp_waiting_room"
                },
                onNavigateToQuickMatchWaiting = {
                    Log.d(TAG, "✅ Navigating Lobby → Quick Match Waiting")
                    currentScreen = "pvp_quick_match_waiting"
                },
                onNavigateToBattle = {
                    Log.d(TAG, "✅ Navigating Lobby → Battle")
                    currentScreen = "pvp_battle"
                },
                onBack = {
                    pvpViewModel.resetPvpState()
                    currentScreen = "home"
                }
            )
        }

        "pvp_quick_match_waiting" -> {
            Log.d(TAG, "Rendering PvpQuickMatchWaitingScreen")

            BackHandler {
                Log.d(TAG, "⚠️ Back pressed in Quick Match Waiting - ignoring")
            }

            PvpQuickMatchWaitingScreen(
                settingsViewModel = settingsViewModel,
                pvpViewModel = pvpViewModel,
                onNavigateToBattle = {
                    Log.d(TAG, "✅ Navigating Quick Match Waiting → Battle")
                    currentScreen = "pvp_battle"
                },
                onCancel = {
                    val roomId = pvpViewModel.pvpState.value.currentRoom?.roomId
                    val userId = firebaseViewModel.authState.value.user?.uid
                    if (roomId != null && userId != null) {
                        Log.d(TAG, "🚪 Canceling match, leaving room...")
                        pvpViewModel.leaveRoom(roomId, userId)
                    }
                    pvpViewModel.resetPvpState()
                    currentScreen = "pvp_lobby"
                }
            )
        }

        "pvp_waiting_room" -> {
            Log.d(TAG, "Rendering PvpWaitingRoomScreen")

            PvpWaitingRoomScreen(
                settingsViewModel = settingsViewModel,
                firebaseViewModel = firebaseViewModel,
                pvpViewModel = pvpViewModel,
                onNavigateToBattle = {
                    Log.d(TAG, "✅ Navigating Waiting Room → Battle")
                    currentScreen = "pvp_battle"
                },
                onBack = {
                    val roomId = pvpViewModel.pvpState.value.currentRoom?.roomId
                    val userId = firebaseViewModel.authState.value.user?.uid
                    if (roomId != null && userId != null) {
                        pvpViewModel.leaveRoom(roomId, userId)
                    }
                    currentScreen = "pvp_lobby"
                }
            )
        }

        "pvp_battle" -> {
            Log.d(TAG, "=== RENDERING PVP BATTLE SCREEN ===")

            val pvpState by pvpViewModel.pvpState.collectAsState()
            val currentRoom = pvpState.currentRoom

            // ✅ CRITICAL: Prevent back navigation during battle
            BackHandler {
                Log.d(TAG, "⚠️ Back pressed during battle - ignoring")
            }

            // ✅ CRITICAL: Auto-navigate to result when match finishes
            LaunchedEffect(currentRoom?.status) {
                if (currentRoom?.status == RoomStatus.FINISHED) {
                    Log.d(TAG, "🏁 Match FINISHED, navigating to result...")
                    kotlinx.coroutines.delay(1500) // Delay để user thấy câu hỏi cuối
                    currentScreen = "pvp_result"
                }
            }

            // ✅ Detailed logging
            LaunchedEffect(currentRoom) {
                Log.d(TAG, "╔════════════════════════════════════════╗")
                Log.d(TAG, "║   BATTLE SCREEN - ROOM CHECK           ║")
                Log.d(TAG, "╚════════════════════════════════════════╝")
                Log.d(TAG, "Room ID: ${currentRoom?.roomId ?: "NULL"}")
                Log.d(TAG, "Status: ${currentRoom?.status}")
                Log.d(TAG, "Players: ${currentRoom?.players?.size ?: 0}")
                Log.d(TAG, "Questions: ${currentRoom?.questions?.size ?: 0}")

                currentRoom?.questions?.forEachIndexed { index, q ->
                    Log.d(TAG, "Q$index: ${q.question.take(50)}...")
                    Log.d(TAG, "  Answers: ${q.answers.size}")
                }
                Log.d(TAG, "═════════════════════════════════════════")
            }

            // ✅ Handle different cases
            when {
                currentRoom == null -> {
                    Log.e(TAG, "❌ ERROR: Room is NULL")
                    ErrorScreen(
                        message = "Lỗi: Không tìm thấy phòng",
                        onRetry = {
                            Log.d(TAG, "Retry: Going back to lobby")
                            pvpViewModel.resetPvpState()
                            currentScreen = "pvp_lobby"
                        }
                    )
                }

                currentRoom.questions.isEmpty() -> {
                    Log.e(TAG, "❌ ERROR: No questions in room")
                    ErrorScreen(
                        message = "Lỗi: Không có câu hỏi",
                        onRetry = {
                            Log.d(TAG, "Retry: Going back to lobby")
                            pvpViewModel.resetPvpState()
                            currentScreen = "pvp_lobby"
                        }
                    )
                }

                currentRoom.status == RoomStatus.FINISHED -> {
                    // ✅ Match finished - show loading while navigating
                    Log.d(TAG, "🏁 Room FINISHED, waiting for navigation...")
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (isDarkMode) Color(0xFF121212) else Color.White
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(80.dp),
                                color = Color(0xFF7B1FA2),
                                strokeWidth = 6.dp
                            )

                            Text(
                                text = "Đang tính điểm...",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color.White else Color.Black
                            )

                            Text(
                                text = "Vui lòng đợi...",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                currentRoom.status == RoomStatus.CANCELLED -> {
                    // ✅ Match cancelled
                    Log.w(TAG, "❌ Room CANCELLED")
                    ErrorScreen(
                        message = "Trận đấu đã bị hủy",
                        onRetry = {
                            pvpViewModel.resetPvpState()
                            currentScreen = "pvp_lobby"
                        }
                    )
                }

                currentRoom.status == RoomStatus.STARTING -> {
                    // ✅ Show countdown while room is starting
                    Log.d(TAG, "⏰ Room STARTING, showing loading...")
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (isDarkMode) Color(0xFF121212) else Color.White
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(80.dp),
                                color = Color(0xFF7B1FA2),
                                strokeWidth = 6.dp
                            )

                            Text(
                                text = "Đang chuẩn bị trận đấu...",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color.White else Color.Black
                            )

                            Text(
                                text = "Vui lòng đợi...",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                currentRoom.status == RoomStatus.WAITING -> {
                    // ✅ Still waiting for players
                    Log.d(TAG, "⏰ Room WAITING, showing loading...")
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (isDarkMode) Color(0xFF121212) else Color.White
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(80.dp),
                                color = Color(0xFF7B1FA2),
                                strokeWidth = 6.dp
                            )

                            Text(
                                text = "Đang đợi đối thủ...",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color.White else Color.Black
                            )

                            Text(
                                text = "Players: ${currentRoom.players.size}/2",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                currentRoom.status == RoomStatus.IN_PROGRESS -> {
                    // ✅ Room is IN_PROGRESS - show battle
                    Log.d(TAG, "✅ Room IN_PROGRESS, rendering battle screen")

                    PvpBattleScreen(
                        settingsViewModel = settingsViewModel,
                        firebaseViewModel = firebaseViewModel,
                        pvpViewModel = pvpViewModel,
                        onNavigateToResult = {
                            Log.d(TAG, "✅ Manual navigation Battle → Result")
                            currentScreen = "pvp_result"
                        },
                        onBack = {
                            val roomId = currentRoom.roomId
                            val userId = firebaseViewModel.authState.value.user?.uid
                            if (userId != null) {
                                pvpViewModel.leaveRoom(roomId, userId)
                            }
                            pvpViewModel.resetPvpState()
                            currentScreen = "home"
                        }
                    )
                }

                else -> {
                    // ✅ Unexpected status
                    Log.w(TAG, "⚠️ WARNING: Unexpected status ${currentRoom.status}")
                    ErrorScreen(
                        message = "Lỗi: Trạng thái không hợp lệ\nStatus: ${currentRoom.status}",
                        onRetry = {
                            Log.d(TAG, "Retry: Going back to lobby")
                            pvpViewModel.resetPvpState()
                            currentScreen = "pvp_lobby"
                        }
                    )
                }
            }
        }

        "pvp_result" -> {
            Log.d(TAG, "Rendering PvpResultScreen")
            PvpResultScreen(
                settingsViewModel = settingsViewModel,
                firebaseViewModel = firebaseViewModel,
                pvpViewModel = pvpViewModel,
                onPlayAgain = {
                    // ✅ "Trận tiếp" → Navigate to PVP Lobby
                    Log.d(TAG, "✅ Next Match → PVP Lobby")
                    pvpViewModel.resetPvpState()
                    currentScreen = "pvp_lobby"
                },
                onBackToLobby = {
                    // ✅ "Trang chủ" → Navigate to Home
                    Log.d(TAG, "✅ Home → HomeScreen")
                    pvpViewModel.resetPvpState()
                    currentScreen = "home"
                }
            )
        }

        else -> {
            Log.e(TAG, "❌ Unknown screen: $currentScreen, redirecting to home")
            LaunchedEffect(Unit) {
                currentScreen = "home"
            }
        }
    }
}

@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2C2C2C)
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(80.dp)
                )

                Text(
                    text = message,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Đang quay về sảnh...",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3D5178)
                    )
                ) {
                    Text(
                        text = "Quay lại sảnh",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Auto redirect after 3 seconds
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000)
        onRetry()
    }
}
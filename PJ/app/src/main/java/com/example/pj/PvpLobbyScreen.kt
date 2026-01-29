// File: PvpLobbyScreen.kt - ✅ UPDATED: Custom icons and timestamp for match history
package com.example.pj

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "PvpLobbyScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PvpLobbyScreen(
    settingsViewModel: SettingsViewModel,
    firebaseViewModel: FirebaseViewModel,
    pvpViewModel: PvpViewModel,
    onNavigateToWaitingRoom: () -> Unit,
    onNavigateToQuickMatchWaiting: () -> Unit,
    onNavigateToBattle: () -> Unit,
    onBack: () -> Unit
) {
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val language by settingsViewModel.language.collectAsState()
    val authState by firebaseViewModel.authState.collectAsState()
    val pvpState by pvpViewModel.pvpState.collectAsState()

    val userProfile = authState.userProfile
    val userId = authState.user?.uid ?: ""
    val username = userProfile?.username ?: "Player"
    val displayName = userProfile?.displayName ?: username

    var showDifficultyDialog by remember { mutableStateOf(false) }
    var showFriendRoomDialog by remember { mutableStateOf(false) }
    var selectedMode by remember { mutableStateOf<PvpMode?>(null) }

    // ✅ CRITICAL FIX: Track navigation per room ID
    var hasNavigatedQuickMatch by remember { mutableStateOf(false) }
    var hasNavigatedFriendMatch by remember { mutableStateOf(false) }
    var lastNavigatedRoomId by remember { mutableStateOf<String?>(null) }

    val surfaceColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val subTextColor = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray

    // ✅ Load stats once
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            Log.d(TAG, "📊 Loading stats for: $userId")
            pvpViewModel.loadUserStats(userId)
            pvpViewModel.loadMatchHistory(userId)
        }
    }

    // ✅ CRITICAL FIX: Reset flags when room changes or cleared
    LaunchedEffect(pvpState.currentRoom?.roomId) {
        val currentRoomId = pvpState.currentRoom?.roomId

        if (currentRoomId == null) {
            hasNavigatedQuickMatch = false
            hasNavigatedFriendMatch = false
            lastNavigatedRoomId = null
            Log.d(TAG, "🔄 Room cleared, reset all navigation flags")
        } else if (currentRoomId != lastNavigatedRoomId) {
            hasNavigatedQuickMatch = false
            hasNavigatedFriendMatch = false
            lastNavigatedRoomId = currentRoomId
            Log.d(TAG, "🆕 New room: $currentRoomId, reset navigation flags")
        }
    }

    // ✅ CRITICAL FIX: Navigate for new rooms, skip for already navigated rooms
    LaunchedEffect(pvpState.currentRoom) {
        val room = pvpState.currentRoom

        if (room == null) {
            Log.d(TAG, "⚠️ No room available")
            return@LaunchedEffect
        }

        // ✅ Check if already navigated for this room
        if (room.roomId == lastNavigatedRoomId) {
            val alreadyNavigated = when (room.mode) {
                PvpMode.QUICK_MATCH -> hasNavigatedQuickMatch
                PvpMode.FRIEND_MATCH -> hasNavigatedFriendMatch
                else -> false
            }

            if (alreadyNavigated) {
                Log.d(TAG, "⚠️ Already navigated for room ${room.roomId}")
                return@LaunchedEffect
            }
        }

        Log.d(TAG, "╔════════════════════════════════════════╗")
        Log.d(TAG, "║   NAVIGATION LOGIC CHECK               ║")
        Log.d(TAG, "╚════════════════════════════════════════╝")
        Log.d(TAG, "Room ID: ${room.roomId}")
        Log.d(TAG, "Status: ${room.status}")
        Log.d(TAG, "Mode: ${room.mode}")
        Log.d(TAG, "Players: ${room.players.size}/2")
        Log.d(TAG, "Questions: ${room.questions.size}")
        Log.d(TAG, "User ID: $userId")
        Log.d(TAG, "Is Host: ${room.isHost(userId)}")
        Log.d(TAG, "User in Room: ${room.players.containsKey(userId)}")
        Log.d(TAG, "Quick Match Nav: $hasNavigatedQuickMatch")
        Log.d(TAG, "Friend Match Nav: $hasNavigatedFriendMatch")
        Log.d(TAG, "Last Nav Room: $lastNavigatedRoomId")

        delay(300)

        when (room.mode) {
            PvpMode.QUICK_MATCH -> {
                // ✅ FIX: Navigate based on room status
                if (!hasNavigatedQuickMatch && room.questions.isNotEmpty()) {
                    when (room.status) {
                        RoomStatus.WAITING -> {
                            Log.d(TAG, "✅ NAVIGATING → Quick Match Waiting (WAITING)")
                            hasNavigatedQuickMatch = true
                            lastNavigatedRoomId = room.roomId
                            onNavigateToQuickMatchWaiting()
                        }
                        RoomStatus.STARTING -> {
                            Log.d(TAG, "✅ NAVIGATING → Quick Match Waiting (STARTING)")
                            hasNavigatedQuickMatch = true
                            lastNavigatedRoomId = room.roomId
                            onNavigateToQuickMatchWaiting()
                        }
                        RoomStatus.IN_PROGRESS -> {
                            // ✅ CRITICAL FIX: Late join - go directly to battle
                            Log.d(TAG, "✅ NAVIGATING → Battle (late join, IN_PROGRESS)")
                            hasNavigatedQuickMatch = true
                            lastNavigatedRoomId = room.roomId
                            onNavigateToBattle()
                        }
                        else -> {
                            Log.d(TAG, "⏳ Quick Match waiting (status=${room.status})")
                        }
                    }
                } else if (hasNavigatedQuickMatch) {
                    Log.d(TAG, "⚠️ Already navigated for this room")
                } else {
                    Log.d(TAG, "⏳ Quick Match waiting (q=${room.questions.size})")
                }
            }

            PvpMode.FRIEND_MATCH -> {
                val isHost = room.isHost(userId)
                val userInRoom = room.players.containsKey(userId)
                val hasQuestions = room.questions.isNotEmpty()

                // ✅ Navigate based on room status
                val shouldNavigate = if (!hasNavigatedFriendMatch && userInRoom && hasQuestions) {
                    when (room.status) {
                        RoomStatus.WAITING -> isHost && room.players.size == 1
                        RoomStatus.STARTING, RoomStatus.IN_PROGRESS -> true
                        else -> false
                    }
                } else false

                Log.d(TAG, "─────────────────────────────────────────")
                Log.d(TAG, "Friend Match Evaluation:")
                Log.d(TAG, "  Is Host: $isHost")
                Log.d(TAG, "  User In Room: $userInRoom")
                Log.d(TAG, "  Has Questions: $hasQuestions")
                Log.d(TAG, "  Status: ${room.status}")
                Log.d(TAG, "  Players: ${room.players.size}")
                Log.d(TAG, "  Should Navigate: $shouldNavigate")
                Log.d(TAG, "─────────────────────────────────────────")

                if (shouldNavigate) {
                    Log.d(TAG, "✅ NAVIGATING → Friend Match")
                    hasNavigatedFriendMatch = true
                    lastNavigatedRoomId = room.roomId

                    when (room.status) {
                        RoomStatus.IN_PROGRESS -> {
                            Log.d(TAG, "   Target: Battle (IN_PROGRESS)")
                            onNavigateToBattle()
                        }
                        else -> {
                            Log.d(TAG, "   Target: Waiting Room (${room.status})")
                            onNavigateToWaitingRoom()
                        }
                    }
                }
            }

            else -> {
                Log.w(TAG, "⚠️ Unknown mode: ${room.mode}")
            }
        }

        Log.d(TAG, "════════════════════════════════════════")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (language) {
                            "en" -> "PvP Battle"
                            "zh" -> "对战竞技"
                            else -> "Đấu PvP"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFF7B1FA2),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                pvpState.userStats?.let { stats ->
                    UserStatsCard(
                        stats = stats,
                        isDarkMode = isDarkMode,
                        language = language
                    )
                }

                PvpModeCard(
                    title = when (language) {
                        "en" -> "Quick Match"
                        "zh" -> "快速匹配"
                        else -> "Tìm trận nhanh"
                    },
                    description = when (language) {
                        "en" -> "Find an opponent instantly"
                        "zh" -> "立即寻找对手"
                        else -> "Tìm đối thủ ngay lập tức"
                    },
                    icon = Icons.Default.PlayArrow,
                    gradient = listOf(Color(0xFFFF6B6B), Color(0xFFEE5A6F)),
                    isLoading = pvpState.isSearchingMatch || pvpState.isLoading,
                    onClick = {
                        Log.d(TAG, "🎮 Quick Match clicked")
                        selectedMode = PvpMode.QUICK_MATCH
                        showDifficultyDialog = true
                    }
                )

                PvpModeCard(
                    title = when (language) {
                        "en" -> "Play with Friend"
                        "zh" -> "与好友对战"
                        else -> "Chơi với bạn"
                    },
                    description = when (language) {
                        "en" -> "Create or join a room"
                        "zh" -> "创建或加入房间"
                        else -> "Tạo hoặc vào phòng"
                    },
                    icon = Icons.Default.Person,
                    gradient = listOf(Color(0xFF4ECDC4), Color(0xFF44A08D)),
                    isLoading = pvpState.isSearchingMatch || pvpState.isLoading,
                    onClick = {
                        Log.d(TAG, "👥 Friend Match clicked")
                        selectedMode = PvpMode.FRIEND_MATCH
                        showFriendRoomDialog = true
                    }
                )

                Text(
                    text = when (language) {
                        "en" -> "Recent Matches"
                        "zh" -> "最近比赛"
                        else -> "Trận đấu gần đây"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(top = 8.dp)
                )

                if (pvpState.matchHistory.isEmpty()) {
                    EmptyMatchHistoryCard(
                        isDarkMode = isDarkMode,
                        language = language,
                        subTextColor = subTextColor
                    )
                } else {
                    pvpState.matchHistory.take(5).forEach { result ->
                        MatchHistoryItem(
                            result = result,
                            userId = userId,
                            isDarkMode = isDarkMode,
                            language = language
                        )
                    }
                }
            }

            if (pvpState.isSearchingMatch || pvpState.isLoading) {
                LoadingOverlay(
                    pvpState = pvpState,
                    selectedMode = selectedMode,
                    isDarkMode = isDarkMode,
                    language = language,
                    textColor = textColor,
                    onCancel = {
                        Log.d(TAG, "🚫 Cancel loading")
                        hasNavigatedQuickMatch = false
                        hasNavigatedFriendMatch = false
                        lastNavigatedRoomId = null
                        pvpViewModel.resetPvpState()
                    }
                )
            }

            if (showDifficultyDialog) {
                DifficultySelectionDialog(
                    language = language,
                    onDismiss = {
                        Log.d(TAG, "❌ Difficulty dialog dismissed")
                        showDifficultyDialog = false
                        selectedMode = null
                    },
                    onConfirm = { difficulty, questionCount ->
                        Log.d(TAG, "✅ Selected: $difficulty, $questionCount questions")
                        showDifficultyDialog = false

                        if (selectedMode == PvpMode.QUICK_MATCH) {
                            Log.d(TAG, "🚀 Starting Quick Match...")
                            pvpViewModel.findQuickMatch(
                                userId = userId,
                                username = username,
                                displayName = displayName,
                                difficulty = difficulty,
                                questionCount = questionCount
                            )
                        }
                    }
                )
            }

            if (showFriendRoomDialog) {
                FriendRoomDialog(
                    language = language,
                    onDismiss = {
                        Log.d(TAG, "❌ Friend room dialog dismissed")
                        showFriendRoomDialog = false
                        selectedMode = null
                    },
                    onCreateRoom = { difficulty, questionCount ->
                        Log.d(TAG, "🏗️ Creating room: $difficulty, $questionCount")
                        showFriendRoomDialog = false
                        pvpViewModel.createFriendRoom(
                            hostUserId = userId,
                            hostUsername = username,
                            hostDisplayName = displayName,
                            difficulty = difficulty,
                            questionCount = questionCount
                        )
                    },
                    onJoinRoom = { roomCode ->
                        Log.d(TAG, "🚪 Joining room: $roomCode")
                        showFriendRoomDialog = false
                        pvpViewModel.joinFriendRoom(
                            roomId = roomCode,
                            userId = userId,
                            username = username,
                            displayName = displayName
                        )
                    }
                )
            }
        }

        pvpState.error?.let { error ->
            LaunchedEffect(error) {
                Log.e(TAG, "❌ Error: $error")
                delay(3000)
                pvpViewModel.clearError()
            }

            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = {
                        pvpViewModel.clearError()
                        hasNavigatedQuickMatch = false
                        hasNavigatedFriendMatch = false
                        lastNavigatedRoomId = null
                    }) {
                        Text("OK")
                    }
                },
                containerColor = Color(0xFFF44336)
            ) {
                Text(error, color = Color.White)
            }
        }
    }
}

// ==================== SUPPORTING COMPOSABLES ====================

@Composable
fun LoadingOverlay(
    pvpState: PvpState,
    selectedMode: PvpMode?,
    isDarkMode: Boolean,
    language: String,
    textColor: Color,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF2C2C2C) else Color.White
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF7B1FA2),
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(64.dp)
                )

                Text(
                    text = when {
                        selectedMode == PvpMode.QUICK_MATCH && pvpState.isSearchingMatch ->
                            when (language) {
                                "en" -> "Finding opponent..."
                                "zh" -> "正在寻找对手..."
                                else -> "Đang tìm đối thủ..."
                            }
                        selectedMode == PvpMode.FRIEND_MATCH && pvpState.isLoading ->
                            when (language) {
                                "en" -> "Creating/Joining room..."
                                "zh" -> "正在创建/加入房间..."
                                else -> "Đang tạo/vào phòng..."
                            }
                        else ->
                            when (language) {
                                "en" -> "Loading..."
                                "zh" -> "加载中..."
                                else -> "Đang tải..."
                            }
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textColor
                )

                pvpState.currentRoom?.let { room ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Room: ${room.shortCode}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = when (language) {
                                "en" -> "Players: ${room.players.size}/2"
                                "zh" -> "玩家: ${room.players.size}/2"
                                else -> "Người chơi: ${room.players.size}/2"
                            },
                            fontSize = 12.sp,
                            color = if (room.players.size == 2) Color(0xFF4CAF50) else Color.Gray,
                            fontWeight = if (room.players.size == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                TextButton(onClick = onCancel) {
                    Text(
                        when (language) {
                            "en" -> "Cancel"
                            "zh" -> "取消"
                            else -> "Hủy"
                        },
                        color = Color(0xFFF44336)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyMatchHistoryCard(isDarkMode: Boolean, language: String, subTextColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF2C2C2C) else Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = subTextColor,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (language) {
                    "en" -> "No matches yet"
                    "zh" -> "暂无比赛记录"
                    else -> "Chưa có trận đấu nào"
                },
                color = subTextColor
            )
        }
    }
}

@Composable
fun UserStatsCard(stats: PvpUserStats, isDarkMode: Boolean, language: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF2C2C2C) else Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when (language) {
                            "en" -> "Your Rank"
                            "zh" -> "你的段位"
                            else -> "Xếp hạng"
                        },
                        fontSize = 14.sp,
                        color = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray
                    )
                    Text(
                        text = stats.getRankTitle(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (stats.getRankTitle()) {
                            "Diamond" -> Color(0xFF00BCD4)
                            "Platinum" -> Color(0xFF9C27B0)
                            "Gold" -> Color(0xFFFFB300)
                            "Silver" -> Color(0xFF9E9E9E)
                            else -> Color(0xFFCD7F32)
                        }
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF7B1FA2)
                ) {
                    Text(
                        text = "${stats.rating}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = when (language) {
                        "en" -> "Wins"
                        "zh" -> "胜场"
                        else -> "Thắng"
                    },
                    value = "${stats.wins}",
                    color = Color(0xFF4CAF50)
                )
                StatItem(
                    label = when (language) {
                        "en" -> "Losses"
                        "zh" -> "败场"
                        else -> "Thua"
                    },
                    value = "${stats.losses}",
                    color = Color(0xFFF44336)
                )
                StatItem(
                    label = when (language) {
                        "en" -> "Win Rate"
                        "zh" -> "胜率"
                        else -> "Tỷ lệ"
                    },
                    value = "${stats.getWinRatePercentage()}%",
                    color = Color(0xFF2196F3)
                )
                StatItem(
                    label = when (language) {
                        "en" -> "Streak"
                        "zh" -> "连胜"
                        else -> "Chuỗi"
                    },
                    value = "${stats.currentStreak}",
                    color = Color(0xFFFF9800)
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun PvpModeCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: List<Color>,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        onClick = onClick,
        enabled = !isLoading
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(gradient))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

// ✅ UPDATED: Custom icons and timestamp display
@Composable
fun MatchHistoryItem(
    result: PvpMatchResult,
    userId: String,
    isDarkMode: Boolean,
    language: String
) {
    val userResult = result.players[userId]
    val isWin = result.winnerId == userId && !result.isDraw

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF2C2C2C) else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ✅ NEW: Custom icon from drawable with balanced sizes
            val iconRes = when {
                result.isDraw -> R.drawable.p_icon_draw
                isWin -> R.drawable.p_win_icon
                else -> R.drawable.p_lose_icon
            }

            val iconSize = when {
                isWin -> 40.dp
                result.isDraw -> 40.dp
                else -> 30.dp // Lose icon much smaller for balance
            }

            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(iconSize)
            )

            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    text = if (result.isDraw) {
                        when (language) {
                            "en" -> "Draw"
                            "zh" -> "平局"
                            else -> "Hòa"
                        }
                    } else if (isWin) {
                        when (language) {
                            "en" -> "Victory"
                            "zh" -> "胜利"
                            else -> "Thắng"
                        }
                    } else {
                        when (language) {
                            "en" -> "Defeat"
                            "zh" -> "失败"
                            else -> "Thua"
                        }
                    },
                    fontWeight = FontWeight.Bold,
                    color = if (isWin) Color(0xFF4CAF50)
                    else if (result.isDraw) Color.Gray
                    else Color(0xFFF44336)
                )
                Text(
                    text = "${userResult?.correctAnswers ?: 0}/${result.questionCount} • ${userResult?.score ?: 0} pts",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                // ✅ NEW: Display timestamp
                Text(
                    text = formatTimestamp(result.createdAt, language),
                    fontSize = 11.sp,
                    color = Color.Gray.copy(alpha = 0.7f)
                )
            }

            Text(
                text = if (userResult?.ratingChange ?: 0 >= 0) {
                    "+${userResult?.ratingChange ?: 0}"
                } else {
                    "${userResult?.ratingChange ?: 0}"
                },
                color = if ((userResult?.ratingChange ?: 0) >= 0)
                    Color(0xFF4CAF50)
                else Color(0xFFF44336),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ✅ NEW: Helper function to format timestamp
private fun formatTimestamp(timestamp: Long, language: String): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when (language) {
        "en" -> when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours hr ago"
            days < 7 -> "$days day${if (days > 1) "s" else ""} ago"
            else -> {
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
                sdf.format(Date(timestamp))
            }
        }
        "zh" -> when {
            seconds < 60 -> "刚刚"
            minutes < 60 -> "${minutes}分钟前"
            hours < 24 -> "${hours}小时前"
            days < 7 -> "${days}天前"
            else -> {
                val sdf = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINESE)
                sdf.format(Date(timestamp))
            }
        }
        else -> when { // Vietnamese
            seconds < 60 -> "Vừa xong"
            minutes < 60 -> "$minutes phút trước"
            hours < 24 -> "$hours giờ trước"
            days < 7 -> "$days ngày trước"
            else -> {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("vi"))
                sdf.format(Date(timestamp))
            }
        }
    }
}
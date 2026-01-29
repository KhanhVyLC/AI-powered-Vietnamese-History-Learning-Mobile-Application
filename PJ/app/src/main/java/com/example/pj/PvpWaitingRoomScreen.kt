// File: PvpWaitingRoomScreen.kt - ✅ FIXED NAVIGATION
package com.example.pj

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val TAG = "PvpWaitingRoom"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PvpWaitingRoomScreen(
    settingsViewModel: SettingsViewModel,
    firebaseViewModel: FirebaseViewModel,
    pvpViewModel: PvpViewModel,
    onNavigateToBattle: () -> Unit,
    onBack: () -> Unit
) {
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val language by settingsViewModel.language.collectAsState()
    val authState by firebaseViewModel.authState.collectAsState()
    val pvpState by pvpViewModel.pvpState.collectAsState()

    val userId = authState.user?.uid ?: ""
    val currentRoom = pvpState.currentRoom
    val roomStatus = currentRoom?.status

    var showLeaveDialog by remember { mutableStateOf(false) }

    // ✅ CRITICAL FIX: Navigate when IN_PROGRESS
    LaunchedEffect(roomStatus) {
        Log.d(TAG, "Room status changed: $roomStatus")
        if (roomStatus == RoomStatus.IN_PROGRESS) {
            Log.d(TAG, "✅ Navigating to battle...")
            onNavigateToBattle()
        }
    }

    // ✅ FIX: Log room state for debugging
    LaunchedEffect(currentRoom) {
        if (currentRoom != null) {
            Log.d(TAG, "Room: ${currentRoom.roomId}")
            Log.d(TAG, "Status: ${currentRoom.status}")
            Log.d(TAG, "Players: ${currentRoom.players.size}")
            Log.d(TAG, "Questions: ${currentRoom.questions.size}")
        } else {
            Log.e(TAG, "❌ Current room is NULL!")
        }
    }

    BackHandler { showLeaveDialog = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (language) {
                            "en" -> "Waiting Room"
                            "zh" -> "等待室"
                            else -> "Phòng chờ"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { showLeaveDialog = true }) {
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
            when (roomStatus) {
                RoomStatus.WAITING -> {
                    WaitingForOpponentContent(
                        currentRoom = currentRoom,
                        userId = userId,
                        isDarkMode = isDarkMode,
                        language = language
                    )
                }
                RoomStatus.STARTING -> {
                    CountdownContent(
                        countdown = pvpState.countdown,
                        isDarkMode = isDarkMode,
                        language = language
                    )
                }
                RoomStatus.IN_PROGRESS -> {
                    // ✅ Show loading while navigating
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = when (language) {
                                    "en" -> "Starting battle..."
                                    "zh" -> "开始战斗..."
                                    else -> "Bắt đầu trận đấu..."
                                },
                                color = if (isDarkMode) Color.White else Color.Black
                            )
                        }
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        if (showLeaveDialog) {
            AlertDialog(
                onDismissRequest = { showLeaveDialog = false },
                title = {
                    Text(
                        when (language) {
                            "en" -> "Leave Match?"
                            "zh" -> "离开比赛？"
                            else -> "Rời trận?"
                        }
                    )
                },
                text = {
                    Text(
                        when (language) {
                            "en" -> "This will count as a loss."
                            "zh" -> "这将被视为失败。"
                            else -> "Điều này sẽ tính là thua."
                        }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            currentRoom?.let {
                                pvpViewModel.leaveRoom(it.roomId, userId)
                            }
                            showLeaveDialog = false
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336)
                        )
                    ) {
                        Text(
                            when (language) {
                                "en" -> "Leave"
                                "zh" -> "离开"
                                else -> "Rời"
                            }
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLeaveDialog = false }) {
                        Text(
                            when (language) {
                                "en" -> "Cancel"
                                "zh" -> "取消"
                                else -> "Hủy"
                            }
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun WaitingForOpponentContent(
    currentRoom: PvpRoom?,
    userId: String,
    isDarkMode: Boolean,
    language: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Room Code Card
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
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = when (language) {
                        "en" -> "Room Code"
                        "zh" -> "房间代码"
                        else -> "Mã phòng"
                    },
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF7B1FA2)
                ) {
                    Text(
                        text = currentRoom?.shortCode ?: "------",
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 4.sp
                    )
                }

                Text(
                    text = when (language) {
                        "en" -> "Share this code with your friend"
                        "zh" -> "与好友分享此代码"
                        else -> "Chia sẻ mã này với bạn"
                    },
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Match Settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF2C2C2C) else Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = when (language) {
                        "en" -> "Match Settings"
                        "zh" -> "比赛设置"
                        else -> "Cài đặt"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                SettingRow(
                    label = when (language) {
                        "en" -> "Difficulty"
                        "zh" -> "难度"
                        else -> "Độ khó"
                    },
                    value = currentRoom?.difficulty ?: "-"
                )
                SettingRow(
                    label = when (language) {
                        "en" -> "Questions"
                        "zh" -> "题目数"
                        else -> "Số câu"
                    },
                    value = "${currentRoom?.questionCount ?: 0}"
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Players
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            currentRoom?.getPlayer(userId)?.let { player ->
                PlayerCard(player, true, isDarkMode, language)
            }

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.CenterVertically),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "VS",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7B1FA2)
                )
            }

            val opponent = currentRoom?.getOpponent(userId)
            if (opponent != null) {
                PlayerCard(opponent, false, isDarkMode, language)
            } else {
                WaitingPlayerCard(isDarkMode, language)
            }
        }

        if (currentRoom?.getOpponent(userId) == null) {
            WaitingAnimation(language)
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun SettingRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
fun PlayerCard(
    player: PvpPlayer,
    isUser: Boolean,
    isDarkMode: Boolean,
    language: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = if (isUser) {
                            listOf(Color(0xFF7B1FA2), Color(0xFF9C27B0))
                        } else {
                            listOf(Color(0xFFFF6B6B), Color(0xFFEE5A6F))
                        }
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = player.username.take(2).uppercase(),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = player.displayName,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 1
        )

        if (player.isReady) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF4CAF50)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = when (language) {
                            "en" -> "Ready"
                            "zh" -> "准备"
                            else -> "Sẵn sàng"
                        },
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun WaitingPlayerCard(isDarkMode: Boolean, language: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(40.dp)
            )
        }

        Text(
            text = when (language) {
                "en" -> "Waiting..."
                "zh" -> "等待中..."
                else -> "Đang chờ..."
            },
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

@Composable
fun WaitingAnimation(language: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "waiting")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(24.dp)
                .scale(scale),
            strokeWidth = 3.dp,
            color = Color(0xFF7B1FA2)
        )
        Text(
            text = when (language) {
                "en" -> "Waiting for opponent..."
                "zh" -> "等待对手..."
                else -> "Đang đợi đối thủ..."
            },
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

@Composable
fun CountdownContent(
    countdown: Int,
    isDarkMode: Boolean,
    language: String
) {
    val scale by animateFloatAsState(
        targetValue = if (countdown > 0) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "countdown_scale"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = when (language) {
                    "en" -> "Get Ready!"
                    "zh" -> "准备！"
                    else -> "Chuẩn bị!"
                },
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7B1FA2)
            )

            if (countdown > 0) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(scale),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF7B1FA2),
                        shadowElevation = 16.dp
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$countdown",
                                fontSize = 96.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
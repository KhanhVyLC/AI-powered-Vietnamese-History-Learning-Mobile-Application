// File: PvpQuickMatchWaitingScreen.kt - ✅ COMPLETE FIX FOR NAVIGATION
package com.example.pj

import android.util.Log
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val TAG = "PvpQuickMatchWaiting"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PvpQuickMatchWaitingScreen(
    settingsViewModel: SettingsViewModel,
    pvpViewModel: PvpViewModel,
    onNavigateToBattle: () -> Unit,
    onCancel: () -> Unit
) {
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val language by settingsViewModel.language.collectAsState()
    val pvpState by pvpViewModel.pvpState.collectAsState()

    val currentRoom = pvpState.currentRoom
    val playersCount = currentRoom?.players?.size ?: 0
    val roomStatus = currentRoom?.status

    // ✅ CRITICAL: Use remember with key to properly track navigation
    var hasNavigated by remember(currentRoom?.roomId) { mutableStateOf(false) }

    val textColor = if (isDarkMode) Color.White else Color.Black

    // ✅ Debug logs for room state
    LaunchedEffect(currentRoom) {
        if (currentRoom != null) {
            Log.d(TAG, "=== ROOM STATE ===")
            Log.d(TAG, "Room ID: ${currentRoom.roomId}")
            Log.d(TAG, "Status: ${currentRoom.status}")
            Log.d(TAG, "Players: ${currentRoom.players.size}/2")
            Log.d(TAG, "Questions: ${currentRoom.questions.size}")

            currentRoom.players.forEach { (id, player) ->
                Log.d(TAG, "Player: ${player.displayName} ($id)")
            }
        } else {
            Log.e(TAG, "❌ Room is NULL")
        }
    }

    // ✅ CRITICAL FIX: Simplified navigation logic - only watch roomStatus
    LaunchedEffect(roomStatus) {
        if (currentRoom == null) {
            Log.e(TAG, "❌ No room available")
            return@LaunchedEffect
        }

        Log.d(TAG, "=== STATUS CHECK ===")
        Log.d(TAG, "Status: $roomStatus")
        Log.d(TAG, "Players: $playersCount/2")
        Log.d(TAG, "Has Navigated: $hasNavigated")
        Log.d(TAG, "Questions: ${currentRoom.questions.size}")

        when (roomStatus) {
            RoomStatus.WAITING -> {
                if (playersCount >= 2) {
                    Log.d(TAG, "⏰ 2 players ready, waiting for STARTING...")
                } else {
                    Log.d(TAG, "🔍 Searching... ($playersCount/2)")
                }
            }
            RoomStatus.STARTING -> {
                Log.d(TAG, "⏰ Room STARTING, showing countdown...")
            }
            RoomStatus.IN_PROGRESS -> {
                if (!hasNavigated) {
                    Log.d(TAG, "✅ Room IN_PROGRESS, navigating to battle NOW!")
                    hasNavigated = true
                    // Navigate immediately
                    onNavigateToBattle()
                } else {
                    Log.d(TAG, "⚠️ Already navigated, skipping")
                }
            }
            else -> {
                Log.w(TAG, "⚠️ Unexpected status: $roomStatus")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (language) {
                            "en" -> "Quick Match"
                            "zh" -> "快速匹配"
                            else -> "Tìm trận nhanh"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFF3D5178),
                    titleContentColor = Color.White
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
                            colors = listOf(Color(0xFFAABBCC), Color.White)
                        )
                    }
                )
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // ✅ Main Content: Show countdown or search animation
                when (roomStatus) {
                    RoomStatus.STARTING -> {
                        if (pvpState.countdown > 0) {
                            // Show countdown
                            CountdownAnimation(
                                countdown = pvpState.countdown,
                                isDarkMode = isDarkMode,
                                language = language
                            )
                        } else {
                            // Fallback animation while waiting for countdown
                            SearchingAnimation(isDarkMode = isDarkMode)
                            Text(
                                text = when (language) {
                                    "en" -> "Starting soon..."
                                    "zh" -> "即将开始..."
                                    else -> "Sắp bắt đầu..."
                                },
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    RoomStatus.IN_PROGRESS -> {
                        // Transitioning to battle
                        CircularProgressIndicator(
                            modifier = Modifier.size(80.dp),
                            color = Color(0xFF3D5178),
                            strokeWidth = 6.dp
                        )
                        Text(
                            text = when (language) {
                                "en" -> "Loading battle..."
                                "zh" -> "加载战斗..."
                                else -> "Đang tải trận đấu..."
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = textColor
                        )
                    }
                    else -> {
                        // Searching for opponent
                        SearchingAnimation(isDarkMode = isDarkMode)

                        StatusText(
                            playersCount = playersCount,
                            language = language,
                            textColor = textColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ✅ Room Information Card
                if (currentRoom != null) {
                    RoomInfoCard(
                        room = currentRoom,
                        playersCount = playersCount,
                        isDarkMode = isDarkMode,
                        language = language
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // ✅ Cancel Button (hide when starting or in progress)
                if (roomStatus != RoomStatus.STARTING &&
                    roomStatus != RoomStatus.IN_PROGRESS) {
                    CancelButton(
                        language = language,
                        onClick = onCancel
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SearchingAnimation(isDarkMode: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "search")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(140.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Background circle
        Surface(
            shape = CircleShape,
            color = Color(0xFF3D5178).copy(alpha = 0.15f),
            modifier = Modifier.fillMaxSize()
        ) {}

        // Progress ring
        CircularProgressIndicator(
            modifier = Modifier.size(110.dp),
            color = Color(0xFF3D5178),
            strokeWidth = 6.dp
        )

        // Icon
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = Color(0xFF3D5178),
            modifier = Modifier.size(56.dp)
        )
    }
}

@Composable
fun StatusText(
    playersCount: Int,
    language: String,
    textColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = when {
                playersCount == 2 -> when (language) {
                    "en" -> "Match Found!"
                    "zh" -> "匹配成功！"
                    else -> "Đã tìm thấy đối thủ!"
                }
                playersCount == 1 -> when (language) {
                    "en" -> "Searching for opponent..."
                    "zh" -> "正在寻找对手..."
                    else -> "Đang tìm đối thủ..."
                }
                else -> when (language) {
                    "en" -> "Connecting..."
                    "zh" -> "连接中..."
                    else -> "Đang kết nối..."
                }
            },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )

        Text(
            text = when (language) {
                "en" -> "Please wait..."
                "zh" -> "请稍候..."
                else -> "Vui lòng đợi..."
            },
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun RoomInfoCard(
    room: PvpRoom,
    playersCount: Int,
    isDarkMode: Boolean,
    language: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF2C2C2C) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = when (language) {
                    "en" -> "Match Details"
                    "zh" -> "比赛详情"
                    else -> "Chi tiết trận đấu"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else Color.Black
            )

            Divider()

            // Players count
            InfoRow(
                label = when (language) {
                    "en" -> "Players"
                    "zh" -> "玩家"
                    else -> "Người chơi"
                },
                value = "$playersCount/2",
                valueColor = if (playersCount == 2) Color(0xFF4CAF50) else Color(0xFFFF9800),
                icon = if (playersCount == 2) Icons.Default.CheckCircle else Icons.Default.Person,
                iconTint = if (playersCount == 2) Color(0xFF4CAF50) else Color(0xFFFF9800)
            )

            // Difficulty
            InfoRow(
                label = when (language) {
                    "en" -> "Difficulty"
                    "zh" -> "难度"
                    else -> "Độ khó"
                },
                value = room.difficulty,
                valueColor = Color(0xFF3D5178),
                icon = Icons.Default.Star,
                iconTint = Color(0xFFFFB300)
            )

            // Questions count
            InfoRow(
                label = when (language) {
                    "en" -> "Questions"
                    "zh" -> "题目数量"
                    else -> "Số câu hỏi"
                },
                value = "${room.questionCount}",
                valueColor = Color(0xFF2196F3),
                icon = Icons.Default.Info,
                iconTint = Color(0xFF2196F3)
            )

            // Status
            InfoRow(
                label = when (language) {
                    "en" -> "Status"
                    "zh" -> "状态"
                    else -> "Trạng thái"
                },
                value = when (room.status) {
                    RoomStatus.WAITING -> when (language) {
                        "en" -> "Waiting"
                        "zh" -> "等待中"
                        else -> "Đang chờ"
                    }
                    RoomStatus.STARTING -> when (language) {
                        "en" -> "Starting"
                        "zh" -> "准备中"
                        else -> "Chuẩn bị"
                    }
                    RoomStatus.IN_PROGRESS -> when (language) {
                        "en" -> "In Progress"
                        "zh" -> "进行中"
                        else -> "Đang diễn ra"
                    }
                    else -> room.status.name
                },
                valueColor = when (room.status) {
                    RoomStatus.WAITING -> Color(0xFFFF9800)
                    RoomStatus.STARTING -> Color(0xFF2196F3)
                    RoomStatus.IN_PROGRESS -> Color(0xFF4CAF50)
                    else -> Color.Gray
                },
                icon = when (room.status) {
                    RoomStatus.WAITING -> Icons.Default.Face
                    RoomStatus.STARTING -> Icons.Default.DateRange
                    RoomStatus.IN_PROGRESS -> Icons.Default.PlayArrow
                    else -> Icons.Default.Info
                },
                iconTint = when (room.status) {
                    RoomStatus.WAITING -> Color(0xFFFF9800)
                    RoomStatus.STARTING -> Color(0xFF2196F3)
                    RoomStatus.IN_PROGRESS -> Color(0xFF4CAF50)
                    else -> Color.Gray
                }
            )
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    valueColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                fontSize = 15.sp,
                color = Color.Gray
            )
        }

        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
fun CountdownAnimation(
    countdown: Int,
    isDarkMode: Boolean,
    language: String
) {
    val scale by animateFloatAsState(
        targetValue = if (countdown > 0) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "countdown_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Title
        Text(
            text = when (language) {
                "en" -> "GET READY!"
                "zh" -> "准备好！"
                else -> "CHUẨN BỊ!"
            },
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF3D5178),
            textAlign = TextAlign.Center
        )

        // Countdown number
        Box(
            modifier = Modifier
                .size(220.dp)
                .scale(scale),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF3D5178),
                shadowElevation = 20.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$countdown",
                        fontSize = 120.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        // Subtitle
        Text(
            text = when (language) {
                "en" -> "Battle starts soon..."
                "zh" -> "战斗即将开始..."
                else -> "Trận đấu sắp bắt đầu..."
            },
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDarkMode) Color.White else Color.Black,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CancelButton(
    language: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color(0xFFF44336)
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 2.dp
        )
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = when (language) {
                "en" -> "Cancel Match"
                "zh" -> "取消匹配"
                else -> "Hủy tìm trận"
            },
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
// File: PvpResultScreen.kt - ✅ UPDATED: UI improvements per user request
package com.example.pj

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PvpResultScreen(
    settingsViewModel: SettingsViewModel,
    firebaseViewModel: FirebaseViewModel,
    pvpViewModel: PvpViewModel,
    onPlayAgain: () -> Unit,
    onBackToLobby: () -> Unit
) {
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val language by settingsViewModel.language.collectAsState()
    val authState by firebaseViewModel.authState.collectAsState()
    val pvpState by pvpViewModel.pvpState.collectAsState()

    val userId = authState.user?.uid ?: ""
    val currentRoom = pvpState.currentRoom

    val userPlayer = currentRoom?.getPlayer(userId)
    val opponentPlayer = currentRoom?.getOpponent(userId)

    val userScore = userPlayer?.score ?: 0
    val opponentScore = opponentPlayer?.score ?: 0

    val isWin = userScore > opponentScore
    val isDraw = userScore == opponentScore

    val textColor = if (isDarkMode) Color.White else Color.Black

    // Animation for crown
    val infiniteTransition = rememberInfiniteTransition(label = "crown")
    val crownScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "crownScale"
    )

    // ✅ Calculate total time spent
    val totalTimeSpent = userPlayer?.answers?.sumOf { it.timeSpent } ?: 0L
    val totalTimeSeconds = (totalTimeSpent / 1000).toInt()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (language) {
                            "en" -> "Match Result"
                            "zh" -> "比赛结果"
                            else -> "Kết quả trận đấu"
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ✅ Result Title Text
            Text(
                text = when {
                    isDraw -> when (language) {
                        "en" -> "It's a Draw!"
                        "zh" -> "平局！"
                        else -> "Hòa!"
                    }
                    isWin -> when (language) {
                        "en" -> "Victory!"
                        "zh" -> "胜利！"
                        else -> "Chiến thắng!"
                    }
                    else -> when (language) {
                        "en" -> "Defeat"
                        "zh" -> "失败"
                        else -> "Thua cuộc"
                    }
                },
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    isDraw -> Color.Gray
                    isWin -> Color(0xFF4CAF50)
                    else -> Color(0xFFF44336)
                }
            )

            // ✅ Players Comparison - Side by Side with Crown
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color(0xFF2C2C2C) else Color.White
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // User Player
                    PlayerColumn(
                        player = userPlayer,
                        score = userScore,
                        isWinner = isWin,
                        isDraw = isDraw,
                        isDarkMode = isDarkMode,
                        language = language,
                        crownScale = crownScale,
                        modifier = Modifier.weight(1f)
                    )

                    // ✅ VS Text only (no divider)
                    Text(
                        text = "VS",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7B1FA2),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Opponent Player
                    PlayerColumn(
                        player = opponentPlayer,
                        score = opponentScore,
                        isWinner = !isWin && !isDraw,
                        isDraw = isDraw,
                        isDarkMode = isDarkMode,
                        language = language,
                        crownScale = crownScale,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ✅ Detailed Stats Card - Updated with Total Time
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = when (language) {
                            "en" -> "Your Performance"
                            "zh" -> "你的表现"
                            else -> "Thành tích của bạn"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Divider()

                    // ✅ Stats Grid - Updated labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatBox(
                            label = when (language) {
                                "en" -> "Total Time"
                                "zh" -> "总时间"
                                else -> "Tổng thời gian"
                            },
                            value = "$totalTimeSeconds",
                            total = "s",
                            color = Color(0xFF9C27B0),
                            isDarkMode = isDarkMode
                        )

                        StatBox(
                            label = when (language) {
                                "en" -> "Correct"
                                "zh" -> "正确"
                                else -> "Đúng"
                            },
                            value = "${userPlayer?.correctAnswers ?: 0}",
                            total = "/${currentRoom?.questionCount ?: 0}",
                            color = Color(0xFF4CAF50),
                            isDarkMode = isDarkMode
                        )

                        StatBox(
                            label = when (language) {
                                "en" -> "Accuracy"
                                "zh" -> "准确率"
                                else -> "Độ chính xác"
                            },
                            value = "${((userPlayer?.correctAnswers ?: 0).toFloat() / (currentRoom?.questionCount ?: 1) * 100).toInt()}",
                            total = "%",
                            color = Color(0xFF2196F3),
                            isDarkMode = isDarkMode
                        )
                    }

                    Divider()

                    // ✅ Rating Change - Removed icon
                    val ratingChange = if (isWin) +10 else if (isDraw) 0 else -5
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (ratingChange >= 0)
                                Color(0xFF4CAF50).copy(alpha = 0.1f)
                            else
                                Color(0xFFF44336).copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (language) {
                                    "en" -> "Rating Change"
                                    "zh" -> "段位变化"
                                    else -> "Thay đổi xếp hạng"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = textColor
                            )
                            Text(
                                text = if (ratingChange >= 0) "+$ratingChange" else "$ratingChange",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (ratingChange >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                        }
                    }
                }
            }

            // ✅ Action Buttons - Updated labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        pvpViewModel.resetPvpState()
                        onPlayAgain()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 2.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        when (language) {
                            "en" -> "Home"
                            "zh" -> "主页"
                            else -> "Trang chủ"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        pvpViewModel.resetPvpState()
                        onBackToLobby()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7B1FA2)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        when (language) {
                            "en" -> "Lobby"
                            "zh" -> "大厅"
                            else -> "Sảnh"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PlayerColumn(
    player: PvpPlayer?,
    score: Int,
    isWinner: Boolean,
    isDraw: Boolean,
    isDarkMode: Boolean,
    language: String,
    crownScale: Float,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        // ✅ Crown Image (only for winner, not for draw)
        if (isWinner && !isDraw) {
            Image(
                painter = painterResource(id = R.drawable.p_vm),
                contentDescription = "Crown",
                modifier = Modifier
                    .size(64.dp)
                    .scale(crownScale),
                contentScale = ContentScale.Fit
            )
        } else {
            Spacer(modifier = Modifier.height(64.dp))
        }

        // Avatar Circle
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = if (isWinner && !isDraw) {
                            listOf(Color(0xFFFFD700), Color(0xFFFFB300))
                        } else if (isDraw) {
                            listOf(Color(0xFF9E9E9E), Color(0xFF757575))
                        } else {
                            listOf(Color(0xFF616161), Color(0xFF424242))
                        }
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = player?.username?.take(2)?.uppercase() ?: "??",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // ✅ Player Name - Fixed width to keep equal size
        Text(
            text = player?.displayName ?: "Unknown",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 120.dp),
            color = if (isDarkMode) Color.White else Color.Black
        )

        // ✅ Score Badge only (no win/lose badge below)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = when {
                isWinner && !isDraw -> Color(0xFF4CAF50)
                isDraw -> Color(0xFF9E9E9E)
                else -> Color(0xFFF44336)
            }
        ) {
            Text(
                text = "$score pts",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun StatBox(
    label: String,
    value: String,
    total: String,
    color: Color,
    isDarkMode: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = total,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = color.copy(alpha = 0.7f)
                )
            }
        }
    }
}
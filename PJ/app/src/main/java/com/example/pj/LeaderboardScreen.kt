// File: LeaderboardScreen.kt - ✅ FIXED: Correctly fetch displayName from users node
package com.example.pj

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

private const val TAG = "LeaderboardScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    settingsViewModel: SettingsViewModel,
    firebaseViewModel: FirebaseViewModel,
    onBack: () -> Unit
) {
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val language by settingsViewModel.language.collectAsState()
    val authState by firebaseViewModel.authState.collectAsState()

    var pvpLeaderboard by remember { mutableStateOf<List<PvpLeaderboardEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val currentUserId = authState.user?.uid ?: ""
    val textColor = if (isDarkMode) Color.White else Color.Black

    //  FIXED: Load PvP Leaderboard với displayName từ players node trong pvp_results
    LaunchedEffect(Unit) {
        try {
            Log.d(TAG, " === LOADING PVP LEADERBOARD START ===")
            isLoading = true

            val realtimeDb = FirebaseDatabase.getInstance(
                "https://vihis-45cc5-default-rtdb.asia-southeast1.firebasedatabase.app"
            )
            val statsRef = realtimeDb.getReference("pvp_stats")
            val resultsRef = realtimeDb.getReference("pvp_results")

            Log.d(TAG, " Step 1: Fetching pvp_stats...")
            val statsSnapshot = statsRef.get().await()
            Log.d(TAG, " Stats count: ${statsSnapshot.childrenCount}")

            val entries = mutableListOf<PvpLeaderboardEntry>()

            for (statChild in statsSnapshot.children) {
                try {
                    val stats = statChild.getValue(PvpUserStats::class.java)
                    if (stats == null || stats.totalMatches == 0) {
                        Log.d(TAG, " Skipping user ${statChild.key}: no matches")
                        continue
                    }

                    val userId = stats.userId
                    Log.d(TAG, " Processing user: $userId")

                    // ✅ FIXED: Tìm displayName từ pvp_results
                    Log.d(TAG, " Step 2: Searching for displayName in pvp_results...")
                    val resultsSnapshot = resultsRef.get().await()

                    var displayName: String? = null

                    // Duyệt qua tất cả các kết quả trận đấu
                    for (resultChild in resultsSnapshot.children) {
                        try {
                            val result = resultChild.getValue(PvpMatchResult::class.java)
                            if (result != null && result.players.containsKey(userId)) {
                                val playerResult = result.players[userId]
                                if (playerResult != null && playerResult.username.isNotEmpty()) {
                                    displayName = playerResult.username
                                    Log.d(TAG, " Found displayName: $displayName from result ${resultChild.key}")
                                    break
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, " Error parsing result ${resultChild.key}: ${e.message}")
                        }
                    }

                    // Nếu không tìm thấy, dùng fallback
                    val finalName = displayName ?: "Player${userId.take(6)}"

                    Log.d(TAG, " Final name for $userId: $finalName")
                    Log.d(TAG, "   Rating: ${stats.rating}, Matches: ${stats.totalMatches}")

                    entries.add(
                        PvpLeaderboardEntry(
                            userId = userId,
                            displayName = finalName,
                            rating = stats.rating,
                            totalMatches = stats.totalMatches,
                            wins = stats.wins,
                            losses = stats.losses,
                            winRate = stats.winRate,
                            rank = stats.getRankTitle()
                        )
                    )

                    Log.d(TAG, " Added entry: $finalName (${stats.rating})")

                } catch (e: Exception) {
                    Log.e(TAG, " Error processing user ${statChild.key}: ${e.message}", e)
                }
            }

            pvpLeaderboard = entries.sortedByDescending { it.rating }
            isLoading = false

            Log.d(TAG, " === LEADERBOARD LOADED ===")
            Log.d(TAG, "   Total players: ${pvpLeaderboard.size}")
            pvpLeaderboard.take(5).forEach { entry ->
                Log.d(TAG, "   - ${entry.displayName}: ${entry.rating}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ === LEADERBOARD LOADING FAILED ===", e)
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF7B1FA2),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = when (language) {
                                "en" -> "Leaderboard"
                                "zh" -> "排行榜"
                                else -> "Bảng xếp hạng"
                            },
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(40.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TabChip(
                            icon = "",
                            label = when (language) {
                                "en" -> "PvP Ranking"
                                "zh" -> "对战排名"
                                else -> "Đấu hạng PvP"
                            },
                            isSelected = selectedTab == 0,
                            onClick = { selectedTab = 0 }
                        )
                        TabChip(
                            icon = "",
                            label = when (language) {
                                "en" -> "Quiz Score"
                                "zh" -> "测验分数"
                                else -> "Điểm Quiz"
                            },
                            isSelected = selectedTab == 1,
                            onClick = { selectedTab = 1 }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (selectedTab == 0) {
            PvpLeaderboardContent(
                pvpLeaderboard = pvpLeaderboard,
                currentUserId = currentUserId,
                isLoading = isLoading,
                isDarkMode = isDarkMode,
                language = language,
                textColor = textColor,
                paddingValues = paddingValues
            )
        } else {
            QuizLeaderboardContent(
                firebaseViewModel = firebaseViewModel,
                isDarkMode = isDarkMode,
                language = language,
                textColor = textColor,
                paddingValues = paddingValues
            )
        }
    }
}

// ==================== PVP LEADERBOARD ====================
@Composable
fun PvpLeaderboardContent(
    pvpLeaderboard: List<PvpLeaderboardEntry>,
    currentUserId: String,
    isLoading: Boolean,
    isDarkMode: Boolean,
    language: String,
    textColor: Color,
    paddingValues: PaddingValues
) {
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = Color(0xFF7B1FA2))
                Text(
                    text = when (language) {
                        "en" -> "Loading rankings..."
                        "zh" -> "加载排名中..."
                        else -> "Đang tải bảng xếp hạng..."
                    },
                    color = textColor
                )
            }
        }
        return
    }

    val backgroundBrush = if (isDarkMode) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF1E1E1E), Color(0xFF121212))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFF3E5F5), Color(0xFFE1BEE7))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(pvpLeaderboard) { index, entry ->
                PvpLeaderboardCard(
                    rank = index + 1,
                    entry = entry,
                    isCurrentUser = entry.userId == currentUserId,
                    isDarkMode = isDarkMode,
                    textColor = textColor,
                    language = language
                )
            }

            if (pvpLeaderboard.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = when (language) {
                                "en" -> "No PvP rankings yet"
                                "zh" -> "暂无对战排名"
                                else -> "Chưa có xếp hạng PvP"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = when (language) {
                                "en" -> "Play PvP matches to appear here!"
                                "zh" -> "完成对战即可上榜！"
                                else -> "Chơi PvP để lên bảng xếp hạng!"
                            },
                            fontSize = 14.sp,
                            color = textColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

// ==================== PVP CARD ====================
@Composable
fun PvpLeaderboardCard(
    rank: Int,
    entry: PvpLeaderboardEntry,
    isCurrentUser: Boolean,
    isDarkMode: Boolean,
    textColor: Color,
    language: String
) {
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> if (isDarkMode) Color(0xFF424242) else Color(0xFFE0E0E0)
    }

    val borderColor = when (rank) {
        1 -> Brush.linearGradient(colors = listOf(Color(0xFFFFD700), Color(0xFFFFA000)))
        2 -> Brush.linearGradient(colors = listOf(Color(0xFFC0C0C0), Color(0xFF9E9E9E)))
        3 -> Brush.linearGradient(colors = listOf(Color(0xFFCD7F32), Color(0xFF8D6E63)))
        else -> null
    }

    val cardBackgroundColor = when {
        rank == 1 -> if (isDarkMode) Color(0xFF4A3800) else Color(0xFFFFF9E6)
        rank == 2 -> if (isDarkMode) Color(0xFF3A3A3A) else Color(0xFFF5F5F5)
        rank == 3 -> if (isDarkMode) Color(0xFF3D2B1F) else Color(0xFFFBE9E7)
        isCurrentUser -> if (isDarkMode) Color(0xFF1B3A2D) else Color(0xFFE8F5E9)
        else -> if (isDarkMode) Color(0xFF2D2D2D) else Color.White
    }

    val podiumIcon = when (rank) {
        1 -> R.drawable.p_first
        2 -> R.drawable.p_second
        3 -> R.drawable.p_third
        else -> null
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = cardBackgroundColor,
        shadowElevation = if (rank <= 3) 8.dp else if (isCurrentUser) 6.dp else 2.dp,
        modifier = Modifier.then(
            if (rank <= 3 && borderColor != null) {
                Modifier.border(width = 3.dp, brush = borderColor, shape = RoundedCornerShape(16.dp))
            } else if (isCurrentUser) {
                Modifier.border(width = 2.dp, color = Color(0xFF4CAF50), shape = RoundedCornerShape(16.dp))
            } else {
                Modifier
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank icon
            if (podiumIcon != null) {
                Image(
                    painter = painterResource(id = podiumIcon),
                    contentDescription = "Rank $rank",
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(rankColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rank.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            // User info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.displayName,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontSize = if (rank <= 3) 17.sp else 15.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (isCurrentUser) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF4CAF50)
                        ) {
                            Text(
                                text = when (language) {
                                    "en" -> "You"
                                    "zh" -> "你"
                                    else -> "Bạn"
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "⭐", fontSize = 13.sp)
                        Text(
                            text = "${entry.rating}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = getRankColor(entry.rank)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = getRankColor(entry.rank).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = entry.rank,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            color = getRankColor(entry.rank),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    Text(
                        text = "${entry.wins}Win-${entry.losses}Lose",
                        fontSize = 11.sp,
                        color = textColor.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.TabChip(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.3f),
        onClick = onClick,
        modifier = Modifier.weight(1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = if (isSelected) Color(0xFF7B1FA2) else Color.White,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
        }
    }
}

// ==================== QUIZ LEADERBOARD ====================
@Composable
fun QuizLeaderboardContent(
    firebaseViewModel: FirebaseViewModel,
    isDarkMode: Boolean,
    language: String,
    textColor: Color,
    paddingValues: PaddingValues
) {
    val leaderboard by firebaseViewModel.leaderboard.collectAsState()
    var selectedPeriod by remember { mutableStateOf("all") }

    LaunchedEffect(selectedPeriod) {
        firebaseViewModel.loadLeaderboard(selectedPeriod)
    }

    val backgroundBrush = if (isDarkMode) {
        Brush.verticalGradient(colors = listOf(Color(0xFF1E1E1E), Color(0xFF121212)))
    } else {
        Brush.verticalGradient(colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFE0B2)))
    }

    Box(modifier = Modifier.fillMaxSize().background(backgroundBrush)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PeriodChip(
                        label = when (language) {
                            "en" -> "All Time"
                            "zh" -> "总榜"
                            else -> "Tổng"
                        },
                        isSelected = selectedPeriod == "all",
                        onClick = { selectedPeriod = "all" },
                        color = Color(0xFFFF9800)
                    )
                    PeriodChip(
                        label = when (language) {
                            "en" -> "Weekly"
                            "zh" -> "周榜"
                            else -> "Tuần"
                        },
                        isSelected = selectedPeriod == "weekly",
                        onClick = { selectedPeriod = "weekly" },
                        color = Color(0xFFFFB300)
                    )
                    PeriodChip(
                        label = when (language) {
                            "en" -> "Monthly"
                            "zh" -> "月榜"
                            else -> "Tháng"
                        },
                        isSelected = selectedPeriod == "monthly",
                        onClick = { selectedPeriod = "monthly" },
                        color = Color(0xFFFFA726)
                    )
                }
            }

            items(leaderboard) { entry ->
                QuizLeaderboardCard(
                    entry = entry,
                    period = selectedPeriod,
                    isDarkMode = isDarkMode,
                    textColor = textColor
                )
            }

            if (leaderboard.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "📚", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = when (language) {
                                "en" -> "No quiz rankings yet"
                                "zh" -> "暂无测验排名"
                                else -> "Chưa có xếp hạng quiz"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.PeriodChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) color else color.copy(alpha = 0.3f),
        onClick = onClick,
        modifier = Modifier.weight(1f)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (isSelected) Color.White else color,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun QuizLeaderboardCard(
    entry: LeaderboardEntry,
    period: String,
    isDarkMode: Boolean,
    textColor: Color
) {
    val rankColor = when (entry.rank) {
        1 -> Color(0xFFFF9800)
        2 -> Color(0xFFFFB74D)
        3 -> Color(0xFFFFA726)
        else -> Color(0xFFFFB300)
    }

    val podiumIcon = when (entry.rank) {
        1 -> R.drawable.p_first
        2 -> R.drawable.p_second
        3 -> R.drawable.p_third
        else -> null
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isDarkMode) Color(0xFF2D2D2D) else Color.White,
        shadowElevation = if (entry.rank <= 3) 4.dp else 2.dp,
        border = if (entry.rank <= 3) {
            androidx.compose.foundation.BorderStroke(2.dp, rankColor)
        } else null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(80.dp).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (podiumIcon != null) {
                Image(
                    painter = painterResource(id = podiumIcon),
                    contentDescription = "Rank ${entry.rank}",
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(rankColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = entry.rank.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = entry.displayName,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = if (entry.rank <= 3) 18.sp else 16.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${getScoreForPeriod(entry, period)} điểm",
                    fontSize = 13.sp,
                    color = textColor.copy(alpha = 0.7f)
                )
            }

            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFFFB300),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ==================== DATA MODELS ====================
data class PvpLeaderboardEntry(
    val userId: String,
    val displayName: String,
    val rating: Int,
    val totalMatches: Int,
    val wins: Int,
    val losses: Int,
    val winRate: Double,
    val rank: String
)

// ==================== HELPER FUNCTIONS ====================
private fun getRankColor(rank: String): Color {
    return when (rank) {
        "Diamond" -> Color(0xFF00E5FF)
        "Platinum" -> Color(0xFFE040FB)
        "Gold" -> Color(0xFFFFD700)
        "Silver" -> Color(0xFFC0C0C0)
        "Bronze" -> Color(0xFFCD7F32)
        else -> Color(0xFF9E9E9E)
    }
}

private fun getScoreForPeriod(entry: LeaderboardEntry, period: String): Int {
    return when (period) {
        "weekly" -> entry.weeklyScore
        "monthly" -> entry.monthlyScore
        else -> entry.totalScore
    }
}
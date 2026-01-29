// File: HomeScreen.kt - ✅ WITH PVP BUTTON
package com.example.pj

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.clickable



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    settingsViewModel: SettingsViewModel,
    firebaseViewModel: FirebaseViewModel,
    onNavigate: (String) -> Unit,
    onTopicClick: (Topic) -> Unit,
    onDailyQuizClick: () -> Unit
) {
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val language by settingsViewModel.language.collectAsState()

    // ✅ LẤY DỮ LIỆU TỪ FIREBASE
    val authState by firebaseViewModel.authState.collectAsState()
    val userProfile = authState.userProfile

    // ✅ FALLBACK VỀ SETTINGSVIEWMODEL NẾU CHƯA CÓ FIREBASE DATA
    val userName = userProfile?.username ?: settingsViewModel.userName.collectAsState().value
    val userScore = userProfile?.totalScore ?: settingsViewModel.userScore.collectAsState().value
    val weeklyQuizCount = userProfile?.weeklyQuizCount ?: settingsViewModel.weeklyQuizCount.collectAsState().value
    var avatarUri by remember {
        mutableStateOf(
            userProfile?.avatarUrl?.let { Uri.parse(it) }  // nếu có trường avatarUrl
        )
    }

    // ✅ Picker chọn ảnh từ gallery
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            avatarUri = it
            // TODO: upload lên Firebase Storage + lưu link vào userProfile
            // ví dụ:
            // firebaseViewModel.updateAvatar(it)
        }
    }
    var searchQuery by remember { mutableStateOf("") }

    val surfaceColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val subTextColor = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray

    // ✅ TỰ ĐỘNG REFRESH DỮ LIỆU KHI VÀO HOME
    LaunchedEffect(Unit) {
        userProfile?.let {
            firebaseViewModel.refreshAllData()
        }
    }

    // Topics
    val allTopics = listOf(
        Topic(1, "Thời Cổ Đại", "Ancient Era", "古代", "", 15,
            listOf(Color(0xFFE1BEE7), Color(0xFFCE93D8)), R.drawable.ic_ancient),
        Topic(2, "Thời Phong Kiến", "Feudal Era", "封建时代", "", 24,
            listOf(Color(0xFFFFE0B2), Color(0xFFFFCC80)), R.drawable.ic_feudal),
        Topic(3, "Thời Cận Đại", "Modern Era", "近代", "", 18,
            listOf(Color(0xFFC8E6C9), Color(0xFFA5D6A7)), R.drawable.ic_contemporary),
        Topic(4, "Thời Hiện Đại", "Contemporary", "现代", "", 21,
            listOf(Color(0xFFE1BEE7), Color(0xFFCE93D8)), R.drawable.ic_modern),
        Topic(5, "Nhân Vật Lịch Sử", "Historical Figures", "历史人物", "", 30,
            listOf(Color(0xFFB3E5FC), Color(0xFF81D4FA)), R.drawable.ic_person),
        Topic(6, "Di Tích & Văn Hóa", "Heritage & Culture", "遗产与文化", "", 20,
            listOf(Color(0xFFF8BBD0), Color(0xFFF48FB1)), R.drawable.ic_culture)
    )

    val filteredTopics = if (searchQuery.isEmpty()) {
        allTopics
    } else {
        allTopics.filter {
            it.getName(language).contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentScreen = "home",
                language = language,
                isDarkMode = isDarkMode,
                onNavigate = onNavigate
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
                            colors = listOf(Color(0xFFF3E5F5), Color.White, Color(0xFFE3F2FD))
                        )
                    }
                )
                .padding(paddingValues)
        ) {
            // Header
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                    color = surfaceColor,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFF9C27B0),
                                                    Color(0xFF7B1FA2)
                                                )
                                            )
                                        )
                                        .clickable {
                                            imagePickerLauncher.launch("image/*")
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (avatarUri != null) {
                                        Image(
                                            painter = rememberAsyncImagePainter(avatarUri),
                                            contentDescription = "Avatar",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            text = userName.take(2).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                                Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFF7B1FA2)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = userScore.toString(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                IconButton(onClick = { onNavigate("settings") }) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = if (isDarkMode) Color(0xFFCE93D8) else Color(0xFF7B1FA2)
                                    )
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = if (isDarkMode) Color(0xFF4A148C) else Color(0xFF7B1FA2)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                                Column {
                                    Text(
                                        text = Translations.weeklyQuizTitle(language),
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = Translations.weeklyQuizCompleted(language, weeklyQuizCount),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Search
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = {
                        Text(
                            when (language) {
                                "en" -> "Search topics..."
                                "zh" -> "搜索主题..."
                                else -> "Tìm kiếm chủ đề..."
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = if (isDarkMode) Color(0xFFCE93D8) else Color(0xFF7B1FA2)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = if (isDarkMode) Color(0xFF757575) else Color.Gray
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = surfaceColor,
                        unfocusedContainerColor = surfaceColor,
                        focusedBorderColor = if (isDarkMode) Color(0xFFCE93D8) else Color(0xFF7B1FA2),
                        unfocusedBorderColor = if (isDarkMode) Color(0xFF757575) else Color.Gray
                    ),
                    singleLine = true
                )
            }

            // ==================== PVP BATTLE CARD - ✅ MỚI ====================
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFFFF6B6B),
                    onClick = { onNavigate("pvp_lobby") }
                ) {
                    Box(
                        modifier = Modifier.height(140.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Text(
                                        text = when (language) {
                                            "en" -> "PvP Battle"
                                            "zh" -> "对战竞技"
                                            else -> "Đấu PvP"
                                        },
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = when (language) {
                                        "en" -> "Challenge other players!"
                                        "zh" -> "挑战其他玩家！"
                                        else -> "Thách đấu người chơi khác!"
                                    },
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color.White
                                ) {
                                    Text(
                                        text = when (language) {
                                            "en" -> "Play Now"
                                            "zh" -> "立即开始"
                                            else -> "Chơi ngay"
                                        },
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        color = Color(0xFFFF6B6B),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(100.dp)
                            )
                        }
                    }
                }
            }

            // Daily Quiz
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFFFF9800),
                    onClick = onDailyQuizClick
                ) {
                    Box(
                        modifier = Modifier.height(160.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .width(140.dp)
                                .padding(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_daily_quiz_bg),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(0.15f),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = Translations.dailyQuizTitle(language),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = Translations.dailyQuizDesc(language),
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onDailyQuizClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = Translations.joinQuiz(language),
                                    color = Color(0xFFFF9800),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Topics Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Translations.exploreTopics(language),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = subTextColor
                    )
                }
            }

            // Topics Grid
            items(filteredTopics.chunked(2)) { rowTopics ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowTopics.forEach { topic ->
                        TopicCard(
                            modifier = Modifier.weight(1f),
                            topic = topic,
                            language = language,
                            onClick = { onTopicClick(topic) }
                        )
                    }
                    if (rowTopics.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun TopicCard(
    modifier: Modifier = Modifier,
    topic: Topic,
    language: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.aspectRatio(0.9f),
        shape = RoundedCornerShape(16.dp),
        color = topic.gradient[0],
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.background(Brush.linearGradient(topic.gradient))
        ) {
            Image(
                painter = painterResource(id = topic.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0.85f),
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
                            ),
                            startY = 200f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = topic.getName(language),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black.copy(alpha = 0.8f),
                            offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    )
                )
                Text(
                    text = "${TopicQuizData.getSetCount(topic.id)} " + when (language) {
                        "en" -> "Quiz Sets"
                        "zh" -> "个测验集"
                        else -> "Bộ Quiz"
                    },
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black.copy(alpha = 0.8f),
                            offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    )
                )
            }
        }
    }
}
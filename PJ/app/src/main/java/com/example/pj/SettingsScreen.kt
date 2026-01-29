//File: SettingsScreen.kt - ✅ UPDATED WITH BEAUTIFUL LANGUAGE DROPDOWN
package com.example.pj

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    firebaseViewModel: FirebaseViewModel,
    onBack: () -> Unit
) {
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val language by settingsViewModel.language.collectAsState()
    val authState by firebaseViewModel.authState.collectAsState()
    val userProfile = authState.userProfile
    val scope = rememberCoroutineScope()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showLanguageOptions by remember { mutableStateOf(false) }

    val surfaceColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black

    // Animation for arrow rotation
    val arrowRotation by animateFloatAsState(
        targetValue = if (showLanguageOptions) 180f else 0f,
        animationSpec = tween(300),
        label = "arrow_rotation"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = Translations.settings(language),
                        fontWeight = FontWeight.Bold
                    )
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = surfaceColor
                )
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
                            colors = listOf(Color(0xFFF3E5F5), Color.White)
                        )
                    }
                )
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ✅ USER PROFILE CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) Color(0xFF2D2D2D) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (isDarkMode) Color(0xFFCE93D8) else Color(0xFF7B1FA2),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = when (language) {
                                    "en" -> "Profile"
                                    "zh" -> "个人资料"
                                    else -> "Hồ sơ"
                                },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }

                        Divider(color = if (isDarkMode) Color(0xFF3D3D3D) else Color(0xFFE0E0E0))

                        ProfileInfoRow(
                            label = when (language) {
                                "en" -> "Username"
                                "zh" -> "用户名"
                                else -> "Tên người dùng"
                            },
                            value = userProfile?.username ?: "Guest",
                            textColor = textColor
                        )

                        ProfileInfoRow(
                            label = when (language) {
                                "en" -> "Total Score"
                                "zh" -> "总分"
                                else -> "Tổng điểm"
                            },
                            value = userProfile?.totalScore?.toString() ?: "0",
                            textColor = textColor,
                            valueColor = Color(0xFF7B1FA2)
                        )

                        ProfileInfoRow(
                            label = when (language) {
                                "en" -> "Quizzes Completed"
                                "zh" -> "已完成测验"
                                else -> "Quiz đã hoàn thành"
                            },
                            value = userProfile?.quizzesCompleted?.toString() ?: "0",
                            textColor = textColor
                        )
                    }
                }
            }

            // ✅ DARK MODE CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) Color(0xFF2D2D2D) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.DateRange else Icons.Default.Face,
                                contentDescription = null,
                                tint = if (isDarkMode) Color(0xFFCE93D8) else Color(0xFF7B1FA2),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = Translations.darkMode(language),
                                fontSize = 16.sp,
                                color = textColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { settingsViewModel.toggleDarkMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF7B1FA2),
                                checkedTrackColor = Color(0xFFE1BEE7)
                            )
                        )
                    }
                }
            }

            // ✅ LANGUAGE SELECTION CARD WITH DROPDOWN
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) Color(0xFF2D2D2D) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header with arrow
                        Surface(
                            onClick = { showLanguageOptions = !showLanguageOptions },
                            color = Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = if (isDarkMode) Color(0xFFCE93D8) else Color(0xFF7B1FA2),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = Translations.language(language),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = textColor
                                        )
                                        Text(
                                            text = when (language) {
                                                "vi" -> "🇻🇳 Tiếng Việt"
                                                "en" -> "🇬🇧 English"
                                                "zh" -> "🇨🇳 中文"
                                                else -> "🇻🇳 Tiếng Việt"
                                            },
                                            fontSize = 14.sp,
                                            color = textColor.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = if (isDarkMode) Color(0xFFCE93D8) else Color(0xFF7B1FA2),
                                    modifier = Modifier.rotate(arrowRotation)
                                )
                            }
                        }

                        // ✅ EXPANDABLE LANGUAGE OPTIONS
                        AnimatedVisibility(
                            visible = showLanguageOptions,
                            enter = expandVertically(
                                animationSpec = tween(300, easing = EaseInOut)
                            ) + fadeIn(
                                animationSpec = tween(300)
                            ),
                            exit = shrinkVertically(
                                animationSpec = tween(300, easing = EaseInOut)
                            ) + fadeOut(
                                animationSpec = tween(300)
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Divider(color = if (isDarkMode) Color(0xFF3D3D3D) else Color(0xFFE0E0E0))

                                // Vietnamese
                                LanguageOption(
                                    flag = "🇻🇳",
                                    label = "Tiếng Việt",
                                    isSelected = language == "vi",
                                    isDarkMode = isDarkMode,
                                    onClick = {
                                        settingsViewModel.setLanguage("vi")
                                        showLanguageOptions = false
                                    }
                                )

                                // English
                                LanguageOption(
                                    flag = "🇬🇧",
                                    label = "English",
                                    isSelected = language == "en",
                                    isDarkMode = isDarkMode,
                                    onClick = {
                                        settingsViewModel.setLanguage("en")
                                        showLanguageOptions = false
                                    }
                                )

                                // Chinese
                                LanguageOption(
                                    flag = "🇨🇳",
                                    label = "中文",
                                    isSelected = language == "zh",
                                    isDarkMode = isDarkMode,
                                    onClick = {
                                        settingsViewModel.setLanguage("zh")
                                        showLanguageOptions = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // ✅ LOGOUT BUTTON
            item {
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (language) {
                            "en" -> "Logout"
                            "zh" -> "登出"
                            else -> "Đăng xuất"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ✅ LOGOUT CONFIRMATION DIALOG
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F)
                )
            },
            title = {
                Text(
                    text = when (language) {
                        "en" -> "Confirm Logout"
                        "zh" -> "确认登出"
                        else -> "Xác nhận đăng xuất"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = when (language) {
                        "en" -> "Are you sure you want to logout?"
                        "zh" -> "您确定要登出吗？"
                        else -> "Bạn có chắc muốn đăng xuất?"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            firebaseViewModel.logout()
                            settingsViewModel.logout()
                            showLogoutDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
                    )
                ) {
                    Text(
                        text = when (language) {
                            "en" -> "Logout"
                            "zh" -> "登出"
                            else -> "Đăng xuất"
                        }
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }) {
                    Text(
                        text = when (language) {
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

/**
 * ✅ PROFILE INFO ROW COMPONENT
 */
@Composable
fun ProfileInfoRow(
    label: String,
    value: String,
    textColor: Color,
    valueColor: Color = textColor
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = textColor.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

/**
 * ✅ LANGUAGE OPTION COMPONENT
 */
@Composable
fun LanguageOption(
    flag: String,
    label: String,
    isSelected: Boolean,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            Color(0xFF7B1FA2).copy(alpha = 0.2f)
        } else {
            Color.Transparent
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = flag,
                    fontSize = 24.sp
                )
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color(0xFF7B1FA2) else {
                        if (isDarkMode) Color.White else Color.Black
                    }
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF7B1FA2),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
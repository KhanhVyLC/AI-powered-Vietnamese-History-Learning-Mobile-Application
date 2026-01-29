//File: LoginScreen.kt - ✅ FINAL VERSION WITH COMPLETE SYNC
package com.example.pj

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    settingsViewModel: SettingsViewModel,
    firebaseViewModel: FirebaseViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val language by settingsViewModel.language.collectAsState()
    val authState by firebaseViewModel.authState.collectAsState()
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isRegisterMode by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }

    val surfaceColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black

    // ✅ KIỂM TRA ĐĂNG NHẬP THÀNH CÔNG VÀ ĐỒNG BỘ DỮ LIỆU
    LaunchedEffect(authState.isAuthenticated, authState.userProfile) {
        if (authState.isAuthenticated && authState.userProfile != null) {
            val profile = authState.userProfile!!

            // ✅ ĐỒNG BỘ DỮ LIỆU VỚI SETTINGSVIEWMODEL
            settingsViewModel.login(
                username = profile.username,
                score = profile.totalScore,
                weeklyQuizCount = profile.weeklyQuizCount
            )

            // ✅ CẬP NHẬT LEADERBOARD
            scope.launch {
                firebaseViewModel.updateLeaderboard(profile.userId, 0)
            }

            onLoginSuccess()
        }
    }

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
                        colors = listOf(Color(0xFFF3E5F5), Color(0xFFE3F2FD))
                    )
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = if (isDarkMode) Color(0xFF9C27B0) else Color(0xFF7B1FA2),
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "🏛️", fontSize = 56.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = when (language) {
                    "en" -> "ViHis"
                    "zh" -> "越南历史问答"
                    else -> "ViHis"
                },
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color(0xFFCE93D8) else Color(0xFF7B1FA2)
            )

            Text(
                text = when (language) {
                    "en" -> "Learn history through quizzes"
                    "zh" -> "通过测验学习历史"
                    else -> "Học lịch sử qua quiz"
                },
                fontSize = 14.sp,
                color = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Form
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = surfaceColor,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (isRegisterMode) {
                            when (language) {
                                "en" -> "Register"
                                "zh" -> "注册"
                                else -> "Đăng ký"
                            }
                        } else {
                            when (language) {
                                "en" -> "Login"
                                "zh" -> "登录"
                                else -> "Đăng nhập"
                            }
                        },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    // Username (chỉ hiện khi đăng ký)
                    if (isRegisterMode) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = {
                                Text(when (language) {
                                    "en" -> "Username"
                                    "zh" -> "用户名"
                                    else -> "Tên người dùng"
                                })
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (isDarkMode) Color(0xFFCE93D8) else Color(0xFF7B1FA2)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isDarkMode) Color(0xFFCE93D8) else Color(0xFF7B1FA2),
                                unfocusedBorderColor = if (isDarkMode) Color(0xFF757575) else Color.Gray
                            ),
                            singleLine = true,
                            enabled = !authState.isLoading
                        )
                    }

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = if (isDarkMode) Color(0xFFCE93D8) else Color(0xFF7B1FA2)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDarkMode) Color(0xFFCE93D8) else Color(0xFF7B1FA2),
                            unfocusedBorderColor = if (isDarkMode) Color(0xFF757575) else Color.Gray
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        enabled = !authState.isLoading
                    )

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = {
                            Text(when (language) {
                                "en" -> "Password"
                                "zh" -> "密码"
                                else -> "Mật khẩu"
                            })
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isDarkMode) Color(0xFFCE93D8) else Color(0xFF7B1FA2)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Face else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (isDarkMode) Color(0xFF757575) else Color.Gray
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDarkMode) Color(0xFFCE93D8) else Color(0xFF7B1FA2),
                            unfocusedBorderColor = if (isDarkMode) Color(0xFF757575) else Color.Gray
                        ),
                        singleLine = true,
                        enabled = !authState.isLoading
                    )

                    // Error message
                    if (authState.error != null) {
                        Text(
                            text = authState.error!!,
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Submit button
                    Button(
                        onClick = {
                            if (isRegisterMode) {
                                firebaseViewModel.register(email, password, username)
                            } else {
                                firebaseViewModel.login(email, password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkMode) Color(0xFF9C27B0) else Color(0xFF7B1FA2)
                        ),
                        enabled = !authState.isLoading && email.isNotEmpty() && password.isNotEmpty() &&
                                (!isRegisterMode || username.isNotEmpty())
                    ) {
                        if (authState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (isRegisterMode) {
                                    when (language) {
                                        "en" -> "Register"
                                        "zh" -> "注册"
                                        else -> "Đăng ký"
                                    }
                                } else {
                                    when (language) {
                                        "en" -> "Login"
                                        "zh" -> "登录"
                                        else -> "Đăng nhập"
                                    }
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Toggle register/login
                    TextButton(
                        onClick = {
                            isRegisterMode = !isRegisterMode
                            firebaseViewModel.clearError()
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        enabled = !authState.isLoading
                    ) {
                        Text(
                            text = if (isRegisterMode) {
                                when (language) {
                                    "en" -> "Already have an account? Login"
                                    "zh" -> "已有账户？登录"
                                    else -> "Đã có tài khoản? Đăng nhập"
                                }
                            } else {
                                when (language) {
                                    "en" -> "Don't have an account? Register"
                                    "zh" -> "没有账户？注册"
                                    else -> "Chưa có tài khoản? Đăng ký"
                                }
                            },
                            color = if (isDarkMode) Color(0xFFCE93D8) else Color(0xFF7B1FA2),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
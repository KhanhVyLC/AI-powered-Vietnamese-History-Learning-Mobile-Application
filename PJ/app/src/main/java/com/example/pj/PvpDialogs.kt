// File: PvpDialogs.kt - DIALOGS FOR PVP
package com.example.pj

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Dialog chọn độ khó và số câu hỏi
 */
@Composable
fun DifficultySelectionDialog(
    language: String,
    onDismiss: () -> Unit,
    onConfirm: (difficulty: String, questionCount: Int) -> Unit
) {
    var selectedDifficulty by remember { mutableStateOf("Trung bình") }
    var selectedQuestionCount by remember { mutableIntStateOf(5) }

    // ✅ 5 ĐỘ KHÓ GIỐNG DAILY QUIZ
    val difficulties = listOf(
        "Dễ" to "",
        "Trung bình" to "",
        "Khó" to ""
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (language) {
                    "en" -> "Match Settings"
                    "zh" -> "比赛设置"
                    else -> "Cài đặt trận đấu"
                },
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Difficulty Selection
                Text(
                    text = when (language) {
                        "en" -> "Difficulty"
                        "zh" -> "难度"
                        else -> "Độ khó"
                    },
                    fontWeight = FontWeight.Medium
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    difficulties.forEach { (difficulty, emoji) ->
                        FilterChip(
                            selected = selectedDifficulty == difficulty,
                            onClick = { selectedDifficulty = difficulty },
                            label = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(emoji)
                                    Text(difficulty)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Divider()

                // Question Count Selection
                Text(
                    text = when (language) {
                        "en" -> "Number of Questions"
                        "zh" -> "题目数量"
                        else -> "Số câu hỏi"
                    },
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5, 10, 15).forEach { count ->
                        FilterChip(
                            selected = selectedQuestionCount == count,
                            onClick = { selectedQuestionCount = count },
                            label = { Text("$count") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedDifficulty, selectedQuestionCount)
                }
            ) {
                Text(
                    when (language) {
                        "en" -> "Start"
                        "zh" -> "开始"
                        else -> "Bắt đầu"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
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


/**
 * Dialog tạo hoặc join phòng bạn bè
 */
@Composable
fun FriendRoomDialog(
    language: String,
    onDismiss: () -> Unit,
    onCreateRoom: (difficulty: String, questionCount: Int) -> Unit,
    onJoinRoom: (roomCode: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Create, 1 = Join
    var roomCode by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf("Trung bình") }
    var selectedQuestionCount by remember { mutableIntStateOf(5) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (language) {
                    "en" -> "Play with Friend"
                    "zh" -> "与好友对战"
                    else -> "Chơi với bạn"
                },
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tab Selection
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                when (language) {
                                    "en" -> "Create"
                                    "zh" -> "创建"
                                    else -> "Tạo phòng"
                                }
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                when (language) {
                                    "en" -> "Join"
                                    "zh" -> "加入"
                                    else -> "Vào phòng"
                                }
                            )
                        }
                    )
                }

                if (selectedTab == 0) {
                    // Create Room Tab
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = when (language) {
                                "en" -> "Difficulty"
                                "zh" -> "难度"
                                else -> "Độ khó"
                            },
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Dễ", "Trung bình", "Khó").forEach { difficulty ->
                                FilterChip(
                                    selected = selectedDifficulty == difficulty,
                                    onClick = { selectedDifficulty = difficulty },
                                    label = { Text(difficulty, fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Text(
                            text = when (language) {
                                "en" -> "Questions"
                                "zh" -> "题目"
                                else -> "Số câu"
                            },
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(5, 10, 15).forEach { count ->
                                FilterChip(
                                    selected = selectedQuestionCount == count,
                                    onClick = { selectedQuestionCount = count },
                                    label = { Text("$count", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                } else {
                    // Join Room Tab
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = when (language) {
                                "en" -> "Enter room code"
                                "zh" -> "输入房间代码"
                                else -> "Nhập mã phòng"
                            },
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        OutlinedTextField(
                            value = roomCode,
                            onValueChange = { roomCode = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    when (language) {
                                        "en" -> "Room code"
                                        "zh" -> "房间代码"
                                        else -> "Mã phòng"
                                    }
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedTab == 0) {
                        onCreateRoom(selectedDifficulty, selectedQuestionCount)
                    } else {
                        if (roomCode.isNotBlank()) {
                            onJoinRoom(roomCode.trim())
                        }
                    }
                },
                enabled = selectedTab == 0 || roomCode.isNotBlank()
            ) {
                Text(
                    when (language) {
                        "en" -> if (selectedTab == 0) "Create" else "Join"
                        "zh" -> if (selectedTab == 0) "创建" else "加入"
                        else -> if (selectedTab == 0) "Tạo" else "Vào"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
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
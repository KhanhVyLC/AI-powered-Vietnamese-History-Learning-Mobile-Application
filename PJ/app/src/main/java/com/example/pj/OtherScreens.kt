//File: OtherScreens.kt - ✅ CHỈ CHỨA QuizSetCard
package com.example.pj

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ==================== QUIZ SET CARD ====================
@Composable
fun QuizSetCard(
    quizSet: TopicQuizSet,
    isDarkMode: Boolean,
    language: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF2D2D2D) else Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF7B1FA2),
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${quizSet.setNumber}",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column {
                    Text(
                        text = quizSet.setName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color.Black
                    )
                    Text(
                        text = "${quizSet.questions.size} " + when (language) {
                            "en" -> "questions"
                            "zh" -> "个问题"
                            else -> "câu hỏi"
                        },
                        fontSize = 14.sp,
                        color = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF7B1FA2).copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
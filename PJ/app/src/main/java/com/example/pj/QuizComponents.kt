//File: CompactAnswerButton in QuizComponents.kt - ✅ FIXED TEXT WRAPPING
package com.example.pj

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ✅ COMPACT ANSWER BUTTON - FIXED: Auto-expand height for long answers
 */
@Composable
fun CompactAnswerButton(
    answer: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    showResult: Boolean,
    enabled: Boolean,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        !showResult && isSelected -> Color(0xFF7B1FA2).copy(alpha = 0.3f)
        showResult && isSelected && isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.3f)
        showResult && isSelected && !isCorrect -> Color(0xFFFF5252).copy(alpha = 0.3f)
        showResult && !isSelected && isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.2f)
        else -> if (isDarkMode) Color(0xFF2D2D2D) else Color.White
    }

    val borderColor = when {
        showResult && isCorrect -> Color(0xFF4CAF50)
        showResult && isSelected && !isCorrect -> Color(0xFFFF5252)
        isSelected -> Color(0xFF7B1FA2)
        else -> if (isDarkMode) Color(0xFF3D3D3D) else Color(0xFFE0E0E0)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp) // ✅ FIXED: Use heightIn instead of fixed height
            .border(2.dp, borderColor, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        onClick = onClick,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp), // ✅ FIXED: Added vertical padding
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = answer,
                fontSize = 15.sp,
                color = if (isDarkMode) Color.White else Color.Black,
                modifier = Modifier.weight(1f),
                lineHeight = 20.sp, // ✅ FIXED: Added line height for better readability
                maxLines = Int.MAX_VALUE // ✅ FIXED: Allow unlimited lines
            )

            if (showResult && isCorrect) {
                Text(
                    text = "✓",
                    fontSize = 22.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp) // ✅ Added spacing
                )
            } else if (showResult && isSelected && !isCorrect) {
                Text(
                    text = "✗",
                    fontSize = 22.sp,
                    color = Color(0xFFFF5252),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp) // ✅ Added spacing
                )
            }
        }
    }
}
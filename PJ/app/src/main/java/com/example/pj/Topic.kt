//File: Topic.kt - DATA CLASS CHO TOPICS
package com.example.pj

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color

/**
 * Data class cho Topic (Chủ đề)
 */
data class Topic(
    val id: Int,
    val nameVi: String,
    val nameEn: String,
    val nameZh: String,
    val icon: String,
    val quizCount: Int,
    val gradient: List<Color>,
    @DrawableRes val imageRes: Int
) {
    fun getName(language: String): String = when (language) {
        "en" -> nameEn
        "zh" -> nameZh
        else -> nameVi
    }
}
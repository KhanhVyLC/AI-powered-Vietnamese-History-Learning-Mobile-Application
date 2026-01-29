//File: FirebaseDataModels.kt - ✅ FIXED: QuizAnswer field naming
package com.example.pj

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * USER MODEL
 */
data class UserProfile(
    @DocumentId
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val totalScore: Int = 0,
    val level: Int = 1,
    val experiencePoints: Int = 0,
    val quizzesCompleted: Int = 0,
    val correctAnswers: Int = 0,
    val totalAnswers: Int = 0,
    val weeklyQuizCount: Int = 0,
    val streak: Int = 0,
    val lastLoginDate: Date? = null,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    @Exclude
    fun getAccuracyRate(): Double {
        return if (totalAnswers > 0) {
            (correctAnswers.toDouble() / totalAnswers) * 100
        } else 0.0
    }
}

/**
 * TOPIC PROGRESS
 */
data class TopicProgress(
    val userId: String = "",
    val topicId: Int = 0,
    val completedSets: List<Int> = emptyList(),
    val totalScore: Int = 0,
    val bestScore: Int = 0,
    val accuracy: Double = 0.0,
    @ServerTimestamp
    val lastAttempt: Date? = null
) {
    @Exclude
    fun isSetCompleted(setNumber: Int): Boolean {
        return completedSets.contains(setNumber)
    }

    @Exclude
    fun getCompletionPercentage(totalSets: Int): Int {
        return if (totalSets > 0) {
            (completedSets.size.toFloat() / totalSets * 100).toInt()
        } else 0
    }
}

/**
 * QUIZ RESULT
 */
data class QuizResult(
    @DocumentId
    val resultId: String = "",
    val userId: String = "",
    val quizId: String = "",
    val topicId: Int = 0,
    val topicName: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val correctAnswers: Int = 0,
    val timeSpent: Long = 0,
    val difficulty: String = "THCS",
    val quizType: String = "topic",
    val setNumber: Int? = null,
    val answers: List<QuizAnswer> = emptyList(),
    @ServerTimestamp
    val completedAt: Date? = null
)

/**
 * ✅ FIXED: QuizAnswer with proper Firestore field naming
 */
data class QuizAnswer(
    val questionId: String = "",
    val questionText: String = "",
    val userAnswer: String = "",
    val correctAnswer: String = "",

    // ✅ FIX: Use @PropertyName to map isCorrect to "isCorrect" in Firestore
    @get:PropertyName("isCorrect")
    @set:PropertyName("isCorrect")
    var isCorrect: Boolean = false,

    val timeSpent: Long = 0
) {
    // ✅ No-argument constructor for Firestore
    constructor() : this("", "", "", "", false, 0)
}

/**
 * ✅ NEW: CHAT SESSION - Một đoạn chat riêng biệt
 */
data class ChatSession(
    @DocumentId
    val sessionId: String = "",
    val userId: String = "",
    val title: String = "Cuộc trò chuyện mới",
    val messages: List<ChatMessageData> = emptyList(),
    val messageCount: Int = 0,
    val lastMessage: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    @Exclude
    fun getPreview(): String {
        return if (lastMessage.length > 50) {
            lastMessage.take(50) + "..."
        } else {
            lastMessage
        }
    }

    @Exclude
    fun getTimeAgo(): String {
        val now = Date()
        val diff = (now.time - (updatedAt?.time ?: now.time)) / 1000 // seconds

        return when {
            diff < 60 -> "Vừa xong"
            diff < 3600 -> "${diff / 60} phút trước"
            diff < 86400 -> "${diff / 3600} giờ trước"
            diff < 604800 -> "${diff / 86400} ngày trước"
            else -> "${diff / 604800} tuần trước"
        }
    }
}

data class ChatMessageData(
    val messageId: String = "",
    val text: String = "",
    val isUser: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * LEGACY: CHAT HISTORY (Keep for backward compatibility)
 */
@Deprecated("Use ChatSession instead")
data class ChatHistory(
    @DocumentId
    val chatId: String = "",
    val userId: String = "",
    val messages: List<ChatMessageData> = emptyList(),
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)

/**
 * ACHIEVEMENT
 */
data class Achievement(
    @DocumentId
    val achievementId: String = "",
    val title: String = "",
    val description: String = "",
    val iconUrl: String = "",
    val requirement: Int = 0,
    val points: Int = 0,
    val category: String = ""
)

data class UserAchievement(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val achievementId: String = "",
    val progress: Int = 0,
    val isUnlocked: Boolean = false,
    @ServerTimestamp
    val unlockedAt: Date? = null
)

/**
 * LEADERBOARD ENTRY
 */
data class LeaderboardEntry(
    @DocumentId
    val userId: String = "",
    val username: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val totalScore: Int = 0,
    val weeklyScore: Int = 0,
    val monthlyScore: Int = 0,
    val rank: Int = 0,
    @ServerTimestamp
    val lastUpdated: Date? = null
)

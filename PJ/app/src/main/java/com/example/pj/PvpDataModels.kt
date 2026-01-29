// File: PvpDataModels.kt - ✅ FIXED: Changed answers from Map to List
package com.example.pj

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

/**
 * ==================== PVP MATCH ROOM ====================
 */
@IgnoreExtraProperties
data class PvpRoom(
    val roomId: String = "",
    val shortCode: String = "",
    val mode: PvpMode = PvpMode.QUICK_MATCH,
    val difficulty: String = "Trung bình",
    val questionCount: Int = 5,
    val status: RoomStatus = RoomStatus.WAITING,
    val hostUserId: String = "",
    val players: Map<String, PvpPlayer> = emptyMap(),
    val questions: List<PvpQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val startTime: Long = 0,
    val endTime: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    @Exclude
    fun isHost(userId: String): Boolean = hostUserId == userId

    @Exclude
    fun isFull(): Boolean = players.size >= 2

    @Exclude
    fun getPlayer(userId: String): PvpPlayer? = players[userId]

    @Exclude
    fun getOpponent(userId: String): PvpPlayer? {
        return players.values.firstOrNull { it.userId != userId }
    }

    @Exclude
    fun canStart(): Boolean {
        return status == RoomStatus.WAITING && players.size >= 2
    }
}

/**
 * ==================== PVP PLAYER ====================
 * ✅ CRITICAL FIX: Changed answers from Map<String, PvpAnswer> to List<PvpAnswer>
 * This prevents Firebase deserialization error: "Expected a Map but got ArrayList"
 */
@IgnoreExtraProperties
data class PvpPlayer(
    val userId: String = "",
    val username: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val score: Int = 0,
    val correctAnswers: Int = 0,
    val answers: List<PvpAnswer> = emptyList(), // ✅ FIXED: Changed to List
    val isReady: Boolean = false,
    val isOnline: Boolean = true,
    val lastSeen: Long = System.currentTimeMillis()
) {
    @Exclude
    fun hasAnswered(questionIndex: Int): Boolean {
        return answers.any { it.questionIndex == questionIndex }
    }

    @Exclude
    fun getAnswer(questionIndex: Int): PvpAnswer? {
        return answers.firstOrNull { it.questionIndex == questionIndex }
    }
}

/**
 * ==================== PVP ANSWER ====================
 */
@IgnoreExtraProperties
data class PvpAnswer(
    val questionIndex: Int = 0,
    val selectedAnswer: String = "",
    val isCorrect: Boolean = false,
    val timeSpent: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * ==================== PVP QUESTION ====================
 */
@IgnoreExtraProperties
data class PvpQuestion(
    val questionId: String = "",
    val question: String = "",
    val answers: List<String> = emptyList(),
    val correctAnswer: String = "",
    val explanation: String = "",
    val imageUrl: String = ""
)

/**
 * ==================== ROOM STATUS ====================
 */
enum class RoomStatus {
    WAITING,
    STARTING,
    IN_PROGRESS,
    FINISHED,
    CANCELLED
}

/**
 * ==================== PVP MODE ====================
 */
enum class PvpMode {
    QUICK_MATCH,
    FRIEND_MATCH,
    TOURNAMENT
}

/**
 * ==================== PVP MATCH RESULT ====================
 */
@IgnoreExtraProperties
data class PvpMatchResult(
    val resultId: String = "",
    val roomId: String = "",
    val winnerId: String = "",
    val loserId: String = "",
    val isDraw: Boolean = false,
    val players: Map<String, PvpPlayerResult> = emptyMap(),
    val questionCount: Int = 0,
    val difficulty: String = "",
    val startTime: Long = 0,
    val endTime: Long = 0,
    val duration: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * ==================== PVP PLAYER RESULT ====================
 */
@IgnoreExtraProperties
data class PvpPlayerResult(
    val userId: String = "",
    val username: String = "",
    val score: Int = 0,
    val correctAnswers: Int = 0,
    val totalQuestions: Int = 0,
    val accuracy: Double = 0.0,
    val averageTimePerQuestion: Long = 0,
    val rank: Int = 0,
    val ratingChange: Int = 0
)

/**
 * ==================== PVP USER STATS ====================
 */
@IgnoreExtraProperties
data class PvpUserStats(
    val userId: String = "",
    val totalMatches: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val winRate: Double = 0.0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val rating: Int = 1000,
    val rank: String = "Beginner",
    val totalScore: Int = 0,
    val averageAccuracy: Double = 0.0,
    val lastPlayedAt: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    @Exclude
    fun getRankTitle(): String {
        return when {
            rating >= 2000 -> "Diamond"
            rating >= 1700 -> "Platinum"
            rating >= 1400 -> "Gold"
            rating >= 1100 -> "Silver"
            rating >= 800 -> "Bronze"
            else -> "Beginner"
        }
    }

    @Exclude
    fun getWinRatePercentage(): Int {
        return if (totalMatches > 0) {
            ((wins.toDouble() / totalMatches) * 100).toInt()
        } else 0
    }
}

/**
 * ==================== PVP QUEUE ENTRY ====================
 */
@IgnoreExtraProperties
data class PvpQueueEntry(
    val userId: String = "",
    val username: String = "",
    val displayName: String = "",
    val rating: Int = 1000,
    val difficulty: String = "Trung bình",
    val questionCount: Int = 5,
    val timestamp: Long = System.currentTimeMillis()
) {
    @Exclude
    fun isExpired(): Boolean {
        return System.currentTimeMillis() - timestamp > 60000
    }
}

/**
 * ==================== PVP INVITATION ====================
 */
@IgnoreExtraProperties
data class PvpInvitation(
    val invitationId: String = "",
    val fromUserId: String = "",
    val fromUsername: String = "",
    val toUserId: String = "",
    val roomId: String = "",
    val difficulty: String = "Trung bình",
    val questionCount: Int = 5,
    val status: InvitationStatus = InvitationStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 300000
) {
    @Exclude
    fun isExpired(): Boolean {
        return System.currentTimeMillis() > expiresAt
    }
}

enum class InvitationStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    EXPIRED,
    CANCELLED
}
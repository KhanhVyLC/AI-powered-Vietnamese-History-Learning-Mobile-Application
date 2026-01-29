//File: FirebaseRepository.kt - ✅ COMPLETE WITH CHAT SESSIONS
package com.example.pj

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val usersCollection = firestore.collection("users")
    private val quizResultsCollection = firestore.collection("quiz_results")
    private val chatHistoryCollection = firestore.collection("chat_history")
    private val chatSessionsCollection = firestore.collection("chat_sessions")
    private val achievementsCollection = firestore.collection("achievements")
    private val leaderboardCollection = firestore.collection("leaderboard")
    private val topicProgressCollection = firestore.collection("topic_progress")

    companion object {
        private const val TAG = "FirebaseRepository"
    }

    // ==================== AUTHENTICATION ====================

    suspend fun registerUser(email: String, password: String, username: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: return Result.failure(Exception("User is null"))

            val userProfile = UserProfile(
                userId = user.uid,
                username = username,
                email = email,
                displayName = username,
                createdAt = Date(),
                updatedAt = Date()
            )

            usersCollection.document(user.uid).set(userProfile).await()
            Log.d(TAG, "✅ User registered: ${user.uid}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Register failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: return Result.failure(Exception("User is null"))

            usersCollection.document(user.uid).update(
                mapOf(
                    "lastLoginDate" to Date(),
                    "updatedAt" to Date()
                )
            ).await()

            Log.d(TAG, "✅ User logged in: ${user.uid}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Login failed: ${e.message}")
            Result.failure(e)
        }
    }

    fun logoutUser() {
        auth.signOut()
        Log.d(TAG, "✅ User logged out")
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // ==================== USER PROFILE ====================

    suspend fun getUserProfile(userId: String): Result<UserProfile> {
        return try {
            val snapshot = usersCollection.document(userId).get().await()
            val profile = snapshot.toObject(UserProfile::class.java)
                ?: return Result.failure(Exception("User profile not found"))

            Log.d(TAG, "✅ Profile loaded: ${profile.username}")
            Result.success(profile)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Get profile failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateUserScore(userId: String, scoreToAdd: Int): Result<Unit> {
        return try {
            val userRef = usersCollection.document(userId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentScore = snapshot.getLong("totalScore")?.toInt() ?: 0
                val newScore = currentScore + scoreToAdd

                transaction.update(userRef, mapOf(
                    "totalScore" to newScore,
                    "updatedAt" to Date()
                ))
            }.await()

            Log.d(TAG, "✅ Score updated: +$scoreToAdd")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Update score failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun incrementQuizCount(userId: String): Result<Unit> {
        return try {
            val userRef = usersCollection.document(userId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentCount = snapshot.getLong("quizzesCompleted")?.toInt() ?: 0
                val weeklyCount = snapshot.getLong("weeklyQuizCount")?.toInt() ?: 0

                transaction.update(userRef, mapOf(
                    "quizzesCompleted" to currentCount + 1,
                    "weeklyQuizCount" to weeklyCount + 1,
                    "updatedAt" to Date()
                ))
            }.await()

            Log.d(TAG, "✅ Quiz count incremented")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Increment quiz count failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateStreak(userId: String): Result<Unit> {
        return try {
            val userRef = usersCollection.document(userId)
            val snapshot = userRef.get().await()
            val lastLoginDate = snapshot.getDate("lastLoginDate")
            val currentStreak = snapshot.getLong("streak")?.toInt() ?: 0

            val today = Date()
            val newStreak = if (lastLoginDate != null) {
                val daysDiff = ((today.time - lastLoginDate.time) / (1000 * 60 * 60 * 24)).toInt()
                when (daysDiff) {
                    0 -> currentStreak
                    1 -> currentStreak + 1
                    else -> 1
                }
            } else {
                1
            }

            userRef.update(mapOf(
                "streak" to newStreak,
                "lastLoginDate" to today,
                "updatedAt" to Date()
            )).await()

            Log.d(TAG, "✅ Streak updated: $newStreak")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Update streak failed: ${e.message}")
            Result.failure(e)
        }
    }

    // ==================== TOPIC PROGRESS ====================

    suspend fun updateTopicProgress(
        userId: String,
        topicId: Int,
        setNumber: Int,
        score: Int,
        correctAnswers: Int,
        totalQuestions: Int
    ): Result<Unit> {
        return try {
            val progressRef = topicProgressCollection
                .document("${userId}_topic_${topicId}")

            val snapshot = progressRef.get().await()

            if (snapshot.exists()) {
                val progress = snapshot.toObject(TopicProgress::class.java)!!
                val completedSets = progress.completedSets.toMutableList()

                if (!completedSets.contains(setNumber)) {
                    completedSets.add(setNumber)
                }

                val newTotalScore = progress.totalScore + score
                val newBestScore = maxOf(progress.bestScore, score)
                val newAccuracy = if (totalQuestions > 0) {
                    (correctAnswers.toDouble() / totalQuestions * 100)
                } else progress.accuracy

                progressRef.update(mapOf(
                    "completedSets" to completedSets.sorted(),
                    "totalScore" to newTotalScore,
                    "bestScore" to newBestScore,
                    "accuracy" to newAccuracy,
                    "lastAttempt" to Date()
                )).await()
            } else {
                val newProgress = TopicProgress(
                    userId = userId,
                    topicId = topicId,
                    completedSets = listOf(setNumber),
                    totalScore = score,
                    bestScore = score,
                    accuracy = if (totalQuestions > 0) {
                        (correctAnswers.toDouble() / totalQuestions * 100)
                    } else 0.0,
                    lastAttempt = Date()
                )
                progressRef.set(newProgress).await()
            }

            Log.d(TAG, "✅ Topic progress updated")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Update topic progress failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getTopicProgress(userId: String, topicId: Int): Result<TopicProgress> {
        return try {
            val snapshot = topicProgressCollection
                .document("${userId}_topic_${topicId}")
                .get()
                .await()

            if (snapshot.exists()) {
                val progress = snapshot.toObject(TopicProgress::class.java)!!
                Result.success(progress)
            } else {
                Result.success(TopicProgress(
                    userId = userId,
                    topicId = topicId
                ))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Get topic progress failed: ${e.message}")
            Result.failure(e)
        }
    }

    // ==================== QUIZ RESULTS ====================

    suspend fun saveQuizResult(result: QuizResult): Result<String> {
        return try {
            val docRef = quizResultsCollection.add(result).await()

            val userRef = usersCollection.document(result.userId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentCorrect = snapshot.getLong("correctAnswers")?.toInt() ?: 0
                val currentTotal = snapshot.getLong("totalAnswers")?.toInt() ?: 0

                transaction.update(userRef, mapOf(
                    "correctAnswers" to currentCorrect + result.correctAnswers,
                    "totalAnswers" to currentTotal + result.totalQuestions,
                    "updatedAt" to Date()
                ))
            }.await()

            Log.d(TAG, "✅ Quiz result saved: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Save quiz result failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getUserQuizHistory(userId: String, limit: Int = 20): Result<List<QuizResult>> {
        return try {
            val snapshot = quizResultsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val results = snapshot.toObjects(QuizResult::class.java)
                .sortedByDescending { it.completedAt }
                .take(limit)

            Log.d(TAG, "✅ Quiz history loaded: ${results.size} results")
            Result.success(results)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Get quiz history failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getDailyQuizHistory(userId: String, limit: Int = 50): Result<List<QuizResult>> {
        return try {
            val snapshot = quizResultsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("quizType", "daily")
                .get()
                .await()

            val results = snapshot.toObjects(QuizResult::class.java)
                .sortedByDescending { it.completedAt }
                .take(limit)

            Log.d(TAG, "✅ Daily quiz history loaded: ${results.size} results")
            Result.success(results)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Get daily quiz history failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getTopicStatistics(userId: String, topicId: Int): Result<TopicStats> {
        return try {
            val snapshot = quizResultsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("topicId", topicId)
                .get()
                .await()

            val results = snapshot.toObjects(QuizResult::class.java)

            if (results.isEmpty()) {
                return Result.success(TopicStats())
            }

            val totalQuizzes = results.size
            val totalScore = results.sumOf { it.score }
            val totalCorrect = results.sumOf { it.correctAnswers }
            val totalQuestions = results.sumOf { it.totalQuestions }
            val avgScore = totalScore / totalQuizzes
            val accuracy = if (totalQuestions > 0) {
                (totalCorrect.toDouble() / totalQuestions * 100)
            } else 0.0

            Result.success(TopicStats(
                totalQuizzes = totalQuizzes,
                averageScore = avgScore,
                accuracy = accuracy,
                totalCorrect = totalCorrect,
                totalQuestions = totalQuestions
            ))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Get topic statistics failed: ${e.message}")
            Result.failure(e)
        }
    }

    // ==================== CHAT SESSIONS (NEW) ====================

    /**
     * ✅ TẠO SESSION MỚI
     */
    suspend fun createChatSession(userId: String, initialTitle: String): Result<ChatSession> {
        return try {
            val newSession = ChatSession(
                userId = userId,
                title = initialTitle,
                messages = emptyList(),
                messageCount = 0,
                lastMessage = "",
                createdAt = Date(),
                updatedAt = Date()
            )

            val docRef = chatSessionsCollection.add(newSession).await()
            val createdSession = newSession.copy(sessionId = docRef.id)

            // Update với sessionId
            docRef.set(createdSession).await()

            Log.d(TAG, "✅ Chat session created: ${docRef.id}")
            Result.success(createdSession)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Create chat session failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * ✅ LẤY TẤT CẢ SESSIONS CỦA USER
     */
    suspend fun getChatSessions(userId: String): Result<List<ChatSession>> {
        return try {
            val snapshot = chatSessionsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            // Sort by updatedAt in memory (no index needed)
            val sessions = snapshot.toObjects(ChatSession::class.java)
                .sortedByDescending { it.updatedAt }

            Log.d(TAG, "✅ Chat sessions loaded: ${sessions.size} sessions")
            Result.success(sessions)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Get chat sessions failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * ✅ LƯU/CẬP NHẬT SESSION
     */
    suspend fun saveChatSession(session: ChatSession): Result<Unit> {
        return try {
            val updatedSession = session.copy(
                messageCount = session.messages.size,
                lastMessage = session.messages.lastOrNull()?.text ?: "",
                updatedAt = Date()
            )

            chatSessionsCollection
                .document(session.sessionId)
                .set(updatedSession)
                .await()

            Log.d(TAG, "✅ Chat session saved: ${session.sessionId}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Save chat session failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * ✅ XÓA SESSION
     */
    suspend fun deleteChatSession(sessionId: String): Result<Unit> {
        return try {
            chatSessionsCollection
                .document(sessionId)
                .delete()
                .await()

            Log.d(TAG, "✅ Chat session deleted: $sessionId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Delete chat session failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * ✅ ĐỔI TÊN SESSION
     */
    suspend fun updateSessionTitle(sessionId: String, newTitle: String): Result<Unit> {
        return try {
            chatSessionsCollection
                .document(sessionId)
                .update(
                    mapOf(
                        "title" to newTitle,
                        "updatedAt" to Date()
                    )
                )
                .await()

            Log.d(TAG, "✅ Session title updated: $sessionId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Update session title failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * ✅ LẤY MỘT SESSION CỤ THỂ
     */
    suspend fun getChatSession(sessionId: String): Result<ChatSession> {
        return try {
            val snapshot = chatSessionsCollection
                .document(sessionId)
                .get()
                .await()

            val session = snapshot.toObject(ChatSession::class.java)
                ?: return Result.failure(Exception("Session not found"))

            Log.d(TAG, "✅ Chat session loaded: $sessionId")
            Result.success(session)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Get chat session failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * ✅ XÓA TẤT CẢ SESSIONS CỦA USER
     */
    suspend fun deleteAllChatSessions(userId: String): Result<Unit> {
        return try {
            val snapshot = chatSessionsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }

            Log.d(TAG, "✅ All chat sessions deleted for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Delete all chat sessions failed: ${e.message}")
            Result.failure(e)
        }
    }

    // ==================== LEGACY CHAT HISTORY ====================

    @Deprecated("Use chat sessions instead")
    suspend fun saveChatHistory(userId: String, messages: List<ChatMessageData>): Result<Unit> {
        return try {
            val chatHistory = ChatHistory(
                userId = userId,
                messages = messages,
                updatedAt = Date()
            )

            val existingChat = chatHistoryCollection
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()

            if (existingChat.isEmpty) {
                chatHistoryCollection.add(chatHistory).await()
            } else {
                existingChat.documents.first().reference.set(chatHistory).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @Deprecated("Use chat sessions instead")
    suspend fun getChatHistory(userId: String): Result<List<ChatMessageData>> {
        return try {
            val snapshot = chatHistoryCollection
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()

            val messages = if (snapshot.isEmpty) {
                emptyList()
            } else {
                val chatHistory = snapshot.documents.first().toObject(ChatHistory::class.java)
                chatHistory?.messages ?: emptyList()
            }

            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @Deprecated("Use chat sessions instead")
    suspend fun clearChatHistory(userId: String): Result<Unit> {
        return try {
            val snapshot = chatHistoryCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            snapshot.documents.forEach { it.reference.delete().await() }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== LEADERBOARD ====================

    suspend fun updateLeaderboard(userId: String, scoreToAdd: Int): Result<Unit> {
        return try {
            val userProfile = getUserProfile(userId).getOrNull()
                ?: return Result.failure(Exception("User not found"))

            val leaderboardRef = leaderboardCollection.document(userId)
            val snapshot = leaderboardRef.get().await()

            if (snapshot.exists()) {
                val currentWeekly = snapshot.getLong("weeklyScore")?.toInt() ?: 0
                val currentMonthly = snapshot.getLong("monthlyScore")?.toInt() ?: 0

                leaderboardRef.update(mapOf(
                    "totalScore" to userProfile.totalScore,
                    "weeklyScore" to currentWeekly + scoreToAdd,
                    "monthlyScore" to currentMonthly + scoreToAdd,
                    "lastUpdated" to Date()
                )).await()
            } else {
                val entry = LeaderboardEntry(
                    userId = userId,
                    username = userProfile.username,
                    displayName = userProfile.displayName,
                    avatarUrl = userProfile.avatarUrl,
                    totalScore = userProfile.totalScore,
                    weeklyScore = scoreToAdd,
                    monthlyScore = scoreToAdd
                )
                leaderboardRef.set(entry).await()
            }

            Log.d(TAG, "✅ Leaderboard updated")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Update leaderboard failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getTopLeaderboard(limit: Int = 10, period: String = "all"): Result<List<LeaderboardEntry>> {
        return try {
            val field = when (period) {
                "weekly" -> "weeklyScore"
                "monthly" -> "monthlyScore"
                else -> "totalScore"
            }

            val snapshot = leaderboardCollection
                .orderBy(field, Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val entries = snapshot.toObjects(LeaderboardEntry::class.java)
                .mapIndexed { index, entry -> entry.copy(rank = index + 1) }

            Log.d(TAG, "✅ Leaderboard loaded: ${entries.size} entries")
            Result.success(entries)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Get leaderboard failed: ${e.message}")
            Result.failure(e)
        }
    }
}

// ==================== DATA CLASSES ====================

data class TopicStats(
    val totalQuizzes: Int = 0,
    val averageScore: Int = 0,
    val accuracy: Double = 0.0,
    val totalCorrect: Int = 0,
    val totalQuestions: Int = 0
)
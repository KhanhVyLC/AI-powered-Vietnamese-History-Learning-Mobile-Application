//File: FirebaseViewModel.kt - ✅ FIXED: No more duplicate profile loads
package com.example.pj

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "FirebaseViewModel"

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val user: FirebaseUser? = null,
    val userProfile: UserProfile? = null,
    val error: String? = null
)

class FirebaseViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _quizHistory = MutableStateFlow<List<QuizResult>>(emptyList())
    val quizHistory: StateFlow<List<QuizResult>> = _quizHistory.asStateFlow()

    private val _dailyQuizHistory = MutableStateFlow<List<QuizResult>>(emptyList())
    val dailyQuizHistory: StateFlow<List<QuizResult>> = _dailyQuizHistory.asStateFlow()

    private val _topicProgress = MutableStateFlow<Map<Int, TopicProgress>>(emptyMap())
    val topicProgress: StateFlow<Map<Int, TopicProgress>> = _topicProgress.asStateFlow()

    private val _leaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()

    private val _chatSessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val chatSessions: StateFlow<List<ChatSession>> = _chatSessions.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        val user = repository.getCurrentUser()
        Log.d(TAG, "checkAuthState: user = ${user?.uid}")
        if (user != null) {
            _authState.value = _authState.value.copy(
                isAuthenticated = true,
                user = user
            )
            loadUserProfile(user.uid)
            loadAllTopicProgress()
            loadQuizHistory()
            loadChatSessions()
        }
    }

    fun register(email: String, password: String, username: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            val result = repository.registerUser(email, password, username)

            result.onSuccess { user ->
                Log.d(TAG, "Register success: ${user.uid}")
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    user = user
                )
                loadUserProfile(user.uid)
                updateLeaderboard(user.uid, 0)
            }.onFailure { exception ->
                Log.e(TAG, "Register failed", exception)
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Registration failed"
                )
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            val result = repository.loginUser(email, password)

            result.onSuccess { user ->
                Log.d(TAG, "Login success: ${user.uid}")
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    user = user
                )
                loadUserProfile(user.uid)
                repository.updateStreak(user.uid)
                updateLeaderboard(user.uid, 0)
                loadAllTopicProgress()
                loadQuizHistory()
                loadDailyQuizHistory()
                loadChatSessions()
            }.onFailure { exception ->
                Log.e(TAG, "Login failed", exception)
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = getErrorMessage(exception)
                )
            }
        }
    }

    fun logout() {
        Log.d(TAG, "Logout")
        repository.logoutUser()
        _authState.value = AuthState()
        _quizHistory.value = emptyList()
        _dailyQuizHistory.value = emptyList()
        _topicProgress.value = emptyMap()
        _leaderboard.value = emptyList()
        _chatSessions.value = emptyList()
    }

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            val result = repository.getUserProfile(userId)
            result.onSuccess { profile ->
                Log.d(TAG, "User profile loaded: ${profile.username}")
                _authState.value = _authState.value.copy(userProfile = profile)
            }.onFailure {
                Log.e(TAG, "Failed to load user profile", it)
            }
        }
    }

    // ✅ FIXED: Private helper to update score and leaderboard ONCE
    private suspend fun updateScoreAndLeaderboard(userId: String, scoreToAdd: Int) {
        repository.updateUserScore(userId, scoreToAdd)
        repository.updateLeaderboard(userId, scoreToAdd)
        loadUserProfile(userId) // Only 1 profile load here
    }

    fun updateScore(scoreToAdd: Int) {
        val userId = _authState.value.user?.uid ?: return

        viewModelScope.launch {
            updateScoreAndLeaderboard(userId, scoreToAdd)
        }
    }

    // ==================== TOPIC QUIZ ====================

    fun saveTopicQuizResult(result: QuizResult, topicId: Int, setNumber: Int) {
        viewModelScope.launch {
            val userId = _authState.value.user?.uid ?: return@launch
            Log.d(TAG, "Saving topic quiz: user=$userId, topic=$topicId, set=$setNumber")

            val resultWithUserId = result.copy(
                userId = userId,
                quizType = "topic",
                setNumber = setNumber,
                topicId = topicId
            )

            repository.saveQuizResult(resultWithUserId).onSuccess {
                Log.d(TAG, "✅ Topic quiz saved successfully")

                // ✅ FIXED: All updates in one batch - no duplicate calls
                repository.incrementQuizCount(userId)
                updateScoreAndLeaderboard(userId, result.score) // Only calls loadUserProfile ONCE

                repository.updateTopicProgress(
                    userId = userId,
                    topicId = topicId,
                    setNumber = setNumber,
                    score = result.score,
                    correctAnswers = result.correctAnswers,
                    totalQuestions = result.totalQuestions
                )

                loadQuizHistory()
                loadTopicProgress(topicId)
            }.onFailure {
                Log.e(TAG, "❌ Failed to save topic quiz", it)
            }
        }
    }

    fun loadTopicProgress(topicId: Int) {
        val userId = _authState.value.user?.uid ?: return

        viewModelScope.launch {
            val result = repository.getTopicProgress(userId, topicId)
            result.onSuccess { progress ->
                val currentMap = _topicProgress.value.toMutableMap()
                currentMap[topicId] = progress
                _topicProgress.value = currentMap
            }
        }
    }

    fun loadAllTopicProgress() {
        val userId = _authState.value.user?.uid ?: return

        viewModelScope.launch {
            val progressMap = mutableMapOf<Int, TopicProgress>()
            for (topicId in 1..6) {
                val result = repository.getTopicProgress(userId, topicId)
                result.onSuccess { progress ->
                    progressMap[topicId] = progress
                }
            }
            _topicProgress.value = progressMap
        }
    }

    fun isSetCompleted(topicId: Int, setNumber: Int): Boolean {
        val progress = _topicProgress.value[topicId]
        return progress?.isSetCompleted(setNumber) ?: false
    }

    // ==================== DAILY QUIZ ====================

    fun saveDailyQuizResult(result: QuizResult) {
        viewModelScope.launch {
            val userId = _authState.value.user?.uid ?: return@launch
            Log.d(TAG, "Saving daily quiz: user=$userId")

            val resultWithUserId = result.copy(
                userId = userId,
                quizType = "daily"
            )

            repository.saveQuizResult(resultWithUserId).onSuccess {
                Log.d(TAG, "✅ Daily quiz saved successfully")

                // ✅ FIXED: All updates in one batch - no duplicate calls
                repository.incrementQuizCount(userId)
                updateScoreAndLeaderboard(userId, result.score) // Only calls loadUserProfile ONCE

                loadDailyQuizHistory()
                loadQuizHistory()
            }.onFailure {
                Log.e(TAG, "❌ Failed to save daily quiz", it)
            }
        }
    }

    fun loadDailyQuizHistory() {
        val userId = _authState.value.user?.uid
        if (userId == null) {
            Log.w(TAG, "Cannot load daily quiz history: user not logged in")
            return
        }

        viewModelScope.launch {
            Log.d(TAG, "Loading daily quiz history for user: $userId")
            val result = repository.getDailyQuizHistory(userId, 50)
            result.onSuccess { history ->
                Log.d(TAG, "✅ Daily quiz history loaded: ${history.size} items")
                _dailyQuizHistory.value = history
            }.onFailure {
                Log.e(TAG, "❌ Failed to load daily quiz history", it)
            }
        }
    }

    // ==================== QUIZ HISTORY (ALL) ====================

    fun loadQuizHistory() {
        val userId = _authState.value.user?.uid
        if (userId == null) {
            Log.w(TAG, "Cannot load quiz history: user not logged in")
            return
        }

        viewModelScope.launch {
            Log.d(TAG, "Loading quiz history for user: $userId")
            val result = repository.getUserQuizHistory(userId, 100)
            result.onSuccess { history ->
                Log.d(TAG, "✅ Quiz history loaded: ${history.size} items")
                _quizHistory.value = history
            }.onFailure {
                Log.e(TAG, "❌ Failed to load quiz history", it)
            }
        }
    }

    suspend fun getTopicStats(topicId: Int): TopicStats? {
        val userId = _authState.value.user?.uid ?: return null
        val result = repository.getTopicStatistics(userId, topicId)
        return result.getOrNull()
    }

    // ==================== LEADERBOARD ====================

    fun updateLeaderboard(userId: String, scoreToAdd: Int) {
        viewModelScope.launch {
            repository.updateLeaderboard(userId, scoreToAdd)
            loadLeaderboard()
        }
    }

    fun loadLeaderboard(period: String = "all") {
        viewModelScope.launch {
            val result = repository.getTopLeaderboard(10, period)
            result.onSuccess { entries ->
                _leaderboard.value = entries
            }
        }
    }

    // ==================== CHAT SESSIONS ====================

    fun loadChatSessions() {
        val userId = _authState.value.user?.uid
        if (userId == null) {
            Log.w(TAG, "Cannot load chat sessions: user not logged in")
            return
        }

        viewModelScope.launch {
            Log.d(TAG, "Loading chat sessions for user: $userId")
            val result = repository.getChatSessions(userId)
            result.onSuccess { sessions ->
                Log.d(TAG, "✅ Chat sessions loaded: ${sessions.size} items")
                _chatSessions.value = sessions
            }.onFailure {
                Log.e(TAG, "❌ Failed to load chat sessions", it)
            }
        }
    }

    suspend fun createChatSession(title: String = "Cuộc trò chuyện mới"): ChatSession? {
        val userId = _authState.value.user?.uid ?: return null

        val result = repository.createChatSession(userId, title)
        return result.onSuccess { session ->
            Log.d(TAG, "✅ Chat session created: ${session.sessionId}")
            loadChatSessions()
        }.getOrNull()
    }

    suspend fun saveChatSession(session: ChatSession) {
        val result = repository.saveChatSession(session)
        result.onSuccess {
            Log.d(TAG, "✅ Chat session saved: ${session.sessionId}")
            val updatedSessions = _chatSessions.value.map {
                if (it.sessionId == session.sessionId) session else it
            }
            _chatSessions.value = updatedSessions
        }.onFailure {
            Log.e(TAG, "❌ Failed to save chat session", it)
        }
    }

    suspend fun getChatSession(sessionId: String): ChatSession? {
        val result = repository.getChatSession(sessionId)
        return result.getOrNull()
    }

    suspend fun deleteChatSession(sessionId: String) {
        val result = repository.deleteChatSession(sessionId)
        result.onSuccess {
            Log.d(TAG, "✅ Chat session deleted: $sessionId")
            _chatSessions.value = _chatSessions.value.filter { it.sessionId != sessionId }
        }.onFailure {
            Log.e(TAG, "❌ Failed to delete chat session", it)
        }
    }

    suspend fun deleteAllChatSessions() {
        val userId = _authState.value.user?.uid ?: return

        val result = repository.deleteAllChatSessions(userId)
        result.onSuccess {
            Log.d(TAG, "✅ All chat sessions deleted")
            _chatSessions.value = emptyList()
        }.onFailure {
            Log.e(TAG, "❌ Failed to delete all chat sessions", it)
        }
    }

    suspend fun updateSessionTitle(sessionId: String, newTitle: String) {
        val result = repository.updateSessionTitle(sessionId, newTitle)
        result.onSuccess {
            Log.d(TAG, "✅ Session title updated: $sessionId")
            val updatedSessions = _chatSessions.value.map {
                if (it.sessionId == sessionId) it.copy(title = newTitle) else it
            }
            _chatSessions.value = updatedSessions
        }.onFailure {
            Log.e(TAG, "❌ Failed to update session title", it)
        }
    }

    // ==================== LEGACY CHAT ====================

    @Deprecated("Use Chat Sessions instead")
    fun saveChatHistory(messages: List<ChatMessage>) {
        val userId = _authState.value.user?.uid ?: return

        viewModelScope.launch {
            val chatMessages = messages.map { msg ->
                ChatMessageData(
                    messageId = msg.timestamp.toString(),
                    text = msg.text,
                    isUser = msg.isUser,
                    timestamp = msg.timestamp
                )
            }
            repository.saveChatHistory(userId, chatMessages)
        }
    }

    @Deprecated("Use Chat Sessions instead")
    suspend fun loadChatHistory(): List<ChatMessage> {
        val userId = _authState.value.user?.uid ?: return emptyList()
        val result = repository.getChatHistory(userId)

        return result.getOrNull()?.map { data ->
            ChatMessage(
                text = data.text,
                isUser = data.isUser,
                hasAudio = !data.isUser,
                timestamp = data.timestamp
            )
        } ?: emptyList()
    }

    @Deprecated("Use Chat Sessions instead")
    suspend fun clearChatHistory() {
        val userId = _authState.value.user?.uid ?: return
        repository.clearChatHistory(userId)
    }

    // ==================== UTILITIES ====================

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }

    fun getCurrentUserProfile(): UserProfile? {
        return _authState.value.userProfile
    }

    fun refreshAllData() {
        val userId = _authState.value.user?.uid ?: return
        Log.d(TAG, "Refreshing all data for user: $userId")
        viewModelScope.launch {
            loadUserProfile(userId)
            loadQuizHistory()
            loadDailyQuizHistory()
            loadAllTopicProgress()
            loadLeaderboard()
            loadChatSessions()
        }
    }

    private fun getErrorMessage(exception: Throwable): String {
        return when {
            exception.message?.contains("password") == true ->
                "Mật khẩu phải có ít nhất 6 ký tự"
            exception.message?.contains("email") == true ->
                "Email không hợp lệ"
            exception.message?.contains("user-not-found") == true ->
                "Tài khoản không tồn tại"
            exception.message?.contains("wrong-password") == true ->
                "Mật khẩu không đúng"
            exception.message?.contains("email-already-in-use") == true ->
                "Email đã được sử dụng"
            exception.message?.contains("network") == true ->
                "Lỗi kết nối mạng"
            else -> exception.message ?: "Đã xảy ra lỗi"
        }
    }
}
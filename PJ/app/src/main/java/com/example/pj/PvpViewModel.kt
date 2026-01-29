// File: PvpViewModel.kt - ✅ CRITICAL FIX: Use actual Room ID for observing
package com.example.pj

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private const val TAG = "PvpViewModel"

data class PvpState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentRoom: PvpRoom? = null,
    val currentQuestionIndex: Int = 0,
    val userStats: PvpUserStats? = null,
    val matchHistory: List<PvpMatchResult> = emptyList(),
    val isSearchingMatch: Boolean = false,
    val countdown: Int = 0
)

class PvpViewModel : ViewModel() {

    private val repository = PvpRepository()

    private val _pvpState = MutableStateFlow(PvpState())
    val pvpState: StateFlow<PvpState> = _pvpState.asStateFlow()

    private var roomListenerJob: kotlinx.coroutines.Job? = null

    // ==================== MATCHMAKING ====================

    fun findQuickMatch(
        userId: String,
        username: String,
        displayName: String,
        difficulty: String = "Trung bình",
        questionCount: Int = 5
    ) {
        viewModelScope.launch {
            Log.d(TAG, "=== FIND QUICK MATCH START ===")
            Log.d(TAG, "User ID: $userId")
            Log.d(TAG, "Username: $username")
            Log.d(TAG, "Display Name: $displayName")
            Log.d(TAG, "Difficulty: $difficulty")
            Log.d(TAG, "Questions: $questionCount")

            _pvpState.update { it.copy(
                isSearchingMatch = true,
                isLoading = true,
                error = null
            )}

            val result = repository.findQuickMatch(
                userId = userId,
                username = username,
                displayName = displayName,
                difficulty = difficulty,
                questionCount = questionCount
            )

            result.onSuccess { roomId ->
                Log.d(TAG, "✅ Match found! Room: $roomId")
                _pvpState.update { it.copy(
                    isSearchingMatch = false,
                    isLoading = false
                )}
                observeRoom(roomId)
            }.onFailure { exception ->
                Log.e(TAG, "❌ findQuickMatch failed", exception)
                _pvpState.update { it.copy(
                    isSearchingMatch = false,
                    isLoading = false,
                    error = exception.message ?: "Không tìm thấy trận đấu"
                )}
            }

            Log.d(TAG, "=== FIND QUICK MATCH END ===")
        }
    }

    fun createFriendRoom(
        hostUserId: String,
        hostUsername: String,
        hostDisplayName: String,
        difficulty: String = "Trung bình",
        questionCount: Int = 5
    ) {
        viewModelScope.launch {
            Log.d(TAG, "=== CREATE FRIEND ROOM START ===")
            _pvpState.update { it.copy(isLoading = true, error = null) }

            val result = repository.createRoom(
                hostUserId = hostUserId,
                hostUsername = hostUsername,
                hostDisplayName = hostDisplayName,
                mode = PvpMode.FRIEND_MATCH,
                difficulty = difficulty,
                questionCount = questionCount
            )

            result.onSuccess { roomId ->
                Log.d(TAG, "✅ Room created: $roomId")
                _pvpState.update { it.copy(isLoading = false) }
                observeRoom(roomId)
            }.onFailure { exception ->
                Log.e(TAG, "❌ createFriendRoom failed", exception)
                _pvpState.update { it.copy(
                    isLoading = false,
                    error = exception.message ?: "Không thể tạo phòng"
                )}
            }

            Log.d(TAG, "=== CREATE FRIEND ROOM END ===")
        }
    }

    fun joinFriendRoom(
        roomId: String,
        userId: String,
        username: String,
        displayName: String
    ) {
        viewModelScope.launch {
            Log.d(TAG, "=== JOIN FRIEND ROOM START ===")
            Log.d(TAG, "Room/Code: $roomId")
            Log.d(TAG, "User: $displayName ($userId)")

            _pvpState.update { it.copy(isLoading = true, error = null) }

            val result = repository.joinRoom(
                roomIdOrCode = roomId,
                userId = userId,
                username = username,
                displayName = displayName
            )

            result.onSuccess {
                Log.d(TAG, "✅ Joined room: $roomId")
                _pvpState.update { it.copy(isLoading = false) }

                // ✅ CRITICAL FIX: Get the actual room ID after joining
                // Repository already resolved short code to room ID
                val actualRoomId = repository.getLastJoinedRoomId() ?: roomId
                Log.d(TAG, "🔑 Observing actual room ID: $actualRoomId")
                observeRoom(actualRoomId)
            }.onFailure { exception ->
                Log.e(TAG, "❌ joinFriendRoom failed", exception)
                _pvpState.update { it.copy(
                    isLoading = false,
                    error = exception.message ?: "Không thể vào phòng"
                )}
            }

            Log.d(TAG, "=== JOIN FRIEND ROOM END ===")
        }
    }

    // ==================== ROOM MANAGEMENT ====================

    private fun observeRoom(roomId: String) {
        Log.d(TAG, "👀 === OBSERVE ROOM START ===")
        Log.d(TAG, "   Room ID: $roomId")

        roomListenerJob?.cancel()
        roomListenerJob = viewModelScope.launch {
            repository.observeRoom(roomId)
                .collect { room ->
                    if (room != null) {
                        Log.d(TAG, "🔔 === ROOM STATE UPDATE ===")
                        Log.d(TAG, "   Room ID: ${room.roomId}")
                        Log.d(TAG, "   Status: ${room.status}")
                        Log.d(TAG, "   Mode: ${room.mode}")
                        Log.d(TAG, "   Players: ${room.players.size}/2")
                        Log.d(TAG, "   Questions: ${room.questions.size}")
                        Log.d(TAG, "   Current Question: ${room.currentQuestionIndex}")

                        // Log each player
                        room.players.forEach { (id, player) ->
                            Log.d(TAG, "   👤 Player: ${player.displayName} ($id)")
                            Log.d(TAG, "      Ready: ${player.isReady}")
                            Log.d(TAG, "      Online: ${player.isOnline}")
                            Log.d(TAG, "      Score: ${player.score}")
                            Log.d(TAG, "      Answers: ${player.answers.size}")
                        }

                        // Update state WITHOUT resetting other fields
                        _pvpState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                error = null,
                                currentRoom = room,
                                currentQuestionIndex = room.currentQuestionIndex,
                                isSearchingMatch = false
                                // Keep userStats and matchHistory
                            )
                        }

                        // Handle countdown for STARTING status
                        if (room.status == RoomStatus.STARTING && _pvpState.value.countdown == 0) {
                            Log.d(TAG, "⏰ Starting countdown...")
                            startCountdown()
                        }

                        Log.d(TAG, "🔔 === ROOM STATE UPDATE END ===")
                    } else {
                        Log.w(TAG, "⚠️ Room is NULL")
                        _pvpState.update { it.copy(
                            currentRoom = null,
                            error = "Phòng không tồn tại"
                        )}
                    }
                }
        }

        Log.d(TAG, "👀 === OBSERVE ROOM END ===")
    }

    private fun startCountdown() {
        viewModelScope.launch {
            Log.d(TAG, "⏰ Starting countdown...")
            for (i in 3 downTo 1) {
                Log.d(TAG, "⏰ Countdown: $i")
                _pvpState.update { it.copy(countdown = i) }
                kotlinx.coroutines.delay(1000)
            }
            Log.d(TAG, "⏰ Countdown finished")
            _pvpState.update { it.copy(countdown = 0) }
        }
    }

    fun leaveRoom(roomId: String, userId: String) {
        viewModelScope.launch {
            Log.d(TAG, "🚪 Leave room: $roomId (user: $userId)")
            repository.leaveRoom(roomId, userId)
            roomListenerJob?.cancel()
            _pvpState.update { it.copy(
                currentRoom = null,
                currentQuestionIndex = 0
            )}
            Log.d(TAG, "✅ Left room successfully")
        }
    }

    // ==================== GAMEPLAY ====================

    fun submitAnswer(
        roomId: String,
        userId: String,
        questionIndex: Int,
        selectedAnswer: String,
        timeSpent: Long
    ) {
        viewModelScope.launch {
            Log.d(TAG, "=== SUBMIT ANSWER ===")
            Log.d(TAG, "Room: $roomId")
            Log.d(TAG, "User: $userId")
            Log.d(TAG, "Question: $questionIndex")
            Log.d(TAG, "Answer: $selectedAnswer")
            Log.d(TAG, "Time: ${timeSpent}ms")

            val result = repository.submitAnswer(
                roomId = roomId,
                userId = userId,
                questionIndex = questionIndex,
                selectedAnswer = selectedAnswer,
                timeSpent = timeSpent
            )

            result.onFailure { exception ->
                Log.e(TAG, "❌ submitAnswer failed", exception)
                _pvpState.update { it.copy(
                    error = exception.message ?: "Không thể gửi câu trả lời"
                )}
            }
        }
    }

    fun getCurrentQuestion(): PvpQuestion? {
        val room = _pvpState.value.currentRoom ?: return null
        return room.questions.getOrNull(_pvpState.value.currentQuestionIndex)
    }

    fun hasUserAnsweredCurrentQuestion(userId: String): Boolean {
        val room = _pvpState.value.currentRoom ?: return false
        val player = room.getPlayer(userId) ?: return false
        return player.hasAnswered(_pvpState.value.currentQuestionIndex)
    }

    fun getUserScore(userId: String): Int {
        return _pvpState.value.currentRoom?.getPlayer(userId)?.score ?: 0
    }

    fun getOpponentScore(userId: String): Int {
        return _pvpState.value.currentRoom?.getOpponent(userId)?.score ?: 0
    }

    // ==================== STATS & HISTORY ====================

    fun loadUserStats(userId: String) {
        viewModelScope.launch {
            Log.d(TAG, "📊 Loading stats for: $userId")
            val result = repository.getUserStats(userId)
            result.onSuccess { stats ->
                _pvpState.update { it.copy(userStats = stats) }
                Log.d(TAG, "✅ Stats loaded: ${stats.totalMatches} matches, rating ${stats.rating}")
            }.onFailure { exception ->
                Log.e(TAG, "❌ loadUserStats failed", exception)
            }
        }
    }

    fun loadMatchHistory(userId: String) {
        viewModelScope.launch {
            Log.d(TAG, "📜 Loading match history for: $userId")
            val result = repository.getMatchHistory(userId, 20)
            result.onSuccess { history ->
                _pvpState.update { it.copy(matchHistory = history) }
                Log.d(TAG, "✅ Match history loaded: ${history.size} matches")
            }.onFailure { exception ->
                Log.e(TAG, "❌ loadMatchHistory failed", exception)
            }
        }
    }

    // ==================== UTILITIES ====================

    fun clearError() {
        _pvpState.update { it.copy(error = null) }
    }

    fun resetPvpState() {
        Log.d(TAG, "🔄 Resetting PVP state")
        roomListenerJob?.cancel()
        _pvpState.value = PvpState()
    }

    fun getRoomStatus(): RoomStatus? {
        return _pvpState.value.currentRoom?.status
    }

    fun isHost(userId: String): Boolean {
        return _pvpState.value.currentRoom?.isHost(userId) ?: false
    }

    fun getOpponentInfo(userId: String): PvpPlayer? {
        return _pvpState.value.currentRoom?.getOpponent(userId)
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "🧹 ViewModel cleared")
        roomListenerJob?.cancel()
    }
}
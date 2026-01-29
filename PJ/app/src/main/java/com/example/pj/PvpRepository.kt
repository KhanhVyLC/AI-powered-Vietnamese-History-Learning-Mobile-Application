// File: PvpRepository.kt - ✅ FIXED: Changed imageUrl to imageRes
package com.example.pj

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "PvpRepository"

class PvpRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database by lazy {
        FirebaseDatabase.getInstance(
            "https://vihis-45cc5-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).apply {
            Log.d(TAG, "✅ Firebase Realtime Database initialized")
        }
    }

    private val roomsRef by lazy { database.getReference("pvp_rooms") }
    private val shortCodesRef by lazy { database.getReference("pvp_shortcodes") }
    private val statsRef by lazy { database.getReference("pvp_stats") }
    private val resultsRef by lazy { database.getReference("pvp_results") }

    private var lastJoinedRoomId: String? = null

    fun getLastJoinedRoomId(): String? = lastJoinedRoomId

    // ==================== MATCHMAKING ====================

    suspend fun findQuickMatch(
        userId: String,
        username: String,
        displayName: String,
        difficulty: String,
        questionCount: Int
    ): Result<String> {
        return try {
            Log.d(TAG, "🔍 === FIND QUICK MATCH START ===")
            Log.d(TAG, "   User: $displayName ($userId)")
            Log.d(TAG, "   Difficulty: $difficulty, Questions: $questionCount")

            val currentUser = auth.currentUser
            if (currentUser == null || currentUser.uid != userId) {
                Log.e(TAG, "❌ Auth failed")
                return Result.failure(Exception("Bạn chưa đăng nhập"))
            }

            Log.d(TAG, "✅ Auth OK")

            // Get all rooms
            val snapshot = roomsRef.get().await()

            if (!snapshot.exists()) {
                Log.d(TAG, "📝 No rooms exist, creating first room...")
                val newRoomId = createRoom(
                    hostUserId = userId,
                    hostUsername = username,
                    hostDisplayName = displayName,
                    mode = PvpMode.QUICK_MATCH,
                    difficulty = difficulty,
                    questionCount = questionCount
                ).getOrThrow()
                return Result.success(newRoomId)
            }

            var matchedRoomId: String? = null
            var availableRooms = 0

            // ✅ FIX: Search for available rooms with detailed logging
            for (child in snapshot.children) {
                try {
                    val room = child.getValue(PvpRoom::class.java) ?: continue

                    // Only check QUICK_MATCH rooms
                    if (room.mode != PvpMode.QUICK_MATCH) {
                        continue
                    }

                    Log.d(TAG, "─────────────────────────────────────────")
                    Log.d(TAG, "🔍 Checking room: ${room.roomId}")
                    Log.d(TAG, "   Short Code: ${room.shortCode}")
                    Log.d(TAG, "   Status: ${room.status}")
                    Log.d(TAG, "   Difficulty: ${room.difficulty}")
                    Log.d(TAG, "   Question Count: ${room.questionCount}")
                    Log.d(TAG, "   Players: ${room.players.size}/2")
                    Log.d(TAG, "   Questions Ready: ${room.questions.size}")
                    Log.d(TAG, "   Contains User: ${room.players.containsKey(userId)}")

                    // ✅ CRITICAL: Strict matching conditions
                    val isStatusOk = room.status == RoomStatus.WAITING
                    val isDifficultyMatch = room.difficulty == difficulty
                    val isQuestionCountMatch = room.questionCount == questionCount
                    val hasOnePlayer = room.players.size == 1
                    val notCurrentUser = !room.players.containsKey(userId)
                    val hasQuestions = room.questions.isNotEmpty()

                    Log.d(TAG, "   ✓ Status OK (WAITING): $isStatusOk")
                    Log.d(TAG, "   ✓ Difficulty Match: $isDifficultyMatch")
                    Log.d(TAG, "   ✓ Question Count Match: $isQuestionCountMatch")
                    Log.d(TAG, "   ✓ Has One Player: $hasOnePlayer")
                    Log.d(TAG, "   ✓ Not Current User: $notCurrentUser")
                    Log.d(TAG, "   ✓ Has Questions: $hasQuestions")

                    if (isStatusOk) {
                        availableRooms++
                    }

                    // All conditions must be true
                    if (isStatusOk &&
                        isDifficultyMatch &&
                        isQuestionCountMatch &&
                        hasOnePlayer &&
                        notCurrentUser &&
                        hasQuestions) {

                        matchedRoomId = room.roomId
                        Log.d(TAG, "   ✅ MATCHED! Will join this room")
                        break
                    } else {
                        Log.d(TAG, "   ❌ Not a match")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Error parsing room: ${e.message}")
                    continue
                }
            }

            Log.d(TAG, "─────────────────────────────────────────")
            Log.d(TAG, "📊 Search Summary:")
            Log.d(TAG, "   Total rooms checked: ${snapshot.childrenCount}")
            Log.d(TAG, "   Available WAITING rooms: $availableRooms")
            Log.d(TAG, "   Matched room: ${matchedRoomId ?: "NONE"}")
            Log.d(TAG, "─────────────────────────────────────────")

            if (matchedRoomId != null) {
                Log.d(TAG, "🚪 Joining existing room: $matchedRoomId")
                joinRoom(matchedRoomId, userId, username, displayName).getOrThrow()
                Log.d(TAG, "✅ Successfully joined room")
                Result.success(matchedRoomId)
            } else {
                Log.d(TAG, "📝 No suitable room found, creating new room...")
                val newRoomId = createRoom(
                    hostUserId = userId,
                    hostUsername = username,
                    hostDisplayName = displayName,
                    mode = PvpMode.QUICK_MATCH,
                    difficulty = difficulty,
                    questionCount = questionCount
                ).getOrThrow()
                Log.d(TAG, "✅ Created new room: $newRoomId")
                Result.success(newRoomId)
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ findQuickMatch failed", e)
            Result.failure(e)
        }
    }

    // ==================== ROOM MANAGEMENT ====================

    suspend fun createRoom(
        hostUserId: String,
        hostUsername: String,
        hostDisplayName: String,
        mode: PvpMode,
        difficulty: String,
        questionCount: Int
    ): Result<String> {
        return try {
            Log.d(TAG, "🆕 === CREATE ROOM START ===")

            val currentUser = auth.currentUser
            if (currentUser == null || currentUser.uid != hostUserId) {
                Log.e(TAG, "❌ Auth failed in createRoom")
                return Result.failure(Exception("Bạn chưa đăng nhập"))
            }

            val roomId = roomsRef.push().key ?: throw Exception("Failed to generate room ID")
            val shortCode = generateShortCode()

            Log.d(TAG, "   Room ID: $roomId")
            Log.d(TAG, "   Short Code: $shortCode")
            Log.d(TAG, "   Host: $hostDisplayName ($hostUserId)")
            Log.d(TAG, "   Mode: $mode")
            Log.d(TAG, "   Difficulty: $difficulty")
            Log.d(TAG, "   Question Count: $questionCount")

            val questions = try {
                Log.d(TAG, "🤖 Generating questions...")
                generateQuestionsFromGemini(questionCount, difficulty)
            } catch (e: Exception) {
                Log.e(TAG, "⚠️ Gemini failed: ${e.message}")
                getFallbackQuestions(questionCount, difficulty)
            }

            Log.d(TAG, "✅ Generated ${questions.size} questions")

            val hostPlayer = PvpPlayer(
                userId = hostUserId,
                username = hostUsername,
                displayName = hostDisplayName,
                isReady = true,
                isOnline = true,
                lastSeen = System.currentTimeMillis()
            )

            val room = PvpRoom(
                roomId = roomId,
                shortCode = shortCode,
                mode = mode,
                difficulty = difficulty,
                questionCount = questionCount,
                status = RoomStatus.WAITING,
                hostUserId = hostUserId,
                players = mapOf(hostUserId to hostPlayer),
                questions = questions,
                currentQuestionIndex = 0,
                startTime = 0,
                endTime = 0,
                createdAt = System.currentTimeMillis()
            )

            Log.d(TAG, "💾 Saving room to Firebase...")
            roomsRef.child(roomId).setValue(room).await()
            shortCodesRef.child(shortCode).setValue(roomId).await()

            // ✅ Wait for Firebase to sync
            Log.d(TAG, "⏳ Waiting for sync...")
            kotlinx.coroutines.delay(800)

            // Verify room was saved
            val verifySnapshot = roomsRef.child(roomId).get().await()
            if (!verifySnapshot.exists()) {
                throw Exception("Room not saved to Firebase")
            }

            val verifyRoom = verifySnapshot.getValue(PvpRoom::class.java)
            Log.d(TAG, "✅ Room verified:")
            Log.d(TAG, "   Status: ${verifyRoom?.status}")
            Log.d(TAG, "   Players: ${verifyRoom?.players?.size}")
            Log.d(TAG, "   Questions: ${verifyRoom?.questions?.size}")

            Log.d(TAG, "✅ === CREATE ROOM SUCCESS ===")
            Result.success(roomId)

        } catch (e: Exception) {
            Log.e(TAG, "❌ createRoom failed", e)
            Result.failure(e)
        }
    }

    private fun generateShortCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    suspend fun joinRoom(
        roomIdOrCode: String,
        userId: String,
        username: String,
        displayName: String
    ): Result<Unit> {
        return try {
            Log.d(TAG, "🚪 === JOIN ROOM START ===")
            Log.d(TAG, "   Room/Code: $roomIdOrCode")
            Log.d(TAG, "   User: $displayName ($userId)")

            val currentUser = auth.currentUser
            if (currentUser == null || currentUser.uid != userId) {
                Log.e(TAG, "❌ Auth failed in joinRoom")
                return Result.failure(Exception("Bạn chưa đăng nhập"))
            }

            Log.d(TAG, "✅ Auth OK")

            // Resolve room ID from short code if needed
            val actualRoomId = if (roomIdOrCode.length == 6) {
                Log.d(TAG, "🔍 Resolving short code: $roomIdOrCode")
                val snapshot = shortCodesRef.child(roomIdOrCode.uppercase()).get().await()
                if (!snapshot.exists()) {
                    Log.e(TAG, "❌ Short code not found")
                    return Result.failure(Exception("Mã phòng không đúng"))
                }
                val resolved = snapshot.getValue(String::class.java) ?: roomIdOrCode
                Log.d(TAG, "✅ Resolved to room ID: $resolved")
                resolved
            } else {
                Log.d(TAG, "📍 Using direct room ID")
                roomIdOrCode
            }

            lastJoinedRoomId = actualRoomId
            Log.d(TAG, "💾 Stored last joined room ID: $actualRoomId")

            // Get room data
            Log.d(TAG, "📖 Reading room data...")
            val snapshot = roomsRef.child(actualRoomId).get().await()

            if (!snapshot.exists()) {
                Log.e(TAG, "❌ Room not found in database")
                return Result.failure(Exception("Phòng không tồn tại"))
            }

            val room = snapshot.getValue(PvpRoom::class.java)
            if (room == null) {
                Log.e(TAG, "❌ Failed to parse room data")
                return Result.failure(Exception("Lỗi đọc dữ liệu phòng"))
            }

            Log.d(TAG, "✅ Room loaded:")
            Log.d(TAG, "   Room ID: ${room.roomId}")
            Log.d(TAG, "   Short Code: ${room.shortCode}")
            Log.d(TAG, "   Status: ${room.status}")
            Log.d(TAG, "   Mode: ${room.mode}")
            Log.d(TAG, "   Players: ${room.players.size}/2")
            Log.d(TAG, "   Questions: ${room.questions.size}")
            Log.d(TAG, "   Host: ${room.hostUserId}")

            // Validate room status
            if (room.status != RoomStatus.WAITING && room.status != RoomStatus.STARTING) {
                Log.e(TAG, "❌ Room status invalid: ${room.status}")
                return Result.failure(Exception("Phòng đã bắt đầu hoặc kết thúc"))
            }

            // Check if room is full
            if (room.players.size >= 2) {
                Log.e(TAG, "❌ Room is full (${room.players.size}/2)")
                return Result.failure(Exception("Phòng đã đủ người"))
            }

            // Check if user already in room
            if (room.players.containsKey(userId)) {
                Log.w(TAG, "⚠️ User already in room")
                return Result.success(Unit)
            }

            // Create player object
            val player = PvpPlayer(
                userId = userId,
                username = username,
                displayName = displayName,
                isReady = true,
                isOnline = true,
                lastSeen = System.currentTimeMillis()
            )

            Log.d(TAG, "👤 Adding player to room...")
            roomsRef.child(actualRoomId)
                .child("players")
                .child(userId)
                .setValue(player)
                .await()

            // Wait for sync
            Log.d(TAG, "⏳ Waiting for player add sync...")
            kotlinx.coroutines.delay(500)

            // Verify player was added
            val verifyPlayerSnapshot = roomsRef.child(actualRoomId)
                .child("players")
                .child(userId)
                .get()
                .await()

            if (!verifyPlayerSnapshot.exists()) {
                Log.e(TAG, "❌ Player not added to Firebase")
                throw Exception("Không thể thêm người chơi vào phòng")
            }

            Log.d(TAG, "✅ Player added successfully")

            // Get updated room to check player count
            val updatedSnapshot = roomsRef.child(actualRoomId).get().await()
            val updatedRoom = updatedSnapshot.getValue(PvpRoom::class.java)

            if (updatedRoom != null) {
                Log.d(TAG, "📊 Updated room state:")
                Log.d(TAG, "   Status: ${updatedRoom.status}")
                Log.d(TAG, "   Players: ${updatedRoom.players.size}/2")

                updatedRoom.players.forEach { (id, p) ->
                    Log.d(TAG, "   Player: ${p.displayName} ($id)")
                }

                // If room now has 2 players and still WAITING, start the match
                if (updatedRoom.status == RoomStatus.WAITING && updatedRoom.players.size >= 2) {
                    Log.d(TAG, "🎮 Room full (${updatedRoom.players.size}/2), initiating match start...")
                    startMatch(actualRoomId)
                } else {
                    Log.d(TAG, "⏳ Waiting for more players (${updatedRoom.players.size}/2)")
                }
            } else {
                Log.w(TAG, "⚠️ Could not read updated room state")
            }

            Log.d(TAG, "✅ === JOIN ROOM SUCCESS ===")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ === JOIN ROOM FAILED ===", e)
            Result.failure(e)
        }
    }

    suspend fun leaveRoom(roomId: String, userId: String): Result<Unit> {
        return try {
            Log.d(TAG, "🚪 === LEAVE ROOM START ===")
            Log.d(TAG, "   Room: $roomId")
            Log.d(TAG, "   User: $userId")

            val snapshot = roomsRef.child(roomId).get().await()
            if (!snapshot.exists()) {
                Log.d(TAG, "⚠️ Room doesn't exist, nothing to leave")
                return Result.success(Unit)
            }

            val room = snapshot.getValue(PvpRoom::class.java)

            // Remove player
            roomsRef.child(roomId)
                .child("players")
                .child(userId)
                .removeValue()
                .await()

            Log.d(TAG, "✅ Player removed from room")

            if (room != null) {
                // If last player left, delete room
                if (room.players.size <= 1) {
                    roomsRef.child(roomId).removeValue().await()
                    if (room.shortCode.isNotEmpty()) {
                        shortCodesRef.child(room.shortCode).removeValue().await()
                    }
                    Log.d(TAG, "🗑️ Room deleted (last player left)")
                }
                // If match in progress/starting, cancel it
                else if (room.status == RoomStatus.IN_PROGRESS || room.status == RoomStatus.STARTING) {
                    roomsRef.child(roomId)
                        .child("status")
                        .setValue(RoomStatus.CANCELLED.name)
                        .await()
                    Log.d(TAG, "❌ Match cancelled (player left during match)")
                }
            }

            Log.d(TAG, "✅ === LEAVE ROOM SUCCESS ===")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ leaveRoom failed", e)
            Result.failure(e)
        }
    }

    private suspend fun startMatch(roomId: String) {
        try {
            Log.d(TAG, "🎮 === START MATCH BEGIN ===")
            Log.d(TAG, "   Room: $roomId")

            val snapshot = roomsRef.child(roomId).get().await()
            val room = snapshot.getValue(PvpRoom::class.java)

            if (room == null) {
                Log.e(TAG, "❌ Room not found")
                return
            }

            Log.d(TAG, "   Current Status: ${room.status}")
            Log.d(TAG, "   Players: ${room.players.size}/2")

            // Don't start if already starting or in progress
            if (room.status == RoomStatus.STARTING || room.status == RoomStatus.IN_PROGRESS) {
                Log.d(TAG, "⚠️ Match already starting/in progress")
                return
            }

            // Need exactly 2 players
            if (room.players.size < 2) {
                Log.e(TAG, "❌ Not enough players (${room.players.size}/2)")
                return
            }

            // Step 1: Set status to STARTING
            Log.d(TAG, "⏰ Step 1: Setting status to STARTING...")
            roomsRef.child(roomId).updateChildren(
                mapOf(
                    "status" to RoomStatus.STARTING.name,
                    "startTime" to ServerValue.TIMESTAMP
                )
            ).await()

            // Step 2: Wait for countdown (3 seconds)
            Log.d(TAG, "⏰ Step 2: Countdown 3 seconds...")
            kotlinx.coroutines.delay(3000)

            // Step 3: Verify room state before starting
            Log.d(TAG, "⏰ Step 3: Verifying room state...")
            val verifySnapshot = roomsRef.child(roomId).get().await()
            val verifyRoom = verifySnapshot.getValue(PvpRoom::class.java)

            if (verifyRoom == null) {
                Log.e(TAG, "❌ Room disappeared during countdown")
                return
            }

            // Check if still have 2 players
            if (verifyRoom.players.size < 2) {
                Log.w(TAG, "⚠️ Player left during countdown (${verifyRoom.players.size}/2)")
                Log.w(TAG, "⚠️ Reverting to WAITING status")
                roomsRef.child(roomId)
                    .child("status")
                    .setValue(RoomStatus.WAITING.name)
                    .await()
                return
            }

            // Step 4: Start the match (set to IN_PROGRESS)
            Log.d(TAG, "✅ Step 4: Setting status to IN_PROGRESS...")
            roomsRef.child(roomId)
                .child("status")
                .setValue(RoomStatus.IN_PROGRESS.name)
                .await()

            Log.d(TAG, "🎮 === START MATCH COMPLETE ===")

        } catch (e: Exception) {
            Log.e(TAG, "❌ startMatch failed", e)
            try {
                // Try to revert to WAITING on error
                roomsRef.child(roomId)
                    .child("status")
                    .setValue(RoomStatus.WAITING.name)
                    .await()
                Log.d(TAG, "↩️ Reverted to WAITING due to error")
            } catch (revertError: Exception) {
                Log.e(TAG, "❌ Failed to revert status", revertError)
            }
        }
    }

    // ==================== GAMEPLAY ====================

    suspend fun submitAnswer(
        roomId: String,
        userId: String,
        questionIndex: Int,
        selectedAnswer: String,
        timeSpent: Long
    ): Result<Unit> {
        return try {
            Log.d(TAG, "📝 === SUBMIT ANSWER START ===")
            Log.d(TAG, "   Room: $roomId")
            Log.d(TAG, "   User: $userId")
            Log.d(TAG, "   Question Index: $questionIndex")
            Log.d(TAG, "   Selected Answer: $selectedAnswer")
            Log.d(TAG, "   Time Spent: ${timeSpent}ms")

            val snapshot = roomsRef.child(roomId).get().await()
            if (!snapshot.exists()) {
                return Result.failure(Exception("Phòng không tồn tại"))
            }

            val room = snapshot.getValue(PvpRoom::class.java)
                ?: return Result.failure(Exception("Lỗi đọc dữ liệu phòng"))

            if (questionIndex >= room.questions.size) {
                return Result.failure(Exception("Câu hỏi không hợp lệ"))
            }

            val question = room.questions[questionIndex]
            val isCorrect = selectedAnswer == question.correctAnswer

            Log.d(TAG, "   Correct Answer: ${question.correctAnswer}")
            Log.d(TAG, "   Is Correct: $isCorrect")

            val answer = PvpAnswer(
                questionIndex = questionIndex,
                selectedAnswer = selectedAnswer,
                isCorrect = isCorrect,
                timeSpent = timeSpent,
                timestamp = System.currentTimeMillis()
            )

            val playerRef = roomsRef.child(roomId).child("players").child(userId)
            val playerSnapshot = playerRef.get().await()
            val player = playerSnapshot.getValue(PvpPlayer::class.java)
                ?: return Result.failure(Exception("Người chơi không tồn tại"))

            val updatedAnswers = player.answers.toMutableList()
            updatedAnswers.add(answer)

            playerRef.child("answers").setValue(updatedAnswers).await()

            // ✅ NEW SCORING: Points = seconds left (max 20 seconds)
            if (isCorrect) {
                val timeLeftMs = 20000 - timeSpent // 20 seconds = 20000ms
                val scoreIncrement = if (timeLeftMs > 0) {
                    (timeLeftMs / 1000).toInt() // Score = seconds left
                } else {
                    0 // Time's up = 0 points even if correct
                }

                playerRef.updateChildren(
                    mapOf(
                        "score" to (player.score + scoreIncrement),
                        "correctAnswers" to (player.correctAnswers + 1)
                    )
                ).await()

                Log.d(TAG, "✅ Correct! +$scoreIncrement points")
                Log.d(TAG, "   Time left: ${timeLeftMs}ms")
                Log.d(TAG, "   New score: ${player.score + scoreIncrement}")
            } else {
                Log.d(TAG, "❌ Wrong answer, 0 points")
            }

            // Check if both players answered
            checkIfQuestionCompleted(roomId, questionIndex)

            Log.d(TAG, "✅ === SUBMIT ANSWER SUCCESS ===")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ submitAnswer failed", e)
            Result.failure(e)
        }
    }

    private suspend fun checkIfQuestionCompleted(roomId: String, questionIndex: Int) {
        try {
            Log.d(TAG, "🔍 Checking if question $questionIndex completed...")

            val snapshot = roomsRef.child(roomId).get().await()
            val room = snapshot.getValue(PvpRoom::class.java) ?: return

            val allAnswered = room.players.values.all { it.hasAnswered(questionIndex) }

            Log.d(TAG, "   Players answered:")
            room.players.forEach { (id, player) ->
                Log.d(TAG, "   - ${player.displayName}: ${if (player.hasAnswered(questionIndex)) "YES" else "NO"}")
            }
            Log.d(TAG, "   All answered: $allAnswered")

            if (allAnswered) {
                val nextIndex = questionIndex + 1
                if (nextIndex < room.questionCount) {
                    Log.d(TAG, "➡️ Moving to next question: ${nextIndex + 1}/${room.questionCount}")
                    roomsRef.child(roomId)
                        .child("currentQuestionIndex")
                        .setValue(nextIndex)
                        .await()
                } else {
                    Log.d(TAG, "🏁 All questions completed, finishing match...")
                    finishMatch(roomId)
                }
            } else {
                Log.d(TAG, "⏳ Waiting for other player(s) to answer...")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ checkIfQuestionCompleted failed", e)
        }
    }

    private suspend fun finishMatch(roomId: String) {
        try {
            Log.d(TAG, "🏁 === FINISH MATCH START ===")
            Log.d(TAG, "   Room: $roomId")

            roomsRef.child(roomId).updateChildren(
                mapOf(
                    "status" to RoomStatus.FINISHED.name,
                    "endTime" to ServerValue.TIMESTAMP
                )
            ).await()

            Log.d(TAG, "✅ Status set to FINISHED")

            saveMatchResult(roomId)

            Log.d(TAG, "✅ === FINISH MATCH SUCCESS ===")

        } catch (e: Exception) {
            Log.e(TAG, "❌ finishMatch failed", e)
        }
    }

    private suspend fun saveMatchResult(roomId: String) {
        try {
            Log.d(TAG, "💾 === SAVE MATCH RESULT START ===")

            val snapshot = roomsRef.child(roomId).get().await()
            val room = snapshot.getValue(PvpRoom::class.java) ?: return

            val sortedPlayers = room.players.values.sortedByDescending { it.score }
            val winner = sortedPlayers.firstOrNull()
            val loser = sortedPlayers.getOrNull(1)
            val isDraw = winner?.score == loser?.score

            Log.d(TAG, "   Final Scores:")
            sortedPlayers.forEach { player ->
                Log.d(TAG, "   - ${player.displayName}: ${player.score} pts (${player.correctAnswers}/${room.questionCount})")
            }
            Log.d(TAG, "   Result: ${if (isDraw) "DRAW" else "${winner?.displayName} WINS"}")

            val playerResults = sortedPlayers.mapIndexed { index, player ->
                player.userId to PvpPlayerResult(
                    userId = player.userId,
                    username = player.username,
                    score = player.score,
                    correctAnswers = player.correctAnswers,
                    totalQuestions = room.questionCount,
                    accuracy = if (room.questionCount > 0) {
                        (player.correctAnswers.toDouble() / room.questionCount) * 100
                    } else 0.0,
                    rank = index + 1,
                    ratingChange = if (index == 0 && !isDraw) +10 else if (isDraw) 0 else -5
                )
            }.toMap()

            val result = PvpMatchResult(
                resultId = resultsRef.push().key ?: "",
                roomId = roomId,
                winnerId = if (!isDraw) winner?.userId ?: "" else "",
                loserId = if (!isDraw) loser?.userId ?: "" else "",
                isDraw = isDraw,
                players = playerResults,
                questionCount = room.questionCount,
                difficulty = room.difficulty,
                startTime = room.startTime,
                endTime = System.currentTimeMillis(),
                duration = System.currentTimeMillis() - room.startTime
            )

            resultsRef.child(result.resultId).setValue(result).await()

            Log.d(TAG, "✅ Match result saved")

            // Update player stats
            playerResults.forEach { (userId, playerResult) ->
                updatePlayerStats(userId, playerResult)
            }

            Log.d(TAG, "✅ === SAVE MATCH RESULT SUCCESS ===")

        } catch (e: Exception) {
            Log.e(TAG, "❌ saveMatchResult failed", e)
        }
    }

    private suspend fun updatePlayerStats(userId: String, result: PvpPlayerResult) {
        try {
            Log.d(TAG, "📊 Updating stats for: $userId")

            val snapshot = statsRef.child(userId).get().await()
            val stats = snapshot.getValue(PvpUserStats::class.java)
                ?: PvpUserStats(userId = userId)

            val isWin = result.rank == 1 && result.ratingChange > 0
            val isLoss = result.rank == 2 && result.ratingChange < 0
            val isDraw = result.ratingChange == 0

            val newStreak = if (isWin) stats.currentStreak + 1 else 0

            val updatedStats = stats.copy(
                totalMatches = stats.totalMatches + 1,
                wins = stats.wins + if (isWin) 1 else 0,
                losses = stats.losses + if (isLoss) 1 else 0,
                draws = stats.draws + if (isDraw) 1 else 0,
                currentStreak = newStreak,
                bestStreak = maxOf(stats.bestStreak, newStreak),
                rating = stats.rating + result.ratingChange,
                totalScore = stats.totalScore + result.score,
                averageAccuracy = if (stats.totalMatches > 0) {
                    ((stats.averageAccuracy * stats.totalMatches) + result.accuracy) / (stats.totalMatches + 1)
                } else result.accuracy,
                lastPlayedAt = System.currentTimeMillis()
            )

            statsRef.child(userId).setValue(updatedStats).await()

            Log.d(TAG, "✅ Stats updated:")
            Log.d(TAG, "   Total Matches: ${updatedStats.totalMatches}")
            Log.d(TAG, "   W/L/D: ${updatedStats.wins}/${updatedStats.losses}/${updatedStats.draws}")
            Log.d(TAG, "   Rating: ${updatedStats.rating} (${if (result.ratingChange >= 0) "+" else ""}${result.ratingChange})")

        } catch (e: Exception) {
            Log.e(TAG, "❌ updatePlayerStats failed", e)
        }
    }

    // ==================== REALTIME ====================

    fun observeRoom(roomId: String): Flow<PvpRoom?> = callbackFlow {
        Log.d(TAG, "👀 Starting to observe room: $roomId")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (!snapshot.exists()) {
                        Log.w(TAG, "⚠️ Room no longer exists")
                        trySend(null)
                        return
                    }

                    val room = snapshot.getValue(PvpRoom::class.java)
                    if (room == null) {
                        Log.e(TAG, "❌ Failed to parse room data")
                        trySend(null)
                    } else {
                        Log.d(TAG, "📡 Room update received:")
                        Log.d(TAG, "   Status: ${room.status}")
                        Log.d(TAG, "   Players: ${room.players.size}")
                        Log.d(TAG, "   Current Question: ${room.currentQuestionIndex}")
                        trySend(room)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ observeRoom onDataChange error", e)
                    trySend(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "❌ observeRoom cancelled", error.toException())
                trySend(null)
            }
        }

        roomsRef.child(roomId).addValueEventListener(listener)

        awaitClose {
            Log.d(TAG, "🛑 Stopping observation of room: $roomId")
            roomsRef.child(roomId).removeEventListener(listener)
        }
    }

    suspend fun getUserStats(userId: String): Result<PvpUserStats> {
        return try {
            Log.d(TAG, "📊 Loading stats for: $userId")
            val snapshot = statsRef.child(userId).get().await()
            val stats = snapshot.getValue(PvpUserStats::class.java)
                ?: PvpUserStats(userId = userId)
            Log.d(TAG, "✅ Stats loaded: ${stats.totalMatches} matches, rating ${stats.rating}")
            Result.success(stats)
        } catch (e: Exception) {
            Log.e(TAG, "❌ getUserStats failed", e)
            Result.failure(e)
        }
    }

    suspend fun getMatchHistory(userId: String, limit: Int = 20): Result<List<PvpMatchResult>> {
        return try {
            Log.d(TAG, "📜 Loading match history for: $userId (limit: $limit)")
            val snapshot = resultsRef.get().await()

            val results = snapshot.children
                .mapNotNull { it.getValue(PvpMatchResult::class.java) }
                .filter { it.players.containsKey(userId) }
                .sortedByDescending { it.createdAt }
                .take(limit)

            Log.d(TAG, "✅ Match history loaded: ${results.size} matches")
            Result.success(results)

        } catch (e: Exception) {
            Log.e(TAG, "❌ getMatchHistory failed", e)
            Result.failure(e)
        }
    }

    // ==================== UTILITIES ====================

    /**
     * ✅ NEW: Get random daily image URL (daily1 to daily5)
     */
    private fun getRandomDailyImageUrl(): String {
        val randomNum = (1..5).random()
        return "android.resource://com.example.pj/drawable/daily$randomNum"
    }

    private suspend fun generateQuestionsFromGemini(
        count: Int,
        difficulty: String
    ): List<PvpQuestion> {
        return try {
            Log.d(TAG, "🤖 Generating questions from DailyQuizData...")
            val allQuestions = DailyQuizData.getAllQuestions(difficulty)

            if (allQuestions.isEmpty()) {
                Log.w(TAG, "⚠️ No questions found, using fallback")
                return getFallbackQuestions(count, difficulty)
            }

            val questions = allQuestions
                .shuffled()
                .take(count)
                .map { q ->
                    PvpQuestion(
                        questionId = q.id,
                        question = q.question,
                        answers = q.answers,
                        correctAnswer = q.correctAnswer,
                        explanation = q.explanation,
                        imageUrl = getRandomDailyImageUrl() // ✅ FIXED: Random daily image
                    )
                }

            Log.d(TAG, "✅ Generated ${questions.size} questions")
            questions

        } catch (e: Exception) {
            Log.e(TAG, "❌ Question generation error: ${e.message}")
            getFallbackQuestions(count, difficulty)
        }
    }

    private fun getFallbackQuestions(count: Int, difficulty: String): List<PvpQuestion> {
        Log.d(TAG, "🔄 Using fallback questions")
        return List(count) { index ->
            PvpQuestion(
                questionId = "fallback_${System.currentTimeMillis()}_$index",
                question = "Sự kiện nào đánh dấu mốc quan trọng trong lịch sử Việt Nam? (Câu ${index + 1})",
                answers = listOf(
                    "Khởi nghĩa Hai Bà Trưng (40 - 43)",
                    "Chiến thắng Bạch Đằng (938)",
                    "Chiến thắng Điện Biên Phủ (1954)",
                    "Giải phóng miền Nam (1975)"
                ),
                correctAnswer = "Chiến thắng Điện Biên Phủ (1954)",
                explanation = "Chiến thắng Điện Biên Phủ (1954) là chiến thắng lịch sử chấm dứt ách thống trị của thực dân Pháp ở Đông Dương.",
                imageUrl = getRandomDailyImageUrl() // ✅ FIXED: Random daily image for fallback too
            )
        }
    }
}
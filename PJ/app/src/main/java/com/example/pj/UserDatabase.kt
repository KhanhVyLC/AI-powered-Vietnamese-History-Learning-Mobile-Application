//File UserDatabase.kt - THÊM MỚI (Quản lý user data)
package com.example.pj

data class User(
    val username: String,
    val password: String,
    val score: Int = 0,
    val quizzesCompleted: Int = 0
)

object UserDatabase {
    // Danh sách user mẫu (trong thực tế bạn sẽ dùng Room Database hoặc Firebase)
    private val users = mutableListOf(
        User("admin", "admin123", 12500, 18),
        User("user1", "pass123", 8500, 12),
        User("demo", "demo", 5000, 8)
    )

    fun authenticateUser(username: String, password: String): User? {
        return users.find {
            it.username == username && it.password == password
        }
    }

    fun registerUser(username: String, password: String): Boolean {
        if (users.any { it.username == username }) {
            return false // Username đã tồn tại
        }
        users.add(User(username, password, 0, 0))
        return true
    }

    fun getUserByUsername(username: String): User? {
        return users.find { it.username == username }
    }

    fun updateUserScore(username: String, newScore: Int) {
        users.find { it.username == username }?.let { user ->
            val index = users.indexOf(user)
            users[index] = user.copy(score = newScore)
        }
    }

    fun updateUserQuizCount(username: String) {
        users.find { it.username == username }?.let { user ->
            val index = users.indexOf(user)
            users[index] = user.copy(quizzesCompleted = user.quizzesCompleted + 1)
        }
    }
}

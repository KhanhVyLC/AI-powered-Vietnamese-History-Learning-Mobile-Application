package com.example.pj

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import okhttp3.OkHttpClient
import okhttp3.Protocol
import java.util.concurrent.TimeUnit
import retrofit2.http.Multipart
import retrofit2.http.Part
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody


private const val TAG = "LocalAIViewModel"

// ==================== API MODELS ====================

data class ChatRequest(
    val message: String,
    val language: String = "vi"
)

data class ChatResponse(
    val reply: String? = null,
    val error: String? = null,
    val model: String? = null,
    val language: String? = null,
    val language_name: String? = null,
    val searched: Boolean? = false,
    val search_results: List<String>? = null
)

data class HealthResponse(
    val status: String,
    val ollama: String? = null,
    val model: String? = null
)

// ==================== API INTERFACE ====================

interface LocalAIApi {
    @POST("/chat")
    suspend fun sendMessage(@Body request: ChatRequest): retrofit2.Response<ChatResponse>

    @Multipart
    @POST("/chat")
    suspend fun sendMessageWithImage(
        @Part("message") message: okhttp3.RequestBody,
        @Part("language") language: okhttp3.RequestBody,
        @Part image: okhttp3.MultipartBody.Part
    ): retrofit2.Response<ChatResponse>

    @GET("/health")
    suspend fun healthCheck(): HealthResponse
}

// ==================== UI STATE ====================

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val hasAudio: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null,
    val searchResults: List<String>? = null
)

//Chị thay cái BASE_URL = http://<IP_máy_PC>:5000 là ip máy chạy server
object RetrofitClient {
    private const val BASE_URL = "http://172.30.95.149:5000"

    private val okHttpClient = OkHttpClient.Builder()
        .protocols(listOf(Protocol.HTTP_1_1))
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)

        .retryOnConnectionFailure(true)

        .build()

    val api: LocalAIApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LocalAIApi::class.java)
    }
}


class GeminiViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val api = RetrofitClient.api
    fun addLocalMessage(message: ChatMessage) {
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + message
        )
    }
    fun sendMessage(
        userMessage: String,
        difficultyLevel: String = "THCS",
        language: String = "vi",
        maxRetries: Int = 3
    ) {
        if (userMessage.isBlank()) {
            Log.w(TAG, "⚠️ Empty message, ignoring")
            return
        }

        val currentMessages = _uiState.value.messages.toMutableList()
        currentMessages.add(
            ChatMessage(
                text = userMessage,
                isUser = true,
                hasAudio = false
            )
        )

        _uiState.value = _uiState.value.copy(
            messages = currentMessages,
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            var retryCount = 0
            var success = false

            while (retryCount < maxRetries && !success) {
                try {
                    Log.d(TAG, "📤 Sending message (attempt ${retryCount + 1}/$maxRetries)")

                    if (retryCount == 0) {
                        try {
                            val healthCheck = api.healthCheck()
                            Log.d(TAG, "✅ Server health: ${healthCheck.status}")
                        } catch (e: Exception) {
                            Log.w(TAG, "⚠️ Health check failed: ${e.message}")
                        }
                    }

                    // 🔹 GỌI API
                    val httpResponse = api.sendMessage(
                        ChatRequest(
                            message = userMessage,
                            language = language
                        )
                    )

                    if (httpResponse.isSuccessful) {
                        val body = httpResponse.body()

                        // Nếu server trả reply → dùng reply, không thì thử dùng error, cuối cùng fallback msg mặc định
                        val botMessage = body?.reply
                            ?: body?.error
                            ?: when (language) {
                                "en" -> "No reply from server."
                                "zh" -> "服务器没有返回回复。"
                                else -> "Server không trả lời."
                            }

                        // 🔹 Thêm message bot vào UI
                        val updatedMessages = _uiState.value.messages.toMutableList()
                        updatedMessages.add(
                            ChatMessage(
                                text = botMessage,
                                isUser = false,
                                hasAudio = true,
                                searchResults = body?.search_results
                            )
                        )

                        _uiState.value = _uiState.value.copy(
                            messages = updatedMessages,
                            isLoading = false,
                            error = null
                        )

                        success = true
                        Log.d(TAG, "✅ Message sent successfully (searched: ${body?.searched})")
                    } else {
                        val errText = httpResponse.errorBody()?.string()
                            ?: "HTTP ${httpResponse.code()}"

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "❌ Server error: $errText"
                        )
                        break
                    }

                } catch (e: Exception) {
                    retryCount++
                    Log.e(TAG, "❌ Error (attempt $retryCount): ${e.message}")

                    val isConnectionError = e.message?.contains("Failed to connect") == true ||
                            e.message?.contains("timeout") == true ||
                            e.message?.contains("ECONNREFUSED") == true

                    if (isConnectionError && retryCount < maxRetries) {
                        Log.w(TAG, "⏳ Connection error, retrying in 2s...")

                        _uiState.value = _uiState.value.copy(
                            error = when (language) {
                                "en" -> "Connection error, retrying... ($retryCount/$maxRetries)"
                                "zh" -> "连接错误，重试中... ($retryCount/$maxRetries)"
                                else -> "Lỗi kết nối, đang thử lại... ($retryCount/$maxRetries)"
                            }
                        )

                        delay(2000)
                    } else {
                        val errorMsg = when {
                            isConnectionError -> when (language) {
                                "en" -> "❌ Cannot connect to server. Check:\n1. Server running?\n2. Same WiFi?\n3. Correct IP?"
                                "zh" -> "❌ 无法连接到服务器。检查：\n1. 服务器运行？\n2. 同一WiFi？\n3. IP正确？"
                                else -> "❌ Không kết nối được server. Kiểm tra:\n1. Server đang chạy?\n2. Cùng WiFi?\n3. IP đúng?"
                            }
                            e.message?.contains("timeout") == true -> when (language) {
                                "en" -> "❌ Request timeout. Server responding too slow."
                                "zh" -> "❌ 请求超时。服务器响应太慢。"
                                else -> "❌ Timeout. Server phản hồi quá lâu."
                            }
                            else -> when (language) {
                                "en" -> "❌ Error: ${e.message}"
                                "zh" -> "❌ 错误：${e.message}"
                                else -> "❌ Lỗi: ${e.message}"
                            }
                        }

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = errorMsg
                        )
                        break
                    }
                }
            }
        }
    }


    fun loadSession(sessionId: String, messages: List<ChatMessage>) {
        Log.d(TAG, "📂 Loading session: $sessionId with ${messages.size} messages")
        _uiState.value = ChatUiState(messages = messages)
    }


    fun createNewSession() {
        Log.d(TAG, "🆕 Creating new session")
        _uiState.value = ChatUiState()
    }

    fun restoreMessage(message: ChatMessage) {
        val currentMessages = _uiState.value.messages.toMutableList()
        currentMessages.add(message)
        _uiState.value = _uiState.value.copy(messages = currentMessages)
    }

    fun generateQuiz(
        topic: String,
        numberOfQuestions: Int = 5,
        difficulty: String = "THCS",
        language: String = "vi"
    ) {
        val prompt = when (language) {
            "en" -> """
                Create $numberOfQuestions multiple choice questions about: "$topic"
                Level: $difficulty
                
                FORMAT:
                Question 1: [Content]
                A. [Answer A]
                B. [Answer B]
                C. [Answer C]
                D. [Answer D]
                ✅ Correct: [A/B/C/D]
                💡 Explanation: [Brief explanation]
            """.trimIndent()

            "zh" -> """
                创建 $numberOfQuestions 个关于"$topic"的选择题
                难度：$difficulty
                
                格式：
                题目 1：[内容]
                A. [答案A]
                B. [答案B]
                C. [答案C]
                D. [答案D]
                ✅ 正确：[A/B/C/D]
                💡 解释：[简短解释]
            """.trimIndent()

            else -> """
                Tạo $numberOfQuestions câu hỏi trắc nghiệm về: "$topic"
                Cấp độ: $difficulty
                
                ĐỊNH DẠNG:
                Câu 1: [Nội dung]
                A. [Đáp án A]
                B. [Đáp án B]
                C. [Đáp án C]
                D. [Đáp án D]
                ✅ Đáp án: [A/B/C/D]
                💡 Giải thích: [Giải thích ngắn]
            """.trimIndent()
        }

        sendMessage(prompt, difficulty, language)
    }

    fun summarizeEvent(
        eventName: String,
        summaryLength: String = "short",
        language: String = "vi"
    ) {
        val lengthInstruction = when (summaryLength) {
            "short" -> when (language) {
                "en" -> "in 3-4 short sentences"
                "zh" -> "用3-4句话"
                else -> "trong 3-4 câu ngắn"
            }
            "medium" -> when (language) {
                "en" -> "in one paragraph (7-10 sentences)"
                "zh" -> "用一段话（7-10句）"
                else -> "trong 1 đoạn văn (7-10 câu)"
            }
            else -> when (language) {
                "en" -> "with detailed timeline, causes, results"
                "zh" -> "详细说明时间、原因、结果"
                else -> "chi tiết với timeline, nguyên nhân, kết quả"
            }
        }

        val prompt = """
            📚 TÓM TẮT: "$eventName"
            
            Tóm tắt $lengthInstruction, bao gồm:
            - 📅 Thời gian xảy ra
            - 👥 Nhân vật chính
            - 🔍 Nguyên nhân
            - ⚔️ Diễn biến quan trọng
            - 🏆 Kết quả và ý nghĩa lịch sử
            
            ${when (language) {
            "en" -> "Write clearly in English"
            "zh" -> "用中文清楚地写"
            else -> "Viết dễ hiểu bằng tiếng Việt"
        }}
        """.trimIndent()

        sendMessage(prompt, language = language)
    }


    fun explainTerm(term: String, language: String = "vi") {
        val prompt = """
            💡 GIẢI THÍCH: "$term"
            
            ${when (language) {
            "en" -> "Explain in English: definition, origin, examples, significance"
            "zh" -> "用中文解释：定义、起源、例子、意义"
            else -> "Giải thích: định nghĩa, nguồn gốc, ví dụ, ý nghĩa"
        }}
        """.trimIndent()

        sendMessage(prompt, language = language)
    }

    // ==================== GỬI ẢNH LÊN SERVER ====================
    fun sendImageMessage(
        imagePart: MultipartBody.Part,
        prompt: String = "Mô tả bức ảnh này giúp tôi",
        language: String = "vi"
    ) {
        // chỉ đổi trạng thái loading, KHÔNG thêm tin nhắn user nữa
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            try {
                val msgBody = prompt.toRequestBody("text/plain".toMediaTypeOrNull())
                val langBody = language.toRequestBody("text/plain".toMediaTypeOrNull())

                // gọi API /chat dạng multipart (có ảnh)
                val httpResponse = api.sendMessageWithImage(
                    message = msgBody,
                    language = langBody,
                    image = imagePart
                )

                if (httpResponse.isSuccessful) {
                    val body = httpResponse.body()

                    val botMessage = body?.reply
                        ?: body?.error
                        ?: "Server không trả lời."

                    val updatedMessages = _uiState.value.messages.toMutableList()
                    updatedMessages.add(
                        ChatMessage(
                            text = botMessage,
                            isUser = false,
                            hasAudio = true,
                            searchResults = body?.search_results
                        )
                    )

                    _uiState.value = _uiState.value.copy(
                        messages = updatedMessages,
                        isLoading = false,
                        error = null
                    )
                } else {
                    val errText = httpResponse.errorBody()?.string()
                        ?: "HTTP ${httpResponse.code()}"

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "❌ Server error: $errText"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "❌ Lỗi gửi ảnh: ${e.message}"
                )
            }
        }
    }

    fun clearChat() {
        Log.d(TAG, "🗑️ Clearing chat")
        _uiState.value = ChatUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "🧹 ViewModel cleared")
    }
}
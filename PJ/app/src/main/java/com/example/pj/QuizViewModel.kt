//File: QuizViewModel.kt - ✅ FIXED: Added random image function
package com.example.pj

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.Date

data class QuizQuestion(
    val question: String,
    val answers: List<String>,
    val correctAnswer: String,
    val explanation: String = "",
    val imageUrl: String = ""
)

data class QuizState(
    val questions: List<QuizQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val currentQuestion: QuizQuestion? = null,
    val selectedAnswer: String? = null,
    val showResult: Boolean = false,
    val showExplanation: Boolean = false,
    val totalScore: Int = 0,
    val scoreGained: Int = 0,
    val timeLeft: Int = 20,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFinished: Boolean = false,
    val result: QuizResult = QuizResult()
)

class QuizViewModel : ViewModel() {

    private val _quizState = MutableStateFlow(QuizState())
    val quizState: StateFlow<QuizState> = _quizState.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = "AIzaSyAv0xPmTcJyxf00tYQEtC9je8G8C6xgfgo"
    )

    private var timerJob: Job? = null
    private val answeredQuestions = mutableListOf<QuizAnswer>()

    /**
     * ✅ GENERATE QUIZ WITH 3 DIFFICULTY LEVELS
     */
    fun generateQuizWithDifficulty(questionCount: Int, difficulty: String, language: String = "vi") {
        viewModelScope.launch {
            try {
                _quizState.value = _quizState.value.copy(
                    isLoading = true,
                    error = null
                )

                val difficultyPrompt = when (difficulty) {
                    "Dễ", "Easy", "简单" -> """
                        **ĐỘ KHÓ: DỄ (EASY)**
                        
                        Yêu cầu:
                        - Câu hỏi về kiến thức CƠ BẢN, PHỔ THÔNG về lịch sử Việt Nam
                        - Sự kiện nổi tiếng, dễ nhớ (VD: Chiến thắng Điện Biên Phủ, Bác Hồ đọc Tuyên ngôn độc lập)
                        - Nhân vật lịch sử nổi bật (VD: Quang Trung, Trần Hưng Đạo, Hồ Chí Minh)
                        - Thời gian, địa điểm dễ nhớ, phổ biến
                        - Phù hợp với học sinh THCS (12-15 tuổi)
                        
                        Ví dụ câu hỏi:
                        - "Ai là người đọc Tuyên ngôn độc lập ngày 2/9/1945?"
                        - "Chiến thắng Điện Biên Phủ diễn ra năm nào?"
                        - "Vua nào đánh tan 30 vạn quân Thanh?"
                    """.trimIndent()

                    "Trung bình", "Medium", "中等" -> """
                        **ĐỘ KHÓ: TRUNG BÌNH (MEDIUM)**
                        
                        Yêu cầu:
                        - Câu hỏi yêu cầu HIỂU BIẾT VỮNG về lịch sử Việt Nam
                        - Phân tích NGUYÊN NHÂN, KẾT QUẢ của sự kiện lịch sử
                        - So sánh các GIAI ĐOẠN lịch sử, các triều đại
                        - Ý NGHĨA lịch sử của sự kiện, nhân vật
                        - Yêu cầu tư duy logic, liên kết kiến thức
                        - Phù hợp với học sinh THPT (15-18 tuổi)
                        
                        Ví dụ câu hỏi:
                        - "Nguyên nhân sâu xa dẫn đến sự sụp đổ của nhà Lê sơ?"
                        - "So sánh chiến thuật của Trần Hưng Đạo và Lê Lợi?"
                        - "Ý nghĩa lịch sử của Cách mạng Tháng Tám 1945?"
                    """.trimIndent()

                    "Khó", "Hard", "困难" -> """
                        **ĐỘ KHÓ: KHÓ (HARD)**
                        
                        Yêu cầu:
                        - Câu hỏi CHUYÊN SÂU, CHI TIẾT về lịch sử Việt Nam
                        - Phân tích QUAN ĐIỂM, Ý NGHĨA lịch sử sâu sắc
                        - Liên hệ với BỐI CẢNH QUỐC TẾ, ảnh hưởng lịch sử
                        - So sánh với các sự kiện lịch sử thế giới
                        - Yêu cầu kiến thức uyên bác, tư duy phản biện
                        - Phù hợp với sinh viên- Phù hợp với sinh viên CHUYÊN NGÀNH LỊCH SỬ
                        
                        Ví dụ câu hỏi:
                        - "Phân tích ảnh hưởng của Chiến tranh thế giới thứ nhất đến phong trào yêu nước Việt Nam?"
                        - "So sánh tư tưởng cải cách của Phan Bội Châu và Phan Châu Trinh?"
                        - "Đánh giá vai trò của Quốc tế Cộng sản trong Cách mạng Tháng Tám?"
                    """.trimIndent()

                    else -> """
                        **ĐỘ KHÓ: TRUNG BÌNH (MEDIUM)**
                        - Câu hỏi yêu cầu hiểu biết vững về lịch sử Việt Nam
                    """.trimIndent()
                }

                val languageInstruction = when (language) {
                    "en" -> "Write ALL questions, answers, and explanations in ENGLISH"
                    "zh" -> "用中文写所有问题、答案和解释"
                    else -> "Viết TẤT CẢ câu hỏi, đáp án và giải thích bằng TIẾNG VIỆT"
                }

                val prompt = """
                    Tạo $questionCount câu hỏi trắc nghiệm về Lịch sử Việt Nam.
                    
                    $difficultyPrompt
                    
                    ĐỊNH DẠNG TRẢ VỀ (JSON):
                    [
                      {
                        "question": "Câu hỏi chi tiết và rõ ràng",
                        "answers": ["Đáp án A", "Đáp án B", "Đáp án C", "Đáp án D"],
                        "correctAnswer": "Đáp án đúng (phải giống y hệt 1 trong 4 đáp án)",
                        "explanation": "Giải thích chi tiết, dễ hiểu (3-4 câu, giải thích tại sao đáp án này đúng)",
                        "imageKeyword": "từ khóa ngắn gọn để tìm ảnh (VD: 'Ho Chi Minh', 'Dien Bien Phu')"
                      }
                    ]
                    
                    YÊU CẦU BẮT BUỘC:
                    1. Mỗi câu hỏi phải có 4 đáp án
                    2. Chỉ có 1 đáp án đúng duy nhất
                    3. correctAnswer phải GIỐNG CHÍNH XÁC 1 trong 4 answers (không sai chính tả, không thay đổi từ ngữ)
                    4. explanation phải chi tiết (3-4 câu), giải thích rõ ràng vì sao đáp án này đúng
                    5. imageKeyword là tên nhân vật/sự kiện/địa điểm để tìm ảnh minh họa
                    6. $languageInstruction
                    7. Câu hỏi phải đúng với độ khó đã chọn
                    
                    CHỈ TRẢ VỀ JSON ARRAY, KHÔNG THÊM BẤT KỲ TEXT NÀO KHÁC.
                """.trimIndent()

                println("🔍 Generating quiz with prompt:")
                println(prompt)

                val response = generativeModel.generateContent(prompt)
                val jsonText = response.text?.trim() ?: throw Exception("Empty response from AI")

                println("✅ AI Response received, parsing...")
                val questions = parseQuestions(jsonText)

                if (questions.isEmpty()) {
                    throw Exception("Không thể tạo câu hỏi. Vui lòng thử lại.")
                }

                println("✅ Successfully parsed ${questions.size} questions")

                _quizState.value = _quizState.value.copy(
                    questions = questions,
                    currentQuestion = questions.first(),
                    currentQuestionIndex = 0,
                    isLoading = false,
                    timeLeft = 20
                )

                startTimer()

            } catch (e: Exception) {
                _quizState.value = _quizState.value.copy(
                    isLoading = false,
                    error = "Không thể tạo quiz: ${e.message}"
                )
                println("❌ Quiz Generation Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * ✅ PARSE JSON RESPONSE FROM AI
     */
    private fun parseQuestions(jsonText: String): List<QuizQuestion> {
        try {
            val cleanJson = jsonText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            println("📄 Parsing JSON: ${cleanJson.take(200)}...")

            val jsonArray = JSONArray(cleanJson)
            val questions = mutableListOf<QuizQuestion>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)

                val answersArray = obj.getJSONArray("answers")
                val answers = mutableListOf<String>()
                for (j in 0 until answersArray.length()) {
                    answers.add(answersArray.getString(j))
                }

                // ✅ FIXED: Use random daily image instead of Unsplash
                val imageUrl = getRandomDailyImageUrl()

                val question = QuizQuestion(
                    question = obj.getString("question"),
                    answers = answers.shuffled(),
                    correctAnswer = obj.getString("correctAnswer"),
                    explanation = obj.optString("explanation", "Không có giải thích"),
                    imageUrl = imageUrl
                )

                questions.add(question)
                println("✅ Question ${i + 1}: ${question.question.take(50)}...")
            }

            return questions
        } catch (e: Exception) {
            println("❌ JSON Parse Error: ${e.message}")
            e.printStackTrace()
            return emptyList()
        }
    }

    /**
     * ✅ NEW: Get random image from daily1 to daily5 (drawable)
     */
    private fun getRandomDailyImageUrl(): String {
        val randomNum = (1..5).random()
        return "android.resource://com.example.pj/drawable/daily$randomNum"
    }

    /**
     * ✅ START COUNTDOWN TIMER
     */
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_quizState.value.timeLeft > 0 && _quizState.value.selectedAnswer == null) {
                delay(1000)
                _quizState.value = _quizState.value.copy(
                    timeLeft = _quizState.value.timeLeft - 1
                )
            }

            if (_quizState.value.selectedAnswer == null) {
                submitAnswer("")
            }
        }
    }

    /**
     * ✅ SUBMIT ANSWER - SCORE = TIME LEFT (if correct)
     */
    suspend fun submitAnswer(answer: String) {
        timerJob?.cancel()

        val currentQuestion = _quizState.value.currentQuestion ?: return
        val isCorrect = answer == currentQuestion.correctAnswer
        val timeSpent = 20 - _quizState.value.timeLeft

        val scoreGained = if (isCorrect) _quizState.value.timeLeft else 0

        answeredQuestions.add(
            QuizAnswer(
                questionId = _quizState.value.currentQuestionIndex.toString(),
                questionText = currentQuestion.question,
                userAnswer = answer,
                correctAnswer = currentQuestion.correctAnswer,
                isCorrect = isCorrect,
                timeSpent = timeSpent.toLong()
            )
        )

        _quizState.value = _quizState.value.copy(
            selectedAnswer = answer,
            showResult = true,
            showExplanation = !isCorrect,
            totalScore = _quizState.value.totalScore + scoreGained,
            scoreGained = scoreGained
        )

        println("✅ Answer submitted: $answer, Correct: $isCorrect, Score gained: $scoreGained")
    }

    /**
     * ✅ MOVE TO NEXT QUESTION
     */
    fun moveToNextQuestion() {
        val nextIndex = _quizState.value.currentQuestionIndex + 1

        if (nextIndex < _quizState.value.questions.size) {
            println("➡️ Moving to question ${nextIndex + 1}/${_quizState.value.questions.size}")

            _quizState.value = _quizState.value.copy(
                currentQuestionIndex = nextIndex,
                currentQuestion = _quizState.value.questions[nextIndex],
                selectedAnswer = null,
                showResult = false,
                showExplanation = false,
                scoreGained = 0,
                timeLeft = 20
            )
            startTimer()
        } else {
            println("🏁 Quiz finished!")
            finishQuiz()
        }
    }

    /**
     * ✅ MOVE TO PREVIOUS QUESTION (view only, cannot change answer)
     */
    fun moveToPreviousQuestion() {
        val prevIndex = _quizState.value.currentQuestionIndex - 1

        if (prevIndex >= 0) {
            println("⬅️ Moving to question ${prevIndex + 1}/${_quizState.value.questions.size}")

            timerJob?.cancel()

            _quizState.value = _quizState.value.copy(
                currentQuestionIndex = prevIndex,
                currentQuestion = _quizState.value.questions[prevIndex],
                selectedAnswer = null,
                showResult = false,
                showExplanation = false,
                scoreGained = 0,
                timeLeft = 20
            )

            // ✅ Don't start timer for previous questions (view only mode)
        }
    }

    /**
     * ✅ FINISH QUIZ AND GENERATE RESULT
     */
    private fun finishQuiz() {
        timerJob?.cancel()

        val correctCount = answeredQuestions.count { it.isCorrect }
        val totalTime = answeredQuestions.sumOf { it.timeSpent }

        val result = QuizResult(
            userId = "",
            quizId = "daily_${System.currentTimeMillis()}",
            topicId = 0,
            topicName = "Daily Quiz",
            score = _quizState.value.totalScore,
            totalQuestions = _quizState.value.questions.size,
            correctAnswers = correctCount,
            timeSpent = totalTime,
            difficulty = "Daily",
            answers = answeredQuestions.toList(),
            completedAt = Date()
        )

        _quizState.value = _quizState.value.copy(
            isFinished = true,
            result = result
        )

        println("🏆 Quiz Result: Score=${result.score}, Correct=${correctCount}/${result.totalQuestions}")
    }

    /**
     * ✅ RESET QUIZ STATE
     */
    fun resetQuiz() {
        timerJob?.cancel()
        answeredQuestions.clear()
        _quizState.value = QuizState()
        println("🔄 Quiz reset")
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        println("🧹 QuizViewModel cleared")
    }
}
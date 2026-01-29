//File: DailyQuizData.kt - ✅ COMPLETE FIXED VERSION
package com.example.pj

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

object DailyQuizData {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = "AIzaSyBE-OJkMsCD4hjB_Ku7KxCVTqPsHjlH038"
    )

    /**
     * ✅ TẠO CÂU HỎI TỪ GEMINI AI
     * NOTE: File này KHÔNG ĐƯỢC SỬ DỤNG nữa vì QuizViewModel đã tự tạo câu hỏi
     * Giữ lại để tham khảo hoặc sử dụng cho mục đích khác
     */
    suspend fun getAllQuestions(difficulty: String, language: String = "vi"): List<TopicQuestion> {
        return withContext(Dispatchers.IO) {
            try {
                val difficultyPrompt = when (difficulty) {
                    "Dễ", "Easy", "简单" -> """
                        **ĐỘ KHÓ: DỄ (EASY)**
                        - Câu hỏi về kiến thức CƠ BẢN về lịch sử Việt Nam
                        - Sự kiện nổi tiếng, nhân vật lịch sử nổi bật
                        - Phù hợp học sinh THCS (12-15 tuổi)
                    """.trimIndent()

                    "Trung bình", "Medium", "中等" -> """
                        **ĐỘ KHÓ: TRUNG BÌNH (MEDIUM)**
                        - Yêu cầu HIỂU BIẾT VỮNG về lịch sử Việt Nam
                        - Phân tích nguyên nhân, kết quả sự kiện
                        - Phù hợp học sinh THPT (15-18 tuổi)
                    """.trimIndent()

                    "Khó", "Hard", "困难" -> """
                        **ĐỘ KHÓ: KHÓ (HARD)**
                        - Kiến thức CHUYÊN SÂU về lịch sử Việt Nam
                        - Phân tích quan điểm, ý nghĩa lịch sử sâu sắc
                        - Phù hợp sinh viên chuyên ngành
                    """.trimIndent()

                    else -> "ĐỘ KHÓ: TRUNG BÌNH"
                }

                val languageInstruction = when (language) {
                    "en" -> "Write ALL questions, answers, and explanations in ENGLISH"
                    "zh" -> "用中文写所有问题、答案和解释"
                    else -> "Viết TẤT CẢ câu hỏi, đáp án và giải thích bằng TIẾNG VIỆT"
                }

                val prompt = """
                    Tạo 20 câu hỏi trắc nghiệm về Lịch sử Việt Nam.
                    
                    $difficultyPrompt
                    
                    ĐỊNH DẠNG TRẢ VỀ (JSON):
                    [
                      {
                        "question": "Câu hỏi chi tiết và rõ ràng",
                        "answers": ["Đáp án A", "Đáp án B", "Đáp án C", "Đáp án D"],
                        "correctAnswer": "Đáp án đúng (phải giống y hệt 1 trong 4 đáp án)",
                        "explanation": "Giải thích chi tiết (3-4 câu)",
                        "imageKeyword": "từ khóa tìm ảnh (VD: 'Ho Chi Minh')"
                      }
                    ]
                    
                    YÊU CẦU BẮT BUỘC:
                    1. Mỗi câu hỏi có 4 đáp án
                    2. Chỉ 1 đáp án đúng
                    3. correctAnswer phải GIỐNG CHÍNH XÁC 1 trong 4 answers
                    4. explanation chi tiết (3-4 câu)
                    5. imageKeyword là tên nhân vật/sự kiện/địa điểm
                    6. $languageInstruction
                    7. Câu hỏi phải đúng độ khó
                    
                    CHỈ TRẢ VỀ JSON ARRAY, KHÔNG THÊM TEXT KHÁC.
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val jsonText = response.text?.trim() ?: throw Exception("Empty response")

                parseQuestions(jsonText)
            } catch (e: Exception) {
                android.util.Log.e("DailyQuizData", "Error generating questions", e)
                getFallbackQuestions()
            }
        }
    }

    /**
     * ✅ PARSE JSON TỪ GEMINI
     */
    private fun parseQuestions(jsonText: String): List<TopicQuestion> {
        try {
            val cleanJson = jsonText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val jsonArray = JSONArray(cleanJson)
            val questions = mutableListOf<TopicQuestion>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)

                val answersArray = obj.getJSONArray("answers")
                val answers = mutableListOf<String>()
                for (j in 0 until answersArray.length()) {
                    answers.add(answersArray.getString(j))
                }

                // ✅ FIXED: Use random daily image instead of Unsplash
                val imageRes = getRandomDailyImage()

                questions.add(
                    TopicQuestion(
                        id = "gemini_${System.currentTimeMillis()}_$i",
                        question = obj.getString("question"),
                        answers = answers.shuffled(),
                        correctAnswer = obj.getString("correctAnswer"),
                        explanation = obj.optString("explanation", "Không có giải thích"),
                        imageRes = imageRes // ✅ Use drawable resource
                    )
                )
            }

            return questions
        } catch (e: Exception) {
            android.util.Log.e("DailyQuizData", "JSON Parse Error", e)
            return getFallbackQuestions()
        }
    }

    /**
     * ✅ NEW: Get random daily image (1-5)
     */
    private fun getRandomDailyImage(): Int {
        val randomNum = (1..5).random()
        return try {
            R.drawable::class.java.getField("daily$randomNum").getInt(null)
        } catch (e: Exception) {
            0 // No image
        }
    }

    /**
     * ✅ FALLBACK: LẤY CÂU HỎI TỪ TOPICQUIZDATA NẾU GEMINI FAIL
     */
    private fun getFallbackQuestions(): List<TopicQuestion> {
        val allQuestions = mutableListOf<TopicQuestion>()

        // ✅ Get questions from all topics
        for (topicId in 1..6) {
            val sets = TopicQuizData.getSetsForTopic(topicId)
            sets.forEach { quizSet ->
                allQuestions.addAll(quizSet.questions)
            }
        }

        // ✅ Return 20 random questions
        return if (allQuestions.isNotEmpty()) {
            allQuestions.shuffled().take(20)
        } else {
            // ✅ If no questions available, return empty list
            emptyList()
        }
    }
}
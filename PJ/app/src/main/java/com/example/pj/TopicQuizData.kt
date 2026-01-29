//File: TopicQuizData.kt - ✅ UPDATED: Use drawable images for topic questions
package com.example.pj

data class TopicQuizSet(
    val setNumber: Int,
    val setName: String,
    val questions: List<TopicQuestion>
)

data class TopicQuestion(
    val id: String,
    val question: String,
    val answers: List<String>,
    val correctAnswer: String,
    val explanation: String,
    val imageRes: Int = 0 //  NEW: Use drawable resource instead of URL
)

object TopicQuizData {

    fun getSetCount(topicId: Int): Int = when(topicId) {
        1 -> ancientEraQuizzes.size
        2 -> 5
        3 -> 5
        4 -> 5
        5 -> 5
        6 -> 5
        else -> 0
    }

    fun getSetsForTopic(topicId: Int): List<TopicQuizSet> = when(topicId) {
        1 -> ancientEraQuizzes
        2 -> feudalEraQuizzes
        3 -> modernEraQuizzes
        4 -> contemporaryQuizzes
        5 -> historicalFiguresQuizzes
        6 -> heritageCultureQuizzes
        else -> emptyList()
    }

    fun getQuizSet(topicId: Int, setNumber: Int): TopicQuizSet? {
        return getSetsForTopic(topicId).find { it.setNumber == setNumber }
    }

    /**
     * ✅ NEW: Get default image for topic question
     */
    private fun getTopicImage(topicId: Int, questionNumber: Int): Int {
        return try {
            val resourceName = "p_topic${topicId}_cau$questionNumber"
            // If specific image exists, use it, otherwise return 0 (no image)
            R.drawable::class.java.getField(resourceName).getInt(null)
        } catch (e: Exception) {
            0 // No image
        }
    }

    // ==================== CHỦ ĐỀ 1: THỜI CỔ ĐẠI ====================
    private val ancientEraQuizzes = listOf(
        // SET 1: Văn Lang – Âu Lạc
        TopicQuizSet(
            setNumber = 1,
            setName = "Văn Lang - Âu Lạc",
            questions = listOf(
                TopicQuestion(
                    id = "ancient_1_1",
                    question = "Nhà nước Văn Lang được thành lập vào khoảng thời gian nào?",
                    answers = listOf(
                        "Khoảng 2879 TCN",
                        "Khoảng 1000 TCN",
                        "Khoảng 500 TCN",
                        "Khoảng 100 TCN"
                    ),
                    correctAnswer = "Khoảng 2879 TCN",
                    explanation = "Nhà nước Văn Lang được thành lập khoảng năm 2879 trước Công nguyên, do vua Hùng Vương thứ nhất sáng lập. Đây là nhà nước đầu tiên của người Việt cổ, đánh dấu bước ngoặt từ xã hội thị tộc sang xã hội có tổ chức nhà nước.",
                    imageRes = getTopicImage(1, 1) // p_topic1_cau1
                ),
                TopicQuestion(
                    id = "ancient_1_2",
                    question = "Vua nào đã đánh bại nhà Hùng Vương và lập ra nước Âu Lạc?",
                    answers = listOf(
                        "An Dương Vương",
                        "Lý Nam Đế",
                        "Triệu Đà",
                        "Hùng Vương"
                    ),
                    correctAnswer = "An Dương Vương",
                    explanation = "An Dương Vương (Thục Phán) đã đánh bại vua Hùng Vương cuối cùng vào khoảng năm 258 TCN và sáng lập nước Âu Lạc. Ông xây dựng kinh đô Cổ Loa với hệ thống thành lũy kiên cố, đánh dấu bước phát triển mới của nhà nước cổ Việt Nam.",
                    imageRes = getTopicImage(1, 2) // p_topic1_cau2
                ),
                TopicQuestion(
                    id = "ancient_1_3",
                    question = "Thành Cổ Loa được xây dựng ở đâu?",
                    answers = listOf(
                        "Đông Anh, Hà Nội",
                        "Hải Dương",
                        "Phú Thọ",
                        "Thanh Hóa"
                    ),
                    correctAnswer = "Đông Anh, Hà Nội",
                    explanation = "Thành Cổ Loa nằm ở Đông Anh, Hà Nội, là kinh đô của nước Âu Lạc do An Dương Vương xây dựng. Thành có cấu trúc 3 vòng thành lũy hình ốc xoắn, thể hiện trình độ kiến trúc quân sự cao của người Việt cổ.",
                    imageRes = getTopicImage(1, 3)
                ),
                TopicQuestion(
                    id = "ancient_1_4",
                    question = "Truyền thuyết nào gắn liền với thời Hùng Vương?",
                    answers = listOf(
                        "Bánh chưng bánh dày",
                        "Thánh Gióng",
                        "Sơn Tinh Thủy Tinh",
                        "Cả 3 đáp án trên"
                    ),
                    correctAnswer = "Cả 3 đáp án trên",
                    explanation = "Thời Hùng Vương gắn liền với nhiều truyền thuyết: Bánh chưng bánh dày (Lang Liêu), Thánh Gióng (chống giặc Ân), Sơn Tinh Thủy Tinh (tranh cướp Mi Nương). Các truyền thuyết này phản ánh đời sống văn hóa và tinh thần dân tộc thời cổ đại.",
                    imageRes = getTopicImage(1, 4)
                ),
                TopicQuestion(
                    id = "ancient_1_5",
                    question = "Nước Âu Lạc tồn tại trong khoảng thời gian nào?",
                    answers = listOf(
                        "Từ 258 TCN đến 207 TCN",
                        "Từ 300 TCN đến 100 TCN",
                        "Từ 500 TCN đến 300 TCN",
                        "Từ 200 TCN đến 100 TCN"
                    ),
                    correctAnswer = "Từ 258 TCN đến 207 TCN",
                    explanation = "Nước Âu Lạc tồn tại từ 258 TCN đến 207 TCN, khoảng hơn 50 năm. Năm 207 TCN, Triệu Đà từ phương Bắc tấn công và sáp nhập Âu Lạc vào nước Nam Việt.",
                    imageRes = getTopicImage(1, 5)
                )
            )
        ),

        // SET 2: Bắc thuộc I – Hán, Đông Hán
        TopicQuizSet(
            setNumber = 2,
            setName = "Bắc thuộc I: Hán - Đông Hán",
            questions = listOf(
                TopicQuestion(
                    id = "ancient_2_1",
                    question = "Thời kỳ Bắc thuộc lần thứ nhất bắt đầu từ năm nào?",
                    answers = listOf(
                        "179 TCN",
                        "111 TCN",
                        "207 TCN",
                        "43 TCN"
                    ),
                    correctAnswer = "111 TCN",
                    explanation = "Thời kỳ Bắc thuộc lần thứ nhất bắt đầu từ năm 111 TCN khi nhà Hán đánh chiếm nước Nam Việt và đặt quận Giao Chỉ, Cửu Chân, Nhật Nam. Đây là giai đoạn Việt Nam bị thống trị trực tiếp bởi phong kiến phương Bắc.",
                    imageRes = getTopicImage(1, 6)
                ),
                TopicQuestion(
                    id = "ancient_2_2",
                    question = "Hai Bà Trưng khởi nghĩa vào năm nào?",
                    answers = listOf(
                        "Năm 40",
                        "Năm 43",
                        "Năm 111",
                        "Năm 39"
                    ),
                    correctAnswer = "Năm 40",
                    explanation = "Hai Bà Trưng (Trưng Trắc và Trưng Nhị) khởi nghĩa năm 40 chống ách thống trị tàn bạo của nhà Hán, lập nên một chính quyền tự chủ tồn tại khoảng 3 năm.",
                    imageRes = getTopicImage(1, 7)
                ),
                TopicQuestion(
                    id = "ancient_2_3",
                    question = "Tướng nào của nhà Hán đã đàn áp cuộc khởi nghĩa Hai Bà Trưng?",
                    answers = listOf(
                        "Mã Viện",
                        "Tô Định",
                        "Lư Gia",
                        "Triệu Đà"
                    ),
                    correctAnswer = "Mã Viện",
                    explanation = "Tướng Mã Viện của nhà Đông Hán được cử vào năm 42 để đàn áp cuộc khởi nghĩa Hai Bà Trưng. Năm 43, nghĩa quân bị thất bại, nhà Hán khôi phục lại nền thống trị.",
                    imageRes = getTopicImage(1, 8)
                ),
                TopicQuestion(
                    id = "ancient_2_4",
                    question = "Quận nào là trung tâm của vùng đất Giao Chỉ thời Bắc thuộc?",
                    answers = listOf(
                        "Quận Giao Chỉ",
                        "Quận Cửu Chân",
                        "Quận Nhật Nam",
                        "Quận Nam Hải"
                    ),
                    correctAnswer = "Quận Giao Chỉ",
                    explanation = "Quận Giao Chỉ (khu vực châu thổ sông Hồng ngày nay) là trung tâm quan trọng nhất trong 3 quận do nhà Hán đặt ở đất Việt, nơi tập trung các hoạt động chính trị, kinh tế và văn hóa.",
                    imageRes = getTopicImage(1, 9)
                ),
                TopicQuestion(
                    id = "ancient_2_5",
                    question = "Chính sách nào của nhà Hán gây bức xúc lớn cho người Việt?",
                    answers = listOf(
                        "Đánh thuế nặng, cướp ruộng đất, bắt lính phu",
                        "Chỉ truyền bá chữ Hán",
                        "Chỉ xây thành quách",
                        "Chỉ khai thác mỏ"
                    ),
                    correctAnswer = "Đánh thuế nặng, cướp ruộng đất, bắt lính phu",
                    explanation = "Nhà Hán thực hiện chính sách bóc lột khắc nghiệt: đánh thuế nặng, cướp đoạt ruộng đất của người Việt, bắt lính và phu phục vụ chiến tranh, khiến mâu thuẫn dân tộc ngày càng sâu sắc.",
                    imageRes = getTopicImage(1, 10)
                )
            )
        ),

        // SET 3: Bắc thuộc II – III
        TopicQuizSet(
            setNumber = 3,
            setName = "Bắc thuộc II-III",
            questions = listOf(
                TopicQuestion(
                    id = "ancient_3_1",
                    question = "Nhân vật nào được nhân dân tôn là 'Sĩ Vương', có công ổn định Giao Châu thời Bắc thuộc?",
                    answers = listOf(
                        "Sĩ Nhiếp",
                        "Tô Định",
                        "Mã Viện",
                        "Triệu Đà"
                    ),
                    correctAnswer = "Sĩ Nhiếp",
                    explanation = "Sĩ Nhiếp là Thái thú Giao Chỉ đầu thế kỷ III, thi hành chính sách mềm mỏng, khuyến khích học chữ Hán, được nhân dân tôn xưng là 'Sĩ Vương'.",
                    imageRes = getTopicImage(1, 11)
                ),
                TopicQuestion(
                    id = "ancient_3_2",
                    question = "Khởi nghĩa Mai Thúc Loan chống nhà Đường bùng nổ tại đâu?",
                    answers = listOf(
                        "Hoan Châu (Nghệ An)",
                        "Giao Chỉ",
                        "Cửu Chân",
                        "Nhật Nam"
                    ),
                    correctAnswer = "Hoan Châu (Nghệ An)",
                    explanation = "Mai Thúc Loan tập hợp lực lượng ở Hoan Châu (Nghệ An), tự xưng là Mai Hắc Đế, lãnh đạo cuộc khởi nghĩa lớn chống lại chính quyền đô hộ nhà Đường.",
                    imageRes = getTopicImage(1, 12)
                ),
                TopicQuestion(
                    id = "ancient_3_3",
                    question = "Phùng Hưng lãnh đạo cuộc khởi nghĩa chống Đường tại vùng nào?",
                    answers = listOf(
                        "Đường Lâm (Sơn Tây)",
                        "Hoa Lư (Ninh Bình)",
                        "Cổ Loa (Hà Nội)",
                        "Huế"
                    ),
                    correctAnswer = "Đường Lâm (Sơn Tây)",
                    explanation = "Phùng Hưng quê ở Đường Lâm (Sơn Tây, Hà Nội ngày nay), lãnh đạo nhân dân nổi dậy đánh đuổi quan đô hộ nhà Đường, được tôn là 'Bố Cái Đại Vương'.",
                    imageRes = getTopicImage(1, 13)
                ),
                TopicQuestion(
                    id = "ancient_3_4",
                    question = "Ai là người đầu tiên giành quyền tự chủ, lập họ Khúc cai quản Tĩnh Hải quân?",
                    answers = listOf(
                        "Khúc Thừa Dụ",
                        "Khúc Hạo",
                        "Dương Đình Nghệ",
                        "Ngô Quyền"
                    ),
                    correctAnswer = "Khúc Thừa Dụ",
                    explanation = "Năm 905, Khúc Thừa Dụ được nhân dân tôn làm Tiết độ sứ Tĩnh Hải quân, thực chất giành quyền tự chủ, mở ra thời kỳ tự chủ lâu dài của người Việt.",
                    imageRes = getTopicImage(1, 14)
                ),
                TopicQuestion(
                    id = "ancient_3_5",
                    question = "Trước khi bị Kiều Công Tiễn sát hại, Dương Đình Nghệ giữ chức vụ gì?",
                    answers = listOf(
                        "Tiết độ sứ Tĩnh Hải quân",
                        "Thái thú Giao Chỉ",
                        "Hoàng đế",
                        "Tổng chỉ huy thủy quân"
                    ),
                    correctAnswer = "Tiết độ sứ Tĩnh Hải quân",
                    explanation = "Dương Đình Nghệ kế tục họ Khúc, giữ chức Tiết độ sứ Tĩnh Hải quân, tiếp tục củng cố nền tự chủ trước khi bị Kiều Công Tiễn phản bội.",
                    imageRes = getTopicImage(1, 15)
                )
            )
        ),

        // SET 4: Thời kỳ tự chủ
        TopicQuizSet(
            setNumber = 4,
            setName = "Thời kỳ tự chủ",
            questions = listOf(
                TopicQuestion(
                    id = "ancient_4_1",
                    question = "Chiến thắng nào của Ngô Quyền đã chấm dứt cơ bản hơn 1000 năm Bắc thuộc?",
                    answers = listOf(
                        "Chiến thắng Bạch Đằng năm 938",
                        "Khởi nghĩa Hai Bà Trưng",
                        "Khởi nghĩa Mai Thúc Loan",
                        "Khởi nghĩa Phùng Hưng"
                    ),
                    correctAnswer = "Chiến thắng Bạch Đằng năm 938",
                    explanation = "Năm 938, Ngô Quyền đánh tan quân Nam Hán trên sông Bạch Đằng, chấm dứt cơ bản ách thống trị trực tiếp của phong kiến phương Bắc.",
                    imageRes = getTopicImage(1, 16)
                ),
                TopicQuestion(
                    id = "ancient_4_2",
                    question = "Sau chiến thắng Bạch Đằng, Ngô Quyền chọn nơi nào làm kinh đô?",
                    answers = listOf(
                        "Cổ Loa",
                        "Hoa Lư",
                        "Thăng Long",
                        "Huế"
                    ),
                    correctAnswer = "Cổ Loa",
                    explanation = "Ngô Quyền lên ngôi vua và chọn Cổ Loa làm kinh đô, tiếp nối truyền thống Âu Lạc xưa.",
                    imageRes = getTopicImage(1, 17)
                ),
                TopicQuestion(
                    id = "ancient_4_3",
                    question = "Ý nghĩa lớn nhất của chiến thắng Bạch Đằng năm 938 là gì?",
                    answers = listOf(
                        "Chấm dứt ách đô hộ lâu dài của phong kiến phương Bắc",
                        "Mở rộng lãnh thổ về phía Nam",
                        "Thành lập nhà Lý",
                        "Đưa Phật giáo lên vị trí độc tôn"
                    ),
                    correctAnswer = "Chấm dứt ách đô hộ lâu dài của phong kiến phương Bắc",
                    explanation = "Chiến thắng Bạch Đằng 938 đã kết thúc hơn 1000 năm Bắc thuộc, khẳng định nền độc lập tự chủ của dân tộc.",
                    imageRes = getTopicImage(1, 18)
                ),
                TopicQuestion(
                    id = "ancient_4_4",
                    question = "Điểm chung quan trọng giữa Khúc Thừa Dụ, Dương Đình Nghệ và Ngô Quyền là gì?",
                    answers = listOf(
                        "Đều hướng tới xây dựng chính quyền tự chủ của người Việt",
                        "Đều được phong vương bởi nhà Đường",
                        "Đều dời đô ra Thăng Long",
                        "Đều chống lại nhà Hồ"
                    ),
                    correctAnswer = "Đều hướng tới xây dựng chính quyền tự chủ của người Việt",
                    explanation = "Các lực lượng Khúc – Dương – Ngô đều có mục tiêu giành và giữ quyền tự chủ cho người Việt, từng bước thoát khỏi sự lệ thuộc vào phong kiến phương Bắc.",
                    imageRes = getTopicImage(1, 19)
                ),
                TopicQuestion(
                    id = "ancient_4_5",
                    question = "Triều đại nào mở đầu cho thời kỳ phong kiến độc lập lâu dài của Việt Nam sau Ngô Quyền?",
                    answers = listOf(
                        "Nhà Đinh",
                        "Nhà Lý",
                        "Nhà Trần",
                        "Nhà Hồ"
                    ),
                    correctAnswer = "Nhà Đinh",
                    explanation = "Sau thời Ngô suy yếu, Đinh Bộ Lĩnh dẹp loạn 12 sứ quân, lập nên nhà Đinh, mở đầu cho chế độ phong kiến tập quyền độc lập lâu dài.",
                    imageRes = getTopicImage(1, 20)
                )
            )
        ),

        // SET 5: Văn hóa cổ đại
        TopicQuizSet(
            setNumber = 5,
            setName = "Văn hóa cổ đại",
            questions = listOf(
                TopicQuestion(
                    id = "ancient_5_1",
                    question = "Trống đồng Đông Sơn là di sản tiêu biểu của nền văn hóa nào?",
                    answers = listOf(
                        "Văn hóa Đông Sơn",
                        "Văn hóa Sa Huỳnh",
                        "Văn hóa Óc Eo",
                        "Văn hóa Hòa Bình"
                    ),
                    correctAnswer = "Văn hóa Đông Sơn",
                    explanation = "Trống đồng là hiện vật tiêu biểu của văn hóa Đông Sơn, thể hiện trình độ luyện kim và nghệ thuật trang trí rất cao của cư dân Việt cổ.",
                    imageRes = getTopicImage(1, 21)
                ),
                TopicQuestion(
                    id = "ancient_5_2",
                    question = "Văn hóa Đông Sơn phát triển mạnh ở khu vực nào của Việt Nam ngày nay?",
                    answers = listOf(
                        "Lưu vực sông Hồng và sông Mã",
                        "Đồng bằng sông Cửu Long",
                        "Duyên hải Nam Trung Bộ",
                        "Tây Nguyên"
                    ),
                    correctAnswer = "Lưu vực sông Hồng và sông Mã",
                    explanation = "Văn hóa Đông Sơn phát triển chủ yếu ở khu vực lưu vực sông Hồng và sông Mã, gắn với cư dân Lạc Việt.",
                    imageRes = getTopicImage(1, 22)
                ),
                TopicQuestion(
                    id = "ancient_5_3",
                    question = "Trong đời sống tinh thần của người Việt cổ, tín ngưỡng phổ biến là gì?",
                    answers = listOf(
                        "Thờ thần thiên nhiên và tổ tiên",
                        "Thờ đạo Hồi",
                        "Thờ Thiên Chúa",
                        "Thờ đạo Bà-la-môn"
                    ),
                    correctAnswer = "Thờ thần thiên nhiên và tổ tiên",
                    explanation = "Người Việt cổ có tín ngưỡng đa thần, thờ các lực lượng tự nhiên như Mặt Trời, Mặt Trăng, thần Sông, thần Núi và thờ cúng tổ tiên.",
                    imageRes = getTopicImage(1, 23)
                ),
                TopicQuestion(
                    id = "ancient_5_4",
                    question = "Loại hình nhà ở phổ biến của cư dân Văn Lang – Âu Lạc là gì?",
                    answers = listOf(
                        "Nhà sàn",
                        "Nhà tầng",
                        "Nhà ngầm",
                        "Nhà kiên cố bằng gạch"
                    ),
                    correctAnswer = "Nhà sàn",
                    explanation = "Do điều kiện khí hậu nóng ẩm, mưa nhiều, cư dân Văn Lang – Âu Lạc phổ biến sống trong các ngôi nhà sàn làm bằng tre, gỗ, nứa.",
                    imageRes = getTopicImage(1, 24)
                ),
                TopicQuestion(
                    id = "ancient_5_5",
                    question = "Lễ hội nào sau đây gắn với thời đại Hùng Vương và còn được duy trì đến ngày nay?",
                    answers = listOf(
                        "Giỗ Tổ Hùng Vương (mùng 10 tháng 3 âm lịch)",
                        "Lễ hội Chùa Hương",
                        "Lễ hội Đền Hùng vào mùng 8 tháng 1",
                        "Lễ hội Đền Trần"
                    ),
                    correctAnswer = "Giỗ Tổ Hùng Vương (mùng 10 tháng 3 âm lịch)",
                    explanation = "Giỗ Tổ Hùng Vương (mùng 10 tháng 3 âm lịch) là ngày lễ lớn tưởng nhớ các vua Hùng, thể hiện truyền thống 'uống nước nhớ nguồn' của dân tộc.",
                    imageRes = getTopicImage(1, 25)
                )
            )
        )
    )

    // ==================== CHỦ ĐỀ 2: THỜI PHONG KIẾN ====================
    private val feudalEraQuizzes = listOf(
        TopicQuizSet(
            setNumber = 1,
            setName = "Nhà Ngô – Đinh – Tiền Lê",
            questions = listOf(
                TopicQuestion(
                    id = "feudal_1_1",
                    question = "Ngô Quyền đánh bại quân Nam Hán trên sông Bạch Đằng vào năm nào?",
                    answers = listOf("Năm 938", "Năm 939", "Năm 905", "Năm 981"),
                    correctAnswer = "Năm 938",
                    explanation = "Năm 938, Ngô Quyền đại phá quân Nam Hán trên sông Bạch Đằng, chấm dứt hơn 1000 năm Bắc thuộc.",
                    imageRes = getTopicImage(2, 1)
                ),
                TopicQuestion(
                    id = "feudal_1_2",
                    question = "Ai là vị vua sáng lập nhà Đinh?",
                    answers = listOf("Đinh Tiên Hoàng", "Lê Đại Hành", "Trần Thái Tông", "Ngô Quyền"),
                    correctAnswer = "Đinh Tiên Hoàng",
                    explanation = "Đinh Bộ Lĩnh dẹp loạn 12 sứ quân, lên ngôi năm 968, lấy hiệu Đinh Tiên Hoàng, lập quốc hiệu Đại Cồ Việt.",
                    imageRes = getTopicImage(2, 2)
                ),
                TopicQuestion(
                    id = "feudal_1_3",
                    question = "Kinh đô Hoa Lư thuộc tỉnh nào ngày nay?",
                    answers = listOf("Ninh Bình", "Thanh Hóa", "Hà Nội", "Bắc Ninh"),
                    correctAnswer = "Ninh Bình",
                    explanation = "Hoa Lư thuộc Ninh Bình ngày nay, là kinh đô của nhà Đinh và Tiền Lê.",
                    imageRes = getTopicImage(2, 3)
                ),
                TopicQuestion(
                    id = "feudal_1_4",
                    question = "Ai lãnh đạo cuộc kháng chiến chống Tống năm 981?",
                    answers = listOf("Lê Đại Hành", "Ngô Quyền", "Lý Thái Tổ", "Lý Thường Kiệt"),
                    correctAnswer = "Lê Đại Hành",
                    explanation = "Lê Hoàn (Lê Đại Hành) đã lãnh đạo quân dân Đại Cồ Việt đánh bại quân Tống năm 981.",
                    imageRes = getTopicImage(2, 4)
                ),
                TopicQuestion(
                    id = "feudal_1_5",
                    question = "Quốc hiệu 'Đại Cồ Việt' được sử dụng dưới triều đại nào?",
                    answers = listOf("Nhà Đinh và Tiền Lê", "Nhà Lý", "Nhà Trần", "Nhà Hồ"),
                    correctAnswer = "Nhà Đinh và Tiền Lê",
                    explanation = "Đinh Tiên Hoàng đặt quốc hiệu Đại Cồ Việt năm 968, tiếp tục dùng dưới thời Tiền Lê.",
                    imageRes = getTopicImage(2, 5)
                )
            )
        ),

        TopicQuizSet(
            setNumber = 2,
            setName = "Nhà Lý – Dời đô – Xây dựng Đại Việt",
            questions = listOf(
                TopicQuestion(
                    id = "feudal_2_1",
                    question = "Lý Công Uẩn dời đô từ Hoa Lư ra Thăng Long vào năm nào?",
                    answers = listOf("Năm 1010", "Năm 1054", "Năm 1009", "Năm 1226"),
                    correctAnswer = "Năm 1010",
                    explanation = "Năm 1010, Lý Công Uẩn ban Chiếu dời đô và dời kinh đô ra Thăng Long.",
                    imageRes = getTopicImage(2, 6)
                ),
                TopicQuestion(
                    id = "feudal_2_2",
                    question = "Quốc hiệu 'Đại Việt' được đặt vào năm nào?",
                    answers = listOf("Năm 1054", "Năm 1010", "Năm 968", "Năm 1428"),
                    correctAnswer = "Năm 1054",
                    explanation = "Năm 1054, vua Lý Thánh Tông đổi quốc hiệu từ 'Đại Cồ Việt' thành 'Đại Việt'.",
                    imageRes = getTopicImage(2, 7)
                ),
                TopicQuestion(
                    id = "feudal_2_3",
                    question = "Ai được xem là người đọc bài thơ 'Nam quốc sơn hà' trên phòng tuyến Như Nguyệt?",
                    answers = listOf("Lý Thường Kiệt", "Lý Công Uẩn", "Trần Hưng Đạo", "Trần Nhật Duật"),
                    correctAnswer = "Lý Thường Kiệt",
                    explanation = "Lý Thường Kiệt cho đọc bài 'Nam quốc sơn hà', được xem là bản Tuyên ngôn độc lập đầu tiên.",
                    imageRes = getTopicImage(2, 8)
                ),
                TopicQuestion(
                    id = "feudal_2_4",
                    question = "Cuộc kháng chiến chống Tống thời Lý diễn ra trong các năm nào?",
                    answers = listOf("1075–1077", "938–939", "1258–1288", "1407–1427"),
                    correctAnswer = "1075–1077",
                    explanation = "Cuộc kháng chiến chống Tống thời Lý diễn ra từ 1075 đến 1077 dưới sự chỉ huy của Lý Thường Kiệt.",
                    imageRes = getTopicImage(2, 9)
                ),
                TopicQuestion(
                    id = "feudal_2_5",
                    question = "Triều Lý nổi bật với chính sách nào?",
                    answers = listOf("Nhân trị và khoan dung", "Pháp trị nghiêm khắc", "Cô lập ngoại giao", "Xâm lược nước láng giềng"),
                    correctAnswer = "Nhân trị và khoan dung",
                    explanation = "Triều Lý thực hiện chính sách nhân trị, chăm lo đời sống dân, xây dựng đất nước ổn định.",
                    imageRes = getTopicImage(2, 10)
                )
            )
        ),

        TopicQuizSet(
            setNumber = 3,
            setName = "Nhà Trần – Kháng chiến chống Nguyên Mông",
            questions = listOf(
                TopicQuestion(
                    id = "feudal_3_1",
                    question = "Nhà Trần được thành lập vào năm nào?",
                    answers = listOf("Năm 1226", "Năm 1285", "Năm 1258", "Năm 1225"),
                    correctAnswer = "Năm 1226",
                    explanation = "Năm 1226, Trần Thái Tông lên ngôi, chính thức lập ra nhà Trần.",
                    imageRes = getTopicImage(2, 11)
                ),
                TopicQuestion(
                    id = "feudal_3_2",
                    question = "Đại Việt dưới thời Trần đã ba lần đánh bại quân xâm lược nào?",
                    answers = listOf("Nguyên Mông", "Nhà Tống", "Nhà Minh", "Nhà Thanh"),
                    correctAnswer = "Nguyên Mông",
                    explanation = "Đại Việt ba lần đánh bại quân Nguyên Mông vào các năm 1258, 1285 và 1287–1288.",
                    imageRes = getTopicImage(2, 12)
                ),
                TopicQuestion(
                    id = "feudal_3_3",
                    question = "Trận Bạch Đằng 1288 gắn liền với danh tướng nào?",
                    answers = listOf("Trần Hưng Đạo", "Trần Quang Khải", "Trần Nhật Duật", "Trần Ích Tắc"),
                    correctAnswer = "Trần Hưng Đạo",
                    explanation = "Trần Hưng Đạo chỉ huy trận Bạch Đằng năm 1288, đại thắng quân Nguyên.",
                    imageRes = getTopicImage(2, 13)
                ),
                TopicQuestion(
                    id = "feudal_3_4",
                    question = "Hội nghị Diên Hồng thời Trần nhằm mục đích gì?",
                    answers = listOf("Hỏi ý bô lão về việc đánh hay hòa", "Chọn người kế vị", "Dời đô", "Tổ chức thi cử"),
                    correctAnswer = "Hỏi ý bô lão về việc đánh hay hòa",
                    explanation = "Hội nghị Diên Hồng biểu thị ý chí toàn dân: 'quyết chiến' chống giặc Nguyên.",
                    imageRes = getTopicImage(2, 14)
                ),
                TopicQuestion(
                    id = "feudal_3_5",
                    question = "Ai là tác giả bài 'Hịch tướng sĩ' nổi tiếng?",
                    answers = listOf("Trần Hưng Đạo", "Trần Thái Tông", "Lý Thường Kiệt", "Lê Lợi"),
                    correctAnswer = "Trần Hưng Đạo",
                    explanation = "'Hịch tướng sĩ' nhằm khích lệ quân sĩ quyết tâm chống giặc Nguyên.",
                    imageRes = getTopicImage(2, 15)
                )
            )
        ),

        TopicQuizSet(
            setNumber = 4,
            setName = "Nhà Hồ – Minh thuộc – Lam Sơn",
            questions = listOf(
                TopicQuestion(
                    id = "feudal_4_1",
                    question = "Nhà Hồ đổi quốc hiệu nước ta thành gì?",
                    answers = listOf("Đại Ngu", "Đại Việt", "Đại Cồ Việt", "An Nam"),
                    correctAnswer = "Đại Ngu",
                    explanation = "Năm 1400, Hồ Quý Ly lên ngôi và đổi quốc hiệu từ Đại Việt thành Đại Ngu.",
                    imageRes = getTopicImage(2, 16)
                ),
                TopicQuestion(
                    id = "feudal_4_2",
                    question = "Ai lãnh đạo cuộc khởi nghĩa Lam Sơn?",
                    answers = listOf("Lê Lợi", "Nguyễn Trãi", "Trần Nguyên Hãn", "Trần Hưng Đạo"),
                    correctAnswer = "Lê Lợi",
                    explanation = "Lê Lợi lãnh đạo cuộc khởi nghĩa Lam Sơn từ năm 1418 đến 1427.",
                    imageRes = getTopicImage(2, 17)
                ),
                TopicQuestion(
                    id = "feudal_4_3",
                    question = "Bài 'Bình Ngô đại cáo' do ai soạn?",
                    answers = listOf("Nguyễn Trãi", "Lê Lợi", "Trần Thủ Độ", "Trương Hán Siêu"),
                    correctAnswer = "Nguyễn Trãi",
                    explanation = "'Bình Ngô đại cáo' là bản tuyên ngôn độc lập thứ hai của dân tộc.",
                    imageRes = getTopicImage(2, 18)
                ),
                TopicQuestion(
                    id = "feudal_4_4",
                    question = "Khởi nghĩa Lam Sơn bùng nổ vào năm nào?",
                    answers = listOf("Năm 1418","Năm 1428","Năm 1407", "Năm 1433"),
                    correctAnswer = "Năm 1418",
                    explanation = "Khởi nghĩa Lam Sơn nổ ra năm 1418 tại Thanh Hóa.",
                    imageRes = getTopicImage(2, 19)
                ),
                TopicQuestion(
                    id = "feudal_4_5",
                    question = "Kết quả của khởi nghĩa Lam Sơn là gì?",
                    answers = listOf("Giải phóng đất nước khỏi ách đô hộ Minh", "Thất bại", "Chỉ giải phóng một phần", "Thành lập nhà Hồ"),
                    correctAnswer = "Giải phóng đất nước khỏi ách đô hộ Minh",
                    explanation = "Năm 1427, quân Minh rút về nước, chấm dứt thời kỳ Minh thuộc.",
                    imageRes = getTopicImage(2, 20)
                )
            )
        ),

        TopicQuizSet(
            setNumber = 5,
            setName = "Lê sơ – Trịnh Nguyễn – Phân tranh",
            questions = listOf(
                TopicQuestion(
                    id = "feudal_5_1",
                    question = "Triều đại Lê sơ bắt đầu từ năm nào?",
                    answers = listOf("Năm 1428", "Năm 1418", "Năm 1400", "Năm 1527"),
                    correctAnswer = "Năm 1428",
                    explanation = "Sau chiến thắng quân Minh, Lê Lợi lên ngôi năm 1428, lập ra triều Lê sơ.",
                    imageRes = getTopicImage(2, 21)
                ),
                TopicQuestion(
                    id = "feudal_5_2",
                    question = "Thời Lê trung hưng, quyền lực thực tế thuộc về ai?",
                    answers = listOf("Chúa Trịnh", "Chúa Nguyễn", "Vua Lê", "Nhà Minh"),
                    correctAnswer = "Chúa Trịnh",
                    explanation = "Ở Đàng Ngoài, vua Lê chỉ là hư vị, thực quyền nằm trong tay các chúa Trịnh.",
                    imageRes = getTopicImage(2, 22)
                ),
                TopicQuestion(
                    id = "feudal_5_3",
                    question = "Cuộc phân tranh Trịnh – Nguyễn chia đất nước thành hai vùng nào?",
                    answers = listOf("Đàng Ngoài – Đàng Trong", "Bắc – Nam", "Trung – Nam", "Miền núi – Đồng bằng"),
                    correctAnswer = "Đàng Ngoài – Đàng Trong",
                    explanation = "Chúa Trịnh cai trị Đàng Ngoài, chúa Nguyễn cai trị Đàng Trong.",
                    imageRes = getTopicImage(2, 23)
                ),
                TopicQuestion(
                    id = "feudal_5_4",
                    question = "Sau khi rút khỏi Thăng Long, nhà Mạc tồn tại chủ yếu tại khu vực nào?",
                    answers = listOf("Cao Bằng", "Thanh Hóa", "Nghệ An", "Thái Bình"),
                    correctAnswer = "Cao Bằng",
                    explanation = "Nhà Mạc được nhà Minh bảo hộ và duy trì ở Cao Bằng trong nhiều thập kỷ.",
                    imageRes = getTopicImage(2, 24)
                ),
                TopicQuestion(
                    id = "feudal_5_5",
                    question = "Đặc điểm nổi bật nhất của thời kỳ Trịnh – Nguyễn phân tranh là gì?",
                    answers = listOf("Đất nước bị chia cắt kéo dài", "Không có chiến tranh", "Hoàn toàn hòa bình", "Thống nhất mạnh mẽ"),
                    correctAnswer = "Đất nước bị chia cắt kéo dài",
                    explanation = "Hơn 200 năm đất nước chia cắt gây nhiều tổn thất cho xã hội.",
                    imageRes = getTopicImage(2, 25)
                )
            )
        )
    )


    // ==================== CHỦ ĐỀ 3: THỜI CẬN ĐẠI ====================
    private val modernEraQuizzes = listOf(
        TopicQuizSet(
            setNumber = 1,
            setName = "Pháp xâm lược và đô hộ",
            questions = listOf(
                TopicQuestion(
                    id = "modern_1_1",
                    question = "Thực dân Pháp nổ súng xâm lược Việt Nam lần đầu ở đâu?",
                    answers = listOf(
                        "Bán đảo Sơn Trà (Đà Nẵng)",
                        "Hà Nội",
                        "Sài Gòn",
                        "Hải Phòng"
                    ),
                    correctAnswer = "Bán đảo Sơn Trà (Đà Nẵng)",
                    explanation = "Ngày 1/9/1858, liên quân Pháp – Tây Ban Nha nổ súng tấn công bán đảo Sơn Trà (Đà Nẵng), mở đầu cuộc chiến xâm lược Việt Nam.",
                    imageRes = getTopicImage(3, 1)
                ),
                TopicQuestion(
                    id = "modern_1_2",
                    question = "Hiệp ước nào đánh dấu triều Nguyễn chính thức thừa nhận nền bảo hộ của Pháp trên toàn cõi Việt Nam?",
                    answers = listOf(
                        "Hiệp ước Pa-tơ-nốt (1884)",
                        "Hiệp ước Nhâm Tuất (1862)",
                        "Hiệp ước Giáp Tuất (1874)",
                        "Hiệp ước Hác-măng (1883)"
                    ),
                    correctAnswer = "Hiệp ước Pa-tơ-nốt (1884)",
                    explanation = "Hiệp ước Pa-tơ-nốt (6/6/1884) khẳng định Việt Nam trở thành thuộc địa nửa phong kiến dưới sự bảo hộ của Pháp.",
                    imageRes = getTopicImage(3, 2)
                ),
                TopicQuestion(
                    id = "modern_1_3",
                    question = "Hiệp ước Nhâm Tuất (1862) buộc triều Nguyễn phải nhượng cho Pháp những tỉnh nào?",
                    answers = listOf(
                        "Gia Định, Định Tường, Biên Hòa",
                        "Gia Định, Vĩnh Long, Hà Tiên",
                        "Hà Nội, Hải Phòng, Nam Định",
                        "Huế, Đà Nẵng, Khánh Hòa"
                    ),
                    correctAnswer = "Gia Định, Định Tường, Biên Hòa",
                    explanation = "Theo Hiệp ước Nhâm Tuất 1862, triều Nguyễn nhượng hẳn cho Pháp ba tỉnh miền Đông Nam Kỳ: Gia Định, Định Tường và Biên Hòa.",
                    imageRes = getTopicImage(3, 3)
                ),
                TopicQuestion(
                    id = "modern_1_4",
                    question = "Sau khi hoàn thành xâm lược, thực dân Pháp chia Việt Nam thành mấy kỳ để cai trị?",
                    answers = listOf(
                        "Ba kỳ: Bắc Kỳ, Trung Kỳ, Nam Kỳ",
                        "Hai kỳ: Bắc Kỳ và Nam Kỳ",
                        "Bốn kỳ: Bắc, Trung, Nam, Tây Nguyên",
                        "Một kỳ: Toàn cõi Đông Dương"
                    ),
                    correctAnswer = "Ba kỳ: Bắc Kỳ, Trung Kỳ, Nam Kỳ",
                    explanation = "Pháp chia Việt Nam thành Bắc Kỳ (bảo hộ), Trung Kỳ (nửa bảo hộ), Nam Kỳ (thuộc địa) để dễ bề cai trị.",
                    imageRes = getTopicImage(3, 4)
                ),
                TopicQuestion(
                    id = "modern_1_5",
                    question = "Mục tiêu chính của thực dân Pháp khi thiết lập bộ máy thống trị ở Việt Nam là gì?",
                    answers = listOf(
                        "Bóc lột kinh tế, khai thác thuộc địa",
                        "Truyền bá văn minh nhân loại",
                        "Giúp Việt Nam phát triển công nghiệp",
                        "Xây dựng nền dân chủ tư sản"
                    ),
                    correctAnswer = "Bóc lột kinh tế, khai thác thuộc địa",
                    explanation = "Mục tiêu cốt lõi của Pháp là biến Việt Nam thành nguồn nhân lực, nguyên liệu và thị trường tiêu thụ hàng hóa.",
                    imageRes = getTopicImage(3, 5)
                )
            )
        ),

        TopicQuizSet(
            setNumber = 2,
            setName = "Phong trào yêu nước cuối thế kỷ XIX",
            questions = listOf(
                TopicQuestion(
                    id = "modern_2_1",
                    question = "Phong trào Cần Vương bùng nổ sau sự kiện nào?",
                    answers = listOf(
                        "Vua Hàm Nghi xuống chiếu Cần Vương",
                        "Kinh thành Huế thất thủ 1885",
                        "Hiệp ước Pa-tơ-nốt được ký",
                        "Pháp đánh chiếm Hà Nội lần thứ hai"
                    ),
                    correctAnswer = "Vua Hàm Nghi xuống chiếu Cần Vương",
                    explanation = "Sau khi kinh thành Huế thất thủ (1885), vua Hàm Nghi xuống chiếu Cần Vương kêu gọi sĩ phu, nhân dân đứng lên giúp vua cứu nước.",
                    imageRes = getTopicImage(3, 6)
                ),
                TopicQuestion(
                    id = "modern_2_2",
                    question = "Lãnh tụ tiêu biểu của phong trào khởi nghĩa Hương Khê (1885–1896) là ai?",
                    answers = listOf(
                        "Phan Đình Phùng",
                        "Trương Công Định",
                        "Nguyễn Trung Trực",
                        "Hoàng Hoa Thám"
                    ),
                    correctAnswer = "Phan Đình Phùng",
                    explanation = "Phan Đình Phùng là linh hồn của khởi nghĩa Hương Khê – phong trào vũ trang mạnh nhất trong thời kỳ Cần Vương.",
                    imageRes = getTopicImage(3, 7)
                ),
                TopicQuestion(
                    id = "modern_2_3",
                    question = "Khởi nghĩa Yên Thế (1884–1913) gắn với tên tuổi của ai?",
                    answers = listOf(
                        "Hoàng Hoa Thám",
                        "Phan Bội Châu",
                        "Nguyễn Thái Học",
                        "Phan Chu Trinh"
                    ),
                    correctAnswer = "Hoàng Hoa Thám",
                    explanation = "Hoàng Hoa Thám (Đề Thám) lãnh đạo khởi nghĩa Yên Thế suốt gần 30 năm, chống Pháp ở vùng rừng núi Bắc Giang.",
                    imageRes = getTopicImage(3, 8)
                ),
                TopicQuestion(
                    id = "modern_2_4",
                    question = "Điểm chung nổi bật của phong trào Cần Vương là gì?",
                    answers = listOf(
                        "Mang tính chất phong kiến, trung quân ái quốc",
                        "Mang tính chất dân chủ tư sản",
                        "Đặt mục tiêu xây dựng xã hội cộng sản",
                        "Chỉ đấu tranh hợp pháp, ôn hòa"
                    ),
                    correctAnswer = "Mang tính chất phong kiến, trung quân ái quốc",
                    explanation = "Phong trào Cần Vương chủ yếu do sĩ phu phong kiến lãnh đạo, mục tiêu 'phò vua cứu nước'.",
                    imageRes = getTopicImage(3, 9)
                ),
                TopicQuestion(
                    id = "modern_2_5",
                    question = "Nguyên nhân chính dẫn đến thất bại của phong trào Cần Vương là gì?",
                    answers = listOf(
                        "Thiếu đường lối và tổ chức lãnh đạo thống nhất",
                        "Thiếu vũ khí hoàn toàn",
                        "Không có sự tham gia của nông dân",
                        "Do Pháp không đàn áp gắt gao"
                    ),
                    correctAnswer = "Thiếu đường lối và tổ chức lãnh đạo thống nhất",
                    explanation = "Phong trào gồm nhiều cuộc khởi nghĩa lẻ tẻ, phân tán, thiếu một tổ chức lãnh đạo chung và đường lối đúng đắn.",
                    imageRes = getTopicImage(3, 10)
                )
            )
        ),

        TopicQuizSet(
            setNumber = 3,
            setName = "Duy Tân – Đông Du – Đông Kinh Nghĩa Thục",
            questions = listOf(
                TopicQuestion(
                    id = "modern_3_1",
                    question = "Phong trào Đông Du do ai khởi xướng?",
                    answers = listOf(
                        "Phan Bội Châu",
                        "Phan Chu Trinh",
                        "Huỳnh Thúc Kháng",
                        "Nguyễn Thái Học"
                    ),
                    correctAnswer = "Phan Bội Châu",
                    explanation = "Phan Bội Châu chủ trương đưa thanh niên sang Nhật Bản học tập để chuẩn bị lực lượng cứu nước.",
                    imageRes = getTopicImage(3, 11)
                ),
                TopicQuestion(
                    id = "modern_3_2",
                    question = "Tư tưởng đấu tranh của Phan Chu Trinh chủ yếu là gì?",
                    answers = listOf(
                        "Ôn hòa, cải cách, dựa vào Pháp để canh tân đất nước",
                        "Bạo động vũ trang giành độc lập",
                        "Cách mạng vô sản",
                        "Liên minh với các nước phong kiến châu Á"
                    ),
                    correctAnswer = "Ôn hòa, cải cách, dựa vào Pháp để canh tân đất nước",
                    explanation = "Phan Chu Trinh chủ trương 'khai dân trí, chấn dân khí, hậu dân sinh', dùng cải cách để tiến tới tự chủ.",
                    imageRes = getTopicImage(3, 12)
                ),
                TopicQuestion(
                    id = "modern_3_3",
                    question = "Đông Kinh Nghĩa Thục (1907) hoạt động chủ yếu trên lĩnh vực nào?",
                    answers = listOf(
                        "Giáo dục, văn hóa",
                        "Quân sự vũ trang",
                        "Kinh tế thương nghiệp",
                        "Ngoại giao"
                    ),
                    correctAnswer = "Giáo dục, văn hóa",
                    explanation = "Đông Kinh Nghĩa Thục là trường học kiểu mới, truyền bá tư tưởng tiến bộ, nâng cao dân trí.",
                    imageRes = getTopicImage(3, 13)
                ),
                TopicQuestion(
                    id = "modern_3_4",
                    question = "Phong trào Duy Tân ở Trung Kỳ gắn với tên tuổi ai?",
                    answers = listOf(
                        "Huỳnh Thúc Kháng, Trần Quý Cáp, Phan Chu Trinh",
                        "Phan Bội Châu, Tăng Bạt Hổ",
                        "Nguyễn Thái Học, Phó Đức Chính",
                        "Hồ Chí Minh, Lê Hồng Phong"
                    ),
                    correctAnswer = "Huỳnh Thúc Kháng, Trần Quý Cáp, Phan Chu Trinh",
                    explanation = "Phong trào Duy Tân kêu gọi học tập, cải cách, chấn hưng kinh tế và xã hội.",
                    imageRes = getTopicImage(3, 14)
                ),
                TopicQuestion(
                    id = "modern_3_5",
                    question = "Nguyên nhân chung khiến các phong trào Đông Du, Duy Tân, Đông Kinh Nghĩa Thục đều thất bại là gì?",
                    answers = listOf(
                        "Chưa có đường lối cách mạng đúng đắn và tổ chức lãnh đạo thống nhất",
                        "Thiếu sự tham gia của trí thức",
                        "Không có sự đàn áp của thực dân Pháp",
                        "Do kinh tế Việt Nam phát triển quá mạnh"
                    ),
                    correctAnswer = "Chưa có đường lối cách mạng đúng đắn và tổ chức lãnh đạo thống nhất",
                    explanation = "Các phong trào mang tính yêu nước sâu sắc nhưng thiếu một đường lối cách mạng khoa học và một chính đảng lãnh đạo.",
                    imageRes = getTopicImage(3, 15)
                )
            )
        ),

        TopicQuizSet(
            setNumber = 4,
            setName = "1919–1930 – Đảng Cộng sản Việt Nam ra đời",
            questions = listOf(
                TopicQuestion(
                    id = "modern_4_1",
                    question = "Chương trình khai thác thuộc địa lần thứ hai của Pháp ở Đông Dương bắt đầu từ năm nào?",
                    answers = listOf(
                        "Năm 1919",
                        "Năm 1907",
                        "Năm 1925",
                        "Năm 1930"
                    ),
                    correctAnswer = "Năm 1919",
                    explanation = "Sau Chiến tranh thế giới thứ nhất, từ năm 1919, Pháp tiến hành khai thác thuộc địa lần thứ hai ở Đông Dương.",
                    imageRes = getTopicImage(3, 16)
                ),
                TopicQuestion(
                    id = "modern_4_2",
                    question = "Nguyễn Ái Quốc gửi 'Bản yêu sách của nhân dân An Nam' tới Hội nghị Véc-xai vào năm nào?",
                    answers = listOf(
                        "Năm 1919",
                        "Năm 1920",
                        "Năm 1923",
                        "Năm 1925"
                    ),
                    correctAnswer = "Năm 1919",
                    explanation = "Năm 1919, Nguyễn Ái Quốc thay mặt người Việt yêu nước gửi bản yêu sách đòi quyền tự do dân chủ cho dân tộc.",
                    imageRes = getTopicImage(3, 17)
                ),
                TopicQuestion(
                    id = "modern_4_3",
                    question = "Nguyễn Ái Quốc tham gia sáng lập Đảng Cộng sản Pháp vào năm nào?",
                    answers = listOf(
                        "Năm 1920",
                        "Năm 1917",
                        "Năm 1923",
                        "Năm 1930"
                    ),
                    correctAnswer = "Năm 1920",
                    explanation = "Tại Đại hội Tua (12/1920), Nguyễn Ái Quốc bỏ phiếu tán thành Quốc tế Cộng sản và tham gia sáng lập Đảng Cộng sản Pháp.",
                    imageRes = getTopicImage(3, 18)
                ),
                TopicQuestion(
                    id = "modern_4_4",
                    question = "Đảng Cộng sản Việt Nam ra đời vào ngày nào?",
                    answers = listOf(
                        "Ngày 3/2/1930",
                        "Ngày 2/9/1945",
                        "Ngày 11/11/1930",
                        "Ngày 19/8/1945"
                    ),
                    correctAnswer = "Ngày 3/2/1930",
                    explanation = "Ngày 3/2/1930, tại Cửu Long (Hồng Kông), Nguyễn Ái Quốc chủ trì hội nghị hợp nhất các tổ chức cộng sản, thành lập Đảng Cộng sản Việt Nam.",
                    imageRes = getTopicImage(3, 19)
                ),
                TopicQuestion(
                    id = "modern_4_5",
                    question = "Sự ra đời của Đảng Cộng sản Việt Nam có ý nghĩa lịch sử như thế nào?",
                    answers = listOf(
                        "Mở ra bước ngoặt mới cho cách mạng Việt Nam",
                        "Chấm dứt hoàn toàn ách đô hộ thực dân",
                        "Thống nhất đất nước ngay lập tức",
                        "Xóa bỏ chế độ phong kiến trong một năm"
                    ),
                    correctAnswer = "Mở ra bước ngoặt mới cho cách mạng Việt Nam",
                    explanation = "Đảng ra đời chấm dứt khủng hoảng về đường lối và giai cấp lãnh đạo, đưa cách mạng Việt Nam bước sang thời kỳ mới.",
                    imageRes = getTopicImage(3, 20)
                )
            )
        ),

        TopicQuizSet(
            setNumber = 5,
            setName = "Cách mạng tháng Tám 1945",
            questions = listOf(
                TopicQuestion(
                    id = "modern_5_1",
                    question = "Mặt trận Việt Minh được thành lập vào năm nào?",
                    answers = listOf(
                        "Năm 1941",
                        "Năm 1930",
                        "Năm 1945",
                        "Năm 1936"
                    ),
                    correctAnswer = "Năm 1941",
                    explanation = "Tháng 5/1941, Hội nghị Trung ương 8 quyết định thành lập Mặt trận Việt Minh để tập hợp lực lượng toàn dân.",
                    imageRes = getTopicImage(3, 21)
                ),
                TopicQuestion(
                    id = "modern_5_2",
                    question = "Sự kiện nào đánh dấu thời cơ 'ngàn năm có một' cho Cách mạng tháng Tám?",
                    answers = listOf(
                        "Nhật đầu hàng Đồng minh (8/1945)",
                        "Nhật đảo chính Pháp (3/1945)",
                        "Thế chiến thứ hai bùng nổ",
                        "Pháp ký Hiệp ước Pa-tơ-nốt"
                    ),
                    correctAnswer = "Nhật đầu hàng Đồng minh (8/1945)",
                    explanation = "Tháng 8/1945, phát xít Nhật đầu hàng Đồng minh, chính quyền bù nhìn tan rã, tạo thời cơ thuận lợi cho cách mạng.",
                    imageRes = getTopicImage(3, 22)
                ),
                TopicQuestion(
                    id = "modern_5_3",
                    question = "Ngày nào được coi là ngày thắng lợi của Cách mạng tháng Tám ở Hà Nội?",
                    answers = listOf(
                        "Ngày 19/8/1945",
                        "Ngày 2/9/1945",
                        "Ngày 23/8/1945",
                        "Ngày 25/8/1945"
                    ),
                    correctAnswer = "Ngày 19/8/1945",
                    explanation = "Ngày 19/8/1945, nhân dân Hà Nội tổng khởi nghĩa giành chính quyền, mở đầu cao trào cách mạng cả nước.",
                    imageRes = getTopicImage(3, 23)
                ),
                TopicQuestion(
                    id = "modern_5_4",
                    question = "Chủ tịch Hồ Chí Minh đọc Tuyên ngôn độc lập tại quảng trường Ba Đình vào ngày nào?",
                    answers = listOf(
                        "Ngày 2/9/1945",
                        "Ngày 19/8/1945",
                        "Ngày 3/2/1930",
                        "Ngày 7/5/1954"
                    ),
                    correctAnswer = "Ngày 2/9/1945",
                    explanation = "Ngày 2/9/1945, Chủ tịch Hồ Chí Minh đọc Tuyên ngôn độc lập, khai sinh nước Việt Nam Dân chủ Cộng hòa.",
                    imageRes = getTopicImage(3, 24)
                ),
                TopicQuestion(
                    id = "modern_5_5",
                    question = "Ý nghĩa lớn nhất của Cách mạng tháng Tám 1945 là gì?",
                    answers = listOf(
                        "Lật đổ ách thống trị của đế quốc, phong kiến, lập nên nhà nước Việt Nam Dân chủ Cộng hòa",
                        "Chấm dứt hoàn toàn chiến tranh trên thế giới",
                        "Thực hiện ngay công nghiệp hóa đất nước",
                        "Giải phóng toàn bộ Đông Dương"
                    ),
                    correctAnswer = "Lật đổ ách thống trị của đế quốc, phong kiến, lập nên nhà nước Việt Nam Dân chủ Cộng hòa",
                    explanation = "Cách mạng tháng Tám giành chính quyền về tay nhân dân, mở ra kỷ nguyên độc lập dân tộc gắn với chủ nghĩa xã hội.",
                    imageRes = getTopicImage(3, 25)
                )
            )
        )
    )
    // ==================== CHỦ ĐỀ 4: THỜI HIỆN ĐẠI ====================
    private val contemporaryQuizzes = listOf(
        TopicQuizSet(
            setNumber = 1,
            setName = "Kháng chiến chống Pháp",
            questions = listOf(
                TopicQuestion(
                    id = "contemporary_1_1",
                    question = "Toàn quốc kháng chiến chống Pháp bùng nổ vào ngày nào?",
                    answers = listOf(
                        "19/12/1946",
                        "23/9/1945",
                        "7/5/1954",
                        "2/9/1945"
                    ),
                    correctAnswer = "19/12/1946",
                    explanation = "Ngày 19/12/1946, Chủ tịch Hồ Chí Minh ra Lời kêu gọi toàn quốc kháng chiến, mở đầu cuộc kháng chiến chống Pháp.",
                    imageRes = getTopicImage(4, 1)
                ),
                TopicQuestion(
                    id = "contemporary_1_2",
                    question = "Chiến dịch nào là thắng lợi quyết định kết thúc kháng chiến chống Pháp?",
                    answers = listOf(
                        "Chiến dịch Điện Biên Phủ",
                        "Chiến dịch Việt Bắc",
                        "Chiến dịch Biên giới 1950",
                        "Chiến dịch Hòa Bình"
                    ),
                    correctAnswer = "Chiến dịch Điện Biên Phủ",
                    explanation = "Chiến dịch Điện Biên Phủ (1954) đập tan tập đoàn cứ điểm mạnh nhất của Pháp, buộc Pháp phải ký Hiệp định Giơ-ne-vơ.",
                    imageRes = getTopicImage(4, 2)
                ),
                TopicQuestion(
                    id = "contemporary_1_3",
                    question = "Hiệp định nào chấm dứt chiến tranh và lập lại hòa bình ở Đông Dương năm 1954?",
                    answers = listOf("Hiệp định Giơ-ne-vơ", "Hiệp định Paris", "Hiệp định Sơ bộ", "Hiệp định Pa-tơ-nốt"),
                    correctAnswer = "Hiệp định Giơ-ne-vơ",
                    explanation = "Hiệp định Giơ-ne-vơ ký ngày 21/7/1954, chấm dứt chiến tranh Đông Dương.",
                    imageRes = getTopicImage(4, 3)
                ),
                TopicQuestion(
                    id = "contemporary_1_4",
                    question = "Ai là Tổng chỉ huy Chiến dịch Điện Biên Phủ?",
                    answers = listOf("Võ Nguyên Giáp", "Hoàng Văn Thái", "Trần Đăng Ninh", "Nguyễn Chí Thanh"),
                    correctAnswer = "Võ Nguyên Giáp",
                    explanation = "Đại tướng Võ Nguyên Giáp là Tổng tư lệnh chiến dịch, đưa ra quyết định chiến lược 'đánh chắc tiến chắc'.",
                    imageRes = getTopicImage(4, 4)
                ),
                TopicQuestion(
                    id = "contemporary_1_5",
                    question = "Chiến dịch Việt Bắc Thu-Đông 1947 của ta nhằm mục đích gì?",
                    answers = listOf(
                        "Phá tan âm mưu 'đánh nhanh thắng nhanh' của Pháp",
                        "Giải phóng toàn bộ Nam Bộ",
                        "Tấn công Hà Nội",
                        "Tiêu diệt quân chủ lực Pháp ở Điện Biên"
                    ),
                    correctAnswer = "Phá tan âm mưu 'đánh nhanh thắng nhanh' của Pháp",
                    explanation = "Chiến dịch nhằm phá kế hoạch đánh úp căn cứ địa Việt Bắc của Pháp.",
                    imageRes = getTopicImage(4, 5)
                )
            )
        ),

        TopicQuizSet(
            setNumber = 2,
            setName = "Miền Bắc – Miền Nam (1954–1975)",
            questions = listOf(
                TopicQuestion(
                    id = "contemporary_2_1",
                    question = "Sự kiện mở đầu cuộc kháng chiến chống Mỹ cứu nước (1954–1975) là gì?",
                    answers = listOf(
                        "Mỹ – Diệm đàn áp phong trào cách mạng ở miền Nam",
                        "Chiến dịch Hồ Chí Minh",
                        "Phong trào Đồng Khởi",
                        "Thiệu – Kỳ lên nắm quyền"
                    ),
                    correctAnswer = "Mỹ – Diệm đàn áp phong trào cách mạng ở miền Nam",
                    explanation = "Ngay sau 1954, chính quyền Mỹ – Diệm thẳng tay đàn áp lực lượng cách mạng ở miền Nam.",
                    imageRes = getTopicImage(4, 6)
                ),
                TopicQuestion(
                    id = "contemporary_2_2",
                    question = "Phong trào 'Đồng Khởi' nổ ra mạnh nhất ở tỉnh nào?",
                    answers = listOf("Bến Tre", "Long An", "Trà Vinh", "Kon Tum"),
                    correctAnswer = "Bến Tre",
                    explanation = "Bến Tre là nơi bùng nổ mạnh nhất vào năm 1960, mở đầu cao trào Đồng Khởi.",
                    imageRes = getTopicImage(4, 7)
                ),
                TopicQuestion(
                    id = "contemporary_2_3",
                    question = "Mặt trận Dân tộc Giải phóng miền Nam Việt Nam được thành lập năm nào?",
                    answers = listOf("1960", "1959", "1965", "1973"),
                    correctAnswer = "1960",
                    explanation = "Mặt trận Dân tộc Giải phóng miền Nam Việt Nam thành lập ngày 20/12/1960.",
                    imageRes = getTopicImage(4, 8)
                ),
                TopicQuestion(
                    id = "contemporary_2_4",
                    question = "Miền Bắc bắt đầu bước vào thời kỳ xây dựng chủ nghĩa xã hội từ năm nào?",
                    answers = listOf("1954", "1960", "1945", "1975"),
                    correctAnswer = "1954",
                    explanation = "Sau 1954, miền Bắc đi lên CNXH, trở thành hậu phương lớn của cả nước.",
                    imageRes = getTopicImage(4, 9)
                ),
                TopicQuestion(
                    id = "contemporary_2_5",
                    question = "Mỹ thực hiện chiến lược 'Chiến tranh đặc biệt' trong giai đoạn nào?",
                    answers = listOf("1961–1965", "1954–1960", "1965–1968", "1969–1973"),
                    correctAnswer = "1961–1965",
                    explanation = "'Chiến tranh đặc biệt' là chiến lược dùng quân đội Sài Gòn làm lực lượng chủ yếu.",
                    imageRes = getTopicImage(4, 10)
                )
            )
        ),

        TopicQuizSet(
            setNumber = 3,
            setName = "Kháng chiến chống Mỹ",
            questions = listOf(
                TopicQuestion(
                    id = "contemporary_3_1",
                    question = "Sự kiện được coi là mở đầu 'Chiến tranh cục bộ' của Mỹ ở miền Nam?",
                    answers = listOf("Mỹ đổ quân vào Đà Nẵng (1965)", "Tết Mậu Thân 1968", "Điện Biên Phủ trên không", "Ký Hiệp định Paris"),
                    correctAnswer = "Mỹ đổ quân vào Đà Nẵng (1965)",
                    explanation = "Ngày 8/3/1965, Mỹ đưa quân viễn chinh vào Đà Nẵng, mở đầu 'Chiến tranh cục bộ'.",
                    imageRes = getTopicImage(4, 11)
                ),
                TopicQuestion(
                    id = "contemporary_3_2",
                    question = "Cuộc tổng tiến công nào đã làm lung lay ý chí xâm lược của Mỹ?",
                    answers = listOf("Tết Mậu Thân 1968", "Đồng Khởi", "Điện Biên phủ trên không", "Chiến dịch Xuân–Hè 1972"),
                    correctAnswer = "Tết Mậu Thân 1968",
                    explanation = "Cuộc Tổng tiến công và nổi dậy Tết Mậu Thân 1968 gây rung chuyển toàn bộ nước Mỹ.",
                    imageRes = getTopicImage(4, 12)
                ),
                TopicQuestion(
                    id = "contemporary_3_3",
                    question = "Chiến dịch 'Điện Biên Phủ trên không' diễn ra vào năm nào?",
                    answers = listOf("1972", "1968", "1975", "1973"),
                    correctAnswer = "1972",
                    explanation = "Tháng 12/1972, Mỹ ném bom B52 xuống Hà Nội – Hải Phòng, nhưng bị thất bại nặng nề.",
                    imageRes = getTopicImage(4, 13)
                ),
                TopicQuestion(
                    id = "contemporary_3_4",
                    question = "Hiệp định Paris về chấm dứt chiến tranh ở Việt Nam được ký vào ngày nào?",
                    answers = listOf("27/1/1973", "30/4/1975", "2/9/1945", "20/12/1960"),
                    correctAnswer = "27/1/1973",
                    explanation = "Hiệp định Paris buộc Mỹ phải rút quân khỏi Việt Nam.",
                    imageRes = getTopicImage(4, 14)
                ),
                TopicQuestion(
                    id = "contemporary_3_5",
                    question = "Sự kiện nào đánh dấu thắng lợi hoàn toàn của cuộc kháng chiến chống Mỹ?",
                    answers = listOf("Chiến dịch Hồ Chí Minh (1975)", "Điện Biên Phủ trên không", "Đồng Khởi", "Mậu Thân 1968"),
                    correctAnswer = "Chiến dịch Hồ Chí Minh (1975)",
                    explanation = "Ngày 30/4/1975, quân ta giải phóng Sài Gòn, thống nhất đất nước.",
                    imageRes = getTopicImage(4, 15)
                )
            )
        ),

        TopicQuizSet(
            setNumber = 4,
            setName = "Sau 1975 – Đổi mới",
            questions = listOf(
                TopicQuestion(
                    id = "contemporary_4_1",
                    question = "Đất nước Việt Nam hoàn toàn thống nhất vào năm nào?",
                    answers = listOf("1975", "1976", "1980", "1995"),
                    correctAnswer = "1976",
                    explanation = "Năm 1976, Quốc hội quyết định đặt tên nước là Cộng hòa Xã hội Chủ nghĩa Việt Nam, thống nhất hệ thống nhà nước.",
                    imageRes = getTopicImage(4, 16)
                ),
                TopicQuestion(
                    id = "contemporary_4_2",
                    question = "Đường lối Đổi mới được thông qua tại Đại hội Đảng lần thứ mấy?",
                    answers = listOf("Đại hội VI (1986)", "Đại hội IV", "Đại hội V", "Đại hội VII"),
                    correctAnswer = "Đại hội VI (1986)",
                    explanation = "Đại hội VI năm 1986 đề ra đường lối Đổi mới toàn diện đất nước.",
                    imageRes = getTopicImage(4, 17)
                ),
                TopicQuestion(
                    id = "contemporary_4_3",
                    question = "Mục tiêu lớn nhất của đường lối Đổi mới là gì?",
                    answers = listOf(
                        "Xây dựng nền kinh tế thị trường định hướng XHCN",
                        "Phát triển quân sự",
                        "Thay đổi toàn bộ lãnh thổ",
                        "Tuyệt đối hóa nhà nước bao cấp"
                    ),
                    correctAnswer = "Xây dựng nền kinh tế thị trường định hướng XHCN",
                    explanation = "Đổi mới đặt trọng tâm vào cải cách kinh tế theo mô hình thị trường.",
                    imageRes = getTopicImage(4, 18)
                ),
                TopicQuestion(
                    id = "contemporary_4_4",
                    question = "Việt Nam gia nhập ASEAN vào năm nào?",
                    answers = listOf("1995", "2000", "1986", "1991"),
                    correctAnswer = "1995",
                    explanation = "Ngày 28/7/1995, Việt Nam chính thức trở thành thành viên ASEAN.",
                    imageRes = getTopicImage(4, 19)
                ),
                TopicQuestion(
                    id = "contemporary_4_5",
                    question = "Hiến pháp hiện hành của Việt Nam được ban hành vào năm nào?",
                    answers = listOf("2013", "1992", "1980", "1959"),
                    correctAnswer = "2013",
                    explanation = "Hiến pháp 2013 khẳng định vai trò của nhà nước pháp quyền và quyền công dân.",
                    imageRes = getTopicImage(4, 20)
                )
            )
        ),

        TopicQuizSet(
            setNumber = 5,
            setName = "Tổng hợp Hiện Đại",
            questions = listOf(
                TopicQuestion(
                    id = "contemporary_5_1",
                    question = "Cuộc chiến tranh nào kéo dài nhất trong lịch sử Việt Nam thế kỷ XX?",
                    answers = listOf("Kháng chiến chống Mỹ", "Kháng chiến chống Pháp", "Chiến tranh biên giới", "Điện Biên Phủ"),
                    correctAnswer = "Kháng chiến chống Mỹ",
                    explanation = "Kháng chiến chống Mỹ kéo dài 21 năm (1954–1975).",
                    imageRes = getTopicImage(4, 21)
                ),
                TopicQuestion(
                    id = "contemporary_5_2",
                    question = "Ngày 30/4/1975 còn được gọi là gì?",
                    answers = listOf(
                        "Ngày Giải phóng miền Nam",
                        "Ngày Toàn quốc kháng chiến",
                        "Ngày Quốc khánh",
                        "Ngày Thống nhất đất nước"
                    ),
                    correctAnswer = "Ngày Giải phóng miền Nam",
                    explanation = "Ngày 30/4 là ngày giải phóng miền Nam, kết thúc chiến tranh.",
                    imageRes = getTopicImage(4, 22)
                ),
                TopicQuestion(
                    id = "contemporary_5_3",
                    question = "Đâu là thành tựu quan trọng của Việt Nam sau Đổi mới?",
                    answers = listOf("Kinh tế phát triển mạnh mẽ", "Thất nghiệp tăng cao", "Xuất khẩu sụt giảm", "Nền kinh tế khủng hoảng"),
                    correctAnswer = "Kinh tế phát triển mạnh mẽ",
                    explanation = "Sau Đổi mới, Việt Nam trở thành một trong những nền kinh tế phát triển nhanh tại châu Á.",
                    imageRes = getTopicImage(4, 23)
                ),
                TopicQuestion(
                    id = "contemporary_5_4",
                    question = "Việt Nam thiết lập quan hệ ngoại giao với Hoa Kỳ vào năm nào?",
                    answers = listOf("1995", "1986", "2005", "2000"),
                    correctAnswer = "1995",
                    explanation = "Quan hệ Việt – Mỹ chính thức bình thường hóa vào năm 1995.",
                    imageRes = getTopicImage(4, 24)
                ),
                TopicQuestion(
                    id = "contemporary_5_5",
                    question = "Việt Nam trở thành thành viên WTO vào năm nào?",
                    answers = listOf("2007", "1995", "2013", "2000"),
                    correctAnswer = "2007",
                    explanation = "Ngày 11/1/2007, Việt Nam chính thức gia nhập Tổ chức Thương mại Thế giới (WTO).",
                    imageRes = getTopicImage(4, 25)
                )
            )
        )
    )

    // ==================== CHỦ ĐỀ 5: NHÂN VẬT LỊCH SỬ ====================
    private val historicalFiguresQuizzes = listOf(
        TopicQuizSet(
            setNumber = 1,
            setName = "Vua và anh hùng dân tộc",
            questions = listOf(
                TopicQuestion(
                    id = "figures_1_1",
                    question = "Ngô Quyền được lịch sử ghi nhận với chiến thắng nào?",
                    answers = listOf(
                        "Chiến thắng Bạch Đằng năm 938",
                        "Chiến thắng Chi Lăng năm 1427",
                        "Chiến thắng Rạch Gầm – Xoài Mút",
                        "Chiến thắng Đống Đa"
                    ),
                    correctAnswer = "Chiến thắng Bạch Đằng năm 938",
                    explanation = "Ngô Quyền lãnh đạo chiến thắng Bạch Đằng năm 938, chấm dứt cơ bản hơn 1000 năm Bắc thuộc.",
                    imageRes = getTopicImage(5, 1)
                ),
                TopicQuestion(
                    id = "figures_1_2",
                    question = "Vị vua nào ban 'Chiếu dời đô' và dời đô ra Thăng Long năm 1010?",
                    answers = listOf(
                        "Lý Thái Tổ",
                        "Lý Thánh Tông",
                        "Trần Thái Tông",
                        "Lê Thánh Tông"
                    ),
                    correctAnswer = "Lý Thái Tổ",
                    explanation = "Lý Công Uẩn (Lý Thái Tổ) ban Chiếu dời đô, chọn Thăng Long làm kinh đô mới.",
                    imageRes = getTopicImage(5, 2)
                ),
                TopicQuestion(
                    id = "figures_1_3",
                    question = "Trần Hưng Đạo là người chỉ huy quân dân Đại Việt trong những cuộc kháng chiến nào?",
                    answers = listOf(
                        "Ba lần kháng chiến chống Nguyên Mông",
                        "Kháng chiến chống Minh",
                        "Kháng chiến chống Pháp",
                        "Kháng chiến chống Tống"
                    ),
                    correctAnswer = "Ba lần kháng chiến chống Nguyên Mông",
                    explanation = "Trần Hưng Đạo là Quốc công Tiết chế, chỉ huy quân dân Đại Việt chống quân Nguyên Mông thế kỷ XIII.",
                    imageRes = getTopicImage(5, 3)
                ),
                TopicQuestion(
                    id = "figures_1_4",
                    question = "Lê Lợi là lãnh tụ cuộc khởi nghĩa nào?",
                    answers = listOf(
                        "Khởi nghĩa Lam Sơn",
                        "Khởi nghĩa Tây Sơn",
                        "Khởi nghĩa Yên Thế",
                        "Khởi nghĩa Hương Khê"
                    ),
                    correctAnswer = "Khởi nghĩa Lam Sơn",
                    explanation = "Lê Lợi lãnh đạo khởi nghĩa Lam Sơn (1418–1427), đánh đuổi quân Minh, lập ra nhà Lê sơ.",
                    imageRes = getTopicImage(5, 4)
                ),
                TopicQuestion(
                    id = "figures_1_5",
                    question = "Vua Quang Trung (Nguyễn Huệ) nổi tiếng với chiến thắng nào?",
                    answers = listOf(
                        "Chiến thắng Ngọc Hồi – Đống Đa năm 1789",
                        "Chiến thắng Bạch Đằng",
                        "Chiến thắng Chi Lăng – Xương Giang",
                        "Chiến thắng Điện Biên Phủ"
                    ),
                    correctAnswer = "Chiến thắng Ngọc Hồi – Đống Đa năm 1789",
                    explanation = "Năm 1789, Quang Trung đại phá quân Thanh trong trận Ngọc Hồi – Đống Đa, bảo vệ nền độc lập.",
                    imageRes = getTopicImage(5, 5)
                )
            )
        ),

        TopicQuizSet(
            setNumber = 2,
            setName = "Danh nhân văn hóa",
            questions = listOf(
                TopicQuestion(
                    id = "figures_2_1",
                    question = "Ai được UNESCO tôn vinh là 'Danh nhân văn hóa thế giới' và là tác giả 'Bình Ngô đại cáo'?",
                    answers = listOf(
                        "Nguyễn Trãi",
                        "Nguyễn Du",
                        "Chu Văn An",
                        "Nguyễn Bỉnh Khiêm"
                    ),
                    correctAnswer = "Nguyễn Trãi",
                    explanation = "Nguyễn Trãi là mưu sĩ của Lê Lợi, tác giả 'Bình Ngô đại cáo', được UNESCO vinh danh năm 1980.",
                    imageRes = getTopicImage(5, 6)
                ),
                TopicQuestion(
                    id = "figures_2_2",
                    question = "Nguyễn Du là tác giả của tác phẩm văn học nổi tiếng nào?",
                    answers = listOf(
                        "Truyện Kiều",
                        "Lục Vân Tiên",
                        "Chinh phụ ngâm",
                        "Cung oán ngâm khúc"
                    ),
                    correctAnswer = "Truyện Kiều",
                    explanation = "Nguyễn Du là tác giả 'Truyện Kiều', kiệt tác của văn học trung đại Việt Nam.",
                    imageRes = getTopicImage(5, 7)
                ),
                TopicQuestion(
                    id = "figures_2_3",
                    question = "Chu Văn An nổi tiếng với đức tính gì?",
                    answers = listOf(
                        "Thanh liêm, chính trực, dâng 'Thất trảm sớ'",
                        "Giàu có, quyền lực",
                        "Giỏi võ nghệ",
                        "Là nhà buôn lớn"
                    ),
                    correctAnswer = "Thanh liêm, chính trực, dâng 'Thất trảm sớ'",
                    explanation = "Chu Văn An là nhà giáo, nhà nho mẫu mực, dâng 'Thất trảm sớ' xin chém 7 nịnh thần thời Trần.",
                    imageRes = getTopicImage(5, 8)
                ),
                TopicQuestion(
                    id = "figures_2_4",
                    question = "Nguyễn Bỉnh Khiêm được nhân dân gọi bằng tên gì?",
                    answers = listOf(
                        "Trạng Trình",
                        "Trạng Quỳnh",
                        "Ông Đồ Nghệ",
                        "Ông Đồ Ba"
                    ),
                    correctAnswer = "Trạng Trình",
                    explanation = "Nguyễn Bỉnh Khiêm (Trạng Trình) nổi tiếng là bậc hiền triết, có nhiều lời sấm truyền.",
                    imageRes = getTopicImage(5, 9)
                ),
                TopicQuestion(
                    id = "figures_2_5",
                    question = "Danh nhân nào được mệnh danh là 'Ông đồ mù yêu nước' ở thế kỷ XIX?",
                    answers = listOf(
                        "Nguyễn Đình Chiểu",
                        "Phan Bội Châu",
                        "Phan Chu Trinh",
                        "Huỳnh Thúc Kháng"
                    ),
                    correctAnswer = "Nguyễn Đình Chiểu",
                    explanation = "Nguyễn Đình Chiểu tuy mù nhưng vẫn sáng ngời tinh thần yêu nước, là tác giả 'Văn tế nghĩa sĩ Cần Giuộc'.",
                    imageRes = getTopicImage(5, 10)
                )
            )
        ),

        TopicQuizSet(
            setNumber = 3,
            setName = "Nhà yêu nước cận đại",
            questions = listOf(
                TopicQuestion(
                    id = "figures_3_1",
                    question = "Ai là người khởi xướng phong trào Đông Du?",
                    answers = listOf(
                        "Phan Bội Châu",
                        "Phan Chu Trinh",
                        "Nguyễn Thái Học",
                        "Huỳnh Thúc Kháng"
                    ),
                    correctAnswer = "Phan Bội Châu",
                    explanation = "Phan Bội Châu lập Hội Duy Tân, khởi xướng phong trào Đông Du, gửi thanh niên sang Nhật.",
                    imageRes = getTopicImage(5, 11)
                ),
                TopicQuestion(
                    id = "figures_3_2",
                    question = "Phan Chu Trinh chủ trương con đường cứu nước như thế nào?",
                    answers = listOf(
                        "Cải cách, ôn hòa, dựa vào Pháp để canh tân đất nước",
                        "Bạo động vũ trang ngay lập tức",
                        "Liên minh với phát xít Nhật",
                        "Cô lập, không giao lưu với bên ngoài"
                    ),
                    correctAnswer = "Cải cách, ôn hòa, dựa vào Pháp để canh tân đất nước",
                    explanation = "Phan Chu Trinh đề cao 'khai dân trí, chấn dân khí, hậu dân sinh', cải cách để tiến tới tự chủ.",
                    imageRes = getTopicImage(5, 12)
                ),
                TopicQuestion(
                    id = "figures_3_3",
                    question = "Nguyễn Thái Học là lãnh tụ của tổ chức nào?",
                    answers = listOf(
                        "Việt Nam Quốc dân Đảng",
                        "Hội Việt Nam Cách mạng Thanh niên",
                        "Đông Kinh Nghĩa Thục",
                        "Đảng Cộng sản Đông Dương"
                    ),
                    correctAnswer = "Việt Nam Quốc dân Đảng",
                    explanation = "Nguyễn Thái Học lãnh đạo Việt Nam Quốc dân Đảng, tổ chức Khởi nghĩa Yên Bái năm 1930.",
                    imageRes = getTopicImage(5, 13)
                ),
                TopicQuestion(
                    id = "figures_3_4",
                    question = "Trương Định là lãnh tụ cuộc khởi nghĩa chống Pháp ở vùng nào?",
                    answers = listOf(
                        "Nam Bộ",
                        "Bắc Bộ",
                        "Trung Bộ",
                        "Tây Nguyên"
                    ),
                    correctAnswer = "Nam Bộ",
                    explanation = "Trương Định lãnh đạo nghĩa quân chống Pháp tại Gia Định và vùng Nam Bộ sau Hiệp ước 1862.",
                    imageRes = getTopicImage(5, 14)
                ),
                TopicQuestion(
                    id = "figures_3_5",
                    question = "Nhà yêu nước nào từng giữ chức 'Quyền Toàn quyền Trung Kỳ' do Pháp bổ nhiệm nhưng từ chức để phản đối?",
                    answers = listOf(
                        "Huỳnh Thúc Kháng",
                        "Phan Bội Châu",
                        "Nguyễn Ái Quốc",
                        "Phan Chu Trinh"
                    ),
                    correctAnswer = "Huỳnh Thúc Kháng",
                    explanation = "Huỳnh Thúc Kháng từng làm Quyền Toàn quyền Trung Kỳ, sau từ chức vì bất bình với chính sách cai trị.",
                    imageRes = getTopicImage(5, 15)
                )
            )
        ),

        TopicQuizSet(
            setNumber = 4,
            setName = "Lãnh tụ cách mạng",
            questions = listOf(
                TopicQuestion(
                    id = "figures_4_1",
                    question = "Chủ tịch Hồ Chí Minh đọc Tuyên ngôn độc lập khai sinh nước Việt Nam Dân chủ Cộng hòa vào ngày nào?",
                    answers = listOf(
                        "2/9/1945",
                        "3/2/1930",
                        "19/8/1945",
                        "7/5/1954"
                    ),
                    correctAnswer = "2/9/1945",
                    explanation = "Ngày 2/9/1945, tại quảng trường Ba Đình, Chủ tịch Hồ Chí Minh tuyên bố độc lập cho dân tộc.",
                    imageRes = getTopicImage(5, 16)
                ),
                TopicQuestion(
                    id = "figures_4_2",
                    question = "Ai là Tổng Bí thư đầu tiên của Đảng Cộng sản Việt Nam?",
                    answers = listOf(
                        "Trần Phú",
                        "Lê Hồng Phong",
                        "Nguyễn Văn Cừ",
                        "Hồ Chí Minh"
                    ),
                    correctAnswer = "Trần Phú",
                    explanation = "Trần Phú là Tổng Bí thư đầu tiên của Đảng (1930–1931), tác giả Luận cương chính trị 1930.",
                    imageRes = getTopicImage(5, 17)
                ),
                TopicQuestion(
                    id = "figures_4_3",
                    question = "Đại tướng Võ Nguyên Giáp gắn liền với cương vị nào trong quân đội nhân dân Việt Nam?",
                    answers = listOf(
                        "Tổng Tư lệnh Quân đội nhân dân Việt Nam",
                        "Chủ nhiệm Tổng cục Chính trị",
                        "Chính ủy trung đoàn",
                        "Chỉ huy địa phương"
                    ),
                    correctAnswer = "Tổng Tư lệnh Quân đội nhân dân Việt Nam",
                    explanation = "Võ Nguyên Giáp là vị Đại tướng đầu tiên, Tổng Tư lệnh Quân đội nhân dân Việt Nam.",
                    imageRes = getTopicImage(5, 18)
                ),
                TopicQuestion(
                    id = "figures_4_4",
                    question = "Tôn Đức Thắng từng giữ chức vụ gì trong bộ máy nhà nước Việt Nam?",
                    answers = listOf(
                        "Chủ tịch nước Cộng hòa Xã hội Chủ nghĩa Việt Nam",
                        "Thủ tướng Chính phủ",
                        "Chủ tịch Quốc hội",
                        "Tổng Bí thư"
                    ),
                    correctAnswer = "Chủ tịch nước Cộng hòa Xã hội Chủ nghĩa Việt Nam",
                    explanation = "Sau Chủ tịch Hồ Chí Minh, Tôn Đức Thắng là Chủ tịch nước CHXHCN Việt Nam.",
                    imageRes = getTopicImage(5, 19)
                ),
                TopicQuestion(
                    id = "figures_4_5",
                    question = "Đồng chí Lê Duẩn gắn liền với vai trò nào trong kháng chiến chống Mỹ?",
                    answers = listOf(
                        "Bí thư thứ nhất/Tổng Bí thư, lãnh đạo đường lối chống Mỹ cứu nước",
                        "Tư lệnh chiến dịch Điện Biên Phủ",
                        "Chủ tịch Quốc hội",
                        "Chủ tịch nước"
                    ),
                    correctAnswer = "Bí thư thứ nhất/Tổng Bí thư, lãnh đạo đường lối chống Mỹ cứu nước",
                    explanation = "Lê Duẩn là người đứng đầu Đảng, cùng tập thể Trung ương bàn và chỉ đạo đường lối kháng chiến chống Mỹ.",
                    imageRes = getTopicImage(5, 20)
                )
            )
        ),

        TopicQuizSet(
            setNumber = 5,
            setName = "Anh hùng, liệt sĩ tiêu biểu",
            questions = listOf(
                TopicQuestion(
                    id = "figures_5_1",
                    question = "Anh hùng liệt sĩ Võ Thị Sáu tham gia chiến đấu chủ yếu ở vùng nào?",
                    answers = listOf(
                        "Bà Rịa – Vũng Tàu",
                        "Cà Mau",
                        "Đồng Tháp",
                        "Lạng Sơn"
                    ),
                    correctAnswer = "Bà Rịa – Vũng Tàu",
                    explanation = "Võ Thị Sáu tham gia đội công tác vũ trang ở vùng Đất Đỏ, Bà Rịa – Vũng Tàu.",
                    imageRes = getTopicImage(5, 21)
                ),
                TopicQuestion(
                    id = "figures_5_2",
                    question = "Nguyễn Văn Trỗi là người thực hiện nhiệm vụ gì trong kháng chiến chống Mỹ?",
                    answers = listOf(
                        "Mưu sát Bộ trưởng Quốc phòng Mỹ Mắc Na-ma-ra",
                        "Tấn công dinh Độc Lập",
                        "Đánh kho xăng Nhà Bè",
                        "Đánh cầu Biên Hòa"
                    ),
                    correctAnswer = "Mưu sát Bộ trưởng Quốc phòng Mỹ Mắc Na-ma-ra",
                    explanation = "Nguyễn Văn Trỗi nhận nhiệm vụ đánh mìn ám sát Mắc Na-ma-ra tại cầu Công Lý (Sài Gòn).",
                    imageRes = getTopicImage(5, 22)
                ),
                TopicQuestion(
                    id = "figures_5_3",
                    question = "Nguyễn Thị Định là nữ tướng gắn với phong trào nào?",
                    answers = listOf(
                        "Phong trào Đồng Khởi Bến Tre",
                        "Khởi nghĩa Nam Kỳ",
                        "Khởi nghĩa Yên Bái",
                        "Đông Du"
                    ),
                    correctAnswer = "Phong trào Đồng Khởi Bến Tre",
                    explanation = "Bà Nguyễn Thị Định là 'Nữ tướng Đồng Khởi', lãnh đạo phong trào ở Bến Tre.",
                    imageRes = getTopicImage(5, 23)
                ),
                TopicQuestion(
                    id = "figures_5_4",
                    question = "Trần Đại Nghĩa là ai trong lịch sử hiện đại Việt Nam?",
                    answers = listOf(
                        "Nhà khoa học – kỹ sư quân sự, chế tạo vũ khí cho kháng chiến",
                        "Nhà thơ yêu nước",
                        "Nhà soạn nhạc cách mạng",
                        "Nhà ngoại giao"
                    ),
                    correctAnswer = "Nhà khoa học – kỹ sư quân sự, chế tạo vũ khí cho kháng chiến",
                    explanation = "Trần Đại Nghĩa là kỹ sư vũ khí, có công lớn trong việc nghiên cứu chế tạo súng, đạn cho quân đội.",
                    imageRes = getTopicImage(5, 24)
                ),
                TopicQuestion(
                    id = "figures_5_5",
                    question = "Bác sĩ Tôn Thất Tùng nổi tiếng với thành tựu nào?",
                    answers = listOf(
                        "Phương pháp mổ gan đặc biệt mang tên ông",
                        "Khám phá vắc-xin bại liệt",
                        "Phát minh thuốc sốt rét",
                        "Tìm ra penicillin"
                    ),
                    correctAnswer = "Phương pháp mổ gan đặc biệt mang tên ông",
                    explanation = "Giáo sư – bác sĩ Tôn Thất Tùng nổi tiếng với phương pháp phẫu thuật gan 'Tôn Thất Tùng' được quốc tế công nhận.",
                    imageRes = getTopicImage(5, 25)
                )
            )
        )
    )


    // ==================== CHỦ ĐỀ 6: DI TÍCH – DI SẢN ====================
    private val heritageCultureQuizzes = listOf(
        TopicQuizSet(
            setNumber = 1,
            setName = "Di sản văn hóa thế giới",
            questions = listOf(
                TopicQuestion(
                    id = "heritage_1_1",
                    question = "Quần thể di tích nào được UNESCO công nhận là Di sản Văn hóa Thế giới đầu tiên của Việt Nam?",
                    answers = listOf("Cố đô Huế", "Phố cổ Hội An", "Thánh địa Mỹ Sơn", "Hoàng thành Thăng Long"),
                    correctAnswer = "Cố đô Huế",
                    explanation = "Cố đô Huế được UNESCO công nhận năm 1993, là di sản thế giới đầu tiên của Việt Nam.",
                    imageRes = getTopicImage(6, 1)
                ),
                TopicQuestion(
                    id = "heritage_1_2",
                    question = "Phố cổ Hội An được UNESCO công nhận là di sản vào năm nào?",
                    answers = listOf("1999", "1993", "2003", "2010"),
                    correctAnswer = "1999",
                    explanation = "Năm 1999, Phố cổ Hội An được UNESCO công nhận là Di sản Văn hóa Thế giới.",
                    imageRes = getTopicImage(6, 2)
                ),
                TopicQuestion(
                    id = "heritage_1_3",
                    question = "Thánh địa Mỹ Sơn thuộc tỉnh nào?",
                    answers = listOf("Quảng Nam", "Huế", "Đà Nẵng", "Quảng Ngãi"),
                    correctAnswer = "Quảng Nam",
                    explanation = "Thánh địa Mỹ Sơn thuộc huyện Duy Xuyên (Quảng Nam), là thánh tích của vương quốc Chăm Pa xưa.",
                    imageRes = getTopicImage(6, 3)
                ),
                TopicQuestion(
                    id = "heritage_1_4",
                    question = "Di sản nào của Việt Nam mang loại hình 'văn hóa – thiên nhiên hỗn hợp'?",
                    answers = listOf(
                        "Quần thể Tràng An",
                        "Phong Nha – Kẻ Bàng",
                        "Cố đô Huế",
                        "Hội An"
                    ),
                    correctAnswer = "Quần thể Tràng An",
                    explanation = "Quần thể danh thắng Tràng An (Ninh Bình) là di sản hỗn hợp duy nhất của Việt Nam.",
                    imageRes = getTopicImage(6, 4)
                ),
                TopicQuestion(
                    id = "heritage_1_5",
                    question = "Phong Nha – Kẻ Bàng được UNESCO công nhận lần đầu vào năm nào?",
                    answers = listOf("2003", "2010", "2014", "1999"),
                    correctAnswer = "2003",
                    explanation = "Năm 2003, Vườn quốc gia Phong Nha – Kẻ Bàng được công nhận Di sản thiên nhiên thế giới.",
                    imageRes = getTopicImage(6, 5)
                )
            )
        ),
        TopicQuizSet(
            setNumber = 2,
            setName = "Di tích lịch sử nổi tiếng",
            questions = listOf(
                TopicQuestion(
                    id = "heritage_2_1",
                    question = "Khu di tích lịch sử Đền Hùng nằm ở tỉnh nào?",
                    answers = listOf("Phú Thọ", "Hà Nội", "Nghệ An", "Thanh Hóa"),
                    correctAnswer = "Phú Thọ",
                    explanation = "Đền Hùng (Phú Thọ) là nơi thờ các Vua Hùng – tổ tiên dân tộc Việt Nam.",
                    imageRes = getTopicImage(6, 6)
                ),
                TopicQuestion(
                    id = "heritage_2_2",
                    question = "Chiến khu Tân Trào, nơi Chủ tịch Hồ Chí Minh ở và lãnh đạo Cách mạng tháng Tám, thuộc tỉnh nào?",
                    answers = listOf("Tuyên Quang", "Cao Bằng", "Bắc Kạn", "Thái Nguyên"),
                    correctAnswer = "Tuyên Quang",
                    explanation = "Tân Trào (Tuyên Quang) là trung tâm căn cứ địa Việt Bắc.",
                    imageRes = getTopicImage(6, 7)
                ),
                TopicQuestion(
                    id = "heritage_2_3",
                    question = "Địa danh gắn với chiến thắng của Hai Bà Trưng là gì?",
                    answers = listOf("Hát Môn", "Cổ Loa", "Phong Châu", "Tam Đảo"),
                    correctAnswer = "Hát Môn",
                    explanation = "Hát Môn (Phúc Thọ, Hà Nội) gắn với cuộc khởi nghĩa Hai Bà Trưng năm 40.",
                    imageRes = getTopicImage(6, 8)
                ),
                TopicQuestion(
                    id = "heritage_2_4",
                    question = "Khu di tích lịch sử Pác Bó (Hồ Chí Minh trở về năm 1941) thuộc tỉnh nào?",
                    answers = listOf("Cao Bằng", "Lạng Sơn", "Bắc Kạn", "Hà Giang"),
                    correctAnswer = "Cao Bằng",
                    explanation = "Hang Pác Bó thuộc huyện Hà Quảng (Cao Bằng), gắn liền với Bác Hồ.",
                    imageRes = getTopicImage(6, 9)
                ),
                TopicQuestion(
                    id = "heritage_2_5",
                    question = "Chiến thắng Điện Biên Phủ gắn với địa điểm nào?",
                    answers = listOf("Điện Biên", "Lai Châu", "Sơn La", "Lào Cai"),
                    correctAnswer = "Điện Biên",
                    explanation = "Chiến dịch quyết định trong kháng chiến chống Pháp diễn ra tại Điện Biên.",
                    imageRes = getTopicImage(6, 10)
                )
            )
        ),
        TopicQuizSet(
            setNumber = 3,
            setName = "Di sản phi vật thể",
            questions = listOf(
                TopicQuestion(
                    id = "heritage_3_1",
                    question = "Loại hình nghệ thuật nào của Việt Nam được UNESCO công nhận là di sản phi vật thể năm 2003?",
                    answers = listOf("Nhã nhạc cung đình Huế", "Ca trù", "Quan họ", "Hát xoan"),
                    correctAnswer = "Nhã nhạc cung đình Huế",
                    explanation = "Nhã nhạc cung đình Huế là di sản phi vật thể đầu tiên của Việt Nam (2003).",
                    imageRes = getTopicImage(6, 11)
                ),
                TopicQuestion(
                    id = "heritage_3_2",
                    question = "Dân ca Quan họ thuộc vùng nào?",
                    answers = listOf("Bắc Ninh – Bắc Giang", "Huế", "Quảng Nam", "Nam Bộ"),
                    correctAnswer = "Bắc Ninh – Bắc Giang",
                    explanation = "Dân ca Quan họ là đặc trưng vùng Kinh Bắc.",
                    imageRes = getTopicImage(6, 12)
                ),
                TopicQuestion(
                    id = "heritage_3_3",
                    question = "Tín ngưỡng thờ Mẫu Tam phủ được UNESCO công nhận vào năm nào?",
                    answers = listOf("2016", "2013", "2008", "2020"),
                    correctAnswer = "2016",
                    explanation = "Năm 2016, Tín ngưỡng thờ Mẫu được UNESCO công nhận là Di sản phi vật thể của nhân loại.",
                    imageRes = getTopicImage(6, 13)
                ),
                TopicQuestion(
                    id = "heritage_3_4",
                    question = "Loại hình nghệ thuật nào gắn liền với các nghệ nhân hát 'cửa đình – cửa quyền'?",
                    answers = listOf("Ca trù", "Hát then", "Quan họ", "Đờn ca tài tử"),
                    correctAnswer = "Ca trù",
                    explanation = "Ca trù là loại hình ca nhạc bác học, phát triển mạnh thời Lê – Trịnh.",
                    imageRes = getTopicImage(6, 14)
                ),
                TopicQuestion(
                    id = "heritage_3_5",
                    question = "'Không gian văn hóa cồng chiêng' là di sản của vùng nào?",
                    answers = listOf("Tây Nguyên", "Đông Nam Bộ", "Bắc Bộ", "Tây Bắc"),
                    correctAnswer = "Tây Nguyên",
                    explanation = "Cồng chiêng là nét văn hóa đặc sắc của các dân tộc Tây Nguyên: Êđê, Ba Na, Gia Rai…",
                    imageRes = getTopicImage(6, 15)
                )
            )
        ),

        TopicQuizSet(
            setNumber = 4,
            setName = "Kiến trúc – Công trình nổi tiếng",
            questions = listOf(
                TopicQuestion(
                    id = "heritage_4_1",
                    question = "Hoàng thành Thăng Long được xây dựng lần đầu dưới triều đại nào?",
                    answers = listOf("Lý", "Trần", "Lê", "Nguyễn"),
                    correctAnswer = "Lý",
                    explanation = "Khi dời đô ra Thăng Long năm 1010, nhà Lý bắt đầu xây dựng Hoàng thành.",
                    imageRes = getTopicImage(6, 16)
                ),
                TopicQuestion(
                    id = "heritage_4_2",
                    question = "Chùa Một Cột được xây dựng theo lối kiến trúc gì?",
                    answers = listOf("Một cột giữa hồ nước", "Tháp tầng", "Gỗ chạm trổ", "Ba gian hai chái"),
                    correctAnswer = "Một cột giữa hồ nước",
                    explanation = "Chùa Một Cột (Hà Nội) có kiến trúc độc đáo mô phỏng bông sen nở trên mặt nước.",
                    imageRes = getTopicImage(6, 17)
                ),
                TopicQuestion(
                    id = "heritage_4_3",
                    question = "Khu di tích lăng Chủ tịch Hồ Chí Minh được khánh thành vào năm nào?",
                    answers = listOf("1975", "1973", "1980", "1969"),
                    correctAnswer = "1975",
                    explanation = "Lăng Bác hoàn thành và mở cửa vào năm 1975 tại quảng trường Ba Đình.",
                    imageRes = getTopicImage(6, 18)
                ),
                TopicQuestion(
                    id = "heritage_4_4",
                    question = "Tháp Bà Ponagar là di tích của nền văn hóa nào?",
                    answers = listOf("Chăm Pa", "Khmer", "Lý – Trần", "Hòa Hảo"),
                    correctAnswer = "Chăm Pa",
                    explanation = "Tháp Bà Ponagar nằm ở Nha Trang, là công trình của người Chăm Pa cổ.",
                    imageRes = getTopicImage(6, 19)
                ),
                TopicQuestion(
                    id = "heritage_4_5",
                    question = "Di tích Cổ Loa gắn với vị vua nào?",
                    answers = listOf("An Dương Vương", "Hùng Vương", "Ngô Quyền", "Lý Thái Tổ"),
                    correctAnswer = "An Dương Vương",
                    explanation = "Cổ Loa là kinh đô của nước Âu Lạc do An Dương Vương xây dựng.",
                    imageRes = getTopicImage(6, 20)
                )
            )
        ),
        TopicQuizSet(
            setNumber = 5,
            setName = "Thiên nhiên & danh thắng",
            questions = listOf(
                TopicQuestion(
                    id = "heritage_5_1",
                    question = "Vịnh Hạ Long nổi tiếng với đặc điểm nào?",
                    answers = listOf("Hàng nghìn đảo núi đá vôi", "Thác nước lớn", "Sông ngòi uốn lượn", "Cồn cát sa mạc"),
                    correctAnswer = "Hàng nghìn đảo núi đá vôi",
                    explanation = "Vịnh Hạ Long nổi bật với hàng nghìn đảo đá vôi và hang động tuyệt đẹp.",
                    imageRes = getTopicImage(6, 21)
                ),
                TopicQuestion(
                    id = "heritage_5_2",
                    question = "Sa Pa (Lào Cai) nổi tiếng với địa hình nào?",
                    answers = listOf("Ruộng bậc thang", "Đảo san hô", "Cồn cát", "Núi đá vôi"),
                    correctAnswer = "Ruộng bậc thang",
                    explanation = "Sa Pa nổi tiếng với ruộng bậc thang, văn hóa dân tộc và khí hậu mát lạnh.",
                    imageRes = getTopicImage(6, 22)
                ),
                TopicQuestion(
                    id = "heritage_5_3",
                    question = "Hang Sơn Đoòng thuộc tỉnh nào?",
                    answers = listOf("Quảng Bình", "Quảng Nam", "Ninh Bình", "Lâm Đồng"),
                    correctAnswer = "Quảng Bình",
                    explanation = "Sơn Đoòng nằm trong quần thể Phong Nha – Kẻ Bàng.",
                    imageRes = getTopicImage(6, 23)
                ),
                TopicQuestion(
                    id = "heritage_5_4",
                    question = "Cố đô Hoa Lư thuộc tỉnh nào?",
                    answers = listOf("Ninh Bình", "Thanh Hóa", "Hà Nam", "Hà Nội"),
                    correctAnswer = "Ninh Bình",
                    explanation = "Hoa Lư là kinh đô của nhà Đinh và Tiền Lê.",
                    imageRes = getTopicImage(6, 24)
                ),
                TopicQuestion(
                    id = "heritage_5_5",
                    question = "Địa danh nào được mệnh danh là 'thành phố ngàn hoa'?",
                    answers = listOf("Đà Lạt", "Huế", "Hội An", "Cần Thơ"),
                    correctAnswer = "Đà Lạt",
                    explanation = "Đà Lạt nổi tiếng với khí hậu ôn hòa, hoa tươi quanh năm.",
                    imageRes = getTopicImage(6, 25)
                )
            )
        )
    )

}
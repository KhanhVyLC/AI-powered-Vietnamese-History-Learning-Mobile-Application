package com.example.pj

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.asRequestBody
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    settingsViewModel: SettingsViewModel,
    firebaseViewModel: FirebaseViewModel,
    onBack: () -> Unit
) {
    val viewModel: GeminiViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }

    // Settings + Auth
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val language by settingsViewModel.language.collectAsState()
    val authState by firebaseViewModel.authState.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Session state
    var chatSessions by remember { mutableStateOf<List<ChatSession>>(emptyList()) }
    var currentSession by remember { mutableStateOf<ChatSession?>(null) }
    var showSessionsDrawer by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf<ChatSession?>(null) }
    var showRenameDialog by remember { mutableStateOf<ChatSession?>(null) }

    // Prevent multiple saves
    var isSaving by remember { mutableStateOf(false) }

    val userId = authState.user?.uid

    // Load sessions list (start with new chat)
    LaunchedEffect(userId) {
        if (userId != null) {
            val repo = FirebaseRepository()
            val result = repo.getChatSessions(userId)
            result.onSuccess { sessions ->
                chatSessions = sessions
                currentSession = null
                viewModel.createNewSession()
            }
        }
    }

    // Auto-save when messages change (debounce)
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty() &&
            currentSession != null &&
            userId != null &&
            !isSaving
        ) {
            isSaving = true
            delay(500)

            val repo = FirebaseRepository()
            val chatMessages = uiState.messages.map { msg ->
                ChatMessageData(
                    messageId = msg.timestamp.toString(),
                    text = msg.text,
                    isUser = msg.isUser,
                    timestamp = msg.timestamp
                )
            }
            val updatedSession = currentSession!!.copy(messages = chatMessages)
            repo.saveChatSession(updatedSession)

            chatSessions = chatSessions.map {
                if (it.sessionId == updatedSession.sessionId) updatedSession else it
            }

            isSaving = false
        }
    }

    // Auto scroll to bottom
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            val lastIndex = uiState.messages.size - 1
            listState.animateScrollToItem(lastIndex)
        }
    }

    // Colors
    val surfaceColor = if (isDarkMode) Color(0xFF0B1120) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black

    // Quick questions
    val quickQuestions = when (language) {
        "en" -> listOf(
            "When did Bach Dang battle occur?",
            "Who is Ngo Quyen?",
            "Dien Bien Phu victory",
            "What is Chu Nom?",
            "How long did Ly dynasty last?",
            "Hai Ba Trung uprising"
        )

        "zh" -> listOf(
            "白藤江战役发生在什么时候？",
            "吴权是谁？",
            "奠边府大捷",
            "喃字是什么？",
            "李朝存在多久？",
            "征氏姐妹起义"
        )

        else -> listOf(
            "Trận Bạch Đằng diễn ra năm nào?",
            "Ngô Quyền là ai?",
            "Chiến thắng Điện Biên Phủ",
            "Chữ Nôm là gì?",
            "Triều Lý tồn tại bao lâu?",
            "Khởi nghĩa Hai Bà Trưng"
        )
    }

    val context = LocalContext.current
    val activity = context as? Activity

    // ========= Voice input =========
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val results =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = results?.firstOrNull() ?: ""
            if (text.isNotEmpty()) {
                inputText = text
            }
        }
    }

    // ========= Camera =========
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val file = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            val uriString = file.toURI().toString()

            viewModel.addLocalMessage(
                ChatMessage(
                    text = "",
                    isUser = true,
                    hasAudio = false,
                    imageUri = uriString
                )
            )

            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData(
                "image",
                file.name,
                requestBody
            )
            viewModel.sendImageMessage(imagePart, "", language)

            Toast.makeText(
                context,
                "Đã chụp ảnh (đã đưa vào khung chat)",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // ========= Gallery =========
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.addLocalMessage(
                ChatMessage(
                    text = "",
                    isUser = true,
                    hasAudio = false,
                    imageUri = uri.toString()
                )
            )

            Toast.makeText(
                context,
                "Đã chọn ảnh (đã đưa vào khung chat)",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    var isPlusMenuExpanded by remember { mutableStateOf(false) }

    fun startVoiceInput() {
        if (activity == null) return

        val langCode = when (language) {
            "en" -> "en-US"
            "zh" -> "zh-CN"
            else -> "vi-VN"
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, langCode)
            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Hãy nói nội dung bạn muốn hỏi"
            )
        }

        try {
            speechLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "Thiết bị không hỗ trợ nhận diện giọng nói",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Helper: generate session title
    fun generateSessionTitle(message: String): String {
        val maxLength = 40
        val clean = message.trim()
        return if (clean.length <= maxLength) clean
        else clean.take(maxLength) + "..."
    }

    // Helper: send message + create session if needed
    fun sendMessageWithSession(message: String) {
        coroutineScope.launch {
            if (currentSession == null && userId != null) {
                val repo = FirebaseRepository()
                val autoTitle = generateSessionTitle(message)
                val result = repo.createChatSession(userId, autoTitle)
                result.onSuccess { newSession ->
                    currentSession = newSession
                    chatSessions = listOf(newSession) + chatSessions
                    viewModel.loadSession(newSession.sessionId, emptyList())
                }
            }
            viewModel.sendMessage(message, language = language)
        }
    }

    // =========================
    // Scaffold
    // =========================

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(onClick = { showSessionsDrawer = true }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Sessions",
                                    tint = Color.White
                                )
                            }

                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = currentSession?.title ?: "ViHis",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (uiState.isLoading) {
                                    Text(
                                        text = when (language) {
                                            "en" -> "Thinking..."
                                            "zh" -> "思考中..."
                                            else -> "Đang suy nghĩ..."
                                        },
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (currentSession != null) {
                                IconButton(onClick = { showRenameDialog = currentSession }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Rename",
                                        tint = Color.White
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    if (userId != null) {
                                        currentSession = null
                                        viewModel.createNewSession()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "New Chat",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = surfaceColor,
                shadowElevation = 8.dp
            ) {
                Column {

                    // Error banner
                    uiState.error?.let { error ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            color = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F)
                                )
                                Text(
                                    text = error,
                                    color = Color(0xFFD32F2F),
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { viewModel.clearError() }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = Color(0xFFD32F2F),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Input row: + menu + textfield + send
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Nút "+" mở menu
                        Box {
                            IconButton(
                                onClick = { isPlusMenuExpanded = true },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        if (isDarkMode)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "More options",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            DropdownMenu(
                                expanded = isPlusMenuExpanded,
                                onDismissRequest = { isPlusMenuExpanded = false }
                            ) {
                                // 1. Voice input
                                DropdownMenuItem(
                                    text = { Text("Voice input") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        isPlusMenuExpanded = false
                                        startVoiceInput()
                                    }
                                )

                                // 2. Take photo
                                DropdownMenuItem(
                                    text = { Text("Take photo") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        isPlusMenuExpanded = false
                                        cameraLauncher.launch(null)
                                    }
                                )

                                // 3. Upload image
                                DropdownMenuItem(
                                    text = { Text("Upload image") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Image,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        isPlusMenuExpanded = false
                                        galleryLauncher.launch("image/*")
                                    }
                                )
                            }
                        }

                        // Text input
                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                Text(
                                    when (language) {
                                        "en" -> "Ask about history..."
                                        "zh" -> "询问历史..."
                                        else -> "Đặt câu hỏi về lịch sử..."
                                    },
                                    fontSize = 14.sp
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = if (isDarkMode)
                                    Color(0xFF2C2C2C) else MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = if (isDarkMode)
                                    Color(0xFF2C2C2C) else MaterialTheme.colorScheme.surfaceVariant,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledContainerColor = Color(0xFFEEEEEE)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 3,
                            enabled = !uiState.isLoading
                        )

                        // Send button
                        IconButton(
                            onClick = {
                                if (inputText.isNotEmpty() &&
                                    !uiState.isLoading &&
                                    userId != null
                                ) {
                                    val messageToSend = inputText
                                    inputText = ""
                                    sendMessageWithSession(messageToSend)
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (inputText.isNotEmpty() && !uiState.isLoading)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                ),
                            enabled = inputText.isNotEmpty() && !uiState.isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        // =========================
        // Content: background + messages
        // =========================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Image(
                painter = painterResource(id = R.drawable.p_chatbg),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (isDarkMode) 0.3f else 0.5f),
                contentScale = ContentScale.Crop
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.messages.isEmpty() && !uiState.isLoading) {
                    // Welcome
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.p_chatbot),
                                contentDescription = "ViHis",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            Text(
                                text = when (language) {
                                    "en" -> "Hello! I'm ViHis"
                                    "zh" -> "你好！我是 ViHis"
                                    else -> "Xin chào! Tôi là ViHis"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = when (language) {
                                    "en" -> "Your Vietnamese History Assistant"
                                    "zh" -> "你的越南历史助手"
                                    else -> "Trợ lý Lịch sử Việt Nam của bạn"
                                },
                                color = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Suggested title
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.icon_rec),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = when (language) {
                                    "en" -> "Suggested questions:"
                                    "zh" -> "建议问题："
                                    else -> "Gợi ý câu hỏi:"
                                },
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    // Suggested questions list
                    items(quickQuestions) { question ->
                        Surface(
                            onClick = {
                                if (!uiState.isLoading && userId != null) {
                                    sendMessageWithSession(question)
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = surfaceColor.copy(alpha = 0.9f),
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = question,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = textColor,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                } else {
                    // Messages
                    items(uiState.messages) { message ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (message.isUser) {
                                Arrangement.End
                            } else {
                                Arrangement.Start
                            }
                        ) {
                            if (!message.isUser) {
                                Image(
                                    painter = painterResource(id = R.drawable.p_chatbot),
                                    contentDescription = "ViHis",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (message.isUser) 16.dp else 4.dp,
                                    bottomEnd = if (message.isUser) 4.dp else 16.dp
                                ),
                                color = if (message.isUser)
                                    MaterialTheme.colorScheme.primary
                                else
                                    surfaceColor.copy(alpha = 0.95f),
                                shadowElevation = if (message.isUser) 0.dp else 2.dp,
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    // 🖼 Nếu tin nhắn có ảnh — hiển thị ảnh
                                    if (message.imageUri != null) {
                                        Image(
                                            painter = rememberAsyncImagePainter(message.imageUri),
                                            contentDescription = "Image message",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 220.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )

                                        if (message.text.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                        }
                                    }

                                    // ✏ Nếu có text thì hiển thị text
                                    if (message.text.isNotBlank()) {
                                        Text(
                                            text = message.text,
                                            color = if (message.isUser) Color.White else textColor,
                                            fontSize = 14.sp,
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Typing indicator
                    if (uiState.isLoading) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.p_chatbot),
                                    contentDescription = "ViHis",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = surfaceColor.copy(alpha = 0.95f),
                                    shadowElevation = 2.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            text = when (language) {
                                                "en" -> "Thinking..."
                                                "zh" -> "思考中..."
                                                else -> "Đang suy nghĩ..."
                                            },
                                            color = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // =========================
    // Left Drawer: Sessions
    // =========================
    if (showSessionsDrawer) {
        val drawerState = rememberDrawerState(DrawerValue.Open)

        LaunchedEffect(drawerState.currentValue) {
            if (drawerState.currentValue == DrawerValue.Closed) {
                showSessionsDrawer = false
            }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(300.dp),
                    drawerContainerColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (language) {
                                    "en" -> "Chat History"
                                    "zh" -> "聊天记录"
                                    else -> "Lịch sử trò chuyện"
                                },
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color.White else Color.Black
                            )

                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        drawerState.close()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = if (isDarkMode) Color.White else Color.Black
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (userId != null) {
                                    currentSession = null
                                    viewModel.createNewSession()
                                    coroutineScope.launch {
                                        drawerState.close()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (language) {
                                    "en" -> "New Chat"
                                    "zh" -> "新聊天"
                                    else -> "Cuộc trò chuyện mới"
                                }
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        if (chatSessions.isEmpty()) {
                            Text(
                                text = when (language) {
                                    "en" -> "No chat history yet"
                                    "zh" -> "还没有聊天记录"
                                    else -> "Chưa có lịch sử trò chuyện"
                                },
                                color = Color.Gray,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(chatSessions) { session ->
                                    SessionCard(
                                        session = session,
                                        isSelected = session.sessionId == currentSession?.sessionId,
                                        isDarkMode = isDarkMode,
                                        onClick = {
                                            currentSession = session
                                            viewModel.loadSession(
                                                sessionId = session.sessionId,
                                                messages = session.messages.map { msg ->
                                                    ChatMessage(
                                                        text = msg.text,
                                                        isUser = msg.isUser,
                                                        hasAudio = !msg.isUser,
                                                        timestamp = msg.timestamp
                                                    )
                                                }
                                            )
                                            coroutineScope.launch {
                                                drawerState.close()
                                            }
                                        },
                                        onDeleteClick = { showDeleteConfirmDialog = session }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            content = { /* main content already rendered above */ }
        )
    }

    // =========================
    // Delete Confirm Dialog
    // =========================
    showDeleteConfirmDialog?.let { session ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F)
                )
            },
            title = {
                Text(
                    text = when (language) {
                        "en" -> "Delete Chat?"
                        "zh" -> "删除聊天？"
                        else -> "Xóa cuộc trò chuyện?"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = when (language) {
                        "en" -> "\"${session.title}\" will be permanently deleted."
                        "zh" -> "\"${session.title}\" 将被永久删除。"
                        else -> "\"${session.title}\" sẽ bị xóa vĩnh viễn."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val repo = FirebaseRepository()
                            repo.deleteChatSession(session.sessionId)
                            chatSessions = chatSessions.filter { it.sessionId != session.sessionId }

                            if (currentSession?.sessionId == session.sessionId) {
                                currentSession = chatSessions.firstOrNull()
                                if (currentSession != null) {
                                    viewModel.loadSession(
                                        sessionId = currentSession!!.sessionId,
                                        messages = currentSession!!.messages.map { msg ->
                                            ChatMessage(
                                                text = msg.text,
                                                isUser = msg.isUser,
                                                hasAudio = !msg.isUser,
                                                timestamp = msg.timestamp
                                            )
                                        }
                                    )
                                } else {
                                    viewModel.createNewSession()
                                }
                            }
                            showDeleteConfirmDialog = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
                    )
                ) {
                    Text(
                        text = when (language) {
                            "en" -> "Delete"
                            "zh" -> "删除"
                            else -> "Xóa"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text(
                        text = when (language) {
                            "en" -> "Cancel"
                            "zh" -> "取消"
                            else -> "Hủy"
                        }
                    )
                }
            }
        )
    }

    // =========================
    // Rename Dialog
    // =========================
    showRenameDialog?.let { session ->
        RenameSessionDialog(
            currentTitle = session.title,
            language = language,
            onDismiss = { showRenameDialog = null },
            onConfirm = { newTitle ->
                coroutineScope.launch {
                    val repo = FirebaseRepository()
                    repo.updateSessionTitle(session.sessionId, newTitle)

                    chatSessions = chatSessions.map {
                        if (it.sessionId == session.sessionId) {
                            it.copy(title = newTitle)
                        } else it
                    }

                    if (currentSession?.sessionId == session.sessionId) {
                        currentSession = currentSession?.copy(title = newTitle)
                    }

                    showRenameDialog = null
                }
            }
        )
    }
}

// =========================
// Session Card Composable
// =========================
@Composable
fun SessionCard(
    session: ChatSession,
    isSelected: Boolean,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            if (isDarkMode) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant
        } else {
            if (isDarkMode) Color(0xFF2C2C2C)
            else MaterialTheme.colorScheme.surfaceVariant
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = session.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) {
                        if (isDarkMode) Color.White
                        else MaterialTheme.colorScheme.primary
                    } else {
                        if (isDarkMode) Color.White else Color.Black
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = session.getTimeAgo(),
                    fontSize = 11.sp,
                    color = if (isSelected) {
                        if (isDarkMode) Color.White.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.primary
                    } else {
                        Color.Gray
                    }
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = if (isSelected) {
                        if (isDarkMode) Color.White else MaterialTheme.colorScheme.primary
                    } else {
                        Color.Gray
                    },
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// =========================
// Rename Session Dialog
// =========================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameSessionDialog(
    currentTitle: String,
    language: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var title by remember { mutableStateOf(currentTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = when (language) {
                    "en" -> "Rename Chat"
                    "zh" -> "重命名聊天"
                    else -> "Đổi tên cuộc trò chuyện"
                },
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = when (language) {
                        "en" -> "Enter new title:"
                        "zh" -> "输入新标题："
                        else -> "Nhập tiêu đề mới:"
                    }
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = title.isNotBlank()
            ) {
                Text(
                    text = when (language) {
                        "en" -> "Save"
                        "zh" -> "保存"
                        else -> "Lưu"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = when (language) {
                        "en" -> "Cancel"
                        "zh" -> "取消"
                        else -> "Hủy"
                    }
                )
            }
        }
    )
}

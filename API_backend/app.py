# -*- coding: utf-8 -*-
from flask import Flask, request, jsonify
from flask_cors import CORS
import requests
import logging
import socket
import base64
import os
from googlesearch import search
from datetime import datetime
from werkzeug.utils import secure_filename

logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)

app.config['MAX_CONTENT_LENGTH'] = 10 * 1024 * 1024 
app.config['UPLOAD_FOLDER'] = '/tmp'

OLLAMA_URL = "http://localhost:11434/api/generate"

TEXT_MODEL = "gemma2:9b"      
VISION_MODEL = "llava:7b"  

ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'webp', 'gif', 'bmp'}

LANGUAGES = {
    'vi': {
        'name': 'Tiếng Việt',
        'system_prompt': """Bạn là trợ lý AI thông minh, trả lời CHÍNH XÁC bằng tiếng Việt.

Quy tắc:
- Trả lời ngắn gọn, đúng trọng tâm (2-5 câu)
- Nếu là câu hỏi về sự kiện/con số cụ thể, trả lời TRỰC TIẾP thông tin đó
- KHÔNG bịa đặt thông tin
- Nếu không biết chắc chắn, nói rõ "Tôi không chắc chắn về điều này"
{search_context}

Câu hỏi: {message}

Trả lời bằng tiếng Việt:""",
        'vision_prompt': """Bạn là trợ lý AI thông minh. Hãy phân tích hình ảnh này và trả lời câu hỏi bằng tiếng Việt.

Câu hỏi: {message}

Hãy mô tả chi tiết những gì bạn nhìn thấy trong hình và trả lời câu hỏi bằng tiếng Việt:""",
        'search_keywords': ['mới nhất', 'hiện tại', 'hôm nay', 'năm nay', 'tin tức', 
                           'thời tiết', 'giá', 'bao nhiêu tỉnh', '2025', '2024', 
                           'cập nhật', 'sáp nhập', 'thay đổi', 'ai là', 'là ai']
    },
    'en': {
        'name': 'English',
        'system_prompt': """You are an intelligent AI assistant. Answer ACCURATELY in English.

Rules:
- Answer concisely and to the point (2-5 sentences)
- For factual questions, provide DIRECT information
- DO NOT make up information
- If uncertain, clearly state "I'm not certain about this"
{search_context}

Question: {message}

Answer in English:""",
        'vision_prompt': """You are an intelligent AI assistant. Analyze this image and answer the question in English.

Question: {message}

Describe in detail what you see in the image and answer the question in English:""",
        'search_keywords': ['latest', 'current', 'today', 'this year', 'news', 
                           'weather', 'price', 'how many', '2025', '2024', 
                           'update', 'merger', 'changes', 'who is']
    },
    'zh': {
        'name': '中文',
        'system_prompt': """你是一个智能AI助手，用中文准确回答。

规则：
- 简洁明了地回答（2-5句话）
- 对于事实性问题，直接提供信息
- 不要编造信息
- 如果不确定，明确说明"我不太确定"
{search_context}

问题：{message}

用中文回答：""",
        'vision_prompt': """你是一个智能AI助手。分析这张图片并用中文回答问题。

问题：{message}

详细描述你在图片中看到的内容，并用中文回答问题：""",
        'search_keywords': ['最新', '当前', '今天', '今年', '新闻', 
                           '天气', '价格', '多少', '2025', '2024', 
                           '更新', '合并', '变化', '是谁']
    }
}

DEFAULT_LANGUAGE = 'vi'

def allowed_file(filename):
    """Kiểm tra file extension hợp lệ"""
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

def detect_language(text):
    """Tự động phát hiện ngôn ngữ"""
    if not text:
        return DEFAULT_LANGUAGE
    
    vietnamese_chars = ['ă', 'â', 'đ', 'ê', 'ô', 'ơ', 'ư', 'á', 'à', 'ả', 'ã', 'ạ']
    if any(char in text.lower() for char in vietnamese_chars):
        return 'vi'
    
    for char in text:
        if '\u4e00' <= char <= '\u9fff':
            return 'zh'
    
    return 'en'

def should_search(message, language):
    """Kiểm tra có cần search Google không"""
    if not message:
        return False
    message_lower = message.lower()
    keywords = LANGUAGES[language]['search_keywords']
    return any(keyword in message_lower for keyword in keywords)

def google_search_info(query, num_results=3):
    """Tìm kiếm Google và lấy URLs"""
    try:
        logger.info(f"🔍 Searching Google for: {query}")
        results = []
        for url in search(query, num_results=num_results, lang='vi'):
            results.append(url)
            if len(results) >= num_results:
                break
        logger.info(f"✅ Found {len(results)} results")
        return results
    except Exception as e:
        logger.error(f"❌ Search error: {e}")
        return []

@app.route('/', methods=['GET'])
def home():
    """API info endpoint"""
    return jsonify({
        'status': 'online',
        'message': 'AI Chat API with Dual-Model System',
        'version': '2.0',
        'models': {
            'text': TEXT_MODEL,
            'vision': VISION_MODEL
        },
        'supported_languages': list(LANGUAGES.keys()),
        'features': [
            'Multi-language support',
            'Google Search integration',
            'Image Understanding (Vision AI)',
            'Auto language detection',
            'Dual-model system (fast text + smart vision)'
        ],
        'endpoints': {
            'GET /': 'API information',
            'GET /health': 'Health check',
            'GET /languages': 'Supported languages',
            'POST /chat': 'Send message (JSON for text, FormData for image)'
        },
        'image_support': {
            'enabled': True,
            'formats': list(ALLOWED_EXTENSIONS),
            'max_size': '10MB',
            'usage': 'Send as FormData with fields: message, language (optional), image (file)'
        }
    })

@app.route('/languages', methods=['GET'])
def get_languages():
    """Get supported languages"""
    return jsonify({
        'languages': {
            code: {'name': lang['name']} 
            for code, lang in LANGUAGES.items()
        },
        'default': DEFAULT_LANGUAGE
    })

@app.route('/health', methods=['GET'])
def health():
    """Health check endpoint"""
    try:
        test_response = requests.get("http://localhost:11434/api/tags", timeout=5)
        if test_response.status_code == 200:
            ollama_status = "running"
            models_data = test_response.json()
            available_models = [m['name'] for m in models_data.get('models', [])]
        else:
            ollama_status = "error"
            available_models = []
    except Exception as e:
        logger.error(f"Health check error: {e}")
        ollama_status = "not running"
        available_models = []
    
    return jsonify({
        'status': 'ok',
        'timestamp': datetime.now().isoformat(),
        'ollama': {
            'status': ollama_status,
            'url': OLLAMA_URL
        },
        'models': {
            'text': {
                'name': TEXT_MODEL,
                'available': TEXT_MODEL in ' '.join(available_models)
            },
            'vision': {
                'name': VISION_MODEL,
                'available': VISION_MODEL in ' '.join(available_models)
            }
        },
        'available_models': available_models,
        'languages': list(LANGUAGES.keys()),
        'features': {
            'google_search': True,
            'vision': True,
            'multi_language': True
        }
    })

@app.route('/chat', methods=['POST'])
def chat():
    """
    Main chat endpoint - supports both text and image
    
    TEXT MODE (JSON):
    {
        "message": "Your question",
        "language": "vi" (optional)
    }
    
    IMAGE MODE (FormData):
    - message: Your question
    - language: vi/en/zh (optional)
    - image: Image file
    """
    
    start_time = datetime.now()
    logger.info(f"\n{'='*60}")
    logger.info(f"📨 NEW REQUEST - {start_time.strftime('%H:%M:%S')}")
    logger.info(f"Content-Type: {request.content_type}")
    
    try:
        has_image = False
        image_base64 = None
        image_filename = None
        
        if request.content_type and 'multipart/form-data' in request.content_type:
            logger.info("Request type: FormData (may contain image)")
            user_message = request.form.get('message', '')
            language = request.form.get('language', None)
            
            # Check for image
            if 'image' in request.files:
                image_file = request.files['image']
                if image_file and image_file.filename:
                    if allowed_file(image_file.filename):
                        has_image = True
                        image_filename = secure_filename(image_file.filename)
                        image_bytes = image_file.read()
                        image_base64 = base64.b64encode(image_bytes).decode('utf-8')
                        logger.info(f"Image received: {image_filename}")
                        logger.info(f"Image size: {len(image_bytes) / 1024:.2f} KB")
                    else:
                        return jsonify({
                            'error': 'Invalid file format',
                            'allowed_formats': list(ALLOWED_EXTENSIONS)
                        }), 400
        else:
            logger.info("Request type: JSON (text only)")
            data = request.json
            if not data:
                return jsonify({'error': 'No data provided'}), 400
            user_message = data.get('message', '')
            language = data.get('language', None)
        
        # ====== HANDLE EMPTY MESSAGE ======
        if not user_message or not user_message.strip():
            if has_image:
                # Nếu có ảnh mà không có câu hỏi → tự tạo prompt mặc định
                if language == "vi":
                    user_message = "Mô tả chi tiết nội dung bức ảnh này."
                elif language == "en":
                    user_message = "Describe in detail what is shown in this image."
                else:  # zh
                    user_message = "详细描述这张图片的内容。"
            else:
                return jsonify({"error": "Message is required"}), 400

        user_message = user_message.strip()
        logger.info(f"Message: {user_message[:100]}...")

        
        if not language:
            language = detect_language(user_message)
            logger.info(f"Auto-detected language: {language} ({LANGUAGES[language]['name']})")
        
        if language not in LANGUAGES:
            logger.warning(f"Unknown language '{language}', using default: {DEFAULT_LANGUAGE}")
            language = DEFAULT_LANGUAGE

        if has_image:
            model = VISION_MODEL
            logger.info(f"MODE: VISION")
            logger.info(f"Using model: {model}")

            prompt_template = LANGUAGES[language]['vision_prompt']
            full_prompt = prompt_template.format(message=user_message)
            
            logger.info(f"Sending to Ollama (Vision)...")

            response = requests.post(OLLAMA_URL, json={
                "model": model,
                "prompt": full_prompt,
                "images": [image_base64],
                "stream": False,
                "options": {
                    "temperature": 0.3,
                    "top_p": 0.9,
                    "num_predict": 500
                }
            }, timeout=180)  
            
            if response.status_code == 200:
                ai_response = response.json().get('response', '').strip()
                processing_time = (datetime.now() - start_time).total_seconds()
                
                logger.info(f"Vision response received")
                logger.info(f"Processing time: {processing_time:.2f}s")
                logger.info(f"Response preview: {ai_response[:100]}...")
                logger.info(f"{'='*60}\n")
                
                return jsonify({
                    'reply': ai_response,
                    'model': model,
                    'mode': 'vision',
                    'language': language,
                    'language_name': LANGUAGES[language]['name'],
                    'searched': False,
                    'search_results': [],
                    'has_image': True,
                    'image_filename': image_filename,
                    'processing_time': round(processing_time, 2)
                })
            else:
                logger.error(f"Ollama vision error: {response.status_code}")
                logger.error(f"Response: {response.text}")
                return jsonify({
                    'error': 'Vision AI service error',
                    'details': response.text
                }), 500

        else:
            model = TEXT_MODEL
            logger.info(f"MODE: TEXT")
            logger.info(f"Using model: {model}")

            search_context = ""
            searched = False
            search_results = []
            
            if should_search(user_message, language):
                logger.info("Search is needed for this query")
                search_results = google_search_info(user_message, num_results=3)
                
                if search_results:
                    searched = True
                    if language == 'vi':
                        search_context = f"\n\nThông tin tham khảo từ web (ngày {datetime.now().strftime('%d/%m/%Y')}):\n"
                    elif language == 'en':
                        search_context = f"\n\nReference information from web ({datetime.now().strftime('%m/%d/%Y')}):\n"
                    else:
                        search_context = f"\n\n网络参考信息 ({datetime.now().strftime('%Y/%m/%d')}):\n"
                    
                    search_context += "\n".join([f"- {url}" for url in search_results])
                    search_context += "\n\nHãy dựa vào thông tin trên để trả lời chính xác."
                    logger.info(f"Added {len(search_results)} search results to context")

            system_prompt = LANGUAGES[language]['system_prompt']
            full_prompt = system_prompt.format(
                message=user_message,
                search_context=search_context
            )
            
            logger.info(f"Sending to Ollama (Text)...")

            response = requests.post(OLLAMA_URL, json={
                "model": model,
                "prompt": full_prompt,
                "stream": False,
                "options": {
                    "temperature": 0.2,
                    "top_p": 0.9,
                    "top_k": 40,
                    "num_predict": 400
                }
            }, timeout=120)
            
            if response.status_code == 200:
                ai_response = response.json().get('response', '').strip()
                processing_time = (datetime.now() - start_time).total_seconds()
                
                logger.info(f"Text response received")
                logger.info(f"Processing time: {processing_time:.2f}s")
                logger.info(f"Response preview: {ai_response[:100]}...")
                logger.info(f"{'='*60}\n")
                
                return jsonify({
                    'reply': ai_response,
                    'model': model,
                    'mode': 'text',
                    'language': language,
                    'language_name': LANGUAGES[language]['name'],
                    'searched': searched,
                    'search_results': search_results,
                    'has_image': False,
                    'processing_time': round(processing_time, 2)
                })
            else:
                logger.error(f"Ollama error: {response.status_code}")
                logger.error(f"Response: {response.text}")
                return jsonify({
                    'error': 'AI service error',
                    'details': response.text
                }), 500
            
    except requests.exceptions.Timeout:
        logger.error("Request timeout")
        return jsonify({
            'error': 'Request timeout',
            'hint': 'The AI model took too long to respond. Try a shorter message.'
        }), 504
        
    except requests.exceptions.ConnectionError:
        logger.error("Cannot connect to Ollama")
        return jsonify({
            'error': 'Cannot connect to Ollama',
            'hint': 'Make sure Ollama is running: ollama serve'
        }), 503
        
    except Exception as e:
        logger.error(f"Unexpected error: {str(e)}", exc_info=True)
        return jsonify({
            'error': 'Internal server error',
            'details': str(e)
        }), 500

@app.errorhandler(413)
def request_entity_too_large(error):
    """Handle file too large error"""
    return jsonify({
        'error': 'File too large',
        'max_size': '10MB',
        'hint': 'Please upload a smaller image'
    }), 413

@app.errorhandler(404)
def not_found(error):
    """Handle 404 errors"""
    return jsonify({
        'error': 'Endpoint not found',
        'available_endpoints': {
            'GET /': 'API information',
            'GET /health': 'Health check',
            'GET /languages': 'Supported languages',
            'POST /chat': 'Send message'
        }
    }), 404

@app.errorhandler(500)
def internal_error(error):
    """Handle 500 errors"""
    logger.error(f"Internal server error: {error}")
    return jsonify({
        'error': 'Internal server error',
        'hint': 'Please check server logs'
    }), 500

if __name__ == '__main__':
    hostname = socket.gethostname()
    local_ip = socket.gethostbyname(hostname)
    
    print("\n" + "="*70)
    print("AI CHAT API SERVER - DUAL MODEL SYSTEM")
    print("="*70)
    print(f"Text Model:   {TEXT_MODEL} (fast)")
    print(f"Vision Model: {VISION_MODEL} (smart)")
    print(f"Languages:    {', '.join([f'{code} ({LANGUAGES[code]['name']})' for code in LANGUAGES])}")
    print(f"Features:     Multi-language, Google Search, Image Understanding")
    print(f"\nServer URLs:")
    print(f"   Local:   http://localhost:5000")
    print(f"   Network: http://{local_ip}:5000")
    print(f"\nEndpoints:")
    print(f"   GET  /          - API information")
    print(f"   GET  /health    - Health check")
    print(f"   GET  /languages - Supported languages")
    print(f"   POST /chat      - Send message")
    print(f"\nImage Support:")
    print(f"   Formats:  {', '.join(sorted(ALLOWED_EXTENSIONS))}")
    print(f"   Max size: 10MB")
    print(f"   Usage:    Send as FormData with 'image' field")
    print("="*70 + "\n")

    try:
        test = requests.get("http://localhost:11434/api/tags", timeout=5)
        if test.status_code == 200:
            print("Ollama is running")
            models = test.json().get('models', [])
            available = [m['name'] for m in models]
            print(f"Available models: {len(available)}")

            text_available = any(TEXT_MODEL in m for m in available)
            vision_available = any(VISION_MODEL in m for m in available)
            
            if text_available:
                print(f"  {TEXT_MODEL} - Ready for text chat")
            else:
                print(f"  {TEXT_MODEL} - NOT FOUND!")
                print(f"      Run: ollama pull {TEXT_MODEL}")
            
            if vision_available:
                print(f"  {VISION_MODEL} - Ready for image analysis")
            else:
                print(f"  {VISION_MODEL} - NOT FOUND!")
                print(f"      Run: ollama pull {VISION_MODEL}")
            
            if not text_available or not vision_available:
                print(f"\nWARNING: Some models are missing!")
                print(f"   The server will start but features may be limited.")
        else:
            print("Cannot connect to Ollama (unexpected response)")
    except requests.exceptions.ConnectionError:
        print("Ollama is NOT running!")
        print("  Please start Ollama first: ollama serve")
        print("  Then pull required models:")
        print(f"     ollama pull {TEXT_MODEL}")
        print(f"     ollama pull {VISION_MODEL}")
    except Exception as e:
        print(f"Error checking Ollama: {e}")
    
    print("\n" + "="*70)
    print("Server starting...")
    print("="*70 + "\n")
    app.run(host='0.0.0.0', port=5000, debug=True)
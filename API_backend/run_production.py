
from waitress import serve
from app import app
import socket

if __name__ == '__main__':
    hostname = socket.gethostname()
    local_ip = socket.gethostbyname(hostname)    
    print("\n" + "="*60)
    print("AI Chat API Server (PRODUCTION MODE)")
    print("="*60)
    print(f"Using Waitress WSGI Server (Production-ready)")
    print(f"Local:  http://localhost:5000")
    print(f"Network: http://{local_ip}:5000")
    print(f"Multi-threaded: YES")
    print(f"Connection handling: STABLE")
    print("="*60 + "\n")
    serve(
        app,
        host='0.0.0.0',
        port=5000,
        threads=8,  
        connection_limit=1000,  
        channel_timeout=120,  
        cleanup_interval=30, 
        url_scheme='http'
    )

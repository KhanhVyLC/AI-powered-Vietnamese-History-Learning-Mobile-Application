python -m venv venv

source venv/bin/activate

pip install fastapi uvicorn sentence-transformers scikit-learn pypdf

python build_index.py

uvicorn app:app --reload --port 8000


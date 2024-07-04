import ollama
import sys

response = ollama.chat(model="swallow", 
    messages = [
        {
            "role": "user",
            "content": f">>>以下に，友人からの問いかけがあります．フレンドリーかつ丁寧に回答してください### 入力:{sys.argv[1]}\n\n### 応答："
        }
    ]
)
print(response["message"]["content"])
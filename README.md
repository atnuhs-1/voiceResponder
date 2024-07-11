# VoiceResponder

VoiceResponderは、KotlinとPythonを使用して構築されたインタラクティブな音声認識アプリケーションです。このアプリケーションは、音声の録音、テキスト変換、および音声の読み上げを提供し、ユーザーが自然に音声で操作できることを目指しています。

## 主な機能

1. **音声の録音**:
    - ユーザーはアプリケーションのGUIを介して音声を録音できます。録音は選択されたオーディオミキサーを使用して行われ、ローカルファイルに保存されます。

2. **音声のテキスト変換**:
    - 録音された音声ファイルをテキストに変換します。Whisperライブラリを使用して日本語の音声を高精度に認識し、テキストに変換します。

3. **テキストの読み上げ**:
    - 指定されたテキストを音声に変換し、再生します。Google Text-to-Speech（gTTS）ライブラリを使用してテキストを音声ファイルに変換し、Pygameライブラリで再生します。

4. **チャット応答**:
    - ユーザーの入力に基づいて、Ollamaチャットモデルを使用してフレンドリーかつ丁寧な応答を生成します。

## 使用技術

- **Kotlin**:
    - アプリケーションのフロントエンド部分を構築し、JavaのSwingライブラリを使用してGUIを提供します。
- **Python**:
    - 音声処理およびテキスト変換のバックエンドロジックを実装します。
    - Whisper、gTTS、Pygame、Ollamaライブラリを使用して音声認識および生成機能を実現します。

## ディレクトリ構成

```
voiceResponder/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/
│   │   │   │   └── org/
│   │   │   │       └── example/
│   │   │   │           └── App.kt
│   ├── read_aloud.py
│   ├── chat.py
│   └── transcribe.py
├── playground/
├── .gitignore
├── .gradle
├── .vscode
├── README.md
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
└── Modefile
```
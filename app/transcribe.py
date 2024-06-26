import sys
import whisper
import wave


def main(audio_path):
    model = whisper.load_model("base")
    result = model.transcribe(audio_path, language="ja")
    print(result['text'])

if __name__ == "__main__":
    audio_file_path = "recording.wav"
    if len(sys.argv) == 2:
        audio_file_path = sys.argv[1]

    main(audio_file_path)

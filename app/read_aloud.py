from gtts import gTTS
import pygame
import sys

def read_aloud(text):
    tts = gTTS(text=text, lang='ja')
    tts.save("response.mp3")

    pygame.mixer.init()
    pygame.mixer.music.load("response.mp3")
    pygame.mixer.music.play()

    while pygame.mixer.music.get_busy():
        pygame.time.Clock().tick(10)

if __name__ == "__main__":
    if len(sys.argv) > 1:
        text = sys.argv[1]
        read_aloud(text)
    else:
        print("No text provided to read aloud.")
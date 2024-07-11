from gtts import gTTS
import pygame
import time

def test_gtts():
    tts = gTTS(text="あいうえお", lang='ja')
    tts.save("output.mp3")

    pygame.mixer.init()
    pygame.mixer.music.load("output.mp3")
    pygame.mixer.music.play()

    while pygame.mixer.music.get_busy():
        time.sleep(1)

if __name__ == "__main__":
    test_gtts()
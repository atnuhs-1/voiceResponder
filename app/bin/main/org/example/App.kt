package org.example

import javax.sound.sampled.*
import javax.swing.*
import kotlin.concurrent.thread
import java.io.File
import java.io.IOException
import java.awt.GridLayout

class AudioRecorderApp : JFrame() {
    private var line: TargetDataLine? = null
    private val format = AudioFormat(44100.0f, 16, 1, true, true)
    private var selectedMixer: Mixer? = null
    private val statusLabel = JLabel("Status Label")
    private val transcriptionResultLabel = JLabel("Transcription Result: ~~~~~~~~~~~~~")
    private val ollamaResultLabel = JLabel("Ollama Result: ~~~~~~~~~~~~~")
    private val mixers = AudioSystem.getMixerInfo()
    private val mixerComboBox = JComboBox(mixers.map { it.name }.toTypedArray())

    init {
        title = "Audio Recorder"
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(400, 400)

        val panel = JPanel()
        panel.layout = GridLayout(8, 2, 10, 10) // 8行2列のグリッドレイアウト

        val startButton = JButton("Start Recording")
        val stopButton = JButton("Stop Recording")
        val transcribeHelloButton = JButton("Transcribe hello.mp3")

        startButton.addActionListener {
            startRecording()
        }

        stopButton.addActionListener {
            stopRecording()
        }

        transcribeHelloButton.addActionListener {
            transcribeHelloMp3()
        }

        // Add components to the panel
        panel.add(JLabel("Select Mixer:"))
        panel.add(mixerComboBox)
        panel.add(startButton)
        panel.add(stopButton)
        panel.add(transcribeHelloButton)
        panel.add(statusLabel)
        panel.add(JLabel()) // 空白セルを追加して整列
        panel.add(transcriptionResultLabel)
        panel.add(JLabel()) // 空白セルを追加して整列
        panel.add(ollamaResultLabel)
        panel.add(JLabel()) // 空白セルを追加して整列

        add(panel)
        isVisible = true
    }

    private fun startRecording() {
        thread {
            try {
                statusLabel.text = "Recording..."
                val selectedMixerIndex = mixerComboBox.selectedIndex
                selectedMixer = AudioSystem.getMixer(mixers[selectedMixerIndex])

                try {
                    selectedMixer!!.open()
                } catch (e: LineUnavailableException) {
                    statusLabel.text = "Selected mixer not supported."
                    return@thread
                }

                val dataLineInfo = DataLine.Info(TargetDataLine::class.java, format)

                if (!selectedMixer!!.isLineSupported(dataLineInfo)) {
                    statusLabel.text = "Selected mixer does not support the specified format."
                    return@thread
                }

                line = selectedMixer!!.getLine(dataLineInfo) as TargetDataLine
                line?.open(format)
                line?.start()

                val audioInputStream = AudioInputStream(line)
                val wavFile = File("recording.wav")

                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, wavFile)
                statusLabel.text = "Recording saved to ${wavFile.absolutePath}"

                // 録音が完了したと同時にPythonスクリプトを実行
                runTranscribeScript(wavFile.absolutePath)

            } catch (ex: LineUnavailableException) {
                ex.printStackTrace()
                statusLabel.text = "Recording failed: Line unavailable"
            } catch (ex: IOException) {
                ex.printStackTrace()
                statusLabel.text = "Recording failed: IO Exception"
            }
        }
    }

    private fun stopRecording() {
        line?.stop()
        line?.close()
        statusLabel.text = "Recording stopped"
    }

    private fun transcribeHelloMp3() {
        thread {
            try {
                statusLabel.text = "Transcribing hello.mp3..."
                val processBuilder = ProcessBuilder("python3", "transcribe.py", "hello.mp3")
                processBuilder.redirectErrorStream(true)
                val process = processBuilder.start()
                val reader = process.inputStream.bufferedReader()
                val output = reader.readText()
                process.waitFor()
                
                // フィルタリングして警告メッセージを除去
                val filteredOutput = output.lines()
                    .filterNot { it.contains("UserWarning") || it.contains("FP16 is not supported on CPU; using FP32 instead") }
                    .joinToString("\n")
                
                println(filteredOutput)

                // Update transcription result label on the Event Dispatch Thread
                SwingUtilities.invokeLater {
                    transcriptionResultLabel.text = "Transcription Result: $filteredOutput"
                    statusLabel.text = "Transcription complete"
                }

                // Execute chat.py to get the response
                val chatProcessBuilder = ProcessBuilder("python3", "chat.py", filteredOutput)
                chatProcessBuilder.redirectErrorStream(true)
                val chatProcess = chatProcessBuilder.start()
                val chatReader = chatProcess.inputStream.bufferedReader()
                val chatOutput = chatReader.readText()
                chatProcess.waitFor()
                
                // Update ollama result label on the Event Dispatch Thread
                SwingUtilities.invokeLater {
                    ollamaResultLabel.text = "Ollama Result: ${chatOutput.trim()}"
                }
                
            } catch (ex: IOException) {
                ex.printStackTrace()
                statusLabel.text = "Failed to run transcription script"
            } catch (ex: InterruptedException) {
                ex.printStackTrace()
                statusLabel.text = "Transcription script interrupted"
            }
        }
    }

    private fun runTranscribeScript(audioFilePath: String) {
        thread {
            try {
                val processBuilder = ProcessBuilder("python3", "transcribe.py", audioFilePath)
                processBuilder.redirectErrorStream(true)
                val process = processBuilder.start()
                val reader = process.inputStream.bufferedReader()
                val output = reader.readText()
                process.waitFor()
                
                // フィルタリングして警告メッセージを除去
                val filteredOutput = output.lines()
                    .filterNot { it.contains("UserWarning") || it.contains("FP16 is not supported on CPU; using FP32 instead") }
                    .joinToString("\n")
                
                // Update transcription result label on the Event Dispatch Thread
                SwingUtilities.invokeLater {
                    transcriptionResultLabel.text = "Transcription Result: $filteredOutput"
                    statusLabel.text = "Transcription complete"
                }

                // Execute chat.py to get the response
                val chatProcessBuilder = ProcessBuilder("python3", "chat.py", filteredOutput)
                chatProcessBuilder.redirectErrorStream(true)
                val chatProcess = chatProcessBuilder.start()
                val chatReader = chatProcess.inputStream.bufferedReader()
                val chatOutput = chatReader.readText()
                chatProcess.waitFor()
                
                // Update ollama result label on the Event Dispatch Thread
                SwingUtilities.invokeLater {
                    ollamaResultLabel.text = "Ollama Result: ${chatOutput.trim()}"
                }
                
            } catch (ex: IOException) {
                ex.printStackTrace()
                statusLabel.text = "Failed to run transcription script"
            } catch (ex: InterruptedException) {
                ex.printStackTrace()
                statusLabel.text = "Transcription script interrupted"
            }
        }
    }
}

fun main() {
    SwingUtilities.invokeLater {
        AudioRecorderApp()
    }
}
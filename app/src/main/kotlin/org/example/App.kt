package org.example

import javax.sound.sampled.*
import javax.swing.*
import kotlin.concurrent.thread
import java.io.File
import java.io.IOException

class AudioRecorderApp : JFrame() {
    private var line: TargetDataLine? = null
    private val format = AudioFormat(44100.0f, 16, 1, true, false)
    private var selectedMixer: Mixer? = null
    private val statusLabel = JLabel("Status Label")
    private val statusValue = JLabel("")
    private val transcriptionResultLabel = JLabel("Transcription Result:")
    private val ollamaResultLabel = JLabel("Ollama Result:")
    private val transcriptionResultValue = JLabel("")
    private val ollamaResultValue = JLabel("")
    private val mixers = AudioSystem.getMixerInfo()
    private val selectMixerLabel = JLabel("Select Mixer:")
    private val filteredMixers = mixers.filter { !it.name.startsWith("Port") }
    private val mixerComboBox = JComboBox(filteredMixers.map { it.name }.toTypedArray())
    private var recordingThread: Thread? = null

    init {
        // ウィンドウの設定
        title = "Audio Recorder"
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(400, 400)
        
        // ボタンの作成
        val startButton = JButton("Start Recording")
        val stopButton = JButton("Stop Recording")
        val transcribeHelloButton = JButton("Transcribe hello.mp3")

        // ボタンにアクションリスナーを追加
        startButton.addActionListener { startRecording() }
        stopButton.addActionListener { stopRecording() }
        transcribeHelloButton.addActionListener { transcribeFile("hello.mp3") }

        // レイアウトの設定
        setupLayout(startButton, stopButton, transcribeHelloButton)
    }

    private fun setupLayout(startButton: JButton, stopButton: JButton, transcribeHelloButton: JButton) {
        val layout = GroupLayout(contentPane)
        contentPane.layout = layout
        layout.setAutoCreateGaps(true)
        layout.setAutoCreateContainerGaps(true)

        // 水平方向のグループ設定
        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(selectMixerLabel)
                .addComponent(startButton)
                .addComponent(statusLabel)
                .addComponent(transcriptionResultLabel)
                .addComponent(ollamaResultLabel))
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(mixerComboBox)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(stopButton)
                    .addComponent(transcribeHelloButton))
                .addComponent(statusValue)
                .addComponent(transcriptionResultValue)
                .addComponent(ollamaResultValue))
        )

        // 垂直方向のグループ設定
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(selectMixerLabel)
                .addComponent(mixerComboBox))
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(startButton)
                .addComponent(stopButton)
                .addComponent(transcribeHelloButton))
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(statusLabel)
                .addComponent(statusValue))
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(transcriptionResultLabel)
                .addComponent(transcriptionResultValue))
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(ollamaResultLabel)
                .addComponent(ollamaResultValue))
        )

        // ウィンドウのサイズを自動調整
        pack()
        isVisible = true
    }

    private fun startRecording() {
        // 録音を新しいスレッドで開始
        recordingThread = thread {
            try {
                updateStatus("Recording...")
                val selectedMixerIndex = mixerComboBox.selectedIndex
                val actualMixerInfo = filteredMixers[selectedMixerIndex]
                selectedMixer = AudioSystem.getMixer(actualMixerInfo)

                // ミキサーを開く
                try {
                    selectedMixer!!.open()
                } catch (e: LineUnavailableException) {
                    updateStatus("Selected mixer not supported.")
                    return@thread
                }

                val dataLineInfo = DataLine.Info(TargetDataLine::class.java, format)
                if (!selectedMixer!!.isLineSupported(dataLineInfo)) {
                    updateStatus("Selected mixer does not support the specified format.")
                    return@thread
                }

                // ターゲットデータラインを取得して開始
                line = selectedMixer!!.getLine(dataLineInfo) as TargetDataLine
                line?.open(format)
                line?.start()

                val audioInputStream = AudioInputStream(line)
                val wavFile = File("recording.wav")

                // 録音データをファイルに保存
                saveRecording(audioInputStream, wavFile)
            } catch (ex: LineUnavailableException) {
                ex.printStackTrace()
                updateStatus("Recording failed: Line unavailable")
            }
        }
    }

    private fun saveRecording(audioInputStream: AudioInputStream, wavFile: File) {
        try {
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, wavFile)
            updateStatus("Recording saved to ${wavFile.absolutePath}")
            transcribeFile(wavFile.absolutePath)
        } catch (ex: IOException) {
            ex.printStackTrace()
            updateStatus("Recording failed: IO Exception")
        }
    }

    private fun stopRecording() {
        // 録音を停止
        println("Recording stopped")
        line?.stop()
        line?.close()
        updateStatus("Recording stopped")
        recordingThread?.join()  // スレッドが終了するのを待つ
    }

    private fun transcribeFile(filePath: String) {
        // 新しいスレッドで転写処理を開始
        thread {
            try {
                updateStatus("Transcribing...")
                val processBuilder = ProcessBuilder("python3", "transcribe.py", filePath)
                processBuilder.redirectErrorStream(true)
                val process = processBuilder.start()
                val output = process.inputStream.bufferedReader().readText()
                process.waitFor()

                val filteredOutput = filterWarnings(output)
                SwingUtilities.invokeLater {
                    transcriptionResultValue.text = filteredOutput
                    updateStatus("Transcription complete")
                }

                val chatResponse = runChatScript(filteredOutput)
                SwingUtilities.invokeLater {
                    ollamaResultValue.text = chatResponse.trim()
                }

                readAloud(chatResponse.trim())
            } catch (ex: IOException) {
                ex.printStackTrace()
                updateStatus("Failed to run transcription script")
            } catch (ex: InterruptedException) {
                ex.printStackTrace()
                updateStatus("Transcription script interrupted")
            }
        }
    }

    private fun filterWarnings(output: String): String {
        // 出力から警告メッセージをフィルタリング
        return output.lines()
            .filterNot { it.contains("UserWarning") || it.contains("FP16 is not supported on CPU; using FP32 instead") }
            .joinToString("\n")
    }

    private fun runChatScript(input: String): String {
        // チャットスクリプトを実行
        return try {
            val processBuilder = ProcessBuilder("python3", "chat.py", input)
            processBuilder.redirectErrorStream(true)
            val process = processBuilder.start()
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            output
        } catch (ex: IOException) {
            ex.printStackTrace()
            "Failed to run chat script"
        } catch (ex: InterruptedException) {
            ex.printStackTrace()
            "Chat script interrupted"
        }
    }

    private fun readAloud(text: String) {
        // 読み上げスクリプトを実行
        thread {
            try {
                val processBuilder = ProcessBuilder("python3", "read_aloud.py", text)
                processBuilder.redirectErrorStream(true)
                val process = processBuilder.start()
                process.inputStream.bufferedReader().readText()
                process.waitFor()
                println("complete")
            } catch (ex: IOException) {
                ex.printStackTrace()
                updateStatus("Failed to run read_aloud script")
            } catch (ex: InterruptedException) {
                ex.printStackTrace()
                updateStatus("Read aloud script interrupted")
            }
        }
    }

    private fun updateStatus(message: String) {
        // ステータスを更新
        SwingUtilities.invokeLater {
            statusValue.text = message
        }
    }
}

fun main() {
    SwingUtilities.invokeLater {
        AudioRecorderApp()
    }
}
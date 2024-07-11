import javax.swing.*;

public class CustomLayoutExample extends JFrame {

    public CustomLayoutExample() {
        // タイトルを設定
        setTitle("Custom Layout Example");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // コンポーネントの作成
        JLabel selectMixerLabel = new JLabel("Select Mixer:");
        JComboBox<String> mixerComboBox = new JComboBox<>(new String[]{"Mixer 1", "Mixer 2", "Mixer 3"});
        JButton startRecordingButton = new JButton("Start Recording");
        JButton stopRecordingButton = new JButton("Stop Recording");
        JButton pauseRecordingButton = new JButton("Pause Recording");
        JLabel statusLabel = new JLabel("Status Label");
        JLabel transcriptionResultLabel = new JLabel("Transcription Result:");
        JTextField transcriptionResultField = new JTextField(20);
        JLabel ollamaResultLabel = new JLabel("Ollama Result:");
        JTextField ollamaResultField = new JTextField(20);

        // GroupLayoutの設定
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        // 自動でギャップを設定
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        // 水平方向のグループ設定
        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(selectMixerLabel)
                .addComponent(startRecordingButton)
                .addComponent(statusLabel)
                .addComponent(transcriptionResultLabel)
                .addComponent(ollamaResultLabel))
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(mixerComboBox)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(stopRecordingButton)
                    .addComponent(pauseRecordingButton))
                .addComponent(transcriptionResultField)
                .addComponent(ollamaResultField))
        );

        // 垂直方向のグループ設定
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(selectMixerLabel)
                .addComponent(mixerComboBox))
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(startRecordingButton)
                .addComponent(stopRecordingButton)
                .addComponent(pauseRecordingButton))
            .addComponent(statusLabel)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(transcriptionResultLabel)
                .addComponent(transcriptionResultField))
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(ollamaResultLabel)
                .addComponent(ollamaResultField))
        );

        // ウィンドウのサイズを自動調整
        pack();
    }

    public static void main(String[] args) {
        // SwingのイベントディスパッチスレッドでGUIを作成
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                CustomLayoutExample frame = new CustomLayoutExample();
                frame.setVisible(true);
            }
        });
    }
}
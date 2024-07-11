import javax.sound.sampled.*;

public class MixerTest {
    public static void main(String[] args) {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();

        for (Mixer.Info mixerInfo : mixerInfos) {
            System.out.println("Mixer: " + mixerInfo.getName());
            Mixer mixer = AudioSystem.getMixer(mixerInfo);

            try {
                mixer.open();
                Line.Info[] targetLineInfos = mixer.getTargetLineInfo();
                
                if (targetLineInfos.length == 0) {
                    System.out.println("  No target lines supported.");
                } else {
                    for (Line.Info lineInfo : targetLineInfos) {
                        System.out.println("  Target Line: " + lineInfo);
                        if (lineInfo instanceof DataLine.Info) {
                            DataLine.Info dataLineInfo = (DataLine.Info) lineInfo;
                            AudioFormat[] formats = dataLineInfo.getFormats();
                            for (AudioFormat format : formats) {
                                System.out.println("    Format: " + format);
                            }
                        }
                    }
                }

                Line.Info[] sourceLineInfos = mixer.getSourceLineInfo();

                if (sourceLineInfos.length == 0) {
                    System.out.println("  No source lines supported.");
                } else {
                    for (Line.Info lineInfo : sourceLineInfos) {
                        System.out.println("  Source Line: " + lineInfo);
                        if (lineInfo instanceof DataLine.Info) {
                            DataLine.Info dataLineInfo = (DataLine.Info) lineInfo;
                            AudioFormat[] formats = dataLineInfo.getFormats();
                            for (AudioFormat format : formats) {
                                System.out.println("    Format: " + format);
                            }
                        }
                    }
                }

                mixer.close();
            } catch (LineUnavailableException e) {
                System.out.println("  Mixer unavailable.");
            }
        }
    }
}
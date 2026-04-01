package safariolds.controller;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundManager {
    private static SoundManager instance = new SoundManager();
    protected Clip clip;
    protected FloatControl volumeControl;

    private SoundManager() {
        // Private constructor for singleton pattern.
    }

    public static SoundManager getInstance() {
        return instance;
    }

    public void playBackgroundMusic(String filePath) {
        try {
            File soundFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            clip.loop(Clip.LOOP_CONTINUOUSLY); // Loop indefinitely
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the volume based on a slider value from 0 to 100.
     * At 50 the volume is medium. Below 50 the decrease is very gradual,
     * and above 50 the increase is linear.
     */
    public void setVolume(float volume) {
    if (volumeControl != null) {
        float min = volumeControl.getMinimum(); // e.g., -80.0 dB
        float max = volumeControl.getMaximum(); // e.g., 6.0 dB
        float factor;
        if (volume <= 50) {
            // Use a power mapping with exponent 0.3 so that at 50 the factor is 0.8.
            double normalized = volume / 50.0;
            factor = (float)(0.8 * Math.pow(normalized, 0.3));
        } else {
            // For values above 50, map linearly from 0.8 (at 50) to 1.0 (at 100).
            factor = 0.8f + 0.2f * ((volume - 50) / 50.0f);
        }
        // Map the factor [0,1] to the gain range [min, max].
        float newValue = min + factor * (max - min);
        volumeControl.setValue(newValue);
    }
}

}

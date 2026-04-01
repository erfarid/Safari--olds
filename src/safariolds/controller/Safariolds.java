package safariolds.controller;
import safariolds.view.GameMenu;

import javax.swing.SwingUtilities;

public class Safariolds {
    public static void main(String[] args) {
        // Start background music using the global instance.
        SoundManager.getInstance().playBackgroundMusic("resources/safari_olds_sound.wav");

        SwingUtilities.invokeLater(() -> new GameMenu());
    }
}

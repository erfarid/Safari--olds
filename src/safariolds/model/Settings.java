package safariolds.model;

import safariolds.view.GameMenu;
import safariolds.controller.SoundManager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.*;
import java.util.Properties;

public class Settings {

    private static boolean globalTestMode = false;
    private boolean testMode;
    private String selectedSpeed = "HOUR";
    private JFrame frame;

    public static void enableGlobalTestMode() {
        globalTestMode = true;
    }

    public Settings() {
        if (!globalTestMode) {
            initUI();
        }
    }

    public Settings(boolean testMode) {
        this.testMode = testMode;

        if (!testMode) {
            initUI();
        }
    }

    private void initUI() {
        frame = new JFrame("SETTINGS");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(true);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(139, 69, 19));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 10, 0);

        JLabel titleLabel = new JLabel("SETTINGS", JLabel.CENTER);
        titleLabel.setFont(new Font("ALGERIAN", Font.BOLD, 60));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridy++;
        panel.add(titleLabel, gbc);

        JLabel speedLabel = new JLabel("GAME SPEED:", JLabel.CENTER);
        speedLabel.setFont(new Font("IMPACT", Font.BOLD, 30));
        speedLabel.setForeground(Color.WHITE);
        gbc.gridy++;
        panel.add(speedLabel, gbc);

        JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        speedPanel.setBackground(new Color(139, 69, 19));
        String[] speedOptions = {"HOUR", "DAY", "WEEK"};
        ButtonGroup speedGroup = new ButtonGroup();

        for (String option : speedOptions) {
            JToggleButton speedButton = new JToggleButton(option);
            speedButton.setFont(new Font("IMPACT", Font.BOLD, 25));
            speedButton.setForeground(Color.WHITE);
            speedButton.setBackground(new Color(92, 51, 23));
            speedButton.setFocusPainted(false);
            speedButton.setPreferredSize(new Dimension(160, 50));
            speedButton.setBorder(BorderFactory.createLineBorder(new Color(255, 204, 0), 3));

            if (option.equals("HOUR")) {
                speedButton.setSelected(true);
            }

            speedGroup.add(speedButton);
            speedPanel.add(speedButton);
        }

        gbc.gridy++;
        panel.add(speedPanel, gbc);

        JLabel volumeLabel = new JLabel("GAME VOLUME:", JLabel.CENTER);
        volumeLabel.setFont(new Font("IMPACT", Font.BOLD, 30));
        volumeLabel.setForeground(Color.WHITE);
        gbc.gridy++;
        panel.add(volumeLabel, gbc);

        JSlider volumeSlider = new JSlider(0, 100, loadVolumeSetting());
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setForeground(Color.WHITE);
        volumeSlider.setBackground(new Color(139, 69, 19));
        volumeSlider.setPreferredSize(new Dimension(800, 60));

        volumeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int volume = volumeSlider.getValue();
                SoundManager.getInstance().setVolume(volume);
                saveVolumeSetting(volume);
            }
        });

        gbc.gridy++;
        panel.add(volumeSlider, gbc);

        JButton backButton = new JButton("BACK");
        backButton.setFont(new Font("IMPACT", Font.BOLD, 30));
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(new Color(92, 51, 23));
        backButton.setPreferredSize(new Dimension(220, 60));
        backButton.setBorder(BorderFactory.createLineBorder(new Color(255, 204, 0), 3));
        gbc.gridy++;
        panel.add(backButton, gbc);

        backButton.addActionListener(e -> {
            new GameMenu();
            frame.dispose();
        });

        frame.add(panel);
        frame.setVisible(true);
    }

    private void saveVolumeSetting(int volume) {
        Properties properties = new Properties();
        try (FileOutputStream fos = new FileOutputStream("settings.properties")) {
            properties.setProperty("volume", String.valueOf(volume));
            properties.store(fos, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int loadVolumeSetting() {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("settings.properties")) {
            properties.load(fis);
            String volumeStr = properties.getProperty("volume", "50");
            return Integer.parseInt(volumeStr);
        } catch (IOException e) {
            e.printStackTrace();
            return 50;
        }
    }

    // For testing access
    public int testLoadVolume() {
        return loadVolumeSetting();
    }

    public void testSaveVolume(int volume) {
        saveVolumeSetting(volume);
    }

    public JFrame getFrame() {
        return frame;
    }
}

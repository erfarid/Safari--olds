package safariolds.model;

import safariolds.view.GameMenu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Difficulty {

    private JFrame frame;

    public Difficulty() {
        this(false);
    }

    public Difficulty(boolean testMode) {
        if (!testMode) {
            frame = new JFrame("Difficulty Selection");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setUndecorated(true);
        }

        JPanel difficultyPanel = new DifficultyPanel();
        difficultyPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 10, 0);

        JLabel titleLabel = new JLabel("SELECT DIFFICULTY", JLabel.CENTER);
        titleLabel.setFont(new Font("Algerian", Font.BOLD, 60));
        titleLabel.setForeground(Color.WHITE);
        difficultyPanel.add(titleLabel, gbc);

        gbc.insets = new Insets(10, 0, 10, 0);
        String[] buttonTexts = {"EASY", "MEDIUM", "HARD", "BACK"};
        for (String text : buttonTexts) {
            gbc.gridy++;
            JButton button = createStyledButton(text);

            if (text.equals("BACK")) {
                button.addActionListener(e -> {
                    new GameMenu();
                    if (frame != null) {
                        frame.dispose();
                    }
                });
            }

            difficultyPanel.add(button, gbc);
        }

        if (!testMode && frame != null) {
            frame.add(difficultyPanel);
            frame.setVisible(true);
        }
    }

    public JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Algerian", Font.BOLD, 30));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(102, 51, 0));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 215, 0), 4),
                BorderFactory.createEmptyBorder(10, 20, 10, 20))
        );
        button.setPreferredSize(new Dimension(250, 60));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(255, 165, 0));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(102, 51, 0));
            }
        });

        return button;
    }

    public static class DifficultyPanel extends JPanel {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            g2d.setColor(new Color(205, 133, 63));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.setColor(new Color(139, 69, 19));
            g2d.fillRoundRect(50, 50, getWidth() - 100, getHeight() - 100, 50, 50);
        }
    }

    // For test access
    public JFrame getFrame() {
        return frame;
    }
}

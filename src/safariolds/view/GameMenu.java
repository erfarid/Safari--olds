package safariolds.view;
import safariolds.model.Difficulty;
import safariolds.model.Settings;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GameMenu extends JFrame {

    public GameMenu() {
        setTitle("Safari Olds Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);

        // Custom panel for background drawing
        MenuPanel menuPanel = new MenuPanel();
        menuPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 10, 0); // Move title very close to the top border

        // Title Label
        JLabel titleLabel = new JLabel("Safari Olds", JLabel.CENTER);
        titleLabel.setFont(new Font("Algerian", Font.BOLD, 60)); // More stylish font
        titleLabel.setForeground(Color.WHITE);
        gbc.gridy++;
        menuPanel.add(titleLabel, gbc);

        // Buttons
        gbc.insets = new Insets(10, 0, 10, 0); // Adjust spacing for buttons
        String[] buttonTexts = {"START GAME", "DIFFICULTY", "SETTINGS", "LOAD", "EXIT"};
        for (String text : buttonTexts) {
            JButton button = createStyledButton(text);
            gbc.gridy++;

            if (text.equals("START GAME")) {
                // Action for the "START GAME" button
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        new StartGame(); // Open the StartGame window
                        dispose(); // Close the GameMenu window
                    }
                });
            } else if (text.equals("DIFFICULTY")) {
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        new Difficulty(); // Open Difficulty screen
                        dispose(); // Close GameMenu
                    }
                });
            } else if (text.equals("SETTINGS")) {
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        new Settings(); // Open Settings screen
                    }
                });
            } else if (text.equals("EXIT")) {
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.exit(0); // Exit the application
                    }
                });
            }

            menuPanel.add(button, gbc);
        }

        add(menuPanel);
        setVisible(true);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Impact", Font.BOLD, 30));
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
    
    
    public class MenuPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            g2d.setColor(new Color(205, 133, 63));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.setColor(new Color(139, 69, 19));
            g2d.fillRoundRect(50, 50, getWidth() - 100, getHeight() - 100, 50, 50);
        }
    }

}

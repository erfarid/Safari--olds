package safariolds.view;
import safariolds.controller.GameMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StartGame extends JFrame {

    public StartGame() {
        setTitle("Start Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen mode
        setUndecorated(true); // Hide window borders

        // Create the main panel
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(139, 69, 19)); // Dark brown background

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 10, 0);

        // Title Label
        JLabel titleLabel = new JLabel("Welcome to the Safari Game", JLabel.CENTER);
        titleLabel.setFont(new Font("ALGERIAN", Font.BOLD, 50));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridy++;
        panel.add(titleLabel, gbc);

        // Player Name Label with more stylish font
        JLabel playerNameLabel = new JLabel("Player Name", JLabel.CENTER); // Updated label text
        playerNameLabel.setFont(new Font("Segoe Script", Font.BOLD, 40)); // Changed to a more stylish font
        playerNameLabel.setForeground(Color.WHITE);
        gbc.gridy++;
        panel.add(playerNameLabel, gbc);

        // Player Name Input Field with the "Comic Sans MS" font
        JTextField playerNameField = new JTextField();
        playerNameField.setFont(new Font("Comic Sans MS", Font.PLAIN, 30)); // Using Comic Sans MS font
        playerNameField.setPreferredSize(new Dimension(300, 40));
        gbc.gridy++;
        panel.add(playerNameField, gbc);

        // Starting Amount Message with the same stylish font
        JLabel amountLabel = new JLabel("Your adventure starts with a total of 10,000 euros.", JLabel.CENTER);
        amountLabel.setFont(new Font("Segoe Script", Font.BOLD, 40)); // Changed to the same font as player name
        amountLabel.setForeground(Color.WHITE);
        gbc.gridy++;
        panel.add(amountLabel, gbc);

        // Ready Button with the same font as title
        JButton readyButton = new JButton("PLAY");
        readyButton.setFont(new Font("ALGERIAN", Font.BOLD, 40)); // Changed to ALGERIAN font
        readyButton.setForeground(Color.WHITE);
        readyButton.setBackground(new Color(92, 51, 23)); // Dark brown button
        readyButton.setPreferredSize(new Dimension(220, 60));
        readyButton.setBorder(BorderFactory.createLineBorder(new Color(255, 204, 0), 3)); // Yellow border
        gbc.gridy++;
        panel.add(readyButton, gbc);

        // Back Button with the same font as title
        JButton backButton = new JButton("BACK");
        backButton.setFont(new Font("ALGERIAN", Font.BOLD, 40)); // Changed to ALGERIAN font
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(new Color(92, 51, 23)); // Dark brown button
        backButton.setPreferredSize(new Dimension(220, 60));
        backButton.setBorder(BorderFactory.createLineBorder(new Color(255, 204, 0), 3)); // Yellow border
        gbc.gridy++;
        panel.add(backButton, gbc);

        // Action for the Ready Button
        readyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String playerName = playerNameField.getText().trim();
                if (playerName.isEmpty()) {
                    showErrorMessage("Please enter a player name.");
                } else {
                    // Proceed directly to the GameMap without showing a message
                    new GameMap(playerName, 100000); // Launch GameMap with player name and 10,000 euros
                    dispose(); // Close the StartGame window
                }
            }
        });

        // Action for the Back Button
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new GameMenu(); // Open the GameMenu screen
                dispose(); // Close the StartGame window
            }
        });

        // Add panel to the frame
        add(panel);
        setVisible(true);
    }

    private void showErrorMessage(String message) {
        JPanel errorPanel = new JPanel();
        errorPanel.setBackground(new Color(139, 69, 19)); 
        errorPanel.setPreferredSize(new Dimension(500, 100)); 

        JLabel errorLabel = new JLabel(message);
        errorLabel.setFont(new Font("ALGERIAN", Font.BOLD, 30)); 
        errorLabel.setForeground(Color.RED); 
        errorPanel.add(errorLabel);

        // Show the error panel at the bottom of the StartGame frame
        JOptionPane.showMessageDialog(this, errorPanel, "Error", JOptionPane.PLAIN_MESSAGE);
    }
}

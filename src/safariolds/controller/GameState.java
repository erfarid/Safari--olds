package safariolds.controller;

import javax.swing.*;
import java.awt.*;

public class GameState {
    public static final int WIN_AMOUNT = 150000;
    public static final int INITIAL_AMOUNT = 100000;
    
    private final GameMap gameMap;
    public boolean gameEnded = false;

    public GameState(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    public void checkGameState(int currentMoney) {
        if (gameEnded) return;

        if (currentMoney >= WIN_AMOUNT) {
            gameEnded = true;
            showGameResult("Congratulations!", 
                "You've reached €150,000! You won the game!");
        } else if (currentMoney <= 0) {
            gameEnded = true;
            showGameResult("Game Over", 
                "You've run out of money! Better luck next time.");
        }
    }

    public void showGameResult(String title, String message) {
        // Use SwingUtilities to ensure this runs on the EDT
        SwingUtilities.invokeLater(() -> {
            // Use the gameMap field that was passed to the constructor
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(gameMap.getMainPanel());
            
            int choice = JOptionPane.showConfirmDialog(
                parentFrame,  // Now using the proper parent component
                message + "\nWould you like to play again?", 
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
            
            if (choice == JOptionPane.YES_OPTION) {
                gameMap.restartGame();
            } else {
                System.exit(0);
            }
        });
    }
}
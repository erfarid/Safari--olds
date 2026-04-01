package safariolds.model;

import safariolds.controller.GameMap;

import javax.swing.*;
import java.util.Iterator;
import java.util.List;

public class Ranger {
    private int row;
    private int col;
    private int salary;
    private GameMap gameMap;
    private JLabel rangerLabel;
    private Timer movementTimer;
    private static final int PROTECTION_RADIUS = 3;

    public Ranger(int row, int col, GameMap gameMap) {
        this.row = row;
        this.col = col;
        this.salary = 1000;
        this.gameMap = gameMap;

        // GUI setup only if not in test mode
        if (!gameMap.isTestMode()) {
            rangerLabel = new JLabel(gameMap.getObjectIcons().get("ranger"));
            rangerLabel.setSize(rangerLabel.getIcon().getIconWidth(), rangerLabel.getIcon().getIconHeight());
            rangerLabel.setLocation(col * gameMap.getTileSize(), row * gameMap.getTileSize());
            gameMap.getGridContainer().add(rangerLabel, JLayeredPane.PALETTE_LAYER);
        }
    }

    public void eliminatePredator(String predatorType) {
        List<Animal> animals = gameMap.getAnimals();
        Iterator<Animal> iterator = animals.iterator();

        while (iterator.hasNext()) {
            Animal animal = iterator.next();
            if (animal instanceof Carnivore && predatorType.equalsIgnoreCase("carnivore")) {
                if (isWithinRadius(animal.getRow(), animal.getCol())) {
                    gameMap.removeObjectFromTile(animal.getRow(), animal.getCol());
                    iterator.remove();
                    gameMap.updateMoney(gameMap.getMoney() + 500);
                    if (!gameMap.isTestMode()) {
                        JOptionPane.showMessageDialog(gameMap.getFrame(), "Ranger eliminated a " + predatorType + "!");
                    }
                    break;
                }
            }
        }
    }

    public void protectAgainstPoachers() {
        List<Poacher> poachers = gameMap.getPoachers();
        Iterator<Poacher> iterator = poachers.iterator();
        while (iterator.hasNext()) {
            Poacher poacher = iterator.next();
            if (isWithinRadius(poacher.getRow(), poacher.getCol())) {
                gameMap.removeObjectFromTile(poacher.getRow(), poacher.getCol());
                iterator.remove();
                gameMap.updateMoney(gameMap.getMoney() + 500);
                if (!gameMap.isTestMode()) {
                    JOptionPane.showMessageDialog(gameMap.getFrame(), "Ranger eliminated a poacher!");
                }
            }
        }
    }

    public boolean isWithinRadius(int targetRow, int targetCol) {
        int distance = Math.abs(targetRow - row) + Math.abs(targetCol - col);
        return distance <= PROTECTION_RADIUS;
    }

    public void moveTo(int newRow, int newCol) {
        if (newRow < 0 || newRow >= gameMap.ROWS || newCol < 0 || newCol >= gameMap.COLS) {
            System.out.println("Invalid move: Ranger cannot move outside the map boundaries.");
            return;
        }

        if (gameMap.isTestMode()) {
            // In test mode, skip animation and directly move
            row = newRow;
            col = newCol;
            afterMove();
            return;
        }

        int startX = col * gameMap.getTileSize();
        int startY = row * gameMap.getTileSize();
        int targetX = newCol * gameMap.getTileSize();
        int targetY = newRow * gameMap.getTileSize();

        if (movementTimer != null && movementTimer.isRunning()) {
            movementTimer.stop();
        }

        movementTimer = new Timer(10, null);
        final int[] currentX = {startX};
        final int[] currentY = {startY};
        int steps = 20;
        int stepX = (targetX - startX) / steps;
        int stepY = (targetY - startY) / steps;

        movementTimer.addActionListener(e -> {
            currentX[0] += stepX;
            currentY[0] += stepY;
            rangerLabel.setLocation(currentX[0], currentY[0]);

            if ((stepX > 0 && currentX[0] >= targetX) || (stepX < 0 && currentX[0] <= targetX)) {
                if ((stepY > 0 && currentY[0] >= targetY) || (stepY < 0 && currentY[0] <= targetY)) {
                    movementTimer.stop();
                    row = newRow;
                    col = newCol;
                    afterMove();
                }
            }
        });

        movementTimer.start();
    }

    public void afterMove() {
        eliminatePredator("carnivore");
    }

    public void moveRandomly() {
        int newRow = row + (int) (Math.random() * 3) - 1;
        int newCol = col + (int) (Math.random() * 3) - 1;

        if (newRow >= 0 && newRow < gameMap.ROWS && newCol >= 0 && newCol < gameMap.COLS) {
            if (gameMap.isTileEmpty(newRow, newCol)) {
                moveTo(newRow, newCol);
            }
        } else {
            System.out.println("Ranger attempted to move outside the map boundaries: (" + newRow + ", " + newCol + ")");
        }
    }

    public boolean paySalary() {
        if (gameMap.getMoney() >= salary) {
            gameMap.updateMoney(gameMap.getMoney() - salary);
            if (!gameMap.isTestMode()) {
                JOptionPane.showMessageDialog(gameMap.getFrame(), "Ranger salary paid: €" + salary);
            }
            return true;
        } else {
            if (!gameMap.isTestMode()) {
                JOptionPane.showMessageDialog(gameMap.getFrame(), "Not enough money to pay the ranger's salary!");
            }
            return false;
        }
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getSalary() {
        return salary;
    }
}

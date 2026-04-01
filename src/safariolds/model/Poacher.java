package safariolds.model;

import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import safariolds.controller.GameMap;

public class Poacher {

    private int row;
    private int col;
    private int xPos;
    private int yPos;
    private JLabel label;
    private GameMap gameMap;
    private final int tileSize;
    private boolean isVisible;
    private static final int MAX_DISTANCE_FROM_JEEP = 4;
    private static final int HUNTING_RANGE = 5; // Tiles
    private static final int SHOOTING_DISTANCE = 1; // Tile
    private static final int MOVE_DELAY = 500; // ms
    private Timer movementTimer;
    public Animal targetAnimal;
    private Random random = new Random();
    private float speedMultiplier = 1.0f;

    public Poacher(int row, int col, GameMap gameMap) {
        this.row = row;
        this.col = col;
        this.gameMap = gameMap;
        this.tileSize = gameMap.getTileSize();
        this.xPos = col * tileSize;
        this.yPos = row * tileSize;
        this.isVisible = false;

        if (!gameMap.isTestMode()) {
            this.label = new JLabel(gameMap.getObjectIcons().get("poacher"));
            this.label.setSize(tileSize, tileSize);
            this.label.setLocation(xPos, yPos);
            this.label.setVisible(false);

            SwingUtilities.invokeLater(() -> {
                try {
                    gameMap.getGridContainer().add(label, JLayeredPane.PALETTE_LAYER);
                    gameMap.getGridContainer().moveToFront(label);
                    gameMap.getGridContainer().revalidate();
                } catch (Exception e) {
                    System.err.println("Error adding poacher to container: " + e.getMessage());
                }
            });
        }

        startHuntingBehavior();
    }

    public void setSpeedMultiplier(float multiplier) {
        this.speedMultiplier = multiplier;
        if (movementTimer != null) {
            movementTimer.cancel();
            startHuntingBehavior(); // Restart with new speed
        }
    }

    public void startHuntingBehavior() {
        movementTimer = new Timer(true);
        movementTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isVisible) {
                    huntAnimals();
                }
            }
        }, (int) (MOVE_DELAY / speedMultiplier), (int) (MOVE_DELAY / speedMultiplier));
    }

    public void huntAnimals() {
        // Find closest animal within hunting range
        targetAnimal = findClosestAnimal();

        if (targetAnimal != null) {
            // Move toward the animal
            moveTowardTarget();

            // Check if close enough to shoot
            if (isInShootingRange()) {
                shootAnimal();
            }
        } else {
            // No target found, wander randomly
            wander();
        }
    }

    public Animal findClosestAnimal() {
        List<Animal> animals = gameMap.getAnimals();
        Animal closest = null;
        int minDistance = Integer.MAX_VALUE;

        for (Animal animal : animals) {
            int distance = Math.abs(animal.getRow() - row) + Math.abs(animal.getCol() - col);
            if (distance < HUNTING_RANGE && distance < minDistance) {
                closest = animal;
                minDistance = distance;
            }
        }
        return closest;
    }

    public void moveTowardTarget() {
        // Store target locally to prevent race conditions
        Animal currentTarget = targetAnimal;
        if (currentTarget == null) {
            return;
        }

        int targetRow = currentTarget.getRow();
        int targetCol = currentTarget.getCol();

        // Determine direction to move
        int rowDirection = Integer.compare(targetRow, row);
        int colDirection = Integer.compare(targetCol, col);

        // Calculate new position
        int newRow = row + rowDirection;
        int newCol = col + colDirection;

        // Validate move
        if (isValidMove(newRow, newCol)) {
            updatePosition(newRow, newCol);
        } else {
            // Try alternative moves if direct path blocked
            if (isValidMove(row + rowDirection, col)) {
                updatePosition(row + rowDirection, col);
            } else if (isValidMove(row, col + colDirection)) {
                updatePosition(row, col + colDirection);
            }
        }
    }

    public void setTargetAnimal(Animal target) {
        this.targetAnimal = target;
    }

    public boolean isValidMove(int newRow, int newCol) {
        // Check boundaries
        if (newRow < 0 || newRow >= gameMap.getROWS()
                || newCol < 0 || newCol >= gameMap.getCOLS()) {
            return false;
        }

        // Check terrain (can move through grass or roads)
        char terrain = gameMap.getTerrainMatrix()[newRow][newCol];
        return terrain == 'G' || terrain == 'R';
    }

    public void updatePosition(int newRow, int newCol) {
        // Always update coordinates
        row = newRow;
        col = newCol;
        xPos = col * tileSize;
        yPos = row * tileSize;

        // Skip all GUI logic in test mode or if label is null
        if (gameMap.isTestMode() || label == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                if (label.getParent() != null) {
                    label.getParent().remove(label);
                }

                label.setLocation(xPos, yPos);
                gameMap.getGridContainer().add(label, JLayeredPane.PALETTE_LAYER);
                gameMap.getGridContainer().moveToFront(label);
                gameMap.getGridContainer().validate();
            } catch (Exception e) {
                System.err.println("Error updating poacher position: " + e.getMessage());
            }
        });
    }

    public boolean isInShootingRange() {
        if (targetAnimal == null) {
            return false;
        }

        // Store target locally to prevent race conditions
        Animal currentTarget = targetAnimal;
        if (currentTarget == null) {
            return false;
        }

        int distance = Math.abs(currentTarget.getRow() - row)
                + Math.abs(currentTarget.getCol() - col);
        return distance <= SHOOTING_DISTANCE;
    }

    public void shootAnimal() {
        // Store target locally to prevent race conditions
        Animal currentTarget = targetAnimal;
        if (currentTarget == null) {
            return;
        }

        System.out.println("Poacher shot animal at " + currentTarget.getRow() + "," + currentTarget.getCol());

        // Remove animal from game
        SwingUtilities.invokeLater(() -> {
            try {
                // Double check target is still valid
                if (currentTarget != null && gameMap.getAnimals().contains(currentTarget)) {
                    currentTarget.stopTimers();
                    JLabel animalLabel = currentTarget.getLabel();
                    if (animalLabel != null && animalLabel.getParent() != null) {
                        animalLabel.getParent().remove(animalLabel);
                    }
                    gameMap.getAnimals().remove(currentTarget);
                    gameMap.getGridContainer().validate();

                    // Play shooting animation/effect
                    showShootingEffect();
                }
            } catch (Exception e) {
                System.err.println("Error shooting animal: " + e.getMessage());
            }
        });

        // Clear target after shooting
        targetAnimal = null;
    }

    public void showShootingEffect() {
        // Create and show a shooting effect
        JLabel shotEffect = new JLabel(gameMap.getObjectIcons().get("shot"));
        shotEffect.setSize(tileSize, tileSize);
        shotEffect.setLocation(xPos, yPos);

        SwingUtilities.invokeLater(() -> {
            try {
                gameMap.getGridContainer().add(shotEffect, JLayeredPane.PALETTE_LAYER);
                gameMap.getGridContainer().moveToFront(shotEffect);
                gameMap.getGridContainer().validate();

                // Remove after short delay
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(() -> {
                            try {
                                if (shotEffect.getParent() != null) {
                                    shotEffect.getParent().remove(shotEffect);
                                    gameMap.getGridContainer().validate();
                                }
                            } catch (Exception e) {
                                System.err.println("Error removing shot effect: " + e.getMessage());
                            }
                        });
                    }
                }, 500);
            } catch (Exception e) {
                System.err.println("Error showing shooting effect: " + e.getMessage());
            }
        });
    }

    public void wander() {
        // Random movement when no target
        int direction = random.nextInt(4);
        int newRow = row;
        int newCol = col;

        switch (direction) {
            case 0:
                newRow--;
                break; // Up
            case 1:
                newRow++;
                break; // Down
            case 2:
                newCol--;
                break; // Left
            case 3:
                newCol++;
                break; // Right
        }

        if (isValidMove(newRow, newCol)) {
            updatePosition(newRow, newCol);
        }
    }

    public void updateVisibility(Jeep jeep) {
        if (jeep == null || label == null) {
            return;
        }

        int distance = Math.abs(jeep.getRow() - row) + Math.abs(jeep.getCol() - col);
        boolean shouldBeVisible = distance <= MAX_DISTANCE_FROM_JEEP;

        if (shouldBeVisible != isVisible) {
            isVisible = shouldBeVisible;
            SwingUtilities.invokeLater(() -> {
                try {
                    if (isVisible) {
                        if (label.getParent() == null) {
                            gameMap.getGridContainer().add(label, JLayeredPane.PALETTE_LAYER);
                        }
                        label.setVisible(true);
                        gameMap.getGridContainer().moveToFront(label);
                    } else {
                        label.setVisible(false);
                    }
                    // Use validate() instead of revalidate() to prevent repaint issues
                    gameMap.getGridContainer().validate();
                } catch (Exception e) {
                    System.err.println("Error updating poacher visibility: " + e.getMessage());
                }
            });
        }
    }

    public void remove() {
        if (movementTimer != null) {
            movementTimer.cancel();
        }
        SwingUtilities.invokeLater(() -> {
            try {
                if (label != null && label.getParent() != null) {
                    label.getParent().remove(label);
                    gameMap.getGridContainer().validate();
                }
            } catch (Exception e) {
                System.err.println("Error removing poacher: " + e.getMessage());
            }
        });
    }
    // Add to Poacher class

    // Getters
    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public JLabel getLabel() {
        return label;
    }

    public boolean isVisible() {
        return isVisible;
    }
}

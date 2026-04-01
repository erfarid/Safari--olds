package safariolds.model;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import safariolds.controller.*;
import safariolds.model.*;

public class Jeep {

    private int row;
    private int col;
    private int xPos;
    private int yPos;
    private boolean isActive = true; 
    private JLabel label;
    private java.util.Timer moveTimer;
    private List<Tourist> tourists = new ArrayList<>();
    private java.util.Timer touristTimer;

    private GameMap gameMap;
    private final int tileSize;
    private List<Point> path;

    private float speedMultiplier = 1.0f; // Default speed multiplier
    private static final int BASE_PIXELS_PER_STEP = 5;
    private static final int BASE_MOVE_DELAY = 40;

    private int pixelsPerStep = BASE_PIXELS_PER_STEP;
    private int moveDelay = BASE_MOVE_DELAY;

    public List<Tourist> getTourists() {
        return tourists;
    }

    public Jeep(int row, int col, GameMap gameMap) {
        this.row = row;
        this.col = col;
        this.gameMap = gameMap;
        this.tileSize = gameMap.getTileSize();

        this.xPos = col * tileSize;
        this.yPos = row * tileSize;

        this.label = new JLabel(gameMap.getObjectIcons().get("jeep"));
        this.label.setSize(tileSize, tileSize);
        this.label.setLocation(xPos, yPos);

        // Add to grid container and ensure it stays there
        SwingUtilities.invokeLater(() -> {
            try {
                if (gameMap.getGridContainer() != null) {
                    gameMap.getGridContainer().add(label, JLayeredPane.PALETTE_LAYER);
                    gameMap.getGridContainer().moveToFront(label);
                    gameMap.getGridContainer().revalidate();
                }
            } catch (Exception e) {
                System.err.println("Error adding jeep to container: " + e.getMessage());
            }
        });

        startJeepMovement();
    }

    private void startJeepMovement() {
        List<List<Point>> allPaths = findAllValidPaths();
        if (allPaths.isEmpty()) {
            removeFromMap();
            return;
        }

        // Create 4 tourists (max capacity)
        for (int i = 0; i < 4; i++) {
            tourists.add(new Tourist(this, gameMap, i));
        }

        // Show tourists immediately (waiting to board)
        setTouristsVisible(true);

        // ⏳ Delay movement by 3 seconds (3000 ms) - boarding time
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    try {
                        // Hide tourists while moving
                        setTouristsVisible(false);

                        // Pick a random valid path
                        path = allPaths.get(new Random().nextInt(allPaths.size()));

                        moveTimer = new java.util.Timer();
                        moveTimer.scheduleAtFixedRate(new java.util.TimerTask() {
                            int stepIndex = 0;

                            @Override
                            public void run() {
                                try {
                                    if (stepIndex >= path.size()) {
                                        stopAndRemove();
                                        return;
                                    }

                                    Point target = path.get(stepIndex);
                                    int targetX = target.y * tileSize;
                                    int targetY = target.x * tileSize;

                                    // Smooth movement logic
                                    if (xPos < targetX) {
                                        xPos = Math.min(xPos + pixelsPerStep, targetX);
                                    }
                                    if (xPos > targetX) {
                                        xPos = Math.max(xPos - pixelsPerStep, targetX);
                                    }
                                    if (yPos < targetY) {
                                        yPos = Math.min(yPos + pixelsPerStep, targetY);
                                    }
                                    if (yPos > targetY) {
                                        yPos = Math.max(yPos - pixelsPerStep, targetY);
                                    }

                                    // Update position within the same container
                                    SwingUtilities.invokeLater(() -> {
                                        try {
                                            if (label.getParent() == null) {
                                                // If label has no parent, add it back
                                                gameMap.getGridContainer().add(label, JLayeredPane.PALETTE_LAYER);
                                            }
                                            label.setLocation(xPos, yPos);
                                            gameMap.getGridContainer().moveToFront(label);
                                            gameMap.getGridContainer().revalidate();
                                        } catch (Exception e) {
                                            System.err.println("Error updating jeep position: " + e.getMessage());
                                            // Continue movement even if there's an error
                                        }
                                    });

                                    // Update poacher visibility safely
                                    SwingUtilities.invokeLater(() -> {
                                        try {
                                            List<Poacher> poachers = gameMap.getPoachers();
                                            if (poachers != null) {
                                                for (Poacher poacher : poachers) {
                                                    if (poacher != null) {
                                                        poacher.updateVisibility(Jeep.this);
                                                    }
                                                }
                                            }
                                        } catch (Exception e) {
                                            System.err.println("Error updating poacher visibility: " + e.getMessage());
                                        }
                                    });

                                    // Occasionally spawn new poachers
                                    if (new Random().nextInt(100) < 5) {
                                        SwingUtilities.invokeLater(() -> {
                                            try {
                                                gameMap.spawnPoachersNearJeep(Jeep.this);
                                            } catch (Exception e) {
                                                System.err.println("Error spawning poachers: " + e.getMessage());
                                            }
                                        });
                                    }

                                    if (xPos == targetX && yPos == targetY) {
                                        row = target.x;
                                        col = target.y;
                                        stepIndex++;
                                        
                                        // If we've reached the end, stop and remove
                                        if (stepIndex >= path.size()) {
                                            stopAndRemove();
                                        }
                                    }
                                } catch (Exception e) {
                                    System.err.println("Error in Jeep movement: " + e.getMessage());
                                    // Continue movement even if there's an error
                                }
                            }
                        }, 0, moveDelay);
                    } catch (Exception e) {
                        System.err.println("Error starting jeep movement: " + e.getMessage());
                    }
                });
            }
        }, 3000);
    }
    
    public void setSpeedMultiplier(float multiplier) {
        this.speedMultiplier = multiplier;
        this.pixelsPerStep = (int) (BASE_PIXELS_PER_STEP * multiplier);
        this.moveDelay = (int) (BASE_MOVE_DELAY / multiplier);

        if (moveTimer != null) {
            moveTimer.cancel();
            startJeepMovement();
        }
    }

    private void stopAndRemove() {
        isActive = false; // Mark the jeep as inactive
        if (moveTimer != null) {
            moveTimer.cancel();
            moveTimer.purge();
            moveTimer = null;
        }

        // Increase capital or perform other end-of-road logic
        gameMap.increaseCapital();

        // Show tourists again (disembarking)
        setTouristsVisible(true);

        // Delay removal by 3 seconds - disembarking time
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    // Remove all tourists
                    for (Tourist tourist : tourists) {
                        tourist.remove();
                    }
                    tourists.clear();

                    // Remove jeep
                    removeFromMap();
                });
            }
        }, 3000);
    }

    private void setTouristsVisible(boolean visible) {
        SwingUtilities.invokeLater(() -> {
            try {
                for (Tourist tourist : tourists) {
                    if (tourist != null) {
                        tourist.updatePosition();
                        JLabel touristLabel = tourist.getLabel();
                        if (touristLabel != null) {
                            touristLabel.setVisible(visible);
                            if (visible) {
                                if (touristLabel.getParent() == null) {
                                    gameMap.getGridContainer().add(touristLabel, JLayeredPane.PALETTE_LAYER);
                                }
                                gameMap.getGridContainer().moveToFront(touristLabel);
                            }
                        }
                    }
                }
                // Ensure jeep is on top of tourists
                if (label != null) {
                    if (label.getParent() == null) {
                        gameMap.getGridContainer().add(label, JLayeredPane.PALETTE_LAYER);
                    }
                    gameMap.getGridContainer().moveToFront(label);
                    gameMap.getGridContainer().revalidate();
                }
            } catch (Exception e) {
                System.err.println("Error setting tourist visibility: " + e.getMessage());
            }
        });
    }

    private void removeFromMap() {
        SwingUtilities.invokeLater(() -> {
            try {
                Container parent = label.getParent();
                if (parent != null) {
                    parent.remove(label);
                    parent.revalidate();
                } else {
                    // If label has no parent, just dispose of it
                    label = null;
                }
                
                // Also remove all tourists
                for (Tourist tourist : tourists) {
                    Container touristParent = tourist.getLabel().getParent();
                    if (touristParent != null) {
                        touristParent.remove(tourist.getLabel());
                        touristParent.revalidate();
                    }
                }
                tourists.clear();
            } catch (Exception e) {
                System.err.println("Error removing jeep from map: " + e.getMessage());
            }
        });
    }

    // Finds all valid paths from START (10,0) to END (10,39), avoiding backtracking
    public List<List<Point>> findAllValidPaths() {
        int rows = gameMap.getROWS();
        int cols = gameMap.getCOLS();
        char[][] terrain = gameMap.getTerrainMatrix();
        Point start = new Point(10, 0);
        Point end = new Point(10, 39);

        List<List<Point>> validPaths = new ArrayList<>();
        Queue<List<Point>> queue = new LinkedList<>();
        List<Point> initialPath = new ArrayList<>();
        initialPath.add(start);
        queue.add(initialPath);

        int[][] directions = {
            {-1, 0}, // up
            {1, 0},  // down
            {0, 1}   // right only (no left)
        };

        while (!queue.isEmpty()) {
            List<Point> currentPath = queue.poll();
            Point current = currentPath.get(currentPath.size() - 1);

            if (current.equals(end)) {
                validPaths.add(currentPath);
                continue;
            }

            for (int[] d : directions) {
                int newRow = current.x + d[0];
                int newCol = current.y + d[1];
                Point next = new Point(newRow, newCol);

                if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols
                        && terrain[newRow][newCol] == 'R'
                        && !currentPath.contains(next)) {

                    List<Point> newPath = new ArrayList<>(currentPath);
                    newPath.add(next);
                    queue.add(newPath);
                }
            }
        }

        // If no valid paths found, create a direct path to the end
        if (validPaths.isEmpty()) {
            List<Point> directPath = new ArrayList<>();
            directPath.add(start);
            directPath.add(end);
            validPaths.add(directPath);
        }

        return validPaths;
    }

    // Update speed multiplier and restart movement
    public void updateSpeed(float speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
        this.pixelsPerStep = (int) (BASE_PIXELS_PER_STEP * speedMultiplier);
        this.moveDelay = (int) (BASE_MOVE_DELAY / speedMultiplier);

        // Restart movement with new speed
        if (moveTimer != null) {
            moveTimer.cancel();
            startJeepMovement();
        }
    }

    // Getters
    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getXPos() {
        return xPos;
    }

    public int getYPos() {
        return yPos;
    }

    public JLabel getLabel() {
        return label;
    }
}
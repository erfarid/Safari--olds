package safariolds.model;

import safariolds.controller.GameMap;
import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import safariolds.controller.State;

public abstract class Animal {

    protected int row;
    protected int col;
    protected int direction;
    protected int xPos;
    protected int yPos;
    protected JLabel label;
    protected String type;
    protected Timer moveTimer;
    protected Timer reproductionTimer; // Timer for checking reproduction
    protected Timer lifespanTimer; // Timer for animal's lifespan
    protected boolean atPond = false;
    protected GameMap gameMap;
    protected static final int ANIMAL_MOVE_DELAY = 40;
    protected static final int MOVE_DELAY = 40;
    protected static final int PIXELS_PER_MOVE = 4;
    protected static final int POND_WAIT_TIME = 5000;
    protected static final int REPRODUCTION_INTERVAL = 45000; // 45 seconds between reproductions
    protected static final int LIFESPAN = 60000; // 60 seconds lifespan for all animals
    protected Random random = new Random();

    // Counter for tracking complete tiles moved in current direction
    protected int tilesCrossed = 0;
    protected static final int DIRECTION_CHANGE_TILES = 2; // Change direction after crossing 2 complete tiles

    // Remember last tile position to detect when a new tile is entered
    protected int lastTileRow;
    protected int lastTileCol;
    public float speedMultiplier = 1.0f;
    private static final int GROUPING_DISTANCE = 100;

    public Animal(int row, int col, JLabel label, String type, GameMap gameMap) {
        if (gameMap.getTerrainMatrix()[row][col] != 'G') {
            throw new IllegalArgumentException("Animals must start on green land");
        }
        //State.addListener(this);
        this.speedMultiplier = State.getMode().getSpeedMultiplier();
        this.row = row;
        this.col = col;
        this.label = label;
        this.type = type;
        this.gameMap = gameMap;
        this.direction = random.nextInt(8) + 1; // Updated to 8 directions
        this.xPos = col * gameMap.getTileSize();
        this.yPos = row * gameMap.getTileSize();
        this.lastTileRow = row;
        this.lastTileCol = col;
        startMovement();
        startReproductionTimer();
        startLifespanTimer();
    }

    public void adjustTimers(float timeMultiplier) {
        // Calculate with float first, then convert to int
        float reproductionInterval = REPRODUCTION_INTERVAL * timeMultiplier;
        startReproductionTimer((int) reproductionInterval);

        float lifespan = LIFESPAN * timeMultiplier;
        startLifespanTimer((int) lifespan);
    }

    private void startLifespanTimer(int lifespan) {
        if (lifespanTimer != null) {
            lifespanTimer.cancel();
            lifespanTimer.purge();
        }

        int safeLifespan = Math.max(1000, lifespan); // Minimum 1 second

        lifespanTimer = new Timer(true);
        lifespanTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                EventQueue.invokeLater(() -> {
                    die();
                });
            }
        }, safeLifespan);
    }

    public void startReproductionTimer(int interval) {
        if (reproductionTimer != null) {
            reproductionTimer.cancel();
            reproductionTimer.purge();
        }

        // Ensure minimum delay of 100ms to prevent system overload
        int safeInterval = Math.max(100, interval);

        reproductionTimer = new Timer(true);
        reproductionTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                EventQueue.invokeLater(() -> {
                    try {
                        reproduce();
                    } catch (Exception e) {
                        System.err.println("Reproduction error: " + e.getMessage());
                    }
                });
            }
        }, safeInterval, safeInterval);
    }

    public void startMovement() {
        // Stop any existing timer first
        if (moveTimer != null) {
            moveTimer.cancel();
            moveTimer.purge();
        }

        // Create a new timer for movement
        moveTimer = new Timer(true); // Use daemon timer
        moveTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                EventQueue.invokeLater(() -> {
                    if (!atPond) {
                        move();
                    }
                });
            }
        }, (int) (MOVE_DELAY / speedMultiplier), (int) (MOVE_DELAY / speedMultiplier));
    }
    public void startReproductionTimer() {
        // Start a timer to periodically check for reproduction
        if (reproductionTimer != null) {
            reproductionTimer.cancel();
            reproductionTimer.purge();
        }

        reproductionTimer = new Timer(true); // Use daemon timer
        reproductionTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                EventQueue.invokeLater(() -> {
                    reproduce();
                });
            }
        }, REPRODUCTION_INTERVAL, REPRODUCTION_INTERVAL);
    }

    public void startLifespanTimer() {
        // Start timer for animal's lifespan - all animals live for 60 seconds
        if (lifespanTimer != null) {
            lifespanTimer.cancel();
            lifespanTimer.purge();
        }

        lifespanTimer = new Timer(true); // Use daemon timer
        lifespanTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                EventQueue.invokeLater(() -> {
                    die();
                });
            }
        }, LIFESPAN); // Die after 60 seconds

        // System.out.println(type + " created at position " + row + "," + col + " will die in 60 seconds");
    }

    public void die() {
        System.out.println(type + " at position " + row + "," + col + " is dying (lifespan ended)");
        gameMap.updateAnimalCount(this.getClass().getSimpleName().toLowerCase(), -1);
        // Stop all timers
        stopTimers();

        // Remove from the game
        try {
            gameMap.getGridContainer().remove(label);
            gameMap.getAnimals().remove(this);

            // Repaint to show the changes
            gameMap.getGridContainer().repaint();

            // System.out.println("Removed " + type + " from the game. Remaining animals: " + gameMap.getAnimals().size());
        } catch (Exception e) {
            System.err.println("Error removing animal: " + e.getMessage());
        }
    }

    public void reproduce() {
        // Each animal will try to reproduce after the fixed interval
        List<Animal> animals = gameMap.getAnimals();
        gameMap.updateAnimalCount(this.getClass().getSimpleName().toLowerCase(), 1);
        // Count and collect animals by type
        List<Animal> herbivores = new ArrayList<>();
        List<Animal> carnivores = new ArrayList<>();

        for (Animal animal : animals) {
            if (animal instanceof Herbivore) {
                herbivores.add(animal);
            } else if (animal instanceof Carnivore) {
                carnivores.add(animal);
            }
        }

        // For odd numbers, subtract 1 to make it even, then divide by 2
        // For example: 7 -> 6 -> 3 animals will reproduce
        int reproduceHerbivoreCount = (herbivores.size() % 2 == 0)
                ? herbivores.size() / 2
                : (herbivores.size() - 1) / 2;

        int reproduceCarnivoreCount = (carnivores.size() % 2 == 0)
                ? carnivores.size() / 2
                : (carnivores.size() - 1) / 2;

        // Check if this animal should reproduce
        if (this instanceof Herbivore) {
            int herbivoreIndex = herbivores.indexOf(this);
            // Only the first half of herbivores will reproduce
            if (herbivoreIndex >= 0 && herbivoreIndex < reproduceHerbivoreCount) {
                // System.out.println("Herbivore " + herbivoreIndex + " out of " + herbivores.size()
                //      + " is reproducing (reproduce count: " + reproduceHerbivoreCount + ")");
                generateNewAnimals("herbivore", 1);
            }
        } else if (this instanceof Carnivore) {
            int carnivoreIndex = carnivores.indexOf(this);
            // Only the first half of carnivores will reproduce
            if (carnivoreIndex >= 0 && carnivoreIndex < reproduceCarnivoreCount) {
                // System.out.println("Carnivore " + carnivoreIndex + " out of " + carnivores.size()
                //+ " is reproducing (reproduce count: " + reproduceCarnivoreCount + ")");
                generateNewAnimals("carnivore", 1);
            }
        }
    }
//animal increases 

    public void generateNewAnimals(String animalType, int count) {
        // System.out.println("Reproducing " + count + " new " + animalType + "s");

        List<Point> availablePositions = findAvailableGrassPositions();
        if (availablePositions.isEmpty()) {
            System.out.println("No available positions for new animals");
            return;
        }

        for (int i = 0; i < count && i < availablePositions.size(); i++) {
            Point pos = availablePositions.get(i);

            // Randomly select a variant (1 or 2)
            String iconKey;
            if (animalType.equals("herbivore")) {
                iconKey = (random.nextBoolean()) ? "herbivore" : "herbivore2";
            } else {
                iconKey = (random.nextBoolean()) ? "carnivore" : "carnivore2";
            }

            JLabel animalLabel = new JLabel(gameMap.getObjectIcons().get(iconKey));
            animalLabel.setSize(gameMap.getObjectIcons().get(iconKey).getIconWidth(),
                    gameMap.getObjectIcons().get(iconKey).getIconHeight());

            Animal newAnimal;
            if (animalType.equals("herbivore")) {
                newAnimal = new Herbivore(pos.x, pos.y, animalLabel, gameMap);
            } else {
                newAnimal = new Carnivore(pos.x, pos.y, animalLabel, gameMap);
            }

            gameMap.getAnimals().add(newAnimal);
            gameMap.getGridContainer().add(animalLabel);
            animalLabel.setLocation(newAnimal.getXPos(), newAnimal.getYPos());

            // System.out.println("Created new " + animalType + " at position " + pos.x + "," + pos.y);
        }
    }

    public List<Point> findAvailableGrassPositions() {
        List<Point> availablePositions = new ArrayList<>();
        char[][] terrainMatrix = gameMap.getTerrainMatrix();

        // Find available grass positions that are not occupied by other animals
        for (int i = 0; i < terrainMatrix.length; i++) {
            for (int j = 0; j < terrainMatrix[i].length; j++) {
                if (terrainMatrix[i][j] == 'G' && !isPositionOccupied(i, j)) {
                    availablePositions.add(new Point(i, j));
                }
            }
        }

        // Shuffle the positions for randomness
        java.util.Collections.shuffle(availablePositions, random);
        return availablePositions;
    }

    public boolean isPositionOccupied(int row, int col) {
        // Check if any animal is at this position
        for (Animal animal : gameMap.getAnimals()) {
            if (animal.getRow() == row && animal.getCol() == col) {
                return true;
            }
        }

        // Check for ponds and trees (from GameMap's grid layers)
        JLayeredPane[][] gridLayers = gameMap.getGridLayers();
        JLayeredPane tilePane = gridLayers[row][col];

        for (Component comp : tilePane.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (label.getIcon() == gameMap.getObjectIcons().get("pond")
                        || label.getIcon() == gameMap.getObjectIcons().get("tree")) {
                    return true;
                }
            }
        }

        return false;
    }

    public void move() {
        // Adjust movement speed
        int adjustedPixels = (int) (PIXELS_PER_MOVE * speedMultiplier);

        // Check if we've reached a pond
        if (checkAtPond()) {
            if (!atPond) {
                atPond = true;
                Timer pondTimer = new Timer(true); // Use daemon timer
                pondTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        EventQueue.invokeLater(() -> leavePond());
                    }
                }, POND_WAIT_TIME);
            }
            return;
        }
        Animal nearestSameSpecies = findNearestSameSpecies();
        if (nearestSameSpecies != null) {
            double distance = Math.hypot(this.xPos - nearestSameSpecies.getXPos(), this.yPos - nearestSameSpecies.getYPos());

            if (distance < 300) { // Only group if close enough (adjust distance as needed)
                moveTowardsGroup(nearestSameSpecies);
            }
        }

        // Calculate movement direction
        int dx = 0, dy = 0;
        switch (direction) {
            case 1:
                dx = adjustedPixels;
                break;                // Right
            case 2:
                dx = adjustedPixels;
                dy = adjustedPixels;
                break;  // Down-Right
            case 3:
                dy = adjustedPixels;
                break;                // Down
            case 4:
                dx = -adjustedPixels;
                dy = adjustedPixels;
                break; // Down-Left
            case 5:
                dx = -adjustedPixels;
                break;               // Left
            case 6:
                dx = -adjustedPixels;
                dy = -adjustedPixels;
                break; // Up-Left
            case 7:
                dy = -adjustedPixels;
                break;               // Up
            case 8:
                dx = adjustedPixels;
                dy = -adjustedPixels;
                break; // Up-Right
        }

        // Calculate new position
        int newX = xPos + dx;
        int newY = yPos + dy;
        int newCol = newX / gameMap.getTileSize();
        int newRow = newY / gameMap.getTileSize();

        // Check boundaries and terrain
        char[][] terrainMatrix = gameMap.getTerrainMatrix();
        int rows = terrainMatrix.length;
        int cols = terrainMatrix[0].length;

        if (newRow < 0 || newRow >= rows || newCol < 0 || newCol >= cols
                || terrainMatrix[newRow][newCol] != 'G') {
            // Reverse direction if hitting boundary or non-grass terrain
            direction = getReverseDirection(direction);
            tilesCrossed = 0; // Reset tiles counter when changing direction
            return;
        }

        // Small chance to randomly change direction
        if (random.nextInt(100) < 3) { // 3% chance to change direction
            direction = random.nextInt(8) + 1; // Random direction between 1 and 8
            tilesCrossed = 0; // Reset tiles counter when changing direction
        }

        // Update position
        xPos = newX;
        yPos = newY;
        row = newRow;
        col = newCol;

        // Check if we've crossed into a new tile
        if (row != lastTileRow || col != lastTileCol) {
            // We've entered a new tile
            tilesCrossed++;

            // Check if we need to change direction after DIRECTION_CHANGE_TILES
            if (tilesCrossed >= DIRECTION_CHANGE_TILES) {
                direction = random.nextInt(8) + 1;
                tilesCrossed = 0; // Reset tiles counter
            }

            // Update last tile position
            lastTileRow = row;
            lastTileCol = col;
        }

        // Update visual position of the animal
        label.setLocation(xPos, yPos);
        if (label.getParent() != null && label.getParent() instanceof JLayeredPane) {
            JLayeredPane layeredPane = (JLayeredPane) label.getParent();
            layeredPane.moveToFront(label);
        }

        // Call abstract method for specific animal behavior
        performSpecificBehavior();
    }

    // Abstract method to be implemented by subclasses
    protected abstract void performSpecificBehavior();

    // Clear method to handle leaving the pond
    public void leavePond() {
        // System.out.println(type + " is leaving the pond at position " + row + "," + col);
        atPond = false;
        direction = random.nextInt(8) + 1; // Choose a new random direction
        tilesCrossed = 0; // Reset tiles counter

        // Force an immediate move to get out of the pond area
        int attempts = 0;
        boolean stillAtPond = true;

        while (stillAtPond && attempts < 8) {
            // Try to move in the current direction
            int dx = 0, dy = 0;
            switch (direction) {
                case 1:
                    dx = PIXELS_PER_MOVE;
                    break;
                case 2:
                    dx = PIXELS_PER_MOVE;
                    dy = PIXELS_PER_MOVE;
                    break;
                case 3:
                    dy = PIXELS_PER_MOVE;
                    break;
                case 4:
                    dx = -PIXELS_PER_MOVE;
                    dy = PIXELS_PER_MOVE;
                    break;
                case 5:
                    dx = -PIXELS_PER_MOVE;
                    break;
                case 6:
                    dx = -PIXELS_PER_MOVE;
                    dy = -PIXELS_PER_MOVE;
                    break;
                case 7:
                    dy = -PIXELS_PER_MOVE;
                    break;
                case 8:
                    dx = PIXELS_PER_MOVE;
                    dy = -PIXELS_PER_MOVE;
                    break;
            }

            // Try a larger move to get out of pond
            int newX = xPos + (dx * 3);
            int newY = yPos + (dy * 3);
            int newCol = newX / gameMap.getTileSize();
            int newRow = newY / gameMap.getTileSize();

            // Check if new position is valid and not at pond
            char[][] terrainMatrix = gameMap.getTerrainMatrix();
            int rows = terrainMatrix.length;
            int cols = terrainMatrix[0].length;

            if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols
                    && terrainMatrix[newRow][newCol] == 'G') {

                // Update position
                xPos = newX;
                yPos = newY;
                row = newRow;
                col = newCol;

                // Update visual position
                label.setLocation(xPos, yPos);
                gameMap.getGridContainer().moveToFront(label);

                // Check if we're still at pond
                stillAtPond = checkAtPond();
                if (!stillAtPond) {
                    // System.out.println(type + " successfully moved away from pond to " + row + "," + col);
                    break;
                }
            }

            // Try a different direction
            direction = (direction % 8) + 1;
            attempts++;
        }

        // If still at pond after all attempts, force a position change
        if (stillAtPond) {
            // Find a random grass position
            char[][] terrainMatrix = gameMap.getTerrainMatrix();
            int rows = terrainMatrix.length;
            int cols = terrainMatrix[0].length;

            for (int attempt = 0; attempt < 20; attempt++) {
                int randomRow = random.nextInt(rows);
                int randomCol = random.nextInt(cols);

                if (terrainMatrix[randomRow][randomCol] == 'G') {
                    // Move to this position
                    row = randomRow;
                    col = randomCol;
                    xPos = col * gameMap.getTileSize();
                    yPos = row * gameMap.getTileSize();

                    // Update visual position
                    label.setLocation(xPos, yPos);
                    gameMap.getGridContainer().moveToFront(label);

                    // System.out.println(type + " was teleported away from pond to " + row + "," + col);
                    break;
                }
            }
        }
    }

    public int getReverseDirection(int direction) {
        switch (direction) {
            case 1:
                return 5; // Right -> Left
            case 2:
                return 6; // Down-Right -> Up-Left
            case 3:
                return 7; // Down -> Up
            case 4:
                return 8; // Down-Left -> Up-Right
            case 5:
                return 1; // Left -> Right
            case 6:
                return 2; // Up-Left -> Down-Right
            case 7:
                return 3; // Up -> Down
            case 8:
                return 4; // Up-Right -> Down-Left
            default:
                return direction;
        }
    }

    public boolean checkAtPond() {
        JLayeredPane[][] gridLayers = gameMap.getGridLayers();
        JLayeredPane tile = gridLayers[row][col];
        for (Component comp : tile.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (label.getIcon() == gameMap.getObjectIcons().get("pond")) {
                    return true;
                }
            }
        }
        return false;
    }

    public void stopTimers() {
        if (moveTimer != null) {
            moveTimer.cancel();
            moveTimer.purge();
            moveTimer = null;
        }

        if (reproductionTimer != null) {
            reproductionTimer.cancel();
            reproductionTimer.purge();
            reproductionTimer = null;
        }

        if (lifespanTimer != null) {
            lifespanTimer.cancel();
            lifespanTimer.purge();
            lifespanTimer = null;
        }
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getDirection() {
        return direction;
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

    public String getType() {
        return type;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void setXPos(int xPos) {
        this.xPos = xPos;
    }

    public void setYPos(int yPos) {
        this.yPos = yPos;
    }

    public boolean isAtPond() {
        return atPond;
    }

    public void setAtPond(boolean atPond) {
        this.atPond = atPond;
    }
    // Make sure this exists in Animal class

    public void setSpeedMultiplier(float multiplier) {
        this.speedMultiplier = multiplier;
        stopTimers();
        startMovement(); // Restart with new speed
    }

    public Animal findNearestSameSpecies() {
        Animal nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Animal animal : gameMap.getAnimals()) {
            if (animal != this && animal.getType().equals(this.type)) {
                double distance = Math.hypot(this.xPos - animal.getXPos(), this.yPos - animal.getYPos());
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = animal;
                }
            }
        }
        return nearest;
    }

    // Minimum distance to stay grouped without overlapping
    public void moveTowardsGroup(Animal target) {
        if (target == null) {
            return;
        }

        int dx = target.getXPos() - this.xPos;
        int dy = target.getYPos() - this.yPos;

        double distance = Math.hypot(dx, dy);

        if (distance < GROUPING_DISTANCE) {
            // Already close enough, no need to move towards it
            return;
        }

        // Move towards target, choosing best direction (including diagonals)
        if (Math.abs(dx) > Math.abs(dy)) {
            direction = (dx > 0) ? 1 : 5; // Right or Left
        } else if (Math.abs(dy) > Math.abs(dx)) {
            direction = (dy > 0) ? 3 : 7; // Down or Up
        } else {
            // If both dx and dy are similar, move diagonally
            if (dx > 0 && dy > 0) {
                direction = 2; // Down-Right
            } else if (dx < 0 && dy > 0) {
                direction = 4; // Down-Left
            } else if (dx < 0 && dy < 0) {
                direction = 6; // Up-Left
            } else if (dx > 0 && dy < 0) {
                direction = 8; // Up-Right
            }
        }
    }
    
}

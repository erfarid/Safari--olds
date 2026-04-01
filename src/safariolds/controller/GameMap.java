package safariolds.controller;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;
import safariolds.model.*;
import safariolds.view.*;

public class GameMap {

    public JLabel carnivoresLabel;
    private JFrame frame;
    public boolean testMode = false;
    private JLabel timerLabel; // Label to display the timer
    private Timer gameTimer; // Swing timer for updating the game time
    private static boolean HEADLESS_MODE = false;
    private int elapsedTime = 0;
    private GameState gameState;
    public JLabel herbivoresLabel;
    public JLabel plantsLabel;
    public JLabel pondsLabel;
    public int carnivoreCount = 0;
    public int herbivoreCount = 0;
    public int plantCount = 0;
    public int pondCount = 0;
    public List<Ranger> rangers = new ArrayList<>();
    public Map<String, Integer> inventory = new HashMap<>();
    public String playerName;
    public int money;
    public JLabel moneyLabel;
    private JPanel mainPanel = new JPanel();
    public Map<Point, String> placedItems = new HashMap<>(); // Tracks placed items by position
    public String selectedItemType = null;

    // Increased grid size to create a more rectangular and detailed map
    public final int ROWS = 20;
    public final int COLS = 40;
    public JLayeredPane[][] gridLayers;
    public char[][] terrainMatrix;
    public HashMap<Character, ImageIcon> terrainIcons;
    public HashMap<String, ImageIcon> objectIcons;
    private Random random = new Random();
    public int tileSize;
    private JLayeredPane gridContainer = new JLayeredPane();

    // Road-related variables
    private boolean isDragging = false;
    private boolean isBuilding = false;
    private Point lastHoveredTile = null;
    public JLabel roadsLeftLabel;
    public static final int START_ROW = 10;
    public static final int START_COL = 0;
    public static final int END_ROW = 10;
    public static final int END_COL = 40 - 1;
    public GameMode currentMode = GameMode.DAY; // default
    private boolean nightMode = false;
    private boolean chipActive = false;
    private Animal chipTarget = null;
    private Map<Animal, Boolean> chippedAnimals = new HashMap<>();
    private Map<Animal, Integer> animalClickCounts = new HashMap<>();

    // Movement configuration
    private static final int MOVE_DELAY = 30;
    private static final int PIXELS_PER_MOVE = 5;
    private static final int ANIMAL_MOVE_DELAY = 30;

    private java.util.Timer movementTimer;
    public List<Jeep> jeeps = new ArrayList<>();
    private HashMap<String, Point> ponds = new HashMap<>();
    public List<Animal> animals = new ArrayList<>();
    private Point lastProcessedTile = null;
    private Map<String, List<Animal>> animalGroups = new HashMap<>();

    private BufferedImage offscreenBuffer;
    private boolean bufferValid = false;
    // MiniMap
    private JScrollPane scrollPane = new JScrollPane();
    private int visibleWidth;
    private int visibleHeight;
    public Point viewportPosition = new Point(0, 0);

    public List<Poacher> poachers = new ArrayList<>();

    public JFrame getFrame() {
        return frame;
    }

    // In GameMap.java
    public void moveObjectBetweenTiles(JLabel object, int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow < 0 || fromRow >= ROWS || fromCol < 0 || fromCol >= COLS
                || toRow < 0 || toRow >= ROWS || toCol < 0 || toCol >= COLS) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                // Remove from source tile
                if (gridLayers[fromRow][fromCol].getComponents().length > 0) {
                    gridLayers[fromRow][fromCol].remove(object);
                    gridLayers[fromRow][fromCol].revalidate();
                }

                // Add to destination tile
                gridLayers[toRow][toCol].add(object, JLayeredPane.PALETTE_LAYER);
                gridLayers[toRow][toCol].moveToFront(object);
                object.setLocation(toCol * tileSize, toRow * tileSize);

                gridContainer.revalidate();
            } catch (Exception e) {
                System.err.println("Error moving object between tiles: " + e.getMessage());
            }
        });
    }

    public void initializeGame() {
        calculateTileSize();
        initTerrainMatrix();
        initTerrainImages();
        initObjectImages();

        if (!testMode) {
            initComponents();
            setupRightClickPlacement();
            placeRandomObjects();
            startMovementTimers();
        }
    }

    public void removeObjectFromTile(JLabel object, int row, int col) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                JLayeredPane tile = gridLayers[row][col];
                if (tile != null && object != null && object.getParent() == tile) {
                    tile.remove(object);
                    tile.revalidate();
                }
            } catch (Exception e) {
                System.err.println("Error removing object from tile: " + e.getMessage());
            }
        });
    }

    public void spawnPoachersNearJeep(Jeep jeep) {
        if (poachers.size() >= 3) {
            return;
        }

        if (random.nextInt(100) < 20) {
            int jeepRow = jeep.getRow();
            int jeepCol = jeep.getCol();

            List<Point> possibleSpawns = new ArrayList<>();
            for (int r = Math.max(0, jeepRow - 4); r <= Math.min(ROWS - 1, jeepRow + 4); r++) {
                for (int c = Math.max(0, jeepCol - 4); c <= Math.min(COLS - 1, jeepCol + 4); c++) {
                    // Only spawn on empty green tiles that are 2-3 tiles from roads
                    if (terrainMatrix[r][c] == 'G'
                            && isTileAtValidDistanceFromRoad(r, c)
                            && isTileEmpty(r, c)
                            && !(r == jeepRow && c == jeepCol)) {
                        possibleSpawns.add(new Point(r, c));
                    }
                }
            }

            if (!possibleSpawns.isEmpty()) {
                Point spawnPoint = possibleSpawns.get(random.nextInt(possibleSpawns.size()));
                SwingUtilities.invokeLater(() -> {
                    try {
                        Poacher poacher = new Poacher(spawnPoint.x, spawnPoint.y, this);
                        poachers.add(poacher);
                        gridContainer.revalidate();
                    } catch (Exception e) {
                        System.err.println("Error spawning poacher: " + e.getMessage());
                    }
                });
            }
        }
    }

    private boolean isTileAtValidDistanceFromRoad(int row, int col) {
        // Check if tile is 2-3 tiles away from any road
        for (int distance = 2; distance <= 3; distance++) {
            if (isTileExactlyDistanceFromRoad(row, col, distance)) {
                return true;
            }
        }
        return false;
    }

    public void incrementPondCount() {
        pondCount++;
        updateCounterLabels();
    }

    public void decrementPondCount() {
        pondCount--;
        updateCounterLabels();
    }

    public void activateChip() {
        this.chipActive = true;
        this.chipTarget = null;
        JOptionPane.showMessageDialog(frame, "You bought a chip. Right-click an animal twice in Night Mode to tag it.");
    }

    public boolean isTileExactlyDistanceFromRoad(int row, int col, int distance) {
        // Check all tiles at specified Manhattan distance
        for (int r = row - distance; r <= row + distance; r++) {
            for (int c = col - distance; c <= col + distance; c++) {
                if (Math.abs(row - r) + Math.abs(col - c) == distance) {
                    if (r >= 0 && r < ROWS && c >= 0 && c < COLS
                            && terrainMatrix[r][c] == 'R') {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void removePoacher(Poacher poacher) {
        poacher.remove();
        poachers.remove(poacher);
    }

    public GameMap(String playerName, int money) {
        this(playerName, money, false); // default to non-test mode
    }

    public GameMap(String playerName, int money, boolean testMode) {
        this.testMode = testMode;
        this.playerName = playerName;
        this.money = (money != 0) ? money : 10000;
        this.gameState = new GameState(this);

        this.inventory = new HashMap<>();
        this.placedItems = new HashMap<>();
        this.animals = new ArrayList<>();
        this.jeeps = new ArrayList<>();
        this.poachers = new ArrayList<>();
        this.rangers = new ArrayList<>();
        this.animalGroups = new HashMap<>();

        inventory.put("herbivore", 0);
        inventory.put("carnivore", 0);
        inventory.put("tree", 0);
        inventory.put("pond", 0);

        if (testMode) {
            // Initialize grid with empty panes
            this.gridLayers = new JLayeredPane[ROWS][COLS];
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++) {
                    gridLayers[i][j] = new JLayeredPane();
                }
            }

            // Initialize terrain matrix with grass
            this.terrainMatrix = new char[ROWS][COLS];
            for (int i = 0; i < ROWS; i++) {
                Arrays.fill(terrainMatrix[i], 'G');
            }

            // Set start/end positions
            terrainMatrix[START_ROW][START_COL] = 'S';
            terrainMatrix[END_ROW][END_COL] = 'E';

            return;
        }

        // GUI initialization (only in non-test mode)
        frame = new JFrame("Game Map");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(true);

        timerLabel = new JLabel("Time: 00:00");
        timerLabel.setFont(new Font("Algerian", Font.BOLD, 24));
        timerLabel.setForeground(Color.WHITE);

        JPanel topPanel = createTopPanel();
        topPanel.add(timerLabel, BorderLayout.EAST);

        initGameTimer();
        calculateTileSize();
        initTerrainMatrix();
        initTerrainImages();
        initObjectImages();
        initComponents();
        setupRightClickPlacement();
        placeRandomObjects();
        startMovementTimers();

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopMovementTimers();
            }
        });

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }

    private void initializeHeadlessMode() {
        // Initialize only non-GUI dependent components
        calculateTileSize();
        initTerrainMatrix();
        initTerrainImages();
        initObjectImages();
        placeRandomObjects();
        initializeCounts();
    }

    private void initializeGuiMode() {
        frame = new JFrame("Game Map");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(true);

        calculateTileSize();
        initTerrainMatrix();
        initTerrainImages();
        initObjectImages();
        initComponents();
        setupRightClickPlacement();
        placeRandomObjects();
        startMovementTimers();

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopMovementTimers();
            }
        });

        frame.setContentPane(mainPanel); // This line connects your layout to the frame
        frame.setVisible(true);
    }

    public void initGameTimer() {
        // Create a Swing timer with a default delay of 1000ms (1 second)
        gameTimer = new Timer(1000, e -> updateTimer());
        gameTimer.start();
    }

    private void updateTimer() {
        SwingUtilities.invokeLater(() -> {
            elapsedTime++;
            int minutes = elapsedTime / 60;
            int seconds = elapsedTime % 60;
            timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));

            // Trigger ranger actions periodically
            if (elapsedTime % 5 == 0) { // Example: every 5 seconds
                for (Ranger ranger : rangers) {
                    ranger.moveRandomly();
                    ranger.protectAgainstPoachers();
                }
            }

            // Pay salaries at the start of each month
            if (elapsedTime % 1800 == 0) { // Example: every 30 minutes
                payRangerSalaries();
            }
        });
    }

    private void startMovementTimers() {
        // Animal movement timers are started from the Animal class constructor
    }

    public Map<String, List<Animal>> getAnimalGroups() {
        return animalGroups;
    }

    public int getMoney() {
        return money;
    }
    // Call this when buying from shop

    public void buyItem(String itemType, int quantity) {
        int current = inventory.getOrDefault(itemType, 0);
        inventory.put(itemType, current + quantity);

        // Update the specific count variable
        switch (itemType.toLowerCase()) {
            case "herbivore":
                herbivoreCount += quantity;
                break;
            case "carnivore":
                carnivoreCount += quantity;
                break;
            case "tree":
                plantCount += quantity;
                break;
            case "pond":
                pondCount += quantity;
                break;
        }
        updateCounterLabels();
    }

    public boolean sellItem(String itemType, int quantity) {
        int removed = 0;
        itemType = itemType.toLowerCase();

        // For animals
        if (itemType.equals("herbivore") || itemType.equals("carnivore")) {
            Iterator<Animal> iterator = animals.iterator();
            while (iterator.hasNext() && removed < quantity) {
                Animal animal = iterator.next();
                if ((itemType.equals("herbivore") && animal instanceof Herbivore)
                        || (itemType.equals("carnivore") && animal instanceof Carnivore)) {

                    // Remove visual representation
                    removeObjectFromTile(animal.getRow(), animal.getCol());
                    iterator.remove();
                    removed++;
                }
            }
        } // For trees and ponds
        else {
            List<Point> toRemove = new ArrayList<>();
            for (Point pos : placedItems.keySet()) {
                if (placedItems.get(pos).equalsIgnoreCase(itemType) && removed < quantity) {
                    toRemove.add(pos);
                    removed++;
                }
            }
            for (Point pos : toRemove) {
                removeObjectFromTile(pos.x, pos.y);
                placedItems.remove(pos);
            }
        }

        // Update counts
        if (removed > 0) {
            switch (itemType) {
                case "herbivore":
                    herbivoreCount -= removed;
                    break;
                case "carnivore":
                    carnivoreCount -= removed;
                    break;
                case "tree":
                    plantCount -= removed;
                    break;
                case "pond":
                    pondCount -= removed;
                    break;
            }
            updateCounterLabels();
            return true;
        }
        return false;
    }

    private void stopMovementTimers() {
        if (movementTimer != null) {
            movementTimer.cancel();
            movementTimer.purge();
        }
        for (Animal animal : animals) {
            animal.stopTimers();
        }
    }

    public void calculateTileSize() {
        if (testMode) {
            tileSize = 32;
            return;
        }

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int maxWidth = (int) (screenSize.width * 1.25) / COLS;
        int maxHeight = (int) (screenSize.height * 1.05) / ROWS;
        tileSize = Math.min(maxWidth, maxHeight);
    }

    public void initTerrainMatrix() {
        terrainMatrix = new char[ROWS][COLS];

        // Fill with grass by default
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                terrainMatrix[i][j] = 'G';
            }
        }

        // Mark start and end positions as special
        terrainMatrix[START_ROW][START_COL] = 'S';
        terrainMatrix[END_ROW][END_COL] = 'E';
    }

    protected void initTerrainImages() {
        terrainIcons = new HashMap<>();
        try {
            ImageIcon greenLand = new ImageIcon(getClass().getResource("/safariolds/view/assets/green_land.jpeg"));
            terrainIcons.put('G',
                    new ImageIcon(greenLand.getImage().getScaledInstance(tileSize, tileSize, Image.SCALE_SMOOTH)));
        } catch (Exception e) {
            System.err.println("Error loading terrain images: " + e.getMessage());
            terrainIcons.put('G', createColorIcon(Color.GREEN, tileSize, tileSize));
        }
    }

    protected ImageIcon createColorIcon(Color color, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        return new ImageIcon(img);
    }

    protected void initObjectImages() {
        objectIcons = new HashMap<>();
        try {
            int objectSize = (int) (tileSize / 1.5);
            int roadSize = tileSize;

            objectIcons.put("pond", scaleImage(
                    new ImageIcon(getClass().getResource("/safariolds/view/assets/Pond.png")), objectSize, objectSize));
            objectIcons.put("herbivore",
                    scaleImage(new ImageIcon(getClass().getResource("/safariolds/view/assets/herbivore.png")),
                            objectSize, objectSize));
            objectIcons.put("herbivore2",
                    scaleImage(new ImageIcon(getClass().getResource("/safariolds/view/assets/herbi-1.png")), objectSize,
                            objectSize));
            objectIcons.put("carnivore",
                    scaleImage(new ImageIcon(getClass().getResource("/safariolds/view/assets/carni.png")), objectSize,
                            objectSize));
            objectIcons.put("carnivore2", scaleImage(
                    new ImageIcon(getClass().getResource("/safariolds/view/assets/beer.png")), objectSize, objectSize));
            objectIcons.put("jeep", scaleImage(
                    new ImageIcon(getClass().getResource("/safariolds/view/assets/jeep.png")), objectSize, objectSize));
            objectIcons.put("tree", scaleImage(
                    new ImageIcon(getClass().getResource("/safariolds/view/assets/tree.png")), objectSize, objectSize));
            objectIcons.put("road", scaleImage(
                    new ImageIcon(getClass().getResource("/safariolds/view/assets/Roads.jpg")), roadSize, roadSize));
            objectIcons.put("poacher",
                    scaleImage(new ImageIcon(getClass().getResource("/safariolds/view/assets/pocher.png")), objectSize,
                            objectSize));
            objectIcons.put("ranger",
                    scaleImage(new ImageIcon(getClass().getResource("/safariolds/view/assets/ranger.png")), objectSize,
                            objectSize));
        } catch (Exception e) {
            System.err.println("Error loading object images: " + e.getMessage());
            int objectSize = (int) (tileSize / 1.5);
            objectIcons.put("herbivore", createColorIcon(Color.RED, objectSize, objectSize));
            objectIcons.put("jeep", createColorIcon(Color.BLUE, objectSize, objectSize));
            objectIcons.put("tree", createColorIcon(Color.GREEN.darker(), objectSize, objectSize));
            objectIcons.put("carni", createColorIcon(Color.ORANGE, objectSize, objectSize));
            objectIcons.put("pond", createColorIcon(Color.BLUE, objectSize, objectSize));
            objectIcons.put("road", createColorIcon(Color.GRAY, tileSize, tileSize));
            objectIcons.put("poacher", createColorIcon(Color.RED, tileSize, tileSize));
            objectIcons.put("ranger",
                    scaleImage(new ImageIcon(getClass().getResource("/safariolds/view/assets/ranger.png")), objectSize,
                            objectSize));
        }
    }

    private ImageIcon scaleImage(ImageIcon icon, int width, int height) {
        return new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }

    protected void placeRandomObjects() {
        List<Point> greenTiles = new ArrayList<>();
        ponds.clear();
        animals.clear();
        jeeps.clear();

        Set<Point> prohibitedTiles = new HashSet<>();

        for (int i = Math.max(0, START_ROW - 2); i <= Math.min(ROWS - 1, START_ROW + 2); i++) {
            for (int j = Math.max(0, START_COL - 2); j <= Math.min(COLS - 1, START_COL + 2); j++) {
                prohibitedTiles.add(new Point(i, j));
            }
        }

        // Prohibit tiles around E (END_ROW, END_COL)
        for (int i = Math.max(0, END_ROW - 2); i <= Math.min(ROWS - 1, END_ROW + 2); i++) {
            for (int j = Math.max(0, END_COL - 2); j <= Math.min(COLS - 1, END_COL + 2); j++) {
                prohibitedTiles.add(new Point(i, j));
            }
        }

        // Collect all available grass tiles
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                Point current = new Point(i, j);

                // Skip prohibited tiles, start, and end positions
                if (prohibitedTiles.contains(current)
                        || (i == START_ROW && j == START_COL)
                        || (i == END_ROW && j == END_COL)) {
                    continue;
                }

                if (terrainMatrix[i][j] == 'G') {
                    greenTiles.add(current);
                }
            }
        }

        Collections.shuffle(greenTiles);

        placeSpecificAnimals(greenTiles);

        // Place ponds and trees
        int numPonds = 75;
        int numTrees = 120;
        int pondsPlaced = 0;
        int treesPlaced = 0;
        int index = 0;

        // Place ponds
        while (pondsPlaced < numPonds && index < greenTiles.size()) {
            Point pos = greenTiles.get(index++);
            if (terrainMatrix[pos.x][pos.y] != 'G') {
                continue;
            }

            addObjectToTile(pos.x, pos.y, "pond");
            ponds.put(pos.x + "," + pos.y, pos);
            pondsPlaced++;
        }

        // Place trees
        for (int i = index; i < greenTiles.size() && treesPlaced < numTrees; i++) {
            Point pos = greenTiles.get(i);
            if (terrainMatrix[pos.x][pos.y] != 'G') {
                continue;
            }

            addObjectToTile(pos.x, pos.y, "tree");
            treesPlaced++;
        }

        System.out.println("Total ponds placed: " + pondsPlaced);
        System.out.println("Total trees placed: " + treesPlaced);
        System.out.println("Total objects placed: " + (pondsPlaced + treesPlaced));
        initializeCounts(); // Add this line
    }

    public void incrementAnimalCount(Animal animal) {
        if (animal instanceof Carnivore) {
            carnivoreCount++;
        } else if (animal instanceof Herbivore) {
            herbivoreCount++;
        }
        updateCounterLabels();
    }

    public void decrementAnimalCount(Animal animal) {
        if (animal instanceof Carnivore) {
            carnivoreCount--;
        } else if (animal instanceof Herbivore) {
            herbivoreCount--;
        }
        updateCounterLabels();
    }

    public void incrementPlantCount() {
        plantCount++;
        updateCounterLabels();
    }

    public void decrementPlantCount() {
        plantCount--;
        updateCounterLabels();
    }

    public void placeSpecificAnimals(List<Point> greenTiles) {
        // Define starting zone rows/cols for each species group
        Point herbivoreZone = new Point(4, 4);
        Point herbivore2Zone = new Point(4, 30);
        Point carnivoreZone = new Point(15, 4);
        Point carnivore2Zone = new Point(15, 30);

        placeGroupedAnimals("herbivore", 6, herbivoreZone, greenTiles);
        placeGroupedAnimals("herbivore2", 6, herbivore2Zone, greenTiles);
        placeGroupedAnimals("carnivore", 6, carnivoreZone, greenTiles);
        placeGroupedAnimals("carnivore2", 6, carnivore2Zone, greenTiles);
    }

    public boolean isTileEmpty(int row, int col) {
        // Check if tile has any objects (excluding terrain background)
        JLayeredPane tilePane = gridLayers[row][col];
        Component[] components = tilePane.getComponentsInLayer(JLayeredPane.PALETTE_LAYER);
        return components.length == 0; // Empty if no objects in PALETTE_LAYER
    }

    public void placeGroupedAnimals(String type, int count, Point center, List<Point> greenTiles) {
        int placed = 0;
        for (Point tile : new ArrayList<>(greenTiles)) {
            // Check if this tile is within a 5x5 area of the group's center
            if (Math.abs(tile.x - center.x) <= 2 && Math.abs(tile.y - center.y) <= 2) {
                JLabel animalLabel = new JLabel(objectIcons.get(type));
                animalLabel.setSize(objectIcons.get(type).getIconWidth(), objectIcons.get(type).getIconHeight());

                Animal animal;
                if (type.contains("herbivore")) {
                    animal = new Herbivore(tile.x, tile.y, animalLabel, this);
                } else {
                    animal = new Carnivore(tile.x, tile.y, animalLabel, this);
                }

                animals.add(animal);
                animalGroups.computeIfAbsent(type, k -> new ArrayList<>()).add(animal);
                gridContainer.add(animalLabel);
                animalLabel.setLocation(animal.getXPos(), animal.getYPos());
                greenTiles.remove(tile);

                placed++;
                if (placed >= count) {
                    break;
                }
            }
        }
    }

    public JLabel addObjectToTile(int row, int col, String objectType) {
        JLayeredPane tilePane = gridLayers[row][col];

        // Don't add if tile already has objects (except for poachers which are handled
        // separately)
        if (!objectType.equals("poacher") && !isTileEmpty(row, col)) {
            return null;
        }

        JLabel objectLabel = new JLabel(objectIcons.get(objectType));
        objectLabel.setSize(objectIcons.get(objectType).getIconWidth(),
                objectIcons.get(objectType).getIconHeight());

        int xPos = (tileSize - objectLabel.getWidth()) / 2;
        int yPos = (tileSize - objectLabel.getHeight()) / 2;
        objectLabel.setLocation(xPos, yPos);

        tilePane.add(objectLabel, JLayeredPane.PALETTE_LAYER);
        tilePane.moveToFront(objectLabel);

        return objectLabel;
    }

    protected void initComponents() {
        gridContainer.setDoubleBuffered(true);
        scrollPane.setDoubleBuffered(true);
        mainPanel.setDoubleBuffered(true);
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(139, 69, 19));

        JPanel topPanel = createTopPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);

        JPanel mapPanel = createMapPanel();

        // Calculate visible area based on screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        visibleWidth = (int) (screenSize.width * 0.6);
        visibleHeight = (int) (screenSize.height * 0.6);

        // Create scroll pane
        scrollPane = new JScrollPane(mapPanel);
        scrollPane.setPreferredSize(new Dimension(visibleWidth, visibleHeight));

        // Add these performance optimizations:
        scrollPane.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
        scrollPane.setDoubleBuffered(true);

        // Create minimap
        MiniMap minimap = new MiniMap(this);

        scrollPane.getViewport().addChangeListener(e -> {
            refreshView();
            Point viewPos = scrollPane.getViewport().getViewPosition();
            Dimension viewSize = scrollPane.getViewport().getExtentSize();
            minimap.updateViewport(viewPos.x / tileSize, viewPos.y / tileSize,
                    viewSize.width / tileSize, viewSize.height / tileSize);
        });

        // Add components to main panel
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Combine timer and minimap in the bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(102, 51, 0));

        // Timer panel (left side of the bottom panel)
        JPanel timerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        timerPanel.setOpaque(false);

        timerLabel = new JLabel("Time: 00:00");
        timerLabel.setFont(new Font("Algerian", Font.BOLD, 24));
        timerLabel.setForeground(Color.WHITE);
        timerPanel.add(timerLabel);

        bottomPanel.add(timerPanel, BorderLayout.WEST);

        // Create control panel for the ranger button
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton rangerButton = new JButton("Place Ranger");
        rangerButton.addActionListener(e -> {
            try {
                // Prompt the user for the row and column
                int row = Integer.parseInt(JOptionPane.showInputDialog("Enter row:"));
                int col = Integer.parseInt(JOptionPane.showInputDialog("Enter column:"));

                // Call the spawnRanger method
                spawnRanger(row, col);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid input! Please enter numeric values for row and column.");
            }
        });

        // Add the button to the control panel
        controlPanel.add(rangerButton);

        // Add the control panel to the center of the bottom panel
        bottomPanel.add(controlPanel, BorderLayout.CENTER);

        // Add minimap to the right side of the bottom panel
        bottomPanel.add(minimap, BorderLayout.EAST);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        frame.setContentPane(mainPanel);

    }

    public void scrollTo(int x, int y) {
        viewportPosition.setLocation(x, y);
        scrollPane.getViewport().setViewPosition(viewportPosition);
    }

    public void scrollBy(int dx, int dy) {
        Point current = scrollPane.getViewport().getViewPosition();
        scrollTo(current.x + dx, current.y + dy);
    }

    public void markStartAndEndTiles() {
        // Start tile - allow roads but mark with 'S'
        JLayeredPane startTile = gridLayers[START_ROW][START_COL];
        JLabel startLabel = new JLabel("S");
        startLabel.setFont(new Font("Arial", Font.BOLD, 24));
        startLabel.setForeground(Color.GREEN);
        startLabel.setBounds(tileSize / 2 - 10, tileSize / 2 - 15, 20, 30);
        startTile.add(startLabel, JLayeredPane.MODAL_LAYER);

        // End tile - allow roads but mark with 'E'
        JLayeredPane endTile = gridLayers[END_ROW][END_COL];
        JLabel endLabel = new JLabel("E");
        endLabel.setFont(new Font("Arial", Font.BOLD, 24));
        endLabel.setForeground(Color.RED);
        endLabel.setBounds(tileSize / 2 - 10, tileSize / 2 - 15, 20, 30);
        endTile.add(endLabel, JLayeredPane.MODAL_LAYER);
    }

    protected JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(102, 51, 0));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        topPanel.setPreferredSize(new Dimension(screenSize.width, 80));

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        infoPanel.setOpaque(false);

        JLabel playerLabel = new JLabel("Player: " + playerName);
        playerLabel.setFont(new Font("Algerian", Font.BOLD, 24));
        playerLabel.setForeground(Color.WHITE);

        moneyLabel = new JLabel("Money: €" + money);
        moneyLabel.setFont(new Font("Algerian", Font.BOLD, 24));
        moneyLabel.setForeground(Color.WHITE);

        roadsLeftLabel = new JLabel("Roads Left: " + (Road.getTotalRoadsPurchased() - Road.getRoadsUsed()));
        roadsLeftLabel.setFont(new Font("Algerian", Font.BOLD, 24));
        roadsLeftLabel.setForeground(Color.WHITE);

        // Initialize counters with 0 - we'll update them after gridLayers is
        // initialized
        carnivoresLabel = new JLabel("C: 0");
        carnivoresLabel.setFont(new Font("Algerian", Font.BOLD, 24));
        carnivoresLabel.setForeground(Color.WHITE);

        herbivoresLabel = new JLabel("H: 0");
        herbivoresLabel.setFont(new Font("Algerian", Font.BOLD, 24));
        herbivoresLabel.setForeground(Color.WHITE);

        plantsLabel = new JLabel("T: 0");
        plantsLabel.setFont(new Font("Algerian", Font.BOLD, 24));
        plantsLabel.setForeground(Color.WHITE);

        pondsLabel = new JLabel("P: 0");
        pondsLabel.setFont(new Font("Algerian", Font.BOLD, 24));
        pondsLabel.setForeground(Color.WHITE);

        infoPanel.add(playerLabel);
        infoPanel.add(moneyLabel);
        infoPanel.add(roadsLeftLabel);
        infoPanel.add(carnivoresLabel);
        infoPanel.add(herbivoresLabel);
        infoPanel.add(plantsLabel);
        infoPanel.add(pondsLabel);

        // Rest of the method remains the same...
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        buttonPanel.setOpaque(false);
        JButton speedButton = createSpeedButton();
        buttonPanel.add(speedButton);

        JButton modeButton = new JButton("Mode");
        styleTopButton(modeButton);

        // Add popup menu for Mode selection
        JPopupMenu modeMenu = new JPopupMenu();
        JMenuItem nightModeItem = new JMenuItem("Night Mode On");
        JMenuItem dayModeItem = new JMenuItem("Night Mode Off");

        nightModeItem.addActionListener(e -> toggleNightMode(true));
        dayModeItem.addActionListener(e -> toggleNightMode(false));

        modeMenu.add(nightModeItem);
        modeMenu.add(dayModeItem);

        modeButton.addActionListener(e -> modeMenu.show(modeButton, 0, modeButton.getHeight()));

        JButton shopButton = new JButton("Shop");
        styleTopButton(shopButton);

        JButton backButton = new JButton("Back");
        styleTopButton(backButton);

        topPanel.add(timerLabel, BorderLayout.EAST);
        buttonPanel.add(modeButton);
        buttonPanel.add(shopButton);
        buttonPanel.add(backButton);

        topPanel.add(infoPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        shopButton.addActionListener(e -> {
            new Shop(this);
        });

        backButton.addActionListener(e -> {
            stopMovementTimers();
            new GameMenu();
            frame.dispose();
        });

        return topPanel;
    }

    public void toggleNightMode(boolean enable) {
        this.nightMode = enable;

        for (Animal animal : animals) {
            JLabel label = animal.getLabel();
            boolean shouldShow = !enable;

            if (enable) {
                int animalRow = animal.getRow();
                int animalCol = animal.getCol();

                for (Ranger ranger : rangers) {
                    int dist = Math.abs(animalRow - ranger.getRow()) + Math.abs(animalCol - ranger.getCol());
                    if (dist <= 10) {
                        shouldShow = true;
                        break;
                    }
                }

                if (!shouldShow) {
                    outer: for (int r = Math.max(0, animalRow - 3); r <= Math.min(ROWS - 1, animalRow + 3); r++) {
                        for (int c = Math.max(0, animalCol - 3); c <= Math.min(COLS - 1, animalCol + 3); c++) {
                            int dist = Math.abs(animalRow - r) + Math.abs(animalCol - c);
                            if (dist <= 3 && terrainMatrix[r][c] == 'R') {
                                shouldShow = true;
                                break outer;
                            }
                        }
                    }
                }

                // NEW: Chip override
                if (!shouldShow && chippedAnimals.getOrDefault(animal, false)) {
                    shouldShow = true;
                }
            }

            label.setVisible(shouldShow);
        }

        refreshView();
    }

    public JButton createSpeedButton() {
        JButton speedButton = new JButton("Speed");
        styleTopButton(speedButton);

        JPopupMenu speedMenu = new JPopupMenu();
        String[] speeds = { "Slow (Hour)", "Normal (Day)", "Fast (Week)" };
        GameMode[] modes = { GameMode.HOUR, GameMode.DAY, GameMode.WEEK };

        for (int i = 0; i < speeds.length; i++) {
            JMenuItem item = new JMenuItem(speeds[i]);
            GameMode mode = modes[i];
            item.addActionListener(e -> setGameSpeed(mode));
            speedMenu.add(item);
        }

        speedButton.addActionListener(e -> speedMenu.show(speedButton, 0, speedButton.getHeight()));

        return speedButton;
    }

    public void updateCounters() {
        roadsLeftLabel.setText("Roads Left: " + (Road.getTotalRoadsPurchased() - Road.getRoadsUsed()));
        carnivoresLabel.setText("Ca: " + carnivoreCount);
        herbivoresLabel.setText("H: " + herbivoreCount);
        plantsLabel.setText("T: " + plantCount);
        pondsLabel.setText("Ponds: " + pondCount);
    }

    private JPanel createMapPanel() {
        // Create the main map panel with null layout for absolute positioning
        JPanel mapPanel = new JPanel(null);
        mapPanel.setPreferredSize(new Dimension(COLS * tileSize, ROWS * tileSize));
        mapPanel.setBackground(new Color(139, 69, 19));
        mapPanel.setOpaque(true);

        // Create the layered container for all game elements
        gridContainer = new JLayeredPane();
        gridContainer.setBounds(0, 0, COLS * tileSize, ROWS * tileSize);
        gridContainer.setBackground(new Color(139, 69, 19));
        gridContainer.setOpaque(true);

        // Add mouse listeners to the grid container
        gridContainer.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (Road.isRoadPlacementMode()) {
                    handleRoadAction(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                lastProcessedTile = null;
            }
        });

        gridContainer.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (Road.isRoadPlacementMode()) {
                    handleRoadAction(e);
                }
            }
        });

        // Initialize all grid tiles
        gridLayers = new JLayeredPane[ROWS][COLS];
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                JLayeredPane tilePane = new JLayeredPane();
                tilePane.setBounds(col * tileSize, row * tileSize, tileSize, tileSize);
                tilePane.setOpaque(true);

                // Add terrain background
                JLabel terrainLabel = new JLabel(terrainIcons.get(terrainMatrix[row][col]));
                terrainLabel.setBounds(0, 0, tileSize, tileSize);
                tilePane.add(terrainLabel, JLayeredPane.DEFAULT_LAYER);

                gridContainer.add(tilePane);
                gridLayers[row][col] = tilePane;
            }
        }

        // Add special markers for start and end points
        markStartAndEndTiles();

        // Add the grid container to the map panel
        mapPanel.add(gridContainer);

        // Add component listener to handle viewport changes
        mapPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Ensure the grid container stays properly sized
                gridContainer.setSize(mapPanel.getSize());
            }
        });

        return mapPanel;
    }

    private void initializeCounts() {
        // Reset all counts
        carnivoreCount = 0;
        herbivoreCount = 0;
        plantCount = 0;
        pondCount = 0;

        // Count animals
        for (Animal animal : animals) {
            if (animal instanceof Carnivore) {
                carnivoreCount++;
            } else if (animal instanceof Herbivore) {
                herbivoreCount++;
            }
        }

        // Count plants and ponds from the map
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                JLayeredPane tile = gridLayers[row][col];
                Component[] components = tile.getComponentsInLayer(JLayeredPane.PALETTE_LAYER);
                for (Component comp : components) {
                    if (comp instanceof JLabel) {
                        JLabel label = (JLabel) comp;
                        if (label.getIcon() == objectIcons.get("tree")) {
                            plantCount++;
                        } else if (label.getIcon() == objectIcons.get("pond")) {
                            pondCount++;
                        }
                    }
                }
            }
        }

        updateCounterLabels();
    }

    public void refreshView() {
        bufferValid = false;
        // gridContainer.repaint();

        // Force immediate repaint of all animals and jeeps
        for (Animal animal : animals) {
            animal.getLabel().repaint();
        }
        for (Jeep jeep : jeeps) {
            jeep.getLabel().repaint();
        }
    }


    void handleRoadAction(MouseEvent e) {
        int col = e.getX() / tileSize;
        int row = e.getY() / tileSize;

        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) {
            return;
        }

        if (lastProcessedTile != null && lastProcessedTile.x == col && lastProcessedTile.y == row) {
            return;
        }

        lastProcessedTile = new Point(col, row);

        if (SwingUtilities.isLeftMouseButton(e)) {
            if (Road.isRoadPlacementMode()) {
                Road.placeRoad(this, row, col);
            }
        } else if (SwingUtilities.isRightMouseButton(e)) {
            // Force immediate removal
            JLayeredPane tilePane = gridLayers[row][col];
            Component[] components = tilePane.getComponentsInLayer(JLayeredPane.PALETTE_LAYER);
            for (Component comp : components) {
                if (comp instanceof JLabel) {
                    tilePane.remove(comp);
                }
            }
            tilePane.revalidate();
            tilePane.repaint();
            Road.removeRoad(this, row, col);
        }
    }
    // Call this when placing an item

    public boolean placeItem(String itemType, int row, int col) {
        itemType = itemType.toLowerCase();

        // Check inventory
        if (inventory.getOrDefault(itemType, 0) <= 0) {
            JOptionPane.showMessageDialog(frame, "No " + itemType + "s left to place!");
            return false;
        }

        // Check valid position
        if (!isValidTile(row, col) || !isTileEmpty(row, col)) {
            JOptionPane.showMessageDialog(frame, "Cannot place " + itemType + " here!");
            return false;
        }

        // Create visual representation
        JLabel itemLabel = new JLabel(objectIcons.get(itemType));
        itemLabel.setSize(objectIcons.get(itemType).getIconWidth(),
                objectIcons.get(itemType).getIconHeight());
        itemLabel.setLocation(col * tileSize, row * tileSize);
        gridContainer.add(itemLabel, JLayeredPane.PALETTE_LAYER);

        // Handle special cases
        if (itemType.equals("herbivore") || itemType.equals("carnivore")) {
            // Create and add animal
            Animal animal = itemType.equals("herbivore")
                    ? new Herbivore(row, col, itemLabel, this)
                    : new Carnivore(row, col, itemLabel, this);
            animals.add(animal);
            animal.startMovement();
        } else if (itemType.equals("tree")) {
            // Add tree to the map
            placedItems.put(new Point(row, col), "tree");
        } else if (itemType.equals("pond")) {
            // Add pond to the map
            placedItems.put(new Point(row, col), "pond");
        }

        // Update counters
        switch (itemType) {
            case "herbivore":
                herbivoreCount++;
                break;
            case "carnivore":
                carnivoreCount++;
                break;
            case "tree":
                plantCount++;
                break;
            case "pond":
                pondCount++;
                break;
        }

        // Deduct from inventory
        inventory.put(itemType, inventory.get(itemType) - 1);
        updateCounterLabels();

        return true;
    }
    // Call this when removing an item

    public void removeItem(String itemType, int row, int col) {
        placedItems.remove(new Point(row, col));
        updateItemCount(itemType, -1);
    }

    private void updateItemCount(String itemType, int delta) {
        switch (itemType.toLowerCase()) {
            case "herbivore":
                herbivoreCount += delta;
                break;
            case "carnivore":
                carnivoreCount += delta;
                break;
            case "tree":
                plantCount += delta;
                break;
            case "pond":
                pondCount += delta;
                break;
        }
        updateCounterLabels();
    }

    public void styleTopButton(JButton button) {
        button.setFont(new Font("Algerian", Font.BOLD, 20));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(92, 51, 23));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(255, 204, 0), 2));
    }

    public int getAvailableCount(String itemType) {
        switch (itemType.toLowerCase()) {
            case "herbivore":
                return herbivoreCount;
            case "carnivore":
                return carnivoreCount;
            case "tree":
                return plantCount;
            case "pond":
                return pondCount;
            default:
                return 0;
        }
    }

    public JPanel getMainPanel() {
        return mainPanel; // Return reference to your main JPanel
    }

    public void restartGame() {
        // Reset game state
        frame.dispose();
        new GameMap(playerName, gameState.INITIAL_AMOUNT);
    }

    public void updateMoney(int newMoney) {
        this.money = newMoney;

        if (!testMode && moneyLabel != null) {
            moneyLabel.setText("Money: €" + money);
        }

        gameState.checkGameState(money);
    }

    public void updateCounterLabels() {
        carnivoresLabel.setText("Ca: " + carnivoreCount);
        herbivoresLabel.setText("H: " + herbivoreCount);
        plantsLabel.setText("T: " + plantCount);
        pondsLabel.setText("Ponds: " + pondCount);
    }
    // Call this when an item is selected in the shop

    public void setSelectedItemType(String itemType) {
        this.selectedItemType = itemType;
    }

    public void setupRightClickPlacement() {
        gridContainer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    Point point = e.getPoint();
                    for (Animal animal : animals) {
                        JLabel label = animal.getLabel();
                        Rectangle bounds = label.getBounds();

                        if (bounds.contains(point)) {
                            int count = animalClickCounts.getOrDefault(animal, 0) + 1;
                            animalClickCounts.put(animal, count);

                            if (chipActive && count == 2) {
                                chippedAnimals.put(animal, true);
                                chipActive = false;
                                chipTarget = null;
                                animalClickCounts.clear();
                                JOptionPane.showMessageDialog(frame, "Chip successfully placed on animal.");
                                toggleNightMode(true);
                                return;
                            }
                            return;
                        }
                    }

                    // If not clicking on animal, fallback to item placement
                    if (selectedItemType != null) {
                        Point clickPoint = e.getPoint();
                        int col = clickPoint.x / tileSize;
                        int row = clickPoint.y / tileSize;
                        placeItem(selectedItemType, row, col);
                    }
                }
            }
        });
    }

    public boolean isValidTile(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    public void removeObjectFromTile(int row, int col) {
        JLayeredPane tile = gridLayers[row][col];
        Component[] components = tile.getComponentsInLayer(JLayeredPane.PALETTE_LAYER);
        for (Component comp : components) {
            tile.remove(comp);
        }
        tile.revalidate();
        // tile.repaint();
    }

    public void animalReproduced(String animalType) {
        if (animalType.equalsIgnoreCase("herbivore")) {
            herbivoreCount++;
        } else if (animalType.equalsIgnoreCase("carnivore")) {
            carnivoreCount++;
        }
        inventory.put(animalType, inventory.getOrDefault(animalType, 0) + 1);
        updateCounterLabels();
    }

    public int removePlacedItem(String itemType, int quantity) {
        int removed = 0;
        itemType = itemType.toLowerCase();

        // Create list of all positions with this item type
        List<Point> positions = new ArrayList<>();
        for (Map.Entry<Point, String> entry : placedItems.entrySet()) {
            if (entry.getValue().equals(itemType)) {
                positions.add(entry.getKey());
            }
        }

        // Randomly select items to remove
        Collections.shuffle(positions);
        int toRemove = Math.min(quantity, positions.size());

        for (int i = 0; i < toRemove; i++) {
            Point pos = positions.get(i);
            removeObjectFromTile(pos.x, pos.y);
            placedItems.remove(pos);
            removed++;
        }

        // Update counts
        switch (itemType) {
            case "herbivore":
                herbivoreCount = Math.max(0, herbivoreCount - removed);
                break;
            case "carnivore":
                carnivoreCount = Math.max(0, carnivoreCount - removed);
                break;
            case "tree":
                plantCount = Math.max(0, plantCount - removed);
                break;
            case "pond":
                pondCount = Math.max(0, pondCount - removed);
                break;
        }

        updateCounterLabels();
        return removed;
    }

    public int countItemsOnMap(String itemType) {
        int count = 0;
        for (String type : placedItems.values()) {
            if (type.equalsIgnoreCase(itemType)) {
                count++;
            }
        }
        return count;
    }

    public int getInventoryCount(String itemName) {
        return inventory.getOrDefault(itemName.toLowerCase(), 0);
    }

    public int getPlacedCount(String itemName) {
        int count = 0;
        for (Point pos : placedItems.keySet()) {
            if (placedItems.get(pos).equalsIgnoreCase(itemName)) {
                count++;
            }
        }
        return count;
    }

    public int getTotalAvailable(String itemType) {
        return inventory.getOrDefault(itemType.toLowerCase(), 0)
                + countItemsOnMap(itemType.toLowerCase());
    }
    // When placing an item from inventory to map

    public boolean placeFromInventory(String itemType) {
        if (inventory.getOrDefault(itemType, 0) > 0) {
            inventory.put(itemType, inventory.get(itemType) - 1);
            return true;
        }
        return false;
    }

    // When returning an item from map to inventory
    public void returnToInventory(String itemType) {
        inventory.put(itemType, inventory.getOrDefault(itemType, 0) + 1);
    }

    // Update animal counts when they reproduce or die
    public void updateAnimalCount(String animalType, int delta) {
        if (animalType.equalsIgnoreCase("herbivore")) {
            herbivoreCount += delta;
        } else if (animalType.equalsIgnoreCase("carnivore")) {
            carnivoreCount += delta;
        }
        inventory.put(animalType, inventory.getOrDefault(animalType, 0) + delta);
        updateCounterLabels();
    }

    public void addToInventory(String itemType, int quantity) {
        itemType = itemType.toLowerCase();
        inventory.put(itemType, inventory.getOrDefault(itemType, 0) + quantity);
    }

    // Update plant count when eaten
    public void plantEaten() {
        plantCount--;
        inventory.put("tree", inventory.getOrDefault("tree", 0) - 1);
        updateCounterLabels();
    }

    // Add this method to center viewport
    public void centerViewportOn(int col, int row) {
        int x = Math.max(0, col * tileSize - visibleWidth / 2);
        int y = Math.max(0, row * tileSize - visibleHeight / 2);

        x = Math.min(x, COLS * tileSize - visibleWidth);
        y = Math.min(y, ROWS * tileSize - visibleHeight);

        viewportPosition.setLocation(x, y);
        scrollPane.getViewport().setViewPosition(viewportPosition);
    }

    // Getters needed for Animal, Jeep, and Road classes
    public char[][] getTerrainMatrix() {
        return terrainMatrix;
    }

    public int getTileSize() {
        return tileSize;
    }

    public HashMap<String, ImageIcon> getObjectIcons() {
        return objectIcons;
    }

    public HashMap<Character, ImageIcon> getTerrainIcons() {
        return terrainIcons;
    }

    public JLayeredPane getGridContainer() {
        return gridContainer;
    }

    public JLayeredPane[][] getGridLayers() {
        return gridLayers;
    }

    public List<Animal> getAnimals() {
        return animals;
    }

    public HashMap<String, Point> getPonds() {
        return ponds;
    }

    public JLabel getRoadsLeftLabel() {
        return roadsLeftLabel;
    }

    public int getROWS() {
        return ROWS;
    }

    public int getCOLS() {
        return COLS;
    }

    // Road-related methods that delegate to Road class
    public void startRoadPlacement(int quantity) {
        Road.startRoadPlacement(this, quantity);
    }

    public void continueRoadPlacement() {
        Road.continueRoadPlacement(this);
    }

    public int getAvailableRoads() {
        return Road.getAvailableRoads();
    }

    public boolean isTestMode() {
        return this.testMode;
    }

    public List<Ranger> getRangers() {
        return rangers;
    }

    public void addPurchasedRoads(int quantity) {
        Road.addPurchasedRoads(this, quantity);
    }

    public void updateRoadsLeftLabel() {
        Road.updateRoadsLeftLabel(this);
    }

    public int getVisibleWidth() {
        return visibleWidth;
    }

    public int getVisibleHeight() {
        return visibleHeight;
    }

    public List<Poacher> getPoachers() {
        return poachers;
    }

    public int removeItemsFromMap(String itemType, int quantity) {
        itemType = itemType.toLowerCase();
        int removed = 0;

        if (itemType.equals("herbivore") || itemType.equals("carnivore")) {
            // Handle animals
            Iterator<Animal> iterator = animals.iterator();
            while (iterator.hasNext() && removed < quantity) {
                Animal animal = iterator.next();
                if ((itemType.equals("herbivore") && animal instanceof Herbivore)
                        || (itemType.equals("carnivore") && animal instanceof Carnivore)) {

                    removeObjectFromTile(animal.getRow(), animal.getCol());
                    iterator.remove();
                    removed++;
                }
            }
        } else {
            // Handle trees and ponds
            for (int row = 0; row < ROWS && removed < quantity; row++) {
                for (int col = 0; col < COLS && removed < quantity; col++) {
                    JLayeredPane tile = gridLayers[row][col];
                    Component[] components = tile.getComponentsInLayer(JLayeredPane.PALETTE_LAYER);

                    for (Component comp : components) {
                        if (comp instanceof JLabel) {
                            JLabel label = (JLabel) comp;
                            if (label.getIcon() == objectIcons.get(itemType)) {
                                tile.remove(label);
                                tile.revalidate();
                                // tile.repaint();
                                removed++;

                                // For ponds, remove from ponds map
                                if (itemType.equals("pond")) {
                                    ponds.remove(row + "," + col);
                                }
                                break; // Move to next tile after removing one item
                            }
                        }
                    }
                }
            }
        }

        // Update counters
        switch (itemType) {
            case "herbivore":
                herbivoreCount -= removed;
                break;
            case "carnivore":
                carnivoreCount -= removed;
                break;
            case "tree":
                plantCount -= removed;
                break;
            case "pond":
                pondCount -= removed;
                break;
        }

        updateCounterLabels();
        return removed;
    }

    public void increaseCapital() {
        Random random = new Random();
        int amount = random.nextInt(2000 - 100 + 1) + 100; // Random value between 100 and 20000
        this.money += amount;
        updateMoney(this.money); // Update the money label
        if (!testMode) {
            JOptionPane.showMessageDialog(getFrame(), "Capital increased!");
        }
    }

    public void updateGameTimers() {
        // Stop existing timers
        stopMovementTimers();

        // Restart timers with new speed
        startMovementTimers();

        // Update animal timers
        for (Animal animal : animals) {
            animal.stopTimers();
            animal.startMovement();
        }
    }

    // Add this method to handle speed changes
    public void setGameSpeed(GameMode mode) {
        if (mode == null) {
            return;
        }

        this.currentMode = mode;

        // Update animals, jeeps, and poachers
        if (animals != null) {
            for (Animal animal : animals) {
                if (animal != null) {
                    animal.setSpeedMultiplier(mode.getSpeedMultiplier());
                }
            }
        }

        if (jeeps != null) {
            for (Jeep jeep : jeeps) {
                if (jeep != null) {
                    jeep.setSpeedMultiplier(mode.getSpeedMultiplier());
                }
            }
        }

        if (poachers != null) {
            for (Poacher poacher : poachers) {
                if (poacher != null) {
                    poacher.setSpeedMultiplier(mode.getSpeedMultiplier());
                }
            }
        }

        // Adjust the game timer speed
        adjustGameTimerSpeed(mode);

        // Update timers as needed
        updateGameTimers();

        if (!testMode) {
            JOptionPane.showMessageDialog(frame, "Game speed set to: " + mode.toString());
        }
    }

    public void adjustGameTimerSpeed(GameMode mode) {
        if (gameTimer != null) {
            int newDelay = (int) (1000 / mode.getSpeedMultiplier());
            gameTimer.setDelay(newDelay);
        }
    }

    public void spawnRanger(int row, int col) {
        if (!isTileEmpty(row, col)) {
            if (!testMode) {
                JOptionPane.showMessageDialog(frame, "Cannot place a ranger here!");
            }
            return;
        }

        Ranger ranger = new Ranger(row, col, this);
        rangers.add(ranger);
        updateMoney(money - ranger.getSalary()); // Deduct initial salary

        if (!testMode) {
            JOptionPane.showMessageDialog(frame, "Ranger placed at (" + row + ", " + col + ").");
        }
    }

    public void rangerEliminatePredator(String predatorType) {
        for (Ranger ranger : rangers) {
            ranger.eliminatePredator(predatorType);
        }

    }

    public void rangerProtectAgainstPoachers() {
        for (Ranger ranger : rangers) {
            ranger.protectAgainstPoachers();
        }

    }

    public void payRangerSalaries() {
        Iterator<Ranger> iterator = rangers.iterator();
        while (iterator.hasNext()) {
            Ranger ranger = iterator.next();
            if (!ranger.paySalary()) {
                iterator.remove(); // Remove ranger if salary cannot be paid
                JOptionPane.showMessageDialog(frame, "A ranger has left due to unpaid salary!");
            }
        }
    }

    public void verifyComponentCounts() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                JLayeredPane pane = gridLayers[row][col];
                if (pane.getComponentCount() > 2) { // Background + possible object
                    System.out
                            .println("Tile [" + row + "][" + col + "] has " + pane.getComponentCount() + " components");
                }
            }
        }
    }
}

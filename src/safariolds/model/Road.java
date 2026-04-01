package safariolds.model;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Queue;
import javax.swing.JOptionPane;
import safariolds.controller.GameMap;

public class Road {

    private int row;
    private int col;
    private JLabel label;
    private GameMap gameMap;

    // Constants for road placement
    private static final int START_ROW = 10;
    private static final int START_COL = 0;
    private static final int END_ROW = 10;
    private static final int END_COL = 39; // COLS - 1 (40 - 1)

    // Road placement state
    public static boolean roadPlacementMode = false;
    public static int roadsToPlace = 0;
    public static List<Road> roads = new ArrayList<>();
    private static Point firstRoadPosition = null;
    private static boolean isFirstRoadPlaced = false;
    private static boolean firstPathCompleted = false;
    public static int totalRoadsPurchased = 0;
    public static int roadsUsed = 0;

    public Road(int row, int col, JLabel label, GameMap gameMap) {
        this.row = row;
        this.col = col;
        this.label = label;
        this.gameMap = gameMap;
    }

    // Getters and setters
    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public JLabel getLabel() {
        return label;
    }

    public static void startRoadPlacement(GameMap gameMap, int quantity) {
        int roadsAvailable = totalRoadsPurchased - roadsUsed;

        if (roadsAvailable <= 0) {
            if (!gameMap.isTestMode()) {
                JOptionPane.showMessageDialog(gameMap.getFrame(),
                        "No roads left! Please buy more from the shop.",
                        "No Roads Available",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        roadsToPlace = roadsAvailable;
        roadPlacementMode = true;

    }

    public static boolean isValidRoadPlacement(GameMap gameMap, int row, int col) {
        if (firstPathCompleted) {
            return true;
        }

        // Allow if connected orthogonally 
        if (hasOrthogonalConnection(gameMap, row, col)) {
            return true;
        }

        // Check for diagonal roads
        for (int i = -1; i <= 1; i += 2) {
            for (int j = -1; j <= 1; j += 2) {
                int diagRow = row + i;
                int diagCol = col + j;

                // If diagonally adjacent tile is a road, reject
                if (diagRow >= 0 && diagRow < gameMap.getROWS()
                        && diagCol >= 0 && diagCol < gameMap.getCOLS()
                        && gameMap.getTerrainMatrix()[diagRow][diagCol] == 'R') {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean isFirstPathCompleted() {
        return firstPathCompleted;
    }

    public static boolean placeRoad(GameMap gameMap, int row, int col) {
        // Validate placement conditions
        if (!roadPlacementMode) {
            return false;
        }

        // Check bounds
        if (row < 0 || row >= gameMap.getROWS() || col < 0 || col >= gameMap.getCOLS()) {
            return false;
        }

        // Check if tile is occupied by objects
        if (isTileOccupied(gameMap, row, col)) {
            if (!gameMap.isTestMode()) {
                JOptionPane.showMessageDialog(gameMap.getFrame(),
                        "Cannot place road on occupied tiles (animals, ponds, trees)!",
                        "Invalid Position",
                        JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }

        // Check if we have roads left in inventory
        if ((totalRoadsPurchased - roadsUsed) <= 0) {
            if (!gameMap.isTestMode()) {
                JOptionPane.showMessageDialog(gameMap.getFrame(),
                        "No more roads in inventory! Buy more from the shop.",
                        "Road Inventory Empty",
                        JOptionPane.WARNING_MESSAGE);
                roadPlacementMode = false;
                return false;
            }
        }

        // Check if we have roads left in current placement session
        if (roadsToPlace <= 0) {
            if (!gameMap.isTestMode()) {
                JOptionPane.showMessageDialog(gameMap.getFrame(),
                        "Current road placement session completed!",
                        "Road Placement Done",
                        JOptionPane.INFORMATION_MESSAGE);
                roadPlacementMode = false;
                return false;
            }
        }

        if (roads.isEmpty()) {
            if (row != START_ROW || col != START_COL) {
                if (!gameMap.isTestMode()) {
                    JOptionPane.showMessageDialog(gameMap.getFrame(),
                            "First road must be placed at the starting point (S)",
                            "Invalid Starting Point",
                            JOptionPane.WARNING_MESSAGE);
                }
                return false;
            }
        }

        // Check if this is the ending point (10, last column)
        if (row == END_ROW && col == END_COL) {
            if (isConnectedToStart(gameMap, row, col)) {
                return placeFinalRoad(gameMap, row, col);
            } else {
                if (!gameMap.isTestMode()) {
                    JOptionPane.showMessageDialog(gameMap.getFrame(),
                            "Road must connect to starting point (S) before reaching the end (E)!",
                            "Disconnected Road",
                            JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            }
        }

        // Normal road placement - must connect to existing roads
        if (!roads.isEmpty() && !isValidRoadConnection(gameMap, row, col)) {
            if (!gameMap.isTestMode()) {
                JOptionPane.showMessageDialog(gameMap.getFrame(),
                        "Roads must connect to existing roads!",
                        "Invalid Road Placement",
                        JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }

        try {
            // Create road visual (full tile size)
            JLabel roadLabel = new JLabel(gameMap.getObjectIcons().get("road"));
            roadLabel.setBounds(0, 0, gameMap.getTileSize(), gameMap.getTileSize());

            // Get the tile pane
            JLayeredPane tilePane = gameMap.getGridLayers()[row][col];

            // Remove existing grass visual
            for (Component comp : tilePane.getComponents()) {
                if (comp instanceof JLabel) {
                    JLabel label = (JLabel) comp;
                    if (label.getIcon() == gameMap.getTerrainIcons().get('G')
                            || label.getText() != null) { // Remove any markers
                        tilePane.remove(comp);
                        break;
                    }
                }
            }

            // Add road to the tile
            tilePane.add(roadLabel, JLayeredPane.PALETTE_LAYER);
            tilePane.moveToFront(roadLabel);

            // Update game state
            gameMap.getTerrainMatrix()[row][col] = 'R';
            roads.add(new Road(row, col, roadLabel, gameMap));
            roadsUsed++;  // Increment total roads used
            roadsToPlace--;  // Decrement roads in current session

            // Update UI
            gameMap.updateRoadsLeftLabel();
            tilePane.revalidate();
            tilePane.repaint();

            if (roadsToPlace <= 0) {
                roadPlacementMode = false;
                if (!gameMap.isTestMode()) {
                    JOptionPane.showMessageDialog(gameMap.getFrame(),
                            "Road placement session completed!",
                            "Roads Placed",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }

            return true;
        } catch (Exception e) {
            System.err.println("Error placing road: " + e.getMessage());
            return false;
        }
    }

    public static int getAvailableRoads() {
        return totalRoadsPurchased - roadsUsed;
    }

    public static void updateRoadsLeftLabel(GameMap gameMap) {
        int roadsLeft = totalRoadsPurchased - roadsUsed;
        gameMap.getRoadsLeftLabel().setText("Roads Left: " + roadsLeft);
    }

    public static void addPurchasedRoads(GameMap gameMap, int quantity) {
        totalRoadsPurchased += quantity;
        updateRoadsLeftLabel(gameMap);
    }

    public static boolean isTileOccupied(GameMap gameMap, int row, int col) {
        // Special handling for start/end tiles - treat them as grass for road placement
        if ((row == START_ROW && col == START_COL) || (row == END_ROW && col == END_COL)) {
            return gameMap.getTerrainMatrix()[row][col] == 'R'; // Only occupied if already has road
        }

        // Check terrain matrix for non-grass tiles
        if (gameMap.getTerrainMatrix()[row][col] != 'G') {
            return true;
        }

        // Check for animals
        for (Animal animal : gameMap.getAnimals()) {
            if (animal.getRow() == row && animal.getCol() == col) {
                return true;
            }
        }

        // Check for ponds
        if (gameMap.getPonds().containsKey(row + "," + col)) {
            return true;
        }

        // Check for trees/plants in the tile components
        JLayeredPane tile = gameMap.getGridLayers()[row][col];
        for (Component comp : tile.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                ImageIcon icon = (ImageIcon) label.getIcon();
                if (icon == gameMap.getObjectIcons().get("tree")
                        || icon == gameMap.getObjectIcons().get("pond")
                        || icon == gameMap.getObjectIcons().get("herbivore")
                        || icon == gameMap.getObjectIcons().get("carni")) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean hasOrthogonalConnection(GameMap gameMap, int row, int col) {
        int ROWS = gameMap.getROWS();
        int COLS = gameMap.getCOLS();
        char[][] terrainMatrix = gameMap.getTerrainMatrix();

        // Check all four orthogonal directions with gap tolerance
        for (int i = Math.max(0, row - 2); i <= Math.min(ROWS - 1, row + 2); i++) {
            if (terrainMatrix[i][col] == 'R' && Math.abs(i - row) <= 2) {
                return true;
            }
        }

        for (int j = Math.max(0, col - 2); j <= Math.min(COLS - 1, col + 2); j++) {
            if (terrainMatrix[row][j] == 'R' && Math.abs(j - col) <= 2) {
                return true;
            }
        }

        return false;
    }

    private static boolean isConnectedToStart(GameMap gameMap, int row, int col) {
        int ROWS = gameMap.getROWS();
        int COLS = gameMap.getCOLS();
        char[][] terrainMatrix = gameMap.getTerrainMatrix();

        boolean[][] visited = new boolean[ROWS][COLS];
        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(row, col));
        visited[row][col] = true;

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // Up, Down, Left, Right

        while (!queue.isEmpty()) {
            Point current = queue.poll();

            // Found starting point
            if (current.x == START_ROW && current.y == START_COL) {
                return true;
            }

            for (int[] dir : directions) {
                int newRow = current.x + dir[0];
                int newCol = current.y + dir[1];

                if (newRow >= 0 && newRow < ROWS && newCol >= 0 && newCol < COLS
                        && !visited[newRow][newCol] && terrainMatrix[newRow][newCol] == 'R') {
                    visited[newRow][newCol] = true;
                    queue.add(new Point(newRow, newCol));
                }
            }
        }

        return false;
    }

    public static boolean isValidRoadConnection(GameMap gameMap, int row, int col) {
        int ROWS = gameMap.getROWS();
        int COLS = gameMap.getCOLS();
        char[][] terrainMatrix = gameMap.getTerrainMatrix();

        // Check all four directions for existing roads
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // Up, Down, Left, Right

        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];

            if (newRow >= 0 && newRow < ROWS && newCol >= 0 && newCol < COLS
                    && terrainMatrix[newRow][newCol] == 'R') {
                return true;
            }
        }

        return false;
    }

    private static boolean placeFinalRoad(GameMap gameMap, int row, int col) {
        try {
            // Create road visual (full tile size)
            JLabel roadLabel = new JLabel(gameMap.getObjectIcons().get("road"));
            roadLabel.setBounds(0, 0, gameMap.getTileSize(), gameMap.getTileSize());

            JLayeredPane tilePane = gameMap.getGridLayers()[row][col];

            // Clear any existing markers
            for (Component comp : tilePane.getComponents()) {
                if (comp instanceof JLabel && ((JLabel) comp).getText() != null) {
                    tilePane.remove(comp);
                }
            }

            tilePane.add(roadLabel, JLayeredPane.PALETTE_LAYER);
            tilePane.moveToFront(roadLabel);

            gameMap.getTerrainMatrix()[row][col] = 'R';
            roads.add(new Road(row, col, roadLabel, gameMap));
            roadsToPlace--;

            tilePane.revalidate();
            tilePane.repaint();

            firstPathCompleted = true;
            roadsUsed++;  // Count this final road piece
            updateRoadsLeftLabel(gameMap);

            // Automatically continue with remaining roads
            int remainingRoads = totalRoadsPurchased - roadsUsed;
            if (remainingRoads > 0) {
                roadsToPlace = remainingRoads;
                roadPlacementMode = true;
                if (!gameMap.isTestMode()) {
                    JOptionPane.showMessageDialog(gameMap.getFrame(),
                            "Road network completed successfully!\n"
                            + "Automatically continuing with " + remainingRoads + " remaining roads.",
                            "Construction Complete",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                roadPlacementMode = false;
                if (!gameMap.isTestMode()) {
                    JOptionPane.showMessageDialog(gameMap.getFrame(),
                            "Road network completed successfully!\n"
                            + "No roads remaining - buy more from the shop.",
                            "Construction Complete",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error placing final road: " + e.getMessage());
            return false;
        }
    }

    public static void continueRoadPlacement(GameMap gameMap) {
        int remainingRoads = totalRoadsPurchased - roadsUsed;
        if (remainingRoads <= 0) {
            if (!gameMap.isTestMode()) {
                JOptionPane.showMessageDialog(gameMap.getFrame(),
                        "No roads left to place!",
                        "No Roads Available",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        roadsToPlace = remainingRoads;
        roadPlacementMode = true;

        // System.out.println("Continuing road placement with " + remainingRoads + " roads");
    }

    public static boolean hasRoadInDirection(GameMap gameMap, int row, int col, int rowDir, int colDir) {
        int ROWS = gameMap.getROWS();
        int COLS = gameMap.getCOLS();
        char[][] terrainMatrix = gameMap.getTerrainMatrix();

        // Check up to 3 tiles away in the specified direction
        for (int i = 1; i <= 3; i++) {
            int newRow = row + (i * rowDir);
            int newCol = col + (i * colDir);

            // If we go out of bounds, stop checking this direction
            if (newRow < 0 || newRow >= ROWS || newCol < 0 || newCol >= COLS) {
                return false;
            }

            // If we find a road, this is a valid connection
            if (terrainMatrix[newRow][newCol] == 'R') {
                return true;
            }

            // If we hit non-grass terrain before finding a road, stop
            if (terrainMatrix[newRow][newCol] != 'G') {
                return false;
            }
        }
        return false;
    }

    public static boolean isAdjacentToExistingRoad(GameMap gameMap, int row, int col) {
        int ROWS = gameMap.getROWS();
        int COLS = gameMap.getCOLS();
        char[][] terrainMatrix = gameMap.getTerrainMatrix();
        return (row > 0 && terrainMatrix[row - 1][col] == 'R')
                || // Up
                (row < ROWS - 1 && terrainMatrix[row + 1][col] == 'R')
                || // Down
                (col > 0 && terrainMatrix[row][col - 1] == 'R')
                || // Left
                (col < COLS - 1 && terrainMatrix[row][col + 1] == 'R');  // Right
    }

    public static void forceRemoveRoad(GameMap gameMap, int row, int col) {
        gameMap.getTerrainMatrix()[row][col] = 'G'; // Reset to grass

        JLayeredPane tilePane = gameMap.getGridLayers()[row][col];
        // Remove all road components
        Component[] components = tilePane.getComponentsInLayer(JLayeredPane.PALETTE_LAYER);
        for (Component comp : components) {
            if (comp instanceof JLabel && ((JLabel) comp).getIcon() == gameMap.getObjectIcons().get("road")) {
                tilePane.remove(comp);
            }
        }

        // Immediate repaint
        tilePane.revalidate();
        tilePane.repaint();
        roadsUsed--;  // This is fine here since we're in the Road class
        gameMap.updateRoadsLeftLabel();
    }

    public static boolean removeRoad(GameMap gameMap, int row, int col) {
        // Check if we're trying to remove start or end points
        if ((row == START_ROW && col == START_COL) || (row == END_ROW && col == END_COL)) {
            if (!gameMap.isTestMode()) {
                JOptionPane.showMessageDialog(gameMap.getFrame(),
                        "Cannot remove the start (S) or end (E) points!",
                        "Protected Location",
                        JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }

        if (!roadPlacementMode || row < 0 || row >= gameMap.getROWS() || col < 0 || col >= gameMap.getCOLS()) {
            return false;
        }

        if (gameMap.getTerrainMatrix()[row][col] != 'R') {
            return false;
        }

        try {
            JLayeredPane tilePane = gameMap.getGridLayers()[row][col];

            // 1. First remove all road components
            Component[] components = tilePane.getComponentsInLayer(JLayeredPane.PALETTE_LAYER);
            for (Component comp : components) {
                if (comp instanceof JLabel && ((JLabel) comp).getIcon() == gameMap.getObjectIcons().get("road")) {
                    tilePane.remove(comp);
                }
            }

            // 2. Immediately update game state
            gameMap.getTerrainMatrix()[row][col] = 'G';
            roadsUsed--;
            roadsToPlace++;

            // 3. Force immediate repaint of the exact tile area
            Rectangle repaintArea = new Rectangle(
                    col * gameMap.getTileSize(),
                    row * gameMap.getTileSize(),
                    gameMap.getTileSize(),
                    gameMap.getTileSize()
            );

            // 4. Triple-buffered repaint approach
            SwingUtilities.invokeLater(() -> {
                // Add grass back
                JLabel grassLabel = new JLabel(gameMap.getTerrainIcons().get('G'));
                grassLabel.setBounds(0, 0, gameMap.getTileSize(), gameMap.getTileSize());
                tilePane.add(grassLabel, JLayeredPane.DEFAULT_LAYER);

                // Force three-phase repaint
                tilePane.revalidate();
                tilePane.repaint();
                gameMap.getGridContainer().paintImmediately(repaintArea);

                Toolkit.getDefaultToolkit().sync(); // Sync with native graphics
            });

            updateRoadsLeftLabel(gameMap);
            return true;

        } catch (Exception e) {
            System.err.println("Error removing road: " + e.getMessage());
            return false;
        }
    }

    public static void endRoadPlacement() {
        roadPlacementMode = false;
    }

    public static boolean isRoadPlacementMode() {
        return roadPlacementMode;
    }

    public static int getTotalRoadsPurchased() {
        return totalRoadsPurchased;
    }

    public static int getRoadsUsed() {
        return roadsUsed;
    }

    public static List<Road> getRoads() {
        return roads;
    }
}

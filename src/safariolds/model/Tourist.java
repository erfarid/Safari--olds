package safariolds.model;

import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.awt.image.BufferedImage;
import safariolds.controller.GameMap;
import safariolds.model.Tourist;

public class Tourist {

    private JLabel label;
    private Jeep jeep;
    private GameMap gameMap;
    private int positionIndex; // 0=left, 1=right, 2=up, 3=down

    // Tourist image paths
    private static final String[] TOURIST_IMAGES = {
        //"/safariolds/view/assets/tourist3.png",
        "/safariolds/view/assets/tourist.png"
    };

    public Tourist(Jeep jeep, GameMap gameMap, int positionIndex) {
        this.jeep = jeep;
        this.gameMap = gameMap;
        this.positionIndex = positionIndex % 4; // Ensure valid index

        // Load and scale tourist image
        ImageIcon icon = loadTouristImage();
        this.label = new JLabel(icon);
        this.label.setSize(icon.getIconWidth(), icon.getIconHeight());

        gameMap.getGridContainer().add(label);
        updatePosition();
        setVisible(false); // Start hidden
    }

    private ImageIcon loadTouristImage() {
        try {
            String imagePath = TOURIST_IMAGES[new Random().nextInt(TOURIST_IMAGES.length)];
            ImageIcon originalIcon = new ImageIcon(getClass().getResource(imagePath));
            int size = gameMap.getTileSize() / 2;
            return new ImageIcon(originalIcon.getImage()
                    .getScaledInstance(size, size, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return createFallbackIcon();
        }
    }

    private ImageIcon createFallbackIcon() {
        int size = gameMap.getTileSize() / 2;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        // Draw colored circle tourist
        g2d.setColor(new Color(255, 220, 180));
        g2d.fillOval(2, 2, size - 4, size - 4);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(2, 2, size - 4, size - 4);

        g2d.dispose();
        return new ImageIcon(img);
    }

    public void updatePosition() {
        int jeepCol = jeep.getCol();
        int jeepRow = jeep.getRow();
        int tileSize = gameMap.getTileSize();

        // Calculate vertical positions (2 above, 2 below)
        int adjCol = jeepCol;
        int adjRow = jeepRow;

        switch (positionIndex % 4) {
            case 0:
                adjRow -= 2;
                break; // 2 above
            case 1:
                adjRow -= 1;
                break; // 1 above
            case 2:
                adjRow += 1;
                break; // 1 below
            case 3:
                adjRow += 2;
                break; // 2 below
        }

        // Ensure positions stay within map bounds
        adjRow = Math.max(0, Math.min(adjRow, gameMap.getROWS() - 1));

        // Center in tile
        int x = adjCol * tileSize + (tileSize - label.getWidth()) / 2;
        int y = adjRow * tileSize + (tileSize - label.getHeight()) / 2;

        label.setLocation(x, y);
    }

    public boolean isPositionValid(int col, int row) {
        if (col < 0 || col >= gameMap.getCOLS() || row < 0 || row >= gameMap.getROWS()) {
            return false;
        }
        return true;
    }

    public void setVisible(boolean visible) {
        if (label.getParent() == null) {
            return; // Skip if already removed
        }
        Point pos = getGridPosition();
        char terrain = gameMap.getTerrainMatrix()[pos.y][pos.x];
        boolean isValid = terrain == 'G' && isTileEmpty(pos);

        label.setVisible(visible && isValid);
        if (visible && isValid) {
            safelyBringToFront();
        }
    }

    private boolean isTileEmpty(Point pos) {
        // Check for ponds
        if (gameMap.getPonds().containsKey(pos.y + "," + pos.x)) {
            return false;
        }

        // Check for animals
        for (Animal animal : gameMap.getAnimals()) {
            if (animal.getCol() == pos.x && animal.getRow() == pos.y) {
                return false;
            }
        }

        return true;
    }

    private void safelyBringToFront() {
        if (label.getParent() != null) {
            try {
                gameMap.getGridContainer().setLayer(label, JLayeredPane.PALETTE_LAYER);
                gameMap.getGridContainer().moveToFront(label);
            } catch (Exception e) {
                System.err.println("Error bringing tourist to front: " + e.getMessage());
            }
        }
    }

    public Point getGridPosition() {
        int tileSize = gameMap.getTileSize();
        int col = label.getX() / tileSize;
        int row = label.getY() / tileSize;
        return new Point(col, row);
    }

    public boolean isValidPosition() {
        Point pos = getGridPosition();

        // 1. Check map boundaries
        if (pos.x < 0 || pos.x >= gameMap.getCOLS()
                || pos.y < 0 || pos.y >= gameMap.getROWS()) {
            return false;
        }

        // 2. Check terrain type (must be grass)
        if (gameMap.getTerrainMatrix()[pos.y][pos.x] != 'G') {
            return false;
        }

        // 3. Check for ponds (water sources)
        if (gameMap.getPonds().containsKey(pos.y + "," + pos.x)) {
            return false;
        }

        // 4. Check for other objects in the tile
        JLayeredPane tilePane = gameMap.getGridLayers()[pos.y][pos.x];
        for (Component comp : tilePane.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel objectLabel = (JLabel) comp;
                ImageIcon icon = (ImageIcon) objectLabel.getIcon();

                // Skip the grass background
                if (icon == gameMap.getTerrainIcons().get('G')) {
                    continue;
                }

                // Reject if any other object is present
                return false;
            }
        }

        // 5. Check for animals
        for (Animal animal : gameMap.getAnimals()) {
            if (animal.getCol() == pos.x && animal.getRow() == pos.y) {
                return false;
            }
        }

        // 6. Check for roads (if you don't want tourists on roads)
        if (gameMap.getTerrainMatrix()[pos.y][pos.x] == 'R') {
            return false;
        }

        return true;
    }

    public void setForcedVisible(boolean visible) {
        if (label.getParent() != null) {
            label.setVisible(visible);
            if (visible) {
                safelyBringToFront();
            }
        }
    }

    public void remove() {
        if (label.getParent() != null) {
            gameMap.getGridContainer().remove(label);
            gameMap.getGridContainer().repaint();
        }
    }

    public JLabel getLabel() {
        return this.label;
    }
}
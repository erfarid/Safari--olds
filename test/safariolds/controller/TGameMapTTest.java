package safariolds.controller;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.util.Map;
import javax.swing.*;
public class TGameMapTTest {
    private TGameMapT gameMap;
    
    @Before
    public void setUp() {
        System.setProperty("java.awt.headless", "true");
        gameMap = new TGameMapT("TestPlayer", 100000);
    }

    // Constructor and Initialization Tests
    @Test
    public void testConstructorInitialization() {
        assertEquals("TestPlayer", gameMap.playerName);
        assertEquals(100000, gameMap.money);
        assertTrue(gameMap.methodCalls.contains("initTerrainImages"));
        assertTrue(gameMap.methodCalls.contains("initObjectImages"));
        assertTrue(gameMap.methodCalls.contains("initComponents"));
    }

    @Test
    public void testInitTerrainImages() {
        Map<Character, ImageIcon> terrainIcons = gameMap.terrainIcons;
        assertNotNull(terrainIcons);
        assertNotNull(terrainIcons.get('G'));
        assertEquals(Color.GREEN, getIconColor(terrainIcons.get('G')));
    }

    @Test
    public void testInitObjectImages() {
        Map<String, ImageIcon> objectIcons = gameMap.objectIcons;
        assertNotNull(objectIcons);
        assertEquals(Color.RED, getIconColor(objectIcons.get("herbivore")));
        assertEquals(Color.ORANGE, getIconColor(objectIcons.get("carnivore")));
        assertEquals(Color.GREEN.darker(), getIconColor(objectIcons.get("tree")));
        assertEquals(Color.BLUE, getIconColor(objectIcons.get("pond")));
        assertEquals(Color.GRAY, getIconColor(objectIcons.get("road")));
    }

    // GUI Component Tests
    @Test
    public void testCreateTopPanel() {
        assertTrue(gameMap.methodCalls.contains("createTopPanel"));
        assertNotNull(gameMap.moneyLabel);
        assertNotNull(gameMap.roadsLeftLabel);
        assertNotNull(gameMap.carnivoresLabel);
        assertNotNull(gameMap.herbivoresLabel);
        assertNotNull(gameMap.plantsLabel);
        assertNotNull(gameMap.pondsLabel);
    }

    @Test
    public void testSetVisible() {
        gameMap.setVisible(true);
        assertTrue(gameMap.guiInitialized);
        assertTrue(gameMap.methodCalls.contains("setVisible(true)"));
        
        gameMap.setVisible(false);
        assertFalse(gameMap.guiInitialized);
        assertTrue(gameMap.methodCalls.contains("setVisible(false)"));
    }

    // Counter Label Tests
    @Test
    public void testUpdateCounterLabels() {
        gameMap.money = 50000;
        gameMap.carnivoreCount = 3;
        gameMap.herbivoreCount = 5;
        gameMap.plantCount = 7;
        gameMap.pondCount = 2;
        
        gameMap.updateCounterLabels();
        
        assertTrue(gameMap.methodCalls.contains("updateCounterLabels"));
        assertEquals("Money: €50000", gameMap.moneyLabel.getText());
        assertEquals("Ca: 3", gameMap.carnivoresLabel.getText());
        assertEquals("H: 5", gameMap.herbivoresLabel.getText());
        assertEquals("T: 7", gameMap.plantsLabel.getText());
        assertEquals("Ponds: 2", gameMap.pondsLabel.getText());
    }

    @Test
    public void testInitializeCounts() {
        gameMap.initializeCounts();
        assertEquals(0, gameMap.carnivoreCount);
        assertEquals(0, gameMap.herbivoreCount);
        assertEquals(0, gameMap.plantCount);
        assertEquals(0, gameMap.pondCount);
        assertTrue(gameMap.methodCalls.contains("initializeCounts"));
    }

    // Tile Manipulation Tests
    @Test
    public void testSetTestTile() {
        gameMap.setTestTile(2, 3, 'G');
        assertEquals('G', gameMap.terrainMatrix[2][3]);
    }

//     Image Creation Tests
    @Test
    public void testCreateColorIcon() {
        ImageIcon icon = gameMap.createColorIcon(Color.RED, 20, 20);
        assertNotNull(icon);
        assertEquals(20, icon.getIconWidth());
        assertEquals(20, icon.getIconHeight());
        assertTrue(gameMap.methodCalls.contains("createColorIcon"));
    }

    @Test
    public void testUpdateCountersWithNullLabels() {
        gameMap.moneyLabel = null;
        gameMap.roadsLeftLabel = null;
        gameMap.carnivoresLabel = null;
        gameMap.herbivoresLabel = null;
        gameMap.plantsLabel = null;
        gameMap.pondsLabel = null;
        
        // Should not throw NPE
        gameMap.updateCounterLabels();
    }

    @Test
    public void testMultipleItemAdditions() {
        gameMap.addTestItem(1, 1, "tree");
        gameMap.addTestItem(2, 2, "tree");
        gameMap.addTestItem(3, 3, "pond");
        assertEquals(3, gameMap.placedItems.size());
        assertEquals(2, gameMap.plantCount);
        assertEquals(1, gameMap.pondCount);
    }

    // Method Call Tracking Tests
    @Test
    public void testMethodCallTracking() {
        assertTrue(gameMap.methodCalls.contains("initTerrainImages"));
        assertTrue(gameMap.methodCalls.contains("initObjectImages"));
        assertTrue(gameMap.methodCalls.contains("initComponents"));
        assertTrue(gameMap.methodCalls.contains("createTopPanel"));
    }

    // State Persistence Tests
    @Test
    public void testStateAfterMultipleOperations() {
        gameMap.setTestTile(1, 1, 'G');
        gameMap.addTestAnimal(1, 1, "herbivore");
        gameMap.addTestItem(1, 1, "tree");
        
        assertEquals('G', gameMap.terrainMatrix[1][1]);
        assertEquals(1, gameMap.animals.size());
        assertEquals("tree", gameMap.placedItems.get(new Point(1, 1)));
    }

    // Helper method to get color from ImageIcon
    private Color getIconColor(ImageIcon icon) {
        BufferedImage img = new BufferedImage(
            icon.getIconWidth(),
            icon.getIconHeight(),
            BufferedImage.TYPE_INT_RGB
        );
        icon.paintIcon(null, img.getGraphics(), 0, 0);
        return new Color(img.getRGB(0, 0));
    }
}
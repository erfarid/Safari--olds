package safariolds.controller;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Map;
import safariolds.model.*;
import java.util.List;
import java.util.ArrayList;
import java.awt.Point;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class GameMapTest {

    private TGameMapT gameMap;

    @Before
    public void setUp() {
        gameMap = new TGameMapT("TestPlayer", 10000);
        gameMap.initTerrainMatrix();
        gameMap.initComponents();
        gameMap.initObjectImages();
    }

    @Test
    public void testInitialization() {
        assertNotNull(gameMap);
        assertEquals("TestPlayer", gameMap.playerName);
        assertEquals(10000, gameMap.getMoney());
        assertEquals(20, gameMap.getROWS());
        assertEquals(40, gameMap.getCOLS());
    }

    @Test
    public void testInitialMoney() {
        assertEquals(10000, gameMap.getMoney());
    }

    @Test
    public void testTileValidation() {
        assertTrue(gameMap.isValidTile(0, 0));
        assertTrue(gameMap.isValidTile(10, 20));
        assertTrue(gameMap.isValidTile(19, 39));

        assertFalse(gameMap.isValidTile(-1, 0));
        assertFalse(gameMap.isValidTile(0, -1));
        assertFalse(gameMap.isValidTile(20, 0));
        assertFalse(gameMap.isValidTile(0, 40));
    }

    @Test
    public void testMethodTracking() {
        gameMap.setVisible(true);
        assertTrue(gameMap.methodCalls.contains("setVisible(true)"));
    }

    @Test
    public void testTerrainMatrixInitialization() {
        char[][] matrix = gameMap.getTerrainMatrix();
        assertNotNull(matrix);
        assertEquals('G', matrix[0][0]);
        assertEquals('S', matrix[GameMap.START_ROW][GameMap.START_COL]);
        assertEquals('E', matrix[GameMap.END_ROW][GameMap.END_COL]);
    }

    @Test
    public void testInventoryInitialization() {
        Map<String, Integer> inventory = gameMap.inventory;
        assertNotNull(inventory);
        assertEquals(0, (int) inventory.get("herbivore"));
        assertEquals(0, (int) inventory.get("carnivore"));
        assertEquals(0, (int) inventory.get("tree"));
        assertEquals(0, (int) inventory.get("pond"));
    }

    @Test
    public void testGameModeChanges() {
        assertEquals(GameMode.DAY, gameMap.currentMode);

        gameMap.setGameSpeed(GameMode.WEEK);
        assertEquals(GameMode.WEEK, gameMap.currentMode);
    }

    @Test
    public void testCountersUpdate() {
        gameMap.carnivoreCount = 5;
        gameMap.herbivoreCount = 10;
        gameMap.plantCount = 20;
        gameMap.pondCount = 3;
        gameMap.updateCounterLabels();
    }

    @Test
    public void testInitialRoadState() {
        int initialRoads = Road.getAvailableRoads();
        assertTrue("Initial road count should be non-negative", initialRoads >= 0);
    }

    @Test
    public void testAddPurchasedRoads() {
        int initialRoads = Road.getAvailableRoads();
        Road.addPurchasedRoads(gameMap, 5);
        assertEquals(initialRoads + 5, Road.getAvailableRoads());
    }

    @Test
    public void testRemovingNonExistentRoad() {
        Road.addPurchasedRoads(gameMap, 5);
        assertFalse(Road.removeRoad(gameMap, 7, 7));
    }

    @Test
    public void testCannotPlaceRoadOnStartOrEndTile() {
        assertFalse(Road.placeRoad(gameMap, GameMap.START_ROW, GameMap.START_COL));
        assertEquals('S', gameMap.getTerrainMatrix()[GameMap.START_ROW][GameMap.START_COL]);

        assertFalse(Road.placeRoad(gameMap, GameMap.END_ROW, GameMap.END_COL));
        assertEquals('E', gameMap.getTerrainMatrix()[GameMap.END_ROW][GameMap.END_COL]);
    }

    @Test
    public void testAnimalReproduction() {
        gameMap.animalReproduced("herbivore");
        assertEquals(1, gameMap.herbivoreCount);
        assertEquals(1, (int) gameMap.inventory.get("herbivore"));
    }

    @Test
    public void testPlantEaten() {
        gameMap.plantCount = 5;
        gameMap.plantEaten();
        assertEquals(4, gameMap.plantCount);
    }

    @Test
    public void testPlantCountChanges() {
        gameMap.incrementPlantCount();
        assertEquals(1, gameMap.plantCount);

        gameMap.decrementPlantCount();
        assertEquals(0, gameMap.plantCount);
    }

    @Test
    public void testPondCountChanges() {
        gameMap.incrementPondCount();
        assertEquals(1, gameMap.pondCount);

        gameMap.decrementPondCount();
        assertEquals(0, gameMap.pondCount);
    }

    @Test
    public void testAddObjectToTile() {
        gameMap.initObjectImages();
        assertNotNull(gameMap.addObjectToTile(5, 5, "tree"));
        assertNull(gameMap.addObjectToTile(5, 5, "tree"));
    }
    //

    @Test
    public void testGameSpeedChanges() {
        gameMap.setGameSpeed(GameMode.HOUR);
        assertEquals(GameMode.HOUR, gameMap.currentMode);

        gameMap.setGameSpeed(null);
        assertEquals(GameMode.HOUR, gameMap.currentMode);
    }

    @Test
    public void testMoneyUpdates() {
        gameMap.updateMoney(15000);
        assertEquals(15000, gameMap.getMoney());

        gameMap.updateMoney(5000);
        assertEquals(5000, gameMap.getMoney());
    }

    @Test
    public void testInventoryManagement() {
        gameMap.addToInventory("tree", 5);
        assertEquals(5, (int) gameMap.inventory.get("tree"));

        gameMap.buyItem("pond", 3);
        assertEquals(3, (int) gameMap.inventory.get("pond"));
    }

    @Test
    public void testItemRemoval() {
        gameMap.addTestItem(5, 5, "tree");
        gameMap.removeItem("tree", 5, 5);
        assertEquals(0, gameMap.countItemsOnMap("tree"));
    }

    @Test
    public void testCounterLabelUpdates() {
        gameMap.carnivoreCount = 3;
        gameMap.updateCounterLabels();
        assertTrue(gameMap.carnivoresLabel.getText().contains("3"));
    }

    @Test
    public void testMoneyUpdate() {
        gameMap.updateMoney(8000);
        assertEquals(8000, gameMap.getMoney());
    }

    @Test
    public void testBuyItemIncrementsInventory() {
        gameMap.buyItem("herbivore", 3);
        assertEquals(3, gameMap.getInventoryCount("herbivore"));
    }

    @Test
    public void testSellItemFailsWithZeroInventory() {
        assertFalse(gameMap.sellItem("tree", 1));
    }

    @Test
    public void testInventoryTracking() {
        gameMap.addToInventory("pond", 2);
        assertEquals(2, gameMap.getInventoryCount("pond"));
    }

    @Test
    public void testIsValidTile() {
        assertTrue(gameMap.isValidTile(0, 0));
        assertFalse(gameMap.isValidTile(-1, 0));
    }

    @Test
    public void testReturnToInventoryIncrementsCorrectly() {
        gameMap.returnToInventory("herbivore");
        assertEquals(1, gameMap.getInventoryCount("herbivore"));
    }

    @Test
    public void testPlaceFromInventoryDecrementsAndFailsWhenZero() {
        gameMap.addToInventory("tree", 1);
        assertTrue(gameMap.placeFromInventory("tree"));
        assertFalse(gameMap.placeFromInventory("tree"));
    }

    @Test
    public void testRemovePlacedItemDecreasesCount() {
        gameMap.addTestItem(2, 2, "pond");
        assertEquals(1, gameMap.getPlacedCount("pond"));

        int removed = gameMap.removePlacedItem("pond", 1);
        assertEquals(1, removed);
        assertEquals(0, gameMap.getPlacedCount("pond"));
    }

    @Test
    public void testGetTotalAvailableCombinesInventoryAndMap() {
        gameMap.addToInventory("tree", 2);
        gameMap.addTestItem(3, 3, "tree");
        assertEquals(3, gameMap.getTotalAvailable("tree"));
    }

    @Test
    public void testUpdateAnimalCountAddsAndUpdatesInventory() {
        gameMap.updateAnimalCount("carnivore", 2);
        assertEquals(2, gameMap.carnivoreCount);
        assertEquals(2, gameMap.getInventoryCount("carnivore"));
    }

    @Test
    public void testCountItemsOnMapReturnsZeroWhenEmpty() {
        assertEquals(0, gameMap.countItemsOnMap("pond"));
    }

    @Test
    public void testRemoveItemsFromMapReturnsZeroWhenEmpty() {
        assertEquals(0, gameMap.removeItemsFromMap("herbivore", 1));
    }

    @Test
    public void testGettersForRoadMethods() {
        int total = gameMap.getAvailableRoads();
        gameMap.addPurchasedRoads(3);
        gameMap.updateRoadsLeftLabel();
        assertTrue(gameMap.getRoadsLeftLabel().getText().contains("" + (total + 3)));
    }

    @Test
    public void testGetTileSize() {
        assertTrue("Tile size should be non-negative", gameMap.getTileSize() >= 0);
    }

    @Test
    public void testGetTerrainMatrix() {
        assertNotNull(gameMap.getTerrainMatrix());
    }

    @Test
    public void testGetObjectIcons() {
        assertNotNull(gameMap.getObjectIcons());
    }

    @Test
    public void testGetTerrainIcons() {
        assertNotNull(gameMap.getTerrainIcons());
    }

    @Test
    public void testGetGridContainer() {
        assertNotNull(gameMap.getGridContainer());
    }

    @Test
    public void testGetGridLayers() {
        assertNotNull(gameMap.getGridLayers());
    }

    @Test
    public void testGetAnimals() {
        assertNotNull(gameMap.getAnimals());
    }

    @Test
    public void testGetPonds() {
        assertNotNull(gameMap.getPonds());
    }

    @Test
    public void testGetRoadsLeftLabel() {
        assertNotNull(gameMap.getRoadsLeftLabel());
    }

    @Test
    public void testRemoveItemsFromMap() {
        gameMap.removeItemsFromMap("road", 1);
    }

    @Test
    public void testRangerEliminatePredator() {
        gameMap.rangerEliminatePredator("ranger1");
    }

    @Test
    public void testRangerProtectAgainstPoachers() {
        gameMap.rangerProtectAgainstPoachers();
    }

    @Test
    public void testPayRangerSalaries() {
        gameMap.payRangerSalaries();
    }

    @Test
    public void testVerifyComponentCounts() {
        gameMap.verifyComponentCounts();
    }

    @Test
    public void testIncrementDecrementPondCount() {
        int before = gameMap.getPonds().size();
        gameMap.incrementPondCount();
        gameMap.decrementPondCount();
        assertEquals(before, gameMap.getPonds().size());
    }

    @Test
    public void testGameMapConstructorWithValidArgs() {
        assertNotNull(gameMap);
        assertEquals(20, gameMap.getROWS());
        assertEquals(40, gameMap.getCOLS());
    }

    @Test
    public void testContinueRoadPlacementWithNoActivePlacement() {
        int before = gameMap.getAvailableRoads();
        gameMap.continueRoadPlacement();
        assertEquals(before, gameMap.getAvailableRoads());
    }

    @Test
    public void testGetPoachersReturnsNonNullList() {
        assertNotNull(gameMap.getPoachers());
    }

    @Test
    public void testGetVisibleWidthHeight() {
        assertTrue(gameMap.getVisibleWidth() >= 0);
        assertTrue(gameMap.getVisibleHeight() >= 0);
    }

    @Test
    public void testSellItem_ValidIfPlaced() {
        gameMap.addTestItem(5, 5, "tree");
        boolean result = gameMap.sellItem("tree", 1);
        assertTrue(result);
    }

    @Test
    public void testSellItem_Invalid_NotEnoughInventory() {
        gameMap.addToInventory("pond", 1);
        boolean result = gameMap.sellItem("pond", 2);
        assertFalse(result);
        assertEquals(1, gameMap.getInventoryCount("pond"));
    }

    @Test
    public void testSellItem_Invalid_ItemNotPresent() {
        boolean result = gameMap.sellItem("carnivore", 1);
        assertFalse(result);
    }

    @Test
    public void testPlaceItem_Valid() {
        gameMap.addToInventory("tree", 1);
        boolean result = gameMap.placeItem("tree", 4, 4);
        assertTrue(result);
    }

    @Test
    public void testToggleNightMode() {
        gameMap.toggleNightMode(true);
    }

    @Test
    public void testCreateTopPanelDoesNotThrow() {
        assertNotNull(gameMap.createTopPanel());
    }

    @Test
    public void testInitObjectImagesLoadsIcons() {
        Map<String, ImageIcon> icons = gameMap.getObjectIcons();
        assertNotNull(icons);
        assertTrue(icons.containsKey("tree"));
    }

    @Test
    public void testInitializeCountsResetsAll() {
        gameMap.carnivoreCount = 10;
        gameMap.herbivoreCount = 10;
        gameMap.pondCount = 10;
        gameMap.plantCount = 10;

        gameMap.initializeCounts();
        assertEquals(0, gameMap.carnivoreCount);
        assertEquals(0, gameMap.herbivoreCount);
        assertEquals(0, gameMap.pondCount);
        assertEquals(0, gameMap.plantCount);
    }

    @Test
    public void testStyleTopButtonAppliesStyle() {
        JButton button = new JButton("Test");
        gameMap.styleTopButton(button);
        assertNotNull(button.getFont());
    }

    @Test
    public void testUpdateCountersDoesNotCrash() {
        gameMap.updateCounters(); // just make sure it runs
    }

    @Test
    public void testMarkStartAndEndTilesSetsCorrectChars() {
        gameMap.markStartAndEndTiles();
        char[][] terrain = gameMap.getTerrainMatrix();
        assertEquals('S', terrain[GameMap.START_ROW][GameMap.START_COL]);
        assertEquals('E', terrain[GameMap.END_ROW][GameMap.END_COL]);
    }

    @Test
    public void testIsTileExactlyDistanceFromRoadReturnsBoolean() {
        gameMap.getTerrainMatrix()[5][5] = 'R';
        assertTrue(gameMap.isTileExactlyDistanceFromRoad(5, 7, 2));
    }

    @Test
    public void testIncrementDecrementAnimalCount() {
        Animal dummy = new Herbivore(0, 0, new JLabel(), gameMap);
        int before = gameMap.herbivoreCount;
        gameMap.incrementAnimalCount(dummy);
        assertEquals(before + 1, gameMap.herbivoreCount);
        gameMap.decrementAnimalCount(dummy);
        assertEquals(before, gameMap.herbivoreCount);
    }

    @Test
    public void testCreateTopPanelNotNull() {
        assertNotNull(gameMap.createTopPanel());
    }

    @Test
    public void testInitComponentsInitializesGrid() {
        assertNotNull(gameMap.getGridContainer());
        assertNotNull(gameMap.getGridLayers());
    }

    @Test
    public void testPlaceGroupedAnimalsPlacesAnimals() {
        List<Point> tiles = new ArrayList<>();
        tiles.add(new Point(5, 5));
        Point center = new Point(5, 5);
        gameMap.placeGroupedAnimals("herbivore", 1, center, tiles);
        assertFalse(gameMap.getAnimals().isEmpty());
    }

    @Test
    public void testCalculateTileSizeIsNonNegative() {
        assertTrue(gameMap.getTileSize() >= 0);
    }

    @Test
    public void testRemoveObjectFromTileCoordinates() {
        gameMap.removeObjectFromTile(2, 2);
    }

    @Test
    public void testStyleTopButtonDoesNotCrash() {
        JButton button = new JButton("Test");
        gameMap.styleTopButton(button);
        assertNotNull(button.getFont());
    }

    @Test
    public void testStartRoadPlacementEnablesMode() {
        Road.addPurchasedRoads(gameMap, 5);
        gameMap.startRoadPlacement(5);
        assertTrue(Road.isRoadPlacementMode());
    }

    @Test
    public void testCenterViewportOnNoCrash() {
        gameMap.centerViewportOn(10, 10);
    }

    @Test
    public void testScrollToNoCrash() {
        gameMap.scrollTo(10, 10);
    }

    @Test
    public void testInitGameTimerNoCrash() {
        gameMap.initGameTimer();
    }

    @Test
    public void testAdjustGameTimerSpeedHandlesNull() {
        gameMap.adjustGameTimerSpeed(null);
        gameMap.adjustGameTimerSpeed(GameMode.HOUR);
    }

    @Test
    public void testUpdateGameTimersNoCrash() {
        gameMap.updateGameTimers();
    }

    @Test
    public void testPlaceSpecificAnimals() {
        List<Point> tiles = new ArrayList<>();
        tiles.add(new Point(1, 1));
        gameMap.placeSpecificAnimals(tiles);
    }

    @Test
    public void testCenterViewportOn() {
        gameMap.centerViewportOn(5, 5);
    }

    @Test
    public void testScrollTo() {
        gameMap.scrollTo(3, 3);
    }

    @Test
    public void testGetMainPanel() {
        assertNotNull(gameMap.getMainPanel());
    }

    @Test
    public void testSetupRightClickPlacement() {
        gameMap.setupRightClickPlacement();
    }

    ///

    @Test
    public void testStyleTopButton() {
        JButton button = new JButton("Test");
        gameMap.styleTopButton(button);
        assertEquals("Test", button.getText());
    }

    @Test
    public void testSetSelectedItemType() {
        gameMap.setSelectedItemType("pond");
        assertEquals("pond", gameMap.selectedItemType);
    }

    @Test
    public void testCalculateTileSize() {
        gameMap.calculateTileSize();
        assertTrue(gameMap.tileSize > 0);
    }

    @Test
    public void testCreateColorIcon() {
        ImageIcon icon = gameMap.createColorIcon(Color.BLUE, 10, 10);
        assertNotNull(icon);
    }

    @Test
    public void testIncreaseCapital() {
        int oldMoney = gameMap.money;
        gameMap.increaseCapital();
        assertTrue(gameMap.money > oldMoney);
    }

    @Test
    public void testHandleRoadAction() {
        gameMap.tileSize = 32;
        Road.addPurchasedRoads(gameMap, 5);
        MouseEvent e = new MouseEvent(new JLabel(), 0, 0, 0, 64, 64, 1, false);
        gameMap.handleRoadAction(e);
        // No exception = pass
    }

    @Test
    public void testSpawnRanger() {
        // Add dummy icon to prevent NPE in Ranger constructor
        gameMap.getObjectIcons().put("ranger", new ImageIcon(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)));

        gameMap.spawnRanger(5, 5);

        assertTrue(gameMap.rangers.size() > 0);  // or check components if private
    }

    @Test
    public void testRemoveObjectFromTile() throws Exception {
        JLabel label = new JLabel();
        gameMap.getGridLayers()[1][1].add(label);
        gameMap.removeObjectFromTile(label, 1, 1);

        // Give the EDT time to process invokeLater
        Thread.sleep(100);

        assertFalse(Arrays.asList(gameMap.getGridLayers()[1][1].getComponents()).contains(label));
    }

    @Test
    public void testRemovePoacher() {
        Poacher p = new Poacher(2, 2, gameMap);
        gameMap.poachers.add(p);
        gameMap.removePoacher(p);
        assertFalse(gameMap.poachers.contains(p));
    }

}

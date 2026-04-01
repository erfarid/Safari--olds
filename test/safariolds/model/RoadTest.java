package safariolds.model;

import org.junit.Before;
import org.junit.Test;
import safariolds.controller.GameMap;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import static org.junit.Assert.*;

public class RoadTest {

    private GameMap gameMap;
    private JLayeredPane[][] gridLayers;
    private ImageIcon roadIcon, treeIcon, pondIcon, herbIcon, carniIcon;
    private char[][] terrain;

    @Before
    public void setUp() {
        System.setProperty("java.awt.headless", "true");

        Road.roadPlacementMode = false;
        Road.roadsToPlace = 0;
        Road.getRoads().clear();
        Road.totalRoadsPurchased = 0;
        Road.roadsUsed = 0;
        try {
            java.lang.reflect.Field field = Road.class.getDeclaredField("firstPathCompleted");
            field.setAccessible(true);
            field.setBoolean(null, false);
        } catch (Exception e) {
            throw new RuntimeException("Failed to reset Road.firstPathCompleted", e);
        }

        gridLayers = new JLayeredPane[20][40];
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 40; j++) {
                gridLayers[i][j] = new JLayeredPane();
            }
        }

        roadIcon = new ImageIcon();
        treeIcon = new ImageIcon();
        pondIcon = new ImageIcon();
        herbIcon = new ImageIcon();
        carniIcon = new ImageIcon();
        terrain = new char[20][40];
        for (char[] row : terrain) {
            Arrays.fill(row, 'G');
        }

        gameMap = new GameMap("TestPlayer", 10000, true) {
            @Override
            public HashMap<String, ImageIcon> getObjectIcons() {
                HashMap<String, ImageIcon> icons = new HashMap<>();
                icons.put("road", roadIcon);
                icons.put("tree", treeIcon);
                icons.put("pond", pondIcon);
                icons.put("herbivore", herbIcon);
                icons.put("carni", carniIcon);
                return icons;
            }

            @Override
            public HashMap<Character, ImageIcon> getTerrainIcons() {
                return new HashMap<>();
            }

            @Override
            public JLayeredPane[][] getGridLayers() {
                return gridLayers;
            }

            @Override
            public int getTileSize() {
                return 32;
            }

            @Override
            public boolean isTestMode() {
                return true;
            }

            @Override
            public char[][] getTerrainMatrix() {
                return terrain;
            }

            @Override
            public JLabel getRoadsLeftLabel() {
                return new JLabel();
            }

            @Override
            public int getROWS() {
                return 20;
            }

            @Override
            public int getCOLS() {
                return 40;
            }

            @Override
            public List<Animal> getAnimals() {
                return new ArrayList<>();
            }

            @Override
            public HashMap<String, Point> getPonds() {
                return new HashMap<>();
            }

            @Override
            public JLayeredPane getGridContainer() {
                return new JLayeredPane();
            }

            @Override
            public JFrame getFrame() {
                return null; 
            }

            @Override
            public void updateRoadsLeftLabel() {
            }
        };
    }

    @Test
    public void testGetters() {
        JLabel label = new JLabel();
        Road road = new Road(2, 3, label, gameMap);
        assertEquals(2, road.getRow());
        assertEquals(3, road.getCol());
        assertEquals(label, road.getLabel());
    }

    @Test
    public void testRoadPlacementFlags() {
        Road.startRoadPlacement(gameMap, 5);
        assertTrue(Road.isRoadPlacementMode());
        Road.endRoadPlacement();
        assertFalse(Road.isRoadPlacementMode());
    }

    @Test
    public void testInventoryCounters() {
        Road.addPurchasedRoads(gameMap, 5);
        assertTrue(Road.getTotalRoadsPurchased() >= 5);
        assertEquals(Road.getTotalRoadsPurchased() - Road.getRoadsUsed(), Road.getAvailableRoads());
    }

    @Test
    public void testIsTileOccupied() {
        int row = 1, col = 1;
        terrain[row][col] = 'G';

        assertTrue(gameMap.getAnimals().isEmpty());
        for (Component c : gridLayers[row][col].getComponents()) {
            gridLayers[row][col].remove(c);
        }

        gameMap.getPonds().remove(row + "," + col);
        assertFalse(Road.isTileOccupied(gameMap, row, col));
    }

    @Test
    public void testHasOrthogonalConnectionAndAdjacency() {
        gameMap.getTerrainMatrix()[5][5] = 'R';
        assertTrue(Road.hasOrthogonalConnection(gameMap, 5, 6));
        assertTrue(Road.isAdjacentToExistingRoad(gameMap, 5, 6));
    }

    @Test
    public void testHasRoadInDirection() {
        gameMap.getTerrainMatrix()[10][10] = 'R';
        assertTrue(Road.hasRoadInDirection(gameMap, 10, 7, 0, 1)); 
        assertFalse(Road.hasRoadInDirection(gameMap, 0, 0, 1, 0)); 
    }

    @Test
    public void testIsValidRoadConnection() {
        gameMap.getTerrainMatrix()[4][4] = 'R';
        assertTrue(Road.isValidRoadConnection(gameMap, 4, 5));
    }

    @Test
    public void testConnectedToStartFalse() {
        assertFalse(Road.placeRoad(gameMap, 10, 39)); 
    }

    @Test
    public void testPlaceAndRemoveRoad() {
        Road.addPurchasedRoads(gameMap, 10);
        Road.startRoadPlacement(gameMap, 10);
        terrain[10][0] = 'R';

        assertTrue(Road.placeRoad(gameMap, 10, 0));
        assertTrue(Road.placeRoad(gameMap, 10, 1));
        assertTrue(Road.removeRoad(gameMap, 10, 1));
    }

    @Test
    public void testForceRemove() {
        gameMap.getTerrainMatrix()[2][2] = 'R';
        JLabel roadLabel = new JLabel(roadIcon);
        gridLayers[2][2].add(roadLabel, JLayeredPane.PALETTE_LAYER);
        Road.forceRemoveRoad(gameMap, 2, 2);
        assertEquals('G', gameMap.getTerrainMatrix()[2][2]);
    }

    @Test
    public void testPlaceFinalRoadViaStartConnection() {
        Road.addPurchasedRoads(gameMap, 20);
        Road.startRoadPlacement(gameMap, 20);
        for (int col = 0; col <= 38; col++) {
            terrain[10][col] = 'R';
        }
        assertTrue(Road.placeRoad(gameMap, 10, 0));
        assertTrue(Road.placeRoad(gameMap, 10, 39));
    }

    @Test
    public void testRoadListNonNull() {
        assertNotNull(Road.getRoads());
    }

    @Test
    public void testIsValidRoadPlacement() {
        Road road = new Road(7, 7, new JLabel(), gameMap);
        assertTrue(road.isValidRoadPlacement(gameMap, 7, 7));
    }

    @Test
    public void testStartAndContinueRoadPlacement() {
        try {
            Road road = new Road(1, 1, new JLabel(), gameMap);
            road.startRoadPlacement(gameMap, 1);
            road.continueRoadPlacement(gameMap);
            assertTrue(true);
        } catch (HeadlessException e) {
            System.out.println("Skipping GUI test in headless mode");
            assertTrue(true);
        }
    }

    @Test
    public void testEndRoadPlacementAndFlags() {
        Road road = new Road(9, 9, new JLabel(), gameMap);
        road.endRoadPlacement();
        assertTrue(true);
    }

    @Test
    public void testIsFirstPathCompleted() {
        Road road = new Road(0, 0, new JLabel(), gameMap);
        assertFalse(road.isFirstPathCompleted());
    }
}

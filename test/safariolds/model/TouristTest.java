package safariolds.model;

import org.junit.Before;
import org.junit.Test;
import safariolds.controller.GameMap;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import static org.junit.Assert.*;

public class TouristTest {

    private GameMap gameMap;
    private JLayeredPane[][] gridLayers;
    private JLayeredPane gridContainer;
    private HashMap<Character, ImageIcon> terrainIcons;
    private List<Animal> animals;
    private HashMap<String, Point> ponds;
    private char[][] terrain;
    private Jeep jeep;

    @Before
    public void setUp() {
        System.setProperty("java.awt.headless", "true");

        gridContainer = new JLayeredPane();
        gridContainer.setLayout(null);
        gridContainer.setPreferredSize(new Dimension(640, 640));

        gridLayers = new JLayeredPane[20][20];
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                gridLayers[i][j] = new JLayeredPane();
            }
        }

        terrain = new char[20][20];
        for (char[] row : terrain) {
            Arrays.fill(row, 'G');
        }

        animals = new ArrayList<>();
        ponds = new HashMap<>();
        terrainIcons = new HashMap<>();
        terrainIcons.put('G', new ImageIcon());

        gameMap = new GameMap("TestMap", 10000, true) {
            @Override
            public int getTileSize() {
                return 32;
            }

            @Override
            public JLayeredPane[][] getGridLayers() {
                return gridLayers;
            }

            @Override
            public JLayeredPane getGridContainer() {
                return gridContainer;
            }

            @Override
            public int getROWS() {
                return 20;
            }

            @Override
            public int getCOLS() {
                return 20;
            }

            @Override
            public char[][] getTerrainMatrix() {
                return terrain;
            }

            @Override
            public HashMap<String, Point> getPonds() {
                return ponds;
            }

            @Override
            public List<Animal> getAnimals() {
                return animals;
            }

            @Override
            public HashMap<String, ImageIcon> getObjectIcons() {
                HashMap<String, ImageIcon> icons = new HashMap<>();
                icons.put("jeep", new ImageIcon());
                icons.put("tourist", new ImageIcon());
                icons.put("poacher", new ImageIcon());
                icons.put("shot", new ImageIcon());
                return icons;
            }

            @Override
            public HashMap<Character, ImageIcon> getTerrainIcons() {
                return terrainIcons;
            }
        };

        jeep = new Jeep(5, 5, gameMap);
    }

    @Test
    public void testConstructorAndLabelNotNull() {
        Tourist tourist = new Tourist(jeep, gameMap, 0);
        assertNotNull(tourist.getLabel());
    }

    @Test
    public void testUpdatePositionWithinBounds() {
        Tourist tourist = new Tourist(jeep, gameMap, 0);
        tourist.updatePosition();
        Point pos = tourist.getGridPosition();
        assertTrue(pos.y >= 0 && pos.y < 20);
    }

    @Test
    public void testIsValidPositionTrue() {
        Tourist tourist = new Tourist(jeep, gameMap, 0);
        assertTrue(tourist.isValidPosition());
    }

    @Test
    public void testIsValidPositionFalseOutOfBounds() {
        terrain[0][0] = 'G';
        Tourist tourist = new Tourist(jeep, gameMap, 0);
        tourist.getLabel().setLocation(-32, -32);
        assertFalse(tourist.isValidPosition());
    }

    @Test
    public void testIsValidPositionFalseDueToPond() {
        Point pos = new Point(5, 6);
        ponds.put("6,5", pos);
        Tourist tourist = new Tourist(jeep, gameMap, 2);
        tourist.updatePosition();
        assertFalse(tourist.isValidPosition());
    }

    @Test
    public void testSetVisibleValid() {
        Tourist tourist = new Tourist(jeep, gameMap, 0);
        tourist.setVisible(true);
        assertTrue(tourist.getLabel().isVisible());
    }

    @Test
    public void testSetVisibleInvalid() {
        terrain[5][3] = 'R'; // simulate road
        Tourist tourist = new Tourist(jeep, gameMap, 0);
        tourist.getLabel().setLocation(3 * 32, 5 * 32);
        tourist.setVisible(true);
        assertFalse(tourist.getLabel().isVisible());
    }

    @Test
    public void testSetForcedVisible() {
        Tourist tourist = new Tourist(jeep, gameMap, 0);
        tourist.setForcedVisible(true);
        assertTrue(tourist.getLabel().isVisible());
    }

    @Test
    public void testRemove() {
        Tourist tourist = new Tourist(jeep, gameMap, 0);
        tourist.setForcedVisible(true);
        tourist.remove();
        assertNull(tourist.getLabel().getParent());
    }

    @Test
    public void testLoadTouristImageFallback() {
        Tourist tourist = new Tourist(jeep, gameMap, 0);
        assertNotNull(tourist.getLabel().getIcon());
    }

    @Test
    public void testGridPositionAccuracy() {
        Tourist tourist = new Tourist(jeep, gameMap, 1);
        Point pos = tourist.getGridPosition();
        assertTrue(pos.x >= 0 && pos.y >= 0);
    }

    @Test
    public void testIsPositionValid() {
        Tourist tourist = new Tourist(jeep, gameMap, 0);

        assertTrue(tourist.isPositionValid(0, 0));
        assertTrue(tourist.isPositionValid(19, 19)); 

        assertFalse(tourist.isPositionValid(-1, 5)); 
        assertFalse(tourist.isPositionValid(20, 10)); 
        assertFalse(tourist.isPositionValid(10, 20)); 
        assertFalse(tourist.isPositionValid(10, -1)); 
    }

}

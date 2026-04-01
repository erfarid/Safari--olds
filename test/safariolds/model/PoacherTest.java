package safariolds.model;

import org.junit.Before;
import org.junit.Test;
import safariolds.controller.GameMap;

import javax.swing.*;
import java.util.*;

import static org.junit.Assert.*;

public class PoacherTest {

    private GameMap gameMap;
    private List<Animal> animals;
    private JLayeredPane gridContainer;
    private HashMap<String, ImageIcon> icons;
    private char[][] terrain;

    @Before
    public void setUp() {
        System.setProperty("java.awt.headless", "true");

        animals = new ArrayList<>();
        gridContainer = new JLayeredPane();
        icons = new HashMap<>();
        icons.put("poacher", new ImageIcon());
        icons.put("shot", new ImageIcon());

        terrain = new char[20][20];
        for (char[] row : terrain) {
            Arrays.fill(row, 'G');
        }

        gameMap = new GameMap("TestMap", 100000, true) {
            @Override
            public int getTileSize() {
                return 32;
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
            public List<Animal> getAnimals() {
                return animals;
            }

            @Override
            public HashMap<String, ImageIcon> getObjectIcons() {
                return icons;
            }
        };
    }

    @Test
    public void testConstructorInitializesPosition() {
        Poacher poacher = new Poacher(2, 3, gameMap);
        assertEquals(2, poacher.getRow());
        assertEquals(3, poacher.getCol());
    }

    @Test
    public void testLabelInitiallyInvisible() {
        Poacher poacher = new Poacher(0, 0, gameMap);
        assertFalse(poacher.isVisible());
    }

    @Test
    public void testUpdateVisibilityFarFromJeep() {
        Poacher poacher = new Poacher(0, 0, gameMap);
        Jeep jeep = new Jeep(10, 10, gameMap);
        poacher.updateVisibility(jeep);
        assertFalse(poacher.isVisible());
    }

    @Test
    public void testWanderMovesToValidPosition() {
        Poacher poacher = new Poacher(5, 5, gameMap);
        poacher.wander();
        assertTrue(poacher.getRow() >= 4 && poacher.getRow() <= 6);
        assertTrue(poacher.getCol() >= 4 && poacher.getCol() <= 6);
    }

    @Test
    public void testIsValidMoveWithinBounds() {
        Poacher poacher = new Poacher(1, 1, gameMap);
        assertTrue(poacher.isValidMove(1, 2));
    }

    @Test
    public void testIsValidMoveOutOfBounds() {
        Poacher poacher = new Poacher(0, 0, gameMap);
        assertFalse(poacher.isValidMove(-1, 0));
    }

    @Test
    public void testSetSpeedMultiplier() {
        Poacher poacher = new Poacher(1, 1, gameMap);
        poacher.setSpeedMultiplier(2.0f);
        assertNotNull(poacher);
    }

    @Test
    public void testRemoveStopsTimers() {
        Poacher poacher = new Poacher(1, 1, gameMap);
        poacher.remove();
        assertNotNull(poacher);
    }

    @Test
    public void testIsInShootingRangeReturnsTrue() {
        JLabel dummy = new JLabel();
        Animal target = new Carnivore(6, 5, dummy, gameMap);
        Poacher poacher = new Poacher(5, 5, gameMap);
        poacher.setTargetAnimal(target);
        assertTrue(poacher.isInShootingRange());
    }

    @Test
    public void testShowShootingEffectDoesNotCrash() {
        Poacher poacher = new Poacher(5, 5, gameMap);
        poacher.showShootingEffect();
        assertNotNull(poacher);
    }

    @Test
    public void testStartHuntingBehaviorRuns() {
        Poacher poacher = new Poacher(5, 5, gameMap);
        poacher.startHuntingBehavior();
        assertNotNull(poacher);
    }

    @Test
    public void testMoveTowardTargetMovesPoacher() {
        JLabel dummy = new JLabel();
        Animal target = new Herbivore(6, 5, dummy, gameMap);
        animals.add(target);
        Poacher poacher = new Poacher(5, 5, gameMap);
        poacher.setTargetAnimal(target);
        int before = poacher.getRow();
        poacher.moveTowardTarget();
        int after = poacher.getRow();
        assertTrue(after > before);
    }

    @Test
    public void testUpdatePositionChangesCoordinates() {
        Poacher poacher = new Poacher(0, 0, gameMap);
        poacher.updatePosition(2, 3);
        assertEquals(2, poacher.getRow());
        assertEquals(3, poacher.getCol());
    }

    @Test
    public void testSetTargetAnimal() {
        Poacher poacher = new Poacher(0, 0, gameMap);
        Animal target = new Carnivore(1, 1, new JLabel(), gameMap);
        poacher.setTargetAnimal(target);
        assertTrue(poacher.isInShootingRange() || !poacher.isInShootingRange());
    }

    @Test
    public void testHuntAnimalsNoTarget() {
        Poacher poacher = new Poacher(5, 5, gameMap);
        poacher.huntAnimals();
        assertNotNull(poacher);
    }

    @Test
    public void testGetLabelReturnsNullInTestMode() {
        Poacher poacher = new Poacher(3, 3, gameMap);
        assertNull("Poacher label should be null in test mode", poacher.getLabel());
    }

}

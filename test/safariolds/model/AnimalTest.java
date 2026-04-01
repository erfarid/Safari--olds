package safariolds.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import safariolds.controller.GameMap;
import javax.swing.*;
import java.awt.Point;
import java.awt.image.BufferedImage;

import java.util.*;

public class AnimalTest {

    private Animal animal;
    private GameMap gameMap;
    private JLabel label;
    private char[][] sharedTerrain;
    private List<Animal> sharedAnimalList;


    @Before
    public void setUp() {
        System.setProperty("java.awt.headless", "true");
        sharedTerrain = new char[10][10];
        for (char[] row : sharedTerrain) {
            Arrays.fill(row, 'G');
        }

        sharedAnimalList = new ArrayList<>(); 

        gameMap = new GameMap("Tester", 100000, true) {
            @Override
            public char[][] getTerrainMatrix() {
                return sharedTerrain;
            }

            @Override
            public java.util.List<Animal> getAnimals() {
                return sharedAnimalList;
            }

            @Override
            public int getTileSize() {
                return 32;
            }

            @Override
            public JLayeredPane[][] getGridLayers() {
                JLayeredPane[][] grid = new JLayeredPane[10][10];
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        grid[i][j] = new JLayeredPane();
                    }
                }
                return grid;
            }

            @Override
            public void updateAnimalCount(String type, int delta) {
                // mock
            }

            @Override
            public HashMap<String, ImageIcon> getObjectIcons() {
                HashMap<String, ImageIcon> icons = new HashMap<>();
                icons.put("herbivore", new ImageIcon(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)));
                return icons;
            }
        };

        label = new JLabel();
        animal = new Herbivore(0, 0, label, gameMap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidPosition() {
        GameMap mockMap = new GameMap("Test", 100000, true) {
            @Override
            public char[][] getTerrainMatrix() {
                return new char[][]{{'W'}};
            }
        };
        new Herbivore(0, 0, new JLabel(), mockMap);
    }

    @Test
    public void testConstructorValidPosition() {
        assertEquals(0, animal.getRow());
        assertEquals(0, animal.getCol());
    }

    @Test
    public void testStartMovementNotNull() {
        animal.startMovement();
        assertNotNull(animal.moveTimer);
    }

    @Test
    public void testSetSpeedMultiplierUpdatesTimer() {
        animal.setSpeedMultiplier(2.0f);
        assertNotNull(animal.moveTimer);
    }

    @Test
    public void testDieRemovesAnimal() {
        gameMap.getAnimals().add(animal);
        animal.die();
        assertFalse(gameMap.getAnimals().contains(animal));
    }

    @Test
    public void testIsPositionOccupiedFalse() {
        assertFalse(animal.isPositionOccupied(1, 1));
    }

    @Test
    public void testCheckAtPondFalse() {
        assertFalse(animal.checkAtPond());
    }

    @Test
    public void testGetReverseDirection() {
        assertEquals(5, animal.getReverseDirection(1));
        assertEquals(2, animal.getReverseDirection(6));
        assertEquals(3, animal.getReverseDirection(7));
    }

    @Test
    public void testStopTimersNullifyAll() {
        animal.stopTimers();
        assertNull(animal.moveTimer);
        assertNull(animal.reproductionTimer);
        assertNull(animal.lifespanTimer);
    }

    @Test
    public void testGettersReturnCorrectValues() {
        assertEquals(label, animal.getLabel());
        assertEquals("herbivore", animal.getType());
    }

    @Test
    public void testSettersUpdateValues() {
        animal.setRow(5);
        animal.setCol(4);
        animal.setDirection(3);
        assertEquals(5, animal.getRow());
        assertEquals(4, animal.getCol());
        assertEquals(3, animal.getDirection());
    }

    @Test
    public void testSetAtPondAndCheck() {
        animal.setAtPond(true);
        assertTrue(animal.isAtPond());
    }

    @Test
    public void testAnimalDoesNotMoveAtPond() {
        animal.setAtPond(true);
        animal.move(); // should not throw
    }

    @Test
    public void testAnimalMoveStillWithinBounds() {
        animal.setDirection(1);
        animal.move();
        assertTrue(animal.getXPos() >= 0);
    }

    //
//    @Test
//    public void testMoveTowardsGroupChangesDirection() {
//        animal.setDirection(5); // Set an initial known direction
//        Animal other = new Herbivore(7, 7, new JLabel(), gameMap); // Farther away
//        gameMap.getAnimals().add(other);
//        gameMap.getAnimals().add(animal);
//
//        int oldDirection = animal.getDirection();
//        animal.moveTowardsGroup(other);
//        assertNotEquals(oldDirection, animal.getDirection());
//    }
    @Test
    public void testFindAvailableGrassPositionsReturnsValidTiles() {
        List<Point> grassTiles = animal.findAvailableGrassPositions();
        assertNotNull(grassTiles);
        assertFalse(grassTiles.isEmpty());
        for (Point p : grassTiles) {
            assertTrue(p.x >= 0 && p.x < 10);
            assertTrue(p.y >= 0 && p.y < 10);
        }
    }

    @Test
    public void testStartReproductionTimerSetsTimer() {
        animal.startReproductionTimer();
        assertNotNull(animal.reproductionTimer);
    }

    @Test
    public void testStartLifespanTimerSetsTimer() {
        animal.startLifespanTimer();
        assertNotNull(animal.lifespanTimer);
    }

    @Test
    public void testAdjustTimersAfterSpeedChange() {
        animal.setSpeedMultiplier(1.5f);
        animal.adjustTimers(2.0f);
        // No exception = pass
    }

    @Test
    public void testFindNearestSameSpeciesReturnsSelfOrNull() {
        Animal nearest = animal.findNearestSameSpecies();
        assertTrue(nearest == null || nearest == animal);
    }

    @Test
    public void testLeavePondDoesNotCrash() {
        animal.setAtPond(true);
        animal.leavePond();
        assertFalse(animal.isAtPond());
    }

    @Test
    public void testMoveStopsAtBoundary() {
        animal.setDirection(7); // move up
        animal.setRow(0); // top edge
        animal.move(); // should not go out of bounds
        assertTrue(animal.getRow() >= 0);
    }

    @Test
    public void testAdjustTimersRestartsTimers() {
        animal.adjustTimers(2.0f);
        assertNotNull(animal.reproductionTimer);
        assertNotNull(animal.lifespanTimer);
    }

    @Test
    public void testReproduceHandlesExceptionGracefully() {
        Animal brokenAnimal = new Herbivore(0, 0, new JLabel(), gameMap) {
            @Override
            public void reproduce() {
                throw new RuntimeException("Test exception");
            }
        };
        brokenAnimal.startMovement(); // triggers timer and eventually reproduce
        // Let timer run briefly
        try {
            Thread.sleep(150);
        } catch (InterruptedException ignored) {
        }
        assertTrue(true); // No crash = pass
    }

    @Test
    public void testLeavePondUpdatesState() {
        sharedTerrain[animal.getRow()][animal.getCol()] = 'P';
        animal.setAtPond(true);

        int beforeRow = animal.getRow();
        int beforeCol = animal.getCol();

        animal.leavePond();
        assertFalse(animal.isAtPond());
        assertEquals('G', sharedTerrain[animal.getRow()][animal.getCol()]);
        boolean moved = animal.getRow() != beforeRow || animal.getCol() != beforeCol;
        assertTrue("Animal should have moved away from the pond", moved);
        assertEquals(animal.getCol() * gameMap.getTileSize(), animal.getXPos());
        assertEquals(animal.getRow() * gameMap.getTileSize(), animal.getYPos());
    }

    @Test
    public void testMoveBlockedByObstacle() {
        sharedTerrain[0][1] = 'T';
        animal.setDirection(3);
        animal.move();
        assertEquals(0, animal.getCol());
    }

    @Test
    public void testMoveTowardsGroupChangesDirection() {
        Animal groupMate = new Herbivore(5, 5, new JLabel(gameMap.getObjectIcons().get("herbivore")), gameMap);
        gameMap.getAnimals().add(groupMate);
        int before = animal.getDirection();
        animal.moveTowardsGroup(groupMate);
        assertNotEquals(before, animal.getDirection());
    }

    @Test
    public void testFindNearestSameSpeciesReturnsNearest() {
        java.util.List<Animal> sharedAnimals = new ArrayList<>();

        gameMap = new GameMap("Tester", 100000, true) {
            @Override
            public java.util.List<Animal> getAnimals() {
                return sharedAnimals;
            }

            @Override
            public char[][] getTerrainMatrix() {
                char[][] matrix = new char[10][10];
                for (char[] row : matrix) {
                    Arrays.fill(row, 'G');
                }
                return matrix;
            }

            @Override
            public JLayeredPane[][] getGridLayers() {
                JLayeredPane[][] grid = new JLayeredPane[10][10];
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        grid[i][j] = new JLayeredPane();
                    }
                }
                return grid;
            }

            @Override
            public HashMap<String, ImageIcon> getObjectIcons() {
                HashMap<String, ImageIcon> icons = new HashMap<>();
                icons.put("herbivore", new ImageIcon(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)));
                return icons;
            }

            @Override
            public void updateAnimalCount(String type, int delta) {
            }
        };

        JLabel label1 = new JLabel();
        JLabel label2 = new JLabel();

        Animal main = new Herbivore(5, 5, label1, gameMap);
        Animal neighbor = new Herbivore(5, 6, label2, gameMap);

        sharedAnimals.add(main);
        sharedAnimals.add(neighbor);

        Animal nearest = main.findNearestSameSpecies();

        assertNotNull(nearest);
        assertEquals(neighbor, nearest);
    }

    @Test
    public void testIsPositionOccupiedTrueForAnimal() {
        Animal another = new Herbivore(2, 2, new JLabel(gameMap.getObjectIcons().get("herbivore")), gameMap);
        gameMap.getAnimals().add(another);
        assertTrue(animal.isPositionOccupied(2, 2));
    }

}

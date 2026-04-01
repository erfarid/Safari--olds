package safariolds.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import safariolds.controller.GameMap;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class CarnivoreTest {
    private Carnivore carnivore;
    private Herbivore herbivore;
    private JLabel carnivoreLabel;
    private JLabel herbivoreLabel;
    
    private GameMap gameMap;
    private JPanel gridContainer;

    @Before
    public void setUp() {
        System.setProperty("java.awt.headless", "true");

       
        gridContainer = new JPanel();

        gameMap = new GameMap("Tester", 100000, true) {
            @Override
            public char[][] getTerrainMatrix() {
                char[][] mock = new char[10][10];
                for (char[] row : mock) Arrays.fill(row, 'G');
                return mock;
            }

           

            @Override
            public int getTileSize() {
                return 32;
            }

         
            @Override
            public void updateAnimalCount(String type, int delta) {
                // mock
            }
        };

        carnivoreLabel = new JLabel();
        carnivore = new Carnivore(1, 1, carnivoreLabel, gameMap);
        carnivore.setXPos(64);
        carnivore.setYPos(64);
    }

    @Test
    public void testConstructorSetsType() {
        assertEquals("carni", carnivore.getType());
    }

    @Test
    public void testPerformSpecificBehavior_NoHerbivoreNearby() {
        carnivore.performSpecificBehavior();
        assertTrue(gameMap.getAnimals().isEmpty());
    }

    @Test
    public void testPerformSpecificBehavior_EatsNearbyHerbivore() {
        herbivoreLabel = new JLabel();
        herbivore = new Herbivore(1, 1, herbivoreLabel, gameMap);
        herbivore.setXPos(65);
        herbivore.setYPos(65);

        gameMap.getAnimals().add(carnivore);
        gameMap.getAnimals().add(herbivore);

        gridContainer.add(herbivoreLabel);
        carnivore.performSpecificBehavior();

        assertFalse(gameMap.getAnimals().contains(herbivore));
    }

    @Test
    public void testCarnivoreDoesNotCrashWithoutPrey() {
        gameMap.getAnimals().add(carnivore);
        carnivore.performSpecificBehavior();
        assertTrue(gameMap.getAnimals().contains(carnivore));
    }

    @Test
    public void testEatenHerbivoreTimersAreStopped() {
        herbivoreLabel = new JLabel();
        herbivore = new Herbivore(1, 1, herbivoreLabel, gameMap);
        herbivore.setXPos(65);
        herbivore.setYPos(65);

        herbivore.startMovement();
        assertNotNull(herbivore.moveTimer);

        gameMap.getAnimals().add(carnivore);
        gameMap.getAnimals().add(herbivore);

        gridContainer.add(herbivoreLabel);
        carnivore.performSpecificBehavior();

        assertNull(herbivore.moveTimer);
    }
}
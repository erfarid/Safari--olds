package safariolds.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import safariolds.controller.GameMap;

import javax.swing.*;
import java.awt.Component;
import java.util.List;
import java.util.*;
import java.util.HashMap;
import java.util.Map;

public class HerbivoreTest {

    private GameMap gameMap;
    private List<Animal> animalList;
    private JLayeredPane[][] gridLayers;

    @Before
    public void setUp() {
        System.setProperty("java.awt.headless", "true");

        animalList = new ArrayList<>();
        gridLayers = new JLayeredPane[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                gridLayers[i][j] = new JLayeredPane();
            }
        }

        gameMap = new GameMap("TestPlayer", 100000, true) {
            @Override
            public char[][] getTerrainMatrix() {
                char[][] matrix = new char[10][10];
                for (char[] row : matrix) {
                    java.util.Arrays.fill(row, 'G');
                }
                return matrix;
            }

            @Override
            public List<Animal> getAnimals() {
                return animalList;
            }

            @Override
            public int getTileSize() {
                return 32;
            }

            @Override
            public JLayeredPane[][] getGridLayers() {
                return gridLayers;
            }

            @Override
            public void updateAnimalCount(String type, int delta) {
            }
        };
    }

    @Test
    public void testCarnivoreEatsNearbyHerbivore() {
        JLabel carnLabel = new JLabel();
        Carnivore carnivore = new Carnivore(1, 1, carnLabel, gameMap);

        JLabel herbLabel = new JLabel();
        Herbivore herbivore = new Herbivore(1, 1, herbLabel, gameMap);
        animalList.add(herbivore);

        carnivore.performSpecificBehavior();
        assertFalse(animalList.contains(herbivore));
    }

    @Test
    public void testCarnivoreDoesNotEatDistantHerbivore() {
        JLabel carnLabel = new JLabel();
        Carnivore carnivore = new Carnivore(1, 1, carnLabel, gameMap);

        JLabel herbLabel = new JLabel();
        Herbivore herbivore = new Herbivore(5, 5, herbLabel, gameMap);
        animalList.add(herbivore);

        carnivore.performSpecificBehavior();
        assertTrue(animalList.contains(herbivore));
    }

    @Test
    public void testHerbivoreDoesNotEatWhenNoTree() {
        JLabel herbLabel = new JLabel();
        Herbivore herbivore = new Herbivore(0, 0, herbLabel, gameMap);

        assertEquals(0, gridLayers[0][0].getComponentCount());
        herbivore.performSpecificBehavior();
        assertEquals(0, gridLayers[0][0].getComponentCount());
    }

    @Test
    public void testCarnivoreOnlyEatsOneHerbivore() {
        JLabel carnLabel = new JLabel();
        Carnivore carnivore = new Carnivore(2, 2, carnLabel, gameMap);

        Herbivore herb1 = new Herbivore(2, 2, new JLabel(), gameMap);
        Herbivore herb2 = new Herbivore(2, 2, new JLabel(), gameMap);

        animalList.add(herb1);
        animalList.add(herb2);

        carnivore.performSpecificBehavior();
        assertEquals(1, animalList.size());
    }
}

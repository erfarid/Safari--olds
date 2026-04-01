package safariolds.model;

import org.junit.Before;
import org.junit.Test;
import safariolds.controller.GameMap;
import javax.swing.JLabel;
import static org.junit.Assert.*;

public class RangerTest {

    private GameMap testMap;
    private Ranger ranger;

    public RangerTest() {
    }

    @Before
    public void setUp() {
        System.setProperty("java.awt.headless", "true");
        testMap = new GameMap("Tester", 10000, true); // testMode = true
        ranger = new Ranger(5, 5, testMap);
    }

    @Test
    public void testEliminatePredator() {
        JLabel dummyLabel = new JLabel(); // safe placeholder
        Carnivore c = new Carnivore(6, 5, dummyLabel, testMap); // match constructor
        testMap.getAnimals().add(c);
        int initialMoney = testMap.getMoney();

        ranger.eliminatePredator("carnivore");

        assertFalse(testMap.getAnimals().contains(c));
        assertEquals(initialMoney + 500, testMap.getMoney());
    }

    @Test
    public void testRangerProtectAgainstPoachersFromMap() {
        Ranger ranger = new Ranger(5, 5, testMap);
        testMap.getRangers().add(ranger);

        Poacher poacher = new Poacher(6, 5, testMap);
        testMap.getPoachers().add(poacher);

        int initialMoney = testMap.getMoney();

        testMap.rangerProtectAgainstPoachers();

        assertFalse(testMap.getPoachers().contains(poacher));
        assertEquals(initialMoney + 500, testMap.getMoney());
    }

    @Test
    public void testIsWithinRadius() {
        assertTrue(ranger.isWithinRadius(6, 5)); // 1 step away
        assertFalse(ranger.isWithinRadius(0, 0)); // out of radius
    }

    @Test
    public void testMoveTo() {
        ranger.moveTo(3, 3);
        assertEquals(3, ranger.getRow());
        assertEquals(3, ranger.getCol());
    }

    @Test
    public void testAfterMove() {
        ranger.afterMove(); // Should not crash
    }

    @Test
    public void testMoveRandomly() {
        ranger.moveRandomly();
        int row = ranger.getRow();
        int col = ranger.getCol();
        assertTrue(row >= 0 && row < 10);
        assertTrue(col >= 0 && col < 10);
    }

    @Test
    public void testPaySalary() {
        testMap.updateMoney(2000);
        assertTrue(ranger.paySalary());
        assertEquals(1000, testMap.getMoney());

        testMap.updateMoney(500);
        assertFalse(ranger.paySalary());
        assertEquals(500, testMap.getMoney());
    }

    @Test
    public void testGetRow() {
        assertEquals(5, ranger.getRow());
    }

    @Test
    public void testGetCol() {
        assertEquals(5, ranger.getCol());
    }

    @Test
    public void testGetSalary() {
        assertEquals(1000, ranger.getSalary());
    }
}

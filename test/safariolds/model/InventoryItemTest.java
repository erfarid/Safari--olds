package safariolds.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class InventoryItemTest {

    private InventoryItem item;

    @Before
    public void setUp() {
        item = new InventoryItem("tree", 2, 5);
    }

    @Test
    public void testGetType() {
        assertEquals("tree", item.getType());
    }

    @Test
    public void testGetCount() {
        assertEquals(2, item.getCount());
    }

    @Test
    public void testGetMaxCount() {
        assertEquals(5, item.getMaxCount());
    }

    @Test
    public void testIncrement() {
        item.increment();
        assertEquals(3, item.getCount());
    }

    @Test
    public void testDecrementWhenAboveZero() {
        item.decrement();
        assertEquals(1, item.getCount());
    }

    @Test
    public void testDecrementDoesNotGoNegative() {
        item = new InventoryItem("pond", 0, 3);
        item.decrement();
        assertEquals(0, item.getCount());
    }

    @Test
    public void testCanPlaceTrue() {
        assertTrue(item.canPlace());
    }

    @Test
    public void testCanPlaceFalseWhenAtMax() {
        item = new InventoryItem("carnivore", 3, 3);
        assertFalse(item.canPlace());
    }
}

package safariolds.controller;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


public class StateTest {
    
    // Mock listener for testing
    private static class TestListener implements State.SpeedListener {
        public GameMode lastMode;
        public int callCount = 0;
        
        @Override
        public void speedChanged(GameMode newMode) {
            lastMode = newMode;
            callCount++;
        }
    }

    @Before
    public void setUp() {
        // Reset state before each test
        State.setMode(GameMode.DAY);
        StateTest.TestListener listener = new StateTest.TestListener();
        State.removeListener(listener); // Ensure clean state
    }

    @Test
    public void testInitialModeIsDay() {
        assertEquals(GameMode.DAY, State.getMode());
    }

    @Test
    public void testSetModeChangesCurrentMode() {
        State.setMode(GameMode.WEEK);
        assertEquals(GameMode.WEEK, State.getMode());
    }

    @Test
    public void testSetModeSameValueDoesNotNotify() {
        TestListener listener = new TestListener();
        State.addListener(listener);
        
        State.setMode(GameMode.DAY); // Same as current
        assertEquals(0, listener.callCount);
    }

    @Test
    public void testSetModeDifferentValueNotifiesListeners() {
        TestListener listener = new TestListener();
        State.addListener(listener);
        
        State.setMode(GameMode.WEEK);
        assertEquals(1, listener.callCount);
        assertEquals(GameMode.WEEK, listener.lastMode);
    }

    @Test
    public void testAddListenerReceivesFutureChanges() {
        TestListener listener = new TestListener();
        State.addListener(listener);
        
        State.setMode(GameMode.HOUR);
        assertEquals(1, listener.callCount);
    }

    @Test
    public void testRemoveListenerStopsNotifications() {
        TestListener listener = new TestListener();
        State.addListener(listener);
        State.removeListener(listener);
        
        State.setMode(GameMode.WEEK);
        assertEquals(0, listener.callCount);
    }

    @Test
    public void testMultipleListenersAllGetNotified() {
        TestListener listener1 = new TestListener();
        TestListener listener2 = new TestListener();
        State.addListener(listener1);
        State.addListener(listener2);
        
        State.setMode(GameMode.HOUR);
        assertEquals(1, listener1.callCount);
        assertEquals(1, listener2.callCount);
    }

    @Test
    public void testListenerOnlyReceivesLatestMode() {
        TestListener listener = new TestListener();
        State.addListener(listener);
        
        State.setMode(GameMode.HOUR);
        State.setMode(GameMode.WEEK);
        
        assertEquals(2, listener.callCount);
        assertEquals(GameMode.WEEK, listener.lastMode);
    }


    @Test
    public void testPrivateConstructorPreventsInstantiation() {
        Class<?> clazz = State.class;
        assertTrue("Constructor should be private", 
            java.lang.reflect.Modifier.isPrivate(clazz.getDeclaredConstructors()[0].getModifiers()));
    }
}
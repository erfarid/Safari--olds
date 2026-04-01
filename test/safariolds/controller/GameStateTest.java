package safariolds.controller;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import javax.swing.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameStateTest {
    private GameState gameState;
    private GameMap gameMap;
    private AtomicBoolean restartCalled;
    private AtomicBoolean exitCalled;

    @Before
    public void setUp() {
        System.setProperty("java.awt.headless", "true");
        
        restartCalled = new AtomicBoolean(false);
        exitCalled = new AtomicBoolean(false);
        
        gameMap = new GameMap("TestPlayer", GameState.INITIAL_AMOUNT, true) {
            @Override
            public void restartGame() {
                restartCalled.set(true);
            }
            
            @Override
            public JPanel getMainPanel() {
                return new JPanel();
            }
        };
        
        gameState = new GameState(gameMap);
    }

    @Test
    public void testConstants() {
        assertEquals(150000, GameState.WIN_AMOUNT);
        assertEquals(100000, GameState.INITIAL_AMOUNT);
    }

    @Test
    public void testConstructor() {
        assertNotNull(gameState);
        assertFalse(gameState.gameEnded);
    }

    @Test
    public void testCheckGameState_NoChange() {
        gameState.checkGameState(50000);
        assertFalse(gameState.gameEnded);
    }

    @Test
    public void testCheckGameState_WinCondition() {
        gameState.checkGameState(GameState.WIN_AMOUNT);
        assertTrue(gameState.gameEnded);
    }

    @Test
    public void testCheckGameState_LoseCondition() {
        gameState.checkGameState(0);
        assertTrue(gameState.gameEnded);
    }

    @Test
    public void testCheckGameState_NegativeMoney() {
        gameState.checkGameState(-100);
        assertTrue(gameState.gameEnded);
    }

    @Test
    public void testCheckGameState_AlreadyEnded() {
        gameState.gameEnded = true;
        gameState.checkGameState(GameState.WIN_AMOUNT);
        assertTrue(gameState.gameEnded);
    }

    @Test
    public void testIntegration_WinFlow() {
        gameState.checkGameState(GameState.WIN_AMOUNT);
        assertTrue(gameState.gameEnded);
    }

    @Test
    public void testIntegration_LoseFlow() {
        gameState.checkGameState(0);
        assertTrue(gameState.gameEnded);
    }

    @Test
    public void testGameStatePersistence() {
        gameState.gameEnded = true;
        gameState.checkGameState(GameState.WIN_AMOUNT);
        assertTrue(gameState.gameEnded);
    }


    @Test
    public void testEdgeCase_WinAmountPlusOne() {
        gameState.checkGameState(GameState.WIN_AMOUNT + 1);
        assertTrue(gameState.gameEnded);
    }

}
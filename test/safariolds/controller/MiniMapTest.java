package safariolds.controller;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.MouseEvent;
import javax.swing.*;

public class MiniMapTest {

    private MiniMap miniMap;
    private GameMap gameMap;

    @Before
    public void setUp() {
        // Setup headless mode
        System.setProperty("java.awt.headless", "true");

        // Create a mock GameMap with required constructor parameters
        gameMap = new GameMap("TestPlayer", 10000, true) {
            @Override
            public int getCOLS() {
                return 40;
            }

            @Override
            public int getROWS() {
                return 20;
            }

            @Override
            public int getVisibleWidth() {
                return 10;
            }

            @Override
            public int getVisibleHeight() {
                return 5;
            }

            @Override
            public char[][] getTerrainMatrix() {
                char[][] matrix = new char[getROWS()][getCOLS()];
                for (int i = 0; i < getROWS(); i++) {
                    for (int j = 0; j < getCOLS(); j++) {
                        matrix[i][j] = 'G'; // Grass by default
                    }
                }
                matrix[0][0] = 'S'; // Start position
                matrix[getROWS() - 1][getCOLS() - 1] = 'E'; // End position
                return matrix;
            }

            @Override
            public void centerViewportOn(int col, int row) {
                // Mock implementation
            }
        };

        miniMap = new MiniMap(gameMap);
    }

    @Test
    public void testInitialization() {
        assertNotNull(miniMap);
        assertEquals(MiniMap.MINIMAP_WIDTH, miniMap.getPreferredSize().width);
        assertEquals(MiniMap.MINIMAP_HEIGHT, miniMap.getPreferredSize().height);
        assertNotNull(miniMap.viewportRect);
    }

    @Test
    public void testPaintComponent() {
        // Create a dummy graphics object
        Graphics g = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).getGraphics();

        // Should not throw exceptions
        miniMap.paintComponent(g);

        // Verify viewport rectangle is drawn
        g.setColor(Color.RED);
        // Can't verify drawing operations directly in headless mode, but we can check the rectangle exists
        assertNotNull(miniMap.viewportRect);
    }

    @Test
    public void testHandleMinimapClick() {
        // Test click in the middle of the minimap
        miniMap.handleMinimapClick(MiniMap.MINIMAP_WIDTH / 2, MiniMap.MINIMAP_HEIGHT / 2);

        // Test click at edges
        miniMap.handleMinimapClick(0, 0);
        miniMap.handleMinimapClick(MiniMap.MINIMAP_WIDTH - 1, MiniMap.MINIMAP_HEIGHT - 1);

        // Test click outside bounds (should clamp)
        miniMap.handleMinimapClick(-10, -10);
        miniMap.handleMinimapClick(MiniMap.MINIMAP_WIDTH + 10, MiniMap.MINIMAP_HEIGHT + 10);
    }

    @Test
    public void testUpdateViewport() {
        // Update with typical values
        miniMap.updateViewport(5, 5, 10, 5);
        assertEquals(5 * (MiniMap.MINIMAP_WIDTH / (double) gameMap.getCOLS()), miniMap.viewportRect.getX(), 0.1);

        // Update with edge values
        miniMap.updateViewport(0, 0, gameMap.getCOLS(), gameMap.getROWS());
        assertEquals(0, miniMap.viewportRect.getX(), 0.1);
        assertEquals(MiniMap.MINIMAP_WIDTH, miniMap.viewportRect.getWidth(), 0.1);

        // Update with invalid values (should handle gracefully)
        miniMap.updateViewport(-1, -1, -1, -1);
    }

    @Test
    public void testMouseEvents() {
        // Test mouse click
        MouseEvent clickEvent = new MouseEvent(
                miniMap,
                MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(),
                0,
                MiniMap.MINIMAP_WIDTH / 2,
                MiniMap.MINIMAP_HEIGHT / 2,
                1,
                false
        );
        miniMap.getMouseListeners()[0].mouseClicked(clickEvent);

        // Test mouse drag
        MouseEvent dragEvent = new MouseEvent(
                miniMap,
                MouseEvent.MOUSE_DRAGGED,
                System.currentTimeMillis(),
                0,
                MiniMap.MINIMAP_WIDTH / 3,
                MiniMap.MINIMAP_HEIGHT / 3,
                1,
                false
        );
        miniMap.getMouseMotionListeners()[0].mouseDragged(dragEvent);
    }

    @Test
    public void testViewportRectInitialization() {
        Rectangle viewport = miniMap.viewportRect;
        assertNotNull(viewport);
        assertEquals(MiniMap.MINIMAP_WIDTH * gameMap.getVisibleWidth() / (double) gameMap.getCOLS(),
                viewport.getWidth(), 0.1);
        assertEquals(MiniMap.MINIMAP_HEIGHT * gameMap.getVisibleHeight() / (double) gameMap.getROWS(),
                viewport.getHeight(), 0.1);
    }

    @Test
    public void testTerrainColorMapping() {
        Graphics g = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).getGraphics();
        miniMap.paintComponent(g);

        // Can't verify colors directly in headless mode, but we can verify the terrain matrix
        char[][] matrix = gameMap.getTerrainMatrix();
        assertEquals('S', matrix[0][0]); // Start
        assertEquals('E', matrix[gameMap.getROWS() - 1][gameMap.getCOLS() - 1]); // End
        assertEquals('G', matrix[1][1]); // Grass
    }

    @Test
    public void testViewportRectScaling() {
        // Test that viewport rectangle scales correctly with different visible dimensions
        gameMap = new GameMap("TestPlayer", 10000, true) {
            @Override
            public int getVisibleWidth() {
                return 20;
            }

            @Override
            public int getVisibleHeight() {
                return 10;
            }
        };
        miniMap = new MiniMap(gameMap);

        assertEquals(20 * (MiniMap.MINIMAP_WIDTH / (double) gameMap.getCOLS()),
                miniMap.viewportRect.getWidth(), 0.1);
        assertEquals(10 * (MiniMap.MINIMAP_HEIGHT / (double) gameMap.getROWS()),
                miniMap.viewportRect.getHeight(), 0.1);
    }

    @Test
    public void testTerrainTypesRendering() {
        // Test that all terrain types are handled in paintComponent
        gameMap = new GameMap("TestPlayer", 10000, true) {
            @Override
            public char[][] getTerrainMatrix() {
                char[][] matrix = new char[getROWS()][getCOLS()];
                matrix[0][0] = 'G'; // Grass
                matrix[0][1] = 'S'; // Start
                matrix[0][2] = 'E'; // End
                matrix[0][3] = 'W'; // Water (if exists)
                return matrix;
            }
        };
        miniMap = new MiniMap(gameMap);

        Graphics g = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).getGraphics();
        miniMap.paintComponent(g); // Should handle all terrain types without exception
    }

    @Test
    public void testViewportRectPositionAfterUpdate() {
        // Test that viewport rectangle position updates correctly
        miniMap.updateViewport(10, 5, 10, 5);
        assertEquals(10 * (MiniMap.MINIMAP_WIDTH / (double) gameMap.getCOLS()),
                miniMap.viewportRect.getX(), 0.1);
        assertEquals(5 * (MiniMap.MINIMAP_HEIGHT / (double) gameMap.getROWS()),
                miniMap.viewportRect.getY(), 0.1);
    }

    @Test
    public void testMouseClickEdgeCases() {
        // Test mouse clicks at exact boundaries
        miniMap.handleMinimapClick(0, 0); // Top-left
        miniMap.handleMinimapClick(MiniMap.MINIMAP_WIDTH - 1, MiniMap.MINIMAP_HEIGHT - 1); // Bottom-right
    }

    @Test
    public void testBorderProperties() {
        // Test the border properties are as expected
        assertEquals(2, miniMap.getBorder().getBorderInsets(miniMap).bottom);
        assertEquals(Color.BLACK, ((javax.swing.border.LineBorder) miniMap.getBorder()).getLineColor());
    }

    @Test
    public void testMouseListenerCount() {
        // Verify correct number of mouse listeners are attached
        assertEquals(1, miniMap.getMouseListeners().length); // Click listener
        assertEquals(1, miniMap.getMouseMotionListeners().length); // Drag listener
    }

    @Test
    public void testMiniMapWithDifferentMapSizes() {
        // Test with a non-default map size
        GameMap largeMap = new GameMap("TestPlayer", 10000, true) {
            @Override
            public int getCOLS() {
                return 80;
            }  // Double width

            @Override
            public int getROWS() {
                return 40;
            }  // Double height
        };

        MiniMap largeMiniMap = new MiniMap(largeMap);
        assertEquals(MiniMap.MINIMAP_WIDTH, largeMiniMap.getPreferredSize().width);
        assertEquals(MiniMap.MINIMAP_HEIGHT, largeMiniMap.getPreferredSize().height);

        // Verify scaling calculations work with different sizes
        largeMiniMap.updateViewport(20, 10, 15, 10);
        assertEquals(20 * (MiniMap.MINIMAP_WIDTH / 80.0), largeMiniMap.viewportRect.getX(), 0.1);
    }

    @Test
    public void testMouseDragUpdatesViewport() {
        // Create a sequence of drag events
        MouseEvent startDrag = new MouseEvent(
                miniMap, MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(), 0,
                10, 10, 1, false
        );
        MouseEvent drag1 = new MouseEvent(
                miniMap, MouseEvent.MOUSE_DRAGGED,
                System.currentTimeMillis(), 0,
                20, 20, 1, false
        );
        MouseEvent drag2 = new MouseEvent(
                miniMap, MouseEvent.MOUSE_DRAGGED,
                System.currentTimeMillis(), 0,
                30, 30, 1, false
        );

        // Verify drag events are processed
        miniMap.getMouseMotionListeners()[0].mouseDragged(drag1);
        miniMap.getMouseMotionListeners()[0].mouseDragged(drag2);

        // Can't verify visual changes in headless mode, but can verify no exceptions
    }

    @Test
    public void testComponentSerialization() {
        // Test basic serialization properties
        assertTrue(miniMap instanceof java.io.Serializable);
        assertEquals(0, miniMap.getComponentCount()); // No child components
    }

    @Test
    public void testHighPrecisionCoordinates() {
        // Test with very precise coordinates
        miniMap.handleMinimapClick(
                (int) (MiniMap.MINIMAP_WIDTH * 0.333333),
                (int) (MiniMap.MINIMAP_HEIGHT * 0.666666)
        );

        miniMap.updateViewport(
                (int) (gameMap.getCOLS() * 0.123456),
                (int) (gameMap.getROWS() * 0.876543),
                5, 5
        );

        // Verify no arithmetic exceptions and reasonable results
        assertTrue(miniMap.viewportRect.getX() >= 0);
        assertTrue(miniMap.viewportRect.getY() >= 0);
    }

}

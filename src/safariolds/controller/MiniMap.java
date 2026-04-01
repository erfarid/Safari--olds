package safariolds.controller;

import safariolds.controller.GameMap;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MiniMap extends JPanel {
    public static final int MINIMAP_WIDTH = 200;
    public static final int MINIMAP_HEIGHT = 100;
    private final GameMap gameMap;
    public Rectangle viewportRect;

    public MiniMap(GameMap gameMap) {
        this.gameMap = gameMap;
        setPreferredSize(new Dimension(MINIMAP_WIDTH, MINIMAP_HEIGHT));
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        
        // Initialize viewport rectangle
        viewportRect = new Rectangle(0, 0, 
            MINIMAP_WIDTH * gameMap.getVisibleWidth() / gameMap.getCOLS(),
            MINIMAP_HEIGHT * gameMap.getVisibleHeight() / gameMap.getROWS());
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMinimapClick(e.getX(), e.getY());
            }
        });
        
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleMinimapClick(e.getX(), e.getY());
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw the entire map scaled down
        double scaleX = (double)MINIMAP_WIDTH / gameMap.getCOLS();
        double scaleY = (double)MINIMAP_HEIGHT / gameMap.getROWS();
        
        // Draw terrain
        for (int row = 0; row < gameMap.getROWS(); row++) {
            for (int col = 0; col < gameMap.getCOLS(); col++) {
                char terrain = gameMap.getTerrainMatrix()[row][col];
                Color color = terrain == 'G' ? Color.GREEN : 
                             terrain == 'S' ? Color.BLUE : 
                             terrain == 'E' ? Color.RED : Color.GRAY;
                
                g.setColor(color);
                g.fillRect((int)(col * scaleX), (int)(row * scaleY), 
                          (int)scaleX + 1, (int)scaleY + 1);
            }
        }
        
        // Draw viewport rectangle
        g.setColor(Color.RED);
        g.drawRect(viewportRect.x, viewportRect.y, 
                  viewportRect.width, viewportRect.height);
    }

    public void handleMinimapClick(int x, int y) {
        // Calculate corresponding map position
        int mapCol = (int)(x * gameMap.getCOLS() / (double)MINIMAP_WIDTH);
        int mapRow = (int)(y * gameMap.getROWS() / (double)MINIMAP_HEIGHT);
        
        // Center the viewport on this position
        gameMap.centerViewportOn(mapCol, mapRow);
    }

    public void updateViewport(int viewportX, int viewportY, int viewportWidth, int viewportHeight) {
        double scaleX = (double)MINIMAP_WIDTH / gameMap.getCOLS();
        double scaleY = (double)MINIMAP_HEIGHT / gameMap.getROWS();
        
        viewportRect.setRect(viewportX * scaleX, viewportY * scaleY,
                           viewportWidth * scaleX, viewportHeight * scaleY);
        repaint();
    }
}
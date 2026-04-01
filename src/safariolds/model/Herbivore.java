package safariolds.model;
import safariolds.controller.GameMap;
import javax.swing.*;
import java.awt.*;

public  class Herbivore extends Animal {
    
    public Herbivore(int row, int col, JLabel label, GameMap gameMap) {
        super(row, col, label, "herbivore", gameMap);
    }
    
    @Override
    public void performSpecificBehavior() {
        checkHerbivoreEatingPlants();
    }
    
    private void checkHerbivoreEatingPlants() {
        JLayeredPane[][] gridLayers = gameMap.getGridLayers();
        JLayeredPane tile = gridLayers[row][col];
        for (Component comp : tile.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (label.getIcon() == gameMap.getObjectIcons().get("tree")) {
                    tile.remove(label);
                    tile.revalidate();
                    tile.repaint();
                    
                    return; 
                }
            }
        }
    }
}
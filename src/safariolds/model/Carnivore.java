package safariolds.model;
import safariolds.controller.GameMap;
import javax.swing.*;
import java.util.List;

public class Carnivore extends Animal {
    
    public Carnivore(int row, int col, JLabel label, GameMap gameMap) {
        super(row, col, label, "carni", gameMap);
    }
    
    @Override
    public void performSpecificBehavior() {
        checkPredatorPreyInteraction();
    }
    
    private void checkPredatorPreyInteraction() {
        // Check if this carnivore is near any herbivores
        List<Animal> animals = gameMap.getAnimals();
        int tileSize = gameMap.getTileSize();
        
        for (Animal animal : animals) {
            if (animal.getType().equals("herbivore") &&
                    Math.abs(animal.getXPos() - xPos) < tileSize/2 &&
                    Math.abs(animal.getYPos() - yPos) < tileSize/2) {
                
              
                //System.out.println("Carnivore is eating a herbivore at position " + row + "," + col);
                
                // Stop herbivore timers
                animal.stopTimers();
                
                // Remove herbivore from the game
                gameMap.getGridContainer().remove(animal.getLabel());
                gameMap.getAnimals().remove(animal);
                
                // Repaint to show the changes
                gameMap.getGridContainer().revalidate();
                gameMap.getGridContainer().repaint();
                
                
                
                break; // Only eat one herbivore at a time
            }
        }
    }
}
//implemeted carnivore interenaction 
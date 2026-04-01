package safariolds.controller;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import javax.swing.*;
import safariolds.model.*;

public class TGameMapT extends GameMap {

// For testing purposes
    public boolean guiInitialized = false;
    public final List<String> methodCalls = new ArrayList<>();

    public TGameMapT(String playerName, int money) {
        super(playerName, money, true); // Call parent constructor

        initTerrainImages();
        initObjectImages();
        initComponents();

    }

    @Override
    protected void initTerrainImages() {
        // Skip tracking in initialization phase
        if (methodCalls != null) {
            methodCalls.add("initTerrainImages");
        }
        terrainIcons = new HashMap<>();
        terrainIcons.put('G', createColorIcon(Color.GREEN, 10, 10));
    }

    // Override all GUI-related methods to prevent actual GUI operations
    public void setVisible(boolean visible) {
        methodCalls.add("setVisible(" + visible + ")");
        guiInitialized = visible;
    }

    @Override
    protected void initComponents() {
        if (methodCalls != null) {
            methodCalls.add("initComponents");
        }

        // Ensure UI labels are created
        createTopPanel(); // 👈 This ensures roadsLeftLabel is NOT null

        gridLayers = new JLayeredPane[ROWS][COLS];
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                gridLayers[row][col] = new JLayeredPane();
            }
        }

        initializeCounts();
    }

    @Override
    protected JPanel createTopPanel() {
        methodCalls.add("createTopPanel");
        JPanel panel = new JPanel();

        // Create minimal UI components needed for testing
        moneyLabel = new JLabel();
        roadsLeftLabel = new JLabel();
        carnivoresLabel = new JLabel();
        herbivoresLabel = new JLabel();
        plantsLabel = new JLabel();
        pondsLabel = new JLabel();

        panel.add(moneyLabel);
        panel.add(roadsLeftLabel);
        panel.add(carnivoresLabel);
        panel.add(herbivoresLabel);
        panel.add(plantsLabel);
        panel.add(pondsLabel);

        return panel;
    }

//    @Override
//    protected void initTerrainImages() {
//        methodCalls.add("initTerrainImages");
//        terrainIcons = new HashMap<>();
//        terrainIcons.put('G', createColorIcon(Color.GREEN, 10, 10));
//    }
    @Override
    protected void initObjectImages() {
        methodCalls.add("initObjectImages");
        objectIcons = new HashMap<>();
        objectIcons.put("herbivore", createColorIcon(Color.RED, 10, 10));
        objectIcons.put("carnivore", createColorIcon(Color.ORANGE, 10, 10));
        objectIcons.put("tree", createColorIcon(Color.GREEN.darker(), 10, 10));
        objectIcons.put("pond", createColorIcon(Color.BLUE, 10, 10));
        objectIcons.put("road", createColorIcon(Color.GRAY, 10, 10));
    }

    @Override
    protected ImageIcon createColorIcon(Color color, int width, int height) {
        methodCalls.add("createColorIcon");
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        return new ImageIcon(img);
    }

    @Override
    public void updateCounterLabels() {
        methodCalls.add("updateCounterLabels");
        if (moneyLabel != null) {
            moneyLabel.setText("Money: €" + money);
        }
        if (roadsLeftLabel != null) {
            roadsLeftLabel.setText("Roads Left: " + (Road.getTotalRoadsPurchased() - Road.getRoadsUsed()));
        }
        if (carnivoresLabel != null) {
            carnivoresLabel.setText("Ca: " + carnivoreCount);
        }
        if (herbivoresLabel != null) {
            herbivoresLabel.setText("H: " + herbivoreCount);
        }
        if (plantsLabel != null) {
            plantsLabel.setText("T: " + plantCount);
        }
        if (pondsLabel != null) {
            pondsLabel.setText("Ponds: " + pondCount);
        }
    }

    // Test helper methods
    public void setTestTile(int row, int col, char terrainType) {
        terrainMatrix[row][col] = terrainType;
    }

    public void addTestAnimal(int row, int col, String type) {
        Animal animal;
        JLabel label = new JLabel(objectIcons.get(type));

        if (type.contains("herbivore")) {
            animal = new Herbivore(row, col, label, this);
        } else {
            animal = new Carnivore(row, col, label, this);
        }

        animals.add(animal);
        if (type.contains("herbivore")) {
            herbivoreCount++;
        } else {
            carnivoreCount++;
        }
    }

    protected void initializeCounts() {
        methodCalls.add("initializeCounts");
        carnivoreCount = 0;
        herbivoreCount = 0;
        plantCount = 0;
        pondCount = 0;
        updateCounterLabels();
    }

    public void addTestItem(int row, int col, String itemType) {
        placedItems.put(new Point(row, col), itemType);
        switch (itemType) {
            case "tree":
                plantCount++;
                break;
            case "pond":
                pondCount++;
                break;
        }
    }

    protected void startMovementTimers() {
        // No-op
    }
}

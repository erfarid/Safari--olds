package safariolds.view;

import safariolds.controller.GameMap;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Shop extends JFrame {

    public GameMap gameMap;
    private JPanel mainPanel;
    private JTabbedPane tabbedPane;

    // Prices for items
    private final int JEEP_PRICE = 2000;
    private final int ROAD_PRICE = 40;
    private final int TREE_PRICE = 300;
    private final int HERBIVORE_PRICE = 1000;
    private final int CARNIVORE_PRICE = 1500;
    private final int POND_PRICE = 700;
    private final int CHIPANIMAL_PRICE = 4000;

    // Selling prices (typically lower than buying prices)
    private final int JEEP_SELL_PRICE = 1500;
    private final int ROAD_SELL_PRICE = 300;
    private final int TREE_SELL_PRICE = 150;
    private final int HERBIVORE_SELL_PRICE = 700;
    private final int CARNIVORE_SELL_PRICE = 1000;
    private final int POND_SELL_PRICE = 400;

    // Maximum quantity that can be bought/sold at once
    private final int MAX_QUANTITY = 10;

    // Shop of the GameMap
    public Shop(GameMap gameMap) {
        this.gameMap = gameMap;
        setTitle("Safari Shop");
        setSize(950, 750);  // Increased size to accommodate quantity controls
        setLocationRelativeTo(gameMap.getFrame());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
        setVisible(true);
    }

    protected void initComponents() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 240, 240));

        // Header
        JLabel header = new JLabel("SAFARI SHOP", SwingConstants.CENTER);
        header.setFont(new Font("Algerian", Font.BOLD, 36));
        header.setForeground(new Color(139, 69, 19));
        mainPanel.add(header, BorderLayout.NORTH);

        // Create tabbed pane for Buy/Sell
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 16));

        // Buy Panel
        JPanel buyPanel = createShopPanel(true);
        tabbedPane.addTab("Buy", buyPanel);

        // Sell Panel
        JPanel sellPanel = createShopPanel(false);
        tabbedPane.addTab("Sell", sellPanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Close button
        JButton closeButton = new JButton("Close Shop");
        styleButton(closeButton);
        closeButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 240, 240));
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    public JPanel createShopPanel(boolean isBuyPanel) {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel itemsPanel = new JPanel(new GridLayout(0, 2, 20, 20));
        itemsPanel.setBackground(new Color(240, 240, 240));

        // Add shop items
        addShopItem(itemsPanel, "Jeep", isBuyPanel ? JEEP_PRICE : JEEP_SELL_PRICE,
                "/safariolds/view/assets/jeep.png", isBuyPanel);
        addShopItem(itemsPanel, "Road", isBuyPanel ? ROAD_PRICE : ROAD_SELL_PRICE,
                "/safariolds/view/assets/road.jpg", isBuyPanel);
        addShopItem(itemsPanel, "Tree", isBuyPanel ? TREE_PRICE : TREE_SELL_PRICE,
                "/safariolds/view/assets/tree.png", isBuyPanel);
        addShopItem(itemsPanel, "Herbivore", isBuyPanel ? HERBIVORE_PRICE : HERBIVORE_SELL_PRICE,
                "/safariolds/view/assets/herbivore.png", isBuyPanel);
        addShopItem(itemsPanel, "Carnivore", isBuyPanel ? CARNIVORE_PRICE : CARNIVORE_SELL_PRICE,
                "/safariolds/view/assets/carni.png", isBuyPanel);
        addShopItem(itemsPanel, "Pond", isBuyPanel ? POND_PRICE : POND_SELL_PRICE,
                "/safariolds/view/assets/Pond.png", isBuyPanel);

        // Add Chipanimal only to the buy panel
        if (isBuyPanel) {
            addShopItem(itemsPanel, "Chipanimal", CHIPANIMAL_PRICE,
                    "/safariolds/view/assets/Chipanimal.png", true);
        }

        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    public void addShopItem(JPanel panel, String name, int price, String imagePath, boolean isBuy) {
        JPanel itemPanel = new JPanel(new BorderLayout(10, 10));
        itemPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        itemPanel.setBackground(Color.WHITE);
        itemPanel.setPreferredSize(new Dimension(350, 180));

        // Item image
        ImageIcon icon = loadImageIcon(name, imagePath);
        JLabel imageLabel = new JLabel(icon);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        itemPanel.add(imageLabel, BorderLayout.CENTER);

        // Item info panel
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        infoPanel.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JLabel priceLabel = new JLabel((isBuy ? "Buy: €" : "Sell: €") + price + " each", SwingConstants.CENTER);
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        // Quantity input - changed from buttons to text field
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        quantityPanel.setBackground(Color.WHITE);

        JLabel quantityPrompt = new JLabel("Quantity:", SwingConstants.CENTER);
        JTextField quantityField = new JTextField("1", 5);
        quantityField.setHorizontalAlignment(JTextField.CENTER);

        // 👇 Prevent user from changing Jeep quantity
        if (name.equals("Jeep")) {
            quantityField.setEditable(false);
            quantityField.setText("1");
        }

        quantityPanel.add(quantityPrompt);
        quantityPanel.add(quantityField);

        infoPanel.add(nameLabel);
        infoPanel.add(priceLabel);
        infoPanel.add(quantityPanel);
        itemPanel.add(infoPanel, BorderLayout.NORTH);

        // Action button
        JButton actionButton = new JButton(isBuy ? "Buy" : "Sell");
        styleButton(actionButton);

        actionButton.addActionListener(e -> {
            try {
                int quantity = Integer.parseInt(quantityField.getText());
                if (quantity <= 0) {
                    if (!gameMap.isTestMode()) {

                        JOptionPane.showMessageDialog(this,
                                "Quantity must be at least 1",
                                "Invalid Quantity",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
                if (isBuy) {
                    handleBuy(name, price, quantity);
                } else {
                    handleSell(name, price, quantity);
                }
            } catch (NumberFormatException ex) {
                if (!gameMap.isTestMode()) {

                    JOptionPane.showMessageDialog(this,
                            "Please enter a valid number",
                            "Invalid Input",
                            JOptionPane.WARNING_MESSAGE);

                }
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(actionButton);
        itemPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(itemPanel);
    }

    public void handleBuy(String itemName, int unitPrice, int quantity) {
        if (itemName.equals("Jeep") && quantity > 1) {
            if (!gameMap.isTestMode()) {
                JOptionPane.showMessageDialog(this, "You can only buy 1 Jeep at a time!", "Limit Reached", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        int totalPrice = unitPrice * quantity;
        if (gameMap.getMoney() >= totalPrice) {
            gameMap.updateMoney(gameMap.getMoney() - totalPrice);

            if (itemName.equals("Road")) {
                gameMap.addPurchasedRoads(quantity);
                gameMap.startRoadPlacement(quantity);
                dispose();
            } else if (itemName.equals("Jeep")) {
                safariolds.model.Jeep jeep = new safariolds.model.Jeep(10, 0, gameMap);
            } else if (itemName.equals("Chipanimal")) {
                if (quantity > 1) {
                    if (!gameMap.isTestMode()) {
                        JOptionPane.showMessageDialog(this,
                                "You can only buy 1 Chip at a time!",
                                "Limit Reached",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }

                gameMap.activateChip(); // <- set chip flag
                if (!gameMap.isTestMode()) {
                    JOptionPane.showMessageDialog(this,
                            "You have purchased a Chip.\nRight-click an animal in Night Mode to activate it!",
                            "Chip Activated",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                // Add to inventory and set as selected item
                gameMap.addToInventory(itemName, quantity);
                gameMap.setSelectedItemType(itemName);
                if (!gameMap.isTestMode()) {
                    JOptionPane.showMessageDialog(this,
                            "Purchased " + quantity + " " + itemName + "(s)\n"
                            + "Right-click on map to place them",
                            "Purchase Successful",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } else {
            if (!gameMap.isTestMode()) {
                JOptionPane.showMessageDialog(this,
                        "Not enough money! You need €" + (totalPrice - gameMap.getMoney()) + " more.",
                        "Purchase Failed",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    public void handleSell(String itemName, int unitPrice, int quantity) {
        // Get current available count
        int availableCount = gameMap.getAvailableCount(itemName);

        // Validate requested quantity
        if (quantity <= 0) {
            if (!gameMap.isTestMode()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid quantity (1 or more)",
                        "Invalid Quantity",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        if (availableCount <= 0) {
            if (!gameMap.isTestMode()) {
                JOptionPane.showMessageDialog(this,
                        "No " + itemName + " available to sell!",
                        "Sale Failed",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        if (quantity > availableCount) {
            if (!gameMap.isTestMode()) {
            JOptionPane.showMessageDialog(this,
                    "Cannot sell " + quantity + " " + itemName + "(s)\n"
                    + "Only " + availableCount + " available!",
                    "Insufficient Quantity",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Proceed with sale
        int actuallyRemoved = gameMap.removeItemsFromMap(itemName, quantity);
        if (actuallyRemoved > 0) {
            gameMap.updateMoney(gameMap.getMoney() + (unitPrice * actuallyRemoved));
            JOptionPane.showMessageDialog(this,
                    "Successfully sold " + actuallyRemoved + " " + itemName + "(s)\n"
                    + "Earned: €" + (unitPrice * actuallyRemoved) + "\n"
                    + "Remaining: " + (availableCount - actuallyRemoved),
                    "Sale Completed",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        }
    }

    public ImageIcon loadImageIcon(String name, String imagePath) {
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));
            return new ImageIcon(icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            // Fallback colored rectangle
            return createColorIcon(name.equals("Jeep") ? Color.BLUE
                    : name.equals("Tree") ? Color.GREEN.darker()
                    : name.equals("Herbivore") ? Color.RED
                    : name.equals("Carnivore") ? Color.ORANGE
                    : name.equals("Chipanimal") ? Color.MAGENTA
                    : Color.CYAN, 80, 80);
        }
    }

    public ImageIcon createColorIcon(Color color, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        return new ImageIcon(image);
    }

    public void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(139, 69, 19));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    }

    public void styleQuantityButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(100, 100, 100));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(30, 20));
        button.setMargin(new Insets(0, 0, 0, 0));
    }
}

package safariolds.model;

public class InventoryItem {
    private String type;
    private int count;
    private int maxCount; // For purchased items
    
    public InventoryItem(String type, int count, int maxCount) {
        this.type = type;
        this.count = count;
        this.maxCount = maxCount;
    }
    
    // Getters and setters
    public String getType() { return type; }
    public int getCount() { return count; }
    public int getMaxCount() { return maxCount; }
    public void increment() { count++; }
    public void decrement() { if(count > 0) count--; }
    public boolean canPlace() { return count < maxCount; }
}
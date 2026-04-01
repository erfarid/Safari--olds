package safariolds.controller;

public enum GameMode {
    HOUR(0.5f, "Slow (Hour)", 2.0f),  // Slower movement, longer timers
    DAY(1.0f, "Normal (Day)", 1.0f),  // Normal speed
    WEEK(2.0f, "Fast (Week)", 0.5f);  // Faster movement, shorter timers

    private final float speedMultiplier;
    private final String displayName;
    private final float timeMultiplier; // Affects timer intervals

    GameMode(float speedMultiplier, String displayName, float timeMultiplier) {
        this.speedMultiplier = speedMultiplier;
        this.displayName = displayName;
        this.timeMultiplier = timeMultiplier;
    }

    public float getSpeedMultiplier() {
        return speedMultiplier;
    }

    public float getTimeMultiplier() {
        return timeMultiplier;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
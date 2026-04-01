package safariolds.model;

public enum DifficultyLevel {
    EASY(0.2, 0.1, 300, 1.0f, 0.05, 10000),
    MEDIUM(0.3, 0.2, 200, 1.5f, 0.1, 8000),
    HARD(0.5, 0.4, 150, 2.0f, 0.2, 5000);

    private final double herbivoreSpawnRate;
    private final double carnivoreSpawnRate;
    private final int gameDuration;
    private final float animalSpeedMultiplier;
    private final double poacherSpawnRate;
    private final int startingMoney;

    DifficultyLevel(double herbivoreSpawnRate, double carnivoreSpawnRate, int gameDuration,
                    float animalSpeedMultiplier, double poacherSpawnRate, int startingMoney) {
        this.herbivoreSpawnRate = herbivoreSpawnRate;
        this.carnivoreSpawnRate = carnivoreSpawnRate;
        this.gameDuration = gameDuration;
        this.animalSpeedMultiplier = animalSpeedMultiplier;
        this.poacherSpawnRate = poacherSpawnRate;
        this.startingMoney = startingMoney;
    }

    public double getHerbivoreSpawnRate() { return herbivoreSpawnRate; }
    public double getCarnivoreSpawnRate() { return carnivoreSpawnRate; }
    public int getGameDuration() { return gameDuration; }
    public float getAnimalSpeedMultiplier() { return animalSpeedMultiplier; }
    public double getPoacherSpawnRate() { return poacherSpawnRate; }
    public int getStartingMoney() { return startingMoney; }
}

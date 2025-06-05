package models;

import java.io.Serializable;

/**
 * GameSaveData - Data class for storing game state information
 * Used for saving and loading game progress
 */
public class GameSaveData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Core game state
    private boolean gamePaused;
    private boolean gameSpeedIncreased;
    private float gameSpeedMultiplier;
    private String currentMapName;
    private String currentDifficulty;
    
    // Game statistics
    private int totalEnemiesSpawned;
    private int enemiesReachedEnd;
    private int enemyDefeated;
    private int totalDamage;
    private int timePlayedInSeconds;
    private long gameTimeMillis;
    
    // Castle health
    private int castleMaxHealth;
    private int castleCurrentHealth;
    
    // Level data
    private int[][] level;
    private int[][] overlay;
    
    // Manager save data (stored as generic Objects for flexibility)
    private Object playerData;
    private Object waveData;
    private Object towerData;
    private Object enemyData;
    private Object weatherData;
    private Object ultiData;
    
    // Constructors
    public GameSaveData() {
        // Default constructor
    }
    
    // Getters and Setters
    public boolean isGamePaused() { return gamePaused; }
    public void setGamePaused(boolean gamePaused) { this.gamePaused = gamePaused; }
    
    public boolean isGameSpeedIncreased() { return gameSpeedIncreased; }
    public void setGameSpeedIncreased(boolean gameSpeedIncreased) { this.gameSpeedIncreased = gameSpeedIncreased; }
    
    public float getGameSpeedMultiplier() { return gameSpeedMultiplier; }
    public void setGameSpeedMultiplier(float gameSpeedMultiplier) { this.gameSpeedMultiplier = gameSpeedMultiplier; }
    
    public String getCurrentMapName() { return currentMapName; }
    public void setCurrentMapName(String currentMapName) { this.currentMapName = currentMapName; }
    
    public String getCurrentDifficulty() { return currentDifficulty; }
    public void setCurrentDifficulty(String currentDifficulty) { this.currentDifficulty = currentDifficulty; }
    
    public int getTotalEnemiesSpawned() { return totalEnemiesSpawned; }
    public void setTotalEnemiesSpawned(int totalEnemiesSpawned) { this.totalEnemiesSpawned = totalEnemiesSpawned; }
    
    public int getEnemiesReachedEnd() { return enemiesReachedEnd; }
    public void setEnemiesReachedEnd(int enemiesReachedEnd) { this.enemiesReachedEnd = enemiesReachedEnd; }
    
    public int getEnemyDefeated() { return enemyDefeated; }
    public void setEnemyDefeated(int enemyDefeated) { this.enemyDefeated = enemyDefeated; }
    
    public int getTotalDamage() { return totalDamage; }
    public void setTotalDamage(int totalDamage) { this.totalDamage = totalDamage; }
    
    public int getTimePlayedInSeconds() { return timePlayedInSeconds; }
    public void setTimePlayedInSeconds(int timePlayedInSeconds) { this.timePlayedInSeconds = timePlayedInSeconds; }
    
    public long getGameTimeMillis() { return gameTimeMillis; }
    public void setGameTimeMillis(long gameTimeMillis) { this.gameTimeMillis = gameTimeMillis; }
    
    public int getCastleMaxHealth() { return castleMaxHealth; }
    public void setCastleMaxHealth(int castleMaxHealth) { this.castleMaxHealth = castleMaxHealth; }
    
    public int getCastleCurrentHealth() { return castleCurrentHealth; }
    public void setCastleCurrentHealth(int castleCurrentHealth) { this.castleCurrentHealth = castleCurrentHealth; }
    
    public int[][] getLevel() { return level; }
    public void setLevel(int[][] level) { this.level = level; }
    
    public int[][] getOverlay() { return overlay; }
    public void setOverlay(int[][] overlay) { this.overlay = overlay; }
    
    // Manager data getters and setters
    public Object getPlayerData() { return playerData; }
    public void setPlayerData(Object playerData) { this.playerData = playerData; }
    
    public Object getWaveData() { return waveData; }
    public void setWaveData(Object waveData) { this.waveData = waveData; }
    
    public Object getTowerData() { return towerData; }
    public void setTowerData(Object towerData) { this.towerData = towerData; }
    
    public Object getEnemyData() { return enemyData; }
    public void setEnemyData(Object enemyData) { this.enemyData = enemyData; }
    
    public Object getWeatherData() { return weatherData; }
    public void setWeatherData(Object weatherData) { this.weatherData = weatherData; }
    
    public Object getUltiData() { return ultiData; }
    public void setUltiData(Object ultiData) { this.ultiData = ultiData; }
} 
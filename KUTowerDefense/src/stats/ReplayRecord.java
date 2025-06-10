package stats;

import java.util.ArrayList;
import java.util.List;

public class ReplayRecord {
    private String mapName;
    private boolean isVictory;
    private int goldEarned;
    private int enemiesSpawned;
    private int enemiesReachedEnd;
    private int towersBuilt;
    private int enemyDefeated;
    private int totalDamage;
    private int timePlayed;
    private List<GameAction> actions;
    private long timestamp;

    public ReplayRecord() {
        this.actions = new ArrayList<>();
        this.timestamp = System.currentTimeMillis();
    }

    public ReplayRecord(List<GameAction> actions) {
        this.actions = new ArrayList<>(actions);
        this.timestamp = System.currentTimeMillis();
    }

    public void addAction(GameAction action) {
        actions.add(action);
    }

    // Getters and setters
    public String getMapName() { return mapName; }
    public void setMapName(String mapName) { this.mapName = mapName; }
    
    public boolean isVictory() { return isVictory; }
    public void setVictory(boolean victory) { isVictory = victory; }
    
    public int getGoldEarned() { return goldEarned; }
    public void setGoldEarned(int goldEarned) { this.goldEarned = goldEarned; }
    
    public int getEnemiesSpawned() { return enemiesSpawned; }
    public void setEnemiesSpawned(int enemiesSpawned) { this.enemiesSpawned = enemiesSpawned; }
    
    public int getEnemiesReachedEnd() { return enemiesReachedEnd; }
    public void setEnemiesReachedEnd(int enemiesReachedEnd) { this.enemiesReachedEnd = enemiesReachedEnd; }
    
    public int getTowersBuilt() { return towersBuilt; }
    public void setTowersBuilt(int towersBuilt) { this.towersBuilt = towersBuilt; }
    
    public int getEnemyDefeated() { return enemyDefeated; }
    public void setEnemyDefeated(int enemyDefeated) { this.enemyDefeated = enemyDefeated; }
    
    public int getTotalDamage() { return totalDamage; }
    public void setTotalDamage(int totalDamage) { this.totalDamage = totalDamage; }
    
    public int getTimePlayed() { return timePlayed; }
    public void setTimePlayed(int timePlayed) { this.timePlayed = timePlayed; }
    
    public List<GameAction> getActions() { return actions; }
    public void setActions(List<GameAction> actions) { this.actions = actions; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
} 
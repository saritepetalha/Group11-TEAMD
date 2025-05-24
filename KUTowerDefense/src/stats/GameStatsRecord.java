package stats;

public class GameStatsRecord {
    private String mapName;
    private boolean victory;
    private int gold;
    private int enemiesSpawned;
    private int enemiesReachedEnd;
    private int towersBuilt;
    private int enemyDefeated;
    private int totalDamage;
    private int timePlayedInSeconds;

    public GameStatsRecord() {}

    public GameStatsRecord(String mapName, boolean victory, int gold, int enemiesSpawned, int enemiesReachedEnd,
                           int towersBuilt, int enemyDefeated, int totalDamage, int timePlayedInSeconds) {
        this.mapName = mapName;
        this.victory = victory;
        this.gold = gold;
        this.enemiesSpawned = enemiesSpawned;
        this.enemiesReachedEnd = enemiesReachedEnd;
        this.towersBuilt = towersBuilt;
        this.enemyDefeated = enemyDefeated;
        this.totalDamage = totalDamage;
        this.timePlayedInSeconds = timePlayedInSeconds;
    }

    public String getMapName() { return mapName; }
    public boolean isVictory() { return victory; }
    public int getGold() { return gold; }
    public int getEnemiesSpawned() { return enemiesSpawned; }
    public int getEnemiesReachedEnd() { return enemiesReachedEnd; }
    public int getTowersBuilt() { return towersBuilt; }
    public int getEnemyDefeated() { return enemyDefeated; }
    public int getTotalDamage() { return totalDamage; }
    public int getTimePlayed() { return timePlayedInSeconds; }
}

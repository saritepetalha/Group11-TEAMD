package config;

import java.util.*;

public class GameOptions {

    // ---------- Economy & player ----------
    private int startingGold     = 500;
    private int startingPlayerHP = 20;

    // ---------- Static stats ----------
    private Map<EnemyType, EnemyStats> enemyStats =
            new EnumMap<>(EnemyType.class);
    private Map<TowerType, TowerStats> towerStats =
            new EnumMap<>(TowerType.class);

    // ---------- Wave schedule ----------
    private List<Wave> waves     = new ArrayList<>();
    private double interWaveDelay = 10.0;

    /* ------------------- Getters / setters ------------------- */
    public int getStartingGold()             { return startingGold; }
    public void setStartingGold(int g)       { this.startingGold = g; }

    public int getStartingPlayerHP()         { return startingPlayerHP; }
    public void setStartingPlayerHP(int hp)  { this.startingPlayerHP = hp; }

    public Map<EnemyType, EnemyStats> getEnemyStats() { return enemyStats; }
    public Map<TowerType, TowerStats> getTowerStats() { return towerStats; }

    public List<Wave> getWaves()             { return waves; }

    public double getInterWaveDelay()        { return interWaveDelay; }
    public void setInterWaveDelay(double d)  { this.interWaveDelay = d; }

    public int getWaveCount() { return waves.size(); }

    /* ---------- Hard-coded defaults (same values as before) ---------- */
    public static GameOptions defaults() {
        GameOptions o = new GameOptions();

        // Enemy defaults - based on Constants.Enemies values
        o.enemyStats.put(EnemyType.GOBLIN, new EnemyStats(100, 0.85, 5));
        o.enemyStats.put(EnemyType.WARRIOR, new EnemyStats(200, 0.45, 25));
        o.enemyStats.put(EnemyType.BARREL, new EnemyStats(150, 0.6, 15));
        o.enemyStats.put(EnemyType.TNT, new EnemyStats(50, 1.2, 2));
        o.enemyStats.put(EnemyType.TROLL, new EnemyStats(300, 0.15, 10));

        // Tower defaults
        o.towerStats.put(TowerType.ARCHER,
                new TowerStats(60, 3.5, 1.2, 0.0, 15));
        o.towerStats.put(TowerType.ARTILLERY,
                new TowerStats(120, 2.5, 0.6, 1.5, 25));
        o.towerStats.put(TowerType.MAGE,
                new TowerStats(90, 3.0, 0.9, 0.0, 20));

        // Waves - updated to use WARRIOR instead of KNIGHT
        Group g1 = new Group(Map.of(EnemyType.GOBLIN, 8), 0.5);
        Group g2 = new Group(Map.of(EnemyType.GOBLIN, 5,
                EnemyType.WARRIOR, 2), 0.4);

        o.waves.add(new Wave(List.of(g1),               0.0)); // 1-group wave
        o.waves.add(new Wave(List.of(g1, g2),           1.5)); // 2-group wave
        o.interWaveDelay     = 8.0;
        o.startingGold       = 300;
        o.startingPlayerHP   = 15;
        return o;
    }
}

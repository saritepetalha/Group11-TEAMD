package interfaces;

import enemies.Enemy;
import managers.*;

/**
 * GameContext interface - Defines the contract for game context objects
 * that managers need to interact with.
 * 
 * This allows both Playing and PlayingModel to implement the same interface,
 * enabling the MVC refactoring while maintaining compatibility with existing managers.
 */
public interface GameContext {
    
    // Manager access methods
    EnemyManager getEnemyManager();
    WeatherManager getWeatherManager();
    PlayerManager getPlayerManager();
    TowerManager getTowerManager();
    GoldBagManager getGoldBagManager();
    UltiManager getUltiManager();
    
    // Game statistics methods
    void incrementEnemyDefeated();
    void addTotalDamage(int damage);
    
    // Enemy lifecycle methods
    void enemyReachedEnd(Enemy enemy);
    void spawnEnemy(int enemyType);
    
    // Game state queries
    boolean isGamePaused();
    float getGameSpeedMultiplier();
    long getGameTime();
    
    // Map data access
    int[][] getLevel();
    int[][] getOverlay();
} 
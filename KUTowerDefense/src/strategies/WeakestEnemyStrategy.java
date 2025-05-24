package strategies;

import enemies.Enemy;
import java.util.List;

/**
 * Targeting strategy that selects the enemy with the lowest current health.
 * Useful for quickly eliminating wounded enemies and reducing the number of threats.
 */
public class WeakestEnemyStrategy implements TargetingStrategy {
    
    @Override
    public Enemy selectTarget(List<Enemy> enemiesInRange, Object tower) {
        Enemy bestTarget = null;
        int lowestHealth = Integer.MAX_VALUE;
        
        for (Enemy enemy : enemiesInRange) {
            if (enemy.isAlive()) {
                int enemyHealth = enemy.getHealth();
                
                // Select enemy with lowest current health
                if (enemyHealth < lowestHealth) {
                    lowestHealth = enemyHealth;
                    bestTarget = enemy;
                }
            }
        }
        
        return bestTarget;
    }
    
    @Override
    public String getStrategyName() {
        return "Weakest";
    }
} 
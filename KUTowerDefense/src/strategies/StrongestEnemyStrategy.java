package strategies;

import enemies.Enemy;
import java.util.List;

/**
 * Targeting strategy that selects the enemy with the highest current health.
 * Useful for focusing on tanky enemies that can absorb a lot of damage.
 */
public class StrongestEnemyStrategy implements TargetingStrategy {
    
    @Override
    public Enemy selectTarget(List<Enemy> enemiesInRange, Object tower) {
        Enemy bestTarget = null;
        int highestHealth = -1;
        
        for (Enemy enemy : enemiesInRange) {
            if (enemy.isAlive()) {
                int enemyHealth = enemy.getHealth();
                
                // Select enemy with highest current health
                if (enemyHealth > highestHealth) {
                    highestHealth = enemyHealth;
                    bestTarget = enemy;
                }
            }
        }
        
        return bestTarget;
    }
    
    @Override
    public String getStrategyName() {
        return "Strongest";
    }
} 
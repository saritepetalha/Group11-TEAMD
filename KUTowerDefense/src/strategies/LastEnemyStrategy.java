package strategies;

import enemies.Enemy;
import java.util.List;

/**
 * Targeting strategy that selects the enemy closest to the exit.
 * This targets enemies with the highest currentPathIndex (furthest along the path).
 */
public class LastEnemyStrategy implements TargetingStrategy {
    
    @Override
    public Enemy selectTarget(List<Enemy> enemiesInRange, Object tower) {
        Enemy bestTarget = null;
        int highestPathIndex = -1;
        
        for (Enemy enemy : enemiesInRange) {
            if (enemy.isAlive()) {
                int enemyPathIndex = enemy.getCurrentPathIndex();
                
                // Select enemy with highest path index (closest to exit)
                if (enemyPathIndex > highestPathIndex) {
                    highestPathIndex = enemyPathIndex;
                    bestTarget = enemy;
                }
            }
        }
        
        return bestTarget;
    }
    
    @Override
    public String getStrategyName() {
        return "Last";
    }
} 
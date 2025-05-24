package strategies;

import enemies.Enemy;
import java.util.List;

/**
 * Targeting strategy that selects the first enemy in range.
 * This represents the current default behavior.
 */
public class FirstEnemyStrategy implements TargetingStrategy {
    
    @Override
    public Enemy selectTarget(List<Enemy> enemiesInRange, Object tower) {
        // Return the first alive enemy in the list (current behavior)
        for (Enemy enemy : enemiesInRange) {
            if (enemy.isAlive()) {
                return enemy;
            }
        }
        return null; // No valid target found
    }
    
    @Override
    public String getStrategyName() {
        return "First";
    }
} 
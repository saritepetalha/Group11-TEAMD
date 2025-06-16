package strategies;

import enemies.Enemy;
import java.util.List;

/**
 * Targeting strategy that selects the enemy furthest from the exit.
 * This targets enemies with the lowest currentPathIndex (just entered the path).
 */
public class FirstEnemyStrategy implements TargetingStrategy {

    @Override
    public Enemy selectTarget(List<Enemy> enemiesInRange, Object tower) {
        Enemy bestTarget = null;
        int lowestPathIndex = Integer.MAX_VALUE;

        for (Enemy enemy : enemiesInRange) {
            if (enemy.isAlive()) {
                int enemyPathIndex = enemy.getCurrentPathIndex();

                // Select enemy with lowest path index (furthest from exit)
                if (enemyPathIndex < lowestPathIndex) {
                    lowestPathIndex = enemyPathIndex;
                    bestTarget = enemy;
                }
            }
        }

        return bestTarget;
    }

    @Override
    public String getStrategyName() {
        return "First";
    }
} 
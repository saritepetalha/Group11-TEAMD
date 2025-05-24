package strategies;

import enemies.Enemy;
import java.util.List;

/**
 * Strategy interface for tower targeting behavior.
 * Implementations define how towers select which enemy to attack.
 */
public interface TargetingStrategy {
    
    /**
     * Selects the best target from a list of enemies within range.
     * 
     * @param enemiesInRange List of enemies that are within the tower's attack range
     * @param tower The tower that is selecting a target
     * @return The enemy to target, or null if no suitable target is found
     */
    Enemy selectTarget(List<Enemy> enemiesInRange, Object tower);
    
    /**
     * Returns the name of this targeting strategy for display purposes.
     * 
     * @return A human-readable name for this strategy
     */
    String getStrategyName();
} 
package strategies;

/**
 * Factory class for creating and managing targeting strategies.
 * Provides easy access to all available targeting strategies.
 */
public class TargetingStrategyFactory {
    
    // Strategy types enum for easy identification
    public enum StrategyType {
        FIRST,
        LAST, 
        STRONGEST,
        WEAKEST
    }
    
    // Singleton instances of strategies (stateless, so can be reused)
    private static final TargetingStrategy FIRST_ENEMY_STRATEGY = new FirstEnemyStrategy();
    private static final TargetingStrategy LAST_ENEMY_STRATEGY = new LastEnemyStrategy();
    private static final TargetingStrategy STRONGEST_ENEMY_STRATEGY = new StrongestEnemyStrategy();
    private static final TargetingStrategy WEAKEST_ENEMY_STRATEGY = new WeakestEnemyStrategy();
    
    /**
     * Creates a targeting strategy based on the specified type.
     * 
     * @param type The type of targeting strategy to create
     * @return A targeting strategy instance
     */
    public static TargetingStrategy createStrategy(StrategyType type) {
        switch (type) {
            case FIRST:
                return FIRST_ENEMY_STRATEGY;
            case LAST:
                return LAST_ENEMY_STRATEGY;
            case STRONGEST:
                return STRONGEST_ENEMY_STRATEGY;
            case WEAKEST:
                return WEAKEST_ENEMY_STRATEGY;
            default:
                return FIRST_ENEMY_STRATEGY; // Default fallback
        }
    }
    
    /**
     * Gets the default targeting strategy (First Enemy).
     * 
     * @return The default targeting strategy
     */
    public static TargetingStrategy getDefaultStrategy() {
        return FIRST_ENEMY_STRATEGY;
    }
    
    /**
     * Gets all available strategy types.
     * 
     * @return Array of all available strategy types
     */
    public static StrategyType[] getAllStrategyTypes() {
        return StrategyType.values();
    }
    
    /**
     * Gets a human-readable name for a strategy type.
     * 
     * @param type The strategy type
     * @return Human-readable name
     */
    public static String getStrategyDisplayName(StrategyType type) {
        TargetingStrategy strategy = createStrategy(type);
        return strategy.getStrategyName();
    }
} 
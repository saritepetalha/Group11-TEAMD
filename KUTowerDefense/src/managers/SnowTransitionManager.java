package managers;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import helpMethods.LoadSave;

/**
 * Manages snow tileset transitions for enhanced visual effects during snowy weather.
 * Handles the transition from normal tiles -> medium snow -> full snow.
 */
public class SnowTransitionManager {

    public enum SnowState {
        NORMAL,           // No snow effect
        TRANSITIONING,    // Currently in transition to medium snow
        MEDIUM_SNOW,      // Medium snow tileset active
        FULL_SNOW         // Full snow tileset active
    }

    private static final float MEDIUM_SNOW_DURATION = 4.0f; // 4 seconds

    private SnowState currentState = SnowState.NORMAL;
    private float transitionTimer = 0.0f;
    private boolean isSnowing = false;
    private boolean lastSnowingState = false;

    private Map<Integer, BufferedImage> mediumSnowTiles = new HashMap<>();
    private Map<Integer, BufferedImage> fullSnowTiles = new HashMap<>();
    private Map<String, BufferedImage> spriteCache = new HashMap<>();

    public SnowTransitionManager() {
        loadSnowTilesets();
    }

    /**
     * Loads the snow tilesets using LoadSave utility
     */
    private void loadSnowTilesets() {
        boolean success = LoadSave.loadSnowTilesets(mediumSnowTiles, fullSnowTiles, spriteCache);
        if (!success) {
            System.err.println("Warning: Some snow tilesets failed to load. Snow effects may not work properly.");
        }
    }

    /**
     * Updates the snow transition state based on weather conditions
     */
    public void update(float deltaTime, boolean isCurrentlySnowing) {
        this.isSnowing = isCurrentlySnowing;

        if (isSnowing != lastSnowingState) {
            handleWeatherChange();
            lastSnowingState = isSnowing;
        }

        if (currentState == SnowState.TRANSITIONING) {
            transitionTimer += deltaTime;

            if (transitionTimer >= MEDIUM_SNOW_DURATION) {
                currentState = SnowState.FULL_SNOW;
                transitionTimer = 0.0f;
                System.out.println("Snow transition: Medium -> Full Snow");
            }
        }

        if (!isSnowing && (currentState == SnowState.MEDIUM_SNOW || currentState == SnowState.FULL_SNOW)) {
            clearSnowEffect();
        }
    }

    /**
     * Handles weather state changes
     */
    private void handleWeatherChange() {
        if (isSnowing && currentState == SnowState.NORMAL) {
            currentState = SnowState.TRANSITIONING;
            transitionTimer = 0.0f;
            System.out.println("Snow transition: Starting snow effect");
        }
    }

    /**
     * Clears snow effects when weather stops
     */
    private void clearSnowEffect() {
        currentState = SnowState.NORMAL;
        transitionTimer = 0.0f;
        System.out.println("Snow transition: Clearing snow effect");
    }

    /**
     * Gets the appropriate sprite for a tile based on current snow state
     */
    public BufferedImage getSnowSprite(int tileId, BufferedImage originalSprite) {
        if (currentState == SnowState.NORMAL) {
            return originalSprite;
        }

        if (tileId < 0) {
            return originalSprite;
        }

        // Special handling for grass tile
        if (tileId == 5) {
            return getSnowGrassSprite();
        }

        // Special handling for four-way road tile (ID 32)
        if (tileId == 32) {
            return getFourWayRoadSnowSprite();
        }

        BufferedImage snowSprite = null;

        if (currentState == SnowState.TRANSITIONING || currentState == SnowState.MEDIUM_SNOW) {
            snowSprite = getSnowSpriteFromCache("medium", tileId);
        } else if (currentState == SnowState.FULL_SNOW) {
            snowSprite = getSnowSpriteFromCache("full", tileId);
        }

        return snowSprite != null ? snowSprite : originalSprite;
    }

    /**
     * Gets the snowy grass sprite for the base layer based on current snow state
     */
    private BufferedImage getSnowGrassSprite() {
        BufferedImage snowGrass = null;

        if (currentState == SnowState.TRANSITIONING || currentState == SnowState.MEDIUM_SNOW) {
            snowGrass = getSnowSpriteFromCache("medium", 5);
        } else if (currentState == SnowState.FULL_SNOW) {
            snowGrass = getSnowSpriteFromCache("full", 5);
        }

        return snowGrass;
    }

    /**
     * Gets the snowy four-way road sprite based on current snow state
     */
    private BufferedImage getFourWayRoadSnowSprite() {
        BufferedImage snowFourWay = null;

        if (currentState == SnowState.TRANSITIONING || currentState == SnowState.MEDIUM_SNOW) {
            snowFourWay = spriteCache.get("medium_fourway");
        } else if (currentState == SnowState.FULL_SNOW) {
            snowFourWay = spriteCache.get("full_fourway");
        }

        return snowFourWay;
    }

    /**
     * Gets a snow sprite from cache
     */
    private BufferedImage getSnowSpriteFromCache(String type, int tileId) {
        String cacheKey = type + "_" + tileId;
        return spriteCache.get(cacheKey);
    }

    public SnowState getCurrentState() {
        return currentState;
    }

    public boolean isSnowEffectActive() {
        return currentState != SnowState.NORMAL;
    }

    public float getTransitionProgress() {
        if (currentState == SnowState.TRANSITIONING) {
            return Math.min(transitionTimer / MEDIUM_SNOW_DURATION, 1.0f);
        }
        return currentState == SnowState.NORMAL ? 0.0f : 1.0f;
    }

    public String getStateDescription() {
        switch (currentState) {
            case NORMAL: return "No Snow";
            case TRANSITIONING: return "Transitioning to Snow";
            case MEDIUM_SNOW: return "Medium Snow";
            case FULL_SNOW: return "Full Snow";
            default: return "Unknown";
        }
    }
} 
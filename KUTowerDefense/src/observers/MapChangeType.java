package observers;

/**
 * Types of map changes that can occur
 */
public enum MapChangeType {
    TERRAIN_CHANGED,    // Tile placement/removal
    OVERLAY_CHANGED,    // Start/end point changes
    FULL_MAP_CHANGED    // Complete map reset/load
} 
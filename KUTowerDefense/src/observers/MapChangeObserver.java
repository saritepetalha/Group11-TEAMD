package observers;

import models.MapModel;

/**
 * Observer interface for map changes
 * Allows views and other components to be notified when the map changes
 */
public interface MapChangeObserver {
    void onMapChanged(MapChangeType changeType, int x, int y, MapModel mapModel);
} 
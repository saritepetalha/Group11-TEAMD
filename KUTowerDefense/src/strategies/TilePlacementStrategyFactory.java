package strategies;

import objects.Tile;
import strategies.impl.CastlePlacementStrategy;
import strategies.impl.ErasePlacementStrategy;
import strategies.impl.RegularTilePlacementStrategy;
import strategies.impl.RoadPlacementStrategy;

/**
 * Factory for creating appropriate tile placement strategies
 */
public class TilePlacementStrategyFactory {
    
    public TilePlacementStrategy getStrategy(Tile selectedTile, String mode) {
        if (mode.equals("Erase")) {
            return new ErasePlacementStrategy();
        }
        
        if (selectedTile == null) {
            return new RegularTilePlacementStrategy();
        }
        
        if (selectedTile.getName().equals("Castle")) {
            return new CastlePlacementStrategy();
        }
        
        if (selectedTile.getName().contains("Road")) {
            return new RoadPlacementStrategy();
        }
        
        return new RegularTilePlacementStrategy();
    }
} 
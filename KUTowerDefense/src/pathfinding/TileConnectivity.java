package pathfinding;

import java.util.*;

/**
 * Defines connectivity rules for different tile types.
 * Each tile type has specific entry and exit directions.
 */
public class TileConnectivity {
    
    /**
     * Enum representing the four cardinal directions
     */
    public enum Direction {
        NORTH(0, -1),
        SOUTH(0, 1),
        EAST(1, 0),
        WEST(-1, 0);
        
        public final int dx;
        public final int dy;
        
        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }
        
        public Direction getOpposite() {
            switch (this) {
                case NORTH: return SOUTH;
                case SOUTH: return NORTH;
                case EAST: return WEST;
                case WEST: return EAST;
                default: return null;
            }
        }
    }
    
    /**
     * Map storing connectivity information for each tile type.
     * Key: Tile ID, Value: Set of directions this tile connects to
     */
    private static final Map<Integer, Set<Direction>> TILE_CONNECTIONS = new HashMap<>();
    
    static {
        // Initialize connectivity rules for each tile type based on actual visual appearance
        
        // ROW 0 (Top row in atlas):
        // ID 0: (0,0) CurvedRoadLeftDown - curves from bottom to right (SOUTH ↔ EAST)
        TILE_CONNECTIONS.put(0, Set.of(Direction.SOUTH, Direction.EAST));     // CurvedRoadLeftDown
        
        // ID 1: (1,0) CurvedRoadNorth - vertical straight road (NORTH ↔ SOUTH)
        TILE_CONNECTIONS.put(1, Set.of(Direction.EAST, Direction.WEST));    // CurvedRoadNorth (vertical)
        
        // ID 2: (2,0) CurvedRoadRightDown - curves from bottom to left (SOUTH ↔ WEST)
        TILE_CONNECTIONS.put(2, Set.of(Direction.SOUTH, Direction.WEST));     // CurvedRoadRightDown
        
        // ID 3: (3,0) FlatRoadUp - dead-end road, entry from SOUTH only (closed on north)
        TILE_CONNECTIONS.put(3, Set.of(Direction.SOUTH));    // FlatRoadUp (dead-end)
        
        // ROW 1:
        // ID 4: (0,1) CurvedRoadWest - curves from right to top (EAST ↔ NORTH)
        TILE_CONNECTIONS.put(4, Set.of(Direction.SOUTH, Direction.NORTH));     // CurvedRoadWest
        
        // ID 5: (1,1) Grass - no connections
        // (Grass has no connections)
        
        // ID 6: (2,1) CurvedRoadEast - curves from left to top (WEST ↔ NORTH)
        TILE_CONNECTIONS.put(6, Set.of(Direction.SOUTH, Direction.NORTH));     // CurvedRoadEast
        
        // ID 7: (3,1) FlatRoadVertical - vertical road (NORTH ↔ SOUTH)
        TILE_CONNECTIONS.put(7, Set.of(Direction.NORTH, Direction.SOUTH));    // FlatRoadVertical
        
        // ROW 2:
        // ID 8: (0,2) CurvedRoadLeftUp - curves from right to bottom (EAST ↔ SOUTH)
        TILE_CONNECTIONS.put(8, Set.of(Direction.EAST, Direction.NORTH));     // CurvedRoadLeftUp
        
        // ID 9: (1,2) CurvedRoadSouth - vertical straight road (NORTH ↔ SOUTH)
        TILE_CONNECTIONS.put(9, Set.of(Direction.EAST, Direction.WEST));    // CurvedRoadSouth (vertical)
        
        // ID 10: (2,2) CurvedRoadRightUp - curves from left to bottom (WEST ↔ SOUTH)
        TILE_CONNECTIONS.put(10, Set.of(Direction.WEST, Direction.NORTH));    // CurvedRoadRightUp
        
        // ID 11: (3,2) FlatRoadDown - dead-end road, entry from NORTH only (closed on south)
        TILE_CONNECTIONS.put(11, Set.of(Direction.NORTH));   // FlatRoadDown (dead-end)
        
        // ROW 3 (Bottom row):
        // ID 12: (0,3) FlatRoadLeft - dead-end road, entry from EAST only (closed on west)
        TILE_CONNECTIONS.put(12, Set.of(Direction.EAST));     // FlatRoadLeft (dead-end)
        
        // ID 13: (1,3) FlatRoadHorizontal - horizontal road (EAST ↔ WEST)
        TILE_CONNECTIONS.put(13, Set.of(Direction.EAST, Direction.WEST));     // FlatRoadHorizontal
        
        // ID 14: (2,3) FlatRoadRight - dead-end road, entry from WEST only (closed on east)
        TILE_CONNECTIONS.put(14, Set.of(Direction.WEST));     // FlatRoadRight (dead-end)
        
        // Four-way road connects all directions
        TILE_CONNECTIONS.put(35, Set.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)); // RoadFourWay
        
        // Special tiles
        TILE_CONNECTIONS.put(-4, Set.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)); // Gate (can connect from any direction)
    }
    
    /**
     * Get the directions a tile connects to
     * @param tileId The ID of the tile
     * @return Set of directions this tile connects to, empty set if not a road tile
     */
    public static Set<Direction> getConnections(int tileId) {
        return TILE_CONNECTIONS.getOrDefault(tileId, Collections.emptySet());
    }
    
    /**
     * Check if a tile is a road tile (has any connections)
     * @param tileId The ID of the tile
     * @return true if the tile is a road tile
     */
    public static boolean isRoadTile(int tileId) {
        return TILE_CONNECTIONS.containsKey(tileId);
    }
    
    /**
     * Check if two adjacent tiles can connect to each other
     * @param fromTileId ID of the source tile
     * @param toTileId ID of the destination tile
     * @param direction Direction from source to destination (direction of movement)
     * @return true if the tiles can connect in the given direction
     */
    public static boolean canConnect(int fromTileId, int toTileId, Direction direction) {
        Set<Direction> fromConnections = getConnections(fromTileId);
        Set<Direction> toConnections = getConnections(toTileId);
        
        // Check if source tile has an exit in the given direction
        // and destination tile has an entry from the opposite direction
        // For example: if moving EAST, source needs EAST exit, destination needs WEST entry
        return fromConnections.contains(direction) && 
               toConnections.contains(direction.getOpposite());
    }
    
    /**
     * Get all valid directions from a tile position
     * @param tileId ID of the tile
     * @return Set of valid exit directions
     */
    public static Set<Direction> getValidExitDirections(int tileId) {
        return getConnections(tileId);
    }
} 
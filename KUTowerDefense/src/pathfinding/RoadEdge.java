package pathfinding;

import pathfinding.TileConnectivity.Direction;

/**
 * Represents an edge in the road network graph.
 * Each edge connects two adjacent road tiles that are compatible.
 */
public class RoadEdge {
    private final RoadNode destination;
    private final Direction direction;
    private final double weight;
    
    public RoadEdge(RoadNode destination, Direction direction, double weight) {
        this.destination = destination;
        this.direction = direction;
        this.weight = weight;
    }
    
    /**
     * Constructor with default weight of 1.0
     * @param destination The destination node
     * @param direction The direction of travel
     */
    public RoadEdge(RoadNode destination, Direction direction) {
        this(destination, direction, 1.0);
    }
    
    public RoadNode getDestination() {
        return destination;
    }
    
    public Direction getDirection() {
        return direction;
    }
    
    public double getWeight() {
        return weight;
    }
    
    @Override
    public String toString() {
        return String.format("RoadEdge{to=%s, dir=%s, weight=%.1f}", 
                destination.getPosition(), direction, weight);
    }
} 
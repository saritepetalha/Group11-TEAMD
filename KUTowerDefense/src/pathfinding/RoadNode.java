package pathfinding;

import objects.GridPoint;
import java.util.*;

/**
 * Represents a node in the road network graph.
 * Each node corresponds to a tile on the game map.
 */
public class RoadNode {
    private final GridPoint position;
    private final int tileId;
    private final List<RoadEdge> outgoingEdges;
    private double gScore; // Distance from start node (for A* algorithm)
    private double fScore; // gScore + heuristic (for A* algorithm)
    private RoadNode parent; // Parent node in the path (for path reconstruction)
    
    public RoadNode(GridPoint position, int tileId) {
        this.position = position;
        this.tileId = tileId;
        this.outgoingEdges = new ArrayList<>();
        this.gScore = Double.POSITIVE_INFINITY;
        this.fScore = Double.POSITIVE_INFINITY;
        this.parent = null;
    }
    
    /**
     * Add an outgoing edge to this node
     * @param edge The edge to add
     */
    public void addEdge(RoadEdge edge) {
        outgoingEdges.add(edge);
    }
    
    /**
     * Get all outgoing edges from this node
     * @return List of outgoing edges
     */
    public List<RoadEdge> getOutgoingEdges() {
        return new ArrayList<>(outgoingEdges);
    }
    
    /**
     * Reset the pathfinding data for this node
     */
    public void reset() {
        this.gScore = Double.POSITIVE_INFINITY;
        this.fScore = Double.POSITIVE_INFINITY;
        this.parent = null;
    }
    
    // Getters and setters
    public GridPoint getPosition() {
        return position;
    }
    
    public int getTileId() {
        return tileId;
    }
    
    public double getGScore() {
        return gScore;
    }
    
    public void setGScore(double gScore) {
        this.gScore = gScore;
    }
    
    public double getFScore() {
        return fScore;
    }
    
    public void setFScore(double fScore) {
        this.fScore = fScore;
    }
    
    public RoadNode getParent() {
        return parent;
    }
    
    public void setParent(RoadNode parent) {
        this.parent = parent;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RoadNode roadNode = (RoadNode) obj;
        return Objects.equals(position, roadNode.position);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(position);
    }
    
    @Override
    public String toString() {
        return String.format("RoadNode{pos=%s, tileId=%d}", position, tileId);
    }
} 
package pathfinding;

import objects.GridPoint;
import pathfinding.TileConnectivity.Direction;
import java.util.*;

/**
 * Graph-based pathfinding system that respects road tile connectivity.
 * Uses A* algorithm to find optimal paths through connected road tiles.
 */
public class RoadNetworkPathfinder {
    
    private final Map<GridPoint, RoadNode> nodeMap;
    private final int mapWidth;
    private final int mapHeight;
    
    public RoadNetworkPathfinder(int mapWidth, int mapHeight) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.nodeMap = new HashMap<>();
    }
    
    /**
     * Build the road network graph from tile data
     * @param tileData 2D array of tile IDs
     */
    public void buildGraph(int[][] tileData) {
        nodeMap.clear();
        
        // First pass: Create nodes for all road tiles
        for (int y = 0; y < tileData.length; y++) {
            for (int x = 0; x < tileData[y].length; x++) {
                int tileId = tileData[y][x];
                if (TileConnectivity.isRoadTile(tileId)) {
                    GridPoint position = new GridPoint(x, y);
                    RoadNode node = new RoadNode(position, tileId);
                    nodeMap.put(position, node);
                }
            }
        }
        
        // Second pass: Create edges between connected tiles
        for (RoadNode node : nodeMap.values()) {
            createEdgesForNode(node, tileData);
        }
        
        System.out.println("Road network built with " + nodeMap.size() + " nodes");
    }
    
    /**
     * Create edges for a given node based on its tile connectivity
     * @param node The node to create edges for
     * @param tileData The tile data array
     */
    private void createEdgesForNode(RoadNode node, int[][] tileData) {
        GridPoint pos = node.getPosition();
        int tileId = node.getTileId();
        
        // Get valid exit directions for this tile
        Set<Direction> exitDirections = TileConnectivity.getValidExitDirections(tileId);
        
        for (Direction direction : exitDirections) {
            int neighborX = pos.getX() + direction.dx;
            int neighborY = pos.getY() + direction.dy;
            
            // Check bounds
            if (neighborX >= 0 && neighborX < mapWidth && 
                neighborY >= 0 && neighborY < mapHeight) {
                
                GridPoint neighborPos = new GridPoint(neighborX, neighborY);
                RoadNode neighborNode = nodeMap.get(neighborPos);
                
                if (neighborNode != null) {
                    int neighborTileId = neighborNode.getTileId();
                    
                    // Check if the tiles can actually connect
                    if (TileConnectivity.canConnect(tileId, neighborTileId, direction)) {
                        RoadEdge edge = new RoadEdge(neighborNode, direction);
                        node.addEdge(edge);
                    }
                }
            }
        }
    }
    
    /**
     * Find a path from start to end using A* algorithm
     * @param start Starting position
     * @param end Ending position
     * @return List of GridPoints representing the path, or empty list if no path found
     */
    public List<GridPoint> findPath(GridPoint start, GridPoint end) {
        RoadNode startNode = nodeMap.get(start);
        RoadNode endNode = nodeMap.get(end);
        
        if (startNode == null || endNode == null) {
            System.out.println("Start or end position is not on a road tile");
            return Collections.emptyList();
        }
        
        return findPathAStar(startNode, endNode);
    }
    
    /**
     * A* pathfinding algorithm implementation
     * @param startNode Starting node
     * @param endNode Target node
     * @return List of GridPoints representing the path
     */
    private List<GridPoint> findPathAStar(RoadNode startNode, RoadNode endNode) {
        // Reset all nodes
        for (RoadNode node : nodeMap.values()) {
            node.reset();
        }
        
        PriorityQueue<RoadNode> openSet = new PriorityQueue<>(
            Comparator.comparingDouble(RoadNode::getFScore)
        );
        Set<RoadNode> closedSet = new HashSet<>();
        
        // Initialize start node
        startNode.setGScore(0);
        startNode.setFScore(heuristic(startNode, endNode));
        openSet.add(startNode);
        
        while (!openSet.isEmpty()) {
            RoadNode current = openSet.poll();
            
            if (current.equals(endNode)) {
                return reconstructPath(current);
            }
            
            closedSet.add(current);

            for (RoadEdge edge : current.getOutgoingEdges()) {
                RoadNode neighbor = edge.getDestination();
                
                if (closedSet.contains(neighbor)) {
                    continue; 
                }
                
                double tentativeGScore = current.getGScore() + edge.getWeight();
                
                if (tentativeGScore < neighbor.getGScore()) {
                    neighbor.setParent(current);
                    neighbor.setGScore(tentativeGScore);
                    neighbor.setFScore(tentativeGScore + heuristic(neighbor, endNode));
                    
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }
        
        System.out.println("No path found from " + startNode.getPosition() + " to " + endNode.getPosition());
        return Collections.emptyList(); 
    }
    
    /**
     * Calculate heuristic distance between two nodes (Manhattan distance)
     * @param node1 First node
     * @param node2 Second node
     * @return Heuristic distance
     */
    private double heuristic(RoadNode node1, RoadNode node2) {
        GridPoint pos1 = node1.getPosition();
        GridPoint pos2 = node2.getPosition();
        return Math.abs(pos1.getX() - pos2.getX()) + Math.abs(pos1.getY() - pos2.getY());
    }
    
    /**
     * Reconstruct the path from end node to start node
     * @param endNode The end node with parent pointers set
     * @return List of GridPoints representing the path from start to end
     */
    private List<GridPoint> reconstructPath(RoadNode endNode) {
        List<GridPoint> path = new ArrayList<>();
        RoadNode current = endNode;
        
        while (current != null) {
            path.add(current.getPosition());
            current = current.getParent();
        }
        
        Collections.reverse(path);
        System.out.println("Path found with " + path.size() + " points");
        return path;
    }
    
    /**
     * Get the node at a specific position
     * @param position The grid position
     * @return The road node at that position, or null if none exists
     */
    public RoadNode getNode(GridPoint position) {
        return nodeMap.get(position);
    }
    
    /**
     * Check if a position has a road node
     * @param position The grid position
     * @return true if there's a road node at this position
     */
    public boolean hasNode(GridPoint position) {
        return nodeMap.containsKey(position);
    }
    
    /**
     * Get debugging information about the graph
     * @return String containing graph statistics
     */
    public String getGraphInfo() {
        int totalEdges = nodeMap.values().stream()
                .mapToInt(node -> node.getOutgoingEdges().size())
                .sum();
        
        return String.format("Graph: %d nodes, %d edges", nodeMap.size(), totalEdges);
    }
} 
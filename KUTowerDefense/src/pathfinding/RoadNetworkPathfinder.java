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

    private final Set<Set<GridPoint>> detectedLoops;
    private final Map<GridPoint, Set<Set<GridPoint>>> nodeToLoops;

    public RoadNetworkPathfinder(int mapWidth, int mapHeight) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.nodeMap = new HashMap<>();
        this.detectedLoops = new HashSet<>();
        this.nodeToLoops = new HashMap<>();
    }

    /**
     * Build the road network graph and detect all loops
     */
    public void buildGraph(int[][] tileData) {
        nodeMap.clear();
        detectedLoops.clear();
        nodeToLoops.clear();

        // First pass: create all road nodes
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                if (TileConnectivity.isRoadTile(tileData[y][x])) {
                    GridPoint position = new GridPoint(x, y);
                    RoadNode node = new RoadNode(position, tileData[y][x]);
                    nodeMap.put(position, node);
                }
            }
        }

        // Second pass: create connections between adjacent road nodes
        for (RoadNode node : nodeMap.values()) {
            createEdgesForNode(node, tileData);
        }

        // Third pass: detect all loops in the network
        detectAllLoops();

        System.out.println("Road network built with " + nodeMap.size() + " nodes and " + detectedLoops.size() + " loops detected.");
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
     * Detect all loops/cycles in the road network
     */
    private void detectAllLoops() {
        Set<RoadNode> visited = new HashSet<>();
        Set<RoadNode> recursionStack = new HashSet<>();

        for (RoadNode node : nodeMap.values()) {
            if (!visited.contains(node)) {
                findLoopsFromNode(node, visited, recursionStack, new ArrayList<>());
            }
        }

        // Create mapping from nodes to their loops
        for (Set<GridPoint> loop : detectedLoops) {
            for (GridPoint point : loop) {
                nodeToLoops.computeIfAbsent(point, k -> new HashSet<>()).add(loop);
            }
        }
    }

    /**
     * DFS-based loop detection
     */
    private void findLoopsFromNode(RoadNode current, Set<RoadNode> visited,
                                   Set<RoadNode> recursionStack, List<RoadNode> currentPath) {
        visited.add(current);
        recursionStack.add(current);
        currentPath.add(current);

        for (RoadEdge edge : current.getOutgoingEdges()) {
            RoadNode neighbor = edge.getDestination();
            if (!visited.contains(neighbor)) {
                findLoopsFromNode(neighbor, visited, recursionStack, currentPath);
            } else if (recursionStack.contains(neighbor)) {
                // Found a cycle - extract the loop
                int loopStartIndex = currentPath.indexOf(neighbor);
                if (loopStartIndex != -1) {
                    List<RoadNode> loopNodes = currentPath.subList(loopStartIndex, currentPath.size());
                    if (loopNodes.size() >= 3) { // Only consider loops with at least 3 nodes
                        Set<GridPoint> loop = new HashSet<>();
                        for (RoadNode node : loopNodes) {
                            loop.add(node.getPosition());
                        }
                        detectedLoops.add(loop);
                    }
                }
            }
        }

        recursionStack.remove(current);
        currentPath.remove(currentPath.size() - 1);
    }

    /**
     * Find path with loop traversal - enemies will take loops at least once
     */
    public List<GridPoint> findPath(GridPoint start, GridPoint end) {
        RoadNode startNode = nodeMap.get(start);
        RoadNode endNode = nodeMap.get(end);

        if (startNode == null || endNode == null) {
            return new ArrayList<>();
        }

        // First, find the basic shortest path
        List<GridPoint> basicPath = findPathAStar(startNode, endNode);

        if (basicPath.isEmpty()) {
            return basicPath;
        }

        // Then, modify the path to include any loops that should be traversed
        return includeLoopTraversal(basicPath, start, end);
    }

    /**
     * Modify the path to include loop traversal with directional awareness
     */
    private List<GridPoint> includeLoopTraversal(List<GridPoint> basicPath, GridPoint start, GridPoint end) {
        List<GridPoint> enhancedPath = new ArrayList<>();
        Set<Set<GridPoint>> traversedLoops = new HashSet<>();

        for (int i = 0; i < basicPath.size(); i++) {
            GridPoint currentPoint = basicPath.get(i);
            enhancedPath.add(currentPoint);

            // Check if this point is part of any loops
            Set<Set<GridPoint>> loopsAtPoint = nodeToLoops.get(currentPoint);
            if (loopsAtPoint != null) {
                for (Set<GridPoint> loop : loopsAtPoint) {
                    // Only traverse each loop once, and only if we haven't already
                    if (!traversedLoops.contains(loop) && shouldTraverseLoop(loop, basicPath, i)) {
                        traversedLoops.add(loop);

                        // Determine movement direction for directional loop traversal
                        TileConnectivity.Direction movementDirection = getApproachDirection(basicPath, i);
                        List<GridPoint> loopPath = createDirectionalLoopTraversalPath(currentPoint, loop, movementDirection);

                        if (!loopPath.isEmpty()) {
                            // Add the loop path (excluding the current point to avoid duplication)
                            enhancedPath.addAll(loopPath.subList(1, loopPath.size()));
                            System.out.println("Enemy will continue " + movementDirection +
                                    " initially, then naturally traverse the loop from " + currentPoint);
                        }
                    }
                }
            }
        }

        return enhancedPath;
    }

    /**
     * Determine the direction the enemy is moving when approaching the current intersection
     */
    private TileConnectivity.Direction getApproachDirection(List<GridPoint> path, int currentIndex) {
        if (currentIndex == 0) {
            // If this is the first point, look at next point to determine initial direction
            if (currentIndex + 1 < path.size()) {
                GridPoint current = path.get(currentIndex);
                GridPoint next = path.get(currentIndex + 1);
                return getDirectionBetweenPoints(current, next);
            }
            return TileConnectivity.Direction.EAST; // Default
        }

        // Determine movement direction from previous point to current point
        GridPoint previous = path.get(currentIndex - 1);
        GridPoint current = path.get(currentIndex);
        return getDirectionBetweenPoints(previous, current);
    }

    /**
     * Get the direction from point A to point B
     */
    private TileConnectivity.Direction getDirectionBetweenPoints(GridPoint from, GridPoint to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();

        if (dx > 0) return TileConnectivity.Direction.EAST;
        if (dx < 0) return TileConnectivity.Direction.WEST;
        if (dy > 0) return TileConnectivity.Direction.SOUTH;
        if (dy < 0) return TileConnectivity.Direction.NORTH;

        return TileConnectivity.Direction.EAST; // Default fallback
    }

    /**
     * Create a directional loop traversal path that continues in current direction initially
     * then finds a natural path through the loop
     */
    private List<GridPoint> createDirectionalLoopTraversalPath(GridPoint startPoint, Set<GridPoint> loop,
                                                               TileConnectivity.Direction movementDirection) {
        List<GridPoint> loopPath = new ArrayList<>();

        if (!loop.contains(startPoint)) {
            return loopPath; // Can't traverse loop if start point isn't in it
        }

        RoadNode startNode = nodeMap.get(startPoint);
        if (startNode == null) return loopPath;

        // Try to continue in current direction first, then find natural path through loop
        List<GridPoint> cycle = findDirectedLoopCycle(startNode, loop, movementDirection);

        return cycle;
    }

    /**
     * Find a cycle in the loop, starting by continuing in the current direction
     */
    private List<GridPoint> findDirectedLoopCycle(RoadNode startNode, Set<GridPoint> loop,
                                                  TileConnectivity.Direction initialDirection) {
        List<GridPoint> path = new ArrayList<>();
        Set<GridPoint> visited = new HashSet<>();
        RoadNode current = startNode;

        // Add starting point
        path.add(current.getPosition());
        visited.add(current.getPosition());

        // First, try to continue in the initial direction if possible
        RoadNode nextNode = findNextNodeInDirection(current, loop, initialDirection, visited);

        if (nextNode != null) {
            // Continue in initial direction for first move
            path.add(nextNode.getPosition());
            visited.add(nextNode.getPosition());
            current = nextNode;
        }

        // Now find any path through the remaining loop back to start
        return completeLoopNaturally(current, startNode, loop, visited, path);
    }

    /**
     * Find the next node in the specified direction within the loop
     */
    private RoadNode findNextNodeInDirection(RoadNode current, Set<GridPoint> loop,
                                             TileConnectivity.Direction direction, Set<GridPoint> visited) {
        for (RoadEdge edge : current.getOutgoingEdges()) {
            RoadNode neighbor = edge.getDestination();
            GridPoint neighborPos = neighbor.getPosition();

            // Check if neighbor is in the loop and in the right direction
            if (loop.contains(neighborPos) && edge.getDirection() == direction && !visited.contains(neighborPos)) {
                return neighbor;
            }
        }
        return null;
    }

    /**
     * Complete the loop naturally by following available connections
     */
    private List<GridPoint> completeLoopNaturally(RoadNode current, RoadNode startNode, Set<GridPoint> loop,
                                                  Set<GridPoint> visited, List<GridPoint> currentPath) {
        // Continue following natural connectivity until we return to start or run out of options
        for (int steps = 0; steps < loop.size() * 2 && current != null; steps++) {
            // Look for any available next node in the loop
            RoadNode nextNode = findAnyNextNodeInLoop(current, loop, visited);

            if (nextNode == null) {
                // Try to connect back to start if possible
                for (RoadEdge edge : current.getOutgoingEdges()) {
                    if (edge.getDestination().equals(startNode) && currentPath.size() > 2) {
                        currentPath.add(startNode.getPosition()); // Close the loop
                        return currentPath;
                    }
                }
                break; // No more moves possible
            }

            // Check if we've completed the loop (returned to start)
            if (nextNode.getPosition().equals(startNode.getPosition()) && currentPath.size() > 2) {
                currentPath.add(nextNode.getPosition()); // Close the loop
                return currentPath;
            }

            currentPath.add(nextNode.getPosition());
            visited.add(nextNode.getPosition());
            current = nextNode;
        }

        // If we couldn't complete naturally, try the original cycle finding method
        if (currentPath.size() < 3 || !currentPath.get(currentPath.size() - 1).equals(startNode.getPosition())) {
            return findSimpleCycleInLoop(startNode, loop);
        }

        return currentPath;
    }

    /**
     * Find any available next node in the loop
     */
    private RoadNode findAnyNextNodeInLoop(RoadNode current, Set<GridPoint> loop, Set<GridPoint> visited) {
        for (RoadEdge edge : current.getOutgoingEdges()) {
            RoadNode neighbor = edge.getDestination();
            GridPoint neighborPos = neighbor.getPosition();

            if (loop.contains(neighborPos) && !visited.contains(neighborPos)) {
                return neighbor;
            }
        }
        return null;
    }

    /**
     * Determine if a loop should be traversed based on path context
     */
    private boolean shouldTraverseLoop(Set<GridPoint> loop, List<GridPoint> basicPath, int currentIndex) {
        // Traverse loop if:
        // 1. We're at a four-way intersection (node has 4 neighbors)
        // 2. The loop hasn't been traversed yet
        // 3. We're not too close to the end of the path

        GridPoint currentPoint = basicPath.get(currentIndex);
        RoadNode currentNode = nodeMap.get(currentPoint);

        if (currentNode == null) return false;

        // Check if it's a major intersection (3+ connections)
        boolean isMajorIntersection = currentNode.getOutgoingEdges().size() >= 3;

        // Don't traverse loops if we're very close to the destination
        boolean notNearEnd = currentIndex < basicPath.size() - 3;

        // Check if the loop is actually connected to our current path
        boolean loopConnectedToPath = isLoopConnectedToPath(loop, basicPath, currentIndex);

        return isMajorIntersection && notNearEnd && loopConnectedToPath;
    }

    /**
     * Check if the loop is properly connected to the current path
     */
    private boolean isLoopConnectedToPath(Set<GridPoint> loop, List<GridPoint> basicPath, int currentIndex) {
        GridPoint currentPoint = basicPath.get(currentIndex);

        // The loop should contain the current point and be reachable
        if (!loop.contains(currentPoint)) return false;

        // Check if we can actually traverse the loop and return to the path
        RoadNode currentNode = nodeMap.get(currentPoint);
        if (currentNode == null) return false;

        // Count how many neighbors of current node are in the loop
        int loopNeighbors = 0;
        for (RoadEdge edge : currentNode.getOutgoingEdges()) {
            if (loop.contains(edge.getDestination().getPosition())) {
                loopNeighbors++;
            }
        }

        // We need at least 2 connections within the loop to traverse it
        return loopNeighbors >= 2;
    }

    /**
     * Find a path from start to end using A* algorithm
     * @param start Starting position
     * @param end Ending position
     * @return List of GridPoints representing the path, or empty list if no path found
     */
    public List<GridPoint> findPathAStar(RoadNode startNode, RoadNode endNode) {
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

    /**
     * Fallback method to find a simple cycle in the loop (original implementation)
     */
    private List<GridPoint> findSimpleCycleInLoop(RoadNode startNode, Set<GridPoint> loop) {
        List<GridPoint> path = new ArrayList<>();
        Set<GridPoint> visited = new HashSet<>();

        // Use the original cycle finding method as fallback
        return findCycleInLoop(startNode, loop, visited, new ArrayList<>());
    }

    /**
     * Find a complete cycle within a loop starting from a given node (original method)
     */
    private List<GridPoint> findCycleInLoop(RoadNode current, Set<GridPoint> loop,
                                            Set<GridPoint> visited, List<GridPoint> path) {
        GridPoint currentPos = current.getPosition();
        path.add(currentPos);
        visited.add(currentPos);

        // If we've visited enough nodes and can return to start, we have a cycle
        if (path.size() > 2) {
            for (RoadEdge edge : current.getOutgoingEdges()) {
                if (edge.getDestination().getPosition().equals(path.get(0)) && path.size() >= 3) {
                    // Found cycle back to start
                    List<GridPoint> cycle = new ArrayList<>(path);
                    cycle.add(path.get(0)); // Close the loop
                    return cycle;
                }
            }
        }

        // Continue exploring unvisited neighbors within the loop
        for (RoadEdge edge : current.getOutgoingEdges()) {
            GridPoint neighborPos = edge.getDestination().getPosition();
            if (loop.contains(neighborPos) && !visited.contains(neighborPos)) {
                List<GridPoint> result = findCycleInLoop(edge.getDestination(), loop, visited, path);
                if (!result.isEmpty()) {
                    return result;
                }
            }
        }

        // Backtrack
        path.remove(path.size() - 1);
        visited.remove(currentPos);

        return new ArrayList<>();
    }
} 
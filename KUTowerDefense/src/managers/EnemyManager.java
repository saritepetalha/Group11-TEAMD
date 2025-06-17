package managers;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import config.GameOptions;
import static constants.Constants.Enemies.BARREL;
import static constants.Constants.Enemies.GOBLIN;
import static constants.Constants.Enemies.TNT;
import static constants.Constants.Enemies.TROLL;
import static constants.Constants.Enemies.KNIGHT;
import static constants.Constants.PathPoints.END_POINT;
import static constants.Constants.PathPoints.START_POINT;
import constants.GameDimensions;
import enemies.Barrel;
import enemies.Enemy;
import enemies.Goblin;
import enemies.TNT;
import enemies.Troll;
import enemies.Knight;
import helpMethods.LoadSave;
import helpMethods.OptionsIO;
import objects.GridPoint;
import scenes.Playing;
import constants.Constants;
import pathfinding.RoadNetworkPathfinder;
import pathfinding.TileConnectivity;
import skills.SkillTree;
import skills.SkillType;

public class EnemyManager {
    // Performance constants
    private static final float CURVE_THRESHOLD_DISTANCE = 0.4f; // 40% of tile size
    private static final float COMBAT_SYNERGY_DISTANCE_SQUARED = 4096f; // 64 * 64
    private static final float EPSILON = 0.001f; // Small value for float comparisons
    private static final int GOLD_BAG_SPAWN_CHANCE = 50; // 50% chance

    private Playing playing;
    private static BufferedImage[] enemyImages;
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<GridPoint> pathPoints = new ArrayList<>();
    private GridPoint startPoint, endPoint;
    private int tileSize = GameDimensions.TILE_DISPLAY_SIZE;
    private int nextEnemyID = 0;
    private boolean pathFound = false;
    private static final Set<Integer> ROAD_IDS = Set.of(0,1,2,3,4,6,7,8,9,10,11,12,13,14,32);
    private GameOptions gameOptions;
    private WeatherManager weatherManager;
    private Map<Enemy, Long> enemySpawnTimes;
    private RoadNetworkPathfinder pathfinder;
    private int[][] tileData; // Store tile data for curve detection

    public EnemyManager(Playing playing, int[][] overlayData, int [][] tileData, GameOptions options) {
        this.playing = playing;
        this.gameOptions = options;
        this.tileData = tileData; // Store reference to tile data
        if (this.gameOptions == null) {
            System.out.println("Warning: Received null GameOptions in EnemyManager, using defaults.");
            this.gameOptions = GameOptions.defaults();
        }
        enemyImages = extractEnemyFrames();
        this.enemySpawnTimes = new HashMap<>();
        this.weatherManager = playing.getWeatherManager();

        this.pathfinder = new RoadNetworkPathfinder(tileData[0].length, tileData.length);
        this.pathfinder.buildGraph(tileData);

        findStartAndEndPoints(overlayData);

        if (startPoint != null && endPoint != null) {
            generatePath(tileData);
        }

        System.out.println("EnemyManager Initialized. Path found? " + pathFound);
        System.out.println(pathfinder.getGraphInfo());
    }

    private void findStartAndEndPoints(int[][] overlayData) {
        for (int y = 0; y < overlayData.length; y++) {
            for (int x = 0; x < overlayData[y].length; x++) {
                if (overlayData[y][x] == START_POINT) {
                    startPoint = new GridPoint(x, y);
                }
                else if (overlayData[y][x] == END_POINT) {
                    endPoint = new GridPoint(x, y);
                    if (isGateTile(x, y, overlayData)) {
                        GridPoint newEndPoint = findLastRoadTileBeforeGate(x, y, overlayData);
                        if (newEndPoint != null) {
                            endPoint = newEndPoint;
                        }
                    }
                }
            }
        }
    }

    private boolean isValidPosition(int x, int y, int rows, int cols) {
        return x >= 0 && x < cols && y >= 0 && y < rows;
    }

    private boolean isRoadTile(int tileId) {
        return TileConnectivity.isRoadTile(tileId);
    }

    private boolean isGateTile(int x, int y, int[][] overlayData) {
        return overlayData[y][x] == -4;
    }

    private GridPoint findLastRoadTileBeforeGate(int gateX, int gateY, int[][] overlayData) {
        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, -1, 0, 1};

        for (int i = 0; i < 4; i++) {
            int newX = gateX + dx[i];
            int newY = gateY + dy[i];

            if (isValidPosition(newX, newY, overlayData.length, overlayData[0].length) &&
                    isRoadTile(overlayData[newY][newX])) {
                return new GridPoint(newX, newY);
            }
        }
        return null;
    }

    /**
     * Generates a path from the start point to the end point using graph-based A* pathfinding.
     * This new implementation respects road tile connectivity, ensuring that enemies can only
     * move along properly connected road segments.
     *
     * @requires tileData != null && tileData.length > 0 && tileData[0].length > 0
     *           && startPoint != null && endPoint != null
     *           && 0 <= startPoint.getX() < tileData[0].length && 0 <= startPoint.getY() < tileData.length
     *           && 0 <= endPoint.getX() < tileData[0].length && 0 <= endPoint.getY() < tileData.length
     *
     * @modifies this.pathPoints, this.pathFound
     *
     * @effects If startPoint or endPoint is null, prints error message and returns without modification.
     *          Otherwise, uses graph-based A* pathfinding to find optimal path from startPoint to endPoint
     *          through connected road tiles only.
     *          If path is found:
     *            - this.pathPoints is updated to contain the sequence of GridPoints from startPoint to endPoint
     *            - this.pathFound is set to true
     *            - prints confirmation message with path length
     *          If no path exists:
     *            - this.pathPoints remains empty
     *            - this.pathFound remains false
     *            - prints error message with debugging information
     *          Only follows valid road connections based on tile connectivity rules.
     *
     * @param tileData 2D array representing the game map where each cell contains a tile type identifier
     */
    private void generatePath(int[][] tileData) {
        if (startPoint == null || endPoint == null) {
            System.out.println("Cannot generate path: Start or end point is null");
            return;
        }

        System.out.println("Generating graph-based path from " + startPoint + " to " + endPoint);

        // Use the new graph-based pathfinding system
        List<GridPoint> foundPath = pathfinder.findPath(startPoint, endPoint);

        if (!foundPath.isEmpty()) {
            pathPoints.clear();
            pathPoints.addAll(foundPath);
            pathFound = true;

            System.out.println("Graph-based path found with " + pathPoints.size() + " points");

            System.out.println("Path points:");
            for (int i = 0; i < pathPoints.size(); i++) {
                GridPoint point = pathPoints.get(i);
                int tileId = tileData[point.getY()][point.getX()];
                System.out.println("  " + i + ": " + point + " (tile " + tileId + ")");
            }

            validatePathConnectivity(tileData);

        } else {
            System.out.println("No graph-based path found!");
            pathFound = false;

            System.out.println("Start point: " + startPoint + " (has road node: " + pathfinder.hasNode(startPoint) + ")");
            System.out.println("End point: " + endPoint + " (has road node: " + pathfinder.hasNode(endPoint) + ")");

            if (!pathfinder.hasNode(startPoint)) {
                int startTileId = tileData[startPoint.getY()][startPoint.getX()];
                System.out.println("Start tile ID " + startTileId + " is not a road tile");
            }

            if (!pathfinder.hasNode(endPoint)) {
                int endTileId = tileData[endPoint.getY()][endPoint.getX()];
                System.out.println("End tile ID " + endTileId + " is not a road tile");
            }
        }
    }

    /**
     * Validates that the generated path follows proper road connectivity rules
     * @param tileData The tile data array
     */
    private void validatePathConnectivity(int[][] tileData) {
        if (pathPoints.size() < 2) return;

        System.out.println("Validating path connectivity...");
        boolean isValid = true;

        for (int i = 0; i < pathPoints.size() - 1; i++) {
            GridPoint current = pathPoints.get(i);
            GridPoint next = pathPoints.get(i + 1);

            int currentTileId = tileData[current.getY()][current.getX()];
            int nextTileId = tileData[next.getY()][next.getX()];

            // Determine direction from current to next
            int dx = next.getX() - current.getX();
            int dy = next.getY() - current.getY();

            TileConnectivity.Direction direction = null;
            if (dx == 1 && dy == 0) direction = TileConnectivity.Direction.EAST;
            else if (dx == -1 && dy == 0) direction = TileConnectivity.Direction.WEST;
            else if (dx == 0 && dy == 1) direction = TileConnectivity.Direction.SOUTH;
            else if (dx == 0 && dy == -1) direction = TileConnectivity.Direction.NORTH;

            if (direction == null || !TileConnectivity.canConnect(currentTileId, nextTileId, direction)) {
                System.out.println("WARNING: Invalid connection from " + current + " (tile " + currentTileId +
                        ") to " + next + " (tile " + nextTileId + ") in direction " + direction);
                isValid = false;
            }
        }

        if (isValid) {
            System.out.println("Path connectivity validation passed!");
        } else {
            System.out.println("Path connectivity validation failed!");
        }
    }

    public void update(float speedMultiplier){
        // Create a copy of the enemies list to avoid concurrent modification
        ArrayList<Enemy> enemiesCopy = new ArrayList<>(enemies);

        // First pass: Update combat synergy
        updateCombatSynergy();

        for (Enemy enemy : enemiesCopy) {
            if (enemy.isAlive()) {
                // adjust animation speed when game speed changes
                enemy.adjustAnimationForGameSpeed(speedMultiplier);
                // call enemy's own update method to handle internal logic like timers
                enemy.update(speedMultiplier);
            }
        }

        if (!pathFound || pathPoints.isEmpty()) return;

        ArrayList<Enemy> enemiesToRemove = new ArrayList<>();

        for (Enemy e : enemiesCopy) {
            if (!e.isAlive()) {
                playing.getPlayerManager().addGold(e.getGoldReward());
                // Plunderer bonus: Eğer skill seçiliyse +1 altın ver
                if (SkillTree.getInstance().isSkillSelected(SkillType.PLUNDERER_BONUS)) {
                    playing.getPlayerManager().addGold(1);
                }
                playing.updateUIResources();
                System.out.println("Enemy " + e.getId() + " killed. + " + e.getGoldReward() + " gold!");
                // Chance to spawn a gold bag
                if (Math.random() * 100 < GOLD_BAG_SPAWN_CHANCE) {
                    float bagX = e.getSpriteCenterX();
                    float bagY = e.getSpriteCenterY();
                    playing.getGoldBagManager().spawnGoldBag(bagX, bagY, 2, 30);
                }
                enemiesToRemove.add(e);
                continue;
            }

            if (e.hasReachedEnd()) {
                playing.enemyReachedEnd(e);
                enemiesToRemove.add(e);
                continue;
            }

            moveEnemy(e, speedMultiplier);
        }

        // remove dead enemies
        enemies.removeAll(enemiesToRemove);
    }

    public void addEnemy(int enemyType){
        if (!pathFound || pathPoints.isEmpty()) {
            System.out.println("Cannot add enemy: Path not found or empty");
            return;
        }

        GridPoint spawnPoint = pathPoints.get(0);

        if (!isValidSpawnPoint(spawnPoint)) {
            for (int i = 0; i < pathPoints.size(); i++) {
                if (isValidSpawnPoint(pathPoints.get(i))) {
                    spawnPoint = pathPoints.get(i);
                    break;
                }
            }
        }


        int x = spawnPoint.getX() * tileSize + tileSize / 2;
        int y = spawnPoint.getY() * tileSize + tileSize / 2;

        System.out.println("Adding enemy at position: " + x + "," + y);

        Enemy enemy = null;

        switch(enemyType){
            case GOBLIN:
                System.out.println("Adding Goblin");
                enemy = new Goblin(x, y, nextEnemyID++);
                break;
            case KNIGHT:
                System.out.println("Adding Knight");
                enemy = new Knight(x, y, nextEnemyID++);
                break;
            case TNT:
                System.out.println("Adding TNT");
                enemy = new TNT(x, y, nextEnemyID++);
                break;
            case BARREL:
                System.out.println("Adding Barrel");
                enemy = new Barrel(x, y, nextEnemyID++);
                break;
            case TROLL:
                System.out.println("Adding Troll");
                enemy = new Troll(x, y, nextEnemyID++);
                break;
            default:
                System.out.println("Unknown enemy type: " + enemyType);
                return;
        }

        if (enemy != null) {
            applyOptionsToEnemy(enemy);

            // Initialize enemy direction based on the path
            initializeEnemyDirection(enemy);

            enemies.add(enemy);
            long spawnTime = System.currentTimeMillis();
            enemySpawnTimes.put(enemy, spawnTime);

        }
    }

    private boolean isValidSpawnPoint(GridPoint point) {
        int[][] overlay = playing.getOverlay();
        return point != null &&
                point != endPoint &&
                !isGateTile(point.getX(), point.getY(), overlay);
    }

    /**
     * Applies the current game options to an enemy
     * @param enemy The enemy to update with options
     */
    private void applyOptionsToEnemy(Enemy enemy) {
        try {
            if (enemy == null) {
                System.out.println("Warning: Cannot apply options to null enemy");
                return;
            }

            if (gameOptions == null) {
                System.out.println("Warning: GameOptions is null, using default stats for enemy ID " + enemy.getId());
                return;
            }

            enemy.updateStatsFromOptions(gameOptions);
        } catch (Exception e) {
            System.out.println("Error applying options to enemy: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Reloads options from the options file and applies them to all enemies
     */
    public void reloadFromOptions() {
        try {
            this.gameOptions = OptionsIO.load();

            // Apply new options to all existing enemies
            for (Enemy enemy : enemies) {
                if (enemy != null && enemy.isAlive()) {
                    applyOptionsToEnemy(enemy);
                }
            }
        } catch (Exception e) {
            System.out.println("Error reloading options: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void moveEnemy(Enemy e, float speedMultiplier) {
        int pathIndex = e.getCurrentPathIndex();

        // if enemy has reached the last path point, it has reached the end
        if (pathIndex >= pathPoints.size() - 1) {
            e.setReachedEnd(true);
            return;
        }

        // Decide whether to use curved or straight movement based on the current path segment
        if (shouldUseCurvedMovement(pathIndex)) {
            moveCurved(e, speedMultiplier);
        } else {
            moveStraight(e, speedMultiplier);
        }

    }

    /**
     * Determines if curved movement should be used for this path segment
     */
    private boolean shouldUseCurvedMovement(int pathIndex) {
        if (pathIndex >= pathPoints.size() - 1) return false;

        // Get the current tile and check if it's a curved road
        GridPoint currentPoint = pathPoints.get(pathIndex);
        int tileId = tileData[currentPoint.getY()][currentPoint.getX()];

        // Check if the tile is one of the curved road tiles
        // Corner curves: 0, 2, 8, 10
        // Flat curves: 1, 4, 6, 9
        return tileId == 0 || tileId == 1 || tileId == 2 || tileId == 4 ||
                tileId == 6 || tileId == 8 || tileId == 9 || tileId == 10;
    }

    /**
     * Moves enemy along a curved path using proper circular arc geometry
     */
    private void moveCurved(Enemy e, float speedMultiplier) {
        int pathIndex = e.getCurrentPathIndex();

        GridPoint currentPoint = pathPoints.get(pathIndex);
        GridPoint nextPoint = pathPoints.get(pathIndex + 1);

        // Calculate movement direction
        int deltaX = nextPoint.getX() - currentPoint.getX();
        int deltaY = nextPoint.getY() - currentPoint.getY();

        // Determine curve type based on direction change and tile type
        CurveParams curveParams = calculateCurveParameters(pathIndex, deltaX, deltaY);

        if (curveParams != null) {
            // Use circular arc movement
            moveAlongCircularArc(e, curveParams, speedMultiplier);
        } else {
            // Fallback to straight line movement
            System.out.println("Warning: shouldUseCurvedMovement was true, but no CurveParams found. Falling back to straight movement for enemy " + e.getId() + " at path index " + pathIndex);
            moveStraight(e, speedMultiplier);
        }
    }

    /**
     * Moves enemy along a circular arc using vector rotation
     * @param enemy The enemy to move
     * @param curveParams The parameters defining the circular arc
     * @param speedMultiplier The current game speed multiplier
     */
    private void moveAlongCircularArc(Enemy enemy, CurveParams curveParams, float speedMultiplier) {
        // Get current enemy position
        float currentX = enemy.getX();
        float currentY = enemy.getY();

        // Get target position (next path point)
        int currentPathIndex = enemy.getCurrentPathIndex();
        if (currentPathIndex >= pathPoints.size() - 1) {
            enemy.setReachedEnd(true);
            return;
        }

        GridPoint nextPoint = pathPoints.get(currentPathIndex + 1);
        float targetX = nextPoint.getX() * tileSize + tileSize / 2.0f;
        float targetY = nextPoint.getY() * tileSize + tileSize / 2.0f;

        // For curved tiles, adjust the target to be on the actual road path, not the tile center
        // Check if the NEXT tile (where we're going) is a curved tile
        int nextTileId = tileData[nextPoint.getY()][nextPoint.getX()];
        boolean isTargetCornerCurve = (nextTileId == 0 || nextTileId == 2 || nextTileId == 8 || nextTileId == 10);
        boolean isTargetFlatCurve = (nextTileId == 1 || nextTileId == 4 || nextTileId == 6 || nextTileId == 9);

        if (isTargetCornerCurve) {
            // Use specific target offsets for each corner curve type
            float centerTileX = nextPoint.getX() * tileSize + tileSize / 2.0f;
            float centerTileY = nextPoint.getY() * tileSize + tileSize / 2.0f;

            switch (nextTileId) {
                case 2: // CurvedRoadEastNorth
                    targetX = centerTileX - 19;
                    targetY = centerTileY + 19;
                    break;
                case 0: // CurvedRoadNorthWest
                    targetX = centerTileX + 19;
                    targetY = centerTileY + 19;
                    break;
                case 8: // CurvedRoadWestSouth
                    targetX = centerTileX + 19;
                    targetY = centerTileY - 19;
                    break;
                case 10: // CurvedRoadSouthEast
                    targetX = centerTileX - 19;
                    targetY = centerTileY - 19;
                    break;
            }
        } else if (isTargetFlatCurve) {
            // Use specific target offsets for each flat curve type
            float centerTileX = nextPoint.getX() * tileSize + tileSize / 2.0f;
            float centerTileY = nextPoint.getY() * tileSize + tileSize / 2.0f;

            switch (nextTileId) {
                case 1: // CurvedRoadNorth
                    targetX = centerTileX;
                    targetY = centerTileY - 10; // 10 pixels above
                    break;
                case 9: // CurvedRoadSouth
                    targetX = centerTileX;
                    targetY = centerTileY + 10; // 10 pixels below
                    break;
                case 4: // CurvedRoadWest
                    targetX = centerTileX - 10; // 10 pixels left
                    targetY = centerTileY;
                    break;
                case 6: // CurvedRoadEast
                    targetX = centerTileX + 10; // 10 pixels right
                    targetY = centerTileY;
                    break;
            }
        }


        // Calculate current vector from center to enemy position
        float vx = currentX - curveParams.centerX;
        float vy = currentY - curveParams.centerY;

        // Calculate target vector from center to target position
        float targetVx = targetX - curveParams.centerX;
        float targetVy = targetY - curveParams.centerY;

        // Calculate the enemy's effective speed with all modifiers
        float baseSpeed = enemy.getSpeed() * speedMultiplier;

        // Apply weather effects to enemy speed
        if (weatherManager != null && weatherManager.isSnowing()) {
            baseSpeed *= weatherManager.getEnemySpeedMultiplier();
        }

        // Apply effective speed modifiers (slow effects, etc.)
        float effectiveSpeed = baseSpeed * enemy.getEffectiveSpeed();

        // Calculate rotation angle based on speed and arc radius
        float rotationAngle = effectiveSpeed / curveParams.radius;

        // Determine the correct rotation direction by checking which direction gets us closer to target
        // Calculate current angle and target angle
        float currentAngle = (float) Math.atan2(vy, vx);
        float targetAngle = (float) Math.atan2(targetVy, targetVx);

        // Calculate the shortest angular distance to target
        float angleDiff = targetAngle - currentAngle;

        // Normalize angle difference to [-π, π] to find shortest path
        while (angleDiff > Math.PI) angleDiff -= 2 * Math.PI;
        while (angleDiff < -Math.PI) angleDiff += 2 * Math.PI;

        // Determine rotation direction based on shortest path to target
        if (angleDiff < 0) {
            rotationAngle = -rotationAngle; // Clockwise rotation
        }

        // Limit rotation angle to not overshoot the target
        if (Math.abs(rotationAngle) > Math.abs(angleDiff)) {
            rotationAngle = angleDiff; // Don't rotate past the target
        }

        // Apply vector rotation using the formula you provided
        float cos_theta = (float) Math.cos(rotationAngle);
        float sin_theta = (float) Math.sin(rotationAngle);

        float vx_prime = vx * cos_theta - vy * sin_theta;
        float vy_prime = vx * sin_theta + vy * cos_theta;

        // Calculate new position
        float newX = curveParams.centerX + vx_prime;
        float newY = curveParams.centerY + vy_prime;

        // Calculate movement delta for direction tracking
        float dx = newX - currentX;
        float dy = newY - currentY;

        // Update enemy position
        enemy.setX(newX);
        enemy.setY(newY);

        // Update direction based on movement (for sprite facing)
        if (dx != 0 || dy != 0) {
            float totalSpeed = (float) Math.sqrt(dx * dx + dy * dy);
            if (totalSpeed > 0) {
                // Store the direction the enemy is facing for sprite rendering
                float dirX = dx / totalSpeed;
                float dirY = dy / totalSpeed;
                enemy.setDirection(dirX, dirY);
            }
        }

        // Check if enemy has reached the target position (with some tolerance)
        float distanceToTarget = (float) Math.sqrt((newX - targetX) * (newX - targetX) +
                (newY - targetY) * (newY - targetY));

        // Different completion thresholds for curved vs straight paths
        float completionThreshold;
        if (isTargetCornerCurve || isTargetFlatCurve) {
            // For curved roads, use a smaller threshold since we're targeting specific road positions
            completionThreshold = effectiveSpeed * 2.0f;
        } else {
            // For straight paths, use the normal threshold
            completionThreshold = effectiveSpeed;
        }

        // If we're close enough to the target, move to next path point
        if (distanceToTarget < completionThreshold || Math.abs(angleDiff) < 0.05f) {
            // Let enemies find their own way to targets without teleporting/snapping
            // This prevents teleportation when transitioning between different road types
            enemy.setCurrentPathIndex(currentPathIndex + 1);
        }
    }

    /**
     * Parameters for circular arc movement
     */
    private static class CurveParams {
        float centerX, centerY;  // Arc center point
        float radius;            // Arc radius (71.5px based on your data)
        float startAngle;        // Starting angle in radians
        float endAngle;          // Ending angle in radians
        float arcLength;         // Total arc length for speed calculation

        CurveParams(float centerX, float centerY, float radius, float startAngle, float endAngle) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.radius = radius;
            this.startAngle = startAngle;
            this.endAngle = endAngle;
            this.arcLength = Math.abs(endAngle - startAngle) * radius;
        }
    }

    /**
     * Calculate the unified center point of the circular arc system
     * Based on the image, the center is at the center of the 3x3 grid area
     */
    private float[] calculateCurveCenter(float currentX, float currentY, int prevDirX, int prevDirY,
                                         int nextDirX, int nextDirY, int tileId, boolean isCornerCurve) {
        // Calculate shared center points so adjacent segments form continuous circles
        // We need to find the center that both adjacent segments can share

        float radius = (float) Math.sqrt(64 * 64 + 32 * 32); // ≈ 71.55px
        float centerX, centerY;

        if (isCornerCurve) {
            // Corner curves: determine which circle system this belongs to
            if (tileId == 0) {
                // CurvedRoadNorthWest: This is between North and West segments
                // Find the center that connects CurvedRoadNorth and CurvedRoadWest
                // The center should be at the intersection point of their circle centers
                centerX = currentX + tileSize; // Offset to match adjacent segments
                centerY = currentY + tileSize;
            } else if (tileId == 2) {
                // CurvedRoadEastNorth: Between East and North segments
                centerX = currentX - tileSize;
                centerY = currentY + tileSize;
            } else if (tileId == 10) {
                // CurvedRoadSouthEast: Between South and East segments
                centerX = currentX - tileSize;
                centerY = currentY - tileSize;
            } else if (tileId == 8) {
                // CurvedRoadWestSouth: Between West and South segments
                centerX = currentX + tileSize;
                centerY = currentY - tileSize;
            } else {
                // Default corner case
                centerX = currentX;
                centerY = currentY;
            }
        } else {
            // Flat curves: these need to match the center used by their adjacent corner curves
            if (tileId == 6) { // CurvedRoadEast
                // This should match CurvedRoadEastNorth and other East-related corners
                centerX = currentX - tileSize;
                centerY = currentY;
            } else if (tileId == 1) { // CurvedRoadNorth
                // This should match CurvedRoadNorthWest and other North-related corners
                centerX = currentX;
                centerY = currentY + tileSize;
            } else if (tileId == 4) { // CurvedRoadWest
                // This should match CurvedRoadWestSouth and other West-related corners
                centerX = currentX + tileSize;
                centerY = currentY;
            } else if (tileId == 9) { // CurvedRoadSouth
                // This should match CurvedRoadSouthEast and other South-related corners
                centerX = currentX;
                centerY = currentY - tileSize;
            } else {
                // Default flat case
                centerX = currentX;
                centerY = currentY;
            }
        }

        return new float[]{centerX, centerY};
    }

    /**
     * Calculate circular arc parameters based on path direction change and tile type
     */
    private CurveParams calculateCurveParameters(int pathIndex, int deltaX, int deltaY) {
        if (pathIndex == 0) return null;

        GridPoint prevPoint = pathPoints.get(pathIndex - 1);
        GridPoint currentPoint = pathPoints.get(pathIndex);
        GridPoint nextPoint = pathPoints.get(pathIndex + 1);

        // Calculate direction vectors
        int prevDirX = currentPoint.getX() - prevPoint.getX();
        int prevDirY = currentPoint.getY() - prevPoint.getY();
        int nextDirX = nextPoint.getX() - currentPoint.getX();
        int nextDirY = nextPoint.getY() - currentPoint.getY();

        // Skip if no movement vectors are valid
        if (Math.abs(prevDirX) + Math.abs(prevDirY) == 0 || Math.abs(nextDirX) + Math.abs(nextDirY) == 0) return null;

        // Unified radius for all curved paths: sqrt(64² + 32²) pixels
        final float UNIFIED_RADIUS = (float) Math.sqrt(64 * 64 + 32 * 32); // ≈ 71.55px
        final float QUARTER_CIRCLE_ARC = (float) Math.toRadians(36.8998); // ~37°

        float currentX = currentPoint.getX() * tileSize + tileSize / 2.0f;
        float currentY = currentPoint.getY() * tileSize + tileSize / 2.0f;

        // Determine curve type and calculate arc parameters
        if (isCornerCurve(currentPoint, prevDirX, prevDirY, nextDirX, nextDirY)) {
            int tileId = tileData[currentPoint.getY()][currentPoint.getX()];
            return calculateQuarterCircleArc(currentX, currentY, prevDirX, prevDirY, nextDirX, nextDirY, UNIFIED_RADIUS, tileId);
        } else if (isFlatCurve(currentPoint, prevDirX, prevDirY, nextDirX, nextDirY)) {
            int tileId = tileData[currentPoint.getY()][currentPoint.getX()];
            return calculateFlatCurveArc(currentX, currentY, prevDirX, prevDirY, nextDirX, nextDirY, tileId);
        }

        return null;
    }

    /**
     * Check if this is a corner curve (90-degree turn)
     * These are the quarter-circle curved roads at corners
     */
    private boolean isCornerCurve(GridPoint currentPoint, int prevDirX, int prevDirY, int nextDirX, int nextDirY) {
        // Check for 90-degree direction changes first
        boolean hasDirectionChange = (Math.abs(prevDirX) != Math.abs(nextDirX)) && (Math.abs(prevDirY) != Math.abs(nextDirY));

        if (hasDirectionChange) {
            // Get the tile ID at the current position
            int tileId = tileData[currentPoint.getY()][currentPoint.getX()];

            // Check for corner curved road tile IDs from TileManager
            boolean isCurvedRoadNorthWest = (tileId == 0);   // CurvedRoadNorthWest
            boolean isCurvedRoadEastNorth = (tileId == 2);   // CurvedRoadEastNorth
            boolean isCurvedRoadWestSouth = (tileId == 8);   // CurvedRoadWestSouth
            boolean isCurvedRoadSouthEast = (tileId == 10);  // CurvedRoadSouthEast

            return isCurvedRoadNorthWest || isCurvedRoadEastNorth || isCurvedRoadWestSouth || isCurvedRoadSouthEast;
        }

        return false;
    }

    /**
     * Check if this is a flat curve (same general direction but with curvature)
     * This should be based on the actual tile type (CurvedRoadNorth, East, West, South)
     */
    private boolean isFlatCurve(GridPoint currentPoint, int prevDirX, int prevDirY, int nextDirX, int nextDirY) {
        // Check for same general direction (no 90-degree turn) but still a curve needed
        // This happens when the current tile is a curved road tile (N, E, W, S types)
        boolean sameDirection = (prevDirX == nextDirX && prevDirY == nextDirY);
        boolean validDirections = (Math.abs(prevDirX) + Math.abs(prevDirY) == 1);

        // Check if this is actually a curved road tile by checking tile ID
        if (sameDirection && validDirections) {
            // Get the tile ID at the current position
            int tileId = tileData[currentPoint.getY()][currentPoint.getX()];

            // Check for flat curved road tile IDs from TileManager
            boolean isCurvedRoadNorth = (tileId == 1);  // CurvedRoadNorth
            boolean isCurvedRoadWest = (tileId == 4);   // CurvedRoadWest  
            boolean isCurvedRoadEast = (tileId == 6);   // CurvedRoadEast
            boolean isCurvedRoadSouth = (tileId == 9);  // CurvedRoadSouth

            return isCurvedRoadNorth || isCurvedRoadWest || isCurvedRoadEast || isCurvedRoadSouth;
        }

        return false;
    }

    /**
     * Calculate parameters for corner curve arcs (segments of the unified circle)
     * All curves are segments of the same large circle with a common center point
     * Now uses actual tile IDs to determine the correct arc parameters
     */
    private CurveParams calculateQuarterCircleArc(float currentX, float currentY, int prevDirX, int prevDirY,
                                                  int nextDirX, int nextDirY, float radius, int tileId) {
        // Calculate the proper center for this specific corner curve segment
        float unifiedRadius = radius;

        float[] center = calculateCurveCenter(currentX, currentY, prevDirX, prevDirY, nextDirX, nextDirY, tileId, true);
        float centerX = center[0];
        float centerY = center[1];

        float startAngle, endAngle;

        // Each corner curve is a segment of the unified circle with EXACT angle ranges
        // Using the actual tile IDs to determine the correct angles

        switch (tileId) {
            case 0: // CurvedRoadNorthWest: 116.57° to 153.43°
                if (prevDirX == 0 && prevDirY == 1 && nextDirX == 1 && nextDirY == 0) {
                    // Moving from South to East
                    startAngle = (float) Math.toRadians(153.43);
                    endAngle = (float) Math.toRadians(116.57);
                } else {
                    // Reverse direction: East to South
                    startAngle = (float) Math.toRadians(116.57);
                    endAngle = (float) Math.toRadians(153.43);

                }
                break;

            case 2: // CurvedRoadEastNorth: 26.57° to 63.43°
                if (prevDirX == 0 && prevDirY == 1 && nextDirX == -1 && nextDirY == 0) {
                    // Moving from South to West
                    startAngle = (float) Math.toRadians(26.57);
                    endAngle = (float) Math.toRadians(63.43);
                } else {
                    // Reverse direction: West to South
                    startAngle = (float) Math.toRadians(63.43);
                    endAngle = (float) Math.toRadians(26.57);
                }
                break;

            case 8: // CurvedRoadWestSouth: 206.57° to 243.43°
                if (prevDirX == -1 && prevDirY == 0 && nextDirX == 0 && nextDirY == -1) {
                    // Moving from East to North
                    startAngle = (float) Math.toRadians(243.43);
                    endAngle = (float) Math.toRadians(206.57);

                } else {
                    // Reverse direction: North to East
                    startAngle = (float) Math.toRadians(206.57);
                    endAngle = (float) Math.toRadians(243.43);
                }
                break;

            case 10: // CurvedRoadSouthEast: 296.57° to 333.43°
                if (prevDirX == 1 && prevDirY == 0 && nextDirX == 0 && nextDirY == -1) {
                    // Moving from West to North
                    startAngle = (float) Math.toRadians(296.57);
                    endAngle = (float) Math.toRadians(333.43);
                } else {
                    // Reverse direction: North to West
                    startAngle = (float) Math.toRadians(333.43);
                    endAngle = (float) Math.toRadians(296.57);
                }
                break;

            default:
                // Fallback case for unknown corner curve types
                System.out.println("Warning: Unknown corner curve tile ID: " + tileId);
                centerX = currentX;
                centerY = currentY;
                startAngle = 0;
                endAngle = (float) Math.PI / 4;
                unifiedRadius = tileSize / 4.0f;
                break;
        }

        return new CurveParams(centerX, centerY, unifiedRadius, startAngle, endAngle);
    }

    /**
     * Calculate parameters for flat curve arcs (segments of the unified circle)
     * All flat curves are also segments of the same large circle with the same center and radius
     * CurvedRoadNorth: horizontal movement (west↔east) curved northward
     * CurvedRoadSouth: horizontal movement (west↔east) curved southward  
     * CurvedRoadWest: vertical movement (north↔south) curved westward
     * CurvedRoadEast: vertical movement (north↔south) curved eastward
     */
    private CurveParams calculateFlatCurveArc(float currentX, float currentY, int prevDirX, int prevDirY,
                                              int nextDirX, int nextDirY, int tileId) {
        // Use unified radius - same as corner curves: sqrt(64² + 32²) pixels
        float curveRadius = (float) Math.sqrt(64 * 64 + 32 * 32); // ≈ 71.55px

        // Calculate the proper center for this specific flat curve segment  
        float[] center = calculateCurveCenter(currentX, currentY, prevDirX, prevDirY, nextDirX, nextDirY, tileId, false);
        float centerX = center[0];
        float centerY = center[1];

        float startAngle, endAngle;



        // Each flat curve is a segment of the unified circle with EXACT angle ranges
        // Using the strict angle definitions provided

        if (tileId == 6) { // CurvedRoadEast: -26.57° to 26.57°
            if (prevDirY == 1 && nextDirY == 1) { // Moving north to south  
                startAngle = (float) Math.toRadians(26.57);
                endAngle = (float) Math.toRadians(-26.57);
            } else if (prevDirY == -1 && nextDirY == -1) { // Moving south to north
                startAngle = (float) Math.toRadians(-26.57);
                endAngle = (float) Math.toRadians(26.57);
            } else {
                // Default - respecting the strict angle range
                startAngle = (float) Math.toRadians(26.57);
                endAngle = (float) Math.toRadians(-26.57);
            }

        } else if (tileId == 1) { // CurvedRoadNorth: 63.43° to 116.57°
            if (prevDirX == 1 && nextDirX == 1) { // Moving west to east
                startAngle = (float) Math.toRadians(116.57);
                endAngle = (float) Math.toRadians(63.43);
            } else if (prevDirX == -1 && nextDirX == -1) { // Moving east to west
                startAngle = (float) Math.toRadians(63.43);
                endAngle = (float) Math.toRadians(116.57);
            } else {
                // Default - respecting the strict angle range
                startAngle = (float) Math.toRadians(63.43);
                endAngle = (float) Math.toRadians(116.57);
            }

        } else if (tileId == 4) { // CurvedRoadWest: 153.43° to 206.57°
            if (prevDirY == 1 && nextDirY == 1) { // Moving north to south
                startAngle = (float) Math.toRadians(153.43);
                endAngle = (float) Math.toRadians(206.57);
            } else if (prevDirY == -1 && nextDirY == -1) { // Moving south to north
                startAngle = (float) Math.toRadians(206.57);
                endAngle = (float) Math.toRadians(153.43);
            } else {
                // Default - respecting the strict angle range
                startAngle = (float) Math.toRadians(153.43);
                endAngle = (float) Math.toRadians(206.57);
            }

        } else if (tileId == 9) { // CurvedRoadSouth: 243.43° to 296.57°
            if (prevDirX == 1 && nextDirX == 1) { // Moving west to east
                startAngle = (float) Math.toRadians(243.43);
                endAngle = (float) Math.toRadians(296.57);
            } else if (prevDirX == -1 && nextDirX == -1) { // Moving east to west
                startAngle = (float) Math.toRadians(296.57);
                endAngle = (float) Math.toRadians(243.43);
            } else {
                // Default - respecting the strict angle range
                startAngle = (float) Math.toRadians(243.43);
                endAngle = (float) Math.toRadians(296.57);
            }

        } else {
            // Default case - minimal curve
            startAngle = 0;
            endAngle = (float) Math.PI / 8;
            curveRadius = tileSize / 2.0f;
        }

        return new CurveParams(centerX, centerY, curveRadius, startAngle, endAngle);
    }


    /**
     * Moves enemy in straight line (fallback for segments without enough control points)
     */
    private void moveStraight(Enemy e, float speedMultiplier) {
        int pathIndex = e.getCurrentPathIndex();
        GridPoint currentPoint = pathPoints.get(pathIndex);
        GridPoint nextPoint = pathPoints.get(pathIndex + 1);

        // calculate target position
        int targetX = nextPoint.getX() * tileSize + tileSize / 2;
        int targetY = nextPoint.getY() * tileSize + tileSize / 2;

        // Calculate direction to move - optimized distance calculation
        float xDiff = targetX - e.getX();
        float yDiff = targetY - e.getY();
        float distanceSquared = xDiff * xDiff + yDiff * yDiff;
        float distance = (float) Math.sqrt(distanceSquared);

        // Check if we should start a curve early to avoid teleport effect
        float thresholdDistance = tileSize * CURVE_THRESHOLD_DISTANCE;

        // Check if next segment is curved and we're close enough to start the curve
        if (distance < thresholdDistance && pathIndex + 1 < pathPoints.size() - 1) {
            if (shouldUseCurvedMovement(pathIndex + 1)) {
                // Don't snap to target position - let the curve logic handle smooth transition
                e.setCurrentPathIndex(pathIndex + 1);
                return;
            }
        }

        // if enemy is very close to the path point, move to next path point
        // Use squared distance comparison to avoid unnecessary sqrt calculation
        float speedThreshold = e.getSpeed() * speedMultiplier;
        if (distanceSquared < speedThreshold * speedThreshold) {
            e.setCurrentPathIndex(pathIndex + 1);
            return;
        }

        // calculate movement speed components
        float baseSpeed = e.getSpeed() * speedMultiplier;

        // Apply weather effects to enemy speed
        if (weatherManager != null && weatherManager.isSnowing()) {
            baseSpeed *= weatherManager.getEnemySpeedMultiplier();
        }

        // Apply effective speed modifiers (slow effects, etc.)
        float effectiveSpeed = baseSpeed * e.getEffectiveSpeed();

        // Calculate normalized direction vector - optimized division
        float invDistance = 1.0f / distance;
        float dirX = xDiff * invDistance;
        float dirY = yDiff * invDistance;

        // Calculate movement distances
        float xSpeed = dirX * effectiveSpeed;
        float ySpeed = dirY * effectiveSpeed;

        // Move enemy by updating position directly
        e.setX(e.getX() + xSpeed);
        e.setY(e.getY() + ySpeed);

        // Set direction directly for consistent sprite facing
        e.setDirection(dirX, dirY);


    }

    /**
     * Initializes enemy direction when they first spawn based on the initial path direction
     */
    private void initializeEnemyDirection(Enemy enemy) {
        if (pathPoints.size() < 2) {
            // Default direction if path is too short
            enemy.setDirection(1.0f, 0.0f); // Face right
            return;
        }

        // Get the direction from spawn point to first path point
        GridPoint firstPoint = pathPoints.get(0);
        GridPoint secondPoint = pathPoints.get(1);

        float dirX = secondPoint.getX() - firstPoint.getX();
        float dirY = secondPoint.getY() - firstPoint.getY();

        float magnitude = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (magnitude > 0.001f) {
            dirX /= magnitude;
            dirY /= magnitude;
            enemy.setDirection(dirX, dirY);
        } else {
            enemy.setDirection(1.0f, 0.0f); // Default to facing right
        }
    }

    public void draw(Graphics g, boolean gamePaused) {

        List<Enemy> enemiesCopy = new ArrayList<>(enemies);


        for (Enemy enemy : enemiesCopy) {
            if (enemy.isAlive() && !gamePaused) {
                enemy.updateAnimationTick();
            }
        }


        for (Enemy enemy : enemiesCopy) {
            if (enemy.isAlive()) {
                if (isGoblinInvisible(enemy)) {
                    drawEnemySilhouette(enemy, g);
                } else {
                    drawEnemy(enemy, g);
                }
            }
        }

        // Optional: Draw curved path visualization (uncomment for debugging)
        // drawCurvedPaths(g); // COMMENTED OUT FOR PERFORMANCE

    }

    /**
     * Debug method to visualize curved paths using circular arcs
     * COMMENTED OUT FOR PERFORMANCE - Uncomment when debugging is needed
     */
    /*
    private void drawCurvedPaths(Graphics g) {
        if (pathPoints.size() < 3) return;
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw path points as small circles
        g2d.setColor(new Color(0, 255, 0, 150)); // Semi-transparent green
        for (GridPoint point : pathPoints) {
            int x = point.getX() * tileSize + tileSize / 2 - 3;
            int y = point.getY() * tileSize + tileSize / 2 - 3;
            g2d.fillOval(x, y, 6, 6);
        }
        
        // Draw circular arcs for curved segments
        g2d.setStroke(new BasicStroke(3.0f));
        g2d.setColor(new Color(255, 255, 0, 180)); // Semi-transparent yellow
        
        for (int i = 1; i < pathPoints.size() - 1; i++) {
            if (shouldUseCurvedMovement(i)) {
                // Calculate curve parameters for this segment
                GridPoint currentPoint = pathPoints.get(i);
                GridPoint nextPoint = pathPoints.get(i + 1);
                int deltaX = nextPoint.getX() - currentPoint.getX();
                int deltaY = nextPoint.getY() - currentPoint.getY();
                
                CurveParams curveParams = calculateCurveParameters(i, deltaX, deltaY);
                
                if (curveParams != null) {
                    // Check curve type by checking tile ID
                    int tileId = tileData[currentPoint.getY()][currentPoint.getX()];
                    boolean isFlatCurve = (tileId == 1 || tileId == 4 || tileId == 6 || tileId == 9);
                    boolean isCornerCurve = (tileId == 0 || tileId == 2 || tileId == 8 || tileId == 10);
                    
                    if (isFlatCurve) {
                        // Draw flat curves in different colors
                        g2d.setStroke(new BasicStroke(4.0f));
                        
                        // Color code by curve type
                        if (tileId == 1) { // CurvedRoadNorth
                            g2d.setColor(new Color(0, 255, 0, 200)); // Green for North
                        } else if (tileId == 9) { // CurvedRoadSouth  
                            g2d.setColor(new Color(255, 0, 255, 200)); // Magenta for South
                        } else if (tileId == 4) { // CurvedRoadWest
                            g2d.setColor(new Color(0, 0, 255, 200)); // Blue for West
                        } else if (tileId == 6) { // CurvedRoadEast
                            g2d.setColor(new Color(255, 165, 0, 200)); // Orange for East
                        }
                        
                        // Draw the flat curve arc
                        drawCircularArc(g2d, curveParams);
                        
                        // Draw arc center point with matching color
                        g2d.fillOval((int)(curveParams.centerX - 6), (int)(curveParams.centerY - 6), 12, 12);
                        

                      
                                     
                    } else if (isCornerCurve) {
                        // Draw corner curves in different colors
                        g2d.setStroke(new BasicStroke(5.0f));
                        
                        // Color code by corner curve type
                        if (tileId == 0) { // CurvedRoadNorthWest
                            g2d.setColor(new Color(255, 0, 0, 200)); // Red for NorthWest
                        } else if (tileId == 2) { // CurvedRoadEastNorth
                            g2d.setColor(new Color(0, 255, 255, 200)); // Cyan for EastNorth
                        } else if (tileId == 8) { // CurvedRoadWestSouth
                            g2d.setColor(new Color(255, 255, 0, 200)); // Yellow for WestSouth
                        } else if (tileId == 10) { // CurvedRoadSouthEast
                            g2d.setColor(new Color(128, 0, 128, 200)); // Purple for SouthEast
                        }
                        
                        // Draw the corner curve arc
                        drawCircularArc(g2d, curveParams);
                        
                        // Draw arc center point with matching color
                        g2d.fillOval((int)(curveParams.centerX - 8), (int)(curveParams.centerY - 8), 16, 16);
                        

                        
                    } else {
                        // Draw other quarter-circle curves in yellow (fallback)
                        g2d.setStroke(new BasicStroke(3.0f));
                        g2d.setColor(new Color(255, 255, 0, 180)); // Yellow for other quarter-circles
                        
                        drawCircularArc(g2d, curveParams);
                        
                        // Draw arc center point
                        g2d.setColor(new Color(255, 0, 0, 200)); // Red center point
                        g2d.fillOval((int)(curveParams.centerX - 4), (int)(curveParams.centerY - 4), 8, 8);
                        

                    }
                    
                    // Reset for next iteration
                    g2d.setStroke(new BasicStroke(3.0f));
                    g2d.setColor(new Color(255, 255, 0, 180));
                }
            }
        }
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }
    */
    

    /*
    // DEBUG METHODS COMMENTED OUT FOR PERFORMANCE
    private String getTileTypeName(int tileId) {
        switch(tileId) {
            case 1: return "North";
            case 4: return "West"; 
            case 6: return "East";
            case 9: return "South";
            default: return "Unknown";
        }
    }

    private String getCornerTypeName(int tileId) {
        switch(tileId) {
            case 0: return "NorthWest";
            case 2: return "EastNorth";
            case 8: return "WestSouth";
            case 10: return "SouthEast";
            default: return "Unknown";
        }
    }
    */

    /**
     * Draw a circular arc based on curve parameters - updated to match movement logic
     */
    private void drawCircularArc(Graphics2D g2d, CurveParams curve) {
        // Store original settings
        Color originalColor = g2d.getColor();
        BasicStroke originalStroke = (BasicStroke) g2d.getStroke();

        // Draw the main arc path
        float arcCenterX = curve.centerX - curve.radius;
        float arcCenterY = curve.centerY - curve.radius;
        float arcDiameter = curve.radius * 2;

        double startAngleDegrees = Math.toDegrees(curve.startAngle);
        double endAngleDegrees = Math.toDegrees(curve.endAngle);
        double arcExtent = endAngleDegrees - startAngleDegrees;

        // Normalize arc extent
        while (arcExtent > 360) arcExtent -= 360;
        while (arcExtent < -360) arcExtent += 360;

        // Draw the main arc path
        g2d.setStroke(new BasicStroke(3.0f));
        g2d.drawArc((int)arcCenterX, (int)arcCenterY, (int)arcDiameter, (int)arcDiameter,
                (int)startAngleDegrees, (int)arcExtent);

        // Restore original settings
        g2d.setColor(originalColor);
        g2d.setStroke(originalStroke);
    }

    // updated method to extract all enemy animation frames
    public static BufferedImage[] extractEnemyFrames() {
        // need space for: 6 goblin + 6 knight + 6 tnt + 3 barrel + 10 troll = 31 frames
        BufferedImage[] enemyFrames = new BufferedImage[31];

        BufferedImage goblinSheet = LoadSave.getEnemyAtlas("goblin");
        BufferedImage knightSheet = LoadSave.getEnemyAtlas("knight");
        BufferedImage barrelSheet = LoadSave.getEnemyAtlas("barrel");
        BufferedImage tntSheet = LoadSave.getEnemyAtlas("tnt");
        BufferedImage trollSheet = LoadSave.getEnemyAtlas("troll");

        // Extract goblin frames (6 frames) - keeping current extraction for consistency
        for (int i = 0; i < 6; i++) {
            BufferedImage goblinFrame = goblinSheet.getSubimage(i * 192, 0, 192, 192);
            enemyFrames[i] = goblinFrame.getSubimage(20, 57, 115, 75);
        }

        // Extract knight frames (6 frames)
        for (int i = 0; i < 6; i++) {
            BufferedImage knightFrame = knightSheet.getSubimage(i * 192, 0, 192, 192);
            enemyFrames[6 + i] = knightFrame.getSubimage(50, 40, 120, 100);
        }

        // Extract TNT frames (6 frames)
        for (int i = 0; i < 6; i++) {
            // TNT: 1344x192 sized walk animation asset with 6 respective sprites
            BufferedImage tntFrame = tntSheet.getSubimage(i * 192, 0, 192, 192);
            enemyFrames[12 + i] = tntFrame.getSubimage(60, 60, 100, 72);
        }

        // Extract barrel frames (3 frames)
        for (int i = 0; i < 3; i++) {
            BufferedImage barrelFrame = barrelSheet.getSubimage(i * 128, 0, 128, 128);
            enemyFrames[18 + i] = barrelFrame.getSubimage(24, 16,80, 80);
        }

        // Extract troll frames (10 frames)
        for (int i = 0; i < 10; i++) {
            BufferedImage trollFrame = trollSheet.getSubimage(i * 401, 0, 401, 268);
            enemyFrames[21 + i] = trollFrame.getSubimage(0, 0, 401, 268);
        }

        return enemyFrames;
    }

    // Cache for anchor offsets to avoid repeated calculations
    private static final Map<String, int[]> anchorOffsetCache = new HashMap<>();

    /**
     * Calculates the anchor point offset for each enemy type based on their sprite sheet anchor points
     * and current sub-image extraction parameters. Results are cached for performance.
     *
     * @param enemyType The type of enemy
     * @param scale The scale factor applied to the sprite
     * @return An array containing [offsetX, offsetY] to align the anchor point with enemy center
     */
    private int[] calculateAnchorOffset(int enemyType, float scale) {
        // Create cache key
        String cacheKey = enemyType + "_" + scale;
        int[] cached = anchorOffsetCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        int anchorX, anchorY;
        int extractStartX, extractStartY;

        switch (enemyType) {
            case GOBLIN:
                anchorX = Enemy.GOBLIN_ANCHOR_X;
                anchorY = Enemy.GOBLIN_ANCHOR_Y;
                extractStartX = 20;
                extractStartY = 57;
                break;
            case KNIGHT:
                anchorX = Enemy.KNIGHT_ANCHOR_X;
                anchorY = Enemy.KNIGHT_ANCHOR_Y;
                extractStartX = 50;
                extractStartY = 40;
                break;
            case TNT:
                anchorX = Enemy.TNT_ANCHOR_X;
                anchorY = Enemy.TNT_ANCHOR_Y;
                extractStartX = 60;
                extractStartY = 60;
                break;
            case BARREL:
                anchorX = Enemy.BARREL_ANCHOR_X;
                anchorY = Enemy.BARREL_ANCHOR_Y;
                extractStartX = 24;
                extractStartY = 24;
                break;
            case TROLL:
                anchorX = Enemy.TROLL_ANCHOR_X;
                anchorY = Enemy.TROLL_ANCHOR_Y;
                extractStartX = 0;
                extractStartY = 0;
                break;
            default:
                return new int[]{0, 0};
        }

        // Calculate where the anchor point is within the extracted sub-image
        int anchorInSubImageX = anchorX - extractStartX;
        int anchorInSubImageY = anchorY - extractStartY;

        // Apply scale and calculate offset to center the anchor point
        int scaledAnchorX = (int)(anchorInSubImageX * scale);
        int scaledAnchorY = (int)(anchorInSubImageY * scale);

        int[] result = new int[]{scaledAnchorX, scaledAnchorY};
        anchorOffsetCache.put(cacheKey, result);
        return result;
    }

    private void drawEnemy(Enemy enemy, Graphics g) {
        //System.out.println("Drawing enemy ID: " + enemy.getId());
        // Calculate base index based on enemy type and get animation frame
        int baseIndex;

        // Map enemy types to their frame positions in the array
        switch (enemy.getEnemyType()) {
            case GOBLIN:
                baseIndex = 0;
                break;
            case KNIGHT:
                baseIndex = 6;
                break;
            case TNT:
                baseIndex = 12;
                break;
            case BARREL:
                baseIndex = 18;
                break;
            case TROLL:
                baseIndex = 21;
                break;
            default:
                baseIndex = 0;
                break;
        }

        int frame = baseIndex + enemy.getAnimationIndex();

        // ensure frame is within bounds and handle special case for barrel (only 3 frames)
        if (enemy.getEnemyType() == BARREL && enemy.getAnimationIndex() >= 3) {
            frame = baseIndex + (enemy.getAnimationIndex() % 3);
        } else if (frame >= enemyImages.length) {
            frame = baseIndex;
        }

        BufferedImage sprite = enemyImages[frame];

        // calculate scale and dimensions based on enemy size
        float scale;
        Enemy.Size size = enemy.getSize();

        switch (size) {
            case SMALL:
                scale = 0.6f; // 60% of original size
                break;
            case MEDIUM:
                scale = 0.8f; // 80% of original size
                break;
            case LARGE:
                if (enemy.getEnemyType() == TROLL) {
                    scale = 0.33f; // 35% for troll (75% of previous 40%)
                } else {
                    scale = 0.5f; // 50% for other large enemies
                }
                break;
            default:
                scale = 1.0f; // 100% size
                break;
        }

        int drawWidth = (int)(sprite.getWidth() * scale);
        int drawHeight = (int)(sprite.getHeight() * scale);

        // Calculate anchor point offset for this enemy type and scale
        int[] anchorOffset = calculateAnchorOffset(enemy.getEnemyType(), scale);

        // Position the sprite so that the anchor point aligns with the enemy's center position
        int drawX = (int)(enemy.getX() - anchorOffset[0]);
        int drawY = (int)(enemy.getY() - anchorOffset[1]);

        // Determine sprite facing based on movement direction
        float dirX = enemy.getDirX();
        float dirY = enemy.getDirY();

        // Calculate the angle of movement direction
        double movementAngle = Math.atan2(dirY, dirX);

        // Convert to degrees for easier understanding
        double angleDegrees = Math.toDegrees(movementAngle);

        // Normalize angle to 0-360 range
        if (angleDegrees < 0) angleDegrees += 360;

        // Face left if moving in the left half of the circle (90° to 270°)
        // This handles all directions: up-left, left, down-left
        boolean facingLeft = angleDegrees > 90 && angleDegrees < 270;

        if (enemy.isTeleporting()) {
            Graphics2D g2d_teleport = (Graphics2D) g.create(); // Create a copy for teleport effect if needed
            // Set a blue/cyan glow with pulse effect based on time
            long currentTime = System.nanoTime();
            float progress = 1.0f - ((float)(currentTime - enemy.getTeleportEffectTimer()) / enemy.TELEPORT_EFFECT_DURATION);
            float alpha = Math.max(0.1f, progress); // Fade out over time

            // Draw a pulsing blue glow
            g2d_teleport.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.7f));
            g2d_teleport.setColor(new Color(0, 200, 255)); // Bright blue
            g2d_teleport.fillOval(
                    drawX - 10,
                    drawY - 10,
                    drawWidth + 20,
                    drawHeight + 20
            );

            // Draw some "sparkle" effects
            g2d_teleport.setColor(new Color(255, 255, 255, (int)(255 * alpha)));
            float pulseSize = 5.0f + (float)(Math.sin(currentTime * 0.00000002) * 3.0);
            int sparkleSize = (int)pulseSize;

            // Draw 5 random sparkles
            for (int i = 0; i < 5; i++) {
                double angle = Math.random() * Math.PI * 2;
                int offsetX = (int)(Math.cos(angle) * drawWidth/2);
                int offsetY = (int)(Math.sin(angle) * drawHeight/2);
                g2d_teleport.fillRect(
                        drawX + drawWidth/2 + offsetX - sparkleSize/2,
                        drawY + drawHeight/2 + offsetY - sparkleSize/2,
                        sparkleSize, sparkleSize
                );
            }
            g2d_teleport.dispose(); // Dispose of the copy
        }

        if (facingLeft) {
            // Mirror the sprite around the enemy's logical center to keep its position stable.
            Graphics2D g2d = (Graphics2D) g.create();

            // Use the enemy's logical coordinates as the center for the flip transformation.
            float centerX = enemy.getX();
            float centerY = enemy.getY();

            // Apply horizontal flip transformation around the enemy's logical center.
            g2d.translate(centerX, centerY);
            g2d.scale(-1, 1);
            g2d.translate(-centerX, -centerY);

            // Draw the sprite. The transformation ensures the sprite is mirrored
            // while its anchor point remains fixed at (enemy.getX(), enemy.getY()).
            g2d.drawImage(sprite, drawX, drawY, drawWidth, drawHeight, null);
            g2d.dispose();
        } else {
            // Normal drawing for facing right
            g.drawImage(sprite, drawX, drawY, drawWidth, drawHeight, null);
        }

        // Health bar and effects should be drawn with the original transform (which we didn't change)
        drawHealthBar(g, enemy, drawX, drawY, drawWidth, drawHeight);

        // If frozen, draw a semi-transparent ice-blue overlay
        if (enemy.isFrozen()) {
            Graphics2D g2d = (Graphics2D) g;
            Composite oldComposite = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            g2d.setColor(new Color(100, 200, 255)); // ice-blue
            g2d.fillOval(drawX, drawY, drawWidth, drawHeight);
            g2d.setComposite(oldComposite);
        }
    }

    private void drawHealthBar(Graphics g, Enemy enemy, int x, int y, int width, int height) {
        int healthBarWidth = 40;
        int healthBarHeight = 4;
        int healthBarX = x + (width - healthBarWidth) / 2;

        // different enemy types get bars in different positions
        boolean drawAbove = enemy.getEnemyType() % 2 == 1; // even types below, odd types above

        int healthBarY;
        if (drawAbove) {
            healthBarY = y - 8;
        } else {
            healthBarY = y + height + 2;
        }

        // draw white rounded contour frame
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1.5f));
        RoundRectangle2D roundedRect = new RoundRectangle2D.Float(
                healthBarX - 1, healthBarY - 1,
                healthBarWidth + 2, healthBarHeight + 2,
                4, 4);
        g2d.draw(roundedRect);

        // draw black background of health bar
        g.setColor(Color.BLACK);
        g.fillRect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);

        // draw red health indicator
        g.setColor(new Color(255, 0, 0));
        int currentHealthWidth = (int) (healthBarWidth * enemy.getHealthBarPercentage());
        g.fillRect(healthBarX, healthBarY, currentHealthWidth, healthBarHeight);

        // Draw status effect icons
        int iconX = healthBarX - 14;
        int iconY = healthBarY - 4;

        // Draw slow effect icon if active
        if (enemy.isSlowed()) {
            if (Enemy.snowflakeIcon == null) {
                Enemy.snowflakeIcon = LoadSave.getImageFromPath("/TowerAssets/snow flake icon.png");
            }
            if (Enemy.snowflakeIcon != null) {
                g.drawImage(Enemy.snowflakeIcon, iconX, iconY, 12, 12, null);
                iconX -= 14; // Move left for next icon
            }
        }

        // Draw combat synergy icon if active
        if (enemy.hasCombatSynergy()) {
            if (Enemy.thunderIcon == null) {
                Enemy.thunderIcon = LoadSave.getImageFromPath("/TowerAssets/thunder_icon.png");
            }
            if (Enemy.thunderIcon != null) {
                g.drawImage(Enemy.thunderIcon, iconX, iconY, 12, 12, null);
                iconX -= 14; // Move left for next icon
            }
        }

        // Draw poison effect icon if active
        if (enemy.isPoisoned()) {
            if (Enemy.poisonIcon == null) {
                Enemy.poisonIcon = LoadSave.getImageFromPath("/TowerAssets/poison_icon.png");
            }
            if (Enemy.poisonIcon != null) {
                g.drawImage(Enemy.poisonIcon, iconX, iconY, 12, 12, null);
                iconX -= 14; // Move left for next icon
            }
        }
    }
    public void spawnEnemy(int nextEnemy) {
        addEnemy(nextEnemy);
    }

    public ArrayList<Enemy> getEnemies() {
        return enemies;
    }

    public void clearEnemies() {
        enemies.clear();
    }

    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
        long spawnTime = System.currentTimeMillis();
        enemySpawnTimes.put(enemy, spawnTime);

    }

    /**
     * Teleports an enemy back to the starting point of the path.
     * The enemy retains its current HP and status effects.
     *
     * @param enemy The enemy to teleport
     * @return true if teleportation was successful, false otherwise
     */
    public boolean teleportEnemyToStart(Enemy enemy) {
        if (!pathFound || pathPoints.isEmpty()) {
            System.out.println("Cannot teleport enemy: Path not found or empty");
            return false;
        }

        GridPoint startingPoint = pathPoints.get(0);
        if (startingPoint == null) {
            System.out.println("Cannot teleport enemy: Invalid starting point");
            return false;
        }
        float newX = startingPoint.getX() * tileSize + tileSize / 2;
        float newY = startingPoint.getY() * tileSize + tileSize / 2;

        enemy.setX(newX);
        enemy.setY(newY);

        enemy.setCurrentPathIndex(0);
        enemy.applyTeleportEffect();

        System.out.println("Enemy " + enemy.getId() + " teleported back to start!");
        return true;
    }

    public boolean canTargetEnemy(Enemy enemy) {
        return isEnemyTargetable(enemy);
    }

    public void render(Graphics g) {
        for (Enemy e : enemies) {
            if (e.isAlive()) {
                if (isGoblinInvisible(e)) {
                    drawEnemySilhouette(e, g);
                } else {
                    drawEnemy(e, g);
                }
            }
        }
    }

    private boolean wasNight = false;

    private boolean isGoblinInvisible(Enemy enemy) {
        if (weatherManager == null) {
            return false;
        }

        boolean isNight = weatherManager.isNight();

        if (isNight && !wasNight) {
            long currentTime = System.currentTimeMillis();
            for (Enemy e : enemies) {
                if (e.getEnemyType() == 0) { // GOBLIN
                    enemySpawnTimes.put(e, currentTime);
                }
            }
        }
        wasNight = isNight;

        if (isNight && enemy.getEnemyType() == 0) { // GOBLIN = 0
            // Check if goblin is lit by any tower with light
            if (playing.getTowerManager().isEnemyLit(enemy)) {
                enemy.setInvisible(false);
                return false; // Goblin is visible due to tower light
            }

            Long spawnTime = enemySpawnTimes.get(enemy);

            if (spawnTime == null) {
                long currentTime = System.currentTimeMillis();
                enemySpawnTimes.put(enemy, currentTime);
                enemy.setInvisible(true);
                return true;
            }

            long currentTime = System.currentTimeMillis();
            boolean isInvisible = (currentTime - spawnTime) < 10000; // First 10 seconds invisible

            enemy.setInvisible(isInvisible);
            return isInvisible;
        }
        enemy.setInvisible(false);
        return false;
    }

    private boolean isEnemyTargetable(Enemy enemy) {
        return !isGoblinInvisible(enemy);
    }

    private void drawEnemySilhouette(Enemy enemy, Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        AlphaComposite originalComposite = (AlphaComposite) g2d.getComposite();

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2d.setColor(new Color(0, 0, 0, 150));

        int drawX = (int)(enemy.getX() - 25);
        int drawY = (int)(enemy.getY() - 30);
        int drawWidth = 50;
        int drawHeight = 60;

        g2d.fillOval(drawX + 10, drawY + 20, drawWidth - 20, drawHeight - 25);

        g2d.fillOval(drawX + 15, drawY + 5, drawWidth - 30, 25);

        g2d.fillOval(drawX + 8, drawY + 8, 8, 12);
        g2d.fillOval(drawX + drawWidth - 16, drawY + 8, 8, 12);

        g2d.setColor(new Color(255, 0, 0, 100)); // Red glow for eyes
        g2d.fillOval(drawX + 18, drawY + 12, 3, 3);
        g2d.fillOval(drawX + 28, drawY + 12, 3, 3);

        g2d.setComposite(originalComposite);
    }

    private void drawEnemies(Graphics g) {
        for (Enemy e : enemies) {
            if (e.isAlive()) {
                drawEnemy(e, g);
            }
        }
    }

    private void updateCombatSynergy() {
        // Get all knights and goblins
        ArrayList<Enemy> knights = new ArrayList<>();
        ArrayList<Enemy> goblins = new ArrayList<>();

        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                if (enemy.getEnemyType() == Constants.Enemies.KNIGHT) {
                    knights.add(enemy);
                } else if (enemy.getEnemyType() == Constants.Enemies.GOBLIN) {
                    goblins.add(enemy);
                }
            }
        }

        // Check each knight's distance to goblins
        for (Enemy knight : knights) {
            boolean hasNearbyGoblin = false;
            float closestGoblinSpeed = 0;

            for (Enemy goblin : goblins) {
                // Use squared distance comparison for better performance
                float distanceSquared = calculateDistanceSquared(knight, goblin);
                if (distanceSquared < COMBAT_SYNERGY_DISTANCE_SQUARED) {
                    hasNearbyGoblin = true;
                    closestGoblinSpeed = goblin.getSpeed();
                    break;
                }
            }

            if (hasNearbyGoblin) {
                knight.applyCombatSynergy(closestGoblinSpeed);
            } else {
                knight.removeCombatSynergy();
            }
        }
    }


    // Optimized version for distance comparison without sqrt
    private float calculateDistanceSquared(Enemy e1, Enemy e2) {
        float xDiff = e1.getSpriteCenterX() - e2.getSpriteCenterX();
        float yDiff = e1.getSpriteCenterY() - e2.getSpriteCenterY();
        return xDiff * xDiff + yDiff * yDiff;
    }

    /**
     * Update all existing enemies with new stats from GameOptions
     * This is called when difficulty changes to apply new enemy stats
     */
    public void updateAllEnemyStatsFromOptions() {
        if (gameOptions == null) {
            System.out.println("Warning: Cannot update enemy stats - GameOptions is null");
            return;
        }

        System.out.println("Updating all existing enemies with new difficulty settings...");
        for (Enemy enemy : enemies) {
            if (enemy != null && enemy.isAlive()) {
                enemy.updateStatsFromOptions(gameOptions);
            }
        }
        System.out.println("Updated " + enemies.size() + " enemies with new difficulty settings");
    }

    /**
     * Update GameOptions reference (for when difficulty changes)
     */
    public void updateGameOptions(GameOptions newOptions) {
        this.gameOptions = newOptions != null ? newOptions : GameOptions.defaults();
        System.out.println("EnemyManager: Updated GameOptions reference");
    }

    public static BufferedImage getEnemyFrame(int enemyType, int animationIndex) {
        // Assuming enemyImages is a static array or accessible in a way that allows this method to work
        // You may need to adjust this based on how frames are stored
        return enemyImages[enemyType * 6 + animationIndex]; // Assuming 6 frames per enemy type
    }

}


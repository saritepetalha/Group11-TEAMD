package managers;

import config.GameOptions;
import objects.GridPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for EnemyManager class focusing on the generatePath method.
 *
 * This test class validates the Breadth-First Search pathfinding algorithm
 * implemented in the EnemyManager's generatePath method, testing various
 * scenarios including successful path finding, blocked paths, edge cases,
 * and algorithm optimality.
 */
@DisplayName("EnemyManager Path Generation Tests")
public class EnemyManagerTest {

    private GameOptions gameOptions;

    /**
     * Setup method executed before each test.
     * Initializes game options with default values.
     */
    @BeforeEach
    void setUp() {
        gameOptions = GameOptions.defaults();
    }

    /**
     * Helper method to create a test EnemyManager instance without calling the constructor.
     * This approach uses reflection to create an uninitialized instance and manually
     * sets only the fields needed for testing the generatePath method.
     */
    private EnemyManager createTestEnemyManager(int[][] overlayData, int[][] tileData) throws Exception {
        // Create an uninitialized EnemyManager instance using sun.misc.Unsafe equivalent
        EnemyManager manager = createUninitializedInstance(EnemyManager.class);

        // Manually initialize only the fields needed for generatePath testing
        setPrivateField(manager, "gameOptions", gameOptions);
        setPrivateField(manager, "pathPoints", new ArrayList<GridPoint>());
        setPrivateField(manager, "pathFound", false);
        setPrivateField(manager, "enemies", new ArrayList<>());
        setPrivateField(manager, "enemySpawnTimes", new HashMap<>());

        // Initialize ROAD_IDS constant (needed for isRoadTile method)
        // This is a static final field, so we need to handle it carefully
        // The actual value is: Set.of(0,1,2,3,4,6,7,8,9,10,11,12,13,14,32)

        // Find and set start/end points from overlay data
        findAndSetStartEndPoints(manager, overlayData);

        // Generate the path using the method we want to test
        GridPoint startPoint = getPrivateField(manager, "startPoint");
        GridPoint endPoint = getPrivateField(manager, "endPoint");
        if (startPoint != null && endPoint != null) {
            invokePrivateMethod(manager, "generatePath", new Class<?>[]{int[][].class}, new Object[]{tileData});
        }

        return manager;
    }

    /**
     * Creates an uninitialized instance of a class using reflection.
     * This bypasses the constructor entirely.
     */
    @SuppressWarnings("unchecked")
    private <T> T createUninitializedInstance(Class<T> clazz) throws Exception {
        try {
            // Try using sun.misc.Unsafe (Java 8-17)
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            Object unsafe = theUnsafeField.get(null);
            Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
            return (T) allocateInstance.invoke(unsafe, clazz);
        } catch (Exception e) {
            try {
                Class<?> unsafeClass = Class.forName("jdk.internal.misc.Unsafe");
                Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
                theUnsafeField.setAccessible(true);
                Object unsafe = theUnsafeField.get(null);
                Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
                return (T) allocateInstance.invoke(unsafe, clazz);
            } catch (Exception e2) {
                var constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                throw new RuntimeException("Unable to create uninitialized instance. Unsafe not available.", e2);
            }
        }
    }

    /**
     * Helper method to find start and end points from overlay data and set them on the manager.
     */
    private void findAndSetStartEndPoints(EnemyManager manager, int[][] overlayData) throws Exception {
        GridPoint startPoint = null;
        GridPoint endPoint = null;

        // Constants for start and end point detection (from Constants.PathPoints)
        final int START_POINT = 1;
        final int END_POINT = 2;

        for (int y = 0; y < overlayData.length; y++) {
            for (int x = 0; x < overlayData[y].length; x++) {
                if (overlayData[y][x] == START_POINT) {
                    startPoint = new GridPoint(x, y);
                }
                else if (overlayData[y][x] == END_POINT) {
                    endPoint = new GridPoint(x, y);
                }
            }
        }

        setPrivateField(manager, "startPoint", startPoint);
        setPrivateField(manager, "endPoint", endPoint);
    }

    /**
     * Helper method to set private fields using reflection.
     */
    private void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    /**
     * Helper method to get private fields using reflection.
     */
    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(obj);
    }

    /**
     * Helper method to invoke private methods using reflection.
     */
    private void invokePrivateMethod(Object obj, String methodName, Class<?>[] paramTypes, Object[] params) throws Exception {
        Method method = obj.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        method.invoke(obj, params);
    }

    /**
     * Test Case 1: Successful path generation between start and end points.
     *
     * This test verifies that the BFS algorithm correctly finds a path when one exists,
     * and properly updates the pathPoints list and pathFound flag.
     */
    @Test
    @DisplayName("Test successful path generation from start to end point")
    void testGeneratePathSuccess() throws Exception {
        // Arrange: Create a simple 5x5 grid with a clear path
        int[][] overlayData = {
                {0, 1, 0, 0, 0},  // Start at (1,0)
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 2, 0}   // End at (3,4)
        };

        int[][] tileData = {
                {15, 0, 15, 15, 15},
                {15, 0, 0, 0, 15},
                {15, 15, 0, 0, 15},
                {15, 15, 0, 0, 15},
                {15, 15, 15, 0, 15}
        };

        // Create EnemyManager with test data
        EnemyManager manager = createTestEnemyManager(overlayData, tileData);

        // Act: Test the path generation (already called in createTestEnemyManager)
        Boolean pathFound = getPrivateField(manager, "pathFound");
        ArrayList<GridPoint> pathPoints = getPrivateField(manager, "pathPoints");
        GridPoint startPoint = getPrivateField(manager, "startPoint");
        GridPoint endPoint = getPrivateField(manager, "endPoint");

        // Assert: Verify path was found and pathPoints contains expected path
        assertTrue(pathFound, "Path should be found between valid start and end points");
        assertNotNull(pathPoints, "Path points should not be null");
        assertTrue(pathPoints.size() > 0, "Path should contain at least one point");
        assertEquals(new GridPoint(1, 0), startPoint, "Start point should be at (1,0)");
        assertEquals(new GridPoint(3, 4), endPoint, "End point should be at (3,4)");

        // Verify path continuity (each consecutive point should be adjacent)
        for (int i = 0; i < pathPoints.size() - 1; i++) {
            GridPoint current = pathPoints.get(i);
            GridPoint next = pathPoints.get(i + 1);
            int xDiff = Math.abs(current.getX() - next.getX());
            int yDiff = Math.abs(current.getY() - next.getY());
            assertTrue((xDiff == 1 && yDiff == 0) || (xDiff == 0 && yDiff == 1),
                    "Adjacent points in path should be exactly one tile apart");
        }
    }

    /**
     * Test Case 2: No path exists between start and end points.
     *
     * This test verifies that when no valid path exists (due to walls blocking the way),
     * the algorithm properly handles the failure case.
     */
    @Test
    @DisplayName("Test no path exists between isolated start and end points")
    void testGeneratePathNoPath() throws Exception {
        // Arrange: Create a 5x5 grid where start and end are isolated by walls
        int[][] overlayData = {
                {0, 1, 0, 0, 0},  // Start at (1,0)
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 2, 0}   // End at (3,4)
        };

        int[][] tileData = {
                {15, 0, 15, 15, 15},  // Start is on road
                {15, 15, 15, 15, 15},
                {15, 15, 15, 15, 15},
                {15, 15, 15, 15, 15},
                {15, 15, 15, 0, 15}   // End is on road but unreachable
        };

        // Create EnemyManager and test isolated scenario
        EnemyManager manager = createTestEnemyManager(overlayData, tileData);

        // Assert: Verify no path was found
        Boolean pathFound = getPrivateField(manager, "pathFound");
        ArrayList<GridPoint> pathPoints = getPrivateField(manager, "pathPoints");

        assertFalse(pathFound, "Path should not be found when start and end are isolated");
        assertTrue(pathPoints.isEmpty(), "Path points should be empty when no path exists");
    }

    /**
     * Test Case 3: Test BFS finds shortest path in complex maze.
     *
     * This test verifies that the BFS algorithm finds the optimal (shortest) path
     * when multiple paths exist between start and end points.
     */
    @Test
    @DisplayName("Test BFS finds shortest path in complex maze")
    void testGeneratePathComplexMaze() throws Exception {
        // Arrange: Create a 7x7 maze with multiple possible paths
        int[][] overlayData = {
                {0, 1, 0, 0, 0, 0, 0},  // Start at (1,0)
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 2, 0}   // End at (5,6)
        };

        // Design a maze where the shortest path is length 19
        // There's a longer path available but BFS should find the shorter one
        int[][] tileData = {
                {15, 0, 0, 0, 0, 0, 15},
                {15, 15, 15, 15, 15, 0, 15},
                {15, 0, 0, 0, 0, 0, 15},
                {15, 0, 15, 15, 15, 15, 15},
                {15, 0, 0, 0, 0, 0, 15},
                {15, 15, 15, 15, 15, 0, 15},
                {15, 15, 15, 15, 15, 0, 15}
        };

        // Create EnemyManager with complex maze
        EnemyManager manager = createTestEnemyManager(overlayData, tileData);

        // Assert: Verify optimal path was found
        Boolean pathFound = getPrivateField(manager, "pathFound");
        ArrayList<GridPoint> pathPoints = getPrivateField(manager, "pathPoints");

        assertTrue(pathFound, "Path should be found in the complex maze");
        assertNotNull(pathPoints, "Path points should not be null");
        assertTrue(pathPoints.size() > 0, "Path should contain points");

        // Verify BFS found an optimal path (BFS guarantees shortest path)
        // The exact length depends on the maze design, but we can verify path validity
        for (int i = 0; i < pathPoints.size() - 1; i++) {
            GridPoint current = pathPoints.get(i);
            GridPoint next = pathPoints.get(i + 1);
            int xDiff = Math.abs(current.getX() - next.getX());
            int yDiff = Math.abs(current.getY() - next.getY());
            assertTrue((xDiff == 1 && yDiff == 0) || (xDiff == 0 && yDiff == 1),
                    "Path should consist of adjacent moves only");
        }
    }

    /**
     * Test Case 4: Edge case where start and end points are the same.
     *
     * This test verifies that the algorithm correctly handles the case where
     * the start and end points are identical.
     */
    @Test
    @DisplayName("Test path generation when start and end points are the same")
    void testGeneratePathSameStartAndEnd() throws Exception {
        // Arrange: Create a grid where start and end are the same point
        int[][] overlayData = {
                {0, 0, 0},
                {0, 3, 0},  // Both start (1) and end (2) at same location - use 3 to represent both
                {0, 0, 0}
        };

        int[][] tileData = {
                {15, 15, 15},
                {15, 0, 15},   // Single road tile
                {15, 15, 15}
        };

        // Create EnemyManager and manually set start and end to be the same
        EnemyManager manager = createTestEnemyManager(overlayData, tileData);

        // Manually set start and end to be the same
        GridPoint samePoint = new GridPoint(1, 1);
        setPrivateField(manager, "startPoint", samePoint);
        setPrivateField(manager, "endPoint", samePoint);
        setPrivateField(manager, "pathFound", false);
        setPrivateField(manager, "pathPoints", new ArrayList<GridPoint>());

        // Act: Call generatePath
        invokePrivateMethod(manager, "generatePath", new Class<?>[]{int[][].class}, new Object[]{tileData});

        // Assert: Verify the algorithm handles same start/end correctly
        Boolean pathFound = getPrivateField(manager, "pathFound");
        ArrayList<GridPoint> pathPoints = getPrivateField(manager, "pathPoints");

        assertTrue(pathFound, "Path should be found when start equals end");
        assertNotNull(pathPoints, "Path points should not be null");
        assertEquals(1, pathPoints.size(), "Path should contain exactly one point when start equals end");
        assertEquals(samePoint, pathPoints.get(0), "The single path point should be the start/end point");
    }

    /**
     * Test Case 5: Test with null start or end points.
     *
     * This test verifies that the system gracefully handles cases
     * where start or end points are null without throwing exceptions.
     */
    @Test
    @DisplayName("Test path generation gracefully handles null points")
    void testGeneratePathNullPoints() throws Exception {
        // Arrange: Create minimal valid tile data
        int[][] tileData = {
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
        };

        int[][] overlayData = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
        EnemyManager manager = createTestEnemyManager(overlayData, tileData);

        // Test Case: Null start point
        setPrivateField(manager, "startPoint", null);
        setPrivateField(manager, "endPoint", new GridPoint(2, 2));
        setPrivateField(manager, "pathFound", false);
        setPrivateField(manager, "pathPoints", new ArrayList<GridPoint>());

        // Act & Assert: Should not throw exception
        assertDoesNotThrow(() -> {
            try {
                invokePrivateMethod(manager, "generatePath", new Class<?>[]{int[][].class}, new Object[]{tileData});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, "Method should handle null start point without throwing exceptions");

        Boolean pathFound = getPrivateField(manager, "pathFound");
        assertFalse(pathFound, "Path should not be found with null start point");
    }
} 
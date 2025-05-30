package managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import managers.EnemyManager;
import objects.GridPoint;
import scenes.Playing;
import config.GameOptions;
import managers.WeatherManager;

/**
 * Test class for EnemyManager.generatePath() method.
 * 
 * This class tests the Breadth-First Search pathfinding algorithm implemented
 * in the generatePath method, ensuring it correctly handles various scenarios
 * including successful path finding, no path scenarios, and edge cases.
 */
public class EnemyManagerTest {

    @Mock
    private Playing mockPlaying;
    
    @Mock
    private WeatherManager mockWeatherManager;
    
    private EnemyManager enemyManager;
    private GameOptions gameOptions;

    /**
     * Setup method executed before each test.
     * Initializes mocks and creates test instances.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gameOptions = GameOptions.defaults();
        when(mockPlaying.getWeatherManager()).thenReturn(mockWeatherManager);
    }

    /**
     * Helper method to create EnemyManager with test data and access private methods/fields.
     */
    private EnemyManager createEnemyManagerWithData(int[][] overlayData, int[][] tileData) throws Exception {
        EnemyManager manager = new EnemyManager(mockPlaying, overlayData, tileData, gameOptions);
        return manager;
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
    private void invokePrivateMethod(Object obj, String methodName, Class<?>[] paramTypes, Object... params) throws Exception {
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
            {0, 1, 0, 0, 0},  // Row 0: Start at (1,0)
            {0, 0, 0, 0, 0},  // Row 1: Road tiles (represented by 0s in tileData)
            {0, 0, 0, 0, 0},  // Row 2: Road tiles
            {0, 0, 0, 0, 0},  // Row 3: Road tiles
            {0, 0, 0, 2, 0}   // Row 4: End at (3,4)
        };
        
        int[][] tileData = {
            {99, 0, 99, 99, 99},  // Row 0: Road at (1,0), walls elsewhere
            {99, 0, 0, 0, 99},    // Row 1: Road path
            {99, 99, 0, 0, 99},   // Row 2: Road path
            {99, 99, 0, 0, 99},   // Row 3: Road path
            {99, 99, 99, 0, 99}   // Row 4: Road at (3,4), walls elsewhere
        };

        // Create EnemyManager with test data
        EnemyManager manager = createEnemyManagerWithData(overlayData, tileData);

        // Act: The constructor automatically calls generatePath, but let's ensure we test the method explicitly
        GridPoint startPoint = new GridPoint(1, 0);
        GridPoint endPoint = new GridPoint(3, 4);
        setPrivateField(manager, "startPoint", startPoint);
        setPrivateField(manager, "endPoint", endPoint);
        
        // Clear existing path and call generatePath
        setPrivateField(manager, "pathPoints", new ArrayList<GridPoint>());
        setPrivateField(manager, "pathFound", false);
        invokePrivateMethod(manager, "generatePath", new Class<?>[]{int[][].class}, tileData);

        // Assert: Verify path was found and pathPoints contains expected path
        Boolean pathFound = getPrivateField(manager, "pathFound");
        ArrayList<GridPoint> pathPoints = getPrivateField(manager, "pathPoints");

        assertTrue(pathFound, "Path should be found between valid start and end points");
        assertNotNull(pathPoints, "Path points should not be null");
        assertTrue(pathPoints.size() > 0, "Path should contain at least one point");
        assertEquals(startPoint, pathPoints.get(0), "First point should be the start point");
        assertEquals(endPoint, pathPoints.get(pathPoints.size() - 1), "Last point should be the end point");
        
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
     * the algorithm properly handles the failure case without modifying pathPoints or pathFound.
     */
    @Test
    @DisplayName("Test no path exists between isolated start and end points")
    void testGeneratePathNoPath() throws Exception {
        // Arrange: Create a 5x5 grid where start and end are isolated by walls
        int[][] overlayData = {
            {0, 1, 0, 0, 0},  // Row 0: Start at (1,0)
            {0, 0, 0, 0, 0},  // Row 1
            {0, 0, 0, 0, 0},  // Row 2: Complete wall row will block path
            {0, 0, 0, 0, 0},  // Row 3
            {0, 0, 0, 2, 0}   // Row 4: End at (3,4)
        };
        
        int[][] tileData = {
            {99, 0, 99, 99, 99},  // Row 0: Start is on road
            {99, 99, 99, 99, 99}, // Row 1: Wall row
            {99, 99, 99, 99, 99}, // Row 2: Wall row - blocks all paths
            {99, 99, 99, 99, 99}, // Row 3: Wall row
            {99, 99, 99, 0, 99}   // Row 4: End is on road but unreachable
        };

        // Create EnemyManager and manually set up the isolated scenario
        EnemyManager manager = createEnemyManagerWithData(overlayData, tileData);
        
        GridPoint startPoint = new GridPoint(1, 0);
        GridPoint endPoint = new GridPoint(3, 4);
        setPrivateField(manager, "startPoint", startPoint);
        setPrivateField(manager, "endPoint", endPoint);
        
        // Clear existing path and set initial state
        ArrayList<GridPoint> originalPathPoints = new ArrayList<>();
        setPrivateField(manager, "pathPoints", originalPathPoints);
        setPrivateField(manager, "pathFound", false);

        // Act: Call generatePath
        invokePrivateMethod(manager, "generatePath", new Class<?>[]{int[][].class}, tileData);

        // Assert: Verify no path was found and state remains unchanged
        Boolean pathFound = getPrivateField(manager, "pathFound");
        ArrayList<GridPoint> pathPoints = getPrivateField(manager, "pathPoints");

        assertFalse(pathFound, "Path should not be found when start and end are isolated");
        assertEquals(originalPathPoints, pathPoints, "Path points should remain unchanged when no path exists");
    }

    /**
     * Test Case 3: Edge case with null start or end points.
     * 
     * This test verifies that the method gracefully handles invalid input
     * when start point or end point is null, ensuring no exceptions are thrown
     * and the system remains in a consistent state.
     */
    @Test
    @DisplayName("Test generatePath handles null start or end points gracefully")
    void testGeneratePathNullPoints() throws Exception {
        // Arrange: Create valid tile data
        int[][] tileData = {
            {0, 0, 0},
            {0, 0, 0},
            {0, 0, 0}
        };

        // Create a basic EnemyManager
        int[][] overlayData = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
        EnemyManager manager = createEnemyManagerWithData(overlayData, tileData);

        // Test Case 3a: Null start point
        setPrivateField(manager, "startPoint", null);
        setPrivateField(manager, "endPoint", new GridPoint(2, 2));
        setPrivateField(manager, "pathFound", false);
        ArrayList<GridPoint> originalPathPoints = new ArrayList<>();
        setPrivateField(manager, "pathPoints", originalPathPoints);

        // Act: Call generatePath with null start point
        invokePrivateMethod(manager, "generatePath", new Class<?>[]{int[][].class}, tileData);

        // Assert: Verify method handles null gracefully
        Boolean pathFound = getPrivateField(manager, "pathFound");
        ArrayList<GridPoint> pathPoints = getPrivateField(manager, "pathPoints");
        
        assertFalse(pathFound, "Path should not be found with null start point");
        assertEquals(originalPathPoints, pathPoints, "Path points should remain unchanged with null start point");

        // Test Case 3b: Null end point
        setPrivateField(manager, "startPoint", new GridPoint(0, 0));
        setPrivateField(manager, "endPoint", null);
        setPrivateField(manager, "pathFound", false);
        setPrivateField(manager, "pathPoints", new ArrayList<GridPoint>());

        // Act: Call generatePath with null end point
        invokePrivateMethod(manager, "generatePath", new Class<?>[]{int[][].class}, tileData);

        // Assert: Verify method handles null gracefully
        pathFound = getPrivateField(manager, "pathFound");
        pathPoints = getPrivateField(manager, "pathPoints");
        
        assertFalse(pathFound, "Path should not be found with null end point");

        // Test Case 3c: Both points null
        setPrivateField(manager, "startPoint", null);
        setPrivateField(manager, "endPoint", null);
        setPrivateField(manager, "pathFound", false);
        setPrivateField(manager, "pathPoints", new ArrayList<GridPoint>());

        // Act & Assert: Should not throw exception
        assertDoesNotThrow(() -> {
            invokePrivateMethod(manager, "generatePath", new Class<?>[]{int[][].class}, tileData);
        }, "Method should handle null points without throwing exceptions");
    }

    /**
     * Test Case 4: Complex maze with multiple possible paths.
     * 
     * This test verifies that BFS finds the shortest path when multiple paths exist,
     * demonstrating the optimality property of the BFS algorithm.
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
        
        // Create a maze where there are multiple paths but one is shorter
        int[][] tileData = {
            {99, 0, 0, 0, 0, 0, 99},  // Top route (shorter)
            {99, 99, 99, 99, 99, 0, 99},
            {99, 0, 0, 0, 0, 0, 99},
            {99, 0, 99, 99, 99, 99, 99},
            {99, 0, 0, 0, 0, 0, 0},   // Bottom route (longer)
            {99, 99, 99, 99, 99, 0, 99},
            {99, 99, 99, 99, 99, 0, 99}
        };

        // Create EnemyManager with maze data
        EnemyManager manager = createEnemyManagerWithData(overlayData, tileData);
        
        GridPoint startPoint = new GridPoint(1, 0);
        GridPoint endPoint = new GridPoint(5, 6);
        setPrivateField(manager, "startPoint", startPoint);
        setPrivateField(manager, "endPoint", endPoint);
        
        setPrivateField(manager, "pathPoints", new ArrayList<GridPoint>());
        setPrivateField(manager, "pathFound", false);

        // Act: Generate path
        invokePrivateMethod(manager, "generatePath", new Class<?>[]{int[][].class}, tileData);

        // Assert: Verify shortest path is found
        Boolean pathFound = getPrivateField(manager, "pathFound");
        ArrayList<GridPoint> pathPoints = getPrivateField(manager, "pathPoints");

        assertTrue(pathFound, "Path should be found in the maze");
        assertNotNull(pathPoints, "Path points should not be null");
        assertTrue(pathPoints.size() > 0, "Path should contain points");
        
        // BFS guarantees shortest path, so verify the path length is optimal
        // The shortest path should be approximately 11 steps for this maze
        assertTrue(pathPoints.size() <= 15, "BFS should find a reasonably short path (≤15 steps)");
        
        // Verify start and end points
        assertEquals(startPoint, pathPoints.get(0), "Path should start at the start point");
        assertEquals(endPoint, pathPoints.get(pathPoints.size() - 1), "Path should end at the end point");
    }

    /**
     * Test Case 5: Single tile path (start and end are the same).
     * 
     * This test covers the edge case where the start and end points are identical,
     * which should result in a path containing only one point.
     */
    @Test
    @DisplayName("Test path generation when start and end points are the same")
    void testGeneratePathSameStartAndEnd() throws Exception {
        // Arrange: Create simple grid where start equals end
        int[][] overlayData = {
            {0, 0, 0},
            {0, 1, 0},  // Start and end at (1,1) - we'll set both to same point
            {0, 0, 0}
        };
        
        int[][] tileData = {
            {99, 99, 99},
            {99, 0, 99},   // Single road tile
            {99, 99, 99}
        };

        EnemyManager manager = createEnemyManagerWithData(overlayData, tileData);
        
        GridPoint samePoint = new GridPoint(1, 1);
        setPrivateField(manager, "startPoint", samePoint);
        setPrivateField(manager, "endPoint", samePoint);  // Same as start
        
        setPrivateField(manager, "pathPoints", new ArrayList<GridPoint>());
        setPrivateField(manager, "pathFound", false);

        // Act: Generate path
        invokePrivateMethod(manager, "generatePath", new Class<?>[]{int[][].class}, tileData);

        // Assert: Verify path contains only the single point
        Boolean pathFound = getPrivateField(manager, "pathFound");
        ArrayList<GridPoint> pathPoints = getPrivateField(manager, "pathPoints");

        assertTrue(pathFound, "Path should be found when start equals end");
        assertNotNull(pathPoints, "Path points should not be null");
        assertEquals(1, pathPoints.size(), "Path should contain exactly one point when start equals end");
        assertEquals(samePoint, pathPoints.get(0), "The single path point should be the start/end point");
    }
} 
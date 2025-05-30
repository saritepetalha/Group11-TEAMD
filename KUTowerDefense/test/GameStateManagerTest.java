
import managers.GameStateManager;
import managers.GameStateMemento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class for GameStateManager.loadGameState(String saveFileName) method.
 * Tests the complex load game functionality with various edge cases and error conditions.
 */
public class GameStateManagerTest {

    private GameStateManager gameStateManager;
    private GameStateMemento testMemento;

    @BeforeEach
    void setUp() {
        gameStateManager = new GameStateManager();

        // Create a test GameStateMemento with sample data for creating test files
        List<GameStateMemento.TowerState> towerStates = new ArrayList<>();
        towerStates.add(new GameStateMemento.TowerState(5, 10, 1, 2));
        towerStates.add(new GameStateMemento.TowerState(15, 20, 2, 3));

        List<GameStateMemento.EnemyState> enemyStates = new ArrayList<>();
        enemyStates.add(new GameStateMemento.EnemyState(25.5f, 30.7f, 1, 100, 0));

        // Create GameOptions using default constructor
        config.GameOptions gameOptions = new config.GameOptions();

        testMemento = new GameStateMemento(
                1000, // gold
                95,   // health
                50,   // shield
                3,    // waveIndex
                2,    // groupIndex
                towerStates,
                enemyStates,
                gameOptions
        );
    }

    @AfterEach
    void tearDown() {
        // Clean up any test files created during testing
        try {
            String saveDir = "demo/src/main/resources/Saves";
            File testFile1 = new File(saveDir, "test_load_success.json");
            File testFile2 = new File(saveDir, "test_load_no_extension.json");
            File testFile3 = new File(saveDir, "test_invalid_json.json");
            File testFile4 = new File(saveDir, "test_load_complex.json");

            if (testFile1.exists()) testFile1.delete();
            if (testFile2.exists()) testFile2.delete();
            if (testFile3.exists()) testFile3.delete();
            if (testFile4.exists()) testFile4.delete();
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    /**
     * Test Case 1: Successful load of existing game state
     * Tests the normal flow where a valid save file exists and should be loaded successfully
     */
    @Test
    void testLoadGameState_Success() {
        String testFileName = "test_load_success";

        // First create a save file to load from
        assertDoesNotThrow(() -> gameStateManager.saveGameState(testMemento, testFileName));

        // Verify the file was created
        assertTrue(gameStateManager.saveFileExists(testFileName));

        // Load the game state
        GameStateMemento loadedMemento = gameStateManager.loadGameState(testFileName);

        // Verify the loaded state matches the saved state
        assertNotNull(loadedMemento);
        assertEquals(testMemento.getGold(), loadedMemento.getGold());
        assertEquals(testMemento.getHealth(), loadedMemento.getHealth());
        assertEquals(testMemento.getShield(), loadedMemento.getShield());
        assertEquals(testMemento.getWaveIndex(), loadedMemento.getWaveIndex());
        assertEquals(testMemento.getGroupIndex(), loadedMemento.getGroupIndex());

        // Verify tower states
        assertEquals(testMemento.getTowerStates().size(), loadedMemento.getTowerStates().size());
        GameStateMemento.TowerState originalTower = testMemento.getTowerStates().get(0);
        GameStateMemento.TowerState loadedTower = loadedMemento.getTowerStates().get(0);
        assertEquals(originalTower.getX(), loadedTower.getX());
        assertEquals(originalTower.getY(), loadedTower.getY());
        assertEquals(originalTower.getType(), loadedTower.getType());
        assertEquals(originalTower.getLevel(), loadedTower.getLevel());
    }

    /**
     * Test Case 2: Load game state from non-existent file
     * Tests the error handling when trying to load from a file that doesn't exist
     */
    @Test
    void testLoadGameState_FileNotFound() {
        String nonExistentFileName = "non_existent_file";

        // Verify the file doesn't exist
        assertFalse(gameStateManager.saveFileExists(nonExistentFileName));

        // Attempt to load from non-existent file
        GameStateMemento loadedMemento = gameStateManager.loadGameState(nonExistentFileName);

        // Should return null when file doesn't exist
        assertNull(loadedMemento);
    }

    /**
     * Test Case 3: Load game state with filename that doesn't have .json extension
     * Tests the filename normalization functionality where .json is appended if missing
     */
    @Test
    void testLoadGameState_FileNameWithoutExtension() {
        String fileNameWithoutExtension = "test_load_no_extension";

        // Save with the filename (to create the file for loading)
        assertDoesNotThrow(() -> gameStateManager.saveGameState(testMemento, fileNameWithoutExtension));

        // Load using the same filename (should automatically append .json)
        GameStateMemento loadedMemento = gameStateManager.loadGameState(fileNameWithoutExtension);

        // Verify successful load
        assertNotNull(loadedMemento);
        assertEquals(testMemento.getGold(), loadedMemento.getGold());
        assertEquals(testMemento.getHealth(), loadedMemento.getHealth());
    }

    /**
     * Test Case 4: Load game state from corrupted/invalid JSON file
     * Tests error handling when the file exists but contains invalid JSON
     */
    @Test
    void testLoadGameState_CorruptedFile() throws IOException {
        String corruptedFileName = "test_invalid_json";

        // Create a file with invalid JSON content
        String saveDir = "demo/src/main/resources/Saves";
        Files.createDirectories(Paths.get(saveDir));
        File corruptedFile = new File(saveDir, corruptedFileName + ".json");
        Files.write(corruptedFile.toPath(), "{ invalid json content }".getBytes());

        // Attempt to load the corrupted file
        GameStateMemento loadedMemento = gameStateManager.loadGameState(corruptedFileName);

        // Should return null when JSON is invalid
        assertNull(loadedMemento);
    }

    /**
     * Test Case 5: Load game state with filename that already includes .json extension
     * Tests the case-insensitive extension checking logic
     */
    @Test
    void testLoadGameState_FilenameWithExtension() {
        String baseFileName = "test_load_with_ext";
        String fileNameWithExtension = baseFileName + ".json";

        // Create a save file
        assertDoesNotThrow(() -> gameStateManager.saveGameState(testMemento, baseFileName));

        // Load using filename WITH .json extension
        GameStateMemento loadedMemento = gameStateManager.loadGameState(fileNameWithExtension);

        // Should successfully load (method should handle extension properly)
        assertNotNull(loadedMemento);
        assertEquals(testMemento.getGold(), loadedMemento.getGold());
    }

    /**
     * Test Case 6: Load game state with complex data
     * Tests loading with multiple towers and enemies to ensure data integrity
     */
    @Test
    void testLoadGameState_ComplexData() {
        // Create a more complex game state
        List<GameStateMemento.TowerState> complexTowerStates = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            complexTowerStates.add(new GameStateMemento.TowerState(i * 10, i * 15, i % 3, i + 1));
        }

        List<GameStateMemento.EnemyState> complexEnemyStates = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            complexEnemyStates.add(new GameStateMemento.EnemyState(
                    i * 12.5f, i * 8.7f, i % 2, 100 - (i * 10), i
            ));
        }

        config.GameOptions complexGameOptions = new config.GameOptions();

        GameStateMemento complexMemento = new GameStateMemento(
                2500, 80, 25, 7, 4, complexTowerStates, complexEnemyStates, complexGameOptions
        );

        String testFileName = "test_load_complex";

        // Save the complex state
        assertDoesNotThrow(() -> gameStateManager.saveGameState(complexMemento, testFileName));

        // Load the complex state
        GameStateMemento loadedMemento = gameStateManager.loadGameState(testFileName);

        // Verify all data is preserved during loading
        assertNotNull(loadedMemento);
        assertEquals(complexMemento.getTowerStates().size(), loadedMemento.getTowerStates().size());
        assertEquals(complexMemento.getEnemyStates().size(), loadedMemento.getEnemyStates().size());

        // Verify specific tower data
        for (int i = 0; i < complexMemento.getTowerStates().size(); i++) {
            GameStateMemento.TowerState original = complexMemento.getTowerStates().get(i);
            GameStateMemento.TowerState loaded = loadedMemento.getTowerStates().get(i);
            assertEquals(original.getX(), loaded.getX());
            assertEquals(original.getY(), loaded.getY());
            assertEquals(original.getType(), loaded.getType());
            assertEquals(original.getLevel(), loaded.getLevel());
        }

        // Verify specific enemy data
        for (int i = 0; i < complexMemento.getEnemyStates().size(); i++) {
            GameStateMemento.EnemyState original = complexMemento.getEnemyStates().get(i);
            GameStateMemento.EnemyState loaded = loadedMemento.getEnemyStates().get(i);
            assertEquals(original.getX(), loaded.getX(), 0.001f);
            assertEquals(original.getY(), loaded.getY(), 0.001f);
            assertEquals(original.getType(), loaded.getType());
            assertEquals(original.getHealth(), loaded.getHealth());
            assertEquals(original.getPathIndex(), loaded.getPathIndex());
        }
    }
}


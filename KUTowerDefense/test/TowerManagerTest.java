import static org.junit.jupiter.api.Assertions.*;

import managers.TowerManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import objects.*;
import scenes.Playing;
import strategies.TargetingStrategy;
import strategies.FirstEnemyStrategy;
import enemies.Enemy;
import managers.EnemyManager;
import managers.WeatherManager;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Field;

/**
 * Test suite for TowerManager class.
 * 
 * ====== OVERVIEW ======
 * TowerManager is a collection-based Abstract Data Type that manages all towers in a tower defense game.
 * It provides functionality for:
 * - Building and placing different types of towers (Archer, Mage, Artillery)
 * - Managing tower collection operations (add, remove, replace, clear)
 * - Coordinating tower attacks against enemies
 * - Handling visual effects and rendering
 * - Processing game state updates (cooldowns, targeting, weather effects)
 * 
 * The TowerManager acts as a centralized controller for all tower-related operations,
 * ensuring consistent state management and coordinated behavior across the game.
 * 
 * ====== TESTING APPROACH NOTE ======
 * These tests focus on the ADT (Abstract Data Type) layer of TowerManager, testing the
 * underlying data structure and method behavior. In the actual game, tower building
 * is constrained by UI rules (towers can only be built on dead tree locations via
 * TreeInteractionManager), but our tests validate the core ADT functionality that
 * supports those higher-level constraints.
 * 
 * ====== ABSTRACT FUNCTION ======
 * AF(c) = A tower management system where:
 *   - c.towers represents the set of all active towers: {t₁, t₂, ..., tₙ}
 *   - c.playing represents the game context for tower operations
 *   - c.towerImages represents the visual assets for tower rendering
 *   - c.upgradeEffects represents the set of active visual upgrade effects
 * 
 * Each tower tᵢ has position (x, y), type ∈ {ARCHER, MAGE, ARTILLERY}, level ∈ {1, 2},
 * and state including damage, range, cooldown, and targeting strategy.
 * 
 * ====== REPRESENTATION INVARIANT ======
 * 1. towers != null
 * 2. playing != null
 * 3. towerImages != null && towerImages.length == 3
 * 4. upgradeEffects != null
 * 5. ∀ tower ∈ towers: tower != null
 * 6. ∀ tower ∈ towers: tower.getX() >= 0 && tower.getY() >= 0
 * 7. ∀ tower ∈ towers: tower.getType() ∈ {ARCHER, MAGE, ARTILLERY}
 * 8. ∀ tower ∈ towers: tower.getLevel() ∈ {1, 2}
 * 9. ∀ tower ∈ towers: tower.getTargetingStrategy() != null
 * 10. No two towers occupy the exact same position: ∀ i,j: towers[i].getX() != towers[j].getX() || towers[i].getY() != towers[j].getY() when i != j
 */
@DisplayName("TowerManager Abstract Data Type Tests")
public class TowerManagerTest {

    private TowerManager towerManager;
    
    @Mock
    private Playing mockPlaying;
    
    @Mock
    private EnemyManager mockEnemyManager;
    
    @Mock
    private WeatherManager mockWeatherManager;
    
    @Mock
    private Graphics2D mockGraphics;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup basic mocks
        when(mockPlaying.getEnemyManager()).thenReturn(mockEnemyManager);
        when(mockPlaying.getWeatherManager()).thenReturn(mockWeatherManager);
        when(mockEnemyManager.getEnemies()).thenReturn(new ArrayList<>());
        when(mockWeatherManager.isRaining()).thenReturn(false);
        when(mockWeatherManager.isWindy()).thenReturn(false);
        when(mockWeatherManager.isNight()).thenReturn(false);
        
        // Create TowerManager instance
        towerManager = new TowerManager(mockPlaying);
    }

    /**
     * REPOK METHOD - Checks if the representation invariant holds.
     * This method verifies all invariant conditions are satisfied.
     * 
     * @param tm The TowerManager instance to check
     * @return true if all invariants hold, false otherwise
     */
    public static boolean repOk(TowerManager tm) {
        try {
            // Access private fields using reflection
            Field towersField = TowerManager.class.getDeclaredField("towers");
            towersField.setAccessible(true);
            ArrayList<Tower> towers = (ArrayList<Tower>) towersField.get(tm);
            
            Field playingField = TowerManager.class.getDeclaredField("playing");
            playingField.setAccessible(true);
            Playing playing = (Playing) playingField.get(tm);
            
            Field towerImagesField = TowerManager.class.getDeclaredField("towerImages");
            towerImagesField.setAccessible(true);
            Object[] towerImages = (Object[]) towerImagesField.get(tm);
            
            Field upgradeEffectsField = TowerManager.class.getDeclaredField("upgradeEffects");
            upgradeEffectsField.setAccessible(true);
            List<?> upgradeEffects = (List<?>) upgradeEffectsField.get(tm);
            
            // Invariant 1: towers != null
            if (towers == null) return false;
            
            // Invariant 2: playing != null
            if (playing == null) return false;
            
            // Invariant 3: towerImages != null && towerImages.length == 3
            if (towerImages == null || towerImages.length != 3) return false;
            
            // Invariant 4: upgradeEffects != null
            if (upgradeEffects == null) return false;
            
            // Invariant 5: ∀ tower ∈ towers: tower != null
            for (Tower tower : towers) {
                if (tower == null) return false;
            }
            
            // Invariant 6: ∀ tower ∈ towers: tower.getX() >= 0 && tower.getY() >= 0
            for (Tower tower : towers) {
                if (tower.getX() < 0 || tower.getY() < 0) return false;
            }
            
            // Invariant 7: ∀ tower ∈ towers: tower.getType() ∈ {ARCHER, MAGE, ARTILLERY}
            for (Tower tower : towers) {
                int type = tower.getType();
                if (type != constants.Constants.Towers.ARCHER && 
                    type != constants.Constants.Towers.MAGE && 
                    type != constants.Constants.Towers.ARTILLERY) {
                    return false;
                }
            }
            
            // Invariant 8: ∀ tower ∈ towers: tower.getLevel() ∈ {1, 2}
            for (Tower tower : towers) {
                int level = tower.getLevel();
                if (level != 1 && level != 2) return false;
            }
            
            // Invariant 9: ∀ tower ∈ towers: tower.getTargetingStrategy() != null
            for (Tower tower : towers) {
                if (tower.getTargetingStrategy() == null) return false;
            }
            
            // Invariant 10: No two towers occupy the exact same position
            for (int i = 0; i < towers.size(); i++) {
                for (int j = i + 1; j < towers.size(); j++) {
                    Tower tower1 = towers.get(i);
                    Tower tower2 = towers.get(j);
                    if (tower1.getX() == tower2.getX() && tower1.getY() == tower2.getY()) {
                        return false;
                    }
                }
            }
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Test Case 1: Test initial state and representation invariant.
     * 
     * Verifies that a newly created TowerManager satisfies all representation invariants
     * and is in a valid initial state.
     */
    @Test
    @DisplayName("Test 1: Initial state satisfies representation invariant")
    void testInitialStateRepresentationInvariant() {
        // Assert: Verify initial state satisfies rep invariant
        assertTrue(repOk(towerManager), "Newly created TowerManager should satisfy representation invariant");
        
        // Verify specific initial conditions
        ArrayList<Tower> towers = towerManager.getTowers();
        assertNotNull(towers, "Tower list should be initialized");
        assertTrue(towers.isEmpty(), "Initial tower list should be empty");
        
        assertNotNull(towerManager.getTowerImages(), "Tower images should be loaded");
        assertEquals(3, towerManager.getTowerImages().length, "Should have exactly 3 tower image types");
    }

    /**
     * Test Case 2: Test tower building operations maintain invariants.
     * 
     * Verifies that building different types of towers maintains the representation
     * invariant and properly adds towers to the collection.
     */
    @Test
    @DisplayName("Test 2: Tower building operations maintain representation invariant")
    void testTowerBuildingMaintainsInvariant() {
        // Act: Build different types of towers
        towerManager.buildArcherTower(64, 64);
        assertTrue(repOk(towerManager), "Building archer tower should maintain invariant");
        
        towerManager.buildMageTower(128, 64);
        assertTrue(repOk(towerManager), "Building mage tower should maintain invariant");
        
        towerManager.buildArtilerryTower(192, 64);
        assertTrue(repOk(towerManager), "Building artillery tower should maintain invariant");
        
        // Assert: Verify towers were added correctly
        ArrayList<Tower> towers = towerManager.getTowers();
        assertEquals(3, towers.size(), "Should have exactly 3 towers after building");
        
        // Verify tower types and positions
        Tower archerTower = towers.get(0);
        assertTrue(archerTower instanceof ArcherTower, "First tower should be ArcherTower");
        assertEquals(64, archerTower.getX(), "Archer tower X position should be 64");
        assertEquals(64, archerTower.getY(), "Archer tower Y position should be 64");
        
        Tower mageTower = towers.get(1);
        assertTrue(mageTower instanceof MageTower, "Second tower should be MageTower");
        assertEquals(128, mageTower.getX(), "Mage tower X position should be 128");
        
        Tower artilleryTower = towers.get(2);
        assertTrue(artilleryTower instanceof ArtilleryTower, "Third tower should be ArtilleryTower");
        assertEquals(192, artilleryTower.getX(), "Artillery tower X position should be 192");
    }

    /**
     * Test Case 3: Test tower collection operations.
     * 
     * Verifies that collection operations (add, clear, replace) maintain the
     * representation invariant and behave correctly.
     */
    @Test
    @DisplayName("Test 3: Tower collection operations maintain representation invariant")
    void testTowerCollectionOperations() {
        // Setup: Create test towers
        ArcherTower tower1 = new ArcherTower(0, 0);
        MageTower tower2 = new MageTower(64, 0);
        ArtilleryTower tower3 = new ArtilleryTower(128, 0);
        
        // Test adding towers
        towerManager.addTower(tower1);
        assertTrue(repOk(towerManager), "Adding tower should maintain invariant");
        assertEquals(1, towerManager.getTowers().size(), "Should have 1 tower after adding");
        
        towerManager.addTower(tower2);
        assertTrue(repOk(towerManager), "Adding second tower should maintain invariant");
        assertEquals(2, towerManager.getTowers().size(), "Should have 2 towers after adding");
        
        // Test replacing towers
        ArtilleryTower replacementTower = new ArtilleryTower(0, 0); // Same position as tower1
        towerManager.replaceTower(tower1, replacementTower);
        assertTrue(repOk(towerManager), "Replacing tower should maintain invariant");
        assertEquals(2, towerManager.getTowers().size(), "Should still have 2 towers after replacement");
        assertTrue(towerManager.getTowers().contains(replacementTower), "Should contain replacement tower");
        assertFalse(towerManager.getTowers().contains(tower1), "Should not contain original tower");
        
        // Test clearing towers
        towerManager.clearTowers();
        assertTrue(repOk(towerManager), "Clearing towers should maintain invariant");
        assertEquals(0, towerManager.getTowers().size(), "Should have 0 towers after clearing");
        assertTrue(towerManager.getTowers().isEmpty(), "Tower list should be empty after clearing");
    }

    /**
     * Test Case 4: Test tower building with custom targeting strategies.
     *
     * Verifies that towers can be built with custom targeting strategies and that
     * the representation invariant is maintained.
     */
    @Test
    @DisplayName("Test 4: Tower building with custom targeting strategies")
    void testTowerBuildingWithCustomTargetingStrategies() {
        // Setup: Create custom targeting strategy
        TargetingStrategy customStrategy = mock(TargetingStrategy.class);

        // Act: Build towers with custom targeting strategies
        towerManager.buildArcherTower(0, 0, customStrategy);
        assertTrue(repOk(towerManager), "Building archer with custom strategy should maintain invariant");

        towerManager.buildMageTower(64, 0, customStrategy);
        assertTrue(repOk(towerManager), "Building mage with custom strategy should maintain invariant");

        towerManager.buildArtilleryTower(128, 0, customStrategy);
        assertTrue(repOk(towerManager), "Building artillery with custom strategy should maintain invariant");

        // Assert: Verify towers have custom targeting strategies
        ArrayList<Tower> towers = towerManager.getTowers();
        assertEquals(3, towers.size(), "Should have 3 towers");

        for (Tower tower : towers) {
            assertEquals(customStrategy, tower.getTargetingStrategy(),
                    "Tower should have the custom targeting strategy");
        }
    }

    /**
     * Test Case 5: Test edge cases and error conditions.
     *
     * Verifies that the TowerManager handles edge cases gracefully while
     * maintaining the representation invariant.
     */
    @Test
    @DisplayName("Test 5: Edge cases and error conditions")
    void testEdgeCasesAndErrorConditions() {
        // Test adding null tower (should be handled gracefully)
        assertDoesNotThrow(() -> {
            // Note: The actual implementation might allow null towers,
            // but repOk should catch this as an invariant violation
            towerManager.addTower(null);
        }, "Adding null tower should not throw exception");

        // If null was added, repOk should fail and we need to remove it for further testing
        if (towerManager.getTowers().contains(null)) {
            assertFalse(repOk(towerManager), "RepOk should fail if null tower is in collection");
            // Clear the collection to continue testing with valid state
            towerManager.clearTowers();
        }

        // Ensure we have a valid state for the next test
        assertTrue(repOk(towerManager), "Should have valid state after clearing");

        // Test replacing non-existent tower
        ArcherTower nonExistentTower = new ArcherTower(999, 999);
        ArcherTower replacementTower = new ArcherTower(1000, 1000);

        assertDoesNotThrow(() -> {
            towerManager.replaceTower(nonExistentTower, replacementTower);
        }, "Replacing non-existent tower should not throw exception");

        assertTrue(repOk(towerManager), "Representation invariant should hold after failed replacement");

        // Test clearing empty collection
        towerManager.clearTowers();
        assertDoesNotThrow(() -> {
            towerManager.clearTowers();
        }, "Clearing already empty collection should not throw exception");

        assertTrue(repOk(towerManager), "Representation invariant should hold after clearing empty collection");
        assertTrue(towerManager.getTowers().isEmpty(), "Collection should remain empty");
    }

    /**
     * Test Case 6: Test update operations and state consistency.
     *
     * Verifies that update operations maintain the representation invariant
     * and that the system remains in a consistent state.
     */
    @Test
    @DisplayName("Test 6: Update operations maintain state consistency")
    void testUpdateOperationsMaintainStateConsistency() {
        // Setup: Build some towers
        towerManager.buildArcherTower(64, 64);
        towerManager.buildMageTower(128, 128);

        assertTrue(repOk(towerManager), "Initial state should maintain invariant");

        // Test basic update
        assertDoesNotThrow(() -> {
            towerManager.update();
        }, "Basic update should not throw exception");

        assertTrue(repOk(towerManager), "State should remain valid after update");

        // Test update with speed multiplier
        assertDoesNotThrow(() -> {
            towerManager.update(2.0f);
        }, "Update with speed multiplier should not throw exception");

        assertTrue(repOk(towerManager), "State should remain valid after speed multiplier update");

        // Test drawing (visual operations should not affect logical state)
        assertDoesNotThrow(() -> {
            towerManager.draw(mockGraphics);
        }, "Drawing should not throw exception");

        assertTrue(repOk(towerManager), "State should remain valid after drawing");

        // Verify towers are still in collection and unchanged
        assertEquals(2, towerManager.getTowers().size(), "Should still have 2 towers after updates");
    }
} 
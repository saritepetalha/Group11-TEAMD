package managers;

import config.GameOptions;
import config.Group;
import config.Wave;
import helpMethods.OptionsIO;
import scenes.Playing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static constants.Constants.Enemies.*;

public class WaveManager {

    private Playing playing;
    private ArrayList<events.Wave> waves = new ArrayList<>();
    private int enemySpawnTickLimit = 60 * 1; // 1 second
    private int enemySpawnTick = enemySpawnTickLimit;
    private int enemyIndex;
    private int waveIndex;
    private boolean waveStartTimer;
    private int waveTickLimit = 60 * 5; // 5 seconds default
    private int waveTick = 0;
    private boolean waveTickTimerOver;
    private GameOptions gameOptions;

    public WaveManager(Playing playing) {
        this.playing = playing;
        this.gameOptions = OptionsIO.load();
        createWaves();

        // Set inter-wave delay from options
        setInterWaveDelay(gameOptions.getInterWaveDelay());
    }

    public void update(){
        if (enemySpawnTick <= enemySpawnTickLimit) {
            enemySpawnTick++;
        }
        if (waveStartTimer) {
            waveTick++;
            if (waveTick >= waveTickLimit) {
                waveTickTimerOver = true;
                waveTick = 0;
            }
        }
    }

    public void incrementWaveIndex() {
        waveIndex++;
        waveTickTimerOver = false;
        waveStartTimer = false;
        // Reset enemy spawn tick to ensure immediate spawning of the next wave
        enemySpawnTick = enemySpawnTickLimit;
    }

    public int getNextEnemy(){
        enemySpawnTick = 0;
        return waves.get(waveIndex).getEnemyList().get(enemyIndex++);
    }

    private void createWaves(){
        // Check if we should use the waves from options
        if (gameOptions != null && !gameOptions.getWaves().isEmpty()) {
            convertConfigWavesToEventWaves();
            return;
        }

        // Fallback to hardcoded waves if options are not available
        // Using the constant values from Constants.Enemies for clarity:
        // GOBLIN = 0, WARRIOR = 1, BARREL = 2, TNT = 4, TROLL = 3

        // Wave 1: Introduction to goblins
        waves.add(new events.Wave(new ArrayList<>(Arrays.asList(GOBLIN,GOBLIN, GOBLIN, GOBLIN, GOBLIN))));

        //Wave 2: Mix of goblins and TNT
        waves.add(new events.Wave(new ArrayList<>(Arrays.asList(GOBLIN, GOBLIN, TNT, GOBLIN, TNT, GOBLIN))));

        // Wave 3: Introduce barrel
        waves.add(new events.Wave(new ArrayList<>(Arrays.asList(GOBLIN, BARREL, GOBLIN, TNT, BARREL, GOBLIN))));

        // Wave 4: Introduce warriors
        waves.add(new events.Wave(new ArrayList<>(Arrays.asList(WARRIOR, GOBLIN, BARREL, WARRIOR, TNT, GOBLIN))));

        // Wave 5: Harder mix of existing enemies
        waves.add(new events.Wave(new ArrayList<>(Arrays.asList(WARRIOR, WARRIOR, BARREL, TNT, TNT, GOBLIN, GOBLIN, GOBLIN))));

        // Wave 6: Introduce troll
        waves.add(new events.Wave(new ArrayList<>(Arrays.asList(TROLL, GOBLIN, GOBLIN, TNT, BARREL))));

        // Wave 7: Final boss wave with multiple trolls
        waves.add(new events.Wave(new ArrayList<>(Arrays.asList(WARRIOR, TROLL, BARREL, TNT, TROLL, GOBLIN, GOBLIN))));
    }

    /**
     * Converts the Wave and Group objects from the config package to events.Wave objects
     * that can be used by the game engine
     */
    private void convertConfigWavesToEventWaves() {
        waves.clear();

        try {
            for (config.Wave configWave : gameOptions.getWaves()) {
                ArrayList<Integer> enemyList = new ArrayList<>();

                // Process each group in the wave
                for (Group group : configWave.getGroups()) {
                    // Skip null groups
                    if (group == null) {
                        System.out.println("Warning: Null group found in wave");
                        continue;
                    }

                    // Skip if composition is null
                    if (group.getComposition() == null) {
                        System.out.println("Warning: Null composition found in group");
                        continue;
                    }

                    // For each enemy type in the group
                    for (Map.Entry<config.EnemyType, Integer> entry : group.getComposition().entrySet()) {
                        config.EnemyType enemyType = entry.getKey();

                        // Skip null enemy types
                        if (enemyType == null) {
                            System.out.println("Warning: Null enemy type found in group");
                            continue;
                        }

                        int count = entry.getValue();

                        // Add the appropriate number of enemies
                        for (int i = 0; i < count; i++) {
                            // Convert from config.EnemyType to Constants.Enemies integer constants
                            Integer enemyConstant = convertEnemyTypeToConstant(enemyType);

                            // Only add valid enemy constants
                            if (enemyConstant != null) {
                                enemyList.add(enemyConstant);
                            }
                        }
                    }
                }

                // Only create a wave if we have enemies
                if (!enemyList.isEmpty()) {
                    // Create a new event wave with the enemy list
                    waves.add(new events.Wave(enemyList));
                } else {
                    System.out.println("Warning: Empty enemy list for wave, skipping");
                }
            }

            // If no waves were created, fall back to default waves
            if (waves.isEmpty()) {
                System.out.println("No valid waves created from options, using default waves");
                createDefaultWaves();
            }
        } catch (Exception e) {
            System.out.println("Error converting config waves: " + e.getMessage());
            e.printStackTrace();

            // Fall back to default waves on error
            waves.clear();
            createDefaultWaves();
        }
    }

    /**
     * Converts an EnemyType enum to the corresponding Constants.Enemies integer constant
     * @param enemyType The EnemyType enum value
     * @return The corresponding integer constant, or null if not found
     */
    private Integer convertEnemyTypeToConstant(config.EnemyType enemyType) {
        if (enemyType == null) return null;

        switch (enemyType) {
            case GOBLIN:
                return GOBLIN;
            case WARRIOR:
                return WARRIOR;
            case BARREL:
                return BARREL;
            case TNT:
                return TNT;
            case TROLL:
                return TROLL;
            default:
                System.out.println("Warning: Unknown enemy type: " + enemyType);
                return null;
        }
    }

    /**
     * Creates the default waves as a fallback
     */
    private void createDefaultWaves() {
        // Wave 1: Introduction to goblins
        waves.add(new events.Wave(new ArrayList<>(Arrays.asList(GOBLIN,GOBLIN, GOBLIN, GOBLIN, GOBLIN))));

        //Wave 2: Mix of goblins and TNT
        waves.add(new events.Wave(new ArrayList<>(Arrays.asList(GOBLIN, GOBLIN, TNT, GOBLIN, TNT, GOBLIN))));

        // Wave 3: Introduce barrel
        waves.add(new events.Wave(new ArrayList<>(Arrays.asList(GOBLIN, BARREL, GOBLIN, TNT, BARREL, GOBLIN))));

        // Wave 4: Introduce warriors
        waves.add(new events.Wave(new ArrayList<>(Arrays.asList(WARRIOR, GOBLIN, BARREL, WARRIOR, TNT, GOBLIN))));

        // Wave 5: Harder mix of existing enemies
        waves.add(new events.Wave(new ArrayList<>(Arrays.asList(WARRIOR, WARRIOR, BARREL, TNT, TNT, GOBLIN, GOBLIN, GOBLIN))));

        // Wave 6: Introduce troll
        waves.add(new events.Wave(new ArrayList<>(Arrays.asList(TROLL, GOBLIN, GOBLIN, TNT, BARREL))));

        // Wave 7: Final boss wave with multiple trolls
        waves.add(new events.Wave(new ArrayList<>(Arrays.asList(WARRIOR, TROLL, BARREL, TNT, TROLL, GOBLIN, GOBLIN))));
    }

    /**
     * Sets the inter-wave delay in seconds from the options
     * @param seconds The delay in seconds between waves
     */
    public void setInterWaveDelay(double seconds) {
        // Convert seconds to game ticks (60 ticks per second)
        this.waveTickLimit = (int)(seconds * 60);
    }

    public ArrayList<events.Wave> getWaves() {
        return waves;
    }

    public boolean isTimeForNewEnemy() {
        return enemySpawnTick >= enemySpawnTickLimit;
    }

    public boolean isWaveFinished() {
        if (waves.isEmpty() || waveIndex >= waves.size()) {
            return true;
        }
        return enemyIndex >= waves.get(waveIndex).getEnemyList().size();
    }

    public boolean isThereMoreWaves() {
        return (waveIndex + 1) < waves.size();
    }

    public void startTimer() {
        waveStartTimer = true;
        waveTick = 0;
        waveTickTimerOver = false;
        enemySpawnTick = enemySpawnTickLimit;
    }

    public boolean isWaveTimerOver() {
        return waveTickTimerOver;
    }

    public void resetEnemyIndex() {
        enemyIndex = 0;
    }

    public void resetWaveIndex() {
        waveIndex = 0;
        waveTickTimerOver = false;
        waveStartTimer = false;
    }

    public int getWaveIndex() {
        return waveIndex;
    }

    public float getTimeLeft() {
        return (waveTickLimit - waveTick) / 60f;
    }

    public boolean isWaveTimerStarted() {
        return waveStartTimer;
    }

    public String getWaveTickLimit() {
        return String.valueOf(waveTickLimit / 60);
    }

    public String getWaveTick() {
        return String.valueOf(waveTick / 60);
    }

    /**
     * Reloads the waves from the options file
     */
    public void reloadFromOptions() {
        this.gameOptions = OptionsIO.load();
        createWaves();
        setInterWaveDelay(gameOptions.getInterWaveDelay());
    }
}

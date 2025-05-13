package managers;

import config.GameOptions;
import config.Group;
import config.Wave;
import config.EnemyType;
import helpMethods.OptionsIO;
import scenes.Playing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.Queue;

import static constants.Constants.Enemies.*;

public class WaveManager {

    private Playing playing;
    private List<config.Wave> waves = new ArrayList<>();
    private GameOptions gameOptions;

    private int interWaveTickLimit = 60 * 10; // Default 10 seconds between waves (configurable)
    private int interWaveTick = 0;
    private int groupDelayTickLimit = 0; // Will be set from config.Wave
    private int groupDelayTick = 0;
    private int enemyDelayTickLimit = 0; // Will be set from config.Group
    private int enemyDelayTick = 0;

    private int waveIndex = 0;
    private int groupIndex = 0;
    private Queue<Integer> currentGroupEnemyQueue = new LinkedList<>(); // Enemies left in the current group
    private boolean waitingForNextWave = true; // Start by waiting for the first wave
    private boolean waitingForNextGroup = false;
    private boolean waitingForNextEnemy = false;
    private boolean waveTimerActive = false; // Tracks if the inter-wave timer is running

    public WaveManager(Playing playing, GameOptions options) {
        this.playing = playing;
        this.gameOptions = options;
        try {
            if (this.gameOptions == null) {
                System.out.println("Warning: Received null GameOptions, using defaults for waves.");
                this.gameOptions = GameOptions.defaults();
            }
        } catch (Exception e) {
            System.out.println("Error initializing WaveManager with options: " + e.getMessage() + ". Using defaults.");
            this.gameOptions = GameOptions.defaults();
        }

        loadWavesFromOptions();

        setInterWaveDelay(gameOptions.getInterWaveDelay());

        prepareNextWave();
    }

    private void loadWavesFromOptions() {
        waves.clear();
        if (gameOptions != null && gameOptions.getWaves() != null && !gameOptions.getWaves().isEmpty()) {
            System.out.println("Loading " + gameOptions.getWaves().size() + " waves from config.");
            for (config.Wave w : gameOptions.getWaves()) {
                if (w != null && w.getGroups() != null && !w.getGroups().isEmpty()) {
                    boolean waveIsValid = true;
                    for(Group g : w.getGroups()) {
                        if(g == null || g.getComposition() == null || g.getComposition().isEmpty()) {
                            System.out.println("Warning: Wave contains invalid group. Skipping wave.");
                            waveIsValid = false;
                            break;
                        }
                        for(Map.Entry<EnemyType, Integer> entry : g.getComposition().entrySet()) {
                            if(entry.getKey() == null || entry.getValue() <= 0) {
                                System.out.println("Warning: Group contains invalid enemy type or count. Skipping wave.");
                                waveIsValid = false;
                                break;
                            }
                        }
                        if (!waveIsValid) break;
                    }
                    if (waveIsValid) {
                        waves.add(w);
                    }
                } else {
                    System.out.println("Warning: Found null or empty wave in config, skipping.");
                }
            }
            System.out.println("Successfully loaded " + waves.size() + " valid waves from config.");
        }

        if (waves.isEmpty()) {
            System.out.println("No valid waves found in config or config not loaded. Creating default waves.");
            createDefaultWaves();
        }
    }

    private void createDefaultWaves() {
        waves.clear();

        Group g1_1 = new Group(Map.of(EnemyType.GOBLIN, 5), 0.5);
        waves.add(new Wave(List.of(g1_1), 0.0));

        Group g2_1 = new Group(Map.of(EnemyType.GOBLIN, 5), 0.5);
        Group g2_2 = new Group(Map.of(EnemyType.TNT, 2), 1.0);
        waves.add(new Wave(List.of(g2_1, g2_2), 2.0));

        Group g3_1 = new Group(Map.of(EnemyType.GOBLIN, 3), 0.5);
        Group g3_2 = new Group(Map.of(EnemyType.BARREL, 2), 1.0);
        Group g3_3 = new Group(Map.of(EnemyType.GOBLIN, 3), 0.5);
        waves.add(new Wave(List.of(g3_1, g3_2, g3_3), 1.5));

        Group g4_1 = new Group(Map.of(EnemyType.WARRIOR, 2), 1.2);
        Group g4_2 = new Group(Map.of(EnemyType.GOBLIN, 4), 0.4);
        Group g4_3 = new Group(Map.of(EnemyType.TNT, 1), 0.0);
        Group g4_4 = new Group(Map.of(EnemyType.BARREL, 2), 1.0);
        waves.add(new Wave(List.of(g4_1, g4_2, g4_3, g4_4), 1.0));

        Group g5_1 = new Group(Map.of(EnemyType.WARRIOR, 3), 1.0);
        Group g5_2 = new Group(Map.of(EnemyType.TNT, 2), 0.8);
        Group g5_3 = new Group(Map.of(EnemyType.GOBLIN, 5), 0.3);
        Group g5_4 = new Group(Map.of(EnemyType.TROLL, 1), 0.0);
        waves.add(new Wave(List.of(g5_1, g5_2, g5_3, g5_4), 2.0));

        System.out.println("Created " + waves.size() + " default waves.");
    }

    private void prepareNextWave() {
        if (waveIndex < waves.size()) {
            groupIndex = 0;
            prepareNextGroup();
            waitingForNextWave = false;
            System.out.println("Preparing Wave: " + (waveIndex + 1));
        } else {
            System.out.println("All waves completed.");
        }
    }

    private void prepareNextGroup() {
        if (waveIndex < waves.size()) {
            Wave currentWave = waves.get(waveIndex);
            if (groupIndex < currentWave.getGroups().size()) {
                Group currentGroup = currentWave.getGroups().get(groupIndex);

                currentGroupEnemyQueue.clear();
                for (Map.Entry<EnemyType, Integer> entry : currentGroup.getComposition().entrySet()) {
                    EnemyType type = entry.getKey();
                    int count = entry.getValue();
                    Integer enemyConstant = convertEnemyTypeToConstant(type);
                    if (enemyConstant != null) {
                        for (int i = 0; i < count; i++) {
                            currentGroupEnemyQueue.add(enemyConstant);
                        }
                    }
                }

                enemyDelayTickLimit = (int) (currentGroup.getIntraEnemyDelay() * 60);
                enemyDelayTick = 0;

                waitingForNextGroup = false;
                waitingForNextEnemy = true;
                System.out.println("Preparing Group: " + (groupIndex + 1) + " in Wave " + (waveIndex + 1) + " with " + currentGroupEnemyQueue.size() + " enemies. Enemy delay: " + enemyDelayTickLimit + " ticks.");

            } else {
                System.out.println("Wave " + (waveIndex+1) + " finished.");
                startInterWaveTimer();
            }
        }
    }

    private void startInterWaveTimer() {
        waitingForNextWave = true;
        waveTimerActive = true;
        interWaveTick = 0;
        System.out.println("Starting inter-wave timer for " + interWaveTickLimit + " ticks.");
    }

    public void update(){
        if (waitingForNextWave) {
            if (waveTimerActive) {
                interWaveTick++;
                if (interWaveTick >= interWaveTickLimit) {
                    waveTimerActive = false;
                    interWaveTick = 0;
                    waveIndex++;
                    if (waveIndex < waves.size()) {
                        prepareNextWave();
                    } else {
                        System.out.println("All waves completed - update loop.");
                        waitingForNextWave = true;
                    }
                }
            }
        } else if (waitingForNextGroup) {
            groupDelayTick++;
            if (groupDelayTick >= groupDelayTickLimit) {
                groupDelayTick = 0;
                groupIndex++;
                prepareNextGroup();
            }
        } else if (waitingForNextEnemy) {
            enemyDelayTick++;
            if(enemyDelayTick >= enemyDelayTickLimit) {
                enemyDelayTick = 0;
                waitingForNextEnemy = false;
            }
        }

        if (!waitingForNextWave && !waitingForNextGroup && !waitingForNextEnemy && !currentGroupEnemyQueue.isEmpty()) {
            Integer enemyTypeObject = currentGroupEnemyQueue.poll();
            if (enemyTypeObject != null) {
                int enemyType = enemyTypeObject.intValue();
                playing.spawnEnemy(enemyType);
                System.out.println("Spawned enemy type: " + enemyType + ". Enemies left in group: " + currentGroupEnemyQueue.size());

                if (!currentGroupEnemyQueue.isEmpty()) {
                    waitingForNextEnemy = true;
                    enemyDelayTick = 0;
                } else {
                    Wave currentWave = waves.get(waveIndex);
                    if (groupIndex + 1 < currentWave.getGroups().size()) {
                        waitingForNextGroup = true;
                        groupDelayTickLimit = (int) (currentWave.getIntraGroupDelay() * 60);
                        groupDelayTick = 0;
                        System.out.println("Group finished. Starting intra-group timer for " + groupDelayTickLimit + " ticks.");
                    } else {
                        System.out.println("Wave " + (waveIndex+1) + " finished processing enemies.");
                        startInterWaveTimer();
                    }
                }
            }
        }
    }

    public boolean isWaveFinished() {
        return waitingForNextWave && waveIndex >= waves.size();
    }

    public boolean isAllWavesFinished() {
        return waveIndex >= waves.size() && waitingForNextWave;
    }

    public boolean isThereMoreWaves() {
        return waveIndex < waves.size();
    }

    public boolean isWaveTimerOver() {
        return waitingForNextWave && !waveTimerActive;
    }

    public void resetWaveManager() {
        loadWavesFromOptions();
        resetWaveManagerEssentials();
        System.out.println("WaveManager fully reset.");
    }

    public int getWaveIndex() {
        return waveIndex;
    }

    public float getTimeUntilNextSpawn() {
        if (waitingForNextWave && waveTimerActive) {
            return (interWaveTickLimit - interWaveTick) / 60f;
        } else if (waitingForNextGroup) {
            return (groupDelayTickLimit - groupDelayTick) / 60f;
        } else if (waitingForNextEnemy) {
            return (enemyDelayTickLimit - enemyDelayTick) / 60f;
        }
        return 0f;
    }

    public String getCurrentStateInfo() {
        if (waitingForNextWave) {
            return waveTimerActive ? "Next Wave In: " + String.format("%.1f", getTimeUntilNextSpawn()) + "s" : "Waiting...";
        } else if (waitingForNextGroup) {
            return "Next Group In: " + String.format("%.1f", getTimeUntilNextSpawn()) + "s";
        } else if (waitingForNextEnemy) {
            return "Next Enemy In: " + String.format("%.1f", getTimeUntilNextSpawn()) + "s";
        } else if (!currentGroupEnemyQueue.isEmpty()){
            return "Spawning...";
        } else if (isAllWavesFinished()){
            return "All Waves Done";
        } else {
            return "Processing...";
        }
    }

    public void setInterWaveDelay(double seconds) {
        this.interWaveTickLimit = Math.max(0, (int)(seconds * 60));
        System.out.println("Inter-wave delay set to: " + seconds + "s (" + this.interWaveTickLimit + " ticks)");
    }

    public void reloadFromOptions() {
        System.out.println("Reloading WaveManager from options...");
        try {
            GameOptions freshOptions = OptionsIO.load();
            if (freshOptions != null) {
                this.gameOptions = freshOptions;
                System.out.println("WaveManager: Successfully reloaded GameOptions.");
            } else {
                System.out.println("Warning: Failed to load GameOptions during WaveManager reload, using current or defaults.");
                if (this.gameOptions == null) this.gameOptions = GameOptions.defaults();
            }
        } catch (Exception e) {
            System.out.println("Error reloading GameOptions in WaveManager: " + e.getMessage() + ". Using current or defaults.");
            if (this.gameOptions == null) this.gameOptions = GameOptions.defaults();
        }
        loadWavesFromOptions();
        int currentWaveIndex = this.waveIndex;
        int currentGroupIndex = this.groupIndex;
        Queue<Integer> currentQueueState = new LinkedList<>(this.currentGroupEnemyQueue);
        boolean currentWaitingWave = this.waitingForNextWave;
        boolean currentWaitingGroup = this.waitingForNextGroup;
        boolean currentWaitingEnemy = this.waitingForNextEnemy;
        boolean currentTimerActive = this.waveTimerActive;
        int currentInterWaveTick = this.interWaveTick;
        int currentGroupDelayTick = this.groupDelayTick;
        int currentEnemyDelayTick = this.enemyDelayTick;

        resetWaveManagerEssentials();

        System.out.println("WaveManager reloaded and progression reset based on new options.");
    }

    private void resetWaveManagerEssentials() {
        waveIndex = 0;
        groupIndex = 0;
        currentGroupEnemyQueue.clear();
        waitingForNextWave = true;
        waitingForNextGroup = false;
        waitingForNextEnemy = false;
        waveTimerActive = false;
        interWaveTick = 0;
        groupDelayTick = 0;
        enemyDelayTick = 0;
        if (this.gameOptions != null) {
            setInterWaveDelay(this.gameOptions.getInterWaveDelay());
        }
        prepareNextWave();
    }

    private Integer convertEnemyTypeToConstant(config.EnemyType enemyType) {
        if (enemyType == null) {
            System.out.println("Warning: Tried to convert null EnemyType.");
            return null;
        }

        switch (enemyType) {
            case GOBLIN: return GOBLIN;
            case WARRIOR: return WARRIOR;
            case BARREL: return BARREL;
            case TNT: return TNT;
            case TROLL: return TROLL;
            default:
                System.out.println("Warning: Unknown enemy type during conversion: " + enemyType);
                return null;
        }
    }
}

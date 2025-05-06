package managers;

import events.Wave;
import scenes.Playing;

import java.util.ArrayList;
import java.util.Arrays;

import static constants.Constants.Enemies.*;

public class WaveManager {

    private Playing playing;
    private ArrayList<Wave> waves = new ArrayList<>();
    private int enemySpawnTickLimit = 60 * 1; // 1 second
    private int enemySpawnTick = enemySpawnTickLimit;
    private int enemyIndex;
    private int waveIndex;
    private boolean waveStartTimer;
    private int waveTickLimit = 60 * 5; // 5 seconds
    private int waveTick = 0;
    private boolean waveTickTimerOver;

    public WaveManager(Playing playing) {
        this.playing = playing;
        createWaves();
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
        // Using the constant values from Constants.Enemies for clarity:
        // GOBLIN = 0, WARRIOR = 1, BARREL = 2, TNT = 3, TROLL = 4

        // Wave 1: Introduction to goblins
        waves.add(new Wave(new ArrayList<>(Arrays.asList(GOBLIN,GOBLIN, GOBLIN, GOBLIN, GOBLIN))));

        //Wave 2: Mix of goblins and TNT
        waves.add(new Wave(new ArrayList<>(Arrays.asList(GOBLIN, GOBLIN, TNT, GOBLIN, TNT, GOBLIN))));

        // Wave 3: Introduce barrel
        waves.add(new Wave(new ArrayList<>(Arrays.asList(GOBLIN, BARREL, GOBLIN, TNT, BARREL, GOBLIN))));

        // Wave 4: Introduce warriors
        waves.add(new Wave(new ArrayList<>(Arrays.asList(WARRIOR, GOBLIN, BARREL, WARRIOR, TNT, GOBLIN))));

        // Wave 5: Harder mix of existing enemies
        waves.add(new Wave(new ArrayList<>(Arrays.asList(WARRIOR, WARRIOR, BARREL, TNT, TNT, GOBLIN, GOBLIN, GOBLIN))));

        // Wave 6: Introduce troll
        waves.add(new Wave(new ArrayList<>(Arrays.asList(TROLL, GOBLIN, GOBLIN, TNT, BARREL))));

        // Wave 7: Final boss wave with multiple trolls
        waves.add(new Wave(new ArrayList<>(Arrays.asList(WARRIOR, TROLL, BARREL, TNT, TROLL, GOBLIN, GOBLIN))));
    }

    public ArrayList<Wave> getWaves() {
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
}

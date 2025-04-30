package managers;

import events.Wave;
import scenes.Playing;

import java.util.ArrayList;
import java.util.Arrays;

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
    }

    public int getNextEnemy(){
        enemySpawnTick = 0;
        return waves.get(waveIndex).getEnemyList().get(enemyIndex++);
    }

    private void createWaves(){
        waves.add(new Wave(new ArrayList<Integer>(Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 1))));
        waves.add(new Wave(new ArrayList<Integer>(Arrays.asList(1, 0, 1, 0, 0, 0, 0, 0, 0, 1))));
    }

    public ArrayList<Wave> getWaves() {
        return waves;
    }

    public boolean isTimeForNewEnemy() {
        return enemySpawnTick >= enemySpawnTickLimit;
    }

    public boolean isWaveFinished() {
        if (waves.isEmpty() || waveIndex >= waves.size()) return true;
        return waves.get(waveIndex).getEnemyList().size() <= enemyIndex;
    }

    public boolean isThereMoreWaves() {
        return (waveIndex + 1) < waves.size();
    }

    public void startTimer() {
        waveStartTimer = true;
    }

    public boolean isWaveTimerOver() {
        return waveTickTimerOver;
    }

    public void resetEnemyIndex() {
        enemyIndex = 0;
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

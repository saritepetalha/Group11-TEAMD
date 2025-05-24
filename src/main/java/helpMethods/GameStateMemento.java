package helpMethods;

import java.util.List;

import config.GameOptions;

public class GameStateMemento {
    private final int gold;
    private final int health;
    private final int shield;
    private final int waveIndex;
    private final int groupIndex;
    private final List<TowerState> towerStates;
    private final List<EnemyState> enemyStates;
    private final GameOptions gameOptions;

    public GameStateMemento(int gold, int health, int shield, int waveIndex, int groupIndex,
                          List<TowerState> towerStates, List<EnemyState> enemyStates, GameOptions gameOptions) {
        this.gold = gold;
        this.health = health;
        this.shield = shield;
        this.waveIndex = waveIndex;
        this.groupIndex = groupIndex;
        this.towerStates = towerStates;
        this.enemyStates = enemyStates;
        this.gameOptions = gameOptions;
    }

    public int getGold() { return gold; }
    public int getHealth() { return health; }
    public int getShield() { return shield; }
    public int getWaveIndex() { return waveIndex; }
    public int getGroupIndex() { return groupIndex; }
    public List<TowerState> getTowerStates() { return towerStates; }
    public List<EnemyState> getEnemyStates() { return enemyStates; }
    public GameOptions getGameOptions() { return gameOptions; }

    public static class TowerState {
        private final int x;
        private final int y;
        private final int type;
        private final int level;

        public TowerState(int x, int y, int type, int level) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.level = level;
        }

        public int getX() { return x; }
        public int getY() { return y; }
        public int getType() { return type; }
        public int getLevel() { return level; }
    }

    public static class EnemyState {
        private final float x;
        private final float y;
        private final int type;
        private final int health;
        private final int pathIndex;

        public EnemyState(float x, float y, int type, int health, int pathIndex) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.health = health;
            this.pathIndex = pathIndex;
        }

        public float getX() { return x; }
        public float getY() { return y; }
        public int getType() { return type; }
        public int getHealth() { return health; }
        public int getPathIndex() { return pathIndex; }
    }
} 
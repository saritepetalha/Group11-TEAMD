package managers;

import java.util.List;
import java.util.Set;
import config.GameOptions;
import skills.SkillType;

public class GameStateMemento {
    private int gold;
    private int health;
    private int shield;
    private int waveIndex;
    private int groupIndex;
    private List<TowerState> towerStates;
    private List<EnemyState> enemyStates;
    private GameOptions gameOptions;
    private String difficulty;
    private Set<SkillType> selectedSkills;
    private Object weatherData;
    private List<TreeState> deadTreeStates;
    private List<TreeState> liveTreeStates;

    /** Gson needs this no-args constructor */
    public GameStateMemento() {
        // leave empty or initialize defaults if you like
    }

    // Legacy constructor for backward compatibility
    public GameStateMemento(int gold,
                            int health,
                            int shield,
                            int waveIndex,
                            int groupIndex,
                            List<TowerState> towerStates,
                            List<EnemyState> enemyStates,
                            GameOptions gameOptions,
                            String difficulty,
                            Set<SkillType> selectedSkills) {
        this(gold, health, shield, waveIndex, groupIndex, towerStates, enemyStates,
                gameOptions, difficulty, selectedSkills, null, null, null);
    }

    public GameStateMemento(int gold,
                            int health,
                            int shield,
                            int waveIndex,
                            int groupIndex,
                            List<TowerState> towerStates,
                            List<EnemyState> enemyStates,
                            GameOptions gameOptions,
                            String difficulty,
                            Set<SkillType> selectedSkills,
                            Object weatherData,
                            List<TreeState> deadTreeStates,
                            List<TreeState> liveTreeStates) {
        this.gold = gold;
        this.health = health;
        this.shield = shield;
        this.waveIndex = waveIndex;
        this.groupIndex = groupIndex;
        this.towerStates = towerStates;
        this.enemyStates = enemyStates;
        this.gameOptions = gameOptions;
        this.selectedSkills = selectedSkills;
        this.weatherData = weatherData;
        this.deadTreeStates = deadTreeStates;
        this.liveTreeStates = liveTreeStates;

        // Ensure difficulty is never null and always valid
        if (difficulty == null) {
            this.difficulty = "Normal";
        } else {
            switch (difficulty) {
                case "Easy":
                case "Normal":
                case "Hard":
                case "Custom":
                    this.difficulty = difficulty;
                    break;
                default:
                    this.difficulty = "Normal";
                    System.out.println("Invalid difficulty '" + difficulty + "' in GameStateMemento, defaulting to Normal");
            }
        }
    }

    public int getGold() {
        return gold;
    }

    public int getHealth() {
        return health;
    }

    public int getShield() {
        return shield;
    }

    public int getWaveIndex() {
        return waveIndex;
    }

    public int getGroupIndex() {
        return groupIndex;
    }

    public List<TowerState> getTowerStates() {
        return towerStates;
    }

    public List<EnemyState> getEnemyStates() {
        return enemyStates;
    }

    public GameOptions getGameOptions() {
        return gameOptions;
    }

    public String getDifficulty() {
        return difficulty != null ? difficulty : "Normal";
    }

    public Set<SkillType> getSelectedSkills() {
        return selectedSkills;
    }

    public void setSelectedSkills(Set<SkillType> selectedSkills) {
        this.selectedSkills = selectedSkills;
    }

    public Object getWeatherData() {
        return weatherData;
    }

    public void setWeatherData(Object weatherData) {
        this.weatherData = weatherData;
    }

    public List<TreeState> getDeadTreeStates() {
        return deadTreeStates;
    }

    public void setDeadTreeStates(List<TreeState> deadTreeStates) {
        this.deadTreeStates = deadTreeStates;
    }

    public List<TreeState> getLiveTreeStates() {
        return liveTreeStates;
    }

    public void setLiveTreeStates(List<TreeState> liveTreeStates) {
        this.liveTreeStates = liveTreeStates;
    }

    public static class TowerState {
        private int x;
        private int y;
        private int type;
        private int level;
        private String targetingStrategy; // Store strategy name for save/load
        private boolean hasLight; // Store whether tower has light upgrade

        /** Gson needs this no-args constructor */
        public TowerState() {
        }

        public TowerState(int x, int y, int type, int level) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.level = level;
            this.targetingStrategy = "First"; // Default targeting strategy
            this.hasLight = false; // Default no light
        }

        public TowerState(int x, int y, int type, int level, String targetingStrategy, boolean hasLight) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.level = level;
            this.targetingStrategy = targetingStrategy != null ? targetingStrategy : "First";
            this.hasLight = hasLight;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getType() {
            return type;
        }

        public int getLevel() {
            return level;
        }

        public String getTargetingStrategy() {
            return targetingStrategy != null ? targetingStrategy : "First";
        }

        public boolean hasLight() {
            return hasLight;
        }
    }

    public static class EnemyState {
        private float x;
        private float y;
        private int type;
        private int health;
        private int pathIndex;

        /** Gson needs this no-args constructor */
        public EnemyState() {
        }

        public EnemyState(float x, float y, int type, int health, int pathIndex) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.health = health;
            this.pathIndex = pathIndex;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public int getType() {
            return type;
        }

        public int getHealth() {
            return health;
        }

        public int getPathIndex() {
            return pathIndex;
        }
    }

    public static class TreeState {
        private int x;
        private int y;

        /** Gson needs this no-args constructor */
        public TreeState() {
        }

        public TreeState(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }
}
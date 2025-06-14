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
                gameOptions, difficulty, selectedSkills, null);
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
                            Object weatherData) {
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

    public static class TowerState {
        private int x;
        private int y;
        private int type;
        private int level;

        /** Gson needs this no-args constructor */
        public TowerState() {
        }

        public TowerState(int x, int y, int type, int level) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.level = level;
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
}
package constants;

public class Constants {
    public static class Projectiles {
        public static final int ARROW = 0;
        public static final int CANNONBALL = 1;
        public static final int MAGICBOLT = 2;

        // speeds (pixels per tick/frame)
        public static float getSpeed(int type) {
            switch (type) {
                case ARROW:
                    return 8f;
                case CANNONBALL:
                    return 4f; // slower, but Area of Effect
                case MAGICBOLT:
                    return 6f;
            }
            return 0f;
        }

        // damage dealt by projectile
        public static int getDamage(int type) {
            switch (type) {
                case ARROW:
                    return 10;
                case CANNONBALL:
                    return 20;
                case MAGICBOLT:
                    return 15;
            }
            return 0;
        }
    }

    public static class Towers {
        public static final int ARCHER = 0;
        public static final int ARTILLERY = 1;
        public static final int MAGE = 2;

        public static int getCost(int towerType) {
            switch (towerType) {
                case ARCHER:
                    return 35;
                case ARTILLERY:
                    return 65;
                case MAGE:
                    return 50;
            }
            return 0;
        }

        public static String getName(int towerType) {
            switch (towerType) {
                case ARCHER:
                    return "Archer Tower";
                case ARTILLERY:
                    return "Artillery Tower";
                case MAGE:
                    return "Mage Tower";
            }
            return "";
        }

        public static int getStartDamage(int towerType) {
            switch (towerType) {
                case ARCHER:
                    return 10;
                case ARTILLERY:
                    return 20;
                case MAGE:
                    return 15;
            }
            return 0;
        }

        public static float getRange(int towerType) {
            switch (towerType) {
                case ARCHER:
                    return 120;
                case ARTILLERY:
                    return 90;
                case MAGE:
                    return 100;
            }
            return 0;
        }

        public static float getCooldown(int towerType) {
            switch (towerType) {
                case ARCHER:
                    return 35;
                case ARTILLERY:
                    return 120;
                case MAGE:
                    return 50;
            }
            return 0;
        }

        public static float getAOERadius(int towerType) {
            if (towerType == ARTILLERY) {
                return 40; // Only Artillery has AOE
            }
            return 0;
        }
    }

    public static class Direction {
        public static final int LEFT = 0;
        public static final int UP = 1;
        public static final int RIGHT = 2;
        public static final int DOWN = 3;
    }

    public static class PathPoints{
        // constants for overlay data
        public static final int NO_OVERLAY = 0;
        public static final int START_POINT = 1;
        public static final int END_POINT = 2;
    }

    public static class Enemies {
        public static final int GOBLIN = 0;
        public static final int WARRIOR = 1;
        public static final int BARREL = 2;
        public static final int TNT = 3;
        public static final int TROLL = 4;

        public static int getReward(int enemyType) {
            switch (enemyType) {
                case GOBLIN:
                    return 5;
                case WARRIOR:
                    return 25;
                case TNT:
                    return 2;
                case BARREL:
                    return 15;
                case TROLL:
                    return 10;

            }
            return 0;
        }

        public static float getSpeed(int enemyType) {
            switch (enemyType) {
                case GOBLIN:
                    return 0.85f;  // faster
                case WARRIOR:
                    return 0.45f;  // slower, tanky
                case BARREL:
                    return 0.6f;
                case TNT:
                    return 1.2f;
                case TROLL:
                    return 0.25f;
            }
            return 0;
        }

        public static int getStartHealth(int enemyType) {
            switch (enemyType) {
                case GOBLIN:
                    return 100;
                case WARRIOR:
                    return 200;
                case BARREL:
                    return 150;
                case TNT:
                    return 50;
                case TROLL:
                    return 300;
            }
            return 0;
        }

        // armor or special resistances to be used for bonus
        public static float getArrowResist(int enemyType) {
            switch (enemyType) {
                case GOBLIN:
                    return 0.7f; // takes more arrow damage (weak)
                case WARRIOR:
                    return 0.3f; // takes less arrow damage (armor)
            }
            return 1.0f;
        }

        public static float getSpellResist(int enemyType) {
            switch (enemyType) {
                case GOBLIN:
                    return 0.5f; // stronger against spells
                case WARRIOR:
                    return 1.0f; // weaker against spells
            }
            return 1.0f;
        }
    }

    public static class Tiles {
        public static final int GRASS_TILE = 1;
        public static final int EMPTY_LOT = 2; // where tower can be built
        public static final int PREBUILT_TOWER = 3;
        public static final int DECORATIVE = 4;

        public static boolean isRoadTile(int tile) {
            return tile >= 0 && tile <= 14 && tile != 5;
        }
    }

    public static class Player{
        public static final int MAX_HEALTH = 10;
        public static final int MAX_SHIELD = 25;
    }
}

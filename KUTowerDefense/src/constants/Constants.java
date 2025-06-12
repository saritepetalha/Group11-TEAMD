package constants;

public class Constants {
    public static class Projectiles {
        public static final int ARROW = 0;
        public static final int CANNONBALL = 1;
        public static final int MAGICBOLT = 2;
        public static final int WIZARD_BOLT = 3;

        // Tracking behavior settings
        public static final float TRACKING_HIT_DISTANCE = 8.0f; // Extra distance for tracking hit detection
        public static final float DIRECT_HIT_DISTANCE = 5.0f; // Distance for guaranteed hit when tracking

        // speeds (pixels per tick/frame)
        public static float getSpeed(int type) {
            switch (type) {
                case ARROW:
                    return 8f;
                case CANNONBALL:
                    return 4f; // slower, but Area of Effect
                case MAGICBOLT:
                    return 6f;
                case WIZARD_BOLT:
                    return 7f; // Slightly faster than regular magic bolt
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
                case WIZARD_BOLT:
                    return 15; // Same damage as magic bolt
            }
            return 0;
        }

        // Check if projectile type should use tracking
        public static boolean shouldTrack(int type) {
            switch (type) {
                case ARROW:
                    return true; // Arrows track for better accuracy
                case CANNONBALL:
                    return true; // Cannonballs track but slower
                case MAGICBOLT:
                    return true; // Magic bolts track
                case WIZARD_BOLT:
                    return true; // Wizard bolts track
                default:
                    return true; // Default to tracking for better gameplay
            }
        }
    }

    public static final int BURN_TREE_COST = 50;

    public static class Towers {
        public static final int ARCHER = 0;
        public static final int ARTILLERY = 1;
        public static final int MAGE = 2;
        public static final int POISON = 3;

        public static int getCost(int towerType) {
            switch (towerType) {
                case ARCHER:
                    return 35;
                case ARTILLERY:
                    return 65;
                case MAGE:
                    return 50;
                case POISON:
                    return 80;
            }
            return 0;
        }

        public static int getUpgradeCost(int towerType) {
            switch (towerType) {
                case ARCHER:
                    return 75;
                case ARTILLERY:
                    return 120;
                case MAGE:
                    return 100;
                case POISON:
                    return 0; // No upgrade available yet
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
                case POISON:
                    return "Poison Tower";
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
                case POISON:
                    return 2; // Lower base damage since it applies poison over time
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
                case POISON:
                    return 110; // Medium range
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
                case POISON:
                    return 80; // Slower attack rate since it has poison effects
            }
            return 0;
        }

        public static float getAOERadius(int towerType) {
            if (towerType == ARTILLERY) {
                return 40; // Only Artillery has AOE
            }
            return 0;
        }

        // Special ability costs for towers
        public static int getSpecialAbilityCost(int towerType) {
            switch (towerType) {
                case POISON:
                    return 150; // Cost for global poison ability
                default:
                    return 0;
            }
        }

        // Special ability cooldowns (in milliseconds)
        public static long getSpecialAbilityCooldown(int towerType) {
            switch (towerType) {
                case POISON:
                    return 45000; // 45 seconds cooldown
                default:
                    return 0;
            }
        }
    }

    public static class PathPoints{
        public static final int NO_OVERLAY = 0;
        public static final int START_POINT = 1;
        public static final int END_POINT = 2;
    }

    public static class Enemies {
        public static final int GOBLIN = 0;
        public static final int KNIGHT = 1;
        public static final int BARREL = 2;
        public static final int TNT = 4;
        public static final int TROLL = 3;

        public static int getReward(int enemyType) {
            switch (enemyType) {
                case GOBLIN:
                    return 5;
                case KNIGHT:
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
                    return 0.85f;
                case KNIGHT:
                    return 0.55f;
                case BARREL:
                    return 0.6f;
                case TNT:
                    return 1.2f;
                case TROLL:
                    return 0.85f;
            }
            return 0;
        }

        public static int getStartHealth(int enemyType) {
            switch (enemyType) {
                case GOBLIN:
                    return 100;
                case KNIGHT:
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
    }

    public static class Player{
        public static final int MAX_HEALTH = 10;
        public static final int MAX_SHIELD = 25;
    }
}

package constants;

public class TowerConstants {

    public static class Towers{
        public static final int ARCHER = 0;
        public static final int BOMB = 1;
        public static final int SPELL = 2;

    }
    public static class Enemies{

        public static final int ORC = 0;
        public static final int WARRIOR = 1;

        public static float getSpeed(int enemy){

            switch (enemy){
                case ORC:
                    return 0.4f;
                case WARRIOR:
                    return 0.5f;
            }
            return 0f;
        }
    }

}

package enemies;

import constants.Constants;

public class Troll extends Enemy {
    public Troll(float x, float y, int id) {
        super(x, y, id, Constants.Enemies.TROLL, Constants.Enemies.getSpeed(Constants.Enemies.TROLL), Size.LARGE, 10);
        // Trolls now move at a fixed speed of 0.85f
        setAnimationSpeed(25);
    }

    @Override
    public float getEffectiveSpeed() {
        return 0.85f; // Force troll speed to 0.85f
    }
}
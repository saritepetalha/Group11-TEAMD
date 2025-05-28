package enemies;

import constants.Constants;

public class Troll extends Enemy {
    public Troll(float x, float y, int id) {
        super(x, y, id, Constants.Enemies.TROLL, Constants.Enemies.getSpeed(Constants.Enemies.TROLL), Size.LARGE, 10);
        // trolls are large and slow, but need slower animation to match their slow movement
        setAnimationSpeed(25);
    }

    @Override
    public float getEffectiveSpeed() {
        return 1.0f; // Force troll speed to 1.0f
    }
}
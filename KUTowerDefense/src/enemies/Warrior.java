package enemies;

import constants.Constants;

public class Warrior extends Enemy{
    public Warrior(float x, float y, int id){
        super(x, y, id, Constants.Enemies.WARRIOR, Constants.Enemies.getSpeed(Constants.Enemies.WARRIOR), Size.MEDIUM, 6);
    }
}

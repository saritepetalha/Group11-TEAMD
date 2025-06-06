package enemies;

import constants.Constants;

public class Knight extends Enemy{
    public Knight(float x, float y, int id){
        super(x, y, id, Constants.Enemies.KNIGHT, Constants.Enemies.getSpeed(Constants.Enemies.KNIGHT), Size.MEDIUM, 6);

        setAnimationSpeed(25);
    }
}
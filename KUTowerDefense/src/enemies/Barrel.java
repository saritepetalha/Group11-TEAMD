package enemies;

import constants.Constants;

public class Barrel extends Enemy{
    public Barrel(float x, float y, int id){
        super(x,y,id, Constants.Enemies.BARREL, Constants.Enemies.getSpeed(Constants.Enemies.BARREL), Size.MEDIUM, 3);
        this.goldReward = 5;
    }
}

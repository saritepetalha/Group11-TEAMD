package enemies;

import constants.Constants;

public class TNT extends Enemy{
    public TNT(float x, float y, int id){
        super(x,y,id,Constants.Enemies.TNT, Constants.Enemies.getSpeed(Constants.Enemies.TNT));
    }
}

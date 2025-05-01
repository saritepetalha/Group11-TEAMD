package enemies;
import constants.Constants;

public class Goblin extends Enemy{
    public Goblin(float x, float y, int id){
        super(x,y,id, Constants.Enemies.GOBLIN, Constants.Enemies.getSpeed(Constants.Enemies.GOBLIN));
    }
}

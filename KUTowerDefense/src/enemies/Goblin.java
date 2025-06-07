package enemies;
import constants.Constants;
import java.awt.image.BufferedImage;
import managers.EnemyManager;

public class Goblin extends Enemy{
    private static EnemyManager enemyManager;

    public static void setEnemyManager(EnemyManager manager) {
        enemyManager = manager;
    }

    public Goblin(float x, float y, int id){
        super(x, y, id, Constants.Enemies.GOBLIN, Constants.Enemies.getSpeed(Constants.Enemies.GOBLIN), Size.SMALL, 6);
    }

    @Override
    protected BufferedImage getSpriteFrame(int animationIndex) {
        return EnemyManager.getEnemyFrame(Constants.Enemies.GOBLIN, animationIndex);
    }
}

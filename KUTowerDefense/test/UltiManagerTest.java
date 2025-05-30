
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import managers.UltiManager;
import scenes.Playing;
import enemies.Enemy;
import managers.EnemyManager;
import managers.PlayerManager;
import managers.WeatherManager;

import java.util.ArrayList;
import java.util.List;

public class UltiManagerTest {

    private UltiManager ultiManager;
    private Playing playing;
    private PlayerManager playerManager;
    private EnemyManager enemyManager;
    private WeatherManager weatherManager;

    @BeforeEach
    public void setUp() {
        playing = Mockito.mock(Playing.class);
        playerManager = Mockito.mock(PlayerManager.class);
        enemyManager = Mockito.mock(EnemyManager.class);
        weatherManager = Mockito.mock(WeatherManager.class);

        Mockito.when(playing.getPlayerManager()).thenReturn(playerManager);
        Mockito.when(playing.getEnemyManager()).thenReturn(enemyManager);
        Mockito.when(playing.getWeatherManager()).thenReturn(weatherManager);
        Mockito.when(playing.getGameTime()).thenReturn(System.currentTimeMillis());

        ultiManager = new UltiManager(playing);
    }

    @Test
    public void testTriggerLightningAt_hitsEnemiesWithinRadius() {
        // Should damage a living enemy within the lightning radius.
        System.out.println("Running test: hitsEnemiesWithinRadius");

        ArrayList<Enemy> enemies = new ArrayList<>();
        Enemy enemy = Mockito.mock(Enemy.class);
        Mockito.when(enemy.getX()).thenReturn(100f);
        Mockito.when(enemy.getY()).thenReturn(100f);
        Mockito.when(enemy.isAlive()).thenReturn(true);
        enemies.add(enemy);

        Mockito.when(enemyManager.getEnemies()).thenReturn(enemies);
        Mockito.when(playerManager.getGold()).thenReturn(100);
        Mockito.when(weatherManager.isRaining()).thenReturn(false);
        Mockito.when(playing.getGameTime()).thenReturn(System.currentTimeMillis());

        ultiManager.triggerLightningAt(100, 100);

        Mockito.verify(enemy).hurt(Mockito.eq(80), Mockito.eq(true));
    }

    @Test
    public void testTriggerLightningAt_notEnoughGold_doesNotTrigger() {
        // Should not trigger lightning if player doesn't have enough gold.
        System.out.println("Running test: notEnoughGold_doesNotTrigger");

        ArrayList<Enemy> enemies = new ArrayList<>();
        Enemy enemy = Mockito.mock(Enemy.class);
        Mockito.when(enemy.getX()).thenReturn(100f);
        Mockito.when(enemy.getY()).thenReturn(100f);
        Mockito.when(enemy.isAlive()).thenReturn(true);
        enemies.add(enemy);

        Mockito.when(enemyManager.getEnemies()).thenReturn(enemies);
        Mockito.when(playerManager.getGold()).thenReturn(10); // Ulti için yeterli değil
        Mockito.when(weatherManager.isRaining()).thenReturn(false);

        ultiManager.triggerLightningAt(100, 100);

        // enemy.hurt çağrılmamalı çünkü ulti aktif olmamalı
        Mockito.verify(enemy, Mockito.never()).hurt(Mockito.anyInt(), Mockito.anyBoolean());
    }

    @Test
    public void testTriggerLightningAt_noEnemiesInRange_doesNothing() {
        // Should not damage enemies that are outside the lightning radius.
        System.out.println("Running test: noEnemiesInRange_doesNothing");

        ArrayList<Enemy> enemies = new ArrayList<>();
        Enemy enemy = Mockito.mock(Enemy.class);
        Mockito.when(enemy.getX()).thenReturn(500f); // Çok uzakta
        Mockito.when(enemy.getY()).thenReturn(500f);
        Mockito.when(enemy.isAlive()).thenReturn(true);
        enemies.add(enemy);

        Mockito.when(enemyManager.getEnemies()).thenReturn(enemies);
        Mockito.when(playerManager.getGold()).thenReturn(100);
        Mockito.when(weatherManager.isRaining()).thenReturn(false);

        ultiManager.triggerLightningAt(100, 100);

        Mockito.verify(enemy, Mockito.never()).hurt(Mockito.anyInt(), Mockito.anyBoolean());
    }

    @Test
    public void testTriggerLightningAt_enemyDead_notHurt() {
        // Should not damage enemies that are already dead.
        System.out.println("Running test: enemyDead_notHurt");

        ArrayList<Enemy> enemies = new ArrayList<>();
        Enemy enemy = Mockito.mock(Enemy.class);
        Mockito.when(enemy.getX()).thenReturn(100f);
        Mockito.when(enemy.getY()).thenReturn(100f);
        Mockito.when(enemy.isAlive()).thenReturn(false); // zaten ölü
        enemies.add(enemy);

        Mockito.when(enemyManager.getEnemies()).thenReturn(enemies);
        Mockito.when(playerManager.getGold()).thenReturn(100);
        Mockito.when(weatherManager.isRaining()).thenReturn(false);

        ultiManager.triggerLightningAt(100, 100);

        Mockito.verify(enemy, Mockito.never()).hurt(Mockito.anyInt(), Mockito.anyBoolean());
    }
}

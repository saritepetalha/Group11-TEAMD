import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import enemies.Enemy;
import enemies.Goblin;
import enemies.Warrior;
import helpMethods.LoadSave;
import objects.Tower;
import objects.ArcherTower;
import strategies.WeakestEnemyStrategy;
import scenes.Playing;

@ExtendWith(MockitoExtension.class)
class TowerManagerTest {
    
    private TowerManager towerManager;
    
    @Mock
    private Playing playing;
    
    @Mock
    private WeatherManager weatherManager;
    
    @Mock
    private EnemyManager enemyManager;
    
    @Mock
    private BufferedImage mockImage;

    @BeforeEach
    void setUp() {
        lenient().when(playing.getWeatherManager()).thenReturn(weatherManager);
        lenient().when(playing.getEnemyManager()).thenReturn(enemyManager);
        lenient().when(weatherManager.isRaining()).thenReturn(false);
        lenient().when(weatherManager.getTowerRangeMultiplier()).thenReturn(1.0f);
        
        try (MockedStatic<LoadSave> mockedLoadSave = mockStatic(LoadSave.class)) {
            mockedLoadSave.when(LoadSave::getSpriteAtlas).thenReturn(mockImage);
            mockedLoadSave.when(() -> LoadSave.getImageFromPath(anyString())).thenReturn(mockImage);
            towerManager = new TowerManager(playing);
        }
    }

    @Test
    void testNormalAttack() {
        Tower tower = spy(new ArcherTower(100, 100));
        Enemy enemy = spy(new Goblin(120f, 120f, 1));
        enemy.setHealth(100);
        
        when(enemy.getSpriteCenterX()).thenReturn(120f);
        when(enemy.getSpriteCenterY()).thenReturn(120f);
        when(enemy.getWidth()).thenReturn(32);
        when(enemy.isAlive()).thenReturn(true);
        
        ArrayList<Enemy> enemies = new ArrayList<>();
        enemies.add(enemy);
        
        towerManager.addTower(tower);
        doReturn(true).when(tower).isCooldownOver();
        
        when(enemyManager.getEnemies()).thenReturn(enemies);
        when(enemyManager.canTargetEnemy(any(Enemy.class))).thenReturn(true);
        
        towerManager.update();
        
        verify(playing).shootEnemy(eq(tower), eq(enemy));
    }

    @Test
    void testAttackInWindyWeather() {
        Tower tower = spy(new ArcherTower(100, 100));
        Enemy enemy = spy(new Goblin(120f, 120f, 1));
        enemy.setHealth(100);
        
        when(enemy.getSpriteCenterX()).thenReturn(120f);
        when(enemy.getSpriteCenterY()).thenReturn(120f);
        when(enemy.getWidth()).thenReturn(32);
        when(enemy.isAlive()).thenReturn(true);
        
        ArrayList<Enemy> enemies = new ArrayList<>();
        enemies.add(enemy);
        
        towerManager.addTower(tower);
        
        when(weatherManager.isWindy()).thenReturn(true);
        when(enemyManager.getEnemies()).thenReturn(enemies);
        when(enemyManager.canTargetEnemy(any(Enemy.class))).thenReturn(true);

        AtomicBoolean shootEnemyCalledThisIteration = new AtomicBoolean(false);
        doAnswer(invocation -> {
            shootEnemyCalledThisIteration.set(true);
            return null;
        }).when(playing).shootEnemy(eq(tower), eq(enemy));
        
        int hits = 0;
        int totalAttempts = 1000;
        
        for (int i = 0; i < totalAttempts; i++) {
            shootEnemyCalledThisIteration.set(false);
            tower.resetCooldown();
            doReturn(true).when(tower).isCooldownOver();
            
            towerManager.update();
            
            if (shootEnemyCalledThisIteration.get()) {
                hits++;
            }
        }
        
        double hitRate = hits / (double) totalAttempts;
        assertTrue(hitRate > 0.65 && hitRate < 0.75, 
            String.format("Hit rate should be approximately 70%% in windy weather, but was %.2f%% (hits: %d, attempts: %d)", hitRate * 100, hits, totalAttempts));
    }

    @Test
    void testTargetingStrategySelection() {
        Tower tower = spy(new ArcherTower(100, 100));
        tower.setTargetingStrategy(new WeakestEnemyStrategy());
        
        Enemy strongEnemy = spy(new Warrior(120f, 120f, 1));
        strongEnemy.setHealth(200);
        when(strongEnemy.getSpriteCenterX()).thenReturn(120f);
        when(strongEnemy.getSpriteCenterY()).thenReturn(120f);
        when(strongEnemy.getWidth()).thenReturn(32);
        when(strongEnemy.isAlive()).thenReturn(true);
        
        Enemy weakEnemy = spy(new Goblin(130f, 130f, 2));
        weakEnemy.setHealth(50);
        when(weakEnemy.getSpriteCenterX()).thenReturn(130f);
        when(weakEnemy.getSpriteCenterY()).thenReturn(130f);
        when(weakEnemy.getWidth()).thenReturn(32);
        when(weakEnemy.isAlive()).thenReturn(true);
        
        ArrayList<Enemy> enemies = new ArrayList<>();
        enemies.add(strongEnemy);
        enemies.add(weakEnemy);
        
        towerManager.addTower(tower);
        doReturn(true).when(tower).isCooldownOver();
        
        when(enemyManager.getEnemies()).thenReturn(enemies);
        when(enemyManager.canTargetEnemy(any(Enemy.class))).thenReturn(true);
        
        towerManager.update();
        
        verify(playing).shootEnemy(eq(tower), eq(weakEnemy));
        verify(playing, never()).shootEnemy(eq(tower), eq(strongEnemy));
    }

    @Test
    void testNoAttackWhenTowerDestroyed() {
        Tower tower = new ArcherTower(100, 100);
        tower.setDestroyed(true);
        
        Enemy enemy = new Goblin(120f, 120f, 1);
        enemy.setHealth(100);
        
        ArrayList<Enemy> enemies = new ArrayList<>();
        enemies.add(enemy);
        
        towerManager.addTower(tower);
        
        towerManager.update();
        
        verify(playing, never()).shootEnemy(any(Tower.class), any(Enemy.class));
        assertEquals(100, enemy.getHealth(), "Enemy health should not change when tower is destroyed");
    }

    @Test
    void testNoAttackWhenTowerOnCooldown() {
        Tower tower = new ArcherTower(100, 100);
        tower.resetCooldown(); 
        
        Enemy enemy = new Goblin(120f, 120f, 1);
        enemy.setHealth(100);
        
        ArrayList<Enemy> enemies = new ArrayList<>();
        enemies.add(enemy);
        
        towerManager.addTower(tower);
        
        towerManager.update();
        
        verify(playing, never()).shootEnemy(any(Tower.class), any(Enemy.class));
        assertEquals(100, enemy.getHealth(), "Enemy health should not change when tower is on cooldown");
    }
} 
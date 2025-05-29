package enemies;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class EnemyTest {

    @Test
    public void testUpdate_SlowEffectDecrementsTimer() {
        Goblin enemy = new Goblin(0, 0, 1);
        enemy.applySlow(0.5f, 10);
        enemy.update();
        assertTrue(enemy.isSlowed(), "Goblin should still be slowed after one update.");
        assertEquals(9, enemy.getSlowTimer(), "Slow timer should decrement by 1.");
    }

    @Test
    public void testUpdate_TeleportEffectTimerDecreases() {
        Goblin enemy = new Goblin(0, 0, 1);
        enemy.applyTeleportEffect();
        long before = enemy.getTeleportEffectTimer();
        enemy.update();
        assertTrue(enemy.getTeleportEffectTimer() < before, "Teleport timer should decrease after update.");
        assertTrue(enemy.isTeleporting(), "Goblin should still be teleporting after one update.");
    }

    @Test
    public void testUpdate_AnimationProgresses() {
        Goblin enemy = new Goblin(0, 0, 1);
        int beforeTick = enemy.getAniTick();
        int beforeIndex = enemy.getAnimationIndex();
        enemy.update();
        boolean animationAdvanced = enemy.getAniTick() > beforeTick || enemy.getAnimationIndex() != beforeIndex;
        assertTrue(animationAdvanced, "Animation tick or frame index should progress.");
    }
}

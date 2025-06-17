package objects;

import constants.Constants;
import constants.GameDimensions;
import enemies.Enemy;
import scenes.Playing;
import skills.SkillTree;
import skills.SkillType;

import java.util.Iterator;

public class PoisonTower extends Tower {
    
    // Special ability fields
    private long lastSpecialAbilityTime = 0;
    private final long specialAbilityCooldown = Constants.Towers.getSpecialAbilityCooldown(Constants.Towers.POISON);
    private final int specialAbilityCost = Constants.Towers.getSpecialAbilityCost(Constants.Towers.POISON);
    
    // Poison application chance (30% chance per attack)
    private final float poisonChance = 0.3f;
    
    // Poison effect parameters - REDUCED VALUES
    private final int poisonDamagePerTick = 1; // Reduced from 3 to 1
    private final int poisonDurationTicks = 180; // Reduced from 300 to 180 (3 seconds at 60 FPS)
    
    // Global poison ability parameters - REDUCED VALUES  
    private final int globalPoisonDamagePerTick = 2; // Reduced from 5 to 2
    private final int globalPoisonDurationTicks = 300; // Reduced from 600 to 300 (5 seconds at 60 FPS)
    
    // Visual effect fields
    private long lastAttackTime = 0;
    private static final long SMOKE_EFFECT_DURATION = 1000; // 1 second smoke effect

    public PoisonTower(int x, int y) {
        super(x, y);
        setDefaultDamage();
        setDefaultRange();
        setDefaultCooldown();
    }

    @Override
    public float getCooldown() {
        return Constants.Towers.getCooldown(Constants.Towers.POISON);
    }

    @Override
    public float getRange() {
        float baseRange = Constants.Towers.getRange(Constants.Towers.POISON);
        if (SkillTree.getInstance().isSkillSelected(SkillType.EAGLE_EYE)) {
            float bonus = GameDimensions.TILE_DISPLAY_SIZE;
            baseRange += bonus;
        }
        return baseRange;
    }

    @Override
    public int getDamage() {
        return Constants.Towers.getStartDamage(Constants.Towers.POISON);
    }

    @Override
    public int getType() {
        return Constants.Towers.POISON;
    }

    @Override
    public Tower upgrade() {
        // No upgrade available yet
        return this;
    }
    
    @Override
    public boolean isUpgradeable() {
        return false; // No upgrade available yet
    }

    /**
     * Apply poison effects to enemies in range - called instead of using projectiles
     * @param enemy The target enemy
     * @param playingScene The playing scene for context
     */
    @Override
    public void applyOnHitEffect(Enemy enemy, Playing playingScene) {
        // Always deal the base damage
        enemy.takeDamage(getConditionBasedDamage(), Enemy.DamageType.MAGICAL);
        
        // Record attack time for visual effects
        lastAttackTime = System.currentTimeMillis();
        
        // 30% chance to apply poison effect
        if (Math.random() < poisonChance) {
            enemy.applyPoison(poisonDamagePerTick, poisonDurationTicks);
            System.out.println("Poison Tower applied poison to enemy " + enemy.getId());
        }
    }
    
    /**
     * Check if the tower should show smoke effect
     * @return true if smoke effect should be visible
     */
    public boolean isShowingSmokeEffect() {
        return System.currentTimeMillis() - lastAttackTime < SMOKE_EFFECT_DURATION;
    }
    
    /**
     * Get the smoke effect progress (0.0 to 1.0)
     * @return progress value for animation
     */
    public float getSmokeEffectProgress() {
        long elapsed = System.currentTimeMillis() - lastAttackTime;
        if (elapsed >= SMOKE_EFFECT_DURATION) return 0.0f;
        return 1.0f - (float)elapsed / SMOKE_EFFECT_DURATION;
    }
    
    /**
     * Check if the special ability can be used
     * @return true if the ability is off cooldown
     */
    public boolean canUseSpecialAbility() {
        return System.currentTimeMillis() - lastSpecialAbilityTime >= specialAbilityCooldown;
    }
    
    /**
     * Get the remaining cooldown time for the special ability
     * @return remaining cooldown in milliseconds
     */
    public long getSpecialAbilityRemainingCooldown() {
        long elapsed = System.currentTimeMillis() - lastSpecialAbilityTime;
        return Math.max(0, specialAbilityCooldown - elapsed);
    }
    
    /**
     * Use the special ability to poison all enemies on the map
     * @param playingScene The playing scene to access enemy manager and player manager
     * @return true if the ability was successfully used
     */
    public boolean useSpecialAbility(Playing playingScene) {
        if (!canUseSpecialAbility()) {
            System.out.println("Poison Tower special ability is on cooldown!");
            return false;
        }
        
        if (playingScene.getPlayerManager().getGold() < specialAbilityCost) {
            System.out.println("Not enough gold for Poison Tower special ability! Cost: " + specialAbilityCost);
            return false;
        }
        
        // Deduct gold cost
        playingScene.getPlayerManager().spendGold(specialAbilityCost);
        
        // Apply poison to all living enemies
        int enemiesPoisoned = 0;
        Iterator<Enemy> it = playingScene.getEnemyManager().getEnemies().iterator();
        while (it.hasNext()) {
            Enemy enemy = it.next();
            if (enemy.isAlive()) {
                enemy.applyPoison(globalPoisonDamagePerTick, globalPoisonDurationTicks);
                enemiesPoisoned++;
            }
        }


        // Update cooldown
        lastSpecialAbilityTime = System.currentTimeMillis();
        
        // Increment usage for condition degradation
        incrementUsage();
        
        System.out.println("Poison Tower used special ability! Poisoned " + enemiesPoisoned + " enemies for " + specialAbilityCost + " gold.");
        
        // Play poison ability sound (if available)
        managers.AudioManager.getInstance().playSpellShotSound(); // Reuse mage sound for now
        
        return true;
    }
    
    /**
     * Get the cost of the special ability
     * @return The gold cost
     */
    public int getSpecialAbilityCost() {
        return specialAbilityCost;
    }

    
    @Override
    protected float getDegradationRate() {
        // Poison towers degrade at a moderate rate due to toxic materials
        return 0.6f;
    }
} 
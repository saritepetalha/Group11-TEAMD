package objects;

import enemies.Enemy;
import scenes.Playing;

import java.awt.image.BufferedImage;

public abstract class TowerDecorator extends Tower {
    public Tower decoratedTower;

    public TowerDecorator(Tower decoratedTower) {
        super(decoratedTower.getX(), decoratedTower.getY()); // Pass coordinates to Tower's constructor
        this.decoratedTower = decoratedTower;
        // The ID should be that of the decorated tower.
        // We need to ensure the Tower base class can have its ID set or copied.
        // For now, Tower's constructor auto-increments ID. This might lead to
        // decorators having different IDs than the tower they wrap if not handled carefully.
        // Let's assume Tower's ID is final once set by its constructor.
        // The decorator will have its own ID by super(x,y) call if Tower's constructor assigns it.
        // To maintain the original tower's ID, the base Tower would need a constructor `Tower(x,y,ID)`
        // or a `setID` method, which is not ideal.
        // A simpler way for decorators:
        // Override getID() to return decoratedTower.getID().
    }

    @Override
    public int getID() {
        return decoratedTower.getID();
    }

    @Override
    public int getType() {
        return decoratedTower.getType();
    }

    @Override
    public float getCooldown() {
        return decoratedTower.getCooldown();
    }

    @Override
    public float getRange() {
        return decoratedTower.getRange();
    }

    @Override
    public int getDamage() {
        return decoratedTower.getDamage();
    }

    @Override
    public void setDefaultCooldown() {
        decoratedTower.setDefaultCooldown();
    }

    @Override
    public void setDefaultRange() {
        decoratedTower.setDefaultRange();
    }

    @Override
    public void setDefaultDamage() {
        decoratedTower.setDefaultDamage();
    }

    @Override
    public boolean isClicked(int mouseX, int mouseY) {
        return decoratedTower.isClicked(mouseX, mouseY);
    }

    // getX and getY are already handled by super(decoratedTower.getX(), decoratedTower.getY())
    // and Tower's own getX(), getY()

    @Override
    public boolean isCooldownOver() {
        return decoratedTower.isCooldownOver();
    }

    @Override
    public void resetCooldown() {
        decoratedTower.resetCooldown();
    }

    @Override
    public void update() {
        decoratedTower.update();
    }

    @Override
    public void update(float gameSpeedMultiplier) {
        decoratedTower.update(gameSpeedMultiplier);
    }

    @Override
    public int getLevel() {
        return decoratedTower.getLevel();
    }

    @Override
    public boolean isUpgradeable() {
        // A decorator itself is an upgraded state, so it cannot be further upgraded by this system.
        // It delegates to the wrapped tower, which should be level 2.
        return decoratedTower.isUpgradeable();
    }

    @Override
    public Tower upgrade() {
        // Decorators represent the upgraded state. They cannot be upgraded further by this mechanism.
        return this;
    }

    @Override
    public void setLevel(int lvl) {
        decoratedTower.setLevel(lvl);
    }

    @Override
    public void applyOnHitEffect(Enemy enemy, Playing playingScene) {
        decoratedTower.applyOnHitEffect(enemy, playingScene); // Delegate by default
    }

    // Abstract method for decorators to provide their specific sprite
    public abstract BufferedImage getSprite();
} 
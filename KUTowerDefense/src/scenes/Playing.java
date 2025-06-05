package scenes;

import java.awt.Graphics;
import java.awt.event.MouseWheelEvent;
import main.Game;
import managers.*;
import javax.swing.JPanel;
import controllers.PlayingController;

/**
 * Playing - Thin wrapper around PlayingController for MVC architecture
 * This class now delegates all functionality to the PlayingController
 * 
 * Responsibilities:
 * - Provide backward compatibility with existing code
 * - Delegate all operations to PlayingController
 * - Maintain the same public interface as before
 */
public class Playing extends GameScene implements SceneMethods {
    
    private PlayingController controller;

    public Playing(Game game, JPanel gamePanel) {
        super(game);
        this.controller = new PlayingController(game);
    }

    public Playing(Game game) {
        super(game);
        this.controller = new PlayingController(game);
    }

    public Playing(Game game, TileManager tileManager) {
        super(game);
        this.controller = new PlayingController(game, tileManager);
    }

    public Playing(Game game, TileManager tileManager, int[][] customLevel, int[][] customOverlay) {
        super(game);
        this.controller = new PlayingController(game, tileManager, customLevel, customOverlay);
    }
    
    // Protected constructor for adapter use - doesn't create controller to avoid circular dependency
    protected Playing(Game game, boolean isAdapter) {
        super(game);
        this.controller = null; // Adapter doesn't need a real controller
    }
    
    // Delegate core game loop methods to controller
    public void update() {
        if (controller != null) controller.update();
    }

    @Override
    public void render(Graphics g) {
        if (controller != null) controller.render(g);
    }
    
    // Delegate input handling to controller
    @Override
    public void mouseClicked(int x, int y) {
        if (controller != null) controller.mouseClicked(x, y);
    }

    @Override
    public void mouseMoved(int x, int y) {
        if (controller != null) controller.mouseMoved(x, y);
    }

    @Override
    public void mousePressed(int x, int y) {
        if (controller != null) controller.mousePressed(x, y);
    }

    @Override
    public void mouseReleased(int x, int y) {
        if (controller != null) controller.mouseReleased(x, y);
    }

    @Override
    public void mouseDragged(int x, int y) {
        if (controller != null) controller.mouseDragged(x, y);
    }
    
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (controller != null) controller.mouseWheelMoved(e);
    }
    
    // Delegate utility methods to controller/model
    public boolean isGamePaused() {
        return controller != null ? controller.isGamePaused() : false;
    }

    public boolean isOptionsMenuOpen() {
        return controller != null ? controller.isOptionsMenuOpen() : false;
    }

    public float getGameSpeedMultiplier() {
        return controller != null ? controller.getGameSpeedMultiplier() : 1.0f;
    }
    
    public String getCurrentMapName() {
        return controller != null ? controller.getCurrentMapName() : "default";
    }
    
    public String getCurrentDifficulty() {
        return controller != null ? controller.getCurrentDifficulty() : "Normal";
    }
    
    // Backward compatibility methods - delegate to model
    public managers.TowerManager getTowerManager() {
        return controller != null ? controller.getModel().getTowerManager() : null;
    }
    
    public managers.EnemyManager getEnemyManager() {
        return controller != null ? controller.getModel().getEnemyManager() : null;
    }
    
    public managers.PlayerManager getPlayerManager() {
        return controller != null ? controller.getModel().getPlayerManager() : null;
    }
    
    public managers.WeatherManager getWeatherManager() {
        return controller != null ? controller.getModel().getWeatherManager() : null;
    }
    
    public managers.TileManager getTileManager() {
        return controller != null ? controller.getModel().getTileManager() : null;
    }
    
    public managers.WaveManager getWaveManager() {
        return controller != null ? controller.getModel().getWaveManager() : null;
    }
    
    public managers.UltiManager getUltiManager() {
        return controller != null ? controller.getModel().getUltiManager() : null;
    }
    
    public managers.GoldBagManager getGoldBagManager() {
        return controller != null ? controller.getModel().getGoldBagManager() : null;
    }
    
    public managers.FireAnimationManager getFireAnimationManager() {
        return controller != null ? controller.getModel().getFireAnimationManager() : null;
    }
    
    public ui_p.PlayingUI getPlayingUI() {
        return controller != null ? controller.getView().getPlayingUI() : null;
    }

    public int[][] getLevel() {
        return controller != null ? controller.getModel().getLevel() : null;
    }

    public int[][] getOverlay() {
        return controller != null ? controller.getModel().getOverlay() : null;
    }
    
    public long getGameTime() {
        return controller != null ? controller.getModel().getGameTime() : 0;
    }
    
    public String getWaveStatus() {
        return controller != null ? controller.getModel().getWaveStatus() : "Loading...";
    }
    
    public boolean isAllWavesFinished() {
        return controller != null ? controller.getModel().isAllWavesFinished() : false;
    }
    
    public java.util.List<ui_p.DeadTree> getDeadTrees() {
        return controller != null ? controller.getModel().getDeadTrees() : null;
    }
    
    public java.util.List<ui_p.LiveTree> getLiveTrees() {
        return controller != null ? controller.getModel().getLiveTrees() : null;
    }
    
    public objects.Tower getDisplayedTower() {
        return controller != null ? controller.getModel().getDisplayedTower() : null;
    }
    
    public void setDisplayedTower(objects.Tower tower) {
        if (controller != null) controller.getModel().setDisplayedTower(tower);
    }
    
    public ui_p.DeadTree getSelectedDeadTree() {
        return controller != null ? controller.getModel().getSelectedDeadTree() : null;
    }
    
    public void setSelectedDeadTree(ui_p.DeadTree deadTree) {
        if (controller != null) controller.getModel().setSelectedDeadTree(deadTree);
    }
    
    // Game state management methods
    public void loadLevel(String levelName) {
        controller.loadLevel(levelName);
    }
    
    public void saveLevel(String filename) {
        controller.saveLevel(filename);
    }
    
    public void reloadGameOptions() {
        controller.reloadGameOptions();
    }
    
    public void returnToMainMenu() {
        controller.returnToMainMenu();
    }
    
    // Compatibility methods for managers that call these
    public void incrementEnemyDefeated() {
        controller.getModel().incrementEnemyDefeated();
    }

    public void addTotalDamage(int damage) {
        controller.getModel().addTotalDamage(damage);
    }
    
    public void enemyReachedEnd(enemies.Enemy enemy) {
        controller.getModel().enemyReachedEnd(enemy);
    }
    
    public void spawnEnemy(int enemyType) {
        controller.getModel().spawnEnemy(enemyType);
    }
    
    public void startWarriorPlacement(objects.Warrior warrior) {
        controller.startWarriorPlacement(warrior);
    }
    
    public void shootEnemy(Object shooter, enemies.Enemy enemy) {
        // Use the abstracted method instead of direct manager access
        if (controller != null) {
            controller.createProjectile(shooter, enemy);
        }
    }
    
    // Legacy methods that might be called by other components
    public String getMapName() {
        return getCurrentMapName();
    }

    public void setCurrentMapName(String mapName) {
        controller.getModel().setCurrentMapName(mapName);
    }

    public void setCurrentDifficulty(String difficulty) {
        if (controller != null) {
            controller.getModel().setCurrentDifficulty(difficulty);
            // Reload the difficulty configuration to apply the new settings
            controller.reloadDifficultyConfiguration();
        }
    }
    
    public config.GameOptions getGameOptions() {
        return controller.getModel().getGameOptions();
    }
    
    @Override
    public void playButtonClickSound() {
        managers.AudioManager.getInstance().playButtonClickSound();
    }
    
    // Provide access to the controller for any code that needs direct access
    public PlayingController getController() {
        return controller;
    }
    
    // UI Resource management - delegate to view observer updates
    public void updateUIResources() {
        if (controller != null && controller.getModel() != null && controller.getModel().getPlayerManager() != null) {
            // Update UI with current resources
            // TODO: Implement UI update
        }
    }
    
    // Game state save/load - delegate to controller
    public void saveGameState() {
        if (controller != null) {
            controller.saveGameState("autosave");
        }
    }
    
    public void loadGameState() {
        if (controller != null) {
            controller.loadGameState("autosave");
        }
    }
    
    public void resetGameState() {
        if (controller != null) {
            controller.resetGameState();
        }
    }
    
    // Tile modification - delegate to controller/model
    public void modifyTile(int x, int y, String tile) {
        x /= 64;
        y /= 64;

        int[][] level = controller.getModel().getLevel();
        if (level == null) return;
        
        if (tile.equals("ARCHER")) {
            level[y][x] = 26;
        } else if (tile.equals("MAGE")) {
            level[y][x] = 20;
        } else if (tile.equals("ARTILERRY")) {
            level[y][x] = 21;
        } else if (tile.equals("DEADTREE")) {
            level[y][x] = 15;
        }
    }
}

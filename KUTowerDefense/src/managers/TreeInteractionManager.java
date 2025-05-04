package managers;

import constants.Constants;
import scenes.Playing;
import ui_p.DeadTree;
import ui_p.LiveTree;
import ui_p.FireAnimation;

import java.util.Iterator;
import java.util.List;

public class TreeInteractionManager {

    private final Playing playing;

    public TreeInteractionManager(Playing playing) {
        this.playing = playing;
    }

    public void handleDeadTreeInteraction(int mouseX, int mouseY) {
        for (DeadTree tree : playing.getDeadTrees()) {
            if (tree.isShowChoices()) {
                int tileX = tree.getX();
                int tileY = tree.getY();
                if (tree.getArcherButton().isMousePressed(mouseX, mouseY)) {
                    int cost = Constants.Towers.getCost(Constants.Towers.ARCHER);
                    if (playing.getPlayerManager().spendGold(cost)) {
                        playing.getTowerManager().buildArcherTower(tileX, tileY);
                        playing.getDeadTrees().remove(tree);
                        playing.setSelectedDeadTree(null);
                        playing.setDisplayedTower(null);
                        playing.modifyTile(tileX, tileY, "ARCHER");
                        playing.updateUIResources();
                    }
                    tree.setShowChoices(false);
                    return;
                }
                if (tree.getMageButton().isMousePressed(mouseX, mouseY)) {
                    int cost = Constants.Towers.getCost(Constants.Towers.MAGE);
                    if (playing.getPlayerManager().spendGold(cost)) {
                        playing.getTowerManager().buildMageTower(tileX, tileY);
                        playing.getDeadTrees().remove(tree);
                        playing.setSelectedDeadTree(null);
                        playing.setDisplayedTower(null);
                        playing.modifyTile(tileX, tileY, "MAGE");
                        playing.updateUIResources();
                    }
                    tree.setShowChoices(false);
                    return;
                }
                if (tree.getArtilleryButton().isMousePressed(mouseX, mouseY)) {
                    int cost = Constants.Towers.getCost(Constants.Towers.ARTILLERY);
                    if (playing.getPlayerManager().spendGold(cost)) {
                        playing.getTowerManager().buildArtilerryTower(tileX, tileY);
                        playing.getDeadTrees().remove(tree);
                        playing.setSelectedDeadTree(null);
                        playing.setDisplayedTower(null);
                        playing.modifyTile(tileX, tileY, "ARTILERRY");
                        playing.updateUIResources();
                    }
                    tree.setShowChoices(false);
                    return;
                }
            }
        }

        for (DeadTree tree : playing.getDeadTrees()) {
            if (tree.isClicked(mouseX, mouseY)) {
                for (DeadTree other : playing.getDeadTrees()) {
                    other.setShowChoices(false);
                }
                tree.setShowChoices(true);
                playing.setSelectedDeadTree(tree);
                playing.setDisplayedTower(null);
                return;
            }
        }
    }

    public void handleLiveTreeInteraction(int mouseX, int mouseY) {
        Iterator<LiveTree> iterator = playing.getLiveTrees().iterator();
        while (iterator.hasNext()) {
            LiveTree tree = iterator.next();
            if (tree.isShowChoices()) {
                int tileX = tree.getX();
                int tileY = tree.getY();
                int burnCost = Constants.BURN_TREE_COST;
                if (tree.getFireButton().isMousePressed(mouseX, mouseY)) {
                    if (playing.getPlayerManager().spendGold(burnCost)) {
                        iterator.remove();
                        playing.getDeadTrees().add(new DeadTree(tileX, tileY));
                        tree.setShowChoices(false);
                        playing.modifyTile(tileX, tileY, "DEADTREE");
                        playing.getFireAnimationManager().addAnimation(new FireAnimation(tileX, tileY));
                        playing.updateUIResources();
                    }
                    return;
                }
            }
        }

        for (LiveTree tree : playing.getLiveTrees()) {
            if (tree.isClicked(mouseX, mouseY)) {
                for (LiveTree other : playing.getLiveTrees()) {
                    other.setShowChoices(false);
                }
                for (DeadTree dt : playing.getDeadTrees()) {
                    dt.setShowChoices(false);
                }
                tree.setShowChoices(true);
                playing.setDisplayedTower(null);
                return;
            }
        }
    }
}

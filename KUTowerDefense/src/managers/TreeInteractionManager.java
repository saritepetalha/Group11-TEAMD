package managers;

import constants.Constants;
import scenes.Playing;
import ui_p.DeadTree;
import ui_p.LiveTree;
import ui_p.FireAnimation;

import java.util.Iterator;

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
                    int cost = playing.getTowerManager().getTowerCostFromOptions(Constants.Towers.ARCHER, playing.getGameOptions());
                    if (playing.getPlayerManager().spendGold(cost)) {
                        playing.getTowerManager().buildArcherTower(tileX, tileY);
                        playing.getDeadTrees().remove(tree);
                        playing.setSelectedDeadTree(null);
                        playing.setDisplayedTower(null);
                        playing.modifyTile(tileX, tileY, "ARCHER");
                        playing.updateUIResources();
                        // Update wave start tree states to reflect the removed dead tree
                        playing.updateWaveStartTreeStates();
                    }
                    tree.setShowChoices(false);
                    return;
                }
                if (tree.getMageButton().isMousePressed(mouseX, mouseY)) {
                    int cost = playing.getTowerManager().getTowerCostFromOptions(Constants.Towers.MAGE, playing.getGameOptions());
                    if (playing.getPlayerManager().spendGold(cost)) {
                        playing.getTowerManager().buildMageTower(tileX, tileY);
                        playing.getDeadTrees().remove(tree);
                        playing.setSelectedDeadTree(null);
                        playing.setDisplayedTower(null);
                        playing.modifyTile(tileX, tileY, "MAGE");
                        playing.updateUIResources();
                        // Update wave start tree states to reflect the removed dead tree
                        playing.updateWaveStartTreeStates();
                    }
                    tree.setShowChoices(false);
                    return;
                }
                if (tree.getArtilleryButton().isMousePressed(mouseX, mouseY)) {
                    int cost = playing.getTowerManager().getTowerCostFromOptions(Constants.Towers.ARTILLERY, playing.getGameOptions());
                    if (playing.getPlayerManager().spendGold(cost)) {
                        playing.getTowerManager().buildArtilerryTower(tileX, tileY);
                        playing.getDeadTrees().remove(tree);
                        playing.setSelectedDeadTree(null);
                        playing.setDisplayedTower(null);
                        playing.modifyTile(tileX, tileY, "ARTILERRY");
                        playing.updateUIResources();
                        // Update wave start tree states to reflect the removed dead tree
                        playing.updateWaveStartTreeStates();
                    }
                    tree.setShowChoices(false);
                    return;
                }
                if (tree.getPoisonButton().isMousePressed(mouseX, mouseY)) {
                    int cost = Constants.Towers.getCost(Constants.Towers.POISON);
                    if (playing.getPlayerManager().spendGold(cost)) {
                        playing.getTowerManager().buildPoisonTower(tileX, tileY);
                        playing.getDeadTrees().remove(tree);
                        playing.setSelectedDeadTree(null);
                        playing.setDisplayedTower(null);
                        playing.modifyTile(tileX, tileY, "POISON");
                        playing.updateUIResources();
                        // Update wave start tree states to reflect the removed dead tree
                        playing.updateWaveStartTreeStates();
                        System.out.println("🧪 Poison Tower built at (" + tileX + ", " + tileY + ") for " + cost + " gold");
                    }
                    tree.setShowChoices(false);
                    return;
                }
            }
        }

        for (DeadTree tree : playing.getDeadTrees()) {
            if (tree.isClicked(mouseX, mouseY)) {
                for (LiveTree lt : playing.getLiveTrees()) {
                    lt.setShowChoices(false);
                }
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
                        // Update wave start tree states to reflect the burned tree
                        playing.updateWaveStartTreeStates();
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

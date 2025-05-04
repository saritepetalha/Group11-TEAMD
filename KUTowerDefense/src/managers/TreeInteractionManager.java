package managers;

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
                    playing.getTowerManager().buildArcherTower(tileX, tileY);
                    playing.getDeadTrees().remove(tree);
                    playing.setSelectedDeadTree(null);
                    playing.setDisplayedTower(null);
                    playing.modifyTile(tileX, tileY, "ARCHER");
                    return;
                }
                if (tree.getMageButton().isMousePressed(mouseX, mouseY)) {
                    playing.getTowerManager().buildMageTower(tileX, tileY);
                    playing.getDeadTrees().remove(tree);
                    playing.setSelectedDeadTree(null);
                    playing.setDisplayedTower(null);
                    playing.modifyTile(tileX, tileY, "MAGE");
                    return;
                }
                if (tree.getArtilleryButton().isMousePressed(mouseX, mouseY)) {
                    playing.getTowerManager().buildArtilerryTower(tileX, tileY);
                    playing.getDeadTrees().remove(tree);
                    playing.setSelectedDeadTree(null);
                    playing.setDisplayedTower(null);
                    playing.modifyTile(tileX, tileY, "ARTILERRY");
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
                if (tree.getFireButton().isMousePressed(mouseX, mouseY)) {
                    iterator.remove();
                    playing.getDeadTrees().add(new DeadTree(tileX, tileY));
                    tree.setShowChoices(false);
                    playing.modifyTile(tileX, tileY, "DEADTREE");
                    playing.getFireAnimationManager().addAnimation(new FireAnimation(tileX, tileY));
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

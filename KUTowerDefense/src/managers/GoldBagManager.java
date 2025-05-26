package managers;

import objects.GoldBag;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class GoldBagManager {
    private final ArrayList<GoldBag> goldBags = new ArrayList<>();
    private final Random random = new Random();

    public void spawnGoldBag(float x, float y, int minGold, int maxGold) {
        int goldAmount = minGold + random.nextInt(maxGold - minGold + 1);
        goldBags.add(new GoldBag(x, y, goldAmount));
        AudioManager.getInstance().playSound("coin_drop");
    }

    public void update() {
        Iterator<GoldBag> iterator = goldBags.iterator();
        while (iterator.hasNext()) {
            GoldBag bag = iterator.next();
            bag.update();
            if (bag.isCollected() || bag.isExpired()) {
                iterator.remove();
            }
        }
    }

    public void draw(Graphics g) {
        for (GoldBag bag : goldBags) {
            bag.draw(g);
        }
    }

    public GoldBag tryCollect(int mouseX, int mouseY) {
        for (GoldBag bag : goldBags) {
            if (!bag.isCollected() && bag.contains(mouseX, mouseY)) {
                bag.collect();
                return bag;
            }
        }
        return null;
    }
} 
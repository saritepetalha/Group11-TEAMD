package managers;

import ui_p.FireAnimation;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FireAnimationManager {

    private final List<FireAnimation> animations;

    public FireAnimationManager() {
        this.animations = new ArrayList<>();
    }

    public void addAnimation(FireAnimation animation) {
        animations.add(animation);
    }

    public void update() {
        Iterator<FireAnimation> iterator = animations.iterator();
        while (iterator.hasNext()) {
            FireAnimation animation = iterator.next();
            animation.update();
            if (animation.isFinished()) {
                iterator.remove();
            }
        }
    }

    public void draw(Graphics g) {
        for (FireAnimation animation : animations) {
            animation.draw(g);
        }
    }

    /**
     * Clear all fire animations for game restart
     */
    public void clear() {
        animations.clear();
    }
}

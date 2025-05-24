package scenes;
import java.awt.Graphics;
import java.awt.event.MouseWheelEvent;
import managers.AudioManager;

public interface SceneMethods {
    public void render(Graphics g);
    public void mouseClicked(int x, int y);
    public void mouseMoved(int x, int y);
    public void mousePressed(int x, int y);
    public void mouseReleased(int x, int y);
    public void mouseDragged(int x, int y);
    public default void mouseWheelMoved(MouseWheelEvent e) {};


    default void playButtonClickSound() {
        AudioManager.getInstance().playSound("button_click");
    }
}

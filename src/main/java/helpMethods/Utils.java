package helpMethods;

public class Utils {

    public static int GetHypo(float x1, float y1, float x2, float y2) {
        float differenceX = Math.abs(x1-x2);
        float differenceY = Math.abs(y1-y2);
        int hypo = (int) Math.hypot(differenceX, differenceY);
        return hypo;
    }

}

package config;

import java.util.EnumMap;
import java.util.Map;

public class Group {

    private Map<EnemyType,Integer> composition = new EnumMap<>(EnemyType.class);
    private double intraEnemyDelay;   // seconds

    public Group() { }

    public Group(Map<EnemyType,Integer> composition,
                 double intraEnemyDelay) {
        this.composition.putAll(composition);   // assumes caller validated map
        setIntraEnemyDelay(intraEnemyDelay);
    }

    public Map<EnemyType,Integer> getComposition() { return composition; }

    public double getIntraEnemyDelay() { return intraEnemyDelay; }
    public void setIntraEnemyDelay(double d) {
        if (d < 0) throw new IllegalArgumentException("Delay ≥ 0 required");
        this.intraEnemyDelay = d;
    }
}

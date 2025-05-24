package config;

public class TowerStats {

    private int buildCost;
    private double range;
    private double fireRate;   // shots / sec
    private double aoeRadius;  // 0 ⇒ single-target
    private int damage;

    public TowerStats() { }

    public TowerStats(int buildCost,
                      double range,
                      double fireRate,
                      double aoeRadius,
                      int damage) {
        setBuildCost(buildCost);
        setRange(range);
        setFireRate(fireRate);
        setAoeRadius(aoeRadius);
        setDamage(damage);
    }

    // Getters
    public int    getBuildCost() { return buildCost; }
    public double getRange()     { return range; }
    public double getFireRate()  { return fireRate; }
    public double getAoeRadius() { return aoeRadius; }
    public int    getDamage()    { return damage; }

    // Setters + validation
    public void setBuildCost(int cost) {
        if (cost < 0) throw new IllegalArgumentException("Cost ≥ 0 required");
        this.buildCost = cost;
    }
    public void setRange(double range) {
        if (range <= 0) throw new IllegalArgumentException("Range > 0 required");
        this.range = range;
    }
    public void setFireRate(double rate) {
        if (rate <= 0) throw new IllegalArgumentException("Fire-rate > 0 required");
        this.fireRate = rate;
    }
    public void setAoeRadius(double r) {
        if (r < 0) throw new IllegalArgumentException("AOE radius ≥ 0 required");
        this.aoeRadius = r;
    }
    public void setDamage(int dmg) {
        if (dmg <= 0) throw new IllegalArgumentException("Damage > 0 required");
        this.damage = dmg;
    }
}

package config;

public class EnemyStats {

    private int hitPoints;
    private double moveSpeed;
    private int goldReward;

    // --- Required by Gson ---
    public EnemyStats() { }

    // --- Convenience ctor for hand-coded defaults / tests ---
    public EnemyStats(int hitPoints, double moveSpeed, int goldReward) {
        setHitPoints(hitPoints);
        setMoveSpeed(moveSpeed);
        setGoldReward(goldReward);
    }

    // Getters
    public int getHitPoints()   { return hitPoints;  }
    public double getMoveSpeed(){ return moveSpeed;  }
    public int getGoldReward()  { return goldReward; }

    // Setters (with validation)
    public void setHitPoints(int hitPoints) {
        if (hitPoints <= 0) throw new IllegalArgumentException("HP must be > 0");
        this.hitPoints = hitPoints;
    }
    public void setMoveSpeed(double moveSpeed) {
        if (moveSpeed <= 0) throw new IllegalArgumentException("Speed must be > 0");
        this.moveSpeed = moveSpeed;
    }
    public void setGoldReward(int goldReward) {
        if (goldReward < 0) throw new IllegalArgumentException("Gold must be ≥ 0");
        this.goldReward = goldReward;
    }
}


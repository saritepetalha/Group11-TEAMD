package config;

import java.util.ArrayList;
import java.util.List;

public class Wave {

    private List<Group> groups = new ArrayList<>();
    private double intraGroupDelay;   // seconds

    public Wave() { }

    public Wave(List<Group> groups, double intraGroupDelay) {
        this.groups.addAll(groups);
        setIntraGroupDelay(intraGroupDelay);
    }

    public List<Group> getGroups() { return groups; }

    public double getIntraGroupDelay() { return intraGroupDelay; }
    public void setIntraGroupDelay(double d) {
        if (d < 0) throw new IllegalArgumentException("Delay ≥ 0 required");
        this.intraGroupDelay = d;
    }
}

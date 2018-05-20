package SwingGUI;

public class UserResults {
    boolean prayerDream;
    int numOverloads;
    int numAbsorptions;
    double activeUsagePercent;

    public UserResults(boolean prayerDream, int numOverloads, int numAbsorptions, double activeUsagePercent) {
        this.prayerDream = prayerDream;
        this.numOverloads = numOverloads;
        this.numAbsorptions = numAbsorptions;
        this.activeUsagePercent = activeUsagePercent;
    }

    public boolean isPrayerDream() {
        return prayerDream;
    }

    public int getNumOverloads() {
        return numOverloads;
    }

    public int getNumAbsorptions() {
        return numAbsorptions;
    }

    public double getActiveUsagePercent() {
        return activeUsagePercent;
    }
}

package SwingGUI;

import JSON_Simple.src.main.java.org.json.simple.JSONObject;

public class GUIResults {
    private boolean set;
    private boolean prayerDream;
    private int numOverloads;
    private int numAbsorptions;
    private int activeUsagePercent;


    public GUIResults(boolean prayerDream, int numOverloads, int numAbsorptions, int activeUsagePercent) {
        this.prayerDream = prayerDream;
        this.numOverloads = numOverloads;
        this.numAbsorptions = numAbsorptions;
        this.activeUsagePercent = activeUsagePercent;
        this.set = true;
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

    public double getActiveUsagePercentAsDecimal() {
        return ((double)activeUsagePercent / 100);
    }

    public boolean isSet() {
        return set;
    }

    public String toJSON(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("set", this.set);
        jsonObject.put("prayerDream", this.prayerDream);
        jsonObject.put("numOverloads", this.numOverloads);
        jsonObject.put("numAbsorptions", this.numAbsorptions);
        jsonObject.put("activeUsagePercent", this.activeUsagePercent);
        return jsonObject.toJSONString();
    }
}

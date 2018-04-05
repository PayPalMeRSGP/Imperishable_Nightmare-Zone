package ScriptClasses.Util;

public enum SpecialAttackWeapons {
    //Melee Weapons
    ABYSSAL_WHIP("Abyssal whip", 4151, 50),
    ABYSSAL_DAGGER("Abyssal dagger", 13265, 50), ABYSSAL_DAGGER_PPP("Abyssal dagger(p++)", 13271, 50),
    DRAGON_DAGGER("Dragon dagger", 1215, 25), DRAGON_DAGGER_PPP("Dragon dagger(p++)", 5698, 25),
    DRAGON_SWORD("Dragon Sword", 21009, 40),
    DRAGON_CLAWS("Dragon claws", 13652, 50),
    DRAGON_SCIMITAR("Dragon scimitar", 4587, 55),
    GRANITE_MAUL("Granite maul", 4153, 50), GRANITE_HAMMER("Granite hammer", 21742, 60),
    SARADOMIN_SWORD("Saradomin sword", 11838, 100),
    //Ranged weapons
    ARMADYL_CROSSBOW("Armadyl crossbow", 11785, 40),
    DRAGON_CROSSBOW("Dragon crossbow", 21902, 60),
    MAGIC_SHORTBOW("Magic shortbow", 861, 55), MAGIC_SHORTBOW_I("Magic shortbow (i)", 12788, 50);

    private String itemName;
    private int itemID;
    private int specUsage;

    SpecialAttackWeapons(String itemName, int itemID, int specUsage) {
        this.itemName = itemName;
        this.itemID = itemID;
        this.specUsage = specUsage;
    }

    public String getItemName() {
        return itemName;
    }

    public int getItemID() {
        return itemID;
    }

    public int getSpecUsage() {
        return specUsage;
    }
}

package Nodes;

import ScriptClasses.PaintInfo;
import ScriptClasses.PublicStaticFinalConstants;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.Prayer;
import org.osbot.rs07.api.Tabs;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.ui.*;
import org.osbot.rs07.listener.MessageListener;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.concurrent.ThreadLocalRandom;
/*
    AFK node does not flick prayer to prevent health regen
    This Node seeks to emulate a human player leaving his account afk in nmz thereby letting his hp regen up to a random amount (hpRegenLimit)
    then coming back and guzzling rockcakes back to 1 hp before afking again.

*/
public class AFKNode implements ExecutableNode {
    private final static String DRINK = "Drink";
    private final static String GUZZLE = "Guzzle";

    private Script hostScriptReference;
    private int hpRegenLimit; //determines when to guzzle back to 1
    private int absorptionMinLimit; //determines when to re-pot absorptions
    private int potionMinBoost; //if using super ranging, determines when to re-pot

    //flags to do certain actions
    private boolean died = false;
    private boolean doOverload = false; //Prep Node handles initial overload, therefore do not need a need initially

    private static ExecutableNode singleton = null;

    private AFKNode(Script hostScriptReference){
        this.hostScriptReference = hostScriptReference;
        this.hpRegenLimit = ThreadLocalRandom.current().nextInt(2, 5); //generate initial random hp limit, this variable is used by handlePotionsAndHP() to determine when to reguzzle back to 1 hp
        this.absorptionMinLimit = absorptionMinLimit = ThreadLocalRandom.current().nextInt(150, 300);
        this.potionMinBoost = ThreadLocalRandom.current().nextInt(5, 8); //generate potion min boost, used to determine next re-pot
    }

    public static ExecutableNode getSingleton(Script hostScriptReference) {
        if(singleton == null){
            singleton = new AFKNode(hostScriptReference);
        }
        return singleton;
    }


    @Override
    public int executeNodeAction() throws InterruptedException {
        boolean drankAbsorptions = handleAbsorptionLvl();
        boolean drankPotions = handlePotionsAndHP();
        hostScriptReference.getMouse().moveOutsideScreen();
        if(!drankAbsorptions && !drankPotions){ //we did not need to drink an absorption or a potion then we are still afking
            PaintInfo.getSingleton(hostScriptReference).setCurrentScriptStatus(PaintInfo.ScriptStatus.AFKING);
        }
        return (int) PublicStaticFinalConstants.randomNormalDist(2000, 400);
    }

    private boolean handleAbsorptionLvl() throws InterruptedException {
        Inventory inv = hostScriptReference.getInventory();
        int absorptionLvl = getAbsorptionLvl();
        if(absorptionLvl < absorptionMinLimit){
            PaintInfo.getSingleton(hostScriptReference).setCurrentScriptStatus(PaintInfo.ScriptStatus.ABSORPTIONS);
            while(absorptionLvl < 300 && doesPlayerHaveAbsorptionsLeft()){
                inv.interact(DRINK, PublicStaticFinalConstants.ABSORPTION_POTION_1_ID, PublicStaticFinalConstants.ABSORPTION_POTION_2_ID,
                        PublicStaticFinalConstants.ABSORPTION_POTION_3_ID, PublicStaticFinalConstants.ABSORPTION_POTION_4_ID);
                absorptionLvl = getAbsorptionLvl();
                MethodProvider.sleep(PublicStaticFinalConstants.randomNormalDist(PublicStaticFinalConstants.RS_GAME_TICK_MS, 60.0));
            }
            this.absorptionMinLimit = ThreadLocalRandom.current().nextInt(150, 300);
            return true;
        }
        return false;
    }

    private boolean handlePotionsAndHP() throws InterruptedException {
        openInventoryTab();
        int currentHealth = hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
        if(currentHealth >= hpRegenLimit){
            handleOverload();
            guzzleRockCakeTo1();
            hostScriptReference.getMouse().moveOutsideScreen();
            hpRegenLimit = ThreadLocalRandom.current().nextInt(2, 5); //generate next random hp limit
            return true;
        }
        hostScriptReference.getMouse().moveOutsideScreen();
        return false;

    }

    private boolean drinkSuperRangingPotion(){
        openInventoryTab();
        int currentRangeBoost = hostScriptReference.getSkills().getDynamic(Skill.RANGED) - hostScriptReference.getSkills().getStatic(Skill.RANGED);
        if(doesPlayerHaveSuperRangePotsLeft() && currentRangeBoost < potionMinBoost){
            Inventory inv = hostScriptReference.getInventory();
            this.potionMinBoost = ThreadLocalRandom.current().nextInt(3, 7);
            return inv.interact(PublicStaticFinalConstants.DRINK, PublicStaticFinalConstants.SUPER_RANGING_4_ID, PublicStaticFinalConstants.SUPER_RANGING_3_ID,
                    PublicStaticFinalConstants.SUPER_RANGING_2_ID, PublicStaticFinalConstants.SUPER_RANGING_1_ID);
        }
        return false;
    }

    private void handleOverload() throws InterruptedException {
        openInventoryTab();
        if(doOverload && doesPlayerHaveOverloadsLeft() && doesPlayerHaveAbsorptionsLeft()){
            PaintInfo.getSingleton(hostScriptReference).setCurrentScriptStatus(PaintInfo.ScriptStatus.OVERLOADING);
            Inventory inv = hostScriptReference.getInventory();
            inv.interact(DRINK, PublicStaticFinalConstants.OVERLOAD_POTION_1_ID, PublicStaticFinalConstants.OVERLOAD_POTION_2_ID,
                    PublicStaticFinalConstants.OVERLOAD_POTION_3_ID, PublicStaticFinalConstants.OVERLOAD_POTION_4_ID);

            //while hp is being depleted from overload it is possible to lose alot of absorptions
            Prayer prayer = PublicStaticFinalConstants.hostScriptReference.getPrayer();
            prayer.open();
            int currentPrayerPts = hostScriptReference.getSkills().getDynamic(Skill.PRAYER);
            boolean didMeleePrayer = false;
            if(currentPrayerPts > 0){
                prayer.set(PrayerButton.PROTECT_FROM_MELEE, true);
                didMeleePrayer = true;
            }

            int startingHealth = hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
            int estimatedHealthAfterOverload = startingHealth - 49;
            new ConditionalSleep(7000, 500){
                @Override
                public boolean condition() throws InterruptedException {

                    int currentHealth = hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
                    return estimatedHealthAfterOverload > currentHealth;
                }
            }.sleep();
            if(didMeleePrayer){
                prayer.set(PrayerButton.PROTECT_FROM_MELEE, false);
            }
            doOverload = false;
            hostScriptReference.log("doOverload = false");
        }
    }

    private void guzzleRockCakeTo1() throws InterruptedException {
        int currentHealth = hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
        if(currentHealth > 50){
            return;
        }
        while(currentHealth > 1){
            PaintInfo.getSingleton(hostScriptReference).setCurrentScriptStatus(PaintInfo.ScriptStatus.GUZZLING_ROCKCAKES);
            Inventory inv = hostScriptReference.getInventory();
            inv.interact(GUZZLE, PublicStaticFinalConstants.DWARVEN_ROCK_CAKE_ID);
            MethodProvider.sleep(PublicStaticFinalConstants.randomNormalDist(PublicStaticFinalConstants.RS_GAME_TICK_MS, 60.0));
            currentHealth = hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
        }
    }

    private void openInventoryTab(){
        Tabs tab = hostScriptReference.getTabs();
        if(tab.getOpen() != Tab.INVENTORY){
            tab.open(Tab.INVENTORY);
        }
    }

    private boolean doesPlayerHaveAbsorptionsLeft(){
        Inventory inv = hostScriptReference.getInventory();
        return inv.contains(PublicStaticFinalConstants.ABSORPTION_POTION_1_ID) || inv.contains(PublicStaticFinalConstants.ABSORPTION_POTION_2_ID)
                || inv.contains(PublicStaticFinalConstants.ABSORPTION_POTION_3_ID) || inv.contains(PublicStaticFinalConstants.ABSORPTION_POTION_4_ID);
    }

    private boolean doesPlayerHaveOverloadsLeft(){
        Inventory inv = hostScriptReference.getInventory();
        return inv.contains(PublicStaticFinalConstants.OVERLOAD_POTION_1_ID) || inv.contains(PublicStaticFinalConstants.OVERLOAD_POTION_2_ID)
                || inv.contains(PublicStaticFinalConstants.OVERLOAD_POTION_3_ID) || inv.contains(PublicStaticFinalConstants.OVERLOAD_POTION_4_ID);
    }

    private boolean doesPlayerHaveSuperRangePotsLeft(){
        Inventory inv = hostScriptReference.getInventory();
        return inv.contains(PublicStaticFinalConstants.SUPER_RANGING_1_ID) || inv.contains(PublicStaticFinalConstants.SUPER_RANGING_2_ID)
                || inv.contains(PublicStaticFinalConstants.SUPER_RANGING_3_ID) || inv.contains(PublicStaticFinalConstants.SUPER_RANGING_4_ID);
    }

    private int getAbsorptionLvl() {
        RS2Widget widget = hostScriptReference.getWidgets().get(202, 1, 9);
        if(widget != null && widget.isVisible() && widget.getMessage() != null) {
            int absorptionLvl = Integer.parseInt(widget.getMessage().replace(",", ""));
            return Integer.parseInt(widget.getMessage().replace(",", ""));
        }

        return 0;
    }

    public void setDied(boolean died) {
        this.died = died;
    }

    public void setDoOverload(boolean doOverload) {
        this.doOverload = doOverload;
    }

}


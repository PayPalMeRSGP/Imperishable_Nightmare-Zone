package Nodes.MidDreamNodes;

import Nodes.ExecutableNode;
import ScriptClasses.PaintInfo;
import ScriptClasses.Statics;
import org.osbot.rs07.api.Camera;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.Prayer;
import org.osbot.rs07.api.Tabs;
import org.osbot.rs07.api.ui.PrayerButton;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public abstract class MidDreamNode implements ExecutableNode {
    private final static String DRINK = "Drink";
    private final static String GUZZLE = "Guzzle";
    Script hostScriptReference;
    private int absorptionMinLimit; //determines when to re-pot absorptions
    private int potionMinBoost; //if using super ranging, determines when to re-pot

    //flags to do certain actions
    private boolean playerDied;
    private boolean doOverload;
    private boolean doCameraRotation;

    MidDreamNode(Script hostScriptReference){
        this.hostScriptReference = hostScriptReference;
        this.absorptionMinLimit =  ThreadLocalRandom.current().nextInt(200, 400);
        this.potionMinBoost = ThreadLocalRandom.current().nextInt(3, 7); //generate potion min boost, used to determine next re-pot
        this.doOverload = true;
    }

    void overloadFailSafe(){
        if(!doOverload && hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS) > 50){ //redundant check as onMessage can rarely not work.
            doOverload = true;
            hostScriptReference.log("redundant check set doOverload -> true");
        }
    }

    boolean handleAbsorptionLvl() throws InterruptedException {
        Inventory inv = hostScriptReference.getInventory();
        int absorptionLvl = getAbsorptionLvl();
        if(absorptionLvl < 0){
            RS2Widget absoprtionWidget = hostScriptReference.getWidgets().get(202, 1, 9);
            if(absoprtionWidget != null){
                boolean absorptionsVisable = absoprtionWidget.isVisible();
                if(!absorptionsVisable && playerDied){
                    hostScriptReference.log("absorptions widget is invisible, likely outside dream, stopping");
                    hostScriptReference.stop();
                }
            }
            else{
                return false;
            }
        }
        //absorptionLvl >= 0 is because getAbsorptionLvl returns -1 in error cases, such as the widget not being visible.
        if(absorptionLvl < absorptionMinLimit && absorptionLvl >= 0){
            PaintInfo.getSingleton(hostScriptReference).setCurrentScriptStatus(PaintInfo.ScriptStatus.ABSORPTIONS);
            openInventoryTab();
            while(absorptionLvl < 300 && doesPlayerHaveAbsorptionsLeft()){
                inv.interact(DRINK, Statics.ABSORPTION_POTION_1_ID, Statics.ABSORPTION_POTION_2_ID,
                        Statics.ABSORPTION_POTION_3_ID, Statics.ABSORPTION_POTION_4_ID);
                absorptionLvl = getAbsorptionLvl();
                MethodProvider.sleep(Statics.randomNormalDist(Statics.RS_GAME_TICK_MS, 60.0));
            }
            this.absorptionMinLimit = ThreadLocalRandom.current().nextInt(200, 400);
            return true;
        }
        return false;
    }

    boolean handlePotionsAndHP() throws InterruptedException {
        int currentHealth = hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
        if(currentHealth > 1){
            handleOverload();
            guzzleRockCakeTo1();
            return true;
        }
        return false;

    }

    boolean drinkSuperRangingPotion(){
        openInventoryTab();
        int currentRangeBoost = hostScriptReference.getSkills().getDynamic(Skill.RANGED) - hostScriptReference.getSkills().getStatic(Skill.RANGED);
        if(doesPlayerHaveSuperRangePotsLeft() && currentRangeBoost < potionMinBoost){
            Inventory inv = hostScriptReference.getInventory();
            this.potionMinBoost = ThreadLocalRandom.current().nextInt(3, 7);
            return inv.interact(Statics.DRINK, Statics.SUPER_RANGING_4_ID, Statics.SUPER_RANGING_3_ID,
                    Statics.SUPER_RANGING_2_ID, Statics.SUPER_RANGING_1_ID);
        }
        return false;
    }

    private void handleOverload() throws InterruptedException {
        if(doOverload && doesPlayerHaveOverloadsLeft() && doesPlayerHaveAbsorptionsLeft()){
            PaintInfo.getSingleton(hostScriptReference).setCurrentScriptStatus(PaintInfo.ScriptStatus.OVERLOADING);
            openInventoryTab();
            Inventory inv = hostScriptReference.getInventory();
            inv.interact(DRINK, Statics.OVERLOAD_POTION_1_ID, Statics.OVERLOAD_POTION_2_ID,
                    Statics.OVERLOAD_POTION_3_ID, Statics.OVERLOAD_POTION_4_ID);

            //while hp is being depleted from overload it is possible to lose alot of absorptions
            Prayer prayer = Statics.hostScriptReference.getPrayer();
            int currentPrayerPts = hostScriptReference.getSkills().getDynamic(Skill.PRAYER);
            boolean didMeleePrayer = false;
            if(currentPrayerPts > 0){
                prayer.open();
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
            hostScriptReference.log("doOverload -> false (drank overload)");
        }
    }

    void guzzleRockCakeTo1() throws InterruptedException {
        int currentHealth = hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
        if(currentHealth > 50){
            return;
        }
        while(currentHealth > 1){
            PaintInfo.getSingleton(hostScriptReference).setCurrentScriptStatus(PaintInfo.ScriptStatus.GUZZLING_ROCKCAKES);
            Inventory inv = hostScriptReference.getInventory();
            inv.interact(GUZZLE, Statics.DWARVEN_ROCK_CAKE_ID);
            MethodProvider.sleep(Statics.randomNormalDist(Statics.RS_GAME_TICK_MS, 60.0));
            currentHealth = hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
        }
    }


    void randomCameraYawRotation(){
        if(doCameraRotation){
            Camera camera = hostScriptReference.getCamera();
            int yaw = ThreadLocalRandom.current().nextInt(0, 361);
            camera.moveYaw(yaw);
            doCameraRotation = false;
            hostScriptReference.log("CAMERA ROTATION: doCameraRotation -> false");
        }
        else{
            //210000ms = 3.5mins
            int delay = (int) Statics.randomNormalDist(210000, 15000);
            hostScriptReference.log("CAMERA ROTATION: doCameraRotation -> true in " + delay/1000 + "s");
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    doCameraRotation = true;
                    hostScriptReference.log("CAMERA ROTATION: doCameraRotation -> true");
                }
            }, delay);
        }

    }

    void openInventoryTab(){
        Tabs tab = hostScriptReference.getTabs();
        if(tab.getOpen() != Tab.INVENTORY){
            tab.open(Tab.INVENTORY);
        }
    }

    private boolean doesPlayerHaveAbsorptionsLeft(){
        Inventory inv = hostScriptReference.getInventory();
        return inv.contains(Statics.ABSORPTION_POTION_1_ID) || inv.contains(Statics.ABSORPTION_POTION_2_ID)
                || inv.contains(Statics.ABSORPTION_POTION_3_ID) || inv.contains(Statics.ABSORPTION_POTION_4_ID);
    }

    boolean doesPlayerHaveOverloadsLeft(){
        Inventory inv = hostScriptReference.getInventory();
        return inv.contains(Statics.OVERLOAD_POTION_1_ID) || inv.contains(Statics.OVERLOAD_POTION_2_ID)
                || inv.contains(Statics.OVERLOAD_POTION_3_ID) || inv.contains(Statics.OVERLOAD_POTION_4_ID);
    }

    private boolean doesPlayerHaveSuperRangePotsLeft(){
        Inventory inv = hostScriptReference.getInventory();
        return inv.contains(Statics.SUPER_RANGING_1_ID) || inv.contains(Statics.SUPER_RANGING_2_ID)
                || inv.contains(Statics.SUPER_RANGING_3_ID) || inv.contains(Statics.SUPER_RANGING_4_ID);
    }

    private int getAbsorptionLvl() {
        RS2Widget widget = hostScriptReference.getWidgets().get(202, 1, 9);
        if(widget != null && widget.isVisible() && widget.getMessage() != null) {
            int absorptionLvl = Integer.parseInt(widget.getMessage().replace(",", ""));
            return Integer.parseInt(widget.getMessage().replace(",", ""));
        }

        return -1; //-1 indicates error
    }

    public void setPlayerDied(boolean playerDied) {
        this.playerDied = playerDied;
    }

    public void setDoOverload(boolean doOverload) {
        this.doOverload = doOverload;
    }
}

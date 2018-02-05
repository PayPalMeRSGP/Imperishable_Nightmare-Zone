package Nodes;

import ScriptClasses.PaintInfo;
import ScriptClasses.PublicStaticFinalConstants;
import org.osbot.rs07.api.Camera;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.Prayer;
import org.osbot.rs07.api.Tabs;
import org.osbot.rs07.api.ui.*;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

/*
    ActiveNode node player flicks. It emulates a player more actively checking to ensure that his account is taking as minimum absorption damage as possible
*/

public class ActiveNode implements ExecutableNode {
    private final static String DRINK = "Drink";
    private final static String GUZZLE = "Guzzle";
    private Script hostScriptReference;
    private int absorptionMinLimit; //determines when to re-pot absorptions
    private int potionMinBoost; //if using super ranging, determines when to re-pot

    //flags to do certain actions
    private boolean playerDied;
    private boolean doOverload; //Prep Node handles initial overload, therefore do not need a need initially
    private boolean doPrayerFlick = true;
    private boolean doCameraRotation;

    private static ExecutableNode singleton = null;

    private ActiveNode(Script hostScriptReference){
        this.hostScriptReference = hostScriptReference;
        this.absorptionMinLimit = absorptionMinLimit = ThreadLocalRandom.current().nextInt(100, 250);
        this.potionMinBoost = ThreadLocalRandom.current().nextInt(3, 7); //generate potion min boost, used to determine next re-pot
        this.doOverload = true;
    }

    public static ExecutableNode getSingleton(Script hostScriptReference) {
        if(singleton == null){
            singleton = new ActiveNode(hostScriptReference);
        }
        return singleton;
    }

    @Override
    public int executeNodeAction() throws InterruptedException {
        boolean drankAbsorptions = handleAbsorptionLvl();
        boolean drankPotions = handlePotionsAndHP();
        if(!doPrayerFlick){
            hostScriptReference.getMouse().moveOutsideScreen();
        }

        if(!drankAbsorptions && !drankPotions){ //we did not need to drink an absorption or a potion then we are still afking
            PaintInfo.getSingleton(hostScriptReference).setCurrentScriptStatus(PaintInfo.ScriptStatus.AFKING);
        }
        rapidHealFlick(); //even though the onloop sleep time is ~1s, rapid heal only flicks if doPrayerFlick variable is true, else it does nothing
        PaintInfo.getSingleton(hostScriptReference).setCurrentScriptStatus(PaintInfo.ScriptStatus.AFKING);
        return (int) PublicStaticFinalConstants.randomNormalDist(1000, 400);
    }

    private boolean handleAbsorptionLvl() throws InterruptedException {
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
            while(absorptionLvl < 300 && doesPlayerHaveAbsorptionsLeft()){
                inv.interact(DRINK, PublicStaticFinalConstants.ABSORPTION_POTION_1_ID, PublicStaticFinalConstants.ABSORPTION_POTION_2_ID,
                        PublicStaticFinalConstants.ABSORPTION_POTION_3_ID, PublicStaticFinalConstants.ABSORPTION_POTION_4_ID);
                absorptionLvl = getAbsorptionLvl();
                MethodProvider.sleep(PublicStaticFinalConstants.randomNormalDist(PublicStaticFinalConstants.RS_GAME_TICK_MS, 60.0));
            }
            this.absorptionMinLimit = ThreadLocalRandom.current().nextInt(100, 250);
            return true;
        }
        return false;
    }

    private boolean handlePotionsAndHP() throws InterruptedException {
        openInventoryTab();
        int currentHealth = hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
        if(currentHealth > 1){
            handleOverload();
            guzzleRockCakeTo1();
            return true;
        }
        return false;

    }

    private void rapidHealFlick() throws InterruptedException {
        if(doPrayerFlick){
            PaintInfo.getSingleton(hostScriptReference).setCurrentScriptStatus(PaintInfo.ScriptStatus.RAPID_HEAL_FLICK);
            int currentHealth = hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
            //(currentHealth <= 49 || !doesPlayerHaveOverloadsLeft()) player still guzzles to 1 if over 49 and overloads are not in inventory
            if(currentHealth > 1 && (currentHealth <= 49 || !doesPlayerHaveOverloadsLeft())){
                guzzleRockCakeTo1();
            }

            if(currentHealth == 1){
                int currentPrayerPts = hostScriptReference.getSkills().getDynamic(Skill.PRAYER);
                if(currentPrayerPts > 0){
                    //hostScriptReference.log("PRAYER FLICK: flicking prayer, doPrayerFlick -> false");
                    Prayer prayer = hostScriptReference.getPrayer();
                    prayer.open();
                    prayer.set(PrayerButton.RAPID_HEAL, true);
                    MethodProvider.sleep(PublicStaticFinalConstants.randomNormalDist(1000, 200));
                    prayer.set(PrayerButton.RAPID_HEAL, false);
                }
            }
            doPrayerFlick = false;
            //schedule a thread to flip doPrayerFlip to true after ~40s
            int delay = (int) PublicStaticFinalConstants.randomNormalDist(40000, 3000);
            //hostScriptReference.log("PRAYER FLICK: doPrayerFlick -> true in " + delay/1000 + "s");
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    doPrayerFlick = true;
                    //hostScriptReference.log("PRAYER FLICK: doPrayerFlick -> true");
                }
            }, delay);
        }
        openInventoryTab();
        hostScriptReference.getMouse().moveOutsideScreen();
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


    private void randomCameraYawRotation(){
        if(doCameraRotation){
            Camera camera = hostScriptReference.getCamera();
            int yaw = ThreadLocalRandom.current().nextInt(0, 361);
            camera.moveYaw(yaw);
            doCameraRotation = false;
            hostScriptReference.log("CAMERA ROTATION: doCameraRotation -> false");
        }
        else{
            //210000ms = 3.5mins
            int delay = (int) PublicStaticFinalConstants.randomNormalDist(210000, 15000);
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

        return -1; //-1 indicates error
    }

    public void setPlayerDied(boolean playerDied) {
        this.playerDied = playerDied;
    }

    public void setDoOverload(boolean doOverload) {
        this.doOverload = doOverload;
    }
}

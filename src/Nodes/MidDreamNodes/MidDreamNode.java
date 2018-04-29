package Nodes.MidDreamNodes;

import ScriptClasses.MarkovNodeExecutor;
import ScriptClasses.Paint.ScriptStatusPainter;
import ScriptClasses.Util.Statics;
import org.osbot.rs07.api.*;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.ui.*;
import org.osbot.rs07.event.WalkingEvent;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public abstract class MidDreamNode implements MarkovNodeExecutor.ExecutableNode {
    final static String DRINK = "Drink";
    final static String GUZZLE = "Guzzle";
    final static String FEEL = "Feel";

    final Script hostScriptReference;
    private int absorptionMinLimit; //determines when to re-pot absorptions
    private int potionMinBoost; //if using super ranging, determines when to re-pot

    //flags to do certain actions
    private boolean playerDied;
    private boolean doOverload;
    private boolean doCameraRotation;
    private boolean noPrayer;
    boolean powerSurgeActive;

    //onLoop calls before switching AFK_NODE <-> Active
    int onLoopsB4Switch;

    MidDreamNode(Script hostScriptReference){
        this.hostScriptReference = hostScriptReference;
        this.absorptionMinLimit =  ThreadLocalRandom.current().nextInt(200, 400);
        this.potionMinBoost = ThreadLocalRandom.current().nextInt(3, 7); //generate potion min boost, used to determine next re-pot
        this.doOverload = true;
        this.noPrayer = false;
    }

    void overloadFailSafe(){
        if(!doOverload && hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS) > 50){ //redundant check as onMessage can rarely not work.
            doOverload = true;
            hostScriptReference.log("redundant check set doOverload -> true");
        }
    }

    void checkAbsorption() throws InterruptedException {
        Inventory inv = hostScriptReference.getInventory();
        int absorptionLvl = getAbsorptionLvl();
        if(absorptionLvl < 0){
            RS2Widget absorptionWidget = hostScriptReference.getWidgets().get(202, 1, 9);
            if(absorptionWidget != null){
                boolean absorptionsVisable = absorptionWidget.isVisible();
                if(!absorptionsVisable && playerDied){
                    hostScriptReference.log("absorptions widget is invisible, likely outside dream, stopping");
                    hostScriptReference.stop(false);
                }
            }
        }
        //absorptionLvl >= 0 is because getAbsorptionLvl returns -1 in error cases, such as the widget not being visible.
        if(absorptionLvl < absorptionMinLimit && absorptionLvl >= -1){
            ScriptStatusPainter.setCurrentScriptStatus(ScriptStatusPainter.ScriptStatus.ABSORPTIONS);
            openInventoryTab();
            while(absorptionLvl < absorptionMinLimit && doesPlayerHaveAbsorptionsLeft()){
                inv.interact(DRINK, Statics.ABSORPTION_POTION_1_ID, Statics.ABSORPTION_POTION_2_ID,
                        Statics.ABSORPTION_POTION_3_ID, Statics.ABSORPTION_POTION_4_ID);
                MethodProvider.sleep(Statics.randomNormalDist(Statics.RS_GAME_TICK_MS, 60.0));
                absorptionLvl = getAbsorptionLvl();

            }
            if(noPrayer){
                this.absorptionMinLimit = ThreadLocalRandom.current().nextInt(400, 600);
            }
            else{
                this.absorptionMinLimit = ThreadLocalRandom.current().nextInt(200, 400);
            }
        }
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

    boolean checkOverload() {
        boolean interacted = false;
        if(doOverload && doesPlayerHaveOverloadsLeft()){
            ScriptStatusPainter.setCurrentScriptStatus(ScriptStatusPainter.ScriptStatus.OVERLOADING);
            //while hp is being depleted from overload it is possible to lose alot of absorptions
            Prayer prayer = Statics.hostScriptReference.getPrayer();
            int currentPrayerPts = hostScriptReference.getSkills().getDynamic(Skill.PRAYER);
            boolean didMeleePrayer = false;
            if(currentPrayerPts > 0){
                prayer.open();
                prayer.set(PrayerButton.PROTECT_FROM_MELEE, true);
                didMeleePrayer = true;
                noPrayer = false;
            }
            else{
                noPrayer = true;
            }
            openInventoryTab();
            Inventory inv = hostScriptReference.getInventory();
            interacted = inv.interact(DRINK, Statics.OVERLOAD_POTION_1_ID, Statics.OVERLOAD_POTION_2_ID,
                    Statics.OVERLOAD_POTION_3_ID, Statics.OVERLOAD_POTION_4_ID);

            if(interacted){
                ScriptStatusPainter.startOverloadTimer();
            }

            int startingHealth = hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
            int estimatedHealthAfterOverload = startingHealth - 51;
            new ConditionalSleep(7000, 500){
                @Override
                public boolean condition() {
                    int currentHealth = hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
                    int difference = Math.abs(estimatedHealthAfterOverload - currentHealth);
                    return difference < 5;
                }
            }.sleep();
            if(didMeleePrayer){
                prayer.set(PrayerButton.PROTECT_FROM_MELEE, false);
            }
            doOverload = false;
            hostScriptReference.log("doOverload -> false (drank overload)");
        }
        return interacted;
    }

    void decreaseHP() throws InterruptedException {
        int currentHealth = hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
        if(currentHealth > 50 || doOverload){
            return;
        }
        //do not reduce hp if overload is about to run out. Hp reduction and overload restore may happen similtaneously, resulting in going under 50hp
        while(currentHealth > 1 && ScriptStatusPainter.getOverloadSecondsLeft() > 5){
            ScriptStatusPainter.setCurrentScriptStatus(ScriptStatusPainter.ScriptStatus.GUZZLING_ROCKCAKES);
            Inventory inv = hostScriptReference.getInventory();

            if(inv.contains(Statics.DWARVEN_ROCK_CAKE_ID)){
                inv.interact(GUZZLE, Statics.DWARVEN_ROCK_CAKE_ID);
            }
            else if(inv.contains(Statics.LOCATOR_ORB_ID)){
                inv.interact(FEEL, Statics.LOCATOR_ORB_ID);
            }
            else{
                hostScriptReference.log("locator orb or rock cake not found");
                hostScriptReference.stop(false);
            }
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
            //1200000ms = 20mins
            int delay = (int) Statics.randomNormalDist(1200000, 300000);
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

    @SuppressWarnings("EmptyMethod")
    void handleSpecialAttack() {

    }

    private boolean walkToCorner() {
        int corner = ThreadLocalRandom.current().nextInt(0, 4);
        WalkingEvent walk;
        switch(corner){
            case 0: //SE corner
                walk = setUpWalker(63, 48);
                break;
            case 1: //SW corner
                walk = setUpWalker(32, 48);
                break;
            case 2: //NW corner
                walk = setUpWalker(32, 48);
                break;
            case 3: //NE corner
                walk = setUpWalker(32, 48);
                break;
            default:
                throw new UnsupportedOperationException("hit default in walkToCorner");
        }
        if(walk != null){
            hostScriptReference.execute(walk);
            final boolean[] finished = new boolean[1];
            new ConditionalSleep(20000) {
                @Override
                public boolean condition() {
                    finished[0] = walk.hasFinished();
                    return finished[0];
                }
            }.sleep();
            return finished[0];
        }
        return false;
    }

    private WalkingEvent setUpWalker(int localX, int localY){
        int actualX = hostScriptReference.getMap().getBaseX() + localX;
        int actualY = hostScriptReference.getMap().getBaseY() + localY;
        int z = hostScriptReference.myPlayer().getPosition().getZ();
        WalkingEvent walk = new WalkingEvent(new Position(actualX, actualY, z));
        walk.setMiniMapDistanceThreshold(5);
        walk.setOperateCamera(true);
        walk.setMinDistanceThreshold(0);
        return walk;
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

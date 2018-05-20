package Nodes.CheatCaveNodes;

import ScriptClasses.MarkovNodeExecutor;
import ScriptClasses.Paint.ScriptStatusPainter;
import ScriptClasses.Util.Statics;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.Prayer;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.PrayerButton;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.event.WalkingEvent;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.concurrent.ThreadLocalRandom;

import static ScriptClasses.Util.Statics.*;

public abstract class MidDreamNode implements MarkovNodeExecutor.ExecutableNode {
    final Script script;
    private int absorptionMinLimit; //determines when to re-pot absorptions
    //private int potionMinBoost; //if using super ranging/mage, determines when to re-pot

    boolean noPrayer;
    boolean doOverload;
    boolean powerSurgeActive;

    //onLoop calls before switching AFK <-> Active
    int onLoopsB4Switch;
    /*
    totalLoops determines how many onLoop calls a cycle of Active and AFK nodes take.
    totalLoops * activeNodeUsagePercent = ActiveNode's onLoopsB4Switch to AFKNode
    totalLoops * (1 - activeNodeUsagePercent) = AFKNode's onLoopsB4Switch to ActiveNode
     */
    static int totalLoops;
    double activeNodeUsagePercent;

    MidDreamNode(Script script){
        this.script = script;
        this.absorptionMinLimit =  ThreadLocalRandom.current().nextInt(200, 400);
        //this.potionMinBoost = ThreadLocalRandom.current().nextInt(3, 7);
        this.doOverload = true;
        this.noPrayer = false;
        this.activeNodeUsagePercent = 0.70;
    }

    MidDreamNode(Script script, double activeNodeUsagePercent){
        this.script = script;
        this.absorptionMinLimit =  ThreadLocalRandom.current().nextInt(200, 400);
        //this.potionMinBoost = ThreadLocalRandom.current().nextInt(3, 7);
        this.doOverload = true;
        this.noPrayer = false;
        this.activeNodeUsagePercent = activeNodeUsagePercent;
    }

    @Override
    public final boolean canExecute() {
        RS2Object exitPotion = script.getObjects().closestThatContains("Potion");
        return exitPotion != null && exitPotion.getLocalX() == 52 && exitPotion.getLocalY() == 47;
    }

    public abstract void resumeNode();

    static void randomizeTotalLoops(){
        MidDreamNode.totalLoops = (int) Statics.randomNormalDist(1000, 500);
        staticScriptRef.log("Active -> AFK totalLoops: " + totalLoops);
    }

    void checkAbsorption() throws InterruptedException {
        Inventory inv = script.getInventory();
        int absorptionLvl = getAbsorptionLvl();
        script.log("absorptions: " + absorptionLvl);
        if(absorptionLvl < absorptionMinLimit){
            ScriptStatusPainter.setCurrentScriptStatus(ScriptStatusPainter.ScriptStatus.ABSORPTIONS);
            script.getTabs().open(Tab.INVENTORY);
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

   /* boolean drinkSuperRangingPotion(){
        script.getTabs().open(Tab.INVENTORY);
        int currentRangeBoost = script.getSkills().getDynamic(Skill.RANGED) - script.getSkills().getStatic(Skill.RANGED);
        if(doesPlayerHaveSuperRangePotsLeft() && currentRangeBoost < potionMinBoost){
            Inventory inv = script.getInventory();
            this.potionMinBoost = ThreadLocalRandom.current().nextInt(3, 7);
            return inv.interact(DRINK, Statics.SUPER_RANGING_4_ID, Statics.SUPER_RANGING_3_ID,
                    Statics.SUPER_RANGING_2_ID, Statics.SUPER_RANGING_1_ID);
        }
        return false;
    }*/

    boolean checkOverload() {
        boolean interacted = false;
        if((doOverload || script.getSkills().getDynamic(Skill.HITPOINTS) > 50) && doesPlayerHaveOverloadsLeft()){
            ScriptStatusPainter.setCurrentScriptStatus(ScriptStatusPainter.ScriptStatus.OVERLOADING);
            //while hp is being depleted from overload it is possible to lose alot of absorptions
            Prayer prayer = Statics.staticScriptRef.getPrayer();
            int currentPrayerPts = script.getSkills().getDynamic(Skill.PRAYER);
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
            script.getTabs().open(Tab.INVENTORY);
            Inventory inv = script.getInventory();
            interacted = inv.interact(DRINK, Statics.OVERLOAD_POTION_1_ID, Statics.OVERLOAD_POTION_2_ID,
                    Statics.OVERLOAD_POTION_3_ID, Statics.OVERLOAD_POTION_4_ID);

            if(interacted){
                ScriptStatusPainter.startOverloadTimer();
            }

            int startingHealth = script.getSkills().getDynamic(Skill.HITPOINTS);
            int estimatedHealthAfterOverload = startingHealth - 51;
            new ConditionalSleep(7000, 500){
                @Override
                public boolean condition() {
                    int currentHealth = script.getSkills().getDynamic(Skill.HITPOINTS);
                    int difference = Math.abs(estimatedHealthAfterOverload - currentHealth);
                    return difference < 5;
                }
            }.sleep();
            if(didMeleePrayer){
                prayer.set(PrayerButton.PROTECT_FROM_MELEE, false);
            }
            doOverload = false;
            script.log("doOverload -> false (drank overload)");
        }
        return interacted;
    }

    void decreaseHP() throws InterruptedException {
        int currentHealth = script.getSkills().getDynamic(Skill.HITPOINTS);
        if(currentHealth > 50 || doOverload){
            return;
        }
        //do not reduce hp if overload is about to run out. Hp reduction and overload restore may happen similtaneously, resulting in going under 50hp
        while(currentHealth > 1 && ScriptStatusPainter.getOverloadSecondsLeft() > 5){
            ScriptStatusPainter.setCurrentScriptStatus(ScriptStatusPainter.ScriptStatus.GUZZLING_ROCKCAKES);
            Inventory inv = script.getInventory();

            if(inv.contains(Statics.ROCK_CAKE_ID)){
                inv.interact(GUZZLE, Statics.ROCK_CAKE_ID);
            }
            else if(inv.contains(Statics.LOCATOR_ORB_ID)){
                inv.interact(FEEL, Statics.LOCATOR_ORB_ID);
            }
            else{
                script.log("locator orb or rock cake not found");
                script.stop(false);
            }
            MethodProvider.sleep(Statics.randomNormalDist(Statics.RS_GAME_TICK_MS, 60.0));
            currentHealth = script.getSkills().getDynamic(Skill.HITPOINTS);
        }
    }

    private boolean walkToCorner() {
        boolean useSECorner = ThreadLocalRandom.current().nextBoolean();
        WalkingEvent walk;
        if(useSECorner)
            walk = setUpWalker(63);
        else
            walk = setUpWalker(32);

        script.execute(walk);
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

    private WalkingEvent setUpWalker(int localX){
        int actualX = script.getMap().getBaseX() + localX;
        int actualY = script.getMap().getBaseY() + 48;
        int z = script.myPlayer().getPosition().getZ();
        WalkingEvent walk = new WalkingEvent(new Position(actualX, actualY, z));
        walk.setMiniMapDistanceThreshold(5);
        walk.setOperateCamera(true);
        walk.setMinDistanceThreshold(0);
        return walk;
    }

    private boolean doesPlayerHaveAbsorptionsLeft(){
        Inventory inv = script.getInventory();
        return inv.contains(Statics.ABSORPTION_POTION_1_ID) || inv.contains(Statics.ABSORPTION_POTION_2_ID)
                || inv.contains(Statics.ABSORPTION_POTION_3_ID) || inv.contains(Statics.ABSORPTION_POTION_4_ID);
    }

    boolean doesPlayerHaveOverloadsLeft(){
        Inventory inv = script.getInventory();
        return inv.contains(Statics.OVERLOAD_POTION_1_ID) || inv.contains(Statics.OVERLOAD_POTION_2_ID)
                || inv.contains(Statics.OVERLOAD_POTION_3_ID) || inv.contains(Statics.OVERLOAD_POTION_4_ID);
    }

    private boolean doesPlayerHaveSuperRangePotsLeft(){
        Inventory inv = script.getInventory();
        return inv.contains(Statics.SUPER_RANGING_1_ID) || inv.contains(Statics.SUPER_RANGING_2_ID)
                || inv.contains(Statics.SUPER_RANGING_3_ID) || inv.contains(Statics.SUPER_RANGING_4_ID);
    }

    private int getAbsorptionLvl() {
        RS2Widget widget = script.getWidgets().get(202, 1, 9);
        if(widget != null && widget.isVisible() && widget.getMessage() != null) {
            int absorptionLvl = Integer.parseInt(widget.getMessage().replace(",", ""));
            return Integer.parseInt(widget.getMessage().replace(",", ""));
        }

        return -1; //-1 indicates error
    }

    public void setDoOverload(boolean doOverload) {
        this.doOverload = doOverload;
    }

}

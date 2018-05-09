package Nodes.MidDreamNodes;

import ScriptClasses.MarkovNodeExecutor;
import ScriptClasses.Paint.ScriptStatusPainter;
import ScriptClasses.Util.Statics;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.event.WalkingEvent;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.concurrent.ThreadLocalRandom;

import static ScriptClasses.Util.Statics.DRINK;
import static ScriptClasses.Util.Statics.FEEL;
import static ScriptClasses.Util.Statics.GUZZLE;

public class PrepNode implements MarkovNodeExecutor.ExecutableNode{
    private static MarkovNodeExecutor.ExecutableNode singleton = null;
    private Script script;

    private PrepNode(Script script){
        this.script = script;
    }

    public static MarkovNodeExecutor.ExecutableNode getSingleton(Script script) {
        if(singleton == null){
            singleton = new PrepNode(script);
        }
        return singleton;
    }

    @Override
    public int executeNode() throws InterruptedException {
        ScriptStatusPainter.setCurrentScriptStatus(ScriptStatusPainter.ScriptStatus.PREPARING);
        ScriptStatusPainter.setCurrentMarkovStatus(ScriptStatusPainter.MarkovStatus.PREP_NODE);
        if(walkToCorner()){
            absorptions();
            hitpoints();
            autoRetaliate();
        }

        return 500;
    }

    @Override
    public boolean doConditionalTraverse() {
        return false;
    }

    @Override
    public void resumeNode(int onLoopsB4Switch) {
    }

    private boolean walkToCorner() {
        int corner = ThreadLocalRandom.current().nextInt(0, 2);
        WalkingEvent walk;
        switch(corner){
            case 0: //SE corner
                walk = walkHelper(63);
                break;
            case 1: //SW corner
                walk = walkHelper(32);
                break;
            case 2: //NW corner
                walk = walkHelper(32);
                break;
            case 3: //NE corner
                walk = walkHelper(32);
                break;
            default:
                throw new UnsupportedOperationException("hit default in walkToCorner");
        }
        if(walk != null){
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
        return false;
    }

    private WalkingEvent walkHelper(int localX){
        int actualX = script.getMap().getBaseX() + localX;
        int actualY = script.getMap().getBaseY() + 48;
        int z = script.myPlayer().getPosition().getZ();
        WalkingEvent walk = new WalkingEvent(new Position(actualX, actualY, z));
        walk.setMiniMapDistanceThreshold(5);
        walk.setOperateCamera(true);
        walk.setMinDistanceThreshold(0);
        return walk;
    }

    private void hitpoints() throws InterruptedException {
        int currentHealth = script.getSkills().getDynamic(Skill.HITPOINTS);

        Inventory inv = script.getInventory();
        if(currentHealth > 50){
            inv.interact(Statics.DRINK, Statics.OVERLOAD_POTION_1_ID, Statics.OVERLOAD_POTION_2_ID,
                    Statics.OVERLOAD_POTION_3_ID, Statics.OVERLOAD_POTION_4_ID);
            ScriptStatusPainter.startOverloadTimer();
            //wait out overload dmg, DO NOT GUZZLE while taking overload dmg, may result in overload dmg player killing player.
            int estimatedHealthAfterOverload = currentHealth - 51;
            new ConditionalSleep(7000, 500){
                @Override
                public boolean condition() {
                    int currentHealth = script.getSkills().getDynamic(Skill.HITPOINTS);
                    int difference = Math.abs(estimatedHealthAfterOverload - currentHealth);
                    return difference < 5;
                }
            }.sleep();
        }
        while(currentHealth > 1){
            ScriptStatusPainter.setCurrentScriptStatus(ScriptStatusPainter.ScriptStatus.GUZZLING_ROCKCAKES);
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

    private void absorptions() throws InterruptedException {
        int absorptionLvl = getAbsorptionLvl();
        while(absorptionLvl < 400 && doesPlayerHaveAbsorptionsLeft()){
            script.getInventory().interact(DRINK, Statics.ABSORPTION_POTION_1_ID, Statics.ABSORPTION_POTION_2_ID,
                    Statics.ABSORPTION_POTION_3_ID, Statics.ABSORPTION_POTION_4_ID);
            MethodProvider.sleep(Statics.randomNormalDist(Statics.RS_GAME_TICK_MS, 60.0));
            absorptionLvl = getAbsorptionLvl();

        }
    }

    private int getAbsorptionLvl() {
        RS2Widget widget = script.getWidgets().get(202, 1, 9);
        if(widget != null && widget.isVisible() && widget.getMessage() != null) {
            int absorptionLvl = Integer.parseInt(widget.getMessage().replace(",", ""));
            return Integer.parseInt(widget.getMessage().replace(",", ""));
        }

        return -1; //-1 indicates error
    }

    private boolean doesPlayerHaveAbsorptionsLeft(){
        Inventory inv = script.getInventory();
        return inv.contains(Statics.ABSORPTION_POTION_1_ID) || inv.contains(Statics.ABSORPTION_POTION_2_ID)
                || inv.contains(Statics.ABSORPTION_POTION_3_ID) || inv.contains(Statics.ABSORPTION_POTION_4_ID);
    }

    private void autoRetaliate(){
        script.getTabs().open(Tab.ATTACK);
        script.getCombat().toggleAutoRetaliate(true);
        script.getTabs().open(Tab.INVENTORY);
    }
}

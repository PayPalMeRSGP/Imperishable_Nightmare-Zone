package Nodes.MidDreamNodes;

import ScriptClasses.MarkovNodeExecutor;
import ScriptClasses.Paint.ScriptStatusPainter;
import ScriptClasses.Util.Statics;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.Menu;
import org.osbot.rs07.api.Mouse;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.event.WalkingEvent;
import org.osbot.rs07.input.mouse.InventorySlotDestination;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.concurrent.ThreadLocalRandom;

public class PrepNode extends MidDreamNode {
    private static MarkovNodeExecutor.ExecutableNode singleton = null;

    private PrepNode(Script hostScriptReference) {
        super(hostScriptReference);
    }

    public static MarkovNodeExecutor.ExecutableNode getSingleton(Script hostScriptReference) {
        if(singleton == null){
            singleton = new PrepNode(hostScriptReference);
        }
        return singleton;
    }

    @Override
    public int executeNode() throws InterruptedException {
        ScriptStatusPainter.setCurrentScriptStatus(ScriptStatusPainter.ScriptStatus.PREPARING);
        ScriptStatusPainter.setCurrentMarkovStatus(ScriptStatusPainter.MarkovStatus.PREP);
        setDoOverload(true);
        if(walkToCorner()){
            handleAbsorptionLvl();
            setPlayerHealthTo1();
            turnOnAutoRetaliate();
        }

        return 1000;
    }

    @Override
    public boolean doConditionalTraverse() {
        return false;
    }

    private boolean walkToCorner() {
        int corner = ThreadLocalRandom.current().nextInt(0, 2);
        WalkingEvent walk;
        switch(corner){
            case 0: //SE corner
                walk = setUpWalker(63);
                break;
            case 1: //SW corner
                walk = setUpWalker(32);
                break;
            case 2: //NW corner
                walk = setUpWalker(32);
                break;
            case 3: //NE corner
                walk = setUpWalker(32);
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

    private WalkingEvent setUpWalker(int localX){
        int actualX = hostScriptReference.getMap().getBaseX() + localX;
        int actualY = hostScriptReference.getMap().getBaseY() + 48;
        int z = hostScriptReference.myPlayer().getPosition().getZ();
        WalkingEvent walk = new WalkingEvent(new Position(actualX, actualY, z));
        walk.setMiniMapDistanceThreshold(5);
        walk.setOperateCamera(true);
        walk.setMinDistanceThreshold(0);
        return walk;
    }

    private void setPlayerHealthTo1() throws InterruptedException {
        int currentHealth = hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);

        Inventory inv = hostScriptReference.getInventory();
        if(currentHealth > 50 && doesPlayerHaveOverloadsLeft()){
            inv.interact(Statics.DRINK, Statics.OVERLOAD_POTION_1_ID, Statics.OVERLOAD_POTION_2_ID,
                    Statics.OVERLOAD_POTION_3_ID, Statics.OVERLOAD_POTION_4_ID);
            ScriptStatusPainter.startOverloadTimer();
            //wait out overload dmg, DO NOT GUZZLE while taking overload dmg, may result in overload dmg player killing player.
            int estimatedHealthAfterOverload = currentHealth - 51;
            new ConditionalSleep(7000, 500){
                @Override
                public boolean condition() {
                    int currentHealth = hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
                    int difference = Math.abs(estimatedHealthAfterOverload - currentHealth);
                    return difference < 5;
                }
            }.sleep();
        }
        while(currentHealth > 1){
            ScriptStatusPainter.setCurrentScriptStatus(ScriptStatusPainter.ScriptStatus.GUZZLING_ROCKCAKES);
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

    private void guzzleRockCake(){
        Inventory inv = hostScriptReference.getInventory();
        Mouse mouse = hostScriptReference.getMouse();
        Menu rockCakeMenu = hostScriptReference.getMenuAPI();
        if(inv.contains(Statics.DWARVEN_ROCK_CAKE_ID)){
            int rockCakeInvSlot = inv.getSlot(Statics.DWARVEN_ROCK_CAKE_ID);
            InventorySlotDestination rockCakeDest = new InventorySlotDestination(hostScriptReference.getBot(), rockCakeInvSlot);
            mouse.click(rockCakeDest, true);
            if(rockCakeMenu.isOpen()){
                rockCakeMenu.selectAction(Statics.GUZZLE);
            }
        }

    }

    private void turnOnAutoRetaliate(){
        hostScriptReference.getTabs().open(Tab.ATTACK);
        hostScriptReference.getCombat().toggleAutoRetaliate(true);
        hostScriptReference.getTabs().open(Tab.INVENTORY);
    }
}

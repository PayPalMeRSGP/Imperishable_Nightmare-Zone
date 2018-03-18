package Nodes.MidDreamNodes;

import Nodes.ExecutableNode;
import ScriptClasses.Paint.CombatXPPainter;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.event.WalkingEvent;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.concurrent.ThreadLocalRandom;

public class PrepNode extends MidDreamNode {
    private Script hostScriptReference;

    private static ExecutableNode singleton = null;

    private PrepNode(Script hostScriptReference) {
        super(hostScriptReference);
    }

    public static ExecutableNode getSingleton(Script hostScriptReference) {
        if(singleton == null){
            singleton = new PrepNode(hostScriptReference);
        }
        return singleton;
    }

    @Override
    public int executeNodeAction() throws InterruptedException {
        CombatXPPainter.getSingleton(hostScriptReference).setCurrentScriptStatus(CombatXPPainter.ScriptStatus.PREPARING);
        if(walkToCorner()){
            handleAbsorptionLvl();
            handleOverload();
            guzzleRockCakeTo1();
            turnOnAutoRetaliate();
        }

        return 1000;
    }

    private boolean walkToCorner() throws InterruptedException {
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
                public boolean condition() throws InterruptedException {
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


    private void turnOnAutoRetaliate(){
        hostScriptReference.getTabs().open(Tab.ATTACK);
        hostScriptReference.getCombat().toggleAutoRetaliate(true);
        hostScriptReference.getTabs().open(Tab.INVENTORY);
    }
}

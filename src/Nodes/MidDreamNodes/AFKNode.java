package Nodes.MidDreamNodes;

import Nodes.ExecutableNode;
import ScriptClasses.Paint.PaintInfo;
import ScriptClasses.Statics;
import org.osbot.rs07.api.ui.*;
import org.osbot.rs07.script.Script;

import java.util.concurrent.ThreadLocalRandom;
/*
    AFK node does not flick prayer to prevent health regen
    This Node seeks to emulate a human player leaving his account afk in nmz thereby letting his hp regen up to a random amount (hpRegenLimit)
    then coming back and guzzling rockcakes back to 1 hp before afking again.

*/
public class AFKNode extends MidDreamNode {
    private static ExecutableNode singleton = null;

    private int hpMaxLimit;

    private AFKNode(Script hostScriptReference){
        super(hostScriptReference);
        this.hpMaxLimit = ThreadLocalRandom.current().nextInt(2, 5);
    }

    public static ExecutableNode getSingleton(Script hostScriptReference) {
        if(singleton == null){
            singleton = new AFKNode(hostScriptReference);
        }
        return singleton;
    }

    @Override
    public int executeNodeAction() throws InterruptedException {
        overloadFailSafe();

        if(hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS) > hpMaxLimit){
            handleOverload();
            guzzleRockCakeTo1();
        }
        hostScriptReference.getMouse().moveOutsideScreen();
        PaintInfo.getSingleton(hostScriptReference).setCurrentScriptStatus(PaintInfo.ScriptStatus.AFKING);
        return (int) Statics.randomNormalDist(2000, 1000);
    }

}


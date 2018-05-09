package Nodes.MidDreamNodes;

import ScriptClasses.MarkovNodeExecutor;
import ScriptClasses.Paint.ScriptStatusPainter;
import ScriptClasses.Util.Statics;
import org.osbot.rs07.api.ui.*;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;

import java.util.concurrent.ThreadLocalRandom;
/*
    AFK_NODE node does not flick prayer to prevent health regen
    This Node seeks to emulate a human player leaving his account afk in nmz thereby letting his hp regen up to a random amount (hpRegenLimit)
    then coming back and guzzling rockcakes back to 1 hp before afking again.

*/
public class AFKNode extends MidDreamNode {
    private static MarkovNodeExecutor.ExecutableNode singleton = null;

    private int hpMaxLimit;

    private AFKNode(Script hostScriptReference){
        super(hostScriptReference);
        this.hpMaxLimit = ThreadLocalRandom.current().nextInt(2, 5);
    }

    @Override
    public void resumeNode(int onLoopsB4Switch) {
        script.log("switching to AFK Node");
        this.onLoopsB4Switch = onLoopsB4Switch;
    }

    public static MarkovNodeExecutor.ExecutableNode getSingleton(Script hostScriptReference) {
        if(singleton == null){
            singleton = new AFKNode(hostScriptReference);
        }
        return singleton;
    }

    @Override
    public int executeNode() throws InterruptedException {
        ScriptStatusPainter.setCurrentMarkovStatus(ScriptStatusPainter.MarkovStatus.AFK_NODE);

        if(script.getSkills().getDynamic(Skill.HITPOINTS) > hpMaxLimit){
            checkOverload();
            decreaseHP();
            this.hpMaxLimit = ThreadLocalRandom.current().nextInt(2, 5);
        }
        script.getMouse().moveOutsideScreen();
        ScriptStatusPainter.setCurrentScriptStatus(ScriptStatusPainter.ScriptStatus.AFKING);
        onLoopsB4Switch--;
        ScriptStatusPainter.setOnLoopsB4Switch(onLoopsB4Switch);
        MethodProvider.sleep(Statics.randomNormalDist(1000, 500));
        return 500;
    }

    @Override
    public boolean doConditionalTraverse() {
        return onLoopsB4Switch <= 0;
    }

}


package Nodes.MidDreamNodes;

import Nodes.ExecutableNode;
import ScriptClasses.PaintInfo;
import ScriptClasses.Statics;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.Prayer;
import org.osbot.rs07.api.Tabs;
import org.osbot.rs07.api.ui.*;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.concurrent.ThreadLocalRandom;
/*
    AFK node does not flick prayer to prevent health regen
    This Node seeks to emulate a human player leaving his account afk in nmz thereby letting his hp regen up to a random amount (hpRegenLimit)
    then coming back and guzzling rockcakes back to 1 hp before afking again.

*/
public class AFKNode extends MidDreamNode {
    private static ExecutableNode singleton = null;

    private AFKNode(Script hostScriptReference){
        super(hostScriptReference);
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
        boolean drankAbsorptions = handleAbsorptionLvl();
        boolean drankPotions = handlePotionsAndHP();
        hostScriptReference.getMouse().moveOutsideScreen();
        if(!drankAbsorptions && !drankPotions){ //we did not need to drink an absorption or a potion then we are still afking
            PaintInfo.getSingleton(hostScriptReference).setCurrentScriptStatus(PaintInfo.ScriptStatus.AFKING);
        }
        return (int) Statics.randomNormalDist(2000, 1000);
    }

}


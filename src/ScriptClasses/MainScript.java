package ScriptClasses;

import Nodes.CheatCaveNodes.AFKNode;
import Nodes.CheatCaveNodes.ActiveNode;
import Nodes.CheatCaveNodes.PrepNode;
import ScriptClasses.Paint.DraggablePaintHandler;
import ScriptClasses.Paint.CombatXPTracker;
import ScriptClasses.Paint.MyPainter;
import ScriptClasses.Paint.ScriptStatusPainter;
import ScriptClasses.Util.Statics;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.canvas.paint.Painter;
import org.osbot.rs07.listener.MessageListener;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;

@SuppressWarnings("unused")
@ScriptManifest(author = "PayPalMeRSGP", name = MainScript.BUILD_NUM + " " + MainScript.SCRIPT_NAME, info = "NMZ_AFK_ALPHA, start inside dream", version = 0.1, logo = "")
public class MainScript extends Script implements MessageListener {
    static final String SCRIPT_NAME = "Imperishable Nightmare-Zone";
    static final int BUILD_NUM = 8;

    private MarkovNodeExecutor executor;
    private DraggablePaintHandler paintHandler;
    private MyPainter paint;

    @Override
    public void onStart() throws InterruptedException {
        super.onStart();
        getBot().addMessageListener(this);
        paint = new MyPainter(this);
        markovChainSetup();
    }

    @Override
    public int onLoop() throws InterruptedException {
        /*paint.determineCombatStyle();
        return executor.executeNodeThenTraverse();*/
        return 10000;
    }

    private void markovChainSetup(){
        Statics.setHostScriptReference(this);

        PrepNode prepNode = (PrepNode) PrepNode.getSingleton(this);
        AFKNode afkNode = (AFKNode) AFKNode.getSingleton(this);
        ActiveNode activeNode = (ActiveNode) ActiveNode.getSingleton(this);

        executor = new MarkovNodeExecutor(prepNode);
        /*executor.addNormalEdgeToNode(prepNode, afkNode, 1);
        executor.addNormalEdgeToNode(afkNode, afkNode, 1);*/
        executor.addNormalEdgeToNode(prepNode, activeNode, 1);
        executor.addNormalEdgeToNode(activeNode, activeNode, 1);
        executor.addNormalEdgeToNode(afkNode, afkNode, 1);
        executor.addCondEdgeToNode(activeNode, afkNode, 1);
        executor.addCondEdgeToNode(afkNode, activeNode, 1);
    }

    @Override
    public void onMessage(Message message) throws InterruptedException {
        if(message.getType() == Message.MessageType.GAME){
            if(message.getMessage().contains(Statics.OVERLOAD_DEPLETED_MSG)){
                AFKNode afkNode = (AFKNode) AFKNode.getSingleton(this);
                ActiveNode activeNode = (ActiveNode) ActiveNode.getSingleton(this);
                afkNode.setDoOverload(true);
                activeNode.setDoOverload(true);
                this.log("doOverload -> true");
            }
            else if(message.getMessage().contains(Statics.DREAM_OVER_MSG)){
                log("died in NMZ, stopping script");
                AFKNode afkNode = (AFKNode) AFKNode.getSingleton(this);
                ActiveNode activeNode = (ActiveNode) ActiveNode.getSingleton(this);
                afkNode.setPlayerDied(true);
                activeNode.setPlayerDied(true);
                MethodProvider.sleep(5000);
                stop(false);
            }
            else if(message.getMessage().contains("Power surge")){
                log("power surge up");
            }
            else if(message.getMessage().contains("You can only drink this potion")){
                log("outside NMZ");
                stop(false);
            }
        }
    }


}

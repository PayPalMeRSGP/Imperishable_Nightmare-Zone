package ScriptClasses;

import Nodes.CheatCaveNodes.AFKNode;
import Nodes.CheatCaveNodes.ActiveNode;
import Nodes.CheatCaveNodes.PrepNode;
import Nodes.NotInCheatCaves.DreamEntryNode;
import ScriptClasses.Paint.DraggablePaintHandler;
import ScriptClasses.Paint.Paint;
import ScriptClasses.Util.NoSuitableNodesException;
import ScriptClasses.Util.Statics;
import SwingGUI.GUI;
import SwingGUI.GUIResults;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.listener.MessageListener;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@SuppressWarnings("unused")
@ScriptManifest(author = "PayPalMeRSGP", name = MainScript.BUILD_NUM + " " + MainScript.SCRIPT_NAME, info = "NMZ_AFK_ALPHA", version = 0.1, logo = "")
public class MainScript extends Script implements MessageListener {
    public static final String SCRIPT_NAME = "Imperishable Nightmare-Zone";
    static final int BUILD_NUM = 13;

    private MarkovNodeExecutor executor;
    private DraggablePaintHandler paintHandler;
    private Paint paint;
    private GUIResults results;

    @Override
    public void onStart() throws InterruptedException {
        super.onStart();
        getBot().addMessageListener(this);
        paint = new Paint(this);
        markovChainSetup();
    }

    @Override
    public int onLoop() throws InterruptedException {
        paint.determineCombatStyle();
        try {
            return executor.executeThenTraverse();
        } catch (NoSuitableNodesException e) {
            e.printStackTrace();
            stop();
            return 0;
        }
    }

    @Override
    public void onExit() throws InterruptedException {
        super.onExit();
        getPrayer().deactivateAll();
    }

    private void markovChainSetup(){
        Statics.setStaticScriptRef(this);

        if(results.isSet()){
            log(results.toJSON());
            if(results.isPrayerDream()){
                log("TODO: prayer dream");
                stop(false);
            }
            else{
                DreamEntryNode entryNode = (DreamEntryNode) DreamEntryNode.getSingleton(this, results.getNumAbsorptions(), results.getNumOverloads());
                PrepNode prepNode = (PrepNode) PrepNode.getSingleton(this);
                AFKNode afkNode = (AFKNode) AFKNode.getSingleton(this);
                ActiveNode activeNode = (ActiveNode) ActiveNode.getSingleton(this);

                executor = new MarkovNodeExecutor(entryNode);
                executor.addNormalEdgeToNode(entryNode, prepNode, 1);
                executor.addNormalEdgeToNode(prepNode, activeNode, 1);
                executor.addNormalEdgeToNode(activeNode, activeNode, 1);
                executor.addNormalEdgeToNode(afkNode, afkNode, 1);
                executor.addCondEdgeToNode(activeNode, afkNode, 1);
                executor.addCondEdgeToNode(afkNode, activeNode, 1);

            }
        }


    }

    private void openGUI(){
        GUI gui = new GUI(this);
        try{
            while(gui.isGuiActive()){
                sleep(500);
            }
        }
        catch (InterruptedException e){
            log(e.toString());
        }

        results = gui.getResults();
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
            else if(message.getMessage().contains("Power surge")){
                log("power surge up");
            }
            else if(message.getMessage().contains("You can only drink this potion")){
                log("outside NMZ");
            }
        }
    }


}

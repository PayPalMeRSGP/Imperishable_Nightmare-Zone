package ScriptClasses;

import Nodes.MidDreamNodes.AFKNode;
import Nodes.MidDreamNodes.ActiveNode;
import Nodes.MidDreamNodes.PrepNode;
import ScriptClasses.Paint.DraggablePaintHandler;
import ScriptClasses.Paint.CombatXPTracker;
import ScriptClasses.Paint.ScriptStatusPainter;
import ScriptClasses.Util.Statics;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.listener.MessageListener;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;

@ScriptManifest(author = "PayPalMeRSGP", name = MainScript.BUILD_NUM + " " + MainScript.SCRIPT_NAME, info = "NMZ_AFK_ALPHA, start inside dream", version = 0.1, logo = "")
public class MainScript extends Script implements MessageListener {
    static final String SCRIPT_NAME = "Imperishable Nightmare-Zone";
    static final int BUILD_NUM = 11;

    private GraphBasedNodeExecutor executor;
    private DraggablePaintHandler paintHandler;
    private CombatXPTracker tracker;
    private Rectangle paintArea;

    @Override
    public void onStart() throws InterruptedException {
        super.onStart();
        getBot().addMessageListener(this);
        setUp();
    }

    @Override
    public int onLoop() throws InterruptedException {
        tracker.setCombatStyle();
        return executor.executeNodeThenTraverse();
    }

    @Override
    public void onPaint(Graphics2D g) {
        super.onPaint(g);
        paintArea = paintHandler.getPaintArea();
        g.setColor(new Color(156,156,156, 127));
        g.fillRect(paintArea.x, paintArea.y, paintArea.width, paintArea.height);

        if(tracker != null){
            if(tracker.getStyle() != null){
                g.setColor(new Color(255, 255, 255));
                if(tracker.getStyle() == CombatXPTracker.CombatStyle.CTRL){
                    paintArea.setBounds(0, 0, 300, 135);
                    int[] atkStrDef = tracker.getAtkStrDefLvls();
                    g.drawString("ATK LVL: " + atkStrDef[0]
                            + " XP: " + formatValue(tracker.getAtkLvl())
                            + " TTL: " + formatTime(tracker.getAtkTTL())
                            + " XPH: " + formatValue(tracker.getTrainingXPH()),
                            paintArea.x + 10, paintArea.y + 15);

                    g.drawString("STR LVL: " + atkStrDef[1]
                            + " XP: " + formatValue(tracker.getStrLvl())
                            + " TTL: " + formatTime(tracker.getStrTTL()),
                            paintArea.x + 10, paintArea.y + 30);

                    g.drawString("DEF LVL: " + atkStrDef[2]
                            + " XP: " + formatValue(tracker.getTrainingXpGained())
                            + " TTL: " + formatTime(tracker.getDefTTL()),
                            paintArea.x + 10, paintArea.y + 45);

                    g.drawString("HP LVL: " + tracker.getHpLvl()
                            + " XP: " + formatValue(tracker.getHpXpGained())
                            + " TTL: " + formatTime(tracker.getHpTTL())
                            + " XPH: " + formatValue(tracker.getHpXPH()),
                            paintArea.x + 10, paintArea.y + 60);

                    g.drawString("Runtime: " + formatTime(tracker.getRunTime()),
                            paintArea.x + 10, paintArea.y + 75);

                    g.drawString("Status: " + ScriptStatusPainter.getCurrentScriptStatus(),
                            paintArea.x + 10, paintArea.y + 90);

                    g.drawString("Overload Timer: ~" + ScriptStatusPainter.getOverloadSecondsLeft()+"s",
                            paintArea.x + 10, paintArea.y + 105);

                    g.drawString("Prayer Flick Timer: ~" + ScriptStatusPainter.getSecondsTilNextFlick()+"s",
                            paintArea.x + 10, paintArea.y + 120);
                }
                else {
                    g.drawString(tracker.getStyle().toString() + " LVL: " + formatValue(tracker.getTrainingSkillLvl())
                            + " XP: " + formatValue(tracker.getTrainingXpGained())
                            + " TTL: " + formatTime(tracker.getTrainingTTL())
                            + " XPH: " + formatValue(tracker.getTrainingXPH()),
                            paintArea.x + 10, paintArea.y + 15);

                    g.drawString("HP LVL: " + tracker.getHpLvl()
                                    + " XP: " + formatValue(tracker.getHpXpGained())
                                    + " TTL: " + formatTime(tracker.getHpTTL())
                                    + " XPH: " + formatValue(tracker.getHpXPH()),
                            paintArea.x + 10, paintArea.y + 30);

                    g.drawString("Runtime: " + formatTime(tracker.getRunTime()),
                            paintArea.x + 10, paintArea.y + 45);

                    g.drawString("Status: " + ScriptStatusPainter.getCurrentScriptStatus(),
                            paintArea.x + 10, paintArea.y + 60);

                    g.drawString("Overload Timer: ~" + ScriptStatusPainter.getOverloadSecondsLeft()+"s",
                            paintArea.x + 10, paintArea.y + 75);

                    g.drawString("Prayer Flick Timer: ~" + ScriptStatusPainter.getSecondsTilNextFlick()+"s",
                            paintArea.x + 10, paintArea.y + 90);

                }
            }
        }

        Point pos = getMouse().getPosition();
        g.drawLine(0, pos.y, 800, pos.y); //horiz line
        g.drawLine(pos.x, 0, pos.x, 500); //vert line
    }

    @SuppressWarnings("deprecation")
    private void setUp(){
        paintHandler = new DraggablePaintHandler();
        this.bot.addMouseListener(paintHandler);

        getBot().addPainter(MainScript.this);
        Statics.setHostScriptReference(this);

        PrepNode prepNode = (PrepNode) PrepNode.getSingleton(this);
        AFKNode afkNode = (AFKNode) AFKNode.getSingleton(this);
        ActiveNode activeNode = (ActiveNode) ActiveNode.getSingleton(this);

        executor = new GraphBasedNodeExecutor(prepNode);
        /*executor.addEdgeToNode(prepNode, afkNode, 1);
        executor.addEdgeToNode(afkNode, afkNode, 1);*/
        executor.addEdgeToNode(prepNode, activeNode, 1);
        executor.addEdgeToNode(activeNode, activeNode, 1);

        tracker = new CombatXPTracker();
        tracker.exchangeContext(bot);
        tracker.initializeModule();
        tracker.setModuleReady();
        tracker.setCombatStyle();
    }

    private String formatTime(final long ms){
        long s = ms / 1000, m = s / 60, h = m / 60;
        s %= 60; m %= 60; h %= 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    private String formatValue(final long l) {
        return (l > 1_000_000) ? String.format("%.2fm", ((double) l / 1_000_000))
                : (l > 1000) ? String.format("%.1fk", ((double) l / 1000))
                : l + "";
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

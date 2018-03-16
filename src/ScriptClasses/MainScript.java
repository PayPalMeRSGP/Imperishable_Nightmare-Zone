package ScriptClasses;

import Nodes.MidDreamNodes.AFKNode;
import Nodes.MidDreamNodes.ActiveNode;
import Nodes.PrepNode;
import ScriptClasses.Paint.DraggablePaintHandler;
import ScriptClasses.Paint.PaintInfo;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.listener.MessageListener;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;

@ScriptManifest(author = "PayPalMeRSGP", name = MainScript.BUILD_NUM + " " + MainScript.SCRIPT_NAME, info = "NMZ_AFK_ALPHA, start inside dream", version = 0.1, logo = "")
public class MainScript extends Script implements MessageListener {
    static final String SCRIPT_NAME = "Imperishable Nighmare-Zone";
    static final int BUILD_NUM = 6;

    private long startTime;

    private GraphBasedNodeExecutor executor;
    private DraggablePaintHandler paintHandler;

    @Override
    public void onStart() throws InterruptedException {
        super.onStart();
        getBot().addMessageListener(this);
        setUp();
    }

    @Override
    public int onLoop() throws InterruptedException {
        PaintInfo.setCombatStyle();
        return executor.executeNodeThenTraverse();
    }

    @Override
    public void onPaint(Graphics2D g) {
        super.onPaint(g);
        long runTime = System.currentTimeMillis() - startTime;

        PaintInfo info = PaintInfo.getSingleton(this);

        int hpXpGained = info.getHpXpGained();
        int hpXPH = info.getHpXPH();
        long hpTTL = info.getHpTTL();
        int hpLvl = info.getHpLvl();

        Point pos = getMouse().getPosition();
        g.drawLine(0, pos.y, 800, pos.y); //horiz line
        g.drawLine(pos.x, 0, pos.x, 500); //vert line

        Rectangle paintArea = paintHandler.getPaintArea();

        g.setColor(new Color(156,156,156));
        g.fillRect(paintArea.x, paintArea.y, paintArea.width, paintArea.height);
        g.setColor(new Color(255, 255, 255));

        PaintInfo.CombatStyle style = PaintInfo.getSingleton(this).getStyle();
        if(style != null){
            if(style == PaintInfo.CombatStyle.CTRL){
                paintArea.setBounds(0, 0, 300, 100);
                g.drawString("ATK" + " LVL: " + formatValue(info.getAtkLvl()) + " XP: " + formatValue(info.getTrainingXpGained()) + " TTL: " + formatTime(info.getAtkTTL()) + " XPH: " + formatValue(info.getTraiingXPH()), paintArea.x + 10, paintArea.y + 15);
                g.drawString("STR" + " LVL: " + formatValue(info.getStrLvl()) + " XP: " + formatValue(info.getTrainingXpGained()) + " TTL: " + formatTime(info.getStrTTL()) + " XPH: " + formatValue(info.getTraiingXPH()), paintArea.x + 10, paintArea.y + 30);
                g.drawString("DEF" + " LVL: " + formatValue(info.getDefLvl()) + " XP: " + formatValue(info.getTrainingXpGained()) + " TTL: " + formatTime(info.getDefTTL()) + " XPH: " + formatValue(info.getTraiingXPH()), paintArea.x + 10, paintArea.y + 45);
                g.drawString("HP LVL: " + formatValue(hpLvl) + " XP: " + formatValue(hpXpGained) + " TTL: " + formatTime(hpTTL) + " XPH: " + formatValue(hpXPH), paintArea.x + 10, paintArea.y + 60);
                g.drawString("runtime: " + formatTime(runTime), paintArea.x + 10, paintArea.y + 75);
                g.drawString("status: " + PaintInfo.getSingleton(this).getCurrentScriptStatus(), paintArea.x + 10, paintArea.y + 90);
            }
            else{
                g.drawString(style.toString() + " LVL: " + formatValue(info.getTrainingSkillLvl()) + " XP: " + formatValue(info.getTrainingXpGained()) + " TTL: " + formatTime(info.getTrainingSkillTTL()) + " XPH: " + formatValue(info.getTraiingXPH()), paintArea.x + 10, paintArea.y + 15);
                g.drawString("HP LVL: " + formatValue(hpLvl) + " XP: " + formatValue(hpXpGained) + " TTL: " + formatTime(hpTTL) + " XPH: " + formatValue(hpXPH), paintArea.x + 10, paintArea.y + 30);
                g.drawString("runtime: " + formatTime(runTime), paintArea.x + 10, paintArea.y + 45);
                g.drawString("status: " + PaintInfo.getSingleton(this).getCurrentScriptStatus(), paintArea.x + 10, paintArea.y + 60);
            }
        }
    }

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


        startTime = System.currentTimeMillis();
        getExperienceTracker().start(Skill.HITPOINTS);
        getExperienceTracker().start(Skill.ATTACK);
        getExperienceTracker().start(Skill.STRENGTH);
        getExperienceTracker().start(Skill.DEFENCE);
        getExperienceTracker().start(Skill.RANGED);
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
                stop(false); //if nodes don't catch the death
            }
        }
    }


}

package ScriptClasses;

import Nodes.AFKNode;
import Nodes.ActiveNode;
import Nodes.PrepNode;
import org.osbot.AF;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.listener.MessageListener;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import ScriptClasses.PublicStaticFinalConstants.MeleeCombatStyle;

@ScriptManifest(author = "PayPalMeRSGP", name = "PrayerFlickTest1", info = "NMZ_AFK_ALPHA, start inside dream", version = 0.1, logo = "")
public class MainScript extends Script implements MouseListener, MouseMotionListener, MessageListener {

    private long startTime;
    private MeleeCombatStyle style;
    private GraphBasedNodeExecutor executor;
    //for draggable paint
    private int xOffset = 0;
    private int yOffset = 0;
    private int paintRectangleTopLeftX = 0;
    private int paintRectangleTopLeftY = 0;
    private Rectangle paintArea = new Rectangle(paintRectangleTopLeftX, paintRectangleTopLeftY, 300, 75);
    private boolean movingPaint = false;

    @Override
    public void onStart() throws InterruptedException {
        super.onStart();
        getBot().addMessageListener(this);
        setUp();
    }

    @Override
    public int onLoop() throws InterruptedException {
        determineMeleeStyle();
        return executor.executeNodeThenTraverse();
    }

    @Override
    public void onPaint(Graphics2D g) {
        super.onPaint(g);
        long runTime = System.currentTimeMillis() - startTime;

        PaintXPInfo info = PaintXPInfo.getSingleton(style, this);

        int hpXpGained = info.getHpXpGained();
        int hpXPH = info.getHpXPH();
        long hpTTL = info.getHpTTL();
        int hpLvl = info.getHpLvl();

        Point pos = getMouse().getPosition();
        g.drawLine(0, pos.y, 800, pos.y); //horiz line
        g.drawLine(pos.x, 0, pos.x, 500); //vert line

        g.setColor(new Color(156,156,156));
        g.fillRect(paintArea.x, paintArea.y, paintArea.width, paintArea.height);
        g.setColor(new Color(255, 255, 255));

        if(style == MeleeCombatStyle.CTRL){
            paintArea.setBounds(paintRectangleTopLeftX, paintRectangleTopLeftY, 300, 100);
            g.drawString("ATK" + " LVL: " + formatValue(info.getAtkLvl()) + " XP: " + formatValue(info.getMeleeXpGained()) + " TTL: " + formatTime(info.getAtkTTL()) + " XPH: " + formatValue(info.getMeleeXPH()), paintArea.x + 10, paintArea.y + 15);
            g.drawString("STR" + " LVL: " + formatValue(info.getStrLvl()) + " XP: " + formatValue(info.getMeleeXpGained()) + " TTL: " + formatTime(info.getStrTTL()) + " XPH: " + formatValue(info.getMeleeXPH()), paintArea.x + 10, paintArea.y + 30);
            g.drawString("DEF" + " LVL: " + formatValue(info.getDefLvl()) + " XP: " + formatValue(info.getMeleeXpGained()) + " TTL: " + formatTime(info.getDefTTL()) + " XPH: " + formatValue(info.getMeleeXPH()), paintArea.x + 10, paintArea.y + 45);
            g.drawString("HP LVL: " + formatValue(hpLvl) + " XP: " + formatValue(hpXpGained) + " TTL: " + formatTime(hpTTL) + " XPH: " + formatValue(hpXPH), paintArea.x + 10, paintArea.y + 60);
            g.drawString("runtime: " + formatTime(runTime), paintArea.x + 10, paintArea.y + 75);
            g.drawString("status: " + PublicStaticFinalConstants.getCurrentScriptStatus(), paintArea.x + 10, paintArea.y + 90);
        }
        else{
            g.drawString(style.toString() + " LVL: " + formatValue(info.getMeleeLvl()) + " XP: " + formatValue(info.getMeleeXpGained()) + " TTL: " + formatTime(info.getMeleeTTL()) + " XPH: " + formatValue(info.getMeleeXPH()), paintArea.x + 10, paintArea.y + 15);
            g.drawString("HP LVL: " + formatValue(hpLvl) + " XP: " + formatValue(hpXpGained) + " TTL: " + formatTime(hpTTL) + " XPH: " + formatValue(hpXPH), paintArea.x + 10, paintArea.y + 30);
            g.drawString("runtime: " + formatTime(runTime), paintArea.x + 10, paintArea.y + 45);
            g.drawString("status: " + PublicStaticFinalConstants.getCurrentScriptStatus(), paintArea.x + 10, paintArea.y + 60);
        }
    }

    private void setUp(){
        this.bot.addMouseListener(this);
        this.bot.getCanvas().addMouseMotionListener(this);
        getBot().addPainter(MainScript.this);
        PublicStaticFinalConstants.setHostScriptReference(this);

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
    }

    private void determineMeleeStyle(){
        int s = this.getConfigs().get(43);
        switch (s){
            case 0:
                style = MeleeCombatStyle.ATK;
                break;
            case 1:
                style = MeleeCombatStyle.STR;
                break;
            case 2:
                style = MeleeCombatStyle.CTRL;
                break;
            case 3:
                style = MeleeCombatStyle.DEF;
                break;
        }

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
    public void mouseClicked(MouseEvent e) {
        //not used
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point clickPt = e.getPoint();
        if(paintArea.contains(clickPt)){
            movingPaint = true;
            xOffset = clickPt.x - paintArea.x;
            yOffset = clickPt.y - paintArea.y;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        movingPaint = false;
        xOffset = 0;
        yOffset = 0;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        //not used
    }

    @Override
    public void mouseExited(MouseEvent e) {
        //not used
    }


    @Override
    public void mouseDragged(MouseEvent e) {
        if(movingPaint){
            Point mousePos = e.getPoint();
            paintArea.x = mousePos.x - xOffset;
            paintArea.y = mousePos.y - yOffset;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        //not used
    }

    @Override
    public void onMessage(Message message) throws InterruptedException {
        PublicStaticFinalConstants.hostScriptReference.log(message.getMessage());
        if(message.getType() == Message.MessageType.GAME){
            if(message.getMessage().contains(PublicStaticFinalConstants.OVERLOAD_DEPLETED_MSG)){
                PublicStaticFinalConstants.hostScriptReference.log("recieved overload worn off msg");
                AFKNode afkNode = (AFKNode) AFKNode.getSingleton(this);
                ActiveNode activeNode = (ActiveNode) ActiveNode.getSingleton(this);
                afkNode.setDoOverload(true);
                activeNode.setDoOverload(true);
            }
            else if(message.getMessage().contains(PublicStaticFinalConstants.DREAM_OVER_MSG)){
                log("died in NMZ, stopping script");
                AFKNode afkNode = (AFKNode) AFKNode.getSingleton(this);
                ActiveNode activeNode = (ActiveNode) ActiveNode.getSingleton(this);
                afkNode.setDied(true);
                activeNode.setDied(true);
                MethodProvider.sleep(10000);
                stop(); //if nodes don't catch the death
            }
        }
    }


}

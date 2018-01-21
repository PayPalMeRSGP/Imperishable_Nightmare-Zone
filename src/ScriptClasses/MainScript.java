package ScriptClasses;

import Nodes.AFKNode;
import org.osbot.rs07.api.Mouse;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

@ScriptManifest(author = "PayPalMeRSGP", name = "NMZ_debug2", info = "NMZ_AFK_ALPHA, start inside dream", version = 0.1, logo = "")
public class MainScript extends Script implements MouseListener, MouseMotionListener {

    private PriorityQueueWrapper pqw;
    private long startTime;

    //for draggable paint
    private int xOffset = 0;
    private int yOffset = 0;
    private int paintRectangleTopLeftX = 315;
    private int paintRectangleTopLeftY = 0;
    private Rectangle paintArea = new Rectangle(paintRectangleTopLeftX, paintRectangleTopLeftY, 300, 75);
    private boolean movingPaint = false;

    @Override
    public void onStart() throws InterruptedException {
        super.onStart();
        this.bot.addMouseListener(this);
        this.bot.getCanvas().addMouseMotionListener(this);
        getBot().addPainter(MainScript.this);
        PublicStaticFinalConstants.setHostScriptReference(this);
        this.pqw = new PriorityQueueWrapper();

        startTime = System.currentTimeMillis();
        getExperienceTracker().start(Skill.HITPOINTS);
        getExperienceTracker().start(Skill.STRENGTH);
    }

    @Override
    public int onLoop() throws InterruptedException {
        return this.pqw.executeTopNode();
    }

    @Override
    public void onPaint(Graphics2D g) {
        super.onPaint(g);
        long runTime = System.currentTimeMillis() - startTime;
        int strXpGained = this.getExperienceTracker().getGainedXP(Skill.STRENGTH);
        int strXPH = this.getExperienceTracker().getGainedXPPerHour(Skill.STRENGTH);
        long strTTL = this.getExperienceTracker().getTimeToLevel(Skill.STRENGTH);
        int strLvl = this.getSkills().getStatic(Skill.STRENGTH);
        int hpXpGained = this.getExperienceTracker().getGainedXP(Skill.HITPOINTS);
        int hpXPH = this.getExperienceTracker().getGainedXPPerHour(Skill.HITPOINTS);
        long hpTTL = this.getExperienceTracker().getTimeToLevel(Skill.HITPOINTS);
        int hpLvl = this.getSkills().getStatic(Skill.HITPOINTS);

        Point pos = getMouse().getPosition();
        g.drawLine(0, pos.y, 800, pos.y); //horiz line
        g.drawLine(pos.x, 0, pos.x, 500); //vert line

        g.setColor(new Color(156,156,156));
        g.fillRect(paintArea.x, paintArea.y, paintArea.width, paintArea.height);
        g.setColor(new Color(255, 255, 255));
        g.drawString("Str LVL: " + formatValue(strLvl) + " XP: " + formatValue(strXpGained) + " TTL: " + formatTime(strTTL) + " XPH: " + formatValue(strXPH), paintArea.x + 10, paintArea.y + 15);
        g.drawString("HP LVL: " + formatValue(hpLvl) + " XP: " + formatValue(hpXpGained) + " TTL: " + formatTime(hpTTL) + " XPH: " + formatValue(hpXPH), paintArea.x + 10, paintArea.y + 30);
        g.drawString("runtime: " + formatTime(runTime), paintArea.x + 10, paintArea.y + 45);
        g.drawString("status: " + PublicStaticFinalConstants.getCurrentScriptStatus(), paintArea.x + 10, paintArea.y + 60);
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
}

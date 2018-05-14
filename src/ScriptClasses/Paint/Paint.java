package ScriptClasses.Paint;

import ScriptClasses.Util.Statics;
import org.osbot.rs07.canvas.paint.Painter;
import org.osbot.rs07.script.Script;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Paint implements Painter{
    private DraggablePaintHandler paintHandler;
    private CombatXPTracker tracker;
    private Script script;
    private BufferedImage overloadIcon;
    private BufferedImage rapidHealIcon;

    private static final Color TRANS_GRAY = new Color(156,156,156, 127);
    private static final String IMG_FOLDER = "/Imperishable_NMZ_Images";

    @SuppressWarnings("deprecation")
    public Paint(Script script){
        this.script = script;
        paintHandler = new DraggablePaintHandler();

        try{
            overloadIcon = ImageIO.read(new File(script.getDirectoryData() + IMG_FOLDER + "/overload.png"));
            rapidHealIcon = ImageIO.read(new File(script.getDirectoryData() + IMG_FOLDER + "/rapid_heal.png"));
        }
        catch(IOException e){
            script.log(e);
        }

        tracker = new CombatXPTracker();
        tracker.exchangeContext(script.bot);
        tracker.initializeModule();
        tracker.setModuleReady();
        tracker.setCombatStyle();
        script.bot.addMouseListener(paintHandler);
        script.getBot().addPainter(this);
    }

    @Override
    public void onPaint(Graphics2D g) {
        Rectangle paintArea = paintHandler.getXpPaint();
        g.setColor(TRANS_GRAY);
        g.fillRect(paintArea.x, paintArea.y, paintArea.width, paintArea.height);

        if(tracker != null){
            if(tracker.getStyle() != null){
                g.setColor(Color.WHITE);
                if(tracker.getStyle() == CombatXPTracker.CombatStyle.CTRL)
                    paintCtrlXp(paintArea, g);
                else
                    paintStdXp(paintArea, g);

            }
        }
        paintOverloadTimer(g);
        paintRapidHealTimer(g);
        paintReset(g);
        paintCursor(g);
    }

    public void determineCombatStyle(){
        tracker.setCombatStyle();
    }

    private void paintCtrlXp(Rectangle paintArea, Graphics2D g){
        paintArea.setBounds(0, 0, 300, 105);
        int[] atkStrDef = tracker.getAtkStrDefLvls();
        g.drawString("Runtime: " + formatTime(tracker.getRunTime()),
                paintArea.x + 10, paintArea.y + 15);

        g.drawString("ATK LVL: " + atkStrDef[0]
                        + " XP: " + formatValue(tracker.getAtkLvl())
                        + " TTL: " + formatTime(tracker.getAtkTTL())
                        + " XPH: " + formatValue(tracker.getTrainingXPH()),
                paintArea.x + 10, paintArea.y + 30);

        g.drawString("STR LVL: " + atkStrDef[1]
                        + " XP: " + formatValue(tracker.getStrLvl())
                        + " TTL: " + formatTime(tracker.getStrTTL()),
                paintArea.x + 10, paintArea.y + 45);

        g.drawString("DEF LVL: " + atkStrDef[2]
                        + " XP: " + formatValue(tracker.getTrainingXpGained())
                        + " TTL: " + formatTime(tracker.getDefTTL()),
                paintArea.x + 10, paintArea.y + 60);

        g.drawString("HP LVL: " + tracker.getHpLvl()
                        + " XP: " + formatValue(tracker.getHpXpGained())
                        + " TTL: " + formatTime(tracker.getHpTTL())
                        + " XPH: " + formatValue(tracker.getHpXPH()),
                paintArea.x + 10, paintArea.y + 75);
        g.drawString("Status: " + ScriptStatusPainter.getCurrentMarkovStatus() + "(" + ScriptStatusPainter.getOnLoopsB4Switch() + ") " +
                "- " + ScriptStatusPainter.getCurrentScriptStatus(), paintArea.x + 10, paintArea.y + 90);
    }

    private void paintStdXp(Rectangle paintArea, Graphics2D g){
        g.drawString("Runtime: " + formatTime(tracker.getRunTime()),
                paintArea.x + 10, paintArea.y + 15);

        g.drawString(tracker.getStyle().toString() + " LVL: " + formatValue(tracker.getTrainingSkillLvl())
                        + " XP: " + formatValue(tracker.getTrainingXpGained())
                        + " TTL: " + formatTime(tracker.getTrainingTTL())
                        + " XPH: " + formatValue(tracker.getTrainingXPH()),
                paintArea.x + 10, paintArea.y + 30);

        g.drawString("HP LVL: " + tracker.getHpLvl()
                        + " XP: " + formatValue(tracker.getHpXpGained())
                        + " TTL: " + formatTime(tracker.getHpTTL())
                        + " XPH: " + formatValue(tracker.getHpXPH()),
                paintArea.x + 10, paintArea.y + 45);

        g.drawString("Status: " + ScriptStatusPainter.getCurrentMarkovStatus() + "(" + ScriptStatusPainter.getOnLoopsB4Switch() + ") " +
                "- " + ScriptStatusPainter.getCurrentScriptStatus(), paintArea.x + 10, paintArea.y + 60);
    }

    private void paintOverloadTimer(Graphics2D g){
        g.setColor(TRANS_GRAY);
        Rectangle paintArea = paintHandler.getOverloadPaint();
        g.fillRect(paintArea.x, paintArea.y, paintArea.width, paintArea.height);
        if(overloadIcon != null){
            g.drawImage(overloadIcon, null, paintArea.x + 8, paintArea.y + 2);
        }
        g.setColor(Color.WHITE);
        g.drawString(ScriptStatusPainter.getOverloadSecondsLeft()+"s", paintArea.x + 50, paintArea.y + 20);
    }

    private void paintRapidHealTimer(Graphics2D g){
        g.setColor(TRANS_GRAY);
        Rectangle paintArea = paintHandler.getPrayerFlickPaint();
        g.fillRect(paintArea.x, paintArea.y, paintArea.width, paintArea.height);
        if(rapidHealIcon != null){
            g.drawImage(rapidHealIcon, null, paintArea.x + 5, paintArea.y + 5);
        }
        g.setColor(Color.WHITE);
        g.drawString(ScriptStatusPainter.getSecondsTilNextFlick()+"s", paintArea.x + 50, paintArea.y + 20);
    }

    private void paintReset(Graphics2D g){
        g.setColor(TRANS_GRAY);
        Rectangle resetArea = paintHandler.getResetPaint();
        g.fillRect(resetArea.x, resetArea.y, resetArea.width, resetArea.height);
        g.setColor(Color.WHITE);
        g.drawString("Reset Locations", resetArea.x + 10, resetArea.y + 15);
    }

    private void paintCursor(Graphics2D g){
        Point pos = script.getMouse().getPosition();
        g.drawLine(0, pos.y, 800, pos.y); //horiz line
        g.drawLine(pos.x, 0, pos.x, 500); //vert line
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

}

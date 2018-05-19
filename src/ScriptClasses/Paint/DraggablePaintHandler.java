package ScriptClasses.Paint;

import org.osbot.rs07.input.mouse.BotMouseListener;

import java.awt.*;
import java.awt.event.MouseEvent;

public class DraggablePaintHandler extends BotMouseListener {

    private int xOffset = 0;
    private int yOffset = 0;
    private static final Point XP_DEFAULT = new Point(4, 260), OVERLOAD_DEFAULT = new Point(425, 40), PRAYER_DEFAULT = new Point(425,80);
    private final Rectangle resetPaint = new Rectangle(390, 320, 126, 20);
    private final Rectangle xpPaint = new Rectangle(XP_DEFAULT.x, XP_DEFAULT.y, 350, 75);
    private final Rectangle overloadPaint = new Rectangle(OVERLOAD_DEFAULT.x, OVERLOAD_DEFAULT.y, 85, 35);
    private final Rectangle prayerFlickPaint = new Rectangle(PRAYER_DEFAULT.x, PRAYER_DEFAULT.y, 85, 35);
    private boolean movingPaint = false;
    private boolean movingOverload = false;
    private boolean movingPrayerFlick = false;

    @Override
    public void checkMouseEvent(MouseEvent mouseEvent) {
        switch (mouseEvent.getID()){
            case MouseEvent.MOUSE_PRESSED:
                Point clickPt = mouseEvent.getPoint();
                if(xpPaint.contains(clickPt)){
                    movingPaint = true;
                    xOffset = clickPt.x - xpPaint.x;
                    yOffset = clickPt.y - xpPaint.y;
                    mouseEvent.consume();
                }
                else if(overloadPaint.contains(clickPt)){
                    movingOverload = true;
                    xOffset = clickPt.x - overloadPaint.x;
                    yOffset = clickPt.y - overloadPaint.y;
                    mouseEvent.consume();
                }
                else if(prayerFlickPaint.contains(clickPt)){
                    movingPrayerFlick = true;
                    xOffset = clickPt.x - prayerFlickPaint.x;
                    yOffset = clickPt.y - prayerFlickPaint.y;
                    mouseEvent.consume();
                }
                else if(resetPaint.contains(clickPt)){
                    xpPaint.setLocation(XP_DEFAULT);
                    overloadPaint.setLocation(OVERLOAD_DEFAULT);
                    prayerFlickPaint.setLocation(PRAYER_DEFAULT);
                }
                break;

            case MouseEvent.MOUSE_RELEASED:
                movingPaint = false;
                movingOverload = false;
                movingPrayerFlick = false;
                xOffset = 0;
                yOffset = 0;

                break;
        }
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        super.mouseDragged(mouseEvent);
        if(movingPaint){
            Point mousePos = mouseEvent.getPoint();
            xpPaint.x = mousePos.x - xOffset;
            xpPaint.y = mousePos.y - yOffset;
        }
        else if(movingOverload){
            Point mousePos = mouseEvent.getPoint();
            overloadPaint.x = mousePos.x - xOffset;
            overloadPaint.y = mousePos.y - yOffset;
        }
        else if(movingPrayerFlick){
            Point mousePos = mouseEvent.getPoint();
            prayerFlickPaint.x = mousePos.x - xOffset;
            prayerFlickPaint.y = mousePos.y - yOffset;
        }

    }

    public Rectangle getXpPaint() {
        return xpPaint;
    }

    public Rectangle getResetPaint() {
        return resetPaint;
    }

    public Rectangle getOverloadPaint() {
        return overloadPaint;
    }

    public Rectangle getPrayerFlickPaint() {
        return prayerFlickPaint;
    }
}

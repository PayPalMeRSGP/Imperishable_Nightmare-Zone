package ScriptClasses.Paint;

import org.osbot.rs07.input.mouse.BotMouseListener;

import java.awt.*;
import java.awt.event.MouseEvent;

public class DraggablePaintHandler extends BotMouseListener {

    private int xOffset = 0;
    private int yOffset = 0;
    private final Rectangle movablePaintArea = new Rectangle(0, 265, 285, 75);
    private final Rectangle resetPaint = new Rectangle(418, 320, 100, 20);
    private boolean movingPaint = false;

    @Override
    public void checkMouseEvent(MouseEvent mouseEvent) {

        switch (mouseEvent.getID()){
            case MouseEvent.MOUSE_PRESSED:
                Point clickPt = mouseEvent.getPoint();
                if(movablePaintArea.contains(clickPt)){
                    movingPaint = true;
                    xOffset = clickPt.x - movablePaintArea.x;
                    yOffset = clickPt.y - movablePaintArea.y;
                    mouseEvent.consume();
                }
                else if(resetPaint.contains(clickPt)){
                    movablePaintArea.setLocation(new Point(0,265));
                }
                break;

            case MouseEvent.MOUSE_RELEASED:
                movingPaint = false;
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
            movablePaintArea.x = mousePos.x - xOffset;
            movablePaintArea.y = mousePos.y - yOffset;
        }

    }

    public Rectangle getMovablePaintArea() {
        return movablePaintArea;
    }

    public Rectangle getResetPaint() {
        return resetPaint;
    }
}

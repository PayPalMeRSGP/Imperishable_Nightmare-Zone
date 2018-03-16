package ScriptClasses.Paint;

import org.osbot.rs07.input.mouse.BotMouseListener;

import java.awt.*;
import java.awt.event.MouseEvent;

public class DraggablePaintHandler extends BotMouseListener {

    private int xOffset = 0;
    private int yOffset = 0;
    private Rectangle paintArea = new Rectangle(0, 0, 350, 75);
    private boolean movingPaint = false;

    @Override
    public void checkMouseEvent(MouseEvent mouseEvent) {

        switch (mouseEvent.getID()){
            case MouseEvent.MOUSE_PRESSED:
                Point clickPt = mouseEvent.getPoint();
                if(paintArea.contains(clickPt)){
                    movingPaint = true;
                    xOffset = clickPt.x - paintArea.x;
                    yOffset = clickPt.y - paintArea.y;
                    mouseEvent.consume();
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
            paintArea.x = mousePos.x - xOffset;
            paintArea.y = mousePos.y - yOffset;
        }

    }

    public Rectangle getPaintArea() {
        return paintArea;
    }

}

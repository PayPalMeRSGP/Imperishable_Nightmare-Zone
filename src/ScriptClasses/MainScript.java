package ScriptClasses;

import Nodes.AFKNode;
import org.osbot.rs07.api.Mouse;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;

@ScriptManifest(author = "PayPalMeRSGP", name = "Run 12-30-17", info = "NMZ_AFK", version = 0.1, logo = "")
public class MainScript extends Script{

    PriorityQueueWrapper pqw;
    String currentScriptStatus;

    @Override
    public void onStart() throws InterruptedException {
        super.onStart();
        PublicStaticFinalConstants.setHostScriptReference(this);
        this.pqw = new PriorityQueueWrapper();
    }

    @Override
    public int onLoop() throws InterruptedException {
        return this.pqw.executeTopNode();

    }

    @Override
    public void onPaint(Graphics2D iiIiiiiiIiIi) {
        super.onPaint(iiIiiiiiIiIi);
        Point pos = getMouse().getPosition();
        iiIiiiiiIiIi.drawLine(0, pos.y, 800, pos.y); //horiz line
        iiIiiiiiIiIi.drawLine(pos.x, 0, pos.x, 500); //vert line
    }

    public void setScriptStatus(String s){
        this.currentScriptStatus = s;
    }
}

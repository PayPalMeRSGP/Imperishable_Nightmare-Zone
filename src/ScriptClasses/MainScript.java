package ScriptClasses;

import Nodes.PrepNode;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.Menu;
import org.osbot.rs07.api.Mouse;
import org.osbot.rs07.api.ui.Option;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.input.mouse.InventorySlotDestination;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;

@ScriptManifest(author = "PayPalMeRSGP", name = "debug", info = "nmz AFK", version = 0.1, logo = "")
public class MainScript extends Script{

    PriorityQueueWrapper pqw;

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



}

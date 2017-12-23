package ScriptClasses;

import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "PayPalMeRSGP", name = "NMZ_AFK_DEBUG1", info = "nmz AFK", version = 0.1, logo = "")
public class MainScript extends Script{

    PriorityQueueWrapper pqw;

    @Override
    public void onStart() throws InterruptedException {
        super.onStart();
        ConstantsAndStatics.setHostScriptReference(this);
        this.pqw = new PriorityQueueWrapper();

    }

    @Override
    public int onLoop() throws InterruptedException {
        return pqw.executeTopNode();
    }

}

package Nodes;

import ScriptClasses.PaintInfo;
import ScriptClasses.Statics;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.Menu;
import org.osbot.rs07.api.Mouse;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.input.mouse.InventorySlotDestination;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

public class PrepNode implements ExecutableNode{
    private Script hostScriptReference;

    private static ExecutableNode singleton = null;

    private PrepNode(Script hostScriptReference) {
        this.hostScriptReference = hostScriptReference;
    }

    public static ExecutableNode getSingleton(Script hostScriptReference) {
        if(singleton == null){
            singleton = new PrepNode(hostScriptReference);
        }
        return singleton;
    }

    @Override
    public int executeNodeAction() throws InterruptedException {
        PaintInfo.getSingleton(hostScriptReference).setCurrentScriptStatus(PaintInfo.ScriptStatus.PREPARING);
        drinkAbsorptions();
        setPlayerHealthTo1();
        turnOnAutoRetaliate();
        return 1000;
    }

    private void drinkAbsorptions() throws InterruptedException {
        Inventory inv = hostScriptReference.getInventory();
        int absorptionLvl = getAbsorptionLvl();
        while(absorptionLvl < 200 && doesPlayerHaveAbsorptionsLeft()){
            inv.interact(Statics.DRINK, Statics.ABSORPTION_POTION_1_ID, Statics.ABSORPTION_POTION_2_ID, Statics.ABSORPTION_POTION_3_ID, Statics.ABSORPTION_POTION_4_ID);
            absorptionLvl = getAbsorptionLvl();
            MethodProvider.sleep(Statics.randomNormalDist(Statics.RS_GAME_TICK_MS*3, 180));
        }

    }

    private void setPlayerHealthTo1() throws InterruptedException {
        int currentHealth = hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
        int estimatedHealthAfterOverload = currentHealth - 49; //49 incase health regenerates 1pt in overload dmg process
        Inventory inv = hostScriptReference.getInventory();
        if(currentHealth > 50 && doesPlayerHaveOverloadsLeft()){
            inv.interact(Statics.DRINK, Statics.OVERLOAD_POTION_1_ID, Statics.OVERLOAD_POTION_2_ID,
                    Statics.OVERLOAD_POTION_3_ID, Statics.OVERLOAD_POTION_4_ID);
            //wait out overload dmg, DO NOT GUZZLE while taking overload dmg, may result in overload dmg player killing player.
            new ConditionalSleep(7000, 500){
                @Override
                public boolean condition() throws InterruptedException {
                    int currentHealth = hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
                    return estimatedHealthAfterOverload > currentHealth;
                }
            }.sleep();
        }
        while(currentHealth > 1){
            guzzleRockCake();
            currentHealth = hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
            Statics.hostScriptReference.log("guzzling rockcake... hp: " + currentHealth);
            MethodProvider.sleep(Statics.randomNormalDist(Statics.RS_GAME_TICK_MS, 60.0));
        }
    }

    private void turnOnAutoRetaliate(){
        hostScriptReference.getTabs().open(Tab.ATTACK);
        hostScriptReference.getCombat().toggleAutoRetaliate(true);
        hostScriptReference.getTabs().open(Tab.INVENTORY);
    }

    private void guzzleRockCake(){
        Inventory inv = hostScriptReference.getInventory();
        Mouse mouse = hostScriptReference.getMouse();
        Menu rockCakeMenu = hostScriptReference.getMenuAPI();
        if(inv.contains(Statics.DWARVEN_ROCK_CAKE_ID)){
            int rockCakeInvSlot = inv.getSlot(Statics.DWARVEN_ROCK_CAKE_ID);
            InventorySlotDestination rockCakeDest = new InventorySlotDestination(hostScriptReference.getBot(), rockCakeInvSlot);
            mouse.click(rockCakeDest, true);
            if(rockCakeMenu.isOpen()){
                rockCakeMenu.selectAction(Statics.GUZZLE);
            }
        }
        
    }

    private boolean doesPlayerHaveOverloadsLeft(){
        Inventory inv = hostScriptReference.getInventory();
        return inv.contains(Statics.OVERLOAD_POTION_1_ID) || inv.contains(Statics.OVERLOAD_POTION_2_ID)
                || inv.contains(Statics.OVERLOAD_POTION_3_ID) || inv.contains(Statics.OVERLOAD_POTION_4_ID);
    }

    private boolean doesPlayerHaveAbsorptionsLeft(){
        Inventory inv = hostScriptReference.getInventory();
        return inv.contains(Statics.ABSORPTION_POTION_1_ID) || inv.contains(Statics.ABSORPTION_POTION_2_ID)
                || inv.contains(Statics.ABSORPTION_POTION_3_ID) || inv.contains(Statics.ABSORPTION_POTION_4_ID);
    }

    private int getAbsorptionLvl() {
        RS2Widget widget = hostScriptReference.getWidgets().get(202, 1, 9);
        if(widget != null && widget.isVisible() && widget.getMessage() != null)
            return Integer.parseInt(widget.getMessage().replace(",", ""));
        return 0;
    }
}

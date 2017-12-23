package Nodes;

import ScriptClasses.ConstantsAndStatics;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.listener.MessageListener;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

public class MainAFKNode implements ExecutableNode, MessageListener, Comparable<ExecutableNode> {
    private final int BASE_STARTING_KEY = 2;
    private int currentKey = BASE_STARTING_KEY;
    private final static String OVERLOAD_WORN_OFF_MSG = "The effects of overload have worn off, and you feel normal again.";
    private final static String DRINK = "Drink";
    private final static String GUZZLE = "Guzzle";

    private boolean overloadActive = true;

    private static MainAFKNode mainAFKNodeSingleton;

    private MainAFKNode(){}

    public static MainAFKNode getMainAFKNodeInstance(){
        if(mainAFKNodeSingleton == null){
            mainAFKNodeSingleton = new MainAFKNode();
        }
        return mainAFKNodeSingleton;
    }

    @Override
    public int executeNodeAction() throws InterruptedException {
        new ConditionalSleep(50000, 250, 100){
            @Override
            public boolean condition() throws InterruptedException {
                return isHpOver1() || isAbsorptionLow();
            }
        }.sleep();
        return 1000;
    }

    private boolean isHpOver1() throws InterruptedException {
        int currentHealth = ConstantsAndStatics.hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
        if(currentHealth > 1){
            if(currentHealth > 50){
                int startingHealth = currentHealth;
                Inventory inv = ConstantsAndStatics.hostScriptReference.getInventory();
                inv.interact(DRINK, ConstantsAndStatics.OVERLOAD_ID);
                while(currentHealth > startingHealth - 48){
                    MethodProvider.sleep(100);
                    currentHealth = ConstantsAndStatics.hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
                    ConstantsAndStatics.hostScriptReference.log("re-drinking overload... hp: " + currentHealth);
                }
            }
            while(currentHealth > 1){
                Inventory inv = ConstantsAndStatics.hostScriptReference.getInventory();
                inv.interact(GUZZLE, ConstantsAndStatics.DWARVEN_ROCK_CAKE_ID);
                MethodProvider.sleep(ConstantsAndStatics.randomNormalDist(ConstantsAndStatics.RS_GAME_TICK_MS, 60.0));
                currentHealth = ConstantsAndStatics.hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
                ConstantsAndStatics.hostScriptReference.log("re-guzzling rockcake... hp: " + currentHealth);
            }
            return true;
        }
        return false;
    }

    private boolean isAbsorptionLow() throws InterruptedException {
        Inventory inv = ConstantsAndStatics.hostScriptReference.getInventory();
        int absorptionLvl = getAbsorptionLvl();
        if(absorptionLvl < 100){
            while(absorptionLvl < 400 && inv.contains(ConstantsAndStatics.ABSORPTION_POTION_ID)){
                ConstantsAndStatics.hostScriptReference.log("absorptionLvl: " + absorptionLvl);
                inv.interact("drink", ConstantsAndStatics.ABSORPTION_POTION_ID);
                absorptionLvl = getAbsorptionLvl();
                MethodProvider.sleep(ConstantsAndStatics.randomNormalDist(ConstantsAndStatics.RS_GAME_TICK_MS, 60.0));
            }
            return true;
        }
        return false;
    }

    private int getAbsorptionLvl() {
        RS2Widget widget = ConstantsAndStatics.hostScriptReference.getWidgets().get(202, 1, 9);
        if(widget != null && widget.isVisible() && widget.getMessage() != null)
            return Integer.parseInt(widget.getMessage().replace(",", ""));
        return 0;
    }

    @Override
    public void resetKey() {
        currentKey = BASE_STARTING_KEY;
    }

    @Override
    public void setKey(int key) {
        currentKey = key;
    }

    @Override
    public int getKey() {
        return currentKey;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " currentKey: " + currentKey;
    }

    @Override
    public void onMessage(Message message) throws InterruptedException {
        if(message.getType() == Message.MessageType.GAME){
            if(message.getMessage().equals(OVERLOAD_WORN_OFF_MSG)){
                ConstantsAndStatics.hostScriptReference.log("recieved overload worn off msg");
            }
        }
    }

    @Override
    public int compareTo(ExecutableNode o) {
        return this.getKey() - o.getKey();
    }
}

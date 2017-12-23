package Nodes;

import ScriptClasses.ConstantsAndStatics;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.MethodProvider;

public class PrepNode implements ExecutableNode, Comparable<ExecutableNode> {
    private final int BASE_STARTING_KEY = 1;
    private int currentKey = BASE_STARTING_KEY;
    private static PrepNode prepNodeSingleton;

    private final static String DRINK = "Drink";
    private final static String GUZZLE = "Guzzle";

    private PrepNode(){

    }

    public static PrepNode getPrepNodeInstance(){
        if(prepNodeSingleton == null){
            prepNodeSingleton = new PrepNode();
        }
        return prepNodeSingleton;
    }

    @Override
    public int executeNodeAction() throws InterruptedException {
        drinkAbsorptions();
        setPlayerHealthTo1();
        return 1000;
    }

    private void drinkAbsorptions() throws InterruptedException {
        Inventory inv = ConstantsAndStatics.hostScriptReference.getInventory();
        int absorptionLvl = getAbsorptionLvl();
        while(absorptionLvl < 400 && inv.contains(ConstantsAndStatics.ABSORPTION_POTION_ID)){
            ConstantsAndStatics.hostScriptReference.log("absorptionLvl: " + absorptionLvl);
            inv.interact(DRINK, ConstantsAndStatics.ABSORPTION_POTION_ID);
            absorptionLvl = getAbsorptionLvl();
            MethodProvider.sleep(ConstantsAndStatics.randomNormalDist(ConstantsAndStatics.RS_GAME_TICK_MS, 60.0));
        }
    }

    private void setPlayerHealthTo1() throws InterruptedException {
        int startingHealth = ConstantsAndStatics.hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
        int currentHealth = startingHealth;
        Inventory inv = ConstantsAndStatics.hostScriptReference.getInventory();
        if(startingHealth >= 50){

            inv.interact(DRINK, ConstantsAndStatics.OVERLOAD_ID);
            while(currentHealth > startingHealth - 48){
                MethodProvider.sleep(100);
                currentHealth = ConstantsAndStatics.hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
                ConstantsAndStatics.hostScriptReference.log("drinking overload... hp: " + currentHealth);
            }
        }
        while(currentHealth > 1){
            inv.interact(GUZZLE, ConstantsAndStatics.DWARVEN_ROCK_CAKE_ID);
            currentHealth = ConstantsAndStatics.hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
            ConstantsAndStatics.hostScriptReference.log("guzzling rockcake... hp: " + currentHealth);
            MethodProvider.sleep(ConstantsAndStatics.randomNormalDist(ConstantsAndStatics.RS_GAME_TICK_MS, 60.0));
        }
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
    public int compareTo(ExecutableNode o) {
        return this.getKey() - o.getKey();
    }
}

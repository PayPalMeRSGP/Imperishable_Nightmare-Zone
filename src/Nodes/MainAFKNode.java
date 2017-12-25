package Nodes;

import ScriptClasses.PublicStaticFinalConstants;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.listener.MessageListener;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.concurrent.ThreadLocalRandom;

public class MainAFKNode implements ExecutableNode, MessageListener, Comparable<ExecutableNode> {
    private final int BASE_STARTING_KEY = 2;
    private int currentKey = BASE_STARTING_KEY;
    private final static String OVERLOAD_WORN_OFF_MSG = "The effects of overload have worn off, and you feel normal again.";
    private final static String DRINK = "Drink";
    private final static String GUZZLE = "Guzzle";

    private static MainAFKNode mainAFKNodeSingleton;

    private int randomHpLimit;

    private MainAFKNode(){}

    public static MainAFKNode getMainAFKNodeInstance(){
        if(mainAFKNodeSingleton == null){
            mainAFKNodeSingleton = new MainAFKNode();
            mainAFKNodeSingleton.randomHpLimit = ThreadLocalRandom.current().nextInt(2, 5);
        }
        return mainAFKNodeSingleton;
    }

    @Override
    public int executeNodeAction() throws InterruptedException {
        int sleepTime = (int) PublicStaticFinalConstants.randomNormalDist(45000,4500);
        new ConditionalSleep(sleepTime, 250, 100){
            @Override
            public boolean condition() throws InterruptedException {
                boolean cond1 = isHpAtRandomHpLimit();
                boolean cond2 = isAbsorptionLow();
                return cond1 || cond2; //prevent short circuiting, isHpAtRandomHpLimit(), isAbsorptionLow() handle drinking overload, guzzling rockcake, and drink absorptions.
            }
        }.sleep();
        togglePrayer();
        return 1000;
    }

    private boolean togglePrayer() throws InterruptedException {
        int currentPrayerPts = PublicStaticFinalConstants.hostScriptReference.getSkills().getDynamic(Skill.PRAYER);
        if(currentPrayerPts > 0){
            RS2Widget prayerWidget = PublicStaticFinalConstants.hostScriptReference.getWidgets().get(160,14);
            if(prayerWidget.interact(true, "Activate")){
                MethodProvider.sleep(PublicStaticFinalConstants.randomNormalDist(2000, 200));
                return prayerWidget.interact(true, "Deactivate");
            }
            return false;
        }
        return false;
    }

    private boolean isHpAtRandomHpLimit() throws InterruptedException {
        int currentHealth = PublicStaticFinalConstants.hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
        if(currentHealth >= randomHpLimit){
            drinkOverload();
            guzzleRockCakeTo1();
            PublicStaticFinalConstants.hostScriptReference.getMouse().moveOutsideScreen();
            randomHpLimit = ThreadLocalRandom.current().nextInt(2, 5);
            return true;
        }
        return false;
    }

    private void drinkOverload() throws InterruptedException {
        int currentHealth = PublicStaticFinalConstants.hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
        if(currentHealth > 50){
            int startingHealth = currentHealth;
            Inventory inv = PublicStaticFinalConstants.hostScriptReference.getInventory();
            inv.interact(DRINK, PublicStaticFinalConstants.OVERLOAD_POTION_1_ID, PublicStaticFinalConstants.OVERLOAD_POTION_2_ID,
                    PublicStaticFinalConstants.OVERLOAD_POTION_3_ID, PublicStaticFinalConstants.OVERLOAD_POTION_4_ID);
            while(currentHealth > startingHealth - 48){
                MethodProvider.sleep(100);
                currentHealth = PublicStaticFinalConstants.hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
                PublicStaticFinalConstants.hostScriptReference.log("re-drinking overload... hp: " + currentHealth);
            }
        }
    }

    private void guzzleRockCakeTo1() throws InterruptedException {
        int currentHealth = PublicStaticFinalConstants.hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
        while(currentHealth > 1){
            Inventory inv = PublicStaticFinalConstants.hostScriptReference.getInventory();
            inv.interact(GUZZLE, PublicStaticFinalConstants.DWARVEN_ROCK_CAKE_ID);
            MethodProvider.sleep(PublicStaticFinalConstants.randomNormalDist(PublicStaticFinalConstants.RS_GAME_TICK_MS, 60.0));
            currentHealth = PublicStaticFinalConstants.hostScriptReference.getSkills().getDynamic(Skill.HITPOINTS);
            PublicStaticFinalConstants.hostScriptReference.log("re-guzzling rockcake... hp: " + currentHealth);
        }
    }


    private boolean isAbsorptionLow() throws InterruptedException {
        Inventory inv = PublicStaticFinalConstants.hostScriptReference.getInventory();
        int absorptionLvl = getAbsorptionLvl();
        if(absorptionLvl < 100){
            while(absorptionLvl <= 200 && doesPlayerHaveAbsorptionsLeft()){
                PublicStaticFinalConstants.hostScriptReference.log("absorptionLvl: " + absorptionLvl);
                inv.interact(DRINK, PublicStaticFinalConstants.ABSORPTION_POTION_1_ID, PublicStaticFinalConstants.ABSORPTION_POTION_2_ID,
                        PublicStaticFinalConstants.ABSORPTION_POTION_3_ID, PublicStaticFinalConstants.ABSORPTION_POTION_4_ID);
                absorptionLvl = getAbsorptionLvl();
                MethodProvider.sleep(PublicStaticFinalConstants.randomNormalDist(PublicStaticFinalConstants.RS_GAME_TICK_MS, 60.0));
            }
            return true;
        }
        return false;
    }

    private boolean doesPlayerHaveAbsorptionsLeft(){
        Inventory inv = PublicStaticFinalConstants.hostScriptReference.getInventory();
        return inv.contains(PublicStaticFinalConstants.ABSORPTION_POTION_1_ID) || inv.contains(PublicStaticFinalConstants.ABSORPTION_POTION_2_ID)
                || inv.contains(PublicStaticFinalConstants.ABSORPTION_POTION_3_ID) || inv.contains(PublicStaticFinalConstants.ABSORPTION_POTION_4_ID);
    }

    private int getAbsorptionLvl() {
        RS2Widget widget = PublicStaticFinalConstants.hostScriptReference.getWidgets().get(202, 1, 9);
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
                PublicStaticFinalConstants.hostScriptReference.log("recieved overload worn off msg");
            }
        }
    }

    @Override
    public int compareTo(ExecutableNode o) {
        return this.getKey() - o.getKey();
    }
}

package Nodes.NotInCheatCaves;

import ScriptClasses.MarkovNodeExecutor;
import ScriptClasses.Paint.ScriptStatusPainter;
import ScriptClasses.Util.Statics;
import ScriptClasses.Util.SupplierWithCE;
import org.osbot.rs07.api.Dialogues;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.event.WalkingEvent;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.concurrent.ThreadLocalRandom;

public class DreamEntryNode implements MarkovNodeExecutor.ExecutableNode{

    private static MarkovNodeExecutor.ExecutableNode singleton;
    private Script script;
    private static final Area NMZ = new Area(2601, 3116, 2606, 3113);

    private int numAbsorptions, numOverloads;

    public static MarkovNodeExecutor.ExecutableNode getSingleton(Script script, int numAbsorptions, int numOverloads){
        if(singleton == null){
            singleton = new DreamEntryNode(script, numAbsorptions, numOverloads);
        }
        return singleton;
    }

    private DreamEntryNode(Script script, int numAbsorptions, int numOverloads){
        this.script = script;
        this.numAbsorptions = numAbsorptions;
        this.numOverloads = numOverloads;
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        NPC dominicOnion = script.getNpcs().closestThatContains("Dominic Onion");
        return dominicOnion != null && dominicOnion.exists();
    }

    @Override
    public int executeNode() throws InterruptedException {
        ScriptStatusPainter.setCurrentScriptStatus(ScriptStatusPainter.ScriptStatus.OUTSIDE_DREAM_PREP);
        ScriptStatusPainter.setCurrentMarkovStatus(ScriptStatusPainter.MarkovStatus.DREAM_ENTRY_NODE);
        Inventory inv = script.getInventory();
        if (inv.contains(Statics.LOCATOR_ORB_ID) ^ inv.contains(Statics.ROCK_CAKE_ID)) {
            if (inv.getAmount(Statics.OVERLOAD_POTION_4_ID) == numOverloads &&
                    inv.getAmount(Statics.ABSORPTION_POTION_4_ID) == numAbsorptions) {
                if(executeStep(this::hasEnoughGP)){
                    if(executeStep(this::dominicInteraction)){
                        if(executeStep(this::enterDream)){
                            Statics.longRandomPause();
                            return 3000;
                        } else return stop("Script Error: error with entering dream interactions");
                    } else return stop("Script Error: error with dominic onion interaction");
                } else return stop("Stopping: not enough RSGP for dream");
            }
            else{ //if current loadout of absorptions/overloads don't abide by what was set, re-withdraw and correct.
                int failCount = 0;
                if(executeStep(this::storeOverloads)){
                    if(executeStep(this::storeAbsorptions)){
                        if(executeStep(this::withdrawOverloads)){
                            if(executeStep(this::withdrawAbsorptions)){
                                if(executeStep(this::hasEnoughGP)){
                                    if(executeStep(this::dominicInteraction)){
                                        if(executeStep(this::enterDream)){
                                            Statics.longRandomPause(); //Tip of the Spear ------------------------------------------------>
                                            return 3000;
                                        } else return stop("Script Error: error with entering dream interactions");
                                    } else return stop("Script Error: error with dominic onion interaction");
                                } else return stop("Stopping: not enough RSGP for dream");
                            } else return stop("Script Error: failure with withdrawing absorption interaction");
                        } else return stop("Script Error: failure with withdrawing overload interaction");
                    } else return stop("Script Error: failure with storing absorption interaction");
                } else return stop("Script Error: failure with storing overload interaction");
            }
        } else return stop("Stopping: Inventory does not contain a locator orb or rock cake! (Or it has both)");
    }

    //interaction methods (below methods) can return false due to random errors, allow up to 5 times to execute properly.
    private boolean executeStep(SupplierWithCE<Boolean, InterruptedException> f) throws InterruptedException{
        boolean result = f.get();
        int attempts = 0;
        while(!result && attempts < 5){
            result = f.get();
            attempts++;
        }
        return result;
    }

    private boolean enterDream(){
        RS2Object entryPotion = script.getObjects().closestThatContains("Potion");
        if(entryPotion != null && entryPotion.interact("Drink")){
            final RS2Widget[] accept = new RS2Widget[1];
            new ConditionalSleep(5000){
                @Override
                public boolean condition() throws InterruptedException {
                    accept[0] = script.getWidgets().getWidgetContainingText(129, "Accept");
                    return accept[0] != null && accept[0].isVisible();
                }
            }.sleep();
            if(accept[0] != null && accept[0].isVisible()){
               return accept[0].interact("Continue");
            }
        }
        return false;
    }

    private boolean hasEnoughGP() {
        WalkingEvent walk = new WalkingEvent(NMZ);
        script.execute(walk);
        new ConditionalSleep(10000) {
            @Override
            public boolean condition() throws InterruptedException {
                return walk.hasFinished();
            }
        }.sleep();

        RS2Widget coffer = script.getWidgets().singleFilter(207, (Filter<RS2Widget>) rs2Widget -> rs2Widget.getItemId() == 995);

        return coffer != null && coffer.getItemAmount() >= 26000;
    }

    private boolean dominicInteraction() throws InterruptedException {
        NPC dominicOnion = script.getNpcs().closestThatContains("Dominic Onion");
        if(dominicOnion != null && dominicOnion.interact("Dream")){
            Dialogues dialogues = script.getDialogues();
            new ConditionalSleep(5000){
                @Override
                public boolean condition() throws InterruptedException {
                    return dialogues.inDialogue();
                }
            }.sleep();
            return talkToDominic();
        }
        return false;
    }

    private boolean talkToDominic() throws InterruptedException {
        Dialogues dialogues = script.getDialogues();

        if(dialogues.inDialogue()){
            if(dialogues.isPendingOption()){
                if(dialogues.selectOption("Previous: Customisable Rumble (hard)")){
                    return dialogues.completeDialogue("Yes");
                }
                else{
                    if(dialogues.selectOption(3)){
                        Statics.shortRandomPause();
                        if(dialogues.selectOption(4)){
                            Statics.shortRandomPause();
                            return dialogues.completeDialogue("Yes");
                        }
                    }
                }
            }
            else return dialogues.completeDialogue("No, don't cancel it.");
        }
        return false;
    }

    private boolean storeOverloads() throws InterruptedException {
        RS2Object overloadStorage = script.getObjects().closestThatContains("Overload");
        if(inventoryHasOverload()){
            Statics.shortRandomPause();
            if(overloadStorage != null && overloadStorage.interact("Store")) {
                Dialogues dialogues = script.getDialogues();
                new ConditionalSleep(10000){
                    @Override
                    public boolean condition() throws InterruptedException {
                        return dialogues.isPendingOption();
                    }
                }.sleep();
                if(dialogues.isPendingOption()) {
                    return dialogues.completeDialogue("Yes, please.");
                }
            }
            return false;
        }
        return true;
    }

    private boolean storeAbsorptions() throws InterruptedException {
        RS2Object absorptionStorage = script.getObjects().closestThatContains("Absorption");
        if(inventoryHasAbsorptions()){
            Statics.shortRandomPause();
            if(absorptionStorage != null && absorptionStorage.interact("Store")) {
                Dialogues dialogues = script.getDialogues();
                new ConditionalSleep(10000){
                    @Override
                    public boolean condition() throws InterruptedException {
                        return dialogues.isPendingOption();
                    }
                }.sleep();
                if(dialogues.isPendingOption()) {
                    return dialogues.completeDialogue("Yes, please.");
                }
            }
            return false;
        }
        return true;
    }

    private boolean withdrawOverloads() throws InterruptedException {
        Statics.shortRandomPause();
        RS2Object overloadStorage = script.getObjects().closestThatContains("Overload");
        if(overloadStorage != null && overloadStorage.interact("Take")){
            final RS2Widget[] numberEntryDialog = new RS2Widget[1];
            new ConditionalSleep(5000){
                @Override
                public boolean condition() throws InterruptedException {
                    numberEntryDialog[0] = script.getWidgets().getWidgetContainingText(162, "How many doses of");
                    return numberEntryDialog[0] != null && numberEntryDialog[0].isVisible();
                }
            }.sleep();

            if(numberEntryDialog[0] != null){
                if(numberEntryDialog[0].isVisible()){
                    String msg = numberEntryDialog[0].getMessage();
                    int numLeft = Integer.parseInt(msg.substring(msg.indexOf("(") + 1, msg.indexOf(")")));
                    if(numOverloads * 4 <= numLeft){
                        if(script.getKeyboard().typeString(String.valueOf(numOverloads * 4))){
                            MethodProvider.sleep(1000);
                            return script.getInventory().getAmount(Statics.OVERLOAD_POTION_4_ID) == numOverloads;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean withdrawAbsorptions() throws InterruptedException {
        Statics.shortRandomPause();
        RS2Object absorptionStorage = script.getObjects().closestThatContains("Absorption");
        if(absorptionStorage != null && absorptionStorage.interact("Take")) {
            final RS2Widget[] numberEntryDialog = new RS2Widget[1];
            new ConditionalSleep(5000){
                @Override
                public boolean condition() throws InterruptedException {
                    numberEntryDialog[0] = script.getWidgets().getWidgetContainingText(162, "How many doses of");
                    return numberEntryDialog[0] != null && numberEntryDialog[0].isVisible();
                }
            }.sleep();
            if (numberEntryDialog[0] != null) {
                if (numberEntryDialog[0].isVisible()) {
                    String msg = numberEntryDialog[0].getMessage();
                    int numLeft = Integer.parseInt(msg.substring(msg.indexOf("(") + 1, msg.indexOf(")")));
                    if (numAbsorptions * 4 <= numLeft) {
                        int rand = ThreadLocalRandom.current().nextInt(numAbsorptions * 5, 1000);
                        if(script.getKeyboard().typeString(String.valueOf(rand))){
                            MethodProvider.sleep(1000);
                            return script.getInventory().getAmount(Statics.ABSORPTION_POTION_4_ID) == numAbsorptions;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean inventoryHasAbsorptions(){
        Inventory inv = script.getInventory();
        return inv.contains(Statics.ABSORPTION_POTION_1_ID) || inv.contains(Statics.ABSORPTION_POTION_2_ID)
                || inv.contains(Statics.ABSORPTION_POTION_3_ID) || inv.contains(Statics.ABSORPTION_POTION_4_ID);
    }

    private boolean inventoryHasOverload(){
        Inventory inv = script.getInventory();
        return inv.contains(Statics.OVERLOAD_POTION_1_ID) || inv.contains(Statics.OVERLOAD_POTION_2_ID)
                || inv.contains(Statics.OVERLOAD_POTION_3_ID) || inv.contains(Statics.OVERLOAD_POTION_4_ID);
    }

    private int stop(String reason){
        script.log(reason);
        script.stop(false);
        return 0;
    }

    @Override
    public boolean doConditionalTraverse() {
        return false;
    }
}

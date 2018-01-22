package ScriptClasses;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.script.Script;

import java.awt.Point;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class PublicStaticFinalConstants {

    public static final int RS_GAME_TICK_MS = 603;
    //items
    public static final int ABSORPTION_POTION_4_ID = 11734;
    public static final int ABSORPTION_POTION_3_ID = 11735;
    public static final int ABSORPTION_POTION_2_ID = 11736;
    public static final int ABSORPTION_POTION_1_ID = 11737;
    public static final int OVERLOAD_POTION_4_ID = 11730;
    public static final int OVERLOAD_POTION_3_ID = 11731;
    public static final int OVERLOAD_POTION_2_ID = 11732;
    public static final int OVERLOAD_POTION_1_ID = 11733;
    public static final int SUPER_RANGING_4_ID = 11722;
    public static final int SUPER_RANGING_3_ID = 11723;
    public static final int SUPER_RANGING_2_ID = 11724;
    public static final int SUPER_RANGING_1_ID = 11725;
    public static final int DWARVEN_ROCK_CAKE_ID = 7510;
    //npc
    public static final int DOMINIC_ONION_ID = 1120;
    //actions
    public final static String DRINK = "Drink";
    public final static String GUZZLE = "Guzzle";
    //area
    public final static Area OUTSIDE_NMZ = new Area(2604, 3111, 2614, 3119);
    //parameters from GUI
    public static boolean usingOverload;
    public static boolean usingSuperRanging;
    public static boolean usingAbsorptions;
    //for paint
    public enum ScriptStatus {
        PREPARING, AFKING, OVERLOADING, ABSORPTIONS, GUZZLING_ROCKCAKES;
    }
    public enum MeleeCombatStyle {
        ATK, STR, DEF, CTRL;
    }

    public static ScriptStatus currentScriptStatus;

    public static Script hostScriptReference;

    private PublicStaticFinalConstants(){} //meant to be a constant provider, no constructor

    public static void setHostScriptReference(Script ref){
        hostScriptReference = ref;
    }

    public static long randomNormalDist(double mean, double stddev){
        long debug = (long) ((new Random().nextGaussian() * stddev + mean));
        return Math.abs(debug); //in case we get a negative number
    }

    private static boolean hoverOverArea(Point upperLeftBound, Point lowerRightBound, Script hostScriptReference){
        int randX = ThreadLocalRandom.current().nextInt(upperLeftBound.x, lowerRightBound.x);
        int randY = ThreadLocalRandom.current().nextInt(upperLeftBound.y, lowerRightBound.y);
        return !hostScriptReference.getMouse().move(randX, randY);
    }

    public static void setCurrentScriptStatus(ScriptStatus currentScriptStatus) {
        PublicStaticFinalConstants.currentScriptStatus = currentScriptStatus;
    }

    public static String getCurrentScriptStatus() {
        if(currentScriptStatus != null){
            return currentScriptStatus.toString();
        }
        return "NULL";
    }


}

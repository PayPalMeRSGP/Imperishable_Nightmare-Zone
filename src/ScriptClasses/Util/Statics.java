package ScriptClasses.Util;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.script.Script;

import java.awt.*;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Statics {

    //onMessage triggers
    public final static String OVERLOAD_DEPLETED_MSG = "The effects of overload have worn off";
    public final static String DREAM_OVER_MSG = "You wake up feeling refreshed";
    public final static String POWER_SURGE = "Power surge";

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
    public static final int ROCK_CAKE_ID = 7510;
    public static final int LOCATOR_ORB_ID = 22081;
    //npc
    public static final int DOMINIC_ONION_ID = 1120;
    //actions
    public final static String DRINK = "Drink";
    public final static String GUZZLE = "Guzzle";
    public final static String FEEL = "Feel";
    //area
    public final static Area OUTSIDE_NMZ = new Area(2604, 3111, 2614, 3119);
    //parameters from GUI
    public static boolean usingOverload;
    public static boolean usingSuperRanging;
    public static boolean usingAbsorptions;

    public static Script hostScriptReference;

    private Statics(){} //meant to be a constant provider, no constructor

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

}

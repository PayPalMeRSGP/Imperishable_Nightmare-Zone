package ScriptClasses;

import org.osbot.rs07.script.Script;

import java.awt.Point;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ConstantsAndStatics {

    public static final int RS_GAME_TICK_MS = 603;
    //items
    public static final int ABSORPTION_POTION_ID = 11735;
    public static final int OVERLOAD_ID = 11731;
    public static final int DWARVEN_ROCK_CAKE_ID = 7509;
    //npc
    public static final int DOMINIC_ONION = 1120;


    public static Script hostScriptReference;

    private ConstantsAndStatics(){} //meant to be a constant provider, no constructor

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

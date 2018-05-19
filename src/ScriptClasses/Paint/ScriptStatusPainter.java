package ScriptClasses.Paint;

import java.util.Timer;
import java.util.TimerTask;

public class ScriptStatusPainter {
    //for paint status
    private static ScriptStatus currentScriptStatus;
    public enum ScriptStatus {
        PREPARING, AFKING, OVERLOADING, ABSORPTIONS, GUZZLING_ROCKCAKES, RAPID_HEAL_FLICK, SPECIAL_ATK
    }

    public static void setCurrentScriptStatus(ScriptStatus currentScriptStatus) {
        ScriptStatusPainter.currentScriptStatus = currentScriptStatus;
    }

    public static ScriptStatus getCurrentScriptStatus() {
        return currentScriptStatus;
    }

    //-----------------------------------------------------------------------------------------------------------------------
    //for paint to show current node
    private static MarkovStatus currentMarkovStatus;
    private static int onLoopsB4Switch;
    public enum MarkovStatus {
        ACTIVE_NODE, AFK_NODE, PREP_NODE
    }

    public static int getOnLoopsB4Switch() {
        return onLoopsB4Switch;
    }

    public static void setOnLoopsB4Switch(int onLoopsB4Switch) {
        ScriptStatusPainter.onLoopsB4Switch = onLoopsB4Switch;
    }

    public static void setCurrentMarkovStatus(MarkovStatus currentMarkovStatus) {
        ScriptStatusPainter.currentMarkovStatus = currentMarkovStatus;
    }

    public static MarkovStatus getCurrentMarkovStatus() {
        return currentMarkovStatus;
    }


    //-----------------------------------------------------------------------------------------------------------------------
    //the below timers are only for paint, at the end of the timer the supposed action is not triggered by the below code
    //for overload timer in paint
    private static Timer overloadTimer;
    private static int overloadSecondsLeft;

    public static void startOverloadTimer() {
        if(overloadTimer != null){
            overloadTimer.cancel();
        }

        overloadTimer = new Timer();
        overloadSecondsLeft = 300;
        overloadTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(--overloadSecondsLeft <= 0){
                    overloadTimer.cancel();
                }
            }
        }, 0, 1000);
    }

    public static int getOverloadSecondsLeft() {
        return overloadSecondsLeft;
    }

    //-----------------------------------------------------------------------------------------------------------------------
    //for prayer flick timer in paint
    private static Timer prayerFlickTimer;
    private static int secondsTilNextFlick;

    public static void startPrayerFlickTimer(int s) {
        if(prayerFlickTimer != null){
            prayerFlickTimer.cancel();
        }
        prayerFlickTimer = new Timer();
        secondsTilNextFlick = s;
        prayerFlickTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(--secondsTilNextFlick <= 0){
                    prayerFlickTimer.cancel();
                }
            }
        }, 0 , 1000);
    }

    public static int getSecondsTilNextFlick() {
        return secondsTilNextFlick;
    }

}

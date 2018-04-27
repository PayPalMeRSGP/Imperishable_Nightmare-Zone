package ScriptClasses.Paint;

import java.util.Timer;
import java.util.TimerTask;

public class ScriptStatusPainter {
    //for paint status
    private static ScriptStatus currentScriptStatus;
    public enum ScriptStatus {
        PREPARING, AFKING, OVERLOADING, ABSORPTIONS, GUZZLING_ROCKCAKES, RAPID_HEAL_FLICK, SPECIAL_ATK
    }

    //for paint to show current markov node
    private static MarkovStatus currentMarkovStatus;
    public enum MarkovStatus {
        ACTIVE, AFK, PREP
    }

    public static MarkovStatus getCurrentMarkovStatus() {
        return currentMarkovStatus;
    }

    public static void setCurrentMarkovStatus(MarkovStatus currentMarkovStatus) {
        ScriptStatusPainter.currentMarkovStatus = currentMarkovStatus;
    }

    public static void setCurrentScriptStatus(ScriptStatus currentScriptStatus) {
        ScriptStatusPainter.currentScriptStatus = currentScriptStatus;
    }

    public static ScriptStatus getCurrentScriptStatus() {
        return currentScriptStatus;
    }


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

    //for markov node switch timer in paint
    private static Timer markovSwitchTimer;
    private static int secondsTilMarkovSwitch;

    public static void startMarkovSwitchTimer(int s){
        if(markovSwitchTimer != null){
            markovSwitchTimer.cancel();
        }
        markovSwitchTimer = new Timer();
        secondsTilMarkovSwitch = s;
        markovSwitchTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(--secondsTilMarkovSwitch <= 0){
                    markovSwitchTimer.cancel();
                }
            }
        }, 0, 1000);
    }

    public static int getSecondsTilMarkovSwitch() {
        return secondsTilMarkovSwitch;
    }
}

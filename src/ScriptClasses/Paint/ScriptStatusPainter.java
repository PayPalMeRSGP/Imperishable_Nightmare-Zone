package ScriptClasses.Paint;

import java.util.Timer;
import java.util.TimerTask;

public class ScriptStatusPainter {
    //for paint status
    private static ScriptStatus currentScriptStatus;
    public enum ScriptStatus {
        PREPARING, AFKING, OVERLOADING, ABSORPTIONS, GUZZLING_ROCKCAKES, RAPID_HEAL_FLICK, SPECIAL_ATK;
    }

    public static void setCurrentScriptStatus(ScriptStatus currentScriptStatus) {
        ScriptStatusPainter.currentScriptStatus = currentScriptStatus;
    }

    public static ScriptStatus getCurrentScriptStatus() {
        return currentScriptStatus;
    }

    //for overload timer in paint
    private static Timer overloadTimer;
    private static int overloadSecondsLeft;

    public static void setOverloadTimer() {
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

    public static void setPrayerFlickTimer(int secondsTilNextFlick) {
        if(prayerFlickTimer != null){
            prayerFlickTimer.cancel();
        }
        prayerFlickTimer = new Timer();
        ScriptStatusPainter.secondsTilNextFlick = secondsTilNextFlick;
        prayerFlickTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(--ScriptStatusPainter.secondsTilNextFlick <= 0){
                    prayerFlickTimer.cancel();
                }
            }
        }, 0 , 1000);
    }

    public static int getSecondsTilNextFlick() {
        return secondsTilNextFlick;
    }
}

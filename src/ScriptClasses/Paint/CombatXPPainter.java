package ScriptClasses.Paint;

import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.API;

import java.util.Timer;
import java.util.TimerTask;

import static ScriptClasses.Util.Statics.hostScriptReference;

public class CombatXPPainter extends API{

    private enum CombatStyle {
        ATK, STR, DEF, CTRL, RNG
    }
    private CombatStyle style;


    //for paint timer of overload
    private Timer overloadTimer;
    private int overloadSecondsLeft;

    //for paint status
    private enum ScriptStatus {
        PREPARING, AFKING, OVERLOADING, ABSORPTIONS, GUZZLING_ROCKCAKES, RAPID_HEAL_FLICK, SPECIAL_ATK;
    }

    private static ScriptStatus currentScriptStatus;


    @Override
    public void initializeModule() {
        exchangeContext(getBot());
    }

    private CombatXPPainter(){}

    //call in onloop
    public void setCombatStyle(){
        int s = hostScriptReference.getConfigs().get(43);
        String equippedWeapon = getEquipment().getItemInSlot(EquipmentSlot.WEAPON.slot).toString();
        boolean isRanging = equippedWeapon.contains("bow") || equippedWeapon.contains("blowpipe")
                || equippedWeapon.contains("throwing") || equippedWeapon.contains("dart")
                || equippedWeapon.contains("knife");
        if(isRanging){
            style =  CombatStyle.RNG;
            return;
        }

        switch (s){
            case 0:
                style = CombatStyle.ATK;
                break;
            case 1:
                style = CombatStyle.STR;
                break;
            case 2:
                style = CombatStyle.CTRL;
                break;
            case 3:
                style = CombatStyle.DEF;
                break;
            default:
                hostScriptReference.log("WARNING: hit default case in setCombatStyle switch statement");
        }
    }

    public void setOverloadTimer(){
        overloadTimer = new Timer();
        overloadSecondsLeft = 300;
        overloadTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                overloadSecondsLeft--;
                if(overloadSecondsLeft <= 0){
                    overloadTimer.cancel();
                }
            }
        }, 0, 1000);

    }

    public int getOverloadSecondsLeft() {
        return overloadSecondsLeft;
    }

    public ScriptStatus getCurrentScriptStatus() {
        return currentScriptStatus;
    }

    public void setCurrentScriptStatus(ScriptStatus currentScriptStatus) {
        CombatXPPainter.currentScriptStatus = currentScriptStatus;
    }

    public CombatStyle getStyle() {
        return style;
    }

    //for not using controlled style
    public int getTrainingSkillLvl() {
        switch(style){
            case ATK:
                return getAtkLvl();
            case STR:
                return getStrLvl();
            case DEF:
                return getDefLvl();
            case RNG:
                return getRngLvl();
        }
        log("error in getTrainingSkillLvl, using controlled?");
        return 0;
    }

    public int getTrainingXpGained() {
        switch(style){
            case ATK:
                return getAtkXpGained();
            case STR:
                return getStrXpGained();
            case DEF:
                return getDefXpGained();
            case RNG:
                return getRngXpGained();
        }
        log("error in getTrainingXpGained, using controlled?");
        return 0;
    }

    public long getTrainingTTL() {
        switch(style){
            case ATK:
                return getAtkTTL();
            case STR:
                return getStrTTL();
            case DEF:
                return getDefTTL();
            case RNG:
                return getRngTTL();
        }
        log("error in getTrainingTTL, using controlled?");
        return 0;
    }

    public int getTrainingXPH() {
        switch(style){
            case ATK:
                return getAtkXPH();
            case STR:
                return getStrXPH();
            case DEF:
                return getDefXPH();
            case RNG:
                return getRngXPH();
        }
        log("error in getTrainingXPH, using controlled?");
        return 0;
    }

    //for using controlled style
    public int[] getAtkStrDefLvls(){
        return new int[]{getAtkLvl(), getStrLvl(), getDefLvl()};
    }

    //Levels
    private int getAtkLvl() {
        return getSkills().getStatic(Skill.ATTACK);
    }

    private int getStrLvl() {
        return getSkills().getStatic(Skill.STRENGTH);
    }

    private int getDefLvl() {
        return getSkills().getStatic(Skill.STRENGTH);
    }

    private int getRngLvl(){
        return getSkills().getStatic(Skill.RANGED);
    }

    public int getHpLvl(){
        return getSkills().getStatic(Skill.HITPOINTS);
    }

    //XP gained
    private int getAtkXpGained(){
        return getExperienceTracker().getGainedXP(Skill.ATTACK);
    }

    private int getStrXpGained(){
        return getExperienceTracker().getGainedXP(Skill.STRENGTH);
    }

    private int getDefXpGained(){
        return getExperienceTracker().getGainedXP(Skill.DEFENCE);
    }

    private int getRngXpGained(){
        return getExperienceTracker().getGainedXP(Skill.RANGED);
    }

    public int getHpXpGained(){
        return getExperienceTracker().getGainedXP(Skill.HITPOINTS);
    }

    //Time to Level
    private long getAtkTTL() {
        return getExperienceTracker().getTimeToLevel(Skill.ATTACK);
    }

    private long getStrTTL() {
        return getExperienceTracker().getTimeToLevel(Skill.STRENGTH);
    }

    private long getDefTTL() {
        return getExperienceTracker().getTimeToLevel(Skill.DEFENCE);
    }

    private long getRngTTL() {
        return getExperienceTracker().getTimeToLevel(Skill.RANGED);
    }

    public long getHpTTL() {
        return getExperienceTracker().getTimeToLevel(Skill.HITPOINTS);
    }

    //Xp per Hr
    private int getAtkXPH(){
        return getExperienceTracker().getGainedXPPerHour(Skill.ATTACK);
    }

    private int getStrXPH(){
        return getExperienceTracker().getGainedXPPerHour(Skill.STRENGTH);
    }

    private int getDefXPH(){
        return getExperienceTracker().getGainedXPPerHour(Skill.DEFENCE);
    }

    private int getRngXPH(){
        return getExperienceTracker().getGainedXPPerHour(Skill.RANGED);
    }

    public int getHpXPH(){
        return getExperienceTracker().getGainedXPPerHour(Skill.HITPOINTS);
    }





}

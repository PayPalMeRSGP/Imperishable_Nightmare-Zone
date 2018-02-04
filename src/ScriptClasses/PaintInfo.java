package ScriptClasses;

import org.osbot.rs07.api.Equipment;
import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;

import static ScriptClasses.PublicStaticFinalConstants.hostScriptReference;

public class PaintInfo {
    //for ctrl atk style. You need these variables separately
    private int atkLvl = 0;
    private int strLvl = 0;
    private int defLvl = 0;
    private long atkTTL = 0;
    private long strTTL = 0;
    private long defTTL = 0;

    //if using non ctrl atk style trainingSkillLvl is used. This can represent atk, str, def, or range.
    private int trainingSkillLvl = 0;
    private long trainingSkillTTL = 0;

    //these can be used in general to get the xp rate and gain of a combat skill.
    // if using controlled atk/str/def/rng will have the same xp gain
    private int trainingXpGained = 0;
    private int traiingXPH = 0;

    private int hpLvl = 0;
    private int hpXpGained = 0;
    private long hpTTL = 0;
    private int hpXPH = 0;

    //non-xp
    public enum ScriptStatus {
        PREPARING, AFKING, OVERLOADING, ABSORPTIONS, GUZZLING_ROCKCAKES, RAPID_HEAL_FLICK;
    }
    public enum CombatStyle {
        ATK, STR, DEF, CTRL, RNG;
    }
    private CombatStyle style;
    private ScriptStatus currentScriptStatus;

    private static PaintInfo singleton = null;

    private PaintInfo(){}

    public static PaintInfo getSingleton(Script hostScriptReference){
        if(singleton == null){
            singleton = new PaintInfo();
        }

        if(singleton.style != null){
            switch (singleton.style){
                case ATK:
                    singleton.trainingSkillLvl = hostScriptReference.getSkills().getStatic(Skill.ATTACK);
                    singleton.trainingXpGained = hostScriptReference.getExperienceTracker().getGainedXP(Skill.ATTACK);
                    singleton.trainingSkillTTL = hostScriptReference.getExperienceTracker().getTimeToLevel(Skill.ATTACK);
                    singleton.traiingXPH = hostScriptReference.getExperienceTracker().getGainedXPPerHour(Skill.ATTACK);
                    break;
                case STR:
                    singleton.trainingSkillLvl = hostScriptReference.getSkills().getStatic(Skill.STRENGTH);
                    singleton.trainingXpGained = hostScriptReference.getExperienceTracker().getGainedXP(Skill.STRENGTH);
                    singleton.trainingSkillTTL = hostScriptReference.getExperienceTracker().getTimeToLevel(Skill.STRENGTH);
                    singleton.traiingXPH = hostScriptReference.getExperienceTracker().getGainedXPPerHour(Skill.STRENGTH);
                    break;
                case CTRL:
                    singleton.atkLvl = hostScriptReference.getSkills().getStatic(Skill.ATTACK);
                    singleton.strLvl = hostScriptReference.getSkills().getStatic(Skill.STRENGTH);
                    singleton.defLvl = hostScriptReference.getSkills().getStatic(Skill.DEFENCE);
                    singleton.trainingXpGained = hostScriptReference.getExperienceTracker().getGainedXP(Skill.ATTACK);
                    singleton.atkTTL = hostScriptReference.getExperienceTracker().getTimeToLevel(Skill.ATTACK);
                    singleton.strTTL = hostScriptReference.getExperienceTracker().getTimeToLevel(Skill.STRENGTH);
                    singleton.defTTL = hostScriptReference.getExperienceTracker().getTimeToLevel(Skill.DEFENCE);
                    singleton.traiingXPH = hostScriptReference.getExperienceTracker().getGainedXPPerHour(Skill.ATTACK);
                    break;
                case DEF:
                    singleton.trainingSkillLvl = hostScriptReference.getSkills().getStatic(Skill.DEFENCE);
                    singleton.trainingXpGained = hostScriptReference.getExperienceTracker().getGainedXP(Skill.DEFENCE);
                    singleton.trainingSkillTTL = hostScriptReference.getExperienceTracker().getTimeToLevel(Skill.DEFENCE);
                    singleton.traiingXPH = hostScriptReference.getExperienceTracker().getGainedXPPerHour(Skill.DEFENCE);
                    break;
                case RNG:
                    singleton.trainingSkillLvl = hostScriptReference.getSkills().getStatic(Skill.RANGED);
                    singleton.trainingXpGained = hostScriptReference.getExperienceTracker().getGainedXP(Skill.RANGED);
                    singleton.trainingSkillTTL = hostScriptReference.getExperienceTracker().getTimeToLevel(Skill.RANGED);
                    singleton.traiingXPH = hostScriptReference.getExperienceTracker().getGainedXPPerHour(Skill.RANGED);
                    break;

            }
            singleton.hpXpGained = hostScriptReference.getExperienceTracker().getGainedXP(Skill.HITPOINTS);
            singleton.hpXPH = hostScriptReference.getExperienceTracker().getGainedXPPerHour(Skill.HITPOINTS);
            singleton.hpTTL = hostScriptReference.getExperienceTracker().getTimeToLevel(Skill.HITPOINTS);
            singleton.hpLvl = hostScriptReference.getSkills().getStatic(Skill.HITPOINTS);
        }
        return singleton;
    }

    //call in onloop
    public static void setCombatStyle(){
        int s = hostScriptReference.getConfigs().get(43);
        Equipment equipment = hostScriptReference.getEquipment();
        String weaponSlotItem = equipment.getItemInSlot(EquipmentSlot.WEAPON.slot).toString();
        boolean isRanging = weaponSlotItem.contains("bow") || weaponSlotItem.contains("blowpipe");
        if(isRanging){
            if(singleton != null){
                singleton.style =  CombatStyle.RNG;
            }
            return;
        }

        switch (s){
            case 0:
                singleton.style = CombatStyle.ATK;
                break;
            case 1:
                singleton.style = CombatStyle.STR;
                break;
            case 2:
                singleton.style = CombatStyle.CTRL;
                break;
            case 3:
                singleton.style = CombatStyle.DEF;
                break;
            default:
                hostScriptReference.log("WARNING: hit default case in setCombatStyle switch statement");
        }

    }

    public ScriptStatus getCurrentScriptStatus() {
        return currentScriptStatus;
    }

    public void setCurrentScriptStatus(ScriptStatus currentScriptStatus) {
        this.currentScriptStatus = currentScriptStatus;
    }

    public CombatStyle getStyle() {
        return style;
    }

    public int getAtkLvl() {
        return atkLvl;
    }

    public int getStrLvl() {
        return strLvl;
    }

    public int getDefLvl() {
        return defLvl;
    }

    public int getTrainingSkillLvl() {
        return trainingSkillLvl;
    }

    public int getTrainingXpGained() {
        return trainingXpGained;
    }

    public long getAtkTTL() {
        return atkTTL;
    }

    public long getStrTTL() {
        return strTTL;
    }

    public long getDefTTL() {
        return defTTL;
    }

    public long getTrainingSkillTTL() {
        return trainingSkillTTL;
    }

    public int getTraiingXPH() {
        return traiingXPH;
    }

    public int getHpLvl() {
        return hpLvl;
    }

    public int getHpXpGained() {
        return hpXpGained;
    }

    public long getHpTTL() {
        return hpTTL;
    }

    public int getHpXPH() {
        return hpXPH;
    }
}

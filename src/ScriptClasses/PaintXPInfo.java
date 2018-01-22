package ScriptClasses;

import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;

import static ScriptClasses.PublicStaticFinalConstants.MeleeCombatStyle;

public class PaintXPInfo {
    //for ctrl atk style. You need these variables separately
    private int atkLvl = 0;
    private int strLvl = 0;
    private int defLvl = 0;
    private long atkTTL = 0;
    private long strTTL = 0;
    private long defTTL = 0;

    //if using non ctrl atk style meleeLvl is used. This can represent atk, str, or def
    private int meleeLvl = 0;
    private long meleeTTL = 0;

    //these can be used in general, if using controlled atk/str/def will have the same xp gain and rate
    private int meleeXpGained = 0;
    private int meleeXPH = 0;

    private int hpLvl = 0;
    private int hpXpGained = 0;
    private long hpTTL = 0;
    private int hpXPH = 0;

    private static PaintXPInfo singleton = null;

    private PaintXPInfo(){}

    public static PaintXPInfo getSingleton(MeleeCombatStyle style, Script hostScriptReference){
        if(singleton == null){
            singleton = new PaintXPInfo();
        }
        if(style != null){
            switch (style){
                case ATK:
                    singleton.meleeLvl = hostScriptReference.getSkills().getStatic(Skill.ATTACK);
                    singleton.meleeXpGained = hostScriptReference.getExperienceTracker().getGainedXP(Skill.ATTACK);
                    singleton.meleeTTL = hostScriptReference.getExperienceTracker().getTimeToLevel(Skill.ATTACK);
                    singleton.meleeXPH = hostScriptReference.getExperienceTracker().getGainedXPPerHour(Skill.ATTACK);
                case STR:
                    singleton.strLvl = hostScriptReference.getSkills().getStatic(Skill.STRENGTH);
                    singleton.meleeXpGained = hostScriptReference.getExperienceTracker().getGainedXP(Skill.STRENGTH);
                    singleton.meleeTTL = hostScriptReference.getExperienceTracker().getTimeToLevel(Skill.STRENGTH);
                    singleton.meleeXPH = hostScriptReference.getExperienceTracker().getGainedXPPerHour(Skill.STRENGTH);
                case CTRL:
                    singleton.atkLvl = hostScriptReference.getSkills().getStatic(Skill.ATTACK);
                    singleton.strLvl = hostScriptReference.getSkills().getStatic(Skill.STRENGTH);
                    singleton.defLvl = hostScriptReference.getSkills().getStatic(Skill.DEFENCE);
                    singleton.meleeXpGained = hostScriptReference.getExperienceTracker().getGainedXP(Skill.ATTACK);
                    singleton.atkTTL = hostScriptReference.getExperienceTracker().getTimeToLevel(Skill.ATTACK);
                    singleton.strTTL = hostScriptReference.getExperienceTracker().getTimeToLevel(Skill.STRENGTH);
                    singleton.defTTL = hostScriptReference.getExperienceTracker().getTimeToLevel(Skill.DEFENCE);
                    singleton.meleeXPH = hostScriptReference.getExperienceTracker().getGainedXPPerHour(Skill.ATTACK);
                case DEF:
                    singleton.meleeLvl = hostScriptReference.getSkills().getStatic(Skill.DEFENCE);
                    singleton.meleeXpGained = hostScriptReference.getExperienceTracker().getGainedXP(Skill.DEFENCE);
                    singleton.meleeTTL = hostScriptReference.getExperienceTracker().getTimeToLevel(Skill.DEFENCE);
                    singleton.meleeXPH = hostScriptReference.getExperienceTracker().getGainedXPPerHour(Skill.DEFENCE);
            }
            singleton.hpXpGained = hostScriptReference.getExperienceTracker().getGainedXP(Skill.HITPOINTS);
            singleton.hpXPH = hostScriptReference.getExperienceTracker().getGainedXPPerHour(Skill.HITPOINTS);
            singleton.hpTTL = hostScriptReference.getExperienceTracker().getTimeToLevel(Skill.HITPOINTS);
            singleton.hpLvl = hostScriptReference.getSkills().getStatic(Skill.HITPOINTS);
        }
        return singleton;
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

    public int getMeleeLvl() {
        return meleeLvl;
    }

    public int getMeleeXpGained() {
        return meleeXpGained;
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

    public long getMeleeTTL() {
        return meleeTTL;
    }

    public int getMeleeXPH() {
        return meleeXPH;
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

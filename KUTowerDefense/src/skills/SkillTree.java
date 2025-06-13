package skills;

import java.util.HashSet;
import java.util.Set;

public class SkillTree {
    private static SkillTree instance;
    private final Set<SkillType> selectedSkills;

    private SkillTree() {
        selectedSkills = new HashSet<>();
    }

    public static SkillTree getInstance() {
        if (instance == null) {
            instance = new SkillTree();
        }
        return instance;
    }

    public void selectSkill(SkillType skill) {
        selectedSkills.add(skill);
    }

    public void unselectSkill(SkillType skill) {
        selectedSkills.remove(skill);
    }

    public boolean isSkillSelected(SkillType skill) {
        return selectedSkills.contains(skill);
    }

    public void clearSkills() {
        selectedSkills.clear();
    }

    public Set<SkillType> getSelectedSkills() {
        return new HashSet<>(selectedSkills); // Return a copy to prevent external modification
    }

    public void setSelectedSkills(Set<SkillType> skills) {
        selectedSkills.clear();
        selectedSkills.addAll(skills);
    }

    // Economy skill effects
    public int getStartingGold() {
        return isSkillSelected(SkillType.BOUNTIFUL_START) ? 150 : 0;
    }

    public int getExtraGoldPerKill() {
        return isSkillSelected(SkillType.PLUNDERER_BONUS) ? 1 : 0;
    }

    public float getInterestRate() {
        boolean isSelected = isSkillSelected(SkillType.INTEREST_SYSTEM);
        System.out.println("Interest System skill selected: " + isSelected);
        return isSelected ? 0.05f : 0f;
    }

    public int calculateInterest(int currentGold) {
        if (!isSkillSelected(SkillType.INTEREST_SYSTEM)) {
            System.out.println("Interest System not selected, no interest earned");
            return 0;
        }
        int interest = (int)(currentGold * getInterestRate());
        System.out.println("Calculating interest: " + currentGold + " gold * " + getInterestRate() + " = " + interest + " gold");
        return interest;
    }
}
package skills;

public enum SkillType {
    // Economy Skills
    BOUNTIFUL_START("Bereketli Başlangıç", "Oyuna +150 altın ile başla", 150),
    PLUNDERER_BONUS("Yağmacı Bonus", "Düşman öldürünce +1 altın ekstra", 1),
    INTEREST_SYSTEM("Faiz Sistemi", "Her dalga sonrası altınların %5'i kadar ekstra gelir", 0.05f),

    // Tower Skills (placeholder for now)
    SHARP_ARROW_TIPS("Keskin Ok Uçları", "Okçu kuleleri +10% hasar verir", 0.10f),
    EAGLE_EYE("Göz Gibi Görür", "Kule menzili +1 kare artar", 1),
    MAGIC_PIERCING("Büyülü Delici", "Mage kuleleri zırhlı hedeflere ekstra %20 hasar", 0.20f),

    // Ultimate Skills (placeholder for now)
    SHATTERING_FORCE("Sarsıcı Kuvvet", "Earthquake %25 daha fazla hasar verir", 0.25f),
    DIVINE_WRATH("Gökten Gelen Azap", "Lightning yeteneği %20 daha kısa sürede kullanılabilir", 0.20f),
    BATTLE_READINESS("Savaş Hazırlığı", "Tüm ultimatelerin cooldown süreleri %10 azalır", 0.10f);

    private final String name;
    private final String description;
    private final float value;

    SkillType(String name, String description, float value) {
        this.name = name;
        this.description = description;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public float getValue() {
        return value;
    }
} 
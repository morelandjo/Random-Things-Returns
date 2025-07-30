package lumien.randomthings.client.vfx;

public enum EFFECT {
    BLOOD_ROSE_DAMAGE(BloodRoseDamage.class),
    BLOOD_ROSE_SPREAD(BloodRoseSpread.class);

    private final Class<? extends VisualEffect> effectClass;

    EFFECT(Class<? extends VisualEffect> effectClass) {
        this.effectClass = effectClass;
    }

    public Class<? extends VisualEffect> getEffectClass() {
        return effectClass;
    }
}
package lumien.randomthings.client.vfx;

import net.minecraft.network.FriendlyByteBuf;

public abstract class VisualEffect {
    private int lifeTime;
    protected int tickCount;

    public VisualEffect(int lifeTime) {
        this.lifeTime = lifeTime;
    }

    public void readData(FriendlyByteBuf buffer) {
        // Override in subclasses to read specific data
    }

    public boolean tick() {
        this.tickCount++;
        return this.tickCount >= this.lifeTime;
    }

    public void renderInternal(float partialTick) {
        this.render(this.tickCount + partialTick);
    }

    public void render(float time) {
        // Override in subclasses to implement rendering
    }

    public void init() {
        // Override in subclasses for initialization
    }
}
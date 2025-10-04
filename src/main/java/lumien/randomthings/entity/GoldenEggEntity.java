package lumien.randomthings.entity;

import lumien.randomthings.item.ModItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class GoldenEggEntity extends ThrowableItemProjectile {

    public GoldenEggEntity(EntityType<? extends GoldenEggEntity> entityType, Level level) {
        super(entityType, level);
    }

    public GoldenEggEntity(Level level, LivingEntity shooter) {
        super(ModEntityTypes.GOLDEN_EGG.get(), shooter, level);
    }

    public GoldenEggEntity(Level level, double x, double y, double z) {
        super(ModEntityTypes.GOLDEN_EGG.get(), x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.GOLDEN_EGG.get();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            // Spawn golden egg break particles
            for (int i = 0; i < 8; ++i) {
                this.level().addParticle(
                    new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(this.getDefaultItem())),
                    this.getX(), this.getY(), this.getZ(),
                    ((double) this.random.nextFloat() - 0.5D) * 0.08D,
                    ((double) this.random.nextFloat() - 0.5D) * 0.08D,
                    ((double) this.random.nextFloat() - 0.5D) * 0.08D
                );
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        // Deal 1 damage to hit entity
        result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 1.0F);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (!this.level().isClientSide) {
            // Spawn golden chicken
            GoldenChickenEntity goldenChicken = new GoldenChickenEntity(ModEntityTypes.GOLDEN_CHICKEN.get(), this.level());
            goldenChicken.setPos(this.getX(), this.getY(), this.getZ());
            goldenChicken.setYRot(this.getYRot());
            this.level().addFreshEntity(goldenChicken);

            // Notify clients to spawn break particles
            this.level().broadcastEntityEvent(this, (byte) 3);

            // Remove the entity
            this.discard();
        }
    }
}

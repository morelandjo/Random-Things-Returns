package lumien.randomthings.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class GoldenChickenEntity extends Chicken {

    private int ingotDropTimer = 0;

    public GoldenChickenEntity(EntityType<? extends Chicken> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Chicken.createAttributes()
            .add(Attributes.MAX_HEALTH, 4.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide && this.isAlive()) {
            // Handle gold ingot dropping
            if (ingotDropTimer > 0 && --this.ingotDropTimer <= 0) {
                this.playSound(SoundEvents.CHICKEN_EGG, 1.0F,
                    (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.5F);
                this.spawnAtLocation(Items.GOLD_INGOT, 3);
            }

            // Handle gold ore consumption
            if (this.ingotDropTimer == 0) {
                AABB searchArea = this.getBoundingBox().inflate(0.5);
                List<ItemEntity> nearbyItems = this.level().getEntitiesOfClass(ItemEntity.class, searchArea);

                for (ItemEntity itemEntity : nearbyItems) {
                    ItemStack stack = itemEntity.getItem();

                    if (!stack.isEmpty() && isGoldOre(stack)) {
                        // Consume one gold ore
                        stack.shrink(1);
                        if (stack.isEmpty()) {
                            itemEntity.discard();
                        } else {
                            itemEntity.setItem(stack);
                        }

                        // Set timer to drop gold ingots (600-1200 ticks = 30-60 seconds)
                        this.ingotDropTimer = 600 + this.random.nextInt(600);
                        break;
                    }
                }
            }
        }
    }

    private boolean isGoldOre(ItemStack stack) {
        // Check for vanilla gold ores and raw gold
        return stack.is(Items.GOLD_ORE) ||
               stack.is(Items.DEEPSLATE_GOLD_ORE) ||
               stack.is(Items.RAW_GOLD) ||
               stack.is(Items.NETHER_GOLD_ORE);
    }

    @Override
    public Chicken getBreedOffspring(net.minecraft.server.level.ServerLevel level, AgeableMob otherParent) {
        return ModEntityTypes.GOLDEN_CHICKEN.get().create(level);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("ingotDropTimer", this.ingotDropTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.ingotDropTimer = compound.getInt("ingotDropTimer");
    }
}

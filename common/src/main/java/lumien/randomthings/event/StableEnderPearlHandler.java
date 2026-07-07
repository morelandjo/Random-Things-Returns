package lumien.randomthings.event;

import java.util.List;
import java.util.UUID;

import lumien.randomthings.item.ModItems;
import lumien.randomthings.util.RTDataKeys;
import lumien.randomthings.util.RTNbt;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Drives dropped Stable Ender Pearls (called from {@code ItemEntityMixin#tick}; the 1.21.1 source
 * used a Forge-only custom ItemEntity subclass instead). A tossed pearl counts down 7 seconds,
 * then teleports its bound player — or a random nearby entity — to where it lies, consuming itself.
 */
public final class StableEnderPearlHandler {

    private static final int TELEPORT_DELAY = 140; // 7 seconds
    private static final String KEY_TIMER = "StablePearlTeleportTimer";

    private StableEnderPearlHandler() {
    }

    public static void tick(ItemEntity itemEntity) {
        if (itemEntity.level().isClientSide || !itemEntity.isAlive()) {
            return;
        }
        ItemStack stack = itemEntity.getItem();
        if (!stack.is(ModItems.STABLE_ENDER_PEARL.get())) {
            return;
        }

        int timer;
        if (!RTNbt.has(stack, KEY_TIMER)) {
            timer = TELEPORT_DELAY;
            itemEntity.setPickUpDelay(TELEPORT_DELAY + 20);
        } else {
            timer = RTNbt.getInt(stack, KEY_TIMER);
        }
        timer--;
        RTNbt.setInt(stack, KEY_TIMER, timer);

        if (timer < 60 && timer % 10 == 0 && itemEntity.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.PORTAL,
                itemEntity.getX(), itemEntity.getY() + 0.5, itemEntity.getZ(),
                5, 0.5, 0.5, 0.5, 0.1);
        }

        if (timer <= 0) {
            performTeleport(itemEntity, stack);
        }
    }

    private static void performTeleport(ItemEntity itemEntity, ItemStack stack) {
        if (itemEntity.level() instanceof ServerLevel serverLevel) {
            UUID boundPlayerUUID = RTNbt.getUUID(stack, RTDataKeys.PLAYER_UUID);
            if (boundPlayerUUID != null) {
                ServerPlayer boundPlayer = serverLevel.getServer().getPlayerList().getPlayer(boundPlayerUUID);
                if (boundPlayer != null) {
                    teleportEntity(serverLevel, itemEntity, boundPlayer);
                    itemEntity.discard();
                    return;
                }
            }
            teleportRandomEntity(serverLevel, itemEntity);
        }
        itemEntity.discard();
    }

    private static void teleportEntity(ServerLevel serverLevel, ItemEntity pearl, Entity entity) {
        serverLevel.playSound(null, pearl.getX(), pearl.getY(), pearl.getZ(),
            SoundEvents.ENDERMAN_TELEPORT, pearl.getSoundSource(), 1.0F, 1.0F);
        serverLevel.sendParticles(ParticleTypes.PORTAL,
            entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
            20, 0.5, 0.5, 0.5, 0.1);

        entity.teleportTo(pearl.getX(), pearl.getY(), pearl.getZ());

        serverLevel.sendParticles(ParticleTypes.PORTAL,
            pearl.getX(), pearl.getY() + 0.5, pearl.getZ(),
            20, 0.5, 0.5, 0.5, 0.1);
        serverLevel.playSound(null, pearl.getX(), pearl.getY(), pearl.getZ(),
            SoundEvents.ENDERMAN_TELEPORT, pearl.getSoundSource(), 1.0F, 1.0F);
    }

    private static void teleportRandomEntity(ServerLevel serverLevel, ItemEntity pearl) {
        AABB searchArea = new AABB(
            pearl.getX() - 10, pearl.getY() - 10, pearl.getZ() - 10,
            pearl.getX() + 10, pearl.getY() + 10, pearl.getZ() + 10);

        List<Entity> nearbyEntities = serverLevel.getEntitiesOfClass(Entity.class, searchArea,
            entity -> entity != pearl && entity.isAlive() && !(entity instanceof ItemEntity));

        if (!nearbyEntities.isEmpty()) {
            Entity randomEntity = nearbyEntities.get(serverLevel.random.nextInt(nearbyEntities.size()));
            teleportEntity(serverLevel, pearl, randomEntity);
        }
    }
}

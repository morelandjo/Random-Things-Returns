package lumien.randomthings.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SummoningPendulumItem extends Item {

    public static final int MAX_ENTITIES = 5;

    public SummoningPendulumItem() {
        super(new Item.Properties()
            .stacksTo(1)
            .rarity(Rarity.RARE)
        );
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (entity.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // Cannot capture monsters or players
        if (entity instanceof Enemy || entity instanceof Player) {
            return InteractionResult.PASS;
        }

        // Get the actual stack from the player's hand to ensure we modify the right one
        ItemStack heldStack = player.getItemInHand(hand);

        // Get current stored entities
        List<CompoundTag> storedEntities = getStoredEntities(heldStack);

        if (storedEntities.size() < MAX_ENTITIES) {
            // Capture the entity
            CompoundTag entityNBT = new CompoundTag();
            // Use save() instead of saveWithoutId() to include the entity ID
            entity.save(entityNBT);

            // Add to stored entities
            storedEntities = new ArrayList<>(storedEntities); // Make mutable copy
            storedEntities.add(entityNBT);
            setStoredEntities(heldStack, storedEntities);

            // Remove entity from world
            entity.discard();

            // Play success sound
            player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 0.5f, 1.5f);
        } else {
            // Already full - play error sound
            player.level().playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE,
                SoundSource.PLAYERS, 0.5f, 1.5f);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());

        if (!level.isClientSide) {
            List<CompoundTag> storedEntities = getStoredEntities(stack);

            if (!storedEntities.isEmpty()) {
                // Release first entity
                CompoundTag entityNBT = storedEntities.get(0);

                // Create entity from NBT
                EntityType.loadEntityRecursive(entityNBT, level, entity -> {
                    // Set position
                    entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);

                    // Spawn entity
                    level.addFreshEntity(entity);

                    // Play success sound
                    level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                        SoundSource.PLAYERS, 0.5f, 0.5f);

                    return entity;
                });

                // Remove entity from stored list
                storedEntities = new ArrayList<>(storedEntities); // Make mutable copy
                storedEntities.remove(0);
                setStoredEntities(stack, storedEntities);
            } else {
                // No entities stored - play error sound
                level.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE,
                    SoundSource.PLAYERS, 0.5f, 0.2f);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        List<CompoundTag> storedEntities = getStoredEntities(stack);
        int entityCount = storedEntities.size();

        // If shift is held and there are entities, show detailed list
        if (Screen.hasShiftDown() && entityCount > 0) {
            for (CompoundTag entityNBT : storedEntities) {
                String entityId = entityNBT.getString("id");
                if (!entityId.isEmpty()) {
                    ResourceLocation entityLocation = ResourceLocation.parse(entityId);
                    String entityName = entityLocation.getPath();

                    // Try to get the proper translation key
                    String translationKey = "entity." + entityLocation.getNamespace() + "." + entityName;
                    tooltip.add(Component.literal("- ").append(Component.translatable(translationKey)));
                }
            }
        } else {
            // Show entity count
            String translationKey = entityCount == 1
                ? "tooltip.randomthings.summoning_pendulum.entityCount.singular"
                : "tooltip.randomthings.summoning_pendulum.entityCount.plural";
            tooltip.add(Component.translatable(translationKey, entityCount));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Show enchantment glint when full
        return getStoredEntities(stack).size() == MAX_ENTITIES;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        // Show durability bar when not full
        return getStoredEntities(stack).size() < MAX_ENTITIES;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int entityCount = getStoredEntities(stack).size();
        // Bar fills as entities are added (0 entities = empty bar, 5 entities = full bar)
        return Math.round(13.0f * entityCount / MAX_ENTITIES);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        // Purple color (matching DyeColor.PURPLE)
        return 0x8932B8;
    }

    /**
     * Get the list of stored entity NBT data
     */
    public static List<CompoundTag> getStoredEntities(ItemStack stack) {
        List<CompoundTag> entities = stack.get(ModDataComponents.SUMMONING_PENDULUM_ENTITIES.get());
        return entities != null ? entities : Collections.emptyList();
    }

    /**
     * Set the list of stored entity NBT data
     */
    public static void setStoredEntities(ItemStack stack, List<CompoundTag> entities) {
        if (entities.isEmpty()) {
            stack.remove(ModDataComponents.SUMMONING_PENDULUM_ENTITIES.get());
        } else {
            stack.set(ModDataComponents.SUMMONING_PENDULUM_ENTITIES.get(), entities);
        }
    }
}

package lumien.randomthings.item;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/** Captures up to five passive creatures and releases them elsewhere. */
public class SummoningPendulumItem extends Item {
    public static final int MAX_ENTITIES = 5;
    private static final String KEY = "SummoningEntities";

    public SummoningPendulumItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (entity.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (entity instanceof Enemy || entity instanceof Player) {
            return InteractionResult.PASS;
        }
        ItemStack heldStack = player.getItemInHand(hand);
        List<CompoundTag> stored = getStoredEntities(heldStack);
        if (stored.size() < MAX_ENTITIES) {
            CompoundTag entityNBT = new CompoundTag();
            entity.save(entityNBT);
            stored = new ArrayList<>(stored);
            stored.add(entityNBT);
            setStoredEntities(heldStack, stored);
            entity.discard();
            player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 1.5f);
        } else {
            player.level().playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.5f, 1.5f);
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
            List<CompoundTag> stored = getStoredEntities(stack);
            if (!stored.isEmpty()) {
                CompoundTag entityNBT = stored.get(0);
                EntityType.loadEntityRecursive(entityNBT, level, entity -> {
                    entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                    level.addFreshEntity(entity);
                    level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 0.5f);
                    return entity;
                });
                stored = new ArrayList<>(stored);
                stored.remove(0);
                setStoredEntities(stack, stored);
            } else {
                level.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.5f, 0.2f);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        List<CompoundTag> stored = getStoredEntities(stack);
        if (Screen.hasShiftDown() && !stored.isEmpty()) {
            for (CompoundTag entityNBT : stored) {
                String entityId = entityNBT.getString("id");
                if (!entityId.isEmpty()) {
                    ResourceLocation loc = new ResourceLocation(entityId);
                    tooltip.add(Component.literal("- ").append(Component.translatable("entity." + loc.getNamespace() + "." + loc.getPath())));
                }
            }
        } else {
            String key = stored.size() == 1
                ? "tooltip.randomthings.summoning_pendulum.entityCount.singular"
                : "tooltip.randomthings.summoning_pendulum.entityCount.plural";
            tooltip.add(Component.translatable(key, stored.size()));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return getStoredEntities(stack).size() == MAX_ENTITIES;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getStoredEntities(stack).size() < MAX_ENTITIES;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * getStoredEntities(stack).size() / MAX_ENTITIES);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x8932B8;
    }

    public static List<CompoundTag> getStoredEntities(ItemStack stack) {
        List<CompoundTag> list = new ArrayList<>();
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(KEY, Tag.TAG_LIST)) {
            ListTag listTag = tag.getList(KEY, Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                list.add(listTag.getCompound(i));
            }
        }
        return list;
    }

    public static void setStoredEntities(ItemStack stack, List<CompoundTag> entities) {
        if (entities.isEmpty()) {
            if (stack.getTag() != null) {
                stack.getTag().remove(KEY);
            }
        } else {
            ListTag listTag = new ListTag();
            listTag.addAll(entities);
            stack.getOrCreateTag().put(KEY, listTag);
        }
    }
}

package lumien.randomthings.item;

import lumien.randomthings.entity.SpectreIlluminatorEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;

/** Use on an illuminated chunk to remove its Spectre Illuminator (dropping it back as an item). */
public class BlackoutPowderItem extends Item {
    public BlackoutPowderItem() {
        super(new Properties());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.randomthings.blackout_powder").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!level.isClientSide && SpectreIlluminatorEntity.isChunkIlluminated(pos, level)) {
            ChunkPos chunkPos = level.getChunkAt(pos).getPos();
            AABB searchBox = new AABB(
                chunkPos.getMinBlockX() - 2, level.getMinBuildHeight(), chunkPos.getMinBlockZ() - 2,
                chunkPos.getMaxBlockX() + 2, level.getMaxBuildHeight(), chunkPos.getMaxBlockZ() + 2);
            List<SpectreIlluminatorEntity> illuminators = level.getEntitiesOfClass(SpectreIlluminatorEntity.class, searchBox);
            if (!illuminators.isEmpty()) {
                illuminators.get(0).discard();
                BlockPos spawnPos = pos.relative(context.getClickedFace());
                level.addFreshEntity(new ItemEntity(level, spawnPos.getX() + 0.5, spawnPos.getY() + 0.5, spawnPos.getZ() + 0.5,
                    new ItemStack(ModItems.SPECTRE_ILLUMINATOR.get())));
                context.getItemInHand().shrink(1);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}

package lumien.randomthings.item;

import lumien.randomthings.entity.SpectreIlluminatorEntity;
import lumien.randomthings.handler.spectreilluminator.SpectreIlluminationHandler;
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
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class BlackoutPowderItem extends Item {
    public BlackoutPowderItem() {
        super(new Properties());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.randomthings.blackout_powder").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (!level.isClientSide) {
            SpectreIlluminationHandler handler = SpectreIlluminationHandler.get(level);

            if (handler.isIlluminated(pos)) {
                // Find the spectre illuminator entity in this chunk
                LevelChunk chunk = level.getChunkAt(pos);
                ChunkPos chunkPos = chunk.getPos();

                AABB searchBox = new AABB(
                    chunkPos.getMinBlockX() - 2, level.getMinBuildHeight(), chunkPos.getMinBlockZ() - 2,
                    chunkPos.getMaxBlockX() + 2, level.getMaxBuildHeight(), chunkPos.getMaxBlockZ() + 2
                );

                List<SpectreIlluminatorEntity> illuminators = level.getEntitiesOfClass(
                    SpectreIlluminatorEntity.class, searchBox
                );

                if (!illuminators.isEmpty()) {
                    SpectreIlluminatorEntity first = illuminators.get(0);
                    first.discard();

                    // Spawn the item at the clicked block position
                    BlockPos spawnPos = pos.relative(context.getClickedFace());
                    ItemEntity itemEntity = new ItemEntity(
                        level,
                        spawnPos.getX() + 0.5,
                        spawnPos.getY() + 0.5,
                        spawnPos.getZ() + 0.5,
                        new ItemStack(ModItems.SPECTRE_ILLUMINATOR.get())
                    );
                    level.addFreshEntity(itemEntity);
                }

                // Toggle the chunk illumination off
                handler.toggleChunk(level, pos);

                // Consume the blackout powder
                context.getItemInHand().shrink(1);

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }
}

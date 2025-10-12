package lumien.randomthings.item;

import lumien.randomthings.entity.SpectreIlluminatorEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class SpectreIlluminatorItem extends Item {
    public SpectreIlluminatorItem() {
        super(new Properties());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.randomthings.spectre_illuminator").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (!level.isClientSide) {
            System.out.println("[SpectreIlluminator] Item used at " + pos);

            // Check if this chunk is already illuminated
            if (!SpectreIlluminatorEntity.isChunkIlluminated(pos, (net.minecraft.world.level.BlockAndTintGetter) level)) {
                ChunkPos chunkPos = new ChunkPos(pos);

                // Check if there's already an illuminator entity nearby
                AABB searchBox = new AABB(
                    chunkPos.getMinBlockX() - 2, level.getMinBuildHeight(), chunkPos.getMinBlockZ() - 2,
                    chunkPos.getMaxBlockX() + 2, level.getMaxBuildHeight(), chunkPos.getMaxBlockZ() + 2
                );

                List<SpectreIlluminatorEntity> existingIlluminators = level.getEntitiesOfClass(
                    SpectreIlluminatorEntity.class, searchBox
                );

                if (existingIlluminators.isEmpty()) {
                    System.out.println("[SpectreIlluminator] Spawning entity at " + pos);

                    // Spawn the illuminator entity
                    SpectreIlluminatorEntity illuminator = new SpectreIlluminatorEntity(
                        level,
                        pos.getX() + 0.5,
                        pos.getY() + 1.2,
                        pos.getZ() + 0.5
                    );

                    level.addFreshEntity(illuminator);

                    // Consume the item (unless in creative mode)
                    if (!context.getPlayer().getAbilities().instabuild) {
                        context.getItemInHand().shrink(1);
                    }

                    return InteractionResult.SUCCESS;
                } else {
                    System.out.println("[SpectreIlluminator] Already has illuminator nearby");
                }
            } else {
                System.out.println("[SpectreIlluminator] Chunk already illuminated");
            }
        }

        return level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }
}

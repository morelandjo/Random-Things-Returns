package lumien.randomthings.item;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.blockentity.FlooBrickBlockEntity;
import lumien.randomthings.handler.floo.FlooNetworkSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Right-click on a vanilla brick block to convert a connected cluster (max 20 blocks, 4-direction
 * horizontal flood-fill) into Floo Bricks, with the clicked position as the master. The destination
 * name is taken from the sign's anvil-renamed CUSTOM_NAME component.
 *
 * Mirrors upstream ItemFlooSign.java:37-129.
 */
public class FlooSignItem extends Item {
    public static final int MAX_FIREPLACE_SIZE = 20;

    public FlooSignItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (level.isClientSide || !(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        BlockState clickedState = level.getBlockState(clickedPos);
        if (!clickedState.is(Blocks.BRICKS)) {
            return InteractionResult.FAIL;
        }

        // Determine facing
        Direction facing;
        Direction clickedFace = context.getClickedFace();
        if (clickedFace.getAxis().isHorizontal()) {
            facing = clickedFace;
        } else {
            facing = player.getDirection().getOpposite();
        }

        // Flood-fill discovery (4-direction horizontal, max 20 blocks INCLUDING the clicked pos)
        List<BlockPos> connectedBricks = floodFill(level, clickedPos);
        if (connectedBricks == null) {
            return InteractionResult.FAIL;
        }
        // Master = clicked position. Children = the rest.
        List<BlockPos> children = new ArrayList<>();
        for (BlockPos p : connectedBricks) {
            if (!p.equals(clickedPos)) children.add(p);
        }

        UUID masterUUID = UUID.randomUUID();
        String name = readSignName(stack);

        FlooNetworkSavedData data = FlooNetworkSavedData.get(serverLevel);
        boolean created = data.createFireplace(serverPlayer.getUUID(), masterUUID, name, clickedPos, children);
        if (!created) {
            serverPlayer.sendSystemMessage(Component.translatable("floo.info.duplicate").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        // Replace blocks with Floo Bricks and initialize block entities
        BlockState flooBrickState = ModBlocks.FLOO_BRICK.get().defaultBlockState();
        level.setBlock(clickedPos, flooBrickState, 3);
        BlockEntity masterBE = level.getBlockEntity(clickedPos);
        if (masterBE instanceof FlooBrickBlockEntity flooMaster) {
            flooMaster.initMaster(masterUUID, facing, children);
        }
        for (BlockPos childPos : children) {
            level.setBlock(childPos, flooBrickState, 3);
            BlockEntity childBE = level.getBlockEntity(childPos);
            if (childBE instanceof FlooBrickBlockEntity flooChild) {
                flooChild.initChild(masterUUID);
            }
        }

        // Consume sign in non-creative
        if (!serverPlayer.isCreative()) {
            stack.shrink(1);
        }

        return InteractionResult.SUCCESS;
    }

    private static List<BlockPos> floodFill(Level level, BlockPos start) {
        List<BlockPos> ordered = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        java.util.Deque<BlockPos> queue = new java.util.ArrayDeque<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            if (ordered.size() >= MAX_FIREPLACE_SIZE) break;
            BlockPos cur = queue.poll();
            BlockState state = level.getBlockState(cur);

            // Reject if any block in the cluster is already a Floo Brick (upstream behaviour).
            if (state.is(ModBlocks.FLOO_BRICK.get())) {
                return null;
            }
            if (!state.is(Blocks.BRICKS)) continue;

            ordered.add(cur);

            for (Direction d : Direction.Plane.HORIZONTAL) {
                BlockPos n = cur.relative(d);
                if (visited.add(n)) {
                    queue.add(n);
                }
            }
        }
        return ordered;
    }

    private static String readSignName(ItemStack stack) {
        Component custom = stack.get(DataComponents.CUSTOM_NAME);
        if (custom == null) return null;
        String s = custom.getString().trim();
        return s.isEmpty() ? null : s;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.randomthings.floo_sign").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}

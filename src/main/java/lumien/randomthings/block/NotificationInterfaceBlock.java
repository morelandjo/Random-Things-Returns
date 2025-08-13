package lumien.randomthings.block;

import lumien.randomthings.blockentity.ModBlockEntityTypes;
import lumien.randomthings.blockentity.NotificationInterfaceBlockEntity;
import lumien.randomthings.menu.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import com.mojang.serialization.MapCodec;

public class NotificationInterfaceBlock extends BaseEntityBlock {
    public static final MapCodec<NotificationInterfaceBlock> CODEC = simpleCodec(properties -> new NotificationInterfaceBlock());

    @Override
    public MapCodec<NotificationInterfaceBlock> codec() {
        return CODEC;
    }

    public NotificationInterfaceBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(2.0f)
                .sound(SoundType.STONE));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NotificationInterfaceBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof NotificationInterfaceBlockEntity notificationInterface) {
                player.openMenu(notificationInterface, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide && placer instanceof ServerPlayer player && 
            level.getBlockEntity(pos) instanceof NotificationInterfaceBlockEntity blockEntity) {
            blockEntity.setPlayerUUID(player.getUUID());
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return false;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return false;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof NotificationInterfaceBlockEntity notificationInterface) {
                notificationInterface.neighborChanged();
            }
        }
    }
}
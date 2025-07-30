package lumien.randomthings.blockentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.client.vfx.EFFECT;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.network.RTPacketHandler;
import lumien.randomthings.network.messages.VisualEffectMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BloodRoseBlockEntity extends BlockEntity {
    BlockPos targetSpreadPos;
    
    int spreadTick;
    int damageTick;
    
    int progress;
    
    final static int MAX_PROGRESS = 20;

    public BloodRoseBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.BLOOD_ROSE.get(), pos, state);
        this.damageTick = 0;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BloodRoseBlockEntity blockEntity) {
        blockEntity.tick();
    }
    
    public void tick() {
        if (!this.level.isClientSide) {
            if (progress == MAX_PROGRESS) {
                damageTick++;
                
                if (damageTick % 20 == 0) {
                    List<LivingEntity> livingEntities = level.getEntitiesOfClass(LivingEntity.class, 
                        new AABB(worldPosition).inflate(2, 2, 2));
                    
                    for (LivingEntity entity : livingEntities) {
                        if (entity instanceof Player player) {
                            if (!player.isCreative() && !player.isSpectator()) {
                                entity.hurt(level.damageSources().magic(), 1.0F);
                                
                                player.getInventory().add(new ItemStack(ModItems.BLOOD_ROSE_PETAL.get()));
                                
                                FriendlyByteBuf buffer = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
                                // Origin (blood rose position)
                                buffer.writeFloat(worldPosition.getX() + 0.5f);
                                buffer.writeFloat(worldPosition.getY() + 0.5f);
                                buffer.writeFloat(worldPosition.getZ() + 0.5f);
                                // Destination (entity position)
                                buffer.writeFloat((float) entity.getX());
                                buffer.writeFloat((float) entity.getY() + 0.5f);
                                buffer.writeFloat((float) entity.getZ());
                                byte[] data = new byte[buffer.readableBytes()];
                                buffer.readBytes(data);
                                RTPacketHandler.sendToTracking(level, worldPosition, 
                                    new VisualEffectMessage(EFFECT.BLOOD_ROSE_DAMAGE, data));
                            }
                        } else {
                            entity.hurt(level.damageSources().magic(), 1.0F);
                            
                            FriendlyByteBuf buffer = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
                            // Origin (blood rose position)
                            buffer.writeFloat(worldPosition.getX() + 0.5f);
                            buffer.writeFloat(worldPosition.getY() + 0.5f);
                            buffer.writeFloat(worldPosition.getZ() + 0.5f);
                            // Destination (entity position)
                            buffer.writeFloat((float) entity.getX());
                            buffer.writeFloat((float) entity.getY() + 0.5f);
                            buffer.writeFloat((float) entity.getZ());
                            byte[] data = new byte[buffer.readableBytes()];
                            buffer.readBytes(data);
                            RTPacketHandler.sendToTracking(level, worldPosition, 
                                new VisualEffectMessage(EFFECT.BLOOD_ROSE_DAMAGE, data));
                        }
                    }
                }

                spreadTick++;
                
                if (spreadTick >= 20 * 10) {
                    spreadTick = 0;
                    
                    if (targetSpreadPos == null) {
                        List<BlockPos> validSpreadPositions = new ArrayList<>();
                        
                        for (BlockPos pos : BlockPos.betweenClosed(worldPosition.offset(-10, -3, -10), 
                                                                 worldPosition.offset(10, 3, 10))) {
                            if (level.getBlockState(pos).isAir() && 
                                level.getBlockState(pos.below()).is(net.minecraft.tags.BlockTags.DIRT)) {
                                validSpreadPositions.add(pos.immutable());
                            }
                        }
                        
                        if (!validSpreadPositions.isEmpty()) {
                            Collections.shuffle(validSpreadPositions);
                            targetSpreadPos = validSpreadPositions.get(0);
                            progress = 0;
                        }
                    } else {
                        progress++;
                        
                        if (progress >= MAX_PROGRESS) {
                            level.setBlock(targetSpreadPos, ModBlocks.BLOOD_ROSE.get().defaultBlockState(), 3);
                            
                            FriendlyByteBuf buffer = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
                            // Origin (blood rose position)
                            buffer.writeFloat(worldPosition.getX() + 0.5f);
                            buffer.writeFloat(worldPosition.getY() + 0.5f);
                            buffer.writeFloat(worldPosition.getZ() + 0.5f);
                            // Destination (target spread position)
                            buffer.writeFloat(targetSpreadPos.getX() + 0.5f);
                            buffer.writeFloat(targetSpreadPos.getY() + 0.5f);
                            buffer.writeFloat(targetSpreadPos.getZ() + 0.5f);
                            byte[] data = new byte[buffer.readableBytes()];
                            buffer.readBytes(data);
                            RTPacketHandler.sendToTracking(level, targetSpreadPos, 
                                new VisualEffectMessage(EFFECT.BLOOD_ROSE_SPREAD, data));
                            
                            targetSpreadPos = null;
                            progress = MAX_PROGRESS;
                        } else {
                            FriendlyByteBuf buffer = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
                            // Origin (blood rose position)
                            buffer.writeFloat(worldPosition.getX() + 0.5f);
                            buffer.writeFloat(worldPosition.getY() + 0.5f);
                            buffer.writeFloat(worldPosition.getZ() + 0.5f);
                            // Destination (target spread position)
                            buffer.writeFloat(targetSpreadPos.getX() + 0.5f);
                            buffer.writeFloat(targetSpreadPos.getY() + 0.5f);
                            buffer.writeFloat(targetSpreadPos.getZ() + 0.5f);
                            byte[] data = new byte[buffer.readableBytes()];
                            buffer.readBytes(data);
                            RTPacketHandler.sendToTracking(level, targetSpreadPos, 
                                new VisualEffectMessage(EFFECT.BLOOD_ROSE_SPREAD, data));
                        }
                    }
                }
            } else {
                progress++;
            }
        }
    }

    public static <T extends BlockEntity> BlockEntityTicker<T> createTicker() {
        return (level, pos, state, blockEntity) -> {
            if (blockEntity instanceof BloodRoseBlockEntity bloodRose) {
                bloodRose.tick();
            }
        };
    }
}
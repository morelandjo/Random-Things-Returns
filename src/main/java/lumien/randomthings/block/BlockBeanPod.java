package lumien.randomthings.block;

import lumien.randomthings.item.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

public class BlockBeanPod extends Block {
    
    public static final MapCodec<BlockBeanPod> CODEC = simpleCodec((properties) -> new BlockBeanPod());
    
    protected static final VoxelShape BEAN_POD_AABB = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    
    public BlockBeanPod() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .strength(1.5F)
                .sound(SoundType.WOOD)
                .noOcclusion());
    }

    @Override
    public MapCodec<BlockBeanPod> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BEAN_POD_AABB;
    }

    @Override
    public void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, tool, dropExperience);
        
        if (!level.isClientSide) {
            RandomSource random = level.getRandom();
            List<ItemStack> drops = generateBeanPodLoot(random);
            
            for (ItemStack drop : drops) {
                ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
                itemEntity.setDefaultPickUpDelay();
                level.addFreshEntity(itemEntity);
            }
        }
    }
    
    private List<ItemStack> generateBeanPodLoot(RandomSource random) {
        List<ItemStack> drops = new ArrayList<>();
        
        // Always drop some beans
        int beanCount = random.nextInt(3) + 2; // 2-4 beans
        drops.add(new ItemStack(ModItems.BEAN.get(), beanCount));
        
        // 60% chance for iron
        if (random.nextFloat() < 0.6f) {
            int ironCount = random.nextInt(3) + 1; // 1-3 iron ingots
            drops.add(new ItemStack(Items.IRON_INGOT, ironCount));
        }
        
        // 40% chance for gold
        if (random.nextFloat() < 0.4f) {
            int goldCount = random.nextInt(2) + 1; // 1-2 gold ingots
            drops.add(new ItemStack(Items.GOLD_INGOT, goldCount));
        }
        
        // 20% chance for diamonds
        if (random.nextFloat() < 0.2f) {
            int diamondCount = random.nextInt(2) + 1; // 1-2 diamonds
            drops.add(new ItemStack(Items.DIAMOND, diamondCount));
        }
        
        // 15% chance for emeralds
        if (random.nextFloat() < 0.15f) {
            int emeraldCount = random.nextInt(2) + 1; // 1-2 emeralds
            drops.add(new ItemStack(Items.EMERALD, emeraldCount));
        }
        
        // 5% chance for rare loot
        if (random.nextFloat() < 0.05f) {
            switch (random.nextInt(3)) {
                case 0:
                    drops.add(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1));
                    break;
                case 1:
                    drops.add(new ItemStack(Items.NETHERITE_INGOT, 1));
                    break;
                case 2:
                    drops.add(new ItemStack(Items.TOTEM_OF_UNDYING, 1));
                    break;
            }
        }
        
        return drops;
    }
}
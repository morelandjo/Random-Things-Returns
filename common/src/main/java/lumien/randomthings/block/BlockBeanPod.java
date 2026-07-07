package lumien.randomthings.block;

import lumien.randomthings.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

/** The treasure pod that crowns a magic beanstalk — breaking it yields beans and a chance of riches. */
public class BlockBeanPod extends Block {
    protected static final VoxelShape BEAN_POD_AABB = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    public BlockBeanPod() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.PLANT).strength(1.5F).sound(SoundType.WOOD).noOcclusion());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BEAN_POD_AABB;
    }

    @Override
    public void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, tool, dropExperience);
        if (!level.isClientSide) {
            for (ItemStack drop : generateBeanPodLoot(level.getRandom())) {
                ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
                itemEntity.setDefaultPickUpDelay();
                level.addFreshEntity(itemEntity);
            }
        }
    }

    private List<ItemStack> generateBeanPodLoot(RandomSource random) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(ModItems.BEAN.get(), random.nextInt(3) + 2));
        if (random.nextFloat() < 0.6f) {
            drops.add(new ItemStack(Items.IRON_INGOT, random.nextInt(3) + 1));
        }
        if (random.nextFloat() < 0.4f) {
            drops.add(new ItemStack(Items.GOLD_INGOT, random.nextInt(2) + 1));
        }
        if (random.nextFloat() < 0.2f) {
            drops.add(new ItemStack(Items.DIAMOND, random.nextInt(2) + 1));
        }
        if (random.nextFloat() < 0.15f) {
            drops.add(new ItemStack(Items.EMERALD, random.nextInt(2) + 1));
        }
        if (random.nextFloat() < 0.05f) {
            switch (random.nextInt(3)) {
                case 0 -> drops.add(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1));
                case 1 -> drops.add(new ItemStack(Items.NETHERITE_INGOT, 1));
                case 2 -> drops.add(new ItemStack(Items.TOTEM_OF_UNDYING, 1));
            }
        }
        drops.add(new ItemStack(ModItems.GOLDEN_EGG.get(), 1));
        return drops;
    }
}

package lumien.randomthings.item;

import lumien.randomthings.util.RTNbt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * Fluid storage and world interaction for the ender buckets, kept in item NBT
 * ("EnderBucketFluid" registry id + "EnderBucketAmount" in millibuckets). Works with any registered
 * {@link FlowingFluid} directly against world blocks — no loader fluid-capability APIs
 * (the 1.21.1 source only ever used its NeoForge fluid handler against world blocks too).
 */
public final class EnderBucketFluidHelper {

    public static final int BUCKET_VOLUME = 1000;
    private static final String KEY_FLUID = "EnderBucketFluid";
    private static final String KEY_AMOUNT = "EnderBucketAmount";

    private EnderBucketFluidHelper() {
    }

    public static Fluid getFluid(ItemStack stack) {
        ResourceLocation id = RTNbt.getResourceLocation(stack, KEY_FLUID);
        if (id == null) {
            return Fluids.EMPTY;
        }
        return BuiltInRegistries.FLUID.get(id);
    }

    public static int getAmount(ItemStack stack) {
        return RTNbt.getInt(stack, KEY_AMOUNT, 0);
    }

    public static void setFluid(ItemStack stack, Fluid fluid, int amount) {
        if (fluid == Fluids.EMPTY || amount <= 0) {
            RTNbt.remove(stack, KEY_FLUID);
            RTNbt.remove(stack, KEY_AMOUNT);
        } else {
            RTNbt.setResourceLocation(stack, KEY_FLUID, BuiltInRegistries.FLUID.getKey(fluid));
            RTNbt.setInt(stack, KEY_AMOUNT, amount);
        }
    }

    /** Whether the source block at {@code pos} can be added to the bucket's current content. */
    public static boolean canPickUp(ItemStack stack, Fluid worldFluid, int capacity) {
        Fluid stored = getFluid(stack);
        int amount = getAmount(stack);
        if (amount + BUCKET_VOLUME > capacity) {
            return false;
        }
        return stored == Fluids.EMPTY || sameFluid(stored, worldFluid);
    }

    private static boolean sameFluid(Fluid a, Fluid b) {
        // Treat flowing/source variants of the same fluid as equal
        FlowingFluid fa = a instanceof FlowingFluid f ? f : null;
        FlowingFluid fb = b instanceof FlowingFluid f ? f : null;
        if (fa != null && fb != null) {
            return fa.getSource() == fb.getSource();
        }
        return a == b;
    }

    /**
     * Removes the fluid source block at {@code pos} and stores one bucket's worth in the stack.
     * The caller must have verified the position holds a matching source block.
     */
    public static boolean pickUpSource(ItemStack stack, Player player, Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Fluid worldFluid = state.getFluidState().getType();
        if (!state.getFluidState().isSource() || !(state.getBlock() instanceof BucketPickup pickup)) {
            return false;
        }

        // pickupBlock removes the fluid (handles waterlogged blocks too); we track content ourselves
        ItemStack vanillaBucket = pickup.pickupBlock(level, pos, state);
        if (vanillaBucket.isEmpty()) {
            return false;
        }

        Fluid stored = getFluid(stack);
        setFluid(stack, stored == Fluids.EMPTY ? worldFluid : stored, getAmount(stack) + BUCKET_VOLUME);

        pickup.getPickupSound().ifPresent(sound ->
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F));
        level.gameEvent(player, net.minecraft.world.level.gameevent.GameEvent.FLUID_PICKUP, pos);
        return true;
    }

    /**
     * Places one bucket's worth of the stored fluid as a source block at {@code pos}
     * (vanilla BucketItem.emptyContents logic). Returns true if the fluid was consumed.
     */
    public static boolean placeFluid(Player player, Level level, BlockPos pos, ItemStack stack) {
        Fluid fluid = getFluid(stack);
        if (!(fluid instanceof FlowingFluid flowing) || getAmount(stack) < BUCKET_VOLUME) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        boolean replaceable = state.canBeReplaced(fluid);
        boolean canPlace = state.isAir() || replaceable
            || (block instanceof LiquidBlockContainer container && container.canPlaceLiquid(level, pos, state, fluid));

        if (!canPlace) {
            return false;
        }

        if (level.dimensionType().ultraWarm() && fluid.is(FluidTags.WATER)) {
            // Vaporize
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
            for (int i = 0; i < 8; i++) {
                level.addParticle(ParticleTypes.LARGE_SMOKE,
                    pos.getX() + Math.random(), pos.getY() + Math.random(), pos.getZ() + Math.random(), 0, 0, 0);
            }
        } else if (block instanceof LiquidBlockContainer container && fluid == Fluids.WATER) {
            container.placeLiquid(level, pos, state, flowing.getSource(false));
            playEmptySound(fluid, player, level, pos);
        } else {
            if (!level.isClientSide && replaceable && !state.liquid()) {
                level.destroyBlock(pos, true);
            }
            if (!level.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), 11)
                && !state.getFluidState().isSource()) {
                return false;
            }
            playEmptySound(fluid, player, level, pos);
        }

        setFluid(stack, fluid, getAmount(stack) - BUCKET_VOLUME);
        return true;
    }

    private static void playEmptySound(Fluid fluid, Player player, Level level, BlockPos pos) {
        SoundEvent sound = fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        level.playSound(player, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(player, net.minecraft.world.level.gameevent.GameEvent.FLUID_PLACE, pos);
    }

    /** Display name of the stored fluid, e.g. "Water". */
    public static net.minecraft.network.chat.Component fluidName(Fluid fluid) {
        return fluid.defaultFluidState().createLegacyBlock().getBlock().getName();
    }
}

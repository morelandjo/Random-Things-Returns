package lumien.randomthings.util;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

/**
 * Typed NBT accessors for item-stack data — the 1.20.1 replacement for {@code DataComponentType}
 * get/set. Reads are null/default-safe; writes lazily create the stack tag.
 *
 * <p>Keys come from {@link RTDataKeys}. For Codec-backed records ({@code PortkeyTarget},
 * {@code ItemFilterData}, {@code ChunkAnalyzerResult}, …) use {@link #setCodec}/{@link #getCodec}.</p>
 */
public final class RTNbt {

    private RTNbt() {
    }

    public static boolean has(ItemStack stack, String key) {
        return stack.hasTag() && stack.getTag().contains(key);
    }

    public static void remove(ItemStack stack, String key) {
        if (stack.hasTag()) {
            stack.getTag().remove(key);
        }
    }

    // --- primitives -------------------------------------------------------

    public static void setInt(ItemStack stack, String key, int value) {
        stack.getOrCreateTag().putInt(key, value);
    }

    public static int getInt(ItemStack stack, String key) {
        return getInt(stack, key, 0);
    }

    public static int getInt(ItemStack stack, String key, int fallback) {
        return has(stack, key) ? stack.getTag().getInt(key) : fallback;
    }

    public static void setBoolean(ItemStack stack, String key, boolean value) {
        stack.getOrCreateTag().putBoolean(key, value);
    }

    public static boolean getBoolean(ItemStack stack, String key) {
        return getBoolean(stack, key, false);
    }

    public static boolean getBoolean(ItemStack stack, String key, boolean fallback) {
        return has(stack, key) ? stack.getTag().getBoolean(key) : fallback;
    }

    public static void setString(ItemStack stack, String key, String value) {
        stack.getOrCreateTag().putString(key, value);
    }

    @Nullable
    public static String getString(ItemStack stack, String key) {
        return has(stack, key) ? stack.getTag().getString(key) : null;
    }

    public static void setIntArray(ItemStack stack, String key, int[] value) {
        stack.getOrCreateTag().putIntArray(key, value);
    }

    @Nullable
    public static int[] getIntArray(ItemStack stack, String key) {
        return has(stack, key) ? stack.getTag().getIntArray(key) : null;
    }

    // --- common Minecraft value types ------------------------------------

    public static void setUUID(ItemStack stack, String key, UUID value) {
        stack.getOrCreateTag().putUUID(key, value);
    }

    @Nullable
    public static UUID getUUID(ItemStack stack, String key) {
        return has(stack, key) ? stack.getTag().getUUID(key) : null;
    }

    public static void setBlockPos(ItemStack stack, String key, BlockPos pos) {
        stack.getOrCreateTag().put(key, NbtUtils.writeBlockPos(pos));
    }

    @Nullable
    public static BlockPos getBlockPos(ItemStack stack, String key) {
        if (!has(stack, key)) {
            return null;
        }
        return NbtUtils.readBlockPos(stack.getTag().getCompound(key));
    }

    public static void setResourceLocation(ItemStack stack, String key, ResourceLocation loc) {
        stack.getOrCreateTag().putString(key, loc.toString());
    }

    @Nullable
    public static ResourceLocation getResourceLocation(ItemStack stack, String key) {
        String s = getString(stack, key);
        return s == null ? null : ResourceLocation.tryParse(s);
    }

    public static void setDyeColor(ItemStack stack, String key, DyeColor color) {
        stack.getOrCreateTag().putInt(key, color.getId());
    }

    @Nullable
    public static DyeColor getDyeColor(ItemStack stack, String key) {
        return has(stack, key) ? DyeColor.byId(stack.getTag().getInt(key)) : null;
    }

    // --- Codec-backed records --------------------------------------------

    public static <T> void setCodec(ItemStack stack, String key, Codec<T> codec, T value) {
        codec.encodeStart(NbtOps.INSTANCE, value)
            .resultOrPartial(err -> {})
            .ifPresent(tag -> stack.getOrCreateTag().put(key, tag));
    }

    public static <T> Optional<T> getCodec(ItemStack stack, String key, Codec<T> codec) {
        if (!has(stack, key)) {
            return Optional.empty();
        }
        Tag tag = stack.getTag().get(key);
        if (tag == null) {
            return Optional.empty();
        }
        return codec.parse(NbtOps.INSTANCE, tag).resultOrPartial(err -> {});
    }

    // --- nested compound (for ItemContainerContents-style inventories) ----

    public static CompoundTag getOrCreateSubTag(ItemStack stack, String key) {
        CompoundTag root = stack.getOrCreateTag();
        if (!root.contains(key)) {
            root.put(key, new CompoundTag());
        }
        return root.getCompound(key);
    }
}

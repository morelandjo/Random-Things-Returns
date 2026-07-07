package lumien.randomthings.item;

import lumien.randomthings.menu.PortableSoundDampenerMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Carried dampener: mutes any sound stored on the Sound Patterns in its nine slots. */
public class ItemPortableSoundDampener extends Item {
    public static final int SIZE = 9;
    private static final String INVENTORY_KEY = "DampenerInventory";

    public ItemPortableSoundDampener() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                (id, inventory, p) -> new PortableSoundDampenerMenu(id, inventory, stack),
                Component.translatable("container.randomthings.portable_sound_dampener")));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.randomthings.portable_sound_dampener").withStyle(ChatFormatting.GRAY));
    }

    /** True if any stored Sound Pattern matches the given sound. */
    public static boolean mutes(ItemStack dampener, ResourceLocation sound) {
        CompoundTag tag = dampener.getTag();
        if (tag == null || !tag.contains(INVENTORY_KEY, Tag.TAG_LIST)) {
            return false;
        }
        ListTag list = tag.getList(INVENTORY_KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            ItemStack pattern = ItemStack.of(list.getCompound(i));
            if (pattern.getItem() instanceof ItemSoundPattern
                && sound.equals(ItemSoundPattern.getSoundLocation(pattern))) {
                return true;
            }
        }
        return false;
    }

    public static SimpleContainer loadInventory(ItemStack dampener) {
        SimpleContainer container = new SimpleContainer(SIZE);
        CompoundTag tag = dampener.getTag();
        if (tag != null && tag.contains(INVENTORY_KEY, Tag.TAG_LIST)) {
            ListTag list = tag.getList(INVENTORY_KEY, Tag.TAG_COMPOUND);
            for (int i = 0; i < Math.min(SIZE, list.size()); i++) {
                container.setItem(i, ItemStack.of(list.getCompound(i)));
            }
        }
        return container;
    }

    public static void saveInventory(ItemStack dampener, SimpleContainer container) {
        ListTag list = new ListTag();
        for (int i = 0; i < container.getContainerSize(); i++) {
            list.add(container.getItem(i).save(new CompoundTag()));
        }
        dampener.getOrCreateTag().put(INVENTORY_KEY, list);
    }
}

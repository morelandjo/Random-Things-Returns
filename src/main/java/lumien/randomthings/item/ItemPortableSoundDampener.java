package lumien.randomthings.item;

import lumien.randomthings.menu.PortableSoundDampenerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemPortableSoundDampener extends Item {
    public static final int INVENTORY_SIZE = 9;

    public ItemPortableSoundDampener() {
        super(new Item.Properties()
                .stacksTo(1)); // Max stack size of 1
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            // Open the portable sound dampener GUI
            serverPlayer.openMenu(createMenuProvider(hand), buf -> {
                buf.writeEnum(hand);
            });
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private MenuProvider createMenuProvider(InteractionHand hand) {
        return new SimpleMenuProvider(
            (id, inventory, player) -> new PortableSoundDampenerMenu(id, inventory, hand),
            Component.translatable("item.randomthings.portable_sound_dampener")
        );
    }
}

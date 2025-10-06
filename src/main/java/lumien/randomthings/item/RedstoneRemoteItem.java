package lumien.randomthings.item;

import lumien.randomthings.menu.RedstoneRemoteEditMenu;
import lumien.randomthings.menu.RedstoneRemoteUseMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RedstoneRemoteItem extends Item {
    public static final int INVENTORY_SIZE = 18;

    public RedstoneRemoteItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (player.isShiftKeyDown()) {
                // Open Edit GUI
                serverPlayer.openMenu(createEditMenuProvider(hand), buf -> {
                    buf.writeEnum(hand);
                });
            } else {
                // Open Use GUI
                serverPlayer.openMenu(createUseMenuProvider(hand), buf -> {
                    buf.writeEnum(hand);
                });
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private MenuProvider createEditMenuProvider(InteractionHand hand) {
        return new SimpleMenuProvider(
            (id, inventory, player) -> new RedstoneRemoteEditMenu(id, inventory, hand),
            Component.translatable("item.randomthings.redstone_remote")
        );
    }

    private MenuProvider createUseMenuProvider(InteractionHand hand) {
        return new SimpleMenuProvider(
            (id, inventory, player) -> new RedstoneRemoteUseMenu(id, inventory, hand),
            Component.translatable("item.randomthings.redstone_remote")
        );
    }
}

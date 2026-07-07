package lumien.randomthings.item;

import lumien.randomthings.handler.spectrecoil.SpectreCoilHandler;
import lumien.randomthings.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/** Drains the owner's Spectre Energy buffer to charge energy-storing items in their inventory. */
public class SpectreChargerItem extends Item {
    private static final String ENABLED_KEY = "spectre_charger_enabled";
    private final Tier tier;

    public enum Tier {
        NORMAL("normal", 0x00FFFF, 1024),
        REDSTONE("redstone", 0xFF0000, 4096),
        ENDER("ender", 0xC800D2, 20480),
        GENESIS("genesis", 0xFFC800, Integer.MAX_VALUE);

        private final String name;
        private final int color;
        private final int chargeRate;

        Tier(String name, int color, int chargeRate) {
            this.name = name;
            this.color = color;
            this.chargeRate = chargeRate;
        }

        public String getName() {
            return name;
        }

        public int getColor() {
            return color;
        }

        public int getChargeRate() {
            return chargeRate;
        }
    }

    public SpectreChargerItem(Tier tier) {
        super(new Item.Properties().stacksTo(1));
        this.tier = tier;
    }

    public Tier getTier() {
        return tier;
    }

    private static boolean isEnabled(ItemStack stack) {
        return lumien.randomthings.util.RTNbt.getBoolean(stack, ENABLED_KEY);
    }

    private static void setEnabled(ItemStack stack, boolean enabled) {
        lumien.randomthings.util.RTNbt.setBoolean(stack, ENABLED_KEY, enabled);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        boolean enabled = isEnabled(stack);
        String rate = tier == Tier.GENESIS ? "Infinite" : String.valueOf(tier.getChargeRate());
        tooltip.add(Component.translatable("item.randomthings.spectre_charger.charge_rate", rate).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(enabled ? "item.randomthings.spectre_charger.enabled" : "item.randomthings.spectre_charger.disabled")
            .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED));
        tooltip.add(Component.translatable("item.randomthings.spectre_charger.toggle").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            boolean wasEnabled = isEnabled(stack);
            setEnabled(stack, !wasEnabled);
            player.displayClientMessage(
                Component.translatable(wasEnabled ? "item.randomthings.spectre_charger.disabled_message" : "item.randomthings.spectre_charger.enabled_message")
                    .withStyle(wasEnabled ? ChatFormatting.RED : ChatFormatting.GREEN), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide || !isEnabled(stack) || !(entity instanceof ServerPlayer player)) {
            return;
        }
        SpectreCoilHandler handler = SpectreCoilHandler.get(player.getServer());
        UUID owner = player.getUUID();
        boolean genesis = tier == Tier.GENESIS;
        int rate = tier.getChargeRate();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (!genesis && handler.getEnergy(owner) <= 0) {
                break;
            }
            int budget = genesis ? rate : Math.min(rate, handler.getEnergy(owner));
            if (budget <= 0) {
                continue;
            }
            int accepted = Services.ENERGY.chargeItem(player, slot, budget, false);
            if (accepted > 0 && !genesis) {
                handler.extract(owner, accepted, false);
            }
        }
    }
}

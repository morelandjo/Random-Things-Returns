package lumien.randomthings.item;

import lumien.randomthings.handler.spectrecoil.SpectreCoilHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.awt.Color;
import java.util.List;

/**
 * Spectre Charger - Charges energy-compatible items in the player's inventory
 * using energy from their Spectre Energy buffer.
 *
 * Right-click to toggle on/off.
 * Different tiers charge at different rates.
 */
public class SpectreChargerItem extends Item {
    private final Tier tier;

    public enum Tier {
        NORMAL("normal", new Color(0, 255, 255).getRGB(), 1024),        // Cyan - 1024 RF/t
        REDSTONE("redstone", Color.RED.getRGB(), 4096),                  // Red - 4096 RF/t
        ENDER("ender", new Color(200, 0, 210).getRGB(), 20480),         // Purple - 20480 RF/t
        GENESIS("genesis", Color.ORANGE.getRGB(), Integer.MAX_VALUE);   // Orange - Infinite (creative)

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

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        boolean enabled = isEnabled(stack);
        String chargeRateStr = tier == Tier.GENESIS ? "Infinite" : String.valueOf(tier.getChargeRate());

        tooltip.add(Component.translatable("item.randomthings.spectre_charger.charge_rate", chargeRateStr)
            .withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.translatable(
            enabled ? "item.randomthings.spectre_charger.enabled" : "item.randomthings.spectre_charger.disabled"
        ).withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED));

        tooltip.add(Component.translatable("item.randomthings.spectre_charger.toggle")
            .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // Toggle enabled state
            boolean wasEnabled = isEnabled(stack);
            setEnabled(stack, !wasEnabled);

            player.displayClientMessage(
                Component.translatable(
                    wasEnabled
                        ? "item.randomthings.spectre_charger.disabled_message"
                        : "item.randomthings.spectre_charger.enabled_message"
                ).withStyle(wasEnabled ? ChatFormatting.RED : ChatFormatting.GREEN),
                true
            );
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        // Only charge on server side when enabled and entity is a player
        if (level.isClientSide || !isEnabled(stack) || !(entity instanceof ServerPlayer player)) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        SpectreCoilHandler handler = SpectreCoilHandler.get(serverLevel);
        IEnergyStorage playerEnergyBuffer = handler.getStorageCoil(player.getUUID());

        int chargeRate = tier.getChargeRate();

        // Iterate through player inventory
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            // Genesis tier has infinite energy
            if (playerEnergyBuffer.getEnergyStored() == 0 && tier != Tier.GENESIS) {
                break;
            }

            ItemStack targetStack = player.getInventory().getItem(slot);
            if (targetStack.isEmpty()) {
                continue;
            }

            // Get energy capability from the item
            IEnergyStorage itemStorage = targetStack.getCapability(Capabilities.EnergyStorage.ITEM);

            if (itemStorage != null && itemStorage.canReceive()) {
                int missingEnergy = itemStorage.getMaxEnergyStored() - itemStorage.getEnergyStored();

                if (tier == Tier.GENESIS) {
                    // Genesis tier - instant full charge
                    itemStorage.receiveEnergy(missingEnergy, false);
                } else if (missingEnergy > 0) {
                    // Normal tiers - charge at limited rate
                    int attemptCharge = Math.min(chargeRate, missingEnergy);
                    int energyExtracted = playerEnergyBuffer.extractEnergy(attemptCharge, false);

                    if (energyExtracted > 0) {
                        int energyInserted = itemStorage.receiveEnergy(energyExtracted, false);
                        int remainder = energyExtracted - energyInserted;

                        // Return unused energy to buffer
                        if (remainder > 0) {
                            playerEnergyBuffer.receiveEnergy(remainder, false);
                        }
                    }
                }
            }
        }
    }

    /**
     * Check if the charger is enabled.
     */
    private boolean isEnabled(ItemStack stack) {
        return stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY)
            .copyTag()
            .getBoolean("enabled");
    }

    /**
     * Set the charger enabled state.
     */
    private void setEnabled(ItemStack stack, boolean enabled) {
        stack.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY,
            data -> data.update(tag -> tag.putBoolean("enabled", enabled)));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Show enchantment glint when enabled
        return isEnabled(stack);
    }
}

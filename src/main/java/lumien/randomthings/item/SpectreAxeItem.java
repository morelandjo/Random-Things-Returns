package lumien.randomthings.item;

import lumien.randomthings.lib.ModConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.List;

public class SpectreAxeItem extends AxeItem {

    private static final ResourceLocation REACH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "spectre_axe_reach");

    public SpectreAxeItem(Item.Properties properties) {
        super(SpectreToolMaterial.SPECTRE, properties
            .attributes(createAttributes())
        );
    }

    private static ItemAttributeModifiers createAttributes() {
        // Start with the default axe attributes
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

        // Add standard axe attack damage and speed
        // Axe typically does more damage than sword but slower
        builder.add(
            Attributes.ATTACK_DAMAGE,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "spectre_axe_attack_damage"),
                8.0 + SpectreToolMaterial.SPECTRE.getAttackDamageBonus(), // Base 5 + material 3 = 8
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.MAINHAND
        );

        builder.add(
            Attributes.ATTACK_SPEED,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "spectre_axe_attack_speed"),
                -3.0, // Standard axe attack speed
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.MAINHAND
        );

        // Add the reach distance modifiers (+3 blocks)
        builder.add(
            Attributes.BLOCK_INTERACTION_RANGE,
            new AttributeModifier(
                REACH_MODIFIER_ID,
                3.0,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.MAINHAND
        );

        builder.add(
            Attributes.ENTITY_INTERACTION_RANGE,
            new AttributeModifier(
                REACH_MODIFIER_ID,
                3.0,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.MAINHAND
        );

        return builder.build();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.randomthings.spectre_axe"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}

package lumien.randomthings.item;

import lumien.randomthings.lib.ModConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.List;

public class SpectreShovelItem extends ShovelItem {

    private static final ResourceLocation REACH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "spectre_shovel_reach");

    public SpectreShovelItem(Item.Properties properties) {
        super(SpectreToolMaterial.SPECTRE, properties
            .attributes(createAttributes())
        );
    }

    private static ItemAttributeModifiers createAttributes() {
        // Start with the default shovel attributes
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

        // Add standard shovel attack damage and speed
        builder.add(
            Attributes.ATTACK_DAMAGE,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "spectre_shovel_attack_damage"),
                4.5 + SpectreToolMaterial.SPECTRE.getAttackDamageBonus(), // Base 1.5 + material 3 = 4.5
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.MAINHAND
        );

        builder.add(
            Attributes.ATTACK_SPEED,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "spectre_shovel_attack_speed"),
                -3.0, // Standard shovel attack speed
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
        tooltipComponents.add(Component.translatable("tooltip.randomthings.spectre_shovel"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}

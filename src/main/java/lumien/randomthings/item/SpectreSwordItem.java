package lumien.randomthings.item;

import lumien.randomthings.lib.ModConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.List;

public class SpectreSwordItem extends SwordItem {

    public SpectreSwordItem(Item.Properties properties) {
        super(SpectreToolMaterial.SPECTRE, properties
            .attributes(createAttributes())
        );
    }

    private static ItemAttributeModifiers createAttributes() {
        // Start with builder
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

        // Add standard sword attack damage and speed
        // Sword base damage = 3 (base) + material damage bonus (3.0)
        builder.add(
            Attributes.ATTACK_DAMAGE,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "spectre_sword_attack_damage"),
                6.0, // 3 base + 3 from material
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.MAINHAND
        );

        builder.add(
            Attributes.ATTACK_SPEED,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "spectre_sword_attack_speed"),
                -2.4, // Standard sword attack speed
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.MAINHAND
        );

        return builder.build();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.randomthings.spectre_sword"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}

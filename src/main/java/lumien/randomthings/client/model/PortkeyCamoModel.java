package lumien.randomthings.client.model;

import lumien.randomthings.item.ModDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class PortkeyCamoModel implements BakedModel {
    private final BakedModel originalModel;
    private final ItemOverrides overrides;

    public PortkeyCamoModel(BakedModel originalModel) {
        this.originalModel = originalModel;
        this.overrides = new PortkeyCamoOverrides();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
        return originalModel.getQuads(state, direction, random);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return originalModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return originalModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return originalModel.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return originalModel.getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }

    private static class PortkeyCamoOverrides extends ItemOverrides {
        @Nullable
        @Override
        public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            // Check if the portkey has a camo
            net.minecraft.resources.ResourceLocation camoItemId = stack.get(ModDataComponents.PORTKEY_CAMO.get());

            if (camoItemId != null) {
                // Get the camo item and its model
                Item camoItem = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(camoItemId);
                if (camoItem != null && camoItem != net.minecraft.world.item.Items.AIR) {
                    ItemStack camoStack = new ItemStack(camoItem);
                    return Minecraft.getInstance().getItemRenderer().getModel(camoStack, level, entity, seed);
                }
            }

            // No camo or invalid camo - use the original portkey model
            return originalModel;
        }
    }
}

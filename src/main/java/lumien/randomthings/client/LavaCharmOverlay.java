package lumien.randomthings.client;

import com.mojang.blaze3d.systems.RenderSystem;
import lumien.randomthings.item.LavaCharmItem;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class LavaCharmOverlay implements LayeredDraw.Layer {

    private static final ResourceLocation LAVA_CHARM_BAR_TEXTURE =
        ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/gui/lava_charm_bar.png");

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null || mc.options.hideGui) {
            return;
        }

        // Find Lava Charm in player's inventory
        ItemStack lavaCharm = findLavaCharmInInventory(player);

        if (!lavaCharm.isEmpty()) {
            renderLavaCharmBar(guiGraphics, lavaCharm);
        }
    }

    private void renderLavaCharmBar(GuiGraphics guiGraphics, ItemStack lavaCharm) {
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Get charge level
        float charge = LavaCharmItem.getCharge(lavaCharm);

        // Calculate how many icons to display (matching old implementation: charge / 2F / 10F)
        // charge ranges from 0-200, so this gives us 0-10 icons
        float iconCount = charge / 2.0f / 10.0f;
        int fullIcons = (int) Math.floor(iconCount);

        // Position above armor bar (matching old implementation)
        int xStart = screenWidth / 2 - 91; // Same starting position as armor/health bar
        int yPos = screenHeight - 39 - 10; // Above armor bar: -39 is armor position, -10 for our bar

        // Enable blending for transparency
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Render icons (matching old implementation logic)
        int left = 0;
        for (int i = 0; i < fullIcons + 1; i++) {
            int xPos = xStart + left;

            if (i == fullIcons) {
                // Last icon - apply partial transparency based on remaining charge
                float countFloat = charge / 2.0f / 10.0f + 10.0f;
                float alpha = countFloat % ((int) countFloat);

                if (alpha > 0) {
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
                    // blit(ResourceLocation, x, y, uOffset, vOffset, width, height, textureWidth, textureHeight)
                    // The texture is 256x256 with a 16x16 icon in the upper left corner
                    guiGraphics.blit(LAVA_CHARM_BAR_TEXTURE, xPos, yPos, 0, 0, 9, 9, 256, 256);
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                }
            } else {
                // Full icon - texture is 256x256, sprite is 16x16 at 0,0
                guiGraphics.blit(LAVA_CHARM_BAR_TEXTURE, xPos, yPos, 0, 0, 9, 9, 256, 256);
            }
            left += 8; // 8 pixel spacing between icons
        }

        RenderSystem.disableBlend();
    }

    /**
     * Find Lava Charm in player's inventory
     */
    private ItemStack findLavaCharmInInventory(Player player) {
        // Check main inventory
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof LavaCharmItem) {
                return stack;
            }
        }

        // Check armor slots
        for (ItemStack stack : player.getInventory().armor) {
            if (stack.getItem() instanceof LavaCharmItem) {
                return stack;
            }
        }

        // Check offhand
        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof LavaCharmItem) {
            return offhand;
        }

        return ItemStack.EMPTY;
    }
}

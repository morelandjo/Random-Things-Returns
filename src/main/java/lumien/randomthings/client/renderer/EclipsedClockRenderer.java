package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import lumien.randomthings.entity.EclipsedClockEntity;
import lumien.randomthings.item.ModDataComponents;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.TimeInABottleItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class EclipsedClockRenderer extends EntityRenderer<EclipsedClockEntity> {
    private final ItemRenderer itemRenderer;

    public EclipsedClockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(EclipsedClockEntity clockEntity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        
        Direction direction = clockEntity.getDirection();
        
        
        // Entity is already positioned correctly - no translation needed
        
        // Apply rotation based on direction
        float yRot = direction.toYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        
        // Render the clock item
        ItemStack clockStack = new ItemStack(ModItems.ECLIPSED_CLOCK.get());
        clockStack.set(ModDataComponents.TARGET_TIME.get(), clockEntity.getTargetTime());
        
        if (!clockStack.isEmpty()) {
            poseStack.pushPose();
            poseStack.scale(0.5F, 0.5F, 0.5F);
            this.itemRenderer.renderStatic(clockStack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, clockEntity.level(), clockEntity.getId());
            poseStack.popPose();
        }
        
        poseStack.popPose();
        
        // Render floating time text using nameplate approach but positioned in front of clock face
        if (clockEntity.shouldDisplayTime()) {
            System.out.println("=== SHOULD DISPLAY TIME ===");
            System.out.println("Time display counter: " + clockEntity.timeDisplayCounter);
            
            poseStack.pushPose();
            
            // Position nameplate in front of the clock face
            double forwardX = direction.getStepX() * 0.3; // Forward from face
            double forwardY = -0.3; // Slightly down from default nameplate position  
            double forwardZ = direction.getStepZ() * 0.3; // Forward from face
            
            poseStack.translate(forwardX, forwardY, forwardZ);
            
            Component timeComponent = Component.literal(clockEntity.getStringTargetTime());
            this.renderNameTag(clockEntity, timeComponent, poseStack, buffer, packedLight, partialTicks);
            
            poseStack.popPose();
        }
        
        super.render(clockEntity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EclipsedClockEntity entity) {
        return null; // We don't need a texture since we're rendering an item
    }
    
    private void renderFloatingText(EclipsedClockEntity entity, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        
        Direction direction = entity.getDirection();
        
        // Position text well above the clock for high visibility
        poseStack.translate(0.0, 1.0, 0.0); // Move 1 block up
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(-0.05F, -0.05F, 0.05F); // Bigger text
        
        Font font = this.getFont();
        String timeText = entity.getStringTargetTime();
        Component textComponent = Component.literal(timeText);
        float textWidth = font.width(textComponent);
        
        System.out.println("=== RENDERING TEXT ===");
        System.out.println("Text: " + timeText);
        System.out.println("Font width: " + textWidth);
        
        // Render with bright text and see-through mode
        font.drawInBatch(textComponent, -textWidth / 2.0F, 0, 0xFFFF00, false, 
            poseStack.last().pose(), buffer, Font.DisplayMode.SEE_THROUGH, 0, packedLight);
        
        poseStack.popPose();
    }
    
    private void renderTimeInABottleTooltip(EclipsedClockEntity entity, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Check if player is holding Time in a Bottle
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        
        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof TimeInABottleItem)) {
            heldItem = player.getOffhandItem();
            if (!(heldItem.getItem() instanceof TimeInABottleItem)) {
                return; // Not holding Time in a Bottle
            }
        }
        
        // Calculate time difference needed
        int currentWorldTime = (int) entity.level().getDayTime();
        int targetTime = entity.getTargetTime();
        int timeDifference = (targetTime - currentWorldTime) % 24000;
        if (timeDifference < 0) {
            timeDifference += 24000;
        }
        
        // Convert to readable format
        int secondsNeeded = timeDifference / 20;
        int minutesNeeded = secondsNeeded / 60;
        int hoursNeeded = minutesNeeded / 60;
        
        String timeNeeded;
        if (hoursNeeded > 0) {
            timeNeeded = String.format("%d:%02d:%02d", hoursNeeded, minutesNeeded % 60, secondsNeeded % 60);
        } else if (minutesNeeded > 0) {
            timeNeeded = String.format("%d:%02d", minutesNeeded, secondsNeeded % 60);
        } else {
            timeNeeded = String.format("%ds", secondsNeeded);
        }
        
        // Render tooltip text below the time display
        poseStack.pushPose();
        
        poseStack.translate(0.0, 0.1, 0.2); // Position below the time display
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(-0.02F, -0.02F, 0.02F); // Slightly smaller text
        
        Font font = this.getFont();
        Component tooltipText = Component.literal("Time needed: " + timeNeeded);
        float textWidth = font.width(tooltipText);
        
        // Render in yellow color
        font.drawInBatch(tooltipText, -textWidth / 2.0F, 0, 0xFFFF00, false, 
            poseStack.last().pose(), buffer, Font.DisplayMode.POLYGON_OFFSET, 0, packedLight);
        
        poseStack.popPose();
    }
    
    @Override
    public Vec3 getRenderOffset(EclipsedClockEntity entity, float partialTicks) {
        // No render offset - use entity's exact position
        return Vec3.ZERO;
    }
}
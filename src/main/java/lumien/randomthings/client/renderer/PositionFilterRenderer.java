package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import lumien.randomthings.client.util.RenderUtils;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.PositionFilterItem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class PositionFilterRenderer {
    private static PositionFilterRenderer instance;

    public static PositionFilterRenderer get() {
        if (instance == null) {
            instance = new PositionFilterRenderer();
        }
        return instance;
    }

    private PositionFilterRenderer() {
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null) {
            return;
        }

        ItemStack mainHandItem = player.getMainHandItem();

        // Check if player is holding a Position Filter
        if (!mainHandItem.isEmpty() && mainHandItem.getItem() == ModItems.POSITION_FILTER.get()) {
            if (PositionFilterItem.hasPosition(mainHandItem)) {
                String dimension = PositionFilterItem.getDimension(mainHandItem);
                BlockPos targetPos = PositionFilterItem.getPosition(mainHandItem);

                // Only render if in the same dimension
                if (targetPos != null && dimension != null &&
                    dimension.equals(player.level().dimension().location().toString())) {

                    // Get camera position (this already accounts for eye height)
                    Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
                    Vec3 cameraPos = camera.getPosition();

                    double cameraX = cameraPos.x();
                    double cameraY = cameraPos.y();
                    double cameraZ = cameraPos.z();

                    poseStack.pushPose();

                    // Translate to world position relative to camera
                    poseStack.translate(
                        targetPos.getX() - cameraX,
                        targetPos.getY() - cameraY,
                        targetPos.getZ() - cameraZ
                    );

                    // Draw purple cube (RGB: 102, 0, 255, Alpha: 51)
                    // Position offset by -0.01 and size 1.02 to make it slightly larger than block
                    RenderUtils.drawCube(
                        poseStack,
                        bufferSource,
                        -0.01f, -0.01f, -0.01f,
                        1.02f,
                        102, 0, 255, 51
                    );

                    poseStack.popPose();
                }
            }
        }
    }
}

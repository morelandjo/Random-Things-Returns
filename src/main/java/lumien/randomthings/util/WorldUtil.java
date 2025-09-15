package lumien.randomthings.util;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class WorldUtil {

    /**
     * Ray trace for fluid blocks, adapted from 1.12.2 implementation
     */
    public static BlockHitResult rayTraceAll(Level level, Player player, boolean useLiquids) {
        double reachDistance = player.blockInteractionRange();
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 endPos = eyePos.add(lookVec.scale(reachDistance));

        // When looking for liquids, we want to detect them, not pass through them
        ClipContext.Fluid fluidMode = useLiquids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE;
        // Use COLLIDER instead of OUTLINE to interact with fluids properly
        ClipContext.Block blockMode = useLiquids ? ClipContext.Block.COLLIDER : ClipContext.Block.OUTLINE;
        ClipContext context = new ClipContext(eyePos, endPos, blockMode, fluidMode, player);

        return level.clip(context);
    }
}
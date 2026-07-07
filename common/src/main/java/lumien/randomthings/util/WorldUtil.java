package lumien.randomthings.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class WorldUtil {

    /** Ray trace from the player's eyes, optionally hitting fluids. */
    public static BlockHitResult rayTraceAll(Level level, Player player, boolean useLiquids) {
        double reachDistance = player.isCreative() ? 5.0D : 4.5D;
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 endPos = eyePos.add(lookVec.scale(reachDistance));

        ClipContext.Fluid fluidMode = useLiquids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE;
        ClipContext.Block blockMode = useLiquids ? ClipContext.Block.COLLIDER : ClipContext.Block.OUTLINE;

        return level.clip(new ClipContext(eyePos, endPos, blockMode, fluidMode, player));
    }
}

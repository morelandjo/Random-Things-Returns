package lumien.randomthings.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import lumien.randomthings.handler.redstonesignal.RedstoneSignalHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.SignalGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Mixin to inject redstone signal support into SignalGetter interface
 */
@Mixin(SignalGetter.class)
public interface SignalGetterMixin {

    @ModifyReturnValue(
            method = "getSignal",
            at = @At("RETURN")
    )
    default int randomthings$getSignal(int original, BlockPos pos, Direction direction) {
        return randomthings$getModifiedSignal(original, pos, direction);
    }

    @ModifyReturnValue(
            method = "getDirectSignal",
            at = @At("RETURN")
    )
    default int randomthings$getDirectSignal(int original, BlockPos pos, Direction direction) {
        return randomthings$getModifiedSignal(original, pos, direction);
    }

    @Unique
    private int randomthings$getModifiedSignal(int original, BlockPos pos, Direction direction) {
        if (original >= 15) return original;

        int signal = original;

        if (this instanceof ServerLevel sl) {
            BlockPos relativePos = pos.relative(direction.getOpposite());
            RedstoneSignalHandler handler = RedstoneSignalHandler.get(sl.getServer());
            int power = handler.getStrongPower(sl, relativePos);
            signal = Math.max(signal, power);
        }

        return signal;
    }
}

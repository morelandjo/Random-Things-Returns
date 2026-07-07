package lumien.randomthings.mixin;

import lumien.randomthings.event.FlooTokenHandler;
import lumien.randomthings.event.PortkeyHandler;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cross-loader item-entity hooks (vanilla/Architectury exposes neither a per-item-entity tick nor an
 * item-pickup event). Drives the Portkey: ages it on the ground, and teleports the player who picks
 * up a primed one (cancelling the normal pickup).
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void randomthings$tick(CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        PortkeyHandler.age(self);
        FlooTokenHandler.tick(self);
        lumien.randomthings.event.StableEnderPearlHandler.tick(self);
    }

    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void randomthings$playerTouch(Player player, CallbackInfo ci) {
        if (PortkeyHandler.tryTeleport((ItemEntity) (Object) this, player)) {
            ci.cancel();
        }
    }
}

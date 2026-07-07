package lumien.randomthings;

import lumien.randomthings.lib.ModConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common (loader-agnostic) entry point for Random Things.
 *
 * <p>Both the Fabric and Forge entry points call {@link #init()} after registering their mod event
 * bus with Architectury. All cross-loader registration is wired here so the platform modules stay
 * thin.</p>
 */
public final class RandomThings {

    public static final String MOD_ID = ModConstants.MOD_ID;
    public static final Logger LOGGER = LoggerFactory.getLogger("RandomThings");

    private RandomThings() {
    }

    public static void init() {
        LOGGER.info("[RandomThings] Initializing common content...");

        // Registration is wired here as features are ported. Architectury DeferredRegisters
        // are registered (i.e. .register() called) inside each Mod* registry class.
        lumien.randomthings.block.ModBlocks.register();
        lumien.randomthings.item.ModItems.register();
        lumien.randomthings.blockentity.ModBlockEntityTypes.register();
        lumien.randomthings.entity.ModEntityTypes.register();
        lumien.randomthings.menu.ModMenuTypes.register();
        lumien.randomthings.recipe.ModRecipeTypes.register();
        lumien.randomthings.recipe.ModRecipeSerializers.register();
        lumien.randomthings.worldgen.ModFeatures.register();
        lumien.randomthings.handler.spectre.ModChunkGenerators.register();
        lumien.randomthings.event.SpectreDimensionHandler.register();

        lumien.randomthings.enchantment.ModEnchantments.register();

        lumien.randomthings.network.RTNetwork.register();
        lumien.randomthings.event.ChatEventHandler.register();
        lumien.randomthings.event.RTEvents.register();
        lumien.randomthings.event.LavaCharmHandler.register();
        lumien.randomthings.event.LootHandler.register();
        lumien.randomthings.event.BootsHandler.register();
        dev.architectury.event.events.common.TickEvent.SERVER_POST.register(
            server -> lumien.randomthings.handler.EscapeRopeHandler.getInstance().tick());

        LOGGER.info("[RandomThings] Common content initialized.");
    }
}

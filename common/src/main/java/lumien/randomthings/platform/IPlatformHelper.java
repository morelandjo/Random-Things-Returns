package lumien.randomthings.platform;

/**
 * Platform-specific helper. One implementation per loader, resolved via {@link Services}.
 * Add cross-loader methods here whenever common code needs a loader-specific answer.
 */
public interface IPlatformHelper {

    /** @return the name of the current platform, e.g. "Forge" or "Fabric". */
    String getPlatformName();

    /** @return true if a mod with the given id is loaded. */
    boolean isModLoaded(String modId);

    /** @return true if running in a development (deobfuscated) environment. */
    boolean isDevelopmentEnvironment();

    /**
     * Creates the Plant Chest block item. The item renders via a chest model
     * (a block-entity-without-level renderer), which each loader wires differently:
     * Forge through {@code initializeClient}/{@code IClientItemExtensions}, Fabric through
     * {@code BuiltinItemRendererRegistry} (registered client-side, so this returns a plain BlockItem).
     */
    net.minecraft.world.item.BlockItem createPlantChestItem(
        net.minecraft.world.level.block.Block block, net.minecraft.world.item.Item.Properties properties);
}

package lumien.randomthings.platform;

import java.util.ServiceLoader;

/**
 * Service loader for platform-specific implementations. Each platform (Forge, Fabric) registers its
 * implementations through Java's {@link ServiceLoader} mechanism (META-INF/services).
 */
public final class Services {

    private Services() {
    }

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final IEnergyBridge ENERGY = load(IEnergyBridge.class);

    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz)
            .findFirst()
            .orElseThrow(() -> new RuntimeException(
                "Failed to load service for " + clazz.getName() +
                ". The platform-specific module is not properly configured."
            ));
    }
}

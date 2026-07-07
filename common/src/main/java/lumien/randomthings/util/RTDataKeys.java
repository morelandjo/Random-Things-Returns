package lumien.randomthings.util;

/**
 * NBT key constants — the 1.20.1 replacement for the 1.21.1 {@code ModDataComponents}.
 *
 * <p>1.20.1 has no Data Components, so every former component is stored on the {@link
 * net.minecraft.world.item.ItemStack}'s NBT under these keys. The key strings intentionally match
 * the old component registry names so saved data and intent stay traceable to the 1.21.1 source.</p>
 *
 * <p>Use together with {@link RTNbt} typed accessors.</p>
 */
public final class RTDataKeys {

    private RTDataKeys() {
    }

    // Biome Crystal / radar
    public static final String BIOME_CRYSTAL_BIOME = "biome_crystal_biome";

    // Generic player binding
    public static final String PLAYER_UUID = "player_uuid";

    // Diaphanous block disguise
    public static final String DIAPHANOUS_BLOCK_STATE = "diaphanous_block_state";
    public static final String DIAPHANOUS_INVERTED = "diaphanous_inverted";

    // Chunk Analyzer
    public static final String CHUNK_ANALYZER_RESULT = "chunk_analyzer_result";

    // Eclipsed/Time items
    public static final String TARGET_TIME = "target_time";
    public static final String STORED_TIME = "stored_time";

    // Nature's Compass-style target
    public static final String COMPASS_TARGET_X = "compass_target_x";
    public static final String COMPASS_TARGET_Z = "compass_target_z";

    // Ender Bucket fluid content (also see FluidHelper for the actual storage)
    public static final String FLUID_CONTENT = "fluid_content";

    // Ender Letter
    public static final String SENDER_NAME = "sender_name";
    public static final String RECEIVER_NAME = "receiver_name";
    public static final String ENDER_LETTER_SIGNED = "ender_letter_signed";

    // Redstone Tool
    public static final String REDSTONE_TOOL_LINK = "redstone_tool_link";

    // Position Filter
    public static final String POSITION_DIMENSION = "position_dimension";
    public static final String POSITION_X = "position_x";
    public static final String POSITION_Y = "position_y";
    public static final String POSITION_Z = "position_z";

    // Item / Entity Filters
    public static final String ITEM_FILTER_DATA = "item_filter_data";
    public static final String ENTITY_TYPE_FILTER = "entity_type_filter";

    // Lava Charm
    public static final String LAVA_CHARM_CHARGE = "lava_charm_charge";
    public static final String LAVA_CHARM_COOLDOWN = "lava_charm_cooldown";

    // Redstone Activator / Remote
    public static final String REDSTONE_ACTIVATOR_DURATION = "redstone_activator_duration";
    public static final String REDSTONE_REMOTE_INVENTORY = "redstone_remote_inventory";

    // Sound items
    public static final String SOUND_LOCATION = "sound_location";
    public static final String DAMPENER_INVENTORY = "dampener_inventory";
    public static final String SOUND_RECORDER_RECORDING = "sound_recorder_recording";
    public static final String SOUND_RECORDER_SOUNDS = "sound_recorder_sounds";

    // Portkey
    public static final String PORTKEY_TARGET = "portkey_target";
    public static final String PORTKEY_AGE = "portkey_age";
    public static final String PORTKEY_CAMO = "portkey_camo";

    // Rune Dust / patterns
    public static final String RUNE_COLOR = "rune_color";
    public static final String RUNE_PATTERN = "rune_pattern";

    // Spectre Anchor
    public static final String SPECTRE_ANCHORED = "spectre_anchored";

    // Summoning Pendulum
    public static final String SUMMONING_PENDULUM_ENTITIES = "summoning_pendulum_entities";

    // Floo
    public static final String FLOO_POUCH_CHARGE = "floo_pouch_charge";
    public static final String FLOO_TOKEN_AGE = "floo_token_age";
}

package lumien.randomthings.blockentity;

import lumien.randomthings.item.BiomeCrystalItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import javax.annotation.Nullable;

/** Searches outward (in a square spiral) for the biome stored on an inserted Biome Crystal. */
public class BiomeRadarBlockEntity extends BlockEntity {
    public enum State { IDLE, SEARCHING, FINISHED }

    private static final int ANTENNA_SLOTS = 4;
    private static final int STEP_DISTANCE = 48;
    private static final int STEPS_PER_TICK = 5;
    private static final int ANTENNA_SYNC_INTERVAL = 100;

    private State state = State.IDLE;
    private ItemStack currentCrystal = ItemStack.EMPTY;
    private boolean powered = false;
    private int searchCounter = 0;
    @Nullable private BlockPos foundPosition;
    @Nullable private ResourceKey<Biome> biomeToSearch;
    private final String[] antennaBiomes = new String[ANTENNA_SLOTS];
    private int antennaCounter = 0;
    private int validityCheckCounter = 0;

    public BiomeRadarBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.BIOME_RADAR.get(), pos, blockState);
    }

    public State getState() {
        return state;
    }

    public ItemStack getCurrentCrystal() {
        return currentCrystal;
    }

    public void setCrystal(ItemStack stack) {
        this.currentCrystal = stack;
        markChangedAndSync();
    }

    public BlockPos getFoundPosition() {
        return this.foundPosition != null ? this.foundPosition : this.worldPosition;
    }

    public void onNeighborChanged(boolean newPowered) {
        if (this.level == null) {
            return;
        }
        boolean stateChanged = false;
        if (!this.powered && newPowered && this.state == State.IDLE
            && !this.currentCrystal.isEmpty() && isAntennaValid(this.level)) {
            ResourceKey<Biome> target = BiomeCrystalItem.getBiome(this.currentCrystal);
            if (target != null) {
                this.state = State.SEARCHING;
                this.searchCounter = 0;
                this.biomeToSearch = target;
                stateChanged = true;
            }
        } else if (this.state == State.SEARCHING && !newPowered && this.powered) {
            this.state = State.IDLE;
            stateChanged = true;
        } else if (this.state == State.FINISHED && this.powered && !newPowered) {
            this.state = State.IDLE;
            stateChanged = true;
        }
        this.powered = newPowered;
        if (stateChanged) {
            markChangedAndSync();
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BiomeRadarBlockEntity be) {
        be.validityCheckCounter++;
        if (be.validityCheckCounter % 60 == 0 && be.state != State.IDLE && !be.isAntennaValid(level)) {
            be.state = State.IDLE;
            be.markChangedAndSync();
        }
        if (be.state != State.SEARCHING || be.biomeToSearch == null) {
            return;
        }
        boolean antennaChanged = false;
        for (int i = 0; i < STEPS_PER_TICK; i++) {
            BlockPos testPos = be.spiralPos(be.searchCounter);
            ResourceKey<Biome> biomeKey = level.getBiome(testPos).unwrapKey().orElse(null);
            if (biomeKey == null) {
                be.searchCounter++;
                continue;
            }
            String biomeId = biomeKey.location().toString();
            boolean alreadyTracked = false;
            for (String tracked : be.antennaBiomes) {
                if (biomeId.equals(tracked)) {
                    alreadyTracked = true;
                    break;
                }
            }
            if (!alreadyTracked) {
                be.antennaBiomes[be.antennaCounter] = biomeId;
                be.antennaCounter = (be.antennaCounter + 1) % ANTENNA_SLOTS;
                antennaChanged = true;
            }
            if (biomeKey.equals(be.biomeToSearch)) {
                be.state = State.FINISHED;
                be.foundPosition = testPos;
                be.markChangedAndSync();
                return;
            }
            be.searchCounter++;
        }
        if (antennaChanged && be.searchCounter % ANTENNA_SYNC_INTERVAL < STEPS_PER_TICK) {
            be.markChangedAndSync();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BiomeRadarBlockEntity be) {
        if (be.state == State.SEARCHING) {
            for (int i = 0; i < ANTENNA_SLOTS; i++) {
                Holder<Biome> holder = biomeHolder(level, be.antennaBiomes[i]);
                if (holder != null) {
                    spawnAntennaParticle(level, pos, i, biomeColor(holder));
                }
            }
        } else if (be.state == State.FINISHED && !be.currentCrystal.isEmpty()) {
            ResourceKey<Biome> key = BiomeCrystalItem.getBiome(be.currentCrystal);
            Holder<Biome> holder = key == null ? null
                : level.registryAccess().registryOrThrow(Registries.BIOME).getHolder(key).orElse(null);
            if (holder != null) {
                int color = biomeColor(holder);
                for (int i = 0; i < ANTENNA_SLOTS; i++) {
                    spawnAntennaParticle(level, pos, i, color);
                }
                level.addParticle(toDust(color), pos.getX() + 0.5, pos.getY() + 3.1, pos.getZ() + 0.5, 0, 0, 0);
            }
        }
    }

    @Nullable
    private static Holder<Biome> biomeHolder(Level level, @Nullable String biomeId) {
        if (biomeId == null) {
            return null;
        }
        ResourceLocation loc = ResourceLocation.tryParse(biomeId);
        if (loc == null) {
            return null;
        }
        return level.registryAccess().registryOrThrow(Registries.BIOME)
            .getHolder(ResourceKey.create(Registries.BIOME, loc)).orElse(null);
    }

    /** Simple biome colour for the antenna particles: a blend of the biome's foliage/water/grass tints. */
    private static int biomeColor(Holder<Biome> holder) {
        Biome biome = holder.value();
        int foliage = biome.getFoliageColor();
        int water = biome.getSpecialEffects().getWaterColor();
        int grass = biome.getGrassColor(0, 0);
        int fw = blend(foliage, water);
        return blend(fw, grass) & 0xFFFFFF;
    }

    private static int blend(int a, int b) {
        int r = (((a >> 16) & 0xFF) + ((b >> 16) & 0xFF)) / 2;
        int g = (((a >> 8) & 0xFF) + ((b >> 8) & 0xFF)) / 2;
        int bl = ((a & 0xFF) + (b & 0xFF)) / 2;
        return (r << 16) | (g << 8) | bl;
    }

    private static void spawnAntennaParticle(Level level, BlockPos pos, int slot, int rgb) {
        double x = slot < 2 ? pos.getX() + 1.5 - slot * 2 : pos.getX() + 0.5;
        double z = slot > 1 ? pos.getZ() + 1.5 - (slot - 2) * 2 : pos.getZ() + 0.5;
        level.addParticle(toDust(rgb), x, pos.getY() + 4.1, z, 0, 0, 0);
    }

    private static DustParticleOptions toDust(int rgb) {
        return new DustParticleOptions(new Vector3f(((rgb >> 16) & 0xFF) / 255.0F, ((rgb >> 8) & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F), 1.0F);
    }

    private boolean isAntennaValid(Level level) {
        BlockPos p = this.worldPosition;
        BlockPos[] required = {
            p.above(), p.above(2),
            p.offset(1, 2, 0), p.offset(-1, 2, 0), p.offset(1, 3, 0), p.offset(-1, 3, 0),
            p.offset(0, 2, 1), p.offset(0, 2, -1), p.offset(0, 3, 1), p.offset(0, 3, -1)
        };
        for (BlockPos req : required) {
            if (!level.getBlockState(req).is(Blocks.IRON_BARS)) {
                return false;
            }
        }
        return true;
    }

    private BlockPos spiralPos(int n) {
        double r = Math.floor((Math.sqrt(n + 1) - 1) / 2) + 1;
        double p = (8 * r * (r - 1)) / 2;
        double en = r * 2;
        double a = (1 + n - p) % (r * 8);
        double x = 0;
        double z = 0;
        switch ((int) Math.floor(a / (r * 2))) {
            case 0 -> { x = a - r; z = -r; }
            case 1 -> { x = r; z = (a % en) - r; }
            case 2 -> { x = r - (a % en); z = r; }
            case 3 -> { x = -r; z = r - (a % en); }
        }
        return new BlockPos(
            this.worldPosition.getX() + (int) (x * STEP_DISTANCE),
            this.worldPosition.getY(),
            this.worldPosition.getZ() + (int) (z * STEP_DISTANCE));
    }

    private void markChangedAndSync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("State", state.ordinal());
        tag.putBoolean("Powered", powered);
        tag.putInt("SearchCounter", searchCounter);
        tag.putInt("AntennaCounter", antennaCounter);
        if (!currentCrystal.isEmpty()) {
            tag.put("CurrentCrystal", currentCrystal.save(new CompoundTag()));
        }
        if (biomeToSearch != null) {
            tag.putString("BiomeToSearch", biomeToSearch.location().toString());
        }
        if (foundPosition != null) {
            tag.putInt("FoundX", foundPosition.getX());
            tag.putInt("FoundY", foundPosition.getY());
            tag.putInt("FoundZ", foundPosition.getZ());
        }
        for (int i = 0; i < ANTENNA_SLOTS; i++) {
            if (antennaBiomes[i] != null) {
                tag.putString("AntennaBiome" + i, antennaBiomes[i]);
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        int stateOrdinal = tag.getInt("State");
        this.state = stateOrdinal >= 0 && stateOrdinal < State.values().length ? State.values()[stateOrdinal] : State.IDLE;
        this.powered = tag.getBoolean("Powered");
        this.searchCounter = tag.getInt("SearchCounter");
        this.antennaCounter = tag.getInt("AntennaCounter") % ANTENNA_SLOTS;
        this.currentCrystal = tag.contains("CurrentCrystal") ? ItemStack.of(tag.getCompound("CurrentCrystal")) : ItemStack.EMPTY;
        this.biomeToSearch = tag.contains("BiomeToSearch")
            ? ResourceKey.create(Registries.BIOME, new ResourceLocation(tag.getString("BiomeToSearch"))) : null;
        this.foundPosition = tag.contains("FoundX")
            ? new BlockPos(tag.getInt("FoundX"), tag.getInt("FoundY"), tag.getInt("FoundZ")) : null;
        for (int i = 0; i < ANTENNA_SLOTS; i++) {
            this.antennaBiomes[i] = tag.contains("AntennaBiome" + i) ? tag.getString("AntennaBiome" + i) : null;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putInt("State", state.ordinal());
        if (!currentCrystal.isEmpty()) {
            tag.put("CurrentCrystal", currentCrystal.save(new CompoundTag()));
        }
        for (int i = 0; i < ANTENNA_SLOTS; i++) {
            if (antennaBiomes[i] != null) {
                tag.putString("AntennaBiome" + i, antennaBiomes[i]);
            }
        }
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}

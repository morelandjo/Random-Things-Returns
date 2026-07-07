package lumien.randomthings.forge;

import lumien.randomthings.blockentity.SpectreEnergyInjectorBlockEntity;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Exposes the Spectre Energy Injector as a Forge Energy receiver (into the owner's buffer). */
@Mod.EventBusSubscriber(modid = ModConstants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ForgeEnergyAttach {

    private ForgeEnergyAttach() {
    }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof SpectreEnergyInjectorBlockEntity injector) {
            event.addCapability(new ResourceLocation(ModConstants.MOD_ID, "spectre_energy"),
                new InjectorProvider(injector));
        }
    }

    private static final class InjectorProvider implements ICapabilitySerializable<CompoundTag> {
        private final LazyOptional<IEnergyStorage> optional;

        InjectorProvider(SpectreEnergyInjectorBlockEntity injector) {
            this.optional = LazyOptional.of(() -> new IEnergyStorage() {
                @Override
                public int receiveEnergy(int maxReceive, boolean simulate) {
                    return injector.receiveEnergy(maxReceive, simulate);
                }

                @Override
                public int extractEnergy(int maxExtract, boolean simulate) {
                    return 0;
                }

                @Override
                public int getEnergyStored() {
                    return injector.getStoredEnergy();
                }

                @Override
                public int getMaxEnergyStored() {
                    return lumien.randomthings.handler.spectrecoil.SpectreCoilHandler.MAX_ENERGY;
                }

                @Override
                public boolean canExtract() {
                    return false;
                }

                @Override
                public boolean canReceive() {
                    return true;
                }
            });
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return cap == ForgeCapabilities.ENERGY ? optional.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return new CompoundTag();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
        }
    }
}

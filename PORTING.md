# Random Things — 1.21.1 NeoForge → 1.20.1 Architectury (Fabric + Forge)

This branch (`1.20.1`) is a **backport** of the 1.21.1 NeoForge code to Minecraft **1.20.1** as an
**Architectury multiloader** project targeting **Fabric** and **Forge**.

## Source of truth

- **`src/` at the repo root is the 1.21.1 NeoForge source** (the thing we are porting *from*).
  It is NOT compiled by the multiloader build — it stays as the behavioural reference. Read the
  1.21.1 class, then translate the *behaviour* into the new module. Do not delete `src/` until the
  port is complete.
- The original 1.12.2 Forge upstream (deeper spec) lives at
  `/Users/josh/Documents/minecraftmodding/othermodsgits/Random-Things`.
- Reference Architectury setup we modelled the build on:
  `/Users/josh/Documents/minecraftmodding/railwaysuntold/1.20.1`.

## Module layout

```
common/   — loader-agnostic code (the bulk of the mod). Vanilla + Architectury API only.
fabric/   — Fabric entry point + Fabric-only glue (FabricRandomThings, platform helper).
forge/    — Forge entry point + Forge-only glue (RandomThingsForge, platform helper).
```

Package roots: common = `lumien.randomthings`, fabric = `lumien.randomthings.fabric`,
forge = `lumien.randomthings.forge`. Mod id: `randomthings`.

## Building / running

> [!IMPORTANT]
> **Use JDK 21** to run Gradle. Architectury Loom runs on Gradle 8.8, which does not support the
> machine-default JDK 25. Either export `JAVA_HOME` to a 21 JDK or set `org.gradle.java.home`.

```
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
./gradlew build
./gradlew :fabric:runClient
./gradlew :forge:runClient
```

The mod **sources** still target Java 17 (1.20.1 runtime), only the Gradle launcher needs 21.

---

## API migration cheat-sheet (1.21.1 NeoForge → 1.20.1)

### 1. Registration — Architectury `DeferredRegister`

NeoForge:
```java
public static final DeferredRegister<Block> BLOCKS =
    DeferredRegister.create(Registries.BLOCK, ModConstants.MOD_ID);   // arg order!
... BLOCKS.register(modEventBus);                                      // in mod ctor
```
Architectury (this project):
```java
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

public static final DeferredRegister<Block> BLOCKS =
    DeferredRegister.create(ModConstants.MOD_ID, Registries.BLOCK);   // (modid, registry)
public static final RegistrySupplier<MyBlock> FOO = BLOCKS.register("foo", () -> new MyBlock(...));
... ModBlocks.register();                                             // calls BLOCKS.register()
```
- `Supplier<T>` → `RegistrySupplier<T>` (still has `.get()`).
- Every `Mod*` registry class exposes a static `register()` that calls `<register>.register()`;
  `RandomThings.init()` calls them in order (blocks before items before block-entities…).
- Creative tab: the `RT_CREATIVE_TAB` is built with a `displayItems` generator that iterates the
  `ITEMS` `DeferredRegister` (which is `Iterable`), so **every** registered item shows automatically.
  Do *not* also use per-item `arch$tab` — that double-adds. (Per-item `arch$tab` proved unreliable
  for bulk population here; the generator is the robust path.) A new item appears in the tab the
  moment it's registered to `ITEMS` — no extra step.

### 2. Data Components → NBT  ⚠️ biggest change

**1.20.1 has no Data Components.** Everything in `ModDataComponents` (~40 types) becomes
`ItemStack` NBT. Use the helper + key constants (see `lumien.randomthings.util.RTNbt` and
`RTDataKeys` — created during the foundation port).

NeoForge:
```java
stack.set(ModDataComponents.TARGET_TIME, 6000);
int t = stack.getOrDefault(ModDataComponents.TARGET_TIME, 0);
```
1.20.1:
```java
stack.getOrCreateTag().putInt(RTDataKeys.TARGET_TIME, 6000);
int t = stack.hasTag() ? stack.getTag().getInt(RTDataKeys.TARGET_TIME) : 0;
// or via helper:  RTNbt.setInt(stack, RTDataKeys.TARGET_TIME, 6000);
```
Mapping of component value types → NBT:
| Component type | NBT storage |
| --- | --- |
| `Integer`/`Boolean`/`String`/`int[]` | `putInt`/`putBoolean`/`putString`/`putIntArray` |
| `BlockPos` | `NbtUtils.writeBlockPos` / store x,y,z ints |
| `ResourceLocation` | `putString(loc.toString())` |
| `UUID` | `putUUID` |
| `DyeColor` | `putInt(color.getId())` or `putString(color.getName())` |
| `ItemContainerContents` | `ContainerHelper.saveAllItems` into a sub-tag; size tracked separately |
| custom Codec records (`PortkeyTarget`, `ItemFilterData`, `ChunkAnalyzerResult`) | encode the existing `CODEC` to `Tag` via `codec.encodeStart(NbtOps.INSTANCE, value)` and store the resulting `Tag`; decode on read |
| `SimpleFluidContent` (fluid in bucket) | store fluid id + amount ints, or a `FluidStack` NBT (platform-specific — see capabilities) |

> Records that already carry a `Codec` (PortkeyTarget, ItemFilterData, ChunkAnalyzerResult,
> RuneData, etc.) port cheaply: keep the record + codec, drop the `StreamCodec`, and serialize the
> codec to NBT. The `StreamCodec`s on those records move to packet (de)serialization (see §3).

### 3. Networking — Architectury `NetworkManager`

1.20.5+ payload API (`CustomPacketPayload`, `StreamCodec`, `PayloadRegistrar`,
`registrar.playToServer/playToClient`) **does not exist in 1.20.1**. Use Architectury:
```java
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

ResourceLocation ID = new ResourceLocation(MOD_ID, "container_signal");

// register (common init, guard client receivers with side)
NetworkManager.registerReceiver(NetworkManager.c2s(), ID, (buf, ctx) -> { /* read buf, ctx.queue(...) */ });
NetworkManager.registerReceiver(NetworkManager.s2c(), ID, (buf, ctx) -> { ... }); // client only

// send
NetworkManager.sendToServer(ID, buf);
NetworkManager.sendToPlayer(serverPlayer, ID, buf);
```
Port each `messages/*` record to manual `FriendlyByteBuf` read/write (the `STREAM_CODEC` tells you
the field order). `IPayloadContext` → Architectury `NetworkManager.PacketContext`
(`ctx.getPlayer()`, `ctx.queue(Runnable)`, `ctx.getEnvironment()`).
**Keep `Minecraft.getInstance()` out of common** — client-bound handlers must call into a
client-only class (see §6 / server-safety rules).

### 4. Capabilities (item handlers / fluids / energy)

NeoForge's unified `Capabilities.ItemHandler.BLOCK` / `registerCapabilities` event does not exist.
Strategy:
- **Item handling:** Architectury does not abstract inventory transfer. Use a platform service.
  Forge: `IItemHandler` + `ICapabilityProvider` on the block entity. Fabric: expose
  `net.fabricmc.fabric.api.transfer.v1` `Storage<ItemVariant>` (or, simpler, implement
  `WorldlyContainer`/`Container` on the BE so hoppers work on both loaders without the Transfer
  API). For Player Interface / Item Collector / Rerouter / Dyeing Machine / Imbuing Station, prefer
  vanilla `Container`/`WorldlyContainer` where the original behaviour allows; fall back to a
  per-platform capability provider where true sided `IItemHandler` semantics are required.
- **Fluids (Ender Bucket):** Forge `FluidUtil` + `IFluidHandlerItem`; Fabric Transfer API
  `FluidStorage`. Abstract behind a platform service interface (e.g. `FluidHelper`).
- **Energy (Spectre network) ✅ — implemented with ZERO added required dependencies.** The Spectre
  buffer itself is plain per-player `SavedData` (`handler/spectrecoil/SpectreCoilHandler`, global via
  the overworld's data storage). Only the two interop seams go through the new
  `platform/IEnergyBridge` service (`Services.ENERGY`):
  1. *Coils pushing into machines* — `insertEnergy(level, pos, side, amount, simulate)`.
  2. *Injector receiving from generators* — exposed platform-side.
  - **Forge:** `ForgeEnergyBridge` uses Forge Energy (built into the loader); the injector's
    receive-only `IEnergyStorage` is attached via `AttachCapabilitiesEvent` (`ForgeEnergyAttach`).
  - **Fabric:** *optional* Team Reborn Energy integration — `modCompileOnly "teamreborn:energy:3.0.0"`
    (package `team.reborn.energy.api`; maven.modmuss50.me). Nothing bundled, nothing required:
    `FabricEnergyBridge` checks class presence once and only then classloads `TREnergyCompat`
    (`EnergyStorage.SIDED` lookup + a `SnapshotParticipant` adapter for correct transaction
    semantics). Without TR Energy the bridge no-ops — and there are no energy machines to interop
    with anyway, so nothing is lost. Verified: 0 `team/reborn` classes in the fabric jar, no new
    `depends` entry.
  - Coil `canAttachTo` relaxed to *energy-capable OR sturdy face* so coils stay placeable when no
    energy API is present.

Put each capability bridge behind an interface in `lumien.randomthings.platform` resolved via
`Services.load(...)`, with `fabric`/`forge` implementations registered in `META-INF/services`.

### 5. Events — Architectury Events

`NeoForge.EVENT_BUS.addListener(...)` / `@SubscribeEvent` → `dev.architectury.event.events.*`:
| NeoForge event | Architectury |
| --- | --- |
| `ServerTickEvent` | `TickEvent.SERVER_PRE/POST` |
| `PlayerTickEvent` | `TickEvent.PLAYER_PRE/POST` |
| `LivingDeathEvent` | `EntityEvent.LIVING_DEATH` |
| `PlayerInteractEvent.RightClickBlock` | `InteractionEvent.RIGHT_CLICK_BLOCK` |
| `BlockEvent.BreakEvent` / place | `BlockEvent.BREAK` / `InteractionEvent` |
| `LivingDropsEvent`, `PlayerEvent.Clone` | `EntityEvent.*` (some need a mixin — no arch event) |
| `RegisterCommandsEvent` | `CommandRegistrationEvent.EVENT` |
| `EntityAttributeCreationEvent` | `EntityAttributeRegistry.register(...)` |
Anything without an Architectury equivalent (e.g. block-drops-to-inventory for the Magnetic
enchantment, living jump) needs a **mixin** in `common` (`lumien.randomthings.mixin`).

### 6. Client + server-safety

- `ClientProxy` / `ClientModEvents` → Architectury client registries:
  `dev.architectury.registry.client.rendering.*` (`BlockEntityRendererRegistry`,
  `EntityRendererRegistry`, `ColorHandlerRegistry`, `RenderTypeRegistry`),
  `MenuRegistry.registerScreenFactory(...)` for screens, `EntityAttributeRegistry` for attributes.
- Gate client-only init through the Fabric `client` entrypoint and the Forge
  `FMLClientSetupEvent`, or `dev.architectury.utils.Env`/`EnvExecutor.runInEnv`.
- **Never reference `net.minecraft.client.*` / `com.mojang.blaze3d.*` from common code that runs on
  a server** (tooltip bodies, packet handlers, util). Route through a client-only class invoked via
  `EnvExecutor`. (Same class of bug that crashed dedicated servers on 1.21.1 — see the original
  `porting-notes/README.md` server-safety rules; they still apply.)

### 7. Small but pervasive API diffs

| 1.21.1 | 1.20.1 |
| --- | --- |
| `ResourceLocation.fromNamespaceAndPath(ns, p)` / `.parse(s)` | `new ResourceLocation(ns, p)` / `new ResourceLocation(s)` |
| `pack_format` 34 | `pack_format` **15** |
| `BlockBehaviour.Properties.of()` (unchanged) | same |
| `Component.translatable` (unchanged) | same |
| `Registries.*` (unchanged) | same |
| `level.getBlockState` etc. (unchanged) | same |
| `Item.Properties` (no `.arch$tab` on NeoForge) | use `.arch$tab(tab)` (Architectury) |
| compatibilityLevel `JAVA_21` in mixins | `JAVA_17` |
| `EntityType.Builder` `.build(String)` | `.build(String)` may be `.build()` — verify per type |

---

## Working order

Follow the existing `porting-notes/order.md` grouping (still valid; the *features* are the same,
only the API changes):
1. Foundation (this scaffold): registries, init, creative tab, NBT helper, network manager, event
   bridge, platform services. **Done.**
2. **Group 1 — redstone blocks. ✅ Done** (builds on Fabric + Forge). Ported: Online Detector,
   Redstone Observer, Basic Redstone Interface, Redstone Tool (linker), Entity Detector +
   Entity Filter, Chat Detector, Global Chat Detector + ID Card. `ChatEventHandler` on Architectury
   `ChatEvent.RECEIVED`.
   - **Deferred within Group 1:** the Floo-teleport branch of `ChatEventHandler` (needs Group 6 —
     marked with a `TODO(Group 6)`); the Redstone Interface link-beam BE renderer (cosmetic — the
     `client/RedstoneInterfaceRenderer` was not ported; the block works without it).
   - **Pulled in early as dependencies:** `EntityFilterItem`, `IdCardItem` (these are filter/utility
     items that also belong to Group 2; ported here because the detectors need them).
3. **Group 2 — item handling. ✅ Done** (builds on Fabric + Forge). Ported: Item Collector,
   Advanced Item Collector + Item Filter (+ `GhostSlot`, `ToggleButton`), Inventory Rerouter,
   Dyeing Machine + custom recipe type/serializer + 15 dyeing recipes.
   - **Cross-loader item handling decision:** NeoForge `IItemHandler`/`ItemStackHandler`/capability
     queries → **vanilla `Container`/`WorldlyContainer`** (`util/RTContainers` reads neighbours via
     `HopperBlockEntity.getContainerAt` and inserts sided-aware). Works with vanilla hoppers on both
     loaders with zero platform code. The Inventory Rerouter is a `WorldlyContainer` proxy that
     forwards to its target with per-face remapping. *Limitation:* inventories exposing only a Forge
     `IItemHandler` / Fabric `Storage` (not a `Container`) aren't seen — acceptable for the golden
     path; revisit with a per-platform capability bridge if needed.
   - **1.20.1 recipe API:** pre-Codec. `RecipeSerializer` uses `fromJson(ResourceLocation,JsonObject)`
     + `fromNetwork(ResourceLocation,FriendlyByteBuf)` + `toNetwork`; recipes carry `getId()`;
     `Recipe<Container>` (no `RecipeInput`); `getRecipeFor` returns `Optional<T>` (no `RecipeHolder`).
   - **Deferred:** JEI category/catalyst for the Dyeing Machine (cross-cutting — do once for all
     recipe blocks in a dedicated JEI pass; the machine works without it).
4. **Group 3 — crafting stations. ✅ Done** (builds on Fabric + Forge). Ported: Imbuing Station
   (block + BE 5-slot `Container` + menu + screen + custom `imbuing` recipe type/serializer +
   example recipe) and 11 wood-typed Custom Crafting Tables (re-skinned vanilla `CraftingTableBlock`,
   vanilla `minecraft:crafting`). Self-drop loot tables generated for all 12 blocks.
   - Imbuing recipe uses the same 1.20.1 pre-Codec pattern as the Dyeing Machine
     (`Recipe<Container>`, `fromJson`/`fromNetwork`/`toNetwork`, `getId()`).
   - **Deferred:** JEI categories/catalysts for the **Dyeing Machine + Imbuing Station** (both recipe
     blocks now exist). JEI plugin entry points are per-loader (`fabric`/`forge` modules), so this is
     a discrete cross-cutting pass — the machines work without it.
5. **Group 5 — decorative/sound. ✅ Done** (builds on Fabric + Forge). Ported: Sound Box
   (block + BE, plays a stored Sound Pattern's sound on redstone rising edge) + `ItemSoundPattern`;
   Colored Grass (one block + `COLOR` blockstate, 16 `ColoredGrassItem`s) with per-block/per-item
   tint via Architectury `ColorHandlerRegistry`. Self-drop loot for both blocks.
   - **1.20.5+→1.20.1 fixes worth noting (vanilla-vs-Forge API):** `useItemOn`/`ItemInteractionResult`
     → `use`/`InteractionResult`; `getCloneItemStack(state,hit,level,pos,player)` → 3-arg
     `getCloneItemStack(level,pos,state)` (5-arg is Forge-only); `BlockEntity.handleUpdateTag` is
     Forge-only (drop the override — `getUpdateTag`+`getUpdatePacket` suffice); per-item
     `ItemProperties.register` is non-public in vanilla 1.20.1 (needs Forge AT / Fabric AW) — the
     Sound Pattern's `has_sound` model swap is therefore skipped (cosmetic).
   - **Deferred:** the broader sound subsystem — **Sound Recorder, Sound Dampener, Portable Sound
     Dampener** (the latter mute world sounds via a client mixin). Not part of this group; the Sound
     Pattern's sound is normally set by the Sound Recorder, so until that's ported a pattern can only
     be configured in creative/by command.
6. **Group 6 — player mechanics. ✅ Done** (builds on Fabric + Forge). Ported: Floo Teleportation
   (Floo Brick block + BE, `FlooNetworkSavedData` per-dimension registry, Floo Sign / Powder / Pouch
   items, chat-triggered teleport re-wired into `ChatEventHandler`) and the Magnetic enchantment
   (code-based `Enchantment` + drops-to-inventory via Architectury `BlockEvent.BREAK` in `RTEvents`).
   - **1.20.1-vs-Forge/1.20.5 fixes:** `SavedData` uses `save(CompoundTag)` + `computeIfAbsent(load,
     factory, name)` (no `HolderLookup.Provider`/`Factory`); `NbtUtils.writeBlockPos`→`CompoundTag`,
     `readBlockPos(CompoundTag)`→`BlockPos` (no key/Optional); `BlockEntity.onLoad()` is Forge-only
     (dropped — the `/setblock`-move resync safety net); enchantments are code-based, not data JSON;
     `BlockDropsEvent` (NeoForge 1.21) → reimplemented harvest on `BlockEvent.BREAK`.
   - **Deferred:** the **Floo Token + Temporary Fireplace entity** (the token needs Forge-only
     `onEntityItemUpdate`, and the entity uses the 1.20.5 `SynchedEntityData.Builder`) — needs a
     per-platform item-entity tick hook. The `floo_powder` recipe substitutes `blaze_powder` for the
     unported `randomthings:bean`. Magnetic's harvest skips XP-orb/Fortune-on-non-standard edge cases.
   - **Not in this group** (separate features, live in the 1.21.1 `RTEventHandler`): Lava Charm,
     Water-Walking Boots, Lava Waders, Super Lubricent Boots, Portkey, Spectre Anchor, etc.
7. **Group 4 — Spectre dimension. ⚠️ Partially done — large multi-part subsystem.**
   - The brief's named features (Spectre **Tree**, Spectre **Lens**) were **never implemented** in the
     1.21.1 source — they were aspirational forward-port notes. What actually exists to downport is the
     Spectre *dimension system*, the mod's largest and most API-entangled subsystem.
   - **Done (self-contained subset, builds on both loaders):** Spectre Block (unbreakable wall),
     Spectre Ingot, Spectre String (+ block model/blockstate/loot, item models, lang). No recipes —
     they depend on the unported **Ectoplasm** item.
   - **Deferred — each a substantial sub-project with prerequisites:**
     1. **Spectre dimension worldgen** — custom `SpectreChunkGenerator` (+ `SpectreCube`,
        `SpectreHandler`, `ModChunkGenerators`) + `dimension`/`dimension_type`/`biome` JSON. 1.20.1
        `ChunkGenerator` differs: `codec()` returns `Codec` not `MapCodec`; `fillFromNoise` takes a
        leading `Executor`; register a `Codec` (not `MapCodec`) into `Registries.CHUNK_GENERATOR`.
     2. **Spectre energy network** — Spectre Coil / Core / Energy Injector + `SpectreCoilHandler`.
        Needs a **cross-loader energy-capability bridge** (Forge `IEnergyStorage` / Fabric Team Reborn
        Energy) — analogous infra to the item-handler decision but for energy. Not yet built.
     3. **Spectre tools** (Axe/Pickaxe/Shovel/Sword) — use the 1.20.5+ component attribute API
        (`ItemAttributeModifiers`/`EquipmentSlotGroup`) → rework to the 1.20.1 constructor-arg tool API;
        the `Tier` uses NeoForge `SimpleTier` + repairs on unported **Ectoplasm**.
     4. **Spectre items** — Charger (energy), Key/Anchor (dimension/death-drop event), Illuminator
        (lighting handler + client mixin).
     5. **Worldgen features** (`ModFeatures`: AncientFurnace, BloodRose, Lotus, GlowingMushroom,
        NatureCore, PitcherPlant, BeanSprout) — most depend on *other* unported plant/block features.
   - **Prerequisites that block the bulk:** the **Ectoplasm** item, a **cross-loader energy bridge**,
     and the `ChunkGenerator` port. Recommend tackling these as their own focused efforts.

Port a whole feature vertically (block + item + block entity + menu + screen + recipe + NBT +
assets + client renderer) so each group is independently testable in-game on both loaders.

---

## Deferral mop-up (in progress)

- **Lava Charm ✅** — first standalone player item. Establishes the cross-loader player-item pattern:
  recharge on `TickEvent.PLAYER_POST`, and cancel lava damage via `EntityEvent.LIVING_HURT`
  (return `EventResult.interruptFalse()`). NBT charge/cooldown via `RTNbt`. See
  `item/LavaCharmItem` + `event/LavaCharmHandler`.
- **Boots ✅** — Water-Walking Boots, Lava Waders, Super Lubricent Boots. Rebuilt `ModArmorMaterials`
  on the 1.20.1 `ArmorMaterial` *interface* (`RTArmorMaterial`; the 1.20.5 record/`Holder`/`Layer`/`BODY`
  forms don't exist). `ArmorItem(material, Type, props)` (no `durability(0)` — indestructible boots use
  a huge material durability). Behaviour in `event/BootsHandler` on `TickEvent.PLAYER_POST`: fluid-walk
  (water/lava surface snap), Super Lubricent friction correction (`getBlockPosBelowThatAffectsMyMovement`
  is protected in 1.20.1 — computed the pos manually), Lava Waders recharge + lava immunity (via
  `LavaCharmHandler`'s `LIVING_HURT`).
  - *Deferred:* boots recipes (depend on unported Super Lubricent Tincture / Obsidian Water-Walking
    Boots); worn-armor *layer* textures (1.20.1 vanilla forces the `minecraft` namespace for armor
    models cross-loader — item icons work, the worn model may show a missing texture).
- **Obsidian Skull ✅** — probabilistic non-lava fire-damage negation, folded into `LavaCharmHandler`'s
  `LIVING_HURT` listener (fire branch). Fully craftable (all-vanilla recipe).
- **JEI (Dyeing + Imbuing) ✅** — `@JeiPlugin` `RandomThingsJEIPlugin` + `DyeingMachineCategory` /
  `ImbuingCategory` in **common** (shadowed into both jars; JEI's annotation scan finds it on Fabric
  and Forge). 1.20.1/JEI-15 API: recipes are used directly (no `RecipeHolder`),
  `new RecipeType<>(uid, Recipe.class)`, `getAllRecipesFor` returns `List<Recipe>`. Builds on both
  loaders; in-game JEI display still wants a runtime smoke-test. Catalysts: Dyeing Machine + Imbuing
  Station blocks (Custom Crafting Tables correctly use vanilla crafting → no catalyst).
- **Portkey ✅ — established the project's first cross-loader mixin.** Teleport-on-pickup + ground
  aging had no Architectury event, so `mixin/ItemEntityMixin` `@Inject`s into `ItemEntity#tick`
  (age) and `#playerTouch` (HEAD, cancellable → teleport), calling `event/PortkeyHandler`. Plain
  Mixin `@Inject` (no MixinExtras); the common mixin config (`randomthings.mixins.json`, already
  wired into `fabric.mod.json` + the Forge `loom.forge.mixinConfig`) generates the refmap and applies
  on both loaders. `PortkeyTarget` keeps its `Codec` (stored via `RTNbt.setCodec`); recipe deferred
  (needs unported `stable_ender_pearl`).
  - **The mixin pattern is now available** for the other Forge-only/event-less hooks (Floo Token's
    item-entity tick, the redstone `SignalGetter` mixin, etc.).
- **Floo Token ✅ — Floo feature now complete.** Ported `FlooTokenItem` + the **Temporary Fireplace
  entity** (1.20.1 `defineSynchedData()` no-arg; registered via a new `entity/ModEntityTypes`; no-op
  client renderer through Architectury `EntityRendererRegistry` at
  `registry.client.level.entity`). Ground-aging reuses `ItemEntityMixin#tick` →
  `event/FlooTokenHandler`; the chat temp-fireplace teleport path is wired into `ChatEventHandler`.
  Craftable (paper + floo powder).
- **Mop-up basket fully cleared.** Done: Lava Charm, 3 boots, Obsidian Skull, Portkey, JEI
  (Dyeing/Imbuing), Floo Token. Remaining project work is the Spectre dimension infrastructure and
  minor leftovers (sound subsystem, deferred recipes needing unported items).
- **Advanced Redstone Wall Torch ✅** — completes the Advanced Redstone Torch feature. Base torch
  gained a `protected (Properties)` ctor + `protected GREEN_DUST`; wall variant reuses
  `WallTorchBlock.getShape`/`getStateForPlacement` and excludes the FACING side from `getSignal`.
  Item switched to `StandingAndWallBlockItem(standing, wall, props, Direction.DOWN)`; wall block
  added to the ADVANCED_REDSTONE_TORCH BE type's valid blocks; wall loot drops the torch item.
- **Spectre Dimension ✅ — the final deferral, now complete.** Per-player void rooms:
  `SpectreChunkGenerator` (empty chunks; 1.20.1: `codec()` returns `Codec` not `MapCodec`,
  `fillFromNoise` takes a leading `Executor`, `getSpawnHeight` is abstract) registered via
  `ModChunkGenerators` into `Registries.CHUNK_GENERATOR`; datapack JSONs for
  `dimension_type/spectre` (1.20.1 IntProvider needs the nested `"value"` form for
  monster_spawn_light_level), `dimension/spectre`, and the `spectre_void` biome.
  `SpectreHandler` SavedData allocates 16-wide cubes along the X axis — **return positions moved
  from `player.getPersistentData()` (Forge-only) into the SavedData itself** (also survives
  restarts now, which the source's in-memory persistent data did not across relogs).
  `DimensionTransition` → `ServerPlayer.teleportTo(level,...)`. `SpectreCube` (hollow 16x16 room,
  Ectoplasm-expandable height), `SpectreCoreBlock` (2x2 center; Forge-only
  canEntityDestroy/canDropFromExplosion overrides dropped — the -1 strength/3.6M resistance
  properties already cover it), `SpectreKeyItem` (5s hold-to-use; `getUseDuration(stack)` 1-arg),
  `SpectreDimensionHandler` (`TickEvent.PLAYER_POST` kicks intruders out of foreign cubes).
  Spectre Core excluded from the creative tab by registry id. Craftable: spectre key
  (ectoplasm + stable ender pearl).
- **Ender Buckets + Stable Ender Pearl ✅ — no fluids bridge needed.** The 1.21.1 buckets only
  ever used their NeoForge fluid handler against *world* blocks (no tank interop), so the port
  stores fluid in item NBT (`EnderBucketFluidHelper`: registry id + millibuckets) and talks to
  the world directly — `BucketPickup.pickupBlock` for pickup (handles waterlogging), a
  `BucketItem.emptyContents` replica for placement (ultrawarm vaporize, waterloggable containers,
  replaceables). Works with any registered FlowingFluid; zero loader fluid APIs. Ender Bucket
  flood-fills to the nearest connected source; Reinforced holds 10 buckets and sneak-collects all
  connected sources. Dropped `getMaxStackSize(stack)` (Forge-only; filled buckets don't stack
  anyway thanks to NBT). **Stable Ender Pearl** ported alongside (recipe dependency): the source's
  Forge-only `hasCustomEntity`/`createEntity` ItemEntity subclass became
  `event/StableEnderPearlHandler` driven from the existing `ItemEntityMixin` tick hook, with the
  countdown in the dropped stack's NBT. This also unblocked the deferred **Portkey recipe** —
  copied it over.
- **Plant Chest ✅ — establishes the cross-loader BEWLR (item-renderer) pattern.** Chest-like
  block (ChestBlockEntity subclass with the plant-chest loot table, vanilla ChestMenu via
  `player.openMenu`, lid animation ticker) + `PlantChestModel`/`PlantChestRenderer` in common
  (model layer via Architectury `EntityModelLayerRegistry`). The `builtin/entity` item model needs
  per-loader wiring: `IPlatformHelper.createPlantChestItem` — Forge returns `PlantChestForgeItem`
  (`initializeClient` → `IClientItemExtensions.getCustomRenderer()` → a BEWLR delegating to the
  common `PlantChestRenderer.renderItemModel`), Fabric returns a plain BlockItem and registers
  `BuiltinItemRendererRegistry` in `FabricRandomThingsClient`. Reuse this seam for any future
  builtin/entity item. `NatureCoreFeature` now places the real plant chest (vanilla-chest stopgap
  removed).
- **Nature Core ✅** — `BlockNatureCore` + BE (per-tick chances: sand→grass/dirt, biome-list
  animal spawning, bonemeal pulses, oak-sapling ring planting, structure self-repair) +
  `NatureCoreFeature` worldgen (3x3x3 jungle-wood shrine, overworld-wide via
  `BiomeModifications` + placed-feature 1/540 rarity). 1.20.1 notes: the source's NeoForge biome
  tags (dense/sparse/wet/dry/dead) are approximated with vanilla tags (IS_JUNGLE/IS_SAVANNA/
  swamp keys/IS_BADLANDS/desert); `Blocks.SHORT_GRASS`→`Blocks.GRASS`; the worldgen loot chest is
  a **vanilla chest** with the `chests/plant_chest` loot table until the custom Plant Chest block
  is ported (swap it back in `NatureCoreFeature.placePlantChest` then). Fixed a source bug: the
  plant-chest loot table referenced `bean_sprout` but the item id is `beansprout`.
- **Ancient Furnace ✅** — the full multiblock: `AncientFurnaceBlock`+BE (nether-star activation
  via the brick's STAR_EMPTY variant → 20s warmup breaking a 5x5x5 shell → per-tick biome flood
  fill → TNT explosion at the end), `AncientFurnaceRenderer` BER (animated glowing rune overlays,
  1.20.1 vertex API), `AncientFurnaceFeature` worldgen (3x3x3, 1/2000 chunks in cold biomes) with
  `ModFeatures` + Architectury `BiomeModifications.addProperties` replacing the NeoForge
  biome-modifier JSON. **Improvements over source:** `setBiome` actually rewrites the biome column
  via `fillBiomesFromNoise` + `resendBiomesForChunks` (the 1.21.1 source stubbed it); the o0-o3
  overlay textures are registered in `assets/minecraft/atlases/blocks.json` (source never stitched
  them — its overlays were missing textures); BE state/counter now syncs to clients so the fade-in
  renders (source relied on setChanged only). The client warmup counter self-advances in clientTick.
- **Golden Chicken + Golden Egg ✅** — `GoldenChickenEntity` (Chicken subclass; eats dropped gold
  ore/raw gold, lays gold ingots 30-60s later; breeds true) + `GoldenEggEntity`
  (ThrowableItemProjectile; hatches a golden chicken on impact) + `GoldenChickenRenderer`
  (vanilla ChickenModel + mod texture). The placeholder golden_egg item was upgraded to the real
  throwable `GoldenEggItem`. First living mob of the port: attributes registered cross-loader via
  Architectury `EntityAttributeRegistry.register` in `ModEntityTypes.register()`.
- **Rain Shield ✅** — block + BE (redstone-toggleable, static weak-set of shields, 80-block 2D
  radius) with flame/smoke spiral while raining. Client suppression via two more injections in
  `mixin/client/LevelRendererMixin`: `renderSnowAndRain` HEAD-cancel (rain rendering) and
  `tickRain` HEAD-cancel (rain ground particles **and** rain sounds — one hook replaces the
  source's per-particle class-name matching and its no-op sound handler). 1.20.1 notes:
  `onChunkUnloaded` is Forge-only — replaced with a `level.hasChunkAt` guard in `shouldRain`;
  the source's server-side snow/fire event handlers hooked events vanilla weather never fires
  (dead code) and were dropped. Fully craftable (flint/blaze rod/netherrack).
- **Divining rod wireframe overlay ✅** — replaced the interim particle locator with the real
  overlay: `client/renderer/DiviningRodRenderer` + `client/util/RenderUtils` (1.20.1 vertex API:
  `.vertex().color().endVertex()` on `RenderType.debugQuads()`/`debugLineStrip`). Rendered from a
  new `renderLevel` TAIL inject in `mixin/client/LevelRendererMixin` (Architectury has no
  cross-loader level-render event); scan runs on `ClientTickEvent.CLIENT_POST`.
- **Magic Hood stealth ✅** — two new client mixins: `LivingEntityRendererMixin`
  (`shouldShowName` HEAD → false for hooded players) and `ParticleEngineMixin` (`createParticle`
  HEAD → null for ENTITY_EFFECT/AMBIENT_ENTITY_EFFECT within 2 blocks of a hooded player; cleaner
  than the source's class-name string matching). Chest loot (dungeon 5%, village toolsmith 15%)
  via Architectury `LootEvent.MODIFY_LOOT_TABLE` in `event/LootHandler` — replaces the NeoForge
  global loot modifiers.
- **Diaphanous invert recipe ✅** — the 1.21.1 source used a shapeless recipe with result
  *components* (1.20.5+ only). Reimplemented inside `DiaphanousBlockRecipe`: a lone diaphanous
  block in the grid now toggles `DIAPHANOUS_INVERTED` while keeping the disguise appearance.

## Worked example: Online Detector (the GUI-block template)

The first ported feature (`ONLINE_DETECTOR`) establishes the reusable template for every
block-with-GUI. Concrete per-class changes applied (use as a checklist):

- **Block** (`block/OnlineDetectorBlock`): drop the 1.20.5+ `MapCodec CODEC` + `codec()` override
  (BaseEntityBlock has no abstract `codec()` in 1.20.1). `useWithoutItem(state,level,pos,player,hit)`
  → `use(state,level,pos,player,hand,hit)`. `serverPlayer.openMenu(provider,pos)` →
  `MenuRegistry.openExtendedMenu(serverPlayer, blockEntity)`.
- **Block entity** (`blockentity/OnlineDetectorBlockEntity`): `saveAdditional(tag, registries)` →
  `saveAdditional(tag)`; `loadAdditional(tag, registries)` → `load(tag)`; `getUpdateTag(registries)`
  → `getUpdateTag()`; drop the `handleUpdateTag` override (default calls `load`). Implement
  Architectury `ExtendedMenuProvider` (adds `saveExtraData(buf)` — write the block pos) instead of
  plain `MenuProvider`.
- **Menu** (`menu/OnlineDetectorMenu`): add a second constructor `(int, Inventory, FriendlyByteBuf)`
  that reads the pos — this is the client factory used by `MenuRegistry.ofExtended(...)` in
  `ModMenuTypes`.
- **Screen** (`client/screen/OnlineDetectorScreen`): annotate `@Environment(EnvType.CLIENT)`;
  `ResourceLocation.fromNamespaceAndPath(ns,p)` → `new ResourceLocation(ns,p)`;
  `PacketDistributor.sendToServer(pkt)` → `RTNetwork.sendToServer(pkt)`;
  `KeyMapping.getKey().getValue()` → `KeyMapping.matches(keyCode, scanCode)`.
- **Packet** (`network/OnlineDetectorUpdatePacket`): drop `CustomPacketPayload`/`StreamCodec`/`Type`;
  implement `RTPacket` (`id()`, `encode(buf)`), add static `decode(buf)` + `handle(PacketContext)`
  (`context.player()`→`getPlayer()`, `context.enqueueWork`→`context.queue`). Register the receiver
  in `RTNetwork.register()`.
- **Registration:** block→`ModBlocks`, `BlockItem` (`.arch$tab(RT_CREATIVE_TAB)`)→`ModItems`,
  type→`ModBlockEntityTypes`, menu (`MenuRegistry.ofExtended`)→`ModMenuTypes`, screen factory
  (`MenuRegistry.registerScreenFactory`)→`client/RandomThingsClient.init()`.
- **Assets/data:** copy blockstate/models/textures/GUI texture verbatim from the 1.21.1 `src/`.
  Recipes: `data/<ns>/recipe/` → `data/<ns>/recipes/`, and the result key `"id"` → `"item"`.
  Loot tables: `data/<ns>/loot_tables/blocks/` (path unchanged). Add lang keys to
  `common/.../lang/en_us.json`.

### Client init plumbing (already wired)

`common/.../client/RandomThingsClient.init()` holds all client registration and is invoked from
`FabricRandomThingsClient.onInitializeClient()` (Fabric) and `forge/ForgeClientSetup.onClientSetup`
(Forge `FMLClientSetupEvent.enqueueWork`). `RTNetwork.register()` runs in common `RandomThings.init`.

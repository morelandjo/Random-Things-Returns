# Random Things Returns

Port of Lumien's **Random Things** mod.

- **Branch `1.21.1`** — NeoForge, Minecraft 1.21.1 (single loader).
- **Branch `1.20.1`** — Architectury multiloader (**Fabric + Forge**), Minecraft 1.20.1. This is a
  backport of the 1.21.1 code. See [`PORTING.md`](PORTING.md) for the build/run instructions
  (note: **Gradle needs JDK 21**) and the full 1.21.1→1.20.1 API migration guide.

- Mod ID: `randomthings`
- Original mod (1.12.2 Forge): authored by Lumien, MIT licensed
- Port maintained by: Vodmordia

## Upstream reference

The original 1.12.2 Forge source is checked out locally at:

```
/Users/josh/Documents/minecraftmodding/othermodsgits/Random-Things
```

(branch `1.12.2`). When porting a feature, use this as the canonical specification:

- **Behaviour:** read the upstream class/recipe/tile entity to understand the original mechanics.
  The modloader API is completely different between 1.12.2 Forge and 1.21.1 NeoForge, so do
  not copy code directly — translate the *behaviour*.
- **Textures:** prefer pulling textures verbatim from
  `othermodsgits/Random-Things/src/main/resources/assets/randomthings/textures/` (blocks, items,
  GUIs) so the visual style stays consistent with the original mod. Resize / re-export only when
  the 1.21 model system requires a different layout.
- **GUIs:** match the original GUI layouts (slot positions, background art, progress arrows)
  as closely as the 1.21 menu/screen system allows. The original GUI background PNGs live
  under `textures/gui/` upstream and are the source of truth.

Upstream wiki (canonical feature list): https://lumien.net/rtwiki/

## Porting work

The `porting-notes/` directory (gitignored) contains group-by-group briefs that drive the
remaining backport work. Start with `porting-notes/README.md` and `porting-notes/order.md`.

## Building

Standard NeoForge ModDevGradle build. Versions live in `gradle.properties`.

```
./gradlew build
./gradlew runClient   # client test
./gradlew runServer   # dedicated server test
```

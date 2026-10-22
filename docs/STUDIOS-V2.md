# Skybox Gallery + Cosmetic Looks — Studio V2

This change extends the **existing** World Studio and Cosmetic Studio in the single Unified client. It does not replace renderers, introduce external downloads, or modify Minecraft server state.

## Skybox Gallery

Open **Right Shift → World Studio → Sky Gallery**. The gallery scans only `config/alien-client/skyboxes/` for local PNG filenames, with an upper bound of 64 files. It displays standard equirectangular PNGs and 3×2 `*.cube.png` atlases using a real in-game texture preview.

- **Favorite:** put up to 16 preferred PNG names first in the list. Favorites persist in the current world/profile JSON and are cleaned on import.
- **Apply selected:** validate and preview the PNG before enabling it. No invalid PNG gets applied by pressing the button.
- **Procedural:** clear the imported filename and restore built-in procedural sky rendering.
- **Refresh:** rescan the dedicated folder after copying files.
- Invalid/oversized files are rejected by the existing safe `SkyboxLoader` (16 MiB; max 4096×2048 PNG; cubemap atlas geometry checked). This screen never fetches remote content.
- The screen deletes its previous dynamic preview texture when changing files or closing, avoiding preview GPU resource leaks.

**Limitations:** the preview shows the full input PNG **as a flat image**; it is not a simulated 360° immersive view. Skybox rotation, drift and brightness are still controlled by Sky Options, and the actual cubemap renderer is unchanged. Seam/orientation review in real gameplay remains necessary.

## Cosmetic Looks

Open **Right Shift → Cosmetic Studio → Looks**. Apply any of four original client-mesh combinations:

| Look | Combination |
| --- | --- |
| Ronin | Samurai hat, Kitsune mask, Cape |
| Celestial | Crown, Wings, star-pattern cape |
| Sakura | Japanese hat, Kitsune mask, Cape |
| Phantom | Futuristic visor, Wings, hand charm |

Original mesh assets are reused. Applying a bundled look clears a previously selected external model/texture and keeps the character's fit and transformations (position, rotation, scale).

Save the currently selected appearance to **Look slot 1, 2 or 3**, then load it again later. Slots are saved inside the currently active JSON profile and work for either self or an individual player's local cosmetics; a loaded slot is a deep copy, not a shared object. Older profile JSONs without slots continue loading normally.

## Verification and boundaries

JUnit tests cover directory traversal resistance, symlink handling, file indexing limits, favorites, old JSON profiles, all four looks, deep-copy snapshots, and corrupted data normalization. Test the new GUI screens under actual Forge 1.8.9/Java 8 before distributing.

This is a separate feature PR. **PR #6 (the installer) and `main` remain untouched until validation and an explicit release choice.**

# Alien Client Unified — Combat Studio (experimental)

The **`main` branch now builds a single Forge 1.8.9 JAR by default**, `AlienClient-Unified-1.8.9.jar`, combining PR #3 (PanelStudio foundation), PR #4 (Sky Studio Ultimate) and PR #5 (Combat Studio / Unified). Older Legit and Full JARs are retained only as optional legacy builds. The combined integration has passed the actual Minecraft test workflows; **a Windows/OptiFine/real GPU release check is still required**.

## What genuinely runs

The existing HUD, World Studio, Cosmetics and Effects are still available in Unified. In the **Combat** column or **Dashboard → Combat Studio** a user can choose:

| Profile | Enabled | Aim assist | Click assist | Aim requires held LMB | Default strength/FOV |
| --- | --- | --- | --- | --- | --- |
| OFF | No | No | No | Yes | 0.15 / 35° |
| LEGIT | Yes | Yes | No | Yes | 0.12 / 25° |
| FULL | Yes | Yes | Yes | No | 0.55 / 110° |
| CUSTOM | Independently editable | Adjustable | Adjustable | Adjustable | Adjusted by sliders |

These labels represent preset strengths, **not any promise that a multiplayer server considers the behavior legitimate**. Both presets may violate PvP server rules and lead to sanctions.

- Aim assist: finds the nearest-on-screen living, visible (by default) non-invisible player within 2–6 blocks and the specified FOV, and **gradually turns the actual local camera**. Uses vanilla tick updates; NO silent rotation or server packet interception.
- Click assist: while **left mouse is physically held**, repeatedly invokes the vanilla attack controller only for a ray-traced visible player at **3 blocks or less**, up to 12 CPS. No extended hit reach and no aura around the player.
- The master switch defaults **OFF** and neither auto aim nor auto clicks are active without explicit configuration.
- Both modules and the numeric settings are saved in existing Alien JSON profiles; invalid values and missing profile sections normalize safely.
- The **Full** profile aims even when left mouse is not held; its attack assist still requires pressing left mouse.
- Preset selection updates all the provided values; moving a slider/switch marks the profile **CUSTOM**.

## What is *not* implemented

**Fake anti-aim, desync, silent aim, packet spoofing, kill aura, anti-cheat bypass and bypass guarantees are NOT implemented.** A client-side local head/camera animation would not automatically change server-side hit detection and would be misleading if labelled anti-aim. No UI control claims these unbuilt features work.

## Build, installation, migration

Use the toolchain in `scripts/bootstrap.sh` (verified Java 8, Gradle 2.14.1 and ForgeGradle 2.1), then:

```bash
bash ./scripts/bootstrap.sh
./gradlew --no-daemon -Pedition=unified setupDecompWorkspace build
```

The reobfuscated file is `build/unified/libs/AlienClient-Unified-1.8.9.jar`. Install **only this Alien Client JAR** in Minecraft's Forge 1.8.9 `mods` folder. Never install Unified alongside the old Legit/Full JAR: they all expose the same Forge mod ID `alienclient` and would conflict.

Unified reuses `config/alien-client/` (the old Legit configuration) so existing Legit profiles and HUD are still available. The old Full configuration is in `config/alien-client-full/`; it **is not migrated automatically**. Back up both folders and transfer Full profile JSONs separately after checking names, avoiding overwrites.

The legacy `-Pedition=legit` and `-Pedition=full` builds remain available for regression/archival compatibility; **Unified is the new default build and distribution target**. No user game files have been modified automatically, and no public GitHub Release has been published as part of these merges.

## Validation

`unified-client-ci.yml` compiles the one JAR using Java 8, runs all JUnit cases including new CombatConfigTest/CombatMathTest, and verifies its manifest, class provenance, metadata and edition marker. The [Unified workflow](https://github.com/AttackAlien/Alien-Client/actions/runs/39809488208) completed successfully on the combined revision. The test-only Forge mod recorded **24 PASS checks** inside Minecraft 1.8.9, covering the actual Combat Studio, preset changes, persisted JSON, local dummy target selection, gradual camera movement and screenshots. The integrated [Sky workflow](https://github.com/AttackAlien/Alien-Client/actions/runs/39809488165) and [PanelStudio workflow](https://github.com/AttackAlien/Alien-Client/actions/runs/39809488157) also passed both legacy editions.

**Still not validated:** physical mouse click timing in a real desktop session, multiplayer PvP, server anti-cheat compatibility, Windows graphics drivers, OptiFine, and real GPU performance. Passing CI is **not** a guarantee of permitted behavior on a PvP server.

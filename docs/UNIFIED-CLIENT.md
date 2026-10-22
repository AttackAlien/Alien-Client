# Alien Client Unified — Combat Studio (experimental)

This branch replaces the *distribution concept* of two almost-identical editions with **one installable Forge 1.8.9 JAR**, `AlienClient-Unified-1.8.9.jar`. It does not merge PR #3 or PR #4 and it does not modify `main`.

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

The legacy `-Pedition=legit` and `-Pedition=full` builds remain temporarily available for regression/archival compatibility; **Unified is the new default build and distribution target**. No user game files or published releases are changed by this PR.

## Validation

`unified-client-ci.yml` compiles the one JAR using Java 8, runs all JUnit cases including new CombatConfigTest/CombatMathTest, and verifies its manifest, class provenance, metadata and edition marker. The CI result must be checked before distribution. We still need a **real in-game smoke test of target selection, camera movement, GUI interaction, attack-click timing**, alongside Windows/OptiFine compatibility review.

A green unit/build CI means the code compiles, **not** that multiplayer PvP behavior has been validated.

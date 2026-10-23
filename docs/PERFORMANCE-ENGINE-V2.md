# Alien Client — Performance Engine 2.0 (experimental)

Base: PR #9 (Unified + redesign 0.4). This feature is isolated in a child branch and must not be merged before PR #9.

## Implemented and measurable

- **Game-only telemetry**: last 240 frames in a bounded circular buffer with mean, P95, P99 and spike count. No measurements while paused/in GUI/unloaded. Pauses longer than 500 ms are excluded. Spike threshold is saved (default 50 ms).
- **Graphical dashboard**: chart is drawn with at most 150 columns to reduce menu draw calls; on small Minecraft GUI Scale 2 windows, use the compact **Graph / Stats** toggle to switch between chart and metrics; A/B details stay hidden in compact mode rather than overlapping. Open **Right Shift → Settings → Performance → Open live Performance Dashboard**. The chart is a snapshot of the last 240 *gameplay* frames, not a fabricated live rendering benchmark while a menu is open.
- **Adaptive Quality (OPT-IN, OFF by default)**: based on P95 against an adjustable 30–240 FPS target, the runtime scale smoothly drops the **Alien-only** particle count, cosmetic rendering count and effect distance to no less than 40% of the user budgets. Recovery is slower; there is a 5-second cooldown. Saved maximums remain untouched. The 3D cape uses its existing low-subdivision mesh when far away or heavily degraded. Original position/appearance values are not edited.
- **Manual BASE/EFFECTS benchmark**: collect two 15-second, in-world sessions. Press BASE, play without changing the scene if possible, then reopen dashboard. Change only the Alien features you intend to compare, press EFFECTS and repeat in the *same* scene. Statistics (frame count, mean, P95, P99) are in memory; they are NOT a repeatable scientific benchmark unless conditions are controlled. The benchmark does not toggle mods or graphics automatically. It is cancelled on world change. Sample collection excludes menus and pauses.
- **Presets preserved**: Competitive, Balanced and Cinematic still set user budgets as before. Diagnostics, custom budget sliders and saved profiles remain compatible with old JSON.

## Known limits and future work

- Does **not** optimize vanilla Minecraft render distance, VSync, chunk building, shaders, game memory allocation, packet handling or other mods.
- Does **not** disable cosmetic effects, clouds, skyboxes, shaders or blur behind the user's back; the adaptation is limited to the named work budgets. Current skybox texture and blur settings are unchanged.
- Percentiles and mean represent a recent window, not whole-game FPS. When a GUI is open no new gameplay frame is added. High-percentile jitter is an imperfect proxy for work done by Alien.
- The BASE and EFFECTS labels do not guarantee that Alien was disabled or enabled; the user controls the scene and visual settings. No specific FPS uplift is claimed.
- Only cape cloth LOD is added to this increment. Further mesh/particle/blur optimization needs profiling and separate controlled benchmarks.
- Tested in CI on real Minecraft/Forge 1.8.9 with Linux Xorg/Mesa and Java 8 once the PR's workflows complete; Windows, OptiFine, GPU drivers and player testing require separate verification.

## Basic test recipe

1. Start a local world with Forge Java 8.
2. Open Performance Dashboard; verify numbers and graph fill after gameplay, and p95/p99 nonzero.
3. Turn on adaptive mode and play with effects enabled. Verify runtime caps never exceed the configured budgets, 40% floor, no profile mutation.
4. Start BASE and EFFECTS samples separately. Stay in the game for 15 seconds each; reopen dashboard to compare.
5. Try a small window and reduced GUI scale; ensure graph and buttons remain inside the screen.
6. Reopen the game with an older JSON profile: adaptive mode remains OFF and target defaults to 90 FPS.

Automation checks: `PerformanceEngineV2Test` verifies ring/statistics, hysteresis, bounds, benchmark storage and migration. The real Minecraft smoke suite opens the dashboard, captures a screenshot, exercises adaptive enable/disable and verifies persisted budgets survive.

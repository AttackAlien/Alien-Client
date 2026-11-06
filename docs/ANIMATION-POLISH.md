# Animation polish / 0.14 candidate

This branch builds on the Ghost effect (PR #30), Now Playing (PR #29) and imported mascots (PR #28). It does not merge the stack into `main` or publish a release.

## What changes

- The original ghost has a rounded cloth surface with smooth normals, volumetric curved arms, pearl lighting, cloth follow-through and a feathered dissolve. A GLSL 1.20 material has a CPU-lit fallback. Geometry, buffers and scratch data are reused.
- The optional constellation finale starts during dissolution. Body motes follow Bézier curves into ten stars, the connecting strokes grow, then the whole pattern fades. `duration` still controls the body; `finaleDuration` adds 0.7–2 seconds only with the finale enabled. Both preferences persist in Unified profiles. Ghost Studio uses the actual world renderer.
- Emotes keep the original procedural choreography and Minecraft rig. Softened bounces, eased gestures, counterbalance and the Robot's short eased holds replace hard transitions. Switching and stopping retain outgoing joint velocity. Angles blend along their short arc. This does not introduce imported Fortnite assets or a new humanoid rig.
- Cape movement uses exact critically damped inertia, slower traveling folds and a denser curved surface in Balanced/High. Shoulders, texture mapping and the Low/Competitive fallback remain. There is no arbitrary phase reset at 20,000 ticks.
- Reactive auras use damped response channels and velocity-aware render interpolation. Their slower geometry keeps its loop continuous at the bounded clock wrap.
- Mascot following uses exact position/yaw springs and cubic interpolation, with teleport snapping at both endpoints. Existing Quaternius clips and Sahur model/credits stay intact. Sahur still has whole-model movement; its asset has no skeletal clips.
- Trails preserve imported textures and width/color/opacity curves. Sampling responds to length/curvature, shares the hard geometry budget across paths and covers each whole path even under a reduced budget. Camera-facing normals stay oriented at sharp turns.
- Particles use velocity-aware paths, eased birth/death, size settling and gentler flutter. Existing presets, shapes and budgets remain separate from the Ghost effect.
- UI switches, hover and press feedback retain velocity when reversed. A bounded weak screen cache preserves control motion across `initGui()` rebuilds. Sliders keep input immediate and animate their feedback. Toast rows settle when a prior notification expires. Global animation and reduced-motion preferences are respected; input rectangles do not move.

## Validation

JUnit checks cover frame partition independence, reversal, saved switch state, cloth phase continuity, particle endpoint velocities, continuous dance loops and stop/switch velocity. Pure mesh/finale checks cover seams, outward unit normals, finite paths and real star endpoints.

`runGhostSmokeClient` attacks an actual labeled low-health zombie in a disposable integrated-server world. Its screenshots verify the ghost and constellation; a pixel check prevents a counted-but-invisible finale from passing. It also checks filtering, disable/cleanup, profile reload and the CPU fallback preview.

`runMotionSmokeClient` opens the production Ghost Studio, reverses its actual switch, tests reduced motion, scrolls using native mouse-wheel input and edits the finale through Minecraft's input handlers. A sentinel GL program and nonzero array/index buffers verify state restoration in shader, CPU fallback and animation-off paths.

The full existing Forge suites remain required. Test and video fixtures are excluded from shipping JARs. Current execution receipts and original framebuffer images will be published after validation; an encoded 30 FPS rate alone is not a rendering benchmark.

Commands:

```bash
./gradlew --no-daemon -Pedition=unified test build
ALIEN_EDITION=unified bash scripts/ci-ghost-smoke-xorg.sh
ALIEN_EDITION=unified bash scripts/ci-motion-smoke-xorg.sh
ALIEN_EDITION=unified bash scripts/ci-ultimate-smoke-xorg.sh
bash scripts/record-showcase.sh
```

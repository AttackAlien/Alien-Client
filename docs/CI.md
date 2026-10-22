# GitHub Actions — Forge 1.8.9 Legit/Full

CI workflow: [`.github/workflows/forge-ci.yml`](../.github/workflows/forge-ci.yml).

## Scope
- Trigger: pull requests targeting `main` (including new commits to PR #3); manual `workflow_dispatch` once the workflow exists on the default branch. Avoid duplicated PR/push jobs on the same revision.
- Matrix: **Legit** and **Full**, independent Linux runners (Ubuntu 22.04).
- Toolchain: Java 8 with `actions/setup-java`, then the repository's pinned, SHA-256-verified Temurin 8u472 and Gradle 2.14.1; ForgeGradle 2.1, Minecraft/Forge 1.8.9 from official endpoints.
- Pipeline: JUnit via `build`, code compilation, ForgeGradle reobfuscation, built-JAR provenance verification, SHA256, and **real game smoke run** via Xorg with a connected dummy display and Mesa software OpenGL.
- Reports/artifacts (14 days): the edition JAR, `SHA256SUMS.txt`, JUnit XML/HTML, smoke `result.txt` and all screenshots/logs produced by `scripts/smoke-client.sh`. Any failed job also attempts to upload available diagnostics.

## Why separate jobs?
The source sets and config roots of Legit and Full must stay independent. Both run the same shared JUnit tests plus an actual edition-aware smoke. Neither build tests advanced combat functionality: the Full edition remains an architecture preview.

## How to view
Open the GitHub **Actions** tab, select **Alien Client - Forge 1.8.9 CI**, and open the newest run. Jobs are labelled `Forge 1.8.9 | legit` and `Forge 1.8.9 | full`. Download the run's two archives to see JARs, test reports and screenshots. The PR displays check results after GitHub schedules the workflow.

If GitHub Actions is disabled, first enable it in repository **Settings → Actions → General**. On newly created workflows, GitHub may require permission to run for first-time contributors or changes to workflow files. Avoid merging while checks remain pending.

## Limits and troubleshooting
- Green means the code builds and the smoke scenarios pass **in Linux with software OpenGL**. It does not prove Windows, OptiFine, other mods, dedicated servers, real NVIDIA/AMD performance or actual FPS improvement.
- Legacy ForgeGradle may encounter upstream 404/429/TLS/dependency availability failures; inspect the job's first failing step/log before changing repository dependencies or disabling verification.
- New commits on the PR head branch trigger the `pull_request` workflow regardless of draft status, **if Actions are enabled and GitHub's policy accepts it**.
- The workflow does not use GitHub release publishing, repository write permissions, account tokens or remote game servers.
- Do not confuse old versioned screenshots under `captures/` with evidence from this run: only `build/<edition>/smoke/` in the current artifact is current.

## Legacy LWJGL virtual monitor compatibility

The first CI run demonstrated JUnit and artifact compilation but Minecraft startup failed in LWJGL 2.9.4 with `ArrayIndexOutOfBoundsException: 0` in `LinuxDisplay.getAvailableDisplayModes()` under Xvfb. LWJGL's XRandR parser expects a connected output, and Xvfb may advertise RandR without one. Use `scripts/ci-xorg-dummy.conf` and `scripts/ci-smoke-xorg.sh` instead. The Xorg dummy 1280x720 display was checked locally with `xdpyinfo` and exposed XRandR, XF86VidMode and GLX; its integration on the GitHub runner must still be proven by a green smoke run. Logs include `xorg.log`, `xdpyinfo.txt`, `xrandr.txt` and `glxinfo.txt` for failure analysis.

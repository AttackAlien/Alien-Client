# Branch protection and combat profile safeguards

## Main protection (GitHub administrator action required)

This repository's GitHub integration cannot edit branch rulesets. Enable protection in GitHub > Settings > Rules > Rulesets > New branch ruleset, target `main`, and select Active. Require a pull request, passing CI checks, block force pushes and deletions, and require review if available. Choose the actual job names GitHub lists (Unified JAR | Java 8 and applicable Forge/Sky checks). Confirm protection on the branch after saving. The Unified workflow now also runs on push to main, but this alone DOES NOT enforce protection.

## Runtime behavior

Combat OFF, LEGIT, FULL and CUSTOM presets remain saved. They do not grant authorization to run on multiplayer connections. When joining multiplayer, Combat Studio offers an explicit "Allow on server" button. The permission is only held in memory and is invalidated by reconnecting (including to the same server), changing worlds, switching combat profiles, or manually revoking. The selected combat options are not erased. In singleplayer, enabled combat works as before. Binding a preset to a server does not grant consent.

Combat assistance can violate multiplayer server rules even when consented to. No anticheat compatibility is implied.

## Tests

The new CombatSessionGuardTest verifies initial blocking, explicit opt-in, manual revocation, disconnect/reconnect, profile swapping, persisted ConfigStore per-server settings, and singleplayer behavior. Unified CI still needs to pass before merging.

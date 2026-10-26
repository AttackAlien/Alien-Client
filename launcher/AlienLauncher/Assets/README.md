`scene.png` is the original real Forge framebuffer capture from
`captures/atual/unified/93-alpine-menu-night.png` (Unified 0.6.0-alpha.2).
The launcher crops the world region in its renderer and adds the UI's dark
gradient. It is a backdrop, not evidence of a new game session. New launcher
screenshots are captured from the running Windows application itself.
`alien-mark.png` is the new transparent emblem, created with image generation
at the user's request. The original generated file is preserved in
`/workspace/generated_images/exec-867754b1-ec67-4a15-aabb-498c47b2bf3e.png`.
The launcher loads it once, fits its visible alpha bounds without distortion
and uses the same asset for the sidebar and main panel. Generated brand art
is not evidence of application testing: the gallery contains real Windows
desktop captures. Interface icons remain vector paths in `StudioControls.cs`.

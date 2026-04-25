# AGENTS Guide

## Project Snapshot
- This repo is a Godot 4.4 project that embeds Kotlin/JVM + Java scripts via the Godot Kotlin/JVM Gradle plugin (`build.gradle.kts`).
- Runtime scene entry is `main.tscn`; project boot config is `project.godot`.
- `Controller Icons` GDScript plugin is enabled as editor plugin and autoload singleton (`project.godot`, `[autoload]` + `[editor_plugins]`).
- A C# port of the same addon exists in `addons/controller_icons_csharp`, but is not enabled by default.

## Architecture You Need First
- **App layer**: sample labels in `main.tscn` bind both generated `.gdj` wrappers and direct source scripts:
  - `res://scripts/godot/PrintText.gdj` and `res://src/main/kotlin/godot/PrintText.kt`
  - `res://scripts/godot/PrintTextJava.gdj` and `res://src/main/java/godot/PrintTextJava.java`
- **JVM registration flow**: Kotlin/Java classes annotated with `@RegisterClass` and `@RegisterFunction` are converted to `.gdj` descriptors (configured by `godot.registrationFileBaseDir` in `build.gradle.kts`).
- **Input-icon domain flow** (`addons/controller_icons`):
  1) `ControllerIcons.gd` tracks last input type + device and emits `input_type_changed`.
  2) `Mapper.gd` maps generic `joypad/*` paths to controller-specific asset paths.
  3) `objects/ControllerIconTexture.gd` listens to the singleton and resolves textures dynamically.

## Critical Workflows (Verified)
- Build JVM artifacts and refresh generated registration files:
  - `./gradlew.bat build`
- Useful Godot-Kotlin tasks (from `./gradlew.bat tasks --all`):
  - `copyJars`, `shadowJar`, `packageBootstrapJar`, `generateGdIgnoreFiles`
- There are currently no test sources (`src/test` absent; Gradle reports `test NO-SOURCE`).

## Conventions and Repo-Specific Rules
- Do not hand-edit generated `.gdj` files in `scripts/godot/*`; they explicitly state they are generated.
- Keep engine/plugin compatibility aligned: plugin version in `build.gradle.kts` is `0.13.1-4.4.1`; README notes engine version must match.
- Prefer changing source scripts in `src/main/kotlin` or `src/main/java`, then rebuild to regenerate metadata.
- `ControllerIcons` is expected as singleton name; addon code references it directly (e.g., `ControllerIconTexture.gd`, `Mapper.gd`).
- Addon configuration lives in `addons/controller_icons/settings.tres` (fallback device, deadzone, custom asset dir/extension).

## Integration Points and Gotchas
- Godot plugin wiring for GDScript addon is in `addons/controller_icons/plugin.gd` (`add_autoload_singleton` + inspector plugin).
- If input actions are changed at runtime, addon docs require calling `ControllerIcons.refresh()` (`addons/controller_icons/DOCS.md`).
- Debugger settings for JVM are in `godot_kotlin_configuration.json` (port `5005`, `wait_for_debugger=true`, `use_debug=false`).
- `.gitignore` excludes heavy/generated dirs (`build/`, `.gradle/`, `.godot/`) and addon asset dirs; avoid assuming assets are versioned here.

## Safe Change Strategy for Agents
- For gameplay/script changes: edit `src/main/kotlin/**` or `src/main/java/**`, run Gradle build, then verify scene bindings in `main.tscn` still resolve.
- For icon-system changes: keep `ControllerIcons.gd` (state + parsing), `Mapper.gd` (device mapping), and `ControllerIconTexture.gd` (render proxy) behavior consistent.
- If switching to C# addon, also update plugin enablement/autoload and scene script references; current project wiring targets GDScript addon paths.

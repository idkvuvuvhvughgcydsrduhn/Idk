# Structure Client v0.2.0-Beta

Fabric client for Minecraft **1.21.11**, Java 21. Purple / black / silver UI, built for touch (Android via Zalith), mouse and keyboard.

## Build

    ./gradlew build        # or: gradle build (Gradle 9.3)

Output: `build/libs/structure-client-0.2.0.jar`. CI (`.github/workflows/build.yml`) builds it on every push.

Versions live in `gradle.properties`. Verify them against https://fabricmc.net/develop before building.

## Install

1. Put the JAR in `mods/` with Fabric API for 1.21.11.
2. Zalith: select the Fabric 1.21.11 profile and add the JAR the same way as any other mod.

## Controls

| Key | Action |
|---|---|
| Right Shift | Open ClickGUI |
| Delete | Panic (disable everything, lock client) |
| Esc | Close a screen |

Each module has its own keybind (Bind row in its settings panel).

## Modules

- **Combat**: KillAura, MobAura, AutoClicker, Criticals, Target HUD, Auto Weapon, Combat Notifications
- **Movement**: Fly, Speed, High Jump, Sprint, No Fall, Movement HUD
- **Visual**: ESP, Fullbright, Tracers, Nametags, Item ESP, Storage ESP, Entity Highlight
- **Player**: Auto Tool, Armor HUD, Inventory HUD, Coordinates, Timer, Potion HUD
- **World**: World Information, Block Information, Entity Information, Environment Display
- **Utility**: ClickGUI, HUD Editor, Panic, Notifications, Custom Main Menu, Client Info
- **HUD**: Watermark, Keystrokes, Client Status, Module List

## Config

Files are stored in `<game>/config/structure-client/`. Auto Save writes the active config every few seconds after a change. Load, Save, Del and the name field are in the bottom bar of the ClickGUI.

## Notes

- Modules that scan the world (ESP, Storage ESP) run on the CPU and draw as 2D overlays, so they do not depend on the render pipeline.
- Timer uses one small mixin with `require = 0`. If it stops matching a future Minecraft build, only Timer stops working and the game still starts.
- Every module is isolated: if one throws, it is disabled and a notification shows, and the rest keep running.

## Responsibility

This client is for personal use in single player and on servers that allow it. Using combat automation (KillAura, AutoClicker, Criticals, Fly, Speed, etc.) on a server is against most server rules and can get the account banned. Use it only where you have permission.

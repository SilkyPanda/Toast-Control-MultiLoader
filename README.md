# Toast Control

Blocks and customises the toast popups Minecraft shows for advancements, recipes, tutorials and system messages. Client side only.

Works on Minecraft 26.2.x with Fabric (needs Fabric API), NeoForge and Forge.

Based on [Toast Control](https://modrinth.com/mod/toast-control) by Shadows-of-Fire.

## Config

Fabric uses `config/toastcontrol.json` and picks up changes while the game is running. NeoForge and Forge use `config/toastcontrol-client.toml`, which can also be edited from the in-game config screen.

Options, with defaults:

- `advancements`, `recipes`, `system` (false) and `tutorial` (true): block that kind of toast
- `global_vanilla`, `global_modded`, `global` (false): block every vanilla toast, every modded toast, or all of them
- `blocked_classes` (empty): full class names of toasts to block
- `force_time` (0): minimum time a toast stays up in ticks, 0 keeps the vanilla time
- `translucent` (true) and `transparent` (false): toast background, transparent wins
- `toast_count` (3): how many toasts show at once
- `x_offset`, `y_offset` (0): offset in pixels
- `no_slide`, `start_left`, `top_down` (false): animation and position
- `print_classes` (false): log the class of every toast that is shown

J clears the toasts on screen.

## Building

```
./gradlew build
```

The jars end up in `fabric/build/libs`, `neoforge/build/libs` and `forge/build/libs`. `./gradlew :fabric:runClient`, `:neoforge:runClient` and `:forge:runClient` start a test client.

Code in `common` is compiled into all three.

## Licence

MIT, see `LICENSE`.

# Toast Control

Blocks and customises the toast popups Minecraft shows for advancements, recipes, tutorials and system messages. Client side only.

Supports Fabric (needs Fabric API) and NeoForge on Minecraft 26.1.x.

Based on [Toast Control](https://modrinth.com/mod/toast-control) by Shadows-of-Fire.

## Config

Fabric: `config/toastcontrol.json`. Changes are picked up while the game is running.
NeoForge: `config/toastcontrol-client.toml`, or the in-game config screen.

| Option | Default | |
|---|---|---|
| `advancements`, `recipes`, `system` | `false` | Block that type of toast |
| `tutorial` | `true` | Block tutorial toasts |
| `global_vanilla`, `global_modded`, `global` | `false` | Block all vanilla, all modded, or all toasts |
| `blocked_classes` | empty | Toast classes to block, by full class name |
| `force_time` | `0` | Minimum time a toast stays up, in ticks (0 is vanilla) |
| `translucent`, `transparent` | `true`, `false` | Toast background (transparent wins) |
| `toast_count` | `3` | Toasts on screen at once |
| `x_offset`, `y_offset` | `0` | Offset in pixels |
| `no_slide`, `start_left`, `top_down` | `false` | Layout and animation |
| `print_classes` | `false` | Log the class of every toast that is shown |

Press J to clear the toasts on screen.

## Building

```
./gradlew build
```

The jars end up in `fabric/build/libs` and `neoforge/build/libs`. `./gradlew :fabric:runClient` and `./gradlew :neoforge:runClient` start a test client.

Shared code is in `common`, which is compiled into both loaders.

## Licence

MIT, see `LICENSE`.

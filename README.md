# Toast Control (Fabric)

Blocks and customises the popups ("toasts") Minecraft shows for advancements, recipes, tutorials and system
messages. Client-side only.

This is a Fabric port of [Toast Control](https://modrinth.com/mod/CnOG2wlS) by Shadows_of_Fire (MIT licence),
originally written for NeoForge.

## Requirements

* Minecraft 26.1.x, Java 25
* Fabric Loader and Fabric API

## Config

Edit `config/toastcontrol.json` (created on first launch). Changes are picked up within about a second while the
game is running.

| Section | Option | Default | Meaning |
|---|---|---|---|
| `blocked_toasts` | `advancements`, `recipes`, `tutorial` | `true` | Block that toast type |
| | `system` | `false` | Block system toasts |
| | `global_vanilla` / `global_modded` / `global` | `false` | Block all vanilla / all non-vanilla / all toasts |
| | `blocked_classes` | `[]` | Fully qualified toast class names to block |
| `visual_options` | `force_time` | `0` | Minimum time on screen in ticks (0 = vanilla) |
| | `translucent` / `transparent` | `true` / `false` | Toast background style (transparent wins) |
| | `toast_count` | `3` | Toasts shown at once (1-7) |
| | `x_offset`, `y_offset` | `0` | Pixel offset |
| | `no_slide` | `false` | Pop in without sliding |
| | `start_left` | `false` | Show on the left side |
| | `top_down` | `false` | Stack toasts coming in from the top |
| `debug` | `print_classes` | `false` | Log the class of every toast shown |

Press **J** (rebindable under Controls) to clear all active toasts.

## Development

```
./gradlew validateAccessWidener   # checks toastcontrol.classtweaker against the game
./gradlew runClient               # start a dev client
./gradlew build                   # jar ends up in build/libs
```

## Licence

MIT, see `LICENSE`. Original work copyright (c) 2019 Brennan Ward.

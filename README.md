# Styled Chat (Forge)

> **Unofficial port.** This is a community-made port of
> [Styled Chat](https://github.com/Patbox/StyledChat) by **Patbox (modmuss50)** to
> Minecraft **1.20.1 / Forge**. It is not made, endorsed, or supported by the original
> author. Please report bugs here, not on the original tracker.

Change the look of your server's chat: per-style colours, gradients, hover text, emoji,
mentions, links, markdown and per-player formatting.

- **Minecraft** 1.20.1
- **Loader** Forge 47.x
- **Licence** LGPL-3.0-only (same as the original)

| | |
| --- | --- |
| CurseForge | https://www.curseforge.com/minecraft/mc-mods/1717597 |
| Modrinth | https://modrinth.com/mod/B9gSmloD |
| Source | https://github.com/Delesk1JX/StyledChatForge |
| Original mod | https://www.curseforge.com/minecraft/mc-mods/styled-chat |
| Original source | https://github.com/Patbox/StyledChat |

## Why a port exists

The original is Fabric-only. Forge servers get the same configuration format, the same
commands and the same placeholder/predicate syntax, so you can move an existing
`styled-chat.json` across without editing it.

The three libraries the mod builds on — Text Placeholder API, Predicate API and Player
Data API — have no Forge release, so their sources are bundled into this jar under their
original package names and LGPL-3.0 licences.

## Configuration

Config lives at `config/styled-chat.json` and is generated on first launch. Per-player
style options are stored at `config/styledchat/options/<uuid>.json`.

Commands are under `/styledchat`: `about`, `reload`, `get`, `set`, `clear`, `tellform`.

Permissions are checked by operator level, matching what the original falls back to when
no permission provider is installed. Nodes used: `styledchat.main`, `styledchat.reload`,
`styledchat.set`, `styledchat.get`, `styledchat.clear`, `styledchat.tellform`,
`styledchat.format.<style>`, `styledchat.unsafe_format.<style>`,
`styledchat.format.spoiler`.

## Building

Requires JDK 17 and about 2 GB of free memory.

```sh
./gradlew build
```

The release jar is written to `build/libs/`, alongside a `-sources.jar` and a
`-javadoc.jar`. The sources jar is what makes the LGPL obligations satisfiable, so please
publish it with every release.

To publish to the distribution sites, supply the tokens through the environment so they
never reach the repository:

```sh
export CURSEFORGE_API_TOKEN=...   # https://www.curseforge.com/account/api-tokens
export MODRINTH_API_TOKEN=...     # https://modrinth.com/settings/pats
export CHANGELOG="..."
./gradlew publishMods
```

## How this port was made

The original release was decompiled and remapped from Fabric intermediary to Mojang
official mappings before being adapted, so the code here reads the way the real Minecraft
API does. The scaffolding used for that lives in `tools/`:

| Tool | Purpose |
| --- | --- |
| `ChainedMappings.java` | chains Fabric intermediary to Mojang official mappings through obf |
| `RemapSource.java` | rewrites decompiled source from intermediary to official names |
| `RefmapTargets.java` | turns the Fabric refmap into official-mapping mixin targets |
| `check-mixins.ps1` | verifies every mixin target against the Minecraft jar |
| `Rcon.java` | minimal RCON client used to smoke-test commands |

Forge's Mixin (0.8.5) cannot inject into interfaces, which the original relied on for
`OutgoingChatMessage`. That part was restructured onto the concrete implementations, and
command registration moved to Forge's `RegisterCommandsEvent`. Full details of every
behavioural difference are in [CREDITS.md](CREDITS.md).

## Credits and licensing

- **Styled Chat** — © Patbox, LGPL-3.0-only — [original project](https://github.com/Patbox/StyledChat)
- Text Placeholder API, Predicate API, Player Data API — © Patbox, LGPL-3.0-only
- **This Forge port** — © Delesk1JX

See [CREDITS.md](CREDITS.md) and [LICENSE](LICENSE).

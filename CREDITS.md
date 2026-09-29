# Credits and licensing

## Original work

**Styled Chat** is the work of **Patbox** (modmuss50), licensed **LGPL-3.0-only**.

- Project: https://github.com/Patbox/StyledChat
- Original release ported here: `styled-chat 2.2.4+1.20.1` (Fabric)
- Original CurseForge project: https://www.curseforge.com/minecraft/mc-mods/styled-chat (493348)
- Original Modrinth project: https://modrinth.com/mod/styled-chat

This repository is an **unofficial, community-made Forge port** of that mod, maintained
separately and not endorsed by the original author. Please report problems here rather
than on the original tracker.

## This port

- Source: https://github.com/Delesk1JX/StyledChatForge
- CurseForge: https://www.curseforge.com/minecraft/mc-mods/1717597
- Modrinth: https://modrinth.com/mod/B9gSmloD

## What this port changes

The port targets Minecraft 1.20.1 on Forge. The configuration format, command names, permission
nodes, placeholder and predicate syntax are unchanged, so existing `styled-chat.json` files and
third-party configuration keep working.

Loader-facing pieces were replaced rather than ported one-to-one:

| Original (Fabric) | This port (Forge) |
| --- | --- |
| `FabricLoader` metadata, config dir, `findPath` | `eu.pb4.loader.Platform` over `ModList` / `FMLPaths` |
| `me.lucko` permissions API | `eu.pb4.loader.Permissions` over Forge's permission handler API, falling back to op level |
| Lucko per-player options | `eu.pb4.loader.Options`, a per-player JSON store under `config/styledchat/options` |
| Fabric `Event` / `EventFactory` | `eu.pb4.loader.SimpleEvent`, same `register()` / `invoker()` shape |
| Mixin into `Commands#<init>` | `RegisterCommandsEvent` |
| Mixin into `OutgoingChatMessage#of` | Mixins on `OutgoingChatMessage.Player` / `.Disguised`, because Mixin 0.8.5 cannot inject into interfaces |
| Reflection-free Melius Vanish | Reflective lookup, still optional |

## Bundled APIs

There is no Forge release of the libraries the mod builds on, so their sources are built into
this jar:

- **Predicate API** 0.1.2+1.20 — © Patbox — LGPL-3.0-only, under its original package
  `eu.pb4.predicate`
- **Player Data API** 0.2.2+1.19.3 — © Patbox — LGPL-3.0-only, under its original package
  `eu.pb4.playerdata`
- **Text Placeholder API** 2.1.4+1.20.1 — © Patbox — LGPL-3.0-only

The Placeholder API is the odd one out: a separate unofficial Forge port of it exists
(`placeholderapi`, https://github.com/Delesk1JX/TextPlaceholderAPI-Forge). If Styled Chat
shipped it under its original package, installing both mods would put the same classes in two
jars, and JPMS refuses to let two modules export one package — the game dies before it starts
with a `ResolutionException`. It is therefore vendored under
`dev.delesk1jx.styledchat.vendor.placeholders`, which no other mod can collide with.

The consequence is that the two mods install side by side: this one uses its own private copy,
and a standalone Text Placeholder API mod is unaffected. Configuration is unaffected either way,
because placeholder ids are data rather than package names.

Licence texts for all three are copied into the jar as `META-INF/LICENSE_placeholder-api`,
`META-INF/LICENSE_predicate-api` and `META-INF/LICENSE_player-data-api`.

## Licence

This port is distributed under **LGPL-3.0-only**, the same licence as the original. The full text is
in `LICENSE`.

The corresponding source for this build is the contents of `src/main`, and is published as a
`-sources.jar` alongside every release.

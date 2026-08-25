# 3639211320 — EFZ Core/Maps/Quests

- Исходный тир: **P1**
- Практический итог после чтения: **P1**
- Workshop: [3639211320](https://steamcommunity.com/sharedfiles/filedetails/?id=3639211320)
- Mod ID: `EFZ_Core`, `42EFZ_Maps`, `EFZ_Quests`
- Версия в `mod.info`: не указан
- Build: B42.13+
- Multiplayer/Dedicated Server: MP/DS code exists; depends on SSR Quest and Bandits2 integrations
- Снимок исходников: ветка репозитория на 2026-08-25; runtime-тест не выполнялся.
- Инвентарь: 55 Lua; client 22, server 8, shared 22, прочие 3.

## Вердикт

Фокус: authored story quests, deployment/extraction objectives, Bandits ambushes, map markers and death-drop sync.

Study as an SSR authoring case and Bandits encounter integration example.

## Архитектура

| Слой | Что находится в слое |
|---|---|
| CLIENT | quest presentation, deploy actions, map marker, books/boosts and compatibility patches. |
| SERVER | Bandits deploy, spawn targets, death drop, loot and world deployment handlers. |
| SHARED | intro script, extraction/zombie-clear definitions, teleport/trade config and timed actions. |

## Network

- SSR QSystem integration
- EFZ-specific server/client deploy and death-drop messages
- object/player ModData

Вывод по authority: серверный слой присутствует, но доверие проверяется по каждому handler отдельно.

## Persistence и identity

delegates quest persistence to SSR Quest; uses entity ModData for scenario objects/state.

## Полезные механики

- real content built on SSR DSL
- Bandits2 encounter deployment
- extraction/zombie-clear objectives
- map marker
- compatibility patch examples

## Риски и anti-patterns

- content pack rather than general framework
- large dependency chain
- authority inherits SSR assumptions
- many compatibility patches are version-sensitive

## Файлы для повторного чтения

- `изучить/P1/3639211320/mods/EFZ Quests/42/media/lua/shared/EFZ_IntroScript.lua`
- `изучить/P1/3639211320/mods/EFZ Quests/42/media/lua/shared/EFZ_DeployExtraction.lua`
- `изучить/P1/3639211320/mods/EFZ Quests/42/media/lua/shared/EFZ_DeployZombieClear.lua`
- `изучить/P1/3639211320/mods/EFZ Quests/42/media/lua/client/EFZ_DeployBandits2Ambush.lua`
- `изучить/P1/3639211320/mods/EFZ Quests/42/media/lua/server/EFZ_BanditsDeployPatch.lua`
- `изучить/P1/3639211320/mods/EFZ Quests/42/media/lua/client/EFZ_MapMarker.lua`
- `изучить/P1/3639211320/mods/EFZ Quests/42/media/lua/server/EFZ_QuestDeathDrop.lua`

## Что проверить в runtime

- objective completion on DS
- ambush ownership with multiple players
- death/reconnect
- SSR dependency versions

## Ограничения отчёта

Это статический разбор локального Workshop-снимка. Поддержка DS означает наличие соответствующего кода, а не подтверждённую совместимость с нашей сборкой 42.20.x. Если в пакете не найдено явной лицензии, исходники считаются материалом для изучения, а не разрешением на копирование.

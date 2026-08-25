# 3667459290 — Project Fallout: Muggy

- Исходный тир: **P1**
- Практический итог после чтения: **P1**
- Workshop: [3667459290](https://steamcommunity.com/sharedfiles/filedetails/?id=3667459290)
- Mod ID: `MUGGYMOD`
- Версия в `mod.info`: не указан
- Build: B42
- Multiplayer/Dedicated Server: MP/DS handlers exist; built on Chat with Me
- Снимок исходников: ветка репозитория на 2026-08-25; runtime-тест не выполнялся.
- Инвентарь: 23 Lua; client 5, server 4, shared 14, прочие 0.

## Вердикт

Фокус: named NPC dialogue/idle definitions, positional voice lines and zone-triggered responses.

Reference content/audio queues and zone dialogue; replace trigger validation and base networking.

## Архитектура

| Слой | Что находится в слое |
|---|---|
| CLIENT | client initializes NPC, passive sounds and reports zone entry/exit. |
| SERVER | server receives playerZoneEntry/playerZoneExit and runs zone response/spawn logic. |
| SHARED | conditions, dialogue definitions, idle definitions and sound catalogue are shared. |

## Network

- MUGGY_ZoneSystem/playerZoneEntry/playerZoneExit
- Chat with Me dialogue/behavior APIs

Вывод по authority: серверный слой присутствует, но доверие проверяется по каждому handler отдельно.

## Persistence и identity

zone/runtime state is held by its response system; no independent robust quest save layer was found.

## Полезные механики

- concrete voiced NPC addon
- subtitle/dialogue data
- passive positional sound
- area-triggered responses
- separation of definitions from runtime

## Риски и anti-patterns

- server trusts client zone-entry report without recalculating coordinates
- inherits Chat with Me reward/network risks
- single-character content is tightly coupled

## Файлы для повторного чтения

- `изучить/P1/3667459290/mods/MuggyMod/42/media/lua/server/NPCSystem/MUGGY_ServerCommandHandler.lua`
- `изучить/P1/3667459290/mods/MuggyMod/42/media/lua/server/NPCSystem/MUGGY_ZoneResponseSystem.lua`
- `изучить/P1/3667459290/mods/MuggyMod/42/media/lua/client/NPCSystem/MUGGY_ZoneResponseClient.lua`
- `изучить/P1/3667459290/mods/MuggyMod/42/media/lua/shared/NPCSystem/Dialogue/NPC_DialogueDefinitions.lua`
- `изучить/P1/3667459290/mods/MuggyMod/42/media/lua/shared/NPCSystem/Sound/MUGGY_SoundDefinitions.lua`
- `изучить/P1/3667459290/mods/MuggyMod/42/media/lua/client/NPCSystem/MUGGY_PassiveSoundInit.lua`

## Что проверить в runtime

- forge remote zone entry
- two nearby listeners
- subtitle/audio ordering
- late join

## Ограничения отчёта

Это статический разбор локального Workshop-снимка. Поддержка DS означает наличие соответствующего кода, а не подтверждённую совместимость с нашей сборкой 42.20.x. Если в пакете не найдено явной лицензии, исходники считаются материалом для изучения, а не разрешением на копирование.

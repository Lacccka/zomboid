# 3785397275 — Extraction Mode

- Исходный тир: **P0**
- Практический итог после чтения: **P0**
- Workshop: [3785397275](https://steamcommunity.com/sharedfiles/filedetails/?id=3785397275)
- Mod ID: `ExtractionMode`
- Версия в `mod.info`: 0.8.74
- Build: B42
- Multiplayer/Dedicated Server: MP/DS: designed around authoritative raid sessions
- Снимок исходников: ветка репозитория на 2026-08-25; runtime-тест не выполнялся.
- Инвентарь: 62 Lua; client 21, server 4, shared 35, прочие 2.

## Вердикт

Фокус: server-owned group raid lifecycle, ready/opt-out, late join, extraction, quests, barter and hideout.

Primary model for a durable GroupQuestSession lifecycle and reconnect/late-join policy.

## Архитектура

| Слой | Что находится в слое |
|---|---|
| CLIENT | Client.lua submits intents; HUD, QuestPanel, TownPicker, MapMarkers and upgrade/garage panels render snapshots. |
| SERVER | Authority and specialized Authority modules own phase changes, rosters, deadlines, rewards/upgrades, route/threat/extraction and recovery. |
| SHARED | Config and domain tables are shared; Authority.lua is shared but contains explicit server branches. |

## Network

- RequestState/SetReady/SetOptOut/JoinFactionRaid/BoardExtraction
- CompleteQuest/CompleteBarter/InstallUpgrade
- SelectTown/FireFlare and generator/garage operations
- sendClientCommand + targeted server replies

Вывод по authority: серверный слой присутствует, но доверие проверяется по каждому handler отдельно.

## Persistence и identity

Config.DATA_KEY in ModData; player ModData backups support raid resume; state includes participants, lateJoinPending, vehicles, extraction/death-rescue pending records and schema version.

## Полезные механики

- strongest session state machine
- ready/opt-out roster
- deadlines and staged phases
- late join/reconnect recovery
- group extraction
- server-authoritative progression

## Риски и anti-patterns

- alpha-scale complexity and large shared Authority.lua
- game-mode assumptions differ from NPC quest sessions
- many recovery branches need state-machine tests
- do not couple our framework to its whole mode

## Файлы для повторного чтения

- `изучить/P0/3785397275/mods/ExtractionMode/42/media/lua/shared/ExtractionMode/Authority.lua`
- `изучить/P0/3785397275/mods/ExtractionMode/42/media/lua/client/ExtractionMode/Client.lua`
- `изучить/P0/3785397275/mods/ExtractionMode/42/media/lua/shared/ExtractionMode/RaidQuestAuthority.lua`
- `изучить/P0/3785397275/mods/ExtractionMode/42/media/lua/shared/ExtractionMode/RaidRouteAuthority.lua`
- `изучить/P0/3785397275/mods/ExtractionMode/42/media/lua/shared/ExtractionMode/RaidFlareAuthority.lua`
- `изучить/P0/3785397275/mods/ExtractionMode/42/media/lua/shared/ExtractionMode/RaidLossAuthority.lua`
- `изучить/P0/3785397275/mods/ExtractionMode/42/media/lua/shared/ExtractionMode/GarageAuthority.lua`
- `изучить/P0/3785397275/mods/ExtractionMode/42/media/lua/client/ExtractionMode/QuestPanel.lua`
- `изучить/P0/3785397275/mods/ExtractionMode/42/media/lua/client/ExtractionMode/MapMarkers.lua`

## Что проверить в runtime

- restart in every phase
- ready toggle race
- late join while transitioning
- double extraction
- disconnect during reward/barter

## Ограничения отчёта

Это статический разбор локального Workshop-снимка. Поддержка DS означает наличие соответствующего кода, а не подтверждённую совместимость с нашей сборкой 42.20.x. Если в пакете не найдено явной лицензии, исходники считаются материалом для изучения, а не разрешением на копирование.

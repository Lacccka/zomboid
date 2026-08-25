# 3751199292 — True Companions

- Исходный тир: **P2**
- Практический итог после чтения: **P1 for Bandits lifecycle (promote)**
- Workshop: [3751199292](https://steamcommunity.com/sharedfiles/filedetails/?id=3751199292)
- Mod ID: `TrueCompanions`
- Версия в `mod.info`: не указан
- Build: B42
- Multiplayer/Dedicated Server: MP/DS partial; explicit comments identify client-local persistence limitations
- Снимок исходников: ветка репозитория на 2026-08-25; runtime-тест не выполнялся.
- Инвентарь: 55 Lua; client 33, server 2, shared 20, прочие 0.

## Вердикт

Фокус: Bandits2 companions, affinity/recruitment, roster, dialogue, schedules, beacons, stations, zones and quests.

Promote as the key Bandits2 integration/reference and a detailed list of persistence traps; redesign all roster/quest truth on server.

## Архитектура

| Слой | Что находится в слое |
|---|---|
| CLIENT | most interaction, roster, talk, inventory, guardian and persistence logic is client-side. |
| SERVER | server owns spawn/wild-spawn and validates admin SetSite/SetSpawnConfig paths; marker broadcasts. |
| SHARED | companion profiles, affinity, talk, quests, schedules, programs and beacon registry are shared. |

## Network

- BanditsNPC Spawn/ForceArrival/SetOpt/SetSpawnConfig/SetSite/RemoveSite
- Bandits Commands/SetMarker
- ModData transmit for overrides/beacon registry

Вывод по authority: серверный слой присутствует, но доверие проверяется по каждому handler отдельно.

## Persistence и identity

TrueCompanionsRoster is explicitly client-local; companion brain also lives in Bandits clusters; beacon registry uses Global ModData and server commands for site changes.

## Полезные механики

- deepest Bandits2 addon
- named companion identity/profile
- affinity/recruitment
- dialogue/quests/schedules
- beacon/roster UI
- comments document serialization and MP hazards

## Риски и anti-patterns

- client-local roster is not DS source of truth
- dual storage can diverge
- client-origin Global ModData is questioned by its own comments
- most gameplay authority is client-side

## Файлы для повторного чтения

- `изучить/P2/3751199292/mods/TrueCompanions/42/media/lua/client/BanditsNPCPersistence.lua`
- `изучить/P2/3751199292/mods/TrueCompanions/42/media/lua/client/BanditsNPCInteract.lua`
- `изучить/P2/3751199292/mods/TrueCompanions/42/media/lua/shared/BanditsNPCTalk.lua`
- `изучить/P2/3751199292/mods/TrueCompanions/42/media/lua/shared/BanditsNPCQuests.lua`
- `изучить/P2/3751199292/mods/TrueCompanions/42/media/lua/shared/BanditsNPCAffinity.lua`
- `изучить/P2/3751199292/mods/TrueCompanions/42/media/lua/shared/BanditsNPCRecruit.lua`
- `изучить/P2/3751199292/mods/TrueCompanions/42/media/lua/shared/BanditsNPCBeaconRegistry.lua`
- `изучить/P2/3751199292/mods/TrueCompanions/42/media/lua/server/BanditsNPCSpawnServer.lua`
- `изучить/P2/3751199292/mods/TrueCompanions/42/media/lua/client/ISUI/BanditsNPCRosterPanel.lua`

## Что проверить в runtime

- two-client ownership conflict
- chunk unload/restart
- beacon registry DS authority
- companion death/respawn
- late join roster

## Ограничения отчёта

Это статический разбор локального Workshop-снимка. Поддержка DS означает наличие соответствующего кода, а не подтверждённую совместимость с нашей сборкой 42.20.x. Если в пакете не найдено явной лицензии, исходники считаются материалом для изучения, а не разрешением на копирование.

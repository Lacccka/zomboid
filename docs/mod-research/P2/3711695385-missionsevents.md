# 3711695385 — MissionsEvents

- Исходный тир: **P2**
- Практический итог после чтения: **P2**
- Workshop: [3711695385](https://steamcommunity.com/sharedfiles/filedetails/?id=3711695385)
- Mod ID: `MissionsEvents`
- Версия в `mod.info`: не указан
- Build: B42
- Multiplayer/Dedicated Server: MP/DS command split exists
- Снимок исходников: ветка репозитория на 2026-08-25; runtime-тест не выполнялся.
- Инвентарь: 9 Lua; client 5, server 2, shared 2, прочие 0.

## Вердикт

Фокус: two small scripted missions with server start/confirm/sync and UI/timed actions.

Use as a compact teaching example, not as framework foundation.

## Архитектура

| Слой | Что находится в слое |
|---|---|
| CLIENT | starts missions, shows UI/halo messages, confirms step and applies RemoveItem response. |
| SERVER | tracks mission state and sends SyncM2/halo/removal messages. |
| SHARED | mission constants/content split into Shared_Mission1/2. |

## Network

- StartM1/HaloStartM1
- StartM2/ConfirmM2/SyncM2/RemoveItem/HaloMessage

Вывод по authority: серверный слой присутствует, но доверие проверяется по каждому handler отдельно.

## Persistence и identity

limited mission state in Lua/player data; no general schema/migration framework found.

## Полезные механики

- small readable client/server mission example
- explicit start-confirm flow
- shared mission definition files

## Риски и anti-patterns

- only two hard-coded missions
- client RemoveItem response is not ideal server authority
- no group session or robust restart recovery

## Файлы для повторного чтения

- `изучить/P2/3711695385/mods/MissionsEvents/42/media/lua/server/Server_Mission1.lua`
- `изучить/P2/3711695385/mods/MissionsEvents/42/media/lua/server/Server_Mission2.lua`
- `изучить/P2/3711695385/mods/MissionsEvents/42/media/lua/client/Client_Mission1.lua`
- `изучить/P2/3711695385/mods/MissionsEvents/42/media/lua/client/Client_Mission2.lua`
- `изучить/P2/3711695385/mods/MissionsEvents/42/media/lua/shared/Shared_Mission1.lua`
- `изучить/P2/3711695385/mods/MissionsEvents/42/media/lua/shared/Shared_Mission2.lua`
- `изучить/P2/3711695385/mods/MissionsEvents/42/media/lua/client/MissionsEvents_UI.lua`

## Что проверить в runtime

- forged ConfirmM2
- restart mid-mission
- duplicate start

## Ограничения отчёта

Это статический разбор локального Workshop-снимка. Поддержка DS означает наличие соответствующего кода, а не подтверждённую совместимость с нашей сборкой 42.20.x. Если в пакете не найдено явной лицензии, исходники считаются материалом для изучения, а не разрешением на копирование.

# 3676995511 — Share Map Notes

- Исходный тир: **P0**
- Практический итог после чтения: **P0**
- Workshop: [3676995511](https://steamcommunity.com/sharedfiles/filedetails/?id=3676995511)
- Mod ID: `PZShareMapNotes`
- Версия в `mod.info`: 1.1
- Build: B42
- Multiplayer/Dedicated Server: MP/DS: yes; explicit SP fallback
- Снимок исходников: ветка репозитория на 2026-08-25; runtime-тест не выполнялся.
- Инвентарь: 5 Lua; client 3, server 1, shared 1, прочие 0.

## Вердикт

Фокус: shared map strokes/markers with validation, visibility groups and batching.

Best compact reference for quest markers and server-to-group marker snapshots.

## Архитектура

| Слой | Что находится в слое |
|---|---|
| CLIENT | captures map strokes, serializes points, requests snapshot and applies add/remove batches. |
| SERVER | validates payload types/count/size, rate-limits commands, assigns IDs/authors, enforces ownership/admin and filters recipients. |
| SHARED | constants and compact stroke schema are shared. |

## Network

- ShareStroke/RemoveStroke/RequestStrokeSync
- sendClientCommand/sendServerCommand
- OnClientCommand/OnServerCommand
- direct local delivery for SP

Вывод по authority: серверный слой присутствует, но доверие проверяется по каждому handler отдельно.

## Persistence и identity

stores strokes in getGameTime():getModData; points are flattened to strings to avoid deep-table serialization loss; saves/hourly persistence and batched full sync.

## Полезные механики

- small auditable secure protocol
- server-assigned IDs
- Everyone/Faction/Safehouse visibility
- payload/rate limits
- snapshot batching
- included client tests

## Риски и anti-patterns

- storage is game-time ModData, not the GlobalModData API despite comments
- stroke schema differs from vanilla symbol objects
- recipient membership must be re-evaluated when groups change

## Файлы для повторного чтения

- `изучить/P0/3676995511/mods/PZShareMapNotes/42/media/lua/server/PZShareMapNotes_Server.lua`
- `изучить/P0/3676995511/mods/PZShareMapNotes/42/media/lua/client/PZShareMapNotes_Client.lua`
- `изучить/P0/3676995511/mods/PZShareMapNotes/42/media/lua/shared/PZShareMapNotes_Shared.lua`
- `изучить/P0/3676995511/mods/PZShareMapNotes/42/media/lua/client/tests/PZShareMapNotes_Tests.lua`

## Что проверить в runtime

- large/stale/malformed payloads
- faction membership change
- late join full sync
- server restart
- SP listen-host fallback

## Ограничения отчёта

Это статический разбор локального Workshop-снимка. Поддержка DS означает наличие соответствующего кода, а не подтверждённую совместимость с нашей сборкой 42.20.x. Если в пакете не найдено явной лицензии, исходники считаются материалом для изучения, а не разрешением на копирование.

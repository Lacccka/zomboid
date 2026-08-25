# 2877685881 — Shared Faction Map

- Исходный тир: **P2**
- Практический итог после чтения: **P2**
- Workshop: [2877685881](https://steamcommunity.com/sharedfiles/filedetails/?id=2877685881)
- Mod ID: `FactionMap`
- Версия в `mod.info`: не указан
- Build: B41-era
- Multiplayer/Dedicated Server: MP uses Global ModData replication
- Снимок исходников: ветка репозитория на 2026-08-25; runtime-тест не выполнялся.
- Инвентарь: 4 Lua; client 2, server 1, shared 1, прочие 0.

## Вердикт

Фокус: faction-scoped vanilla world-map annotations.

Use only as an anti-pattern comparison against Share Map Notes' validated protocol.

## Архитектура

| Слой | Что находится в слое |
|---|---|
| CLIENT | switches local map symbol table by faction ID and transmits the full table. |
| SERVER | accepts OnReceiveGlobalModData packet, stores it under the supplied module and retransmits. |
| SHARED | shared patch helpers integrate with map symbols. |

## Network

- ModData.add/getOrCreate/transmit
- Events.OnReceiveGlobalModData

Вывод по authority: серверный слой присутствует, но доверие проверяется по каждому handler отдельно.

## Persistence и identity

one Global ModData table per faction ID plus FactionMap_VanillaBackup.

## Полезные механики

- simple faction-specific map view
- vanilla map symbol persistence

## Риски и anti-patterns

- critical trust issue: server accepts client-supplied module/table without ownership, size or membership validation
- full-table last-writer-wins conflicts
- no rate limiting

## Файлы для повторного чтения

- `изучить/P2/2877685881/mods/FactionAnnotations/media/lua/client/~MxFactionMap.lua`
- `изучить/P2/2877685881/mods/FactionAnnotations/media/lua/server/~MxFactionMapServer.lua`
- `изучить/P2/2877685881/mods/FactionAnnotations/media/lua/client/~FMWorldMapSymbols.lua`
- `изучить/P2/2877685881/mods/FactionAnnotations/media/lua/shared/FactionAnnotations.shared.lua`

## Что проверить в runtime

- cross-faction overwrite exploit
- concurrent edits
- oversized packet

## Ограничения отчёта

Это статический разбор локального Workshop-снимка. Поддержка DS означает наличие соответствующего кода, а не подтверждённую совместимость с нашей сборкой 42.20.x. Если в пакете не найдено явной лицензии, исходники считаются материалом для изучения, а не разрешением на копирование.

# 3761337621 — QP Survivor Tasks

- Исходный тир: **P1**
- Практический итог после чтения: **P1**
- Workshop: [3761337621](https://steamcommunity.com/sharedfiles/filedetails/?id=3761337621)
- Mod ID: `QPSurvivorTasks`
- Версия в `mod.info`: 0.7.0
- Build: B42
- Multiplayer/Dedicated Server: MP/DS: server-authoritative board workflow
- Снимок исходников: ветка репозитория на 2026-08-25; runtime-тест не выполнялся.
- Инвентарь: 4 Lua; client 2, server 2, shared 0, прочие 0.

## Вердикт

Фокус: physical multiplayer task board, creation handshake, sync and delivery ledgers.

Study board placement identity, request/result flow and pending ledgers; do not copy code.

## Архитектура

| Слой | Что находится в слое |
|---|---|
| CLIENT | world context/UI finds boards, creates requests, tracks sync and handles server result/application messages. |
| SERVER | large QPST_Server resolves objects/permissions, emits create-board result, manages compatibility ledgers and pending delivery. |
| SHARED | minimal separate shared layer; many constants live in the two main files. |

## Network

- client command handler + targeted server commands
- CreateBoardResult and board sync
- pending delivery on OnCreatePlayer

Вывод по authority: серверный слой присутствует, но доверие проверяется по каждому handler отдельно.

## Persistence и identity

world-object ModData plus global ledgers via ModData.getOrCreate/transmit for idempotency/recovery.

## Полезные механики

- server-created physical quest board
- pending operation ledgers
- reconnect delivery
- world context and synchronization

## Риски и anti-patterns

- two multi-thousand-line files
- constants/protocol are not centralized
- compatibility ledgers add complexity
- license is All Rights Reserved in package—study only

## Файлы для повторного чтения

- `изучить/P1/3761337621/mods/QPSurvivorTasks/42/media/lua/server/QPSurvivorTasks/QPST_Server.lua`
- `изучить/P1/3761337621/mods/QPSurvivorTasks/42/media/lua/client/QPSurvivorTasks/QPST_Context.lua`
- `изучить/P1/3761337621/mods/QPSurvivorTasks/README.txt`
- `изучить/P1/3761337621/mods/QPSurvivorTasks/LICENSE.txt`

## Что проверить в runtime

- duplicate create
- board destroyed/replaced
- disconnect before result
- ledger migration
- permission spoof

## Ограничения отчёта

Это статический разбор локального Workshop-снимка. Поддержка DS означает наличие соответствующего кода, а не подтверждённую совместимость с нашей сборкой 42.20.x. Если в пакете не найдено явной лицензии, исходники считаются материалом для изучения, а не разрешением на копирование.

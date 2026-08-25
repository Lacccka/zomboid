# 3761060249 — QP Survivor Contracts

- Исходный тир: **P0**
- Практический итог после чтения: **P0**
- Workshop: [3761060249](https://steamcommunity.com/sharedfiles/filedetails/?id=3761060249)
- Mod ID: `QPSurvivorContracts`
- Версия в `mod.info`: 1.3.2
- Build: B42 with compatibility copy
- Multiplayer/Dedicated Server: MP/DS: yes; server-authoritative design
- Снимок исходников: ветка репозитория на 2026-08-25; runtime-тест не выполнялся.
- Инвентарь: 14 Lua; client 12, server 2, shared 0, прочие 0.

## Вердикт

Фокус: multi-stage individual/group contracts, participants, shared progress, delivery and rewards.

Primary reference for our shared QuestSession, objective contributions, idempotent completion and reward delivery.

## Архитектура

| Слой | Что находится в слое |
|---|---|
| CLIENT | contract admin/player UI, tracked HUD, multi-objective UI, map markers and bounded event reports. |
| SERVER | single authoritative QPSC_Server owns schema v12, transitions, participants, progress, delivery handshake, reward locks and diagnostics. |
| SHARED | objective/config/translation models are client/server-readable; authoritative state is not client-owned. |

## Network

- QPSurvivorContracts module
- RequestContracts/Add|Update|Accept|Cancel|Complete|Delete/Clear
- ReportMultiZombieKill/ReportImmediateLocation
- SubmitDelivery + DeliveryConsumptionAck
- ContractsData/ConsumeDeliveryItems responses

Вывод по authority: серверный слой присутствует, но доверие проверяется по каждому handler отдельно.

## Persistence и identity

QPSC_Data in ModData.getOrCreate; ModData.transmit; schema migration/normalization; startup duplicate/status/reward diagnostics.

## Полезные механики

- best group objective reference
- participant records and per-user contributions
- sharedProgress/sharedCompleted metadata
- one-active-contract invariant
- two-phase delivery token
- reward idempotency counters/locks
- late pending rewards

## Риски и anti-patterns

- very large single server file (~8.5k lines)
- client kill fallback is necessarily heuristic, though bounded by objective/range
- does not implement nearby invitation Accept/Decline
- B41 compatibility paths complicate inventory truth

## Файлы для повторного чтения

- `изучить/P0/3761060249/mods/QPSurvivorContracts/42/media/lua/server/QPSurvivorContracts/QPSC_Server.lua`
- `изучить/P0/3761060249/mods/QPSurvivorContracts/42/media/lua/client/QPSurvivorContracts/QPSC_Client.lua`
- `изучить/P0/3761060249/mods/QPSurvivorContracts/42/media/lua/client/QPSurvivorContracts/QPSC_MapMarkers.lua`
- `изучить/P0/3761060249/mods/QPSurvivorContracts/42/media/lua/client/QPSurvivorContracts/QPSC_MultiObjectiveUI.lua`
- `изучить/P0/3761060249/mods/QPSurvivorContracts/42/media/lua/client/QPSurvivorContracts/QPSC_TrackedUI.lua`

## Что проверить в runtime

- two players complete final step simultaneously
- duplicate ACK/replay
- disconnect during delivery/reward
- restart with pending token
- late join participant snapshot

## Ограничения отчёта

Это статический разбор локального Workshop-снимка. Поддержка DS означает наличие соответствующего кода, а не подтверждённую совместимость с нашей сборкой 42.20.x. Если в пакете не найдено явной лицензии, исходники считаются материалом для изучения, а не разрешением на копирование.

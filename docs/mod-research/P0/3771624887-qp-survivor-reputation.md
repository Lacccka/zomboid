# 3771624887 — QP Survivor Reputation

- Исходный тир: **P0**
- Практический итог после чтения: **P0**
- Workshop: [3771624887](https://steamcommunity.com/sharedfiles/filedetails/?id=3771624887)
- Mod ID: `QPSurvivorReputation`
- Версия в `mod.info`: 0.4.2
- Build: B41/B42
- Multiplayer/Dedicated Server: MP/DS: yes; server-owned profiles
- Снимок исходников: ветка репозитория на 2026-08-25; runtime-тест не выполнялся.
- Инвентарь: 21 Lua; client 6, server 6, shared 9, прочие 0.

## Вердикт

Фокус: multi-path reputation, levels/titles, history, automation and admin editing.

Adapt profile/history/idempotency concepts for faction and NPC reputation, with separate public/private projections.

## Архитектура

| Слой | Что находится в слое |
|---|---|
| CLIENT | profile/history UI requests snapshots; admin UI submits bounded changes and automation settings. |
| SERVER | owns profiles, clamps points, writes history/source IDs, runs automation scans and admin-gates mutations. |
| SHARED | paths community/hunter/explorer/medic/mechanic/builder, thresholds/titles and automation registry are shared. |

## Network

- RequestProfile/RequestAdminProfile
- RequestAutomationSettings/SaveAutomationSettings/ResetAutomationSettings
- AdminAdd/AdminSet/AdminReset
- server profile snapshot commands

Вывод по authority: серверный слой присутствует, но доверие проверяется по каждому handler отдельно.

## Persistence и identity

QPReputation_v1 via ModData.getOrCreate/transmit; profiles and schemaVersion; automation schema v3/registry version; stable source IDs prevent duplicate awards.

## Полезные механики

- clean reputation domain model
- history/audit entries
- level thresholds
- automation registry
- cross-mod source idempotency
- admin gates

## Риски и anti-patterns

- paths are community roles, not NPC-specific affinity
- Global ModData transmits may expose more data than a per-player snapshot if misused
- automation scans need load profiling

## Файлы для повторного чтения

- `изучить/P0/3771624887/mods/qp-survivor-reputation/42/media/lua/server/QPReputation_Server.lua`
- `изучить/P0/3771624887/mods/qp-survivor-reputation/42/media/lua/server/QPReputation_Automation.lua`
- `изучить/P0/3771624887/mods/qp-survivor-reputation/42/media/lua/shared/QPReputation_Shared.lua`
- `изучить/P0/3771624887/mods/qp-survivor-reputation/42/media/lua/shared/QPReputation_Config.lua`
- `изучить/P0/3771624887/mods/qp-survivor-reputation/42/media/lua/shared/QPReputation_AutomationRegistry.lua`
- `изучить/P0/3771624887/mods/qp-survivor-reputation/42/media/lua/client/QPReputation_Client.lua`

## Что проверить в runtime

- duplicate sourceId
- concurrent automation/admin mutation
- schema migration
- offline player profile
- unauthorized admin command

## Ограничения отчёта

Это статический разбор локального Workshop-снимка. Поддержка DS означает наличие соответствующего кода, а не подтверждённую совместимость с нашей сборкой 42.20.x. Если в пакете не найдено явной лицензии, исходники считаются материалом для изучения, а не разрешением на копирование.

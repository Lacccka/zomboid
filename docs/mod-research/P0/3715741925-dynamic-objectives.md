# 3715741925 — Dynamic Objectives

- Исходный тир: **P0**
- Практический итог после чтения: **P0**
- Workshop: [3715741925](https://steamcommunity.com/sharedfiles/filedetails/?id=3715741925)
- Mod ID: `DynamicObjectives`
- Версия в `mod.info`: 0.1.0
- Build: B42.16
- Multiplayer/Dedicated Server: MP/DS: networked server handlers; alpha 0.1.0
- Снимок исходников: ветка репозитория на 2026-08-25; runtime-тест не выполнялся.
- Инвентарь: 66 Lua; client 20, server 2, shared 44, прочие 0.

## Вердикт

Фокус: objective runtime, quest chains, offers, rewards, encounters, hooks, HUD and markers.

Reference for objective decomposition and hooks; do not inherit its coupling or reward trust without tests.

## Архитектура

| Слой | Что находится в слое |
|---|---|
| CLIENT | quest manager/viewer/modals, tracked-objective HUD, scanner, map/world markers and network response handling. |
| SERVER | DO_ServerCommands handles encounter spawn, objective hooks/incidents, escort actions, quest items and reward-related requests. |
| SHARED | QuestRuntime is decomposed into lifecycle/state/offers/chains/rewards/spawn/queries; objective registry/hooks and reward definitions are shared. |

## Network

- DynamicObjectives commands SpawnQuestEncounter/FinalizeObjectiveHookQuest/GrantQuestRewards/RefreshObjectiveHooks/SpawnQuestItem
- server replies EncounterSpawned/ObjectiveHooksRefreshed/HookIncidentAccepted|Failed/HookEscortActionResult

Вывод по authority: серверный слой присутствует, но доверие проверяется по каждому handler отдельно.

## Persistence и identity

quest state is integrated with Dynamic Trading common/runtime data; exact durable ownership must be traced per QuestRuntime module.

## Полезные механики

- modular objective lifecycle
- chain/offers model
- world incidents and area hooks
- reward registry
- tracked HUD and map markers

## Риски и anti-patterns

- alpha and coupled to DynamicTradingCommon
- reward calls originate from shared/client contexts and require adversarial audit
- server-owned group sessions are not a finished abstraction
- large distributed state machine

## Файлы для повторного чтения

- `изучить/P0/3715741925/mods/DynamicObjectives/42.16/media/lua/server/DO/DO_ServerCommands.lua`
- `изучить/P0/3715741925/mods/DynamicObjectives/42.16/media/lua/client/DO/DO_ClientNetwork.lua`
- `изучить/P0/3715741925/mods/DynamicObjectives/42.16/media/lua/shared/DO/Quests/QuestRuntime/QuestRuntime_Lifecycle.lua`
- `изучить/P0/3715741925/mods/DynamicObjectives/42.16/media/lua/shared/DO/Quests/QuestRuntime/QuestRuntime_State.lua`
- `изучить/P0/3715741925/mods/DynamicObjectives/42.16/media/lua/shared/DO/Quests/QuestRuntime/QuestRuntime_Chains.lua`
- `изучить/P0/3715741925/mods/DynamicObjectives/42.16/media/lua/shared/DO/Quests/QuestRuntime/QuestRuntime_ProceduralRewards.lua`
- `изучить/P0/3715741925/mods/DynamicObjectives/42.16/media/lua/shared/DO/Rewards/DO_Rewards.lua`
- `изучить/P0/3715741925/mods/DynamicObjectives/42.16/media/lua/client/DO/Markers/DO_MapMarkerSystem.lua`
- `изучить/P0/3715741925/mods/DynamicObjectives/42.16/media/lua/client/DO/Markers/DO_WorldMarkerSystem.lua`

## Что проверить в runtime

- forged completion/reward
- duplicate incident acceptance
- restart during encounter
- objective race from two clients
- chain migration

## Ограничения отчёта

Это статический разбор локального Workshop-снимка. Поддержка DS означает наличие соответствующего кода, а не подтверждённую совместимость с нашей сборкой 42.20.x. Если в пакете не найдено явной лицензии, исходники считаются материалом для изучения, а не разрешением на копирование.

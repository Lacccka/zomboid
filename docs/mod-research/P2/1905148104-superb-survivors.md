# 1905148104 — Superb Survivors

- Исходный тир: **P2**
- Практический итог после чтения: **P2**
- Workshop: [1905148104](https://steamcommunity.com/sharedfiles/filedetails/?id=1905148104)
- Mod ID: `SuperbSurvivors`
- Версия в `mod.info`: не указан
- Build: B41-era
- Multiplayer/Dedicated Server: SP/client-centric; not a DS architecture
- Снимок исходников: ветка репозитория на 2026-08-25; runtime-тест не выполнялся.
- Инвентарь: 138 Lua; client 106, server 1, shared 1, прочие 30.

## Вердикт

Фокус: classic survivor AI, dialogue, group UI, task manager and quest windows.

Mine UI/task concepts only; Bandits2 remains our runtime.

## Архитектура

| Слой | Что находится в слое |
|---|---|
| CLIENT | almost all AI, dialogue, quests, inventory and group management execute client-side. |
| SERVER | only incidental/vanilla object command usage; no authoritative NPC server runtime. |
| SHARED | minimal shared/server footprint. |

## Network

- no meaningful custom MP protocol

Вывод по authority: серверный слой присутствует, но доверие проверяется по каждому handler отдельно.

## Persistence и identity

local/client state and game objects; not suitable for durable server quest sessions.

## Полезные механики

- historical dialogue/task vocabulary
- large catalogue of NPC tasks
- group/roster windows
- quest-manager UI

## Риски и anti-patterns

- obsolete and client-centric
- known performance/compatibility burden
- not a reference for MP authority or persistence

## Файлы для повторного чтения

- `изучить/P2/1905148104/mods/Superb-Survivors/media/lua/client/1_Dialogue/SuperSurvivorDialogue.lua`
- `изучить/P2/1905148104/mods/Superb-Survivors/media/lua/client/5_UI/SSQuestManagerDialogue.lua`
- `изучить/P2/1905148104/mods/Superb-Survivors/media/lua/client/5_UI/SSQuestManagerWindow.lua`
- `изучить/P2/1905148104/mods/Superb-Survivors/media/lua/client/7_Tasks/TaskManager.lua`
- `изучить/P2/1905148104/mods/Superb-Survivors/media/lua/client/7_Tasks/DialogueTask.lua`
- `изучить/P2/1905148104/mods/Superb-Survivors/media/lua/client/7_Tasks/GiveRewardToPlayerTask.lua`
- `изучить/P2/1905148104/mods/Superb-Survivors/media/lua/client/5_UI/SuperSurvivorMyGroupWindow.lua`

## Что проверить в runtime

- SP archival inspection only

## Ограничения отчёта

Это статический разбор локального Workshop-снимка. Поддержка DS означает наличие соответствующего кода, а не подтверждённую совместимость с нашей сборкой 42.20.x. Если в пакете не найдено явной лицензии, исходники считаются материалом для изучения, а не разрешением на копирование.

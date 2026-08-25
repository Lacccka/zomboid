# 2793385743 — SSR: Quest System

- Исходный тир: **P0**
- Практический итог после чтения: **P0**
- Workshop: [2793385743](https://steamcommunity.com/sharedfiles/filedetails/?id=2793385743)
- Mod ID: `ssr-quests`, `ssr-quests-e1`, `ssr-quests-e2`, `ssr-quests-e3`
- Версия в `mod.info`: 2025_12_15
- Build: B41/B42 package layout
- Multiplayer/Dedicated Server: MP/DS: supported, with SSR Core and Java-side pieces
- Снимок исходников: ветка репозитория на 2026-08-25; runtime-тест не выполнялся.
- Инвентарь: 126 Lua; client 66, server 10, shared 50, прочие 0.

## Вердикт

Фокус: quest DSL, dialogue, journal, actions/tasks, NPC stand-ins, audio and trading plugin.

Use as the richest reference for content schema, journal, dialogue presentation and authoring workflow; redesign authority and rewards for DS.

## Архитектура

| Слой | Что находится в слое |
|---|---|
| CLIENT | DialoguePanel, QuestLog/QuestPanel, reward/task/action packs, map areas and AudioManager drive presentation and local quest execution. |
| SERVER | QSystem handles init/save and privileged actions; QMapProtector and extension servers support world-side behavior. |
| SHARED | Quest/Task/Action models plus QuestManager, CharacterManager, SaveManager and script parser form the content runtime. |

## Network

- module QSystem
- commands init/saveData/verify/requestItem/removeItem/addXP/createHorde/removeZombies/teleport/spawnVehicle
- callback IDs via sendServerCommand

Вывод по authority: серверный слой присутствует, но доверие проверяется по каждому handler отдельно.

## Persistence и identity

SaveManager serializes per-SteamID quest data through QSystem; compatibility/verification metadata is exchanged at init.

## Полезные механики

- large, proven quest content model
- branching dialogue panel
- quest journal
- NPC extensions using static furniture/mannequins
- merchant plugin
- audio queue/manager
- scriptable task and action packs

## Риски и anti-patterns

- much quest execution is client/shared rather than purely server-authoritative
- server privileged handlers need a security review before reuse
- depends on SSR Core and external Java implementation
- do not copy content/code without license permission

## Файлы для повторного чтения

- `изучить/P0/2793385743/mods/ssr_quests/media/lua/shared/Scripting/QuestManager.lua`
- `изучить/P0/2793385743/mods/ssr_quests/media/lua/shared/Scripting/SaveManager.lua`
- `изучить/P0/2793385743/mods/ssr_quests/media/lua/client/UI/DialoguePanel.lua`
- `изучить/P0/2793385743/mods/ssr_quests/media/lua/client/UI/QuestLog.lua`
- `изучить/P0/2793385743/mods/ssr_quests/media/lua/shared/Quests/Quest.lua`
- `изучить/P0/2793385743/mods/ssr_quests/media/lua/shared/Quests/Task.lua`
- `изучить/P0/2793385743/mods/ssr_quests/media/lua/shared/Quests/Action.lua`
- `изучить/P0/2793385743/mods/ssr_quests/media/lua/client/Scripting/AudioManager.lua`
- `изучить/P0/2793385743/mods/ssr_quests-e3/media/lua/client/UI/MerchantPanel.lua`
- `изучить/P0/2793385743/mods/ssr_quests-e3/media/lua/shared/Scripting/MerchantManager.lua`
- `изучить/P0/2793385743/mods/ssr_quests/media/lua/shared/Communications/QSystem.lua`

## Что проверить в runtime

- verify save/reconnect on DS
- attempt client-forged reward and teleport calls
- profile large journal/UI
- map active duplicate tree

## Ограничения отчёта

Это статический разбор локального Workshop-снимка. Поддержка DS означает наличие соответствующего кода, а не подтверждённую совместимость с нашей сборкой 42.20.x. Если в пакете не найдено явной лицензии, исходники считаются материалом для изучения, а не разрешением на копирование.

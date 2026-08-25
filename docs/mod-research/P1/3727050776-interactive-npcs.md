# 3727050776 — Interactive NPCs

- Исходный тир: **P1**
- Практический итог после чтения: **P0 (promote)**
- Workshop: [3727050776](https://steamcommunity.com/sharedfiles/filedetails/?id=3727050776)
- Mod ID: `InteractiveNPCs`
- Версия в `mod.info`: не указан
- Build: B41 label, source uses modern APIs
- Multiplayer/Dedicated Server: MP/DS: server validates interaction; ElyonLib dependency
- Снимок исходников: ветка репозитория на 2026-08-25; runtime-тест не выполнялся.
- Инвентарь: 14 Lua; client 9, server 1, shared 4, прочие 0.

## Вердикт

Фокус: persistent NPC registry, proximity/context interaction, server-validated dialogue request, branching UI and admin editor.

Use as the direct comparison baseline; replace static objectRef with our Bandits adapter and move every consequential response to a server DialogueSession transition.

## Архитектура

| Слой | Что находится в слое |
|---|---|
| CLIENT | maintains public NPC registry, context prompt, sends RequestDialogue, renders typewriter/portrait/outfit UI and traverses responses. |
| SERVER | loads JSON registry, resolves stable ID/objectRef, checks enabled/visibility/access/distance, returns full dialogue; admin commands are access-gated. |
| SHARED | Shared owns ID/objectRef schema, limits, dialogue node/response normalization and public projections. |

## Network

- RequestRegistry/ReceiveRegistry/NpcsUpdated
- RequestDialogue/ReceiveDialogue/InteractionDenied
- admin save/delete/mark/preset commands
- OnClientCommand/OnServerCommand

Вывод по authority: серверный слой присутствует, но доверие проверяется по каждому handler отдельно.

## Persistence и identity

server files InteractiveNPCs/index.json, npcs/<safe-id>/npc.json and avatar-presets.json; objectRef key combines x/y/z/sprite/index; world object is marked with InteractiveNPCs_id.

## Полезные механики

- closest match to our initial interaction chain
- server distance validation
- persistent ID and registry
- branching node/response schema
- admin editor + JSON import/export
- payload limits and public/admin projections

## Риски и anti-patterns

- dialogue traversal and response actions are client-only after initial open
- no server DialogueSession/history/quest-start validation
- static world objects, not Bandits runtime actors
- object index identity can drift
- B42.20 runtime unverified

## Файлы для повторного чтения

- `изучить/P1/3727050776/mods/InteractiveNPCs/media/lua/server/InteractiveNPCs/Server.lua`
- `изучить/P1/3727050776/mods/InteractiveNPCs/media/lua/client/InteractiveNPCs/Client.lua`
- `изучить/P1/3727050776/mods/InteractiveNPCs/media/lua/shared/InteractiveNPCs/Shared.lua`
- `изучить/P1/3727050776/mods/InteractiveNPCs/media/lua/shared/InteractiveNPCs/Persistence.lua`
- `изучить/P1/3727050776/mods/InteractiveNPCs/media/lua/shared/InteractiveNPCs/WorldObjectUtils.lua`
- `изучить/P1/3727050776/mods/InteractiveNPCs/media/lua/client/InteractiveNPCs/ContextMenu.lua`
- `изучить/P1/3727050776/mods/InteractiveNPCs/media/lua/client/InteractiveNPCs/UI/DialogueUI.lua`
- `изучить/P1/3727050776/mods/InteractiveNPCs/media/lua/client/InteractiveNPCs/UI/AdminUI.lua`

## Что проверить в runtime

- B42.20 DS load
- remote dialogue denial
- object index drift
- forged response/quest start
- two clients same NPC
- restart JSON recovery

## Ограничения отчёта

Это статический разбор локального Workshop-снимка. Поддержка DS означает наличие соответствующего кода, а не подтверждённую совместимость с нашей сборкой 42.20.x. Если в пакете не найдено явной лицензии, исходники считаются материалом для изучения, а не разрешением на копирование.

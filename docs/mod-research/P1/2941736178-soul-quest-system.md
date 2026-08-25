# 2941736178 — Soul Quest System

- Исходный тир: **P1**
- Практический итог после чтения: **P2 (historical only)**
- Workshop: [2941736178](https://steamcommunity.com/sharedfiles/filedetails/?id=2941736178)
- Mod ID: `SoulQuestSystem`
- Версия в `mod.info`: не указан
- Build: legacy/B41-era
- Multiplayer/Dedicated Server: MP code attempted; DS reliability doubtful
- Снимок исходников: ветка репозитория на 2026-08-25; runtime-тест не выполнялся.
- Инвентарь: 31 Lua; client 23, server 6, shared 2, прочие 0.

## Вердикт

Фокус: mission lists, factions, context objectives and text-file quest saves.

Keep only as a UI/history reference and a catalogue of persistence mistakes to avoid.

## Архитектура

| Слой | Что находится в слое |
|---|---|
| CLIENT | mission/faction UI and world/delivery context actions; client submits entire progress data. |
| SERVER | saveData writes a username-derived text file; sendData attempts to read it and return setProgress. |
| SHARED | only light tweaks/definitions are shared. |

## Network

- SFQuest/saveData/sendData
- SFQuest/setProgress

Вывод по authority: серверный слой присутствует, но доверие проверяется по каждому handler отдельно.

## Persistence и identity

custom /Backup/SFQuest_<username>.txt serialization.

## Полезные механики

- historical mission panel
- faction list UI
- world event/context patterns
- custom text serializer

## Риски и anti-patterns

- server source contains apparent defects: undefined path in getFileReader and malformed concatenation
- client can submit full saved progress
- username filename needs sanitization
- not a secure DS reference

## Файлы для повторного чтения

- `изучить/P1/2941736178/mods/SoulQuestSystem/media/lua/server/SFQuest_ServerCommands.lua`
- `изучить/P1/2941736178/mods/SoulQuestSystem/media/lua/server/SFQuest_ServerFunctions.lua`
- `изучить/P1/2941736178/mods/SoulQuestSystem/media/lua/client/SFQuest_ClientCommands.lua`
- `изучить/P1/2941736178/mods/SoulQuestSystem/media/lua/client/XpSystem/ISUI/SFQuest_MissionPanel.lua`
- `изучить/P1/2941736178/mods/SoulQuestSystem/media/lua/client/XpSystem/ISUI/SFQuest_FactionLists.lua`
- `изучить/P1/2941736178/mods/SoulQuestSystem/media/lua/client/ISUI/SFQuest_QuestMarker.lua`

## Что проверить в runtime

- first confirm files compile
- path traversal username
- forged save payload
- restart/load round-trip

## Ограничения отчёта

Это статический разбор локального Workshop-снимка. Поддержка DS означает наличие соответствующего кода, а не подтверждённую совместимость с нашей сборкой 42.20.x. Если в пакете не найдено явной лицензии, исходники считаются материалом для изучения, а не разрешением на копирование.

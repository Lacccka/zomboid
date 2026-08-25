# 3403180543 — Bandits Week One

- Исходный тир: **P1**
- Практический итог после чтения: **P1**
- Workshop: [3403180543](https://steamcommunity.com/sharedfiles/filedetails/?id=3403180543)
- Mod ID: `BanditsWeekOne`
- Версия в `mod.info`: не указан
- Build: B42.18/B42.20
- Multiplayer/Dedicated Server: MP/DS code exists but scenario is heavily client-driven
- Снимок исходников: ветка репозитория на 2026-08-25; runtime-тест не выполнялся.
- Инвентарь: 217 Lua; client 68, server 6, shared 140, прочие 3.

## Вердикт

Фокус: large scripted story/event scheduler built on Bandits2, chat/radio/audio and world encounters.

Study triggers, narrative scheduling and Bandits scene orchestration, not its authority model.

## Архитектура

| Слой | Что находится в слое |
|---|---|
| CLIENT | scheduler, square loader, event presentation, chat, music, radio and much Bandits population control run client-side. |
| SERVER | BWOClientCommands and Bandits server modules apply world/object/vehicle mutations. |
| SHARED | event definitions, buildings, zombie actions/programs and Global ModData helpers are shared. |

## Network

- Commands/Spawner plus Bandits protocols
- BanditWeekOne ModData transmit
- server effect broadcasts

Вывод по authority: серверный слой присутствует, но доверие проверяется по каждому handler отдельно.

## Persistence и identity

BanditWeekOne Global ModData stores scheduler/world state; repeating place events are restored on game start.

## Полезные механики

- scripted event scheduler
- area/place triggers
- story NPC/population choreography
- chat/radio/music integration
- 42.20 Bandits compatibility

## Риски и anti-patterns

- client-led story progression is weak for shared MP
- very large scenario-specific code
- duplicate 42.18/42.20 trees
- global scenario state may conflict among players

## Файлы для повторного чтения

- `изучить/P1/3403180543/mods/BanditsWeekOne/42.20/media/lua/client/BWOScheduler.lua`
- `изучить/P1/3403180543/mods/BanditsWeekOne/42.20/media/lua/shared/BWOEvents.lua`
- `изучить/P1/3403180543/mods/BanditsWeekOne/42.20/media/lua/shared/BWOEventsPlace.lua`
- `изучить/P1/3403180543/mods/BanditsWeekOne/42.20/media/lua/client/BWOSquareLoader.lua`
- `изучить/P1/3403180543/mods/BanditsWeekOne/42.20/media/lua/server/BWOClientCommands.lua`
- `изучить/P1/3403180543/mods/BanditsWeekOne/42.20/media/lua/client/BWOChat.lua`
- `изучить/P1/3403180543/mods/BanditsWeekOne/42.20/media/lua/client/BWORadio.lua`
- `изучить/P1/3403180543/mods/BanditsWeekOne/42.20/media/lua/client/BWOMusic.lua`
- `изучить/P1/3403180543/mods/BanditsWeekOne/42.20/media/lua/shared/BWOGMD.lua`

## Что проверить в runtime

- two players in different phases
- late join
- restart repeating events
- 42.20 active path

## Ограничения отчёта

Это статический разбор локального Workshop-снимка. Поддержка DS означает наличие соответствующего кода, а не подтверждённую совместимость с нашей сборкой 42.20.x. Если в пакете не найдено явной лицензии, исходники считаются материалом для изучения, а не разрешением на копирование.

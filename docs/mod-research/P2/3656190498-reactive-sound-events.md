# 3656190498 — Reactive Sound Events

- Исходный тир: **P2**
- Практический итог после чтения: **P1 for audio/events (promote)**
- Workshop: [3656190498](https://steamcommunity.com/sharedfiles/filedetails/?id=3656190498)
- Mod ID: `ReactiveSE`
- Версия в `mod.info`: 1.0.0
- Build: B42.13/B42.15
- Multiplayer/Dedicated Server: MP/DS: server schedules and broadcasts events
- Снимок исходников: ветка репозитория на 2026-08-25; runtime-тест не выполнялся.
- Инвентарь: 94 Lua; client 14, server 48, shared 32, прочие 0.

## Вердикт

Фокус: dynamic scenes, positional sound, radio intel, markers and persistent event scheduler.

Promote for voice/positional audio queue and server-triggered narrated encounters.

## Архитектура

| Слой | Что находится в слое |
|---|---|
| CLIENT | receives PlaySoundEvent/SyncModData/RadioIntel/EventStatus, places sound/map markers and plays localized audio. |
| SERVER | event scheduler/generator/scene manager creates camp/combat/gunshot/scream/vehicle/zombie scenes and sends filtered events. |
| SHARED | sound library/player, constants, state initialization and event calculator are shared. |

## Network

- PLAY_SOUND_EVENT/SYNC_MODDATA/RADIO_INTEL/EventStatus
- OnClientCommand/OnServerCommand
- targeted/broadcast sendServerCommand

Вывод по authority: серверный слой присутствует, но доверие проверяется по каждому handler отдельно.

## Persistence и identity

ModData.getOrCreate(Constants.MOD_ID) stores scheduler/world event state; initialized/migrated in ReactiveSE_Initialize.

## Полезные механики

- best positional audio/event reference
- server event scheduler
- scene abstraction
- map/radio markers
- sound library separate from triggers

## Риски и anti-patterns

- snapshot is 42.15, not 42.20
- sound timing is broadcast rather than strict clock synchronization
- duplicate version trees
- large server simulation footprint

## Файлы для повторного чтения

- `изучить/P2/3656190498/mods/ReactiveSE/42.15/media/lua/server/ReactiveSE/ReactiveSE_EventScheduler.lua`
- `изучить/P2/3656190498/mods/ReactiveSE/42.15/media/lua/server/ReactiveSE/ReactiveSE_EventGenerator.lua`
- `изучить/P2/3656190498/mods/ReactiveSE/42.15/media/lua/server/ReactiveSE/ReactiveSE_SceneManager.lua`
- `изучить/P2/3656190498/mods/ReactiveSE/42.15/media/lua/server/ReactiveSE/ReactiveSE_ServerCommands.lua`
- `изучить/P2/3656190498/mods/ReactiveSE/42.15/media/lua/client/ReactiveSE/ReactiveSE_ClientCommands.lua`
- `изучить/P2/3656190498/mods/ReactiveSE/42.15/media/lua/shared/ReactiveSE/ReactiveSE_SoundPlayer.lua`
- `изучить/P2/3656190498/mods/ReactiveSE/42.15/media/lua/shared/ReactiveSE/ReactiveSE_SoundLibrary.lua`
- `изучить/P2/3656190498/mods/ReactiveSE/42.15/media/lua/client/ReactiveSE/ReactiveSE_SoundMarker.lua`

## Что проверить в runtime

- two clients hear same event
- late packet/subtitle drift
- out-of-range filtering
- restart scheduler
- 42.20 audio APIs

## Ограничения отчёта

Это статический разбор локального Workshop-снимка. Поддержка DS означает наличие соответствующего кода, а не подтверждённую совместимость с нашей сборкой 42.20.x. Если в пакете не найдено явной лицензии, исходники считаются материалом для изучения, а не разрешением на копирование.

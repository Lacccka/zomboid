# 3001908830 — PZNS Framework

- Исходный тир: **P1**
- Практический итог после чтения: **P2 for MP; P1 for NPC ideas**
- Workshop: [3001908830](https://steamcommunity.com/sharedfiles/filedetails/?id=3001908830)
- Mod ID: `PZNS_Framework`
- Версия в `mod.info`: не указан
- Build: B41-era
- Multiplayer/Dedicated Server: primarily client/SP; no mod network protocol
- Снимок исходников: ветка репозитория на 2026-08-25; runtime-тест не выполнялся.
- Инвентарь: 79 Lua; client 79, server 0, shared 0, прочие 0.

## Вердикт

Фокус: NPC groups/zones, AI jobs/orders, inventory/context UI and map rendering.

Use conceptual NPC UI/group/zone patterns only, never its authority layout.

## Архитектура

| Слой | Что находится в слое |
|---|---|
| CLIENT | nearly the entire framework, persistence and AI tick run client-side. |
| SERVER | no meaningful authoritative server subsystem. |
| SHARED | no shared Lua layer in this snapshot. |

## Network

- no custom sendClientCommand/OnClientCommand protocol
- client ModData tables PZNS_ActiveNPCs/Groups/Zones

Вывод по authority: авторитетного серверного слоя для этой системы нет.

## Persistence и identity

client creates ModData tables and saves on Events.OnSave; unsuitable as DS source of truth.

## Полезные механики

- NPC group and zone data models
- orders/jobs/task patterns
- context menu and inventory UI
- world-map NPC rendering

## Риски и anti-patterns

- client-only simulation
- not a Dedicated Server architecture
- render-tick AI is expensive
- object identity/persistence is local

## Файлы для повторного чтения

- `изучить/P1/3001908830/mods/PZNS_Framework/media/lua/client/02_mod_utils/PZNS_UtilsDataNPCs.lua`
- `изучить/P1/3001908830/mods/PZNS_Framework/media/lua/client/03_mod_core/PZNS_NPCGroup.lua`
- `изучить/P1/3001908830/mods/PZNS_Framework/media/lua/client/03_mod_core/PZNS_NPCZone.lua`
- `изучить/P1/3001908830/mods/PZNS_Framework/media/lua/client/08_mod_contextmenu/PZNS_ContextMenuInvite.lua`
- `изучить/P1/3001908830/mods/PZNS_Framework/media/lua/client/09_mod_ui/PZNS_NPCInfoPanel.lua`
- `изучить/P1/3001908830/mods/PZNS_Framework/media/lua/client/09_mod_ui/PZNS_WorldMap.lua`
- `изучить/P1/3001908830/mods/PZNS_Framework/media/lua/client/11_events_spawning/PZNS_Events.lua`

## Что проверить в runtime

- SP baseline only
- do not spend DS test time until ported

## Ограничения отчёта

Это статический разбор локального Workshop-снимка. Поддержка DS означает наличие соответствующего кода, а не подтверждённую совместимость с нашей сборкой 42.20.x. Если в пакете не найдено явной лицензии, исходники считаются материалом для изучения, а не разрешением на копирование.

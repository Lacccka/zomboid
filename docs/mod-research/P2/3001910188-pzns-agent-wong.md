# 3001910188 — PZNS Agent Wong

- Исходный тир: **P2**
- Практический итог после чтения: **P2**
- Workshop: [3001910188](https://steamcommunity.com/sharedfiles/filedetails/?id=3001910188)
- Mod ID: `PZNS_AgentWong`
- Версия в `mod.info`: не указан
- Build: B41-era
- Multiplayer/Dedicated Server: inherits client-only PZNS behavior
- Снимок исходников: ветка репозитория на 2026-08-25; runtime-тест не выполнялся.
- Инвентарь: 2 Lua; client 2, server 0, shared 0, прочие 0.

## Вердикт

Фокус: small example NPC addon/template for PZNS.

Useful only to see the smallest external NPC framework extension surface.

## Архитектура

| Слой | Что находится в слое |
|---|---|
| CLIENT | registers/creates a named NPC from PZNS APIs. |
| SERVER | none. |
| SHARED | none. |

## Network

- none

Вывод по authority: авторитетного серверного слоя для этой системы нет.

## Persistence и identity

inherits PZNS local data.

## Полезные механики

- minimal example of an addon-defined named NPC

## Риски и anti-patterns

- not Bandits2
- no dialogue/quest/server protocol
- client-only

## Файлы для повторного чтения

- `изучить/P2/3001910188/mods/PZNS_AgentWong/media/lua/client/pzns_agentwong/PZNS_AgentWong.lua`
- `изучить/P2/3001910188/mods/PZNS_AgentWong/mod.info`

## Что проверить в runtime

- none beyond code reading

## Ограничения отчёта

Это статический разбор локального Workshop-снимка. Поддержка DS означает наличие соответствующего кода, а не подтверждённую совместимость с нашей сборкой 42.20.x. Если в пакете не найдено явной лицензии, исходники считаются материалом для изучения, а не разрешением на копирование.

# 3508537032 — NeatUI Framework

- Исходный тир: **P1**
- Практический итог после чтения: **P1**
- Workshop: [3508537032](https://steamcommunity.com/sharedfiles/filedetails/?id=3508537032)
- Mod ID: `NeatUI_Framework`
- Версия в `mod.info`: 1.0.8
- Build: B42
- Multiplayer/Dedicated Server: client-only UI library
- Снимок исходников: ветка репозитория на 2026-08-25; runtime-тест не выполнялся.
- Инвентарь: 11 Lua; client 11, server 0, shared 0, прочие 0.

## Вердикт

Фокус: scroll views, virtual lists, grid virtualization, 9-patch and text rendering.

Useful for a large journal/discovered-NPC UI if native ISUI list performance becomes a bottleneck.

## Архитектура

| Слой | Что находится в слое |
|---|---|
| CLIENT | all functionality is client ISUI/components. |
| SERVER | none. |
| SHARED | none beyond UI helpers loaded client-side. |

## Network

- none

Вывод по authority: авторитетного серверного слоя для этой системы нет.

## Persistence и identity

none.

## Полезные механики

- virtualized quest/NPC lists
- custom scrollbars
- 9-patch panels
- text truncation and percentage helpers

## Риски и anti-patterns

- not a gameplay/network reference
- API/dependency stability must be checked on 42.20
- license not found in package

## Файлы для повторного чтения

- `изучить/P1/3508537032/mods/NeatUI_Framework/42/media/lua/client/neatui_framework/scrollview/niscrollview.lua`
- `изучить/P1/3508537032/mods/NeatUI_Framework/42/media/lua/client/neatui_framework/scrollview/nivirtualscrollview.lua`
- `изучить/P1/3508537032/mods/NeatUI_Framework/42/media/lua/client/neatui_framework/scrollview/nigridvirtualscrollview.lua`
- `изучить/P1/3508537032/mods/NeatUI_Framework/42/media/lua/client/neatui_framework/scrollview/niscrollbar.lua`
- `изучить/P1/3508537032/mods/NeatUI_Framework/42/media/lua/client/neatui_framework/neattool/neattool_9patch.lua`
- `изучить/P1/3508537032/mods/NeatUI_Framework/42/media/lua/client/neatui_framework/neattool/neattool_textrender.lua`

## Что проверить в runtime

- 10k-row virtual list
- resolution/UI scaling
- controller input

## Ограничения отчёта

Это статический разбор локального Workshop-снимка. Поддержка DS означает наличие соответствующего кода, а не подтверждённую совместимость с нашей сборкой 42.20.x. Если в пакете не найдено явной лицензии, исходники считаются материалом для изучения, а не разрешением на копирование.

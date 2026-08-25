# 3143880496 — PZNS Interaction Buttons

- Исходный тир: **P2**
- Практический итог после чтения: **P2**
- Workshop: [3143880496](https://steamcommunity.com/sharedfiles/filedetails/?id=3143880496)
- Mod ID: `oza_pzns_interaction_buttons`
- Версия в `mod.info`: не указан
- Build: B41-era
- Multiplayer/Dedicated Server: client-only addon
- Снимок исходников: ветка репозитория на 2026-08-25; runtime-тест не выполнялся.
- Инвентарь: 7 Lua; client 7, server 0, shared 0, прочие 0.

## Вердикт

Фокус: floating/quick interaction buttons for PZNS NPC actions.

Potential UX sketch for interaction controls, nothing more.

## Архитектура

| Слой | Что находится в слое |
|---|---|
| CLIENT | creates in-game UI buttons and invokes PZNS operations. |
| SERVER | none. |
| SHARED | none. |

## Network

- none

Вывод по authority: авторитетного серверного слоя для этой системы нет.

## Persistence и identity

none.

## Полезные механики

- compact NPC interaction control placement
- button-state UX

## Риски и anti-patterns

- PZNS/client-only
- no proximity validation or server binding
- not reusable as authority

## Файлы для повторного чтения

- `изучить/P2/3143880496/mods/PZNS_InteractionButtons/media/lua/client/Toolbox_PZNSIBS/7_in_game_ui.lua`

## Что проверить в runtime

- resolution/controller UX only

## Ограничения отчёта

Это статический разбор локального Workshop-снимка. Поддержка DS означает наличие соответствующего кода, а не подтверждённую совместимость с нашей сборкой 42.20.x. Если в пакете не найдено явной лицензии, исходники считаются материалом для изучения, а не разрешением на копирование.

# 3087165610 — tradeHunter

- Исходный тир: **P2**
- Практический итог после чтения: **P2**
- Workshop: [3087165610](https://steamcommunity.com/sharedfiles/filedetails/?id=3087165610)
- Mod ID: `tradeHunter`
- Версия в `mod.info`: не указан
- Build: legacy/B41-era
- Multiplayer/Dedicated Server: misplaced server/UI code; MP safety poor
- Снимок исходников: ветка репозитория на 2026-08-25; runtime-тест не выполнялся.
- Инвентарь: 7 Lua; client 0, server 7, shared 0, прочие 0.

## Вердикт

Фокус: spawned trader/hunter and custom trade UI.

Keep only as a negative comparison; Dynamic Trading is the vendor reference.

## Архитектура

| Слой | Что находится в слое |
|---|---|
| CLIENT | UI Lua is placed under server; client-origin calls request spawn/explosion. |
| SERVER | OnClientCommand accepts spawnHunter/explode paths. |
| SHARED | none. |

## Network

- Trd_module/spawnHunter/explode

Вывод по authority: серверный слой присутствует, но доверие проверяется по каждому handler отдельно.

## Persistence и identity

no robust stock/transaction persistence found.

## Полезные механики

- small historical trade UI

## Риски и anti-patterns

- architecture/layout is incorrect for DS UI
- commands appear weakly validated
- not a vendor transaction reference
- legacy APIs

## Файлы для повторного чтения

- `изучить/P2/3087165610/mods/tradeHunter/media/lua/server/TradeHunter.lua`
- `изучить/P2/3087165610/mods/tradeHunter/media/lua/server/UI/TradeUI.lua`
- `изучить/P2/3087165610/mods/tradeHunter/media/lua/server/UI/ItemsListUI.lua`

## Что проверить в runtime

- do not enable on production DS

## Ограничения отчёта

Это статический разбор локального Workshop-снимка. Поддержка DS означает наличие соответствующего кода, а не подтверждённую совместимость с нашей сборкой 42.20.x. Если в пакете не найдено явной лицензии, исходники считаются материалом для изучения, а не разрешением на копирование.

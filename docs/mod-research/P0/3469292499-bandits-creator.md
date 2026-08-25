# 3469292499 — Bandits Creator

- Исходный тир: **P0**
- Практический итог после чтения: **P0**
- Workshop: [3469292499](https://steamcommunity.com/sharedfiles/filedetails/?id=3469292499)
- Mod ID: `BanditsCreator`
- Версия в `mod.info`: не указан
- Build: B42.20 plus legacy copy
- Multiplayer/Dedicated Server: MP/DS: creator is client UI; sync goes through Bandits2 Custom module
- Снимок исходников: ветка репозитория на 2026-08-25; runtime-тест не выполнялся.
- Инвентарь: 19 Lua; client 19, server 0, shared 0, прочие 0.

## Вердикт

Фокус: authoring custom Bandits2 NPC/clan definitions.

Use it to document and generate valid NPC templates for Alexey and future persistent quest actors.

## Архитектура

| Слой | Что находится в слое |
|---|---|
| CLIENT | option screens edit appearance, inventory, traits and clan data and write/export definitions. |
| SERVER | no independent server tree; relies on Bandits2's Custom commands. |
| SHARED | data contract is the exported Bandits clan/NPC table/file format. |

## Network

- Custom/SendStats
- Custom/SendToClients
- Custom/ReceiveFromClient

Вывод по authority: авторитетного серверного слоя для этой системы нет.

## Persistence и identity

exports under the user's Zomboid/Lua/bandits area; live distribution is delegated to Bandits2.

## Полезные механики

- canonical field names for Bandits2 custom agents
- appearance/inventory editor
- sync workflow for custom definitions

## Риски и anti-patterns

- not a runtime quest system
- client-only authoring means no authority model to borrow
- legacy and 42.20 copies coexist

## Файлы для повторного чтения

- `изучить/P0/3469292499/mods/BanditsCreator/42.20/media/lua/client/OptionScreens/BanditClansMain.lua`
- `изучить/P0/3469292499/mods/BanditsCreator/42.20/media/lua/client/OptionScreens/BanditSync.lua`
- `изучить/P0/3469292499/mods/BanditsCreator/42.20/media/lua/client/OptionScreens/BanditCreationMain.lua`
- `изучить/P0/3469292499/mods/BanditsCreator/42.20/media/lua/client/OptionScreens/BanditItemsListTable.lua`

## Что проверить в runtime

- export then load on headless DS
- verify all clients receive identical custom stats
- confirm filename/ID stability

## Ограничения отчёта

Это статический разбор локального Workshop-снимка. Поддержка DS означает наличие соответствующего кода, а не подтверждённую совместимость с нашей сборкой 42.20.x. Если в пакете не найдено явной лицензии, исходники считаются материалом для изучения, а не разрешением на копирование.

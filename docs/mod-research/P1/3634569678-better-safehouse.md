# 3634569678 — Better Safehouse

- Исходный тир: **P1**
- Практический итог после чтения: **P1**
- Workshop: [3634569678](https://steamcommunity.com/sharedfiles/filedetails/?id=3634569678)
- Mod ID: `BetterSafehouse`
- Версия в `mod.info`: 3.1.0
- Build: B42.15-B42.19
- Multiplayer/Dedicated Server: MP/DS: server-centric access-control mod
- Снимок исходников: ветка репозитория на 2026-08-25; runtime-тест не выполнялся.
- Инвентарь: 85 Lua; client 47, server 19, shared 19, прочие 0.

## Вердикт

Фокус: group ownership, invites/sub-owners, permissions and protected-zone mutations.

Reference permission helpers and membership ownership checks for group quest administration.

## Архитектура

| Слой | Что находится в слое |
|---|---|
| CLIENT | safehouse management UI submits structured requests and shows membership/rights. |
| SERVER | handlers validate access level/ownership and mutate native SafeHouse state; expansion/custom-claim modules add compatibility. |
| SHARED | client/shared helpers mirror safehouse fields and settings. |

## Network

- many sendClientCommand/OnClientCommand handlers
- targeted sendServerCommand results
- object/safehouse synchronization

Вывод по authority: серверный слой присутствует, но доверие проверяется по каждому handler отдельно.

## Persistence и identity

primarily relies on native SafeHouse persistence plus supporting ModData/config.

## Полезные механики

- mature permission checks
- owner/sub-owner roles
- membership invitations
- compatibility across consecutive B42 versions

## Риски и anti-patterns

- six version copies create audit noise
- safehouse semantics are not quest-party semantics
- verify every admin/mutation handler in active 42.19 path

## Файлы для повторного чтения

- `изучить/P1/3634569678/mods/BetterSafehouse/42.15/media/lua/server/BetterSafehouse/BetterSafehouse_Server.lua`
- `изучить/P1/3634569678/mods/BetterSafehouse/42.19/media/lua/server/BetterSafehouse/BetterSafehouse_SubOwner_Server.lua`
- `изучить/P1/3634569678/mods/BetterSafehouse/42.19/media/lua/client/BetterSafehouse/BetterSafehouse_SubOwner_UI.lua`
- `изучить/P1/3634569678/mods/BetterSafehouse/42.15/media/lua/server/BetterSafehouse/BetterSafehouse_CustomClaim_Server.lua`
- `изучить/P1/3634569678/mods/BetterSafehouse/42.19/media/lua/server/BetterSafehouse/BetterSafehouse_Expansion_Server.lua`

## Что проверить в runtime

- non-owner forged commands
- owner offline
- membership race
- 42.20 compatibility

## Ограничения отчёта

Это статический разбор локального Workshop-снимка. Поддержка DS означает наличие соответствующего кода, а не подтверждённую совместимость с нашей сборкой 42.20.x. Если в пакете не найдено явной лицензии, исходники считаются материалом для изучения, а не разрешением на копирование.

# 3744455714 — Pager Network

- Исходный тир: **P1**
- Практический итог после чтения: **P1**
- Workshop: [3744455714](https://steamcommunity.com/sharedfiles/filedetails/?id=3744455714)
- Mod ID: `PagerMod`
- Версия в `mod.info`: 1.0.0
- Build: B42
- Multiplayer/Dedicated Server: MP/DS: server-owned messaging; explicit SP path
- Снимок исходников: ветка репозитория на 2026-08-25; runtime-тест не выполнялся.
- Инвентарь: 20 Lua; client 12, server 4, shared 4, прочие 0.

## Вердикт

Фокус: device identity, direct/channel/broadcast/SOS messages, nearby sharing, unread ACKs and map pings.

Reference the targeted invite notification/ACK and nearby-player discovery layers.

## Архитектура

| Слой | Что находится в слое |
|---|---|
| CLIENT | PagerWindow and context actions submit messages, channels, registration, nearby share and acknowledgements. |
| SERVER | validates held pager numbers/items, recipients/channels/towers, applies limits and persists network state. |
| SHARED | constants/data helpers are shared across B42/common copies. |

## Network

- Register/Assign/Send/Broadcast/Channel/SOS/Global/ShareNearby/MarkRead/Fetch
- targeted server replies
- SP direct OnServerCommand workaround

Вывод по authority: серверный слой присутствует, но доверие проверяется по каждому handler отдельно.

## Persistence и identity

ModData.getOrCreate(PagerMod.MODDATA) stores devices/messages/towers/acks; server initializes on OnInitGlobalModData.

## Полезные механики

- invitation-like targeted notification
- nearby recipient discovery
- ACK/read tracking
- map ping
- offline device inbox
- message limits

## Риски и anti-patterns

- communication membership is device-based, not quest-session membership
- server file is large
- nearby-share authorization and location must be traced carefully

## Файлы для повторного чтения

- `изучить/P1/3744455714/mods/PagerMod/42/media/lua/server/PagerMod_Server.lua`
- `изучить/P1/3744455714/mods/PagerMod/42/media/lua/client/PagerMod_Client.lua`
- `изучить/P1/3744455714/mods/PagerMod/42/media/lua/client/ISUI/PagerWindow.lua`
- `изучить/P1/3744455714/mods/PagerMod/42/media/lua/client/PagerMod_MapPing.lua`
- `изучить/P1/3744455714/mods/PagerMod/42/media/lua/shared/PagerMod_Shared.lua`

## Что проверить в runtime

- spoof pager number/itemId
- nearby distance boundary
- duplicate ACK
- offline recipient
- restart

## Ограничения отчёта

Это статический разбор локального Workshop-снимка. Поддержка DS означает наличие соответствующего кода, а не подтверждённую совместимость с нашей сборкой 42.20.x. Если в пакете не найдено явной лицензии, исходники считаются материалом для изучения, а не разрешением на копирование.

# 3635333613 — Dynamic Trading Common + Dynamic Trading V1/V2

- Исходный тир: **P0**
- Практический итог после чтения: **P0**
- Workshop: [3635333613](https://steamcommunity.com/sharedfiles/filedetails/?id=3635333613)
- Mod ID: `DynamicTradingCommon`, `DynamicTrading`, `DynamicTradingV2`
- Версия в `mod.info`: 1.0.0, 1.0.1, 1.1.1
- Build: B42.16 snapshot; V2 alpha
- Multiplayer/Dedicated Server: MP/DS: explicitly networked, broad server tree
- Снимок исходников: ветка репозитория на 2026-08-25; runtime-тест не выполнялся.
- Инвентарь: 1419 Lua; client 334, server 96, shared 989, прочие 0.

## Вердикт

Фокус: traders, factions, prices, stock, persistent Bandits-compatible NPCs, dialogue, jobs and radar.

Study transaction validation, stock locking and UUID sync as independent subsystems; keep our quest protocol much smaller.

## Архитектура

| Слой | Что находится в слое |
|---|---|
| CLIENT | trader/contact/faction UI, BeginTradeView/EndTradeView/PerformTrade requests, NPC chat/context/radar and nearby-sync client cache. |
| SERVER | trade, stock, faction and NPC managers; sanitized SyncNPC broadcasts; spawn/order/loot/revive handlers; persistent NPC save/load. |
| SHARED | common price/store/faction/contact models plus the V2 NPC/job system; many shared files branch on isServer/isClient. |

## Network

- DynamicTrading_V2: RequestTrader/RequestFaction/RequestStock/GenerateStock/BeginTradeView/EndTradeView/PerformTrade
- DTNPC: RequestNearbySync/RequestFullSync/SyncNPC/SyncNearbyNPCs/SyncAllNPCs/UpdateNPC/Order
- server sanitizes NPC wire data

Вывод по authority: серверный слой присутствует, но доверие проверяется по каждому handler отдельно.

## Persistence и identity

DTNPC_GlobalList via ModData.getOrCreate and GlobalModData.save; persistent DTNPC_UUID stored on NPC/zombie ModData.

## Полезные механики

- strongest vendor reference
- stock and price services
- trade viewer lifecycle
- persistent NPC UUID
- Bandits compatibility patch
- faction roster and relations
- NPC jobs including travel companion/revive

## Риски и anti-patterns

- 1,400+ Lua files including duplicate/versioned modules
- V2 is alpha
- large shared authority surface is hard to audit
- debug commands must be verified as admin-gated
- extract concepts, not wholesale dependencies

## Файлы для повторного чтения

- `изучить/P0/3635333613/mods/DynamicTradingV2/42.16/media/lua/client/DT/V2/DynamicTrading_Network_Client.lua`
- `изучить/P0/3635333613/mods/DynamicTradingCommon/42.16/media/lua/shared/DT/Common/Faction/TradingSys/DynamicTrading_Network_Server.lua`
- `изучить/P0/3635333613/mods/DynamicTradingV2/42.16/media/lua/client/DT/V2/NPC/DTNPC_TradingHandler.lua`
- `изучить/P0/3635333613/mods/DynamicTradingV2/42.16/media/lua/client/DT/V2/NPC/UI/DTNPC_TraderDialogue_Hub.lua`
- `изучить/P0/3635333613/mods/DynamicTradingV2/42.16/media/lua/server/DT/V2/NPC/Manager/DTNPC_Manager_SaveLoad.lua`
- `изучить/P0/3635333613/mods/DynamicTradingV2/42.16/media/lua/server/DT/V2/NPC/ServerCore/DTNPC_ServerCore_Sync.lua`
- `изучить/P0/3635333613/mods/DynamicTradingV2/42.16/media/lua/server/DT/V2/NPC/ServerCore/ServerCoreCommands/DTNPC_ServerCoreCommands_SyncRequests.lua`
- `изучить/P0/3635333613/mods/DynamicTradingCommon/42.16/media/lua/server/Misc/DT_MoneyServerCommands.lua`
- `изучить/P0/3635333613/mods/DynamicTradingV2/42.16/media/lua/shared/DT/V2/mod-patches/bandits/DTModPatches_Bandits.lua`

## Что проверить в runtime

- double-spend with two clients
- disconnect during trade
- stale stock/version conflict
- forged price/item/quantity
- NPC restart/late join

## Ограничения отчёта

Это статический разбор локального Workshop-снимка. Поддержка DS означает наличие соответствующего кода, а не подтверждённую совместимость с нашей сборкой 42.20.x. Если в пакете не найдено явной лицензии, исходники считаются материалом для изучения, а не разрешением на копирование.

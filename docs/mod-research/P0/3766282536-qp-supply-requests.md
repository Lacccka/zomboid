# 3766282536 — QP Supply Requests

- Исходный тир: **P0**
- Практический итог после чтения: **P0**
- Workshop: [3766282536](https://steamcommunity.com/sharedfiles/filedetails/?id=3766282536)
- Mod ID: `QPSupplyRequests`
- Версия в `mod.info`: 0.7.2
- Build: B42 plus compatibility copy
- Multiplayer/Dedicated Server: MP/DS: yes; server-authoritative object requests
- Снимок исходников: ветка репозитория на 2026-08-25; runtime-тест не выполнялся.
- Инвентарь: 8 Lua; client 4, server 2, shared 2, прочие 0.

## Вердикт

Фокус: world-container request board, item contribution progress, protected supplies and reputation integration.

Useful for physical quest boards/drop-off containers and stable cross-system completion IDs.

## Архитектура

| Слой | Что находится в слое |
|---|---|
| CLIENT | context/UI selects exact container compartment and reports transfer intent/progress. |
| SERVER | resolves square/object/index/sprite/container type, validates creator/admin removal, controls activation/protection and reconciles transfers. |
| SHARED | shared constants encode commands, limits, item rows, priorities and activation modes. |

## Network

- Create/Remove/ReleaseSupplies/UnlockProtection/TransferIntent/ProgressChanged
- server response helper
- object transmitModData

Вывод по authority: серверный слой присутствует, но доверие проверяется по каждому handler отдельно.

## Persistence и identity

request state is stored on world-object ModData and transmitted; request IDs derive from coordinates/object/container/creation stamp; reputation completion uses stable source IDs.

## Полезные механики

- exact world-object/container identity
- multi-item requests
- server-side text/item/priority limits
- transfer intent reconciliation
- cross-mod reputation event

## Риски и anti-patterns

- world object index can change when square contents mutate
- client reports are accepted only after resolver checks but need race testing
- state durability depends on world object persistence
- large hotfix history indicates compatibility complexity

## Файлы для повторного чтения

- `изучить/P0/3766282536/mods/QP Supply Requests/42/media/lua/server/QPSupplyRequests/QPSR_Server.lua`
- `изучить/P0/3766282536/mods/QP Supply Requests/42/media/lua/client/QPSupplyRequests/QPSR_Client.lua`
- `изучить/P0/3766282536/mods/QP Supply Requests/42/media/lua/shared/QPSupplyRequests/QPSR_Shared.lua`
- `изучить/P0/3766282536/mods/QP Supply Requests/README.txt`

## Что проверить в runtime

- two contributors same tick
- container replaced/moved
- multi-compartment fridge
- restart with protected inventory
- duplicate reputation emission

## Ограничения отчёта

Это статический разбор локального Workshop-снимка. Поддержка DS означает наличие соответствующего кода, а не подтверждённую совместимость с нашей сборкой 42.20.x. Если в пакете не найдено явной лицензии, исходники считаются материалом для изучения, а не разрешением на копирование.

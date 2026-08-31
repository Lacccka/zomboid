# Research: внутреннее устройство Workshop-модов

Статический разбор всех пакетов из `изучить/P0`, `изучить/P1`, `изучить/P2` для разработки `LaccckaQuestFramework`. Дата снимка: 2026-08-25.

## Главные выводы

- Лучший прямой baseline для `NPC → server distance validation → branching UI`: **Interactive NPCs**; фактически повышен из P1 в P0.
- Лучший quest/group objective и reward reference: **QP Survivor Contracts**.
- Лучший invite/Accept/Decline/two-phase reference: **Bclan**; его следует сочетать концептуально с QP Contracts.
- Лучший durable group/session state machine: **Extraction Mode**.
- Лучший компактный безопасный marker protocol: **Share Map Notes**.
- Лучший trader/NPC UUID reference: **Dynamic Trading V2**, но это большая alpha-система.
- Главный security anti-pattern: **Chat with Me** — полезный UI, но `AwardItems`/`AwardGift` нельзя использовать как образец authority.
- Для Bandits2 особенно полезны **True Companions** и **NPC&QUEST**, оба фактически повышены из P2 в P1; они также хорошо показывают опасность client-local roster/progress.

## Матрица

| Исходный тир | Workshop ID | Мод | Практический итог | Ключевая подсистема | Отчёт |
|---|---:|---|---|---|---|
| P0 | [1178769629](https://steamcommunity.com/sharedfiles/filedetails/?id=1178769629) | SSR: Core | P0 | generic synchronization and framework services | [1178769629](./P0/1178769629-ssr-core.md) |
| P0 | [2793385743](https://steamcommunity.com/sharedfiles/filedetails/?id=2793385743) | SSR: Quest System | P0 | quest DSL, dialogue, journal, actions/tasks, NPC stand-ins, audio and trading plugin | [2793385743](./P0/2793385743-ssr-quest-system.md) |
| P0 | [3268487204](https://steamcommunity.com/sharedfiles/filedetails/?id=3268487204) | Bandits2 | P0 | NPC brain/runtime, permanent agents, spawning, custom clans, AI programs and network synchronization | [3268487204](./P0/3268487204-bandits2.md) |
| P0 | [3469292499](https://steamcommunity.com/sharedfiles/filedetails/?id=3469292499) | Bandits Creator | P0 | authoring custom Bandits2 NPC/clan definitions | [3469292499](./P0/3469292499-bandits-creator.md) |
| P0 | [3635333613](https://steamcommunity.com/sharedfiles/filedetails/?id=3635333613) | Dynamic Trading Common + Dynamic Trading V1/V2 | P0 | traders, factions, prices, stock, persistent Bandits-compatible NPCs, dialogue, jobs and radar | [3635333613](./P0/3635333613-dynamic-trading-common-dynamic-trading-v1-v2.md) |
| P0 | [3667458787](https://steamcommunity.com/sharedfiles/filedetails/?id=3667458787) | Chat with Me | P1 for UI; reject as DS authority reference | proximity scanner, prompt, branching dialogue, behavior, gifting and trader UI | [3667458787](./P0/3667458787-chat-with-me.md) |
| P0 | [3676995511](https://steamcommunity.com/sharedfiles/filedetails/?id=3676995511) | Share Map Notes | P0 | shared map strokes/markers with validation, visibility groups and batching | [3676995511](./P0/3676995511-share-map-notes.md) |
| P0 | [3715741925](https://steamcommunity.com/sharedfiles/filedetails/?id=3715741925) | Dynamic Objectives | P0 | objective runtime, quest chains, offers, rewards, encounters, hooks, HUD and markers | [3715741925](./P0/3715741925-dynamic-objectives.md) |
| P0 | [3761060249](https://steamcommunity.com/sharedfiles/filedetails/?id=3761060249) | QP Survivor Contracts | P0 | multi-stage individual/group contracts, participants, shared progress, delivery and rewards | [3761060249](./P0/3761060249-qp-survivor-contracts.md) |
| P0 | [3766282536](https://steamcommunity.com/sharedfiles/filedetails/?id=3766282536) | QP Supply Requests | P0 | world-container request board, item contribution progress, protected supplies and reputation integration | [3766282536](./P0/3766282536-qp-supply-requests.md) |
| P0 | [3768490404](https://steamcommunity.com/sharedfiles/filedetails/?id=3768490404) | BONE > Bclan | P0 | invites, accept/decline, membership, alliances and two-phase native faction mutations | [3768490404](./P0/3768490404-bone-bclan.md) |
| P0 | [3771624887](https://steamcommunity.com/sharedfiles/filedetails/?id=3771624887) | QP Survivor Reputation | P0 | multi-path reputation, levels/titles, history, automation and admin editing | [3771624887](./P0/3771624887-qp-survivor-reputation.md) |
| P0 | [3785397275](https://steamcommunity.com/sharedfiles/filedetails/?id=3785397275) | Extraction Mode | P0 | server-owned group raid lifecycle, ready/opt-out, late join, extraction, quests, barter and hideout | [3785397275](./P0/3785397275-extraction-mode.md) |
| P1 | [2941736178](https://steamcommunity.com/sharedfiles/filedetails/?id=2941736178) | Soul Quest System | P2 (historical only) | mission lists, factions, context objectives and text-file quest saves | [2941736178](./P1/2941736178-soul-quest-system.md) |
| P1 | [3001908830](https://steamcommunity.com/sharedfiles/filedetails/?id=3001908830) | PZNS Framework | P2 for MP; P1 for NPC ideas | NPC groups/zones, AI jobs/orders, inventory/context UI and map rendering | [3001908830](./P1/3001908830-pzns-framework.md) |
| P1 | [3384377738](https://steamcommunity.com/sharedfiles/filedetails/?id=3384377738) | Elyon Lib | P1 | UI toolkit, layout/theme, notifications, logging, timers, JSON and network convenience | [3384377738](./P1/3384377738-elyon-lib.md) |
| P1 | [3403180543](https://steamcommunity.com/sharedfiles/filedetails/?id=3403180543) | Bandits Week One | P1 | large scripted story/event scheduler built on Bandits2, chat/radio/audio and world encounters | [3403180543](./P1/3403180543-bandits-week-one.md) |
| P1 | [3508537032](https://steamcommunity.com/sharedfiles/filedetails/?id=3508537032) | NeatUI Framework | P1 | scroll views, virtual lists, grid virtualization, 9-patch and text rendering | [3508537032](./P1/3508537032-neatui-framework.md) |
| P1 | [3634569678](https://steamcommunity.com/sharedfiles/filedetails/?id=3634569678) | Better Safehouse | P1 | group ownership, invites/sub-owners, permissions and protected-zone mutations | [3634569678](./P1/3634569678-better-safehouse.md) |
| P1 | [3639211320](https://steamcommunity.com/sharedfiles/filedetails/?id=3639211320) | EFZ Core/Maps/Quests | P1 | authored story quests, deployment/extraction objectives, Bandits ambushes, map markers and death-drop sync | [3639211320](./P1/3639211320-efz-core-maps-quests.md) |
| P1 | [3667459290](https://steamcommunity.com/sharedfiles/filedetails/?id=3667459290) | Project Fallout: Muggy | P1 | named NPC dialogue/idle definitions, positional voice lines and zone-triggered responses | [3667459290](./P1/3667459290-project-fallout-muggy.md) |
| P1 | [3723411241](https://steamcommunity.com/sharedfiles/filedetails/?id=3723411241) | Knox Net | P1 | persistent group messaging, membership/admin roles, unread state and file persistence | [3723411241](./P1/3723411241-knox-net.md) |
| P1 | [3727050776](https://steamcommunity.com/sharedfiles/filedetails/?id=3727050776) | Interactive NPCs | P0 (promote) | persistent NPC registry, proximity/context interaction, server-validated dialogue request, branching UI and admin editor | [3727050776](./P1/3727050776-interactive-npcs.md) |
| P1 | [3744455714](https://steamcommunity.com/sharedfiles/filedetails/?id=3744455714) | Pager Network | P1 | device identity, direct/channel/broadcast/SOS messages, nearby sharing, unread ACKs and map pings | [3744455714](./P1/3744455714-pager-network.md) |
| P1 | [3761337621](https://steamcommunity.com/sharedfiles/filedetails/?id=3761337621) | QP Survivor Tasks | P1 | physical multiplayer task board, creation handshake, sync and delivery ledgers | [3761337621](./P1/3761337621-qp-survivor-tasks.md) |
| P1 | [3766508989](https://steamcommunity.com/sharedfiles/filedetails/?id=3766508989) | Aegis Panel | P1 | large tabbed UI framework, roles/rights, factions/relations, zones, logs, notifications and player/server panels | [3766508989](./P1/3766508989-aegis-panel.md) |
| P2 | [1905148104](https://steamcommunity.com/sharedfiles/filedetails/?id=1905148104) | Superb Survivors | P2 | classic survivor AI, dialogue, group UI, task manager and quest windows | [1905148104](./P2/1905148104-superb-survivors.md) |
| P2 | [2877685881](https://steamcommunity.com/sharedfiles/filedetails/?id=2877685881) | Shared Faction Map | P2 | faction-scoped vanilla world-map annotations | [2877685881](./P2/2877685881-shared-faction-map.md) |
| P2 | [3001910188](https://steamcommunity.com/sharedfiles/filedetails/?id=3001910188) | PZNS Agent Wong | P2 | small example NPC addon/template for PZNS | [3001910188](./P2/3001910188-pzns-agent-wong.md) |
| P2 | [3087165610](https://steamcommunity.com/sharedfiles/filedetails/?id=3087165610) | tradeHunter | P2 | spawned trader/hunter and custom trade UI | [3087165610](./P2/3087165610-tradehunter.md) |
| P2 | [3143880496](https://steamcommunity.com/sharedfiles/filedetails/?id=3143880496) | PZNS Interaction Buttons | P2 | floating/quick interaction buttons for PZNS NPC actions | [3143880496](./P2/3143880496-pzns-interaction-buttons.md) |
| P2 | [3656190498](https://steamcommunity.com/sharedfiles/filedetails/?id=3656190498) | Reactive Sound Events | P1 for audio/events (promote) | dynamic scenes, positional sound, radio intel, markers and persistent event scheduler | [3656190498](./P2/3656190498-reactive-sound-events.md) |
| P2 | [3711695385](https://steamcommunity.com/sharedfiles/filedetails/?id=3711695385) | MissionsEvents | P2 | two small scripted missions with server start/confirm/sync and UI/timed actions | [3711695385](./P2/3711695385-missionsevents.md) |
| P2 | [3751199292](https://steamcommunity.com/sharedfiles/filedetails/?id=3751199292) | True Companions | P1 for Bandits lifecycle (promote) | Bandits2 companions, affinity/recruitment, roster, dialogue, schedules, beacons, stations, zones and quests | [3751199292](./P2/3751199292-true-companions.md) |
| P2 | [3754417819](https://steamcommunity.com/sharedfiles/filedetails/?id=3754417819) | NPC&QUEST | P1 for authored Bandits story (promote) | persistent named sister Bandits NPC, dialogue, quests, map/radio/story and vehicle companion state | [3754417819](./P2/3754417819-npc-quest.md) |

## Рекомендуемый порядок глубокого runtime-аудита

1. Interactive NPCs — прогнать точный interaction/dialogue flow на 42.20 DS.
2. QP Survivor Contracts — извлечь state machine, contribution и reward-idempotency тесты.
3. Bclan — воспроизвести invitation/finalization/reconciliation под disconnect/restart.
4. Extraction Mode — составить таблицу фаз и reconnect/late-join переходов.
5. Share Map Notes — использовать как эталон малой сетевой поверхности.
6. Dynamic Trading V2 — отдельно проверить PerformTrade/stock locks и DTNPC_UUID.
7. Bandits2 + True Companions + NPC&QUEST — проверить постоянную identity через unload/restart/death.
8. Reactive Sound Events + Muggy — измерить синхронизацию voice/subtitle для нескольких клиентов.

## Общий принцип для LaccckaQuestFramework

Клиент обнаруживает NPC и показывает UI, но сервер повторно разрешает NPC по постоянному ID, проверяет расстояние/доступ/текущий node/session, исполняет transition, обновляет durable state и выдаёт награду идемпотентно. Клиент получает только проекцию состояния и presentation-команды. Ни один найденный мод не закрывает все эти требования одновременно; полезная архитектура складывается из нескольких reference implementations.

## Внешний research synthesis — 2026-09-01

После дополнительного прохода по актуальным Build 42 NPC/RPG/economy/settlement-модам, форумам Indie Stone и community feedback выводы были сведены в отдельный архитектурный документ:

- [`docs/design/quest-framework-external-research-roadmap-2026-09-01.md`](../design/quest-framework-external-research-roadmap-2026-09-01.md)

Главное изменение ближайшего roadmap: **до фиксации общего economy ledger необходимо ввести `QuantitySemantics` и B42 tag/resource-aware matching**, чтобы экономика не закрепилась на неверном универсальном правиле `1 InventoryItem = 1 unit`.

Также в roadmap формально добавлены:

- inventory custody / resource reservations;
- settlement `SiteArea` / storage roles;
- `WorldEvent` + Storylet layer;
- event-based NPC memory and rumours;
- logical Squad state above provider brains;
- WorldEventDirector / convoys / raids / distress / rescue encounters;
- faction radio/comms;
- party quest server-side canonical random state;
- content revisions/migrations;
- network/spatial interest management;
- generalized AccessPolicy above current SafeHouse exclusion;
- knowledge/intelligence map;
- surrender/prisoner/disarm as future logical NPC states.

Эти пункты являются архитектурными направлениями `LaccckaQuestFramework`, а не утверждением, что соответствующий внешний мод является безопасным implementation reference. Для внешних модов без локального source-аудита заимствуются только проверяемые концепции.
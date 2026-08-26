# Spatial model Project Zomboid B42.20.3: объекты мира, зоны, координаты, quest targets и map markers

**Status:** research/design reference  
**Context:** Project Zomboid Build 42.20.3 (B42.20.x), Dedicated Server / Multiplayer, `WorkshopPatches/QuestFramework`  
**Purpose:** зафиксировать фактическую модель пространства игры и правила, по которым QuestFramework должен адресовать здания, территории, комнаты, точки назначения и маркеры на карте.

Связанный документ: `docs/design/quest-framework-target-vision.md`.

---

## 1. Краткий вывод

Главный вывод исследования: **Project Zomboid не представляет большинство реальных мест как один цельный именованный объект высокого уровня**.

Например, понятие «полицейский участок» для игрока выглядит как один объект, но внутри игры это может быть комбинация из:

- глобальных координат мира;
- `BuildingDef` — метаописания здания;
- нескольких `RoomDef`;
- нескольких прямоугольников внутри каждого `RoomDef`;
- загруженных в данный момент `IsoRoom` / `IsoBuilding`;
- отдельных `Zone`, наложенных на здание, двор, парковку или окружающую территорию;
- `IsoGridSquare` и находящихся на них `IsoObject`;
- world-map geometry / properties;
- отдельного названия улицы или населённого пункта;
- отдельных gameplay-данных, например vehicle zones, loot room types, zombie zones и т. п.

Поэтому QuestFramework **не должен считать, что в vanilla существует универсальный объект** вида:

```text
PoliceStation {
    id = "RosewoodPoliceStation"
    name = "Rosewood Police Station"
    x, y, width, height
}
```

Такого общего контракта у движка нет.

Для квестов необходимо разделять как минимум три вещи:

```text
Location identity      — наше стабильное имя места
QuestTarget            — геометрия/условие, которое проверяет сервер
QuestMarker            — то, что разрешено показать игроку на карте
```

То есть принципиально:

```text
Location != BuildingDef != Zone != QuestTarget != QuestMarker
```

---

## 2. На чём основаны выводы

Исследование сделано непосредственно по сохранённому в репозитории декомпилированному Build 42.20.3 и по уже разобранным модам.

Основные vanilla-классы:

- `game_source/common-42.20.3/java/zombie/iso/IsoMetaGrid.java`
- `game_source/common-42.20.3/java/zombie/iso/IsoMetaCell.java`
- `game_source/common-42.20.3/java/zombie/iso/BuildingDef.java`
- `game_source/common-42.20.3/java/zombie/iso/RoomDef.java`
- `game_source/common-42.20.3/java/zombie/iso/zones/Zone.java`
- `game_source/common-42.20.3/java/zombie/iso/areas/IsoBuilding.java`
- `game_source/common-42.20.3/java/zombie/iso/areas/IsoRoom.java`
- `game_source/common-42.20.3/java/zombie/worldMap/WorldMap.java`
- `game_source/common-42.20.3/java/zombie/worldMap/WorldMapFeature.java`
- `game_source/common-42.20.3/java/zombie/worldMap/streets/WorldMapStreet.java`
- `game_source/common-42.20.3/java/zombie/worldMap/symbols/WorldMapSymbols.java`
- `game_source/common-42.20.3/java/zombie/worldMap/symbols/WorldMapBaseSymbol.java`
- `game_source/common-42.20.3/java/zombie/worldMap/UIWorldMapV1.java`
- `game_source/common-42.20.3/java/zombie/worldMap/WorldMapRenderer.java`

Практические mod references:

- `docs/mod-research/P0/3715741925-dynamic-objectives.md`
- `docs/mod-research/P0/3676995511-share-map-notes.md`
- `docs/mod-research/P0/2793385743-ssr-quest-system.md`
- `изучить/P0/3715741925/mods/DynamicObjectives/42.16/media/lua/client/DO/Markers/DO_MapMarkerSystem.lua`

`game_source/client-42.20.3/BUILD.txt` подтверждает, что сохранённый runtime overlay относится к Build 42.20.3.

---

# 3. Координатная модель мира

## 3.1. Основная единица — world square / tile

Большая часть пространственной логики Project Zomboid опирается на абсолютные мировые координаты:

```text
x, y, z
```

Где:

- `x` — одна горизонтальная мировая ось;
- `y` — вторая горизонтальная мировая ось;
- `z` — уровень/этаж;
- целочисленные `x/y` обычно адресуют конкретный `IsoGridSquare`.

Это **глобальные координаты объединённого мира**, а не локальные координаты относительно Роузвуда, Риверсайда или конкретного здания.

У каждого города нет собственного `(0, 0)`.

`(0, 0)` является математическим началом абсолютной системы world squares, однако реальная загруженная карта имеет свои `minX/minY/maxX/maxY`; playable vanilla world не обязан начинаться на видимой карте ровно с `(0, 0)`.

Для обычного top-down представления карты меньшие `x/y` находятся ближе к северо-западной стороне мира, большие — к юго-восточной. Для квестовой логики важнее не направление, а тот факт, что это единое абсолютное пространство.

---

## 3.2. Chunk в B42.20.3 — 8 × 8 squares

Это подтверждается, например, `RoomDef.forEachChunk()`, где room rectangles переводятся в chunk coordinates через деление на `8`:

```text
chunkX = squareX / 8
chunkY = squareY / 8
```

Следовательно:

```text
1 chunk = 8 × 8 world squares
```

Chunk — прежде всего единица streaming/runtime мира. Для authored quest target хранить цель в chunk coordinates обычно не нужно.

---

## 3.3. Meta cell — 256 × 256 squares

`IsoMetaCell` задаёт:

```text
CHUNKS_PER_CELL = 32
```

При chunk size 8:

```text
32 × 8 = 256
```

`IsoMetaGrid.getCellDataAbs(x, y)` также переводит абсолютный world square в meta cell через `/ 256`.

Таким образом:

```text
1 meta cell = 32 × 32 chunks
            = 256 × 256 world squares
```

Начало meta cell `(cellX, cellY)` в world-square coordinates:

```text
worldX = cellX * 256
worldY = cellY * 256
```

`WorldMap` использует ту же размерность: bounds в cells переводятся в square bounds через `* 256`.

Это важное подтверждение того, что world map и gameplay world опираются на совместимое координатное пространство.

---

## 3.4. Иерархия координат

Упрощённо:

```text
World
└── Meta Cell (256 × 256 squares)
    └── Chunk (8 × 8 squares)
        └── IsoGridSquare (1 tile)
            └── IsoObject / character / item / vehicle / etc.
```

Для QuestFramework основным публичным пространственным контрактом должны быть **world-square coordinates / geometry**, а не meta-cell/chunk indices.

---

# 4. Почему «здание» в игре — не один монолитный объект

## 4.1. `BuildingDef` — метаописание здания

`BuildingDef` содержит, среди прочего:

```text
rooms
x, y, x2, y2
metaID
keyId
WorkshopID
modID
...
```

Но очень важно понимать происхождение его bounds.

`BuildingDef.CalculateBounds()` проходит по `RoomDef.rects` всех комнат и вычисляет минимальный enclosing rectangle:

```text
building.x  = min(room rectangles x)
building.y  = min(room rectangles y)
building.x2 = max(room rectangles x2)
building.y2 = max(room rectangles y2)
```

Следовательно:

> `BuildingDef` bounding box — это охватывающий прямоугольник, а не гарантированно точный footprint здания.

Если здание имеет Г-образную форму, внутренний двор, два крыла или другие пустоты, bounding box способен включать squares, которые фактически не принадлежат помещениям здания.

Поэтому проверка:

```text
player.x between building.x and building.x2
AND
player.y between building.y and building.y2
```

может быть слишком грубой для точного quest trigger.

---

## 4.2. `RoomDef` тоже не обязательно один прямоугольник

`RoomDef` содержит:

```text
String name
int level
ArrayList<RoomRect> rects
BuildingDef building
...
```

Одна комната может состоять из **нескольких `RoomRect`**.

`RoomDef.isInside(x, y, z)`:

1. проверяет уровень `z`;
2. проходит по всем `rects`;
3. считает square принадлежащим комнате, если он попал хотя бы в один rectangle.

То есть геометрически room — это скорее:

```text
RoomDef = union(RoomRect1, RoomRect2, RoomRect3, ...)
```

а не обязательно:

```text
RoomDef = one rectangle
```

У `RoomDef` тоже есть рассчитанные `x/y/x2/y2`, но это enclosing bounds, а не замена точной проверки по `rects`.

---

## 4.3. Runtime `IsoBuilding` и `IsoRoom` — ещё один слой

Движок разделяет metadata и реально загруженное runtime-представление.

### Metadata

```text
BuildingDef
RoomDef
```

### Runtime / streamed world

```text
IsoBuilding
IsoRoom
IsoGridSquare
```

`IsoBuilding` содержит список загруженных `IsoRoom` и ссылку на `BuildingDef`.

`IsoRoom` содержит:

- ссылку на `RoomDef`;
- ссылку на `IsoBuilding`;
- список реально доступных `IsoGridSquare`;
- exits, beds, water sources и другие runtime-данные.

Следовательно, **runtime Java object нельзя использовать как долговременную идентичность quest location**.

Он может зависеть от streaming/loading состояния мира.

Для persistence квестов следует хранить сериализуемое пространственное описание, а runtime `IsoBuilding/IsoRoom` использовать только как временно разрешённую ссылку.

---

# 5. Что такое `Zone`

`zombie.iso.zones.Zone` — отдельная пространственная сущность.

Она имеет как минимум:

```text
name
type
x, y, z
w, h
bounds
geometry
geometryType
```

То есть zone действительно может давать именованную/типизированную территорию.

Однако важная поправка:

> Не каждый «объект» игры имеет одну собственную Zone, и не каждая Zone является зданием.

Zone — самостоятельный слой map/gameplay metadata.

---

## 5.1. Zone не ограничена только прямоугольником

Конструктор с `x/y/w/h` создаёт Rectangle zone, но код поддерживает также geometry.

`Zone.isInside(...)` различает как минимум:

- Rectangle;
- Point;
- Polygon.

То есть для более сложных областей движок способен хранить не только axis-aligned rectangle.

Это особенно интересно для будущих quest search areas.

---

## 5.2. Zone может иметь `name` и `type`

Например, движок работает с такими типами зон, как:

```text
Water
Forest
DeepForest
ForagingNav
TrailerPark
TownZone
Farm
FarmLand
...
```

Также существуют другие gameplay-specific зоны и vehicle zones.

`IsoMetaGrid` предоставляет запросы уровня:

```text
getZoneAt(x, y, z)
getZonesAt(x, y, z, ...)
registerZone(name, type, x, y, z, w, h)
```

Одновременно в одной точке может быть релевантно несколько разных пространственных слоёв.

Поэтому `Zone.name/type` полезны как selector, но их нельзя автоматически трактовать как уникальный ID реального POI.

---

# 6. «Полицейский участок» как пример

Для человека:

```text
Rosewood Police Station
```

— один объект.

Для движка эта же локация потенциально складывается из:

```text
BuildingDef
├── RoomDef: lobby / office / storage / cells / ...
│   └── one or more RoomRect
├── RoomDef
│   └── one or more RoomRect
└── ...

+ surrounding IsoGridSquare
+ possible outdoor/parking/vehicle Zone
+ world-map geometry/properties
+ street data
+ loot room classifications
+ other metadata
```

Нельзя исходить из того, что в коде обязательно существует:

```text
BuildingDef.name == "Rosewood Police Station"
```

У `BuildingDef` нет обязательного канонического human-readable POI name такого уровня.

При этом:

- `RoomDef` имеет `name`;
- `Zone` имеет `name/type`;
- `WorldMapStreet` имеет собственный `name`;
- generic `WorldMapFeature` имеет geometry + arbitrary properties;
- город/вариант карты существует отдельным data/config слоем (`media/maps/...`, `map.info`).

Это разные источники семантики. Они не объединены движком в один универсальный `Location` object.

---

# 7. Именованные обозначения: что можно считать стабильным

## 7.1. Что НЕ следует делать

Не следует сохранять quest definition только как:

```lua
location = "PoliceStation"
```

или:

```lua
buildingMetaID = 12345
```

или как ссылку на runtime object:

```lua
building = someIsoBuilding
```

без дополнительной валидации.

Причины:

- название может быть room/zone classification, а не уникальным POI;
- несколько мест могут иметь одинаковый `name/type`;
- metadata IDs не следует считать нашим долговременным semantic contract между версиями карты, пока стабильность отдельно не доказана;
- runtime object не является persistence identity;
- модовые карты могут заменять/перекрывать cells и менять геометрию.

---

## 7.2. Наш `LocationRegistry`

QuestFramework должен ввести собственный semantic layer.

Например:

```lua
LocationRegistry["vanilla.rosewood.police_station"] = {
    displayNameKey = "IGUI_LQF_Location_RosewoodPoliceStation",

    target = {
        kind = "BUILDING_REGION",
        -- validated resolver / authored geometry
    },

    marker = {
        labelKey = "IGUI_LQF_Location_RosewoodPoliceStation",
        icon = "LQF_QuestTarget",
    },
}
```

Тогда:

```text
vanilla.rosewood.police_station
```

— это **наш стабильный ID**, а не предположение о внутреннем vanilla ID.

Внутри registry мы можем хранить:

- заранее проверенную геометрию;
- representative point;
- expected map/build fingerprint;
- optional `BuildingDef.metaID` как быстрый lookup hint;
- ожидаемые room names;
- zone selectors;
- fallback coordinates;
- человекочитаемое/локализуемое название.

Это позволит переживать изменения implementation details, не меняя quest content API.

---

# 8. Как должен выглядеть QuestTarget

Квестовое условие должно описывать **что сервер проверяет**, а не то, что игрок видит на карте.

Рекомендуемые базовые spatial target kinds:

```text
POINT
RADIUS
RECT
POLYGON
ROOM
BUILDING_REGION
ZONE
LOCATION
ENTITY
```

### `POINT`

Точная world square / позиция.

```lua
{
    kind = "POINT",
    x = 8123,
    y = 11456,
    z = 0,
}
```

Полезно для конкретного объекта, NPC, двери, терминала и т. п.

### `RADIUS`

```lua
{
    kind = "RADIUS",
    x = 8123,
    y = 11456,
    z = 0,
    radius = 10,
}
```

Хорошо подходит для «доберитесь до места» без требования наступить на один tile.

### `RECT`

```lua
{
    kind = "RECT",
    x1 = ...,
    y1 = ...,
    x2 = ...,
    y2 = ...,
    zMin = 0,
    zMax = 1,
}
```

Подходит для простой authored territory, но нужно помнить: enclosing rectangle здания может включать лишние squares.

### `POLYGON`

Предпочтителен для сложной территории, двора, военного объекта, compound и т. п.

### `ROOM`

Условие принадлежности к определённому `RoomDef`, желательно через валидированный resolver, а не только по room name.

### `BUILDING_REGION`

Условие «игрок действительно находится внутри помещения/комнат нужного здания», а не просто внутри bounding box `BuildingDef`.

### `ZONE`

Использует конкретную Zone или selector + validation.

### `LOCATION`

Ссылка на наш `LocationRegistry`:

```lua
{
    kind = "LOCATION",
    id = "vanilla.rosewood.police_station",
}
```

### `ENTITY`

Динамическая цель: NPC, vehicle, moving convoy и т. п.

---

# 9. Точная проверка здания

Если objective означает:

> Войдите внутрь полицейского участка.

нежелательно проверять только `BuildingDef` bounds.

Лучше:

```text
player square
→ room / building membership
→ resolved BuildingDef
→ compare with expected resolved location
```

или проверять union room rectangles конкретного здания.

Если objective означает:

> Доберитесь до территории полицейского участка.

тогда target может сознательно включать:

- здание;
- парковку;
- двор;
- подход к входу.

То есть semantic target шире самого `BuildingDef`.

Это ещё одна причина, почему quest location и engine building нельзя отождествлять.

---

# 10. Что показывать игроку: координаты или место

Для обычного gameplay игроку **не следует выдавать raw world coordinates как основной ориентир**.

Плохой UX:

```text
Идите к 8123, 11456, 0.
```

Хороший UX:

```text
Доберитесь до полицейского участка в Роузвуде.
```

При наличии полезной географии:

```text
Роузвуд — полицейский участок, рядом с <ориентир/улица>.
```

Причины:

- координаты — техническая система движка, а не естественная навигация игрока;
- они ломают immersion;
- раскрывают точность, которую quest designer иногда хочет скрыть;
- игроку всё равно потребуется карта/внешний инструмент, чтобы интерпретировать числа.

Raw coordinates должны оставаться доступны для:

- admin/debug UI;
- quest authoring tools;
- логов;
- telemetry;
- диагностики desync/location resolver.

---

# 11. World map markers в B42.20.3

Build 42.20.3 предоставляет подходящий native symbol layer.

`WorldMapSymbols` поддерживает:

```text
addTexture(symbolID, x, y, ...)
addText(..., x, y, ...)
addTranslatedText(...)
addUntranslatedText(...)
removeSymbol(...)
removeSymbolByIndex(...)
```

У symbol есть:

- world `x/y`;
- anchor;
- scale;
- rotation;
- RGBA;
- `minZoom/maxZoom`;
- visibility;
- `userDefined`;
- network metadata для shared symbols.

`WorldMapBaseSymbol.layout()` преобразует сохранённые world coordinates через:

```text
worldToUIX(this.x, this.y)
worldToUIY(this.x, this.y)
```

То есть QuestFramework может ставить marker непосредственно в той же world-coordinate domain, в которой задан target.

---

## 11.1. Это уже подтверждено сторонним B42-модом

`Dynamic Objectives` использует именно этот путь.

В `DO_MapMarkerSystem.lua` клиент получает `symbolsAPI`, затем создаёт quest texture приблизительно по следующей схеме:

```lua
symbolsAPI:addTexture("DOQuestTarget", x, y, ...)
```

и затем:

```lua
marker:setAnchor(0.5, 0.5)
marker:setScale(...)
marker:setCollide(false)
marker:setUserDefined(false)
```

При изменении цели marker можно перемещать через `setPosition(x, y)`.

Это сильное практическое подтверждение, что native B42 world-map symbol API подходит для quest markers.

---

# 12. `QuestTarget != QuestMarker`

Это должно стать жёстким архитектурным правилом.

## QuestTarget

Отвечает на вопрос:

> Где/что сервер считает выполнением objective?

Например:

```lua
QuestTarget = {
    kind = "POLYGON",
    points = { ... },
    zMin = 0,
    zMax = 1,
}
```

## QuestMarker

Отвечает на вопрос:

> Какую часть этой информации разрешено показать игроку?

Например:

```lua
QuestMarker = {
    mode = "APPROXIMATE",
    x = ...,
    y = ...,
    icon = "LQF_SearchArea",
    labelKey = "IGUI_LQF_SearchArea",
}
```

Они могут иметь разные координаты и разную точность.

---

# 13. Режимы раскрытия местоположения

Рекомендуется как минимум четыре режима.

## `EXACT`

Игрок получает точную точку.

```text
[!] Полицейский участок
```

Подходит для обычного «доберитесь до NPC/здания».

## `APPROXIMATE`

Marker стоит в representative point или слегка обобщённой позиции, а server target остаётся точным.

```text
[?] Район поиска
```

Подходит для investigation/search quests.

## `AREA`

Игроку показывается search area, если мы реализуем area overlay/radius rendering поверх native symbols.

Сервер может знать конкретный объект внутри области, не раскрывая его координату.

## `HIDDEN / LANDMARK_ONLY`

Marker отсутствует.

NPC сообщает только естественный ориентир:

```text
«Ищи старый склад к северу от города».
```

Server target при этом остаётся строго формализованным.

---

# 14. Representative point

Не каждая территория естественно имеет одну точку.

Поэтому spatial target должен уметь вычислять отдельный:

```text
representativePoint
```

для UI/map marker.

Например:

```text
RECT/POLYGON target
        ↓
server validates full area
        ↓
marker uses center / entrance / authored hint
```

Для здания representative point лучше иногда ставить не в математический центр bounds, а:

- возле главного входа;
- на визуально понятной части здания;
- в центре нужного compound;
- в deliberately approximate position для search quest.

---

# 15. Multiplayer architecture

Quest state должен оставаться server-authoritative.

Рекомендуемый поток:

```text
SERVER
QuestRuntimeState
├── objective state
├── canonical QuestTarget
└── marker presentation policy
        ↓
   targeted quest sync
        ↓
CLIENT
QuestMarkerService
        ↓
WorldMapSymbols API
```

То есть сервер решает:

- кто участвует в квесте;
- какая стадия активна;
- какой target действителен;
- выполнен ли objective;
- что разрешено раскрыть участнику.

Клиент только визуализирует marker.

---

## 15.1. Почему не стоит делать квестовый marker обычной пользовательской пометкой

`WorldMapSymbols` различает user-defined и default/system-like annotations.

Есть, например:

```text
clearDefaultAnnotations()
clearUserAnnotations()
```

У symbol есть `setUserDefined(boolean)`.

`Dynamic Objectives` для quest target выставляет:

```lua
setUserDefined(false)
```

Это хороший шаблон для нас.

Квестовый marker должен принадлежать `QuestMarkerService`, а не пользователю.

Пользователь не должен случайно превратить его в долговременную независимую map note.

---

## 15.2. Shared symbol networking

`WorldMapBaseSymbol` в B42.20.3 уже имеет network metadata и понятия:

```text
private
shared
visible to everyone
visible to faction
visible to safehouse
visible to selected player
```

`Share Map Notes` также демонстрирует multiplayer synchronization map annotations.

Однако для QuestFramework базовый вариант лучше делать так:

> Сервер синхронизирует quest state только релевантным участникам, а каждый клиент создаёт локальное UI-представление marker.

Преимущества:

- quest authority остаётся отдельно от map-note subsystem;
- проще party-specific visibility;
- marker легко восстановить после reconnect из quest snapshot;
- пользовательское редактирование карты не влияет на quest state;
- можно давать разную информацию разным игрокам/стадиям;
- marker lifecycle однозначно связан с objective lifecycle.

Native shared-symbol network можно использовать позднее как adapter, если появится конкретная причина.

---

# 16. Рекомендуемый lifecycle marker

```text
Objective activated
    ↓
QuestMarkerService.ensureMarker()

Target presentation changed
    ↓
QuestMarkerService.updateMarker()

Moving target changed position
    ↓
QuestMarkerService.setPosition()

Objective completed / failed / hidden
    ↓
QuestMarkerService.removeMarker()

Reconnect / UI reopened
    ↓
rebuild markers from authoritative quest snapshot
```

Не следует считать Java/Lua marker object частью persistence состояния квеста.

Persistence хранит marker **definition/state**, а UI object восстанавливается.

---

# 17. Динамические targets

Так как `WorldMapBaseSymbol` поддерживает `setPosition(x, y)`, marker способен следовать за динамической целью.

Примеры:

- NPC;
- vehicle;
- convoy;
- moving extraction point;
- последняя известная позиция цели.

Но marker не нужно обновлять каждый frame/tick без необходимости.

Лучше использовать:

- event-driven updates;
- threshold по пройденной дистанции;
- умеренный timer;
- «last known position» semantics.

---

# 18. Рекомендуемая модель данных QuestFramework

Пример conceptual schema:

```lua
QuestSpatialTarget = {
    id = "objective.target.main",

    kind = "LOCATION", -- POINT/RADIUS/RECT/POLYGON/ROOM/BUILDING_REGION/ZONE/LOCATION/ENTITY

    locationId = "vanilla.rosewood.police_station",

    -- resolved/static geometry when applicable
    x = nil,
    y = nil,
    z = nil,
    radius = nil,
    rect = nil,
    polygon = nil,

    -- optional resolver hints, never sole semantic identity unless explicitly guaranteed
    resolver = {
        mapId = "Muldraugh, KY",
        buildingMetaId = nil,
        roomNames = nil,
        zoneName = nil,
        zoneType = nil,
    },

    validation = {
        build = "42.20.3",
        source = "vanilla",
    },
}
```

Отдельно:

```lua
QuestMarkerDefinition = {
    id = "objective.marker.main",
    targetId = "objective.target.main",

    visible = true,
    mode = "EXACT", -- EXACT / APPROXIMATE / AREA / HIDDEN

    icon = "LQF_QuestTarget",
    labelKey = "IGUI_LQF_Objective_GoToPoliceStation",

    positionMode = "REPRESENTATIVE_POINT",
    x = nil,
    y = nil,

    minZoom = nil,
    maxZoom = nil,

    showOnWorldMap = true,
    showOnMiniMap = false,
}
```

---

# 19. Рекомендуемые сервисы

## `LocationRegistry`

Наш каталог стабильных семантических мест.

Обязанности:

- stable location IDs;
- localization keys;
- resolver definitions;
- authored geometry/fallbacks;
- representative points;
- validation metadata.

## `SpatialResolver`

Превращает declarative target в runtime-checkable representation.

Пример API:

```text
resolve(targetDefinition)
contains(resolvedTarget, x, y, z)
representativePoint(resolvedTarget)
describe(resolvedTarget, locale)
validate(resolvedTarget)
```

## `QuestMarkerService`

Клиентская визуализация.

```text
ensureMarker(markerState)
updateMarker(markerState)
removeMarker(markerId)
rebuildFromSnapshot(activeQuestState)
```

## `QuestObjectiveValidator`

Серверная проверка факта выполнения objective.

Она не должна доверять факту наличия/позиции UI marker.

---

# 20. Примеры использования

## 20.1. «Доберитесь до полицейского участка»

### Игрок видит

```text
Задача: Доберитесь до полицейского участка в Роузвуде.

Map marker:
[!] Полицейский участок
```

### Сервер хранит

```text
Location ID:
vanilla.rosewood.police_station

Resolved target:
validated building/entry region
```

Objective завершается, когда player реально входит в требуемую область.

---

## 20.2. «Обыщите территорию полицейского участка»

Здесь target не должен быть равен только помещениям здания.

```text
QuestTarget = compound/search polygon
QuestMarker = approximate representative point
```

Игрок видит:

```text
[?] Зона поиска
```

Сервер знает полную точную геометрию.

---

## 20.3. «Найдите тайник возле участка»

```text
actual cache = exact x/y/z or spawned entity
allowed search area = radius/polygon
marker = center of allowed search area
```

Игрок не получает координаты самого тайника.

---

## 20.4. «Найдите движущегося NPC»

```text
QuestTarget.kind = ENTITY
QuestTarget.entityId = stable NPC id
```

Server-authoritative NPC identity остаётся отдельной от физического Bandits/Bandits2 instance.

Marker может отображать:

- exact current position;
- periodically updated position;
- last known position;
- вообще ничего.

---

# 21. Модовые карты и compatibility risks

Этот слой особенно важен для большой mod сборки.

Модовая карта способна:

- занимать те же meta cells;
- изменять load order;
- заменять/добавлять здания;
- менять `RoomDef`/`BuildingDef` topology;
- добавлять свои zones;
- менять world-map features;
- смещать фактическую семантику конкретной территории.

Поэтому authored vanilla location нельзя считать автоматически валидной в любой map stack.

Для `LocationRegistry` желательно предусмотреть:

```text
map requirements
map incompatibilities
resolver validation
fallback disabled state
admin diagnostics
```

Например:

```text
LocationResolver: vanilla.rosewood.police_station
status: INVALIDATED
reason: expected building geometry not found / map cell replaced
```

В такой ситуации лучше не запускать квест, чем отправить игрока в неправильное место.

---

# 22. Persistence и стабильность

Нельзя сохранять как единственный источник истины:

- ссылку на `IsoBuilding`;
- ссылку на `IsoRoom`;
- ссылку на `Zone` Java object;
- world-map symbol object;
- transient Bandits instance;
- один `metaID` без контекста и validation.

Следует сохранять declarative/stable state:

```text
questId
objectiveId
locationId / target definition
target resolution version
marker presentation state
party/player scope
runtime quest progress
```

После server restart/reconnect нужные engine objects должны резолвиться заново.

---

# 23. Логи и диагностика

Spatial system должен логировать минимум:

```text
questId
objectiveId
player
locationId
target kind
resolved geometry / representative point
player x/y/z
resolution result
inside/outside result
map/build validation status
marker id
marker add/update/remove
```

Пример:

```text
[LQF][Spatial]
quest=main_001
objective=reach_police
location=vanilla.rosewood.police_station
player=Lacccka
pos=8131,11462,0
resolver=BUILDING_REGION
inside=true
```

Это значительно упростит MP desync/debugging.

---

# 24. Что считать каноническим правилом QuestFramework

## Правило 1

**Ни одно реальное место не считается одним engine object только потому, что игрок воспринимает его как один объект.**

## Правило 2

**`BuildingDef` — aggregate metadata здания, а его bounding rectangle не равен гарантированно точной площади здания.**

## Правило 3

**`RoomDef` может состоять из нескольких rectangles.**

## Правило 4

**`Zone` — независимый spatial/gameplay layer. Она может быть Rectangle, Point или Polygon и иметь `name/type`, но zone не является универсальным POI identity.**

## Правило 5

**Runtime `IsoBuilding/IsoRoom` не являются persistence identity.**

## Правило 6

**Для контента используется собственный `LocationRegistry` со стабильными IDs.**

## Правило 7

**Server quest validation работает с `QuestTarget`, а UI — с отдельным `QuestMarker`.**

## Правило 8

**Игроку показывается человекочитаемое место/ориентир и, когда это соответствует дизайну квеста, marker. Raw coordinates остаются authoring/debug information.**

## Правило 9

**Marker никогда не является доказательством выполнения objective. Сервер проверяет реальную spatial condition.**

## Правило 10

**Любая authored location должна быть валидируема относительно текущей карты/mod stack.**

---

# 25. Предлагаемый следующий этап реализации

Перед массовым созданием квестов стоит реализовать минимальный spatial foundation:

```text
LQF LocationRegistry
        ↓
LQF SpatialResolver
        ↓
server QuestObjectiveValidator
        ↓
quest state sync
        ↓
client QuestMarkerService
        ↓
WorldMapSymbols
```

Первый integration test лучше сделать на одной заранее проверенной vanilla location:

1. зарегистрировать location в `LocationRegistry`;
2. вывести resolver diagnostics;
3. показать exact marker на world map;
4. проверить marker reconnect/rebuild;
5. войти в target area;
6. серверно завершить objective;
7. удалить marker;
8. повторить в MP с двумя игроками, из которых квест есть только у одного;
9. затем проверить shared/party objective;
10. после этого добавить approximate/search-area вариант.

---

# 26. Итог

Project Zomboid B42.20.3 предоставляет достаточно пространственных данных для полноценной quest/location system, но движок не даёт готового универсального слоя «именованных игровых POI».

Правильная модель для QuestFramework:

```text
Vanilla/mod world data
    ├── coordinates
    ├── BuildingDef / RoomDef
    ├── Zone
    ├── world-map features
    └── runtime world objects
             ↓
       SpatialResolver
             ↓
       LocationRegistry
             ↓
        QuestTarget          QuestMarker
       (server truth)       (client presentation)
             ↓                    ↓
 objective validation       WorldMapSymbols
```

Это позволяет одновременно получить:

- точные server-authoritative quest triggers;
- нормальные человекочитаемые задания;
- exact/approximate/hidden navigation;
- native world-map markers;
- поддержку group/party quests;
- восстановление после reconnect/restart;
- возможность адаптации к mod maps;
- независимость quest content от нестабильных runtime Java/Lua objects.

Именно этот слой следует считать базовой spatial architecture будущего `LaccckaQuestFramework`.

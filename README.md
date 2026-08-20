# Lacccka B42.20 Compatibility Patch

Репозиторий используется для поддержки и тестирования **Project Zomboid Dedicated Server** на **Build 42.20.x**, а также для разработки локального **Lacccka B42.20 Compatibility Patch**.

Числовые каталоги в корне репозитория соответствуют **Steam Workshop ID** исходных модов. Эта таблица нужна, чтобы быстро понять, какой каталог относится к какому моду и какие `Mod ID` используются сервером.

> Текущий порядок и набор подключённых модов см. в [`servertest.ini`](./servertest.ini). Статус ниже отражает текущий `Mods=` в этой ветке.

## Workshop ID → мод

| Workshop ID / каталог | Мод | Mod ID в сборке | Статус |
|---|---|---|---|
| [`3217685049`](./3217685049) | PZK VLC (Vanilla look-like car pack) | `PzkVanillaPlusCarPack`, `PZKCarzoneWorkshop` | Подключён |
| [`3268487204`](./3268487204) | [B42] Bandits NPC | `Bandits2` | Подключён |
| [`3304582091`](./3304582091) | Standardized Vehicle Upgrades 3 - Vanilla Addon | `StandardizedVehicleUpgrades3V` | Подключён |
| [`3402491515`](./3402491515) | Tsar's Common Library B42 | `tsarslib` | Подключён |
| [`3403490889`](./3403490889) | Standardized Vehicle Upgrades 3 - Core - B42 | `StandardizedVehicleUpgrades3Core` | Подключён |
| [`3403870858`](./3403870858) | Lifestyle: Hobbies | `LifestyleHobbies` | Подключён |
| [`3413150945`](./3413150945) | More Damaged Objects [42MP] | `MoreDamagedObjects` | Подключён |
| [`3464606086`](./3464606086) | [B42] HDCP Immersive Vehicle Paint | `ImmersiveVehiclePaint` | Подключён |
| [`3633421539`](./3633421539) | ModernFirearmsSystem 42.19 (SP/MP) | `ModernFirearmsSystem`, `BackpackSystemB42` *(Workshop также содержит `BladesmithSystemB42`)* | Подключён частично |
| [`3739256725`](./3739256725) | New Music [42+] | `NewMusic` | Подключён |
| [`3744973332`](./3744973332) | New Music [42+]: Russian Albums Collection | `RussianAlbumsNewMusic` | Подключён |
| [`3745718141`](./3745718141) | US Military Grenades [B42] | `Explosives` | Подключён |
| [`3750253491`](./3750253491) | Common Sense [B42.19+] | `VB_CommonSense` | Подключён |
| [`3766508989`](./3766508989) | Aegis Panel | `AP` | Подключён |
| [`3766693411`](./3766693411) | Federal Ranger's [Chimera] | `Federal_Rangers_Chimera` | Подключён |
| [`3774448621`](./3774448621) | Survival's Hauler | `SurvivalsHauler` | Подключён |
| [`3774826484`](./3774826484) | Jumbo Tree Indoor Fix | `JumboTreeIndoorFix` | Подключён |
| [`3775841600`](./3775841600) | Ladders?! [B42.20 Unofficial] | `Ladders4220` | Подключён |
| [`3779749594`](./3779749594) | zRe Vaccine 3.0 / B42.20 MP ReMod | `zReModVaccin30bykERHUS42S`, `zReModVaccin30bykERHUS42S_Addon` | Подключён |
| [`3780151182`](./3780151182) | MFS Community Fix | `MFS_community_fix` | Подключён |
| [`3780257415`](./3780257415) | zombieREengine / zRe Framework | `zReFRAMEWORK` | Подключён |
| [`3781229261`](./3781229261) | Physical Progression Overhaul | `PhysicalProgressionOverhaul` | Подключён |
| [`3781771367`](./3781771367) | Craftable Military Fences | `CraftableMilitaryFences` | Подключён |
| [`3781771737`](./3781771737) | Craftable Security Fences | `CraftableSecurityFences` | Подключён |
| [`3782313362`](./3782313362) | Grid Inventory | `GridInventory` | **Не подключён сейчас** |

## Локальный Compatibility Patch

Каталог [`WorkshopPatches`](./WorkshopPatches) и связанные файлы содержат исправления совместимости для Build 42.20.x и конфликтов между модами.

Сам патч подключается как несколько локальных `Mod ID`, которые **не являются Steam Workshop ID**:

- `LaccckaB4220PatchCore`
- `LaccckaB4220RuntimeFixes`
- `LaccckaB4220ActivityFixes`
- `LaccckaB4220CompatBridges`
- `LaccckaB4220SafetyFixes`
- `LaccckaB4220RussianText`

## Как читать структуру

Пример:

```text
3268487204/
└── mods/
    └── Bandits/
```

Здесь `3268487204` — Steam Workshop ID, а серверный `Mod ID` для текущей версии Bandits — `Bandits2`.

При диагностике проблем важно не путать:

- **Workshop ID** — числовой ID элемента Steam Workshop, используется в `WorkshopItems=`;
- **Mod ID** — внутренний идентификатор мода Project Zomboid, используется в `Mods=`;
- **каталог репозитория** — локальная копия конкретного Workshop item, обычно названная его Workshop ID.

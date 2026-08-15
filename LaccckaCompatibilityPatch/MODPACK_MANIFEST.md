# Lacccka B42.20 Frozen Modpack

Frozen Project Zomboid Build 42.20 mod collection assembled from the Workshop-ID snapshots stored in this repository.

- Workshop item: `3782987959`
- Generated: 2026-08-15
- Internal mod directories: 31
- All declared `require=` dependencies are included in the bundle.
- Canonical publishing contents: `LaccckaCompatibilityPatch/Contents/mods/`
- Mirrored Workshop snapshot: `3782987959/mods/`

## Server configuration

Use only the frozen pack in `WorkshopItems=`:

```ini
WorkshopItems=3782987959
```

The internal mods still need to be enabled through `Mods=`. Keep the compatibility patch last:

```ini
Mods=tsarslib;zReFRAMEWORK;zReModVaccin30bykERHUS42S;zReModVaccin30bykERHUS42S_Addon;BackpackSystemB42;Bandits2;BladesmithSystemB42;CraftableMilitaryFences;CraftableSecurityFences;Explosives;Federal_Rangers_Chimera;JumboTreeIndoorFix;Ladders4220;LifestyleHobbies;MoreDamagedObjects;NewMusic;RussianAlbumsNewMusic;OpenAllContainers;PhysicalProgressionOverhaul;VB_CommonSense;StandardizedVehicleUpgrades3Core;StandardizedVehicleUpgrades3V;ImmersiveVehiclePaint;PzkVanillaPlusCarPack;PZKCarzoneWorkshop;SurvivalsHauler;GridInventory;AP;ModernFirearmsSystem;MFS_community_fix;LaccckaB4220Compat
```

Do not keep the original Workshop IDs in `WorkshopItems=`, otherwise Steam can download newer upstream copies alongside this frozen bundle.

## Frozen sources

| Workshop ID | Directory | Internal mod ID |
|---:|---|---|
| 3217685049 | PZKCarzoneWorkshop | PZKCarzoneWorkshop |
| 3217685049 | PZKVanillaPlusCarPack | PzkVanillaPlusCarPack |
| 3268487204 | Bandits | Bandits2 |
| 3304582091 | StandardizedVehicleUpgrades3Vanilla | StandardizedVehicleUpgrades3V |
| 3402491515 | tsarslib | tsarslib |
| 3403490889 | StandardizedVehicleUpgrades3Core | StandardizedVehicleUpgrades3Core |
| 3403870858 | Lifestyle | LifestyleHobbies |
| 3413150945 | MoreDamagedObjects | MoreDamagedObjects |
| 3464606086 | HDCP_ImmersiveVehiclePaint | ImmersiveVehiclePaint |
| 3465040406 | OpenAllContainers | OpenAllContainers |
| 3633421539 | BackpackSystem | BackpackSystemB42 |
| 3633421539 | BladesmithSystem | BladesmithSystemB42 |
| 3633421539 | Escape from Kentucky4215 | ModernFirearmsSystem |
| 3739256725 | Talis New Music | NewMusic |
| 3744973332 | RussianAlbumsNewMusic | RussianAlbumsNewMusic |
| 3745718141 | Explosives | Explosives |
| 3750253491 | CommonSense | VB_CommonSense |
| 3766508989 | AP | AP |
| 3766693411 | Chimera | Federal_Rangers_Chimera |
| 3774448621 | Survivals.Hauler | SurvivalsHauler |
| 3774826484 | JumboTreeIndoorFix | JumboTreeIndoorFix |
| 3775841600 | Ladders4220 | Ladders4220 |
| 3779749594 | zReVaccin 3 addon | zReModVaccin30bykERHUS42S_Addon |
| 3779749594 | zReVaccin 3 | zReModVaccin30bykERHUS42S |
| 3780151182 | MFS_community_fix | MFS_community_fix |
| 3780257415 | zombieREengine | zReFRAMEWORK |
| 3781229261 | PhysicalProgressionOverhaul | PhysicalProgressionOverhaul |
| 3781771367 | CraftableMilitaryFences | CraftableMilitaryFences |
| 3781771737 | CraftableSecurityFences | CraftableSecurityFences |
| 3782313362 | GridInventory | GridInventory |
| 3782987959 | LaccckaCompatibilityPatch | LaccckaB4220Compat |

## Maintenance rule

Upstream Workshop updates no longer affect the server while only `3782987959` is installed. To update a component intentionally, replace its source snapshot in the repository, test it, then rebuild both bundle locations and publish a new revision of the same Workshop item.

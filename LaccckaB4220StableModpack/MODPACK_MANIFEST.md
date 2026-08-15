# Lacccka B42.20 Stable Modpack manifest

This new Workshop publication contains the same verified frozen contents as commit `90c63179c58351db35ec3cb7b861ce779e261ca6`.

- Source Workshop directories: 27
- Internal mod directories: 31
- New Workshop ID: assigned by Steam on first publication
- Publishing directory: `LaccckaB4220StableModpack`
- Compatibility patch internal ID: `LaccckaB4220Compat` (load last)

The count is 31 because three Workshop sources contain multiple internal mods:

- `3217685049`: 2 internal mods
- `3633421539`: 3 internal mods
- `3779749594`: 2 internal mods
- The other 24 Workshop sources: 1 internal mod each

All declared `require=` dependencies are present in the bundle.

## Recommended server configuration after publishing

```ini
WorkshopItems=<NEW_WORKSHOP_ID>
Mods=tsarslib;zReFRAMEWORK;zReModVaccin30bykERHUS42S;zReModVaccin30bykERHUS42S_Addon;BackpackSystemB42;Bandits2;BladesmithSystemB42;CraftableMilitaryFences;CraftableSecurityFences;Explosives;Federal_Rangers_Chimera;JumboTreeIndoorFix;Ladders4220;LifestyleHobbies;MoreDamagedObjects;NewMusic;RussianAlbumsNewMusic;OpenAllContainers;PhysicalProgressionOverhaul;VB_CommonSense;StandardizedVehicleUpgrades3Core;StandardizedVehicleUpgrades3V;ImmersiveVehiclePaint;PzkVanillaPlusCarPack;PZKCarzoneWorkshop;SurvivalsHauler;GridInventory;AP;ModernFirearmsSystem;MFS_community_fix;LaccckaB4220Compat
```

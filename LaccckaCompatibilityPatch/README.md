# Lacccka B42.20 Compatibility Patch

Compatibility fixes for the Project Zomboid Build 42.20.2 multiplayer mod set audited in Lacccka/zomboid.

Workshop ID: `3782987959`

This is a standalone compatibility patch. It does not bundle or replace subscribed Workshop mods.

## Fixed integrations

- Modern Firearms System dedicated-server cursor crash.
- SVU3 / Tsar's Common Library legacy module alias.
- PZK VLC module aliases.
- zombie RE engine vanilla BodyLocations alias.
- Bandits shared/client farming load phase.
- Lifestyle hygiene load phase and west-side bath placement.
- Aegis Panel nil source-container transaction.
- Federal Ranger's Chimera three-state Ghillie Suit menu.
- Bandits and PZK B42.20 UI module-path aliases.
- B42.20 recipe-magazine callback rename.

## Russian translation

The patch provides the Russian Bandits translation from:

`media/lua/shared/Translate/RU/IG_UI_ru.json`

Keep the original Bandits mod enabled. Because `LaccckaB4220Compat` loads after `Bandits2`, Project Zomboid reads the translation from this patch without modifying the original Workshop mod.

## Version 1.0.2

- Converted the Workshop item back from a frozen modpack to a standalone compatibility patch.
- Removed bundled copies of third-party mods.
- Added the Russian Bandits 42.20 translation to the patch.
- Preserved all compatibility fixes from version 1.0.1.

## Server order

Add `3782987959` to `WorkshopItems=`. Add `LaccckaB4220Compat` as the last entry in `Mods=`.

The original dependency mods, including `Bandits2`, must remain separately subscribed and enabled.

Remove the obsolete `Lifestyle4220Compat` mod from both lists.

## Test checklist

1. Start a dedicated server and confirm there is no `ISPlace3DItemCursor_Fix.lua:1` exception.
2. Confirm the previously recorded failed requires are gone.
3. Confirm there are no failed requires for `ISUI/ISCharacterScreen`, either legacy `ISVehiclePartMenu` path, or `ISBaseTimedAction`.
4. Confirm there is no missing `SpecialLootSpawns.OnCreateRecipeMagazine` callback.
5. Right-click all three Ghillie Suit states and switch between them.
6. Transfer and drop items using GridInventory, including vehicle and Aegis-modified containers.
7. Enter baths from east and west sides.
8. Switch the game language to Russian and confirm Bandits speech/UI strings are translated.

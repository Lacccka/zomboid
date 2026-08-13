# Lacccka B42.20 Compatibility Patch

Compatibility fixes for the Project Zomboid Build 42.20.2 multiplayer mod set audited in Lacccka/zomboid.

This is a separate mod. It does not modify subscribed Workshop content.

## Fixed integrations

- Modern Firearms System dedicated-server cursor crash.
- SVU3 / Tsar's Common Library legacy module alias.
- PZK VLC module aliases.
- zombie RE engine vanilla BodyLocations alias.
- Bandits shared/client farming load phase.
- Lifestyle hygiene load phase and west-side bath placement.
- Aegis Panel nil source-container transaction.
- Federal Ranger's Chimera three-state Ghillie Suit menu.

## Server order

Add the published Workshop ID to WorkshopItems=. Add LaccckaB4220Compat as the last entry in Mods=.

Remove the obsolete Lifestyle4220Compat mod from both lists.

## Test checklist

1. Start a dedicated server and confirm there is no ISPlace3DItemCursor_Fix.lua:1 exception.
2. Confirm the previously recorded failed requires are gone.
3. Right-click all three Ghillie Suit states and switch between them.
4. Transfer and drop items using GridInventory, including vehicle and Aegis-modified containers.
5. Enter baths from east and west sides.


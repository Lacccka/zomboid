# Bandits reconnect clothing PoC

## Reproduced defect

B42.20 multiplayer testing reproduced a strong session-boundary failure:

- persistent Bandits surviving a disconnect/reconnect retain `brain.clothing`;
- their `ItemVisuals` still contain the expected clothing definitions;
- their real `WornItems` collapse to zero or nearly zero;
- fresh Bandits created in the current session can still have a complete real worn state;
- the later corpse clothing failure therefore reflects an already broken live-character state rather than being caused primarily by `OnZombieDead` cleanup.

The upstream implementation explains the boundary. `Bandit.ApplyVisuals()` clears `bandit:getWornItems()` and rebuilds clothing as standalone `ItemVisual` records. The clothing block in `Bandit.UpdateItemsToSpawnAtDeath()` is commented out.

## Current PoC

`WorkshopPatches/Bandits-LCC-Dev/42.20/media/lua/client/zz_LCC_BanditClothingRestore.lua` wraps `Bandit.ApplyVisuals()` and, after upstream visual processing, materializes only missing `brain.clothing` slots as real `InventoryItem` objects attached through `bandit:setWornItem(location, item)`.

The PoC intentionally does **not** call `inventory:AddItem()` and does not add these objects to the upstream death-spawn queue. This matches the observed vanilla zombie model where real worn items can exist without appearing in the character's main inventory, and reduces the risk of creating duplicate corpse clothing through both WornItems and `addItemToSpawnAtDeath()`.

The same item object is cached per live Bandit and re-worn after subsequent upstream `ApplyVisuals()` calls clear WornItems again; it is not recreated every tick.

A missing bag is also restored through the item's `canBeEquipped()` body location when that slot is free.

## Runtime markers

A correct load prints:

```text
[LCC][BanditsClothingPoC][BOOT] marker=real-worn-reconnect-v1 mode=materialize-missing-real-worn inventoryAdd=false
```

When a persistent Bandit is repaired for the first time in the current client session:

```text
[LCC][BanditsClothingPoC][RESTORE] marker=real-worn-reconnect-v1 id=... beforeWorn=... expectedClothing=... restored=... created=... afterWorn=... bag=...
```

If an expected body location is already occupied by a different real item, the PoC does not overwrite it and reports:

```text
[LCC][BanditsClothingPoC][SLOT_CONFLICT] ... intervention=false
```

## Test matrix

1. Start with several existing Bandits that survived the previous server session.
2. Join the server and confirm they are visibly clothed rather than stripping after load.
3. Confirm `[RESTORE]` records have `afterWorn` close to `expectedClothing` (plus a bag where applicable).
4. Kill several restored persistent Bandits and inspect `BanditsDeathLoot` diagnostics.
5. Verify corpse clothing is present and no duplicate clothing types appear.
6. Spawn fresh Bandits in the same session and verify they remain visually correct and are not unnecessarily replaced.
7. Disconnect and reconnect again without recreating the Bandits; confirm the same survivors recover their real worn state again.

## Success criteria

A strong result requires:

- old Bandits no longer visibly strip after reconnect;
- their live `WornItems` are restored from `brain.clothing`;
- fresh Bandits remain correct;
- corpse `worn` counts track expected clothing after killing restored NPCs;
- no duplicate clothing appears in corpse inventory/worn slots;
- no slot-conflict spam or Lua/Java exceptions are introduced.

This remains an experimental working-copy change. It should not be promoted to a public compatibility fix until reconnect and corpse-duplication tests pass.

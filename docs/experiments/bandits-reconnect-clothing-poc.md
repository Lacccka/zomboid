# Bandits reconnect clothing PoC

## Reproduced defect

B42.20 multiplayer testing reproduced a strong session-boundary failure:

- persistent Bandits surviving a disconnect/reconnect retain `brain.clothing`;
- their `ItemVisuals` still contain the expected clothing definitions;
- their real `WornItems` collapse to zero or nearly zero;
- fresh Bandits created in the current session can still have a complete real worn state;
- the later corpse clothing failure therefore reflects an already broken live-character state rather than being caused primarily by `OnZombieDead` cleanup.

The upstream implementation explains the boundary. `Bandit.ApplyVisuals()` clears `bandit:getWornItems()` and rebuilds clothing as standalone `ItemVisual` records. The clothing block in `Bandit.UpdateItemsToSpawnAtDeath()` is commented out.

## Rejected v1

The first `real-worn-reconnect-v1` experiment incorrectly passed the string keys from `brain.clothing` (for example `Hat`, `Jacket`) directly to B42 `getWornItem()` / `setWornItem()`. B42 expects an `ItemBodyLocation` object. The first runtime test therefore produced a repeated Java/Kahlua error:

```text
expected argument of type ItemBodyLocation, got String
```

This was an LCC PoC bug, not an upstream Bandits failure.

## Current PoC: real-worn-reconnect-v2

`WorkshopPatches/Bandits-LCC-Dev/42.20/media/lua/client/zz_LCC_BanditClothingRestore.lua` still wraps `Bandit.ApplyVisuals()`, but the string key from `brain.clothing` is now used only for metadata lookup, tint lookup, caching and logging.

For every instantiated clothing item, v2 obtains the B42 typed slot from:

```lua
item:getBodyLocation()
```

Only that `ItemBodyLocation` object is passed to:

```lua
bandit:getWornItem(location)
bandit:setWornItem(location, item)
```

The same rule is used for bags. There is no `canBeEquipped()` string fallback.

All B42 worn-item API calls and the complete restore pass are protected. If a body location is unavailable or an API call fails, the PoC skips that item and logs a bounded diagnostic instead of breaking `Bandit.ApplyVisuals()` / `OnZombieUpdate` repeatedly.

The PoC intentionally does **not** call `inventory:AddItem()` and does not add restored clothing to the upstream death-spawn queue. The same real item object is cached per live Bandit and re-worn after a later upstream `ApplyVisuals()` clears WornItems again.

## Runtime markers

A correct v2 load prints:

```text
[LCC][BanditsClothingPoC][BOOT] marker=real-worn-reconnect-v2 mode=typed-ItemBodyLocation inventoryAdd=false
```

A successful repair prints:

```text
[LCC][BanditsClothingPoC][RESTORE] marker=real-worn-reconnect-v2 id=... beforeWorn=... expectedClothing=... restored=... created=... afterWorn=... bag=...
```

Bounded failure diagnostics include:

```text
[LCC][BanditsClothingPoC][BODY_LOCATION_MISSING] ...
[LCC][BanditsClothingPoC][BAG_LOCATION_UNAVAILABLE] ...
[LCC][BanditsClothingPoC][BODY_LOCATION_API_ERROR] ...
[LCC][BanditsClothingPoC][RESTORE_ERROR] ...
[LCC][BanditsClothingPoC][SLOT_CONFLICT] ...
```

## Test matrix

1. Start with several existing Bandits that survived the previous server session.
2. Join the server and confirm the old `ItemBodyLocation, got String` exception no longer appears.
3. Confirm old Bandits are visibly clothed rather than stripping after load.
4. Confirm `[RESTORE]` records have `afterWorn` close to `expectedClothing` (plus a bag where applicable).
5. Kill several restored persistent Bandits and inspect `BanditsDeathLoot` diagnostics.
6. Verify corpse clothing is present and no duplicate clothing types appear.
7. Spawn fresh Bandits in the same session and verify they remain visually correct.
8. Disconnect and reconnect again without recreating the Bandits; confirm the same survivors recover their real worn state again.

## Success criteria

A strong result requires:

- zero `expected argument of type ItemBodyLocation, got String` errors from the PoC;
- old Bandits no longer visibly strip after reconnect;
- their live `WornItems` are restored from `brain.clothing`;
- fresh Bandits remain correct;
- corpse `worn` counts track expected clothing after killing restored NPCs;
- no duplicate clothing appears in corpse inventory/worn slots;
- no repeated slot/API/restore error spam is introduced.

This remains an experimental working-copy change. It should not be promoted to a public compatibility fix until reconnect and corpse-duplication tests pass.

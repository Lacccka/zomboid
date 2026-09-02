# Lacccka B42 Runtime Fixes — runtime/dedicated contract

This Workshop project contains LCC-authored low-level runtime compatibility hooks only. It must not contain complete third-party Lua files or assets.

Validated NPC combat, terminal-death and corpse/clothing behavior now belongs to the separate stable `NPCFixes` item. Diagnostics and admin stress tooling remain in `NPCCombatExperimental`.

Validated against the repository snapshot at `3268487204/mods/Bandits/42.20` (`Mod ID: Bandits2`) and the B42.20 dedicated/client failures observed by this server project. The MFS attachment overlay was statically rebased on 2026-09-02 against repository commit `02462abb9a87b9d1ed58661e626a82559d5afae8` (`ModernFirearmsSystem`).

## 1. Squareless/despawned zombie lifecycle

Upstream Bandits caches `IsoZombie` references and B42.20 MP can transiently deliver an object after its square is gone.

`zzz_LCC_BanditsZombieCacheGuard.lua` uses two layers: an existing upstream early-return seam for squareless objects, then post-update/post-flush removal from Bandits caches.

## 2. Farming actions

`zzz_LCC_BanditsFarmingGuard.lua` wraps only the affected callbacks and keeps upstream actions authoritative. Invalid/transient watering or stomp states complete cleanly instead of repeatedly throwing or leaving a stuck task.

## 3. Dedicated `BanditZombie.GetInstanceById`

`zzz_LCC_BanditsDedicatedServerGuard.lua` maintains an O(1) registry containing only live Bandits rather than starting Bandits' disabled complete `getZombieList()` scan. Existing Bandit lifecycle seams register/purge references; stale entries are pruned safely.

## 4. Legacy character-screen path

`client/ISUI/ISCharacterScreen.lua` is a tiny path shim redirecting the old module name to B42.20 `XpSystem/ISUI/ISCharacterScreen`. It contains no upstream character-screen source.

## 5. Modern Firearms System occupied-slot replacement guard

`client/MFSAttachmentAccessFix.lua` is an LCC-authored overlay for `ModernFirearmsSystem` plus `MFS_community_fix`; it does not modify or bundle either upstream mod.

As of the 2026-09-02 MFS import, upstream now owns attachment discovery and rendering. Its `getReachableContainers()` + recursive `scanParts()` path covers the player's inventory, nested/equipped containers and nearby reachable world containers, and its `addAttachmentButton` transfers a selected non-root part through vanilla `ISInventoryTransferAction`. RuntimeFixes therefore no longer replaces `selectAttachmentPane:renderInventory()`, `selectAttachmentPane:update()` or the new magazine-selection code.

The overlay now fills only the remaining occupied-slot UX/safety gap for normal `WeaponPart` slots:

- LMB opens the upstream compatible-part selector even when a slot is already occupied;
- selecting a part from a non-root container first validates that the original source is still reachable and then uses vanilla inventory transfer;
- a zero-time bridge action uses a CAS-style snapshot of the occupied part ID, so a stale selector can never remove a different attachment that was installed after the click;
- only after the selected part is present in the root inventory does the queue run upstream `ISRemoveWeaponUpgrade` followed by upstream `ISUpgradeWeapon` and `ISEquipWeaponAction`;
- RMB explicitly removes an installed normal attachment, while double-LMB removal stays disabled;
- magazine, attack-mode, skin, 3D-positioning and selector rendering remain fully upstream-authoritative.

This arrangement intentionally preserves the MFS 2026-09-02 attachment-state/model/MP refresh added to `ISUpgradeWeapon:perform()` instead of reimplementing it in the patch.

The runtime patch remains soft: `RuntimeFixes` is usable without MFS. `loadModAfter` only establishes ordering when `ModernFirearmsSystem` and/or `MFS_community_fix` are present.

## Boundary with NPCFixes

Do not add combat targeting, AttackState workarounds, terminal `Die` processing, corpse clothing materialization or fake-hit relation cleanup here. Those are validated NPC behavior fixes and belong to `NPCFixes`.

`RuntimeFixes` must remain useful when `NPCFixes` is not enabled, and `NPCFixes` may soft-load after this item when both are installed.

## Regression checklist

Test with the original upstream mods and split patch items; `Bandits-LCC-Dev` must not be copied into the game for a stable regression test.

- devirtualize populated areas; no squareless `getSquare()` crash or persistent stale Bandit cache growth;
- exercise valid/invalid NPC farming actions; valid actions remain authoritative and transient invalid states complete cleanly;
- exercise server paths needing `BanditZombie.GetInstanceById()`; no missing-API exception and no complete-zombie-list fallback scan;
- with MFS + `MFS_community_fix`, verify the unmodified upstream selector still discovers attachments in the root inventory, equipped/nested bags, nearby built containers and nearby dropped bags;
- verify an occupied normal attachment slot opens the selector on LMB, selecting a replacement removes the old part and installs the selected part, RMB removes, and double-LMB does not remove;
- verify magazine/drum selection by shared ammo type, attack-mode controls, skin controls and 3D attachment-position sliders still use the updated upstream behavior;
- move away from a world container after opening the selector and ensure a stale selection neither removes the installed attachment nor mutates the weapon;
- in MP, verify transfer -> remove -> install ordering and that the updated upstream attachment-state/hand-model/inspect-preview refresh executes after installation;
- grouped audit must reject reintroduced complete third-party source under RuntimeFixes.

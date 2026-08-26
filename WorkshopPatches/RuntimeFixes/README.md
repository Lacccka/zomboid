# Lacccka B42 Runtime Fixes — runtime/dedicated contract

This Workshop project contains LCC-authored low-level runtime compatibility hooks only. It must not contain complete third-party Lua files or assets.

Validated NPC combat, terminal-death and corpse/clothing behavior now belongs to the separate stable `NPCFixes` item. Diagnostics and admin stress tooling remain in `NPCCombatExperimental`.

Validated against the repository snapshot at `3268487204/mods/Bandits/42.20` (`Mod ID: Bandits2`) and the B42.20 dedicated/client failures observed by this server project.

## 1. Squareless/despawned zombie lifecycle

Upstream Bandits caches `IsoZombie` references and B42.20 MP can transiently deliver an object after its square is gone.

`zzz_LCC_BanditsZombieCacheGuard.lua` uses two layers: an existing upstream early-return seam for squareless objects, then post-update/post-flush removal from Bandits caches.

## 2. Farming actions

`zzz_LCC_BanditsFarmingGuard.lua` wraps only the affected callbacks and keeps upstream actions authoritative. Invalid/transient watering or stomp states complete cleanly instead of repeatedly throwing or leaving a stuck task.

## 3. Dedicated `BanditZombie.GetInstanceById`

`zzz_LCC_BanditsDedicatedServerGuard.lua` maintains an O(1) registry containing only live Bandits rather than starting Bandits' disabled complete `getZombieList()` scan. Existing Bandit lifecycle seams register/purge references; stale entries are pruned safely.

## 4. Legacy character-screen path

`client/ISUI/ISCharacterScreen.lua` is a tiny path shim redirecting the old module name to B42.20 `XpSystem/ISUI/ISCharacterScreen`. It contains no upstream character-screen source.

## 5. Modern Firearms System attachment access and slot UX

`client/MFSAttachmentAccessFix.lua` is an LCC-authored overlay for `ModernFirearmsSystem` plus `MFS_community_fix`; it does not modify or bundle either upstream mod.

It extends the weapon-inspection attachment selector to compatible parts in the player's main inventory, equipped bags and vanilla-accessible nearby loot containers. Non-root parts are first moved through vanilla `ISInventoryTransferAction`, preserving B42.20 multiplayer item transactions and access validation before any weapon mutation is queued.

For normal weapon-part slots, LMB always opens the compatible-part selector even when the slot is occupied, selecting another part queues replacement, RMB removes the installed part, and double-LMB removal is disabled. Magazine, attack-mode and skin controls retain their upstream behavior.

The runtime patch is deliberately soft: `RuntimeFixes` remains usable without MFS. `loadModAfter` only establishes ordering when `ModernFirearmsSystem` and/or `MFS_community_fix` are present.

## Boundary with NPCFixes

Do not add combat targeting, AttackState workarounds, terminal `Die` processing, corpse clothing materialization or fake-hit relation cleanup here. Those are validated NPC behavior fixes and belong to `NPCFixes`.

`RuntimeFixes` must remain useful when `NPCFixes` is not enabled, and `NPCFixes` may soft-load after this item when both are installed.

## Regression checklist

Test with the original upstream mods and split patch items; `Bandits-LCC-Dev` must not be copied into the game for a stable regression test.

- devirtualize populated areas; no squareless `getSquare()` crash or persistent stale Bandit cache growth;
- exercise valid/invalid NPC farming actions; valid actions remain authoritative and transient invalid states complete cleanly;
- exercise server paths needing `BanditZombie.GetInstanceById()`; no missing-API exception and no complete-zombie-list fallback scan;
- with MFS + `MFS_community_fix`, verify attachment discovery from equipped bags and reachable nearby containers, occupied-slot LMB replacement, RMB removal and no double-LMB removal;
- move away from a world container after opening the selector and ensure a stale selection cannot remove the currently installed attachment;
- grouped audit must reject reintroduced complete third-party source under RuntimeFixes.

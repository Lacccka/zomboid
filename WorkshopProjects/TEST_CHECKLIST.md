# Split-package test checklist

The monolithic `LaccckaCompatibilityPatch` remains the source/regression package. Do not enable it together with an equivalent split runtime project during A/B tests because duplicate hooks/legacy shims can hide load-order mistakes.

## Static checks

1. Run `python3 tools/audit_workshop_split.py`.
2. Run `python3 tools/audit_wellness_proxy.py`.
3. Run `python3 tools/audit_survivor_ai_guards.py`.
4. Run the existing compatibility-contract and translation audits.
5. Confirm all **12 READY projects** have unique Mod IDs, active `workshop.txt`, empty Workshop `id=`, and `visibility=unlisted` before first upload.
6. Confirm no READY project has `workshop.txt.DISABLED`.
7. Confirm the deprecated `LCCB4220ThirdPartyRU` project does not exist.
8. Confirm the Survivor AI runtime split contains none of: `BanditZombie.lua`, `BanditUpdate.lua`, `BanditServerWanderers.lua`, `ZAStompPlant.lua`, `ZAWaterFarm.lua`.
9. Confirm the cache/update, farming and empty-server guards exist; the empty-server guard must not require `isClient()`.
10. Confirm the cache/update guard wraps the installed global compatibility predicate and preserves the original predicate for normal zombies.
11. Confirm Wellness `media/perks.txt` declares exactly one perk: `Yoga`, `parent = Lifestyle`, with `xp1..xp10 = 0`.
12. Confirm `zzy_LCC_LifestyleYogaContract.lua` exists and validates `Perks.Yoga:getParent()` against `Lifestyle`.
13. Confirm public `title=` values remain target-neutral while direct compatibility descriptions contain target/author credits.
14. Confirm `LCCB4220BanditsRU/.../IG_UI.json` matches the isolated target-side RU `IG_UI` snapshot byte-for-byte.
15. Confirm `LCCB4220LifestyleRU/42/.../RU/IG_UI.json` does not exist; survivor dialogue remains isolated in its own translation item.

## Runtime smoke-test setup

1. Start from the same server/mod set that currently works with `LaccckaB4220Compat`.
2. Remove `LaccckaB4220Compat` from `Mods=` for the split runtime test.
3. Add only the split projects being tested, after their original dependencies.
4. Keep server/world settings unchanged for the first comparison.
5. Compare startup Lua errors and `[LCC][Guard]` diagnostics with the monolithic baseline.
6. Join from a client and test the exact feature owned by each enabled module.
7. Keep every Workshop item unlisted until its specific smoke test passes.

## General runtime checks

- Firearms Placement Bridge: no dedicated-server cursor error; ranged weapon part render state remains correct on client.
- Vehicle API Bridge: legacy `ATA2Tuning2` require resolves to the current implementation.
- Vaccine API Bridge: old root `BodyLocations` import resolves without carrying a copied implementation.
- Inventory Safety Guard: invalid transfer actions fail safely; valid actions still call the installed original validator.
- Legacy Callback Bridge: old recipe-magazine callback delegates only when the legacy callback is absent.
- RU Skill Descriptions: vanilla/B42 skill descriptions display in Russian without supplying wellness/hobby descriptions.

## Survivor AI Stability

- Connect a client, move through a dense zombie/NPC area and confirm no stale/squareless cache failure returns.
- Force or observe a despawn/devirtualization transition and confirm a zombie with no square exits the installed update consumer before combat code, then disappears from LCC-observed caches.
- Confirm normal zombies still call the installed `BanditCompatibility.IsReanimatedForGrappleOnly` predicate and continue through normal update behavior.
- Wait through at least one `EveryOneMinute` cache rebuild and confirm the post-flush sweep does not break cache counts.
- Exercise watering/stomping tasks and verify transient/missing Farming states fail safely while normal states still call installed callbacks.
- With a player online, wait through an `EveryTenMinutes` tick and confirm normal wanderer scheduling sees the real clan table.
- Disconnect all players, leave the dedicated server empty through an `EveryTenMinutes` tick, and confirm the previous nil-day comparison does not return.
- Reconnect and confirm the real clan list/wanderer scheduling is restored immediately.
- Treat any unexpected `[LCC][Guard][DISABLED][bandits.*]` line as a regression.

## Wellness Compatibility

- Confirm the wellness/hobby dependency loads before `LCCB4220WellnessCompat` and the `Lifestyle` parent perk exists when the LCC `Yoga` proxy is resolved.
- Confirm startup/client log contains `[LCC][Wellness] Yoga CustomPerk contract OK: parent=Lifestyle`.
- Treat `[LCC][Guard][DISABLED][lifestyle.yoga-progress-ui]` as a failed Yoga contract test.
- Confirm the Skills panel shows Yoga without duplicating/replacing Lifestyle, Art, Cleaning, Dancing, Meditation, or Music.
- Confirm Yoga level/progress match `HiddenSkills` across reconnect/reload and no vanilla XP is awarded to the proxy.
- Confirm level 10, disabled-Yoga sandbox configuration, and tooltip rendering behave correctly.
- Test bathtub approach from east and west; unrelated cases must delegate to the installed original behavior.
- Test shower/bathtub shared/server loading and confirm placeholders do not override real client functions.

## Vehicle Integration Bridge

- Exercise vehicle part menus, water-tank integration, zones and vehicle-upgrade support with original dependencies installed separately.
- Confirm legacy requires resolve to current B42.20 modules without duplicate module/state creation.
- Temporarily test without the optional support module and confirm the split bridge fails soft through `LCCGuard` rather than aborting the entire compatibility item.

## Outfit Menu Safety

- Test both affected ghillie types and confirm extra-menu arrays normalize correctly.
- Test unrelated clothing items and verify the wrapper delegates without changing their context menus.

## Translation Workshop items

### Survivor Dialogue RU

- Enable `LCCB4220BanditsRU` after its original dependency.
- Confirm representative NPC speech/UI strings render in Russian and JSON loads without errors.
- Confirm runtime stability fixes are not required for the translation item itself.
- Keep it unlisted until the translation smoke test passes.

### Wellness & Hobbies RU

- Enable `LCCB4220LifestyleRU` after its original dependency.
- Run the existing Lifestyle translation coverage/placeholder audit.
- Check skill descriptions, tooltips, moodles, context menus, recipes, sandbox/UI strings and representative moveables in game.
- Confirm Survivor Dialogue strings are not supplied by this item.
- Keep it unlisted until the translation smoke test passes.

## Workshop rollout

After a project passes its test, assign its real Workshop ID and record it before changing server `WorkshopItems=` / `Mods=`. Replace the monolithic patch incrementally rather than switching every split module at once.

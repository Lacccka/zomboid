# Split-package test checklist

The monolithic `LaccckaCompatibilityPatch` remains the source/regression package. Do not enable it together with an equivalent split runtime project during A/B tests because duplicate hooks/legacy shims can hide load-order mistakes.

## Static checks

1. Run `python3 tools/audit_workshop_split.py`.
2. Run the existing compatibility-contract and Lifestyle translation audits.
3. Confirm all READY runtime projects have unique Mod IDs, active `workshop.txt`, empty Workshop `id=`, and `visibility=unlisted` before first upload.
4. Confirm `LCCB4220BanditsRU` and `LCCB4220ThirdPartyRU` have `workshop.txt.DISABLED` only.
5. Confirm the Bandits runtime split contains none of: `BanditZombie.lua`, `BanditServerWanderers.lua`, `ZAStompPlant.lua`, `ZAWaterFarm.lua`.
6. Confirm the Bandits cache/farming/empty-server guards exist.
7. Confirm Wellness `media/perks.txt` declares exactly one perk: `Yoga`, with `xp1..xp10 = 0`.
8. Confirm Workshop descriptions for Bandits/Lifestyle/PZK/Chimera name and credit their targets and state that original content is not bundled.
9. Confirm `LCCB4220BanditsRU/.../IG_UI.json` matches `Bandits/.../RU/IG_UI_ru.json` byte-for-byte and that the old mixed `LCCB4220ThirdPartyRU/.../IG_UI.json` no longer exists.

## Runtime smoke test

1. Start from the same server/mod set that currently works with `LaccckaB4220Compat`.
2. Remove `LaccckaB4220Compat` from `Mods=` for the split test.
3. Add only the split runtime projects being tested, after their original dependencies.
4. Keep server/world settings unchanged for the first comparison.
5. Compare startup Lua errors and `[LCC][Guard]` diagnostics with the monolithic baseline.
6. Join from a client and test the exact feature owned by each enabled module.
7. Keep every new runtime Workshop item unlisted until its feature checks pass.

## General feature checks

- Firearms bridge: no dedicated-server cursor error; ranged weapon part render state remains correct on client.
- SVU/Tsar bridge: legacy `ATA2Tuning2` require resolves to the B42 implementation.
- zRe bridge: old root `BodyLocations` import resolves without carrying a copied vanilla implementation.
- Aegis guard: invalid transfer actions fail safely; valid actions still call the original validator.
- Legacy callback bridge: old recipe-magazine callback delegates only when the old callback is absent.
- RU skill descriptions: vanilla/B42 skill descriptions display in Russian; no Lifestyle names/descriptions are supplied by this project.

## Bandits runtime split

- Connect a client, move through a dense zombie/bandit area and confirm no stale/squareless cache failure returns.
- Wait through at least one `EveryOneMinute` cache rebuild and confirm the post-flush sweep does not break cache counts.
- Exercise watering/stomping tasks and verify transient/missing Farming states fail safely while normal states still call installed Bandits callbacks.
- With a player online, wait through an `EveryTenMinutes` tick and confirm normal wanderer scheduling sees the real clan table.
- Disconnect all players, leave the dedicated server empty through an `EveryTenMinutes` tick, and confirm the previous nil-day comparison does not return.
- Reconnect and confirm the real clan list/wanderer scheduling is restored immediately.

## Lifestyle runtime split

- Confirm `LifestyleHobbies` loads before `LCCB4220WellnessCompat` and the `Lifestyle` parent perk exists when the LCC `Yoga` proxy is registered.
- Confirm the Skills panel shows Yoga without duplicating/replacing Lifestyle, Art, Cleaning, Dancing, Meditation, or Music.
- Confirm Yoga level/progress match Lifestyle `HiddenSkills` across reconnect/reload and no vanilla XP is awarded to the proxy.
- Confirm level 10, disabled-Yoga sandbox configuration, and tooltip rendering behave correctly.
- Test bathtub approach from east and west; all unrelated cases must delegate to Lifestyle.
- Test shower/bathtub shared/server loading to confirm placeholders do not override real client functions.

## PZK/SVU runtime split

- Exercise vehicle part menus, water-tank integration, PZK zones and SVU support with original dependencies installed separately.
- Confirm legacy requires resolve to current B42.20 modules without duplicate module/state creation.

## Chimera runtime split

- Test both affected ghillie types and confirm extra-menu arrays normalize correctly.
- Test unrelated clothing items and verify the wrapper delegates without changing their context menus.

## Translation boundary

- `LCCB4220BanditsRU` stays upload-disabled even though provenance is exact; resolve translation publication permission/rights separately.
- `LCCB4220ThirdPartyRU` stays upload-disabled until remaining categories/keys are classified against Lifestyle and any other origin.
- Do not use either translation staging project as a dependency for source-clean runtime fixes.

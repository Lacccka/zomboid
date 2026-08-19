# Split-package test checklist

The monolithic `LaccckaCompatibilityPatch` remains the source/reference package. Do not enable it together with an equivalent split project during A/B tests because duplicate hooks/full overrides can hide load-order mistakes.

## Static checks

1. Run `python3 tools/audit_workshop_split.py`.
2. Run the existing compatibility-contract and Lifestyle translation audits.
3. Confirm each READY project has a unique Mod ID and an active `workshop.txt` with an empty Workshop `id=` before first upload.
4. Confirm each BLOCKED project has `workshop.txt.DISABLED` only.
5. Confirm the Bandits split does **not** contain `BanditZombie.lua`, `ZAStompPlant.lua` or `ZAWaterFarm.lua`.
6. Confirm `BanditServerWanderers.lua` is still marked as the sole Bandits full-file blocker.

## Runtime smoke test

1. Start from the same server/mod set that currently works with `LaccckaB4220Compat`.
2. Remove `LaccckaB4220Compat` from `Mods=` for the split test.
3. Add only the split projects being tested, after their target mods.
4. Keep server/world settings unchanged for the first comparison.
5. Compare startup Lua errors and `[LCC][Guard]` diagnostics with the monolithic baseline.
6. Join from a client and test the exact feature owned by each enabled module.
7. Do not assign Workshop IDs to permission-blocked projects.

## Feature checks

- Firearms bridge: no dedicated-server cursor error; ranged weapon part render state remains correct on client.
- SVU/Tsar bridge: legacy `ATA2Tuning2` require resolves to the B42 implementation.
- zRe bridge: old root `BodyLocations` import resolves without carrying a copied vanilla implementation.
- Aegis guard: invalid transfer actions fail safely; valid actions still call the original validator.
- Legacy callback bridge: old recipe-magazine callback delegates only when the old callback is absent.
- RU skill descriptions: vanilla/B42 skill descriptions display in Russian; no Lifestyle names/descriptions are supplied by this project.

### Bandits split-only refactor

- Connect a client, move through a dense zombie/bandit area and confirm no stale/squareless cache failure returns in combat/update code.
- Wait through at least one `EveryOneMinute` cache rebuild and confirm the post-flush sweep does not break Bandit/Zombie cache counts.
- Exercise Bandits watering/stomping tasks and verify absent/transient `CFarmingSystem.instance` states return safely while normal Farming states still execute the original Bandits callbacks.
- Leave the dedicated server empty through an `EveryTenMinutes` tick and verify the remaining `BanditServerWanderers.lua` override still prevents the `day` nil comparison.
- Review `[LCC][Guard][bandits.*]` diagnostics for unexpected disabled features.

Permission-blocked modules should be tested locally from `Contents/mods` only until their publication status is resolved.

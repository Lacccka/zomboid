# LCC Workshop Projects

This directory is a publication-oriented split of `LaccckaCompatibilityPatch` for Project Zomboid Build 42.20.x.

The monolithic patch remains the development/regression baseline. Runtime fixes and localization are separated into focused Workshop projects so each item can be tested and updated independently.

## Packaging model

All 12 split projects are **READY_FOR_UNLISTED_TEST**.

- Public Workshop titles are neutral LCC functional names and do not contain target-mod names.
- When a project directly works with another mod, its Workshop **description** still names/credits that compatibility target and requires it separately.
- Runtime projects contain LCC-authored hooks, wrappers, guards, path/API shims, or vanilla/B42 localization; they do not repack target Lua/assets.
- Translation projects are separate Workshop items and remain independent from runtime compatibility projects.
- Every new item stays `visibility=unlisted` with an empty Workshop `id=` until its smoke test passes.

## READY_FOR_UNLISTED_TEST

- `LCCB4220FirearmsBridge` — **LCC B42.20 Firearms Placement Bridge**; 3D placement compatibility wrapper.
- `LCCB4220SVUTsarBridge` — **LCC B42.20 Vehicle API Bridge**; legacy vehicle API/path bridge.
- `LCCB4220zReBridge` — **LCC B42.20 Vaccine API Bridge**; legacy body-location API bridge.
- `LCCB4220AegisGuard` — **LCC B42.20 Inventory Safety Guard**; inventory-transfer validation guard.
- `LCCB4220LegacyCallbacks` — generic Build 42 legacy callback bridge.
- `LCCB4220SkillDescriptionsRU` — LCC-authored vanilla/Build 42 Russian skill descriptions.
- `LCCB4220SurvivorAIStability` — **LCC B42.20 Survivor AI Stability**; source-clean NPC/survivor runtime guards.
- `LCCB4220WellnessCompat` — **LCC B42.20 Wellness Compatibility**; bath/shower/Yoga compatibility.
- `LCCB4220PZKBridge` — **LCC B42.20 Vehicle Integration Bridge**; legacy vehicle integration path/API shims.
- `LCCB4220OutfitMenuSafety` — **LCC B42.20 Outfit Menu Safety**; clothing extra-menu normalization wrapper.
- `LCCB4220BanditsRU` — **LCC B42.20 Survivor Dialogue RU**; standalone Russian survivor/NPC dialogue localization.
- `LCCB4220LifestyleRU` — **LCC B42.20 Wellness & Hobbies RU**; standalone Russian wellness/hobby localization plus LCC strings.

## Source-clean runtime rules

The Survivor AI runtime split contains none of the former full upstream overrides (`BanditZombie.lua`, `BanditServerWanderers.lua`, `ZAStompPlant.lua`, `ZAWaterFarm.lua`). Its Farming wrappers fail open to installed original callbacks if an LCC precheck itself breaks. The empty-server guard targets dedicated multiplayer semantics and does not require client state.

The Wellness runtime split declares only the LCC `Yoga` UI proxy in `perks.txt` and validates the resolved `Lifestyle` parent at runtime.

Audits:

- `tools/audit_workshop_split.py` — packaging, neutral titles, credits, source/translation boundaries and core contracts.
- `tools/audit_wellness_proxy.py` — Yoga-only CustomPerk declaration and runtime parent contract.
- `tools/audit_survivor_ai_guards.py` — source-clean Survivor AI guards, dedicated-MP behavior and Farming fail-open semantics.

## Linux launcher

`server/linux/start-server.sh` remains repository/server tooling and is intentionally not duplicated into Workshop items.

## Before public visibility

1. Run `python3 tools/audit_workshop_split.py`, `python3 tools/audit_wellness_proxy.py`, and `python3 tools/audit_survivor_ai_guards.py`, plus the existing compatibility/translation audits.
2. Test each split item unlisted with original dependencies installed separately.
3. Never enable an equivalent split runtime project together with `LaccckaB4220Compat` during A/B testing.
4. Keep target credits/dependency details in descriptions even though public titles are neutral.
5. Assign real Workshop IDs only after the relevant smoke test succeeds.
6. Change visibility from unlisted only after dedicated-server/client regression testing.

See `PUBLICATION_AUDIT.md`, `SPLIT_MANIFEST.md`, and `TEST_CHECKLIST.md`.

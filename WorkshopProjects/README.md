# LCC Workshop Projects

This directory is a publication-oriented split of `LaccckaCompatibilityPatch` for Project Zomboid Build 42.20.x.

The monolithic patch remains the development/regression baseline. Runtime projects are separated by target/function so one upstream change or Workshop review does not block unrelated fixes. Translation projects are separated from runtime code and remain permission-gated.

## Safety model

A runtime compatibility project is **READY_FOR_UNLISTED_TEST** when its Workshop payload contains only LCC-authored code/localization, requires original mods separately, and does not redistribute their source/assets. LCC hooks/wrappers/shims may interact with installed target APIs/types; the target relationship must remain explicit and credited.

**BLOCKED_PENDING_PERMISSION** is used for mod-specific translated text. `Unlisted` is not a permission bypass; it is only controlled first-test visibility for source-clean runtime items.

## READY_FOR_UNLISTED_TEST

- `LCCB4220FirearmsBridge` — MFS 3D placement compatibility wrapper.
- `LCCB4220SVUTsarBridge` — SVU3/TsarLib legacy API path bridge.
- `LCCB4220zReBridge` — zRe legacy `BodyLocations` path bridge.
- `LCCB4220AegisGuard` — Aegis inventory-transfer guard.
- `LCCB4220LegacyCallbacks` — generic Build 42 legacy callback bridge.
- `LCCB4220SkillDescriptionsRU` — LCC-authored vanilla/Build 42 Russian skill descriptions only.
- `LCCB4220SurvivorAIStability` — independent Bandits runtime guards; no Bandits source/assets bundled.
- `LCCB4220WellnessCompat` — independent Lifestyle bath/shower/Yoga runtime compatibility; no Lifestyle source/assets/translations bundled.
- `LCCB4220PZKBridge` — independent PZK/SVU/Tsar path/API shims; no PZK vehicle/source/assets bundled.
- `LCCB4220OutfitMenuSafety` — independent Chimera runtime wrapper; no Chimera source/clothing assets bundled.

## BLOCKED_PENDING_PERMISSION

- `LCCB4220BanditsRU` — isolated Bandits Russian `IG_UI` translation. Provenance is exact, but translation publication rights/permission are not yet documented.
- `LCCB4220LifestyleRU` — isolated Lifestyle-oriented Russian localization staging. Bandits content is excluded; publication remains disabled until translation rights/permission and final key-level provenance are documented.

## Linux launcher

`server/linux/start-server.sh` remains repository/server tooling and is intentionally not duplicated into Workshop items.

## Before public visibility

1. Run `python3 tools/audit_workshop_split.py` plus the existing compatibility/translation audits.
2. Test runtime modules unlisted with original target mods installed separately.
3. Never enable equivalent monolithic and split runtime fixes together during A/B testing.
4. Keep target mod/author credits explicit and mark LCC modules unofficial.
5. Re-check current target Workshop pages and official PZ/Steam policies.
6. Keep mod-specific translations upload-disabled until their rights/provenance are clear.

See `PUBLICATION_AUDIT.md`, `SPLIT_MANIFEST.md`, and `TEST_CHECKLIST.md`.

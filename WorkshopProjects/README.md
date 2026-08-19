# LCC Workshop Projects

This directory is a publication-oriented split of `LaccckaCompatibilityPatch` for Project Zomboid Build 42.20.x.

The monolithic patch remains the development/regression baseline. Workshop projects are separated by target/function so one review, upstream change, or translation permission question does not block unrelated runtime fixes.

## Safety model

A runtime compatibility project is eligible for **READY_FOR_UNLISTED_TEST** when its Workshop payload contains only LCC-authored code/localization, requires original mods separately, and does not redistribute their source/assets. LCC hooks/wrappers/shims may interact with installed target APIs/types; the target relationship must be explicit and credited in the Workshop description.

**BLOCKED_PENDING_PERMISSION** is reserved for payloads that contain permission-sensitive mod-specific translated text or whose translation provenance is not complete.

`Unlisted` is not a permission bypass. It is the controlled first-test visibility for source-clean runtime items; target terms and the Project Zomboid/Steam policies must still be re-checked before public visibility.

## READY_FOR_UNLISTED_TEST

- `LCCB4220FirearmsBridge` — MFS 3D placement compatibility wrapper.
- `LCCB4220SVUTsarBridge` — SVU3/TsarLib legacy API path bridge.
- `LCCB4220zReBridge` — zRe legacy `BodyLocations` path bridge.
- `LCCB4220AegisGuard` — inventory-transfer nil-container guard used with Aegis.
- `LCCB4220LegacyCallbacks` — generic Build 42 legacy item callback bridge.
- `LCCB4220SkillDescriptionsRU` — LCC-authored Russian descriptions for vanilla/Build 42 skills only.
- `LCCB4220SurvivorAIStability` — independent Bandits compatibility guards; no Bandits source/assets bundled.
- `LCCB4220WellnessCompat` — independent Lifestyle bath/shower/Yoga runtime compatibility; no Lifestyle source/assets/translations bundled.
- `LCCB4220PZKBridge` — independent PZK/SVU/Tsar path/API shims; no PZK vehicle/source/assets bundled.
- `LCCB4220OutfitMenuSafety` — independent Chimera runtime wrapper; no Chimera source/clothing assets bundled.

## BLOCKED_PENDING_PERMISSION

- `LCCB4220BanditsRU` — isolated Bandits Russian `IG_UI` translation. Provenance is exact, but translation publication rights/permission are not yet documented.
- `LCCB4220ThirdPartyRU` — remaining mod-specific RU staging after Bandits extraction. Mostly Lifestyle-oriented, but key-level provenance is still being classified.

## Linux launcher

`server/linux/start-server.sh` remains repository/server tooling and is intentionally not duplicated into Workshop items.

## Before public visibility

1. Run `python3 tools/audit_workshop_split.py` plus the existing compatibility/translation audits.
2. Test the runtime module unlisted with the original target mod installed separately.
3. Never enable the equivalent monolithic patch at the same time during A/B testing.
4. Keep the target mod/author clearly credited and mark every LCC module unofficial.
5. Re-check the current target Workshop page and official PZ/Steam policies.
6. Keep mod-specific translations upload-disabled until their rights/provenance are clear.

See `PUBLICATION_AUDIT.md`, `SPLIT_MANIFEST.md`, and `TEST_CHECKLIST.md` for the exact boundary and test plan.

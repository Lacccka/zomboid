# LCC Workshop Projects

This directory is a publication-oriented split of `LaccckaCompatibilityPatch` for Project Zomboid Build 42.20.x.

## Safety model

The original monolithic patch remains untouched and is the development/reference implementation. Most runtime files are mirrored byte-for-byte. Where a mirrored third-party full-file override can be replaced safely, the split project may instead contain a smaller LCC-authored shim/wrapper; those deliberate divergences are documented in `SPLIT_MANIFEST.md` and checked by `tools/audit_workshop_split.py`.

Projects are separated by target/function so one Workshop review, permission question, or upstream change does not block unrelated fixes.

Three publication states are used:

- **READY_FOR_UNLISTED_TEST** — contains LCC-authored compatibility code (or LCC-authored vanilla/B42 localization) and does not redistribute the target mod. A real `workshop.txt` is present with an empty Workshop ID and `visibility=unlisted` for initial testing.
- **TECHNICALLY_CLEAN_PERMISSION_REVIEW** — the split contains only LCC-authored compatibility code/shims, but author-specific terms or prior communication still need explicit review. It remains upload-disabled with `workshop.txt.DISABLED`.
- **BLOCKED_PENDING_PERMISSION** — publication is explicitly blocked by known extension/translation restrictions or third-party material. `workshop.txt.DISABLED` must not be activated until permission/evidence covers the project.

`Unlisted` is not a permission bypass. It is only the initial visibility for projects that pass the publication audit.

## Projects

### READY_FOR_UNLISTED_TEST

- `LCCB4220FirearmsBridge` — MFS 3D placement compatibility wrapper.
- `LCCB4220SVUTsarBridge` — SVU3/TsarLib legacy API path bridge.
- `LCCB4220zReBridge` — zRe legacy `BodyLocations` path bridge.
- `LCCB4220AegisGuard` — inventory-transfer nil-container guard used with Aegis.
- `LCCB4220LegacyCallbacks` — generic Build 42 legacy item callback bridge.
- `LCCB4220SkillDescriptionsRU` — LCC-authored Russian descriptions for vanilla/Build 42 skills only.

### TECHNICALLY_CLEAN_PERMISSION_REVIEW

- `LCCB4220SurvivorAIStability` — Bandits compatibility. All four former full-file source overrides have now been replaced in the split by LCC-authored guards/wrappers. Upload remains disabled pending current author-policy review and runtime regression testing.

### BLOCKED_PENDING_PERMISSION

- `LCCB4220WellnessCompat` — Lifestyle runtime/Yoga compatibility; the upstream author requires permission for extensions.
- `LCCB4220PZKBridge` — PZK compatibility shims; upstream extension policy requires permission.
- `LCCB4220OutfitMenuSafety` — Chimera runtime compatibility; kept blocked because the author's historical permission policy is restrictive.
- `LCCB4220ThirdPartyRU` — third-party Russian localization currently combining Lifestyle/Bandits-related translation material; keep blocked until the relevant permissions are documented.

## Linux launcher

`server/linux/start-server.sh` remains repository/server tooling and is intentionally not duplicated into Workshop items. Its case-sensitivity preflight operates on installed Workshop content and should continue to be maintained separately.

## Before publishing anything

1. Check `PUBLICATION_AUDIT.md`.
2. Confirm the target mod's current permissions have not changed.
3. Keep the original mod as a dependency; never bundle its Workshop directory/assets.
4. Credit the target mod/author in the Workshop description when the project has a direct compatibility relationship.
5. Never describe an LCC module as official.
6. Run the compatibility audits and a dedicated-server/client smoke test before changing visibility from unlisted to public.

See `SPLIT_MANIFEST.md` for the exact source-to-project file mapping and deliberate publication refactors.

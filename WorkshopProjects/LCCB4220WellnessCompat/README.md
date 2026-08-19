# LCC B42.20 Wellness Compatibility

Status: **READY_FOR_UNLISTED_TEST**.

Unofficial independent compatibility patch for `Lifestyle: Hobbies` by Mopop and Angry. The original `LifestyleHobbies` Workshop mod remains a required separate dependency; this project contains no Lifestyle Lua source, assets, or third-party translations.

Runtime contents are LCC-authored compatibility code:

- `zzz_LCC_LifestyleBathFix.lua` wraps the installed `BathTubFunctions.walkToFront` behavior and delegates to the original outside the B42.20 compatibility case.
- `Hygiene/BathTubFunctions.lua` and `Hygiene/ShowerFunctions.lua` are minimal shared/server placeholders for client-only helper tables.
- `zzz_LCC_LifestyleYogaProgress.lua` exposes a UI proxy that reads authoritative Yoga level/XP from Lifestyle `HiddenSkills` storage.
- `media/perks.txt` declares only the LCC Yoga UI proxy with `parent = Lifestyle`; it does not reproduce Lifestyle's `Lifestyle`, `Art`, `Cleaning`, `Dancing`, `Meditation`, or `Music` perk declarations. Proxy XP thresholds are zero because real progression is read from HiddenSkills.
- `zzy_LCC_LifestyleYogaContract.lua` validates after game startup that the B42 custom-perk pipeline registered `Perks.Yoga` and resolved its parent to `Lifestyle`. If that contract changes, `LCCGuard` disables only the Yoga UI compatibility feature.

The current B42 API exposes `CustomPerk.m_parent` as a string and resolves custom perks through `CustomPerks`; this is consistent with a cross-file/custom-mod parent declaration, but the split is still explicitly marked for unlisted runtime smoke testing before public release.

Lifestyle-specific Russian localization is isolated in permission-gated `LCCB4220LifestyleRU` and is not part of this runtime item.

Before public visibility, verify Yoga registration/loading after `LifestyleHobbies`, the runtime contract log, bath/shower interactions, and client/server startup with the split checklist.

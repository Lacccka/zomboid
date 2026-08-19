# LCC B42.20 Wellness Compatibility

Status: **READY_FOR_UNLISTED_TEST**.

Unofficial independent compatibility patch for `Lifestyle: Hobbies` by Mopop and Angry. The original `LifestyleHobbies` Workshop mod remains a required separate dependency; this project contains no Lifestyle Lua source, assets, or third-party translations.

Runtime contents are LCC-authored compatibility code:

- `zzz_LCC_LifestyleBathFix.lua` wraps the installed `BathTubFunctions.walkToFront` behavior and delegates to the original outside the B42.20 compatibility case.
- `Hygiene/BathTubFunctions.lua` and `Hygiene/ShowerFunctions.lua` are minimal shared/server placeholders for client-only helper tables.
- `zzz_LCC_LifestyleYogaProgress.lua` exposes a UI proxy that reads the authoritative Yoga level/XP from Lifestyle `HiddenSkills` storage.
- `media/perks.txt` now declares only the LCC Yoga UI proxy. It no longer reproduces Lifestyle's `Lifestyle`, `Art`, `Cleaning`, `Dancing`, `Meditation`, or `Music` perk definitions; proxy XP thresholds are intentionally zero because progression is read from HiddenSkills.

Lifestyle-specific Russian localization remains isolated in `LCCB4220ThirdPartyRU` and is not part of this runtime item.

Before public visibility, verify Yoga registration/loading after `LifestyleHobbies`, bath/shower interactions, and client/server startup with the split checklist.

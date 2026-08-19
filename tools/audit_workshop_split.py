#!/usr/bin/env python3
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "LaccckaCompatibilityPatch" / "Contents" / "mods" / "LaccckaCompatibilityPatch"
WP = ROOT / "WorkshopProjects"

# Files intentionally mirrored byte-for-byte from the monolithic regression baseline.
pairs = {
    "LCCB4220FirearmsBridge/Contents/mods/LCCB4220FirearmsBridge/42/media/lua/shared/LCC/Guard.lua": "42/media/lua/shared/LCC/Guard.lua",
    "LCCB4220FirearmsBridge/Contents/mods/LCCB4220FirearmsBridge/42/media/lua/server/BuildingObjects/ISPlace3DItemCursor_Fix.lua": "42/media/lua/server/BuildingObjects/ISPlace3DItemCursor_Fix.lua",
    "LCCB4220SVUTsarBridge/Contents/mods/LCCB4220SVUTsarBridge/42/media/lua/shared/LCC/Guard.lua": "42/media/lua/shared/LCC/Guard.lua",
    "LCCB4220SVUTsarBridge/Contents/mods/LCCB4220SVUTsarBridge/42/media/lua/server/Tuning2/ATA2Tuning2.lua": "42/media/lua/server/Tuning2/ATA2Tuning2.lua",
    "LCCB4220zReBridge/Contents/mods/LCCB4220zReBridge/42/media/lua/shared/LCC/Guard.lua": "42/media/lua/shared/LCC/Guard.lua",
    "LCCB4220zReBridge/Contents/mods/LCCB4220zReBridge/42/media/lua/shared/BodyLocations.lua": "42/media/lua/shared/BodyLocations.lua",
    "LCCB4220AegisGuard/Contents/mods/LCCB4220AegisGuard/42/media/lua/shared/LCC/Guard.lua": "42/media/lua/shared/LCC/Guard.lua",
    "LCCB4220AegisGuard/Contents/mods/LCCB4220AegisGuard/42/media/lua/client/zzz_LCC_AegisTransferGuard.lua": "42/media/lua/client/zzz_LCC_AegisTransferGuard.lua",
    "LCCB4220LegacyCallbacks/Contents/mods/LCCB4220LegacyCallbacks/42/media/lua/shared/zzz_LCC_LegacyItemCallbacks.lua": "42/media/lua/shared/zzz_LCC_LegacyItemCallbacks.lua",
    "LCCB4220SkillDescriptionsRU/Contents/mods/LCCB4220SkillDescriptionsRU/42/media/lua/shared/Translate/RU/ZZ_LCC_VanillaPerks_RU.txt": "42/media/lua/shared/Translate/RU/ZZ_LCC_VanillaPerks_RU.txt",
    "LCCB4220SurvivorAIStability/Contents/mods/LCCB4220SurvivorAIStability/42/media/lua/shared/LCC/Guard.lua": "42/media/lua/shared/LCC/Guard.lua",
    "LCCB4220SurvivorAIStability/Contents/mods/LCCB4220SurvivorAIStability/42/media/lua/client/ISUI/ISCharacterScreen.lua": "42/media/lua/client/ISUI/ISCharacterScreen.lua",
    "LCCB4220SurvivorAIStability/Contents/mods/LCCB4220SurvivorAIStability/42/media/lua/server/zzz_LCC_BanditsDedicatedServerGuard.lua": "42/media/lua/server/zzz_LCC_BanditsDedicatedServerGuard.lua",
    "LCCB4220WellnessCompat/Contents/mods/LCCB4220WellnessCompat/42/media/lua/shared/LCC/Guard.lua": "42/media/lua/shared/LCC/Guard.lua",
    "LCCB4220WellnessCompat/Contents/mods/LCCB4220WellnessCompat/42/media/lua/client/zzz_LCC_LifestyleBathFix.lua": "42/media/lua/client/zzz_LCC_LifestyleBathFix.lua",
    "LCCB4220WellnessCompat/Contents/mods/LCCB4220WellnessCompat/42/media/lua/client/zzz_LCC_LifestyleYogaProgress.lua": "42/media/lua/client/zzz_LCC_LifestyleYogaProgress.lua",
    "LCCB4220WellnessCompat/Contents/mods/LCCB4220WellnessCompat/42/media/lua/shared/Hygiene/BathTubFunctions.lua": "42/media/lua/shared/Hygiene/BathTubFunctions.lua",
    "LCCB4220WellnessCompat/Contents/mods/LCCB4220WellnessCompat/42/media/lua/shared/Hygiene/ShowerFunctions.lua": "42/media/lua/shared/Hygiene/ShowerFunctions.lua",
    "LCCB4220PZKBridge/Contents/mods/LCCB4220PZKBridge/42/media/lua/shared/LCC/Guard.lua": "42/media/lua/shared/LCC/Guard.lua",
    "LCCB4220PZKBridge/Contents/mods/LCCB4220PZKBridge/42/media/lua/client/Vehicle/ISUI/ISVehiclePartMenu.lua": "42/media/lua/client/Vehicle/ISUI/ISVehiclePartMenu.lua",
    "LCCB4220PZKBridge/Contents/mods/LCCB4220PZKBridge/42/media/lua/client/Vehicle/ISVehiclePartMenu.lua": "42/media/lua/client/Vehicle/ISVehiclePartMenu.lua",
    "LCCB4220PZKBridge/Contents/mods/LCCB4220PZKBridge/42/media/lua/shared/ISBaseTimedAction.lua": "42/media/lua/shared/ISBaseTimedAction.lua",
    "LCCB4220PZKBridge/Contents/mods/LCCB4220PZKBridge/42/media/lua/server/utils/pzkZonesFunction.lua": "42/media/lua/server/utils/pzkZonesFunction.lua",
    "LCCB4220PZKBridge/Contents/mods/LCCB4220PZKBridge/42/media/lua/shared/SVU3_PZKVLCCars_Stuffs.lua": "42/media/lua/shared/SVU3_PZKVLCCars_Stuffs.lua",
    "LCCB4220OutfitMenuSafety/Contents/mods/LCCB4220OutfitMenuSafety/42/media/lua/shared/LCC/Guard.lua": "42/media/lua/shared/LCC/Guard.lua",
    "LCCB4220OutfitMenuSafety/Contents/mods/LCCB4220OutfitMenuSafety/42/media/lua/client/zzz_LCC_ChimeraGhillieFix.lua": "42/media/lua/client/zzz_LCC_ChimeraGhillieFix.lua",
}

translation_files = [
    "ContextMenu.json", "Farming.json", "IG_UI.json", "IG_UI_RU.txt", "ItemName.json",
    "Mod.json", "Moodles.json", "Moveables.json", "Moveables_RU.txt", "Recipes.json",
    "Recorded_Media.json", "Sandbox.json", "Tooltip.json", "UI.json", "ZZ_LCC_Perks_RU.txt",
]
for name in translation_files:
    pairs[f"LCCB4220ThirdPartyRU/Contents/mods/LCCB4220ThirdPartyRU/42/media/lua/shared/Translate/RU/{name}"] = f"42/media/lua/shared/Translate/RU/{name}"
for name in ["IG_UI.json", "Moveables.json", "Tooltip.json"]:
    pairs[f"LCCB4220ThirdPartyRU/Contents/mods/LCCB4220ThirdPartyRU/common/media/lua/shared/Translate/RU/{name}"] = f"common/media/lua/shared/Translate/RU/{name}"

# Deliberate publication-safe divergences from the monolithic regression package.
split_owned_required = [
    "LCCB4220SurvivorAIStability/Contents/mods/LCCB4220SurvivorAIStability/42/media/lua/client/zzz_LCC_BanditsZombieCacheGuard.lua",
    "LCCB4220SurvivorAIStability/Contents/mods/LCCB4220SurvivorAIStability/42/media/lua/shared/zzz_LCC_BanditsFarmingGuard.lua",
    "LCCB4220SurvivorAIStability/Contents/mods/LCCB4220SurvivorAIStability/42/media/lua/server/zzz_LCC_BanditsEmptyServerWandererGuard.lua",
    "LCCB4220WellnessCompat/Contents/mods/LCCB4220WellnessCompat/42/media/perks.txt",
]

forbidden_bandits_copies = [
    "LCCB4220SurvivorAIStability/Contents/mods/LCCB4220SurvivorAIStability/42/media/lua/client/BanditZombie.lua",
    "LCCB4220SurvivorAIStability/Contents/mods/LCCB4220SurvivorAIStability/42/media/lua/server/BanditServerWanderers.lua",
    "LCCB4220SurvivorAIStability/Contents/mods/LCCB4220SurvivorAIStability/42/media/lua/shared/ZombieActions/ZAStompPlant.lua",
    "LCCB4220SurvivorAIStability/Contents/mods/LCCB4220SurvivorAIStability/42/media/lua/shared/ZombieActions/ZAWaterFarm.lua",
]

ready = {
    "LCCB4220FirearmsBridge", "LCCB4220SVUTsarBridge", "LCCB4220zReBridge",
    "LCCB4220AegisGuard", "LCCB4220LegacyCallbacks", "LCCB4220SkillDescriptionsRU",
    "LCCB4220SurvivorAIStability", "LCCB4220WellnessCompat", "LCCB4220PZKBridge",
    "LCCB4220OutfitMenuSafety",
}
blocked = {"LCCB4220ThirdPartyRU"}

# Direct compatibility relationships must remain explicit even when titles are neutral.
credit_markers = {
    "LCCB4220SurvivorAIStability": ("Bandits", "Slayer"),
    "LCCB4220WellnessCompat": ("Lifestyle", "Mopop", "Angry"),
    "LCCB4220PZKBridge": ("PZK", "PZK Forge"),
    "LCCB4220OutfitMenuSafety": ("Chimera", "EtherealShigure"),
}

errors = []
for dst_rel, src_rel in sorted(pairs.items()):
    dst = WP / dst_rel
    src = SRC / src_rel
    if not src.is_file():
        errors.append(f"missing source: {src_rel}")
        continue
    if not dst.is_file():
        errors.append(f"missing split copy: {dst_rel}")
        continue
    if src.read_bytes() != dst.read_bytes():
        errors.append(f"split copy differs from source: {dst_rel} <- {src_rel}")

for rel in split_owned_required:
    if not (WP / rel).is_file():
        errors.append(f"missing split-owned publication refactor: {rel}")

for rel in forbidden_bandits_copies:
    if (WP / rel).exists():
        errors.append(f"Bandits split must not redistribute upstream override: {rel}")

# Lifestyle split may declare only our Yoga proxy; all upstream Lifestyle perk blocks stay upstream.
wellness_perks = WP / "LCCB4220WellnessCompat/Contents/mods/LCCB4220WellnessCompat/42/media/perks.txt"
if wellness_perks.is_file():
    perk_names = re.findall(r"(?m)^\s*perk\s+([A-Za-z0-9_]+)\s*$", wellness_perks.read_text(encoding="utf-8"))
    if perk_names != ["Yoga"]:
        errors.append(f"Wellness split perks.txt must declare only Yoga; found: {perk_names}")
    text = wellness_perks.read_text(encoding="utf-8")
    for key in ("xp1", "xp2", "xp3", "xp4", "xp5", "xp6", "xp7", "xp8", "xp9", "xp10"):
        if not re.search(rf"(?m)^\s*{key}\s*=\s*0,?\s*$", text):
            errors.append(f"Wellness Yoga proxy must keep {key}=0 and read real XP from HiddenSkills")
else:
    errors.append("missing Wellness split perks.txt")

for project in sorted(ready):
    descriptor = WP / project / "workshop.txt"
    if not descriptor.is_file():
        errors.append(f"READY project lacks workshop.txt: {project}")
        continue
    if (WP / project / "workshop.txt.DISABLED").exists():
        errors.append(f"READY project unexpectedly has disabled descriptor: {project}")
    text = descriptor.read_text(encoding="utf-8")
    if "visibility=unlisted" not in text:
        errors.append(f"READY project must remain unlisted until smoke-tested: {project}")
    if not re.search(r"(?m)^id=\s*$", text):
        errors.append(f"READY project should have empty Workshop id before first upload: {project}")

for project, markers in credit_markers.items():
    descriptor = WP / project / "workshop.txt"
    if descriptor.is_file():
        text = descriptor.read_text(encoding="utf-8")
        for marker in markers:
            if marker not in text:
                errors.append(f"Workshop description for {project} must credit/name relationship marker: {marker}")

for project in sorted(blocked):
    if (WP / project / "workshop.txt").exists():
        errors.append(f"BLOCKED project must not have active workshop.txt: {project}")
    if not (WP / project / "workshop.txt.DISABLED").is_file():
        errors.append(f"BLOCKED project lacks workshop.txt.DISABLED: {project}")

vanilla_project = WP / "LCCB4220SkillDescriptionsRU"
if any(p.name == "ZZ_LCC_Perks_RU.txt" for p in vanilla_project.rglob("*")):
    errors.append("vanilla/B42 skill project must not contain Lifestyle ZZ_LCC_Perks_RU.txt")

if errors:
    print("Workshop split audit FAILED:")
    for error in errors:
        print(f" - {error}")
    sys.exit(1)

print(
    f"Workshop split audit OK: {len(pairs)} mirrored source files; "
    f"{len(split_owned_required)} split-owned refactors; {len(ready)} ready projects; "
    f"{len(blocked)} blocked translation project(s)"
)

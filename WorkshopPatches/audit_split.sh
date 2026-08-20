#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SPLIT="$ROOT/WorkshopPatches"

fail=0

error() {
    printf 'ERROR: %s\n' "$*" >&2
    fail=1
}

require_file() {
    local path="$1"
    if [[ ! -f "$path" ]]; then
        error "missing required file: ${path#$ROOT/}"
        return 1
    fi
    if [[ ! -s "$path" ]]; then
        error "required file is empty: ${path#$ROOT/}"
        return 1
    fi
    return 0
}

require_marker() {
    local path="$1"
    local marker="$2"
    local message="$3"
    if [[ ! -f "$path" ]]; then
        error "cannot validate marker; file missing: ${path#$ROOT/}"
        return
    fi
    if ! grep -Fq -- "$marker" "$path"; then
        error "$message"
    fi
}

forbid_marker() {
    local path="$1"
    local marker="$2"
    local message="$3"
    if [[ -f "$path" ]] && grep -Fq -- "$marker" "$path"; then
        error "$message"
    fi
}

expected_patch_dirs=(
    ActivityFixes
    CompatibilityBridges
    PatchCore
    RuntimeFixes
    RussianTextFixes
    SafetyFixes
)

tmp_actual_dirs="$(mktemp)"
tmp_expected_dirs="$(mktemp)"
trap 'rm -f "$tmp_actual_dirs" "$tmp_expected_dirs"' EXIT

find "$SPLIT" -mindepth 1 -maxdepth 1 -type d -printf '%f\n' | sort > "$tmp_actual_dirs"
printf '%s\n' "${expected_patch_dirs[@]}" | sort > "$tmp_expected_dirs"
if ! diff -u "$tmp_expected_dirs" "$tmp_actual_dirs"; then
    error "WorkshopPatches must contain exactly the six supported patch directories"
fi

core="$SPLIT/PatchCore/Contents/mods/LaccckaB4220PatchCore/42/media"
runtime="$SPLIT/RuntimeFixes/Contents/mods/LaccckaB4220RuntimeFixes/42/media"
activity="$SPLIT/ActivityFixes/Contents/mods/LaccckaB4220ActivityFixes/42/media"
bridges="$SPLIT/CompatibilityBridges/Contents/mods/LaccckaB4220CompatBridges/42/media"
safety="$SPLIT/SafetyFixes/Contents/mods/LaccckaB4220SafetyFixes/42/media"
text42="$SPLIT/RussianTextFixes/Contents/mods/LaccckaB4220RussianText/42/media"
text_common="$SPLIT/RussianTextFixes/Contents/mods/LaccckaB4220RussianText/common/media"

required_files=(
    "$core/lua/shared/LCC/Guard.lua"
    "$core/lua/shared/LCC/CoreGuard.lua"

    "$runtime/lua/client/ISUI/ISCharacterScreen.lua"
    "$runtime/lua/client/zzz_LCC_BanditsAdminSpawnMenu.lua"
    "$runtime/lua/client/zzz_LCC_BanditsZombieCacheGuard.lua"
    "$runtime/lua/server/zzz_LCC_BanditsDedicatedServerGuard.lua"
    "$runtime/lua/server/zzz_LCC_BanditsEmptyServerWandererGuard.lua"
    "$runtime/lua/shared/LCC/Guard.lua"
    "$runtime/lua/shared/zzz_LCC_BanditsFarmingGuard.lua"

    "$activity/lua/client/zzz_LCC_LifestyleBathFix.lua"
    "$activity/lua/client/zzz_LCC_LifestyleYogaProgress.lua"
    "$activity/lua/client/zzz_LCC_SkillDescriptions.lua"
    "$activity/lua/shared/Hygiene/BathTubFunctions.lua"
    "$activity/lua/shared/Hygiene/ShowerFunctions.lua"
    "$activity/lua/shared/LCC/Guard.lua"
    "$activity/perks.txt"

    "$bridges/lua/client/Vehicle/ISUI/ISVehiclePartMenu.lua"
    "$bridges/lua/client/Vehicle/ISVehiclePartMenu.lua"
    "$bridges/lua/server/BuildingObjects/ISPlace3DItemCursor_Fix.lua"
    "$bridges/lua/server/Tuning2/ATA2Tuning2.lua"
    "$bridges/lua/server/utils/pzkZonesFunction.lua"
    "$bridges/lua/shared/BodyLocations.lua"
    "$bridges/lua/shared/ISBaseTimedAction.lua"
    "$bridges/lua/shared/LCC/Guard.lua"
    "$bridges/lua/shared/SVU3_PZKVLCCars_Stuffs.lua"
    "$bridges/lua/shared/zzz_LCC_LegacyItemCallbacks.lua"

    "$safety/lua/client/zzz_LCC_AegisTransferGuard.lua"
    "$safety/lua/client/zzz_LCC_ChimeraGhillieFix.lua"
    "$safety/lua/shared/LCC/Guard.lua"
)

for path in "${required_files[@]}"; do
    require_file "$path" || true
done

# PatchCore and optional-Guard bootstrap contract.
core_guard="$core/lua/shared/LCC/Guard.lua"
core_entry="$core/lua/shared/LCC/CoreGuard.lua"

if [[ -f "$core_guard" && -f "$core_entry" ]] && ! cmp -s "$core_guard" "$core_entry"; then
    error "PatchCore CoreGuard.lua and Guard.lua must remain byte-for-byte equivalent"
fi

for marker in \
    'function Guard.safeRequire' \
    'function Guard.protect' \
    'function Guard.install' \
    'function Guard.wrapBefore' \
    'Guard.__initialized = true'; do
    require_marker "$core_guard" "$marker" "PatchCore Guard lost contract marker: $marker"
done

runtime_guard="$runtime/lua/shared/LCC/Guard.lua"
activity_guard="$activity/lua/shared/LCC/Guard.lua"
bridges_guard="$bridges/lua/shared/LCC/Guard.lua"
safety_guard="$safety/lua/shared/LCC/Guard.lua"

functional_guards=(
    "$runtime_guard"
    "$activity_guard"
    "$bridges_guard"
    "$safety_guard"
)

for guard in "${functional_guards[@]}"; do
    require_marker "$guard" 'pcall(require, "LCC/CoreGuard")' "Guard bootstrap does not prefer PatchCore: ${guard#$ROOT/}"
    require_marker "$guard" 'CoreGuard.MODE = "GUARDED"' "Guard bootstrap lost GUARDED mode: ${guard#$ROOT/}"
    require_marker "$guard" 'Guard.MODE = "DEGRADED"' "Guard bootstrap lost DEGRADED fallback: ${guard#$ROOT/}"
    require_marker "$guard" 'Correct operation is not guaranteed' "Guard bootstrap lost degraded warning: ${guard#$ROOT/}"
done

for guard in "$activity_guard" "$bridges_guard" "$safety_guard"; do
    if [[ -f "$runtime_guard" && -f "$guard" ]] && ! cmp -s "$runtime_guard" "$guard"; then
        error "functional Guard bootstraps must remain identical: ${guard#$ROOT/}"
    fi
done

# RuntimeFixes: source-clean Bandits contracts.
runtime_character="$runtime/lua/client/ISUI/ISCharacterScreen.lua"
runtime_admin="$runtime/lua/client/zzz_LCC_BanditsAdminSpawnMenu.lua"
runtime_cache="$runtime/lua/client/zzz_LCC_BanditsZombieCacheGuard.lua"
runtime_dedicated="$runtime/lua/server/zzz_LCC_BanditsDedicatedServerGuard.lua"
runtime_empty="$runtime/lua/server/zzz_LCC_BanditsEmptyServerWandererGuard.lua"
runtime_farming="$runtime/lua/shared/zzz_LCC_BanditsFarmingGuard.lua"

for forbidden in \
    "$runtime/lua/client/BanditZombie.lua" \
    "$runtime/lua/client/BanditUpdate.lua" \
    "$runtime/lua/server/BanditServerWanderers.lua" \
    "$runtime/lua/shared/ZombieActions/ZAStompPlant.lua" \
    "$runtime/lua/shared/ZombieActions/ZAWaterFarm.lua"; do
    [[ ! -e "$forbidden" ]] || error "RuntimeFixes must not bundle upstream Bandits source: ${forbidden#$ROOT/}"
done

require_marker "$runtime_character" 'Guard.safeRequire(FEATURE, "XpSystem/ISUI/ISCharacterScreen")' \
    "RuntimeFixes character-screen shim lost the B42.20 target path"
require_marker "$runtime_admin" 'sendClientCommand(player, "Spawner", "Clan", args)' \
    "RuntimeFixes admin spawn helper lost Bandits server-command path"
require_marker "$runtime_admin" 'hasStaffAccess' \
    "RuntimeFixes admin spawn helper lost staff-access guard"

require_marker "$runtime_empty" 'BanditCustom.ClanGetAll = function' \
    "RuntimeFixes empty-server guard lost ClanGetAll wrapper"
require_marker "$runtime_empty" 'players:size() == 0' \
    "RuntimeFixes empty-server guard lost zero-player condition"
require_marker "$runtime_empty" 'return originalClanGetAll(...)' \
    "RuntimeFixes empty-server guard must preserve normal ClanGetAll behavior"

require_marker "$runtime_cache" 'BanditCompatibility.IsReanimatedForGrappleOnly = function' \
    "RuntimeFixes cache guard lost BanditUpdate early-return seam"
require_marker "$runtime_cache" 'not getSquareSafe(zombie)' \
    "RuntimeFixes cache guard lost squareless predicate"
require_marker "$runtime_cache" 'Events.OnZombieUpdate.Add' \
    "RuntimeFixes cache guard lost post-update cleanup"
require_marker "$runtime_cache" 'Events.EveryOneMinute.Add' \
    "RuntimeFixes cache guard lost post-flush sweep"

require_marker "$runtime_farming" 'return original(...)' \
    "RuntimeFixes farming wrappers must preserve original callbacks"
require_marker "$runtime_farming" 'shouldSkipWaterComplete' \
    "RuntimeFixes farming guard must finish invalid water tasks cleanly"
require_marker "$runtime_farming" 'CFarmingSystem.instance' \
    "RuntimeFixes farming guard lost B42 farming availability check"

require_marker "$runtime_dedicated" 'BanditZombie.GetInstanceById = lookupZombie' \
    "RuntimeFixes dedicated guard must install lookup contract"
require_marker "$runtime_dedicated" 'BanditServerZombie.Cache' \
    "RuntimeFixes dedicated lookup lost optional native server-cache path"
require_marker "$runtime_dedicated" 'Guard.wrapBefore(FEATURE, Bandit, "ApplyVisuals", registerZombie)' \
    "RuntimeFixes dedicated lookup lost Bandit registration hook"
require_marker "$runtime_dedicated" 'Guard.wrapBefore(FEATURE, Bandit, "UpdateItemsToSpawnAtDeath", registerZombie)' \
    "RuntimeFixes dedicated lookup lost death-item registration hook"
require_marker "$runtime_dedicated" 'Events.EveryOneMinute.Add(pruneRegistry)' \
    "RuntimeFixes dedicated registry lost stale-entry pruning"
require_marker "$runtime_dedicated" 'pcall(getId, zombie)' \
    "RuntimeFixes dedicated lookup lost stale-IsoZombie protection"
forbid_marker "$runtime_dedicated" 'getZombieList()' \
    "RuntimeFixes dedicated lookup must not scan the complete server zombie list"

# ActivityFixes: Lifestyle/hygiene/Yoga/skill-description contracts.
activity_bath="$activity/lua/client/zzz_LCC_LifestyleBathFix.lua"
activity_yoga="$activity/lua/client/zzz_LCC_LifestyleYogaProgress.lua"
activity_skills="$activity/lua/client/zzz_LCC_SkillDescriptions.lua"
activity_bathtub_shim="$activity/lua/shared/Hygiene/BathTubFunctions.lua"
activity_shower_shim="$activity/lua/shared/Hygiene/ShowerFunctions.lua"
activity_perks="$activity/perks.txt"

require_marker "$activity_bath" 'BathTubFunctions.walkToFront' \
    "ActivityFixes bathtub hook lost Lifestyle walkToFront seam"
require_marker "$activity_bath" 'fixtures_bathroom_01_25' \
    "ActivityFixes bathtub hook lost west-entry fixture handling"
require_marker "$activity_bath" 'Events.OnGameStart.Add(installBathFix)' \
    "ActivityFixes bathtub hook lost late install retry"

require_marker "$activity_yoga" 'HiddenSkills.getSkill' \
    "ActivityFixes Yoga UI lost HiddenSkills authority"
require_marker "$activity_yoga" 'ISSkillProgressBar.new = function' \
    "ActivityFixes Yoga UI lost skill-progress-bar proxy"
require_marker "$activity_yoga" 'function LCCYogaSkillProgressBar:onMouseUp' \
    "ActivityFixes Yoga UI lost no-LevelPerk proxy protection"
require_marker "$activity_yoga" 'Farming_LCC_Skill_Yoga_Description' \
    "ActivityFixes Yoga UI lost Russian description key"

require_marker "$activity_skills" 'ISSkillProgressBar.updateTooltip = function' \
    "ActivityFixes skill-description repair lost tooltip wrapper"
require_marker "$activity_skills" 'RU_DESCRIPTION_KEYS' \
    "ActivityFixes skill-description repair lost Russian override map"

require_marker "$activity_bathtub_shim" 'BathTubFunctions.DoAction = BathTubFunctions.DoAction or function() end' \
    "ActivityFixes bathtub shared shim lost DoAction fallback"
require_marker "$activity_shower_shim" 'ShowerFunctions.DoAction = ShowerFunctions.DoAction or function() end' \
    "ActivityFixes shower shared shim lost DoAction fallback"

require_marker "$activity_perks" 'perk Yoga' \
    "ActivityFixes perks.txt must declare Yoga"
require_marker "$activity_perks" 'parent = Lifestyle' \
    "ActivityFixes Yoga proxy must remain under Lifestyle"

for skill in Art Cleaning Dancing Meditation Music; do
    if [[ -f "$activity_perks" ]] && grep -Eq "^[[:space:]]*perk[[:space:]]+$skill([[:space:]]|$)" "$activity_perks"; then
        error "ActivityFixes perks.txt must not redeclare upstream Lifestyle perk: $skill"
    fi
done

# CompatibilityBridges: legacy module/API redirects.
bridge_vehicle_isui="$bridges/lua/client/Vehicle/ISUI/ISVehiclePartMenu.lua"
bridge_vehicle="$bridges/lua/client/Vehicle/ISVehiclePartMenu.lua"
bridge_place3d="$bridges/lua/server/BuildingObjects/ISPlace3DItemCursor_Fix.lua"
bridge_tuning="$bridges/lua/server/Tuning2/ATA2Tuning2.lua"
bridge_pzk="$bridges/lua/server/utils/pzkZonesFunction.lua"
bridge_body="$bridges/lua/shared/BodyLocations.lua"
bridge_timed="$bridges/lua/shared/ISBaseTimedAction.lua"
bridge_svu="$bridges/lua/shared/SVU3_PZKVLCCars_Stuffs.lua"
bridge_callbacks="$bridges/lua/shared/zzz_LCC_LegacyItemCallbacks.lua"

require_marker "$bridge_vehicle_isui" 'Guard.safeRequire(FEATURE, "Vehicles/ISUI/ISVehiclePartMenu")' \
    "CompatibilityBridges ISUI vehicle shim lost B42 target"
require_marker "$bridge_vehicle" 'Guard.safeRequire(FEATURE, "Vehicles/ISUI/ISVehiclePartMenu")' \
    "CompatibilityBridges vehicle shim lost B42 target"
require_marker "$bridge_place3d" 'if isServer() then return end' \
    "CompatibilityBridges 3D-item cursor fix must stay client-only"
require_marker "$bridge_place3d" 'ISPlace3DItemCursor.__LCCWeaponPartRenderFix' \
    "CompatibilityBridges 3D-item cursor fix lost install marker"

for path in "$bridge_tuning" "$bridge_pzk" "$bridge_body" "$bridge_timed"; do
    require_marker "$path" 'LCC/Guard' "CompatibilityBridges guarded redirect lost Guard dependency: ${path#$ROOT/}"
done

require_marker "$bridge_svu" 'return require "OtherModsSupport/SVU3_PZKVLCCars_Stuffs"' \
    "CompatibilityBridges SVU3/PZK redirect lost its current target"
require_marker "$bridge_callbacks" 'SpecialLootSpawns.OnCreateRecipeMagazine' \
    "CompatibilityBridges legacy item callback shim lost old callback contract"
require_marker "$bridge_callbacks" 'ItemCodeOnCreate.onCreateRecipeMagazine' \
    "CompatibilityBridges legacy item callback shim lost B42 callback target"

# SafetyFixes: narrow defensive wrappers.
safety_aegis="$safety/lua/client/zzz_LCC_AegisTransferGuard.lua"
safety_chimera="$safety/lua/client/zzz_LCC_ChimeraGhillieFix.lua"

require_marker "$safety_aegis" 'ISInventoryTransferAction.isValid' \
    "SafetyFixes Aegis guard lost transfer validity seam"
require_marker "$safety_aegis" 'not self.item or not self.srcContainer or not self.destContainer' \
    "SafetyFixes Aegis guard lost nil-container precheck"
require_marker "$safety_chimera" 'Guard.install' \
    "SafetyFixes Chimera guard lost guarded install contract"

# RussianTextFixes: standalone RU-only translation package.
ru42="$text42/lua/shared/Translate/RU"
ru_common="$text_common/lua/shared/Translate/RU"

[[ -d "$ru42" ]] || error "RussianTextFixes 42 RU tree is missing"
[[ -d "$ru_common" ]] || error "RussianTextFixes common RU tree is missing"

if [[ -d "$text42/lua/shared/Translate" ]] && \
        find "$text42/lua/shared/Translate" -mindepth 1 -maxdepth 1 -type d ! -name RU -print -quit | grep -q .; then
    error "RussianTextFixes 42 translation layer must contain RU only"
fi
if [[ -d "$text_common/lua/shared/Translate" ]] && \
        find "$text_common/lua/shared/Translate" -mindepth 1 -maxdepth 1 -type d ! -name RU -print -quit | grep -q .; then
    error "RussianTextFixes common translation layer must contain RU only"
fi

if [[ -d "$text42" ]] && find "$text42" -type f ! -path "$ru42/*" -print -quit | grep -q .; then
    error "RussianTextFixes 42/media must contain translation files only"
fi
if [[ -d "$text_common" ]] && find "$text_common" -type f ! -path "$ru_common/*" -print -quit | grep -q .; then
    error "RussianTextFixes common/media must contain translation files only"
fi

if [[ -d "$ru42" ]]; then
    count42="$(find "$ru42" -type f | wc -l)"
    [[ "$count42" -eq 16 ]] || error "RussianTextFixes 42 RU tree expected 16 files, found $count42"
fi
if [[ -d "$ru_common" ]]; then
    count_common="$(find "$ru_common" -type f | wc -l)"
    [[ "$count_common" -eq 4 ]] || error "RussianTextFixes common RU tree expected 4 files, found $count_common"
fi

for json_root in "$ru42" "$ru_common"; do
    [[ -d "$json_root" ]] || continue
    while IFS= read -r -d '' json; do
        if ! python3 -m json.tool "$json" >/dev/null 2>&1; then
            error "invalid JSON translation file: ${json#$ROOT/}"
        fi
    done < <(find "$json_root" -type f -name '*.json' -print0)
done

for path in \
    "$ru42/Farming.json" \
    "$ru42/IG_UI.json" \
    "$ru42/Moodles.json" \
    "$ru42/Tooltip.json" \
    "$ru42/ZZ_LCC_Perks_RU.txt" \
    "$ru42/ZZ_LCC_VanillaPerks_RU.txt" \
    "$ru_common/IG_UI.json" \
    "$ru_common/Tooltip.json"; do
    require_file "$path" || true
done

# Workshop and mod.info metadata contracts.
declare -A expected_ids=(
    [PatchCore]="LaccckaB4220PatchCore"
    [RuntimeFixes]="LaccckaB4220RuntimeFixes"
    [ActivityFixes]="LaccckaB4220ActivityFixes"
    [CompatibilityBridges]="LaccckaB4220CompatBridges"
    [SafetyFixes]="LaccckaB4220SafetyFixes"
    [RussianTextFixes]="LaccckaB4220RussianText"
)

declare -A expected_workshop_ids=(
    [PatchCore]="3786175901"
    [RuntimeFixes]="3786175979"
    [ActivityFixes]="3786175725"
    [CompatibilityBridges]="3786175808"
    [SafetyFixes]="3786176221"
    [RussianTextFixes]="3786176120"
)

seen_mod_ids=""
seen_workshop_ids=""

for folder in "${expected_patch_dirs[@]}"; do
    id="${expected_ids[$folder]}"
    workshop_id="${expected_workshop_ids[$folder]}"
    workshop="$SPLIT/$folder/workshop.txt"
    preview="$SPLIT/$folder/preview.png"
    modinfo="$SPLIT/$folder/Contents/mods/$id/42/mod.info"

    require_file "$workshop" || true
    require_file "$preview" || true
    require_file "$modinfo" || true

    if [[ -f "$modinfo" ]]; then
        grep -Fxq "id=$id" "$modinfo" || error "$folder: wrong mod ID"
        grep -Fxq 'versionMin=42.20.0' "$modinfo" || error "$folder: versionMin must stay on 42.20.0"
    fi

    if [[ -f "$workshop" ]]; then
        grep -Fxq "id=$workshop_id" "$workshop" || error "$folder: wrong published Workshop ID"
        grep -Fqi 'Do not use' "$workshop" || error "$folder: Workshop warning is missing"
        grep -Eqi 'original mod(s| Lua files| files)?.*not included|original mods are not included' "$workshop" \
            || error "$folder: no-bundled-mods disclaimer is missing"
    fi

    if grep -Fqx "$id" <<<"$seen_mod_ids"; then
        error "$folder: duplicate mod ID $id"
    fi
    seen_mod_ids+="$id"$'\n'

    if grep -Fqx "$workshop_id" <<<"$seen_workshop_ids"; then
        error "$folder: duplicate Workshop ID $workshop_id"
    fi
    seen_workshop_ids+="$workshop_id"$'\n'
done

for folder in RuntimeFixes ActivityFixes CompatibilityBridges SafetyFixes; do
    id="${expected_ids[$folder]}"
    modinfo="$SPLIT/$folder/Contents/mods/$id/42/mod.info"
    [[ -f "$modinfo" ]] || continue

    if grep -Eq '^require=.*LaccckaB4220PatchCore' "$modinfo"; then
        error "$folder: PatchCore must remain a soft dependency, not require="
    fi
    grep -Fq '\LaccckaB4220PatchCore' "$modinfo" \
        || error "$folder: PatchCore soft load-order dependency missing"
    grep -Fqi 'strongly recommended' "$modinfo" \
        || error "$folder: optional PatchCore warning missing from mod.info"
done

runtime_modinfo="$SPLIT/RuntimeFixes/Contents/mods/LaccckaB4220RuntimeFixes/42/mod.info"
activity_modinfo="$SPLIT/ActivityFixes/Contents/mods/LaccckaB4220ActivityFixes/42/mod.info"
bridges_modinfo="$SPLIT/CompatibilityBridges/Contents/mods/LaccckaB4220CompatBridges/42/mod.info"
safety_modinfo="$SPLIT/SafetyFixes/Contents/mods/LaccckaB4220SafetyFixes/42/mod.info"
text_modinfo="$SPLIT/RussianTextFixes/Contents/mods/LaccckaB4220RussianText/42/mod.info"

require_marker "$runtime_modinfo" '\Bandits2' "RuntimeFixes loadafter lost Bandits2"
require_marker "$activity_modinfo" '\LifestyleHobbies' "ActivityFixes loadafter lost LifestyleHobbies"

for dep in ModernFirearmsSystem MFS_community_fix PZKCarzoneWorkshop PzkVanillaPlusCarPack StandardizedVehicleUpgrades3Core tsarslib zReFRAMEWORK; do
    require_marker "$bridges_modinfo" "\\$dep" "CompatibilityBridges loadafter lost dependency: $dep"
done

for dep in AP GridInventory Federal_Rangers_Chimera; do
    require_marker "$safety_modinfo" "\\$dep" "SafetyFixes loadafter lost dependency: $dep"
done

require_marker "$text_modinfo" '\Bandits2' "RussianTextFixes loadafter lost Bandits2"
require_marker "$text_modinfo" '\LifestyleHobbies' "RussianTextFixes loadafter lost LifestyleHobbies"

if [[ -f "$text_modinfo" ]] && grep -Fq '\LaccckaB4220PatchCore' "$text_modinfo"; then
    error "RussianTextFixes must remain standalone and must not depend on PatchCore"
fi

if (( fail != 0 )); then
    exit 1
fi

printf 'Grouped Workshop patches audit: OK (6 current packages)\n'

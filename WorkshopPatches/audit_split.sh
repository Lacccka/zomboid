#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MONO="$ROOT/LaccckaCompatibilityPatch/Contents/mods/LaccckaCompatibilityPatch"
SPLIT="$ROOT/WorkshopPatches"

fail=0
mapped_sources=()

error() {
    printf 'ERROR: %s\n' "$*" >&2
    fail=1
}

same() {
    local src_rel="$1"
    local dst="$2"
    local src="$MONO/42/media/$src_rel"
    mapped_sources+=("$src_rel")
    if [[ ! -f "$src" ]]; then
        error "missing monolith source: $src_rel"
        return
    fi
    if [[ ! -f "$dst" ]]; then
        error "missing split target for $src_rel: ${dst#$ROOT/}"
        return
    fi
    if ! cmp -s "$src" "$dst"; then
        error "split target differs from monolith: $src_rel -> ${dst#$ROOT/}"
    fi
}

core="$SPLIT/PatchCore/Contents/mods/LaccckaB4220PatchCore/42/media"
runtime="$SPLIT/RuntimeFixes/Contents/mods/LaccckaB4220RuntimeFixes/42/media"
activity="$SPLIT/ActivityFixes/Contents/mods/LaccckaB4220ActivityFixes/42/media"
bridges="$SPLIT/CompatibilityBridges/Contents/mods/LaccckaB4220CompatBridges/42/media"
safety="$SPLIT/SafetyFixes/Contents/mods/LaccckaB4220SafetyFixes/42/media"
text42="$SPLIT/RussianTextFixes/Contents/mods/LaccckaB4220RussianText/42/media"
text_common="$SPLIT/RussianTextFixes/Contents/mods/LaccckaB4220RussianText/common"

# Shared patch helper.
same "lua/shared/LCC/Guard.lua" "$core/lua/shared/LCC/Guard.lua"

# Runtime fixes.
same "lua/client/BanditZombie.lua" "$runtime/lua/client/BanditZombie.lua"
same "lua/client/ISUI/ISCharacterScreen.lua" "$runtime/lua/client/ISUI/ISCharacterScreen.lua"
same "lua/server/BanditServerWanderers.lua" "$runtime/lua/server/BanditServerWanderers.lua"
same "lua/server/zzz_LCC_BanditsDedicatedServerGuard.lua" "$runtime/lua/server/zzz_LCC_BanditsDedicatedServerGuard.lua"
same "lua/shared/ZombieActions/ZAStompPlant.lua" "$runtime/lua/shared/ZombieActions/ZAStompPlant.lua"
same "lua/shared/ZombieActions/ZAWaterFarm.lua" "$runtime/lua/shared/ZombieActions/ZAWaterFarm.lua"

# Activity fixes.
same "lua/client/zzz_LCC_LifestyleBathFix.lua" "$activity/lua/client/zzz_LCC_LifestyleBathFix.lua"
same "lua/client/zzz_LCC_LifestyleYogaProgress.lua" "$activity/lua/client/zzz_LCC_LifestyleYogaProgress.lua"
same "lua/shared/Hygiene/BathTubFunctions.lua" "$activity/lua/shared/Hygiene/BathTubFunctions.lua"
same "lua/shared/Hygiene/ShowerFunctions.lua" "$activity/lua/shared/Hygiene/ShowerFunctions.lua"
same "perks.txt" "$activity/perks.txt"

# Compatibility bridges.
same "lua/client/Vehicle/ISUI/ISVehiclePartMenu.lua" "$bridges/lua/client/Vehicle/ISUI/ISVehiclePartMenu.lua"
same "lua/client/Vehicle/ISVehiclePartMenu.lua" "$bridges/lua/client/Vehicle/ISVehiclePartMenu.lua"
same "lua/server/BuildingObjects/ISPlace3DItemCursor_Fix.lua" "$bridges/lua/server/BuildingObjects/ISPlace3DItemCursor_Fix.lua"
same "lua/server/Tuning2/ATA2Tuning2.lua" "$bridges/lua/server/Tuning2/ATA2Tuning2.lua"
same "lua/server/utils/pzkZonesFunction.lua" "$bridges/lua/server/utils/pzkZonesFunction.lua"
same "lua/shared/BodyLocations.lua" "$bridges/lua/shared/BodyLocations.lua"
same "lua/shared/ISBaseTimedAction.lua" "$bridges/lua/shared/ISBaseTimedAction.lua"
same "lua/shared/SVU3_PZKVLCCars_Stuffs.lua" "$bridges/lua/shared/SVU3_PZKVLCCars_Stuffs.lua"
same "lua/shared/zzz_LCC_LegacyItemCallbacks.lua" "$bridges/lua/shared/zzz_LCC_LegacyItemCallbacks.lua"

# Defensive/UI fixes.
same "lua/client/zzz_LCC_AegisTransferGuard.lua" "$safety/lua/client/zzz_LCC_AegisTransferGuard.lua"
same "lua/client/zzz_LCC_ChimeraGhillieFix.lua" "$safety/lua/client/zzz_LCC_ChimeraGhillieFix.lua"

# The translation package must remain byte-for-byte equivalent to both monolith layers.
if ! diff -qr "$MONO/42/media/lua/shared/Translate" "$text42/lua/shared/Translate" >/dev/null; then
    error "42 translation tree differs from monolith"
fi
if ! diff -qr "$MONO/common" "$text_common" >/dev/null; then
    error "common translation tree differs from monolith"
fi

# Catch any new non-translation monolith file that has not been assigned to a split item.
tmp_actual="$(mktemp)"
tmp_mapped="$(mktemp)"
trap 'rm -f "$tmp_actual" "$tmp_mapped"' EXIT
find "$MONO/42/media" -type f ! -path '*/lua/shared/Translate/*' \
    -printf '%P\n' | sort > "$tmp_actual"
printf '%s\n' "${mapped_sources[@]}" | sort -u > "$tmp_mapped"
if ! diff -u "$tmp_actual" "$tmp_mapped"; then
    error "non-translation source coverage is incomplete or stale"
fi

# Workshop metadata contracts.
declare -A expected_ids=(
    [PatchCore]="LaccckaB4220PatchCore"
    [RuntimeFixes]="LaccckaB4220RuntimeFixes"
    [ActivityFixes]="LaccckaB4220ActivityFixes"
    [CompatibilityBridges]="LaccckaB4220CompatBridges"
    [SafetyFixes]="LaccckaB4220SafetyFixes"
    [RussianTextFixes]="LaccckaB4220RussianText"
)

seen_ids=""
for folder in "${!expected_ids[@]}"; do
    id="${expected_ids[$folder]}"
    workshop="$SPLIT/$folder/workshop.txt"
    modinfo="$SPLIT/$folder/Contents/mods/$id/42/mod.info"

    [[ -f "$workshop" ]] || { error "$folder: workshop.txt missing"; continue; }
    [[ -f "$modinfo" ]] || { error "$folder: mod.info missing"; continue; }

    grep -Fxq "id=$id" "$modinfo" || error "$folder: wrong mod ID"
    grep -Fxq 'versionMin=42.20.0' "$modinfo" || error "$folder: versionMin must stay on 42.20.0"
    grep -Fqi 'Do not use' "$workshop" || error "$folder: Workshop warning is missing"
    grep -Fqi 'original mods are not included' "$workshop" || error "$folder: no-bundled-mods disclaimer is missing"
    grep -Fxq 'id=0' "$workshop" || error "$folder: staging Workshop ID must remain 0 until publication"

    if grep -Fqx "$id" <<<"$seen_ids"; then
        error "$folder: duplicate mod ID $id"
    fi
    seen_ids+="$id"$'\n'
done

for folder in RuntimeFixes ActivityFixes CompatibilityBridges SafetyFixes; do
    id="${expected_ids[$folder]}"
    modinfo="$SPLIT/$folder/Contents/mods/$id/42/mod.info"
    grep -Fxq 'require=\LaccckaB4220PatchCore' "$modinfo" || error "$folder: PatchCore dependency missing"
done

if (( fail != 0 )); then
    exit 1
fi

printf 'Workshop split audit: OK\n'

#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
lua_root="$project_root/Contents/mods/LaccckaQuestFramework/42/media/lua"

constants="$lua_root/shared/LCCQF/LCCQFConstants.lua"
registry_def="$lua_root/shared/LCCQF/Core/LCCQFFactionRegistry.lua"
faction_defs="$lua_root/shared/LCCQF/Content/LCCQFFactionDefinitions.lua"
site_registry="$lua_root/server/LCCQF/FactionWorld/LCCQFFactionSiteRegistry.lua"
candidates="$lua_root/server/LCCQF/FactionWorld/LCCQFFactionSiteCandidateIndex.lua"
validator="$lua_root/server/LCCQF/FactionWorld/LCCQFFactionSiteSafetyValidator.lua"
scanner="$lua_root/server/LCCQF/FactionWorld/LCCQFFactionSiteResourceScanner.lua"
validation_service="$lua_root/server/LCCQF/FactionWorld/zz_LCCQFFactionSiteValidationService.lua"
allocator="$lua_root/server/LCCQF/FactionWorld/zz_LCCQFFactionSiteAllocator.lua"
debug_server="$lua_root/server/LCCQF/FactionWorld/LCCQFFactionSiteDebugServer.lua"
debug_client="$lua_root/client/LCCQF/Faction/LCCQFFactionSiteDebugClient.lua"
server_bootstrap="$lua_root/server/zz_LCCQFFactionBootstrap.lua"
client_bootstrap="$lua_root/client/LCCQF/zz_LCCQFRPGHubBootstrap.lua"

fail() {
    echo "QuestFramework faction site audit: FAIL: $1" >&2
    exit 1
}

require_pattern() {
    local pattern="$1"
    local file="$2"
    local message="$3"
    rg -q "$pattern" "$file" || fail "$message"
}

for required in "$constants" "$registry_def" "$faction_defs" "$site_registry" "$candidates" "$validator" "$scanner" "$validation_service" "$allocator" "$debug_server" "$debug_client" "$server_bootstrap" "$client_bootstrap"; do
    [[ -f "$required" ]] || fail "missing $required"
done

require_pattern 'FACTION_SITE_PERSISTENCE_SCHEMA_VERSION = 1' "$constants" "faction site schema constant missing"
require_pattern 'FACTION_SITE_MODDATA_KEY = "LCCQF_FactionSites"' "$constants" "faction site ModData key missing"
require_pattern 'FACTION_SITE_MAX_CANDIDATES' "$constants" "bounded candidate cap missing"
require_pattern 'FACTION_SITE_MAX_ROOMS_PER_PASS' "$constants" "bounded room discovery cap missing"
require_pattern 'FACTION_SITE_RESOURCE_SCAN_MAX_TILES' "$constants" "bounded resource scan cap missing"
require_pattern 'REQUEST_FACTION_SITES_DEBUG' "$constants" "privileged faction site debug request command missing"
require_pattern 'FACTION_SITES_DEBUG' "$constants" "faction site debug snapshot command missing"

require_pattern 'validateSiteProfile' "$registry_def" "faction site profile validation missing"
require_pattern 'wantsWater' "$registry_def" "implemented water requirement is not validated"
require_pattern 'wantsBeds' "$registry_def" "implemented bed requirement is not validated"
require_pattern 'minStorageContainers' "$registry_def" "implemented storage requirement is not validated"
require_pattern 'minFreeSpawnPoints' "$registry_def" "implemented spawn capacity requirement is not validated"
require_pattern 'wantsRoadAccess' "$registry_def" "unsupported road-access policy guard missing"
require_pattern 'is not implemented yet' "$registry_def" "unsupported site profile fields are silently accepted"

require_pattern 'siteProfile = \{' "$faction_defs" "authored faction site profile missing"
require_pattern 'preferredZones' "$faction_defs" "authored zone preferences missing"
require_pattern 'minDistanceFromPlayers' "$faction_defs" "authored player distance policy missing"
require_pattern 'minDistanceFromOtherFactionSites' "$faction_defs" "authored faction separation policy missing"
require_pattern 'wantsBeds = true' "$faction_defs" "checkpoint settlement bed requirement missing"
require_pattern 'wantsWater = true' "$faction_defs" "checkpoint settlement water requirement missing"
require_pattern 'minStorageContainers' "$faction_defs" "checkpoint settlement storage requirement missing"
require_pattern 'minFreeSpawnPoints' "$faction_defs" "checkpoint settlement spawn capacity requirement missing"
if rg -n 'baseX|baseY|baseZ|fixedX|fixedY|fixedZ' "$faction_defs"; then
    fail "faction content contains fixed base coordinate fields"
fi

require_pattern 'ModData\.getOrCreate\(C\.FACTION_SITE_MODDATA_KEY\)' "$site_registry" "persistent faction site store missing"
require_pattern 'siteIdsByFaction' "$site_registry" "faction-to-multiple-sites persistence missing"
require_pattern 'reservationsByCandidateKey' "$site_registry" "candidate reservation index missing"
require_pattern 'state = "RESERVED"' "$site_registry" "reservation state missing"
require_pattern 'function Sites\.Transition' "$site_registry" "site state transition API missing"
require_pattern 'DistanceToNearestOtherFactionSite' "$site_registry" "site separation query missing"

require_pattern 'getCell and getCell\(\)' "$candidates" "loaded-world-only discovery missing"
require_pattern 'getRoomList' "$candidates" "loaded room discovery missing"
require_pattern 'FACTION_SITE_MAX_ROOMS_PER_PASS' "$candidates" "room discovery is not bounded"
require_pattern 'FACTION_SITE_MAX_CANDIDATES' "$candidates" "candidate index is not bounded"
require_pattern 'buildingFingerprint' "$candidates" "reconstructible building fingerprint missing"
require_pattern 'room has no loaded sample square' "$candidates" "candidate index still permits metadata-only unloaded buildings"
require_pattern 'getMetaGrid' "$candidates" "meta-zone scoring context missing"
require_pattern 'preferredZones' "$candidates" "preferred zone scoring missing"
require_pattern 'avoidedZones' "$candidates" "avoided zone scoring missing"
require_pattern 'rejectionHistory' "$candidates" "repeated bad candidate penalty missing"

require_pattern 'SafeHouse\.isSafeHouse' "$validator" "SafeHouse exclusion missing"
require_pattern 'getOnlinePlayers' "$validator" "active player proximity validation missing"
require_pattern 'getFreeSquare' "$validator" "free room square validation missing"
require_pattern 'building fingerprint changed' "$validator" "live building fingerprint reconciliation missing"
require_pattern 'candidate overlaps a player SafeHouse' "$validator" "whole-building SafeHouse rejection missing"

require_pattern 'FACTION_SITE_RESOURCE_SCAN_MAX_TILES' "$scanner" "resource scanner is not bounded"
require_pattern 'room:getSquares' "$scanner" "resource scanner does not use live loaded room squares"
require_pattern 'IsoFlagType\.bed' "$scanner" "engine bed flag classifier missing"
require_pattern 'getFluidAmount' "$scanner" "Build 42 fluid water probe missing"
require_pattern 'IsoFlagType\.waterPiped' "$scanner" "plumbed water source probe missing"
require_pattern 'getAllEvalRecurse' "$scanner" "recursive food-container probe missing"
require_pattern 'IsoTelevision' "$scanner" "television classifier missing"
require_pattern 'IsoStove' "$scanner" "stove classifier missing"
require_pattern 'freeSpawnPoints' "$scanner" "free indoor spawn-point scan missing"
require_pattern 'safeHouseOverlap' "$scanner" "resource scan SafeHouse reconciliation missing"
require_pattern 'function Scanner\.Evaluate' "$scanner" "resource requirement evaluation missing"

require_pattern 'site.state == "RESERVED"' "$validation_service" "validation service does not consume reservations"
require_pattern 'Scanner\.Scan' "$validation_service" "validation service does not run live resource scan"
require_pattern 'Scanner\.Evaluate' "$validation_service" "validation service does not enforce faction requirements"
require_pattern 'site\.derived' "$validation_service" "resource scan is not persisted as reconstructible derived data"
require_pattern '"ABANDONED"' "$validation_service" "failed resource sites are not released"
require_pattern '"VALIDATING"' "$validation_service" "accepted resource sites do not advance state"
require_pattern 'Candidates\.NoteRejection' "$validation_service" "resource failures do not feed candidate rejection history"

require_pattern 'dryRun=true materialization=false' "$allocator" "allocator is not explicitly dry-run"
require_pattern 'Candidates\.DiscoverLoadedBuildings' "$allocator" "allocator does not use bounded loaded candidate discovery"
require_pattern 'Validator\.Validate' "$allocator" "allocator skips non-destructive safety validation"
require_pattern 'Sites\.ReserveCandidate' "$allocator" "allocator does not persist reservation"
require_pattern 'Events\.EveryOneMinute' "$allocator" "allocator is not globally throttled"
require_pattern 'isClient and isClient\(\)' "$allocator" "allocator lacks direct MP-client authority guard"
require_pattern 'isServer and isServer\(\)' "$allocator" "allocator client guard does not preserve server authority"

require_pattern 'REQUEST_FACTION_SITES_DEBUG' "$debug_server" "debug server request handler missing"
require_pattern 'isPrivileged' "$debug_server" "debug snapshot is not privilege gated"
require_pattern 'sendServerCommand' "$debug_server" "debug snapshot response missing"
require_pattern 'Sites\.ListSites' "$debug_server" "debug snapshot is not sourced from server registry"
require_pattern 'REQUEST_FACTION_SITES_DEBUG' "$debug_client" "admin debug client cannot request server sites"
require_pattern 'FACTION_SITES_DEBUG' "$debug_client" "admin debug client cannot consume server snapshot"
require_pattern 'ISWorldMap' "$debug_client" "admin faction-site map visualization missing"
require_pattern 'setUserDefined\(true\)' "$debug_client" "debug marker compatibility flag missing"
require_pattern 'Show faction sites on map' "$debug_client" "admin context action missing"

require_pattern 'LCCQF/FactionWorld/zz_LCCQFFactionSiteAllocator' "$server_bootstrap" "site allocator not bootstrapped"
require_pattern 'LCCQF/FactionWorld/zz_LCCQFFactionSiteValidationService' "$server_bootstrap" "site validation not bootstrapped"
require_pattern 'LCCQF/FactionWorld/LCCQFFactionSiteDebugServer' "$server_bootstrap" "privileged debug server not bootstrapped"
require_pattern 'isClient and isClient\(\)' "$server_bootstrap" "faction server bootstrap lacks MP-client authority guard"
require_pattern 'isServer and isServer\(\)' "$server_bootstrap" "faction bootstrap client guard does not preserve server authority"
require_pattern 'LCCQF/Faction/LCCQFFactionSiteDebugClient' "$client_bootstrap" "faction site debug client not bootstrapped"

allocator_guard_line="$(rg -n 'isClient and isClient\(\)' "$allocator" | head -n1 | cut -d: -f1)"
allocator_require_line="$(rg -n '^require ' "$allocator" | head -n1 | cut -d: -f1)"
bootstrap_guard_line="$(rg -n 'isClient and isClient\(\)' "$server_bootstrap" | head -n1 | cut -d: -f1)"
bootstrap_require_line="$(rg -n '^require ' "$server_bootstrap" | head -n1 | cut -d: -f1)"
[[ "$allocator_guard_line" -lt "$allocator_require_line" ]] || fail "allocator client guard must execute before server-domain requires"
[[ "$bootstrap_guard_line" -lt "$bootstrap_require_line" ]] || fail "faction bootstrap client guard must execute before server-domain requires"

core_world_files=("$site_registry" "$candidates" "$validator" "$scanner" "$validation_service" "$allocator")
if rg -n 'BanditServer|BanditCustom|BanditBrain|Bandits2|Bandit\.Spawner' "${core_world_files[@]}"; then
    fail "faction world allocation/validation layer leaks Bandits runtime dependency"
fi
if rg -n 'sendClientCommand|sendServerCommand|OnClientCommand' "${core_world_files[@]}"; then
    fail "faction world core exposes unnecessary client/network authority"
fi
if rg -n 'removeFromWorld|removeFromSquare|RemoveZombie|clearZombies|ClearZombies|setSquare|AddZombie' "${core_world_files[@]}"; then
    fail "faction site allocation/validation contains destructive world mutation"
fi
if rg -n 'loadstring|loadstream' "${core_world_files[@]}" "$registry_def" "$faction_defs" "$debug_server" "$debug_client"; then
    fail "Build 42.20.4 removed dynamic code execution API is present"
fi
if rg -n 'TEST_FACTION_ID|checkpoint_survivors|CheckpointSurvivors' "${core_world_files[@]}"; then
    fail "reusable faction site core contains authored test faction special-case"
fi

if command -v lua >/dev/null 2>&1; then
    for lua_file in "$constants" "$registry_def" "$faction_defs" "$site_registry" "$candidates" "$validator" "$scanner" "$validation_service" "$allocator" "$debug_server" "$debug_client" "$server_bootstrap" "$client_bootstrap"; do
        lua -e "assert(loadfile([[$lua_file]]))"
    done
fi

echo "QuestFramework faction site audit: PASS (server-authoritative autonomous reservation + bounded live resource validation + privileged map diagnostics)"

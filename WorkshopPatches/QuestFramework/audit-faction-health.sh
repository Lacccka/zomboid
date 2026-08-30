#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
lua_root="$project_root/Contents/mods/LaccckaQuestFramework/42/media/lua"
health="$lua_root/server/LCCQF/FactionWorld/zz_LCCQFFactionSiteHealthService.lua"
bootstrap="$lua_root/server/zz_LCCQFFactionBootstrap.lua"

fail() {
    echo "QuestFramework faction health audit: FAIL: $1" >&2
    exit 1
}

require_pattern() {
    local pattern="$1"
    local file="$2"
    local message="$3"
    rg -q "$pattern" "$file" || fail "$message"
}

[[ -f "$health" ]] || fail "missing health service"
[[ -f "$bootstrap" ]] || fail "missing faction bootstrap"

require_pattern 'FactionSiteResourceScanner' "$health" "health service does not use bounded resource scanner"
require_pattern 'FactionSiteRelocationService' "$health" "health service cannot request relocation"
require_pattern 'UNLOADED_RETRY_HOURS' "$health" "unloaded sites are not throttled"
require_pattern 'RESOURCE_FAILURE_THRESHOLD = 2' "$health" "transient resource failures are not debounced"
require_pattern 'result\.safeHouseOverlap == true' "$health" "SafeHouse hard relocation trigger missing"
require_pattern 'result\.complete == false' "$health" "incomplete scans are treated as hard failures"
require_pattern 'Candidates\.NoteRejection' "$health" "failed active site is not penalized for reallocation"
require_pattern 'site\.state == "ACTIVE" or site\.state == "DORMANT"' "$health" "health service scans wrong site states"
require_pattern 'Events\.EveryOneMinute' "$health" "health service is not scheduled"
require_pattern 'zz_LCCQFFactionSiteHealthService' "$bootstrap" "health service not bootstrapped"

if rg -n 'BanditServer|BanditCustom|BanditBrain|BanditClusters|sendClientCommand|OnClientCommand' "$health"; then
    fail "provider-neutral health service leaks runtime/client authority"
fi
if rg -n 'removeFromWorld|removeFromSquare|RemoveZombie|clearZombies|ClearZombies' "$health"; then
    fail "health service destructively changes world entities"
fi

if command -v lua >/dev/null 2>&1; then
    lua -e "assert(loadfile([[$health]]))"
fi

echo "QuestFramework faction health audit: PASS (loaded-world health + debounced resource failure + SafeHouse relocation)"

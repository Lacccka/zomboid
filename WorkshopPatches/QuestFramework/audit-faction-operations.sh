#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
lua_root="$project_root/Contents/mods/LaccckaQuestFramework/42/media/lua"

job_registry="$lua_root/shared/LCCQF/Core/LCCQFFactionJobRegistry.lua"
job_defs="$lua_root/shared/LCCQF/Content/LCCQFFactionJobDefinitions.lua"
faction_defs="$lua_root/shared/LCCQF/Content/LCCQFFactionDefinitions.lua"
operations="$lua_root/server/LCCQF/FactionWorld/LCCQFFactionSiteOperations.lua"
operations_service="$lua_root/server/LCCQF/FactionWorld/zz_LCCQFFactionSiteOperationsService.lua"
economy="$lua_root/server/LCCQF/FactionWorld/LCCQFFactionSiteEconomy.lua"
projection="$lua_root/server/LCCQF/Runtime/LCCQFBanditsFactionOperationsProjection.lua"
projection_service="$lua_root/server/LCCQF/FactionWorld/zz_LCCQFFactionSiteOperationsProjectionService.lua"
guard_program="$lua_root/shared/LCCQF/Runtime/LCCQFBanditsFactionGuardProgram.lua"
bootstrap="$lua_root/server/zz_LCCQFFactionBootstrap.lua"
debug_server="$lua_root/server/LCCQF/FactionWorld/LCCQFFactionSiteDebugServer.lua"

fail() {
    echo "QuestFramework faction operations audit: FAIL: $1" >&2
    exit 1
}

require_pattern() {
    local pattern="$1"
    local file="$2"
    local message="$3"
    rg -q "$pattern" "$file" || fail "$message"
}

required_files=(
    "$job_registry" "$job_defs" "$faction_defs" "$operations" "$operations_service" "$economy"
    "$projection" "$projection_service" "$guard_program" "$bootstrap" "$debug_server"
)
for required in "${required_files[@]}"; do [[ -f "$required" ]] || fail "missing $required"; done

require_pattern 'function Registry\.ValidateOperationsProfile' "$job_registry" "operations profile validation missing"
require_pattern 'exactly one schedule or rotation' "$job_registry" "schedule/rotation exclusivity validation missing"
require_pattern 'Registry\.IsRegistered\(entry\.jobId\)' "$job_registry" "schedule jobs are not registry validated"
require_pattern 'rotation\.activeCount' "$job_registry" "rotation active count validation missing"
require_pattern 'profile\.supplies' "$job_registry" "supply policy validation missing"
require_pattern 'minimumPerResident' "$job_registry" "per-resident supply threshold validation missing"

for job in guard command rest work; do
    require_pattern "jobId = \"$job\"" "$job_defs" "missing built-in $job job"
done

require_pattern 'operationsProfile = operationsProfile' "$faction_defs" "checkpoint faction has no operations profile"
require_pattern 'jobId = "command"' "$faction_defs" "leader command schedule missing"
require_pattern 'jobId = "rest"' "$faction_defs" "rest schedule missing"
require_pattern 'shiftHours = 12' "$faction_defs" "guard rotation missing"
require_pattern 'activeCount = 1' "$faction_defs" "guard coverage intent missing"
require_pattern 'category = "food"' "$faction_defs" "checkpoint food reserve policy missing"
require_pattern 'minimumPerResident = 2' "$faction_defs" "checkpoint per-resident food policy missing"

require_pattern 'function Operations\.ResolveTarget' "$operations" "site target resolver missing"
require_pattern 'function Operations\.UpdateSite' "$operations" "operations update API missing"
require_pattern 'member\.assignment' "$operations" "job assignment is not persisted with logical member"
require_pattern 'scheduleKey' "$operations" "assignment schedule identity missing"
require_pattern 'housingDeficit' "$operations" "housing need missing"
require_pattern 'waterSourceMissing' "$operations" "water need missing"
require_pattern 'foodAccessMissing' "$operations" "food-access need missing"
require_pattern 'require "LCCQF/FactionWorld/LCCQFFactionSiteEconomy"' "$operations" \
    "operations does not depend on the canonical economy projection"
require_pattern 'Economy\.ListSupplies\(site\)' "$operations" \
    "operations re-computes supply policy instead of consuming economy rows"
require_pattern 'unitKind = row\.unitKind' "$operations" "supply needs lose unit kind"
require_pattern 'precision = row\.precision' "$operations" "supply needs lose quantity precision"
require_pattern 'splittable = row\.splittable == true' "$operations" "supply needs lose split semantics"
require_pattern 'local deficit = math\.max\(0, tonumber\(need\.deficit\) or 0\)' "$operations" \
    "supply signal floors measured deficits"
require_pattern 'signal\.unitKind = need\.unitKind' "$operations" "supply signal loses unit kind"
require_pattern 'signal\.precision = need\.precision' "$operations" "supply signal loses precision"
require_pattern 'signal\.economyRevision = need\.economyRevision' "$operations" \
    "supply signal is not tied to economy revision"
require_pattern ':need:supply:' "$operations" "stable supply signal IDs missing"
require_pattern 'kind = "supply"' "$operations" "supply signal classification missing"
require_pattern 'status = shouldOpen and "OPEN" or "RESOLVED"' "$operations" "infrastructure signal lifecycle missing"
require_pattern 'GetOpenSignals' "$operations" "open signal query missing"

if rg -q 'Stock\.GetCategoryQuantity|minimumPerResident.*reserve|math\.ceil\(\(livingPopulation' "$operations"; then
    fail "operations duplicates stock/reserve policy authority instead of projecting economy"
fi

# Operations may observe logical economy state but must never mutate physical inventory.
if rg -n 'ConsumeItem|AddItem|AddItems|RemoveItem|RemoveOneOf|RemoveAll' "$operations"; then
    fail "operations mutates physical stock"
fi

require_pattern 'definition\.operationsProfile' "$operations_service" "operations service ignores faction content"
require_pattern 'site\.state == "VALIDATING"' "$operations_service" "assignments are not planned before first spawn"
require_pattern 'Operations\.UpdateSite' "$operations_service" "operations service bypasses logical operations model"

require_pattern 'function Adapter\.ApplyOperations' "$projection" "Bandits operations projection missing"
require_pattern 'lccqJobId' "$projection" "job ID is not projected to runtime"
require_pattern 'lccqDutyMode' "$projection" "duty mode is not projected to runtime"
require_pattern 'lccqDutyX' "$projection" "duty target is not projected to runtime"
require_pattern 'member\.runtimeId' "$projection" "projection does not prefer authoritative runtime binding"

require_pattern 'Materializers\.Get\(profile\.materializer\)' "$projection_service" "operations projection bypasses provider registry"
require_pattern 'adapter\.ApplyOperations' "$projection_service" "provider projection method not used"

require_pattern 'lccqDutyMode' "$guard_program" "physical program ignores duty assignment"
require_pattern 'dutyMode == "rest" or dutyMode == "work"' "$guard_program" "rest/work physical modes missing"
require_pattern 'dutyMode == "command"' "$guard_program" "command physical mode missing"
require_pattern 'home\.returnRadius' "$guard_program" "home leash disappeared"

require_pattern 'zz_LCCQFFactionSiteStockService' "$bootstrap" "stock service not bootstrapped"
require_pattern 'zz_LCCQFFactionSiteEconomyService' "$bootstrap" "economy service not bootstrapped"
require_pattern 'zz_LCCQFFactionSiteOperationsService' "$bootstrap" "logical operations service not bootstrapped"
require_pattern 'LCCQFBanditsFactionOperationsProjection' "$bootstrap" "Bandits operations projection not bootstrapped"
require_pattern 'zz_LCCQFFactionSiteOperationsProjectionService' "$bootstrap" "operations projection service not bootstrapped"

stock_line="$(rg -n 'zz_LCCQFFactionSiteStockService' "$bootstrap" | head -n1 | cut -d: -f1)"
economy_line="$(rg -n 'zz_LCCQFFactionSiteEconomyService' "$bootstrap" | head -n1 | cut -d: -f1)"
operations_line="$(rg -n 'zz_LCCQFFactionSiteOperationsService' "$bootstrap" | head -n1 | cut -d: -f1)"
materialization_line="$(rg -n 'zz_LCCQFFactionSiteMaterializationService' "$bootstrap" | head -n1 | cut -d: -f1)"
projection_line="$(rg -n 'zz_LCCQFFactionSiteOperationsProjectionService' "$bootstrap" | head -n1 | cut -d: -f1)"
[[ "$stock_line" -lt "$economy_line" && "$economy_line" -lt "$operations_line" ]] \
    || fail "stock -> economy -> operations ordering is not preserved"
[[ "$operations_line" -lt "$materialization_line" ]] || fail "logical assignments must exist before first physical spawn"
[[ "$projection_line" -gt "$materialization_line" ]] || fail "physical operations projection must run after first spawn"

require_pattern 'assignment = sanitizeAssignment' "$debug_server" "admin diagnostics omit member assignment"
require_pattern 'operations = sanitizeOperations' "$debug_server" "admin diagnostics omit operations state"
require_pattern 'signals = signals' "$debug_server" "admin diagnostics omit settlement signals"

core_files=("$job_registry" "$job_defs" "$operations" "$operations_service" "$projection_service")
if rg -n 'BanditServer|BanditCustom|BanditBrain|BanditClusters|Bandits2' "${core_files[@]}"; then
    fail "provider-neutral operations core leaks Bandits dependency"
fi
if rg -n 'sendClientCommand|OnClientCommand' "$operations" "$operations_service" "$projection_service"; then
    fail "client can mutate server operations"
fi
if rg -n 'removeFromWorld|removeFromSquare|RemoveZombie|clearZombies|ClearZombies' "$projection" "$guard_program"; then
    fail "operations destructively removes world entities"
fi

if command -v lua >/dev/null 2>&1; then
    for lua_file in "${required_files[@]}"; do lua -e "assert(loadfile([[$lua_file]]))"; done
fi

echo "QuestFramework faction operations audit: PASS (server jobs + deterministic schedules + infrastructure needs + quantity-aware economy-backed supply signals + provider projection)"

#!/usr/bin/env bash
set -euo pipefail

ROOT="WorkshopPatches/QuestFramework"
MOD="$ROOT/Contents/mods/LaccckaQuestFramework/42/media/lua"
EXECUTOR="$MOD/server/LCCQF/FactionWorld/LCCQFFactionSiteConsumptionExecutor.lua"
SERVICE="$MOD/server/LCCQF/FactionWorld/zz_LCCQFFactionSiteConsumptionExecutorService.lua"
PLAN="$MOD/server/LCCQF/FactionWorld/LCCQFFactionSiteConsumptionPlan.lua"
STOCK="$MOD/server/LCCQF/FactionWorld/LCCQFFactionSiteStock.lua"
CATEGORIES="$MOD/shared/LCCQF/Core/LCCQFSupplyCategoryRegistry.lua"
CONSTANTS="$MOD/shared/LCCQF/LCCQFConstants.lua"
BOOTSTRAP="$MOD/server/zz_LCCQFFactionBootstrap.lua"

fail() {
    echo "[faction-consumption-executor-audit] ERROR: $*" >&2
    exit 1
}

for file in "$EXECUTOR" "$SERVICE" "$PLAN" "$STOCK" "$CATEGORIES" "$CONSTANTS" "$BOOTSTRAP"; do
    [[ -f "$file" ]] || fail "missing executor dependency: $file"
done

rg -q 'function Registry.Matches\(categoryId, item\)' "$CATEGORIES" \
    || fail "supply categories expose no shared physical item predicate"
rg -q 'Categories\.Classify\(item\)' "$STOCK" \
    || fail "stock scanner does not use the shared supply category registry"
rg -q 'Categories\.Matches\(category, item\)' "$EXECUTOR" \
    || fail "executor reimplements or bypasses supply category semantics"

rg -q 'FACTION_SITE_CONSUMPTION_MAX_ITEMS_PER_PASS = 16' "$CONSTANTS" \
    || fail "physical consumption pass is not explicitly bounded"
rg -q 'math\.min\(pending, available, maxPerPass\)' "$EXECUTOR" \
    || fail "executor does not enforce pending/available/pass bounds"

rg -q 'Resolver\.Resolve\(snapshotRow\.locator\)' "$EXECUTOR" \
    || fail "executor does not re-resolve exact persisted container locators"
rg -q 'itemId = id' "$EXECUTOR" \
    || fail "prepared transaction does not persist exact item IDs"
rg -q 'collectLiveItemIds' "$EXECUTOR" \
    || fail "executor has no post-mutation site-wide item-id reconciliation"
rg -q 'liveIds\[tonumber\(descriptor\.itemId\)\]' "$EXECUTOR" \
    || fail "reconciliation does not prove exact item IDs disappeared"

rg -q 'return sendRemoveItemFromContainer ~= nil' "$EXECUTOR" \
    || fail "executor does not fail closed when the Lua container sync API is unavailable"
rg -q 'container:Remove\(item\)' "$EXECUTOR" \
    || fail "executor does not perform authoritative ItemContainer removal"
rg -q 'sendRemoveItemFromContainer\(container, item\)' "$EXECUTOR" \
    || fail "executor does not use the Build 42 LuaManager removal sync wrapper"
if rg -q 'GameServer\.sendRemoveItemFromContainer' "$EXECUTOR"; then
    fail "executor bypasses the official LuaManager container removal wrapper"
fi

rg -q 'state = "PREPARED"' "$EXECUTOR" \
    || fail "executor has no persisted PREPARED transaction state"
rg -q 'tx\.state = "MUTATED"' "$EXECUTOR" \
    || fail "executor has no persisted MUTATED transaction state"
rg -q 'descriptor\.state = "REMOVING"' "$EXECUTOR" \
    || fail "executor cannot recover a crash around exact item removal"
rg -q 'descriptor\.state = "REMOVED"' "$EXECUTOR" \
    || fail "executor does not record proven physical removals"
rg -q 'freshStock\(site\)' "$EXECUTOR" \
    || fail "executor lacks pre/post physical stock reconciliation"

rg -q 'Plan\.AcknowledgeApplied\(site, tx\.supplyId, applied, tx\.txId\)' "$EXECUTOR" \
    || fail "executor does not bind logical acknowledgement to transaction identity"
rg -q 'lastAcknowledgedTransactionId == txId' "$PLAN" \
    || fail "consumption acknowledgement is not idempotent across restart/retry"
rg -q 'row\.execution = nil' "$EXECUTOR" \
    || fail "completed physical transaction is never retired"

if rg -q 'sendClientCommand|OnClientCommand' "$EXECUTOR" "$SERVICE"; then
    fail "physical settlement consumption must remain server authoritative"
fi
if rg -q 'getZombieList|getObjectList|IsoWorld\.instance|forceLoad|LoadChunk' "$EXECUTOR" "$SERVICE"; then
    fail "physical consumption introduced an unbounded/forced world traversal"
fi

rg -q 'site.state == "ACTIVE" or site.state == "DORMANT"' "$SERVICE" \
    || fail "executor service does not preserve dormant fail-closed reconciliation"
rg -q 'Economy\.Refresh\(site, definition\)' "$SERVICE" \
    || fail "post-consumption economy is not refreshed before downstream operations"

stock_line=$(rg -n 'zz_LCCQFFactionSiteStockService' "$BOOTSTRAP" | head -n1 | cut -d: -f1)
economy_line=$(rg -n 'zz_LCCQFFactionSiteEconomyService' "$BOOTSTRAP" | head -n1 | cut -d: -f1)
plan_line=$(rg -n 'zz_LCCQFFactionSiteConsumptionService' "$BOOTSTRAP" | head -n1 | cut -d: -f1)
exec_line=$(rg -n 'zz_LCCQFFactionSiteConsumptionExecutorService' "$BOOTSTRAP" | head -n1 | cut -d: -f1)
operations_line=$(rg -n 'zz_LCCQFFactionSiteOperationsService' "$BOOTSTRAP" | head -n1 | cut -d: -f1)
[[ -n "$stock_line" && -n "$economy_line" && -n "$plan_line" && -n "$exec_line" && -n "$operations_line" \
   && "$stock_line" -lt "$economy_line" && "$economy_line" -lt "$plan_line" \
   && "$plan_line" -lt "$exec_line" && "$exec_line" -lt "$operations_line" ]] \
    || fail "bootstrap order must be stock -> economy -> plan -> physical executor -> operations"

echo "[faction-consumption-executor-audit] OK"

#!/usr/bin/env bash
set -euo pipefail

ROOT="WorkshopPatches/QuestFramework"
MOD="$ROOT/Contents/mods/LaccckaQuestFramework/42/media/lua"
PLAN="$MOD/server/LCCQF/FactionWorld/LCCQFFactionSiteConsumptionPlan.lua"
SERVICE="$MOD/server/LCCQF/FactionWorld/zz_LCCQFFactionSiteConsumptionService.lua"
DEFINITION="$MOD/shared/LCCQF/Content/LCCQFFactionDefinitions.lua"
BOOTSTRAP="$MOD/server/zz_LCCQFFactionBootstrap.lua"

fail() {
    echo "[faction-consumption-audit] ERROR: $*" >&2
    exit 1
}

for file in "$PLAN" "$SERVICE" "$DEFINITION" "$BOOTSTRAP"; do
    [[ -f "$file" ]] || fail "missing consumption file: $file"
done

rg -q 'consumptionPerResidentPerDay = 1' "$DEFINITION" \
    || fail "test faction has no explicit food consumption policy"
rg -q 'pendingUnits' "$PLAN" \
    || fail "consumption planner does not retain pending demand"
rg -q 'accruedFraction' "$PLAN" \
    || fail "consumption planner loses sub-unit demand"
rg -q 'MAX_CATCHUP_HOURS' "$PLAN" \
    || fail "consumption catch-up is unbounded"
rg -q 'elapsed < MIN_INTERVAL_HOURS' "$PLAN" \
    || fail "planner can persist on every minute tick"
rg -q 'site.state == "ACTIVE" or site.state == "DORMANT"' "$SERVICE" \
    || fail "dormant settlements do not accrue logical consumption demand"
rg -q 'function Plan.AcknowledgeApplied' "$PLAN" \
    || fail "future exact-world executor has no bounded acknowledgement boundary"

if rg -q 'AddItem|RemoveItem|DoRemoveItem|sendAddItemToContainer|sendRemoveItemFromContainer|InventoryItemFactory' "$PLAN" "$SERVICE"; then
    fail "consumption planning layer must not mutate physical stock"
fi
if rg -q 'sendClientCommand|OnClientCommand' "$PLAN" "$SERVICE"; then
    fail "consumption planning must remain server authoritative"
fi

stock_line=$(rg -n 'zz_LCCQFFactionSiteStockService' "$BOOTSTRAP" | head -n1 | cut -d: -f1)
economy_line=$(rg -n 'zz_LCCQFFactionSiteEconomyService' "$BOOTSTRAP" | head -n1 | cut -d: -f1)
consumption_line=$(rg -n 'zz_LCCQFFactionSiteConsumptionService' "$BOOTSTRAP" | head -n1 | cut -d: -f1)
operations_line=$(rg -n 'zz_LCCQFFactionSiteOperationsService' "$BOOTSTRAP" | head -n1 | cut -d: -f1)
[[ -n "$stock_line" && -n "$economy_line" && -n "$consumption_line" && -n "$operations_line" \
   && "$stock_line" -lt "$economy_line" && "$economy_line" -lt "$consumption_line" \
   && "$consumption_line" -lt "$operations_line" ]] \
    || fail "bootstrap order must be stock -> economy -> consumption -> operations"

echo "[faction-consumption-audit] OK"

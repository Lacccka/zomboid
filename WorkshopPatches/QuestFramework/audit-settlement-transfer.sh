#!/usr/bin/env bash
set -euo pipefail

ROOT="WorkshopPatches/QuestFramework"
MOD="$ROOT/Contents/mods/LaccckaQuestFramework/42/media/lua"
CONSTANTS="$MOD/shared/LCCQF/LCCQFConstants.lua"
CATEGORIES="$MOD/shared/LCCQF/Core/LCCQFSupplyCategoryRegistry.lua"
CLIENT="$MOD/client/LCCQF/FactionWorld/LCCQFSettlementTransferObserver.lua"
SERVER="$MOD/server/LCCQF/FactionWorld/LCCQFSettlementTransferObserver.lua"

fail() {
    echo "[settlement-transfer-audit] ERROR: $*" >&2
    exit 1
}

[[ -f "$CLIENT" ]] || fail "client transfer observer missing"
[[ -f "$SERVER" ]] || fail "server transfer observer missing"
[[ -f "$CATEGORIES" ]] || fail "supply category registry missing"

rg -q 'REPORT_SETTLEMENT_TRANSFER_INTENT = "ReportSettlementTransferIntent"' "$CONSTANTS" \
    || fail "transfer intent command missing"
rg -q 'FACTION_SITE_TRANSFER_INTENT_TTL_MS' "$CONSTANTS" \
    || fail "transfer intent TTL missing"
rg -q 'FACTION_SITE_TRANSFER_REFRESH_DEBOUNCE_MS' "$CONSTANTS" \
    || fail "transfer refresh debounce missing"

rg -q 'require "TimedActions/ISInventoryTransferAction"' "$CLIENT" \
    || fail "client must hook vanilla inventory transfer action"
rg -q 'function ISInventoryTransferAction:new\(' "$CLIENT" \
    || fail "client early per-item intent hook missing"
rg -q 'function ISInventoryTransferAction:checkQueueList\(\)' "$CLIENT" \
    || fail "client batch pre-transaction hook missing"
rg -q 'reportItemIntent\(action, item\)' "$CLIENT" \
    || fail "client does not report exact item intent"
rg -q 'reportQueuedIntents\(self\)' "$CLIENT" \
    || fail "client does not report all batched item IDs"
rg -q 'originalCheckQueueList\(self\)' "$CLIENT" \
    || fail "client does not delegate vanilla queue batching"

rg -q 'findOwnedItem\(player, itemIdValue\)' "$SERVER" \
    || fail "server pre-transfer ownership verification missing"
rg -q 'Resolver.Resolve\(locator\)' "$SERVER" \
    || fail "server exact destination resolution missing"
rg -q 'Resolver.MakeLocator\(object, container\)' "$SERVER" \
    || fail "server canonical locator rebuild missing"
rg -q 'site.stock and site.stock.containers' "$SERVER" \
    || fail "server does not constrain destination to observed settlement stock"
rg -q 'findDirectItem\(container, entry.itemId\)' "$SERVER" \
    || fail "server same-item destination reconciliation missing"
rg -q 'if findOwnedItem\(player, entry.itemId\) then return false end' "$SERVER" \
    || fail "server ownership disappearance confirmation missing"
rg -q 'require "LCCQF/Content/LCCQFSupplyCategoryDefinitions"' "$SERVER" \
    || fail "server observer does not load canonical supply categories"
rg -q 'Categories\.Classify\(item\)' "$SERVER" \
    || fail "server-confirmed event bypasses measured supply category semantics"
rg -q 'categories = itemCategories\(item\)' "$SERVER" \
    || fail "confirmed transfer event lacks server-observed categories"
rg -q 'function Registry\.Classify\(item\)' "$CATEGORIES" \
    || fail "canonical supply classifier missing"

rg -q 'require "LCCQF/FactionWorld/LCCQFFactionSiteEconomy"' "$SERVER" \
    || fail "confirmed delivery chain does not load settlement economy"
rg -q 'Stock\.Refresh\(site\)' "$SERVER" \
    || fail "confirmed delivery does not refresh observed stock"
rg -q 'Economy\.Refresh\(site, definition\)' "$SERVER" \
    || fail "confirmed delivery does not refresh quantity-aware economy"
rg -q 'Operations\.UpdateSite\(site, definition\)' "$SERVER" \
    || fail "confirmed delivery does not refresh settlement needs"
rg -q 'event\.stockRefreshOk = pipelineOk == true' "$SERVER" \
    || fail "confirmed transfer can escape before full stock/economy/operations refresh"
rg -q 'event\.economyRevision' "$SERVER" \
    || fail "confirmed event omits economy revision evidence"
rg -q 'event\.operationsRevision' "$SERVER" \
    || fail "confirmed event omits operations revision evidence"

stock_line=$(rg -n 'Stock\.Refresh\(site\)' "$SERVER" | head -n1 | cut -d: -f1)
economy_line=$(rg -n 'Economy\.Refresh\(site, definition\)' "$SERVER" | head -n1 | cut -d: -f1)
operations_line=$(rg -n 'Operations\.UpdateSite\(site, definition\)' "$SERVER" | head -n1 | cut -d: -f1)
emit_line=$(rg -n 'emit\(event\)' "$SERVER" | tail -n1 | cut -d: -f1)
[[ -n "$stock_line" && -n "$economy_line" && -n "$operations_line" && -n "$emit_line" \
   && "$stock_line" -lt "$economy_line" && "$economy_line" -lt "$operations_line" \
   && "$operations_line" -lt "$emit_line" ]] \
    || fail "confirmed transfer ordering must be stock -> economy -> operations -> emit"

if rg -q 'item:IsFood\(\)' "$SERVER"; then
    fail "transfer observer must not reimplement category-specific item predicates"
fi
if rg -q '(AddItem|Remove)\(' "$SERVER"; then
    fail "server observer must not create, clone, or remove inventory items"
fi

echo "[settlement-transfer-audit] OK"

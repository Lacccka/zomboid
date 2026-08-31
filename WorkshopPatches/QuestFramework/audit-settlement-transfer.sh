#!/usr/bin/env bash
set -euo pipefail

ROOT="WorkshopPatches/QuestFramework"
MOD="$ROOT/Contents/mods/LaccckaQuestFramework/42/media/lua"
CONSTANTS="$MOD/shared/LCCQF/LCCQFConstants.lua"
CLIENT="$MOD/client/LCCQF/FactionWorld/LCCQFSettlementTransferObserver.lua"
SERVER="$MOD/server/LCCQF/FactionWorld/LCCQFSettlementTransferObserver.lua"

fail() {
    echo "[settlement-transfer-audit] ERROR: $*" >&2
    exit 1
}

[[ -f "$CLIENT" ]] || fail "client transfer observer missing"
[[ -f "$SERVER" ]] || fail "server transfer observer missing"

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
rg -q 'item:IsFood\(\)' "$SERVER" \
    || fail "server-confirmed event does not classify real delivered food"
rg -q 'categories = itemCategories\(item\)' "$SERVER" \
    || fail "confirmed transfer event lacks server-observed categories"
rg -q 'Stock.Refresh\(site\)' "$SERVER" \
    || fail "confirmed delivery does not refresh observed stock"
rg -q 'Operations.UpdateSite\(site, definition\)' "$SERVER" \
    || fail "confirmed delivery does not refresh settlement needs"

if rg -q '(AddItem|Remove)\(' "$SERVER"; then
    fail "server observer must not create, clone, or remove inventory items"
fi

echo "[settlement-transfer-audit] OK"

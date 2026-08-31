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
rg -q 'function ISInventoryTransferAction:start\(\)' "$CLIENT" \
    || fail "client pre-transaction start hook missing"
rg -q 'reportIntent\(self\)' "$CLIENT" \
    || fail "client does not report pre-transaction intent"
rg -q 'return originalStart\(self\)' "$CLIENT" \
    || fail "client does not delegate to vanilla transfer"

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
rg -q 'Stock.Refresh\(site\)' "$SERVER" \
    || fail "confirmed delivery does not refresh observed stock"
rg -q 'Operations.UpdateSite\(site, definition\)' "$SERVER" \
    || fail "confirmed delivery does not refresh settlement needs"

if rg -q '(AddItem|Remove)\(' "$SERVER"; then
    fail "server observer must not create, clone, or remove inventory items"
fi

echo "[settlement-transfer-audit] OK"

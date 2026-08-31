#!/usr/bin/env bash
set -euo pipefail

ROOT="WorkshopPatches/QuestFramework"
MOD="$ROOT/Contents/mods/LaccckaQuestFramework/42/media/lua"
REGISTRY="$MOD/shared/LCCQF/Core/LCCQFSupplyCategoryRegistry.lua"
DEFINITIONS="$MOD/shared/LCCQF/Content/LCCQFSupplyCategoryDefinitions.lua"
ECONOMY="$MOD/server/LCCQF/FactionWorld/LCCQFFactionSiteEconomy.lua"
SERVICE="$MOD/server/LCCQF/FactionWorld/zz_LCCQFFactionSiteEconomyService.lua"
BOOTSTRAP="$MOD/server/zz_LCCQFFactionBootstrap.lua"

fail() {
    echo "[faction-economy-audit] ERROR: $*" >&2
    exit 1
}

for file in "$REGISTRY" "$DEFINITIONS" "$ECONOMY" "$SERVICE" "$BOOTSTRAP"; do
    [[ -f "$file" ]] || fail "missing economy file: $file"
done

rg -q 'function Registry.Register\(definition\)' "$REGISTRY" \
    || fail "supply category registry has no registration API"
rg -q 'function Registry.Classify\(item\)' "$REGISTRY" \
    || fail "supply category registry has no classifier API"
rg -q 'function Registry.Measure\(categoryId, item\)' "$REGISTRY" \
    || fail "supply category registry has no quantity measurement API"
rg -q 'item:IsFood\(\)' "$REGISTRY" \
    || fail "food classifier is not backed by the B42 item API"
rg -q 'categoryId = "food"' "$DEFINITIONS" \
    || fail "food supply category is not registered"

rg -q 'sourceStockRevision' "$ECONOMY" \
    || fail "economy snapshot is not tied to a verified stock revision"
rg -q 'livingPopulation' "$ECONOMY" \
    || fail "economy snapshot does not account for logical population"
rg -q 'deficit = normalizeQuantity\(categoryId' "$ECONOMY" \
    || fail "economy snapshot has no quantity-aware deficit metric"
rg -q 'surplus = normalizeQuantity\(categoryId' "$ECONOMY" \
    || fail "economy snapshot has no quantity-aware surplus metric"
rg -q 'coverage = coverageRatio\(categoryId' "$ECONOMY" \
    || fail "economy snapshot has no quantity-aware coverage metric"
rg -q 'status = statusFor\(categoryId' "$ECONOMY" \
    || fail "economy snapshot has no quantity-aware scarcity status"
rg -q 'unitKind = semantics\.unitKind' "$ECONOMY" \
    || fail "economy row does not expose category unit semantics"
rg -q 'precision = semantics\.precision' "$ECONOMY" \
    || fail "economy row does not expose category precision"
rg -q 'Categories.IsRegistered\(categoryId\)' "$ECONOMY" \
    || fail "economy policy can reference unregistered supply categories"
rg -q 'Sites.MarkDirty\(site.siteId, "settlement economy snapshot changed"\)' "$ECONOMY" \
    || fail "economy changes are not persisted through the site registry"

if rg -q 'AddItem|RemoveItem|DoRemoveItem|sendAddItemToContainer|sendRemoveItemFromContainer|InventoryItemFactory' "$ECONOMY" "$SERVICE"; then
    fail "economy observation layer must not mutate physical inventory"
fi
if rg -q 'sendClientCommand|OnClientCommand' "$ECONOMY" "$SERVICE"; then
    fail "economy foundation must not add a client authority surface"
fi

stock_line=$(rg -n 'zz_LCCQFFactionSiteStockService' "$BOOTSTRAP" | head -n1 | cut -d: -f1)
economy_line=$(rg -n 'zz_LCCQFFactionSiteEconomyService' "$BOOTSTRAP" | head -n1 | cut -d: -f1)
operations_line=$(rg -n 'zz_LCCQFFactionSiteOperationsService' "$BOOTSTRAP" | head -n1 | cut -d: -f1)
[[ -n "$stock_line" && -n "$economy_line" && -n "$operations_line" \
   && "$stock_line" -lt "$economy_line" && "$economy_line" -lt "$operations_line" ]] \
    || fail "bootstrap order must be stock -> economy -> operations"

echo "[faction-economy-audit] OK"

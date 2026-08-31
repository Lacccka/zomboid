#!/usr/bin/env bash
set -euo pipefail

ROOT="WorkshopPatches/QuestFramework"
MOD="$ROOT/Contents/mods/LaccckaQuestFramework/42/media/lua"
REGISTRY="$MOD/shared/LCCQF/Core/LCCQFSupplyCategoryRegistry.lua"
DEFINITIONS="$MOD/shared/LCCQF/Content/LCCQFSupplyCategoryDefinitions.lua"
STOCK="$MOD/server/LCCQF/FactionWorld/LCCQFFactionSiteStock.lua"
ECONOMY="$MOD/server/LCCQF/FactionWorld/LCCQFFactionSiteEconomy.lua"
EXEC_SERVICE="$MOD/server/LCCQF/FactionWorld/zz_LCCQFFactionSiteConsumptionExecutorService.lua"

fail() {
    echo "[faction-quantity-semantics-audit] ERROR: $*" >&2
    exit 1
}

for file in "$REGISTRY" "$DEFINITIONS" "$STOCK" "$ECONOMY" "$EXEC_SERVICE"; do
    [[ -f "$file" ]] || fail "missing quantity-semantics dependency: $file"
done

rg -q 'local UNIT_KINDS = ' "$REGISTRY" \
    || fail "quantity unit-kind registry missing"
for kind in ITEM USE ROUND LITER PORTION CUSTOM; do
    rg -q "${kind} = true" "$REGISTRY" \
        || fail "quantity unit kind missing: $kind"
done
rg -q 'function Registry\.Measure\(categoryId, item\)' "$REGISTRY" \
    || fail "category registry has no item measurement API"
rg -q 'function Registry\.NormalizeQuantity\(categoryId, value\)' "$REGISTRY" \
    || fail "category registry has no quantity normalization API"
rg -q 'function Registry\.GetQuantitySemantics\(categoryId\)' "$REGISTRY" \
    || fail "category registry exposes no sanitized quantity semantics"
rg -q 'function Registry\.SupportsWholeItemConsumption\(categoryId\)' "$REGISTRY" \
    || fail "category registry has no destructive whole-item capability gate"
rg -q 'measureKind == "CUSTOM".*measureFn' "$REGISTRY" \
    || fail "custom quantity measurement is not validated"
rg -q 'local amount = measure\(definition, item\)' "$REGISTRY" \
    || fail "classification still assumes one item equals one category unit"

# Build 42 item tags are registry objects, not string predicates. The shared matcher
# must resolve ResourceLocation -> ItemTag and call InventoryItem:hasTag(ItemTag).
rg -q 'match\.kind == "itemTagAny" or match\.kind == "itemTagAll"' "$REGISTRY" \
    || fail "namespaced B42 item-tag match kinds missing"
rg -q 'ResourceLocation\.of\(tagId\)' "$REGISTRY" \
    || fail "item tags are not resolved through B42 ResourceLocation"
rg -q 'ItemTag\.get\(location\)' "$REGISTRY" \
    || fail "item tag resolver bypasses the B42 item-tag registry"
rg -q 'item:hasTag\(tag\)' "$REGISTRY" \
    || fail "item-tag match does not use InventoryItem:hasTag(ItemTag)"
rg -q 'if not okTag or not tag then return nil end' "$REGISTRY" \
    || fail "unknown registry tags do not fail closed"
if rg -q 'ItemTag\.register' "$REGISTRY"; then
    fail "category matching must not register unknown item tags"
fi
if rg -q 'item:hasTag\("' "$REGISTRY" || rg -q "item:hasTag\\('" "$REGISTRY"; then
    fail "category matching must not pass raw strings to InventoryItem:hasTag"
fi

rg -q 'unitKind = "ITEM"' "$DEFINITIONS" \
    || fail "food quantity unit is not explicitly declared"
rg -q 'measureKind = "ITEM"' "$DEFINITIONS" \
    || fail "food whole-item measurement is not explicit"
rg -q 'splittable = false' "$DEFINITIONS" \
    || fail "food whole-item non-splittable contract missing"

rg -q 'schemaVersion = 2' "$STOCK" \
    || fail "stock snapshot schema was not advanced for measured categories"
rg -q 'addMeasuredQuantity\(categories, category, amount\)' "$STOCK" \
    || fail "container category quantities are still integer-counted"
rg -q 'addMeasuredQuantity\(snapshot\.categories, category, amount\)' "$STOCK" \
    || fail "site category quantities are still integer-counted"
rg -q 'Categories\.NormalizeQuantity\(category, value\)' "$STOCK" \
    || fail "stock category query does not preserve category precision"

rg -q 'schemaVersion = 2' "$ECONOMY" \
    || fail "economy schema was not advanced for quantity semantics"
rg -q 'unitKind = semantics\.unitKind' "$ECONOMY" \
    || fail "economy rows do not persist unit kind"
rg -q 'precision = semantics\.precision' "$ECONOMY" \
    || fail "economy rows do not persist quantity precision"
rg -q 'targetQuantity\(categoryId' "$ECONOMY" \
    || fail "economy targets bypass category quantity semantics"
rg -q 'Categories\.NormalizeQuantity' "$ECONOMY" \
    || fail "economy quantities still use universal integer normalization"

rg -q 'Categories\.SupportsWholeItemConsumption\(category\) ~= true' "$EXEC_SERVICE" \
    || fail "physical executor service does not fail closed for non-item quantities"
rg -q 'quantity-specific physical executor required' "$EXEC_SERVICE" \
    || fail "non-item quantity deferral is not diagnosable"

if rg -q 'math\.floor\(tonumber\(categories and categories\[category\]\)' "$STOCK"; then
    fail "stock category quantity API still floors measured quantities"
fi

if command -v lua >/dev/null 2>&1; then
    lua -e "assert(loadfile([[$REGISTRY]]))"
    lua -e "assert(loadfile([[$DEFINITIONS]]))"
    lua -e "assert(loadfile([[$STOCK]]))"
    lua -e "assert(loadfile([[$ECONOMY]]))"
    lua -e "assert(loadfile([[$EXEC_SERVICE]]))"
fi

echo "[faction-quantity-semantics-audit] OK"

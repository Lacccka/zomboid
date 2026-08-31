#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
lua_root="$project_root/Contents/mods/LaccckaQuestFramework/42/media/lua"

constants="$lua_root/shared/LCCQF/LCCQFConstants.lua"
categories="$lua_root/shared/LCCQF/Core/LCCQFSupplyCategoryRegistry.lua"
category_defs="$lua_root/shared/LCCQF/Content/LCCQFSupplyCategoryDefinitions.lua"
resolver="$lua_root/server/LCCQF/World/LCCQFWorldContainerResolver.lua"
stock="$lua_root/server/LCCQF/FactionWorld/LCCQFFactionSiteStock.lua"
service="$lua_root/server/LCCQF/FactionWorld/zz_LCCQFFactionSiteStockService.lua"
bootstrap="$lua_root/server/zz_LCCQFFactionBootstrap.lua"

fail() {
    echo "QuestFramework faction stock audit: FAIL: $1" >&2
    exit 1
}

require_pattern() {
    local pattern="$1"
    local file="$2"
    local message="$3"
    rg -q "$pattern" "$file" || fail "$message"
}

for required in "$constants" "$categories" "$category_defs" "$resolver" "$stock" "$service" "$bootstrap"; do
    [[ -f "$required" ]] || fail "missing $required"
done

require_pattern 'FACTION_SITE_STOCK_SCAN_MAX_TILES' "$constants" "stock tile budget missing"
require_pattern 'FACTION_SITE_STOCK_MAX_CONTAINERS' "$constants" "stock container budget missing"
require_pattern 'FACTION_SITE_STOCK_MAX_ITEMS' "$constants" "stock item budget missing"
require_pattern 'FACTION_SITE_STOCK_REFRESH_WORLD_HOURS' "$constants" "stock refresh cadence missing"

require_pattern 'function Resolver\.MakeLocator' "$resolver" "exact container locator builder missing"
require_pattern 'function Resolver\.Resolve' "$resolver" "exact container resolver missing"
require_pattern 'function Resolver\.ListObjectContainers' "$resolver" "multi-container enumeration missing"
require_pattern 'getContainerCount' "$resolver" "Build 42 compartment probe missing"
require_pattern 'primaryContainer\(object\) and 1 or 0' "$resolver" "Build 42 zero-count primary-container compatibility missing"
require_pattern 'getItemContainer' "$resolver" "Build 42 getItemContainer compatibility missing"
require_pattern 'objectCollection' "$resolver" "normal/special object collection discriminator missing"
require_pattern 'collectionIndex' "$resolver" "collection-local object index missing"
require_pattern 'containerIndex' "$resolver" "container compartment identity missing"
require_pattern 'containerType' "$resolver" "container type identity missing"
require_pattern 'spriteName' "$resolver" "world object sprite identity missing"

require_pattern 'function Stock\.Scan' "$stock" "settlement stock scan missing"
require_pattern 'function Stock\.ApplySnapshot' "$stock" "stock snapshot reconciliation missing"
require_pattern 'function Stock\.GetQuantity' "$stock" "stock quantity query missing"
require_pattern 'function Stock\.GetCategoryQuantity' "$stock" "stock category query missing"
require_pattern 'function Stock\.FindContainersForItem' "$stock" "stock locator query missing"
require_pattern 'quantitiesByFullType' "$stock" "full-type stock aggregation missing"
require_pattern 'categories = \{\}' "$stock" "stock category aggregation missing"
require_pattern 'Categories\.Classify\(item\)' "$stock" "stock scanner bypasses shared supply categories"
require_pattern 'match\.kind == "food"' "$categories" "food category implementation missing"
require_pattern 'item:IsFood\(\)' "$categories" "actual B42 food-item predicate missing"
require_pattern 'categoryId = "food"' "$category_defs" "food category is not registered as content"
require_pattern 'snapshot\.complete ~= true' "$stock" "incomplete scans do not fail closed"
require_pattern 'container budget exhausted' "$stock" "container budget failure state missing"
require_pattern 'item budget exhausted' "$stock" "item budget failure state missing"
require_pattern 'tile budget exhausted' "$stock" "tile budget failure state missing"
require_pattern 'Sites\.MarkDirty' "$stock" "stock changes are not persisted through site registry"

require_pattern 'site\.state == "ACTIVE" or site\.state == "DORMANT"' "$service" "stock service scans invalid site lifecycle states"
require_pattern 'FACTION_SITE_STOCK_REFRESH_WORLD_HOURS' "$service" "stock service ignores refresh throttle"
require_pattern 'Events\.EveryOneMinute' "$service" "stock refresh coordinator missing"
require_pattern 'zz_LCCQFFactionSiteStockService' "$bootstrap" "stock service not bootstrapped"

# The stock layer is observational. Item mutation belongs to the transactional executor.
if rg -n 'AddItem|AddItems|RemoveItem|RemoveOneOf|RemoveAll|clear\(|Clear\(' "$resolver" "$stock" "$service"; then
    fail "read-only stock layer mutates inventory"
fi

if rg -n 'sendClientCommand|sendServerCommand|OnClientCommand' "$resolver" "$stock" "$service"; then
    fail "stock layer exposes client/network authority"
fi

if rg -n 'BanditServer|BanditBrain|BanditClusters|Bandits2' "$resolver" "$stock" "$service"; then
    fail "stock layer leaks Bandits provider dependency"
fi

if command -v lua >/dev/null 2>&1; then
    lua -e "assert(loadfile([[$resolver]]))"
    lua -e "assert(loadfile([[$stock]]))"
    lua -e "assert(loadfile([[$service]]))"
fi

echo "QuestFramework faction stock audit: PASS (exact B42 container locators + bounded read-only settlement snapshots + shared observed item categories + fail-closed reconciliation)"

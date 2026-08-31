require "LCCQF/LCCQFConstants"
require "LCCQF/Core/LCCQFSupplyCategoryRegistry"

local C = LCCQF.Constants
local Registry = LCCQF.SupplyCategoryRegistry

local function register(definition)
    local ok, err = Registry.Register(definition)
    if not ok and err ~= "duplicate categoryId" then
        print(C.LOG_PREFIX .. "[FACTION:SUPPLY:CATEGORY] registration failed categoryId="
            .. tostring(definition and definition.categoryId) .. " error=" .. tostring(err))
    end
end

-- B42-backed classifier: food is detected through InventoryItem:IsFood().
-- Food intentionally remains whole-item quantity for the current runtime-accepted
-- consumption path. Future portion/nutrition semantics must opt into a different
-- measureKind and a compatible physical executor instead of silently changing this.
register({
    categoryId = "food",
    match = { kind = "food" },
    quantity = {
        unitKind = "ITEM",
        measureKind = "ITEM",
        precision = 0,
        splittable = false,
    },
})

-- Additional medicine/ammunition/water categories must be added only after their
-- concrete B42 item/tag/fluid semantics are verified; do not infer them from names.
return Registry

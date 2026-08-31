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
-- Additional medicine/ammunition/water categories must be added only after their
-- concrete B42 item semantics are verified; do not infer them from display names.
register({
    categoryId = "food",
    match = { kind = "food" },
})

return Registry

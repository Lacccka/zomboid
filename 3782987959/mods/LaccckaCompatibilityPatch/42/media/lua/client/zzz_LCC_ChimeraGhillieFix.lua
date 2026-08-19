local Guard = require "LCC/Guard"
local FEATURE = "chimera.ghillie-extra-menu"

Guard.safeRequire(FEATURE, "ISUI/ISInventoryPaneContextMenu")
if not Guard.isEnabled(FEATURE) then return end

local brokenTypes = {
    ["CHIMERA.Ghillie_Top_FaceHide"] = true,
    ["CHIMERA.Ghillie_Top"] = true,
}

Guard.install {
    id = FEATURE,
    validate = function()
        if type(ISInventoryPaneContextMenu) ~= "table" then
            return false, "ISInventoryPaneContextMenu is unavailable"
        end
        if type(ISInventoryPaneContextMenu.doClothingItemExtraMenu) ~= "function" then
            return false, "doClothingItemExtraMenu is unavailable"
        end
        return true
    end,
    install = function()
        Guard.wrapBefore(FEATURE, ISInventoryPaneContextMenu, "doClothingItemExtraMenu", function(context, item, playerObj)
            if not item then return end

            local fullType = item:getFullType()
            if not brokenTypes[fullType] then return end

            local extras = item:getClothingItemExtra()
            local options = item:getClothingItemExtraOption()

            if extras and options and extras:size() == 2 and options:size() == 3 then
                extras:add("CHIMERA.Ghillie_Top")
            end

            if fullType == "CHIMERA.Ghillie_Top"
                    and options and options:size() >= 3
                    and options:get(2) == "Ghillie_Top_Large" then
                options:set(2, "Ghillie_Top")
            end
        end)
    end,
}

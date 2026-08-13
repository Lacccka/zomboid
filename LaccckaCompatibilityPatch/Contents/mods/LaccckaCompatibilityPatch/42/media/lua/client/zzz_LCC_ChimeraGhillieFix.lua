require "ISUI/ISInventoryPaneContextMenu"

local brokenTypes = {
    ["CHIMERA.Ghillie_Top_FaceHide"] = true,
    ["CHIMERA.Ghillie_Top"] = true,
}

local originalMenu = ISInventoryPaneContextMenu.doClothingItemExtraMenu

ISInventoryPaneContextMenu.doClothingItemExtraMenu = function(context, item, playerObj)
    if item and brokenTypes[item:getFullType()] then
        local extras = item:getClothingItemExtra()
        local options = item:getClothingItemExtraOption()

        if extras and options and extras:size() == 2 and options:size() == 3 then
            extras:add("CHIMERA.Ghillie_Top")
        end
        if item:getFullType() == "CHIMERA.Ghillie_Top"
                and options and options:size() >= 3
                and options:get(2) == "Ghillie_Top_Large" then
            options:set(2, "Ghillie_Top")
        end
    end
    return originalMenu(context, item, playerObj)
end


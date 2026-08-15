if not getActivatedMods():contains("RS_WaterCistern")  then


---- Code from CocoLiquidOverhaul mod by Konijima-------------------------------------------------------------------------------------------------------------------------------------

local CLO_Funcs = {}

---GetAllFillableWaterItemInInventory
---@param _inventory ItemContainer
---@return table
function CLO_Funcs.GetAllFillableWaterItemInInventory(_inventory)
    if not instanceof(_inventory, "ItemContainer") then return end
    local result = {}
    local items = _inventory:getItems()
    for i = 0, items:size() - 1 do
        ---@type InventoryItem
        local item = items:get(i)
        --if item:canStoreWater() and not item:isWaterSource() and not item:isBroken() and item:getFluidContainer() and item:getFluidContainer():isFull() and item:getFluidContainer():getContainerContentType() == "TaintedWater" then
            --table.insert(result, item)
        --elseif item:canStoreWater() and not item:isWaterSource() and not item:isBroken() and item:getFluidContainer() and item:getFluidContainer():isFull() and item:getFluidContainer():getContainerContentType() == "Water" then
            --table.insert(result, item)
        --elseif item:canStoreWater() and not item:isWaterSource() and not item:isBroken() then
        if item:canStoreWater() and not item:isWaterSource() and not item:isBroken() then
            table.insert(result, item)
        elseif item:canStoreWater() and item:isWaterSource() and not item:isBroken() and instanceof(item, "DrainableComboItem") and item:getUsedDelta() < 1 then
            table.insert(result, item)
	elseif ISFluidUtil.validateContainer(item) then
	    table.insert(result, item)
        end
    end
    return result
end

CLO_Inventory_RS = {
    GetAllFillableWaterItemInInventory = CLO_Funcs.GetAllFillableWaterItemInInventory,
}
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

end
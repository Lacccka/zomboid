LCCQF = LCCQF or {}
LCCQF.QuestObjectives = LCCQF.QuestObjectives or {}

local ItemUtils = {}

local function validFullType(fullType)
    return type(fullType) == "string" and fullType ~= "" and #fullType <= 128
end

local function itemFullType(item)
    if not item then return nil end
    if item.getFullType then
        local value = item:getFullType()
        if type(value) == "string" and value ~= "" then return value end
    end
    return nil
end

local function collectFromContainer(container, fullType, result)
    if not container or not container.getItems then return end
    local items = container:getItems()
    if not items then return end

    for i = 0, items:size() - 1 do
        local item = items:get(i)
        if item then
            if itemFullType(item) == fullType then
                result[#result + 1] = { container = container, item = item }
            end

            if instanceof and instanceof(item, "InventoryContainer") and item.getItemContainer then
                local nested = item:getItemContainer()
                if nested then collectFromContainer(nested, fullType, result) end
            end
        end
    end
end

function ItemUtils.Count(player, fullType)
    if not player or not validFullType(fullType) or not player.getInventory then return 0 end
    local inventory = player:getInventory()
    if not inventory then return 0 end

    local matches = {}
    collectFromContainer(inventory, fullType, matches)
    return #matches
end

function ItemUtils.Remove(player, fullType, required)
    if not player or not validFullType(fullType) or not player.getInventory then return false, 0 end
    required = math.max(1, math.floor(tonumber(required) or 1))

    local inventory = player:getInventory()
    if not inventory then return false, 0 end

    local matches = {}
    collectFromContainer(inventory, fullType, matches)
    if #matches < required then return false, #matches end

    for i = 1, required do
        local entry = matches[i]
        if sendRemoveItemFromContainer then
            sendRemoveItemFromContainer(entry.container, entry.item)
        end
        entry.container:Remove(entry.item)
    end

    return true, required
end

LCCQF.QuestObjectives.ItemUtils = ItemUtils

return ItemUtils

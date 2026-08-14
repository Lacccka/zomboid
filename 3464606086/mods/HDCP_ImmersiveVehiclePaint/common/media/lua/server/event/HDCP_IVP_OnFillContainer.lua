local tableInsert = table.insert

local HDCP_IVP_OnFillContainer = {}

function HDCP_IVP_OnFillContainer.new(deps)
    local Constants       = deps and deps.Constants or require('HDCP_IVP_Constants')

    local usedPaintDummy  = Constants.ITEMS.USED_SPRAY_PAINT_DUMMY
    local usedPrimerDummy = Constants.ITEMS.USED_SPRAY_PRIMER_DUMMY
    local newPaintDummy   = Constants.ITEMS.NEW_SPRAY_PAINT_DUMMY
    local newPrimerDummy  = Constants.ITEMS.NEW_SPRAY_PRIMER_DUMMY

    local primerType      = Constants.ITEMS.AUTOMOTIVE_PRIMER_SPRAY

    local itemTypes       = Constants.ITEMS.SPRAY_PAINT

    local itemsToReplace  = {}

    local module          = {}

    local function applyRandomUses(item)
        local maxUses = item:getMaxUses()

        local usedDelta = item:getUseDelta() * ZombRand(3, maxUses)

        item:setUsedDelta(usedDelta)
    end

    local function replaceItem(container, oldItem, newItem)
        if not newItem then return end

        local itemType = oldItem:getFullType()

        if itemType == usedPaintDummy or itemType == usedPrimerDummy then
            applyRandomUses(newItem)
        end

        container:Remove(oldItem)
        container:AddItem(newItem)
    end

    local function createItem(item)
        local itemType = item:getFullType()

        if itemType == newPaintDummy or itemType == usedPaintDummy then
            local itemTypeIndex = ZombRand(#itemTypes) + 1

            return instanceItem(itemTypes[itemTypeIndex].type)
        elseif itemType == newPrimerDummy or itemType == usedPrimerDummy then
            return instanceItem(primerType)
        end
    end

    local function iterateContainer(container)
        local items = container:getItems()

        for index = 1, items:size() do
            local item = items:get(index - 1)

            local itemType = item:getFullType()

            if itemType == newPaintDummy or
                itemType == usedPaintDummy or
                itemType == newPrimerDummy or
                itemType == usedPrimerDummy
            then
                tableInsert(itemsToReplace, {
                    container = container,
                    item = item
                })
            end

            if item:IsInventoryContainer() then
                iterateContainer(item:getItemContainer())
            end
        end
    end

    module.run = function(_, _, container)
        if not instanceof(container, 'ItemContainer') then return end

        itemsToReplace = {}

        iterateContainer(container)

        for _, entry in ipairs(itemsToReplace) do
            local newItem = createItem(entry.item)

            replaceItem(entry.container, entry.item, newItem)
        end
    end

    return module
end

return HDCP_IVP_OnFillContainer

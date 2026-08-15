local tableInsert = table.insert
local tableRemove = table.remove

local OnClientCommand = {}

function OnClientCommand.new()
    local module = {}

    local function consumeItems(items, usesRequired)
        if usesRequired <= 0 then return end

        local item = items[#items]

        local itemUses = item:getCurrentUses()

        itemUses = itemUses > usesRequired and usesRequired or itemUses

        for _ = 1, itemUses do item:UseAndSync() end

        tableRemove(items, #items)

        consumeItems(items, usesRequired - itemUses)
    end

    function module.useItem(player, args)
        local items = player:getInventory():getAllTypeRecurse(args.itemType)

        local itemList = {}

        for i = 1, items:size() do
            tableInsert(itemList, items:get(i - 1))
        end

        consumeItems(itemList, args.uses)
    end

    module.run = function(moduleName, command, player, args)
        if moduleName == 'ImmersiveVehiclePaint' and module[command] then
            local argStr = ''

            args = args or {}

            for k, v in pairs(args) do
                argStr = argStr .. ' ' .. k .. '=' .. tostring(v)
            end

            module[command](player, args)
        end
    end

    return module
end

return OnClientCommand

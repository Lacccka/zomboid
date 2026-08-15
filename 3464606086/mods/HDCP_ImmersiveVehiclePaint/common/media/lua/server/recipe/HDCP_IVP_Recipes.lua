local HDCP_IVP_Recipes = {}

function HDCP_IVP_Recipes.new(deps)
    local Constants = deps and deps.Constants or require('HDCP_IVP_Constants')

    local module = {}

    -- a box yields six cans of a single colour, so the odds of getting the colour you
    -- wanted are one over the size of the pool: narrowing the pool by shade is the
    -- whole point of the themed boxes
    local function coloursOf(group)
        if not group then return Constants.ITEMS.SPRAY_PAINT end

        local pool = {}

        for _, entry in ipairs(Constants.ITEMS.SPRAY_PAINT) do
            if entry.group == group then
                table.insert(pool, entry)
            end
        end

        return pool
    end

    local function openBox(character, group)
        local itemTypes = coloursOf(group)

        if #itemTypes == 0 then return end

        local itemTypeIndex = ZombRand(#itemTypes) + 1

        local itemType = itemTypes[itemTypeIndex].type

        local spraysPerBox = 6

        local inventory = character:getInventory()

        for i = 1, spraysPerBox do
            local item = instanceItem(itemType)

            if item then
                inventory:AddItem(item)

                sendAddItemToContainer(inventory, item)
            end
        end
    end

    module.colourPool = coloursOf

    -- the legacy box, still openable in saves that carry one, keeps the full pool
    module.openBoxOfAutomotiveSprayPaint = function(character)
        openBox(character)
    end

    module.openBoxOfWarmAutomotiveSprayPaint = function(character)
        openBox(character, "warm")
    end

    module.openBoxOfCoolAutomotiveSprayPaint = function(character)
        openBox(character, "cool")
    end

    module.openBoxOfNeutralAutomotiveSprayPaint = function(character)
        openBox(character, "neutral")
    end

    return module
end

return HDCP_IVP_Recipes

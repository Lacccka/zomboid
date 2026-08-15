local tableInsert = table.insert

local HDCP_IVP_Distribution = {}

function HDCP_IVP_Distribution.new(deps)
    local Helpers = deps and deps.Helper or require('HDCP_IVP_Helpers')
    local Noises  = deps and deps.Noises or require('HDCP_IVP_Noises')

    local module  = {}

    local function insertList(procList, list)
        if not list then return end

        for i = 1, #list do
            tableInsert(procList, list[i])
        end
    end

    local function canAddItemList(list, place, container)
        local isSatisfied = list[place][container].procList ~= nil

        if not isSatisfied then
            Helpers.noise(Noises.PROCLIST_NOT_DEFINED:format(place, container))
        end

        return isSatisfied
    end

    local function addItemList(list, place, container, itemList)
        if canAddItemList(list, place, container) then
            insertList(list[place][container].procList, itemList)
        end
    end

    local function containerExists(list, place, container)
        local isSatisfied = list[place] and list[place][container] ~= nil

        if not isSatisfied then
            Helpers.noise(Noises.DISTRIBUTION_CONTAINER_NOT_FOUND:format(
                tostring(place), container
            ))
        end

        return isSatisfied
    end

    local function iterateContainers(list, place, containers)
        for container, itemList in pairs(containers) do
            if containerExists(list, place, container) then
                addItemList(list, place, container, itemList)
            end
        end
    end

    local function insertItems(place, container, items)
        local containerItems = container.items

        if not containerItems then
            Helpers.noise(Noises.MISSING_ITEMS_KEY_FOR_CONTAINER:format(place))
        end

        for i = 1, #items do
            tableInsert(containerItems, items[i])
        end
    end

    local function placeExists(list, place)
        local isSatisfied = list and list[place] ~= nil

        if not isSatisfied then
            Helpers.noise(Noises.DISTRIBUTION_PLACE_NOT_FOUND:format(place))
        end

        return isSatisfied
    end

    local function iteratePlaces(list, places)
        for place, entry in pairs(places) do
            if placeExists(list, place) then
                local container = list[place]

                if container.items ~= nil then
                    insertItems(place, container, entry.items)
                else
                    iterateContainers(list, place, entry)
                end
            end
        end
    end

    module.include = function(distList, modList)
        iteratePlaces(distList, modList)
    end

    return module
end

return HDCP_IVP_Distribution

local PATCH_TAG = "[EFZ_Tetris_4217_Compat]"

if _G.__EFZ_TETRIS_4217_COMPAT_FILE_LOADED then
    return
end
_G.__EFZ_TETRIS_4217_COMPAT_FILE_LOADED = true
print(PATCH_TAG .. " Loaded EFZ_Tetris_4217_Compat.lua")

local function debugPrint(message)
    print(PATCH_TAG .. " " .. message)
end

local function logOnce(key, message)
    if not _G.__EFZ_TETRIS_4217_COMPAT_LOG_KEYS then
        _G.__EFZ_TETRIS_4217_COMPAT_LOG_KEYS = {}
    end

    if _G.__EFZ_TETRIS_4217_COMPAT_LOG_KEYS[key] then
        return
    end

    _G.__EFZ_TETRIS_4217_COMPAT_LOG_KEYS[key] = true
    debugPrint(message)
end

local function tryRequire(path)
    local ok, result = pcall(require, path)
    if ok then
        return result, nil
    end
    return nil, tostring(result)
end

local function isInventoryTetrisActive()
    local mods = getActivatedMods()
    return mods and mods:contains("INVENTORY_TETRIS")
end

local function isNpcCharacter(character)
    if not character then
        return false
    end

    if type(character.getIsNPC) == "function" then
        return character:getIsNPC()
    end

    if type(character.isNPC) == "function" then
        return character:isNPC()
    end

    return false
end

local function hasExpectedValidateEquippedItemsStructure(TetrisHandMonitor)
    if type(TetrisHandMonitor) ~= "table" then
        return false, "module is not a table"
    end

    if type(TetrisHandMonitor.validateEquippedItems) ~= "function" then
        return false, "validateEquippedItems is " .. type(TetrisHandMonitor.validateEquippedItems)
    end

    if type(TetrisHandMonitor.ticksByPlayer) ~= "table" then
        return false, "ticksByPlayer is " .. type(TetrisHandMonitor.ticksByPlayer)
    end

    if not Events or not Events.OnPlayerUpdate then
        return false, "Events.OnPlayerUpdate is missing"
    end

    if type(Events.OnPlayerUpdate.Add) ~= "function" then
        return false, "Events.OnPlayerUpdate.Add is " .. type(Events.OnPlayerUpdate.Add)
    end

    if type(Events.OnPlayerUpdate.Remove) ~= "function" then
        return false, "Events.OnPlayerUpdate.Remove is " .. type(Events.OnPlayerUpdate.Remove)
    end

    return true, nil
end

local function hasExpectedPlayerMainGridStructure(ItemContainerGrid)
    if type(ItemContainerGrid) ~= "table" then
        return false, "module is not a table"
    end

    local requiredMembers = {
        { "_getPlayerMainGrid", "function" },
        { "GetOrCreate", "function" },
        { "FindInstance", "function" },
        { "new", "function" },
        { "_playerMainGrids", "table" },
        { "_unpositionedItemSetsByPlayer", "table" },
        { "_gridCache", "table" },
        { "_tempGrid", "table" },
    }

    for i = 1, #requiredMembers do
        local key = requiredMembers[i][1]
        local expectedType = requiredMembers[i][2]
        local actualType = type(ItemContainerGrid[key])
        if actualType ~= expectedType then
            return false, key .. " is " .. actualType
        end
    end

    if not Events or not Events.OnTick then
        return false, "Events.OnTick is missing"
    end

    if type(Events.OnTick.Add) ~= "function" then
        return false, "Events.OnTick.Add is " .. type(Events.OnTick.Add)
    end

    return true, nil
end

local function patchValidateEquippedItemsSystem()
    local TetrisHandMonitor, requireError = tryRequire("InventoryTetris/System/ValidateEquippedItemsSystem")
    if not TetrisHandMonitor then
        logOnce("validate-require-failed", "Skipped ValidateEquippedItemsSystem patch: require failed: " .. requireError)
        return false
    end

    if TetrisHandMonitor._efz4217ValidatePatched then
        return true
    end

    local structureOk, structureReason = hasExpectedValidateEquippedItemsStructure(TetrisHandMonitor)
    if not structureOk then
        logOnce("validate-structure-mismatch", "Skipped ValidateEquippedItemsSystem patch: unexpected structure: " .. structureReason)
        return false
    end

    TetrisHandMonitor._efz4217OriginalValidateEquippedItems = TetrisHandMonitor.validateEquippedItems
    Events.OnPlayerUpdate.Remove(TetrisHandMonitor.validateEquippedItems)

    function TetrisHandMonitor.validateEquippedItems(playerObj)
        if isNpcCharacter(playerObj) then
            return
        end

        local playerNum = playerObj:getPlayerNum()
        local tick = TetrisHandMonitor.ticksByPlayer[playerNum] or 0
        if tick < 15 then
            TetrisHandMonitor.ticksByPlayer[playerNum] = tick + 1
            return
        end
        TetrisHandMonitor.ticksByPlayer[playerNum] = 0

        local primHand = playerObj:getPrimaryHandItem()
        if primHand and not primHand:getContainer() then
            playerObj:setPrimaryHandItem(nil)
        end

        local secHand = playerObj:getSecondaryHandItem()
        if secHand and not secHand:getContainer() then
            playerObj:setSecondaryHandItem(nil)
        end
    end

    Events.OnPlayerUpdate.Add(TetrisHandMonitor.validateEquippedItems)
    TetrisHandMonitor._efz4217ValidatePatched = true
    debugPrint("Patched ValidateEquippedItemsSystem.")
    return true
end

local function patchPlayerMainGridRefresh()
    local ItemContainerGrid, requireError = tryRequire("InventoryTetris/Model/ItemContainerGrid")
    if not ItemContainerGrid then
        logOnce("main-grid-require-failed", "Skipped ItemContainerGrid patch: require failed: " .. requireError)
        return false
    end

    if ItemContainerGrid._efz4217MainGridPatched then
        return true
    end

    local structureOk, structureReason = hasExpectedPlayerMainGridStructure(ItemContainerGrid)
    if not structureOk then
        logOnce("main-grid-structure-mismatch", "Skipped ItemContainerGrid patch: unexpected structure: " .. structureReason)
        return false
    end

    -- Move the player main-grid cache to a separate table so the original
    -- anonymous OnTick handler never touches the broken :isNPC() call path.
    local compatPlayerMainGrids = ItemContainerGrid._efz_playerMainGrids or {}
    ItemContainerGrid._efz_playerMainGrids = compatPlayerMainGrids
    ItemContainerGrid._efz4217OriginalGetPlayerMainGrid = ItemContainerGrid._getPlayerMainGrid

    for playerNum, grid in pairs(ItemContainerGrid._playerMainGrids) do
        compatPlayerMainGrids[playerNum] = grid
        ItemContainerGrid._playerMainGrids[playerNum] = nil
    end

    function ItemContainerGrid._getPlayerMainGrid(playerObj, playerNum)
        local inventory = playerObj:getInventory()

        local containerGrid = compatPlayerMainGrids[playerNum]
        if containerGrid and containerGrid.inventory ~= inventory then
            containerGrid = nil
        end

        if not containerGrid then
            containerGrid = ItemContainerGrid:new(inventory, playerNum)
            compatPlayerMainGrids[playerNum] = containerGrid
        end

        return containerGrid
    end

    if not ItemContainerGrid._efz4217RefreshPlayerMainGrids then
        function ItemContainerGrid._efz4217RefreshPlayerMainGrids()
            for playerNum, grid in pairs(compatPlayerMainGrids) do
                local player = getSpecificPlayer(playerNum)
                if not player or player:isDead() or isNpcCharacter(player) then
                    compatPlayerMainGrids[playerNum] = nil
                    ItemContainerGrid._unpositionedItemSetsByPlayer[playerNum] = nil
                else
                    if grid:shouldRefresh() then
                        grid:refresh()
                    end
                end
            end
        end

        Events.OnTick.Add(ItemContainerGrid._efz4217RefreshPlayerMainGrids)
    end

    ItemContainerGrid._efz4217MainGridPatched = true
    debugPrint("Patched ItemContainerGrid player main grid refresh.")
    return true
end

local function applyPatch()
    if not isInventoryTetrisActive() then
        return
    end

    local validatePatched = patchValidateEquippedItemsSystem()
    local mainGridPatched = patchPlayerMainGridRefresh()

    if not validatePatched and not mainGridPatched then
        logOnce("no-compat-patch-applied", "No Inventory Tetris 42.17 compatibility patch was applied.")
    end
end

Events.OnGameBoot.Add(applyPatch)
Events.OnCreatePlayer.Add(applyPatch)
Events.OnGameStart.Add(applyPatch)

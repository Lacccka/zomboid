-- One-time starter inventory grants.

if isClient() and not isServer() then
    return
end

local MOD_ID = "ModpackFestivalSpawn"
local LOG_PREFIX = "[" .. MOD_ID .. "][StarterInventory] "
local FLASHLIGHT_ITEM = "Base.HandTorch"

local function giveStarterFlashlight(player)
    if not player or not player.getModData or not player.getInventory then
        return false
    end
    local md = player:getModData()
    if md.modpackFestivalStarterFlashlightGiven == true then
        return false
    end
    local inv = player:getInventory()
    if not inv then
        return false
    end

    local ok, item = pcall(function()
        return inv:AddItem(FLASHLIGHT_ITEM)
    end)
    if not ok or not item then
        print(LOG_PREFIX .. "failed to add starter flashlight")
        return false
    end
    if sendAddItemToContainer then
        pcall(sendAddItemToContainer, inv, item)
    end
    md.modpackFestivalStarterFlashlightGiven = true
    if player.transmitModData then
        pcall(function() player:transmitModData() end)
    end
    print(LOG_PREFIX .. "starter flashlight added")
    return true
end

local function onCreatePlayer(_playerIndex, player)
    giveStarterFlashlight(player)
end

local function onGameStart()
    local player = getSpecificPlayer and getSpecificPlayer(0)
    giveStarterFlashlight(player)
end

if Events.OnCreatePlayer then
    Events.OnCreatePlayer.Add(onCreatePlayer)
end
if Events.OnGameStart then
    Events.OnGameStart.Add(onGameStart)
end

print(LOG_PREFIX .. "loaded")

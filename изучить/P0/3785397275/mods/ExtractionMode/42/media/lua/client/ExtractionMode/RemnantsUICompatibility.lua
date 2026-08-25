require "ExtractionMode/ProjectRemnantsIntegration"
require "ISUI/PlayerData/ISPlayerData"
require "ISUI/ISEquippedItem"

ExtractionMode = ExtractionMode or {}
local Integration = ExtractionMode.ProjectRemnantsIntegration
local Compatibility = {}
local guardedButtons = {}
local tickCounter = 0
local installedLogged = false
local equippedItemGuardInstalled = false

local function activeMod(modId)
    local mods = getActivatedMods and getActivatedMods()
    return mods ~= nil and mods:contains(modId)
end

local function remnantsEnabled()
    if isClient and isClient() then return false end
    if isServer and isServer() then return false end
    return activeMod("ProjectRemnants") and Integration ~= nil and Integration.isAvailable()
end

local function cleanUIEnabled()
    return remnantsEnabled() and activeMod("CleanUI")
end

local function installEquippedItemGuard()
    if equippedItemGuardInstalled or not remnantsEnabled()
        or ISEquippedItem == nil or type(ISEquippedItem.checkToolTip) ~= "function" then
        return
    end

    local originalCheckToolTip = ISEquippedItem.checkToolTip
    ISEquippedItem.checkToolTip = function(equippedItem)
        local playerNum = equippedItem and equippedItem.playerNum
        local player = playerNum ~= nil and getSpecificPlayer(playerNum) or nil
        local contextMenu = playerNum ~= nil and getPlayerContextMenu
            and getPlayerContextMenu(playerNum) or nil

        -- Project Remnants briefly unregisters the controlled player when the
        -- final squad member dies. Vanilla still renders this toolbar for one
        -- frame and otherwise calls isAnyVisible() on the missing context menu.
        if player == nil or contextMenu == nil then
            if equippedItem and equippedItem.toolRender then
                equippedItem.toolRender:setVisible(false)
            end
            return
        end

        return originalCheckToolTip(equippedItem)
    end
    equippedItemGuardInstalled = true
    print("[ExtractionMode] Project Remnants death toolbar guard active")
end

local function guardTransferButton(page)
    local button = page and page.transferAllButton
    if button == nil or guardedButtons[button] or type(button.render) ~= "function" then return end

    local originalRender = button.render
    button.render = function(renderButton)
        local playerNum = tonumber(page.player)
        if playerNum == nil or getSpecificPlayer(playerNum) == nil then return end
        return originalRender(renderButton)
    end
    guardedButtons[button] = true
end

local function eachInventoryPage(callback)
    if getPlayerData == nil then return end
    for playerNum = 0, 3 do
        local data = getPlayerData(playerNum)
        if data ~= nil then
            if data.playerInventory ~= nil then callback(data.playerInventory) end
            if data.lootInventory ~= nil then callback(data.lootInventory) end
        end
    end
end

local function installGuards()
    installEquippedItemGuard()
    if not cleanUIEnabled() then return end
    eachInventoryPage(guardTransferButton)
    if not installedLogged then
        installedLogged = true
        print("[ExtractionMode] Project Remnants + CleanUI null-player inventory guard active")
    end
end

local function hideInventoryPage(page)
    pcall(function() page:setVisible(false) end)
end

local function onPlayerDeath(player)
    if not cleanUIEnabled() or Integration.hasLivingSuccessor(player) then return end
    -- Project Remnants unregisters players[0] when the final squad body dies.
    -- Hide CleanUI before its transfer button renders against that null slot.
    eachInventoryPage(hideInventoryPage)
end

local function onTick()
    if not remnantsEnabled() then return end
    tickCounter = tickCounter + 1
    if tickCounter < 30 then return end
    tickCounter = 0
    installGuards()
end

Events.OnGameStart.Add(installGuards)
Events.OnPlayerDeath.Add(onPlayerDeath)
Events.OnTick.Add(onTick)

ExtractionMode.RemnantsUICompatibility = Compatibility
return Compatibility

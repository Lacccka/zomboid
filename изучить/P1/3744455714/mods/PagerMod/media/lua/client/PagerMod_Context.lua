-- ============================================================
-- PagerMod_Context.lua
-- Right-click options on a Pager item: open, quick-page a
-- contact/channel with canned phrases, SOS, share number,
-- say number aloud, mute, mark read, rename, activate.
-- ============================================================

require "PagerMod_Shared"
require "PagerMod_Client"

local WARN = { r = 0.9, g = 0.7, b = 0.2 }

local function resolveItem(entry)
    if not entry then return nil end
    if instanceof(entry, "InventoryItem") then return entry end
    if entry.items and entry.items[1] then
        local first = entry.items[1]
        if instanceof(first, "InventoryItem") then return first end
    end
    return nil
end

-- ── Actions ────────────────────────────────────────────────

local function openPager(item)
    if not item then return end
    if not item:getModData().pagerNumber then
        PagerMod.halo(getText("IGUI_PagerMod_NeedActivate"), WARN)
        return
    end
    PagerMod.ensureBattery(item)
    if PagerMod.batteryDead(item) then
        PagerMod.halo(getText("IGUI_PagerMod_BatteryDead"), { r = 0.9, g = 0.4, b = 0.2 })
        return
    end
    PagerMod.OpenPagerUI(item)
end

local function activatePager(item)
    if not item then return end
    local md = item:getModData()
    if md.pagerNumber then
        PagerMod.halo(getText("IGUI_PagerMod_AlreadyActive") .. " " .. PagerMod.formatNumber(md.pagerNumber))
        return
    end
    PagerMod.requestAssign(item, getText("IGUI_PagerMod_DefaultName"))
end

local function doRename(item, button)
    if button.internal ~= "OK" then return end
    local txt = button.parent.entry:getText()
    if txt and txt ~= "" then
        local name = txt:sub(1, 24)
        item:getModData().pagerName = name
        PagerMod.saveItem(item)
        -- Let the server persist the name (authoritative in B42) and push it to
        -- everyone who has this pager saved, so their contact entry stays updated.
        PagerMod.requestRenamePager(item, name)
        PagerMod.halo(getText("IGUI_PagerMod_Renamed"))
        if PagerMod.UI and PagerMod.UI.refresh then PagerMod.UI:refresh() end
    end
end

local function renamePager(item)
    if not item then return end
    local player = getPlayer()
    local md = item:getModData()
    local modal = ISTextBox:new(0, 0, 320, 160,
        getText("IGUI_PagerMod_EnterName"), md.pagerName or "", item, doRename,
        player:getPlayerNum())
    modal:initialise()
    modal:addToUIManager()
end

-- ── Quick-page submenu builders ────────────────────────────

-- Build the canned-phrase submenu for a single target (contact or channel).
local function buildPhraseMenu(context, parentOption, item, sendFn)
    local sub = ISContextMenu:getNew(context)
    context:addSubMenu(parentOption, sub)
    for _, key in ipairs(PagerMod.QuickPhrases) do
        local phrase = getText(key)
        sub:addOption(phrase, nil, function() sendFn(phrase) end)
    end
    sub:addOption(getText("IGUI_PagerMod_CustomMsg"), nil, function() sendFn(nil) end)
end

local function buildQuickPageMenu(context, parentOption, item)
    local sub = ISContextMenu:getNew(context)
    context:addSubMenu(parentOption, sub)

    local contacts = PagerMod.getContacts()
    local channels = PagerMod.getChannels()

    if #contacts == 0 and #channels == 0 then
        local none = sub:addOption(getText("IGUI_PagerMod_NoContacts"), nil, nil)
        none.notAvailable = true
        return
    end

    for _, c in ipairs(contacts) do
        local opt = sub:addOption(c.name or PagerMod.formatNumber(c.number), nil, nil)
        buildPhraseMenu(sub, opt, item, function(phrase)
            if phrase then
                PagerMod.sendMessage(item, c.number, phrase)
            else
                PagerMod.openComposeTo(item, c.number)
            end
        end)
    end

    for _, ch in ipairs(channels) do
        local label = "[" .. getText("IGUI_PagerMod_TabGroups") .. "] " .. (ch.name or "?")
        local opt = sub:addOption(label, nil, nil)
        buildPhraseMenu(sub, opt, item, function(phrase)
            if phrase then
                PagerMod.sendChannel(item, ch, phrase)
            else
                PagerMod.openComposeChannel(item, ch)
            end
        end)
    end
end

-- ── Menu assembly ──────────────────────────────────────────

local function fillActivatedMenu(context, item)
    PagerMod.refreshConfig()
    local md = item:getModData()
    local unread = md.unread or 0
    local st = PagerMod.netStatus or {}
    local canSend = PagerMod.canSend()

    local openLabel = getText("IGUI_PagerMod_Open")
    if unread > 0 then openLabel = openLabel .. " (" .. unread .. ")" end
    context:addOption(openLabel, item, openPager)

    -- Quick page (sending only — hidden on receive-only pagers)
    if canSend then
        local qp = context:addOption(getText("IGUI_PagerMod_QuickPage"), nil, nil)
        buildQuickPageMenu(context, qp, item)
    end

    -- SOS (sending only, and only when enabled)
    if canSend and st.allowSOS ~= false and PagerMod.Config.allowSOS then
        context:addOption(getText("IGUI_PagerMod_SendSOS"), item, function() PagerMod.sendSOS(item) end)
    end

    -- Share / say number
    context:addOption(getText("IGUI_PagerMod_ShareNearby"), item, function() PagerMod.shareNearby(item) end)
    context:addOption(getText("IGUI_PagerMod_SayNumberOpt"), item, function() PagerMod.sayNumber(item) end)

    -- Mark all read
    if unread > 0 then
        context:addOption(getText("IGUI_PagerMod_MarkRead"), item, function() PagerMod.markPagerRead(item) end)
    end

    -- Replace battery (shows current charge; disabled if no spare battery)
    local pct = PagerMod.batteryPercent(item)
    local battOpt = context:addOption(
        getText("IGUI_PagerMod_ReplaceBattery") .. " (" .. pct .. "%)",
        item, function() PagerMod.replaceBattery(item) end)
    if not PagerMod.hasSpareBattery() then
        battOpt.notAvailable = true
        local tip = ISToolTip:new()
        tip:initialise()
        tip:setName(getText("IGUI_PagerMod_ReplaceBattery"))
        tip.description = getText("IGUI_PagerMod_NeedBattery")
        battOpt.toolTip = tip
    end

    -- Mute toggle
    local muteLabel = md.muted and getText("IGUI_PagerMod_Unmute") or getText("IGUI_PagerMod_Mute")
    context:addOption(muteLabel, item, function()
        local muted = PagerMod.toggleMute(item)
        PagerMod.halo(muted and getText("IGUI_PagerMod_Muted") or getText("IGUI_PagerMod_Unmuted"))
    end)

    context:addOption(getText("IGUI_PagerMod_Rename"), item, renamePager)
end

-- Pager tower (inventory): set it up at the player's tile, which drops it on
-- the ground so its 3D model shows and registers the tile with the server.
local function fillTowerMenu(context, item)
    context:addOption(getText("IGUI_PagerMod_DeployTower"), item, function()
        PagerMod.deployTower(item)
    end)
end

local function onFillContextMenu(playerNum, context, items)
    local player = getSpecificPlayer(playerNum)
    if not player then return end

    for _, entry in ipairs(items) do
        local item = resolveItem(entry)
        if item and item:getFullType() == PagerMod.ITEM then
            if item:getModData().pagerNumber then
                fillActivatedMenu(context, item)
            else
                context:addOption(getText("IGUI_PagerMod_Activate"), item, activatePager)
            end
            return -- only add once
        elseif item and item:getFullType() == PagerMod.TOWER and item:getContainer() then
            -- Only offer "Set up" for a CARRIED tower. The deployed tower is a
            -- PagerTower world item (no container); offering deploy on it let
            -- players re-set-up the same object endlessly, duping towers.
            fillTowerMenu(context, item)
            return
        end
    end
end

-- Pager tower (deployed in the world): right-click it to operate its console
-- (send / read as 000-0001) or pack it back up. The deployed tower is an
-- IsoThumpable tagged with modData.pagerTower.
local function onFillWorldMenu(playerNum, context, worldobjects)
    local player = getSpecificPlayer(playerNum)
    if not player then return end
    local towerObj = nil
    -- 1. a tower object directly under the cursor (the B41 tower is a dropped
    --    world inventory item, so don't restrict to IsoObject here).
    for _, obj in ipairs(worldobjects or {}) do
        if obj and PagerMod.isTowerObject(obj) then
            towerObj = obj
            break
        end
    end
    -- 2. fallback: a tower on or next to the player's tile. The visible art is
    --    drawn offset above the object's tile, so the exact click-tile is
    --    ambiguous; this makes the menu appear whenever you're beside the tower.
    if not towerObj and PagerMod.findTowerObject then
        local cell = getCell()
        local px, py, pz = math.floor(player:getX()), math.floor(player:getY()), math.floor(player:getZ())
        for dx = -1, 1 do
            for dy = -1, 1 do
                local sq = cell and cell:getGridSquare(px + dx, py + dy, pz)
                local o = sq and PagerMod.findTowerObject(sq)
                if o then towerObj = o; break end
            end
            if towerObj then break end
        end
    end
    if not towerObj then return end

    local useOpt = context:addOption(getText("IGUI_PagerMod_UseTower"), towerObj, function(o)
        PagerMod.requestOpenTower(o:getSquare())
    end)
    -- Can't operate an unpowered tower (when power is required): grey it out and
    -- explain it needs to be hooked up.
    if not PagerMod.squareHasTowerPower(towerObj:getSquare()) then
        useOpt.notAvailable = true
        local tip = ISToolTip:new()
        tip:initialise()
        tip:setName(getText("IGUI_PagerMod_UseTower"))
        tip.description = getText("IGUI_PagerMod_TowerNeedsPower")
        useOpt.toolTip = tip
    end
    context:addOption(getText("IGUI_PagerMod_RenameTower"), towerObj, function(o)
        local sq = o:getSquare()
        local modal = ISTextBox:new(0, 0, 320, 160, getText("IGUI_PagerMod_EnterTowerName"), "",
            sq, function(square, button)
                if button.internal == "OK" then
                    PagerMod.requestRenameTower(square, button.parent.entry:getText())
                end
            end, player:getPlayerNum())
        modal:initialise()
        modal:addToUIManager()
    end)
    context:addOption(getText("IGUI_PagerMod_PickupTower"), towerObj, function(o)
        PagerMod.pickupTower(o)
    end)
end

Events.OnFillInventoryObjectContextMenu.Add(onFillContextMenu)
Events.OnFillWorldObjectContextMenu.Add(onFillWorldMenu)

print("[PagerMod] Context menu loaded")

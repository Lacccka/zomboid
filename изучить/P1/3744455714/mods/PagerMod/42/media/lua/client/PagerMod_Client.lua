-- ============================================================
-- PagerMod_Client.lua
-- Client side: tracks held pagers, registers numbers with the
-- server, receives messages, stores them on the physical pager
-- item, and fires notifications. Exposes the API the UI uses.
-- ============================================================

require "PagerMod_Shared"

local C2S = PagerMod.C2S
local S2C = PagerMod.S2C

PagerMod._pending = PagerMod._pending or {}  -- reqId -> item awaiting a number
PagerMod._reqSeq  = PagerMod._reqSeq or 0
PagerMod._lastRegistered = PagerMod._lastRegistered or ""
PagerMod.netStatus = PagerMod.netStatus or {}

local INFO_COLOR  = { r = 0.2, g = 0.9, b = 0.4 }
local ALERT_COLOR = { r = 0.3, g = 0.8, b = 1.0 }
local ERROR_COLOR = { r = 0.9, g = 0.3, b = 0.2 }

-- The HaloTextHelper API differs between builds and the two share this Lua,
-- so detect at runtime:
--   B42: addGoodText(player,text) / addBadText(player,text); no coloured addText.
--   B41: addText(player, text, Color); no addGoodText/addBadText.
local function halo(text, color)
    local player = getPlayer()
    if not player or not text then return end
    color = color or INFO_COLOR
    local bad = (color.r or 0) >= 0.7 and (color.g or 0) < 0.78  -- reddish / warning
    local H = HaloTextHelper
    if not H then return end
    if H.addBadText and H.addGoodText then            -- B42
        if bad then H.addBadText(player, text) else H.addGoodText(player, text) end
    elseif H.addText then                              -- B41 (3-arg + Color)
        local col
        if bad then col = H.getColorRed and H.getColorRed() or nil
        else        col = H.getColorGreen and H.getColorGreen() or nil end
        if col ~= nil then H.addText(player, text, col)
        else pcall(function() H.addText(player, text) end) end
    end
end
PagerMod.halo = halo

-- ── Sound ──────────────────────────────────────────────────
-- playUISound has no volume parameter, so the BeepVolume sandbox option maps
-- to one of four pre-scaled entries in media/scripts/PagerMod_sounds.txt (same
-- .ogg, different clip volume). Fallbacks: the full-volume PagerBeep, then the
-- vanilla UI click.
local BEEP_BY_VOLUME = { "PagerBeep25", "PagerBeep50", "PagerBeep75", "PagerBeep" }

local function beepCandidates()
    local level = PagerMod.Config.beepVolume or 2
    local names = {}
    if BEEP_BY_VOLUME[level] then table.insert(names, BEEP_BY_VOLUME[level]) end
    if names[1] ~= "PagerBeep" then table.insert(names, "PagerBeep") end
    table.insert(names, "UIActivateButton")
    return names
end

-- The engine's UI sound emitter (SoundManager.uiEmitter) only exists once the
-- game is fully in-world AND audio actually initialised. Calling playUISound
-- before that — or when audio is off / the device failed — throws a
-- NullPointerException ("uiEmitter is null") that the engine LOGS even though we
-- pcall it. So we (a) don't beep until the first tick with a live player, and
-- (b) stop trying for the session the first time playUISound actually throws, so
-- a soundless environment gets at most one log line instead of a beep-storm.
PagerMod._soundReady  = false
PagerMod._soundBroken = false
local function pagerMarkSoundReady()
    if getPlayer() then
        PagerMod._soundReady = true
        Events.OnTick.Remove(pagerMarkSoundReady)
    end
end
Events.OnTick.Add(pagerMarkSoundReady)

function PagerMod.playBeep()
    PagerMod.refreshConfig()
    if not PagerMod.Config.notifySound then return end
    if PagerMod._soundBroken or not PagerMod._soundReady then return end
    local sm = getSoundManager()
    if not sm then return end
    for _, name in ipairs(beepCandidates()) do
        -- playUISound returns a numeric handle; a missing sound (e.g. an
        -- unshipped "PagerBeep") returns 0/-1, which is TRUTHY in Lua, so we
        -- must check the value or we'd stop on a sound that never played.
        local ok, handle = pcall(function() return sm:playUISound(name) end)
        if not ok then
            -- playUISound threw (uiEmitter null: audio off / not initialised).
            -- Give up this session so we never spam the debug log.
            PagerMod._soundBroken = true
            return
        end
        if type(handle) == "number" and handle > 0 then return end
    end
end

-- ── Held pager helpers ─────────────────────────────────────

-- Recursively collect every Pager the player carries, including inside bags.
local function collectPagers(container, out)
    if not container then return end
    local items = container:getItems()
    if not items then return end
    for i = 0, items:size() - 1 do
        local it = items:get(i)
        if it:getFullType() == PagerMod.ITEM then
            local md = it:getModData()
            md.messages = md.messages or {}
            table.insert(out, { item = it, number = md.pagerNumber, name = md.pagerName, md = md })
        end
        if instanceof(it, "InventoryContainer") then
            collectPagers(it:getInventory(), out)
        end
    end
end

function PagerMod.getHeldPagers()
    local out = {}
    local player = getPlayer()
    if not player or not player:getInventory() then return out end
    collectPagers(player:getInventory(), out)
    return out
end

-- ── Battery ────────────────────────────────────────────────
-- Time-based drain off the in-game world clock. md.batteryAge is the world
-- age (hours) when the current battery was installed; it drains over
-- PagerMod.batteryLifeHours() and then the pager goes dead until replaced.

local function worldAge()
    local gt = getGameTime()
    if not gt then return 0 end
    local ok, h = pcall(function() return gt:getWorldAgeHours() end)
    return (ok and h) or 0
end

-- 0..1 remaining charge (1 when life is infinite or the clock hasn't started).
function PagerMod.batteryFraction(item)
    PagerMod.refreshConfig()
    local life = PagerMod.batteryLifeHours()
    if life <= 0 then return 1 end
    local md = item:getModData()
    if not md.batteryAge then return 1 end
    local frac = 1 - ((worldAge() - md.batteryAge) / life)
    if frac < 0 then return 0 end
    if frac > 1 then return 1 end
    return frac
end

function PagerMod.batteryPercent(item)
    return math.floor(PagerMod.batteryFraction(item) * 100 + 0.5)
end

function PagerMod.batteryDead(item)
    return PagerMod.batteryFraction(item) <= 0
end

-- Start the battery clock on first real use (so old/looted pagers don't
-- retroactively die before you ever switch them on).
function PagerMod.ensureBattery(item)
    local md = item:getModData()
    if md.batteryAge == nil then
        md.batteryAge = worldAge()
        PagerMod.saveItem(item)
    end
end

local function findItemRecurse(container, fullType)
    if not container then return nil end
    local items = container:getItems()
    if not items then return nil end
    for i = 0, items:size() - 1 do
        local it = items:get(i)
        if it:getFullType() == fullType then return it end
        if instanceof(it, "InventoryContainer") then
            local f = findItemRecurse(it:getInventory(), fullType)
            if f then return f end
        end
    end
    return nil
end

function PagerMod.hasSpareBattery()
    local p = getPlayer()
    return p ~= nil and findItemRecurse(p:getInventory(), "Base.Battery") ~= nil
end

-- Consume one Base.Battery from inventory and refresh the pager's charge.
-- The new charge (batteryAge) must survive logout / drop / moving the pager
-- between containers. In B42 item modData is SERVER-authoritative, so a purely
-- client-side write is silently reverted on the next re-sync — that is the
-- "battery back to 0%" bug. So we both (a) write + saveItem locally, which
-- persists on B41 via transmitModData and updates the UI immediately, and
-- (b) tell the server to stamp the authoritative copy (pushItemModData), which
-- is what persists on B42. This mirrors how ASSIGN / RENAME already work.
function PagerMod.replaceBattery(item)
    local p = getPlayer()
    if not p or not item then return false end
    local batt = findItemRecurse(p:getInventory(), "Base.Battery")
    if not batt then
        PagerMod.halo(getText("IGUI_PagerMod_NeedBattery"), { r = 0.9, g = 0.4, b = 0.2 })
        return false
    end
    local cont = batt:getContainer()
    if cont then cont:Remove(batt) end

    local md = item:getModData()
    md.batteryAge = worldAge()
    PagerMod.saveItem(item)

    -- Server round-trip: persists batteryAge on the authoritative item copy (B42).
    local itemId = nil
    if item.getID then
        local ok, id = pcall(function() return item:getID() end); itemId = ok and id or nil
    end
    sendClientCommand(PagerMod.MODULE, C2S.REPLACE_BATT, { itemId = itemId, number = md.pagerNumber })

    PagerMod.playBeep()
    PagerMod.halo(getText("IGUI_PagerMod_BatteryReplaced"))
    if PagerMod.UI and PagerMod.UI.refresh then PagerMod.UI:refresh() end
    return true
end

function PagerMod.getHeldNumbers()
    local nums = {}
    for _, p in ipairs(PagerMod.getHeldPagers()) do
        if p.number then table.insert(nums, p.number) end
    end
    return nums
end

function PagerMod.findHeldByNumber(number)
    if not number then return nil end
    for _, p in ipairs(PagerMod.getHeldPagers()) do
        if p.number == number then return p end
    end
    return nil
end

function PagerMod.totalUnread()
    local n = 0
    for _, p in ipairs(PagerMod.getHeldPagers()) do
        n = n + (p.md.unread or 0)
    end
    return n
end

-- ── Registration ───────────────────────────────────────────

-- ── Persistence ────────────────────────────────────────────
-- Client modData edits are only saved if synced to the (authoritative) server
-- copy; without this the pager number, messages, battery and contacts are lost
-- on logout in multiplayer. The item-sync API differs by build and pcall does
-- NOT swallow a nil-method call, so we feature-detect:
--   B41: item:transmitModData()
--   B42: syncItemModData(player, item)  (InventoryItem has no transmitModData)
function PagerMod.saveItem(item)
    if not item then return end
    if item.transmitModData then            -- B41
        item:transmitModData()
    elseif syncItemModData then             -- B42 (item modData sync)
        syncItemModData(getPlayer(), item)
    elseif item.syncItemFields then         -- B42 fallback
        item:syncItemFields()
    end
end
function PagerMod.savePlayer()
    local p = getPlayer()
    if p and p.transmitModData then p:transmitModData() end
end

-- ── Per-character pager data (contacts / channels / block list) ─────
local function pagerData()
    local md = getPlayer():getModData()
    md.pagerMod = md.pagerMod or {}
    md.pagerMod.contacts = md.pagerMod.contacts or {}
    md.pagerMod.channels = md.pagerMod.channels or {}
    md.pagerMod.blocked  = md.pagerMod.blocked  or {}
    return md.pagerMod
end

function PagerMod.getBlocked() return pagerData().blocked end

function PagerMod.isBlockedNumber(n)
    for _, x in ipairs(PagerMod.getBlocked()) do
        if x == n then return true end
    end
    return false
end

-- Returns the new blocked state (true = now blocked).
function PagerMod.toggleBlock(number)
    number = PagerMod.sanitizeNumber(number)
    if number == "" then return false end
    local list = PagerMod.getBlocked()
    for i, x in ipairs(list) do
        if x == number then
            table.remove(list, i)
            PagerMod.savePlayer()
            PagerMod.registerNumbers(true)
            return false
        end
    end
    table.insert(list, number)
    PagerMod.savePlayer()
    PagerMod.registerNumbers(true)
    return true
end

function PagerMod.getChannels() return pagerData().channels end
function PagerMod.saveChannels(list) pagerData().channels = list; PagerMod.savePlayer() end

function PagerMod.getContacts() return pagerData().contacts end
function PagerMod.saveContacts(list) pagerData().contacts = list; PagerMod.savePlayer() end

-- A tower console has no backing item; each tower's inbox/name lives in player
-- modData keyed by its number, so history persists across sessions.
function PagerMod.getTowerMD(number, name)
    local pd = pagerData()
    pd.towers = pd.towers or {}
    local t = pd.towers[number] or {}
    pd.towers[number] = t
    t.pagerNumber = number
    if name then t.pagerName = name end
    t.pagerName = t.pagerName or getText("IGUI_PagerMod_TowerName")
    t.messages  = t.messages or {}
    t.unread    = t.unread or 0
    return t
end
function PagerMod.saveTowerMD() PagerMod.savePlayer() end

-- The town-wide global channel feed (one shared store in player modData).
function PagerMod.getGlobalMD()
    local pd = pagerData()
    pd.global = pd.global or {}
    pd.global.messages = pd.global.messages or {}
    pd.global.unread   = pd.global.unread or 0
    return pd.global
end

-- Add (or update) a contact; returns true if it was newly added.
function PagerMod.addContact(number, name, updateOnly)
    number = PagerMod.sanitizeNumber(number)
    if number == "" then return false end
    local list = PagerMod.getContacts()
    for _, c in ipairs(list) do
        if c.number == number then
            if name and name ~= "" then c.name = name:sub(1, 24) end
            PagerMod.savePlayer()
            return false
        end
    end
    if updateOnly then return false end  -- a rename push: don't create new contacts
    table.insert(list, { number = number, name = (name and name ~= "" and name:sub(1, 24)) or PagerMod.formatNumber(number) })
    PagerMod.savePlayer()
    return true
end

function PagerMod.registerNumbers(force)
    local nums = PagerMod.getHeldNumbers()
    table.sort(nums)
    local key = table.concat(nums, ",")
    if not force and key == PagerMod._lastRegistered then return end
    PagerMod._lastRegistered = key
    sendClientCommand(PagerMod.MODULE, C2S.REGISTER, {
        numbers = nums,
        blocked = PagerMod.getBlocked(),
    })
end

function PagerMod.requestAssign(item, name)
    PagerMod._reqSeq = PagerMod._reqSeq + 1
    local reqId = PagerMod._reqSeq
    PagerMod._pending[reqId] = item
    -- Pass the item's network id so the SERVER writes the number onto the exact
    -- pager you clicked. Item modData is server-authoritative in B42 (client
    -- sync is a no-op), so the server must set it for it to persist.
    local itemId = nil
    if item and item.getID then
        local ok, id = pcall(function() return item:getID() end); itemId = ok and id or nil
    end
    sendClientCommand(PagerMod.MODULE, C2S.ASSIGN, { reqId = reqId, name = name, itemId = itemId })
end

-- ── Sending ────────────────────────────────────────────────

local function senderFields(fromItem)
    local md = fromItem and fromItem:getModData() or {}
    return md.pagerNumber, md.pagerName or getPlayer():getUsername()
end

-- Returns true (and warns) if this pager can't send right now: receive-only
-- mode, or a dead battery.
local function sendBlocked(fromItem)
    if not PagerMod.canSend() then
        halo(getText("IGUI_PagerMod_ReceiveOnly"), ERROR_COLOR)
        return true
    end
    if fromItem and PagerMod.batteryDead(fromItem) then
        halo(getText("IGUI_PagerMod_BatteryDead"), ERROR_COLOR)
        return true
    end
    return false
end

-- Every outgoing page goes through here: trim/clamp it, and if the sender is
-- illiterate (Illiteracy sandbox option) reduce it to a digits-only code, since
-- a character who can't read can't write words either.
local function outgoing(text)
    return PagerMod.trimMessage(PagerMod.illiterateStrip(text))
end

function PagerMod.sendMessage(fromItem, toNumber, text, withLocation)
    if sendBlocked(fromItem) then return false end
    text = outgoing(text)
    toNumber = PagerMod.sanitizeNumber(toNumber)
    if text == "" or toNumber == "" then
        halo(getText("IGUI_PagerMod_EmptyMessage"), ERROR_COLOR)
        return false
    end
    local from, fromName = senderFields(fromItem)
    sendClientCommand(PagerMod.MODULE, C2S.SEND, {
        to = toNumber, text = text, from = from, fromName = fromName,
        location = withLocation == true,
    })
    return true
end

function PagerMod.broadcast(fromItem, text, withLocation)
    if sendBlocked(fromItem) then return false end
    text = outgoing(text)
    if text == "" then
        halo(getText("IGUI_PagerMod_EmptyMessage"), ERROR_COLOR)
        return false
    end
    local from, fromName = senderFields(fromItem)
    sendClientCommand(PagerMod.MODULE, C2S.BROADCAST, {
        text = text, from = from, fromName = fromName,
        location = withLocation == true,
    })
    return true
end

function PagerMod.sendChannel(fromItem, channel, text, withLocation)
    if sendBlocked(fromItem) then return false end
    text = outgoing(text)
    if text == "" then
        halo(getText("IGUI_PagerMod_EmptyMessage"), ERROR_COLOR)
        return false
    end
    if not channel or not channel.members or #channel.members == 0 then
        halo(getText("IGUI_PagerMod_EmptyChannel"), ERROR_COLOR)
        return false
    end
    local from, fromName = senderFields(fromItem)
    sendClientCommand(PagerMod.MODULE, C2S.CHANNEL, {
        members = channel.members, channelName = channel.name,
        text = text, from = from, fromName = fromName,
        location = withLocation == true,
    })
    return true
end

function PagerMod.sendSOS(fromItem, text)
    if sendBlocked(fromItem) then return false end
    local from, fromName = senderFields(fromItem)
    sendClientCommand(PagerMod.MODULE, C2S.SOS, {
        text = outgoing(text or ""),
        from = from, fromName = fromName,
    })
    return true
end

-- ── Tower console sends (operate a deployed tower as its own number) ─────
-- These bypass the pager receive-only / battery checks: a tower is not a pager
-- and can always send. The server validates the player is next to THAT powered
-- tower (viaTower + from=towerNumber) before routing.
function PagerMod.towerSend(towerNumber, towerName, toNumber, text, withLocation)
    text = outgoing(text)
    toNumber = PagerMod.sanitizeNumber(toNumber)
    if text == "" or toNumber == "" then
        halo(getText("IGUI_PagerMod_EmptyMessage"), ERROR_COLOR)
        return false
    end
    sendClientCommand(PagerMod.MODULE, C2S.SEND, {
        to = toNumber, text = text, from = towerNumber, fromName = towerName,
        location = withLocation == true, viaTower = true,
    })
    return true
end

function PagerMod.towerBroadcast(towerNumber, towerName, text, withLocation)
    text = outgoing(text)
    if text == "" then halo(getText("IGUI_PagerMod_EmptyMessage"), ERROR_COLOR); return false end
    sendClientCommand(PagerMod.MODULE, C2S.BROADCAST, {
        text = text, from = towerNumber, fromName = towerName,
        location = withLocation == true, viaTower = true,
    })
    return true
end

function PagerMod.towerSendChannel(towerNumber, towerName, channel, text, withLocation)
    text = outgoing(text)
    if text == "" then halo(getText("IGUI_PagerMod_EmptyMessage"), ERROR_COLOR); return false end
    if not channel or not channel.members or #channel.members == 0 then
        halo(getText("IGUI_PagerMod_EmptyChannel"), ERROR_COLOR)
        return false
    end
    sendClientCommand(PagerMod.MODULE, C2S.CHANNEL, {
        members = channel.members, channelName = channel.name,
        text = text, from = towerNumber, fromName = towerName,
        location = withLocation == true, viaTower = true,
    })
    return true
end

function PagerMod.towerSOS(towerNumber, towerName, text)
    sendClientCommand(PagerMod.MODULE, C2S.SOS, {
        text = outgoing(text or ""),
        from = towerNumber, fromName = towerName, viaTower = true,
    })
    return true
end

-- Ask the server to open the tower at this square (it replies with TOWER_OPEN).
function PagerMod.requestOpenTower(square)
    if not square then return end
    sendClientCommand(PagerMod.MODULE, C2S.OPEN_TOWER, {
        x = square:getX(), y = square:getY(), z = square:getZ(),
    })
end

-- Rename the tower at this square (server validates owner/admin; the new name
-- syncs to everyone's saved contact, the console title and outgoing pages).
function PagerMod.requestRenameTower(square, name)
    if not square or not name or name == "" then return end
    sendClientCommand(PagerMod.MODULE, C2S.RENAME_TOWER, {
        x = square:getX(), y = square:getY(), z = square:getZ(), name = name,
    })
end

-- Rename my own pager. The server persists the name onto the item (authoritative
-- in B42) and pushes it to everyone who has this number saved, so their contact
-- entry stays in sync. Pass the item so the server can find it by network id.
function PagerMod.requestRenamePager(item, name)
    if not item or not name or name == "" then return end
    local md = item:getModData()
    local itemId = nil
    if item.getID then
        local ok, id = pcall(function() return item:getID() end); itemId = ok and id or nil
    end
    sendClientCommand(PagerMod.MODULE, C2S.RENAME, {
        itemId = itemId, number = md.pagerNumber, name = name,
    })
end

-- Post to the town-wide global channel. Obeys the same send rules as a normal
-- page (receive-only, battery, cooldown) but reaches everyone regardless of
-- saved contacts or faction.
function PagerMod.globalSend(fromItem, text, withLocation)
    if sendBlocked(fromItem) then return false end
    text = outgoing(text)
    if text == "" then halo(getText("IGUI_PagerMod_EmptyMessage"), ERROR_COLOR); return false end
    local from, fromName = senderFields(fromItem)
    sendClientCommand(PagerMod.MODULE, C2S.GLOBAL, {
        text = text, from = from, fromName = fromName, location = withLocation == true,
    })
    return true
end

-- Broadcast my number to nearby pagers so they can save me as a contact.
function PagerMod.shareNearby(fromItem)
    local md = fromItem and fromItem:getModData() or {}
    if not md.pagerNumber then
        halo(getText("IGUI_PagerMod_NeedActivate"), ERROR_COLOR)
        return
    end
    sendClientCommand(PagerMod.MODULE, C2S.SHARE_NEARBY, {
        from = md.pagerNumber,
        fromName = md.pagerName or getPlayer():getUsername(),
    })
end

-- Toggle "do not disturb" on a specific pager. Returns the new muted state.
function PagerMod.toggleMute(item)
    local md = item:getModData()
    md.muted = not md.muted
    PagerMod.saveItem(item)
    return md.muted == true
end

-- Mark every page on a pager as read (and fire read receipts).
function PagerMod.markPagerRead(item)
    local md = item:getModData()
    md.messages = md.messages or {}
    local justRead = {}
    for _, m in ipairs(md.messages) do
        if not m.read then m.read = true; table.insert(justRead, m) end
    end
    md.unread = 0
    PagerMod.saveItem(item)
    PagerMod.sendReadReceipts(justRead, item)
    if PagerMod.UI and PagerMod.UI.refresh then PagerMod.UI:refresh() end
end

-- Speak the pager's number aloud (handy to share over proximity voice chat).
function PagerMod.sayNumber(fromItem)
    local md = fromItem and fromItem:getModData() or {}
    if not md.pagerNumber then
        halo(getText("IGUI_PagerMod_NeedActivate"), ERROR_COLOR)
        return
    end
    getPlayer():Say(string.format(getText("IGUI_PagerMod_SayNumber"), PagerMod.formatNumber(md.pagerNumber)))
end

-- Tell the server which pages we just read (drives read receipts).
function PagerMod.sendReadReceipts(messages, myItem)
    PagerMod.refreshConfig()
    if not PagerMod.Config.readReceipts or not messages or #messages == 0 then return end
    local acks = {}
    for _, m in ipairs(messages) do
        if m.fromUser then table.insert(acks, { toUser = m.fromUser }) end
    end
    if #acks == 0 then return end
    local md = myItem and myItem:getModData() or {}
    local label = md.pagerName
        or (md.pagerNumber and PagerMod.formatNumber(md.pagerNumber))
        or getPlayer():getUsername()
    sendClientCommand(PagerMod.MODULE, C2S.MARK_READ, { acks = acks, readerName = label })
end

-- A one-line location/distance summary for a received page, or nil.
-- Coordinates are coerced to numbers: in MP the page crosses the command bus
-- and a field could arrive as a non-number, which used to break arithmetic
-- (and, via the inbox renderer, the whole pager window).
function PagerMod.locationLine(msg)
    if not msg then return nil end
    local mx, my = tonumber(msg.x), tonumber(msg.y)
    if not mx or not my then return nil end
    local label = "@ " .. math.floor(mx) .. "," .. math.floor(my)
    local p = getPlayer()
    if p then
        local px, py = p:getX(), p:getY()
        local dist = PagerMod.distance(px, py, mx, my)
        label = label .. "  " .. PagerMod.distanceLabel(dist)
        local dir = PagerMod.compass(mx - px, my - py)
        -- NOTE: `dir ~= ""` does NOT catch nil (nil ~= "" is true in Lua), which
        -- let a nil compass result hit the concat -> "__concat ... null" crash.
        if dir and dir ~= "" then label = label .. " " .. dir end
    end
    return label
end

function PagerMod.fetchInbox()
    sendClientCommand(PagerMod.MODULE, C2S.FETCH, { numbers = PagerMod.getHeldNumbers() })
end

function PagerMod.pingNetwork()
    sendClientCommand(PagerMod.MODULE, C2S.PING, {})
end

-- ── Pager tower (Ultra tier) ───────────────────────────────
-- The tower is a solid world object: deploying spawns an IsoThumpable using a
-- vanilla collidable sprite (PagerMod.TOWER_SPRITE) so players can walk around
-- but not through it, and registers that tile with the server for TOWER signal
-- mode. Pickup removes the object and that registration.

-- PagerMod.isTowerObject is defined in shared/PagerMod_TowerSprite.lua
-- (by object name + modData tag).
function PagerMod.findTowerObject(square)
    if not square then return nil end
    local function scan(objs)
        if not objs then return nil end
        for i = 0, objs:size() - 1 do
            local o = objs:get(i)
            if PagerMod.isTowerObject(o) then return o end
        end
        return nil
    end
    -- World inventory objects (the deployed tower item) live in getWorldObjects().
    return scan(square:getObjects()) or scan(square:getSpecialObjects())
        or (square.getWorldObjects and scan(square:getWorldObjects()))
end

function PagerMod.deployTower(item)
    local p = getPlayer()
    local origin = p and p:getCurrentSquare()
    if not origin or not item then return end
    -- Place the solid tower on the tile the player is facing so they aren't
    -- trapped standing inside it; fall back to their own tile at the map edge.
    local square = origin:getAdjacentSquare(p:getDir()) or origin
    -- Don't stack two towers on the same tile.
    if PagerMod.findTowerObject(square) then
        PagerMod.halo(getText("IGUI_PagerMod_TowerAlreadyHere"), { r = 0.9, g = 0.7, b = 0.2 })
        return
    end

    -- Facing for the tower art: 0 = up/down sprite, 1 = left/right, from the way
    -- the player is looking. Sent to the server, which stamps it on the object.
    local dir = 0
    pcall(function()
        local d = p:getDir()
        if d == IsoDirections.E or d == IsoDirections.W
            or d == IsoDirections.SE or d == IsoDirections.NW then
            dir = 1
        end
    end)

    -- Consume the carried tower item (inventory removal DOES sync in B42). The
    -- deployed tower is itself a PagerTower world item sitting on the tile, which
    -- has NO container — so requiring one here blocks "setting up" that ground
    -- object (which consumed nothing and duped towers). It must be carried.
    local cont = item:getContainer()
    if not cont then return end
    cont:Remove(item)

    -- IMPORTANT: the world object is spawned by the SERVER, not here. A
    -- client-side square:transmitAddObjectToSquare only actually transmits when
    -- run on the server (the engine gates it on GameServer.server), so a
    -- client-placed tower is invisible to every other player and can't be
    -- operated by them — which is exactly the MP tower bug. The server creates
    -- it authoritatively (onDeployTower) as a dropped WorldStaticModel item,
    -- which renders the 3D tower and syncs to every client.
    sendClientCommand(PagerMod.MODULE, C2S.DEPLOY_TOWER, {
        x = square:getX(), y = square:getY(), z = square:getZ(), dir = dir,
    })
end

-- Pick a deployed tower back up. `obj` is the IsoThumpable the player clicked.
-- Pickup is server-authoritative (ownership is enforced there): we only ask, and
-- the server removes the object and returns the item if allowed.
function PagerMod.pickupTower(obj)
    local p = getPlayer()
    if not p or not obj then return end
    local square = obj:getSquare()
    if not square then return end
    sendClientCommand(PagerMod.MODULE, C2S.PICKUP_TOWER, {
        x = square:getX(), y = square:getY(), z = square:getZ(),
    })
end

-- ── Storing received messages on the physical pager ─────────

-- Append messages to a store (pager item md or the tower md), enforcing the cap.
local function appendMessages(md, messages, markRead)
    md.messages = md.messages or {}
    md.unread = md.unread or 0
    local added = 0
    for _, msg in ipairs(messages) do
        msg.read = markRead == true
        table.insert(md.messages, msg)
        if not msg.read then md.unread = md.unread + 1 end
        added = added + 1
    end
    -- Enforce the per-pager cap (oldest first).
    PagerMod.refreshConfig()
    local cap = PagerMod.Config.maxMessages or 50
    while #md.messages > cap do
        local removed = table.remove(md.messages, 1)
        if removed and not removed.read and md.unread > 0 then
            md.unread = md.unread - 1
        end
    end
    return added
end

-- Global-channel pages go to the shared global feed (deduped by id, since
-- holding multiple pagers would otherwise receive each post more than once).
local function storeGlobal(messages, markRead)
    local g = PagerMod.getGlobalMD()
    local seen = {}
    for _, m in ipairs(g.messages) do if m.id then seen[m.id] = true end end
    local fresh = {}
    for _, m in ipairs(messages) do
        if not (m.id and seen[m.id]) then
            fresh[#fresh + 1] = m
            if m.id then seen[m.id] = true end
        end
    end
    if #fresh == 0 then return 0 end
    local added = appendMessages(g, fresh, markRead)
    if added > 0 then PagerMod.savePlayer() end
    return added
end

local function storeMessages(number, messages, markRead)
    -- Split out global-channel pages (they go to the shared global feed, not the
    -- per-pager inbox). Tower-node pages arrive via the TOWER_OPEN flow, not here.
    local normal, globals = {}, {}
    for _, m in ipairs(messages) do
        if m.global then globals[#globals + 1] = m else normal[#normal + 1] = m end
    end
    local added = 0
    if #globals > 0 then added = added + storeGlobal(globals, markRead) end
    if #normal > 0 then
        local p = PagerMod.findHeldByNumber(number)
        if p then
            local a = appendMessages(p.md, normal, markRead)
            if a > 0 then PagerMod.saveItem(p.item) end
            added = added + a
        end
    end
    return added
end

-- ── Server -> Client handlers ──────────────────────────────

local function onAssigned(args)
    local item = PagerMod._pending[args.reqId]
    PagerMod._pending[args.reqId] = nil
    if not item then
        -- The reqId round-trip can miss (serialization/SP loopback); fall back
        -- to the first held pager that still lacks a number so activation never
        -- silently fails.
        for _, p in ipairs(PagerMod.getHeldPagers()) do
            if not p.number then item = p.item break end
        end
    end
    if not item then return end
    local md = item:getModData()
    md.pagerNumber = args.number
    md.pagerName = md.pagerName or getText("IGUI_PagerMod_DefaultName")
    md.messages = md.messages or {}
    md.unread = md.unread or 0
    PagerMod.ensureBattery(item)
    PagerMod.saveItem(item)   -- persist the new number/name so it survives logout
    halo(getText("IGUI_PagerMod_NumberIs") .. " " .. PagerMod.formatNumber(args.number))
    PagerMod.registerNumbers(true)
    if PagerMod.UI and PagerMod.UI.refresh then PagerMod.UI:refresh() end
    -- If activation was triggered by the hotkey, open the pager now.
    if PagerMod._openAfterAssign then
        PagerMod._openAfterAssign = nil
        if PagerMod.OpenPagerUI then PagerMod.OpenPagerUI(item) end
    end
end

local SOS_COLOR = { r = 1.0, g = 0.35, b = 0.2 }

-- Message previews go through readableText: an illiterate character sees "?"
-- instead of words (the beep itself still sounds — that's audible, not written).
local function preview(text)
    text = tostring(PagerMod.readableText(text) or "")
    if #text > 40 then text = text:sub(1, 40) .. "..." end
    return text
end

local function notifyNewMessage(number, messages)
    local first = messages[1]
    -- Guard the nil case before garbling: garbleText(nil) is "", which is truthy
    -- in Lua and would swallow the number fallback.
    local fromLabel = "?"
    if first then
        fromLabel = (first.fromName and first.fromName ~= "" and PagerMod.readableText(first.fromName))
            or PagerMod.formatNumber(first.from)
    end

    -- Global-channel pages are their own feed: beep unless the player is already
    -- watching the GLOBAL tab, and never suppressed by which pager is open.
    if first and first.global then
        -- Don't beep for your own post (it's still stored so you see it).
        local me = getPlayer()
        if me and first.fromUser and first.fromUser == me:getUsername() then return end
        local watching = PagerMod.UI and PagerMod.UI:getIsVisible() and PagerMod.UI.globalMode
        if watching then return end
        PagerMod.playBeep()
        halo("[" .. getText("IGUI_PagerMod_TabGlobal") .. "] " .. fromLabel .. ": " .. preview(first.text or ""), ALERT_COLOR)
        return
    end

    -- Suppress the beep if the UI is open on this exact pager.
    local viewing = PagerMod.UI and PagerMod.UI:getIsVisible() and PagerMod.UI.pagerNumber == number
    if viewing then return end

    -- Respect per-pager "Do not disturb" (mute) unless it is an SOS.
    local held = PagerMod.findHeldByNumber(number)
    local muted = held and held.md and held.md.muted
    local sosOverride = false
    for _, m in ipairs(messages) do if m.sos then sosOverride = true break end end
    if muted and not sosOverride then return end
    -- A dead pager doesn't beep (the message is still stored for later).
    if held and held.item and PagerMod.batteryDead(held.item) then return end

    PagerMod.playBeep()
    local anySOS = false
    for _, m in ipairs(messages) do if m.sos then anySOS = true break end end

    local n = #messages
    if anySOS then
        PagerMod.playBeep()
        halo(getText("IGUI_PagerMod_SOSFrom") .. " " .. fromLabel .. "! " .. preview(first.text), SOS_COLOR)
        -- Briefly mark each SOS sender's location on the world map, if it came with one.
        if PagerMod.addSOSMapPing then
            for _, m in ipairs(messages) do
                if m.sos and m.x and m.y then PagerMod.addSOSMapPing(m.x, m.y) end
            end
        end
    elseif n == 1 then
        halo(fromLabel .. ": " .. preview(first.text), ALERT_COLOR)
    else
        halo(string.format("%d %s", n, getText("IGUI_PagerMod_NewMessages")), ALERT_COLOR)
    end
end

local function onDeliver(args)
    -- If we no longer hold a pager with this number (dropped/traded mid-flight),
    -- bounce the messages back to the server so they aren't lost. Tower nodes are
    -- never delivered to live (their pages queue for the console), so this only
    -- ever concerns real pagers.
    if not PagerMod.findHeldByNumber(args.number) then
        sendClientCommand(PagerMod.MODULE, C2S.REQUEUE, { number = args.number, messages = args.messages })
        return
    end
    local added = storeMessages(args.number, args.messages or {}, false)
    if added > 0 then
        -- Remember the most recent real sender for "Page last sender".
        for i = #args.messages, 1, -1 do
            local f = args.messages[i].from
            if f and f ~= "SYSTEM" then PagerMod.lastSender = f break end
        end
        notifyNewMessage(args.number, args.messages)
        if PagerMod.UI and PagerMod.UI:getIsVisible() and PagerMod.UI.pagerNumber == args.number then
            PagerMod.UI:markAllRead()
            PagerMod.UI:refresh()
        elseif PagerMod.UI and PagerMod.UI.refresh then
            PagerMod.UI:refresh()
        end
    end
end

local function onInbox(args)
    local total = 0
    for number, messages in pairs(args.inbox or {}) do
        total = total + storeMessages(number, messages, false)
    end
    if total > 0 then
        PagerMod.playBeep()
        halo(string.format("%d %s", total, getText("IGUI_PagerMod_QueuedDelivered")), ALERT_COLOR)
    end
    if PagerMod.UI and PagerMod.UI.refresh then PagerMod.UI:refresh() end
end

local function onStatus(args)
    PagerMod.netStatus = args or {}
    if PagerMod.UI and PagerMod.UI.refresh then PagerMod.UI:refresh() end
end

local function onSentOk(args)
    if args.global then
        halo(string.format(getText("IGUI_PagerMod_GlobalSent"), args.count or 0))
    elseif args.sos then
        halo(string.format(getText("IGUI_PagerMod_SOSSent"), args.count or 0), SOS_COLOR)
    elseif args.channel then
        halo(string.format(getText("IGUI_PagerMod_ChannelSent"), tostring(args.channel), args.count or 0))
    elseif args.broadcast then
        halo(string.format(getText("IGUI_PagerMod_BroadcastSent"), args.count or 0))
    elseif args.delivered then
        halo(getText("IGUI_PagerMod_Delivered"))
    else
        halo(getText("IGUI_PagerMod_Queued"))
    end
    if PagerMod.UI and PagerMod.UI.onMessageSent then PagerMod.UI:onMessageSent() end
end

local function onSentFail(args)
    halo(args.reason or getText("IGUI_PagerMod_SendFailed"), ERROR_COLOR)
end

local function onInfo(args)
    halo(args.text or "", args.isError and ERROR_COLOR or INFO_COLOR)
end

local function onAddContact(args)
    if not args.number then return end
    local added = PagerMod.addContact(args.number, args.name, args.updateOnly)
    if added then
        PagerMod.playBeep()
        halo(string.format(getText("IGUI_PagerMod_GotNumberFrom"), args.name or PagerMod.formatNumber(args.number)), ALERT_COLOR)
    end
    -- Refresh either way so a renamed contact updates in an open window.
    if PagerMod.UI and PagerMod.UI.refresh then PagerMod.UI:refresh() end
end

-- Server resolved a tower we asked to operate: stash its drained inbox into that
-- tower's client store and open the console bound to it.
local function onTowerOpen(args)
    if not args or not args.number then return end
    local md = PagerMod.getTowerMD(args.number, args.name)
    if args.inbox and #args.inbox > 0 then
        appendMessages(md, args.inbox, false)
        PagerMod.saveTowerMD()
    end
    if PagerMod.OpenTowerUI then
        PagerMod.OpenTowerUI(args.number, args.name, args.powered)
    end
end

-- Remove the deployed tower object at a tile (used by both refund and pickup).
local function removeTowerObjectAt(x, y, z)
    local cell = getCell and getCell()
    local sq = cell and cell:getGridSquare(x, y, z)
    if not sq then return end
    local obj = PagerMod.findTowerObject(sq)
    if obj then
        sq:transmitRemoveItemFromSquare(obj)
        sq:RecalcAllWithNeighbours(true)
    end
end

-- Server rejected an optimistically-placed tower: remove the object we put down
-- and hand the carried item back.
local function onTowerRefund(args)
    if not args then return end
    removeTowerObjectAt(args.x, args.y, args.z)
    local p = getPlayer()
    if p then p:getInventory():AddItem(PagerMod.TOWER) end
end

-- Pickup was authorised by the server (it already granted the item): remove the
-- world object locally so it actually disappears on the host's view.
local function onPickupOk(args)
    if not args then return end
    removeTowerObjectAt(args.x, args.y, args.z)
end

local DISPATCH = {
    [S2C.ASSIGNED]    = onAssigned,
    [S2C.DELIVER]     = onDeliver,
    [S2C.INBOX]       = onInbox,
    [S2C.STATUS]      = onStatus,
    [S2C.SENT_OK]     = onSentOk,
    [S2C.SENT_FAIL]   = onSentFail,
    [S2C.INFO]        = onInfo,
    [S2C.ADD_CONTACT] = onAddContact,
    [S2C.TOWER_OPEN]  = onTowerOpen,
    [S2C.TOWER_REFUND]= onTowerRefund,
    [S2C.PICKUP_OK]   = onPickupOk,
}

-- Exposed so the server can deliver directly in singleplayer, where
-- Events.OnServerCommand never fires (the command bus is one-way in SP).
function PagerMod.handleServerCommand(command, args)
    local h = DISPATCH[command]
    if h then h(args or {}) end
end

local function onServerCommand(module, command, args)
    if module ~= PagerMod.MODULE then return end
    PagerMod.handleServerCommand(command, args)
end

-- ── Lifecycle: keep registration in sync ───────────────────

local started = false
local function onPlayerStart()
    if started then return end
    started = true
    PagerMod.refreshConfig()
    PagerMod.registerNumbers(true)
    PagerMod.fetchInbox()
    PagerMod.pingNetwork()
end

-- Periodically re-sync the held-number set (covers loot/trade/drop).
-- OnTick's argument is a frame counter, so throttle on real seconds.
local lastCheck = 0
local function onTick()
    if not started then return end
    local now = getTimestamp()
    if now - lastCheck < 5 then return end
    lastCheck = now
    PagerMod.registerNumbers(false)
end

Events.OnServerCommand.Add(onServerCommand)
Events.OnCreatePlayer.Add(onPlayerStart)
Events.OnGameStart.Add(onPlayerStart)
Events.OnTick.Add(onTick)

print("[PagerMod] Client loaded")

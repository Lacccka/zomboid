-- Admin/debug context-menu helper for spawning a small Bandits stress-test group.
--
-- Five independent size=1 requests are intentional: upstream spawnGroup caps one
-- Clan request to the number of unique profiles in a clan. Requests are queued and
-- sent with a short gap so Bandits/vanilla MP synchronization is not hit by five
-- spawn+GlobalModData transmissions in the same client frame.
if isServer() then return end

local Guard = require "LCC/Guard"
local FEATURE = "bandits.admin-spawn-menu"
local SPAWN_COUNT = 5
local SEND_INTERVAL_TICKS = 30
local MODULE = "LCCBanditsTest"
local COMMAND = "SpawnOne"

Guard.safeRequire(FEATURE, "BanditCustom")
Guard.safeRequire(FEATURE, "BanditCompatibility")
if not Guard.isEnabled(FEATURE) then return end

local pending = {}
local ticksUntilSend = 0
local batchSerial = 0

local function hasStaffAccess(player)
    if type(isDebugEnabled) == "function" then
        local ok, enabled = pcall(isDebugEnabled)
        if ok and enabled then return true end
    end

    if type(isAdmin) == "function" then
        local ok, admin = pcall(isAdmin)
        if ok and admin then return true end
    end

    if player and player.getAccessLevel then
        local ok, access = pcall(function()
            return player:getAccessLevel()
        end)
        if ok and access then
            local normalized = string.lower(tostring(access))
            if normalized == "admin"
                    or normalized == "gm"
                    or normalized == "overseer"
                    or normalized == "moderator" then
                return true
            end
        end
    end

    return false
end

local function getClickedSquare(player, worldobjects)
    if BanditCompatibility and type(BanditCompatibility.GetClickedSquare) == "function" then
        local ok, square = pcall(BanditCompatibility.GetClickedSquare)
        if ok and square then return square end
    end

    if type(worldobjects) == "table" then
        for _, object in ipairs(worldobjects) do
            if object and object.getSquare then
                local ok, square = pcall(function()
                    return object:getSquare()
                end)
                if ok and square then return square end
            end
        end
    end

    if player and player.getSquare then
        local ok, square = pcall(function()
            return player:getSquare()
        end)
        if ok then return square end
    end

    return nil
end

local function tableSize(tab)
    if type(tab) ~= "table" then return 0 end
    local count = 0
    for _ in pairs(tab) do count = count + 1 end
    return count
end

local function squareIsFree(square)
    if not square then return false end
    local ok, free = pcall(function()
        return square:isFree(false)
    end)
    return ok and free == true
end

local function getSpawnSquares(player, clickedSquare, count)
    local result = {}
    if not player or not clickedSquare then return result end

    local cell = player:getCell()
    if not cell then return result end

    local x = clickedSquare:getX()
    local y = clickedSquare:getY()
    local z = clickedSquare:getZ()

    local offsets = {
        {0, 0},
        {1, 0}, {-1, 0}, {0, 1}, {0, -1},
        {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
        {2, 0}, {-2, 0}, {0, 2}, {0, -2},
    }

    for i = 1, #offsets do
        if #result >= count then break end
        local ox, oy = offsets[i][1], offsets[i][2]
        local square = cell:getGridSquare(x + ox, y + oy, z)
        if square and (i == 1 or squareIsFree(square)) then
            result[#result + 1] = square
        end
    end

    while #result < count do
        result[#result + 1] = clickedSquare
    end

    return result
end

local function nextBatchId()
    batchSerial = batchSerial + 1
    local stamp = type(getTimestampMs) == "function" and getTimestampMs() or 0
    return tostring(stamp) .. "-" .. tostring(batchSerial)
end

local function queueBanditBatch(player, square, cid)
    if not player or not square or not cid then return end
    if type(sendClientCommand) ~= "function" then return end

    local spawnSquares = getSpawnSquares(player, square, SPAWN_COUNT)
    if #spawnSquares == 0 then return end

    local batch = nextBatchId()
    local queueWasEmpty = #pending == 0

    for i = 1, SPAWN_COUNT do
        local spawnSquare = spawnSquares[i]
        pending[#pending + 1] = {
            player = player,
            batch = batch,
            index = i,
            total = SPAWN_COUNT,
            cid = cid,
            x = spawnSquare:getX(),
            y = spawnSquare:getY(),
            z = spawnSquare:getZ(),
        }
    end

    -- Let the first request leave on the next OnTick; subsequent requests are
    -- spaced by SEND_INTERVAL_TICKS.
    if queueWasEmpty then ticksUntilSend = 0 end

    print(string.format(
        "[LCC][BanditsSpawn][BATCH_QUEUED] batch=%s count=%d cid=%s around=%d,%d,%d queueDepth=%d intervalTicks=%d",
        batch,
        SPAWN_COUNT,
        tostring(cid),
        square:getX(), square:getY(), square:getZ(),
        #pending,
        SEND_INTERVAL_TICKS
    ))
end

local function processSpawnQueue()
    if #pending == 0 then return end

    if ticksUntilSend > 0 then
        ticksUntilSend = ticksUntilSend - 1
        return
    end

    local item = table.remove(pending, 1)
    if not item or not item.player then return end

    local args = {
        lccBatch = item.batch,
        lccIndex = item.index,
        lccTotal = item.total,
        cid = item.cid,
        x = item.x,
        y = item.y,
        z = item.z,
        program = "Bandit",
        size = 1,
    }

    sendClientCommand(item.player, MODULE, COMMAND, args)
    print(string.format(
        "[LCC][BanditsSpawn][SEND] batch=%s index=%d/%d cid=%s at=%d,%d,%d remaining=%d",
        item.batch,
        item.index,
        item.total,
        tostring(item.cid),
        item.x, item.y, item.z,
        #pending
    ))

    if item.index == item.total then
        print(string.format(
            "[LCC][BanditsSpawn][BATCH_SENT] batch=%s count=%d",
            item.batch,
            item.total
        ))
    end

    ticksUntilSend = SEND_INTERVAL_TICKS
end

local function addSpawnMenu(playerID, context, worldobjects, test)
    local player = getSpecificPlayer(playerID)
    if not player or not hasStaffAccess(player) then return end

    local square = getClickedSquare(player, worldobjects)
    if not square then return end

    BanditCustom.Load()
    local clans = BanditCustom.ClanGetAllSorted()
    if type(clans) ~= "table" then return end

    local root = context:addOption("[LCC TEST] Spawn 5 Bandits")
    local submenu = context:getNew(context)
    context:addSubMenu(root, submenu)

    local count = 0
    for cid, clan in pairs(clans) do
        if cid and clan and clan.general then
            count = count + 1
            local name = clan.general.name or tostring(cid)
            local profiles = BanditCustom.GetFromClan(cid)
            local profileCount = tableSize(profiles)
            local label = string.format("Clan %s [profiles: %d]", tostring(name), profileCount)

            local option = submenu:addOption(label, player, queueBanditBatch, square, cid)
            if profileCount == 0 then
                option.notAvailable = true
            end
        end
    end

    if count == 0 then
        root.notAvailable = true
        local option = submenu:addOption("No Bandits clans available")
        option.notAvailable = true
    end
end

Guard.install {
    id = FEATURE,
    validate = function()
        if type(BanditCustom) ~= "table"
                or type(BanditCustom.Load) ~= "function"
                or type(BanditCustom.ClanGetAllSorted) ~= "function"
                or type(BanditCustom.GetFromClan) ~= "function" then
            return false, "BanditCustom clan API is unavailable"
        end
        if not Events or not Events.OnPreFillWorldObjectContextMenu or not Events.OnTick then
            return false, "required context-menu/OnTick event is unavailable"
        end
        if type(sendClientCommand) ~= "function" then
            return false, "sendClientCommand is unavailable"
        end
        return true
    end,
    install = function()
        Events.OnPreFillWorldObjectContextMenu.Add(function(playerID, context, worldobjects, test)
            if Guard.isEnabled(FEATURE) then
                Guard.protect(FEATURE, "build context menu", addSpawnMenu, playerID, context, worldobjects, test)
            end
        end)

        Events.OnTick.Add(function()
            if Guard.isEnabled(FEATURE) then
                Guard.protect(FEATURE, "process staged spawn queue", processSpawnQueue)
            end
        end)

        print(string.format(
            "[LCC][BanditsSpawn][CLIENT_INIT] staged test spawner active; count=%d intervalTicks=%d",
            SPAWN_COUNT,
            SEND_INTERVAL_TICKS
        ))
    end,
}

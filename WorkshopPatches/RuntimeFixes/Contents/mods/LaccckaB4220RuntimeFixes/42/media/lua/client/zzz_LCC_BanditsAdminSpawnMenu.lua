-- Admin/debug context-menu helper for spawning one Bandits NPC at a chosen square.
-- Uses Bandits' own Spawner/Clan server command path; no NPC construction is
-- duplicated here.
if isServer() then return end

local Guard = require "LCC/Guard"
local FEATURE = "bandits.admin-spawn-menu"

Guard.safeRequire(FEATURE, "BanditCustom")
Guard.safeRequire(FEATURE, "BanditCompatibility")
if not Guard.isEnabled(FEATURE) then return end

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

local function spawnOneBandit(player, square, cid)
    if not player or not square or not cid then return end
    if type(sendClientCommand) ~= "function" then return end

    local args = {
        cid = cid,
        x = square:getX(),
        y = square:getY(),
        z = square:getZ(),
        program = "Bandit",
        size = 1,
    }

    sendClientCommand(player, "Spawner", "Clan", args)
    print(string.format(
        "[LCC][BanditsSpawn] requested cid=%s at %d,%d,%d",
        tostring(cid), args.x, args.y, args.z
    ))
end

local function addSpawnMenu(playerID, context, worldobjects, test)
    local player = getSpecificPlayer(playerID)
    if not player or not hasStaffAccess(player) then return end

    local square = getClickedSquare(player, worldobjects)
    if not square then return end

    BanditCustom.Load()
    local clans = BanditCustom.ClanGetAllSorted()
    if type(clans) ~= "table" then return end

    local root = context:addOption("[LCC TEST] Spawn 1 Bandit")
    local submenu = context:getNew(context)
    context:addSubMenu(root, submenu)

    local count = 0
    for cid, clan in pairs(clans) do
        if cid and clan and clan.general then
            count = count + 1
            local name = clan.general.name or tostring(cid)
            submenu:addOption("Clan " .. tostring(name), player, spawnOneBandit, square, cid)
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
                or type(BanditCustom.ClanGetAllSorted) ~= "function" then
            return false, "BanditCustom clan API is unavailable"
        end
        if not Events or not Events.OnPreFillWorldObjectContextMenu then
            return false, "world context-menu event is unavailable"
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
    end,
}

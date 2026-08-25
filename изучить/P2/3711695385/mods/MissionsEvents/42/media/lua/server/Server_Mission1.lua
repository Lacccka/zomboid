MissionsEvents = MissionsEvents or {}
MissionsEvents.M1 = MissionsEvents.M1 or {}

local M1 = MissionsEvents.M1

M1.players = M1.players or {}

-- =========================
-- CONFIG
-- =========================
local function getConfig()
    local sv = SandboxVars and SandboxVars.MissionsEvents

    return {
        enabled = sv and sv.M1_Enable ~= false,
        minZ = 5,
        maxZ = sv and sv.M1_Zombies or 20,
        cooldown = (sv and sv.M1_CooldownHours or 2) * 60,
        rewards = sv and sv.M1_Rewards or ""
    }
end

-- =========================
-- PLAYER DATA
-- =========================
local function getPlayerData(player)
    local id = player:getOnlineID() or player:getUsername()

    if not M1.players[id] then
        M1.players[id] = {
            active = false,
            kills = 0,
            maxKills = 0,
            lastStart = 0,
            endTime = 0,
            spawnedZombies = {},
            spawnSquare = nil
        }
    end

    return M1.players[id]
end

-- =========================
-- REWARDS PARSE
-- =========================
local function parseRewards(str)
    local items = {}

    for item in string.gmatch(str, "([^,]+)") do
        item = string.gsub(item, "%s+", "")
        table.insert(items, item)
    end

    return items
end

-- =========================
-- DROP REWARD
-- =========================
local function dropReward(player, data, rewards)

    local square = data.spawnSquare or player:getSquare()
    if not square then return end

    local bag = instanceItem("Base.Bag_NormalHikingBag")
    if not bag then return end

    local container = bag:getItemContainer()

    for _, itemID in ipairs(rewards) do
        local item = instanceItem(itemID)
        if item then
            container:AddItem(item)
        end
    end

    square:AddWorldInventoryItem(bag, 0.5, 0.5, 0)
end

-- =========================
-- START EVENT
-- =========================
function M1.start(player)

    local cfg = getConfig()
    if not cfg.enabled then return end

    local data = getPlayerData(player)
    local now = getGameTime():getWorldAgeHours() * 60

    -- =========================
    -- BLOQUEO: si M2 activo
    -- =========================
	local m2Players = MissionsEvents.M2 and MissionsEvents.M2.players
	
	if m2Players then
		local id = player:getOnlineID() or player:getUsername()
		local m2 = m2Players[id]
	
		if m2 and m2.active then
	
			sendServerCommand(player, "MissionsEvents", "HaloMessage", {
				text = "IGUI_M1_BlockedByM2"
			})
	
			return
		end
	end

    -- =========================
    -- COOLDOWN
    -- =========================
    if (now - data.lastStart) < cfg.cooldown then
        return
    end

    -- =========================
    -- SET DATA 
    -- =========================
    data.active = true
    data.kills = 0
    data.lastStart = now

    -- =========================
    -- SPAWN ZOMBIES
    -- =========================
    local px = math.floor(player:getX())
    local py = math.floor(player:getY())
    local pz = math.floor(player:getZ())

    local sx = px + ZombRand(-50, 51)
    local sy = py + ZombRand(-50, 51)

    local square = getCell():getGridSquare(sx, sy, pz)

    if square then
        data.spawnSquare = square

        addZombiesInOutfit(
            sx,
            sy,
            pz,
            ZombRand(cfg.minZ, cfg.maxZ + 1),
            "Survivor",
            100
        )
    end

    -- =========================
    -- NOTIFY CLIENT
    -- =========================
    sendServerCommand(player, "MissionsEvents", "HaloStartM1", {
        text = "IGUI_SpawnHorde",
        x = sx,
        y = sy,
        z = pz
    })

    dropReward(player, data, parseRewards(cfg.rewards))
end
-- =========================
-- CLIENT COMMAND
-- =========================
Events.OnClientCommand.Add(function(module, command, player, args)

    if module ~= "MissionsEvents" then return end

    if command == "StartM1" then
        M1.start(player)
    end
end)

-- =========================
-- CLEANUP LOOP
-- =========================
Events.OnPlayerUpdate.Add(function(player)

    local data = getPlayerData(player)
    local now = getGameTime():getWorldAgeHours() * 60

end)

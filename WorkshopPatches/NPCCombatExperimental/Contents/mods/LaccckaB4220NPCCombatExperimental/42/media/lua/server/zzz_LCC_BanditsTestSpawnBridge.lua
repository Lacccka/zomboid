-- Diagnostic server bridge for the LCC Bandits stress-test context menu.
--
-- The normal Bandits Spawner.Clan API is still authoritative. LCC only gives the
-- test tool its own command channel so we can prove, in the server log, whether a
-- requested NPC was actually created and registered in Bandits GlobalModData.
if not isServer() then return end

local Guard = require "LCC/Guard"
local FEATURE = "bandits.test-spawn-bridge"
local MODULE = "LCCBanditsTest"
local COMMAND = "SpawnOne"
local VERIFY_RADIUS = 1.25

Guard.safeRequire(FEATURE, "BanditUtils")
if not Guard.isEnabled(FEATURE) then return end

local function hasStaffAccess(player)
    if not player or not player.getAccessLevel then return false end

    local ok, access = pcall(function()
        return player:getAccessLevel()
    end)
    if not ok or access == nil then return false end

    local normalized = string.lower(tostring(access))
    return normalized == "admin"
        or normalized == "gm"
        or normalized == "overseer"
        or normalized == "moderator"
end

local function getBanditBrainForZombie(zombie)
    if not zombie or not zombie.getPersistentOutfitID then return nil, nil end

    local id = zombie:getPersistentOutfitID()
    if id == nil then return nil, nil end

    if type(GetBanditClusterData) ~= "function" then return id, nil end
    local cluster = GetBanditClusterData(id)
    if type(cluster) ~= "table" then return id, nil end

    return id, cluster[id]
end

local function collectBanditIdsNear(x, y, z, radius)
    local result = {}
    local count = 0
    local cell = getCell()
    if not cell then return result, count end

    local zombies = cell:getZombieList()
    if not zombies then return result, count end

    local radius2 = radius * radius
    for i = 0, zombies:size() - 1 do
        local zombie = zombies:get(i)
        if zombie then
            local dz = math.abs(zombie:getZ() - z)
            if dz < 0.5 then
                local dx = zombie:getX() - x
                local dy = zombie:getY() - y
                if dx * dx + dy * dy <= radius2 then
                    local id, brain = getBanditBrainForZombie(zombie)
                    if id ~= nil and type(brain) == "table" then
                        local key = tostring(id)
                        if not result[key] then
                            result[key] = true
                            count = count + 1
                        end
                    end
                end
            end
        end
    end

    return result, count
end

local function diffIds(before, after)
    local created = {}
    for id in pairs(after) do
        if not before[id] then
            created[#created + 1] = id
        end
    end
    table.sort(created)
    return created
end

local function handleSpawnOne(player, args)
    if type(args) ~= "table" then
        print("[LCC][BanditsSpawn][SERVER_REJECT] reason=invalid-args")
        return
    end

    local batch = tostring(args.lccBatch or "unknown")
    local index = tonumber(args.lccIndex) or 0
    local total = tonumber(args.lccTotal) or 0

    if not hasStaffAccess(player) then
        print(string.format(
            "[LCC][BanditsSpawn][SERVER_REJECT] batch=%s index=%d/%d reason=staff-access-required",
            batch, index, total
        ))
        return
    end

    local cid = args.cid
    local x = tonumber(args.x)
    local y = tonumber(args.y)
    local z = tonumber(args.z)
    if not cid or not x or not y or not z then
        print(string.format(
            "[LCC][BanditsSpawn][SERVER_REJECT] batch=%s index=%d/%d reason=missing-spawn-data",
            batch, index, total
        ))
        return
    end

    local before, beforeCount = collectBanditIdsNear(x, y, z, VERIFY_RADIUS)

    print(string.format(
        "[LCC][BanditsSpawn][SERVER_BEGIN] batch=%s index=%d/%d cid=%s at=%d,%d,%d nearbyBefore=%d",
        batch, index, total, tostring(cid), x, y, z, beforeCount
    ))

    local spawnArgs = {
        cid = cid,
        x = x,
        y = y,
        z = z,
        program = "Bandit",
        size = 1,
    }

    local ok, err = pcall(BanditServer.Spawner.Clan, player, spawnArgs)
    if not ok then
        print(string.format(
            "[LCC][BanditsSpawn][SERVER_ERROR] batch=%s index=%d/%d error=%s",
            batch, index, total, tostring(err)
        ))
        return
    end

    -- Spawner.Clan already transmits the created brain's cluster. Keep the same
    -- whole-GMD refresh performed by Bandits' normal Spawner client-command path.
    if type(TransmitBanditModData) == "function" then
        local txOk, txErr = pcall(TransmitBanditModData)
        if not txOk then
            print(string.format(
                "[LCC][BanditsSpawn][SERVER_WARN] batch=%s index=%d/%d TransmitBanditModData=%s",
                batch, index, total, tostring(txErr)
            ))
        end
    end

    local after, afterCount = collectBanditIdsNear(x, y, z, VERIFY_RADIUS)
    local created = diffIds(before, after)

    print(string.format(
        "[LCC][BanditsSpawn][SERVER_RESULT] batch=%s index=%d/%d nearbyBefore=%d nearbyAfter=%d created=%d ids=%s",
        batch,
        index,
        total,
        beforeCount,
        afterCount,
        #created,
        #created > 0 and table.concat(created, ",") or "none"
    ))
end

local function onClientCommand(module, command, player, args)
    if module ~= MODULE or command ~= COMMAND then return end
    handleSpawnOne(player, args)
end

Guard.install {
    id = FEATURE,
    validate = function()
        if type(BanditServer) ~= "table"
                or type(BanditServer.Spawner) ~= "table"
                or type(BanditServer.Spawner.Clan) ~= "function" then
            return false, "BanditServer.Spawner.Clan is unavailable"
        end
        if type(GetBanditClusterData) ~= "function" then
            return false, "GetBanditClusterData is unavailable"
        end
        if not Events or not Events.OnClientCommand then
            return false, "OnClientCommand event is unavailable"
        end
        return true
    end,
    install = function()
        Events.OnClientCommand.Add(function(module, command, player, args)
            if Guard.isEnabled(FEATURE) then
                Guard.protect(FEATURE, "test spawn request", onClientCommand, module, command, player, args)
            end
        end)
        print("[LCC][BanditsSpawn][SERVER_INIT] diagnostic test-spawn bridge active")
    end,
}

if isServer() then
    return
end

local LOG_PREFIX = "[EFZ][QuestDeathDropSync] "
local SYNC_MODULE = "EFZ"
local SYNC_COMMAND = "QuestDeathDropSync"

local function toInt(value)
    local n = tonumber(value)
    if n == nil then
        return nil
    end
    return math.floor(n)
end

local function removePlayerCorpseAt(x, y, z)
    local square = getSquare(x, y, z)
    if not square then
        return
    end

    local objects = square:getStaticMovingObjects()
    if not objects then
        return
    end

    for i = objects:size() - 1, 0, -1 do
        local obj = objects:get(i)
        if obj and instanceof(obj, "IsoDeadBody") and obj.isPlayer and obj:isPlayer() then
            if square.removeCorpse then
                square:removeCorpse(obj, false)
            else
                obj:removeFromWorld()
                obj:removeFromSquare()
            end
        end
    end
end

local function removeReanimatedPlayerZombieAt(x, y, z)
    local cell = getCell()
    if not cell then
        return
    end

    local zombies = cell:getZombieList()
    if not zombies then
        return
    end

    for i = zombies:size() - 1, 0, -1 do
        local zombie = zombies:get(i)
        if zombie and zombie:isReanimatedPlayer() then
            local square = zombie:getSquare()
            local zx = square and square:getX() or toInt(zombie:getX())
            local zy = square and square:getY() or toInt(zombie:getY())
            local zz = square and square:getZ() or toInt(zombie:getZ())
            if zx == x and zy == y and zz == z then
                zombie:removeFromWorld()
                zombie:removeFromSquare()
            end
        end
    end
end

local function onServerCommand(module, command, args)
    if module ~= SYNC_MODULE or command ~= SYNC_COMMAND or not args then
        return
    end

    local action = args.action and tostring(args.action) or nil
    local x = toInt(args.x)
    local y = toInt(args.y)
    local z = toInt(args.z)
    if not action or x == nil or y == nil or z == nil then
        print(LOG_PREFIX .. "Ignored invalid sync payload.")
        return
    end

    if action == "RemovePlayerCorpse" then
        removePlayerCorpseAt(x, y, z)
        return
    end

    if action == "RemoveReanimatedPlayerZombie" then
        removeReanimatedPlayerZombieAt(x, y, z)
        return
    end
end

if Events and Events.OnServerCommand then
    Events.OnServerCommand.Add(onServerCommand)
end


require "zombies/NMServerZombieScanHelpers"

NMServerMPZombieScanPolicy = NMServerMPZombieScanPolicy or {}

function NMServerMPZombieScanPolicy.shouldRunNaturalIntakeScan(ticks, interval)
    local scanInterval = tonumber(interval) or 0
    local nowTicks = tonumber(ticks) or 0
    return scanInterval > 0 and (nowTicks % scanInterval) == 0
end

function NMServerMPZombieScanPolicy.shouldRunFallbackScan(ticks, tickInterval)
    local interval = tonumber(tickInterval) or 0
    local nowTicks = tonumber(ticks) or 0
    return interval > 0 and (nowTicks % interval) == 0
end

function NMServerMPZombieScanPolicy.scanAroundCharacter(character, callback, radius, maxZombies)
    local square = character and character.getCurrentSquare and character:getCurrentSquare() or nil
    if not square then
        return 0
    end
    local seen = {}
    local processed = 0
    local scanRadius = tonumber(radius) or 0
    local scanMax = tonumber(maxZombies) or 0
    for x = square:getX() - scanRadius, square:getX() + scanRadius do
        for y = square:getY() - scanRadius, square:getY() + scanRadius do
            if processed >= scanMax then
                return processed
            end
            local gridSquare = getCell() and getCell():getGridSquare(x, y, square:getZ()) or nil
            if gridSquare then
                local moving = gridSquare:getMovingObjects()
                for i = 0, moving:size() - 1 do
                    local zombie = moving:get(i)
                    local id = zombie and zombie.getObjectID and tostring(zombie:getObjectID() or "") or tostring(zombie)
                    if not seen[id] and NMServerZombieScanHelpers.isAliveZombie(zombie) then
                        seen[id] = true
                        if callback then
                            callback(zombie)
                        end
                        processed = processed + 1
                        if processed >= scanMax then
                            return processed
                        end
                    end
                end
            end
        end
    end
    return processed
end

local function visitPlayers(players, visitor)
    if players and players.size then
        for i = 0, players:size() - 1 do
            local player = players:get(i)
            if player then
                visitor(player)
            end
        end
        return true
    end
    return false
end

function NMServerMPZombieScanPolicy.observeNaturalCandidates(players, fallbackPlayer, enqueueFn, diag, options)
    local scanned = 0
    local scanSource = options and options.source or "first_seen_scan"
    local radius = options and options.radius or 0
    local maxZombies = options and options.maxZombies or 0
    local callback = function(zombie)
        if enqueueFn then
            enqueueFn(zombie, scanSource)
        end
    end

    local visitedPlayers = visitPlayers(players, function(player)
        scanned = scanned + NMServerMPZombieScanPolicy.scanAroundCharacter(player, callback, radius, maxZombies)
    end)

    if not visitedPlayers and fallbackPlayer then
        scanned = scanned + NMServerMPZombieScanPolicy.scanAroundCharacter(fallbackPlayer, callback, radius, maxZombies)
    end

    if diag then
        diag.queueScanned = (diag.queueScanned or 0) + scanned
    end
    return scanned
end

function NMServerMPZombieScanPolicy.runFallback(players, fallbackPlayer, applyFn, options)
    local applied = 0
    local source = options and options.source or "fallback_scan"
    local radius = options and options.radius or 0
    local maxZombies = options and options.maxZombies or 0
    local callback = function(zombie)
        if applyFn and applyFn(zombie, source) == true then
            applied = applied + 1
        end
    end

    local visitedPlayers = visitPlayers(players, function(player)
        NMServerMPZombieScanPolicy.scanAroundCharacter(player, callback, radius, maxZombies)
    end)

    if not visitedPlayers and fallbackPlayer then
        NMServerMPZombieScanPolicy.scanAroundCharacter(fallbackPlayer, callback, radius, maxZombies)
    end

    return applied
end

return NMServerMPZombieScanPolicy

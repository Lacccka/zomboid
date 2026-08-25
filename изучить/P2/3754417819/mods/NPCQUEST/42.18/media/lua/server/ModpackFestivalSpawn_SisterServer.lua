-- Server-side Alyssa spawn/upkeep using Bandits2 Companion.

if isClient() and not isServer() then
    return
end

ModpackFestivalSisterServer = ModpackFestivalSisterServer or {}

local MOD_ID = "ModpackFestivalSpawn"
local LOG_PREFIX = "[" .. MOD_ID .. "][SisterServer] "

local SPAWN_RETRY_TICKS = 60
local UPKEEP_TICKS = 15
local SEARCH_RADIUS = 26

local tickCount = 0

local function featureEnabled()
    if ModpackFestivalFeatures and ModpackFestivalFeatures.isSisterSpawnEnabled then
        return ModpackFestivalFeatures.isSisterSpawnEnabled() == true
    end
    return false
end

local function distSqXY(ax, ay, bx, by)
    local dx = ax - bx
    local dy = ay - by
    return dx * dx + dy * dy
end

local function getPlayer()
    return getSpecificPlayer and getSpecificPlayer(0)
end

local function getState()
    local md = ModData.getOrCreate(MOD_ID)
    md.sisterRuntime = md.sisterRuntime or {}
    return md.sisterRuntime
end

local function storeInventorySnapshot(entries)
    if ModpackFestivalSister and ModpackFestivalSister.storeInventorySnapshot and entries then
        ModpackFestivalSister.storeInventorySnapshot(entries)
        return true
    end
    return false
end

local function captureLiveInventory(sister)
    if not sister or not ModpackFestivalSister or not ModpackFestivalSister.captureInventoryFromBandit then
        return false
    end
    local ledger = ModpackFestivalSister.captureInventoryFromBandit(sister)
    if ledger then
        local count = ledger.entries and #ledger.entries or 0
        print(LOG_PREFIX .. "snapshot saved: " .. count .. " entry types, sig=" .. tostring(ledger.signature))
    else
        -- captureInventoryFromBandit returns nil for empty inventory or non-sister — log why
        local isSister = ModpackFestivalSister.isSisterBandit and ModpackFestivalSister.isSisterBandit(sister)
        local entries = ModpackFestivalSister.serializeBanditInventory and ModpackFestivalSister.serializeBanditInventory(sister) or {}
        print(LOG_PREFIX .. "snapshot skipped: isSister=" .. tostring(isSister) .. " entries=" .. tostring(#entries))
    end
    return ledger ~= nil
end

local function removeLiveSister(sister)
    if not sister then
        return false
    end
    captureLiveInventory(sister)
    pcall(function()
        local vehicle = sister.getVehicle and sister:getVehicle()
        if vehicle and vehicle.exit then
            vehicle:exit(sister)
        end
    end)
    pcall(function()
        if BanditBrain and BanditBrain.Remove then
            BanditBrain.Remove(sister)
        end
    end)
    pcall(function()
        if sister.removeFromSquare then
            sister:removeFromSquare()
        end
    end)
    pcall(function()
        if sister.removeFromWorld then
            sister:removeFromWorld()
        end
    end)
    return true
end

local function findAllSistersInCell(player)
    local cell = player and player.getCell and player:getCell()
    if not cell or not cell.getZombieList then return {} end
    local zombies = cell:getZombieList()
    if not zombies or not zombies.size then return {} end
    local found = {}
    for i = 0, zombies:size() - 1 do
        local z = zombies:get(i)
        if ModpackFestivalSister.isSisterBandit(z) and not (z.isDead and z:isDead()) then
            found[#found + 1] = z
        end
    end
    return found
end

local function removeAllLiveSisters(player)
    local all = findAllSistersInCell(player)
    -- also check global fallback for any out-of-cell body
    local global = ModpackFestivalSister.findSisterBandit and ModpackFestivalSister.findSisterBandit()
    if global then
        local already = false
        for _, v in ipairs(all) do
            if v == global then already = true break end
        end
        if not already then all[#all + 1] = global end
    end
    for _, s in ipairs(all) do
        removeLiveSister(s)
    end
    if #all > 1 then
        print(LOG_PREFIX .. "removed " .. #all .. " duplicate Alyssa bodies")
    end
    return #all
end

local function restorePendingInventory(sister, clearFirst)
    local st = getState()
    if not st.pendingInventoryRestore then
        return false
    end
    if not sister or not ModpackFestivalSister then
        return false
    end
    local hasSnapshot = ModpackFestivalSister.hasInventorySnapshot
        and ModpackFestivalSister.hasInventorySnapshot()
    local snapshot = ModpackFestivalSister.getInventorySnapshot and ModpackFestivalSister.getInventorySnapshot()
    print(LOG_PREFIX .. "restorePendingInventory: hasSnapshot=" .. tostring(hasSnapshot)
        .. " entries=" .. tostring(snapshot and #snapshot or 0))
    -- fall back to default spawn items on very first spawn (no prior snapshot)
    local entries = nil
    if not hasSnapshot then
        entries = ModpackFestivalSister.DEFAULT_SPAWN_ITEMS or {}
    end
    if ModpackFestivalSister.applyInventorySnapshotToBandit
        and ModpackFestivalSister.applyInventorySnapshotToBandit(sister, entries, clearFirst) then
        st.pendingInventoryRestore = false
        st.inventoryRestoredAtTick = tickCount
        print(LOG_PREFIX .. (hasSnapshot and "restored Alyssa inventory ledger" or "applied Alyssa default spawn items"))
        return true
    end
    print(LOG_PREFIX .. "restorePendingInventory: applyInventorySnapshotToBandit returned false")
    return false
end

local function isValidSpawnSquare(sq)
    if not sq then
        return false
    end
    if sq.getZombie and sq:getZombie() then
        return false
    end
    if sq.getVehicleContainer and sq:getVehicleContainer() then
        return false
    end
    if sq.isVehicleIntersecting and sq:isVehicleIntersecting() then
        return false
    end
    if sq.isFree and sq:isFree(false) then
        return true
    end
    return sq.isOutside and sq:isOutside() and not (sq.isSolid and sq:isSolid())
end

local function findSpawnSquareNear(cell, x, y, z, maxRadius)
    local baseX = math.floor(x + 0.5)
    local baseY = math.floor(y + 0.5)
    local baseZ = math.floor((z or 0) + 0.5)
    local sq = cell and cell:getGridSquare(baseX, baseY, baseZ)
    if isValidSpawnSquare(sq) then
        return sq
    end
    for radius = 1, maxRadius or 8 do
        for dx = -radius, radius do
            for dy = -radius, radius do
                sq = cell:getGridSquare(baseX + dx, baseY + dy, baseZ)
                if isValidSpawnSquare(sq) then
                    return sq
                end
            end
        end
    end
    return nil
end

local function findSisterInCell(player, centerX, centerY, radius)
    local cell = player and player.getCell and player:getCell()
    if not cell or not cell.getZombieList then
        return nil
    end
    local zombies = cell:getZombieList()
    if not zombies or not zombies.size then
        return nil
    end
    local radiusSq = (radius or 200) * (radius or 200)
    for i = 0, zombies:size() - 1 do
        local zombie = zombies:get(i)
        if ModpackFestivalSister.isSisterBandit(zombie)
            and not (zombie.isDead and zombie:isDead())
            and (not centerX or distSqXY(zombie:getX(), zombie:getY(), centerX, centerY) <= radiusSq) then
            return zombie
        end
    end
    return nil
end

local function findSister(player)
    return findSisterInCell(player, nil, nil, 9999)
        or (ModpackFestivalSister.findSisterBandit and ModpackFestivalSister.findSisterBandit())
end

local function sisterUnlockedForCallOver(player)
    if findSister(player) then
        return true
    end
    if not ModpackFestivalQuests then
        return false
    end
    if ModpackFestivalQuests.isCompleted
        and (ModpackFestivalQuests.isCompleted("meet_sister")
            or ModpackFestivalQuests.isCompleted("get_home")) then
        return true
    end
    local activeId = ModpackFestivalQuests.getActiveQuestId and ModpackFestivalQuests.getActiveQuestId()
    return activeId == "get_home"
end

local function banditsReady()
    if not BanditServer or not BanditServer.Spawner or not BanditServer.Spawner.Clan then
        return false
    end
    if BanditCustom and BanditCustom.Load then
        pcall(BanditCustom.Load)
    end
    return true
end

local function spawnSisterAt(player, x, y, z)
    if not player or not banditsReady() then
        return false
    end
    local cell = player:getCell()
    local sq = findSpawnSquareNear(cell, x, y, z, 10)
    if not sq then
        return false
    end

    local args = {
        cid = ModpackFestivalSister.CLAN_ID,
        program = ModpackFestivalSister.PROGRAM_NAME or "ModpackCompanion",
        size = 1,
        hostileP = false,
        hostile = false,
        loyal = true,
        permanent = true,
        fullname = ModpackFestivalSister.getSisterForename(),
        spawnPoints = {
            { x = sq:getX(), y = sq:getY(), z = sq:getZ() or 0 },
        },
    }
    BanditServer.Spawner.Clan(player, args)

    local st = getState()
    st.spawnRequested = true
    st.spawnX = sq:getX()
    st.spawnY = sq:getY()
    st.spawnZ = sq:getZ() or 0
    st.spawnedAtTick = tickCount
    st.pendingInventoryRestore = true  -- always restore; falls back to DEFAULT_SPAWN_ITEMS if no snapshot
    print(LOG_PREFIX .. "spawn requested at "
        .. tostring(st.spawnX) .. "," .. tostring(st.spawnY) .. "," .. tostring(st.spawnZ))
    return true
end

local function finalizeSpawnedSister(player)
    local st = getState()
    if not st.spawnRequested then
        return false
    end
    local sister = findSisterInCell(player, st.spawnX, st.spawnY, SEARCH_RADIUS)
    if not sister then
        return false
    end
    ModpackFestivalSister.enableFollowMode(sister, player)
    restorePendingInventory(sister, true)
    st.spawnRequested = false
    st.spawnDone = true
    st.lastSeenTick = tickCount
    print(LOG_PREFIX .. "Alyssa finalized as Companion")
    return true
end

local function playerNearMeetPoint(player)
    if not player then
        return false
    end
    local radius = (ModpackFestivalQuests and ModpackFestivalQuests.SISTER_MEET_SPAWN_RADIUS) or 72
    return distSqXY(
        player:getX(), player:getY(),
        ModpackFestivalSister.SPAWN_X, ModpackFestivalSister.SPAWN_Y
    ) <= radius * radius
end

local function shouldSpawnForQuest(player)
    if not ModpackFestivalQuests then
        return false
    end
    local activeId = ModpackFestivalQuests.getActiveQuestId and ModpackFestivalQuests.getActiveQuestId()
    if activeId ~= "meet_sister" and activeId ~= "get_home" then
        return false
    end
    return playerNearMeetPoint(player)
end

local function callOver(player)
    if not player then
        return false
    end
    if not sisterUnlockedForCallOver(player) then
        return false
    end
    local sister = findSister(player)
    local px = player:getX()
    local py = player:getY()
    local pz = player:getZ() or 0
    local sq = findSpawnSquareNear(player:getCell(), px + 2, py + 2, pz, 8)
    if not sq then
        sq = player:getSquare()
    end
    if sister and sq then
        captureLiveInventory(sister)
        removeAllLiveSisters(player)
        local st = getState()
        st.spawnDone = false
        st.spawnRequested = false
        st.pendingInventoryRestore = ModpackFestivalSister.hasInventorySnapshot
            and ModpackFestivalSister.hasInventorySnapshot()
        print(LOG_PREFIX .. "call-over removed existing Alyssa for fresh respawn")
    end
    return spawnSisterAt(player, sq and sq:getX() or (px + 2), sq and sq:getY() or (py + 2), sq and sq:getZ() or pz)
end

local function upkeep(player)
    local all = findAllSistersInCell(player)
    if #all == 0 then
        return false
    end
    -- purge duplicates, keep first
    if #all > 1 then
        print(LOG_PREFIX .. "duplicate Alyssa detected (" .. #all .. "), removing extras")
        for i = 2, #all do
            removeLiveSister(all[i])
        end
    end
    local sister = all[1]
    ModpackFestivalSister.enableFollowMode(sister, player)
    ModpackFestivalSister.protectSister(sister)
    restorePendingInventory(sister, true)
    -- authoritative server-side snapshot: saves directly to server ModData (persists to disk)
    captureLiveInventory(sister)
    getState().lastSeenTick = tickCount
    return true
end

local function onTick()
    if not featureEnabled() then
        return
    end
    tickCount = tickCount + 1
    local player = getPlayer()
    if not player or not player.getSquare or not player:getSquare() then
        return
    end

    local st = getState()
    if st.sisterInVehicle then
        if player.getVehicle and player:getVehicle() then
            return
        end
        st.sisterInVehicle = false
        if not sisterUnlockedForCallOver(player) then
            st.spawnDone = false
            st.spawnRequested = false
            st.pendingInventoryRestore = false
            return
        end
        st.spawnDone = false
        st.spawnRequested = false
        st.pendingInventoryRestore = ModpackFestivalSister.hasInventorySnapshot
            and ModpackFestivalSister.hasInventorySnapshot()
        callOver(player)
        return
    end

    if ModpackFestivalTick.every(tickCount, UPKEEP_TICKS) then
        upkeep(player)
    end
    if finalizeSpawnedSister(player) then
        return
    end
    if not ModpackFestivalTick.every(tickCount, SPAWN_RETRY_TICKS) then
        return
    end

    st = getState()
    if st.spawnDone and findSister(player) then
        return
    end
    if shouldSpawnForQuest(player) and not findSister(player) then
        spawnSisterAt(
            player,
            ModpackFestivalSister.SPAWN_X,
            ModpackFestivalSister.SPAWN_Y,
            ModpackFestivalSister.SPAWN_Z
        )
    end
end

local function onClientCommand(module, command, player, args)
    if module ~= MOD_ID or not featureEnabled() then
        return
    end
    if command == "SisterCallOver" then
        if args and args.inventory then
            storeInventorySnapshot(args.inventory)
        end
        callOver(player)
    elseif command == "SisterSpawn" then
        if args and args.inventory then
            storeInventorySnapshot(args.inventory)
        end
        spawnSisterAt(
            player,
            ModpackFestivalSister.SPAWN_X,
            ModpackFestivalSister.SPAWN_Y,
            ModpackFestivalSister.SPAWN_Z
        )
    elseif command == "SisterAppearance" then
        if args and args.appearance then
            ModpackFestivalSister.storeSisterAppearance(args.appearance, args.buildString)
            local sister = findSister(player)
            if sister then
                ModpackFestivalSister.enableFollowMode(sister, player)
            end
        end
    elseif command == "SisterDied" then
        local st = getState()
        -- ignore duplicate death signals while a respawn is already in flight
        if st.spawnRequested and (tickCount - (st.lastDeathRecoveryTick or 0)) < 180 then
            return
        end
        st.spawnDone = false
        st.spawnRequested = false
        st.pendingInventoryRestore = true
        if args and args.inventory then
            storeInventorySnapshot(args.inventory)
        end
        st.lastDeathRecoveryTick = tickCount
        callOver(player)
    elseif command == "SisterInventorySnapshot" then
        if args and args.inventory then
            storeInventorySnapshot(args.inventory)
        end
    elseif command == "SisterVehicleSnapshot" then
        if not sisterUnlockedForCallOver(player) then
            return
        end
        local st = getState()
        st.sisterInVehicle = true
        if args and args.inventory then
            storeInventorySnapshot(args.inventory)
        end
    elseif command == "SisterVehicleDespawn" then
        if not sisterUnlockedForCallOver(player) then
            return
        end
        local st = getState()
        st.sisterInVehicle = true
        st.spawnDone = false
        st.spawnRequested = false
        st.pendingInventoryRestore = true
        if args and args.inventory then
            storeInventorySnapshot(args.inventory)
        end
        removeAllLiveSisters(player)
        print(LOG_PREFIX .. "Alyssa hidden for vehicle travel")
    elseif command == "SisterVehicleExit" then
        local st = getState()
        if not st.sisterInVehicle and not sisterUnlockedForCallOver(player) then
            st.spawnDone = false
            st.spawnRequested = false
            st.pendingInventoryRestore = false
            return
        end
        st.sisterInVehicle = false
        st.spawnDone = false
        st.spawnRequested = false
        st.pendingInventoryRestore = ModpackFestivalSister.hasInventorySnapshot
            and ModpackFestivalSister.hasInventorySnapshot()
        if args and args.inventory then
            storeInventorySnapshot(args.inventory)
            st.pendingInventoryRestore = true
        end
        callOver(player)
    end
end

-- OnGameStart fires only for brand-new worlds (not on load).
-- ModData in memory can carry over from a previous session if PZ wasn't restarted,
-- so explicitly wipe the inventory ledger and runtime state to prevent old inventory
-- bleeding into the new game.
local function onGameStart()
    pcall(function()
        local md = ModData.getOrCreate(MOD_ID)
        if md.sister then
            md.sister.sisterInventoryLedger = nil
            md.sister.sisterAppearanceData = nil
        end
        if md.sisterRuntime then
            md.sisterRuntime.spawnDone = false
            md.sisterRuntime.pendingInventoryRestore = false
        end
        -- also wipe legacy flat keys if present
        md.sisterInventoryLedger = nil
        md.sisterLedgerEncoded = nil
        print(LOG_PREFIX .. "new game: cleared sister inventory ledger")
    end)
end

local function onSave()
    if not featureEnabled() then return end
    local player = getPlayer()
    if not player then return end
    local sister = findSister(player)
    if not sister then return end
    local ledger = captureLiveInventory(sister)
    if ledger then
        print(LOG_PREFIX .. "OnSave: snapshot captured")
    end
end

Events.OnTick.Add(onTick)
Events.OnClientCommand.Add(onClientCommand)
Events.OnGameStart.Add(onGameStart)
Events.OnSave.Add(onSave)
print(LOG_PREFIX .. "loaded")

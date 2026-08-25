ExtractionMode = ExtractionMode or {}

local Integration = {}
local COMPANION_SCALING_WEIGHT = 0.5

local function singleplayer()
    return not (isClient and isClient()) and not (isServer and isServer())
end

local function safeCall(callback, fallback)
    local ok, value = pcall(callback)
    if ok then return value end
    return fallback
end

local function living(character)
    return character ~= nil and safeCall(function() return not character:isDead() end, false)
end

-- Project Remnants injects these functions after its Java agent and Lua module
-- have initialized. Look them up dynamically so load order does not matter and
-- so this file is a complete no-op when the optional mod is absent or disabled.
function Integration.isAvailable()
    if not singleplayer() or NPCFW == nil or NPCFW.ready ~= true then return false end
    if npcfwGetPartyMemberIds == nil or npcfwGetNPC == nil then return false end
    return npcfwIsReady == nil or safeCall(function() return npcfwIsReady() == true end, false)
end

local function originalPlayer()
    if not Integration.isAvailable() or npcfwIsPossessing == nil
        or npcfwGetOriginalPlayer == nil then return nil end
    if not safeCall(function() return npcfwIsPossessing() == true end, false) then return nil end
    return safeCall(function() return npcfwGetOriginalPlayer() end, nil)
end

-- Possession swaps Project Zomboid's active player slot to an NPC body. Keep
-- Extraction Mode's participant identity tied to the original survivor.
function Integration.canonicalPlayer(player)
    if not Integration.isAvailable() or player == nil then return player end
    local original = originalPlayer()
    if original == nil then return player end

    local controlled = safeCall(function()
        return npcfwIsControlledCharacter and npcfwIsControlledCharacter(player) == true
    end, false)
    local active = safeCall(function() return getPlayer and getPlayer() end, nil)
    if controlled or player == active then return original end
    return player
end

local function rosterEntries()
    local result = {}
    if not Integration.isAvailable() then return result end

    local roster = safeCall(function() return npcfwGetPartyMemberIds() end, nil)
    if roster == nil then return result end
    local seen = {}
    for index = 1, 128 do
        local npcId = roster[index]
        if npcId == nil then break end
        npcId = tostring(npcId)
        if npcId ~= "" and not seen[npcId] then
            seen[npcId] = true
            local npc = safeCall(function() return npcfwGetNPC(npcId) end, nil)
            if living(npc) then
                result[#result + 1] = { id = npcId, character = npc, original = false }
            end
        end
    end
    return result
end

function Integration.activeCompanionCount()
    return #rosterEntries()
end

function Integration.scalingContribution()
    if not Integration.isAvailable() then return 0 end
    return Integration.activeCompanionCount() * COMPANION_SCALING_WEIGHT
end

-- Project Remnants transfers control to another living squad body when the
-- controlled character dies. During that handoff getPlayer() may still return
-- the corpse for a tick, so Extraction Mode must consider the complete active
-- squad before removing the singleplayer raid participant.
function Integration.hasLivingSuccessor(deadCharacter)
    if not Integration.isAvailable() then return false end

    local seen = {}
    local function isOtherLiving(character)
        if character == nil or character == deadCharacter or seen[character] then return false end
        seen[character] = true
        return living(character)
    end

    local active = safeCall(function() return getPlayer and getPlayer() end, nil)
    if isOtherLiving(active) then return true end

    -- While possessing an NPC, the original player body is not part of the NPC
    -- roster but is still a valid body for Remnants to return control to.
    if isOtherLiving(originalPlayer()) then return true end

    for _, entry in ipairs(rosterEntries()) do
        if isOtherLiving(entry.character) then return true end
    end
    return false
end

local function activePartyMember(character)
    if character == nil or not Integration.isAvailable() then return false end
    if npcfwIsActivePartyMember then
        return safeCall(function() return npcfwIsActivePartyMember(character) == true end, false)
    end
    for _, entry in ipairs(rosterEntries()) do
        if entry.character == character then return true end
    end
    return false
end

local function managedRemnantsNPC(character)
    if character == nil or not Integration.isAvailable() then return false end
    if npcfwIsManagedRuntimeNPC then
        return safeCall(function() return npcfwIsManagedRuntimeNPC(character) == true end, false)
    end
    if npcfwIsNPC then
        return safeCall(function() return npcfwIsNPC(character) == true end, false)
    end
    return false
end

-- Active squad kills belong to the single human player's quest group. Hostile
-- or unrecruited Remnants NPC kills must not create a fake player quest owner.
function Integration.questCreditPlayer(killer)
    if not Integration.isAvailable() then return killer end
    local original = originalPlayer()
    if killer == original then return original end
    if activePartyMember(killer) then
        return original or Integration.canonicalPlayer(safeCall(function()
            return getPlayer and getPlayer()
        end, nil))
    end
    if managedRemnantsNPC(killer) then return nil end
    return Integration.canonicalPlayer(killer)
end

-- Capture bodies before the controlled player moves far enough to unload the
-- old cell. The original body is separate from the NPC roster during possession.
function Integration.captureActiveSquad()
    local result = rosterEntries()
    if not Integration.isAvailable() then return result end
    local original = originalPlayer()
    if living(original) then
        result[#result + 1] = { id = nil, character = original, original = true }
    end
    return result
end

local RELOCATION_OFFSETS = {
    { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 },
    { 1, 1 }, { -1, 1 }, { 1, -1 }, { -1, -1 },
    { 2, 0 }, { -2, 0 }, { 0, 2 }, { 0, -2 },
    { 2, 1 }, { -2, 1 }, { 2, -1 }, { -2, -1 },
    { 1, 2 }, { -1, 2 }, { 1, -2 }, { -1, -2 },
}

local function usableSquare(square, requireOutdoor)
    if square == nil then return false end
    local usable = safeCall(function()
        return square:getFloor() ~= nil and square:TreatAsSolidFloor()
            and not square:isSolid() and not square:isSolidTrans()
    end, false)
    if not usable then return false end
    if requireOutdoor then
        return safeCall(function()
            return square:isOutside() and not square:has(IsoFlagType.water)
        end, false)
    end
    return true
end

local function relocationPoint(x, y, z, index, requireOutdoor)
    local cell = getCell and getCell()
    if cell == nil then return x, y, z end
    local baseX = math.floor(tonumber(x) or 0)
    local baseY = math.floor(tonumber(y) or 0)
    local baseZ = math.floor(tonumber(z) or 0)
    for attempt = 0, #RELOCATION_OFFSETS - 1 do
        local offset = RELOCATION_OFFSETS[((index + attempt - 1) % #RELOCATION_OFFSETS) + 1]
        local square = cell:getGridSquare(baseX + offset[1], baseY + offset[2], baseZ)
        if usableSquare(square, requireOutdoor) then
            return square:getX() + 0.5, square:getY() + 0.5, square:getZ()
        end
    end
    return x, y, z
end

local function leaveVehicle(character)
    local vehicle = safeCall(function() return character:getVehicle() end, nil)
    if vehicle == nil then return end
    pcall(function() vehicle:exit(character) end)
    if npcfwFinishVehicleExitAction then
        pcall(function() npcfwFinishVehicleExitAction(character) end)
    end
end

function Integration.relocateCapturedSquad(captured, leader, x, y, z, requireOutdoor)
    if not Integration.isAvailable() or leader == nil then return 0 end
    local moved = 0
    local seen = { [leader] = true }
    for _, entry in ipairs(captured or {}) do
        local character = entry.character
        if living(character) and not seen[character] then
            seen[character] = true
            if entry.id and npcfwClearTasks then
                pcall(function() npcfwClearTasks(entry.id) end)
            end
            leaveVehicle(character)
            local targetX, targetY, targetZ = relocationPoint(
                x, y, z, moved + 1, requireOutdoor == true)
            local ok = pcall(function()
                character:teleportTo(targetX, targetY, targetZ)
            end)
            if ok then moved = moved + 1 end
        end
    end

    -- Re-establish normal Remnants party following after the cross-cell move.
    for _, entry in ipairs(captured or {}) do
        local character = entry.character
        if living(character) and character ~= leader then
            if entry.original and npcfwOrderOriginalFollow then
                pcall(function() npcfwOrderOriginalFollow(leader) end)
            elseif not entry.original and npcfwOrderFollow then
                pcall(function() npcfwOrderFollow(character, leader) end)
            end
        end
    end
    if npcfwSyncPartyLinksToActiveLeader then
        pcall(function() npcfwSyncPartyLinksToActiveLeader() end)
    end
    return moved
end

local HIDEOUT_SPAWN_OFFSETS = {
    { -4, 0 }, { -2, 0 }, { 2, 0 }, { 4, 0 }, { 0, 0 },
}

local function insideCell(character, minimumX, minimumY, maximumX, maximumY)
    if character == nil then return false end
    local x = safeCall(function() return character:getX() end, nil)
    local y = safeCall(function() return character:getY() end, nil)
    return x ~= nil and y ~= nil and x >= minimumX and x < maximumX
        and y >= minimumY and y < maximumY
end

local function validHideoutSquare(square, building, requireFree)
    if square == nil or building == nil then return false end
    local valid = safeCall(function()
        return square:getBuilding() == building and square:getRoom() ~= nil
            and square:getFloor() ~= nil and square:TreatAsSolidFloor()
            and not square:isSolid() and not square:isSolidTrans()
    end, false)
    if not valid or not requireFree then return valid end
    return safeCall(function() return square:isFree(false) end, false)
end

local function safeHideoutSquare(cell, hideout, building, claimed)
    local anchorX = math.floor(tonumber(hideout.x) or 0)
    local anchorY = math.floor(tonumber(hideout.y) or 0)
    local anchorZ = math.floor(tonumber(hideout.z) or 0)
    for _, offset in ipairs(HIDEOUT_SPAWN_OFFSETS) do
        local square = cell:getGridSquare(anchorX + offset[1], anchorY + offset[2], anchorZ)
        if validHideoutSquare(square, building, true) and not claimed[square] then
            claimed[square] = true
            return square
        end
    end

    -- Map edits may move one of the authored spawn tiles. Fall back to the
    -- nearest free indoor tile in the same mapped building instead of placing a
    -- companion outdoors merely because a preferred square is unavailable.
    for radius = 1, math.max(4, math.floor(tonumber(hideout.radius) or 14)) do
        for x = anchorX - radius, anchorX + radius do
            for y = anchorY - radius, anchorY + radius do
                if math.abs(x - anchorX) == radius or math.abs(y - anchorY) == radius then
                    local square = cell:getGridSquare(x, y, anchorZ)
                    if validHideoutSquare(square, building, true) and not claimed[square] then
                        claimed[square] = true
                        return square
                    end
                end
            end
        end
    end
    return nil
end

-- Remnants' new-game party creation places companions at playerX + 1..3.
-- ExtractionMap has several valid player spawnpoints, so the rightmost one can
-- otherwise push a companion through or beyond the hideout wall. Repair only
-- active squad bodies that are already inside the off-map hideout cell but not
-- actually standing in its mapped indoor building. World NPCs and residents are
-- deliberately ignored.
function Integration.ensureActiveSquadSafeInHideout(hideout, leader)
    if not Integration.isAvailable() or hideout == nil or leader == nil then return 0 end
    local cell = getCell and getCell()
    if cell == nil then return 0 end

    local cellSize = 256
    local minimumX = math.floor((tonumber(hideout.x) or 0) / cellSize) * cellSize
    local minimumY = math.floor((tonumber(hideout.y) or 0) / cellSize) * cellSize
    local maximumX = minimumX + cellSize
    local maximumY = minimumY + cellSize
    if not insideCell(leader, minimumX, minimumY, maximumX, maximumY) then return 0 end

    local anchor = cell:getGridSquare(math.floor(tonumber(hideout.x) or 0),
        math.floor(tonumber(hideout.y) or 0), math.floor(tonumber(hideout.z) or 0))
    local building = anchor and anchor:getBuilding()
    if building == nil then return 0 end

    local claimed = {}
    local leaderSquare = safeCall(function() return leader:getSquare() end, nil)
    if leaderSquare then claimed[leaderSquare] = true end
    local invalid = {}
    for _, entry in ipairs(rosterEntries()) do
        local character = entry.character
        if character ~= leader
            and insideCell(character, minimumX, minimumY, maximumX, maximumY) then
            local square = safeCall(function() return character:getSquare() end, nil)
            if validHideoutSquare(square, building, false) then
                claimed[square] = true
            else
                invalid[#invalid + 1] = entry
            end
        end
    end

    local moved = 0
    for _, entry in ipairs(invalid) do
        local square = safeHideoutSquare(cell, hideout, building, claimed)
        if square then
            if entry.id and npcfwClearTasks then
                pcall(function() npcfwClearTasks(entry.id) end)
            end
            leaveVehicle(entry.character)
            local ok = pcall(function()
                entry.character:teleportTo(square:getX() + 0.5,
                    square:getY() + 0.5, square:getZ())
            end)
            if ok then
                moved = moved + 1
                if npcfwOrderFollow then
                    pcall(function() npcfwOrderFollow(entry.character, leader) end)
                end
            end
        end
    end
    if moved > 0 and npcfwSyncPartyLinksToActiveLeader then
        pcall(function() npcfwSyncPartyLinksToActiveLeader() end)
    end
    return moved
end

ExtractionMode.ProjectRemnantsIntegration = Integration
return Integration

-- Client-side Alyssa context menu, dialogue, and protection hooks.

if isServer() then
    return
end

local MOD_ID = "ModpackFestivalSpawn"
local tickCount = 0
local lastDialogueMs = 0
local lastFleeDialogueMs = 0
local lastShoutCallMs = 0
local SHOUT_CALL_COOLDOWN_MS = 1500
local CALL_OVER_COOLDOWN_MS = 30000
local lastCallOverMs = 0
local FLEE_DIALOGUE_COOLDOWN_MS = 12000
local FLEE_COMMIT_TICKS = 80       -- 4 seconds at ~20 ticks/sec
local FLEE_COOLDOWN_MS    = 15000  -- 15 seconds between flee episodes
local wasFleeing = false
local sisterWasFleeingForCooldown = false
local sisterFleeEndedMs = 0
-- Kill milestone tracking
local sisterKillCount          = 0
local sisterMilestonesHit      = {}
local sisterRecentHitSet       = {}   -- zombie tostring → timestamp; cleared on onZombieDead

-- Injury limp window
local BURST_HIT_WINDOW_MS      = 5000   -- hits within this window count toward burst
local BURST_HIT_THRESHOLD      = 3      -- hits needed to trigger limp
local LIMP_DURATION_MS         = 20000  -- how long limp lasts
local sisterHitBurstCount      = 0
local sisterHitBurstWindowMs   = 0
local sisterLimpUntilMs        = 0

local banditVisualDamagePatched = false
local banditEnemyFilterPatched = false
local banditMenuPatched = false
local banditFriendlyFirePatched = false
local banditFatalTaskPatched = false
local banditSayPatched = false
local banditDeathInventoryPatched = false
local DEFEND_PLAYER_RADIUS = 6
local DEFEND_PLAYER_RADIUS_SQ = DEFEND_PLAYER_RADIUS * DEFEND_PLAYER_RADIUS
local lastWindowAssistMs = 0
local WINDOW_ASSIST_COOLDOWN_MS = 700
local WINDOW_ASSIST_MAX_PLAYER_DIST = 28
local INVENTORY_SESSION_MS = 30000
local IDLE_AFTER_COMBAT_SUPPRESS_MS = 60000
local IDLE_UNLOCK_AFTER_FIND_SISTER_MS = 10 * 60 * 1000   -- 10 real-world minutes
-- Sister-hit emotional cost: stress spikes fast (50 hits to max), unhappiness lingers (150 hits)
local SISTER_HIT_STRESS_DELTA     = 1.0 / 100   -- 0.01 per hit (0–1 scale)
local SISTER_HIT_UNHAPPY_DELTA    = 100.0 / 300 -- ~0.333 per hit (0–100 scale)
local SISTER_HIT_EMOTIONAL_COOLDOWN_MS = 500
local lastSisterHitEmotionalMs = 0
local sisterInventorySessionUntilMs = 0
local cachedSister = nil               -- current live sister zombie; updated each onTick
local lastKnownSisterId = nil          -- detect fresh spawn (new zombie ID = new spawn)
local clientInventoryApplied = false   -- only attempt once per spawn
local clientAppearanceApplied = false  -- only attempt once per spawn
local lastInventorySnapshotSig = nil
local activeSisterInventory = nil
local activeSisterInventoryPlayerIndex = nil
local sisterInventoryOrigChar = nil  -- saved player character before we swap ISInventoryPage
local pendingSisterInventoryRestore = nil
local lastSisterCombatMs = -IDLE_AFTER_COMBAT_SUPPRESS_MS
local lastSisterRecoveryRequestMs = -30000
local sisterVehicleState = nil
local protectSister
local sendSisterInventorySnapshot

local function noop()
end

local function getPlayer()
    return getSpecificPlayer and getSpecificPlayer(0)
end

local function isIsoZombie(obj)
    if not obj or not instanceof then
        return false
    end
    local ok, result = pcall(function()
        return instanceof(obj, "IsoZombie")
    end)
    return ok and result == true
end

local function isSister(obj)
    if not isIsoZombie(obj) or not ModpackFestivalSister or not ModpackFestivalSister.isSisterBandit then
        return false
    end
    local ok, result = pcall(ModpackFestivalSister.isSisterBandit, obj)
    return ok and result == true
end

local function hasSisterTag(obj)
    if not obj or not obj.getModData or not ModpackFestivalSister or not ModpackFestivalSister.hasSisterModData then
        return false
    end
    local ok, result = pcall(ModpackFestivalSister.hasSisterModData, obj)
    return ok and result == true
end

local function isSisterOrTagged(obj)
    return hasSisterTag(obj) or isSister(obj)
end

local function isSisterBrain(brain)
    return ModpackFestivalSister and ModpackFestivalSister.isSisterBrain
        and ModpackFestivalSister.isSisterBrain(brain)
end

local function isBandit(obj)
    if not obj or not obj.getVariableBoolean then
        return false
    end
    local ok, result = pcall(function()
        return obj:getVariableBoolean("Bandit")
    end)
    return ok and result == true
end

local function isLocalPlayerAttacker(attacker)
    local player = getPlayer()
    if player ~= nil and attacker == player then
        return true
    end
    if not attacker or not instanceof then
        return false
    end
    local okPlayer, isPlayer = pcall(function()
        return instanceof(attacker, "IsoPlayer")
    end)
    if not okPlayer or not isPlayer then
        return false
    end
    if attacker.isNPC then
        local okNpc, isNpc = pcall(function()
            return attacker:isNPC()
        end)
        if okNpc and isNpc then
            return false
        end
    end
    return true
end

local function getBrain(obj)
    if not obj or not obj.getModData then
        return nil
    end
    if BanditBrain and BanditBrain.Get then
        local ok, brain = pcall(BanditBrain.Get, obj)
        if ok and brain then
            return brain
        end
    end
    local md = obj:getModData()
    return md and md.brain
end

local function markBanditHostileToPlayer(bandit)
    if not isBandit(bandit) or isSisterOrTagged(bandit) then
        return false
    end
    local brain = getBrain(bandit)
    if brain then
        brain.hostile = true
        brain.hostileP = true
        brain.loyal = false
        brain.modpackFestivalHostileToPlayer = true
        brain.modpackFestivalHostileToSister = true
        if BanditBrain and BanditBrain.Update then
            pcall(BanditBrain.Update, bandit, brain)
        end
    end
    local md = bandit.getModData and bandit:getModData() or nil
    if md then
        md.modpackFestivalHostileToPlayer = true
        md.modpackFestivalHostileToSister = true
        if bandit.transmitModData then
            pcall(function() bandit:transmitModData() end)
        end
    end
    if Bandit and Bandit.SetHostileP then
        pcall(Bandit.SetHostileP, bandit, true)
    end
    return true
end

local function isHostileToSisterBrain(brain)
    return brain and brain.modpackFestivalHostileToSister == true
end

local function distSqXY(ax, ay, bx, by)
    local dx = ax - bx
    local dy = ay - by
    return dx * dx + dy * dy
end

local function findSisterFromContext(worldobjects)
    if worldobjects then
        for _, obj in ipairs(worldobjects) do
            if isSister(obj) then
                return obj
            end
        end
    end

    local player = getPlayer()
    local sq = player and player.getCurrentSquare and player:getCurrentSquare()
    if not sq then
        return nil
    end
    local candidates = { sq, sq:getN(), sq:getS(), sq:getE(), sq:getW() }
    for i = 1, #candidates do
        local csq = candidates[i]
        local zombie = csq and csq.getZombie and csq:getZombie()
        if isSister(zombie) then
            return zombie
        end
    end
    return nil
end

local function getSisterDisplayName(sister)
    if sister and BanditBrain and BanditBrain.Get then
        local ok, brain = pcall(BanditBrain.Get, sister)
        if ok and brain and brain.fullname and brain.fullname ~= "" then
            return brain.fullname
        end
    end
    if ModpackFestivalSister and ModpackFestivalSister.getSisterForename then
        return ModpackFestivalSister.getSisterForename()
    end
    return "Alyssa"
end

local function requestCallOver(player)
    if not player or not sendClientCommand then
        return
    end
    -- find sister early so we can clear stand watch regardless of cooldown
    local sister = ModpackFestivalSister and ModpackFestivalSister.findSisterBandit
        and ModpackFestivalSister.findSisterBandit()
    if sister then
        local sw_brain = getBrain(sister)
        if sw_brain and sw_brain.modpackFestivalStandWatch then
            sw_brain.modpackFestivalStandWatch = false
            if BanditBrain and BanditBrain.Update then
                pcall(BanditBrain.Update, sister, sw_brain)
            end
        end
    end
    local now = getTimestampMs and getTimestampMs() or 0
    if now - lastCallOverMs < CALL_OVER_COOLDOWN_MS then
        local remaining = math.ceil((CALL_OVER_COOLDOWN_MS - (now - lastCallOverMs)) / 1000)
        if player.Say then
            local sName = ModpackFestivalSister and ModpackFestivalSister.getSisterForename and ModpackFestivalSister.getSisterForename() or "Alyssa"
            pcall(function() player:Say("(" .. sName .. " needs " .. remaining .. "s to regroup)") end)
        end
        return
    end
    lastCallOverMs = now
    local inventory = sister and ModpackFestivalSister.serializeBanditInventory
        and ModpackFestivalSister.serializeBanditInventory(sister) or nil
    sendClientCommand(player, MOD_ID, "SisterCallOver", { inventory = inventory })
end

local function requestSisterRecovery(sister)
    local now = getTimestampMs and getTimestampMs() or 0
    if now - lastSisterRecoveryRequestMs < 10000 then
        return
    end
    lastSisterRecoveryRequestMs = now
    local player = getPlayer()
    if player and sendClientCommand then
        -- don't serialize inventory here — the zombie may already be dead/cleared.
        -- the server uses the last snapshot saved during normal gameplay instead.
        sendClientCommand(player, MOD_ID, "SisterDied", {})
    end
end

local function getSisterNameForShout()
    local sister = ModpackFestivalSister and ModpackFestivalSister.findSisterBandit
        and ModpackFestivalSister.findSisterBandit()
    return getSisterDisplayName(sister)
end

local function shoutSisterNameAndCallOver(player)
    if not player then
        return false
    end
    if ModpackFestivalFeatures and ModpackFestivalFeatures.isSisterSpawnEnabled
        and not ModpackFestivalFeatures.isSisterSpawnEnabled() then
        return false
    end
    local now = getTimestampMs and getTimestampMs() or 0
    if now - lastShoutCallMs < SHOUT_CALL_COOLDOWN_MS then
        return false
    end
    lastShoutCallMs = now

    local name = getSisterNameForShout()
    if name and name ~= "" and player.Say then
        player:Say(string.upper(name) .. "!")
    end
    requestCallOver(player)
    return true
end

local function onKeyStartPressed(key)
    local core = getCore and getCore()
    if not core or not core.isKey or not core:isKey("Shout", key) then
        return
    end
    local player = getPlayer()
    if not player or (player.isDead and player:isDead()) then
        return
    end
    if shoutSisterNameAndCallOver(player) and GameKeyboard and GameKeyboard.eatKeyPress then
        pcall(GameKeyboard.eatKeyPress, key)
    end
end

local function getSisterInventoryTitle(sister)
    local name = getSisterDisplayName(sister)
    if name and name ~= "" then
        return name .. "'s Inventory"
    end
    return "Sister's Inventory"
end

local function addActiveSisterInventoryContainer(page, state)
    if state ~= "buttonsAdded" or not activeSisterInventory or not page then
        return
    end
    local playerIndex = activeSisterInventoryPlayerIndex or 0
    if page ~= (getPlayerLoot and getPlayerLoot(playerIndex) or nil) then
        return
    end
    local sister = ModpackFestivalSister and ModpackFestivalSister.findSisterBandit
        and ModpackFestivalSister.findSisterBandit()
    if not sister or not sister.getInventory or sister:getInventory() ~= activeSisterInventory then
        -- defer ISInventoryPage character restore to avoid re-entrancy during refreshBackpacks
        local snapIndex = playerIndex
        activeSisterInventory = nil
        activeSisterInventoryPlayerIndex = nil
        pendingSisterInventoryRestore = snapIndex
        return
    end
    local title = getSisterInventoryTitle(sister)
    local button = page:addContainerButton(activeSisterInventory, nil, title, title)
    if button then
        button.capacity = activeSisterInventory:getEffectiveCapacity(getSpecificPlayer(playerIndex))
    end
end

local function restoreSisterInventoryPage(playerIndex)
    if not sisterInventoryOrigChar then return end
    pcall(function()
        local playerInvPage = getPlayerInventory and getPlayerInventory(playerIndex or 0)
        if playerInvPage and playerInvPage.character ~= sisterInventoryOrigChar then
            playerInvPage.character = sisterInventoryOrigChar
            if playerInvPage.refreshBackpacks then playerInvPage:refreshBackpacks() end
            if playerInvPage.refresh then playerInvPage:refresh() end
        end
    end)
    sisterInventoryOrigChar = nil
end

local INVENTORY_OPEN_LINES = {
    "Oh sure, just go through all my stuff. Cool.",
    "Don't touch the knife. Actually — you know what, fine.",
    "Yeah yeah, dig around. I'll just stand here.",
    "If you move things around I swear to god.",
    "What, you don't trust me to carry my own stuff?",
    "Looking for something? Could've just asked.",
    "Go ahead. It's not like I have anything embarrassing in there.",
    "Ugh, fine. But I know exactly what's in there, so don't even think about it.",
    "Real subtle. Just rummaging through my bag.",
    "Help yourself, I guess. Not like I had a choice.",
    "You know normal people just ask first, right?",
    "Sure. Whatever. Just... try not to make a mess.",
}

local function sayInventoryOpenLine(sister)
    if not sister then return end
    local line = INVENTORY_OPEN_LINES[ZombRand and (ZombRand(#INVENTORY_OPEN_LINES) + 1) or 1]
    pcall(function()
        -- Bandit.Say only works with pre-registered sound keys, not freeform text.
        -- addLineChatElement shows a speech bubble with arbitrary text.
        if sister.addLineChatElement then
            sister:addLineChatElement(line, 0.9, 0.9, 0.9)
        end
    end)
end

local function openSisterInventory(playerIndex, sister)
    if not sister or not sister.getInventory then
        return
    end
    local inv = sister:getInventory()
    if not inv then
        return
    end
    local loot = getPlayerLoot and getPlayerLoot(playerIndex) or nil
    if not loot then
        return
    end
    activeSisterInventory = inv
    activeSisterInventoryPlayerIndex = playerIndex
    sisterInventorySessionUntilMs = (getTimestampMs and getTimestampMs() or 0) + INVENTORY_SESSION_MS
    sayInventoryOpenLine(sister)
    if ModpackFestivalSister and ModpackFestivalSister.serializeBanditInventory and sendClientCommand then
        local player = getSpecificPlayer(playerIndex)
        local inventory = ModpackFestivalSister.serializeBanditInventory(sister)
        lastInventorySnapshotSig = ModpackFestivalSister.inventoryEntriesSignature
            and ModpackFestivalSister.inventoryEntriesSignature(inventory)
            or lastInventorySnapshotSig
        if player then
            sendClientCommand(player, MOD_ID, "SisterInventorySnapshot", { inventory = inventory })
        end
    end
    -- Swap the player inventory panel (ISInventoryPage) to show Alyssa as the character.
    -- This gives us the worn-item slots and character model display instead of the raw loot panel.
    pcall(function()
        local playerInvPage = getPlayerInventory and getPlayerInventory(playerIndex)
        if playerInvPage and playerInvPage.character and playerInvPage.character ~= sister then
            sisterInventoryOrigChar = playerInvPage.character
            playerInvPage.character = sister
            playerInvPage:setVisible(true)
            if playerInvPage.refreshBackpacks then playerInvPage:refreshBackpacks() end
            if playerInvPage.refresh then playerInvPage:refresh() end
        end
    end)
    pcall(function()
        loot:setVisible(true)
        loot.isCollapsed = false
        if loot.clearMaxDrawHeight then
            loot:clearMaxDrawHeight()
        end
        if loot.setForceSelectedContainer then
            loot:setForceSelectedContainer(inv, INVENTORY_SESSION_MS)
        end
        if loot.refreshBackpacks then
            loot:refreshBackpacks()
        end
        if loot.selectButtonForContainer then
            loot:selectButtonForContainer(inv)
        end
        if setJoypadFocus and JoypadState and JoypadState.players and JoypadState.players[playerIndex + 1] then
            setJoypadFocus(playerIndex, loot)
        end
    end)
end

local function collectInventoryContextItems(items, out, seen)
    out = out or {}
    seen = seen or {}
    for _, entry in pairs(items or {}) do
        if entry and entry.getFullType then
            local key = tostring(entry)
            if not seen[key] then
                seen[key] = true
                table.insert(out, entry)
            end
        elseif type(entry) == "table" then
            if entry.item and entry.item.getFullType then
                local key = tostring(entry.item)
                if not seen[key] then
                    seen[key] = true
                    table.insert(out, entry.item)
                end
            end
            if entry.items then
                collectInventoryContextItems(entry.items, out, seen)
            end
        end
    end
    return out
end

local function isInActiveSisterInventory(item)
    if not item or not activeSisterInventory or not item.getContainer then
        return false
    end
    return item:getContainer() == activeSisterInventory
end

local function canWearItem(item)
    if not item then
        return false
    end
    local ok, bodyLocation = pcall(function()
        return item.getBodyLocation and item:getBodyLocation() or nil
    end)
    return ok and bodyLocation ~= nil and tostring(bodyLocation) ~= ""
end

local function isSisterEquipped(sister, item)
    if not sister or not item then return false end
    local ok, result = pcall(function()
        local primary = sister.getPrimaryHandItem and sister:getPrimaryHandItem()
        local secondary = sister.getSecondaryHandItem and sister:getSecondaryHandItem()
        return primary == item or secondary == item
    end)
    return ok and result == true
end

local function equipSisterInventoryItem(playerIndex, item, slot)
    local sister = ModpackFestivalSister and ModpackFestivalSister.findSisterBandit
        and ModpackFestivalSister.findSisterBandit()
    if not sister or not isInActiveSisterInventory(item) then
        return
    end
    if ModpackFestivalSister.equipInventoryItem
        and ModpackFestivalSister.equipInventoryItem(sister, item, slot) then
        sendSisterInventorySnapshot(sister, true)
        local loot = getPlayerLoot and getPlayerLoot(playerIndex or activeSisterInventoryPlayerIndex or 0) or nil
        if loot and loot.refreshBackpacks then
            pcall(function() loot:refreshBackpacks() end)
        end
    end
end

local function unequipSisterInventoryItem(playerIndex, item)
    local sister = ModpackFestivalSister and ModpackFestivalSister.findSisterBandit
        and ModpackFestivalSister.findSisterBandit()
    if not sister then return end
    pcall(function()
        if sister.setPrimaryHandItem then sister:setPrimaryHandItem(nil) end
        if sister.setSecondaryHandItem then sister:setSecondaryHandItem(nil) end
        -- clear equipped weapon from brain so respawn doesn't restore it
        local brain = BanditBrain and BanditBrain.Get and BanditBrain.Get(sister)
            or (sister.getModData and sister:getModData().brain)
        if brain then
            brain.modpackFestivalEquippedWeapon = nil
            brain.weapons = brain.weapons or {}
            brain.weapons.melee = nil
            brain.weapons.primary = nil
            brain.weapons.secondary = nil
            if BanditBrain and BanditBrain.Update then
                pcall(BanditBrain.Update, sister, brain)
            end
        end
    end)
    sendSisterInventorySnapshot(sister, true)
    local loot = getPlayerLoot and getPlayerLoot(playerIndex or activeSisterInventoryPlayerIndex or 0) or nil
    if loot and loot.refreshBackpacks then
        pcall(function() loot:refreshBackpacks() end)
    end
end

local function onFillInventoryObjectContextMenu(playerIndex, context, items)
    if not activeSisterInventory or not context then
        return
    end
    local sister = ModpackFestivalSister and ModpackFestivalSister.findSisterBandit
        and ModpackFestivalSister.findSisterBandit()
    if not sister or not sister.getInventory or sister:getInventory() ~= activeSisterInventory then
        return
    end

    local selected = {}
    for _, item in ipairs(collectInventoryContextItems(items)) do
        if isInActiveSisterInventory(item) then
            table.insert(selected, item)
        end
    end
    if #selected == 0 then
        return
    end

    local item = selected[1]
    local name = getSisterDisplayName(sister) or "Alyssa"
    local label = item.getDisplayName and item:getDisplayName() or item:getFullType()

    if isSisterEquipped(sister, item) then
        context:addOption("Unequip from " .. name .. ": " .. tostring(label), playerIndex, unequipSisterInventoryItem, item)
    else
        context:addOption("Equip on " .. name .. ": " .. tostring(label), playerIndex, equipSisterInventoryItem, item, "primary")
    end
end

function sendSisterInventorySnapshot(sister, allowEmpty)
    if not sister or not ModpackFestivalSister or not ModpackFestivalSister.serializeBanditInventory
        or not sendClientCommand then
        return false
    end
    local inventory = ModpackFestivalSister.serializeBanditInventory(sister)
    local signature = ModpackFestivalSister.inventoryEntriesSignature
        and ModpackFestivalSister.inventoryEntriesSignature(inventory)
        or tostring(#inventory)
    if signature == lastInventorySnapshotSig then
        return false
    end
    if #inventory == 0 and not allowEmpty then
        return false
    end
    local player = getPlayer()
    if not player then
        return false
    end
    lastInventorySnapshotSig = signature
    sendClientCommand(player, MOD_ID, "SisterInventorySnapshot", { inventory = inventory })
    return true
end

local function getSisterInventoryPayload(sister)
    if not sister or not ModpackFestivalSister or not ModpackFestivalSister.serializeBanditInventory then
        return nil
    end
    return ModpackFestivalSister.serializeBanditInventory(sister)
end

local function sendSisterVehicleCommand(command, sister, extra)
    if not sendClientCommand then
        return false
    end
    local player = getPlayer()
    if not player then
        return false
    end
    local args = extra or {}
    args.inventory = args.inventory or getSisterInventoryPayload(sister)
    sendClientCommand(player, MOD_ID, command, args)
    return true
end

local function isLocalPlayerCharacter(character)
    local player = getPlayer()
    return player ~= nil and character == player
end

local function sisterVehicleTravelUnlocked()
    if ModpackFestivalSister and ModpackFestivalSister.findSisterBandit
        and ModpackFestivalSister.findSisterBandit() then
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

local function clearSisterTasks(sister)
    local brain = getBrain(sister)
    if brain then
        brain.tasks = {}
        brain.modpackFestivalSisterCombatAllowed = false
        if BanditBrain and BanditBrain.Update then
            pcall(BanditBrain.Update, sister, brain)
        end
    end
    pcall(function()
        if Bandit and Bandit.ClearTasks then
            Bandit.ClearTasks(sister)
        end
    end)
    pcall(function() sister:setTarget(nil) end)
    pcall(function() sister:clearAggroList() end)
    pcall(function()
        local pathFind = sister.getPathFindBehavior2 and sister:getPathFindBehavior2()
        if pathFind and pathFind.cancel then
            pathFind:cancel()
        end
        if sister.setPath2 then
            sister:setPath2(nil)
        end
    end)
end

local function hideSisterForVehicle(sister, vehicle)
    sendSisterVehicleCommand("SisterVehicleDespawn", sister, {
        vehicleId = vehicle and vehicle.getId and vehicle:getId() or nil,
    })
    sisterVehicleState = {
        vehicle = vehicle,
        hidden = true,
    }
    return true
end


local function requestBoardSisterForVehicle(vehicle)
    local player = getPlayer()
    if not player or not vehicle then
        return false
    end
    if not sisterVehicleTravelUnlocked() then
        sisterVehicleState = nil
        return false
    end
    local sister = ModpackFestivalSister and ModpackFestivalSister.findSisterBandit
        and ModpackFestivalSister.findSisterBandit()
    if not sister and not (ModpackFestivalQuests and ModpackFestivalQuests.isCompleted
        and (ModpackFestivalQuests.isCompleted("meet_sister")
            or ModpackFestivalQuests.isCompleted("get_home"))) then
        sisterVehicleState = nil
        return false
    end
    return hideSisterForVehicle(sister, vehicle)
end

local function exitSisterFromVehicle(player)
    local state = sisterVehicleState
    if not state then
        return false
    end
    sisterVehicleState = nil
    if state.hidden then
        return sendSisterVehicleCommand("SisterVehicleExit", nil, {
            vehicleId = state.vehicle and state.vehicle.getId and state.vehicle:getId() or nil,
        })
    end

    local sister = ModpackFestivalSister and ModpackFestivalSister.findSisterBandit
        and ModpackFestivalSister.findSisterBandit()
    if sister and state.vehicle then
        sendSisterInventorySnapshot(sister, true)
        local seat = state.seat or (state.vehicle.getSeat and state.vehicle:getSeat(sister)) or -1
        pcall(function() state.vehicle:exit(sister) end)
        if seat and seat >= 0 then
            pcall(function() state.vehicle:setCharacterPosition(sister, seat, "outside") end)
        end
        local md = sister.getModData and sister:getModData() or nil
        if md then
            md.modpackFestivalSisterInVehicle = nil
            md.modpackFestivalSisterVehicleSeat = nil
        end
        clearSisterTasks(sister)
        if ModpackFestivalSister and ModpackFestivalSister.enableFollowMode then
            ModpackFestivalSister.enableFollowMode(sister, player)
        end
        sendSisterVehicleCommand("SisterVehicleExit", sister, {
            vehicleId = state.vehicle.getId and state.vehicle:getId() or nil,
            seat = seat,
        })
        return true
    end
    return sendSisterVehicleCommand("SisterVehicleExit", nil, {})
end

local function isSisterRidingWithPlayer(sister, player)
    if not sister or not player or not sister.getVehicle or not player.getVehicle then
        return false
    end
    local vehicle = player:getVehicle()
    return vehicle ~= nil and sister:getVehicle() == vehicle
end

local function onEnterVehicle(character)
    if not isLocalPlayerCharacter(character) then
        return
    end
    local vehicle = character.getVehicle and character:getVehicle() or nil
    if vehicle then
        requestBoardSisterForVehicle(vehicle)
    end
end

local function onExitVehicle(character)
    if not isLocalPlayerCharacter(character) then
        return
    end
    exitSisterFromVehicle(character)
end

local function sisterStandWatch(playerIndex, sister)
    local player = getSpecificPlayer(playerIndex)
    if not player or not sister then return end
    local brain = getBrain(sister)
    if not brain then return end
    local activating = not brain.modpackFestivalStandWatch
    brain.modpackFestivalStandWatch = activating
    if BanditBrain and BanditBrain.Update then
        pcall(BanditBrain.Update, sister, brain)
    end
    pcall(function()
        if ModpackFestivalSister and ModpackFestivalSister.sayAsSister then
            if activating then
                ModpackFestivalSister.sayAsSister("I'll keep watch here.", true)
            else
                ModpackFestivalSister.sayAsSister("Got it, moving.", true)
            end
        end
    end)
end

local function onPreFillWorldObjectContextMenu(playerIndex, context, worldobjects, test)
    if test then
        return
    end
    local sister = findSisterFromContext(worldobjects)
    if not sister then
        return
    end
    local player = getSpecificPlayer(playerIndex)
    local name = getSisterDisplayName(sister)
    local option = context:addOption(name, nil, noop)
    local menu = context:getNew(context)
    context:addSubMenu(option, menu)
    menu:addOption("Call " .. name .. " over", player, requestCallOver)
    local sisterBrain = getBrain(sister)
    local watchLabel = (sisterBrain and sisterBrain.modpackFestivalStandWatch) and "Stop Stand Watch" or "Stand Watch"
    menu:addOption(watchLabel, playerIndex, sisterStandWatch, sister)
    menu:addOption("Open Sister's Inventory", playerIndex, openSisterInventory, sister)
end

function protectSister(sister)
    if ModpackFestivalSister and ModpackFestivalSister.protectSister then
        return ModpackFestivalSister.protectSister(sister)
    end
    return false
end

local function clearSisterFatalTasks(sister)
    local brain = getBrain(sister)
    if not brain or not brain.tasks then
        return false
    end
    local kept = {}
    local changed = false
    for _, task in pairs(brain.tasks) do
        if task and (task.action == "Die" or task.action == "Zombify") then
            changed = true
        else
            table.insert(kept, task)
        end
    end
    if changed then
        brain.tasks = kept
        if BanditBrain and BanditBrain.Update then
            pcall(BanditBrain.Update, sister, brain)
        end
    end
    return changed
end

local SISTER_CLEAVE_RADIUS    = 1.5
local SISTER_CLEAVE_RADIUS_SQ = SISTER_CLEAVE_RADIUS * SISTER_CLEAVE_RADIUS
local SISTER_CLEAVE_DMG_MULT  = 0.6   -- cleave hits deal 60% of primary hit damage

local function applySisterBonusDamage(target, attacker, handWeapon)
    if not target or not attacker then return end
    if target.isDead and target:isDead() then return end
    local dmg = 0.3
    if handWeapon and handWeapon.getDamage then
        local ok, v = pcall(function() return handWeapon:getDamage() end)
        if ok and v and v > 0 then dmg = v end
    end
    pcall(function()
        target:changeHealth(-dmg)
        if target.getHealth and target:getHealth() <= 0 and not (target.isDead and target:isDead()) then
            if target.Kill then target:Kill(attacker) end
        end
    end)
end

local function applySisterCleave(primaryTarget, attacker, handWeapon)
    local cell = attacker and attacker.getCell and attacker:getCell()
    if not cell or not cell.getZombieList then return end
    local tx, ty, tz = primaryTarget:getX(), primaryTarget:getY(), primaryTarget:getZ() or 0
    local dmg = 0.3
    if handWeapon and handWeapon.getDamage then
        local ok, v = pcall(function() return handWeapon:getDamage() end)
        if ok and v and v > 0 then dmg = v * SISTER_CLEAVE_DMG_MULT end
    else
        dmg = dmg * SISTER_CLEAVE_DMG_MULT
    end
    local zombies = cell:getZombieList()
    if not zombies then return end
    for i = 0, zombies:size() - 1 do
        pcall(function()
            local z = zombies:get(i)
            if not z or z == primaryTarget or z == attacker then return end
            if z.isDead and z:isDead() then return end
            if z.getVariableBoolean and z:getVariableBoolean("Bandit") then return end
            local dz = math.abs((z:getZ() or 0) - tz)
            if dz > 0.75 then return end
            local dx = z:getX() - tx
            local dy = z:getY() - ty
            if dx * dx + dy * dy <= SISTER_CLEAVE_RADIUS_SQ then
                local bpt = BodyPartType and BodyPartType.Torso_Upper
                if bpt and z.Hit then
                    z:Hit(bpt, attacker, handWeapon, dmg, false, true)
                else
                    z:changeHealth(-dmg)
                    if z.getHealth and z:getHealth() <= 0 and not (z.isDead and z:isDead()) then
                        if z.Kill then z:Kill(attacker) end
                    end
                end
            end
        end)
    end
end

-- directly double min/max damage on all weapons in sister's inventory
-- called each UI tick so it reapplies automatically after respawn or item restore
local function boostSisterWeaponDamage(sister)
    local inv = sister.getInventory and sister:getInventory()
    if not inv then return end
    local items = inv.getItems and inv:getItems()
    if not items then return end
    for i = 0, items:size() - 1 do
        local item = items:get(i)
        if item and item.getMinDamage and item.setMinDamage then
            local md = item.getModData and item:getModData()
            if md and not md._sisterDmgBoosted then
                pcall(function()
                    local mn = item:getMinDamage()
                    local mx = item:getMaxDamage()
                    if mn and mx and (mn > 0 or mx > 0) then
                        item:setMinDamage(mn * 2)
                        item:setMaxDamage(mx * 2)
                        md._sisterDmgBoosted = true
                    end
                end)
            end
        end
    end
end

local function applyEmotionalHit()
    local player = getPlayer()
    if not player or not CharacterStat then return end
    local stats = player.getStats and player:getStats()
    if not stats then return end
    local stress = stats:get(CharacterStat.STRESS) or 0
    stats:set(CharacterStat.STRESS, math.min(1.0, stress + SISTER_HIT_STRESS_DELTA))
    local unhappy = stats:get(CharacterStat.UNHAPPINESS) or 0
    stats:set(CharacterStat.UNHAPPINESS, math.min(100.0, unhappy + SISTER_HIT_UNHAPPY_DELTA))
end


-- Feature 1: kill milestone acknowledgment
local function checkKillMilestone(count)
    local lines = ModpackFestivalSister and ModpackFestivalSister.MILESTONE_DIALOGUE_LINES
    if not lines then return end
    local line = lines[count]
    if not line or sisterMilestonesHit[count] then return end
    sisterMilestonesHit[count] = true
    local sister = cachedSister
    if not sister then return end
    pcall(function()
        if sister.addLineChatElement then
            sister:addLineChatElement(line, 0.4, 0.85, 1.0)
        end
    end)
    lastDialogueMs = getTimestampMs and getTimestampMs() or 0
end

-- Feature 2: contextual idle line picking (night / rain over generic)
local function pickContextualLine()
    if not ModpackFestivalSister then return nil end
    local isRaining = false
    pcall(function()
        local cm = getClimateManager and getClimateManager()
        isRaining = cm and cm.getRainIntensity and cm:getRainIntensity() > 0.3
    end)
    local isNight = false
    pcall(function()
        local gt = getGameTime and getGameTime()
        isNight = gt and gt.getNight and gt:getNight()
    end)
    if isRaining and ZombRand(2) == 0 then
        local pool = ModpackFestivalSister.RAIN_DIALOGUE_LINES
        if pool and #pool > 0 then return pool[ZombRand(#pool) + 1] end
    end
    if isNight and ZombRand(2) == 0 then
        local pool = ModpackFestivalSister.NIGHT_DIALOGUE_LINES
        if pool and #pool > 0 then return pool[ZombRand(#pool) + 1] end
    end
    return ModpackFestivalSister.pickIdleDialogueLine and ModpackFestivalSister.pickIdleDialogueLine()
end

-- Feature 4: injury limp window — tracks hit burst and applies limp animation
local function handleBurstHit(sister)
    local now = getTimestampMs and getTimestampMs() or 0
    if now - sisterHitBurstWindowMs > BURST_HIT_WINDOW_MS then
        sisterHitBurstCount  = 0
        sisterHitBurstWindowMs = now
    end
    sisterHitBurstCount = sisterHitBurstCount + 1
    if sisterHitBurstCount >= BURST_HIT_THRESHOLD and sisterLimpUntilMs < now then
        sisterLimpUntilMs    = now + LIMP_DURATION_MS
        sisterHitBurstCount  = 0
        pcall(function()
            sister:setVariable("BanditWalkType", "Limp")
            if sister.setWalkType then sister:setWalkType("Limp") end
        end)
        local pool = ModpackFestivalSister and ModpackFestivalSister.BURST_HIT_LINES
        if pool and #pool > 0 then
            local line = pool[ZombRand and (ZombRand(#pool) + 1) or 1]
            pcall(function()
                if sister.addLineChatElement then
                    sister:addLineChatElement(line, 1.0, 0.5, 0.5)
                end
            end)
            lastDialogueMs = now
        end
    end
end

local function onHitZombie(zombie, attacker, bodyPartType, handWeapon)
    if isLocalPlayerAttacker(attacker) and markBanditHostileToPlayer(zombie) then
        return
    end
    -- Double Alyssa's damage and cleave nearby zombies
    if isSister(attacker) then
        applySisterBonusDamage(zombie, attacker, handWeapon)
        applySisterCleave(zombie, attacker, handWeapon)
        -- record this hit so onZombieDead can credit the kill to sister
        sisterRecentHitSet[tostring(zombie)] = getTimestampMs and getTimestampMs() or 0
    end
    local ok, sister = pcall(isSisterOrTagged, zombie)
    if not ok or not sister then
        return
    end
    -- snapshot inventory now, while she may still be alive, so recovery has a clean ledger
    pcall(function()
        if ModpackFestivalSister and ModpackFestivalSister.captureInventoryFromBandit then
            ModpackFestivalSister.captureInventoryFromBandit(zombie)
        end
    end)
    -- watching sister get hurt stresses and upsets the player (cooldown prevents burst spikes)
    pcall(function()
        local now = getTimestampMs and getTimestampMs() or 0
        if now - lastSisterHitEmotionalMs >= SISTER_HIT_EMOTIONAL_COOLDOWN_MS then
            lastSisterHitEmotionalMs = now
            applyEmotionalHit()
        end
    end)
    -- injury burst window: enough hits in a short window triggers limp + voiced reaction
    handleBurstHit(zombie)
    -- immediately undo any damage from this hit, including headshots
    pcall(function()
        local bd = zombie.getBodyDamage and zombie:getBodyDamage()
        if bd and bd.RestoreToFullHealth then bd:RestoreToFullHealth() end
        if zombie.setHealth then zombie:setHealth(ModpackFestivalSister.FULL_HEALTH) end
    end)
    protectSister(zombie)
    clearSisterFatalTasks(zombie)
    if zombie.isDead and zombie:isDead() then
        requestSisterRecovery(zombie)
    end
end

local function onZombieDead(zombie)
    local ok, isSis = pcall(isSisterOrTagged, zombie)
    if ok and isSis then
        requestSisterRecovery(zombie)
        return
    end
    local zid = tostring(zombie)
    if sisterRecentHitSet[zid] then
        sisterRecentHitSet[zid] = nil
        sisterKillCount = sisterKillCount + 1
        checkKillMilestone(sisterKillCount)
    end
end

local function onZombieUpdate(zombie)
    if not zombie or not zombie.getModData then
        return
    end
    if not hasSisterTag(zombie) then
        if not zombie.getVariableBoolean or not zombie:getVariableBoolean("Bandit") or not isSister(zombie) then
            return
        end
    end

    protectSister(zombie)
    clearSisterFatalTasks(zombie)
    local health = zombie.getHealth and zombie:getHealth() or 1
    if health <= 0 then
        -- only restore body parts + health when actually at zero — avoids disrupting locomotion every tick
        pcall(function()
            local bd = zombie.getBodyDamage and zombie:getBodyDamage()
            if bd and bd.RestoreToFullHealth then bd:RestoreToFullHealth() end
            if zombie.setHealth then zombie:setHealth(ModpackFestivalSister.FULL_HEALTH) end
        end)
        if Bandit and Bandit.ClearTasks then
            pcall(Bandit.ClearTasks, zombie)
        end
    end
end

local function patchBanditVisualDamage()
    if banditVisualDamagePatched or not Bandit or not Bandit.AddVisualDamage then
        return
    end
    local original = Bandit.AddVisualDamage
    Bandit.AddVisualDamage = function(bandit, handWeapon)
        if isSister(bandit) then
            protectSister(bandit)
            return
        end
        if not handWeapon then
            return
        end
        return original(bandit, handWeapon)
    end
    banditVisualDamagePatched = true
end

local function patchBanditFriendlyFire()
    if banditFriendlyFirePatched or not BanditPlayer or not BanditPlayer.CheckFriendlyFire then
        return
    end
    local originalCheckFriendlyFire = BanditPlayer.CheckFriendlyFire
    BanditPlayer.CheckFriendlyFire = function(bandit, attacker)
        if isSisterOrTagged(bandit) then
            protectSister(bandit)
            return
        end
        if isLocalPlayerAttacker(attacker) and markBanditHostileToPlayer(bandit) then
            return
        end
        local ok, result = pcall(originalCheckFriendlyFire, bandit, attacker)
        if not ok then
            if isLocalPlayerAttacker(attacker) then
                markBanditHostileToPlayer(bandit)
                return
            end
            print("[ModpackFestivalSpawn][SisterClient] CheckFriendlyFire failed: " .. tostring(result))
            return
        end
        return result
    end
    banditFriendlyFirePatched = true
end

local function patchBanditFatalTasks()
    if banditFatalTaskPatched or not Bandit then
        return
    end
    if Bandit.AddTask then
        local originalAddTask = Bandit.AddTask
        Bandit.AddTask = function(bandit, task)
            if isSisterOrTagged(bandit) and task and (task.action == "Die" or task.action == "Zombify") then
                protectSister(bandit)
                clearSisterFatalTasks(bandit)
                return
            end
            return originalAddTask(bandit, task)
        end
    end
    if Bandit.AddTaskFirst then
        local originalAddTaskFirst = Bandit.AddTaskFirst
        Bandit.AddTaskFirst = function(bandit, task)
            if isSisterOrTagged(bandit) and task and (task.action == "Die" or task.action == "Zombify") then
                protectSister(bandit)
                clearSisterFatalTasks(bandit)
                return
            end
            return originalAddTaskFirst(bandit, task)
        end
    end
    banditFatalTaskPatched = true
end

local function patchBanditSay()
    if banditSayPatched or not Bandit then
        return
    end
    if Bandit.Say then
        local originalSay = Bandit.Say
        Bandit.Say = function(bandit, phrase, force)
            if not isSisterOrTagged(bandit) then
                return originalSay(bandit, phrase, force)
            end
            -- suppress default Bandits2 caption/audio; show sister's own line instead
            -- skip combat bark if a scripted/priority line is currently displayed
            if ModpackFestivalSister and ModpackFestivalSister.isScriptedSpeechActive
                and ModpackFestivalSister.isScriptedSpeechActive() then
                return
            end
            local line = ModpackFestivalSister and ModpackFestivalSister.pickCombatLine
                and ModpackFestivalSister.pickCombatLine(phrase)
            if line then
                pcall(function()
                    local brain = getBrain(bandit)
                    if brain and brain.speech and brain.speech > 0 and not force then return end
                    bandit:addLineChatElement(line, 0.4, 0.85, 1.0)
                    if brain then brain.speech = 2 end
                end)
            end
            -- always suppress the Bandits2 voice audio for sister (she's not a generic bandit)
        end
    end
    if Bandit.SayLocation then
        local originalSayLocation = Bandit.SayLocation
        Bandit.SayLocation = function(bandit, targetSquare)
            if isSisterOrTagged(bandit) then
                return
            end
            return originalSayLocation(bandit, targetSquare)
        end
    end
    banditSayPatched = true
end

local function patchBanditDeathInventory()
    if banditDeathInventoryPatched or not Bandit or not Bandit.UpdateItemsToSpawnAtDeath then
        return
    end
    local original = Bandit.UpdateItemsToSpawnAtDeath
    Bandit.UpdateItemsToSpawnAtDeath = function(zombie, brain)
        if not isSisterOrTagged(zombie) then
            return original(zombie, brain)
        end
        -- The original crashes on getAllEvalRecurse for sister.
        -- We still need to mark her items preserve=true so PZ doesn't drop them on death,
        -- but we skip the spawnAtDeath list entirely since we manage inventory via snapshot.
        pcall(function()
            local inv = zombie.getInventory and zombie:getInventory()
            if not inv then return end
            local items = inv.getItems and inv:getItems()
            if not items then return end
            for i = 0, items:size() - 1 do
                local item = items:get(i)
                if item and item.getModData then
                    item:getModData().preserve = true
                end
            end
        end)
        pcall(function()
            if zombie.clearItemsToSpawnAtDeath then
                zombie:clearItemsToSpawnAtDeath()
            end
        end)
    end
    banditDeathInventoryPatched = true
end

local function patchBanditEnemyFilter()
    if banditEnemyFilterPatched or not BanditUtils or not BanditUtils.AreEnemies then
        return
    end
    local original = BanditUtils.AreEnemies
    BanditUtils.AreEnemies = function(brain1, brain2)
        local sisterBrain = nil
        local otherBrain = nil
        if isSisterBrain(brain1) then
            sisterBrain = brain1
            otherBrain = brain2
        elseif isSisterBrain(brain2) then
            sisterBrain = brain2
            otherBrain = brain1
        end
        if sisterBrain then
            if otherBrain ~= nil then
                return isHostileToSisterBrain(otherBrain)
            end
            return sisterBrain.modpackFestivalSisterCombatAllowed == true
        end
        return original(brain1, brain2)
    end
    banditEnemyFilterPatched = true
end

local function getBanditMenuClickedZombie()
    if not BanditCompatibility or not BanditCompatibility.GetClickedSquare then
        return nil
    end
    local ok, square = pcall(BanditCompatibility.GetClickedSquare)
    if not ok or not square then
        return nil
    end
    local zombie = square:getZombie()
    if zombie then
        return zombie
    end
    local squareS = square:getS()
    if squareS then
        zombie = squareS:getZombie()
        if zombie then
            return zombie
        end
    end
    local squareW = square:getW()
    return squareW and squareW:getZombie() or nil
end

local function patchBanditMenu()
    if banditMenuPatched or not BanditMenu then
        return
    end

    if BanditMenu.SwitchProgram then
        local originalSwitchProgram = BanditMenu.SwitchProgram
        BanditMenu.SwitchProgram = function(player, bandit, program)
            if program == "Looter" and isSister(bandit) then
                if ModpackFestivalSister and ModpackFestivalSister.enableFollowMode then
                    ModpackFestivalSister.enableFollowMode(bandit, player)
                end
                return
            end
            return originalSwitchProgram(player, bandit, program)
        end
    end

    if BanditMenu.WorldContextMenuPre then
        local originalWorldContextMenuPre = BanditMenu.WorldContextMenuPre
        BanditMenu.WorldContextMenuPre = function(playerID, context, worldobjects, test)
            local zombie = getBanditMenuClickedZombie()
            if isSister(zombie) then
                return
            end
            return originalWorldContextMenuPre(playerID, context, worldobjects, test)
        end
        if Events and Events.OnPreFillWorldObjectContextMenu then
            pcall(function()
                Events.OnPreFillWorldObjectContextMenu.Remove(originalWorldContextMenuPre)
            end)
            pcall(function()
                Events.OnPreFillWorldObjectContextMenu.Add(BanditMenu.WorldContextMenuPre)
            end)
        end
    end

    banditMenuPatched = true
end

local function isCombatTask(task)
    if not task then
        return false
    end
    return task.action == "Smack"
        or task.action == "Push"
        or task.action == "Shoot"
        or task.action == "Aim"
        or task.action == "FaceLocation"
end

local function isOffensiveCombatTask(task)
    if not task then
        return false
    end
    return task.action == "Smack"
        or task.action == "Push"
        or task.action == "Shoot"
        or task.action == "Aim"
end

local function isSisterInCombat(sister)
    local brain = getBrain(sister)
    if not brain or not brain.tasks then
        return false
    end
    if brain.modpackFestivalSisterCombatAllowed == true then
        return true
    end
    for _, task in pairs(brain.tasks) do
        if isOffensiveCombatTask(task) then
            return true
        end
    end
    return false
end

-- Count non-sister zombies within radius of sister using Bandits2's light cache.
-- Returns enemies, friendlies (player counts as 1 friendly if within radius).
local FLEE_COUNT_RADIUS    = 6
local FLEE_COUNT_RADIUS_SQ = FLEE_COUNT_RADIUS * FLEE_COUNT_RADIUS
local FLEE_ENEMY_THRESHOLD = 8   -- must outnumber friendlies by this many to allow flee

local function countFleeParticipants(sister, player)
    local sx, sy, sz = sister:getX(), sister:getY(), sister:getZ() or 0
    local enemies = 0
    if BanditZombie and BanditZombie.CacheLightZ then
        for _, light in pairs(BanditZombie.CacheLightZ) do
            if math.abs((light.z or 0) - sz) <= 0.75 then
                local dx = light.x - sx
                local dy = light.y - sy
                if dx * dx + dy * dy <= FLEE_COUNT_RADIUS_SQ then
                    enemies = enemies + 1
                end
            end
        end
    end
    local friendlies = 0
    if player then
        local dx = player:getX() - sx
        local dy = player:getY() - sy
        if dx * dx + dy * dy <= FLEE_COUNT_RADIUS_SQ then
            friendlies = friendlies + 1
        end
    end
    return enemies, friendlies
end

local function cancelFleeTasks(sister)
    pcall(function()
        local brain = getBrain(sister)
        if not brain then return end
        Bandit.ClearTasks(sister)
        brain.tasks = {}
        if BanditBrain.Update then pcall(BanditBrain.Update, sister, brain) end
    end)
end

local function isBanditsFleeTask(brain, task)
    if not brain or not task then
        return false
    end
    if task.action ~= "Move" and task.action ~= "GoTo" then
        return false
    end
    if task.walkType ~= "Run" then
        return false
    end
    if task.tid or task.isPlayer == true then
        return false
    end
    return brain.escapeX ~= nil
        and brain.escapeY ~= nil
        and (task.time or 0) >= 100
end

local function isFleeingFromBandits(sister)
    local brain = getBrain(sister)
    if not brain or not brain.tasks then
        return false
    end
    for _, task in pairs(brain.tasks) do
        if isBanditsFleeTask(brain, task) then
            return true
        end
    end
    return false
end

local function maybeSpeakFleeLine(sister)
    local fleeing = isFleeingFromBandits(sister)
    if not fleeing then
        wasFleeing = false
        return
    end
    if wasFleeing then
        return
    end
    wasFleeing = true
    if ModpackFestivalSister and ModpackFestivalSister.isScriptedSpeechActive
        and ModpackFestivalSister.isScriptedSpeechActive() then
        return
    end
    local now = getTimestampMs and getTimestampMs() or 0
    if now - lastFleeDialogueMs < FLEE_DIALOGUE_COOLDOWN_MS then
        return
    end
    local line = ModpackFestivalSister and ModpackFestivalSister.pickFleeDialogueLine
        and ModpackFestivalSister.pickFleeDialogueLine()
    if line and ModpackFestivalSister.sayAsSister(line, true) then
        lastFleeDialogueMs = now
        lastDialogueMs = now
    end
end

local function clearInvalidSisterCombat(sister, player)
    if not sister or not player or not BanditBrain or not BanditBrain.Get then
        return
    end
    local ok, brain = pcall(BanditBrain.Get, sister)
    if not ok or not brain or not brain.tasks then
        return
    end

    local shouldClear = false
    for _, task in pairs(brain.tasks) do
        if isCombatTask(task) and task.x and task.y then
            if math.abs((task.z or sister:getZ() or 0) - (player:getZ() or 0)) > 0.75
                or distSqXY(task.x, task.y, player:getX(), player:getY()) > DEFEND_PLAYER_RADIUS_SQ then
                shouldClear = true
                break
            end
        end
    end

    if shouldClear then
        brain.tasks = {}
        brain.modpackFestivalSisterCombatAllowed = false
        if BanditBrain.Update then
            pcall(BanditBrain.Update, sister, brain)
        end
    end
end

-- reduce all Bandits2 task durations by 20% so every action (attack, shoot, equip, move) completes faster
-- task.attackTime is also scaled so hit registration stays proportional within the animation
-- If sister is physically holding two different weapons, drop the secondary.
-- This prevents the dual-wield state that crashes BanditBrain.IsOutOfAmmo.
local function enforceSingleWeapon(sister)
    pcall(function()
        local primary   = sister.getPrimaryHandItem   and sister:getPrimaryHandItem()
        local secondary = sister.getSecondaryHandItem and sister:getSecondaryHandItem()
        if not primary or not secondary then return end
        if primary == secondary then return end  -- two-handed grip on same item is fine
        -- secondary hand has a different item — clear it
        if sister.setSecondaryHandItem then sister:setSecondaryHandItem(nil) end
        -- also sanitize brain weapon slots so bulletsLeft is never nil
        local brain = getBrain(sister)
        if brain and brain.weapons and ModpackFestivalSister and ModpackFestivalSister.sanitizeWeaponSlots then
            ModpackFestivalSister.sanitizeWeaponSlots(brain.weapons)
        end
    end)
end

local function boostSisterTaskSpeed(sister)
    local brain = getBrain(sister)
    if not brain or not brain.tasks then return end
    for _, task in pairs(brain.tasks) do
        if not task._sisterSpeedBoosted and task.time and task.time > 1 then
            task._sisterSpeedBoosted = true
            task.time = math.max(1, math.floor(task.time * 0.833))
            if task.attackTime and task.attackTime > 0 then
                task.attackTime = math.max(1, math.floor(task.attackTime * 0.833))
            end
        end
    end
end

local function noteSisterCombatIfActive(sister)
    if isSisterInCombat(sister) then
        lastSisterCombatMs = getTimestampMs and getTimestampMs() or 0
    end
end

local function getCurrentMoveTarget(sister, player)
    local brain = getBrain(sister)
    if brain and brain.tasks then
        for _, task in pairs(brain.tasks) do
            if (task.action == "Move" or task.action == "GoTo") and task.x and task.y then
                return task.x, task.y, task.z or sister:getZ() or 0
            end
        end
    end
    return player:getX(), player:getY(), player:getZ() or 0
end

local function isWindowLike(obj)
    if not obj or not instanceof then
        return false
    end
    if instanceof(obj, "IsoWindow") or instanceof(obj, "IsoWindowFrame") then
        return true
    end
    return instanceof(obj, "IsoThumpable") and obj.isWindow and obj:isWindow()
end

local function getObjectNorth(obj)
    if obj and obj.getNorth then
        local ok, north = pcall(function() return obj:getNorth() end)
        if ok then
            return north == true
        end
    end
    if obj and obj.isNorth then
        local ok, north = pcall(function() return obj:isNorth() end)
        if ok then
            return north == true
        end
    end
    local props = obj and obj.getProperties and obj:getProperties()
    return props and props.has and IsoFlagType and IsoFlagType.WindowN
        and props:has(IsoFlagType.WindowN)
end

local function windowSeparatesFollowerFromTarget(window, sister, tx, ty)
    local sq = window and window.getSquare and window:getSquare()
    if not sq then
        return false
    end
    local wx = sq:getX()
    local wy = sq:getY()
    local sx = sister:getX()
    local sy = sister:getY()
    if getObjectNorth(window) then
        return ((sy < wy and ty >= wy) or (sy >= wy and ty < wy))
            and math.abs(sx - wx) <= 1.75
    end
    return ((sx < wx and tx >= wx) or (sx >= wx and tx < wx))
        and math.abs(sy - wy) <= 1.75
end

local function findBlockingWindowNearSister(sister, player)
    local sq = sister and sister.getSquare and sister:getSquare()
    local cell = sq and sq.getCell and sq:getCell()
    if not cell then
        return nil
    end
    local tx, ty, tz = getCurrentMoveTarget(sister, player)
    local sx = math.floor(sister:getX())
    local sy = math.floor(sister:getY())
    local sz = math.floor((sister:getZ() or 0) + 0.5)
    for dx = -1, 1 do
        for dy = -1, 1 do
            local testSq = cell:getGridSquare(sx + dx, sy + dy, sz)
            local objects = testSq and testSq.getObjects and testSq:getObjects()
            if objects then
                for i = 0, objects:size() - 1 do
                    local obj = objects:get(i)
                    if isWindowLike(obj)
                        and math.abs((tz or sz) - sz) <= 0.75
                        and windowSeparatesFollowerFromTarget(obj, sister, tx, ty) then
                        return obj
                    end
                end
            end
        end
    end
    return nil
end

local function canClimbThrough(window, sister)
    if not window or not window.canClimbThrough then
        return false
    end
    local ok, result = pcall(function()
        return window:canClimbThrough(sister)
    end)
    return ok and result == true
end

local function openOrBreakWindow(window, sister)
    if not window then
        return
    end
    if instanceof(window, "IsoWindow") then
        pcall(function()
            if not window:IsOpen() and not window:isSmashed() then
                window:ToggleWindow(sister)
            end
        end)
        if not canClimbThrough(window, sister) then
            pcall(function()
                if not window:isSmashed() then
                    window:smashWindow()
                    local sq = window:getSquare()
                    if sq and sq.playSound then
                        sq:playSound("SmashWindow")
                    end
                end
            end)
        end
    elseif instanceof(window, "IsoThumpable") and window.isWindow and window:isWindow() then
        pcall(function()
            if window.ToggleWindow and not window:IsOpen() then
                window:ToggleWindow(sister)
            end
        end)
        if not canClimbThrough(window, sister) then
            pcall(function()
                if window.smashWindow then
                    window:smashWindow()
                end
            end)
        end
    end
end

local function climbThroughWindow(window, sister)
    if not window or not sister or not ClimbThroughWindowState then
        return false
    end
    if not canClimbThrough(window, sister) then
        return false
    end
    local ok = pcall(function()
        ClimbThroughWindowState.instance():setParams(sister, window)
        sister:changeState(ClimbThroughWindowState.instance())
        if sister.setBumpType then
            sister:setBumpType("ClimbWindow")
        end
    end)
    return ok == true
end

local function assistSisterWindowFollow(sister, player)
    if not sister or not player then
        return
    end
    if distSqXY(sister:getX(), sister:getY(), player:getX(), player:getY())
        > (WINDOW_ASSIST_MAX_PLAYER_DIST * WINDOW_ASSIST_MAX_PLAYER_DIST) then
        return
    end
    local now = getTimestampMs and getTimestampMs() or 0
    if now - lastWindowAssistMs < WINDOW_ASSIST_COOLDOWN_MS then
        return
    end
    local window = findBlockingWindowNearSister(sister, player)
    if not window then
        return
    end
    lastWindowAssistMs = now
    openOrBreakWindow(window, sister)
    if climbThroughWindow(window, sister) then
        local brain = getBrain(sister)
        if brain and brain.tasks then
            brain.tasks = {}
            if BanditBrain and BanditBrain.Update then
                pcall(BanditBrain.Update, sister, brain)
            end
        end
    end
end

-- Returns the real-world ms timestamp when find_sister was completed,
-- storing it in ModData the first time it's detected.
local function getFindSisterCompletedAtMs()
    if not ModpackFestivalQuests or not ModpackFestivalQuests.isCompleted then
        return nil
    end
    if not ModpackFestivalQuests.isCompleted("find_sister") then
        return nil
    end
    local md = ModData.getOrCreate("ModpackFestivalSpawn")
    if not md.findSisterCompletedAtMs then
        md.findSisterCompletedAtMs = getTimestampMs and getTimestampMs() or 0
    end
    return md.findSisterCompletedAtMs
end

local function idleDialogueUnlocked()
    local completedAt = getFindSisterCompletedAtMs()
    if not completedAt then return false end
    local now = getTimestampMs and getTimestampMs() or 0
    return now - completedAt >= IDLE_UNLOCK_AFTER_FIND_SISTER_MS
end

local function maybeSpeak(sister, player)
    if ModpackFestivalSister and ModpackFestivalSister.isScriptedSpeechActive
        and ModpackFestivalSister.isScriptedSpeechActive() then
        return
    end
    if not idleDialogueUnlocked() then
        return
    end
    local now = getTimestampMs and getTimestampMs() or 0
    if now - lastSisterCombatMs < IDLE_AFTER_COMBAT_SUPPRESS_MS then
        return
    end
    if now - lastDialogueMs < 90000 then
        return
    end
    if ZombRand and ZombRand(3) ~= 0 then
        return
    end
    local line
    if player and player.getVehicle and player:getVehicle()
        and ModpackFestivalSister.pickVehicleDialogueLine then
        line = ModpackFestivalSister.pickVehicleDialogueLine()
    else
        line = pickContextualLine()
    end
    if line and ModpackFestivalSister.sayAsSister(line) then
        lastDialogueMs = now
    end
end

-- Apply inventory from file backup directly on the client. This bypasses the server-side
-- inv:AddItem path which silently fails for Bandits2 zombies in B42.
local function clientApplyInventoryFromFile(sister)
    if not ModpackFestivalSister or not ModpackFestivalSister.loadInventoryFromFile then return false end
    -- only restore from file if ModData also has a ledger for this save — prevents
    -- stale file backup from bleeding into a new game that has no inventory snapshot yet
    local snapshot = ModpackFestivalSister.getInventorySnapshot and ModpackFestivalSister.getInventorySnapshot()
    if not snapshot or #snapshot == 0 then return false end
    local entries, equippedWeapon = ModpackFestivalSister.loadInventoryFromFile()
    if not entries or #entries == 0 then return false end
    local inv = sister.getInventory and sister:getInventory()
    if not inv then return false end
    local added = 0
    for _, entry in ipairs(entries) do
        for _ = 1, entry.count or 1 do
            local item = nil
            pcall(function() item = inv:AddItem(entry.fullType) end)
            if item then
                added = added + 1
            end
        end
    end
    if added > 0 then
        -- equip the saved weapon
        if equippedWeapon then
            pcall(function()
                local items = inv:getItems()
                for i = 0, items:size() - 1 do
                    local it = items:get(i)
                    if it and it.getFullType and it:getFullType() == equippedWeapon then
                        ModpackFestivalSister.equipInventoryItem(sister, it, "primary")
                        break
                    end
                end
            end)
        end
        -- sync snapshot to server
        sendSisterInventorySnapshot(sister, true)
        print("[ModpackFestivalSpawn][SisterClient] client-side inventory applied: " .. added .. " items")
        return true
    end
    return false
end

local function onTick()
    tickCount = tickCount + 1
    if pendingSisterInventoryRestore ~= nil then
        local idx = pendingSisterInventoryRestore
        pendingSisterInventoryRestore = nil
        restoreSisterInventoryPage(idx)
    end
    patchBanditVisualDamage()
    patchBanditFriendlyFire()
    patchBanditEnemyFilter()
    patchBanditFatalTasks()
    patchBanditSay()
    patchBanditMenu()
    patchBanditDeathInventory()
    local needsFast = ModpackFestivalTick.every(tickCount, ModpackFestivalTick.UI_FAST)
    local needsUi = ModpackFestivalTick.every(tickCount, ModpackFestivalTick.UI)
    local needsRare = ModpackFestivalTick.every(tickCount, ModpackFestivalTick.RARE)
    if not needsFast and not needsUi and not needsRare then
        return
    end
    local sister = ModpackFestivalSister and ModpackFestivalSister.findSisterBandit
        and ModpackFestivalSister.findSisterBandit()
    if not sister then
        cachedSister = nil
        local player = getPlayer()
        if sisterVehicleState and sisterVehicleState.hidden then
            if not (player and player.getVehicle and player:getVehicle()) then
                exitSisterFromVehicle(player)
            end
        end
        wasFleeing = false
        restoreSisterInventoryPage(activeSisterInventoryPlayerIndex)
        activeSisterInventory = nil
        activeSisterInventoryPlayerIndex = nil
        return
    end
    cachedSister = sister
    local player = getPlayer()
    -- detect fresh spawn by zombie ID change
    local sid = sister.getOnlineID and sister:getOnlineID() or tostring(sister)
    if sid ~= lastKnownSisterId then
        lastKnownSisterId = sid
        clientInventoryApplied = false
        clientAppearanceApplied = false
        sisterWasFleeingForCooldown = false
        sisterFleeEndedMs = 0
    end

    if needsUi then
        protectSister(sister)
        clearSisterFatalTasks(sister)
        -- client-side appearance: getHumanVisual only works here, not server-side
        if not clientAppearanceApplied and ModpackFestivalSister and ModpackFestivalSister.applyStoredAppearanceToBandit then
            pcall(ModpackFestivalSister.applyStoredAppearanceToBandit, sister, true)
            clientAppearanceApplied = true
        end
        local now = getTimestampMs and getTimestampMs() or 0
        sendSisterInventorySnapshot(sister, false)
        boostSisterWeaponDamage(sister)
        -- client-side inventory fallback: if sister has no items, apply from file backup
        if not clientInventoryApplied then
            local inv = sister.getInventory and sister:getInventory()
            local items = inv and inv.getItems and inv:getItems()
            local count = items and items.size and items:size() or 0
            if count == 0 then
                clientInventoryApplied = clientApplyInventoryFromFile(sister)
            else
                clientInventoryApplied = true
            end
        end
        if activeSisterInventory and now >= sisterInventorySessionUntilMs then
            local playerIndex = activeSisterInventoryPlayerIndex or 0
            local loot = getPlayerLoot and getPlayerLoot(playerIndex) or nil
            if not loot or not loot.getIsVisible or not loot:getIsVisible() then
                restoreSisterInventoryPage(playerIndex)
                activeSisterInventory = nil
                activeSisterInventoryPlayerIndex = nil
            end
        end
    end
    if isSisterRidingWithPlayer(sister, player) then
        if needsFast then
            clearSisterTasks(sister)
            protectSister(sister)
        end
        if needsRare then
            maybeSpeak(sister, player)
        end
        return
    elseif sister.getVehicle and sister:getVehicle() and not (player and player.getVehicle and player:getVehicle()) then
        exitSisterFromVehicle(player)
        return
    elseif needsUi and player and player.getVehicle and player:getVehicle()
        and not sisterVehicleState
    then
        -- Tick-based fallback: player entered a vehicle but OnEnterVehicle was missed.
        requestBoardSisterForVehicle(player:getVehicle())
        return
    end
    if needsFast then
        if ModpackFestivalSister and ModpackFestivalSister.applyFollowSpeed then
            ModpackFestivalSister.applyFollowSpeed(sister)
        end
        if ModpackFestivalSister and ModpackFestivalSister.applyAnimationSpeed then
            ModpackFestivalSister.applyAnimationSpeed(sister)
        end
        -- limp recovery — re-apply limp each fast tick to override applyAnimationSpeed,
        -- then clear and restore walk when the window expires
        if sisterLimpUntilMs > 0 then
            local now = getTimestampMs and getTimestampMs() or 0
            if now >= sisterLimpUntilMs then
                sisterLimpUntilMs = 0
                pcall(function()
                    sister:setVariable("BanditWalkType", "Walk")
                    if sister.setWalkType then sister:setWalkType("Walk") end
                end)
            else
                pcall(function() sister:setVariable("BanditWalkType", "Limp") end)
            end
        end
        maybeSpeakFleeLine(sister)
        local fleeing = isFleeingFromBandits(sister)
        if fleeing then
            local now = getTimestampMs and getTimestampMs() or 0
            local enemies, friendlies = countFleeParticipants(sister, player)
            local outnumbered = enemies >= friendlies + FLEE_ENEMY_THRESHOLD

            if not outnumbered then
                -- not outnumbered enough — cancel flee, return to companion AI
                cancelFleeTasks(sister)
            elseif sisterFleeEndedMs > 0 and now - sisterFleeEndedMs < FLEE_COOLDOWN_MS then
                -- outnumbered but still on cooldown — cancel
                cancelFleeTasks(sister)
            else
                -- genuinely outnumbered: commit flee for fixed 4 seconds
                pcall(function()
                    local brain = getBrain(sister)
                    if not brain or not brain.tasks then return end
                    for _, task in pairs(brain.tasks) do
                        if (task.action == "Move" or task.action == "GoTo")
                            and task.walkType == "Run"
                            and task.lock == true
                            and not task._timeSet
                        then
                            task.time = FLEE_COMMIT_TICKS
                            task._timeSet = true
                        end
                    end
                end)
                -- speed boost while fleeing
                pcall(function()
                    sister:setSpeedMod(ModpackFestivalSister.FOLLOW_SPEED_MULT * 1.5)
                    sister:setVariable("RunSpeed", ModpackFestivalSister.RUN_ANIM_SPEED * 1.5)
                end)
            end
            sisterWasFleeingForCooldown = outnumbered
        elseif sisterWasFleeingForCooldown then
            -- flee just ended: start cooldown
            sisterFleeEndedMs = getTimestampMs and getTimestampMs() or 0
            sisterWasFleeingForCooldown = false
        end
        enforceSingleWeapon(sister)
        boostSisterTaskSpeed(sister)
        clearInvalidSisterCombat(sister, player)
        noteSisterCombatIfActive(sister)
        assistSisterWindowFollow(sister, player)
    end
    if needsRare then
        maybeSpeak(sister, getPlayer())
    end
end

patchBanditFriendlyFire()
patchBanditFatalTasks()
patchBanditSay()
patchBanditDeathInventory()

-- PZ Java disables fast-forward when IsoZombies (including Bandits2 NPCs) are nearby.
-- Re-apply the desired speed via the SpeedControls object after the vanilla handler runs.
pcall(function()
    if isClient() then return end
    local orig = SpeedControlsHandler and SpeedControlsHandler.onKeyPressed
    if not orig then return end
    SpeedControlsHandler.onKeyPressed = function(key)
        orig(key)
        local sc = UIManager and UIManager.getSpeedControls and UIManager.getSpeedControls()
        if not sc then return end
        pcall(function()
            if getCore():isKey("Fast Forward x1", key) then
                sc:SetCurrentGameSpeed(2); getGameTime():setMultiplier(5)
            elseif getCore():isKey("Fast Forward x2", key) then
                sc:SetCurrentGameSpeed(3); getGameTime():setMultiplier(20)
            elseif getCore():isKey("Fast Forward x3", key) then
                sc:SetCurrentGameSpeed(4); getGameTime():setMultiplier(40)
            elseif getCore():isKey("Normal Speed", key) then
                sc:SetCurrentGameSpeed(1); getGameTime():setMultiplier(1)
            end
        end)
    end
end)

Events.OnPreFillWorldObjectContextMenu.Add(onPreFillWorldObjectContextMenu)
Events.OnRefreshInventoryWindowContainers.Add(addActiveSisterInventoryContainer)
if Events.OnFillInventoryObjectContextMenu then
    Events.OnFillInventoryObjectContextMenu.Add(onFillInventoryObjectContextMenu)
end
Events.OnZombieUpdate.Add(onZombieUpdate)
Events.OnHitZombie.Add(onHitZombie)
Events.OnZombieDead.Add(onZombieDead)
Events.OnKeyStartPressed.Add(onKeyStartPressed)
-- Reset all client-side sister state on new game so stale local vars can't carry over.
local function onGameStart()
    cachedSister = nil
    lastKnownSisterId = nil
    clientInventoryApplied = false
    clientAppearanceApplied = false
    sisterWasFleeingForCooldown = false
    sisterFleeEndedMs = 0
    lastCallOverMs = 0
    lastSisterHitEmotionalMs = 0
    sisterKillCount        = 0
    sisterMilestonesHit    = {}
    sisterRecentHitSet     = {}
    sisterHitBurstCount    = 0
    sisterHitBurstWindowMs = 0
    sisterLimpUntilMs      = 0
    -- also clear the ModData ledger client-side in case it carried over in memory
    pcall(function()
        local md = ModData.getOrCreate("ModpackFestivalSpawn")
        if md.sister then
            md.sister.sisterInventoryLedger = nil
            md.sister.sisterAppearanceData = nil
        end
    end)
end

local function onSave()
    local sister = cachedSister
    if sister and not (sister.isDead and sister:isDead()) then
        sendSisterInventorySnapshot(sister, false)
    end
end

Events.OnEnterVehicle.Add(onEnterVehicle)
Events.OnExitVehicle.Add(onExitVehicle)
Events.OnTick.Add(onTick)
Events.OnGameStart.Add(onGameStart)
Events.OnSave.Add(onSave)
print("[ModpackFestivalSpawn] sister client hooks loaded")

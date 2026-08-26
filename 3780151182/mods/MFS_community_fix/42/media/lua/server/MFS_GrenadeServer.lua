--[[---------------------------------------------------------------------------
    MFS_GrenadeServer.lua

    Server authority and client visual broadcasts for the rebuilt MFS grenade
    launcher. The server owns authoritative multiplayer damage through an
    owner-bound IsoTrap. The shooter also runs the tested B42 local trap path
    for native sound/kill attribution; durable double-application remains an
    explicit audit test, so do not suppress either path without evidence.
    Clients receive launch and impact coordinates for local visuals.

    The command structure follows ExplosivesServer.lua from "US Military
    Explosives / US Military Grenades [B42]" by Corvus, used with permission.
    MFS adds validation, request deduplication and server-to-client broadcasts.
-----------------------------------------------------------------------------]]

if isClient() then return end

require "MFSUnderbarrelRegistry"

MFSGrenadeServer = MFSGrenadeServer or {}
local Server = MFSGrenadeServer

local MP = MFSUnderbarrelRegistry.MP
Server.VERSION = MP.VERSION
Server.MODULE = MP.MODULE
Server.LAUNCH = MP.LAUNCH
Server.TRIGGER_EXPLOSION = MP.TRIGGER_EXPLOSION
Server.EXPLOSION = MP.EXPLOSION
Server.CREATE_UNDERBARREL = MP.CREATE_UNDERBARREL
Server.CREATE_UNDERBARREL_ACK = MP.CREATE_UNDERBARREL_ACK
Server.REMOVE_UNDERBARREL = MP.REMOVE_UNDERBARREL
Server.SHOT_TTL_MS = 15000
Server.UNDERBARREL_CREATE_COOLDOWN_MS = 500
Server.LAUNCH_COOLDOWN_MS = 250
Server.UNDERBARREL_REMOVE_COOLDOWN_MS = 250
Server.DEBUG = getDebug()
Server._shots = Server._shots or {}
Server._nextUnderbarrelCreateAt = Server._nextUnderbarrelCreateAt or {}
Server._nextLaunchAt = Server._nextLaunchAt or {}
Server._nextUnderbarrelRemoveAt = Server._nextUnderbarrelRemoveAt or {}
Server._commandRatePruneAt = Server._commandRatePruneAt
    or Server._underbarrelRatePruneAt or 0

local WEAPON_CONFIGS = {
    ["Base.MFS_MTL30_cat"] = {
        payloadType = "MFS_Explosives.40mmExplosives",
    },
}
for _, definition in pairs(MFSUnderbarrelRegistry.LAUNCHERS) do
    WEAPON_CONFIGS[definition.pseudoType] = definition.ballistics
end
local MAX_FLAT_RANGE = 45
local MAX_START_OFFSET = 3

local function log(message)
    if Server.DEBUG then
        print("[MFSGrenadeServer] " .. tostring(message))
    end
end

local function isFinite(value)
    value = tonumber(value)
    return value and value == value and value ~= math.huge and value ~= -math.huge
end

local function validShotID(value)
    return type(value) == "string" and #value > 0 and #value <= 96
end

local function playerKey(player)
    local onlineID = -1
    pcall(function() onlineID = player:getOnlineID() end)
    return tostring(onlineID)
end

local function shotKey(player, shotID)
    return playerKey(player) .. ":" .. shotID
end

local function acceptPlayerCommandWindow(player, rateMap, cooldownMs)
    local now = getTimestampMs()
    local key = playerKey(player)
    if now < (rateMap[key] or 0) then return false end
    rateMap[key] = now + cooldownMs

    -- Bound stale per-player entries on long-running public servers.
    if now >= Server._commandRatePruneAt then
        Server._commandRatePruneAt = now + 60000
        local maps = {
            Server._nextUnderbarrelCreateAt,
            Server._nextLaunchAt,
            Server._nextUnderbarrelRemoveAt,
        }
        for _, map in ipairs(maps) do
            for playerID, nextAt in pairs(map) do
                if nextAt < now - 60000 then map[playerID] = nil end
            end
        end
    end
    return true
end

local function acceptUnderbarrelCreateWindow(player)
    -- MP PERFORMANCE/ABUSE SAFETY CONCERN:
    -- Creating launcher mode allocates a server item, replication packet and
    -- acknowledgement. Reject modified-client spam before validation or work.
    return acceptPlayerCommandWindow(player, Server._nextUnderbarrelCreateAt,
        Server.UNDERBARREL_CREATE_COOLDOWN_MS)
end

local function diagnosticValue(getter)
    local ok, value = pcall(getter)
    return ok and tostring(value) or "error"
end

local function logWeaponDiagnostics(label, weapon)
    if not Server.DEBUG or not weapon then return end
    log("[Audit] " .. tostring(label)
        .. " type=" .. tostring(weapon:getFullType())
        .. " id=" .. tostring(weapon:getID())
        .. " ammo=" .. diagnosticValue(function() return weapon:getCurrentAmmoCount() end)
        .. " chambered=" .. diagnosticValue(function() return weapon:isRoundChambered() end)
        .. " maxAmmo=" .. diagnosticValue(function() return weapon:getMaxAmmo() end)
        .. " weight=" .. diagnosticValue(function() return weapon:getWeight() end)
        .. " aimingTime=" .. diagnosticValue(function() return weapon:getAimingTime() end)
        .. " recoilDelay=" .. diagnosticValue(function() return weapon:getRecoilDelay() end)
        .. " reloadTime=" .. diagnosticValue(function() return weapon:getReloadTime() end))
end

local function pruneShots(now)
    for key, shot in pairs(Server._shots) do
        if now - shot.createdAt > Server.SHOT_TTL_MS then
            Server._shots[key] = nil
        end
    end
end

local function heldLauncherMatches(player, weaponType)
    local weapon = player and player:getPrimaryHandItem() or nil
    return weapon and weapon:getFullType() == weaponType
end

local function findItemRecursive(container, itemID, depth)
    if not container or not itemID or depth > 8 then return nil end
    local direct = container:getItemById(itemID)
    if direct then return direct end
    local items = container:getItems()
    for index = 0, items:size() - 1 do
        local item = items:get(index)
        if item and item:getID() == itemID then return item end
        if item and instanceof(item, "InventoryContainer") then
            local nested = findItemRecursive(item:getInventory(), itemID, depth + 1)
            if nested then return nested end
        end
    end
    return nil
end

local function findLinkedPseudo(container, hostID, definition, depth)
    if not container or depth > 8 then return nil end
    local items = container:getItems()
    for index = 0, items:size() - 1 do
        local item = items:get(index)
        if item then
            if item:getFullType() == definition.pseudoType
                and tonumber(item:getModData().MFSUnderbarrelHostID) == hostID then
                return item
            end
            if instanceof(item, "InventoryContainer") then
                local nested = findLinkedPseudo(item:getInventory(), hostID, definition, depth + 1)
                if nested then return nested end
            end
        end
    end
    return nil
end

local function underbarrelAck(player, args, accepted, reason, pseudo)
    sendServerCommand(player, Server.MODULE, Server.CREATE_UNDERBARREL_ACK, {
        protocolVersion = Server.VERSION,
        requestID = args and args.requestID or nil,
        hostID = args and args.hostID or nil,
        launcherName = args and args.launcherName or nil,
        accepted = accepted == true,
        reason = reason,
        pseudoID = pseudo and pseudo:getID() or nil,
    })
end

local function copyAuthoritativeVisualParts(host, pseudo)
    -- Client copyWeaponParts() has access to MFS's client-only visual-part
    -- patch and GunPos data. This server copy is intentionally separate: its
    -- native clones establish authoritative replicated part identity. Do not
    -- merge the two paths without re-proving observer rendering and stats.
    pseudo:setWeaponSprite(host:getWeaponSprite())
    local existingParts = pseudo:getAllWeaponParts()
    local existingSlots = {}
    if existingParts then
        for index = 0, existingParts:size() - 1 do
            local part = existingParts:get(index)
            if part and part:getPartType() then
                existingSlots[#existingSlots + 1] = part:getPartType()
            end
        end
    end
    for _, slot in ipairs(existingSlots) do pseudo:clearWeaponPart(slot) end

    local sourceParts = host:getAllWeaponParts()
    if not sourceParts then return 0 end
    local count = 0
    for index = 0, sourceParts:size() - 1 do
        local sourcePart = sourceParts:get(index)
        if sourcePart and sourcePart:getPartType() and sourcePart:getFullType() then
            local clone = instanceItem(sourcePart:getFullType())
            if clone and instanceof(clone, "WeaponPart") then
                pseudo:setWeaponPart(sourcePart:getPartType(), clone)
                count = count + 1
            end
        end
    end
    pseudo:getModData().MFSUnderbarrelNativePartCount = count
    logWeaponDiagnostics("server pseudo after visual clone", pseudo)
    return count
end

local function handleCreateUnderbarrel(player, args)
    if not player or type(args) ~= "table" or args.protocolVersion ~= Server.VERSION
        or type(args.requestID) ~= "string" or #args.requestID > 96
        or type(args.launcherName) ~= "string" then
        underbarrelAck(player, args, false, "invalid creation request")
        return false, "invalid creation request"
    end

    local definition = MFSUnderbarrelRegistry.LAUNCHERS[args.launcherName]
    local hostID = tonumber(args.hostID)
    local inventory = player:getInventory()
    local host = definition and hostID and findItemRecursive(inventory, hostID, 0) or nil
    if not host or MFSUnderbarrelRegistry.getForHost(host) ~= definition then
        underbarrelAck(player, args, false, "host or launcher attachment unavailable")
        return false, "host or launcher attachment unavailable"
    end

    -- Server authority owns creation because B42 timed actions resolve every
    -- InventoryItem field by server-side item ID. Client-created pseudo guns
    -- serialize as nil even if the client sends a generic container-add packet.
    local pseudo = findLinkedPseudo(inventory, hostID, definition, 0)
    if not pseudo then
        pseudo = instanceItem(definition.pseudoType)
        if not pseudo then
            underbarrelAck(player, args, false, "pseudo item unavailable")
            return false, "pseudo item unavailable"
        end
        local data = pseudo:getModData()
        data.MFSUnderbarrelHostID = hostID
        data.MFSUnderbarrelHostType = host:getFullType()
        data.MFSUnderbarrelLauncherName = definition.name
        if type(args.ammo) == "number" then
            pseudo:setCurrentAmmoCount(math.max(0, math.min(pseudo:getMaxAmmo(), args.ammo)))
        end
        if type(args.condition) == "number" then
            pseudo:setCondition(math.max(0, math.min(pseudo:getConditionMax(), args.condition)))
        end
        if type(args.jammed) == "boolean" then pseudo:setJammed(args.jammed) end
        copyAuthoritativeVisualParts(host, pseudo)
        inventory:AddItem(pseudo)
        sendAddItemToContainer(inventory, pseudo)
    else
        -- Recovery path: if an earlier client exit removed only its local copy,
        -- resend the still-authoritative item before acknowledging this entry.
        -- The requesting client has no linked local pseudo at this point.
        copyAuthoritativeVisualParts(host, pseudo)
        sendAddItemToContainer(inventory, pseudo)
    end
    underbarrelAck(player, args, true, nil, pseudo)
    return true
end

local function handleRemoveUnderbarrel(player, args)
    if not player or type(args) ~= "table" or args.protocolVersion ~= Server.VERSION then
        return false, "invalid removal request"
    end
    local pseudoID = tonumber(args.pseudoID)
    local hostID = tonumber(args.hostID)
    local inventory = player:getInventory()
    local pseudo = pseudoID and findItemRecursive(inventory, pseudoID, 0) or nil
    local definition = pseudo and MFSUnderbarrelRegistry.getForPseudo(pseudo) or nil
    if not pseudo or not definition
        or tonumber(pseudo:getModData().MFSUnderbarrelHostID) ~= hostID then
        return false, "pseudo unavailable"
    end
    local container = pseudo:getContainer()
    if not container then return false, "pseudo has no container" end
    container:Remove(pseudo)
    sendRemoveItemFromContainer(container, pseudo)
    return true
end

local function validateLaunch(player, args)
    if not player or type(args) ~= "table" or args.protocolVersion ~= Server.VERSION
        or not WEAPON_CONFIGS[args.weaponType] or not validShotID(args.shotID)
        or not heldLauncherMatches(player, args.weaponType) then
        return nil, "invalid launcher request"
    end

    local values = {}
    local names = { "startX", "startY", "startZ", "targetX", "targetY", "targetZ", "direction" }
    for _, name in ipairs(names) do
        if not isFinite(args[name]) then return nil, "invalid " .. name end
        values[name] = tonumber(args[name])
    end

    local startDistance = IsoUtils.DistanceTo(player:getX(), player:getY(), values.startX, values.startY)
    local flightDistance = IsoUtils.DistanceTo(values.startX, values.startY, values.targetX, values.targetY)
    if startDistance > MAX_START_OFFSET or math.abs(values.startZ - player:getZ()) > 2
        or flightDistance > MAX_FLAT_RANGE or math.abs(values.targetZ - values.startZ) > 32 then
        return nil, "launch geometry out of bounds"
    end

    values.protocolVersion = Server.VERSION
    values.shotID = args.shotID
    values.weaponType = args.weaponType
    values.hitWall = args.hitWall == true
    values.floorsDown = math.max(0, math.min(32, math.floor(tonumber(args.floorsDown) or 0)))
    values.shooterOnlineID = playerKey(player)
    return values
end

local function handleLaunch(player, args)
    local launch, reason = validateLaunch(player, args)
    if not launch then return false, reason end
    logWeaponDiagnostics("accepted launch arrival", player:getPrimaryHandItem())

    local now = getTimestampMs()
    pruneShots(now)
    local key = shotKey(player, launch.shotID)
    if Server._shots[key] then return false, "duplicate shot ID" end

    Server._shots[key] = {
        createdAt = now,
        startX = launch.startX,
        startY = launch.startY,
        targetX = launch.targetX,
        targetY = launch.targetY,
        weaponType = launch.weaponType,
        exploded = false,
    }
    sendServerCommand(Server.MODULE, Server.LAUNCH, launch)
    return true
end

local function handleTriggerExplosion(player, args)
    if not player or type(args) ~= "table" or args.protocolVersion ~= Server.VERSION
        or not WEAPON_CONFIGS[args.weaponType] or not validShotID(args.shotID) then
        return false, "invalid explosion request"
    end

    local key = shotKey(player, args.shotID)
    local shot = Server._shots[key]
    if not shot or shot.exploded then return false, "unknown or completed shot" end
    if args.weaponType ~= shot.weaponType then return false, "launcher type changed" end

    local x, y, z = tonumber(args.x), tonumber(args.y), tonumber(args.z)
    if not isFinite(x) or not isFinite(y) or not isFinite(z) then
        return false, "invalid impact coordinates"
    end
    x, y, z = math.floor(x), math.floor(y), math.floor(z)
    if IsoUtils.DistanceTo(shot.startX, shot.startY, x, y) > MAX_FLAT_RANGE
        or math.abs(z - player:getZ()) > 32 then
        return false, "impact geometry out of bounds"
    end

    local square = getCell():getGridSquare(x, y, z)
    if not square then return false, "impact square is not loaded" end
    local weaponConfig = WEAPON_CONFIGS[args.weaponType]
    local payload = instanceItem(weaponConfig.payloadType)
    if not payload then return false, "payload item is unavailable" end

    shot.exploded = true
    local trap = IsoTrap.new(player, payload, getCell(), square)
    trap:triggerExplosion(false)

    sendServerCommand(Server.MODULE, Server.EXPLOSION, {
        protocolVersion = Server.VERSION,
        shotID = args.shotID,
        weaponType = shot.weaponType,
        x = x + 0.5,
        y = y + 0.5,
        z = z,
        shooterOnlineID = playerKey(player),
    })
    return true
end

local function onClientCommand(module, command, player, args)
    if module ~= Server.MODULE then return end

    local ok, accepted, reason
    if command == Server.LAUNCH then
        -- Bound the custom 1:N launch broadcast independently of client fire
        -- controls. The 250 ms window remains below the fastest supported
        -- launcher's 500 ms MinimumSwingTime.
        if not acceptPlayerCommandWindow(player, Server._nextLaunchAt,
            Server.LAUNCH_COOLDOWN_MS) then return end
        ok, accepted, reason = pcall(handleLaunch, player, args)
    elseif command == Server.TRIGGER_EXPLOSION then
        ok, accepted, reason = pcall(handleTriggerExplosion, player, args)
    elseif command == Server.CREATE_UNDERBARREL then
        if not acceptUnderbarrelCreateWindow(player) then return end
        ok, accepted, reason = pcall(handleCreateUnderbarrel, player, args)
    elseif command == Server.REMOVE_UNDERBARREL then
        -- Use a separate window from CREATE so a legitimate immediate exit is
        -- never rejected by the creation cooldown. Apply it before the depth-8
        -- inventory lookup to prevent malformed-ID traversal spam.
        if not acceptPlayerCommandWindow(player, Server._nextUnderbarrelRemoveAt,
            Server.UNDERBARREL_REMOVE_COOLDOWN_MS) then return end
        ok, accepted, reason = pcall(handleRemoveUnderbarrel, player, args)
    else
        return
    end

    if not ok then
        if command == Server.CREATE_UNDERBARREL then
            underbarrelAck(player, args, false, "server creation exception")
        end
        log(command .. " failed: " .. tostring(accepted))
    elseif not accepted then
        log(command .. " rejected: " .. tostring(reason))
    end
end

if Server._registeredCallback then
    Events.OnClientCommand.Remove(Server._registeredCallback)
end
Server._registeredCallback = onClientCommand
Events.OnClientCommand.Add(onClientCommand)

-- Always emit one startup line so coop-console can prove the authoritative
-- handler loaded even when debug logging is disabled.
print("[MFSGrenadeServer] version " .. Server.VERSION
    .. " loaded; server-authoritative underbarrel pseudo creation enabled; createRate="
    .. tostring(Server.UNDERBARREL_CREATE_COOLDOWN_MS) .. "ms; launchRate="
    .. tostring(Server.LAUNCH_COOLDOWN_MS) .. "ms; removeRate="
    .. tostring(Server.UNDERBARREL_REMOVE_COOLDOWN_MS) .. "ms")

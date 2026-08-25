-- Copyright (c) 2026 ReapBone. All rights reserved.

require "BClan/BClan_Config"

if isClient() then return end

local MODULE = BClan.Config.NetworkModule
local DATA_KEY = BClan.Config.DataKey
local runtimeKills = {}
local pendingCreates = {}
local pendingLeaves = {}
local data = nil
local nextAutoSaveAt = nil

local function log(message)
    print("[BClan] " .. tostring(message))
end

local function getData()
    if data then return data end
    if not ModData.exists(DATA_KEY) then
        ModData.add(DATA_KEY, {})
    end
    data = ModData.get(DATA_KEY)
    data.version = BClan.Config.Version
    data.clans = data.clans or {}
    data.invites = data.invites or {}
    return data
end

local function transmit()
    ModData.transmit(DATA_KEY)
end

local function notice(player, key)
    if player then
        sendServerCommand(player, MODULE, "Notice", { key = key })
    end
end

local function isAdmin(player)
    if not player then return false end
    local ok, result = pcall(function()
        return player:getRole():hasCapability(Capability.FactionCheat)
    end)
    return ok and result == true
end

local function factionFor(player)
    if not player then return nil end
    return Faction.getPlayerFaction(player)
end

local function memberCount(faction)
    if not faction then return 0 end
    return faction:getPlayers():size() + 1
end

local function hasMember(faction, username)
    if not faction or not username then return false end
    return faction:isOwner(username) or faction:isMember(username)
end

local function makeClanEntry(faction)
    local now = 0
    if getGameTime() then now = getGameTime():getWorldAgeHours() end
    local color = faction:getTagColor()
    local entry = {
        level = 1,
        xp = 0,
        totalXP = 0,
        friendlyFire = false,
        allies = {},
        pendingAllies = {},
        pendingJoins = {},
        knownMembers = {},
        tagColor = { r = color:getR(), g = color:getG(), b = color:getB() },
        createdAt = now,
    }
    entry.knownMembers[faction:getOwner()] = true
    for i = 0, faction:getPlayers():size() - 1 do
        entry.knownMembers[faction:getPlayers():get(i)] = true
    end
    return entry
end

local function spentXPForLevel(level)
    local total = 0
    for current = 1, math.max(1, tonumber(level) or 1) - 1 do
        total = total + BClan.xpForLevel(current)
    end
    return total
end

local function ensureClan(faction)
    if not faction then return nil end
    local store = getData()
    local name = faction:getName()
    local entry = store.clans[name]
    if not entry then
        entry = makeClanEntry(faction)
        store.clans[name] = entry
        log("Registered native faction: " .. name)
    end
    entry.level = math.max(1, math.min(BClan.Config.MaxLevel, tonumber(entry.level) or 1))
    entry.xp = math.max(0, tonumber(entry.xp) or 0)
    if entry.totalXP == nil then
        entry.totalXP = spentXPForLevel(entry.level) + entry.xp
    else
        entry.totalXP = math.max(0, tonumber(entry.totalXP) or 0)
    end
    entry.friendlyFire = entry.friendlyFire == true
    entry.allies = entry.allies or {}
    entry.pendingAllies = entry.pendingAllies or {}
    entry.pendingJoins = entry.pendingJoins or {}
    entry.knownMembers = entry.knownMembers or {}
    if not entry.tagColor then
        local color = faction:getTagColor()
        entry.tagColor = { r = color:getR(), g = color:getG(), b = color:getB() }
    end
    entry.knownMembers[faction:getOwner()] = true
    return entry
end

local function pendingJoinCount(entry)
    local count = 0
    for _, enabled in pairs(entry.pendingJoins or {}) do
        if enabled == true then count = count + 1 end
    end
    return count
end

local function clanByName(name)
    if type(name) ~= "string" then return nil, nil end
    local faction = Faction.getFaction(name)
    if not faction then return nil, nil end
    return faction, ensureClan(faction)
end

local function playerByUsername(username)
    local players = getOnlinePlayers()
    for i = 0, players:size() - 1 do
        local candidate = players:get(i)
        if candidate and candidate:getUsername() == username then return candidate end
    end
    return nil
end

local function isOwnerOrAdmin(player, faction)
    return player and faction and (faction:isOwner(player:getUsername()) or isAdmin(player))
end

local function addXP(faction, amount)
    if not faction or not amount or amount <= 0 then return false end
    local entry = ensureClan(faction)
    if not entry then return false end

    entry.xp = entry.xp + amount
    entry.totalXP = entry.totalXP + amount
    local leveled = false
    while entry.level < BClan.Config.MaxLevel do
        local needed = BClan.xpForLevel(entry.level)
        if needed <= 0 or entry.xp < needed then break end
        entry.xp = entry.xp - needed
        entry.level = entry.level + 1
        leveled = true
    end
    if leveled then
        sendServerCommand(MODULE, "ClanLeveled", { clan = faction:getName(), level = entry.level })
        log(faction:getName() .. " reached level " .. tostring(entry.level))
    end
    return true
end

local function queueClanXP(queue, faction, amount)
    amount = tonumber(amount) or 0
    if not faction or amount <= 0 then return end
    local name = faction:getName()
    local queued = queue[name]
    if queued then
        queued.amount = queued.amount + amount
    else
        queue[name] = { faction = faction, amount = amount }
    end
end

local function applyQueuedXP(queue)
    local changed = false
    for _, queued in pairs(queue) do
        changed = addXP(queued.faction, queued.amount) or changed
    end
    return changed
end

local function setFactionFriendlyFire(faction, enabled)
    local players = getOnlinePlayers()
    for i = 0, players:size() - 1 do
        local member = players:get(i)
        if member and hasMember(faction, member:getUsername()) then
            member:setFactionPvp(enabled)
            pcall(function() sendFactionStatsChange(member) end)
            sendServerCommand(member, MODULE, "SetFactionPvp", { enabled = enabled })
        end
    end
end

local function areProtected(attackerName, targetName)
    local attackerFaction = Faction.getPlayerFaction(attackerName)
    local targetFaction = Faction.getPlayerFaction(targetName)
    if not attackerFaction or not targetFaction then return false end

    if attackerFaction:getName() == targetFaction:getName() then
        local entry = ensureClan(attackerFaction)
        return entry.friendlyFire ~= true
    end

    local entry = ensureClan(attackerFaction)
    return entry.allies[targetFaction:getName()] == true
end

local function commandCreate(player, args)
    if factionFor(player) then return notice(player, "BClan_Notice_AlreadyInClan") end
    local name = BClan.trim(args and args.name)
    local tag = BClan.trim(args and args.tag):upper()
    if not BClan.isValidName(name) then return notice(player, "BClan_Notice_InvalidName") end
    if not BClan.isValidTag(tag) then return notice(player, "BClan_Notice_InvalidTag") end
    if Faction.factionExist(name) then return notice(player, "BClan_Notice_NameUsed") end
    if Faction.tagExist(tag) then return notice(player, "BClan_Notice_TagUsed") end
    for username, pending in pairs(pendingCreates) do
        if username ~= player:getUsername() and pending.name == name then return notice(player, "BClan_Notice_NameUsed") end
        if username ~= player:getUsername() and pending.tag == tag then return notice(player, "BClan_Notice_TagUsed") end
    end
    if not Faction.canCreateFaction(player) then return notice(player, "BClan_Notice_FactionDisabled") end

    pendingCreates[player:getUsername()] = { name = name, tag = tag, createdAt = getTimestampMs() }
    sendServerCommand(player, MODULE, "ApplyCreate", { name = name, tag = tag })
end

local function commandCancelCreate(player)
    pendingCreates[player:getUsername()] = nil
end

local function commandFinalizeCreate(player, args)
    local pending = pendingCreates[player:getUsername()]
    local faction = factionFor(player)
    if not pending or not faction or faction:getName() ~= pending.name then
        return notice(player, "BClan_Notice_Failed")
    end
    pendingCreates[player:getUsername()] = nil
    local entry = ensureClan(faction)
    entry.knownMembers[player:getUsername()] = true
    setFactionFriendlyFire(faction, false)
    transmit()
    notice(player, "BClan_Notice_Created")
end

local function commandInvite(player, args)
    local faction = factionFor(player)
    if not isOwnerOrAdmin(player, faction) then return notice(player, "BClan_Notice_NotOwner") end
    local entry = ensureClan(faction)
    if memberCount(faction) >= BClan.memberLimit(entry.level) then
        return notice(player, "BClan_Notice_ClanFull")
    end
    local username = BClan.safeUsername(args and args.username)
    if not username or username == player:getUsername() then return notice(player, "BClan_Notice_InvalidPlayer") end
    local target = playerByUsername(username)
    if not target then return notice(player, "BClan_Notice_PlayerOffline") end
    if Faction.isAlreadyInFaction(username) then return notice(player, "BClan_Notice_PlayerHasClan") end

    getData().invites[username] = faction:getName()
    transmit()
    sendServerCommand(player, MODULE, "ApplyNativeInvite", {
        clan = faction:getName(),
        owner = faction:getOwner(),
        username = username,
    })
    sendServerCommand(target, MODULE, "InviteReceived", { clan = faction:getName(), owner = faction:getOwner() })
    notice(player, "BClan_Notice_InviteSent")
end

local function commandRespondInvite(player, args)
    local store = getData()
    local username = player:getUsername()
    local clanName = store.invites[username]
    if not clanName then return notice(player, "BClan_Notice_InviteMissing") end
    local faction, entry = clanByName(clanName)

    if args and args.accept == true then
        if factionFor(player) then
            transmit()
            return notice(player, "BClan_Notice_AlreadyInClan")
        end
        if not faction then
            transmit()
            return notice(player, "BClan_Notice_InviteMissing")
        end
        local alreadyPending = entry.pendingJoins[username] == true
        local reservedSlots = pendingJoinCount(entry) - (alreadyPending and 1 or 0)
        if memberCount(faction) + reservedSlots >= BClan.memberLimit(entry.level) then
            transmit()
            return notice(player, "BClan_Notice_ClanFull")
        end
        entry.pendingJoins[username] = true
        sendServerCommand(player, MODULE, "ApplyJoin", {
            accept = true,
            clan = clanName,
            owner = faction:getOwner(),
            username = username,
        })
    else
        store.invites[username] = nil
        if faction then
            sendServerCommand(player, MODULE, "ApplyJoin", {
                accept = false,
                clan = clanName,
                owner = faction:getOwner(),
                username = username,
            })
        end
        notice(player, "BClan_Notice_InviteDeclined")
    end
    transmit()
end

local function commandFinalizeJoin(player, args)
    local faction = factionFor(player)
    if not faction then return notice(player, "BClan_Notice_Failed") end
    local entry = ensureClan(faction)
    local username = player:getUsername()
    if entry.pendingJoins[username] ~= true then return notice(player, "BClan_Notice_Failed") end
    entry.pendingJoins[username] = nil
    entry.knownMembers[username] = true
    getData().invites[username] = nil
    setFactionFriendlyFire(faction, entry.friendlyFire)
    transmit()
end

local function commandRemoveMember(player, args)
    local faction = factionFor(player)
    if not isOwnerOrAdmin(player, faction) then return notice(player, "BClan_Notice_NotOwner") end
    local username = BClan.safeUsername(args and args.username)
    if not username or faction:isOwner(username) or not faction:isMember(username) then
        return notice(player, "BClan_Notice_InvalidPlayer")
    end
    local entry = ensureClan(faction)
    sendServerCommand(player, MODULE, "ApplyRemove", { clan = faction:getName(), username = username })
    notice(player, "BClan_Notice_MemberRemoved")
end

local function commandLeave(player)
    local faction = factionFor(player)
    if not faction then return notice(player, "BClan_Notice_NoClan") end
    if faction:isOwner(player:getUsername()) then return notice(player, "BClan_Notice_OwnerCannotLeave") end
    ensureClan(faction)
    pendingLeaves[player:getUsername()] = faction:getName()
    sendServerCommand(player, MODULE, "ApplyLeave", { clan = faction:getName(), username = player:getUsername() })
end

local function commandFinalizeLeave(player, args)
    local username = player:getUsername()
    local clanName = pendingLeaves[username]
    if not clanName or (args and args.clan ~= clanName) then
        return notice(player, "BClan_Notice_Failed")
    end
    local faction, entry = clanByName(clanName)
    if faction and hasMember(faction, username) then
        return notice(player, "BClan_Notice_Failed")
    end
    pendingLeaves[username] = nil
    if entry then entry.knownMembers[username] = nil end
    transmit()
    notice(player, "BClan_Notice_LeftClan")
end

local function commandSetPvp(player, args)
    local faction = factionFor(player)
    if not isOwnerOrAdmin(player, faction) then return notice(player, "BClan_Notice_NotOwner") end
    local entry = ensureClan(faction)
    entry.friendlyFire = args and args.enabled == true
    setFactionFriendlyFire(faction, entry.friendlyFire)
    transmit()
    notice(player, entry.friendlyFire and "BClan_Notice_PvpEnabled" or "BClan_Notice_PvpDisabled")
end

local function commandSetTagColor(player, args)
    local faction = factionFor(player)
    if not isOwnerOrAdmin(player, faction) then return notice(player, "BClan_Notice_NotOwner") end

    local r = tonumber(args and args.r)
    local g = tonumber(args and args.g)
    local b = tonumber(args and args.b)
    if not r or not g or not b then return notice(player, "BClan_Notice_Failed") end

    r = math.max(0, math.min(1, r))
    g = math.max(0, math.min(1, g))
    b = math.max(0, math.min(1, b))
    local entry = ensureClan(faction)
    entry.tagColor = { r = r, g = g, b = b }
    transmit()
    sendServerCommand(player, MODULE, "ApplyTagColor", { r = r, g = g, b = b })
    notice(player, "BClan_Notice_TagColorChanged")
end

local function commandRequestAlly(player, args)
    local faction = factionFor(player)
    if not isOwnerOrAdmin(player, faction) then return notice(player, "BClan_Notice_NotOwner") end
    local targetName = BClan.trim(args and args.clan)
    local targetFaction, targetEntry = clanByName(targetName)
    if not targetFaction or targetFaction:getName() == faction:getName() then
        return notice(player, "BClan_Notice_InvalidClan")
    end
    local entry = ensureClan(faction)
    if entry.allies[targetFaction:getName()] then return notice(player, "BClan_Notice_AlreadyAllied") end
    targetEntry.pendingAllies[faction:getName()] = true
    transmit()
    local owner = playerByUsername(targetFaction:getOwner())
    if owner then sendServerCommand(owner, MODULE, "AllyRequest", { clan = faction:getName() }) end
    notice(player, "BClan_Notice_AllyRequestSent")
end

local function commandRespondAlly(player, args)
    local faction = factionFor(player)
    if not isOwnerOrAdmin(player, faction) then return notice(player, "BClan_Notice_NotOwner") end
    local fromName = BClan.trim(args and args.clan)
    local entry = ensureClan(faction)
    if entry.pendingAllies[fromName] ~= true then return notice(player, "BClan_Notice_RequestMissing") end
    entry.pendingAllies[fromName] = nil

    if args and args.accept == true then
        local otherFaction, otherEntry = clanByName(fromName)
        if not otherFaction then
            transmit()
            return notice(player, "BClan_Notice_InvalidClan")
        end
        entry.allies[fromName] = true
        otherEntry.allies[faction:getName()] = true
        notice(player, "BClan_Notice_AllyAccepted")
    else
        notice(player, "BClan_Notice_AllyDeclined")
    end
    transmit()
end

local function commandRemoveAlly(player, args)
    local faction = factionFor(player)
    if not isOwnerOrAdmin(player, faction) then return notice(player, "BClan_Notice_NotOwner") end
    local allyName = BClan.trim(args and args.clan)
    local entry = ensureClan(faction)
    if entry.allies[allyName] ~= true then return notice(player, "BClan_Notice_InvalidClan") end
    entry.allies[allyName] = nil
    local _, otherEntry = clanByName(allyName)
    if otherEntry then otherEntry.allies[faction:getName()] = nil end
    transmit()
    notice(player, "BClan_Notice_AllyRemoved")
end

local function commandRestoreProtectedHit(player, args)
    local attacker = BClan.safeUsername(args and args.attacker)
    local target = BClan.safeUsername(args and args.target)
    local amount = math.max(0, math.min(100, tonumber(args and args.damage) or 0))
    if not attacker or not target or amount <= 0 then return end
    if player:getUsername() ~= attacker then return end
    if not areProtected(attacker, target) then return end
    local targetPlayer = playerByUsername(target)
    if not targetPlayer then return end
    local body = targetPlayer:getBodyDamage()
    if body then body:AddGeneralHealth(amount) end
    sendServerCommand(targetPlayer, MODULE, "RestoreHealth", { amount = amount })
end

local commands = {
    Create = commandCreate,
    CancelCreate = commandCancelCreate,
    FinalizeCreate = commandFinalizeCreate,
    Invite = commandInvite,
    RespondInvite = commandRespondInvite,
    FinalizeJoin = commandFinalizeJoin,
    RemoveMember = commandRemoveMember,
    Leave = commandLeave,
    FinalizeLeave = commandFinalizeLeave,
    SetPvp = commandSetPvp,
    SetTagColor = commandSetTagColor,
    RequestAlly = commandRequestAlly,
    RespondAlly = commandRespondAlly,
    RemoveAlly = commandRemoveAlly,
    RestoreProtectedHit = commandRestoreProtectedHit,
}

local function onClientCommand(module, command, player, args)
    if module ~= MODULE then return end
    if command == "RequestData" then
        ModData.transmit(DATA_KEY)
        return
    end
    local handler = commands[command]
    if handler then
        local ok, err = pcall(handler, player, args or {})
        if not ok then
            log("Command " .. tostring(command) .. " failed: " .. tostring(err))
            notice(player, "BClan_Notice_Failed")
        end
    end
end

local function updateProgression()
    local players = getOnlinePlayers()
    local online = {}
    local xpQueue = {}
    for i = 0, players:size() - 1 do
        local player = players:get(i)
        if player then
            local username = player:getUsername()
            online[username] = true
            local kills = math.max(0, tonumber(player:getZombieKills()) or 0)
            if runtimeKills[username] == nil then
                runtimeKills[username] = kills
            elseif kills > runtimeKills[username] then
                local faction = factionFor(player)
                if faction then
                    queueClanXP(xpQueue, faction, (kills - runtimeKills[username]) * BClan.Config.ZombieKillXP)
                end
                runtimeKills[username] = kills
            elseif kills < runtimeKills[username] then
                runtimeKills[username] = kills
            end
        end
    end
    for username, _ in pairs(runtimeKills) do
        if not online[username] then runtimeKills[username] = nil end
    end
    if applyQueuedXP(xpQueue) then transmit() end
end

local function awardSurvivalHour()
    local players = getOnlinePlayers()
    local xpQueue = {}
    for i = 0, players:size() - 1 do
        local faction = factionFor(players:get(i))
        queueClanXP(xpQueue, faction, BClan.Config.SurvivalHourXP)
    end
    if applyQueuedXP(xpQueue) then transmit() end
end

local function reconcileFactions()
    local now = getTimestampMs()
    for username, pending in pairs(pendingCreates) do
        if not playerByUsername(username) or now - (tonumber(pending.createdAt) or now) > 60000 then
            pendingCreates[username] = nil
        end
    end
    local factions = Faction.getFactions()
    local changed = false
    for i = 0, factions:size() - 1 do
        local faction = factions:get(i)
        local existing = getData().clans[faction:getName()]
        if not existing then changed = true end
        local entry = ensureClan(faction)
        local present = { [faction:getOwner()] = true }
        for p = 0, faction:getPlayers():size() - 1 do
            present[faction:getPlayers():get(p)] = true
        end
        for username, enabled in pairs(entry.pendingJoins) do
            if enabled == true and hasMember(faction, username) then
                entry.pendingJoins[username] = nil
                entry.knownMembers[username] = true
                getData().invites[username] = nil
                changed = true
            end
        end
        for username, _ in pairs(entry.knownMembers) do
            if not present[username] then
                entry.knownMembers[username] = nil
                changed = true
            end
        end
        for username, clanName in pairs(getData().invites) do
            if clanName == faction:getName() and present[username] then
                entry.knownMembers[username] = true
                entry.pendingJoins[username] = nil
                getData().invites[username] = nil
                changed = true
            end
        end
        for username, clanName in pairs(pendingLeaves) do
            if clanName == faction:getName() and not present[username] then
                pendingLeaves[username] = nil
                entry.knownMembers[username] = nil
                changed = true
            end
        end
        local limit = BClan.memberLimit(entry.level)
        if memberCount(faction) > limit then
            local owner = playerByUsername(faction:getOwner())
            for p = faction:getPlayers():size() - 1, 0, -1 do
                local username = faction:getPlayers():get(p)
                if entry.knownMembers[username] ~= true then
                    local unauthorized = playerByUsername(username)
                    if owner then
                        sendServerCommand(owner, MODULE, "ApplyRemove", { clan = faction:getName(), username = username })
                    elseif unauthorized then
                        pendingLeaves[username] = faction:getName()
                        sendServerCommand(unauthorized, MODULE, "ApplyLeave", { clan = faction:getName(), username = username })
                    end
                    changed = true
                    if memberCount(faction) <= limit then break end
                end
            end
        end
    end
    if changed then transmit() end
end

local function autoSave()
    local interval = math.max(1, tonumber(BClan.Config.AutoSaveMinutes) or 15) * 60 * 1000
    local now = getTimestampMs()
    if not nextAutoSaveAt then
        nextAutoSaveAt = now + interval
        return
    end
    if now < nextAutoSaveAt then return end
    nextAutoSaveAt = now + interval
    local ok, err = pcall(saveGame)
    if ok then
        log("Clan and world data saved automatically")
    else
        log("Automatic save failed: " .. tostring(err))
    end
end

local function onInitGlobalModData()
    getData()
    reconcileFactions()
    nextAutoSaveAt = getTimestampMs() + (math.max(1, tonumber(BClan.Config.AutoSaveMinutes) or 15) * 60 * 1000)
    log("Server data initialized")
end

Events.OnInitGlobalModData.Add(onInitGlobalModData)
Events.OnClientCommand.Add(onClientCommand)
Events.EveryOneMinute.Add(updateProgression)
Events.EveryOneMinute.Add(autoSave)
Events.EveryHours.Add(awardSurvivalHour)
Events.EveryTenMinutes.Add(reconcileFactions)

-- Copyright (c) 2026 ReapBone. All rights reserved.

require "ISUI/ISEquippedItem"
require "BClan/BClan_Config"
require "BClan/BClan_Language"
require "BClan/BClan_UI"

if not isClient() then return end

BClan.ClientData = BClan.ClientData or nil
BClan.reopenWindow = false
local protectedTargets = {}
local protectionTick = 0
local protectionRefreshTicks = math.max(15, tonumber(BClan.Config.ProtectionRefreshTicks) or 60)
local pendingCreate = nil
local pendingJoin = nil
local pendingLeave = nil

local MODULE = BClan.Config.NetworkModule
local DATA_KEY = BClan.Config.DataKey
local oldInitialise = ISEquippedItem.initialise

local function showNotice(key)
    local player = getPlayer()
    if player and key then player:setHaloNote(BClan.text(key), 255, 210, 80, 300) end
end

local function onClanButton()
    BClanUI.toggle()
end

function ISEquippedItem:initialise()
    oldInitialise(self)
    if self.chr:getPlayerNum() ~= 0 or self.bclanBtn then return end
    local size = math.max(42, self:getWidth())
    local icon = getTexture("media/ui/BClan/clan_icon.png")
    self.bclanBtn = ISButton:new(0, self:getHeight() + 15, size, size * 0.75, "", nil, onClanButton)
    self.bclanBtn:setImage(icon)
    self.bclanBtn:initialise()
    self.bclanBtn:instantiate()
    self.bclanBtn:setDisplayBackground(false)
    self.bclanBtn:ignoreWidthChange()
    self.bclanBtn:ignoreHeightChange()
    self:addChild(self.bclanBtn)
    self:addMouseOverToolTipItem(self.bclanBtn, BClan.text("BClan_Tooltip"))
    self:shrinkWrap()
end

local function onReceiveGlobalModData(key, received)
    if key ~= DATA_KEY then return end
    BClan.ClientData = received or { clans = {}, invites = {} }
    if BClanUI.instance then BClanUI.instance:refreshLists() end
    if BClanRankingUI and BClanRankingUI.instance then BClanRankingUI.instance:refreshList() end
end

local function onServerCommand(module, command, args)
    if module ~= MODULE then return end
    args = args or {}
    if command == "Notice" then
        showNotice(args.key)
    elseif command == "ApplyCreate" then
        local player = getPlayer()
        if player and not Faction.getPlayerFaction(player) then
            pendingCreate = { name = args.name, tag = args.tag, startedAt = getTimestampMs() }
            sendFactionCreate(args.name, player:getUsername())
        end
    elseif command == "ApplyNativeInvite" then
        local player = getPlayer()
        local faction = player and Faction.getPlayerFaction(player) or nil
        if faction and faction:getName() == args.clan and faction:isOwner(player:getUsername()) then
            sendFactionInvite(faction, player:getUsername(), args.username)
        end
    elseif command == "ApplyJoin" then
        local faction = Faction.getFaction(args.clan)
        local player = getPlayer()
        if faction and player then
            local accepted = args.accept == true
            if accepted and not Faction.getPlayerFaction(player) then
                pendingJoin = { clan = args.clan, startedAt = getTimestampMs() }
            end
            acceptFactionInvite(faction, args.owner, player:getUsername(), accepted)
        end
    elseif command == "ApplyRemove" then
        local faction = Faction.getFaction(args.clan)
        if faction and faction:isMember(args.username) then
            sendFactionRemoveMember(faction, args.username)
        end
    elseif command == "ApplyLeave" then
        local faction = Faction.getFaction(args.clan)
        local player = getPlayer()
        if faction and player and faction:isMember(player:getUsername()) then
            pendingLeave = { clan = args.clan, startedAt = getTimestampMs() }
            sendFactionRemoveMember(faction, player:getUsername())
        end
    elseif command == "InviteReceived" then
        showNotice("BClan_Notice_InviteReceived")
        ModData.request(DATA_KEY)
    elseif command == "AllyRequest" then
        showNotice("BClan_Notice_AllyRequestReceived")
        ModData.request(DATA_KEY)
    elseif command == "SetFactionPvp" then
        local player = getPlayer()
        if player then
            player:setFactionPvp(args.enabled == true)
            sendFactionStatsChange(player)
        end
    elseif command == "ApplyTagColor" then
        local player = getPlayer()
        local faction = player and Faction.getPlayerFaction(player) or nil
        if faction then
            local r = math.max(0, math.min(1, tonumber(args.r) or 0.72))
            local g = math.max(0, math.min(1, tonumber(args.g) or 0.16))
            local b = math.max(0, math.min(1, tonumber(args.b) or 0.14))
            faction:setTagColor(ColorInfo.new(r, g, b, 1))
            sendFactionChangeTag(faction)
            if BClanUI.instance then BClanUI.instance:refreshLists() end
        end
    elseif command == "RestoreHealth" then
        local player = getPlayer()
        local amount = math.max(0, math.min(100, tonumber(args.amount) or 0))
        if player and amount > 0 and player:getBodyDamage() then
            player:getBodyDamage():AddGeneralHealth(amount)
        end
    elseif command == "ClanLeveled" then
        local player = getPlayer()
        local faction = player and Faction.getPlayerFaction(player) or nil
        if faction and faction:getName() == args.clan then
            player:setHaloNote(BClan.text("BClan_Notice_LevelUp") .. " " .. tostring(args.level), 255, 190, 40, 400)
        end
    end
end

local function finalizeNativeActions()
    local player = getPlayer()
    if not player then return end
    local faction = Faction.getPlayerFaction(player)
    local now = getTimestampMs()

    if pendingCreate then
        if faction and faction:getName() == pendingCreate.name then
            faction:setTag(pendingCreate.tag)
            faction:setTagColor(ColorInfo.new(0.72, 0.19, 0.19, 1))
            sendFactionChangeTag(faction)
            player:setShowTag(true)
            sendFactionStatsChange(player)
            sendClientCommand(player, MODULE, "FinalizeCreate", { name = pendingCreate.name })
            pendingCreate = nil
        elseif now - pendingCreate.startedAt > 30000 then
            pendingCreate = nil
            sendClientCommand(player, MODULE, "CancelCreate", {})
            showNotice("BClan_Notice_Failed")
        end
    end

    if pendingJoin then
        if faction and faction:getName() == pendingJoin.clan then
            player:setShowTag(true)
            sendFactionStatsChange(player)
            sendClientCommand(player, MODULE, "FinalizeJoin", { clan = pendingJoin.clan })
            showNotice("BClan_Notice_InviteAccepted")
            pendingJoin = nil
        elseif now - pendingJoin.startedAt > 30000 then
            pendingJoin = nil
            showNotice("BClan_Notice_Failed")
        end
    end

    if pendingLeave then
        if not faction or faction:getName() ~= pendingLeave.clan then
            sendClientCommand(player, MODULE, "FinalizeLeave", { clan = pendingLeave.clan })
            pendingLeave = nil
        elseif now - pendingLeave.startedAt > 30000 then
            pendingLeave = nil
            showNotice("BClan_Notice_Failed")
        end
    end
end

local function isProtectedFactions(attackerFaction, targetFaction, clans)
    if not attackerFaction or not targetFaction then return false end

    local attackerClan = attackerFaction:getName()
    local targetClan = targetFaction:getName()
    local entry = clans and clans[attackerClan] or nil
    if attackerClan == targetClan then
        return not entry or entry.friendlyFire ~= true
    end
    return entry and entry.allies and entry.allies[targetClan] == true
end

local function onWeaponHitCharacter(attacker, target, weapon, damage)
    if not attacker or not target or not attacker.getUsername or not target.getUsername then return end
    local attackerName = attacker:getUsername()
    local targetName = target:getUsername()
    if attackerName == targetName then return end
    local clans = BClan.ClientData and BClan.ClientData.clans or nil
    local protected = isProtectedFactions(Faction.getPlayerFaction(attacker), Faction.getPlayerFaction(target), clans)
    if protected then
        sendClientCommand(attacker, MODULE, "RestoreProtectedHit", {
            attacker = attackerName,
            target = targetName,
            damage = tonumber(damage) or 0,
        })
    end
end

local function refreshProtectedTargets()
    local localPlayer = getPlayer()
    if not localPlayer then return end
    local localName = localPlayer:getUsername()
    local localFaction = Faction.getPlayerFaction(localPlayer)
    local clans = BClan.ClientData and BClan.ClientData.clans or nil
    local seen = {}
    local players = getOnlinePlayers()
    for i = 0, players:size() - 1 do
        local other = players:get(i)
        if other and other:getUsername() ~= localName then
            local username = other:getUsername()
            seen[username] = true
            local protected = isProtectedFactions(localFaction, Faction.getPlayerFaction(other), clans)
            if protected then
                other:setAvoidDamage(true)
                protectedTargets[username] = other
            elseif protectedTargets[username] then
                pcall(function() other:setAvoidDamage(false) end)
                protectedTargets[username] = nil
            end
        end
    end
    for username, object in pairs(protectedTargets) do
        if not seen[username] then
            pcall(function() object:setAvoidDamage(false) end)
            protectedTargets[username] = nil
        end
    end
end

local function onCreatePlayer()
    ModData.request(DATA_KEY)
end

local function onTick()
    if pendingCreate or pendingJoin or pendingLeave then
        finalizeNativeActions()
    end
    protectionTick = protectionTick + 1
    if protectionTick >= protectionRefreshTicks then
        protectionTick = 0
        refreshProtectedTargets()
    end
    if BClan.reopenWindow then
        BClan.reopenWindow = false
        BClanUI.toggle()
    end
end

Events.OnReceiveGlobalModData.Add(onReceiveGlobalModData)
Events.OnServerCommand.Add(onServerCommand)
Events.OnWeaponHitCharacter.Add(onWeaponHitCharacter)
Events.OnWeaponSwing.Add(refreshProtectedTargets)
Events.OnCreatePlayer.Add(onCreatePlayer)
Events.OnTick.Add(onTick)
Events.SyncFaction.Add(finalizeNativeActions)

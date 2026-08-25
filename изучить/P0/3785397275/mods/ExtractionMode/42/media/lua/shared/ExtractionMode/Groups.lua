require "ExtractionMode/Util"

ExtractionMode = ExtractionMode or {}

local Util = ExtractionMode.Util
local Groups = {}

local function playerFaction(player, username)
    if player == nil or Faction == nil or Faction.getPlayerFaction == nil then return nil end
    local ok, faction = pcall(function() return Faction.getPlayerFaction(player) end)
    if ok and faction ~= nil then return faction end

    -- Keep a username fallback for dedicated-server builds whose exposed Java
    -- overload accepts the account name instead of the IsoPlayer instance.
    ok, faction = pcall(function() return Faction.getPlayerFaction(username) end)
    return ok and faction or nil
end

local function factionDetails(faction)
    local ok, name = pcall(function() return faction:getName() end)
    name = ok and tostring(name or "") or ""
    if name == "" then return nil end

    local owner = ""
    ok, owner = pcall(function() return faction:getOwner() end)
    owner = ok and tostring(owner or "") or ""
    local members = {}
    if owner ~= "" then members[owner] = true end
    pcall(function()
        local players = faction:getPlayers()
        for index = 0, players:size() - 1 do
            members[tostring(players:get(index))] = true
        end
    end)
    return { name = name, owner = owner, members = members }
end

local function overlapCount(left, right)
    local count = 0
    for username in pairs(left or {}) do
        if right and right[username] == true then count = count + 1 end
    end
    return count
end

local function resolveFactionKey(details, registry)
    if registry == nil then return "faction:" .. details.name end
    registry.factionNames = registry.factionNames or {}
    registry.factions = registry.factions or {}

    local key = registry.factionNames[details.name]
    if key == nil then
        local bestKey = nil
        local bestOverlap = 0
        for candidateKey, record in pairs(registry.factions) do
            if details.owner ~= "" and record.owner == details.owner then
                bestKey = candidateKey
                break
            end
            local overlap = overlapCount(details.members, record.members)
            if overlap > bestOverlap then
                bestKey = candidateKey
                bestOverlap = overlap
            end
        end
        -- One player moving between unrelated factions must not carry the old
        -- faction's progression with them. Member overlap is only a rename
        -- fallback when at least two members still identify the same group.
        if bestOverlap < 2 and (bestKey == nil
            or registry.factions[bestKey].owner ~= details.owner) then
            bestKey = nil
        end
        key = bestKey or ("faction:" .. details.name)
        registry.factionNames[details.name] = key
    end

    -- Old names remain as aliases. Updating the record lets a renamed faction
    -- retain its stable key through ownership transfers and later renames.
    registry.factions[key] = {
        name = details.name,
        owner = details.owner,
        members = details.members,
    }
    return key
end

local function localCoopOwner(player)
    if getSpecificPlayer == nil or getNumActivePlayers == nil then return nil end

    -- A listen server may have local split-screen survivors and remote clients at
    -- the same time. Only collapse the locally controlled characters into player
    -- 0's ownership; otherwise every remote client would inherit the host's group.
    local networked = (isClient and isClient()) or (isServer and isServer())
    local localPlayer = not networked
    if player ~= nil then pcall(function() localPlayer = player:isLocalPlayer() == true end) end
    if not localPlayer then return nil end

    local ok, count = pcall(getNumActivePlayers)
    if not ok or (tonumber(count) or 0) <= 1 then return nil end
    local primary = getSpecificPlayer(0)
    if primary == nil then return nil end
    local username = Util.accountUsername(primary)
    if username == "" then username = "singleplayer" end
    return username
end

-- This is intentionally a general ownership primitive rather than quest-only
-- logic. Upgrades, hideouts, and other persistent systems can adopt the same
-- group key when they are partitioned in the future. Passing the persistent
-- registry also preserves the key when a faction changes its display name.
function Groups.forPlayer(player, registry)
    local username = Util.accountUsername(player)
    -- Local split-screen is one cooperative household even though its characters
    -- are not members of a multiplayer Faction. Keep player 1's existing solo key
    -- so adding another controller cannot swap the UI to a fresh, unselected raid.
    local coopOwner = localCoopOwner(player)
    if coopOwner ~= nil then
        return {
            key = "player:" .. coopOwner,
            kind = "local-coop",
            name = coopOwner,
        }
    end

    local faction = playerFaction(player, username)
    if faction ~= nil then
        local details = factionDetails(faction)
        if details ~= nil then
            return {
                key = resolveFactionKey(details, registry),
                kind = "faction",
                name = details.name,
            }
        end
    end

    if username == "" then username = "singleplayer" end
    return {
        key = "player:" .. username,
        kind = "player",
        name = username,
    }
end

function Groups.same(left, right)
    return left ~= nil and right ~= nil and tostring(left.key) == tostring(right.key)
end

ExtractionMode.Groups = Groups
return Groups

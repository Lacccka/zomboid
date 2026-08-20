-- LCC controlled PoC for the B42.20 Bandits reconnect clothing bug.
-- B42 worn-item APIs require ItemBodyLocation objects, not string slot names.
--
-- v2 proved that persistent Bandits can restore real WornItems after reconnect.
-- The death-queue companion below keeps those same materialized items eligible
-- for corpse creation without re-enabling upstream's copy-creating clothing loop.
if isServer() then return end

local MARKER = "real-worn-reconnect-v2"
local DEATH_MARKER = "real-worn-death-queue-v1"
LCC_BANDITS_CLOTHING_RESTORE = MARKER
LCC_BANDITS_CLOTHING_DEATH_QUEUE = DEATH_MARKER

if type(Bandit) ~= "table" or type(Bandit.ApplyVisuals) ~= "function" then
    print("[LCC][BanditsClothingPoC][DISABLED] Bandit.ApplyVisuals unavailable")
    return
end
if type(Bandit.UpdateItemsToSpawnAtDeath) ~= "function" then
    print("[LCC][BanditsClothingPoC][DISABLED] Bandit.UpdateItemsToSpawnAtDeath unavailable")
    return
end
if type(BanditCompatibility) ~= "table" or type(BanditCompatibility.InstanceItem) ~= "function" then
    print("[LCC][BanditsClothingPoC][DISABLED] BanditCompatibility.InstanceItem unavailable")
    return
end

local originalApplyVisuals = Bandit.ApplyVisuals
local originalUpdateItemsToSpawnAtDeath = Bandit.UpdateItemsToSpawnAtDeath
local stateByBandit = setmetatable({}, { __mode = "k" })
local warned = {}

local function fullType(item)
    if not item then return nil end
    local ok, value = pcall(function() return item:getFullType() end)
    return ok and value and tostring(value) or nil
end

local function characterId(character, brain)
    if brain and brain.id ~= nil then return tostring(brain.id) end
    if character and BanditUtils and type(BanditUtils.GetCharacterID) == "function" then
        local ok, value = pcall(BanditUtils.GetCharacterID, character)
        if ok and value ~= nil then return tostring(value) end
    end
    return "nil"
end

local function warnOnce(key, message)
    if warned[key] then return end
    warned[key] = true
    print(message)
end

local function wornSize(bandit)
    local ok, worn = pcall(function() return bandit:getWornItems() end)
    if not ok or not worn then return -1 end
    local okSize, size = pcall(function() return worn:size() end)
    return okSize and size or -1
end

local function expectedClothingCount(brain)
    if not brain or type(brain.clothing) ~= "table" then return 0 end
    local count = 0
    for _ in pairs(brain.clothing) do count = count + 1 end
    return count
end

local function applyTint(item, brain, brainLocation)
    if not item or not brain or type(brain.tint) ~= "table" then return end
    local packed = brain.tint[brainLocation]
    if packed == nil or not BanditUtils or type(BanditUtils.dec2rgb) ~= "function" then return end
    pcall(function()
        local visual = item:getVisual()
        if not visual then return end
        local color = BanditUtils.dec2rgb(packed)
        visual:setTint(ImmutableColor.new(color.r, color.g, color.b, 1))
    end)
end

local function stateFor(bandit)
    local state = stateByBandit[bandit]
    if not state then
        state = {
            items = {},
            reportedRestore = false,
            conflicts = {},
            lastDeathQueueCount = -1,
        }
        stateByBandit[bandit] = state
    end
    return state
end

local function markRealItem(item)
    if not item then return end
    local md = item:getModData()
    if md then
        md.LCC_BanditsRealClothing = MARKER
        md.preserve = true
    end
end

local function createRealItem(itemType, brain, brainLocation)
    local item = BanditCompatibility.InstanceItem(itemType)
    if not item then return nil end
    markRealItem(item)
    applyTint(item, brain, brainLocation)
    return item
end

local function typedBodyLocation(item)
    if not item then return nil end
    local ok, location = pcall(function() return item:getBodyLocation() end)
    if not ok or location == nil then return nil end
    return location
end

local function ensureSlot(bandit, brain, state, brainLocation, itemType)
    if not brainLocation or not itemType then return 0, 0 end
    local cacheKey = tostring(brainLocation)
    local item = state.items[cacheKey]
    local created = false

    if not item or fullType(item) ~= tostring(itemType) then
        item = createRealItem(itemType, brain, brainLocation)
        if not item then return 0, 0 end
        state.items[cacheKey] = item
        created = true
    else
        markRealItem(item)
        applyTint(item, brain, brainLocation)
    end

    local location = typedBodyLocation(item)
    if not location then
        warnOnce("missing:" .. tostring(itemType), string.format(
            "[LCC][BanditsClothingPoC][BODY_LOCATION_MISSING] id=%s brainLocation=%s item=%s intervention=false",
            characterId(bandit, brain), tostring(brainLocation), tostring(itemType)
        ))
        return 0, created and 1 or 0
    end

    local okCurrent, current = pcall(function() return bandit:getWornItem(location) end)
    if not okCurrent then
        warnOnce("get:" .. tostring(itemType), string.format(
            "[LCC][BanditsClothingPoC][BODY_LOCATION_API_ERROR] operation=get item=%s location=%s intervention=false",
            tostring(itemType), tostring(location)
        ))
        return 0, created and 1 or 0
    end

    if current then
        if fullType(current) == tostring(itemType) then
            markRealItem(current)
            state.items[cacheKey] = current
            return 0, created and 1 or 0
        end
        if not state.conflicts[cacheKey] then
            state.conflicts[cacheKey] = true
            print(string.format(
                "[LCC][BanditsClothingPoC][SLOT_CONFLICT] id=%s brainLocation=%s typedLocation=%s expected=%s actual=%s intervention=false",
                characterId(bandit, brain), tostring(brainLocation), tostring(location), tostring(itemType), tostring(fullType(current) or "<unknown>")
            ))
        end
        return 0, created and 1 or 0
    end

    local okSet = pcall(function() bandit:setWornItem(location, item) end)
    if not okSet then
        warnOnce("set:" .. tostring(itemType), string.format(
            "[LCC][BanditsClothingPoC][BODY_LOCATION_API_ERROR] operation=set item=%s location=%s intervention=false",
            tostring(itemType), tostring(location)
        ))
        return 0, created and 1 or 0
    end

    return 1, created and 1 or 0
end

local function bagName(brain)
    if not brain or brain.bag == nil then return nil end
    if type(brain.bag) == "table" then return brain.bag.name end
    if type(brain.bag) == "string" then return brain.bag end
    return nil
end

local function ensureBag(bandit, brain, state)
    local itemType = bagName(brain)
    if not itemType then return 0 end
    local key = "__bag"
    local item = state.items[key]
    if not item or fullType(item) ~= tostring(itemType) then
        item = createRealItem(itemType, brain, nil)
        if not item then return 0 end
        state.items[key] = item
    else
        markRealItem(item)
    end

    local location = typedBodyLocation(item)
    if not location then
        warnOnce("bag:" .. tostring(itemType), string.format(
            "[LCC][BanditsClothingPoC][BAG_LOCATION_UNAVAILABLE] id=%s item=%s intervention=false",
            characterId(bandit, brain), tostring(itemType)
        ))
        return 0
    end

    local okCurrent, current = pcall(function() return bandit:getWornItem(location) end)
    if not okCurrent or current then
        if current and fullType(current) == tostring(itemType) then markRealItem(current) end
        return 0
    end
    local okSet = pcall(function() bandit:setWornItem(location, item) end)
    return okSet and 1 or 0
end

local function queueMaterializedWornItems(bandit, brain)
    if not bandit then return 0 end
    local state = stateFor(bandit)
    local worn = bandit:getWornItems()
    if not worn then return 0 end

    local queued = 0
    for i = 0, worn:size() - 1 do
        local wornEntry = worn:get(i)
        local item = wornEntry and wornEntry:getItem() or nil
        if item then
            local md = item:getModData()
            if md and md.LCC_BanditsRealClothing == MARKER then
                md.preserve = true
                md.LCC_BanditsDeathQueue = DEATH_MARKER
                bandit:addItemToSpawnAtDeath(item)
                queued = queued + 1
            end
        end
    end

    if queued > 0 and state.lastDeathQueueCount ~= queued then
        state.lastDeathQueueCount = queued
        print(string.format(
            "[LCC][BanditsClothingPoC][DEATH_QUEUE] marker=%s id=%s queuedRealWorn=%d currentWorn=%d expectedClothing=%d bag=%s",
            DEATH_MARKER,
            characterId(bandit, brain),
            queued,
            worn:size(),
            expectedClothingCount(brain),
            tostring(bagName(brain) or "<none>")
        ))
    end

    return queued
end

Bandit.UpdateItemsToSpawnAtDeath = function(bandit, brain)
    originalUpdateItemsToSpawnAtDeath(bandit, brain)
    local ok, err = pcall(queueMaterializedWornItems, bandit, brain)
    if not ok then
        warnOnce("death-queue-runtime", "[LCC][BanditsClothingPoC][DEATH_QUEUE_ERROR] " .. tostring(err))
    end
end

local function restoreRealWorn(bandit, brain)
    if not bandit or not brain or type(brain.clothing) ~= "table" then return end
    if not bandit:isAlive() then return end

    local state = stateFor(bandit)
    local beforeWorn = wornSize(bandit)
    local restored, created = 0, 0

    if BanditCompatibility.GetBodyLocationsOrdered then
        for _, brainLocation in pairs(BanditCompatibility.GetBodyLocationsOrdered()) do
            local itemType = brain.clothing[brainLocation]
            if itemType then
                local r, c = ensureSlot(bandit, brain, state, brainLocation, itemType)
                restored = restored + r
                created = created + c
            end
        end
    else
        for brainLocation, itemType in pairs(brain.clothing) do
            local r, c = ensureSlot(bandit, brain, state, brainLocation, itemType)
            restored = restored + r
            created = created + c
        end
    end

    restored = restored + ensureBag(bandit, brain, state)
    local afterWorn = wornSize(bandit)
    if restored > 0 then
        bandit:resetModelNextFrame()
        if not state.reportedRestore then
            state.reportedRestore = true
            print(string.format(
                "[LCC][BanditsClothingPoC][RESTORE] marker=%s id=%s beforeWorn=%d expectedClothing=%d restored=%d created=%d afterWorn=%d bag=%s",
                MARKER, characterId(bandit, brain), beforeWorn, expectedClothingCount(brain), restored, created, afterWorn, tostring(bagName(brain) or "<none>")
            ))
        end
    end

    -- Upstream ApplyVisuals queues death items before this PoC re-materializes
    -- WornItems. Rebuild the queue once more so the same real worn objects are
    -- appended after upstream has cleared/rebuilt its normal weapon/loot queue.
    local ok, err = pcall(Bandit.UpdateItemsToSpawnAtDeath, bandit, brain)
    if not ok then
        warnOnce("death-queue-refresh", "[LCC][BanditsClothingPoC][DEATH_QUEUE_REFRESH_ERROR] " .. tostring(err))
    end
end

Bandit.ApplyVisuals = function(bandit, brain)
    originalApplyVisuals(bandit, brain)
    local ok, err = pcall(restoreRealWorn, bandit, brain)
    if not ok then
        warnOnce("restore-runtime", "[LCC][BanditsClothingPoC][RESTORE_ERROR] " .. tostring(err))
    end
end

print(string.format(
    "[LCC][BanditsClothingPoC][BOOT] marker=%s deathMarker=%s mode=typed-ItemBodyLocation+same-object-death-queue inventoryAdd=false copyClothing=false",
    MARKER,
    DEATH_MARKER
))
